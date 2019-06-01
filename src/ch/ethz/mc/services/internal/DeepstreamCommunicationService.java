package ch.ethz.mc.services.internal;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.DeepstreamConstants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.memory.ExternalSystemMessage;
import ch.ethz.mc.model.memory.ExternalRegistration;
import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.memory.SystemLoad;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.InterventionExternalSystem;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.DialogMessageTypes;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.model.persistent.types.PushNotificationTypes;
import ch.ethz.mc.model.rest.Variable;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.tools.InternalDateTime;
import io.deepstream.ConfigOptions;
import io.deepstream.ConnectionState;
import io.deepstream.ConnectionStateListener;
import io.deepstream.DeepstreamClient;
import io.deepstream.DeepstreamRuntimeErrorHandler;
import io.deepstream.Event;
import io.deepstream.HasResult;
import io.deepstream.LoginResult;
import io.deepstream.PresenceEventListener;
import io.deepstream.Record;
import io.deepstream.SnapshotResult;
import io.deepstream.Topic;
import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Handles the communication with a deepstream server
 * 
 * @author Andreas Filler
 */
@Log4j2
public class DeepstreamCommunicationService extends Thread
		implements PresenceEventListener, DeepstreamRuntimeErrorHandler,
		ConnectionStateListener {
	@Getter
	private static DeepstreamCommunicationService	instance			= null;
	private final CommunicationManagerService		communicationManagerService;

	private final SystemLoad						systemLoad;

	private boolean									running				= true;
	private boolean									shouldStop			= false;

	private final File								connectionStateFile;

	private InterventionExecutionManagerService		interventionExecutionManagerService;

	private RESTManagerService						restManagerService;

	private final Set<String>						loggedInParticipants;
	private final Set<String>						loggedInSupervisors;
	private final Set<String>						loggedInTeamManagers;
	private final Set<String>						loggedInObservers;
	private final Set<String>						loggedInExternalSystems;

	private final Hashtable<String, Integer>		allUsersVisibleMessagesSentSinceLastLogout;

	private final int								substringLength;

	private final List<ReceivedMessage>				receivedMessages;
	private final List<ExternalSystemMessage>		receivedExternalSystemMessages;

	private DeepstreamClient						client				= null;
	private final String							host;
	private final JsonObject						loginData;

	private boolean									startupComplete		= false;
	private int										startupAttempt		= 0;
	private boolean									restStartupComplete	= false;
	private boolean									reconnecting		= false;

	private final Gson								gson;

	private final String							participantRole;
	private final String							supervisorRole;
	private final String							teamManagerRole;
	private final String							observerRole;
	private final String							externalSystemRole;

	private DeepstreamCommunicationService(final String deepstreamHost,
			final String deepstreamServerPassword,
			final CommunicationManagerService communicationManagerService) {
		this.communicationManagerService = communicationManagerService;

		systemLoad = SystemLoad.getInstance();

		loggedInParticipants = new HashSet<String>();
		loggedInSupervisors = new HashSet<String>();
		loggedInTeamManagers = new HashSet<String>();
		loggedInObservers = new HashSet<String>();
		loggedInExternalSystems = new HashSet<String>();

		allUsersVisibleMessagesSentSinceLastLogout = new Hashtable<String, Integer>();

		// Care for connection state file
		connectionStateFile = new File(
				new File(Constants.getStatisticsFile()).getParentFile(),
				"ds_connected");
		if (connectionStateFile.exists()) {
			try {
				connectionStateFile.delete();
			} catch (final Exception e) {
				log.warn("Error when deleting connection state file: {}",
						e.getMessage());
			}
		}

		host = deepstreamHost;

		participantRole = ImplementationConstants.DEEPSTREAM_PARTICIPANT_ROLE;
		supervisorRole = ImplementationConstants.DEEPSTREAM_SUPERVISOR_ROLE;
		teamManagerRole = ImplementationConstants.DEEPSTREAM_TEAM_MANAGER_ROLE;
		observerRole = ImplementationConstants.DEEPSTREAM_OBSERVER_ROLE;
		externalSystemRole = ImplementationConstants.DEEPSTREAM_EXTERNAL_SYSTEM_ROLE;

		loginData = new JsonObject();
		loginData.addProperty(DeepstreamConstants.REST_FIELD_CLIENT_VERSION,
				Constants.getDeepstreamMinClientVersion());
		loginData.addProperty(DeepstreamConstants.REST_FIELD_USER,
				ImplementationConstants.DEEPSTREAM_SERVER_ROLE);
		loginData.addProperty(DeepstreamConstants.REST_FIELD_SECRET,
				deepstreamServerPassword);
		loginData.addProperty(DeepstreamConstants.REST_FIELD_ROLE,
				ImplementationConstants.DEEPSTREAM_SERVER_ROLE);
		loginData.addProperty(
				DeepstreamConstants.REST_FIELD_INTERVENTION_PASSWORD,
				"not required");

		gson = new Gson();

		substringLength = ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
				.length();

		receivedMessages = new ArrayList<ReceivedMessage>();
		receivedExternalSystemMessages = new ArrayList<>();
	}

	public static DeepstreamCommunicationService prepare(
			final String deepstreamHost, final String deepstreamServerPassword,
			final CommunicationManagerService communicationManagerService) {
		log.info("Preparing service...");
		if (instance == null) {
			instance = new DeepstreamCommunicationService(deepstreamHost,
					deepstreamServerPassword, communicationManagerService);

			instance.setName(
					DeepstreamCommunicationService.class.getSimpleName());
			instance.start();
		}
		log.info("Prepared.");
		return instance;
	}

	@Synchronized
	public void startThreadedService(
			final InterventionExecutionManagerService interventionExecutionManagerService)
			throws Exception {
		log.info("Starting service...");

		this.interventionExecutionManagerService = interventionExecutionManagerService;

		if (!connectOrReconnect()) {
			throw new Exception(
					"A problem when connecting to deepstream at startup occurred.");
		}

		log.info("Started.");
	}

	@Override
	public void run() {
		long nextLoadInfo = System.currentTimeMillis() + 10000;

		while (!shouldStop) {
			try {
				sleep(500);
			} catch (final InterruptedException e) {
				// Do nothing
			}

			// Update load info every 10 seconds
			if (nextLoadInfo < System.currentTimeMillis()) {
				nextLoadInfo = System.currentTimeMillis() + 10000;
				systemLoad.setLoggedInUsers(
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
								+ participantRole,
						loggedInParticipants.size());
				systemLoad.setLoggedInUsers(
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
								+ supervisorRole,
						loggedInSupervisors.size());
				systemLoad.setLoggedInUsers(
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
								+ teamManagerRole,
						loggedInTeamManagers.size());
				systemLoad.setLoggedInUsers(
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
								+ observerRole,
						loggedInObservers.size());
				systemLoad.setLoggedInUsers(
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
								+ externalSystemRole,
						loggedInExternalSystems.size());
			}
		}

		// Stop service
		try {
			cleanupClient();
			client = null;
		} catch (final Exception e) {
			log.warn("Could not close deepstream connection: {}",
					e.getMessage());
		}

		running = false;
	}

	@Synchronized
	public void stopThreadedService() throws Exception {
		log.info("Stopping service...");

		// Care for connection state file
		if (connectionStateFile.exists()) {
			try {
				connectionStateFile.delete();
			} catch (final Exception e) {
				log.warn("Error when deleting connection state file: {}",
						e.getMessage());
			}
		}

		shouldStop = true;
		interrupt();

		while (running) {
			sleep(500);
		}

		log.info("Stopped.");
	}

	/*
	 * Public methods
	 */
	/**
	 * Enables the REST interface to inform this server about it's own startup
	 * 
	 * @param restManagerService
	 */
	public void RESTInterfaceStarted(
			final RESTManagerService restManagerService) {
		this.restManagerService = restManagerService;
		restStartupComplete = true;
	}

	/**
	 * Queue message for sending using deepstream
	 * 
	 * @param dialogOption
	 * @param dialogMessageId
	 * @return Number of visible message sent to this user since the last logout
	 *         or zero if no message has been sent
	 */
	@Synchronized
	public int asyncSendMessage(final DialogOption dialogOption,
			final ObjectId dialogMessageId) {
		log.debug("Sending message {}", dialogMessageId);

		val dialogMessage = interventionExecutionManagerService
				.dialogMessageStatusChangesForSending(dialogMessageId,
						DialogMessageStatusTypes.SENDING,
						InternalDateTime.currentTimeMillis());

		val timestamp = InternalDateTime.currentTimeMillis();
		int messagesSentSinceLastLogout = 0;

		Record record = null;
		synchronized (client) {
			try {
				val participantOrSupervisorIdentifier = dialogOption.getData()
						.substring(substringLength);

				val isCommand = dialogMessage
						.getType() == DialogMessageTypes.COMMAND;

				val messageObject = new JsonObject();
				messageObject.addProperty(DeepstreamConstants.ID,
						dialogMessage.getOrder());
				messageObject.addProperty(DeepstreamConstants.STATUS,
						DeepstreamConstants.STATUS_SENT_BY_SERVER);
				messageObject.addProperty(DeepstreamConstants.TYPE,
						isCommand ? DeepstreamConstants.TYPE_COMMAND
								: DeepstreamConstants.TYPE_PLAIN);
				if (!StringUtils.isBlank(dialogMessage.getSurveyLink())) {
					messageObject.addProperty(
							DeepstreamConstants.CONTAINS_SURVEY,
							dialogMessage.getSurveyLink());
				}
				if (!StringUtils.isBlank(dialogMessage.getMediaObjectLink())) {
					messageObject.addProperty(
							DeepstreamConstants.CONTAINS_MEDIA,
							dialogMessage.getMediaObjectLink());
				}
				if (!StringUtils.isBlank(dialogMessage.getMediaObjectName())) {
					messageObject.addProperty(DeepstreamConstants.MEDIA_NAME,
							dialogMessage.getMediaObjectName());
				}
				if (dialogMessage.getMediaObjectType() != null) {
					messageObject.addProperty(DeepstreamConstants.MEDIA_TYPE,
							dialogMessage.getMediaObjectType().toJSONField());
				}
				if (!StringUtils.isBlank(
						dialogMessage.getTextBasedMediaObjectContent())) {
					messageObject.addProperty(DeepstreamConstants.CONTENT,
							dialogMessage.getTextBasedMediaObjectContent());
				} else {
					messageObject.addProperty(DeepstreamConstants.CONTENT, "");
				}
				if (isCommand) {
					messageObject.addProperty(
							DeepstreamConstants.SERVER_MESSAGE,
							dialogMessage.getMessage().replaceAll("[ ]?"
									+ ImplementationConstants.PLACEHOLDER_LINKED_MEDIA_OBJECT
									+ "[ ]?", "")
									.replaceAll("[ ]?"
											+ ImplementationConstants.PLACEHOLDER_LINKED_SURVEY
											+ "[ ]?", ""));
				} else {
					messageObject.addProperty(
							DeepstreamConstants.SERVER_MESSAGE,
							dialogMessage.getMessage());
				}
				messageObject.addProperty(DeepstreamConstants.FORMAT,
						dialogMessage.getTextFormat().toString());
				val answerType = dialogMessage.getAnswerType();
				if (answerType != null) {
					val answerTypeMessageObject = new JsonObject();
					answerTypeMessageObject.addProperty(
							DeepstreamConstants.TYPE, answerType.toJSONField());
					val answerOptions = dialogMessage.getAnswerOptions();
					if (answerType.isKeyValueBased()) {
						answerTypeMessageObject.add(DeepstreamConstants.OPTIONS,
								gson.fromJson(answerOptions,
										JsonElement.class));
					} else {
						answerTypeMessageObject.addProperty(
								DeepstreamConstants.OPTIONS, answerOptions);
					}
					messageObject.add(DeepstreamConstants.ANSWER_FORMAT,
							answerTypeMessageObject);
				}
				messageObject.addProperty(DeepstreamConstants.MESSAGE_TIMESTAMP,
						timestamp);
				messageObject.addProperty(DeepstreamConstants.EXPECTS_ANSWER,
						dialogMessage.isMessageExpectsAnswer());
				messageObject.addProperty(DeepstreamConstants.LAST_MODIFIED,
						timestamp);
				messageObject.addProperty(DeepstreamConstants.STICKY,
						dialogMessage.isMessageIsSticky());
				messageObject.addProperty(DeepstreamConstants.DEACTIVATION,
						dialogMessage.isMessageDeactivatesAllOpenQuestions());

				record = client.record
						.getRecord(DeepstreamConstants.PATH_MESSAGES
								+ participantOrSupervisorIdentifier);

				record.set(
						DeepstreamConstants.PATH_LIST
								+ String.valueOf(dialogMessage.getOrder()),
						messageObject);

				client.event.emit(
						DeepstreamConstants.PATH_MESSAGE_UPDATE
								+ participantOrSupervisorIdentifier,
						messageObject);

				// If it's not a visible message ignore it
				if (!isCommand) {
					synchronized (allUsersVisibleMessagesSentSinceLastLogout) {
						if (loggedInParticipants
								.contains(participantOrSupervisorIdentifier)
								|| loggedInSupervisors.contains(
										participantOrSupervisorIdentifier)) {
							// If user is logged in remember as one (for late
							// logout
							// users)
							messagesSentSinceLastLogout = 1;
						} else if (allUsersVisibleMessagesSentSinceLastLogout
								.containsKey(
										participantOrSupervisorIdentifier)) {
							// User is not logged in and well known
							messagesSentSinceLastLogout = allUsersVisibleMessagesSentSinceLastLogout
									.get(participantOrSupervisorIdentifier) + 1;
						} else {
							// User is not logged in and not known
							messagesSentSinceLastLogout = 1;
						}
						allUsersVisibleMessagesSentSinceLastLogout.put(
								participantOrSupervisorIdentifier,
								messagesSentSinceLastLogout);
					}
				}
			} catch (final Exception e) {
				log.warn("Could not send message to {}: {}",
						dialogOption.getData(), e.getMessage());

				interventionExecutionManagerService
						.dialogMessageStatusChangesForSending(dialogMessageId,
								DialogMessageStatusTypes.PREPARED_FOR_SENDING,
								timestamp);

				return 0;
			} finally {
				if (record != null) {
					try {
						record.discard();
					} catch (final Exception e) {
						log.warn("Could not discard record on message sending");
					}
				}
			}
		}

		if (dialogMessage.isMessageExpectsAnswer()) {
			interventionExecutionManagerService
					.dialogMessageStatusChangesForSending(dialogMessageId,
							DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER,
							timestamp);
		} else {
			interventionExecutionManagerService
					.dialogMessageStatusChangesForSending(dialogMessageId,
							DialogMessageStatusTypes.SENT_BUT_NOT_WAITING_FOR_ANSWER,
							timestamp);
		}

		log.debug("Message {} sent", dialogMessageId);

		return messagesSentSinceLastLogout;
	}

	/**
	 * Acknowledge retrieval of a message
	 * 
	 * @param dialogMessage
	 * @param receivedMessage
	 */
	@Synchronized
	public void asyncAcknowledgeMessage(final DialogMessage dialogMessage,
			final ReceivedMessage receivedMessage) {
		log.debug("Acknowledging message {}", dialogMessage.getId());

		Record record = null;
		synchronized (client) {
			try {
				val timestamp = InternalDateTime.currentTimeMillis();

				val participantIdentifier = receivedMessage.getSender()
						.substring(substringLength);

				record = client.record
						.getRecord(DeepstreamConstants.PATH_MESSAGES
								+ participantIdentifier);

				val messageOrder = dialogMessage.getOrder();

				val retrievedMessageObject = record
						.get(DeepstreamConstants.PATH_LIST
								+ String.valueOf(messageOrder));

				final JsonObject messageConfirmationObject;
				final JsonObject messageObject;
				if (retrievedMessageObject instanceof JsonNull) {
					// No separate confirmation required
					messageConfirmationObject = null;

					// No related former message by user
					messageObject = new JsonObject();

					messageObject.addProperty(DeepstreamConstants.STATUS,
							DeepstreamConstants.STATUS_SENT_BY_USER);
					switch (dialogMessage.getType()) {
						case PLAIN:
							messageObject.addProperty(DeepstreamConstants.TYPE,
									DeepstreamConstants.TYPE_PLAIN);
							break;
						case INTENTION:
							messageObject.addProperty(DeepstreamConstants.TYPE,
									DeepstreamConstants.TYPE_INTENTION);
							break;
						default:
							log.error(
									"A message of a type {} should never be acknowledged",
									dialogMessage.getType());
							break;
					}
					messageObject.addProperty(DeepstreamConstants.ID,
							messageOrder);
				} else {
					// Prepare confirmation for message
					messageConfirmationObject = new JsonObject();

					messageConfirmationObject.addProperty(
							DeepstreamConstants.STATUS,
							DeepstreamConstants.STATUS_SENT_BY_USER);
					switch (dialogMessage.getType()) {
						case PLAIN:
							messageConfirmationObject.addProperty(
									DeepstreamConstants.TYPE,
									DeepstreamConstants.TYPE_PLAIN);
							break;
						case INTENTION:
							messageConfirmationObject.addProperty(
									DeepstreamConstants.TYPE,
									DeepstreamConstants.TYPE_INTENTION);
							break;
						default:
							log.error(
									"A message of a type {} should never be acknowledged",
									dialogMessage.getType());
							break;
					}
					messageConfirmationObject.addProperty(
							DeepstreamConstants.ID,
							"c-" + receivedMessage.getReceivedTimestamp());

					// Former message will be extended afterwards
					messageObject = (JsonObject) retrievedMessageObject;
					messageObject.addProperty(DeepstreamConstants.STATUS,
							DeepstreamConstants.STATUS_ANSWERED_BY_USER);
				}

				if (receivedMessage.getMessage() != null) {
					messageObject.addProperty(DeepstreamConstants.USER_MESSAGE,
							receivedMessage.getMessage());
				}
				if (receivedMessage.getIntention() != null) {
					messageObject.addProperty(
							DeepstreamConstants.USER_INTENTION,
							receivedMessage.getMessage());
				}
				if (receivedMessage.getContent() != null) {
					messageObject.addProperty(DeepstreamConstants.USER_CONTENT,
							receivedMessage.getContent());
				}
				if (receivedMessage.getText() != null) {
					messageObject.addProperty(DeepstreamConstants.USER_TEXT,
							receivedMessage.getText());
				}
				messageObject.addProperty(DeepstreamConstants.USER_TIMESTAMP,
						receivedMessage.getClientTimestamp());
				messageObject.addProperty(DeepstreamConstants.LAST_MODIFIED,
						timestamp);

				if (messageConfirmationObject != null) {
					if (receivedMessage.getMessage() != null) {
						messageConfirmationObject.addProperty(
								DeepstreamConstants.USER_MESSAGE,
								receivedMessage.getMessage());
					}
					if (receivedMessage.getIntention() != null) {
						messageConfirmationObject.addProperty(
								DeepstreamConstants.USER_INTENTION,
								receivedMessage.getMessage());
					}
					if (receivedMessage.getContent() != null) {
						messageConfirmationObject.addProperty(
								DeepstreamConstants.USER_CONTENT,
								receivedMessage.getContent());
					}
					if (receivedMessage.getText() != null) {
						messageConfirmationObject.addProperty(
								DeepstreamConstants.USER_TEXT,
								receivedMessage.getText());
					}
					messageConfirmationObject.addProperty(
							DeepstreamConstants.USER_TIMESTAMP,
							receivedMessage.getClientTimestamp());
					messageConfirmationObject.addProperty(
							DeepstreamConstants.LAST_MODIFIED, timestamp);
					if (receivedMessage.getMediaURL() != null
							&& receivedMessage.getMediaType() != null) {
						messageConfirmationObject.addProperty(
								DeepstreamConstants.CONTAINS_MEDIA,
								receivedMessage.getMediaURL());
						messageConfirmationObject.addProperty(
								DeepstreamConstants.MEDIA_TYPE,
								receivedMessage.getMediaType());
					}
				}

				if (messageConfirmationObject != null) {
					record.set(
							DeepstreamConstants.PATH_LIST
									+ String.valueOf("c-" + timestamp),
							messageConfirmationObject);

					client.event.emit(
							DeepstreamConstants.PATH_MESSAGE_UPDATE
									+ participantIdentifier,
							messageConfirmationObject);
				}

				record.set(DeepstreamConstants.PATH_LIST
						+ String.valueOf(messageOrder), messageObject);

				client.event.emit(DeepstreamConstants.PATH_MESSAGE_UPDATE
						+ participantIdentifier, messageObject);
			} catch (final Exception e) {
				log.warn("Could not acknowledge message to {}: {}",
						receivedMessage.getSender(), e.getMessage());
			} finally {
				if (record != null) {
					try {
						record.discard();
					} catch (final Exception e) {
						log.warn(
								"Could not discard record on acknoledging message");
					}
				}
			}
		}

		log.debug("Message {} acknowledged", dialogMessage.getId());
	}

	/**
	 * Inform about answering timeout of a message
	 * 
	 * @param dialogOption
	 * @param dialogMessage
	 */
	@Synchronized
	public void asyncInformAboutAnsweringTimeout(
			final DialogOption dialogOption,
			final DialogMessage dialogMessage) {
		log.debug("Informing about timeout of message {}",
				dialogMessage.getId());

		Record record = null;
		synchronized (client) {
			try {
				val timestamp = InternalDateTime.currentTimeMillis();

				val participantIdentifier = dialogOption.getData()
						.substring(substringLength);

				record = client.record
						.getRecord(DeepstreamConstants.PATH_MESSAGES
								+ participantIdentifier);

				val messageOrder = dialogMessage.getOrder();

				val retrievedMessageObject = record
						.get(DeepstreamConstants.PATH_LIST
								+ String.valueOf(messageOrder));

				// Former message will be extended
				val messageObject = (JsonObject) retrievedMessageObject;

				messageObject.addProperty(DeepstreamConstants.STATUS,
						DeepstreamConstants.STATUS_NOT_ANSWERED_BY_USER);
				messageObject.addProperty(DeepstreamConstants.LAST_MODIFIED,
						timestamp);

				record.set(DeepstreamConstants.PATH_LIST
						+ String.valueOf(messageOrder), messageObject);

				client.event.emit(DeepstreamConstants.PATH_MESSAGE_UPDATE
						+ participantIdentifier, messageObject);
			} catch (final Exception e) {
				log.warn("Could not inform about answering timeout: {}",
						e.getMessage());
			} finally {
				if (record != null) {
					try {
						record.discard();
					} catch (final Exception e) {
						log.warn(
								"Could not discard record on sending answering timeout");
					}
				}
			}
		}

		log.debug("Informed about timeout of message {}",
				dialogMessage.getId());
	}

	/**
	 * Get all messages received by deepstream since the last check
	 * 
	 * @param receivedMessage
	 */
	public void getReceivedMessages(
			final List<ReceivedMessage> receivedMessage) {
		synchronized (receivedMessages) {
			receivedMessage.addAll(receivedMessages);
			receivedMessages.clear();
		}
	}

	/**
	 * Get all external system messages received by deepstream since the last
	 * check
	 * 
	 * @param receivedMessages
	 */
	public void getReceivedExternalSystemMessages(
			final List<ExternalSystemMessage> receivedMessages) {
		synchronized (receivedExternalSystemMessages) {
			receivedMessages.addAll(receivedExternalSystemMessages);
			receivedExternalSystemMessages.clear();
		}
	}

	/**
	 * Check secret of user using deepstream
	 * 
	 * @param participantOrSupervisorIdentifier
	 * @param secret
	 * @return
	 */
	public boolean checkSecret(final String participantOrSupervisorIdentifier,
			final String secret) {
		return checkSecret(participantOrSupervisorIdentifier, secret, -1);
	}

	/**
	 * Check secret of user using deepstream
	 * 
	 * @param participantOrSupervisorIdentifier
	 * @param secret
	 * @param charactersToCheck
	 *            The observer check only compares the first x characters of the
	 *            secret, or all in case of -1
	 * @return
	 */
	public boolean checkSecret(final String participantOrSupervisorIdentifier,
			final String secret, final int charactersToCheck) {
		log.debug("Checking secret for {} (characters to check: {})",
				participantOrSupervisorIdentifier, charactersToCheck);

		Record record = null;
		try {
			String secretFromRecord;
			synchronized (client) {
				record = client.record
						.getRecord(DeepstreamConstants.PATH_MESSAGES
								+ participantOrSupervisorIdentifier);

				secretFromRecord = record.get(DeepstreamConstants.SECRET)
						.getAsString();
			}

			if (StringUtils.isBlank(secretFromRecord)) {
				log.debug("Secret check for {} returns {}",
						participantOrSupervisorIdentifier, false);
				return false;
			}

			if (charactersToCheck == -1 ? secretFromRecord.equals(secret)
					: secretFromRecord.substring(0, charactersToCheck)
							.equals(secret)) {
				log.debug("Secret check for {} returns {}",
						participantOrSupervisorIdentifier, true);
				return true;
			} else {
				log.debug("Secret check for {} returns {}",
						participantOrSupervisorIdentifier, false);
				return false;
			}
		} catch (final Exception e) {
			log.warn("Could not check secret of participant/supervisor {}",
					participantOrSupervisorIdentifier);
		} finally {
			if (record != null) {
				try {
					record.discard();
				} catch (final Exception e) {
					log.warn("Could not discard record on secret check");
				}
			}
		}
		log.debug("Secret check for {} returns {}",
				participantOrSupervisorIdentifier, true);
		return true;
	}

	public boolean checkExternalSystemToken(final String systemId,
			final String token) {
		log.debug("Checking system token for {}", systemId);

		Record record = null;
		try {
			String secretFromRecord;
			synchronized (client) {

				String recordName = DeepstreamConstants.PATH_EXTERNAL_SYSTEMS
						+ systemId;
				HasResult hasResult = client.record.has(recordName);
				if (!hasResult.hasError() && hasResult.getResult()) {
					record = client.record.getRecord(recordName);

					secretFromRecord = record.get(DeepstreamConstants.TOKEN)
							.getAsString();
				} else {
					log.debug("Token check for system {} returns {}",
							systemId, false);
					return false;
				}
			}

			if (StringUtils.isBlank(secretFromRecord)) {
				log.debug("Token check for system {} returns {}", systemId,
						false);
				return false;
			}

			if (secretFromRecord.equals(token)) {
				log.debug("Token check for system {} returns {}", systemId,
						true);
				return true;
			} else {
				log.debug("Token check for system {} returns {}", systemId,
						false);
				return false;
			}
		} catch (final Exception e) {
			log.warn("Could not check token of system {}", systemId);
		} finally {
			if (record != null) {
				try {
					record.discard();
				} catch (final Exception e) {
					log.warn("Could not discard record on token check");
				}
			}
		}
		log.debug("Token check for system {} returns {}", systemId, false);
		return false;
	}

	/**
	 * Creates a deepstream participant/supervisor and the belonging
	 * intervention participant structures (or reuses them if they already
	 * exist); Returns null if not allowed
	 * 
	 * @param nickname
	 * @param participantIdToCreateUserFor
	 * @param relatedParticipantExternalId
	 * @param interventionPattern
	 * @param interventionPassword
	 * @param supervisorRequest
	 * @return
	 */
	public ExternalRegistration registerUser(final String nickname,
			final ObjectId participantIdToCreateUserFor,
			final String relatedParticipantExternalId,
			final String interventionPattern, final String interventionPassword,
			final boolean supervisorRequest) {
		log.debug("Trying to register user for {}",
				participantIdToCreateUserFor);

		// TODO: No check anymore. Former implementation lead to freeze on DS
		// side.
		// do {
		// participantOrSupervisorExternalId = RandomStringUtils
		// .randomAlphanumeric(32);
		// } while (client.record.has(DeepstreamConstants.PATH_MESSAGES
		// + participantOrSupervisorExternalId).getResult());

		// Prepare deepstream and reserve unique ID
		final String participantOrSupervisorExternalId = RandomStringUtils
				.randomAlphanumeric(25) + System.currentTimeMillis()
				+ RandomStringUtils.randomAlphanumeric(25);
		final String secret = RandomStringUtils.randomAlphanumeric(128);

		Record record = null;
		synchronized (client) {
			try {
				record = client.record
						.getRecord(DeepstreamConstants.PATH_MESSAGES
								+ participantOrSupervisorExternalId);
				record.set(DeepstreamConstants.SECRET, secret);
				record.set(DeepstreamConstants.ROLE,
						supervisorRequest ? supervisorRole : participantRole);
			} finally {
				if (record != null) {
					try {
						record.discard();
					} catch (final Exception e) {
						log.warn(
								"Could not discard record on user registration");
					}
				}
			}
		}

		boolean createdSucessfully = false;
		try {
			if (participantIdToCreateUserFor != null) {
				// Extend existing participant
				createdSucessfully = interventionExecutionManagerService
						.registerExternalDialogOptionForParticipantOrSupervisor(
								participantIdToCreateUserFor,
								ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
										+ relatedParticipantExternalId,
								ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
										+ participantOrSupervisorExternalId,
								supervisorRequest);
			} else {
				// Create new participant
				createdSucessfully = interventionExecutionManagerService
						.checkAccessRightsAndRegisterParticipantOrSupervisorExternallyWithoutSurvey(
								nickname,
								ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
										+ relatedParticipantExternalId,
								ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
										+ participantOrSupervisorExternalId,
								interventionPattern, interventionPassword,
								supervisorRequest);
			}
		} finally {
			// Cleanup prepared user if registration was not possible
			if (!createdSucessfully) {
				record = null;
				synchronized (client) {
					try {
						record = client.record
								.getRecord(DeepstreamConstants.PATH_MESSAGES
										+ participantOrSupervisorExternalId);

						record.delete();
					} finally {
						if (record != null) {
							try {
								record.discard();
							} catch (final Exception e) {
								log.warn(
										"Could not discard record on user registration");
							}
						}
					}

				}

				return null;
			}
		}

		log.debug("User registered for {}", participantIdToCreateUserFor);

		return new ExternalRegistration(participantOrSupervisorExternalId,
				secret);
	}

	public ExternalRegistration registerExternalSystem(
			final String externalSystemName) {
		
		log.debug("Trying to register external system for {}",
				externalSystemName);

		Record record = null;
		String externalSystemId;
		String token;
		synchronized (client) {
			try {
				// Generate token and UID.
				token = RandomStringUtils.randomAlphanumeric(128);
				externalSystemId = client.getUid();
				// Write token and role to record with path: "external-systems/[systemId]".
				record = client.record
						.getRecord(DeepstreamConstants.PATH_EXTERNAL_SYSTEMS
								+ externalSystemId);
				record.set(DeepstreamConstants.TOKEN, token);
				record.set(DeepstreamConstants.ROLE, externalSystemRole);
			} finally {
				// If something goes wrong discard record.
				if (record != null) {
					try {
						record.discard();
					} catch (final Exception e) {
						log.warn(
								"Could not discard record on external system registration");
					}
				}
			}
		}
		log.debug("External system registered for {}", externalSystemName);

		return new ExternalRegistration(externalSystemId, token);
	}

	public void deleteExternalSystem(
			final InterventionExternalSystem externalSystem) {

		log.debug("Trying to delete external system {}",
				externalSystem.getName());

		Record record = null;
		synchronized (client) {
			String recordName = DeepstreamConstants.PATH_EXTERNAL_SYSTEMS
					+ externalSystem.getSystemId();

			HasResult hasResult = client.record.has(recordName);
			if (!hasResult.hasError() && hasResult.getResult()) {

				record = client.record.getRecord(recordName);
				record.delete();
				log.debug("External system {} deleted",
						externalSystem.getName());
			} else {
				log.warn("External system record {} could not be deleted",
						recordName);
			}
		}
	}

	public String renewExternalSystemToken(
			final InterventionExternalSystem externalSystem) {

		log.debug("Trying to renew token from external system {}",
				externalSystem.getName());

		Record record = null;
		String token = null;
		synchronized (client) {
			String recordName = DeepstreamConstants.PATH_EXTERNAL_SYSTEMS
					+ externalSystem.getSystemId();

			HasResult hasResult = client.record.has(recordName);
			if (!hasResult.hasError() && hasResult.getResult()) {
				token = RandomStringUtils.randomAlphanumeric(128);

				record = client.record.getRecord(recordName);
				record.set(DeepstreamConstants.TOKEN, token);
				log.debug("Token renewed from external system {}",
						externalSystem.getName());
			} else {
				log.warn(
						"External system record {} could not be retrieved. Can't renew token",
						recordName);
			}
		}
		return token;
	}

	/**
	 * Checks if participant is currently connected
	 * 
	 * @param participantId
	 * @return
	 */
	public boolean checkIfParticipantIsConnected(final String participantId) {
		if (loggedInParticipants.contains(participantId)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Cleans up deepstream based on a specific participant/supervisor
	 * 
	 * @param participantOrSupervisorId
	 */
	public void cleanupForParticipantOrSupervisor(
			final String participantOrSupervisorId) {

		Record record = null;
		synchronized (client) {
			try {
				restManagerService.destroyParticipantToken(
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
								+ participantOrSupervisorId);
			} catch (final Exception e) {
				log.warn(
						"Could not cleanup deepstream token for participant/supervisor {}",
						participantOrSupervisorId);
			}
			try {
				record = client.record
						.getRecord(DeepstreamConstants.PATH_MESSAGES
								+ participantOrSupervisorId);

				record.delete();
			} catch (final Exception e) {
				log.warn(
						"Could not cleanup deepstream participant messages for participant/supervisor {}",
						participantOrSupervisorId);
			} finally {
				if (record != null) {
					try {
						record.discard();
					} catch (final Exception e) {
						log.warn("Could not discard record on cleanup");
					}
				}
			}
			try {
				record = client.record
						.getRecord(DeepstreamConstants.PATH_DASHBOARD
								+ participantOrSupervisorId);

				record.delete();
			} catch (final Exception e) {
				log.warn(
						"Could not cleanup deepstream dashboard messages for participant/supervisor {}",
						participantOrSupervisorId);
			} finally {
				if (record != null) {
					try {
						record.discard();
					} catch (final Exception e) {
						log.warn("Could not discard record on cleanup");
					}
				}
			}
		}
	}

	/*
	 * Class methods
	 */
	/**
	 * Connects/reconnects to the deepstream server
	 * 
	 * @throws Exception
	 */
	private boolean connectOrReconnect() throws Exception {
		if (!reconnecting) {
			while (!restStartupComplete) {
				log.debug("Waiting for REST interface to come up...");

				try {
					Thread.sleep(1000);
				} catch (final Exception e) {
					// Ignore
				}
			}
			log.info("Connecting to deepstream...");
		} else {
			log.info("Reconnecting to deepstream...");

			try {
				Thread.sleep(1000);
			} catch (final Exception e) {
				// Ignore
			}
		}

		final Properties properties = new Properties();
		properties.setProperty(ConfigOptions.MAX_RECONNECT_ATTEMPTS.toString(),
				"0");

		client = new DeepstreamClient(host, properties);
		synchronized (client) {
			client.setRuntimeErrorHandler(this);
			client.addConnectionChangeListener(this);
		}

		LoginResult result = null;
		log.debug("Trying to login...");
		result = client.login(loginData);

		if (!result.loggedIn()) {
			log.warn("Login failed.");
			cleanupClient();

			if (startupAttempt < 10) {
				startupAttempt++;
				log.info("Retrying attempt {}...", startupAttempt);

				try {
					Thread.sleep(1000);
				} catch (final Exception e) {
					// Ignore
				}

				return connectOrReconnect();
			} else {
				return false;
			}
		}

		synchronized (client) {
			log.debug("Login successful.");

			if (client.getConnectionState() != ConnectionState.OPEN) {
				log.error("Could not login to deepstream server: {}");
				return false;
			}

			log.debug("Caching presence information...");
			synchronized (loggedInParticipants) {
				synchronized (loggedInSupervisors) {
					synchronized (loggedInTeamManagers) {
						synchronized (loggedInObservers) {
							synchronized (loggedInExternalSystems) {
								loggedInParticipants.clear();
								loggedInSupervisors.clear();
								loggedInTeamManagers.clear();
								loggedInObservers.clear();
								loggedInExternalSystems.clear();

								for (val userAndRole : client.presence
										.getAll()) {
									val userAndRoleArray = userAndRole
											.split(" ");
									val user = userAndRoleArray[0];
									val role = userAndRoleArray[1];

									if (role.equals(participantRole)) {
										loggedInParticipants.add(user);

										allUsersVisibleMessagesSentSinceLastLogout
												.put(user, 0);
										interventionExecutionManagerService
												.participantRememberLoginBasedOnDialogOptionTypeAndData(
														DialogOptionTypes.EXTERNAL_ID,
														ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
																+ user);
									} else if (role.equals(supervisorRole)) {
										loggedInSupervisors.add(user);

										allUsersVisibleMessagesSentSinceLastLogout
												.put(user, 0);
									} else if (role.equals(observerRole)) {
										loggedInObservers.add(user);
									} else if (role.equals(teamManagerRole)) {
										loggedInTeamManagers.add(user);
									} else if (role
											.equals(externalSystemRole)) {
										loggedInExternalSystems.add(user);
									}
								}

								client.presence.subscribe(this);
							}
						}
					}
				}
			}

			systemLoad.setLoggedInUsers(
					ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
							+ participantRole,
					loggedInParticipants.size());
			systemLoad.setLoggedInUsers(
					ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
							+ supervisorRole,
					loggedInSupervisors.size());
			systemLoad.setLoggedInUsers(
					ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
							+ teamManagerRole,
					loggedInTeamManagers.size());
			systemLoad.setLoggedInUsers(
					ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
							+ observerRole,
					loggedInObservers.size());
			systemLoad.setLoggedInUsers(
					ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
							+ externalSystemRole,
					loggedInExternalSystems.size());

			startupComplete = true;
			reconnecting = false;

			log.info("Connection to deepstream established.");

			provideMethods();

			// Care for connection state file
			if (!connectionStateFile.exists()) {
				try {
					connectionStateFile.createNewFile();
				} catch (final Exception e) {
					log.warn("Error when creating connection state file: {}",
							e.getMessage());
				}
			}

			return true;
		}
	}

	/**
	 * Provide RPC methods
	 */
	private void provideMethods() {
		// Can only be called by a "participant" (role)
		synchronized (client) {
			client.rpc.provide(DeepstreamConstants.RPC_REST_TOKEN,
					(rpcName, data, rpcResponse) -> {
						final JsonObject jsonData = (JsonObject) gson
								.toJsonTree(data);

						try {
							final String restToken = createRESTToken(
									jsonData.get(DeepstreamConstants.USER)
											.getAsString());

							rpcResponse.send(restToken);
						} catch (final Exception e) {
							log.warn("Error when requesting REST token: {}",
									e.getMessage());
							rpcResponse.send(JsonNull.INSTANCE);
						}
					});
			// Can be called by a "participant" or "supervisor" (role)
			client.rpc.provide(DeepstreamConstants.RPC_PUSH_TOKEN,
					(rpcName, data, rpcResponse) -> {
						final JsonObject jsonData = (JsonObject) gson
								.toJsonTree(data);

						try {
							val platform = jsonData
									.get(DeepstreamConstants.PLATFORM)
									.getAsString();

							PushNotificationTypes platformType;
							if (platform
									.equals(DeepstreamConstants.PLATFORM_IOS)) {
								platformType = PushNotificationTypes.IOS;
							} else if (platform.equals(
									DeepstreamConstants.PLATFORM_ANDROID)) {
								platformType = PushNotificationTypes.ANDROID;
							} else {
								throw new Exception("Given platform " + platform
										+ " is not supported");
							}

							final boolean storedSuccessful = storePushToken(
									jsonData.get(DeepstreamConstants.USER)
											.getAsString(),
									platformType,
									jsonData.get(DeepstreamConstants.TOKEN)
											.getAsString());

							if (storedSuccessful) {
								rpcResponse.send(new JsonPrimitive(true));
							} else {
								rpcResponse.send(new JsonPrimitive(false));
							}
						} catch (final Exception e) {
							log.warn("Error when storing push token: {}",
									e.getMessage());
							rpcResponse.send(new JsonPrimitive(false));
						}
					});
			// Can only be called by a "participant" (role)
			client.rpc.provide(DeepstreamConstants.RPC_USER_MESSAGE,
					(rpcName, data, rpcResponse) -> {
						final JsonObject jsonData = (JsonObject) gson
								.toJsonTree(data);

						try {
							final boolean receivedSuccessful = receiveUserMessage(
									jsonData.get(DeepstreamConstants.USER)
											.getAsString(),
									jsonData.get(
											DeepstreamConstants.USER_MESSAGE)
											.getAsString(),
									null, null,
									jsonData.has(DeepstreamConstants.USER_TEXT)
											? jsonData.get(
													DeepstreamConstants.USER_TEXT)
													.getAsString()
											: null,
									jsonData.get(
											DeepstreamConstants.USER_TIMESTAMP)
											.getAsLong(),
									jsonData.has(
											DeepstreamConstants.CONTAINS_MEDIA)
													? jsonData.get(
															DeepstreamConstants.CONTAINS_MEDIA)
															.getAsString()
													: null,
									jsonData.has(DeepstreamConstants.MEDIA_TYPE)
											? jsonData.get(
													DeepstreamConstants.MEDIA_TYPE)
													.getAsString()
											: null,
									jsonData.has(
											DeepstreamConstants.RELATED_MESSAGE_ID)
													? jsonData.get(
															DeepstreamConstants.RELATED_MESSAGE_ID)
															.getAsInt()
													: -1,
									jsonData.has(DeepstreamConstants.CLIENT_ID)
											? jsonData.get(
													DeepstreamConstants.CLIENT_ID)
													.getAsString()
											: null,
									false);

							if (receivedSuccessful) {
								rpcResponse.send(new JsonPrimitive(true));
							} else {
								rpcResponse.send(new JsonPrimitive(false));
							}
						} catch (final Exception e) {
							log.warn("Error when receiving message: {}",
									e.getMessage());
							rpcResponse.send(new JsonPrimitive(false));
						}
					});
			// Can only be called by a "team-manager" (role)
			client.rpc.provide(DeepstreamConstants.RPC_DASHBOARD_MESSAGE,
					(rpcName, data, rpcResponse) -> {
						final JsonObject jsonData = (JsonObject) gson
								.toJsonTree(data);

						try {
							final boolean receivedSuccessful = receiveDashboardMessage(
									jsonData.get(DeepstreamConstants.USER)
											.getAsString(),
									jsonData.get(DeepstreamConstants.CLIENT_ID)
											.getAsString(),
									jsonData.get(DeepstreamConstants.ROLE)
											.getAsString(),
									jsonData.get(
											DeepstreamConstants.USER_MESSAGE)
											.getAsString());

							if (receivedSuccessful) {
								rpcResponse.send(new JsonPrimitive(true));
							} else {
								rpcResponse.send(new JsonPrimitive(false));
							}
						} catch (final Exception e) {
							log.warn("Error when receiving message: {}",
									e.getMessage());
							rpcResponse.send(new JsonPrimitive(false));
						}
					});
			// Can only be called by a "participant" (role)
			client.rpc.provide(DeepstreamConstants.RPC_USER_INTENTION,
					(rpcName, data, rpcResponse) ->

					{
						final JsonObject jsonData = (JsonObject) gson
								.toJsonTree(data);

						try {
							final boolean receivedSuccessful = receiveUserMessage(
									jsonData.get(DeepstreamConstants.USER)
											.getAsString(),
									null,
									jsonData.get(
											DeepstreamConstants.USER_INTENTION)
											.getAsString(),
									jsonData.has(
											DeepstreamConstants.USER_CONTENT)
													? jsonData.get(
															DeepstreamConstants.USER_CONTENT)
															.getAsString()
													: null,
									jsonData.has(DeepstreamConstants.USER_TEXT)
											? jsonData.get(
													DeepstreamConstants.USER_TEXT)
													.getAsString()
											: null,
									jsonData.get(
											DeepstreamConstants.USER_TIMESTAMP)
											.getAsLong(),
									null, null, -1,
									jsonData.has(DeepstreamConstants.CLIENT_ID)
											? jsonData.get(
													DeepstreamConstants.CLIENT_ID)
													.getAsString()
											: null,
									true);

							if (receivedSuccessful) {
								rpcResponse.send(new JsonPrimitive(true));
							} else {
								rpcResponse.send(new JsonPrimitive(false));
							}
						} catch (final Exception e) {
							log.warn(
									"Error when receiving intention message: {}",
									e.getMessage());
							rpcResponse.send(new JsonPrimitive(false));
						}
					});
			// Can only be called by a "participant" (role)
			client.rpc.provide(DeepstreamConstants.RPC_USER_VARIABLE,
					(rpcName, data, rpcResponse) -> {
						final JsonObject jsonData = (JsonObject) gson
								.toJsonTree(data);

						try {
							final boolean variableStored = writeVariableValue(
									jsonData.get(DeepstreamConstants.USER)
											.getAsString(),
									jsonData.get(DeepstreamConstants.VARIABLE)
											.getAsString(),
									jsonData.get(DeepstreamConstants.VALUE)
											.getAsString());

							if (variableStored) {
								rpcResponse.send(new JsonPrimitive(true));
							} else {
								rpcResponse.send(new JsonPrimitive(false));
							}
						} catch (final Exception e) {
							log.warn(
									"Error when writing variable value for participant: {}",
									e.getMessage());
							rpcResponse.send(new JsonPrimitive(false));
						}
					});
			// Can be called by a "participant" or "supervisor" (role)
			client.rpc.provide(DeepstreamConstants.RPC_MESSAGE_DIFF,
					(rpcName, data, rpcResponse) -> {
						final JsonObject jsonData = (JsonObject) gson
								.toJsonTree(data);

						try {
							final JsonObject messageDiff = getUserMessageDiff(
									jsonData.get(DeepstreamConstants.USER)
											.getAsString(),
									jsonData.get(
											DeepstreamConstants.SERVER_TIMESTAMP)
											.getAsLong());

							rpcResponse.send(messageDiff);
						} catch (final Exception e) {
							rpcResponse.send(JsonNull.INSTANCE);
							log.warn(
									"Error when calculating user message diff: {}",
									e.getMessage());
						}
					});
			// Can be called by a "participant" or "team-manager" (role)
			client.rpc.provide(DeepstreamConstants.RPC_DASHBOARD_DIFF,
					(rpcName, data, rpcResponse) -> {
						final JsonObject jsonData = (JsonObject) gson
								.toJsonTree(data);

						try {
							final JsonObject messageDiff = getDashboardMessageDiff(
									jsonData.get(DeepstreamConstants.USER)
											.getAsString(),
									jsonData.get(
											DeepstreamConstants.SERVER_TIMESTAMP)
											.getAsLong());

							rpcResponse.send(messageDiff);
						} catch (final Exception e) {
							rpcResponse.send(JsonNull.INSTANCE);
							log.warn(
									"Error when calculating dashboard message diff: {}",
									e.getMessage());
						}
					});
			// Provide external-message RPC method.
			client.rpc.provide(DeepstreamConstants.RPC_EXTERNAL_MESSAGE,
					(rpcName, data, rpcResponse) -> {
						final JsonObject jsonData = (JsonObject) gson
								.toJsonTree(data);

						try {
							// Collect all method parameters.
							val systemId = jsonData.get(
									DeepstreamConstants.REST_FIELD_SYSTEM_ID)
									.getAsString();
							val jsonVariables = jsonData.get(
									DeepstreamConstants.REST_FIELD_VARIABLES)
									.getAsJsonObject();
							List<String> participants = new ArrayList<>();
							if (jsonData.has(
									DeepstreamConstants.REST_FIELD_PARTICIPANTS)) {
								val participantJsonArray = jsonData.get(
										DeepstreamConstants.REST_FIELD_PARTICIPANTS)
										.getAsJsonArray();
								participantJsonArray.forEach(
										jsonElement -> participants.add(
												jsonElement.getAsString()));
							}

							// Collect all key-value variable pairs.
							Map<String, Variable> variables = new HashMap<>();
							for (Map.Entry<String, JsonElement> entry : jsonVariables
									.entrySet()) {
								String name = entry.getKey();
								JsonElement element = entry.getValue();

								variables.put(name, new Variable(name,
										element.getAsString()));
							}
							// Create a ExternalSystemMessage and add it to the received messages.
							final boolean receivedSuccessful = receiveExternalSystemMessage(
									systemId, participants, variables);

							if (receivedSuccessful) {
								rpcResponse.send(new JsonPrimitive(true));
							} else {
								rpcResponse.send(new JsonPrimitive(false));
							}
						} catch (final Exception e) {
							log.warn(
									"Error when receiving external system message: {}",
									e.getMessage());
							rpcResponse.send(new JsonPrimitive(false));
						}
					});
		}
	}

	/**
	 * Creates a REST token for the {@link Participant}
	 * 
	 * @param participantId
	 * @return
	 */
	private String createRESTToken(final String participantId) {
		return restManagerService.createParticipantToken(
				ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
						+ participantId);
	}

	/**
	 * Stores a push token for participant or supervisor
	 * 
	 * @param participantOrSupervisorId
	 * @return
	 */
	private boolean storePushToken(final String participantOrSupervisorId,
			final PushNotificationTypes pushNotificationType,
			final String pushToken) {
		val userTry = interventionExecutionManagerService
				.dialogOptionAddPushNotificationTokenBasedOnTypeAndData(
						DialogOptionTypes.EXTERNAL_ID,
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
								+ participantOrSupervisorId,
						pushNotificationType, pushToken);

		if (userTry) {
			return true;
		}

		return interventionExecutionManagerService
				.dialogOptionAddPushNotificationTokenBasedOnTypeAndData(
						DialogOptionTypes.SUPERVISOR_EXTERNAL_ID,
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
								+ participantOrSupervisorId,
						pushNotificationType, pushToken);
	}

	/**
	 * @param participantId
	 * @param message
	 * @param intention
	 * @param content
	 * @param text
	 * @param clientTimestamp
	 * @param containsMedia
	 * @param mediaType
	 * @param relatedMessageIdBasedOnOrder
	 * @param clientId
	 * @param typeIntention
	 * @return
	 */
	private boolean receiveUserMessage(final String participantId,
			final String message, final String intention, final String content,
			final String text, final long clientTimestamp,
			final String containsMedia, final String mediaType,
			final int relatedMessageIdBasedOnOrder, final String clientId,
			final boolean typeIntention) {
		log.debug("Received {} message for participant {}",
				typeIntention ? "intention" : "regular", participantId);

		val receivedMessage = new ReceivedMessage();

		// Always set as participant message (if it is from a supervisor it will
		// be discarded later automatically when no appropriate dialog option is
		// found)
		receivedMessage.setType(DialogOptionTypes.EXTERNAL_ID);
		receivedMessage.setSender(
				ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
						+ participantId);
		receivedMessage.setTypeIntention(typeIntention);
		receivedMessage.setClientId(clientId);
		receivedMessage.setMessage(message);
		receivedMessage.setIntention(intention);
		receivedMessage.setContent(content);
		receivedMessage.setText(text);
		receivedMessage.setClientTimestamp(clientTimestamp);
		receivedMessage
				.setReceivedTimestamp(InternalDateTime.currentTimeMillis());
		receivedMessage.setMediaURL(containsMedia);
		receivedMessage.setMediaType(mediaType);
		receivedMessage
				.setRelatedMessageIdBasedOnOrder(relatedMessageIdBasedOnOrder);

		if (typeIntention && receivedMessage.getIntention() != null
				&& receivedMessage.getSender() != null) {
			synchronized (receivedMessages) {
				receivedMessages.add(receivedMessage);
			}
			return true;
		} else if (!typeIntention && receivedMessage.getMessage() != null
				&& receivedMessage.getSender() != null) {
			synchronized (receivedMessages) {
				receivedMessages.add(receivedMessage);
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean receiveExternalSystemMessage(final String systemId,
			final List<String> participants,
			final Map<String, Variable> variables) {

		log.debug("Received external message for system {}", systemId);
		
		if(StringUtils.isBlank(systemId)) {
			return false;
		}

		val externalSystemMessage = new ExternalSystemMessage();
		externalSystemMessage.setSystemId(systemId);
		externalSystemMessage.addAllParticipants(participants);
		externalSystemMessage.putAllVariables(variables);

			synchronized (receivedExternalSystemMessages) {
				receivedExternalSystemMessages.add(externalSystemMessage);
			}
		return true;
	}

	/**
	 * @param participantIdentifier
	 * @param clientMessageId
	 * @param role
	 * @param message
	 * @return
	 */
	private boolean receiveDashboardMessage(final String participantIdentifier,
			final String clientMessageId, final String role,
			final String message) {
		log.debug("Received dashboard message from {} for participant {}", role,
				participantIdentifier);

		Record record = null;
		val timestamp = InternalDateTime.currentTimeMillis();

		val dashboardMessage = interventionExecutionManagerService
				.dashboardMessageCreateUsingDialogOptionTypeAndData(
						DialogOptionTypes.EXTERNAL_ID,
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
								+ participantIdentifier,
						clientMessageId, role, message, timestamp);

		if (dashboardMessage == null) {
			return false;
		}

		val messageObject = new JsonObject();
		messageObject.addProperty(DeepstreamConstants.CLIENT_ID,
				clientMessageId);
		messageObject.addProperty(DeepstreamConstants.ROLE, role);
		messageObject.addProperty(DeepstreamConstants.USER_MESSAGE, message);
		messageObject.addProperty(DeepstreamConstants.SERVER_TIMESTAMP,
				timestamp);

		boolean sendingResult = false;
		synchronized (client) {
			try {
				record = client.record
						.getRecord(DeepstreamConstants.PATH_DASHBOARD
								+ participantIdentifier);

				record.set(
						DeepstreamConstants.PATH_LIST
								+ String.valueOf(dashboardMessage.getOrder()),
						messageObject);

				client.event.emit(DeepstreamConstants.PATH_DASHBOARD_UPDATE
						+ participantIdentifier, messageObject);

				sendingResult = true;
			} catch (final Exception e) {
				try {

				} catch (final Exception e1) {
					// Do nothing
				}
				try {
					if (record != null) {
						record.delete();
					}
				} finally {
					if (record != null) {
						try {
							record.discard();
						} catch (final Exception e2) {
							log.warn(
									"Could not discard record on dashboard message sending");
						}
					}
				}
			}
		}

		// Send notifications when sending was successful
		if (sendingResult) {
			if (role.equals(participantRole)) {
				// Message by participant (send email notification to
				// team-manager)

				communicationManagerService.sendDashboardChatNotification(false,
						dashboardMessage.getParticipant(),
						dashboardMessage.getMessage(), 0, null);
			} else if (role.equals(teamManagerRole)) {
				// Message by team manager (send push notification to
				// participant)

				int messagesSentSinceLastLogout = 0;
				synchronized (allUsersVisibleMessagesSentSinceLastLogout) {
					if (loggedInParticipants.contains(participantIdentifier)) {
						// If user is logged in remember as one (for late
						// logout
						// users)
						messagesSentSinceLastLogout = 1;
					} else if (allUsersVisibleMessagesSentSinceLastLogout
							.containsKey(participantIdentifier)) {
						// User is not logged in and well known
						messagesSentSinceLastLogout = allUsersVisibleMessagesSentSinceLastLogout
								.get(participantIdentifier) + 1;
					} else {
						// User is not logged in and not known
						messagesSentSinceLastLogout = 1;
					}
					allUsersVisibleMessagesSentSinceLastLogout.put(
							participantIdentifier, messagesSentSinceLastLogout);
				}

				if (messagesSentSinceLastLogout > 0) {
					communicationManagerService.sendDashboardChatNotification(
							true, dashboardMessage.getParticipant(),
							dashboardMessage.getMessage(),
							messagesSentSinceLastLogout,
							ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
									+ participantIdentifier);
				}
			}

		}

		return sendingResult;
	}

	/**
	 * @param participantId
	 * @param variable
	 * @param value
	 * @return
	 */
	private boolean writeVariableValue(final String participantId,
			final String variable, final String value) {
		log.debug("Received new value for variable {} for participant {}",
				variable, participantId);

		final boolean variableStored = interventionExecutionManagerService
				.participantAdjustVariableValueExternallyBasedOnDialogOptionTypeAndData(
						DialogOptionTypes.EXTERNAL_ID,
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
								+ participantId,
						variable, value);

		if (variableStored) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param participantOrSupervisorId
	 * @param timestamp
	 * @return
	 */
	private JsonObject getUserMessageDiff(
			final String participantOrSupervisorId, final long timestamp) {
		log.debug(
				"Calculating user message diff for participant/supervisor {} and timestamp {}",
				participantOrSupervisorId, timestamp);

		long newestTimestamp = 0;

		SnapshotResult snapshot;
		synchronized (client) {
			snapshot = client.record.snapshot(DeepstreamConstants.PATH_MESSAGES
					+ participantOrSupervisorId);
		}

		val jsonObject = new JsonObject();
		val jsonObjects = new JsonObject();

		final Iterator<Entry<String, JsonElement>> iterator = snapshot.getData()
				.getAsJsonObject().entrySet().iterator();
		while (iterator.hasNext()) {
			val element = iterator.next();
			if (element.getKey().startsWith(DeepstreamConstants.PATH_LIST)) {
				val timestampToCompare = ((JsonObject) element.getValue())
						.get(DeepstreamConstants.LAST_MODIFIED).getAsLong();
				if (timestampToCompare > timestamp) {
					jsonObjects.add(element.getKey(), element.getValue());
				}
				if (timestampToCompare > newestTimestamp) {
					newestTimestamp = timestampToCompare;
				}
			}
		}

		jsonObject.add("list", jsonObjects);
		jsonObject.addProperty("latest-timestamp", newestTimestamp);

		log.debug("Message diff calculated");

		return jsonObject;
	}

	/**
	 * @param participantId
	 * @param timestamp
	 * @return
	 */
	private JsonObject getDashboardMessageDiff(final String participantId,
			final long timestamp) {
		log.debug(
				"Calculating dashboard message diff for participant {} and timestamp {}",
				participantId, timestamp);

		long newestTimestamp = 0;

		SnapshotResult snapshot;
		synchronized (client) {
			snapshot = client.record.snapshot(
					DeepstreamConstants.PATH_DASHBOARD + participantId);
		}

		val jsonObject = new JsonObject();
		val jsonObjects = new JsonObject();

		if (snapshot.getData() != null) {
			final Iterator<Entry<String, JsonElement>> iterator = snapshot
					.getData().getAsJsonObject().entrySet().iterator();
			while (iterator.hasNext()) {
				val element = iterator.next();
				if (element.getKey()
						.startsWith(DeepstreamConstants.PATH_LIST)) {
					val timestampToCompare = ((JsonObject) element.getValue())
							.get(DeepstreamConstants.SERVER_TIMESTAMP)
							.getAsLong();
					if (timestampToCompare > timestamp) {
						jsonObjects.add(element.getKey(), element.getValue());
					}
					if (timestampToCompare > newestTimestamp) {
						newestTimestamp = timestampToCompare;
					}
				}
			}
		}

		jsonObject.add("list", jsonObjects);
		jsonObject.addProperty("latest-timestamp", newestTimestamp);

		log.debug("Message diff calculated");

		return jsonObject;
	}

	private void cleanupClient() throws Exception {
		log.debug("Cleaning up client...");

		try {
			synchronized (client) {
				client.setRuntimeErrorHandler(null);
				client.removeConnectionChangeListener(this);
				client.presence.unsubscribe(this);
				client.rpc.unprovide(DeepstreamConstants.RPC_REST_TOKEN);
				client.rpc.unprovide(DeepstreamConstants.RPC_USER_MESSAGE);
				client.rpc.unprovide(DeepstreamConstants.RPC_USER_INTENTION);
				client.rpc.unprovide(DeepstreamConstants.RPC_MESSAGE_DIFF);

				client.close();
				client = null;
			}
		} catch (final Exception e) {
			log.warn("Problems when cleaning up client: {}", e.getMessage());
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (final Exception e) {
					log.warn("Could not close client on cleanup");
				}
			}
		}

		log.debug("Cleaning up done.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.deepstream.PresenceEventListener#onClientLogin(java.lang.String)
	 */
	@Override
	public void onClientLogin(final String userAndRole) {
		val userAndRoleArray = userAndRole.split(" ");
		val user = userAndRoleArray[0];
		val role = userAndRoleArray[1];

		if (role.equals(participantRole)) {
			loggedInParticipants.add(user);

			interventionExecutionManagerService
					.participantRememberLoginBasedOnDialogOptionTypeAndData(
							DialogOptionTypes.EXTERNAL_ID,
							ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
									+ user);
		} else if (role.equals(supervisorRole)) {
			loggedInSupervisors.add(user);
		} else if (role.equals(teamManagerRole)) {
			loggedInTeamManagers.add(user);
		} else if (role.equals(observerRole)) {
			loggedInObservers.add(user);
		} else if (role.equals(externalSystemRole)) {
			loggedInExternalSystems.add(user);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.deepstream.PresenceEventListener#onClientLogout(java.lang.String)
	 */
	@Override
	public void onClientLogout(final String userAndRole) {
		val userAndRoleArray = userAndRole.split(" ");
		val user = userAndRoleArray[0];
		val role = userAndRoleArray[1];

		if (role.equals(participantRole)) {
			loggedInParticipants.remove(user);

			allUsersVisibleMessagesSentSinceLastLogout.put(user, 0);
			interventionExecutionManagerService
					.participantRememberLogoutBasedOnDialogOptionTypeAndData(
							DialogOptionTypes.EXTERNAL_ID,
							ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
									+ user);
		} else if (role.equals(supervisorRole)) {
			loggedInSupervisors.remove(user);

			allUsersVisibleMessagesSentSinceLastLogout.put(user, 0);
		} else if (role.equals(teamManagerRole)) {
			loggedInTeamManagers.remove(user);
		} else if (role.equals(observerRole)) {
			loggedInObservers.remove(user);
		} else if (role.equals(externalSystemRole)) {
			loggedInExternalSystems.remove(user);
		}
	}

	@Override
	public void onException(final Topic topic, final Event event,
			final String description) {
		if (startupComplete && event == Event.CONNECTION_ERROR) {
			if (reconnecting) {
				log.warn("Deepstream connection still lost...");
			} else {
				log.warn("Deepstream connection lost!");
			}

			// Care for connection state file
			if (connectionStateFile.exists()) {
				try {
					connectionStateFile.delete();
				} catch (final Exception e) {
					log.warn("Error when deleting connection state file: {}",
							e.getMessage());
				}
			}

			try {
				cleanupClient();
				client = null;
			} catch (final Exception e) {
				log.warn("Could not cleanup on disconnect: ", e.getMessage());
			} finally {
				client = null;
			}

			reconnecting = true;

			try {
				connectOrReconnect();
			} catch (final Exception e) {
				log.error("Problem when reconnecting to deepstream: {}",
						e.getMessage());
			}
		}
	}

	@Override
	public void connectionStateChanged(final ConnectionState connectionState) {
		log.debug("New deepstream connection state: {}", connectionState);

		if (startupComplete && connectionState == ConnectionState.CLOSED) {
			onException(null, Event.CONNECTION_ERROR, null);
		}
	}
}
