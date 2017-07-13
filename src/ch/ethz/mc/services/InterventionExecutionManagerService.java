package ch.ethz.mc.services;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Pattern;

import lombok.Cleanup;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
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
import ch.ethz.mc.model.persistent.MonitoringMessageRule;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
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
import ch.ethz.mc.tools.RuleEvaluator;
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
			final boolean supervisorMessage, final boolean answerExpected,
			final int hoursUntilHandledAsNotAnswered) {
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

		// Remember accepted reply time for manual message
		if (manuallySent && answerExpected) {
			final long isUnansweredAfterTimestamp = timestampToSendMessage
					+ hoursUntilHandledAsNotAnswered
					* ImplementationConstants.HOURS_TO_TIME_IN_MILLIS_MULTIPLICATOR;

			dialogMessage
					.setIsUnansweredAfterTimestamp(isUnansweredAfterTimestamp);
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

			if (dialogMessage.isManuallySent()) {
				final long appropriateReplyTimeframe = dialogMessage
						.getIsUnansweredAfterTimestamp()
						- dialogMessage.getShouldBeSentTimestamp();

				dialogMessage.setIsUnansweredAfterTimestamp(timeStampOfEvent
						+ appropriateReplyTimeframe);
			}
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
	/**
	 * Cares for the execution of the following two main methods in the
	 * appropriate order. Caution: The same should alwasy be performed in this
	 * order to retain data consistency
	 */
	@Synchronized
	public void performMessaging() throws Exception {
		log.debug("Create a list of all relevant participants to perfom messaging");
		val participantsWithDialogStatus = getAllParticipantsRelevantForAnsweredInTimeChecksAndMonitoringScheduling();

		try {
			log.debug("React on unanswered messages");
			reactOnAnsweredAndUnansweredMessages(participantsWithDialogStatus,
					false);
		} catch (final Exception e) {
			log.error("Could not react on unanswered messages: {}",
					e.getMessage());
		}
		try {
			log.debug("React on answered messages");
			reactOnAnsweredAndUnansweredMessages(participantsWithDialogStatus,
					true);
		} catch (final Exception e) {
			log.error("Could not react on answered messages: {}",
					e.getMessage());
		}
		try {
			log.debug("Scheduling new messages");
			scheduleMessagesForSending(participantsWithDialogStatus);
		} catch (final Exception e) {
			log.error("Could not schedule new monitoring messages: {}",
					e.getMessage());
		}
	}

	@Synchronized
	private void reactOnAnsweredAndUnansweredMessages(
			final Hashtable<Participant, DialogStatus> participantsWithDialogStatus,
			final boolean reactOnAnsweredMessages) {
		log.debug("Handling {} messages", reactOnAnsweredMessages ? "answered"
				: "unanswered");

		for (val participant : participantsWithDialogStatus.keySet()) {

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
								this,
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
											: false, false, 0);
						}
					}
				}
			}
		}
	}

	@Synchronized
	private void scheduleMessagesForSending(
			final Hashtable<Participant, DialogStatus> participantsWithDialogStatus) {
		log.debug("Scheduling monitoring messages");

		val dateIndex = StringHelpers.createDailyUniqueIndex();
		val dateToday = new Date(InternalDateTime.currentTimeMillis());
		val todayDayIndex = Integer.parseInt(dayInWeekFormatter
				.format(dateToday));

		for (val participant : participantsWithDialogStatus.keySet()) {
			// Check if participant has already been scheduled today
			val dialogStatus = participantsWithDialogStatus.get(participant);

			// Only start interventions on assigned intervention monitoring
			// starting days
			if (dialogStatus != null
					&& dialogStatus.getMonitoringDaysParticipated() == 0) {
				// Check starting day based on intervention
				val intervention = databaseManagerService.getModelObjectById(
						Intervention.class, participant.getIntervention());

				boolean todayIsAStartingDay = false;

				for (val startingDay : intervention.getMonitoringStartingDays()) {
					if (startingDay == todayDayIndex) {
						todayIsAStartingDay = true;
						break;
					}
				}

				if (!todayIsAStartingDay) {
					log.debug(
							"Participant {} has not been scheduled at all! Wait until next monitoring starting day to start with scheduling...",
							participant.getId());

					continue;
				}
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
							this, databaseManagerService,
							variablesManagerService, participant, true, null,
							null, false);

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
									monitoringMessageExpectsAnswer, 0);
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

				dialogMessageCreateAsUnexpectedReceived(
						dialogOption.getParticipant(), receivedMessage);

				dialogStatusSetMonitoringFinished(dialogOption.getParticipant());

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
					log.error("Could not send prepared message, because there was no valid dialog option to send message to participantor supervisor; solution: remove current dialog message");

					try {
						databaseManagerService
								.deleteModelObject(dialogMessageToSend);
						log.debug("Cleanup successful");
					} catch (final Exception e) {
						log.error("Cleanup not successful: {}", e.getMessage());
					}
				}
			} catch (final Exception e) {
				log.error("Could not send prepared message: {}", e.getMessage());
			}
		}
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
	 * Checks message groups which groups and messages are (1) already used //
	 * (2) used less // (3) simply fit and
	 * returns the {@link MonitoringMessage} to send or null if there are no
	 * messages in the group
	 * left
	 *
	 * @return
	 */
	@Synchronized
	public MonitoringMessage determineMessageOfMessageGroupToSend(
			final Participant participant,
			final MonitoringMessageGroup messageGroup,
			final MonitoringMessage relatedMonitoringMessageForReplyRuleCase,
			final boolean isMonitoringRule) {
		val iterableMessages = databaseManagerService.findSortedModelObjects(
				MonitoringMessage.class,
				Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
				Queries.MONITORING_MESSAGE__SORT_BY_ORDER_ASC,
				messageGroup.getId());

		@SuppressWarnings("unchecked")
		final List<MonitoringMessage> messages = IteratorUtils
				.toList(iterableMessages.iterator());

		if (!isMonitoringRule
				&& messageGroup.isSendSamePositionIfSendingAsReply()) {
			// Send in same position if sending as reply
			log.debug("Searching message on same position as former message in other message group...");
			val originalMessageGroupId = relatedMonitoringMessageForReplyRuleCase
					.getMonitoringMessageGroup();
			val originalIterableMessages = databaseManagerService
					.findSortedModelObjects(
							MonitoringMessage.class,
							Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
							Queries.MONITORING_MESSAGE__SORT_BY_ORDER_ASC,
							originalMessageGroupId);

			@SuppressWarnings("unchecked")
			final List<MonitoringMessage> originalMessages = IteratorUtils
					.toList(originalIterableMessages.iterator());

			for (int i = 0; i < originalMessages.size(); i++) {
				if (originalMessages
						.get(i)
						.getId()
						.equals(relatedMonitoringMessageForReplyRuleCase
								.getId())
						&& i < messages.size()) {
					val message = messages.get(i);
					log.debug(
							"Monitoring message {} is at the same position as monitoring message {} and will thereofore be used as reply on answer",
							message.getId(),
							relatedMonitoringMessageForReplyRuleCase.getId());
					return message;
				}
			}
		} else {
			// Send in random order?
			if (messageGroup.isSendInRandomOrder()) {
				log.debug("Searching random message...");
				Collections.shuffle(messages);
			} else {
				log.debug("Searching appropriate message...");
			}

			Hashtable<String, AbstractVariableWithValue> variablesWithValues = null;

			// Loop over all messages until an appropriate message has been
			// found
			MonitoringMessage messageToStartWithInFallbackCase = null;
			int timesMessageAlreadyUsed = Integer.MAX_VALUE;

			for (int i = 0; i < 3; i++) {
				messageLoop: for (val message : messages) {
					// Fallback solution 1: Start with less used message (case
					// i==1)
					if (i == 1 && message != messageToStartWithInFallbackCase) {
						// Skip messages until you reach less used message
						continue messageLoop;
					}

					// Try to find next message (case i==0)
					val dialogMessages = new ArrayList<DialogMessage>();
					if (i == 0) {
						// Determine how often the message has already been used
						val dialogMessagesIterator = databaseManagerService
								.findModelObjects(
										DialogMessage.class,
										Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_RELATED_MONITORING_MESSAGE,
										participant.getId(), message.getId())
								.iterator();

						CollectionUtils.addAll(dialogMessages,
								dialogMessagesIterator);

						if (dialogMessages.size() < timesMessageAlreadyUsed) {
							// Remember this message as least used message
							messageToStartWithInFallbackCase = message;
							timesMessageAlreadyUsed = dialogMessages.size();
						}
					}

					// In case of fallback 1 or 2, or if the message has never
					// been used
					if (i >= 1 || dialogMessages.size() == 0) {
						if (i == 0) {
							log.debug(
									"Monitoring message {} was not used for participant, yet",
									message.getId());
						} else if (i == 1) {
							log.debug(
									"Monitoring message {} was LESS used for participant",
									message.getId());
						} else if (i == 2) {
							log.debug(
									"Monitoring message {} could be used for participant as last option",
									message.getId());
						}

						// Check rules of message for execution
						val rules = databaseManagerService
								.findSortedModelObjects(
										MonitoringMessageRule.class,
										Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
										Queries.MONITORING_MESSAGE_RULE__SORT_BY_ORDER_ASC,
										message.getId());

						for (val rule : rules) {
							if (variablesWithValues == null) {
								variablesWithValues = variablesManagerService
										.getAllVariablesWithValuesOfParticipantAndSystem(participant);
							}

							val ruleResult = RuleEvaluator.evaluateRule(
									participant.getId(),
									participant.getLanguage(), rule,
									variablesWithValues.values());

							if (!ruleResult.isEvaluatedSuccessful()) {
								log.error("Error when validating rule: "
										+ ruleResult.getErrorMessage());
								continue;
							}

							// Check if true rule matches
							if (!ruleResult.isRuleMatchesEquationSign()) {
								log.debug("Rule does not match, so skip this message");
								continue messageLoop;
							}
						}

						return message;
					}
				}

				if (i == 0) {
					log.debug("All message in this group were already used for the participant...so start over and use least used message");
				} else if (i == 1) {
					log.debug("All messages were already used for the participant and no least used message could be determined...so start over and use ANY message that fits the rules");
				} else if (i == 2) {
					log.warn(
							"No message fits the rules! Message group {} ({}) should be checked for participant {}",
							messageGroup.getId(), messageGroup.getName(),
							participant.getId());
				}
			}
		}

		return null;
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

		// Determine message text to send
		val messageTextToSend = VariableStringReplacer
				.findVariablesAndReplaceWithTextValues(
						participant.getLanguage(), messageWithPlaceholders,
						variablesWithValues.values(), "");

		// Create dialog message
		dialogMessageCreateManuallyOrByRulesIncludingMediaObject(participant,
				messageTextToSend, true, InternalDateTime.currentTimeMillis(),
				null, null, advisorMessage, false, 0);
	}

	/**
	 * Sends a manual message based on a {@link MonitoringMessageGroup}
	 *
	 * @param participant
	 * @param advisorMessage
	 * @param monitoringMessageGroup
	 * @param hoursUntilHandledAsNotAnswered
	 */
	@Synchronized
	public void sendManualMessage(final Participant participant,
			final boolean advisorMessage,
			final MonitoringMessageGroup monitoringMessageGroup,
			final int hoursUntilHandledAsNotAnswered) {

		val determinedMonitoringMessageToSend = determineMessageOfMessageGroupToSend(
				participant, monitoringMessageGroup, null, true);

		if (determinedMonitoringMessageToSend == null) {
			log.warn(
					"There are no more messages left in message group {} to send a message to participant {}",
					monitoringMessageGroup, participant.getId());

			return;
		}

		// Determine message text to send
		val variablesWithValues = variablesManagerService
				.getAllVariablesWithValuesOfParticipantAndSystem(participant,
						determinedMonitoringMessageToSend);
		val messageTextToSend = VariableStringReplacer
				.findVariablesAndReplaceWithTextValues(participant
						.getLanguage(), determinedMonitoringMessageToSend
						.getTextWithPlaceholders().get(participant),
						variablesWithValues.values(), "");

		// Create dialog message
		dialogMessageCreateManuallyOrByRulesIncludingMediaObject(participant,
				messageTextToSend, true, InternalDateTime.currentTimeMillis(),
				null, determinedMonitoringMessageToSend, advisorMessage,
				monitoringMessageGroup.isMessagesExpectAnswer(),
				hoursUntilHandledAsNotAnswered);
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
						val dialogStatus = databaseManagerService
								.findOneModelObject(
										DialogStatus.class,
										Queries.DIALOG_STATUS__BY_PARTICIPANT_AND_DATA_FOR_MONITORING_PARTICIPATION_AVAILABLE_TRUE_AND_SCREENING_SURVEY_PERFORMED_TRUE_AND_MONITORING_PERFORMED_FALSE,
										participantId);

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
	private Hashtable<Participant, DialogStatus> getAllParticipantsRelevantForAnsweredInTimeChecksAndMonitoringScheduling() {
		val relevantParticipants = new Hashtable<Participant, DialogStatus>();

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
						val dialogStatus = databaseManagerService
								.findOneModelObject(
										DialogStatus.class,
										Queries.DIALOG_STATUS__BY_PARTICIPANT_AND_DATA_FOR_MONITORING_PARTICIPATION_AVAILABLE_TRUE_AND_SCREENING_SURVEY_PERFORMED_TRUE_AND_MONITORING_PERFORMED_FALSE,
										participant.getId());

						if (dialogStatus != null) {
							relevantParticipants.put(participant, dialogStatus);
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
