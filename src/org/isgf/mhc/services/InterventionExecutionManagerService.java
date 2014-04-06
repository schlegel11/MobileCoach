package org.isgf.mhc.services;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.memory.ReceivedMessage;
import org.isgf.mhc.model.persistent.DialogMessage;
import org.isgf.mhc.model.persistent.DialogOption;
import org.isgf.mhc.model.persistent.DialogStatus;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.MediaObject;
import org.isgf.mhc.model.persistent.MediaObjectParticipantShortURL;
import org.isgf.mhc.model.persistent.Participant;
import org.isgf.mhc.model.persistent.types.DialogMessageStatusTypes;
import org.isgf.mhc.model.persistent.types.DialogOptionTypes;
import org.isgf.mhc.services.internal.CommunicationManagerService;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.VariablesManagerService;
import org.isgf.mhc.services.threads.IncomingMessageWorker;
import org.isgf.mhc.services.threads.MasterRuleEvaluationWorker;
import org.isgf.mhc.services.threads.OutgoingMessageWorker;

/**
 * Cares for the orchestration of the {@link Intervention}s as well as all
 * related {@link ModelObject}s
 * 
 * @author Andreas Filler
 */
@Log4j2
public class InterventionExecutionManagerService {
	private static InterventionExecutionManagerService	instance	= null;

	private final DatabaseManagerService				databaseManagerService;
	private final FileStorageManagerService				fileStorageManagerService;
	private final VariablesManagerService				variablesManagerService;
	final CommunicationManagerService					communicationManagerService;

	private final IncomingMessageWorker					incomingMessageWorker;
	private final OutgoingMessageWorker					outgoingMessageWorker;
	private final MasterRuleEvaluationWorker			masterRuleEvaluationWorker;

	private InterventionExecutionManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final CommunicationManagerService communicationManagerService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.variablesManagerService = variablesManagerService;
		this.communicationManagerService = communicationManagerService;

		// Reset all messages which could not be sent the last times
		dialogMessagesResetStatusAfterRestart();

		outgoingMessageWorker = new OutgoingMessageWorker(this,
				communicationManagerService);
		outgoingMessageWorker.start();
		incomingMessageWorker = new IncomingMessageWorker(this,
				communicationManagerService);
		incomingMessageWorker.start();
		masterRuleEvaluationWorker = new MasterRuleEvaluationWorker(this);
		masterRuleEvaluationWorker.start();

		log.info("Started.");
	}

	public static InterventionExecutionManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final CommunicationManagerService communicationManagerService)
			throws Exception {
		if (instance == null) {
			instance = new InterventionExecutionManagerService(
					databaseManagerService, fileStorageManagerService,
					variablesManagerService, communicationManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.debug("Stopping master rule evaluation worker...");
		synchronized (masterRuleEvaluationWorker) {
			masterRuleEvaluationWorker.interrupt();
			masterRuleEvaluationWorker.join();
		}
		log.debug("Stopping incoming message worker...");
		synchronized (incomingMessageWorker) {
			incomingMessageWorker.interrupt();
			incomingMessageWorker.join();
		}
		log.debug("Stopping outgoing message worker...");
		synchronized (outgoingMessageWorker) {
			outgoingMessageWorker.interrupt();
			outgoingMessageWorker.join();
		}

		log.info("Stopped.");
	}

	/*
	 * Modification methods
	 */
	// System Unique Id
	@Synchronized
	public MediaObjectParticipantShortURL systemUniqueIdCreate(
			final DialogMessage relatedDialogMessage,
			final MediaObject relatedMediaObject) {

		val newestSystemUniqueId = databaseManagerService
				.findOneSortedModelObject(
						MediaObjectParticipantShortURL.class,
						Queries.ALL,
						Queries.MEDIA_OBJECT_PARTICIPANT_SHORT_URL__SORT_BY_SHORT_ID_DESC);

		final long nextShortId = newestSystemUniqueId == null ? 1
				: newestSystemUniqueId.getShortId() + 1;

		val newSystemUniqueId = new MediaObjectParticipantShortURL(nextShortId,
				relatedDialogMessage.getId(), relatedMediaObject.getId());

		databaseManagerService.saveModelObject(newSystemUniqueId);

		return newSystemUniqueId;
	}

	// Dialog Message
	@Synchronized
	public void dialogMessageCreate(final ObjectId participantId,
			final String message, final int hourToSendMessage,
			final int hoursUntilMessageIsHandledAsUnanswered,
			final boolean manuallySent,
			final ObjectId relatedMonitoringRuleForReplyRules,
			final ObjectId relatedMonitoringMessage) {
		val dialogMessage = new DialogMessage(participantId, 0,
				DialogMessageStatusTypes.PREPARED_FOR_SENDING, message, -1, -1,
				-1, -1, null, false, relatedMonitoringRuleForReplyRules,
				relatedMonitoringMessage, false, manuallySent);

		val highestOrderMessage = databaseManagerService
				.findOneSortedModelObject(DialogMessage.class,
						Queries.DIALOG_MESSAGE__BY_PARTICIPANT,
						Queries.DIALOG_MESSAGE__SORT_BY_ORDER_DESC,
						participantId);

		if (highestOrderMessage != null) {
			dialogMessage.setOrder(highestOrderMessage.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(dialogMessage);
	}

	@Synchronized
	public void dialogMessageCreateAsUnexpectedReceived(
			final ObjectId participantId, final ReceivedMessage receivedMessage) {
		val dialogMessage = new DialogMessage(participantId, 0,
				DialogMessageStatusTypes.RECEIVED_UNEXPECTEDLY, "", -1, -1, -1,
				receivedMessage.getReceivedTimestamp(),
				receivedMessage.getMessage(), true, null, null, false, false);

		val highestOrderMessage = databaseManagerService
				.findOneSortedModelObject(DialogMessage.class,
						Queries.DIALOG_MESSAGE__BY_PARTICIPANT,
						Queries.DIALOG_MESSAGE__SORT_BY_ORDER_DESC,
						participantId);

		if (highestOrderMessage != null) {
			dialogMessage.setOrder(highestOrderMessage.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(dialogMessage);
	}

	@SuppressWarnings("incomplete-switch")
	@Synchronized
	public void dialogMessageSetStatus(final ObjectId dialogMessageId,
			final DialogMessageStatusTypes newStatus,
			final long timeStampOfEvent, final String dataOfEvent) {
		val dialogMessage = databaseManagerService.getModelObjectById(
				DialogMessage.class, dialogMessageId);

		dialogMessage.setStatus(newStatus);

		switch (newStatus) {
			case SENT:
				dialogMessage.setSentTimestamp(timeStampOfEvent);
				break;
			case SENT_AND_ANSWERED_BY_PARTICIPANT:
				dialogMessage.setAnswerReceivedTimestamp(timeStampOfEvent);
				dialogMessage.setAnswerReceived(dataOfEvent);
				break;
		}

		databaseManagerService.saveModelObject(dialogMessage);
	}

	@Synchronized
	public void dialogMessageSetMediaContentViewed(
			final ObjectId dialogMessageId) {
		val dialogMessage = databaseManagerService.getModelObjectById(
				DialogMessage.class, dialogMessageId);

		dialogMessage.setMediaContentViewed(true);

		databaseManagerService.saveModelObject(dialogMessage);
	}

	// Dialog status
	@Synchronized
	private void dialogStatusSetInterventionFinished(
			final ObjectId participantId) {
		val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participantId);

		dialogStatus.setInterventionPerformed(true);
		dialogStatus.setInterventionPerformedTimestamp(System
				.currentTimeMillis());

		databaseManagerService.saveModelObject(dialogStatus);
	}

	/*
	 * Special methods
	 */
	@Synchronized
	private void dialogMessagesResetStatusAfterRestart() {
		log.debug("Resetting dialog message status after restart...");

		val pendingDialogMessages = databaseManagerService.findModelObjects(
				DialogMessage.class, Queries.DIALOG_MESSAGE__BY_STATUS,
				DialogMessageStatusTypes.SENDING);

		for (val pendingDialogMessage : pendingDialogMessages) {
			pendingDialogMessage
					.setStatus(DialogMessageStatusTypes.PREPARED_FOR_SENDING);

			databaseManagerService.saveModelObject(pendingDialogMessage);
		}
	}

	/**
	 * Cleanup method for the case of problems when trying to send to a
	 * participant
	 * 
	 * @param participantId
	 */
	@Synchronized
	public void deactivateMessagingForParticipantAndDeleteDialogMessages(
			final ObjectId participantId) {
		val dialogMessagesToDelete = databaseManagerService.findModelObjects(
				DialogMessage.class, Queries.DIALOG_MESSAGE__BY_PARTICIPANT,
				participantId);

		for (val dialogMessageToDelete : dialogMessagesToDelete) {
			databaseManagerService.deleteModelObject(dialogMessageToDelete);
		}

		val participant = databaseManagerService.getModelObjectById(
				Participant.class, participantId);

		databaseManagerService.deleteModelObject(participant);
	}

	/*
	 * Getter methods
	 */
	@Synchronized
	public MediaObjectParticipantShortURL getSystemUniqueId(final long shortId) {
		return databaseManagerService.findOneModelObject(
				MediaObjectParticipantShortURL.class,
				Queries.MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_SHORT_ID,
				shortId);
	}

	/**
	 * Returns a list of {@link DialogMessage}s that should be sent; Parameters
	 * therefore are:
	 * 
	 * - The message should have the status PREPARED_FOR_SENDING
	 * - The should be sent timestamp should be lower than the current time
	 * 
	 * @return
	 */
	@Synchronized
	public Iterable<DialogMessage> getDialogMessagesWaitingToBeSent() {
		val dialogMessagesWaitingToBeSend = databaseManagerService
				.findModelObjects(
						DialogMessage.class,
						Queries.DIALOG_MESSAGE__BY_STATUS_AND_SHOULD_BE_SENT_TIMESTAMP_LOWER,
						DialogMessageStatusTypes.PREPARED_FOR_SENDING,
						System.currentTimeMillis());

		return dialogMessagesWaitingToBeSend;
	}

	@Synchronized
	public DialogOption getDialogOptionByParticipantAndType(
			final ObjectId participantId,
			final DialogOptionTypes dialogOptionType) {
		val dialogOption = databaseManagerService.findOneModelObject(
				DialogOption.class,
				Queries.DIALOG_OPTION__BY_PARTICIPANT_AND_TYPE, participantId,
				dialogOptionType);

		return dialogOption;
	}

	@Synchronized
	public DialogOption getDialogOptionByTypeAndData(
			final DialogOptionTypes dialogOptionType,
			final String dialogOptionData) {
		val dialogOption = databaseManagerService.findOneModelObject(
				DialogOption.class, Queries.DIALOG_OPTION__BY_TYPE_AND_DATA,
				dialogOptionType, dialogOptionData);

		return dialogOption;
	}

	@Synchronized
	public DialogMessage getDialogMessageByParticipantAndStatus(
			final ObjectId participantId,
			final DialogMessageStatusTypes dialogMessageStatusType) {
		val dialogMessage = databaseManagerService.findOneSortedModelObject(
				DialogMessage.class,
				Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS,
				Queries.DIALOG_MESSAGE__SORT_BY_ORDER_DESC, participantId,
				dialogMessageStatusType);

		return dialogMessage;
	}
}
