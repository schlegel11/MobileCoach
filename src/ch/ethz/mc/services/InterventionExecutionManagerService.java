package ch.ethz.mc.services;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Pattern;

import lombok.Cleanup;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.memory.DialogMessageWithSenderIdentification;
import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MediaObject;
import ch.ethz.mc.model.persistent.MediaObjectParticipantShortURL;
import ch.ethz.mc.model.persistent.MonitoringMessage;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ParticipantVariableWithValue;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.services.internal.CommunicationManagerService;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.RecursiveAbstractMonitoringRulesResolver;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.services.threads.IncomingMessageWorker;
import ch.ethz.mc.services.threads.MonitoringSchedulingWorker;
import ch.ethz.mc.services.threads.OutgoingMessageWorker;
import ch.ethz.mc.services.types.SystemVariables;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.tools.VariableStringReplacer;
import ch.ethz.mc.ui.NotificationMessageException;

/**
 * Cares for the orchestration of the {@link Intervention}s as well as all
 * related {@link ModelObject}s
 *
 * @author Andreas Filler
 */
@Log4j2
public class InterventionExecutionManagerService {
	private final Object								$lock;

	private static InterventionExecutionManagerService	instance			= null;

	private static SimpleDateFormat						dayInWeekFormatter	= new SimpleDateFormat(
																					"u");
	private final String[]								acceptedStopWords;

	private final DatabaseManagerService				databaseManagerService;
	private final VariablesManagerService				variablesManagerService;
	final CommunicationManagerService					communicationManagerService;

	final InterventionAdministrationManagerService		interventionAdministrationManagerService;
	final SurveyExecutionManagerService					surveyExecutionManagerService;

	private final IncomingMessageWorker					incomingMessageWorker;
	private final OutgoingMessageWorker					outgoingMessageWorker;
	private final MonitoringSchedulingWorker			monitoringSchedulingWorker;

	private InterventionExecutionManagerService(
			final DatabaseManagerService databaseManagerService,
			final VariablesManagerService variablesManagerService,
			final CommunicationManagerService communicationManagerService,
			final InterventionAdministrationManagerService interventionAdministrationManagerService,
			final SurveyExecutionManagerService surveyExecutionManagerService)
			throws Exception {
		$lock = MC.getInstance();

		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.variablesManagerService = variablesManagerService;
		this.communicationManagerService = communicationManagerService;
		this.interventionAdministrationManagerService = interventionAdministrationManagerService;
		this.surveyExecutionManagerService = surveyExecutionManagerService;

		// Remember stop words
		acceptedStopWords = Constants.getAcceptedStopWords();

		// Reset all messages which could not be sent the last times
		dialogMessagesResetStatusAfterRestart();

		outgoingMessageWorker = new OutgoingMessageWorker(this);
		outgoingMessageWorker.start();
		incomingMessageWorker = new IncomingMessageWorker(this,
				communicationManagerService);
		incomingMessageWorker.start();
		monitoringSchedulingWorker = new MonitoringSchedulingWorker(this,
				surveyExecutionManagerService);
		monitoringSchedulingWorker.start();

		log.info("Started.");
	}

	public static InterventionExecutionManagerService start(
			final DatabaseManagerService databaseManagerService,
			final VariablesManagerService variablesManagerService,
			final CommunicationManagerService communicationManagerService,
			final InterventionAdministrationManagerService interventionAdministrationManagerService,
			final SurveyExecutionManagerService screeningSurveyExecutionManagerService)
			throws Exception {
		if (instance == null) {
			instance = new InterventionExecutionManagerService(
					databaseManagerService, variablesManagerService,
					communicationManagerService,
					interventionAdministrationManagerService,
					screeningSurveyExecutionManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.debug("Stopping master rule evaluation worker...");
		synchronized (monitoringSchedulingWorker) {
			monitoringSchedulingWorker.interrupt();
			monitoringSchedulingWorker.join();
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
	public List<Participant> participantsSwitchMonitoring(
			final List<Participant> participants) {
		final List<Participant> adjustedParticipants = new ArrayList<Participant>();

		for (Participant participant : participants) {
			// Ensure to have the latest version of the participant before
			// changing it
			participant = databaseManagerService.getModelObjectById(
					Participant.class, participant.getId());
			adjustedParticipants.add(participant);

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

		return adjustedParticipants;
	}

	// Dialog message
	@Synchronized
	private void dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
			final Participant participant, String message,
			final boolean manuallySent, final long timestampToSendMessage,
			final MonitoringRule relatedMonitoringRule,
			final MonitoringMessage relatedMonitoringMessage,
			final boolean supervisorMessage, final boolean answerExpected) {
		log.debug("Create message and prepare for sending");
		val dialogMessage = new DialogMessage(participant.getId(), 0,
				DialogMessageStatusTypes.PREPARED_FOR_SENDING, message,
				timestampToSendMessage, -1, supervisorMessage, answerExpected,
				-1, -1, null, null, false, relatedMonitoringRule == null ? null
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

		// Check linked intermediate survey
		ScreeningSurvey linkedIntermediateSurvey = null;
		if (relatedMonitoringMessage != null) {
			val monitoringMessage = databaseManagerService.getModelObjectById(
					MonitoringMessage.class,
					dialogMessage.getRelatedMonitoringMessage());

			if (monitoringMessage != null
					&& monitoringMessage.getLinkedIntermediateSurvey() != null) {
				linkedIntermediateSurvey = databaseManagerService
						.getModelObjectById(ScreeningSurvey.class,
								monitoringMessage.getLinkedIntermediateSurvey());
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

		// Special saving case for linked media object or intermediate survey
		if (linkedMediaObject != null || linkedIntermediateSurvey != null) {
			dialogMessage.setStatus(DialogMessageStatusTypes.IN_CREATION);
			databaseManagerService.saveModelObject(dialogMessage);

			String URLsToAdd = "";

			// Integrate media object URL
			if (linkedMediaObject != null) {
				val mediaObjectParticipantShortURL = mediaObjectParticipantShortURLEnsure(
						dialogMessage, linkedMediaObject);

				val mediaObjectParticipantShortURLString = mediaObjectParticipantShortURL
						.calculateURL();
				log.debug("Integrating media object into message with URL {}",
						mediaObjectParticipantShortURLString);

				if (message
						.contains(ImplementationConstants.PLACEHOLDER_LINKED_MEDIA_OBJECT)) {
					message = message
							.replace(
									ImplementationConstants.PLACEHOLDER_LINKED_MEDIA_OBJECT,
									mediaObjectParticipantShortURLString);
				} else {
					URLsToAdd += " " + mediaObjectParticipantShortURLString;
				}
			}

			// Integrate intermediate survey URL
			if (linkedIntermediateSurvey != null) {
				val intermediateSurveyShortURL = surveyExecutionManagerService
						.intermediateSurveyParticipantShortURLEnsure(
								participant.getId(),
								linkedIntermediateSurvey.getId());

				val intermediateSurveyShortURLString = intermediateSurveyShortURL
						.calculateURL();
				log.debug(
						"Integrating intermediate survey into message with URL {}",
						intermediateSurveyShortURLString);

				if (message
						.contains(ImplementationConstants.PLACEHOLDER_LINKED_SURVEY)) {
					message = message.replace(
							ImplementationConstants.PLACEHOLDER_LINKED_SURVEY,
							intermediateSurveyShortURLString);
				} else {
					URLsToAdd += " " + intermediateSurveyShortURLString;
				}
			}

			dialogMessage.setMessage(message + URLsToAdd);
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
			final String newUncleanedButCorrectedResult)
			throws NotificationMessageException {
		log.debug("Marking dialog message {} as problem solved");

		val dialogMessage = databaseManagerService.getModelObjectById(
				DialogMessage.class, dialogMessageId);

		if (dialogMessage.getStatus() == DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER) {
			dialogMessageStatusChangesAfterSending(
					dialogMessageId,
					DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
					dialogMessage.getAnswerReceivedTimestamp(),
					StringHelpers
							.cleanReceivedMessageString(newUncleanedButCorrectedResult),
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
	// Media Object Participant Short URL
	@Synchronized
	private MediaObjectParticipantShortURL mediaObjectParticipantShortURLEnsure(
			final DialogMessage relatedDialogMessage,
			final MediaObject relatedMediaObject) {

		val existingShortIdObject = databaseManagerService
				.findOneModelObject(
						MediaObjectParticipantShortURL.class,
						Queries.MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_DIALOG_MESSAGE_AND_MEDIA_OBJECT,
						relatedDialogMessage.getId(),
						relatedMediaObject.getId());

		if (existingShortIdObject != null) {
			return existingShortIdObject;
		} else {
			val newestIdObject = databaseManagerService
					.findOneSortedModelObject(
							MediaObjectParticipantShortURL.class,
							Queries.ALL,
							Queries.MEDIA_OBJECT_PARTICIPANT_SHORT_URL__SORT_BY_SHORT_ID_DESC);

			final long nextShortId = newestIdObject == null ? 1
					: newestIdObject.getShortId() + 1;

			val newShortIdObject = new MediaObjectParticipantShortURL(
					nextShortId, relatedDialogMessage.getId(),
					relatedMediaObject.getId());

			databaseManagerService.saveModelObject(newShortIdObject);

			return newShortIdObject;
		}
	}

	// Dialog Message
	@Synchronized
	private void dialogMessageCreateAsUnexpectedReceived(
			final ObjectId participantId, final ReceivedMessage receivedMessage) {
		val dialogMessage = new DialogMessage(participantId, 0,
				DialogMessageStatusTypes.RECEIVED_UNEXPECTEDLY, "", -1, -1,
				false, false, -1, receivedMessage.getReceivedTimestamp(),
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

		if (dialogStatus.getMonitoringStartedTimestamp() == 0) {
			dialogStatus.setMonitoringStartedTimestamp(InternalDateTime
					.currentTimeMillis());
		}

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
					// available)
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
											cleanedMessageValue, true, false);
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

							MonitoringReplyRule monitoringReplyRule = null;
							if (messageToSendTask
									.getAbstractMonitoringRuleRequiredToPrepareMessage() != null) {
								monitoringReplyRule = (MonitoringReplyRule) messageToSendTask
										.getAbstractMonitoringRuleRequiredToPrepareMessage();
							}
							val monitoringMessage = messageToSendTask
									.getMonitoringMessageToSend();
							val messageTextToSend = messageToSendTask
									.getMessageTextToSend();

							// Prepare message for sending
							dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
									participant,
									messageTextToSend,
									false,
									InternalDateTime.currentTimeMillis(),
									null,
									monitoringMessage,
									monitoringReplyRule != null ? monitoringReplyRule
											.isSendMessageToSupervisor()
											: false, false);
						}
					}
				}
			}
		}
	}

	@Synchronized
	public void scheduleMessagesForSending() {
		log.debug("Create a list of all relevant participants for scheduling of monitoring messages");
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
						"Participant {} has not been scheduled at all! Wait until next monday to start with scheduling...",
						participant.getId());
				continue;
			}

			if (dialogStatus != null
					&& !dialogStatus
							.getDateIndexOfLastDailyMonitoringProcessing()
							.equals(dateIndex)) {
				log.debug(
						"Participant {} has not been scheduled today! Start scheduling...",
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

							val monitoringRule = (MonitoringRule) messageToSendTask
									.getAbstractMonitoringRuleRequiredToPrepareMessage();
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
									participant,
									messageTextToSend,
									false,
									timeToSendMessage.getTimeInMillis(),
									monitoringRule,
									monitoringMessage,
									monitoringRule != null ? monitoringRule
											.isSendMessageToSupervisor()
											: false,
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
				receivedMessage.getType(), receivedMessage.getSender());

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

				// FIXME Special solution for MCAT
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
				// Has validation expression, but does not match

				dialogMessageStatusChangesAfterSending(dialogMessage.getId(),
						DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER,
						receivedMessage.getReceivedTimestamp(),
						cleanedMessageValue, receivedMessage.getMessage());

				return;
			} else if (relatedMonitoringMessageGroup.getValidationExpression() != null
					&& cleanedMessageValue
							.matches(relatedMonitoringMessageGroup
									.getValidationExpression())) {
				// Has validation expression and matches

				val matcher = Pattern
						.compile(
								relatedMonitoringMessageGroup
										.getValidationExpression()).matcher(
								cleanedMessageValue);

				if (matcher.groupCount() > 0) {
					// Pattern has a group
					matcher.find();

					dialogMessageStatusChangesAfterSending(
							dialogMessage.getId(),
							DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
							receivedMessage.getReceivedTimestamp(),
							matcher.group(1), receivedMessage.getMessage());

					return;
				} else {
					// Pattern has no group
					dialogMessageStatusChangesAfterSending(
							dialogMessage.getId(),
							DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
							receivedMessage.getReceivedTimestamp(),
							cleanedMessageValue, receivedMessage.getMessage());

					return;
				}
			} else {
				// Has no validation expression

				dialogMessageStatusChangesAfterSending(
						dialogMessage.getId(),
						DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
						receivedMessage.getReceivedTimestamp(),
						cleanedMessageValue, receivedMessage.getMessage());

				return;
			}
		}
	}

	@Synchronized
	public void handleOutgoingMessages() {
		if (communicationManagerService.getMessagingThreadCount() > ImplementationConstants.MAILING_MAXIMUM_THREAD_COUNT) {
			log.warn("Too many messages currently prepared for sending...delay until the next run");
			return;
		}

		val dialogMessagesWithSenderIdentificationToSend = getDialogMessagesWithSenderWaitingToBeSentOfActiveInterventions();
		int sentMessages = 0;
		for (val dialogMessageWithSenderIdentificationToSend : dialogMessagesWithSenderIdentificationToSend) {
			final val dialogMessageToSend = dialogMessageWithSenderIdentificationToSend
					.getDialogMessage();
			try {
				DialogOption dialogOption = null;
				boolean sendToSupervisor = false;
				if (dialogMessageWithSenderIdentificationToSend
						.getDialogMessage().isSupervisorMessage()) {
					sendToSupervisor = true;
					dialogOption = getDialogOptionByParticipantAndRecipientType(
							dialogMessageToSend.getParticipant(), true);
				} else {
					dialogOption = getDialogOptionByParticipantAndRecipientType(
							dialogMessageToSend.getParticipant(), false);
				}

				if (dialogOption != null) {
					// FIXME Special solution for MCAT
					log.debug("Checking messages for special mRCT case in MCAT");
					if (dialogMessageToSend.getRelatedMonitoringMessage() != null) {
						val relatedMonitoringMessage = databaseManagerService
								.getModelObjectById(MonitoringMessage.class,
										dialogMessageToSend
												.getRelatedMonitoringMessage());
						val relatedMonitoringMessageGroup = databaseManagerService
								.getModelObjectById(
										MonitoringMessageGroup.class,
										relatedMonitoringMessage
												.getMonitoringMessageGroup());

						val messageGroupName = relatedMonitoringMessageGroup
								.getName().toLowerCase();
						if (messageGroupName
								.startsWith(ImplementationConstants.MESSAGE_GROUP_NAME_SUBSTRING_DEFINING_MRCT_YES_TRIGGER_IN_MCAT)) {
							log.debug("Message found from mRCT case in MCAT - checking status variable for further proceeding");

							// Status: 0 = inactive, 1 = sent, no reply yet, 2 =
							// answered and yes, 3 = not answered or no
							val participantVariableWithValue = databaseManagerService
									.findOneSortedModelObject(
											ParticipantVariableWithValue.class,
											Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_NAME,
											Queries.PARTICIPANT_VARIABLE_WITH_VALUE__SORT_BY_TIMESTAMP_DESC,
											dialogMessageToSend
													.getParticipant(),
											ImplementationConstants.VARIABLE_DEFINING_MRCT_STATUS_IN_MCAT);

							if (participantVariableWithValue != null) {
								if (participantVariableWithValue.getValue()
										.equals("0")) {
									log.debug("mRCT currently inactive (should not happen) - simply ignore message for a while");

									continue;
								} else if (participantVariableWithValue
										.getValue().equals("1")) {
									log.debug("mRCT sent - delay messsage and wait for reply");

									continue;
								} else if (participantVariableWithValue
										.getValue().equals("2")) {
									log.debug("mRCT was answered and yes -> send message now");
								} else if (participantVariableWithValue
										.getValue().equals("3")) {
									log.debug("mRCT was not answered or no -> delete message");

									databaseManagerService
											.deleteModelObject(dialogMessageToSend);

									continue;
								}
							}
						}
					}
					// End of solution

					log.debug("Sending prepared message to {} ({})",
							sendToSupervisor ? "supervisor" : "participant",
							dialogOption.getData());
					communicationManagerService.sendMessage(dialogOption,
							dialogMessageToSend.getId(),
							dialogMessageWithSenderIdentificationToSend
									.getMessageSenderIdentification(),
							dialogMessageToSend.getMessage(),
							dialogMessageToSend.isMessageExpectsAnswer());
					sentMessages++;

					if (sentMessages > ImplementationConstants.MAILING_MAXIMUM_THREAD_COUNT) {
						log.debug("Too many messages currently prepared for sending...delay until the next run");
						break;
					}
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
	 * @param advisorMessage
	 * @param messageWithPlaceholders
	 */
	@Synchronized
	public void sendManualMessage(final Participant participant,
			final boolean advisorMessage, final String messageWithPlaceholders) {
		val variablesWithValues = variablesManagerService
				.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		val messageToSend = VariableStringReplacer
				.findVariablesAndReplaceWithTextValues(
						participant.getLanguage(), messageWithPlaceholders,
						variablesWithValues.values(), "");
		dialogMessageCreateManuallyOrByRulesIncludingMediaObject(participant,
				messageToSend, true, InternalDateTime.currentTimeMillis(),
				null, null, advisorMessage, false);
	}

	/*
	 * PRIVATE Getter methods
	 */
	/**
	 * Returns a list of {@link DialogMessage}s that should be sent; Parameters
	 * therefore are:
	 *
	 * - the belonging intervention is active
	 * - the belonging intervention has sender identification
	 * - the belonging intervention monitoring is active
	 * - the participant has monitoring active
	 * - the participant has all data for monitoring available
	 * - the participant has finished the screening survey
	 * - the participant not finished the monitoring
	 * - the message should have the status PREPARED_FOR_SENDING
	 * - the should be sent timestamp should be lower than the current time
	 *
	 * @return
	 */
	@Synchronized
	private List<DialogMessageWithSenderIdentification> getDialogMessagesWithSenderWaitingToBeSentOfActiveInterventions() {
		val dialogMessagesWaitingToBeSend = new ArrayList<DialogMessageWithSenderIdentification>();

		for (val intervention : databaseManagerService.findModelObjects(
				Intervention.class,
				Queries.INTERVENTION__ACTIVE_TRUE_MONITORING_ACTIVE_TRUE)) {
			if (intervention.getAssignedSenderIdentification() != null) {
				for (val participantId : databaseManagerService
						.findModelObjectIds(
								Participant.class,
								Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
								intervention.getId())) {
					if (participantId != null) {
						for (val dialogStatus : databaseManagerService
								.findModelObjects(
										DialogStatus.class,
										Queries.DIALOG_STATUS__BY_PARTICIPANT_AND_DATA_FOR_MONITORING_PARTICIPATION_AVAILABLE_TRUE_AND_SCREENING_SURVEY_PERFORMED_TRUE_AND_MONITORING_PERFORMED_FALSE,
										participantId)) {
							if (dialogStatus != null) {

								val dialogMessagesWaitingToBeSendOfParticipant = databaseManagerService
										.findModelObjects(
												DialogMessage.class,
												Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_SHOULD_BE_SENT_TIMESTAMP_LOWER,
												participantId,
												DialogMessageStatusTypes.PREPARED_FOR_SENDING,
												InternalDateTime
														.currentTimeMillis());

								for (val dialogMessageWaitingToBeSendOfParticipant : dialogMessagesWaitingToBeSendOfParticipant) {
									dialogMessagesWaitingToBeSend
											.add(new DialogMessageWithSenderIdentification(
													dialogMessageWaitingToBeSendOfParticipant,
													intervention
															.getAssignedSenderIdentification()));
								}
							}
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
	private DialogOption getDialogOptionByParticipantAndRecipientType(
			final ObjectId participantId, final boolean isSupervisorMessage) {
		val dialogOption = databaseManagerService
				.findOneModelObject(
						DialogOption.class,
						isSupervisorMessage ? Queries.DIALOG_OPTION__FOR_SUPERVISOR_BY_PARTICIPANT
								: Queries.DIALOG_OPTION__FOR_PARTICIPANT_BY_PARTICIPANT,
						participantId);

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
	 * - the belonging intervention has a sender identification
	 * - the belonging intervention monitoring is active
	 * - the participant has monitoring active
	 * - the participant has all data for monitoring available
	 * - the participant has finished the screening survey
	 * - the participant not finished the monitoring
	 *
	 * @return
	 */
	@Synchronized
	private List<Participant> getAllParticipantsRelevantForAnsweredInTimeChecksAndMonitoringSheduling() {
		val relevantParticipants = new ArrayList<Participant>();

		for (val intervention : databaseManagerService.findModelObjects(
				Intervention.class,
				Queries.INTERVENTION__ACTIVE_TRUE_MONITORING_ACTIVE_TRUE)) {
			if (intervention.getAssignedSenderIdentification() != null) {
				for (val participant : databaseManagerService
						.findModelObjects(
								Participant.class,
								Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
								intervention.getId())) {
					if (participant != null) {
						for (val dialogStatus : databaseManagerService
								.findModelObjects(
										DialogStatus.class,
										Queries.DIALOG_STATUS__BY_PARTICIPANT_AND_DATA_FOR_MONITORING_PARTICIPATION_AVAILABLE_TRUE_AND_SCREENING_SURVEY_PERFORMED_TRUE_AND_MONITORING_PERFORMED_FALSE,
										participant.getId())) {
							if (dialogStatus != null) {
								relevantParticipants.add(participant);
							}
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

	// FIXME Special (ugly) solution for MCAT
	@Synchronized
	public void rememberMediaObjectForDialogMessage(
			final ObjectId dialogMessageId, final MediaObject mediaObject) {
		val dialogMessage = databaseManagerService.getModelObjectById(
				DialogMessage.class, dialogMessageId);

		if (dialogMessage == null) {
			return;
		}

		val participant = databaseManagerService.getModelObjectById(
				Participant.class, dialogMessage.getParticipant());

		if (participant == null) {
			return;
		}

		variablesManagerService.rememberMediaObjectForParticipant(participant,
				mediaObject);
	}

	// End of solution

	@Synchronized
	public void createStatistics(final File statisticsFile) throws IOException {
		final Properties statistics = new Properties() {
			private static final long	serialVersionUID	= -478652106406702866L;

			@Override
			public synchronized Enumeration<Object> keys() {
				return Collections.enumeration(new TreeSet<Object>(super
						.keySet()));
			}
		};

		statistics.setProperty("created",
				StringHelpers.createDailyUniqueIndex());
		val activeInterventions = databaseManagerService.findModelObjects(
				Intervention.class, Queries.INTERVENTION__ACTIVE_TRUE);

		int activeInterventionsCount = 0;
		// Create statistics of all active interventions
		for (val intervention : activeInterventions) {
			activeInterventionsCount++;

			// Check all relevant participants
			val participants = databaseManagerService
					.findModelObjects(
							Participant.class,
							Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
							intervention.getId());

			// Message counts
			int totalSentMessages = 0;
			int totalReceivedMessages = 0;
			int answeredQuestions = 0;
			int unansweredQuestions = 0;
			int mediaObjectsViewed = 0;

			for (val participant : participants) {
				val dialogMessages = databaseManagerService
						.findModelObjects(
								DialogMessage.class,
								Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_MESSAGE_TYPE,
								participant.getId(), false);
				for (val dialogMessage : dialogMessages) {
					switch (dialogMessage.getStatus()) {
						case IN_CREATION:
							break;
						case PREPARED_FOR_SENDING:
							break;
						case RECEIVED_UNEXPECTEDLY:
							totalReceivedMessages++;
							break;
						case SENDING:
							break;
						case SENT_AND_ANSWERED_AND_PROCESSED:
							totalSentMessages++;
							totalReceivedMessages++;
							answeredQuestions++;
							break;
						case SENT_AND_ANSWERED_BY_PARTICIPANT:
							totalSentMessages++;
							totalReceivedMessages++;
							answeredQuestions++;
							break;
						case SENT_AND_NOT_ANSWERED_AND_PROCESSED:
							totalSentMessages++;
							unansweredQuestions++;
							break;
						case SENT_AND_WAITING_FOR_ANSWER:
							totalSentMessages++;
							break;
						case SENT_BUT_NOT_WAITING_FOR_ANSWER:
							totalSentMessages++;
							break;
					}

					if (dialogMessage.isMediaContentViewed()) {
						mediaObjectsViewed++;
					}
				}
			}

			// Write values
			statistics.setProperty("intervention."
					+ intervention.getId().toString() + ".name",
					intervention.getName());

			statistics.setProperty("intervention."
					+ intervention.getId().toString() + ".totalSentMessages",
					String.valueOf(totalSentMessages));
			statistics.setProperty("intervention."
					+ intervention.getId().toString()
					+ ".totalReceivedMessages",
					String.valueOf(totalReceivedMessages));
			statistics.setProperty("intervention."
					+ intervention.getId().toString() + ".answeredQuestions",
					String.valueOf(answeredQuestions));
			statistics.setProperty("intervention."
					+ intervention.getId().toString() + ".unansweredQuestions",
					String.valueOf(unansweredQuestions));
			statistics.setProperty("intervention."
					+ intervention.getId().toString() + ".mediaObjectsViewed",
					String.valueOf(mediaObjectsViewed));
		}

		statistics.setProperty("activeInterventions",
				String.valueOf(activeInterventionsCount));

		@Cleanup
		val fileWriter = new FileWriter(statisticsFile);
		statistics.store(fileWriter,
				ImplementationConstants.LOGGING_APPLICATION_NAME
						+ " Statistics File");
		fileWriter.flush();

		@Cleanup
		val stringWriter = new StringWriter();
		statistics.store(stringWriter,
				ImplementationConstants.LOGGING_APPLICATION_NAME
						+ " Statistics File");
		stringWriter.flush();
		log.debug(stringWriter.toString());
	}
}
