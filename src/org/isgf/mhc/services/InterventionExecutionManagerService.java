package org.isgf.mhc.services;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.persistent.DialogMessage;
import org.isgf.mhc.model.persistent.MediaObject;
import org.isgf.mhc.model.persistent.SystemUniqueId;
import org.isgf.mhc.model.persistent.types.DialogMessageStatusTypes;
import org.isgf.mhc.services.internal.CommunicationManagerService;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.VariablesManagerService;
import org.isgf.mhc.services.threads.IncomingMessageWorker;
import org.isgf.mhc.services.threads.MasterRuleEvaluationWorker;
import org.isgf.mhc.services.threads.OutgoingMessageWorker;

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

		outgoingMessageWorker = new OutgoingMessageWorker(
				communicationManagerService);
		outgoingMessageWorker.start();
		incomingMessageWorker = new IncomingMessageWorker(
				communicationManagerService);
		incomingMessageWorker.start();
		masterRuleEvaluationWorker = new MasterRuleEvaluationWorker();
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
	public SystemUniqueId systemUniqueIdCreate(
			final DialogMessage relatedDialogMessage,
			final MediaObject relatedMediaObject) {

		val newestSystemUniqueId = databaseManagerService
				.findOneSortedModelObject(SystemUniqueId.class, Queries.ALL,
						Queries.SYSTEM_UNIQUE_ID__SORT_BY_SHORT_ID_DESC);

		final long nextShortId = newestSystemUniqueId == null ? 1
				: newestSystemUniqueId.getShortId() + 1;

		val newSystemUniqueId = new SystemUniqueId(nextShortId,
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
			final ObjectId relatedMonitoringRuleForReplyRules) {
		val dialogMessage = new DialogMessage(participantId, 0,
				DialogMessageStatusTypes.PREPARED_FOR_SENDING, message, -1, -1,
				-1, -1, null, false, relatedMonitoringRuleForReplyRules, false,
				manuallySent);

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
			final long timeStampOfEvent) {
		val dialogMessage = databaseManagerService.getModelObjectById(
				DialogMessage.class, dialogMessageId);

		dialogMessage.setStatus(newStatus);

		switch (newStatus) {
			case SENT:
				dialogMessage.setSentTimestamp(timeStampOfEvent);
				break;
			case SENT_AND_ANSWERED_BY_PARTICIPANT:
				dialogMessage.setAnswerReceivedTimestamp(timeStampOfEvent);
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

	/*
	 * Getter methods
	 */
	public SystemUniqueId getSystemUniqueId(final long shortId) {
		return databaseManagerService.findOneModelObject(SystemUniqueId.class,
				Queries.SYSTEM_UNIQUE_ID__BY_SHORT_ID, shortId);
	}
}
