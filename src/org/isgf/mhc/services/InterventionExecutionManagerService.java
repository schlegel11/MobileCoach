package org.isgf.mhc.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.ImplementationConstants;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.memory.ReceivedMessage;
import org.isgf.mhc.model.persistent.DialogMessage;
import org.isgf.mhc.model.persistent.DialogOption;
import org.isgf.mhc.model.persistent.DialogStatus;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.MediaObject;
import org.isgf.mhc.model.persistent.MediaObjectParticipantShortURL;
import org.isgf.mhc.model.persistent.MonitoringMessage;
import org.isgf.mhc.model.persistent.MonitoringMessageGroup;
import org.isgf.mhc.model.persistent.MonitoringRule;
import org.isgf.mhc.model.persistent.Participant;
import org.isgf.mhc.model.persistent.types.DialogMessageStatusTypes;
import org.isgf.mhc.model.persistent.types.DialogOptionTypes;
import org.isgf.mhc.services.internal.CommunicationManagerService;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.RecursiveAbstractMonitoringRulesResolver;
import org.isgf.mhc.services.internal.VariablesManagerService;
import org.isgf.mhc.services.threads.IncomingMessageWorker;
import org.isgf.mhc.services.threads.MonitoringShedulingWorker;
import org.isgf.mhc.services.threads.OutgoingMessageWorker;
import org.isgf.mhc.services.types.SystemVariables;
import org.isgf.mhc.tools.InternalDateTime;
import org.isgf.mhc.tools.StringHelpers;
import org.isgf.mhc.tools.VariableStringReplacer;
import org.isgf.mhc.ui.NotificationMessageException;

/**
 * Cares for the orchestration of the {@link Intervention}s as well as all
 * related {@link ModelObject}s
 * 
 * @author Andreas Filler
 */
@Log4j2
public class InterventionExecutionManagerService {
	private static InterventionExecutionManagerService	instance			= null;

	private static SimpleDateFormat						dayInWeekFormatter	= new SimpleDateFormat(
																					"u");

	private final String[]								acceptedStopWords;

	private final DatabaseManagerService				databaseManagerService;
	private final VariablesManagerService				variablesManagerService;
	final CommunicationManagerService					communicationManagerService;

	private final IncomingMessageWorker					incomingMessageWorker;
	private final OutgoingMessageWorker					outgoingMessageWorker;
	private final MonitoringShedulingWorker				monitoringShedulingWorker;

	private InterventionExecutionManagerService(
			final DatabaseManagerService databaseManagerService,
			final VariablesManagerService variablesManagerService,
			final CommunicationManagerService communicationManagerService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.variablesManagerService = variablesManagerService;
		this.communicationManagerService = communicationManagerService;

		// Remember stop words
		acceptedStopWords = Constants.getAcceptedStopWords();

		// Reset all messages which could not be sent the last times
		dialogMessagesResetStatusAfterRestart();

		outgoingMessageWorker = new OutgoingMessageWorker(this);
		outgoingMessageWorker.start();
		incomingMessageWorker = new IncomingMessageWorker(this,
				communicationManagerService);
		incomingMessageWorker.start();
		monitoringShedulingWorker = new MonitoringShedulingWorker(this);
		monitoringShedulingWorker.start();

		log.info("Started.");
	}

	public static InterventionExecutionManagerService start(
			final DatabaseManagerService databaseManagerService,
			final VariablesManagerService variablesManagerService,
			final CommunicationManagerService communicationManagerService)
			throws Exception {
		if (instance == null) {
			instance = new InterventionExecutionManagerService(
					databaseManagerService, variablesManagerService,
					communicationManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.debug("Stopping master rule evaluation worker...");
		synchronized (monitoringShedulingWorker) {
			monitoringShedulingWorker.interrupt();
			monitoringShedulingWorker.join();
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
	 * PUBLIC Modification methods
	 */
	// Intervention
	@Synchronized
	public void interventionSetStatus(final Intervention intervention,
			final boolean value) {
		intervention.setActive(value);

		databaseManagerService.saveModelObject(intervention);
	}

	@Synchronized
	public void interventionSetMonitoring(final Intervention intervention,
			final boolean value) {
		intervention.setMonitoringActive(value);

		databaseManagerService.saveModelObject(intervention);
	}

	// Participant
	@Synchronized
	public void participantsSwitchMonitoring(
			final List<Participant> participants) {
		for (val participant : participants) {

			if (participant.isMonitoringActive()) {
				participant.setMonitoringActive(false);
			} else {
				val dialogStatus = databaseManagerService.findOneModelObject(
						DialogStatus.class,
						Queries.DIALOG_STATUS__BY_PARTICIPANT,
						participant.getId());

				if (dialogStatus != null
						&& dialogStatus
								.isDataForMonitoringParticipationAvailable()) {
					participant.setMonitoringActive(true);
				}
			}

			databaseManagerService.saveModelObject(participant);
		}
	}

	// Dialog message
	@Synchronized
	private void dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
			final Participant participant, final String message,
			final boolean manuallySent, final long timestampToSendMessage,
			final MonitoringRule relatedMonitoringRule,
			final MonitoringMessage relatedMonitoringMessage,
			final boolean answerExpected) {
		log.debug("Create message and prepare for sending");
		val dialogMessage = new DialogMessage(participant.getId(), 0,
				DialogMessageStatusTypes.PREPARED_FOR_SENDING, message,
				timestampToSendMessage, -1, answerExpected, -1, -1, null, null,
				false, relatedMonitoringRule == null ? null
						: relatedMonitoringRule.getId(),
				relatedMonitoringMessage == null ? null
						: relatedMonitoringMessage.getId(), false, manuallySent);

		// Check linked media object
		MediaObject linkedMediaObject = null;
		if (relatedMonitoringMessage != null) {
			val monitoringMessage = databaseManagerService.getModelObjectById(
					MonitoringMessage.class,
					dialogMessage.getRelatedMonitoringMessage());

			if (monitoringMessage != null
					&& monitoringMessage.getLinkedMediaObject() != null) {
				linkedMediaObject = databaseManagerService.getModelObjectById(
						MediaObject.class,
						monitoringMessage.getLinkedMediaObject());
			}
		}

		// Determine order
		val highestOrderMessage = databaseManagerService
				.findOneSortedModelObject(DialogMessage.class,
						Queries.DIALOG_MESSAGE__BY_PARTICIPANT,
						Queries.DIALOG_MESSAGE__SORT_BY_ORDER_DESC,
						participant.getId());

		if (highestOrderMessage != null) {
			dialogMessage.setOrder(highestOrderMessage.getOrder() + 1);
		}

		// Special saving case for linked media object
		if (linkedMediaObject != null) {
			dialogMessage.setStatus(DialogMessageStatusTypes.IN_CREATION);
			databaseManagerService.saveModelObject(dialogMessage);

			val mediaObjectParticipantShortURL = mediaObjectParticipantShortURLCreate(
					dialogMessage, linkedMediaObject);

			val mediaObjectParticipantShortURLString = mediaObjectParticipantShortURL
					.calculateURL();

			log.debug("Integrating media object into message with URL {}",
					mediaObjectParticipantShortURLString);
			dialogMessage.setMessage(message + " "
					+ mediaObjectParticipantShortURLString);
			dialogMessage
					.setStatus(DialogMessageStatusTypes.PREPARED_FOR_SENDING);
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

	@Synchronized
	public void dialogMessageSetProblemSolved(final ObjectId dialogMessageId,
			final String newCleanedResult) throws NotificationMessageException {
		log.debug("Marking dialog message {} as problem solved");

		val dialogMessage = databaseManagerService.getModelObjectById(
				DialogMessage.class, dialogMessageId);

		if (dialogMessage.getStatus() == DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER) {
			dialogMessageStatusChangesAfterSending(dialogMessageId,
					DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
					dialogMessage.getAnswerReceivedTimestamp(),
					StringHelpers.cleanReceivedMessageString(newCleanedResult),
					dialogMessage.getAnswerReceivedRaw());
		} else if (dialogMessage.getStatus() == DialogMessageStatusTypes.RECEIVED_UNEXPECTEDLY) {
			dialogMessage.setAnswerNotAutomaticallyProcessable(false);
			databaseManagerService.saveModelObject(dialogMessage);
		} else {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__CASE_CANT_BE_SOLVED_ANYMORE);
		}
	}

	/**
	 * Handles states form "PREPARED_FOR_SENDING" to
	 * "SENT_AND_WAITING_FOR_ANSWER" or "SENT_AND_WAITING_FOR_ANSWER"
	 * 
	 * @param dialogMessageId
	 * @param newStatus
	 * @param timeStampOfEvent
	 */
	@Synchronized
	public void dialogMessageStatusChangesForSending(
			final ObjectId dialogMessageId,
			final DialogMessageStatusTypes newStatus,
			final long timeStampOfEvent) {
		val dialogMessage = databaseManagerService.getModelObjectById(
				DialogMessage.class, dialogMessageId);

		dialogMessage.setStatus(newStatus);

		// Adjust for sent
		if (newStatus == DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER) {
			if (dialogMessage.getRelatedMonitoringRuleForReplyRules() != null) {
				val monitoringRule = databaseManagerService.getModelObjectById(
						MonitoringRule.class,
						dialogMessage.getRelatedMonitoringRuleForReplyRules());

				if (monitoringRule != null) {
					final long isUnansweredAfterTimestamp = timeStampOfEvent
							+ monitoringRule
									.getHoursUntilMessageIsHandledAsUnanswered()
							* ImplementationConstants.HOURS_TO_TIME_IN_MILLIS_MULTIPLICATOR;

					dialogMessage
							.setIsUnansweredAfterTimestamp(isUnansweredAfterTimestamp);
				}
			}
			dialogMessage.setSentTimestamp(timeStampOfEvent);
		} else if (newStatus == DialogMessageStatusTypes.SENT_BUT_NOT_WAITING_FOR_ANSWER) {
			dialogMessage.setSentTimestamp(timeStampOfEvent);
		}

		databaseManagerService.saveModelObject(dialogMessage);
	}

	/*
	 * PRIVATE Modification methods
	 */
	// System Unique Id
	@Synchronized
	private MediaObjectParticipantShortURL mediaObjectParticipantShortURLCreate(
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
	private void dialogMessageCreateAsUnexpectedReceived(
			final ObjectId participantId, final ReceivedMessage receivedMessage) {
		val dialogMessage = new DialogMessage(participantId, 0,
				DialogMessageStatusTypes.RECEIVED_UNEXPECTEDLY, "", -1, -1,
				false, -1, receivedMessage.getReceivedTimestamp(),
				receivedMessage.getMessage(), receivedMessage.getMessage(),
				true, null, null, false, false);

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
	 * Handles states form "SENT_AND_ANSWERED_BY_PARTICIPANT" till end
	 * 
	 * @param dialogMessageId
	 * @param newStatus
	 * @param timeStampOfEvent
	 * @param cleanedReceivedMessage
	 * @param rawReceivedMessage
	 */
	@SuppressWarnings("incomplete-switch")
	@Synchronized
	private void dialogMessageStatusChangesAfterSending(
			final ObjectId dialogMessageId,
			final DialogMessageStatusTypes newStatus,
			final long timeStampOfEvent, final String cleanedReceivedMessage,
			final String rawReceivedMessage) {
		val dialogMessage = databaseManagerService.getModelObjectById(
				DialogMessage.class, dialogMessageId);

		dialogMessage.setStatus(newStatus);

		switch (newStatus) {
			case SENT_AND_WAITING_FOR_ANSWER:
				// same as answered, but mark as not correctly parsable
				dialogMessage.setAnswerNotAutomaticallyProcessable(true);
			case SENT_AND_ANSWERED_BY_PARTICIPANT:
				// setting timestamp and answer received
				if (newStatus == DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT) {
					dialogMessage.setAnswerNotAutomaticallyProcessable(false);
				}
				dialogMessage.setAnswerReceivedTimestamp(timeStampOfEvent);
				dialogMessage.setAnswerReceived(cleanedReceivedMessage);
				dialogMessage.setAnswerReceivedRaw(rawReceivedMessage);
				break;
			case SENT_AND_ANSWERED_AND_PROCESSED:
				// no changes necessary
				break;
			case SENT_AND_NOT_ANSWERED_AND_PROCESSED:
				// no changes necessary
				break;
		}

		databaseManagerService.saveModelObject(dialogMessage);
	}

	// Dialog status
	@Synchronized
	private void dialogStatusUpdate(final ObjectId dialogStatusId,
			final String dateIndex) {
		val dialogStatus = databaseManagerService.getModelObjectById(
				DialogStatus.class, dialogStatusId);

		dialogStatus.setDateIndexOfLastDailyMonitoringProcessing(dateIndex);
		dialogStatus.setMonitoringDaysParticipated(dialogStatus
				.getMonitoringDaysParticipated() + 1);

		databaseManagerService.saveModelObject(dialogStatus);
	}

	@Synchronized
	private void dialogStatusSetMonitoringFinished(final ObjectId participantId) {
		val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participantId);

		dialogStatus.setMonitoringPerformed(true);
		dialogStatus.setMonitoringPerformedTimestamp(InternalDateTime
				.currentTimeMillis());

		databaseManagerService.saveModelObject(dialogStatus);
	}

	/*
	 * MAIN methods -
	 * 
	 * (the following two methods contain the elemental parts of
	 * the monitoring process)
	 */
	@Synchronized
	public void reactOnAnsweredAndUnansweredMessages(
			final boolean reactOnAnsweredMessages) {
		log.debug(
				"Create a list of all relevant participants for handling {} messages",
				reactOnAnsweredMessages ? "answered" : "unanswered");
		val participants = getAllParticipantsRelevantForAnsweredInTimeChecksAndMonitoringSheduling();

		for (val participant : participants) {

			// Get relevant messages of participant
			Iterable<DialogMessage> dialogMessages;
			if (reactOnAnsweredMessages) {
				dialogMessages = getDialogMessagesOfParticipantAnsweredByParticipant(participant
						.getId());
			} else {
				dialogMessages = getDialogMessagesOfParticipantUnansweredByParticipant(participant
						.getId());
			}

			// Handle messages
			for (val dialogMessage : dialogMessages) {
				// Handle storing of message reply (the text sent) by
				// participant if
				// message is answered by
				// participant and not manually sent
				if (reactOnAnsweredMessages
						&& dialogMessage.getRelatedMonitoringMessage() != null) {
					log.debug("Managing message reply (because the message is answered and has a reference to a monitoring message)");

					val relatedMonitoringMessage = databaseManagerService
							.getModelObjectById(MonitoringMessage.class,
									dialogMessage.getRelatedMonitoringMessage());

					// Store value to variable (which is only relevant if a
					// reply is expected = related monitoring message is
					// available
					if (relatedMonitoringMessage != null) {
						val cleanedMessageValue = dialogMessage
								.getAnswerReceived();

						log.debug(
								"Store value '{}' (cleaned: '{}') of message to '{}' for participant {}",
								dialogMessage.getAnswerReceived(),
								cleanedMessageValue, relatedMonitoringMessage
										.getStoreValueToVariableWithName(),
								participant.getId());
						try {
							if (relatedMonitoringMessage
									.getStoreValueToVariableWithName() != null
									&& !relatedMonitoringMessage
											.getStoreValueToVariableWithName()
											.equals("")) {
								variablesManagerService
										.writeVariableValueOfParticipant(
												participant.getId(),
												relatedMonitoringMessage
														.getStoreValueToVariableWithName(),
												cleanedMessageValue);
							}
							variablesManagerService
									.writeVariableValueOfParticipant(
											participant.getId(),
											SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES.participantMessageReply
													.toVariableName(),
											cleanedMessageValue, true);
						} catch (final Exception e) {
							log.error(
									"Could not store value '{}' of message to '{}' for participant {}: {}",
									dialogMessage.getAnswerReceived(),
									relatedMonitoringMessage
											.getStoreValueToVariableWithName(),
									participant.getId(), e.getMessage());
						}
					}
				}

				// Set new message status
				if (reactOnAnsweredMessages) {
					dialogMessageStatusChangesAfterSending(
							dialogMessage.getId(),
							DialogMessageStatusTypes.SENT_AND_ANSWERED_AND_PROCESSED,
							InternalDateTime.currentTimeMillis(), null, null);
				} else {
					dialogMessageStatusChangesAfterSending(
							dialogMessage.getId(),
							DialogMessageStatusTypes.SENT_AND_NOT_ANSWERED_AND_PROCESSED,
							InternalDateTime.currentTimeMillis(), null, null);
				}

				// Handle rule actions if rule was not sent manually or
				// based on reply rules
				if (dialogMessage.getRelatedMonitoringRuleForReplyRules() != null) {
					log.debug("Caring for reply rules resolving");

					// Resolve rules
					RecursiveAbstractMonitoringRulesResolver recursiveRuleResolver;
					try {
						recursiveRuleResolver = new RecursiveAbstractMonitoringRulesResolver(
								databaseManagerService,
								variablesManagerService,
								participant,
								false,
								dialogMessage.getRelatedMonitoringMessage(),
								dialogMessage
										.getRelatedMonitoringRuleForReplyRules(),
								reactOnAnsweredMessages);

						recursiveRuleResolver.resolve();
					} catch (final Exception e) {
						log.error(
								"Could not resolve reply rules for participant {}: {}",
								participant.getId(), e.getMessage());
						continue;
					}

					for (val messageToSendTask : recursiveRuleResolver
							.getMessageSendingResultForMonitoringReplyRules()) {
						if (messageToSendTask.getMessageTextToSend() != null) {
							log.debug("Preparing reply message for sending for participant");

							val monitoringMessage = messageToSendTask
									.getMonitoringMessageToSend();
							val messageTextToSend = messageToSendTask
									.getMessageTextToSend();

							// Prepare message for sending
							dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
									participant, messageTextToSend, false,
									InternalDateTime.currentTimeMillis(), null,
									monitoringMessage, false);
						}
					}
				}
			}
		}
	}

	@Synchronized
	public void scheduleMessagesForSending() {
		log.debug("Create a list of all relevant participants for sheduling of monitoring messages");
		val participants = getAllParticipantsRelevantForAnsweredInTimeChecksAndMonitoringSheduling();

		val dateIndex = StringHelpers.createDailyUniqueIndex();
		val dateToday = new Date(InternalDateTime.currentTimeMillis());
		val todayDayIndex = Integer.parseInt(dayInWeekFormatter
				.format(dateToday));
		for (val participant : participants) {

			// Check if participant has already been scheduled today
			val dialogStatus = getDialogStatusByParticipant(participant.getId());

			// Only start interventions on Monday
			if (dialogStatus != null
					&& dialogStatus.getMonitoringDaysParticipated() == 0
					&& todayDayIndex != 1) {
				log.debug(
						"Participant {} has not been scheduled at all! Wait until next monday to start with sheduling...",
						participant.getId());
				continue;
			}

			if (dialogStatus != null
					&& !dialogStatus
							.getDateIndexOfLastDailyMonitoringProcessing()
							.equals(dateIndex)) {
				log.debug(
						"Participant {} has not been scheduled today! Start sheduling...",
						participant.getId());

				// Resolve rules
				RecursiveAbstractMonitoringRulesResolver recursiveRuleResolver;
				try {
					recursiveRuleResolver = new RecursiveAbstractMonitoringRulesResolver(
							databaseManagerService, variablesManagerService,
							participant, true, null, null, false);

					recursiveRuleResolver.resolve();
				} catch (final Exception e) {
					log.error("Could not resolve rules for participant {}: {}",
							participant.getId(), e.getMessage());
					continue;
				}

				val finishIntervention = recursiveRuleResolver
						.isInterventionFinishedForParticipantAfterThisResolving();

				if (finishIntervention) {
					log.debug("Finishing intervention for participant");
					dialogStatusSetMonitoringFinished(participant.getId());
				} else {
					for (val messageToSendTask : recursiveRuleResolver
							.getMessageSendingResultForMonitoringRules()) {
						if (messageToSendTask.getMessageTextToSend() != null) {

							log.debug("Preparing message for sending for participant");

							val monitoringRule = messageToSendTask
									.getMonitoringRuleThatCausedMessageSending();
							val monitoringMessage = messageToSendTask
									.getMonitoringMessageToSend();
							val monitoringMessageExpectsAnswer = messageToSendTask
									.isMonitoringRuleExpectsAnswer();
							val messageTextToSend = messageToSendTask
									.getMessageTextToSend();

							// Calculate time to send message
							final int hourToSendMessage = monitoringRule
									.getHourToSendMessage();
							final Calendar timeToSendMessage = Calendar
									.getInstance();
							timeToSendMessage.setTimeInMillis(InternalDateTime
									.currentTimeMillis());
							timeToSendMessage.set(Calendar.HOUR_OF_DAY,
									hourToSendMessage);
							timeToSendMessage.set(Calendar.MINUTE, 0);
							timeToSendMessage.set(Calendar.SECOND, 0);
							timeToSendMessage.set(Calendar.MILLISECOND, 0);

							// Prepare message for sending
							dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
									participant, messageTextToSend, false,
									timeToSendMessage.getTimeInMillis(),
									monitoringRule, monitoringMessage,
									monitoringMessageExpectsAnswer);
						}
					}

					// Update status and status values
					dialogStatusUpdate(dialogStatus.getId(), dateIndex);
				}
			}
		}
	}

	@Synchronized
	public void handleReceivedMessage(final ReceivedMessage receivedMessage) {
		val dialogOption = getDialogOptionByTypeAndDataOfActiveInterventions(
				communicationManagerService.getSupportedDialogOptionType(),
				receivedMessage.getSender());

		if (dialogOption == null) {
			log.warn(
					"The received message with sender number '{}' does not fit to any participant of an active intervention, skip it",
					receivedMessage.getSender());
			return;
		}

		// Check if received messages is a "stop"-message
		for (val stopWord : acceptedStopWords) {
			if (StringHelpers.cleanReceivedMessageString(
					receivedMessage.getMessage()).equals(stopWord)) {
				log.debug("Received stop message by participant {}",
						dialogOption.getParticipant());

				// FIXME Emergency solution for first intervention
				try {
					variablesManagerService
							.writeVariableValueOfParticipant(
									dialogOption.getParticipant(),
									ImplementationConstants.VARIABLE_DEFINING_PARTICIPATION_IN_MOBILE_COACH_EXTRA,
									"0");
				} catch (final Exception e) {
					log.warn(
							"Caution: Error when performing MobileCoach+ fix: {}",
							e.getMessage());
				}
				/*
				 * dialogMessageCreateAsUnexpectedReceived(
				 * dialogOption.getParticipant(), receivedMessage);
				 * 
				 * dialogStatusSetMonitoringFinished(dialogOption.getParticipant(
				 * ));
				 */

				return;
			}
		}

		val dialogMessage = getDialogMessageOfParticipantWaitingForAnswer(
				dialogOption.getParticipant(),
				receivedMessage.getReceivedTimestamp());

		val cleanedMessageValue = StringHelpers
				.cleanReceivedMessageString(receivedMessage.getMessage());

		if (dialogMessage == null) {
			log.debug(
					"Received an unexpected SMS from '{}', store it and mark it as unexpected",
					receivedMessage.getSender());
			dialogMessageCreateAsUnexpectedReceived(
					dialogOption.getParticipant(), receivedMessage);

			return;
		} else {
			// Check if result is in general automatically
			// processable

			val relatedMonitoringMessage = databaseManagerService
					.getModelObjectById(MonitoringMessage.class,
							dialogMessage.getRelatedMonitoringMessage());

			val relatedMonitoringMessageGroup = databaseManagerService
					.getModelObjectById(MonitoringMessageGroup.class,
							relatedMonitoringMessage
									.getMonitoringMessageGroup());
			if (relatedMonitoringMessageGroup.getValidationExpression() != null
					&& !cleanedMessageValue
							.matches(relatedMonitoringMessageGroup
									.getValidationExpression())) {

				dialogMessageStatusChangesAfterSending(dialogMessage.getId(),
						DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER,
						receivedMessage.getReceivedTimestamp(),
						cleanedMessageValue, receivedMessage.getMessage());

				return;
			}
		}

		dialogMessageStatusChangesAfterSending(dialogMessage.getId(),
				DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
				receivedMessage.getReceivedTimestamp(), cleanedMessageValue,
				receivedMessage.getMessage());
	}

	@Synchronized
	public void handleOutgoingMessages() {
		final val dialogMessagesToSend = getDialogMessagesWaitingToBeSentOfActiveInterventions();
		for (val dialogMessageToSend : dialogMessagesToSend) {
			try {

				val dialogOption = getDialogOptionByParticipantAndType(
						dialogMessageToSend.getParticipant(),
						communicationManagerService
								.getSupportedDialogOptionType());

				if (dialogOption != null) {
					log.debug("Sending prepared message to {}",
							dialogOption.getData());
					communicationManagerService.sendMessage(dialogOption,
							dialogMessageToSend.getId(),
							dialogMessageToSend.getMessage(),
							dialogMessageToSend.isMessageExpectsAnswer());
				} else {
					log.error("Could not send prepared message, because there was no valid dialog option to send message to participant; solution: deactive messaging for participant and removing current dialog message");

					try {
						deactivateMessagingForParticipantAndDeleteDialogMessages(dialogMessageToSend
								.getParticipant());
						log.debug("Cleanup sucessful");
					} catch (final Exception e) {
						log.error("Cleanup not sucessful: {}", e.getMessage());
					}
				}
			} catch (final Exception e) {
				log.error("Could not send prepared message: {}", e.getMessage());
			}
		}
	}

	/**
	 * Cleanup method for the case of problems when trying to send to a
	 * participant
	 * 
	 * @param participantId
	 */
	@Synchronized
	private void deactivateMessagingForParticipantAndDeleteDialogMessages(
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
	 * PUBLIC Getter methods
	 */
	@Synchronized
	public MediaObjectParticipantShortURL getMediaObjectParticipantShortURLByShortId(
			final long shortId) {
		return databaseManagerService.findOneModelObject(
				MediaObjectParticipantShortURL.class,
				Queries.MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_SHORT_ID,
				shortId);
	}

	/**
	 * Sends a manual message
	 * 
	 * @param participant
	 * @param messageWithPlaceholders
	 */
	@Synchronized
	public void sendManualMessage(final Participant participant,
			final String messageWithPlaceholders) {
		val variablesWithValues = variablesManagerService
				.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		val messageToSend = VariableStringReplacer
				.findVariablesAndReplaceWithTextValues(messageWithPlaceholders,
						variablesWithValues.values(), "");
		dialogMessageCreateManuallyOrByRulesIncludingMediaObject(participant,
				messageToSend, true, InternalDateTime.currentTimeMillis(),
				null, null, false);
	}

	/*
	 * PRIVATE Getter methods
	 */
	/**
	 * Returns a list of {@link DialogMessage}s that should be sent; Parameters
	 * therefore are:
	 * 
	 * - the belonging intervention is active
	 * - the belonging intervention monitoring is active
	 * - the participant has monitoring active
	 * - the participant finished screening survey
	 * - the participant not finished monitoring
	 * - the message should have the status PREPARED_FOR_SENDING
	 * - the should be sent timestamp should be lower than the current time
	 * 
	 * @return
	 */
	@Synchronized
	private List<DialogMessage> getDialogMessagesWaitingToBeSentOfActiveInterventions() {
		val dialogMessagesWaitingToBeSend = new ArrayList<DialogMessage>();

		for (val intervention : databaseManagerService.findModelObjects(
				Intervention.class,
				Queries.INTERVENTION__ACTIVE_TRUE_MONITORING_ACTIVE_TRUE)) {
			for (val participant : databaseManagerService
					.findModelObjects(
							Participant.class,
							Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
							intervention.getId())) {
				if (participant != null) {
					for (val dialogStatus : databaseManagerService
							.findModelObjects(DialogStatus.class,
									Queries.DIALOG_STATUS__BY_PARTICIPANT,
									participant.getId())) {
						if (dialogStatus != null
								&& dialogStatus.isScreeningSurveyPerformed()
								&& !dialogStatus.isMonitoringPerformed()) {

							val dialogMessagesWaitingToBeSendOfParticipant = databaseManagerService
									.findModelObjects(
											DialogMessage.class,
											Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_SHOULD_BE_SENT_TIMESTAMP_LOWER,
											participant.getId(),
											DialogMessageStatusTypes.PREPARED_FOR_SENDING,
											InternalDateTime
													.currentTimeMillis());

							CollectionUtils.addAll(
									dialogMessagesWaitingToBeSend,
									dialogMessagesWaitingToBeSendOfParticipant
											.iterator());
						}
					}
				}
			}
		}

		return dialogMessagesWaitingToBeSend;
	}

	@Synchronized
	private Iterable<DialogMessage> getDialogMessagesOfParticipantUnansweredByParticipant(
			final ObjectId participantId) {
		val dialogMessages = databaseManagerService
				.findSortedModelObjects(
						DialogMessage.class,
						Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_UNANSWERED_AFTER_TIMESTAMP_LOWER,
						Queries.DIALOG_MESSAGE__SORT_BY_ORDER_DESC,
						participantId,
						DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER,
						InternalDateTime.currentTimeMillis());

		return dialogMessages;
	}

	@Synchronized
	private Iterable<DialogMessage> getDialogMessagesOfParticipantAnsweredByParticipant(
			final ObjectId participantId) {
		val dialogMessages = databaseManagerService.findSortedModelObjects(
				DialogMessage.class,
				Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS,
				Queries.DIALOG_MESSAGE__SORT_BY_ORDER_DESC, participantId,
				DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT);

		return dialogMessages;
	}

	@Synchronized
	private DialogMessage getDialogMessageOfParticipantWaitingForAnswer(
			final ObjectId participantId,
			final long latestTimestampAnswerIsAccepted) {
		val dialogMessage = databaseManagerService
				.findOneSortedModelObject(
						DialogMessage.class,
						Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_NOT_AUTOMATICALLY_PROCESSABLE_AND_UNANSWERED_AFTER_TIMESTAMP_HIGHER,
						Queries.DIALOG_MESSAGE__SORT_BY_ORDER_DESC,
						participantId,
						DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER,
						false, latestTimestampAnswerIsAccepted);

		return dialogMessage;
	}

	@Synchronized
	private DialogOption getDialogOptionByTypeAndDataOfActiveInterventions(
			final DialogOptionTypes dialogOptionType,
			final String dialogOptionData) {
		val dialogOptions = databaseManagerService.findModelObjects(
				DialogOption.class, Queries.DIALOG_OPTION__BY_TYPE_AND_DATA,
				dialogOptionType, dialogOptionData);

		long highestCreatedTimestamp = 0;
		DialogOption appropriateDialogOption = null;

		for (val dialogOption : dialogOptions) {
			if (dialogOption == null) {
				continue;
			}

			val participant = databaseManagerService.getModelObjectById(
					Participant.class, dialogOption.getParticipant());

			if (participant != null) {
				val intervention = databaseManagerService.getModelObjectById(
						Intervention.class, participant.getIntervention());

				if (intervention != null) {
					if (intervention.isActive()) {
						if (participant.getCreatedTimestamp() > highestCreatedTimestamp) {
							highestCreatedTimestamp = participant
									.getCreatedTimestamp();
							appropriateDialogOption = dialogOption;
							continue;
						}
					} else {
						continue;
					}
				} else {
					continue;
				}
			} else {
				continue;
			}
		}

		return appropriateDialogOption;
	}

	@Synchronized
	private DialogOption getDialogOptionByParticipantAndType(
			final ObjectId participantId,
			final DialogOptionTypes dialogOptionType) {
		val dialogOption = databaseManagerService.findOneModelObject(
				DialogOption.class,
				Queries.DIALOG_OPTION__BY_PARTICIPANT_AND_TYPE, participantId,
				dialogOptionType);

		return dialogOption;
	}

	@Synchronized
	private DialogStatus getDialogStatusByParticipant(
			final ObjectId participantId) {
		val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participantId);

		return dialogStatus;
	}

	/**
	 * Returns a list of {@link Participant}s that are relevant for monitoring;
	 * Parameters therefore are:
	 * 
	 * - the belonging intervention is active
	 * - the belonging intervention monitoring is active
	 * - the participant has monitoring active
	 * - the participant finished screening survey
	 * - the participant not finished monitoring
	 * 
	 * @return
	 */
	@Synchronized
	private List<Participant> getAllParticipantsRelevantForAnsweredInTimeChecksAndMonitoringSheduling() {
		val relevantParticipants = new ArrayList<Participant>();

		for (val intervention : databaseManagerService.findModelObjects(
				Intervention.class,
				Queries.INTERVENTION__ACTIVE_TRUE_MONITORING_ACTIVE_TRUE)) {
			for (val participant : databaseManagerService
					.findModelObjects(
							Participant.class,
							Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
							intervention.getId())) {
				if (participant != null) {
					for (val dialogStatus : databaseManagerService
							.findModelObjects(DialogStatus.class,
									Queries.DIALOG_STATUS__BY_PARTICIPANT,
									participant.getId())) {
						if (dialogStatus != null
								&& dialogStatus
										.isDataForMonitoringParticipationAvailable()
								&& !dialogStatus.isMonitoringPerformed()) {
							relevantParticipants.add(participant);
						}
					}
				}
			}
		}

		return relevantParticipants;
	}

	@Synchronized
	public boolean participantAdjustVariableValue(final ObjectId participantId,
			final String variableName, final String variableValue) {
		val participant = databaseManagerService.getModelObjectById(
				Participant.class, participantId);

		try {
			variablesManagerService.writeVariableValueOfParticipant(
					participant.getId(), variableName, variableValue);
		} catch (final Exception e) {
			return false;
		}

		return true;
	}
}
