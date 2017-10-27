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
import io.deepstream.PresenceEventListener;

import java.util.ArrayList;
import java.util.HashSet;
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
import ch.ethz.mc.conf.DeepstreamConstants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.memory.ExternalRegistration;
import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.DialogMessageTypes;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.RESTManagerService;
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
public class DeepstreamCommunicationService implements ConnectionStateListener,
		PresenceEventListener {
	@Getter
	private static DeepstreamCommunicationService	instance			= null;

	private final HashSet<String>					loggedInUsers;

	private InterventionExecutionManagerService		interventionExecutionManagerService;

	private RESTManagerService						restManagerService;

	private final int								substringLength;

	private final List<ReceivedMessage>				receivedMessages;

	private DeepstreamClient						client				= null;
	private final String							host;
	private final JsonObject						loginData;

	private boolean									startupComplete		= false;
	private boolean									restStartupComplete	= false;
	private boolean									reconnecting		= false;

	private final Gson								gson;

	private DeepstreamCommunicationService(final String deepstreamHost,
			final String deepstreamServerRole,
			final String deepstreamServerPassword) {
		loggedInUsers = new HashSet<String>();

		host = deepstreamHost;

		loginData = new JsonObject();
		loginData.addProperty(DeepstreamConstants.USER,
				Constants.getDeepstreamServerRole());
		loginData.addProperty(DeepstreamConstants.SECRET,
				deepstreamServerPassword);
		loginData.addProperty(DeepstreamConstants.ROLE, deepstreamServerRole);
		loginData.addProperty(DeepstreamConstants.INTERVENTION_PASSWORD,
				"not required");

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
			final InterventionExecutionManagerService interventionExecutionManagerService)
			throws Exception {
		log.info("Starting service...");

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
	 * @param textBasedMediaObjectContent
	 * @param surveyLink
	 * @param contentObjectLink
	 * @param messageExpectsAnswer
	 */
	@Synchronized
	public void asyncSendMessage(final DialogOption dialogOption,
			final ObjectId dialogMessageId, final int messageOrder,
			final String message, final String textBasedMediaObjectContent,
			final String surveyLink, final String contentObjectLink,
			final boolean messageExpectsAnswer) {
		val dialogMessage = interventionExecutionManagerService
				.dialogMessageStatusChangesForSending(dialogMessageId,
						DialogMessageStatusTypes.SENDING,
						InternalDateTime.currentTimeMillis());

		val timestamp = InternalDateTime.currentTimeMillis();
		try {
			val isCommand = dialogMessage.getType() == DialogMessageTypes.COMMAND;

			val participantOrSupervisorIdentifier = dialogOption.getData()
					.substring(substringLength);
			val record = client.record
					.getRecord(DeepstreamConstants.PATH_MESSAGES
							+ participantOrSupervisorIdentifier);

			final JsonObject messageObject = new JsonObject();
			messageObject.addProperty(DeepstreamConstants.ID, messageOrder);
			messageObject.addProperty(DeepstreamConstants.STATUS,
					DeepstreamConstants.STATUS_SENT_BY_SERVER);
			messageObject.addProperty(DeepstreamConstants.TYPE,
					isCommand ? DeepstreamConstants.TYPE_COMMAND
							: DeepstreamConstants.TYPE_PLAIN);
			if (!StringUtils.isBlank(dialogMessage.getSurveyLink())) {
				messageObject.addProperty(DeepstreamConstants.CONTAINS_SURVEY,
						dialogMessage.getSurveyLink());
			}
			if (!StringUtils.isBlank(dialogMessage.getMediaObjectLink())) {
				messageObject.addProperty(DeepstreamConstants.CONTAINS_MEDIA,
						dialogMessage.getMediaObjectLink());
			}
			if (isCommand) {
				if (!StringUtils.isBlank(textBasedMediaObjectContent)) {
					messageObject.addProperty(DeepstreamConstants.CONTENT,
							textBasedMediaObjectContent);
				} else {
					messageObject.addProperty(DeepstreamConstants.CONTENT, "");
				}
			}
			messageObject.addProperty(DeepstreamConstants.SERVER_MESSAGE,
					message);
			messageObject.addProperty(DeepstreamConstants.MESSAGE_TIMESTAMP,
					timestamp);
			messageObject.addProperty(DeepstreamConstants.EXPECTS_ANSWER,
					messageExpectsAnswer);
			messageObject.addProperty(DeepstreamConstants.LAST_MODIFIED,
					timestamp);

			record.set(
					DeepstreamConstants.PATH_LIST
							+ String.valueOf(messageOrder), messageObject);

			client.event.emit(DeepstreamConstants.PATH_MESSAGE_UPDATE
					+ participantOrSupervisorIdentifier, messageObject);
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
			val record = client.record
					.getRecord(DeepstreamConstants.PATH_MESSAGES
							+ participantIdentifier);

			val messageOrder = dialogMessage.getOrder();

			val retrievedMessageObject = record
					.get(DeepstreamConstants.PATH_LIST
							+ String.valueOf(messageOrder));

			final JsonObject messageObject;
			if (retrievedMessageObject instanceof JsonNull) {
				messageObject = new JsonObject();
				messageObject.addProperty(DeepstreamConstants.STATUS,
						DeepstreamConstants.STATUS_SENT_BY_USER);
				messageObject.addProperty(DeepstreamConstants.TYPE,
						DeepstreamConstants.TYPE_PLAIN);
				messageObject.addProperty(DeepstreamConstants.ID, messageOrder);
			} else {
				messageObject = (JsonObject) retrievedMessageObject;
				messageObject.addProperty(DeepstreamConstants.STATUS,
						DeepstreamConstants.STATUS_ANSWERED_BY_USER);
			}

			messageObject.addProperty(DeepstreamConstants.USER_MESSAGE,
					receivedMessage.getMessage());
			messageObject.addProperty(DeepstreamConstants.USER_TIMESTAMP,
					receivedMessage.getReceivedTimestamp());
			messageObject.addProperty(DeepstreamConstants.LAST_MODIFIED,
					timestamp);

			record.set(
					DeepstreamConstants.PATH_LIST
							+ String.valueOf(messageOrder), messageObject);

			client.event.emit(DeepstreamConstants.PATH_MESSAGE_UPDATE
					+ participantIdentifier, messageObject);
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
			val record = client.record
					.getRecord(DeepstreamConstants.PATH_MESSAGES
							+ participantOrSupervisorIdentifier);

			val secretFromRecord = record.get(DeepstreamConstants.SECRET)
					.getAsString();

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
			while (!restStartupComplete) {
				log.debug("Waiting for REST interface to come up...");

				Thread.sleep(1000);
			}
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

			log.debug("Caching presence information...");
			synchronized (loggedInUsers) {
				loggedInUsers.clear();

				for (val user : client.presence.getAll()) {
					loggedInUsers.add(user);
				}

				client.presence.subscribe(this);
			}

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
					client.presence.unsubscribe(this);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.deepstream.PresenceEventListener#onClientLogin(java.lang.String)
	 */
	@Override
	public void onClientLogin(final String user) {
		loggedInUsers.add(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.deepstream.PresenceEventListener#onClientLogout(java.lang.String)
	 */
	@Override
	public void onClientLogout(final String user) {
		loggedInUsers.remove(user);
	}

	/**
	 * Provide RPC methods
	 */
	private void provideMethods() {
		// Can only be called by a "participant" (role)
		client.rpc
				.provide(
						DeepstreamConstants.REST_TOKEN,
						(rpcName, data, rpcResponse) -> {
							final JsonObject jsonData = (JsonObject) gson
									.toJsonTree(data);
							try {
								val participantId = jsonData.get(
										DeepstreamConstants.USER).getAsString();

								final String restToken = createRESTToken(participantId);

								rpcResponse.send(restToken);
							} catch (final Exception e) {
								log.warn(
										"Error when requesting REST token: {}",
										e.getMessage());
								rpcResponse.send(JsonNull.INSTANCE);
								return;
							}
						});
		// Can only be called by a "participant" (role)
		client.rpc
				.provide(
						DeepstreamConstants.RPC_USER_MESSAGE,
						(rpcName, data, rpcResponse) -> {
							final JsonObject jsonData = (JsonObject) gson
									.toJsonTree(data);
							try {
								final boolean receivedSuccessful = receiveMessage(
										jsonData.get(DeepstreamConstants.USER)
												.getAsString(),
										jsonData.get(
												DeepstreamConstants.USER_MESSAGE)
												.getAsString(),
										jsonData.get(
												DeepstreamConstants.USER_TIMESTAMP)
												.getAsLong(), false);

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
		// Can only be called by a "participant" (role)
		client.rpc
				.provide(
						DeepstreamConstants.RPC_USER_INTENTION,
						(rpcName, data, rpcResponse) -> {
							final JsonObject jsonData = (JsonObject) gson
									.toJsonTree(data);
							try {
								final boolean receivedSuccessful = receiveMessage(
										jsonData.get(DeepstreamConstants.USER)
												.getAsString(),
										jsonData.get(
												DeepstreamConstants.USER_INTENTION)
												.getAsString(),
										jsonData.get(
												DeepstreamConstants.USER_TIMESTAMP)
												.getAsLong(), true);

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
								return;
							}
						});
		// Can be called by a "participant" or "supervisor" (role)
		client.rpc.provide(
				DeepstreamConstants.RPC_MESSAGE_DIFF,
				(rpcName, data, rpcResponse) -> {
					final JsonObject jsonData = (JsonObject) gson
							.toJsonTree(data);
					try {
						final JsonObject messageDiff = getMessageDiff(
								jsonData.get(DeepstreamConstants.USER)
										.getAsString(),
								jsonData.get(
										DeepstreamConstants.SERVER_TIMESTAMP)
										.getAsLong());

						rpcResponse.send(messageDiff);
					} catch (final Exception e) {
						rpcResponse.send(JsonNull.INSTANCE);
						log.warn("Error when calculating message diff: {}",
								e.getMessage());
					}
				});
	}

	private String createRESTToken(final String participantId) {
		return restManagerService
				.createToken(ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
						+ participantId);
	}

	/**
	 * @param participantId
	 * @param content
	 * @param timestamp
	 * @param isIntention
	 * @return
	 */
	private boolean receiveMessage(final String participantId,
			final String content, final long timestamp,
			final boolean isIntention) {
		log.debug("Received {} message for participant {}",
				isIntention ? "intention" : "regular", participantId);

		val receivedMessage = new ReceivedMessage();

		// Always set as participant message (if it is from a supervisor it will
		// be discarded later automatically when no appropriate dialog option is
		// found)
		// TODO Should be implemented for supervisors as well
		receivedMessage.setType(DialogOptionTypes.EXTERNAL_ID);
		receivedMessage.setIntention(isIntention);
		receivedMessage.setReceivedTimestamp(timestamp);
		receivedMessage.setMessage(content);
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

		val record = client.record.getRecord(DeepstreamConstants.PATH_MESSAGES
				+ participantOrSupervisorId);

		val jsonObject = new JsonObject();
		val jsonObjects = new JsonObject();

		final Iterator<Entry<String, JsonElement>> iterator = record.get()
				.getAsJsonObject().entrySet().iterator();
		while (iterator.hasNext()) {
			val element = iterator.next();
			if (element.getKey().startsWith(DeepstreamConstants.PATH_LIST)) {
				val timestampToCompare = ((JsonObject) element.getValue()).get(
						DeepstreamConstants.LAST_MODIFIED).getAsLong();
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
					DeepstreamConstants.PATH_MESSAGES
							+ participantOrSupervisorExternalId).getResult());

			val record = client.record
					.getRecord(DeepstreamConstants.PATH_MESSAGES
							+ participantOrSupervisorExternalId);
			record.set(DeepstreamConstants.SECRET, secret);
			record.set(DeepstreamConstants.ROLE,
					supervisorRequest ? Constants.getDeepstreamSupervisorRole()
							: Constants.getDeepstreamParticipantRole());
		}

		val record = client.record.getRecord(DeepstreamConstants.PATH_MESSAGES
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
	 * Checks if participant is currently connected
	 * 
	 * @param participantId
	 * @return
	 */
	public boolean checkIfParticipantIsConnected(final String participantId) {
		if (loggedInUsers.contains(participantId)) {
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
		try {
			restManagerService
					.destroyToken(ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
							+ participantOrSupervisorId);

			val record = client.record
					.getRecord(DeepstreamConstants.PATH_MESSAGES
							+ participantOrSupervisorId);

			record.delete();
		} catch (final Exception e) {
			log.warn(
					"Could not cleanup deepstream for participant/supervisor {}",
					participantOrSupervisorId);
		}
	}

	/**
	 * Enables the REST interface to inform this server about it's own startup
	 * 
	 * @param restManagerService
	 */
	public void RESTInterfaceStarted(final RESTManagerService restManagerService) {
		this.restManagerService = restManagerService;
		restStartupComplete = true;
	}
}
