package ch.ethz.mc.services;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.model.persistent.MicroDialogDecisionPoint;
import ch.ethz.mc.model.persistent.MicroDialogMessage;
import ch.ethz.mc.model.persistent.MicroDialogMessageRule;
import ch.ethz.mc.model.persistent.MonitoringMessage;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringMessageRule;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.persistent.types.AnswerTypes;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.DialogMessageTypes;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.model.persistent.types.PushNotificationTypes;
import ch.ethz.mc.services.internal.CommunicationManagerService;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService.FILE_STORES;
import ch.ethz.mc.services.internal.RecursiveAbstractMonitoringRulesResolver;
import ch.ethz.mc.services.internal.RecursiveAbstractMonitoringRulesResolver.EXECUTION_CASE;
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
import lombok.Cleanup;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Cares for the orchestration of the {@link Intervention}s as well as all
 * related {@link ModelObject}s
 *
 * @author Andreas Filler
 */
@Log4j2
public class InterventionExecutionManagerService {
	private final Object								$lock;

	private static InterventionExecutionManagerService	instance				= null;

	private static SimpleDateFormat						dayInWeekFormatter		= new SimpleDateFormat(
			"u");

	private final boolean								simulatorActive;

	private long										lastPeriodicScheduling	= 0;

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

		simulatorActive = Constants.isSimulatedDateAndTime();

		// Remember stop words
		acceptedStopWords = Constants.getAcceptedStopWords();

		// Reset all messages which could not be sent the last times
		dialogMessagesResetStatusAfterRestart();

		// Prepare working threads
		outgoingMessageWorker = new OutgoingMessageWorker(this);
		incomingMessageWorker = new IncomingMessageWorker(this,
				communicationManagerService);
		monitoringSchedulingWorker = new MonitoringSchedulingWorker(this,
				surveyExecutionManagerService);

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

	public void startCommunicationAndWorking(final MC mc) {
		new Thread("Communication and Worker Starter") {
			@Override
			public void run() {
				log.info("Starting communication and workers...");

				// Start communication manager service
				try {
					communicationManagerService.start(instance);

					// Start working threads
					outgoingMessageWorker.start();
					incomingMessageWorker.start();
					monitoringSchedulingWorker.start();
				} catch (final Exception e) {
					log.error("Error at starting communication and workers: {}",
							e.getMessage());

					mc.forceShutdown();
				}

				log.info("Started.");
			}
		}.start();
	}

	public void stop() throws Exception {
		log.info(
				"Stopping communication, workers and service (takes several seconds)...");

		log.info("Stopping master rule evaluation worker...");
		synchronized (monitoringSchedulingWorker) {
			monitoringSchedulingWorker.setShouldStop(true);
			Thread.sleep(2000);
			try {
				monitoringSchedulingWorker.interrupt();
				monitoringSchedulingWorker.join();
			} catch (final Exception e) {
				// Do nothing
			}
		}
		log.info("Stopping incoming message worker...");
		synchronized (incomingMessageWorker) {
			incomingMessageWorker.setShouldStop(true);
			Thread.sleep(2000);
			try {
				incomingMessageWorker.interrupt();
				incomingMessageWorker.join();
			} catch (final Exception e) {
				// Do nothing
			}
		}
		log.info("Stopping outgoing message worker...");
		synchronized (outgoingMessageWorker) {
			outgoingMessageWorker.setShouldStop(true);
			Thread.sleep(2000);
			try {
				outgoingMessageWorker.interrupt();
				outgoingMessageWorker.join();
			} catch (final Exception e) {
				// Do nothing
			}
		}

		while (monitoringSchedulingWorker.isAlive()
				|| incomingMessageWorker.isAlive()
				|| outgoingMessageWorker.isAlive()) {
			Thread.sleep(200);
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
			participant = databaseManagerService
					.getModelObjectById(Participant.class, participant.getId());
			adjustedParticipants.add(participant);

			if (participant.isMonitoringActive()) {
				participant.setMonitoringActive(false);
			} else {
				val dialogStatus = databaseManagerService.findOneModelObject(
						DialogStatus.class,
						Queries.DIALOG_STATUS__BY_PARTICIPANT,
						participant.getId());

				if (dialogStatus != null && dialogStatus
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
			final Participant participant, final DialogMessageTypes type,
			final String message, final AnswerTypes answerType,
			final String answerOptions, final boolean manuallySent,
			final long timestampToSendMessage,
			final MonitoringRule relatedMonitoringRule,
			final MonitoringMessage relatedMonitoringMessage,
			final MicroDialog relatedMicroDialogForActivation,
			final MicroDialogMessage relatedMicroDialogMessage,
			final boolean supervisorMessage, final boolean answerExpected,
			final boolean isSticky,
			final int minutesUntilHandledAsNotAnswered) {
		log.debug("Create message and prepare for sending");
		val dialogMessage = new DialogMessage(participant.getId(), 0,
				DialogMessageStatusTypes.PREPARED_FOR_SENDING, type, null,
				message, message, answerType, answerOptions, null, null, null,
				null, timestampToSendMessage, -1, supervisorMessage,
				answerExpected, isSticky, -1, -1, null, null, false,
				relatedMonitoringRule == null ? null
						: relatedMonitoringRule.getId(),
				relatedMonitoringMessage == null ? null
						: relatedMonitoringMessage.getId(),
				relatedMicroDialogForActivation == null ? null
						: relatedMicroDialogForActivation.getId(),
				relatedMicroDialogMessage == null ? null
						: relatedMicroDialogMessage.getId(),
				false, manuallySent);

		// Check linked media object
		MediaObject linkedMediaObject = null;
		if (relatedMonitoringMessage != null) {
			if (relatedMonitoringMessage.getLinkedMediaObject() != null) {
				linkedMediaObject = databaseManagerService.getModelObjectById(
						MediaObject.class,
						relatedMonitoringMessage.getLinkedMediaObject());
			}
		} else if (relatedMicroDialogMessage != null) {
			if (relatedMicroDialogMessage.getLinkedMediaObject() != null) {
				linkedMediaObject = databaseManagerService.getModelObjectById(
						MediaObject.class,
						relatedMicroDialogMessage.getLinkedMediaObject());
			}
		}

		// Check linked intermediate survey
		ScreeningSurvey linkedIntermediateSurvey = null;
		if (relatedMonitoringMessage != null) {
			if (relatedMonitoringMessage
					.getLinkedIntermediateSurvey() != null) {
				linkedIntermediateSurvey = databaseManagerService
						.getModelObjectById(ScreeningSurvey.class,
								relatedMonitoringMessage
										.getLinkedIntermediateSurvey());
			}
		} else if (relatedMicroDialogMessage != null) {
			if (relatedMicroDialogMessage
					.getLinkedIntermediateSurvey() != null) {
				linkedIntermediateSurvey = databaseManagerService
						.getModelObjectById(ScreeningSurvey.class,
								relatedMicroDialogMessage
										.getLinkedIntermediateSurvey());
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
					+ minutesUntilHandledAsNotAnswered
							* ImplementationConstants.MINUTES_TO_TIME_IN_MILLIS_MULTIPLICATOR;

			dialogMessage
					.setIsUnansweredAfterTimestamp(isUnansweredAfterTimestamp);
		}

		// Special saving case for linked media object or intermediate survey
		String messageWithForcedLinks = message;
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

				dialogMessage.setMediaObjectLink(
						mediaObjectParticipantShortURLString);
				dialogMessage.setMediaObjectType(linkedMediaObject.getType());

				if (message.contains(
						ImplementationConstants.PLACEHOLDER_LINKED_MEDIA_OBJECT)) {
					messageWithForcedLinks = messageWithForcedLinks.replace(
							ImplementationConstants.PLACEHOLDER_LINKED_MEDIA_OBJECT,
							mediaObjectParticipantShortURLString);
				} else {
					URLsToAdd += " " + mediaObjectParticipantShortURLString;
				}

				// Read media objects if it is text-based
				if (linkedMediaObject.isTextBased()) {
					if (linkedMediaObject.isFileBased()) {
						val file = interventionAdministrationManagerService
								.mediaObjectGetFile(linkedMediaObject,
										FILE_STORES.STORAGE);
						try {
							val fileContent = FileUtils.readFileToString(file);
							dialogMessage.setTextBasedMediaObjectContent(
									fileContent);
						} catch (final IOException e) {
							log.error(
									"File could not be read for creating dialog message with file content: {}",
									e.getMessage());
						}
					} else {
						dialogMessage.setTextBasedMediaObjectContent(
								linkedMediaObject.getUrlReference());
					}
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

				dialogMessage.setSurveyLink(intermediateSurveyShortURLString);
				if (message.contains(
						ImplementationConstants.PLACEHOLDER_LINKED_SURVEY)) {
					messageWithForcedLinks = messageWithForcedLinks.replace(
							ImplementationConstants.PLACEHOLDER_LINKED_SURVEY,
							intermediateSurveyShortURLString);
				} else {
					URLsToAdd += " " + intermediateSurveyShortURLString;
				}
			}

			dialogMessage.setMessageWithForcedLinks(
					messageWithForcedLinks + URLsToAdd);
			dialogMessage
					.setStatus(DialogMessageStatusTypes.PREPARED_FOR_SENDING);
		}

		databaseManagerService.saveModelObject(dialogMessage);
	}

	@Synchronized
	private boolean dialogMessageCheckForDuplicateBasedOnClientId(
			final ObjectId participant, final String clientId) {
		val dialogMessage = databaseManagerService.findOneModelObject(
				DialogMessage.class,
				Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_CLIENT_ID,
				participant, clientId);

		if (dialogMessage == null) {
			return false;
		} else {
			return true;
		}
	}

	@Synchronized
	public void dialogMessageSetMediaContentViewed(
			final ObjectId dialogMessageId) {
		val dialogMessage = databaseManagerService
				.getModelObjectById(DialogMessage.class, dialogMessageId);

		dialogMessage.setMediaContentViewed(true);

		databaseManagerService.saveModelObject(dialogMessage);
	}

	@Synchronized
	public void dialogMessageSetProblemSolved(final ObjectId dialogMessageId,
			final String newUncleanedButCorrectedResult)
			throws NotificationMessageException {
		log.debug("Marking dialog message {} as problem solved");

		val dialogMessage = databaseManagerService
				.getModelObjectById(DialogMessage.class, dialogMessageId);

		if (dialogMessage
				.getStatus() == DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER) {
			dialogMessageStatusChangesAfterSending(dialogMessageId,
					DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
					dialogMessage.getAnswerReceivedTimestamp(),
					StringHelpers.cleanReceivedMessageString(
							newUncleanedButCorrectedResult),
					dialogMessage.getAnswerReceivedRaw(), null);
		} else if (dialogMessage
				.getStatus() == DialogMessageStatusTypes.RECEIVED_UNEXPECTEDLY) {
			unexpectedDialogMessageSetProblemSolved(dialogMessage);
		} else {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__CASE_CANT_BE_SOLVED_ANYMORE);
		}
	}

	@Synchronized
	private void unexpectedDialogMessageSetProblemSolved(
			final DialogMessage dialogMessage) {
		dialogMessage.setAnswerNotAutomaticallyProcessable(false);
		databaseManagerService.saveModelObject(dialogMessage);
	}

	/**
	 * Handles states form "PREPARED_FOR_SENDING" to
	 * "SENT_AND_WAITING_FOR_ANSWER" or "SENT_BUT_NOT_WAITING_FOR_ANSWER"
	 *
	 * @param dialogMessageId
	 * @param newStatus
	 * @param timeStampOfEvent
	 */
	@Synchronized
	public DialogMessage dialogMessageStatusChangesForSending(
			final ObjectId dialogMessageId,
			final DialogMessageStatusTypes newStatus,
			final long timeStampOfEvent) {
		val dialogMessage = databaseManagerService
				.getModelObjectById(DialogMessage.class, dialogMessageId);

		dialogMessage.setStatus(newStatus);

		// Adjust for sent
		if (newStatus == DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER) {
			// Adjust unanswered time frame for monitoring messages
			if (dialogMessage.getRelatedMonitoringRuleForReplyRules() != null) {
				val monitoringRule = databaseManagerService.getModelObjectById(
						MonitoringRule.class,
						dialogMessage.getRelatedMonitoringRuleForReplyRules());

				if (monitoringRule != null) {
					long isUnansweredAfterTimestamp;
					if (monitoringRule
							.getMinutesUntilMessageIsHandledAsUnanswered() == Integer.MAX_VALUE) {
						isUnansweredAfterTimestamp = Long.MAX_VALUE;
					} else {
						isUnansweredAfterTimestamp = timeStampOfEvent
								+ monitoringRule
										.getMinutesUntilMessageIsHandledAsUnanswered()
										* ImplementationConstants.MINUTES_TO_TIME_IN_MILLIS_MULTIPLICATOR;
					}

					dialogMessage.setIsUnansweredAfterTimestamp(
							isUnansweredAfterTimestamp);
				}
			}

			// Adjust unanswered time frame for micro dialog messages
			if (dialogMessage.getRelatedMicroDialogMessage() != null) {
				val microDialogMessage = databaseManagerService
						.getModelObjectById(MicroDialogMessage.class,
								dialogMessage.getRelatedMicroDialogMessage());

				if (microDialogMessage != null) {
					long isUnansweredAfterTimestamp;
					if (microDialogMessage
							.getMinutesUntilMessageIsHandledAsUnanswered() == Integer.MAX_VALUE) {
						isUnansweredAfterTimestamp = Long.MAX_VALUE;
					} else {
						isUnansweredAfterTimestamp = timeStampOfEvent
								+ microDialogMessage
										.getMinutesUntilMessageIsHandledAsUnanswered()
										* ImplementationConstants.MINUTES_TO_TIME_IN_MILLIS_MULTIPLICATOR;
					}

					dialogMessage.setIsUnansweredAfterTimestamp(
							isUnansweredAfterTimestamp);
				}
			}

			dialogMessage.setSentTimestamp(timeStampOfEvent);

			// Adjust unanswered time frame for manual messages
			if (dialogMessage.isManuallySent()) {
				final long appropriateReplyTimeframe = dialogMessage
						.getIsUnansweredAfterTimestamp()
						- dialogMessage.getShouldBeSentTimestamp();

				dialogMessage.setIsUnansweredAfterTimestamp(
						timeStampOfEvent + appropriateReplyTimeframe);
			}
		} else if (newStatus == DialogMessageStatusTypes.SENT_BUT_NOT_WAITING_FOR_ANSWER) {
			dialogMessage.setSentTimestamp(timeStampOfEvent);
		}

		databaseManagerService.saveModelObject(dialogMessage);

		return dialogMessage;
	}

	/*
	 * PRIVATE Modification methods
	 */
	// Media Object Participant Short URL
	@Synchronized
	private MediaObjectParticipantShortURL mediaObjectParticipantShortURLEnsure(
			final DialogMessage relatedDialogMessage,
			final MediaObject relatedMediaObject) {

		val existingShortIdObject = databaseManagerService.findOneModelObject(
				MediaObjectParticipantShortURL.class,
				Queries.MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_DIALOG_MESSAGE_AND_MEDIA_OBJECT,
				relatedDialogMessage.getId(), relatedMediaObject.getId());

		if (existingShortIdObject != null) {
			return existingShortIdObject;
		} else {
			val newestIdObject = databaseManagerService
					.findOneSortedModelObject(
							MediaObjectParticipantShortURL.class, Queries.ALL,
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
	private DialogMessage dialogMessageCreateAsUnexpectedReceivedOrIntention(
			final ObjectId participantId, final DialogMessageTypes type,
			final ReceivedMessage receivedMessage) {

		// Check type
		val isTypeIntention = receivedMessage.isTypeIntention();

		// Create values
		String answerRaw;
		final String answerCleaned;
		if (isTypeIntention) {
			answerCleaned = receivedMessage.getContent() == null
					? receivedMessage.getIntention()
					: receivedMessage.getIntention() + "\n"
							+ receivedMessage.getContent();
			answerRaw = receivedMessage.getMessage();
		} else {
			answerCleaned = StringHelpers
					.cleanReceivedMessageString(receivedMessage.getMessage());
			answerRaw = receivedMessage.getMessage();
		}

		val dialogMessage = new DialogMessage(participantId, 0,
				isTypeIntention ? DialogMessageStatusTypes.RECEIVED_AS_INTENTION
						: DialogMessageStatusTypes.RECEIVED_UNEXPECTEDLY,
				type, receivedMessage.getClientId(), "", "", null, null, null,
				null, null, null, -1, -1, false, false, false, -1,
				receivedMessage.getReceivedTimestamp(), answerCleaned,
				answerRaw, isTypeIntention ? false : true, null, null, null,
				null, false, false);

		val highestOrderMessage = databaseManagerService
				.findOneSortedModelObject(DialogMessage.class,
						Queries.DIALOG_MESSAGE__BY_PARTICIPANT,
						Queries.DIALOG_MESSAGE__SORT_BY_ORDER_DESC,
						participantId);

		if (highestOrderMessage != null) {
			dialogMessage.setOrder(highestOrderMessage.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(dialogMessage);

		return dialogMessage;
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
	 * @param clientId
	 */
	@SuppressWarnings("incomplete-switch")
	@Synchronized
	private DialogMessage dialogMessageStatusChangesAfterSending(
			final ObjectId dialogMessageId,
			final DialogMessageStatusTypes newStatus,
			final long timeStampOfEvent, final String cleanedReceivedMessage,
			final String rawReceivedMessage, final String clientId) {
		val dialogMessage = databaseManagerService
				.getModelObjectById(DialogMessage.class, dialogMessageId);

		dialogMessage.setStatus(newStatus);
		if (!StringUtils.isBlank(clientId)) {
			dialogMessage.setClientId(clientId);
		}

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

		return dialogMessage;
	}

	// Dialog status
	@Synchronized
	private void dialogStatusUpdate(final ObjectId dialogStatusId,
			final String dateIndex) {
		val dialogStatus = databaseManagerService
				.getModelObjectById(DialogStatus.class, dialogStatusId);

		dialogStatus.setDateIndexOfLastDailyMonitoringProcessing(dateIndex);
		dialogStatus.setMonitoringDaysParticipated(
				dialogStatus.getMonitoringDaysParticipated() + 1);

		if (dialogStatus.getMonitoringStartedTimestamp() == 0) {
			dialogStatus.setMonitoringStartedTimestamp(
					InternalDateTime.currentTimeMillis());
		}

		databaseManagerService.saveModelObject(dialogStatus);
	}

	@Synchronized
	private void dialogStatusSetMonitoringFinished(
			final ObjectId participantId) {
		val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participantId);

		dialogStatus.setMonitoringPerformed(true);
		dialogStatus.setMonitoringPerformedTimestamp(
				InternalDateTime.currentTimeMillis());

		databaseManagerService.saveModelObject(dialogStatus);
	}

	// Dialog option
	@Synchronized
	public boolean dialogOptionAddPushNotificationTokenBasedOnTypeAndData(
			final DialogOptionTypes dialogOptionType, final String data,
			final PushNotificationTypes pushNotificationType,
			final String pushToken) {

		val dialogOption = databaseManagerService.findOneModelObject(
				DialogOption.class, Queries.DIALOG_OPTION__BY_TYPE_AND_DATA,
				dialogOptionType, data);

		if (dialogOption != null) {
			dialogOptionAddPushNotificationToken(dialogOption.getId(),
					pushNotificationType, pushToken);
			return true;
		} else {
			return false;
		}
	}

	@Synchronized
	private void dialogOptionAddPushNotificationToken(
			final ObjectId dialogOptionId,
			final PushNotificationTypes pushNotificationType,
			final String newToken) {
		val tokenToStore = pushNotificationType.toString() + newToken;

		val dialogOption = databaseManagerService
				.getModelObjectById(DialogOption.class, dialogOptionId);

		if (!ArrayUtils.contains(dialogOption.getPushNotificationTokens(),
				tokenToStore)) {
			dialogOption.setPushNotificationTokens(ArrayUtils.add(
					dialogOption.getPushNotificationTokens(), tokenToStore));
		}

		databaseManagerService.saveModelObject(dialogOption);
	}

	@Synchronized
	public void dialogOptionRemovePushNotificationToken(
			final ObjectId dialogOptionId,
			final PushNotificationTypes pushNotificationType,
			final String token) {
		val tokenToRemove = pushNotificationType.toString() + token;

		val dialogOption = databaseManagerService
				.getModelObjectById(DialogOption.class, dialogOptionId);

		dialogOption.setPushNotificationTokens(ArrayUtils.removeElement(
				dialogOption.getPushNotificationTokens(), tokenToRemove));

		databaseManagerService.saveModelObject(dialogOption);
	}

	/*
	 * MAIN methods -
	 * 
	 * (the following two methods contain the elemental parts of the monitoring
	 * process)
	 */
	/**
	 * Cares for the execution of the following four main methods in the
	 * appropriate order. Caution: The same should always be performed in this
	 * order to retain data consistency
	 * 
	 * Important: For performance reasons this method is NOT synchronized
	 * anymore.
	 * 
	 * @return Count of participants the messaging has been performed for
	 * @throws Exception
	 */
	public long performMessaging() throws Exception {
		long messagingPerformedForParticipants = 0;

		log.debug(
				"Create a list of all relevant participants to perfom messaging");
		val participants = getAllParticipantsRelevantForAnsweredInTimeChecksAndMonitoringScheduling();

		// Scheduling of new messages (periodic) will only be
		// performed every x minutes
		boolean periodicScheduling;

		if (System.currentTimeMillis() > lastPeriodicScheduling
				+ (simulatorActive
						? ImplementationConstants.PERIODIC_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITH_SIMULATOR
						: ImplementationConstants.PERIODIC_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITHOUT_SIMULATOR)
						* 1000) {
			lastPeriodicScheduling = System.currentTimeMillis();
			periodicScheduling = true;
		} else {
			periodicScheduling = false;
		}

		for (val participantToCheck : participants) {
			// Synchronization is only be done on participant level
			synchronized ($lock) {
				// Participant and intervention check has to be done again (due
				// to potential inconsistency because of missing
				// synchronization)
				val participant = databaseManagerService.getModelObjectById(
						Participant.class, participantToCheck.getId());

				if (participant == null || !participant.isMonitoringActive()) {
					continue;
				}

				val intervention = databaseManagerService.getModelObjectById(
						Intervention.class, participant.getIntervention());

				if (intervention == null || !intervention.isActive()
						|| !intervention.isMonitoringActive()) {
					continue;
				}

				// Check dialog status:
				// - the participant has all data for monitoring available
				// - the participant has finished the screening survey
				// - the participant not finished the monitoring
				val dialogStatus = databaseManagerService.findOneModelObject(
						DialogStatus.class,
						Queries.DIALOG_STATUS__BY_PARTICIPANT,
						participant.getId());

				if (dialogStatus != null
						&& dialogStatus
								.isDataForMonitoringParticipationAvailable()
						&& dialogStatus.isScreeningSurveyPerformed()
						&& !dialogStatus.isMonitoringPerformed()) {
					/*
					 * Participant is relevant for messaging
					 * 
					 * The following steps should always be performed in this
					 * order to retain data consistency
					 */
					messagingPerformedForParticipants++;

					try {
						log.debug("React on unanswered messages");
						reactOnAnsweredAndUnansweredMessages(participant,
								dialogStatus, false);
					} catch (final Exception e) {
						log.error("Could not react on unanswered messages: {}",
								e.getMessage());
					}
					try {
						log.debug("React on answered messages");
						reactOnAnsweredAndUnansweredMessages(participant,
								dialogStatus, true);
					} catch (final Exception e) {
						log.error("Could not react on answered messages: {}",
								e.getMessage());
					}
					try {
						log.debug("Scheduling new messages (daily)");
						scheduleMessagesForSending(participant, dialogStatus,
								false);
					} catch (final Exception e) {
						log.error(
								"Could not schedule new monitoring messages (daily): {}",
								e.getMessage());
					}
					try {
						if (periodicScheduling) {
							log.debug("Scheduling of new messages (periodic)");
							scheduleMessagesForSending(participant,
									dialogStatus, true);
						}
					} catch (final Exception e) {
						log.error(
								"Could not schedule new monitoring messages (periodic): {}",
								e.getMessage());
					}
				}
			}
		}

		return messagingPerformedForParticipants;
	}

	@Synchronized
	private void reactOnAnsweredAndUnansweredMessages(
			final Participant participant, final DialogStatus dialogStatus,
			final boolean userAnswered) {
		log.debug("Handling {} messages for participant {}",
				userAnswered ? "answered" : "unanswered", participant.getId());

		// Get relevant messages of participant
		Iterable<DialogMessage> dialogMessages;
		if (userAnswered) {
			dialogMessages = getDialogMessagesOfParticipantAnsweredByParticipant(
					participant.getId());
		} else {
			dialogMessages = getDialogMessagesOfParticipantUnansweredByParticipant(
					participant.getId());
		}

		// Handle messages
		for (val dialogMessage : dialogMessages) {
			// Remember this here on a higher level to reuse it later for
			// further micro dialog handling
			MicroDialogMessage relatedMicroDialogMessage = null;
			if (dialogMessage.getRelatedMicroDialogMessage() != null) {
				relatedMicroDialogMessage = databaseManagerService
						.getModelObjectById(MicroDialogMessage.class,
								dialogMessage.getRelatedMicroDialogMessage());
			}

			// Handle storing of message reply (the text sent) by
			// participant if
			// message is answered by
			// participant and not manually sent
			if (userAnswered) {
				MonitoringMessage relatedMonitoringMessage = null;
				if (dialogMessage.getRelatedMonitoringMessage() != null) {
					relatedMonitoringMessage = databaseManagerService
							.getModelObjectById(MonitoringMessage.class,
									dialogMessage
											.getRelatedMonitoringMessage());
				}

				// Store value to variable (which is only relevant if a
				// reply is expected = related monitoring message or micro
				// dialog message is available)
				if (relatedMonitoringMessage != null
						|| relatedMicroDialogMessage != null) {
					log.debug(
							"Managing message reply (because the message is answered and has a reference to a monitoring or micro dialog message)");

					val cleanedMessageValue = dialogMessage.getAnswerReceived();
					val rawMessageValue = dialogMessage.getAnswerReceivedRaw();

					boolean storeCleaned = true;
					String variableToStore;
					if (relatedMonitoringMessage != null) {
						variableToStore = relatedMonitoringMessage
								.getStoreValueToVariableWithName();

						if (relatedMonitoringMessage.getAnswerType().isRAW()) {
							storeCleaned = false;
						}
					} else {
						variableToStore = relatedMicroDialogMessage
								.getStoreValueToVariableWithName();

						if (relatedMicroDialogMessage.getAnswerType().isRAW()) {
							storeCleaned = false;
						}
					}
					try {
						if (storeCleaned) {
							log.debug(
									"Store value '{}' (cleand as: '{}') of message to '{}' and reply variables for participant {}",
									rawMessageValue, cleanedMessageValue,
									variableToStore, participant.getId());
							if (!StringUtils.isBlank(variableToStore)) {
								variablesManagerService
										.writeVariableValueOfParticipant(
												participant.getId(),
												variableToStore,
												cleanedMessageValue);
							}
						} else {
							log.debug(
									"Store value '{}' of message to '{}' and reply variables for participant {}",
									rawMessageValue, variableToStore,
									participant.getId());
							if (!StringUtils.isBlank(variableToStore)) {
								variablesManagerService
										.writeVariableValueOfParticipant(
												participant.getId(),
												variableToStore,
												rawMessageValue);
							}
						}
						variablesManagerService.writeVariableValueOfParticipant(
								participant.getId(),
								SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES.participantMessageReply
										.toVariableName(),
								cleanedMessageValue, true, false);
						variablesManagerService.writeVariableValueOfParticipant(
								participant.getId(),
								SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES.participantRawMessageReply
										.toVariableName(),
								rawMessageValue, true, false);
					} catch (final Exception e) {
						log.error(
								"Could not store value '{}' of message to '{}' for participant {}: {}",
								dialogMessage.getAnswerReceived(),
								variableToStore, participant.getId(),
								e.getMessage());
					}
				}
			} else {
				// User did not answer so store default value for micro dialog
				// messages
				if (relatedMicroDialogMessage != null && !StringUtils
						.isBlank(relatedMicroDialogMessage.getNoReplyValue())) {
					val variableToStore = relatedMicroDialogMessage
							.getStoreValueToVariableWithName();
					val noReplyValue = relatedMicroDialogMessage
							.getNoReplyValue();
					try {
						log.debug(
								"Store micro dialog message no reply value '{}' (cleand as: '{}') of message to '{}' for participant {}",
								noReplyValue, noReplyValue, variableToStore,
								participant.getId());
						if (!StringUtils.isBlank(variableToStore)) {
							variablesManagerService
									.writeVariableValueOfParticipant(
											participant.getId(),
											variableToStore, noReplyValue);
						}
					} catch (final Exception e) {
						log.error(
								"Could not store micro dialog message no reply value '{}' of message to '{}' for participant {}: {}",
								dialogMessage.getAnswerReceived(),
								variableToStore, participant.getId(),
								e.getMessage());
					}
				}
			}

			// Set new message status
			if (userAnswered) {
				dialogMessageStatusChangesAfterSending(dialogMessage.getId(),
						DialogMessageStatusTypes.SENT_AND_ANSWERED_AND_PROCESSED,
						InternalDateTime.currentTimeMillis(), null, null, null);
			} else {
				dialogMessageStatusChangesAfterSending(dialogMessage.getId(),
						DialogMessageStatusTypes.SENT_AND_NOT_ANSWERED_AND_PROCESSED,
						InternalDateTime.currentTimeMillis(), null, null, null);
			}

			// Inform about answering timeout
			if (!userAnswered) {
				DialogOption dialogOption = null;
				if (dialogMessage.isSupervisorMessage()) {
					dialogOption = getDialogOptionByParticipantAndRecipientType(
							dialogMessage.getParticipant(), true);
				} else {
					dialogOption = getDialogOptionByParticipantAndRecipientType(
							dialogMessage.getParticipant(), false);
				}

				communicationManagerService.informAboutAnsweringTimeout(
						dialogOption, dialogMessage);
			}

			// Handle rule actions if rule was not sent manually or
			// based on reply rules
			if (dialogMessage.getRelatedMonitoringRuleForReplyRules() != null) {
				log.debug("Caring for reply rules resolving");

				// Resolve rules
				RecursiveAbstractMonitoringRulesResolver recursiveRuleResolver;
				try {
					recursiveRuleResolver = new RecursiveAbstractMonitoringRulesResolver(
							this, databaseManagerService,
							variablesManagerService, participant,
							EXECUTION_CASE.MONITORING_REPLY_RULES,
							dialogMessage.getRelatedMonitoringMessage(),
							dialogMessage
									.getRelatedMonitoringRuleForReplyRules(),
							userAnswered, null);

					recursiveRuleResolver.resolve();
				} catch (final Exception e) {
					log.error(
							"Could not resolve reply rules for participant {}: {}",
							participant.getId(), e.getMessage());
					continue;
				}

				/*
				 * Care for rule execution results
				 */

				// Prepare messages for sending
				for (val messageToSendTask : recursiveRuleResolver
						.getMessageSendingResultForMonitoringReplyRules()) {
					if (messageToSendTask.getMessageTextToSend() != null) {
						log.debug(
								"Preparing reply message for sending for participant");

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
						val answerTypeToSend = messageToSendTask
								.getAnswerTypeToSend();
						val answerOptionsToSend = messageToSendTask
								.getAnswerOptionsToSend();

						val dialogMessageType = monitoringMessage == null
								? DialogMessageTypes.PLAIN
								: monitoringMessage.isCommandMessage()
										? DialogMessageTypes.COMMAND
										: DialogMessageTypes.PLAIN;

						// Prepare message for sending
						dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
								participant, dialogMessageType,
								messageTextToSend, answerTypeToSend,
								answerOptionsToSend, false,
								InternalDateTime.currentTimeMillis(), null,
								monitoringMessage, null, null,
								monitoringReplyRule != null
										? monitoringReplyRule
												.isSendMessageToSupervisor()
										: false,
								false, false, 0);
					}
				}

				// Check micro dialog activation
				for (val microDialogActivation : recursiveRuleResolver
						.getMicroDialogsToActivate()) {

					dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
							participant,
							DialogMessageTypes.MICRO_DIALOG_ACTIVATION,
							microDialogActivation.getMiroDialogToActivate()
									.getName(),
							AnswerTypes.CUSTOM, null, false,
							InternalDateTime.currentTimeMillis(), null, null,
							microDialogActivation.getMiroDialogToActivate(),
							null, false, false, false, 0);
				}
			} else if (relatedMicroDialogMessage != null) {
				log.debug("Caring for further micro dialog handling");

				handleMicroDialog(dialogMessage.getParticipant(),
						relatedMicroDialogMessage.getMicroDialog(),
						relatedMicroDialogMessage.getId());
			}
		}

	}

	@Synchronized
	private void scheduleMessagesForSending(final Participant participant,
			final DialogStatus dialogStatus, final boolean periodicCheck) {
		log.debug("Scheduling monitoring messages for participant {}",
				participant.getId());

		val dateIndex = StringHelpers.createDailyUniqueIndex();

		// Only start interventions on assigned intervention monitoring
		// starting days
		if (dialogStatus != null
				&& dialogStatus.getMonitoringDaysParticipated() == 0) {
			// Check starting day based on intervention
			val intervention = databaseManagerService.getModelObjectById(
					Intervention.class, participant.getIntervention());

			val dateToday = new Date(InternalDateTime.currentTimeMillis());
			val todayDayIndex = Integer
					.parseInt(dayInWeekFormatter.format(dateToday));

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

				return;
			}
		}

		if (dialogStatus != null && (periodicCheck
				|| !dialogStatus.getDateIndexOfLastDailyMonitoringProcessing()
						.equals(dateIndex))) {
			if (periodicCheck) {
				log.debug("Start periodic scheduling for participant {}...",
						participant.getId());
			} else {
				log.debug(
						"Participant {} has not been scheduled today! Start daily scheduling...",
						participant.getId());
			}

			// Resolve rules
			RecursiveAbstractMonitoringRulesResolver recursiveRuleResolver;
			try {
				recursiveRuleResolver = new RecursiveAbstractMonitoringRulesResolver(
						this, databaseManagerService, variablesManagerService,
						participant,
						periodicCheck ? EXECUTION_CASE.MONITORING_RULES_PERIODIC
								: EXECUTION_CASE.MONITORING_RULES_DAILY,
						null, null, false, null);

				recursiveRuleResolver.resolve();
			} catch (final Exception e) {
				log.error("Could not resolve rules for participant {}: {}",
						participant.getId(), e.getMessage());
				return;
			}

			val finishIntervention = recursiveRuleResolver
					.isInterventionFinishedForParticipantAfterThisResolving();

			if (finishIntervention) {
				log.debug("Finishing intervention for participant");
				dialogStatusSetMonitoringFinished(participant.getId());
			} else {
				/*
				 * Care for rule execution results
				 */

				// Prepare messages for sending
				for (val messageToSendTask : recursiveRuleResolver
						.getMessageSendingResultForMonitoringRules()) {
					if (messageToSendTask.getMessageTextToSend() != null) {

						log.debug(
								"Preparing message for sending to participant");

						val monitoringRule = (MonitoringRule) messageToSendTask
								.getAbstractMonitoringRuleRequiredToPrepareMessage();
						val monitoringMessage = messageToSendTask
								.getMonitoringMessageToSend();
						val monitoringMessageExpectsAnswer = messageToSendTask
								.isAnswerExpected();
						val messageTextToSend = messageToSendTask
								.getMessageTextToSend();
						val answerTypeToSend = messageToSendTask
								.getAnswerTypeToSend();
						val answerOptionsToSend = messageToSendTask
								.getAnswerOptionsToSend();

						// Calculate time to send message
						long timeToSendMessageInMillis;
						final double hourToSendMessage = calculateHourTeSendMessageOrActivateMicroDialog(
								participant, monitoringRule);
						if (hourToSendMessage > 0) {
							final int hourPart = new Double(
									Math.floor(hourToSendMessage)).intValue();
							final int minutePart = new Double(Math
									.floor((hourToSendMessage - hourPart) * 60))
											.intValue();

							final Calendar timeToSendMessage = Calendar
									.getInstance();
							timeToSendMessage.setTimeInMillis(
									InternalDateTime.currentTimeMillis());
							timeToSendMessage.set(Calendar.HOUR_OF_DAY,
									hourPart);
							timeToSendMessage.set(Calendar.MINUTE, minutePart);
							timeToSendMessage.set(Calendar.SECOND, 0);
							timeToSendMessage.set(Calendar.MILLISECOND, 0);
							timeToSendMessageInMillis = timeToSendMessage
									.getTimeInMillis();
						} else {
							timeToSendMessageInMillis = InternalDateTime
									.currentTimeMillis();
						}

						val dialogMessageType = monitoringMessage == null
								? DialogMessageTypes.PLAIN
								: monitoringMessage.isCommandMessage()
										? DialogMessageTypes.COMMAND
										: DialogMessageTypes.PLAIN;

						// Prepare message for sending
						dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
								participant, dialogMessageType,
								messageTextToSend, answerTypeToSend,
								answerOptionsToSend, false,
								timeToSendMessageInMillis, monitoringRule,
								monitoringMessage, null, null,
								monitoringRule != null
										? monitoringRule
												.isSendMessageToSupervisor()
										: false,
								monitoringMessageExpectsAnswer, false, 0);
					}
				}

				// Check micro dialog activation
				for (val microDialogActivation : recursiveRuleResolver
						.getMicroDialogsToActivate()) {
					// Calculate time to send message
					long timeToSendMessageInMillis;
					final double hourToSendMessage = microDialogActivation
							.getHourToActivateMicroDialog();
					if (hourToSendMessage > 0) {
						final int hourPart = new Double(
								Math.floor(hourToSendMessage)).intValue();
						final int minutePart = new Double(
								Math.floor((hourToSendMessage - hourPart) * 60))
										.intValue();

						final Calendar timeToSendMessage = Calendar
								.getInstance();
						timeToSendMessage.setTimeInMillis(
								InternalDateTime.currentTimeMillis());
						timeToSendMessage.set(Calendar.HOUR_OF_DAY, hourPart);
						timeToSendMessage.set(Calendar.MINUTE, minutePart);
						timeToSendMessage.set(Calendar.SECOND, 0);
						timeToSendMessage.set(Calendar.MILLISECOND, 0);
						timeToSendMessageInMillis = timeToSendMessage
								.getTimeInMillis();
					} else {
						timeToSendMessageInMillis = InternalDateTime
								.currentTimeMillis();
					}

					dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
							participant,
							DialogMessageTypes.MICRO_DIALOG_ACTIVATION,
							microDialogActivation.getMiroDialogToActivate()
									.getName(),
							AnswerTypes.CUSTOM, null, false,
							timeToSendMessageInMillis, null, null,
							microDialogActivation.getMiroDialogToActivate(),
							null, false, false, false, 0);
				}

				if (!periodicCheck) {
					// Update status and status values
					dialogStatusUpdate(dialogStatus.getId(), dateIndex);
				}
			}
		}
	}

	/**
	 * Handles all received messages
	 * 
	 * @param receivedMessage
	 */
	@Synchronized
	public DialogMessage handleReceivedMessage(
			final ReceivedMessage receivedMessage) {
		val dialogOption = getDialogOptionByTypeAndDataOfActiveInterventions(
				receivedMessage.getType(), receivedMessage.getSender());

		if (dialogOption == null) {
			log.warn(
					"The received message with sender number '{}' does not fit to any participant of an active intervention, skip it",
					receivedMessage.getSender());
			return null;
		}

		// Check for duplicate
		if (!StringUtils.isBlank(receivedMessage.getClientId())) {
			if (dialogMessageCheckForDuplicateBasedOnClientId(
					dialogOption.getParticipant(),
					receivedMessage.getClientId())) {
				log.warn("Duplicate message received for participant {}",
						dialogOption.getParticipant());
				return null;
			}
		}

		// Check type
		val isTypeIntention = receivedMessage.isTypeIntention();

		// Create values
		String rawMessageValue;
		final String cleanedMessageValue;
		if (isTypeIntention) {
			rawMessageValue = receivedMessage.getIntention();
			cleanedMessageValue = StringHelpers
					.cleanReceivedMessageString(receivedMessage.getIntention());
		} else {
			rawMessageValue = receivedMessage.getMessage();
			cleanedMessageValue = StringHelpers
					.cleanReceivedMessageString(receivedMessage.getMessage());
		}

		// Check if received messages is a "stop"-message (only relevant for SMS
		// or Email messages)
		if (dialogOption.getType() == DialogOptionTypes.SMS
				|| dialogOption.getType() == DialogOptionTypes.EMAIL) {
			for (val stopWord : acceptedStopWords) {
				if (cleanedMessageValue.equals(stopWord)) {
					log.debug("Received stop message by participant {}",
							dialogOption.getParticipant());

					val dialogMessage = dialogMessageCreateAsUnexpectedReceivedOrIntention(
							dialogOption.getParticipant(),
							DialogMessageTypes.PLAIN, receivedMessage);

					dialogStatusSetMonitoringFinished(
							dialogOption.getParticipant());

					return dialogMessage;
				}
			}
		}

		// Check for intention messages or reply cases (message reply or micro
		// dialog reply)
		DialogMessage dialogMessage = null;
		if (!isTypeIntention) {
			dialogMessage = getDialogMessageOfParticipantWaitingForAnswer(
					dialogOption.getParticipant(),
					receivedMessage.getReceivedTimestamp(),
					receivedMessage.getRelatedMessageIdBasedOnOrder());
		}

		if (dialogMessage == null) {
			log.debug(
					"Received an {} message from '{}', store it, mark it accordingly and execute rules",
					isTypeIntention ? "intention" : "unexpected",
					receivedMessage.getSender());

			val dialogMessageCreated = dialogMessageCreateAsUnexpectedReceivedOrIntention(
					dialogOption.getParticipant(),
					isTypeIntention ? DialogMessageTypes.INTENTION
							: DialogMessageTypes.PLAIN,
					receivedMessage);

			val participant = databaseManagerService.getModelObjectById(
					Participant.class, dialogOption.getParticipant());

			try {
				if (isTypeIntention) {
					variablesManagerService.writeVariableValueOfParticipant(
							participant.getId(),
							SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES.participantIntention
									.toVariableName(),
							cleanedMessageValue, true, false);
					variablesManagerService.writeVariableValueOfParticipant(
							participant.getId(),
							SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES.participantRawIntention
									.toVariableName(),
							rawMessageValue, true, false);
					variablesManagerService.writeVariableValueOfParticipant(
							participant.getId(),
							SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES.participantIntentionContent
									.toVariableName(),
							receivedMessage.getContent(), true, false);
				} else {
					variablesManagerService.writeVariableValueOfParticipant(
							participant.getId(),
							SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES.participantUnexpectedMessage
									.toVariableName(),
							cleanedMessageValue, true, false);
					variablesManagerService.writeVariableValueOfParticipant(
							participant.getId(),
							SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES.participantUnexpectedRawMessage
									.toVariableName(),
							rawMessageValue, true, false);
				}
			} catch (final Exception e) {
				log.error(
						"Could not store value '{}' of {} message for participant {}: {}",
						rawMessageValue,
						isTypeIntention ? "intention" : "unexpected",
						participant.getId(), e.getMessage());
				return null;
			}

			log.debug("Caring for {} message rules resolving",
					isTypeIntention ? "intention" : "unexpected");

			// Resolve rules
			RecursiveAbstractMonitoringRulesResolver recursiveRuleResolver;
			try {
				recursiveRuleResolver = new RecursiveAbstractMonitoringRulesResolver(
						this, databaseManagerService, variablesManagerService,
						participant,
						isTypeIntention
								? EXECUTION_CASE.MONITORING_RULES_USER_INTENTION
								: EXECUTION_CASE.MONITORING_RULES_UNEXPECTED_MESSAGE,
						null, null, false, null);

				recursiveRuleResolver.resolve();
			} catch (final Exception e) {
				log.error(
						"Could not resolve {} message rules for participant {}: {}",
						isTypeIntention ? "intention" : "unexpected",
						participant.getId(), e.getMessage());
				return null;
			}

			/*
			 * Care for rule execution results
			 */

			// Rule solves problem
			if (recursiveRuleResolver.isCaseMarkedAsSolved()
					&& !isTypeIntention) {
				unexpectedDialogMessageSetProblemSolved(dialogMessageCreated);
			}

			// Prepare messages for sending
			for (val messageToSendTask : recursiveRuleResolver
					.getMessageSendingResultForMonitoringRules()) {
				if (messageToSendTask.getMessageTextToSend() != null) {
					log.debug(
							"Preparing message on {} message for sending to participant",
							isTypeIntention ? "intention" : "unexpected");

					val monitoringRule = (MonitoringRule) messageToSendTask
							.getAbstractMonitoringRuleRequiredToPrepareMessage();
					val monitoringMessage = messageToSendTask
							.getMonitoringMessageToSend();
					val monitoringMessageExpectsAnswer = messageToSendTask
							.isAnswerExpected();
					val messageTextToSend = messageToSendTask
							.getMessageTextToSend();
					val answerTypeToSend = messageToSendTask
							.getAnswerTypeToSend();
					val answerOptionsToSend = messageToSendTask
							.getAnswerOptionsToSend();

					// Calculate time to send message
					long timeToSendMessageInMillis;
					final double hourToSendMessage = calculateHourTeSendMessageOrActivateMicroDialog(
							participant, monitoringRule);
					if (hourToSendMessage > 0) {
						final int hourPart = new Double(
								Math.floor(hourToSendMessage)).intValue();
						final int minutePart = new Double(
								Math.floor((hourToSendMessage - hourPart) * 60))
										.intValue();

						final Calendar timeToSendMessage = Calendar
								.getInstance();
						timeToSendMessage.setTimeInMillis(
								InternalDateTime.currentTimeMillis());
						timeToSendMessage.set(Calendar.HOUR_OF_DAY, hourPart);
						timeToSendMessage.set(Calendar.MINUTE, minutePart);
						timeToSendMessage.set(Calendar.SECOND, 0);
						timeToSendMessage.set(Calendar.MILLISECOND, 0);
						timeToSendMessageInMillis = timeToSendMessage
								.getTimeInMillis();
					} else {
						timeToSendMessageInMillis = InternalDateTime
								.currentTimeMillis();
					}

					val dialogMessageType = monitoringMessage == null
							? DialogMessageTypes.PLAIN
							: monitoringMessage.isCommandMessage()
									? DialogMessageTypes.COMMAND
									: DialogMessageTypes.PLAIN;

					// Prepare message for sending
					dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
							participant, dialogMessageType, messageTextToSend,
							answerTypeToSend, answerOptionsToSend, false,
							timeToSendMessageInMillis, monitoringRule,
							monitoringMessage, null, null,
							monitoringRule != null
									? monitoringRule.isSendMessageToSupervisor()
									: false,
							monitoringMessageExpectsAnswer, false, 0);
				}
			}

			// Check micro dialog activation
			for (val microDialogActivation : recursiveRuleResolver
					.getMicroDialogsToActivate()) {
				// Calculate time to send message
				long timeToSendMessageInMillis;
				final double hourToSendMessage = microDialogActivation
						.getHourToActivateMicroDialog();
				if (hourToSendMessage > 0) {
					final int hourPart = new Double(
							Math.floor(hourToSendMessage)).intValue();
					final int minutePart = new Double(
							Math.floor((hourToSendMessage - hourPart) * 60))
									.intValue();

					final Calendar timeToSendMessage = Calendar.getInstance();
					timeToSendMessage.setTimeInMillis(
							InternalDateTime.currentTimeMillis());
					timeToSendMessage.set(Calendar.HOUR_OF_DAY, hourPart);
					timeToSendMessage.set(Calendar.MINUTE, minutePart);
					timeToSendMessage.set(Calendar.SECOND, 0);
					timeToSendMessage.set(Calendar.MILLISECOND, 0);
					timeToSendMessageInMillis = timeToSendMessage
							.getTimeInMillis();
				} else {
					timeToSendMessageInMillis = InternalDateTime
							.currentTimeMillis();
				}

				dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
						participant, DialogMessageTypes.MICRO_DIALOG_ACTIVATION,
						microDialogActivation.getMiroDialogToActivate()
								.getName(),
						AnswerTypes.CUSTOM, null, false,
						timeToSendMessageInMillis, null, null,
						microDialogActivation.getMiroDialogToActivate(), null,
						false, false, false, 0);
			}

			return dialogMessageCreated;
		} else {
			// Check if result is in general automatically
			// processable

			if (dialogMessage.getRelatedMonitoringMessage() != null) {
				log.debug(
						"Received an expected message reply from '{}', start validation",
						receivedMessage.getSender());

				val relatedMonitoringMessage = databaseManagerService
						.getModelObjectById(MonitoringMessage.class,
								dialogMessage.getRelatedMonitoringMessage());

				val relatedMonitoringMessageGroup = databaseManagerService
						.getModelObjectById(MonitoringMessageGroup.class,
								relatedMonitoringMessage
										.getMonitoringMessageGroup());

				if (relatedMonitoringMessageGroup
						.getValidationExpression() != null
						&& !cleanedMessageValue
								.matches(relatedMonitoringMessageGroup
										.getValidationExpression())) {
					// Has validation expression, but does not match

					dialogMessage = dialogMessageStatusChangesAfterSending(
							dialogMessage.getId(),
							DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER,
							receivedMessage.getReceivedTimestamp(),
							cleanedMessageValue, receivedMessage.getMessage(),
							receivedMessage.getClientId());

					return dialogMessage;
				} else if (relatedMonitoringMessageGroup
						.getValidationExpression() != null
						&& cleanedMessageValue
								.matches(relatedMonitoringMessageGroup
										.getValidationExpression())) {
					// Has validation expression and matches

					val matcher = Pattern
							.compile(relatedMonitoringMessageGroup
									.getValidationExpression())
							.matcher(cleanedMessageValue);

					if (matcher.groupCount() > 0) {
						// Pattern has a group
						matcher.find();

						dialogMessage = dialogMessageStatusChangesAfterSending(
								dialogMessage.getId(),
								DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
								receivedMessage.getReceivedTimestamp(),
								matcher.group(1), receivedMessage.getMessage(),
								receivedMessage.getClientId());

						return dialogMessage;
					} else {
						// Pattern has no group
						dialogMessage = dialogMessageStatusChangesAfterSending(
								dialogMessage.getId(),
								DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
								receivedMessage.getReceivedTimestamp(),
								cleanedMessageValue,
								receivedMessage.getMessage(),
								receivedMessage.getClientId());

						return dialogMessage;
					}
				} else {
					// Has no validation expression
					dialogMessage = dialogMessageStatusChangesAfterSending(
							dialogMessage.getId(),
							DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
							receivedMessage.getReceivedTimestamp(),
							cleanedMessageValue, receivedMessage.getMessage(),
							receivedMessage.getClientId());

					return dialogMessage;
				}
			} else {
				log.debug("Received an expected micro dialog reply from '{}'",
						receivedMessage.getSender());

				dialogMessage = dialogMessageStatusChangesAfterSending(
						dialogMessage.getId(),
						DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
						receivedMessage.getReceivedTimestamp(),
						cleanedMessageValue, receivedMessage.getMessage(),
						receivedMessage.getClientId());

				return dialogMessage;
			}
		}
	}

	/**
	 * Handles sending of messages
	 * 
	 * Important: For performance reasons this method is NOT synchronized
	 * anymore.
	 */
	public void handleOutgoingMessages() {
		val participantIdsWithMessagesWaitingToBeSent = getParticipantIdsWithMessagesWaitingToBeSent();

		for (val participantId : participantIdsWithMessagesWaitingToBeSent) {
			// Synchronization is only be done on participant level
			synchronized ($lock) {
				val dialogMessagesWithSenderIdentificationToSend = getDialogMessagesWithSenderWaitingToBeSentOfParticipant(
						participantId);
				for (val dialogMessageWithSenderIdentificationToSend : dialogMessagesWithSenderIdentificationToSend) {
					final val dialogMessageToSend = dialogMessageWithSenderIdentificationToSend
							.getDialogMessage();

					if (dialogMessageToSend == null) {
						continue;
					}

					if (dialogMessageToSend
							.getType() == DialogMessageTypes.MICRO_DIALOG_ACTIVATION) {
						// It's a micro dialog activation
						try {
							dialogMessageStatusChangesForSending(
									dialogMessageToSend.getId(),
									DialogMessageStatusTypes.SENDING,
									InternalDateTime.currentTimeMillis());

							handleMicroDialog(participantId,
									dialogMessageToSend
											.getRelatedMicroDialogForActivation(),
									null);

							dialogMessageStatusChangesForSending(
									dialogMessageToSend.getId(),
									DialogMessageStatusTypes.SENT_BUT_NOT_WAITING_FOR_ANSWER,
									InternalDateTime.currentTimeMillis());
						} catch (final Exception e) {
							log.error("Error at hanlding micro dialog");
						}

						continue;
					}

					try {
						DialogOption dialogOption = null;
						boolean sendToSupervisor;
						if (dialogMessageWithSenderIdentificationToSend
								.getDialogMessage().isSupervisorMessage()) {
							sendToSupervisor = true;
							dialogOption = getDialogOptionByParticipantAndRecipientType(
									dialogMessageToSend.getParticipant(), true);
						} else {
							sendToSupervisor = false;
							dialogOption = getDialogOptionByParticipantAndRecipientType(
									dialogMessageToSend.getParticipant(),
									false);
						}

						if (dialogOption != null) {
							switch (dialogOption.getType()) {
								case SMS:
								case EMAIL:
								case SUPERVISOR_SMS:
								case SUPERVISOR_EMAIL:
									if (StringUtils.isBlank(
											dialogMessageWithSenderIdentificationToSend
													.getMessageSenderIdentification())) {
										log.error(
												"Message to participant {} cannot be sent because it belongs to an intervention without assigned sender identification; solution: remove current dialog message"
														+ dialogMessageToSend
																.getParticipant());

										try {
											databaseManagerService
													.deleteModelObject(
															dialogMessageToSend);
											log.debug("Cleanup successful");
										} catch (final Exception e) {
											log.error(
													"Cleanup not successful: {}",
													e.getMessage());
										}

										continue;
									}
									if (communicationManagerService
											.getMessagingThreadCount() > ImplementationConstants.EMAIL_SENDING_MAXIMUM_THREAD_COUNT) {
										log.debug(
												"Too many email based messages currently prepared for sending...delay until the next run");
										continue;
									}
									break;
								case EXTERNAL_ID:
								case SUPERVISOR_EXTERNAL_ID:
									// No special checks for external messages,
									// currently
									break;
							}

							log.debug("Sending prepared message to {} ({})",
									sendToSupervisor ? "supervisor"
											: "participant",
									dialogOption.getData());
							communicationManagerService.sendMessage(
									dialogOption, dialogMessageToSend,
									dialogMessageWithSenderIdentificationToSend
											.getMessageSenderIdentification());
						} else {
							log.error(
									"Could not send prepared message, because there was no valid dialog option to send message to participant or supervisor; solution: remove current dialog message");

							try {
								databaseManagerService
										.deleteModelObject(dialogMessageToSend);
								log.debug("Cleanup successful");
							} catch (final Exception e) {
								log.error("Cleanup not successful: {}",
										e.getMessage());
							}
						}
					} catch (final Exception e) {
						log.error("Could not send prepared message: {}",
								e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Handles the processing of {@link MicroDialog}s for a specific
	 * {@link Participant}; If a {@link MicroDialogMessage} is given, the
	 * processing proceeds at the given point
	 * 
	 * @param participantId
	 * @param microDialogId
	 * @param microDialogMessageId
	 */
	@Synchronized
	private void handleMicroDialog(final ObjectId participantId,
			final ObjectId microDialogId, final ObjectId microDialogMessageId) {
		int currentOrder = -1;
		boolean stopMicroDialogHandling = false;
		long lastMessageSent = 0;

		Participant participant = null;
		MicroDialog microDialog = null;

		boolean variablesRequireRefresh = true;
		Hashtable<String, AbstractVariableWithValue> variablesWithValues = null;

		if (microDialogMessageId != null) {
			val microDialogMessage = databaseManagerService.getModelObjectById(
					MicroDialogMessage.class, microDialogMessageId);
			if (microDialogMessage != null) {
				// Only proceed with micro dialog if former message was a
				// blocking message
				if (!microDialogMessage
						.isMessageBlocksMicroDialogUntilAnswered()) {
					return;
				}

				currentOrder = microDialogMessage.getOrder();
			}
		}

		itemsLoop: do {
			val microDialogMessage = databaseManagerService
					.findOneSortedModelObject(MicroDialogMessage.class,
							Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG_AND_ORDER_HIGHER,
							Queries.MICRO_DIALOG_MESSAGE__SORT_BY_ORDER_ASC,
							microDialogId, currentOrder);
			val microDialogDecisionPoint = databaseManagerService
					.findOneSortedModelObject(MicroDialogDecisionPoint.class,
							Queries.MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG_AND_ORDER_HIGHER,
							Queries.MICRO_DIALOG_DECISION_POINT__SORT_BY_ORDER_ASC,
							microDialogId, currentOrder);

			boolean handleMessage;
			if (microDialogMessage == null
					&& microDialogDecisionPoint == null) {
				return;
			} else if (microDialogMessage == null) {
				handleMessage = false;
			} else if (microDialogDecisionPoint == null) {
				handleMessage = true;
			} else if (microDialogMessage.getOrder() < microDialogDecisionPoint
					.getOrder()) {
				handleMessage = true;
			} else {
				handleMessage = false;
			}

			// Retrieve participant (only once)
			if (participant == null) {
				participant = databaseManagerService
						.getModelObjectById(Participant.class, participantId);
			}

			if (handleMessage) {
				// Handle message
				currentOrder = microDialogMessage.getOrder();
				log.debug("Checking micro dialog message {}",
						microDialogMessage.getId());

				// Check rules of message for execution
				val rules = databaseManagerService.findSortedModelObjects(
						MicroDialogMessageRule.class,
						Queries.MICRO_DIALOG_MESSAGE_RULE__BY_MICRO_DIALOG_MESSAGE,
						Queries.MICRO_DIALOG_MESSAGE_RULE__SORT_BY_ORDER_ASC,
						microDialogMessage.getId());

				for (val rule : rules) {
					if (variablesRequireRefresh
							|| variablesWithValues == null) {
						variablesWithValues = variablesManagerService
								.getAllVariablesWithValuesOfParticipantAndSystem(
										participant);
						variablesRequireRefresh = false;
					}

					val ruleResult = RuleEvaluator.evaluateRule(
							participant.getId(), participant.getLanguage(),
							rule, variablesWithValues.values());

					if (!ruleResult.isEvaluatedSuccessful()) {
						log.error("Error when validating rule: "
								+ ruleResult.getErrorMessage());
						continue;
					}

					// Check if true rule matches
					if (!ruleResult.isRuleMatchesEquationSign()) {
						log.debug("Rule does not match, so skip this message");
						continue itemsLoop;
					}
				}

				// Determine message text and answer type with options to send
				val variablesWithValuesForMessageGeneration = variablesManagerService
						.getAllVariablesWithValuesOfParticipantAndSystem(
								participant, null, microDialogMessage);
				val messageTextToSend = VariableStringReplacer
						.findVariablesAndReplaceWithTextValues(
								participant.getLanguage(),
								microDialogMessage.getTextWithPlaceholders()
										.get(participant),
								variablesWithValuesForMessageGeneration
										.values(),
								"");

				AnswerTypes answerTypeToSend = null;
				String answerOptionsToSend = null;
				if (microDialogMessage.isMessageExpectsAnswer()) {
					answerTypeToSend = microDialogMessage.getAnswerType();

					if (answerTypeToSend.isKeyValueBased()) {
						answerOptionsToSend = StringHelpers
								.parseColonSeparatedMultiLineStringToJSON(
										microDialogMessage
												.getAnswerOptionsWithPlaceholders(),
										participant.getLanguage(),
										variablesWithValuesForMessageGeneration
												.values());
					} else {
						answerOptionsToSend = VariableStringReplacer
								.findVariablesAndReplaceWithTextValues(
										participant.getLanguage(),
										microDialogMessage
												.getAnswerOptionsWithPlaceholders()
												.get(participant),
										variablesWithValuesForMessageGeneration
												.values(),
										"");
					}
				}

				// Ensure higher timestamp
				val newTimestamp = InternalDateTime.currentTimeMillis();
				if (newTimestamp > lastMessageSent) {
					lastMessageSent = newTimestamp;
				} else {
					lastMessageSent++;
				}

				if (microDialog == null) {
					microDialog = databaseManagerService.getModelObjectById(
							MicroDialog.class, microDialogId);
				}

				// Prepare message for sending
				dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
						participant,
						microDialogMessage.isCommandMessage()
								? DialogMessageTypes.COMMAND
								: DialogMessageTypes.PLAIN,
						messageTextToSend, answerTypeToSend,
						answerOptionsToSend, false, lastMessageSent, null, null,
						microDialog, microDialogMessage, false,
						microDialogMessage.isMessageExpectsAnswer(),
						microDialogMessage.isMessageIsSticky(), 0);

				// Proceed with micro dialog handling?
				if (microDialogMessage
						.isMessageBlocksMicroDialogUntilAnswered()) {
					stopMicroDialogHandling = true;
				}
			} else {
				// Handle decision point
				currentOrder = microDialogDecisionPoint.getOrder();
				log.debug("Checking micro dialog decision point {}",
						microDialogDecisionPoint.getId());

				// Resolve rules
				RecursiveAbstractMonitoringRulesResolver recursiveRuleResolver;
				try {
					recursiveRuleResolver = new RecursiveAbstractMonitoringRulesResolver(
							this, databaseManagerService,
							variablesManagerService, participant,
							EXECUTION_CASE.MICRO_DIALOG_DECISION_POINT, null,
							null, false, microDialogDecisionPoint.getId());

					recursiveRuleResolver.resolve();
				} catch (final Exception e) {
					log.error(
							"Could not resolve micro dialog decision point message rules for participant {}: {}",
							participant.getId(), e.getMessage());
					return;
				}

				/*
				 * Care for rule execution results
				 */
				if (recursiveRuleResolver.isStopMicroDialogWhenTrue()) {
					log.debug("Completely stop micro dialog");
					return;
				} else if (recursiveRuleResolver
						.getNextMicroDialogMessage() != null) {
					val nextMicroDialogMessage = recursiveRuleResolver
							.getNextMicroDialogMessage();
					log.debug("Jump to micro dialog messge {}",
							nextMicroDialogMessage.getId());

					currentOrder = nextMicroDialogMessage.getOrder() - 1;
				} else if (recursiveRuleResolver.getNextMicroDialog() != null) {
					val nextMicroDialog = recursiveRuleResolver
							.getNextMicroDialog();
					log.debug("Jump to micro dialog {}",
							nextMicroDialog.getId());

					stopMicroDialogHandling = true;

					// Ensure higher timestamp
					val newTimestamp = InternalDateTime.currentTimeMillis();
					if (newTimestamp > lastMessageSent) {
						lastMessageSent = newTimestamp;
					} else {
						lastMessageSent++;
					}

					// Start activation of next micro dialog
					dialogMessageCreateManuallyOrByRulesIncludingMediaObject(
							participant,
							DialogMessageTypes.MICRO_DIALOG_ACTIVATION,
							nextMicroDialog.getName(), AnswerTypes.CUSTOM, null,
							false, lastMessageSent, null, null, nextMicroDialog,
							null, false, false, false, 0);
				}

				// Variables need to be refreshed after performing rules
				variablesRequireRefresh = true;
			}
		} while (!stopMicroDialogHandling);
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
	 * (2) used less // (3) simply fit and returns the {@link MonitoringMessage}
	 * to send or null if there are no messages in the group left
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
			log.debug(
					"Searching message on same position as former message in other message group...");
			val originalMessageGroupId = relatedMonitoringMessageForReplyRuleCase
					.getMonitoringMessageGroup();
			val originalIterableMessages = databaseManagerService
					.findSortedModelObjects(MonitoringMessage.class,
							Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
							Queries.MONITORING_MESSAGE__SORT_BY_ORDER_ASC,
							originalMessageGroupId);

			@SuppressWarnings("unchecked")
			final List<MonitoringMessage> originalMessages = IteratorUtils
					.toList(originalIterableMessages.iterator());

			for (int i = 0; i < originalMessages.size(); i++) {
				if (originalMessages.get(i).getId().equals(
						relatedMonitoringMessageForReplyRuleCase.getId())
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
								.findModelObjects(DialogMessage.class,
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
										.getAllVariablesWithValuesOfParticipantAndSystem(
												participant);
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
								log.debug(
										"Rule does not match, so skip this message");
								continue messageLoop;
							}
						}

						return message;
					}
				}

				if (i == 0) {
					log.debug(
							"All message in this group were already used for the participant...so start over and use least used message");
				} else if (i == 1) {
					log.debug(
							"All messages were already used for the participant and no least used message could be determined...so start over and use ANY message that fits the rules");
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
			final boolean advisorMessage,
			final String messageWithPlaceholders) {
		val variablesWithValues = variablesManagerService
				.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		// Determine message text to send
		val messageTextToSend = VariableStringReplacer
				.findVariablesAndReplaceWithTextValues(
						participant.getLanguage(), messageWithPlaceholders,
						variablesWithValues.values(), "");

		// Create dialog message
		dialogMessageCreateManuallyOrByRulesIncludingMediaObject(participant,
				DialogMessageTypes.PLAIN, messageTextToSend, null, null, true,
				InternalDateTime.currentTimeMillis(), null, null, null, null,
				advisorMessage, false, false, 0);
	}

	/**
	 * Sends a manual message based on a {@link MonitoringMessageGroup}
	 *
	 * @param participant
	 * @param advisorMessage
	 * @param monitoringMessageGroup
	 * @param minutesUntilHandledAsNotAnswered
	 */
	@Synchronized
	public void sendManualMessage(final Participant participant,
			final boolean advisorMessage,
			final MonitoringMessageGroup monitoringMessageGroup,
			final int minutesUntilHandledAsNotAnswered) {

		val determinedMonitoringMessageToSend = determineMessageOfMessageGroupToSend(
				participant, monitoringMessageGroup, null, true);

		if (determinedMonitoringMessageToSend == null) {
			log.warn(
					"There are no more messages left in message group {} to send a message to participant {}",
					monitoringMessageGroup, participant.getId());

			return;
		}

		// Determine message text and answer type with options to send
		val variablesWithValues = variablesManagerService
				.getAllVariablesWithValuesOfParticipantAndSystem(participant,
						determinedMonitoringMessageToSend, null);
		val messageTextToSend = VariableStringReplacer
				.findVariablesAndReplaceWithTextValues(
						participant.getLanguage(),
						determinedMonitoringMessageToSend
								.getTextWithPlaceholders().get(participant),
						variablesWithValues.values(), "");

		AnswerTypes answerTypeToSend = null;
		String answerOptionsToSend = null;
		if (monitoringMessageGroup.isMessagesExpectAnswer()) {
			answerTypeToSend = determinedMonitoringMessageToSend
					.getAnswerType();

			if (answerTypeToSend.isKeyValueBased()) {
				answerOptionsToSend = StringHelpers
						.parseColonSeparatedMultiLineStringToJSON(
								determinedMonitoringMessageToSend
										.getAnswerOptionsWithPlaceholders(),
								participant.getLanguage(),
								variablesWithValues.values());
			} else {
				answerOptionsToSend = VariableStringReplacer
						.findVariablesAndReplaceWithTextValues(
								participant.getLanguage(),
								determinedMonitoringMessageToSend
										.getAnswerOptionsWithPlaceholders()
										.get(participant),
								variablesWithValues.values(), "");
			}
		}

		// Create dialog message
		dialogMessageCreateManuallyOrByRulesIncludingMediaObject(participant,
				determinedMonitoringMessageToSend.isCommandMessage()
						? DialogMessageTypes.COMMAND : DialogMessageTypes.PLAIN,
				messageTextToSend, answerTypeToSend, answerOptionsToSend, true,
				InternalDateTime.currentTimeMillis(), null,
				determinedMonitoringMessageToSend, null, null, advisorMessage,
				monitoringMessageGroup.isMessagesExpectAnswer(), false,
				minutesUntilHandledAsNotAnswered);
	}

	/**
	 * Enables the adjustment of variables by authors directly in the results
	 * window
	 * 
	 * @param participantId
	 * @param variableName
	 * @param variableValue
	 * @return
	 */
	@Synchronized
	public boolean participantAdjustVariableValue(final ObjectId participantId,
			final String variableName, final String variableValue) {
		val participant = databaseManagerService
				.getModelObjectById(Participant.class, participantId);

		try {
			variablesManagerService.writeVariableValueOfParticipant(
					participant.getId(), variableName, variableValue);
		} catch (final Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * Enables the adjustment of variables values using services (e.g. specific
	 * communication managers)
	 * 
	 * @param dialogOptionType
	 * @param dialogOptionData
	 * @param variableName
	 * @param variableValue
	 * @return
	 */
	@Synchronized
	public boolean participantAdjustVariableValueExternallyBasedOnDialogOptionTypeAndData(
			final DialogOptionTypes dialogOptionType,
			final String dialogOptionData, final String variableName,
			final String variableValue) {
		val dialogOption = getDialogOptionByTypeAndDataOfActiveInterventions(
				dialogOptionType, dialogOptionData);

		if (dialogOption == null) {
			return false;
		}

		try {
			variablesManagerService.externallyWriteVariableForParticipant(
					dialogOption.getParticipant(), variableName, variableValue,
					false, true);
		} catch (final Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * Enables to remember last user login using services (e.g. specific
	 * communication managers)
	 * 
	 * @param dialogOptionType
	 * @param dialogOptionData
	 */
	public boolean participantRememberLoginBasedOnDialogOptionTypeAndData(
			final DialogOptionTypes dialogOptionType,
			final String dialogOptionData) {
		val dialogOption = getDialogOptionByTypeAndDataOfActiveInterventions(
				dialogOptionType, dialogOptionData);

		if (dialogOption == null) {
			return false;
		}

		try {
			val participant = databaseManagerService.getModelObjectById(
					Participant.class, dialogOption.getParticipant());

			if (participant != null) {
				participant.setLastLoginTimestamp(
						InternalDateTime.currentTimeMillis());

				databaseManagerService.saveModelObject(participant);
			}
		} catch (final Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * Enables to remember last user logout using services (e.g. specific
	 * communication managers)
	 * 
	 * @param dialogOptionType
	 * @param dialogOptionData
	 */
	public boolean participantRememberLogoutBasedOnDialogOptionTypeAndData(
			final DialogOptionTypes dialogOptionType,
			final String dialogOptionData) {
		val dialogOption = getDialogOptionByTypeAndDataOfActiveInterventions(
				dialogOptionType, dialogOptionData);

		if (dialogOption == null) {
			return false;
		}

		try {
			val participant = databaseManagerService.getModelObjectById(
					Participant.class, dialogOption.getParticipant());

			if (participant != null) {
				participant.setLastLogoutTimestamp(
						InternalDateTime.currentTimeMillis());

				databaseManagerService.saveModelObject(participant);
			}
		} catch (final Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * Create a statistics file
	 * 
	 * @param statisticsFile
	 * @throws IOException
	 */
	@Synchronized
	public void createStatistics(final File statisticsFile) throws IOException {
		final Properties statistics = new Properties() {
			private static final long serialVersionUID = -478652106406702866L;

			@Override
			public synchronized Enumeration<Object> keys() {
				return Collections
						.enumeration(new TreeSet<Object>(super.keySet()));
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
			val participants = databaseManagerService.findModelObjects(
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
				val dialogMessages = databaseManagerService.findModelObjects(
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
						case RECEIVED_AS_INTENTION:
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
			statistics.setProperty(
					"intervention." + intervention.getId().toString() + ".name",
					intervention.getName());

			statistics.setProperty(
					"intervention." + intervention.getId().toString()
							+ ".totalSentMessages",
					String.valueOf(totalSentMessages));
			statistics.setProperty(
					"intervention." + intervention.getId().toString()
							+ ".totalReceivedMessages",
					String.valueOf(totalReceivedMessages));
			statistics.setProperty(
					"intervention." + intervention.getId().toString()
							+ ".answeredQuestions",
					String.valueOf(answeredQuestions));
			statistics.setProperty(
					"intervention." + intervention.getId().toString()
							+ ".unansweredQuestions",
					String.valueOf(unansweredQuestions));
			statistics.setProperty(
					"intervention." + intervention.getId().toString()
							+ ".mediaObjectsViewed",
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

	/**
	 * Creates a participant or assigns a supervisor and adapts the belonging
	 * intervention structures without prior survey participation
	 * 
	 * @param nickname
	 * @param relatedParticipant
	 * @param externalIdDialogOptionData
	 * @param interventionPattern
	 * @param interventionPassword
	 * @param supervisorRequest
	 * @return
	 */
	@Synchronized
	public boolean checkAccessRightsAndRegisterParticipantOrSupervisorExternallyWithoutSurvey(
			final String nickname, final String relatedParticipantExternalId,
			final String externalIdDialogOptionData,
			final String interventionPattern, final String interventionPassword,
			final boolean supervisorRequest) {
		log.debug(
				"Checking external participant/supervisor creation without survey participation (intervention pattern: {})",
				interventionPattern);

		Intervention appropriateIntervention = null;
		for (val intervention : interventionAdministrationManagerService
				.getAllInterventions()) {
			if (intervention.isActive()
					&& intervention.getName().matches(interventionPattern)) {
				// Check deepstream password (or other external services in
				// upcoming version)
				if (externalIdDialogOptionData.startsWith(
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM)
						&& intervention.getDeepstreamPassword()
								.equals(interventionPassword)) {
					appropriateIntervention = intervention;
					break;
				}
			}
		}

		if (appropriateIntervention == null) {
			log.debug(
					"No appropriate intervention found for external registration");
			return false;
		}

		if (supervisorRequest) {
			val dialogOption = databaseManagerService.findOneModelObject(
					DialogOption.class, Queries.DIALOG_OPTION__BY_TYPE_AND_DATA,
					DialogOptionTypes.EXTERNAL_ID,
					relatedParticipantExternalId);

			if (dialogOption != null) {
				val participant = databaseManagerService.getModelObjectById(
						Participant.class, dialogOption.getParticipant());

				if (participant.getIntervention()
						.equals(appropriateIntervention.getId())) {
					log.debug(
							"Creating supervisor externally without survey participation (intervention: {}, participant: {})",
							appropriateIntervention.getId(),
							dialogOption.getParticipant());

					try {
						variablesManagerService.writeVariableValueOfParticipant(
								dialogOption.getParticipant(),
								SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES.participantSupervisorDialogOptionExternalID
										.toVariableName(),
								externalIdDialogOptionData, false, false);
					} catch (final Exception e) {
						log.error(
								"Should never occur: Error at writing participant dialog option external ID: {}",
								e.getMessage());
						return false;
					}

					return true;
				}
			}
		} else {
			log.debug(
					"Creating particpant externally without survey participation (intervention: {})",
					appropriateIntervention.getId());

			val creationTimestamp = InternalDateTime.currentTimeMillis();

			final val participant = new Participant(
					appropriateIntervention.getId(), creationTimestamp,
					creationTimestamp, creationTimestamp, "",
					Constants.getInterventionLocales()[0], null, null, null,
					null, null, true, "", "");

			databaseManagerService.saveModelObject(participant);

			try {
				variablesManagerService.writeVariableValueOfParticipant(
						participant.getId(),
						SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES.participantDialogOptionExternalID
								.toVariableName(),
						externalIdDialogOptionData, false, false);

				variablesManagerService
						.writeVariableValueOfParticipant(participant.getId(),
								SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES.participantName
										.toVariableName(),
								nickname, false, false);
			} catch (final Exception e) {
				log.error(
						"Should never occur: Error at writing participant dialog option external ID: {}",
						e.getMessage());
				databaseManagerService.deleteModelObject(participant);
				return false;
			}

			final long currentTimestamp = InternalDateTime.currentTimeMillis();
			final val dialogStatus = new DialogStatus(participant.getId(), "",
					null, null, currentTimestamp, true, currentTimestamp,
					currentTimestamp, true, 0, 0, false, 0);

			databaseManagerService.saveModelObject(dialogStatus);

			log.debug("Created participant {}", participant);

			return true;
		}

		return false;
	}

	/**
	 * Registers a new participant or supervisor external id for an existing
	 * {@link Participant}
	 * 
	 * @param participantIdToCreateUserFor
	 * @param relatedParticipantExternalId
	 * @param externalIdDialogOptionData
	 * @param supervisorRequest
	 * @return
	 */
	@Synchronized
	public boolean registerExternalDialogOptionForParticipantOrSupervisor(
			final ObjectId participantIdToCreateUserFor,
			final String relatedParticipantExternalId,
			final String externalIdDialogOptionData,
			final boolean supervisorRequest) {
		if (participantIdToCreateUserFor == null) {
			log.error(
					"Should never happen: No user given for the creation of external id dialog option");
			return false;
		}

		if (supervisorRequest) {
			val dialogOption = databaseManagerService.findOneModelObject(
					DialogOption.class, Queries.DIALOG_OPTION__BY_TYPE_AND_DATA,
					DialogOptionTypes.EXTERNAL_ID,
					relatedParticipantExternalId);

			if (dialogOption != null) {
				if (!dialogOption.getParticipant()
						.equals(participantIdToCreateUserFor)) {
					log.error(
							"Should never occur: Attempt to create supervisor for wrong/not fitting participant was made and rejected");
					return false;
				}

				try {
					variablesManagerService.writeVariableValueOfParticipant(
							dialogOption.getParticipant(),
							SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES.participantSupervisorDialogOptionExternalID
									.toVariableName(),
							externalIdDialogOptionData, false, false);
				} catch (final Exception e) {
					log.error(
							"Should never occur: Error at writing participant dialog option external ID: {}",
							e.getMessage());
					return false;
				}

				return true;
			}
		} else {
			try {
				variablesManagerService.writeVariableValueOfParticipant(
						participantIdToCreateUserFor,
						SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES.participantDialogOptionExternalID
								.toVariableName(),
						externalIdDialogOptionData, false, false);
			} catch (final Exception e) {
				log.error(
						"Should never occur: Error at writing participant dialog option external ID: {}",
						e.getMessage());
				return false;
			}

			return true;
		}

		return false;
	}

	/**
	 * Calculates appropriate hour for sending out message or activate micro
	 * dialog
	 * 
	 * @param participant
	 * @param monitoringRule
	 * @return
	 */
	@Synchronized
	public double calculateHourTeSendMessageOrActivateMicroDialog(
			final Participant participant,
			final MonitoringRule monitoringRule) {

		if (monitoringRule
				.getVariableForDecimalHourToSendMessageOrActivateMicroDialog() == null) {
			return monitoringRule.getHourToSendMessageOrActivateMicroDialog();
		} else {
			val variable = monitoringRule
					.getVariableForDecimalHourToSendMessageOrActivateMicroDialog();

			val stringValue = variablesManagerService
					.getParticipantOrInterventionVariableValueOfParticipant(
							participant, variable);

			if (stringValue == null) {
				log.warn(
						"Could not find variable {} for participant {} to calculate hour to send message or activate micro dialog",
						variable, participant.getId());

				return ImplementationConstants.FALLBACK_HOUR_TO_SEND_MESSAGE;
			} else {
				double doubleValue = ImplementationConstants.FALLBACK_HOUR_TO_SEND_MESSAGE;

				try {
					doubleValue = Double.parseDouble(stringValue);
				} catch (final Exception e) {
					log.warn(
							"Could not parse variable {} value {} for participant {} to calculate hour to send message or activate micro dialog: {}",
							variable, stringValue, participant.getId(),
							e.getMessage());

					return ImplementationConstants.FALLBACK_HOUR_TO_SEND_MESSAGE;
				}

				if (doubleValue < ImplementationConstants.HOUR_TO_SEND_MESSAGE_MIN) {
					doubleValue = ImplementationConstants.HOUR_TO_SEND_MESSAGE_MIN;
				} else if (doubleValue > ImplementationConstants.HOUR_TO_SEND_MESSAGE_MAX) {
					doubleValue = ImplementationConstants.HOUR_TO_SEND_MESSAGE_MAX;
				}

				return doubleValue;
			}
		}
	}

	/*
	 * PRIVATE Getter methods
	 */
	/**
	 * Returns a list of {@link ObjectId}s of {@link Participant}s that have
	 * messages that should be sent; Parameters therefore are:
	 *
	 * - the belonging intervention is active - the belonging intervention
	 * monitoring is active - the participant has monitoring active - the
	 * participant has all data for monitoring available - the participant has
	 * finished the screening survey - the participant not finished the
	 * monitoring - the message should have the status PREPARED_FOR_SENDING -
	 * the should be sent timestamp should be lower than the current time -->
	 * relevant, but will be checked later
	 *
	 * @return
	 */
	@Synchronized
	private List<ObjectId> getParticipantIdsWithMessagesWaitingToBeSent() {
		val participantsWithMessagesWaitingToBeSend = new ArrayList<ObjectId>();

		for (val intervention : databaseManagerService.findModelObjects(
				Intervention.class,
				Queries.INTERVENTION__ACTIVE_TRUE_MONITORING_ACTIVE_TRUE)) {
			for (val participantId : databaseManagerService.findModelObjectIds(
					Participant.class,
					Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
					intervention.getId())) {
				if (participantId != null) {
					val dialogStatus = databaseManagerService
							.findOneModelObject(DialogStatus.class,
									Queries.DIALOG_STATUS__BY_PARTICIPANT,
									participantId);

					if (dialogStatus != null
							&& dialogStatus
									.isDataForMonitoringParticipationAvailable()
							&& dialogStatus.isScreeningSurveyPerformed()
							&& !dialogStatus.isMonitoringPerformed()) {
						val dialogMessagesWaitingToBeSendOfParticipant = databaseManagerService
								.findModelObjects(DialogMessage.class,
										Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS,
										participantId,
										DialogMessageStatusTypes.PREPARED_FOR_SENDING);

						if (dialogMessagesWaitingToBeSendOfParticipant
								.iterator().hasNext()) {
							participantsWithMessagesWaitingToBeSend
									.add(participantId);
						}
					}
				}
			}
		}

		return participantsWithMessagesWaitingToBeSend;
	}

	/**
	 * Returns a list of {@link DialogMessage}s that should be sent; Parameters
	 * therefore are:
	 *
	 * - the belonging intervention is active - the belonging intervention
	 * monitoring is active - the participant has monitoring active - the
	 * participant has all data for monitoring available - the participant has
	 * finished the screening survey - the participant not finished the
	 * monitoring - the message should have the status PREPARED_FOR_SENDING -
	 * the should be sent timestamp should be lower than the current time
	 *
	 * @return
	 */
	@Synchronized
	private List<DialogMessageWithSenderIdentification> getDialogMessagesWithSenderWaitingToBeSentOfParticipant(
			final ObjectId participantId) {
		val dialogMessagesWaitingToBeSend = new ArrayList<DialogMessageWithSenderIdentification>();

		// Participant and intervention check has to be done again (due
		// to potential inconsistency because of missing
		// synchronization before)
		val participant = databaseManagerService
				.getModelObjectById(Participant.class, participantId);

		if (participant == null || !participant.isMonitoringActive()) {
			return dialogMessagesWaitingToBeSend;
		}

		val intervention = databaseManagerService.getModelObjectById(
				Intervention.class, participant.getIntervention());

		if (intervention == null || !intervention.isActive()
				|| !intervention.isMonitoringActive()) {
			return dialogMessagesWaitingToBeSend;
		}

		val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participantId);

		if (dialogStatus != null
				&& dialogStatus.isDataForMonitoringParticipationAvailable()
				&& dialogStatus.isScreeningSurveyPerformed()
				&& !dialogStatus.isMonitoringPerformed()) {
			val dialogMessagesWaitingToBeSendOfParticipant = databaseManagerService
					.findSortedModelObjects(DialogMessage.class,
							Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_SHOULD_BE_SENT_TIMESTAMP_LOWER,
							Queries.DIALOG_MESSAGE__SORT_BY_ORDER_ASC,
							participantId,
							DialogMessageStatusTypes.PREPARED_FOR_SENDING,
							InternalDateTime.currentTimeMillis());

			for (val dialogMessageWaitingToBeSendOfParticipant : dialogMessagesWaitingToBeSendOfParticipant) {
				dialogMessagesWaitingToBeSend
						.add(new DialogMessageWithSenderIdentification(
								dialogMessageWaitingToBeSendOfParticipant,
								intervention
										.getAssignedSenderIdentification()));
			}
		}

		return dialogMessagesWaitingToBeSend;
	}

	@Synchronized
	private Iterable<DialogMessage> getDialogMessagesOfParticipantUnansweredByParticipant(
			final ObjectId participantId) {
		val dialogMessages = databaseManagerService.findSortedModelObjects(
				DialogMessage.class,
				Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_UNANSWERED_AFTER_TIMESTAMP_LOWER,
				Queries.DIALOG_MESSAGE__SORT_BY_ORDER_ASC, participantId,
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
				Queries.DIALOG_MESSAGE__SORT_BY_ORDER_ASC, participantId,
				DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT);

		return dialogMessages;
	}

	@Synchronized
	private DialogMessage getDialogMessageOfParticipantWaitingForAnswer(
			final ObjectId participantId, final long timestampOfReceivedMessage,
			final int relatedMessageIdBasedOnOrder) {

		DialogMessage dialogMessage = null;
		if (relatedMessageIdBasedOnOrder >= 0) {
			val dialogMessageWithFittingOrder = databaseManagerService
					.findOneModelObject(DialogMessage.class,
							Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_ORDER,
							participantId, relatedMessageIdBasedOnOrder);

			if (dialogMessageWithFittingOrder
					.getStatus() == DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER
					&& !dialogMessageWithFittingOrder
							.isAnswerNotAutomaticallyProcessable()
					&& dialogMessageWithFittingOrder
							.getIsUnansweredAfterTimestamp() > timestampOfReceivedMessage) {
				dialogMessage = dialogMessageWithFittingOrder;
			}
		}

		if (dialogMessage == null) {
			dialogMessage = databaseManagerService.findOneSortedModelObject(
					DialogMessage.class,
					Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_NOT_AUTOMATICALLY_PROCESSABLE_AND_UNANSWERED_AFTER_TIMESTAMP_HIGHER,
					Queries.DIALOG_MESSAGE__SORT_BY_ORDER_ASC, participantId,
					DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER, false,
					timestampOfReceivedMessage);
		}

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
						if (participant
								.getCreatedTimestamp() > highestCreatedTimestamp) {
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
		val dialogOption = databaseManagerService.findOneModelObject(
				DialogOption.class,
				isSupervisorMessage
						? Queries.DIALOG_OPTION__FOR_SUPERVISOR_BY_PARTICIPANT
						: Queries.DIALOG_OPTION__FOR_PARTICIPANT_BY_PARTICIPANT,
				participantId);

		return dialogOption;
	}

	/**
	 * Returns a list of {@link Participant}s that are relevant for monitoring;
	 * Parameters therefore are:
	 *
	 * - the belonging intervention is active - the belonging intervention
	 * monitoring is active - the participant has monitoring active - the
	 * participant has all data for monitoring available --> relevant, but will
	 * be checked later - the participant has finished the screening survey -->
	 * relevant, but will be checked later - the participant not finished the
	 * monitoring --> relevant, but will be checked later
	 *
	 * @return
	 */
	@Synchronized
	private List<Participant> getAllParticipantsRelevantForAnsweredInTimeChecksAndMonitoringScheduling() {
		val relevantParticipants = new ArrayList<Participant>();

		for (val intervention : databaseManagerService.findModelObjects(
				Intervention.class,
				Queries.INTERVENTION__ACTIVE_TRUE_MONITORING_ACTIVE_TRUE)) {
			for (val participant : databaseManagerService.findModelObjects(
					Participant.class,
					Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
					intervention.getId())) {
				if (participant != null) {
					relevantParticipants.add(participant);
				}
			}
		}

		return relevantParticipants;
	}
}
