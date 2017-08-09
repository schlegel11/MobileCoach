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

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.tools.InternalDateTime;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gwt.dev.json.JsonBoolean;

/**
 * Handles the communication with a deepstream server
 * 
 * @author Andreas Filler
 */
@Log4j2
public class DeepstreamCommunicationService implements ConnectionStateListener {
	private static DeepstreamCommunicationService	instance		= null;

	private InterventionExecutionManagerService		interventionExecutionManagerService;

	private final int								substringLength;

	private final List<ReceivedMessage>				receivedMessages;

	private DeepstreamClient						client			= null;
	private final String							host;
	private final JsonObject						loginData;

	private boolean									startupComplete	= false;
	private boolean									reconnecting	= false;

	final Gson										gson;

	private DeepstreamCommunicationService(final String deepstreamHost,
			final String deepstreamUser, final String deepStreamPassword)
			throws Exception {
		host = deepstreamHost;

		if (deepstreamUser != null && !deepstreamUser.equals("")) {
			loginData = new JsonObject();
			loginData.addProperty("username", deepstreamUser);
			loginData.addProperty("password", deepStreamPassword);
		} else {
			loginData = null;
		}

		gson = new Gson();

		substringLength = ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
				.length();

		receivedMessages = new ArrayList<ReceivedMessage>();

		try {
			connectOrReconnect();
		} catch (final Exception e) {
			log.error("Problem when connecting to deepstream: {}",
					e.getMessage());
			throw e;
		}
	}

	public static DeepstreamCommunicationService start(
			final String deepstreamHost, final String deepstreamUser,
			final String deepStreamPassword) throws Exception {
		log.info("Starting service...");
		if (instance == null) {
			instance = new DeepstreamCommunicationService(deepstreamHost,
					deepstreamUser, deepStreamPassword);
		}
		log.info("Started.");
		return instance;
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
			final String message, final boolean messageExpectsAnswer) {
		synchronized (this) {
			if (interventionExecutionManagerService == null) {
				interventionExecutionManagerService = MC.getInstance()
						.getInterventionExecutionManagerService();
			}
		}

		interventionExecutionManagerService
				.dialogMessageStatusChangesForSending(dialogMessageId,
						DialogMessageStatusTypes.SENDING,
						InternalDateTime.currentTimeMillis());

		val timestamp = InternalDateTime.currentTimeMillis();
		try {
			val participantIdentifier = dialogOption.getData().substring(
					substringLength);
			val record = client.record.getRecord("messages/"
					+ participantIdentifier);

			final JsonObject messageObject = new JsonObject();
			messageObject.addProperty("order", messageOrder);
			messageObject.addProperty("type", "SYSTEM_MESSAGE");
			messageObject.addProperty("message", message);
			messageObject.addProperty("message-timestamp", timestamp);
			messageObject.addProperty("expects-answer", messageExpectsAnswer);
			messageObject.addProperty("last-modified", timestamp);

			record.set("list/" + String.valueOf(messageOrder), messageObject);

			client.event.emit("message-update/" + participantIdentifier,
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
		synchronized (this) {
			if (interventionExecutionManagerService == null) {
				interventionExecutionManagerService = MC.getInstance()
						.getInterventionExecutionManagerService();
			}
		}

		val newReceivedMessages = new ArrayList<ReceivedMessage>();

		synchronized (receivedMessages) {
			newReceivedMessages.addAll(receivedMessages);
			receivedMessages.clear();
		}

		return newReceivedMessages;
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

			if (loginData != null) {
				result = client.login(loginData);
			} else {
				result = client.login();
			}

			if (!result.loggedIn()) {
				log.warn("Login and authentication failed.");

				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					// Do nothing
				}
			}
		} while (startupComplete && !result.loggedIn());

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

	/**
	 * Provide RPC methods
	 */
	private void provideMethods() {
		client.rpc.provide(
				"message-inbox",
				(rpcName, data, rpcResponse) -> {
					final JsonObject jsonData = (JsonObject) gson
							.toJsonTree(data);
					try {
						final boolean receivedSuccessful = receiveMessage(
								jsonData.get("participant").getAsString(),
								jsonData.get("message").getAsString(), jsonData
										.get("timestamp").getAsLong());

						if (receivedSuccessful) {
							rpcResponse.send(JsonBoolean.TRUE);
						} else {
							rpcResponse.send(JsonBoolean.FALSE);
						}
					} catch (final Exception e) {
						log.warn("Error when receiving message: {}",
								e.getMessage());
						rpcResponse.send(JsonBoolean.FALSE);
						return;
					}
				});

		client.rpc.provide(
				"message-diff",
				(rpcName, data, rpcResponse) -> {
					final JsonObject jsonData = (JsonObject) gson
							.toJsonTree(data);
					try {
						final JsonObject messageDiff = getMessageDiff(jsonData
								.get("participant").getAsString(), jsonData
								.get("timestamp").getAsLong());
						rpcResponse.send(messageDiff);
					} catch (final Exception e) {
						log.warn("Error when calculating message diff: {}",
								e.getMessage());
						rpcResponse.send(JsonNull.INSTANCE);
					}
				});
	}

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

	private boolean receiveMessage(final String participantId,
			final String message, final long timestamp) {
		log.debug("Received message for participant {}", participantId);

		val receivedMessage = new ReceivedMessage();

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

	private JsonObject getMessageDiff(final String participantId,
			final long timestamp) {
		log.debug(
				"Calculating message diff for participant {} and timestamp {}",
				participantId, timestamp);

		long newestTimestamp = 0;

		val record = client.record.getRecord("messages/" + participantId);

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
				messageObject.addProperty("type", "participantId_MESSAGE");
			} else {
				messageObject = (JsonObject) retrievedMessageObject;
				messageObject.addProperty("type", "SYSTEM_MESSAGE_WITH_REPLY");
			}

			messageObject.addProperty("order", messageOrder);
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
}
