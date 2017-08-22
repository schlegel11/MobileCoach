package ch.ethz.mc.services.internal;

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
import io.deepstream.ConfigOptions;
import io.deepstream.ConnectionState;
import io.deepstream.ConnectionStateListener;
import io.deepstream.DeepstreamClient;
import io.deepstream.DeepstreamFactory;
import io.deepstream.LoginResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.memory.ExternalRegistration;
import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.SurveyExecutionManagerService;
import ch.ethz.mc.tools.InternalDateTime;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Handles the communication with a deepstream server
 * 
 * @author Andreas Filler
 */
@Log4j2
public class DeepstreamCommunicationService implements ConnectionStateListener {
	@Getter
	private static DeepstreamCommunicationService	instance		= null;

	private SurveyExecutionManagerService			surveyExecutionManagerService;
	private InterventionExecutionManagerService		interventionExecutionManagerService;

	private final int								substringLength;

	private final List<ReceivedMessage>				receivedMessages;

	private DeepstreamClient						client			= null;
	private final String							host;
	private final JsonObject						loginData;

	private boolean									startupComplete	= false;
	private boolean									reconnecting	= false;

	private final Gson								gson;

	private DeepstreamCommunicationService(final String deepstreamHost,
			final String deepstreamServerRole,
			final String deepstreamServerPassword) {
		host = deepstreamHost;

		loginData = new JsonObject();
		loginData.addProperty("user", "server");
		loginData.addProperty("password", "not required");
		loginData.addProperty("role", deepstreamServerRole);
		loginData.addProperty("secret", deepstreamServerPassword);

		gson = new Gson();

		substringLength = ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
				.length();

		receivedMessages = new ArrayList<ReceivedMessage>();
	}

	public static DeepstreamCommunicationService prepare(
			final String deepstreamHost, final String deepstreamServerRole,
			final String deepstreamServerPassword) {
		log.info("Preparing service...");
		if (instance == null) {
			instance = new DeepstreamCommunicationService(deepstreamHost,
					deepstreamServerRole, deepstreamServerPassword);
		}
		log.info("Prepared.");
		return instance;
	}

	public void start(
			final SurveyExecutionManagerService surveyExecutionManagerService,
			final InterventionExecutionManagerService interventionExecutionManagerService)
			throws Exception {
		log.info("Starting service...");

		this.surveyExecutionManagerService = surveyExecutionManagerService;
		this.interventionExecutionManagerService = interventionExecutionManagerService;

		try {
			connectOrReconnect();
		} catch (final Exception e) {
			log.error("Problem when connecting to deepstream: {}",
					e.getMessage());
			throw e;
		}

		log.info("Started.");
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		try {
			client.removeConnectionChangeListener(this);
			client.close();
			client = null;
		} catch (final Exception e) {
			log.warn("Could not close deepstream connection: {}",
					e.getMessage());
		}

		log.info("Stopped.");
	}

	/*
	 * Public methods
	 */
	/**
	 * Queue message for sending using deepstream
	 * 
	 * @param dialogOption
	 * @param dialogMessageId
	 * @param messageOrder
	 * @param message
	 * @param messageExpectsAnswer
	 */
	@Synchronized
	public void asyncSendMessage(final DialogOption dialogOption,
			final ObjectId dialogMessageId, final int messageOrder,
			String message, final boolean messageExpectsAnswer) {
		interventionExecutionManagerService
				.dialogMessageStatusChangesForSending(dialogMessageId,
						DialogMessageStatusTypes.SENDING,
						InternalDateTime.currentTimeMillis());

		val timestamp = InternalDateTime.currentTimeMillis();
		try {
			val participantOrSupervisorIdentifier = dialogOption.getData()
					.substring(substringLength);
			val record = client.record.getRecord("messages/"
					+ participantOrSupervisorIdentifier);

			final JsonObject messageObject = new JsonObject();
			messageObject.addProperty("id", messageOrder);
			messageObject.addProperty("status", "SENT_BY_SYSTEM");
			messageObject.addProperty("type", "PLAIN");
			if (message.contains(Constants.getMediaObjectLinkingBaseURL())) {
				val indexFrom = message.indexOf(Constants
						.getMediaObjectLinkingBaseURL());
				int indexTo;
				if ((indexTo = message.indexOf(" ", indexFrom)) == -1) {
					messageObject.addProperty("contains-media",
							message.substring(indexFrom));
				} else {
					messageObject.addProperty("contains-media",
							message.substring(indexFrom, indexTo));
				}
				message = message
						.replaceFirst(
								Constants.getMediaObjectLinkingBaseURL()
										+ "[^ ]*",
								ImplementationConstants.PLACEHOLDER_LINKED_MEDIA_OBJECT);
			}
			if (message.contains(Constants.getSurveyLinkingBaseURL())) {
				val indexFrom = message.indexOf(Constants
						.getSurveyLinkingBaseURL());
				int indexTo;
				if ((indexTo = message.indexOf(" ", indexFrom)) == -1) {
					messageObject.addProperty("contains-survey",
							message.substring(indexFrom));
				} else {
					messageObject.addProperty("contains-survey",
							message.substring(indexFrom, indexTo));
				}
				message = message.replaceFirst(
						Constants.getSurveyLinkingBaseURL() + "[^ ]*",
						ImplementationConstants.PLACEHOLDER_LINKED_SURVEY);
			}
			messageObject.addProperty("message", message);
			messageObject.addProperty("message-timestamp", timestamp);
			messageObject.addProperty("expects-answer", messageExpectsAnswer);
			messageObject.addProperty("last-modified", timestamp);

			record.set("list/" + String.valueOf(messageOrder), messageObject);

			client.event.emit("message-update/"
					+ participantOrSupervisorIdentifier,
					String.valueOf(messageOrder));
		} catch (final Exception e) {
			log.warn("Could not send message to {}: {}",
					dialogOption.getData(), e.getMessage());

			interventionExecutionManagerService
					.dialogMessageStatusChangesForSending(dialogMessageId,
							DialogMessageStatusTypes.PREPARED_FOR_SENDING,
							timestamp);

			return;
		}

		if (messageExpectsAnswer) {
			interventionExecutionManagerService
					.dialogMessageStatusChangesForSending(
							dialogMessageId,
							DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER,
							timestamp);
		} else {
			interventionExecutionManagerService
					.dialogMessageStatusChangesForSending(
							dialogMessageId,
							DialogMessageStatusTypes.SENT_BUT_NOT_WAITING_FOR_ANSWER,
							timestamp);
		}
	}

	/**
	 * Get all messages received by deepstream since the last check
	 * 
	 * @return
	 */
	public List<ReceivedMessage> getReceivedMessages() {
		val newReceivedMessages = new ArrayList<ReceivedMessage>();

		synchronized (receivedMessages) {
			newReceivedMessages.addAll(receivedMessages);
			receivedMessages.clear();
		}

		return newReceivedMessages;
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
		try {
			val participantIdentifier = receivedMessage.getSender().substring(
					substringLength);
			val timestamp = InternalDateTime.currentTimeMillis();
			val record = client.record.getRecord("messages/"
					+ participantIdentifier);

			val messageOrder = dialogMessage.getOrder();

			val retrievedMessageObject = record.get("list/"
					+ String.valueOf(messageOrder));

			final JsonObject messageObject;
			if (retrievedMessageObject instanceof JsonNull) {
				messageObject = new JsonObject();
				messageObject.addProperty("status", "SENT_BY_USER");
				messageObject.addProperty("type", "PLAIN");
				messageObject.addProperty("id", messageOrder);
			} else {
				messageObject = (JsonObject) retrievedMessageObject;
				messageObject.addProperty("status", "ANSWERED_BY_USER");
			}

			messageObject.addProperty("reply", receivedMessage.getMessage());
			messageObject.addProperty("reply-timestamp",
					receivedMessage.getReceivedTimestamp());
			messageObject.addProperty("last-modified", timestamp);

			record.set("list/" + String.valueOf(messageOrder), messageObject);

			client.event.emit("message-update/" + participantIdentifier,
					String.valueOf(messageOrder));
		} catch (final Exception e) {
			log.warn("Could not acknowledge message to {}: {}",
					receivedMessage.getSender(), e.getMessage());
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
		try {
			val record = client.record.getRecord("messages/"
					+ participantOrSupervisorIdentifier);

			val secretFromRecord = record.get("secret").getAsString();

			if (StringUtils.isBlank(secretFromRecord)) {
				return false;
			}

			if (secretFromRecord.equals(secret)) {
				return true;
			} else {
				return false;
			}
		} catch (final Exception e) {
			log.warn("Could not check secret of participant/supervisor {}",
					participantOrSupervisorIdentifier);
		}
		return true;
	}

	/*
	 * Class methods
	 */
	/**
	 * Connects/reconnects to the deepstream server
	 * 
	 * @throws Exception
	 */
	private void connectOrReconnect() throws Exception {
		if (!reconnecting) {
			log.info("Connecting to deepstream...");
		} else {
			log.info("Reconnecting to deepstream...");
		}

		final Properties properties = new Properties();
		properties.setProperty(ConfigOptions.MAX_RECONNECT_ATTEMPTS.toString(),
				"8");

		client = DeepstreamFactory.getInstance().getClient(host, properties);
		client.addConnectionChangeListener(this);

		LoginResult result = null;
		do {
			log.debug("Trying to login...");

			result = client.login(loginData);

			if (!result.loggedIn()) {
				log.warn("Login and authentication failed.");

				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					// Do nothing
				}
			}
		} while (startupComplete || !result.loggedIn());

		if (result.loggedIn()) {
			log.debug("Login successful.");

			startupComplete = true;
			reconnecting = false;
			log.info("Connection to deepstream established.");

			provideMethods();
		} else {
			log.error("Could not login to deepstream server at startup: {}",
					result.getErrorEvent());
			throw new Exception(
					"Could not connect to deepstream server at startup!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.deepstream.ConnectionStateListener#connectionStateChanged(io.deepstream
	 * .ConnectionState)
	 */
	@Override
	public void connectionStateChanged(final ConnectionState connectionState) {
		if (startupComplete && connectionState == ConnectionState.CLOSED) {
			if (reconnecting) {
				log.warn("Deepstream connection still lost...");
			} else {
				log.warn("Deepstream connection lost!");

				try {
					client.removeConnectionChangeListener(this);
					client = null;
				} catch (final Exception e) {
					log.warn("Could not cleanup on disconnect: ",
							e.getMessage());
				}

				reconnecting = true;
			}

			try {
				connectOrReconnect();
			} catch (final Exception e) {
				log.error("Problem when reconnecting to deepstream: {}",
						e.getMessage());
			}
		}
	}

	/**
	 * Provide RPC methods
	 */
	private void provideMethods() {
		// Can only be called by a "participant" (role)
		client.rpc.provide("request-rest-token",
				(rpcName, data, rpcResponse) -> {
					// TODO DS request rest token without survey
			});
		// Can only be called by a "supervisor" (role)
		client.rpc.provide(
				"message-inbox",
				(rpcName, data, rpcResponse) -> {
					final JsonObject jsonData = (JsonObject) gson
							.toJsonTree(data);
					try {
						final boolean receivedSuccessful = receiveMessage(
								jsonData.get("user").getAsString(), jsonData
										.get("message").getAsString(), jsonData
										.get("timestamp").getAsLong());

						if (receivedSuccessful) {
							rpcResponse.send(new JsonPrimitive(true));
						} else {
							rpcResponse.send(new JsonPrimitive(false));
						}
					} catch (final Exception e) {
						log.warn("Error when receiving message: {}",
								e.getMessage());
						rpcResponse.send(new JsonPrimitive(false));
						return;
					}
				});
		// Can be called by a "participant" or "supervisor" (role)
		client.rpc.provide(
				"message-diff",
				(rpcName, data, rpcResponse) -> {
					final JsonObject jsonData = (JsonObject) gson
							.toJsonTree(data);
					try {
						final JsonObject messageDiff = getMessageDiff(jsonData
								.get("user").getAsString(),
								jsonData.get("timestamp").getAsLong());

						rpcResponse.send(messageDiff);
					} catch (final Exception e) {
						rpcResponse.send(JsonNull.INSTANCE);
						log.warn("Error when calculating message diff: {}",
								e.getMessage());
					}
				});
	}

	/**
	 * @param participantId
	 * @param message
	 * @param timestamp
	 * @return
	 */
	private boolean receiveMessage(final String participantId,
			final String message, final long timestamp) {
		log.debug("Received message for participant {}", participantId);

		val receivedMessage = new ReceivedMessage();

		// Always set as participant message (if it is from a supervisor it will
		// be discarded later automatically when no appropriate dialog option is
		// found)
		// TODO Should be implemented for supervisors as well
		receivedMessage.setType(DialogOptionTypes.EXTERNAL_ID);
		receivedMessage.setReceivedTimestamp(timestamp);
		receivedMessage.setMessage(message);
		receivedMessage
				.setSender(ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
						+ participantId);

		if (receivedMessage.getMessage() != null
				&& receivedMessage.getSender() != null) {
			synchronized (receivedMessages) {
				receivedMessages.add(receivedMessage);
			}
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
	private JsonObject getMessageDiff(final String participantOrSupervisorId,
			final long timestamp) {
		log.debug(
				"Calculating message diff for participant/supervisor {} and timestamp {}",
				participantOrSupervisorId, timestamp);

		long newestTimestamp = 0;

		val record = client.record.getRecord("messages/"
				+ participantOrSupervisorId);

		val jsonObject = new JsonObject();
		val jsonObjects = new JsonObject();

		final Iterator<Entry<String, JsonElement>> iterator = record.get()
				.getAsJsonObject().entrySet().iterator();
		while (iterator.hasNext()) {
			val element = iterator.next();
			if (element.getKey().startsWith("list/")) {
				val timestampToCompare = ((JsonObject) element.getValue()).get(
						"last-modified").getAsLong();
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

		return jsonObject;
	}

	/**
	 * Creates a deepstream participant/supervisor and the belonging
	 * intervention participant structures or returns null if not allowed
	 * 
	 * @param nickname
	 * @param relatedParticipantExternalId
	 * @param interventionPattern
	 * @param interventionPassword
	 * @param supervisorRequest
	 * @return
	 */
	public ExternalRegistration registerUser(final String nickname,
			final String relatedParticipantExternalId,
			final String interventionPattern,
			final String interventionPassword, final boolean supervisorRequest) {

		// Prepare deepstream and reserve unique ID
		String participantOrSupervisorExternalId;
		final String secret = RandomStringUtils.randomAlphanumeric(128);
		synchronized (client) {
			do {
				participantOrSupervisorExternalId = RandomStringUtils
						.randomAlphanumeric(32);
			} while (client.record.has(
					"messages/" + participantOrSupervisorExternalId)
					.getResult());

			val record = client.record.getRecord("messages/"
					+ participantOrSupervisorExternalId);
			record.set("secret", secret);
			record.set("role",
					supervisorRequest ? Constants.getDeepstreamSupervisorRole()
							: Constants.getDeepstreamParticipantRole());
		}

		val record = client.record.getRecord("messages/"
				+ participantOrSupervisorExternalId);

		boolean createdSucessfully = false;
		try {
			createdSucessfully = interventionExecutionManagerService
					.checkAccessRightsAndRegisterParticipantOrSupervisorExternallyWithoutSurvey(
							nickname,
							ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
									+ relatedParticipantExternalId,
							ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
									+ participantOrSupervisorExternalId,
							interventionPattern, interventionPassword,
							supervisorRequest);
		} finally {
			// Cleanup prepared user if registration was not possible
			if (!createdSucessfully) {
				record.delete();

				return null;
			}
		}

		return new ExternalRegistration(participantOrSupervisorExternalId,
				secret);
	}

	/**
	 * Cleans up deepstream based on a specific participant/supervisor
	 * 
	 * @param participantOrSupervisorId
	 */
	public void cleanupForParticipantOrSupervisor(
			final String participantOrSupervisorId) {
		try {
			val record = client.record.getRecord("messages/"
					+ participantOrSupervisorId);

			record.delete();
		} catch (final Exception e) {
			log.warn(
					"Could not cleanup deepstream for participant/supervisor {}",
					participantOrSupervisorId);
		}
	}
}
