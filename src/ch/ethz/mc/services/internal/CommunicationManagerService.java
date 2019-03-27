package ch.ethz.mc.services.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.memory.ExternalServiceMessage;
import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.InterventionExternalService;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.DialogMessageTypes;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.model.persistent.types.SMSServiceType;
import ch.ethz.mc.model.persistent.types.TextFormatTypes;
import ch.ethz.mc.rest.services.v02.TWILIOMessageRetrievalServiceV02;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.tools.VariableStringReplacer;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Handles communication with the message gateways
 *
 * @author Andreas Filler
 */
@Log4j2
public class CommunicationManagerService {
	private static CommunicationManagerService		instance						= null;

	private static final String						ASPSMS_API_URL					= "https://json.aspsms.com/SendSimpleTextSMS";

	private final boolean							simulatorActive;

	private long									lastSMSCheck					= 0;
	private long									lastEmailCheck					= 0;

	private final boolean							emailActive;
	private final boolean							smsActive;
	private final boolean							deepstreamActive;
	private final boolean							pushNotificationsActive;

	@Getter
	private DeepstreamCommunicationService			deepstreamCommunicationService;
	private PushNotificationService					pushNotificationService;

	private InterventionExecutionManagerService		interventionExecutionManagerService;
	private final VariablesManagerService			variablesManagerService;
	private final DatabaseManagerService			databaseManagerService;

	private final Session							incomingMailSession;
	private final Session							outgoingMailSession;

	private final String							mailboxProtocol;
	private final String							mailboxFolder;

	private final String							emailFrom;
	private final String							emailSubjectForParticipant;
	private final String							emailSubjectForSupervisor;
	private final String							emailSubjectForTeamManager;
	private final String							emailTemplateForTeamManager;

	private final SMSServiceType					smsServiceType;
	private final String							smsMailSubjectStartsWith;
	private final String							smsUserKey;
	private final String							smsUserPassword;

	private TWILIOMessageRetrievalServiceV02		twilioMessageRetrievalService	= null;

	private final DocumentBuilderFactory			documentBuilderFactory;
	private final SimpleDateFormat					receiverDateFormat;

	private final List<AsyncSendingThread>			runningAsyncSendingThreads;

	private final ConcurrentHashMap<String, Long>	lastTeamManagerNotificationsCache;

	private CommunicationManagerService(
			final VariablesManagerService variablesManagerService,
			final DatabaseManagerService databaseManagerService)
			throws Exception {
		log.info("Preparing service...");

		this.variablesManagerService = variablesManagerService;
		this.databaseManagerService = databaseManagerService;

		runningAsyncSendingThreads = new ArrayList<AsyncSendingThread>();

		lastTeamManagerNotificationsCache = new ConcurrentHashMap<String, Long>();

		simulatorActive = Constants.isSimulatedDateAndTime();

		// General settings
		emailActive = Constants.isEmailActive();
		smsActive = Constants.isSmsActive();
		deepstreamActive = Constants.isDeepstreamActive();
		pushNotificationsActive = Constants.isPushNotificationsActive();

		// Mailing configuration
		val mailhostIncoming = Constants.getMailhostIncoming();
		mailboxProtocol = Constants.getMailboxProtocol();
		mailboxFolder = Constants.getMailboxFolder();
		val mailhostOutgoing = Constants.getMailhostOutgoing();
		val mailUser = Constants.getMailUser();
		val mailPassword = Constants.getMailPassword();

		boolean useAuthentication;
		if (mailUser != null && !mailUser.equals("")) {
			log.debug("Using authentication for mail servers");
			useAuthentication = true;
		} else {
			log.debug("Using no authentication for mail servers");
			useAuthentication = false;
		}

		// Email configuration
		emailFrom = Constants.getEmailFrom();
		emailSubjectForParticipant = Constants.getEmailSubjectForParticipant();
		emailSubjectForSupervisor = Constants.getEmailSubjectForSupervisor();
		emailSubjectForTeamManager = Constants.getEmailSubjectForTeamManager();
		emailTemplateForTeamManager = Constants
				.getEmailTemplateForTeamManager();

		// SMS configuration
		smsServiceType = Constants.getSmsServiceType();
		smsMailSubjectStartsWith = Constants.getSmsMailSubjectStartsWith();
		smsUserKey = Constants.getSmsUserKey();
		smsUserPassword = Constants.getSmsUserPassword();

		if (smsActive && smsServiceType == SMSServiceType.TWILIO) {
			Twilio.init(smsUserKey, smsUserPassword);
		}

		// General properties
		val properties = new Properties();
		properties.setProperty("mail.pop3.timeout",
				ImplementationConstants.MAIL_SERVER_TIMEOUT);
		properties.setProperty("mail.pop3.connectiontimeout",
				ImplementationConstants.MAIL_SERVER_CONNECTION_TIMEOUT);
		properties.setProperty("mail.smtp.timeout",
				ImplementationConstants.MAIL_SERVER_TIMEOUT);
		properties.setProperty("mail.smtp.connectiontimeout",
				ImplementationConstants.MAIL_SERVER_CONNECTION_TIMEOUT);
		properties.setProperty("mail.smtps.timeout",
				ImplementationConstants.MAIL_SERVER_TIMEOUT);
		properties.setProperty("mail.smtps.connectiontimeout",
				ImplementationConstants.MAIL_SERVER_CONNECTION_TIMEOUT);
		properties.setProperty("mail.pop3.host", mailhostIncoming);
		properties.setProperty("mail.smtp.host", mailhostOutgoing);
		if (useAuthentication) {
			properties.setProperty("mail.pop3.auth", "true");
			properties.setProperty("mail.smtp.auth", "true");
		}
		log.debug(properties);

		// Setup mail sessions (if required)
		if (emailActive || smsActive) {
			if (useAuthentication) {
				incomingMailSession = Session.getInstance(properties,
						new PasswordAuthenticator(mailUser, mailPassword));
				outgoingMailSession = Session.getInstance(properties,
						new PasswordAuthenticator(mailUser, mailPassword));
			} else {
				incomingMailSession = Session.getInstance(properties);
				outgoingMailSession = Session.getInstance(properties);
			}
		} else {
			incomingMailSession = null;
			outgoingMailSession = null;
		}

		// Prepare XML parsing
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		receiverDateFormat = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.US);

		// Initialize deepstream communication service (if required)
		if (deepstreamActive) {
			deepstreamCommunicationService = DeepstreamCommunicationService
					.prepare(Constants.getDeepstreamHost(),
							Constants.getDeepstreamServerPassword(), this);
		} else {
			deepstreamCommunicationService = null;
		}
		// Initialize push notification service (if required)
		if (pushNotificationsActive) {
			pushNotificationService = PushNotificationService
					.prepare(Constants.isPushNotificationsIOSActive(),
							Constants.isPushNotificationsIOSEncrypted(),
							Constants.isPushNotificationsAndroidActive(),
							Constants.isPushNotificationsAndroidEncrypted(),
							Constants.isPushNotificationsProductionMode(),
							Constants.getPushNotificationsIOSAppIdentifier(),
							Constants.getPushNotificationsIOSCertificateFile(),
							Constants
									.getPushNotificationsIOSCertificatePassword(),
							Constants.getPushNotificationsAndroidAuthKey());
		} else {
			pushNotificationService = null;
		}

		log.info("Prepared.");
	}

	public static CommunicationManagerService prepare(
			final VariablesManagerService variablesManagerService,
			final DatabaseManagerService databaseManagerService)
			throws Exception {
		if (instance == null) {
			instance = new CommunicationManagerService(variablesManagerService, databaseManagerService);
		}

		return instance;
	}

	public void start(
			final InterventionExecutionManagerService interventionExecutionManagerService)
			throws Exception {
		log.info("Starting service...");

		this.interventionExecutionManagerService = interventionExecutionManagerService;

		if (deepstreamActive) {
			deepstreamCommunicationService
					.startThreadedService(interventionExecutionManagerService);
		}
		if (pushNotificationsActive) {
			pushNotificationService.start(interventionExecutionManagerService);
		}

		log.info("Started.");
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		if (deepstreamActive) {
			log.debug("Stopping deepstream service...");
			try {
				deepstreamCommunicationService.stopThreadedService();
			} catch (final Exception e) {
				log.warn("Error when stopping deepstream service: {}",
						e.getMessage());
			}
		}
		if (pushNotificationsActive) {
			log.debug("Stopping push notification service...");
			try {
				pushNotificationService.stop();
			} catch (final Exception e) {
				log.warn("Error when stopping push notification service: {}",
						e.getMessage());
			}
		}

		log.debug("Stopping mailing threads...");
		synchronized (runningAsyncSendingThreads) {
			for (val runningAsyncSendingThread : runningAsyncSendingThreads) {
				synchronized (runningAsyncSendingThread) {
					runningAsyncSendingThread.interrupt();
					runningAsyncSendingThread.join();
				}
			}
		}

		log.info("Stopped.");
	}

	@Synchronized
	private void ensureSensefulParticipantTimings(
			final DatabaseManagerService databaseManagerService) {
		log.info("Ensuring senseful participant login/logout timinigs...");

		val participants = databaseManagerService.findModelObjects(
				Participant.class,
				Queries.PARTICIPANT__WHERE_LAST_LOGIN_TIME_IS_BIGGER_THAN_LAST_LOGOUT_TIME);

		for (val participant : participants) {
			participant.setLastLogoutTimestamp(
					participant.getLastLoginTimestamp());

			databaseManagerService.saveModelObject(participant);
		}

		log.info("Done");
	}

	/**
	 * Sends a message (asynchronous)
	 *
	 * @param dialogOption
	 * @param dialogMessage
	 * @param messageSender
	 */
	@Synchronized
	public void sendMessage(final DialogOption dialogOption,
			final DialogMessage dialogMessage, final String messageSender) {

		switch (dialogOption.getType()) {
			case SMS:
			case SUPERVISOR_SMS:
				if (smsActive) {
					val mailingThread = new AsyncSendingThread(dialogOption,
							dialogMessage.getId(), messageSender,
							dialogMessage.getMessageWithForcedLinks(),
							dialogMessage.isMessageExpectsAnswer());

					synchronized (runningAsyncSendingThreads) {
						runningAsyncSendingThreads.add(mailingThread);
					}

					mailingThread.start();
				}
				break;
			case EMAIL:
			case SUPERVISOR_EMAIL:
				if (emailActive) {
					val mailingThread = new AsyncSendingThread(dialogOption,
							dialogMessage.getId(), messageSender,
							dialogMessage.getMessageWithForcedLinks(),
							dialogMessage.isMessageExpectsAnswer());

					synchronized (runningAsyncSendingThreads) {
						runningAsyncSendingThreads.add(mailingThread);
					}

					mailingThread.start();
				}
				break;
			case EXTERNAL_ID:
			case SUPERVISOR_EXTERNAL_ID:
				int visibleMessagesSentSinceLogout = 0;

				if (!dialogMessage.isPushOnly()) {
					// Send only visible messages
					if (dialogOption.getData().startsWith(
							ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM)
							&& deepstreamActive) {
						try {
							visibleMessagesSentSinceLogout = deepstreamCommunicationService
									.asyncSendMessage(dialogOption,
											dialogMessage.getId());
						} catch (final Exception e) {
							log.warn(
									"Could not send message using deepstream: {}",
									e.getMessage());
						}
					} else {
						log.warn(
								"No appropriate handler could be found for external id dialog option with data {}",
								dialogOption.getData());
					}
				} else {
					// Mark push only push messages as sent
					if (dialogMessage.isMessageExpectsAnswer()) {
						interventionExecutionManagerService
								.dialogMessageStatusChangesForSending(
										dialogMessage.getId(),
										DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER,
										InternalDateTime.currentTimeMillis());
					} else {
						interventionExecutionManagerService
								.dialogMessageStatusChangesForSending(
										dialogMessage.getId(),
										DialogMessageStatusTypes.SENT_BUT_NOT_WAITING_FOR_ANSWER,
										InternalDateTime.currentTimeMillis());
					}

				}

				// Only send push notifications if
				// (1) it is switched on in general
				// (2) if the message was really sent to out / will in general
				// not be sent, because it's push only
				// (3) it is a visible message / a forced push message
				if (pushNotificationsActive
						&& (visibleMessagesSentSinceLogout > 0
								|| dialogMessage.isPushOnly())
						&& dialogMessage
								.getType() != DialogMessageTypes.COMMAND) {
					try {
						pushNotificationService.asyncSendPushNotification(
								dialogOption,
								cleanupForPush(dialogMessage.getTextFormat(),
										dialogMessage.getMessage()),
								visibleMessagesSentSinceLogout,
								dialogMessage.isPushOnly());
					} catch (final Exception e) {
						log.warn("Could not send push notification: {}",
								e.getMessage());
					}
				}
				break;
		}
	}

	/**
	 * Cleans the given message to be sent out as push notification
	 * 
	 * @param textFormat
	 * @param message
	 * @return
	 */
	private String cleanupForPush(final TextFormatTypes textFormat,
			final String message) {
		switch (textFormat) {
			case HTML:
				// TODO: Check for completeness
				return StringEscapeUtils
						.unescapeHtml4(message.replaceAll("(\r\n|\r|\n)", " ")
								.replaceAll("\\<br.*\\>", " ")
								.replaceAll("\\<.*\\>", ""));
			case PLAIN:
			default:
				return message;
		}
	}

	/**
	 * Receives messages
	 *
	 * @return
	 */
	public List<ReceivedMessage> receiveMessages() {
		val receivedMessages = new ArrayList<ReceivedMessage>();

		// Add Email messages (every x minutes only)
		if (emailActive
				& System.currentTimeMillis() > lastEmailCheck + (simulatorActive
						? ImplementationConstants.SMS_AND_EMAIL_RETRIEVAL_INTERVAL_IN_SECONDS_WITH_SIMULATOR
						: ImplementationConstants.SMS_AND_EMAIL_RETRIEVAL_INTERVAL_IN_SECONDS_WITHOUT_SIMULATOR)
						* 1000) {
			lastEmailCheck = System.currentTimeMillis();

			// TODO LONGTERM Emails are currently not handled, but it could be
			// implemented here.
		}

		// Add SMS messages (every x minutes only)
		if (smsActive
				& System.currentTimeMillis() > lastSMSCheck + (simulatorActive
						? ImplementationConstants.SMS_AND_EMAIL_RETRIEVAL_INTERVAL_IN_SECONDS_WITH_SIMULATOR
						: ImplementationConstants.SMS_AND_EMAIL_RETRIEVAL_INTERVAL_IN_SECONDS_WITHOUT_SIMULATOR)
						* 1000) {
			lastSMSCheck = System.currentTimeMillis();

			switch (smsServiceType) {
				case ASPSMS:
					receiveSMSMessagesFromASPSMS(receivedMessages);
					break;
				case TWILIO:
					receiveSMSMessagesFromTWILIO(receivedMessages);
					break;
			}
		}

		// Add deepstream messages
		if (deepstreamActive) {
			try {
				deepstreamCommunicationService
						.getReceivedMessages(receivedMessages);
				receiveMessagesFromExternalService(receivedMessages);
			} catch (final Exception e) {
				log.warn("Could not receive message using deepstream: {}",
						e.getMessage());
			}
		}

		/*
		 * Messages from other services could be retrieved here
		 */

		return receivedMessages;
	}
	
	private void receiveMessagesFromExternalService(final ArrayList<ReceivedMessage> receivedMessages) {

		List<ExternalServiceMessage> externalServiceMessages = new ArrayList<>();
		deepstreamCommunicationService.getReceivedExternalServiceMessages(externalServiceMessages);

		for (ExternalServiceMessage externalServiceMessage : externalServiceMessages) {

			val externalService = databaseManagerService.findOneModelObject(InterventionExternalService.class,
					Queries.INTERVENTION_EXTERNAL_SERVICE__BY_SERVICE_ID, externalServiceMessage.getServiceId());
			if (externalService == null) {
				// error
				return;
			}
			if (externalServiceMessage.getParticipants().isEmpty()) {
				val participants = databaseManagerService.findModelObjects(Participant.class,
						Queries.PARTICIPANT__BY_INTERVENTION, externalService.getIntervention());
				participants
						.forEach(participant -> externalServiceMessage.addParticipant(participant.getId().toString()));
			}

			for (String participantId : externalServiceMessage.getParticipants()) {

				//TODO Check valid ObjectId
				val participant = databaseManagerService.getModelObjectById(Participant.class,
						new ObjectId(participantId));
				if (participant != null && participant.getIntervention().equals(externalService.getIntervention())) {

					val dialogOptions = databaseManagerService.findModelObjects(DialogOption.class,
							Queries.DIALOG_OPTION__BY_PARTICIPANT, participant.getId());
					
					val receivedMessage = new ReceivedMessage();
					receivedMessage.setTypeIntention(false);
					receivedMessage.setRelatedMessageIdBasedOnOrder(-1);
					receivedMessage.setReceivedTimestamp(InternalDateTime.currentTimeMillis());
					receivedMessage.setExternalServiceId(externalServiceMessage.getServiceId());
					receivedMessage.setExternalService(true);
					receivedMessage.setMessage("");

					for (DialogOption dialogOption : dialogOptions) {
						receivedMessage.setSender(dialogOption.getData());
						
						if (dialogOption.getType() == DialogOptionTypes.EXTERNAL_ID) {
							receivedMessage.setType(DialogOptionTypes.EXTERNAL_ID);
							break;
						} else if (dialogOption.getType() == DialogOptionTypes.SUPERVISOR_EXTERNAL_ID) {
							receivedMessage.setType(DialogOptionTypes.SUPERVISOR_EXTERNAL_ID);
							break;
						} else {
							receivedMessage.setType(dialogOption.getType());
						}
					}
					receivedMessages.add(receivedMessage);
				}
			}
		}
	}

	/**
	 * Receive messages from ASPSMS
	 * 
	 * @param receivedMessages
	 */
	private void receiveSMSMessagesFromASPSMS(
			final ArrayList<ReceivedMessage> receivedMessages) {
		Store store = null;
		Folder folder = null;
		try {
			log.debug("Retrieving SMS messages from ASPSMS...");
			store = incomingMailSession.getStore(mailboxProtocol);
			store.connect();
			folder = store.getFolder(mailboxFolder);
			folder.open(Folder.READ_WRITE);

			for (val message : folder.getMessages()) {
				// Only handle messages who match the subject pattern
				if (message.getSubject().startsWith(smsMailSubjectStartsWith)) {
					try {

						log.debug("Mail received with subject '{}'",
								message.getSubject());
						val receivedMessage = new ReceivedMessage();
						receivedMessage.setType(DialogOptionTypes.SMS);
						receivedMessage.setTypeIntention(false);
						receivedMessage.setRelatedMessageIdBasedOnOrder(-1);

						// Parse message content
						val documentBuilder = documentBuilderFactory
								.newDocumentBuilder();
						val inputSource = new InputSource(new StringReader(
								String.class.cast(message.getContent())));
						val document = documentBuilder.parse(inputSource);

						final XPath xPath = XPathFactory.newInstance()
								.newXPath();

						val sender = ((NodeList) xPath.evaluate(
								"/aspsms/Originator/PhoneNumber",
								document.getDocumentElement(),
								XPathConstants.NODESET)).item(0)
										.getTextContent();

						receivedMessage.setSender(
								StringHelpers.cleanPhoneNumber(sender));

						val receivedTimestampString = ((NodeList) xPath
								.evaluate("/aspsms/DateReceived",
										document.getDocumentElement(),
										XPathConstants.NODESET)).item(0)
												.getTextContent();

						// Adjust for simulated date and time
						if (simulatorActive) {
							receivedMessage.setReceivedTimestamp(
									InternalDateTime.currentTimeMillis());
						} else {
							val receivedTimestamp = receiverDateFormat
									.parse(receivedTimestampString).getTime();
							receivedMessage
									.setReceivedTimestamp(receivedTimestamp);
						}

						val messageStringEncoded = ((NodeList) xPath.evaluate(
								"/aspsms/MessageData",
								document.getDocumentElement(),
								XPathConstants.NODESET)).item(0)
										.getTextContent();
						val messageString = URLDecoder
								.decode(messageStringEncoded, "ISO-8859-1");

						receivedMessage.setMessage(messageString);

						log.debug("Mail parsed as {}",
								receivedMessage.toString());

						receivedMessages.add(receivedMessage);
					} catch (final Exception e) {
						log.error(
								"Could not parse message, so remove it unparsed. Reason: {}",
								e.getMessage());
					}
				}

				// Delete also messages not matching the subject pattern
				message.setFlag(Flags.Flag.DELETED, true);
			}
		} catch (final Exception e) {
			log.error("Could not retrieve SMS messages from ASPSMS: {}",
					e.getMessage());
		} finally {
			try {
				folder.close(true);
			} catch (final Exception e) {
				// Do nothing
			}
			try {
				store.close();
			} catch (final Exception e) {
				// Do nothing
			}
		}
	}

	/**
	 * Receive message from TWILIO
	 * 
	 * @param receivedMessages
	 */
	private void receiveSMSMessagesFromTWILIO(
			final ArrayList<ReceivedMessage> receivedMessages) {
		try {
			log.debug("Retrieving SMS messages from TWILIO...");

			if (twilioMessageRetrievalService == null) {
				twilioMessageRetrievalService = TWILIOMessageRetrievalServiceV02
						.getInstance();
			}

			if (twilioMessageRetrievalService != null) {
				twilioMessageRetrievalService
						.getReceivedMessages(receivedMessages);
			}

		} catch (final Exception e) {
			log.error("Could not retrieve SMS messages from TWILIO: {}",
					e.getMessage());
		}
	}

	/**
	 * Acknowledges received message
	 * 
	 * @param dialogMessage
	 * @param receivedMessage
	 */
	public void acknowledgeMessage(final DialogMessage dialogMessage,
			final ReceivedMessage receivedMessage) {
		switch (receivedMessage.getType()) {
			case SMS:
			case SUPERVISOR_SMS:
				if (smsActive) {
					// Not necessary for SMS
				}
				break;
			case EMAIL:
			case SUPERVISOR_EMAIL:
				if (emailActive) {
					// Not necessary for Email
				}
				break;
			case EXTERNAL_ID:
			case SUPERVISOR_EXTERNAL_ID:
				if (deepstreamActive
						&& !StringUtils.isBlank(receivedMessage.getSender())
						&& receivedMessage.getSender().startsWith(
								ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM)) {
					deepstreamCommunicationService.asyncAcknowledgeMessage(
							dialogMessage, receivedMessage);
				}
				break;
		}
	}

	/**
	 * Inform about answering timeout of a message
	 * 
	 * @param dialogOption
	 * @param dialogMessage
	 */
	public void informAboutAnsweringTimeout(final DialogOption dialogOption,
			final DialogMessage dialogMessage) {
		switch (dialogOption.getType()) {
			case SMS:
			case SUPERVISOR_SMS:
				if (smsActive) {
					// Not necessary for SMS
				}
				break;
			case EMAIL:
			case SUPERVISOR_EMAIL:
				if (emailActive) {
					// Not necessary for Email
				}
				break;
			case EXTERNAL_ID:
			case SUPERVISOR_EXTERNAL_ID:
				if (deepstreamActive) {
					deepstreamCommunicationService
							.asyncInformAboutAnsweringTimeout(dialogOption,
									dialogMessage);
				}
				break;
		}
	}

	/**
	 * Password authenticator for mail accounts with authentication
	 *
	 * @author Andreas Filler
	 */
	private class PasswordAuthenticator extends Authenticator {
		private final String	mailUser;
		private final String	mailPassword;

		public PasswordAuthenticator(final String mailUser,
				final String mailPassword) {
			this.mailUser = mailUser;
			this.mailPassword = mailPassword;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(mailUser, mailPassword);
		}
	}

	/**
	 * Enables threaded sending of messages, with retries
	 *
	 * @author Andreas Filler
	 */
	private class AsyncSendingThread extends Thread {
		private final DialogOption	dialogOption;
		private final ObjectId		dialogMessageId;
		private final String		messageSender;
		private final String		message;
		private final boolean		messageExpectsAnswer;

		public AsyncSendingThread(final DialogOption dialogOption,
				final ObjectId dialogMessageId, final String smsPhoneNumberFrom,
				final String message, final boolean messageExpectsAnswer) {
			setName("Async Sending Thread " + dialogOption.getData());
			this.dialogOption = dialogOption;
			this.dialogMessageId = dialogMessageId;
			messageSender = smsPhoneNumberFrom;
			this.message = message;
			this.messageExpectsAnswer = messageExpectsAnswer;

			interventionExecutionManagerService
					.dialogMessageStatusChangesForSending(dialogMessageId,
							DialogMessageStatusTypes.SENDING,
							InternalDateTime.currentTimeMillis());
		}

		@Override
		public void run() {
			try {
				sendMessage(dialogOption, messageSender, message);

				if (messageExpectsAnswer) {
					interventionExecutionManagerService
							.dialogMessageStatusChangesForSending(
									dialogMessageId,
									DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER,
									InternalDateTime.currentTimeMillis());
				} else {
					interventionExecutionManagerService
							.dialogMessageStatusChangesForSending(
									dialogMessageId,
									DialogMessageStatusTypes.SENT_BUT_NOT_WAITING_FOR_ANSWER,
									InternalDateTime.currentTimeMillis());
				}

				removeFromList();

				return;
			} catch (final Exception e) {
				log.warn(
						"Could not send message/mail to {}, retrying later another {} times: ",
						dialogOption.getData(),
						ImplementationConstants.EMAIL_SENDING_RETRIES,
						e.getMessage());
			}

			for (int i = 0; i < ImplementationConstants.EMAIL_SENDING_RETRIES; i++) {
				try {
					TimeUnit.SECONDS.sleep(
							ImplementationConstants.EMAIL_SENDING_RETRIES_SLEEP_BETWEEN_RETRIES_IN_SECONDS);
				} catch (final InterruptedException e) {
					log.warn("Interrupted messaging sending approach {}", i);

					interventionExecutionManagerService
							.dialogMessageStatusChangesForSending(
									dialogMessageId,
									DialogMessageStatusTypes.PREPARED_FOR_SENDING,
									InternalDateTime.currentTimeMillis());

					return;
				}

				try {
					sendMessage(dialogOption, messageSender, message);

					if (messageExpectsAnswer) {
						interventionExecutionManagerService
								.dialogMessageStatusChangesForSending(
										dialogMessageId,
										DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER,
										InternalDateTime.currentTimeMillis());
					} else {
						interventionExecutionManagerService
								.dialogMessageStatusChangesForSending(
										dialogMessageId,
										DialogMessageStatusTypes.SENT_BUT_NOT_WAITING_FOR_ANSWER,
										InternalDateTime.currentTimeMillis());
					}

					removeFromList();

					return;
				} catch (final Exception e) {
					log.warn("Could not send mail to {} in approach {}: {}",
							dialogOption.getData(), i, e.getMessage());
				}
			}

			log.error("Could not send mail to {} several times...giving up",
					dialogOption.getData());

			interventionExecutionManagerService
					.dialogMessageStatusChangesForSending(dialogMessageId,
							DialogMessageStatusTypes.PREPARED_FOR_SENDING,
							InternalDateTime.currentTimeMillis());

			removeFromList();
		}

		private void removeFromList() {
			synchronized (runningAsyncSendingThreads) {
				runningAsyncSendingThreads.remove(this);
			}
		}

		/**
		 * Async send messages
		 *
		 * @param dialogOption
		 * @param messageSender
		 * @param message
		 * @throws AddressException
		 * @throws MessagingException
		 */
		private void sendMessage(final DialogOption dialogOption,
				final String messageSender, final String message)
				throws Exception {
			log.debug("Sending message with text {} to {}", message,
					dialogOption.getData());

			switch (dialogOption.getType()) {
				case SMS:
				case SUPERVISOR_SMS:
					switch (smsServiceType) {
						case ASPSMS:
							sendMessageUsingASPSMS(dialogOption, messageSender,
									message);
							break;
						case TWILIO:
							sendMessageUsingTWILIO(dialogOption, messageSender,
									message);
							break;
					}

					break;
				case EMAIL:
				case SUPERVISOR_EMAIL:
					val EmailMessage = new MimeMessage(outgoingMailSession);

					EmailMessage.setFrom(new InternetAddress(emailFrom));
					EmailMessage.addRecipient(Message.RecipientType.TO,
							new InternetAddress(dialogOption.getData()));
					if (dialogOption.getType() == DialogOptionTypes.EMAIL) {
						EmailMessage.setSubject(emailSubjectForParticipant);
					} else {
						EmailMessage.setSubject(emailSubjectForSupervisor);
					}
					EmailMessage.setText(message, "UTF-8");

					Transport.send(EmailMessage);
					break;
				case EXTERNAL_ID:
				case SUPERVISOR_EXTERNAL_ID:
					log.error(
							"An external ID message was tried to be sent by a mailing thread. This should never happen!");
					break;
			}

			log.debug("Message sent to {}", dialogOption.getData());
		}

		/**
		 * Sends message using ASPSMS
		 * 
		 * @param dialogOption
		 * @param messageSender
		 * @param message
		 * @throws Exception
		 */
		private void sendMessageUsingASPSMS(final DialogOption dialogOption,
				final String messageSender, final String message)
				throws Exception {
			HttpURLConnection conn = null;
			try {
				final URL url = new URL(ASPSMS_API_URL);
				conn = (HttpURLConnection) url.openConnection();

				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setConnectTimeout(
						ImplementationConstants.SMS_SERVER_TIMEOUT);
				conn.setReadTimeout(ImplementationConstants.SMS_SERVER_TIMEOUT);

				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");

				final JsonObject jsonObject = new JsonObject();

				jsonObject.addProperty("UserName", smsUserKey);
				jsonObject.addProperty("Password", smsUserPassword);
				jsonObject.addProperty("Originator", messageSender);
				val recipients = new JsonArray();
				recipients.add(dialogOption.getData());
				jsonObject.add("Recipients", recipients);
				jsonObject.addProperty("MessageText", message);
				jsonObject.addProperty("ForceGSM7bit", false);

				@Cleanup
				final OutputStreamWriter wr = new OutputStreamWriter(
						conn.getOutputStream());
				wr.write(jsonObject.toString());
				wr.flush();
				int status = 0;

				if (null != conn) {
					status = conn.getResponseCode();
				}

				if (status == 200) {
					@Cleanup
					val reader = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));

					final StringBuffer response = new StringBuffer();
					String line;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}

					if (response.toString().contains("\"StatusCode\":\"1\"")) {
						log.debug("Message accepted by ASPSMS gateway.");
					} else {
						log.warn("Message rejected by ASPSMS gateway: ",
								response);

						throw new Exception(
								"Message rejected: Status code " + status);
					}
				} else {
					log.warn("Message rejected by ASPSMS gateway: ", status);

					throw new Exception(
							"Message rejected: Status code " + status);
				}
				conn.disconnect();
			} catch (final Exception e) {
				log.debug(
						"Message rejected by ASPSMS gateway or connection failed: ",
						e.getMessage());

				throw e;
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
		}

		/**
		 * Sends message using TWILIO
		 * 
		 * @param dialogOption
		 * @param messageSender
		 * @param message
		 * @throws Exception
		 */
		private void sendMessageUsingTWILIO(final DialogOption dialogOption,
				final String messageSender, final String message)
				throws Exception {
			try {
				val smsMessage = com.twilio.rest.api.v2010.account.Message
						.creator(
								new PhoneNumber(StringHelpers
										.cleanPhoneNumberPlusFormat(
												dialogOption.getData())),
								new PhoneNumber(StringHelpers
										.cleanPhoneNumberPlusFormat(
												messageSender)),
								message)
						.create();

				log.debug("Message sent using TWILIO: {}", smsMessage.getSid());
			} catch (final Exception e) {
				log.debug("Message rejected by TWILIO: ", e.getMessage());

				throw e;
			}
		}
	}

	public int getAsyncSendingThreadCount() {
		return runningAsyncSendingThreads.size();
	}

	public void sendDashboardChatNotification(final boolean sendToParticipant,
			final ObjectId participantId, final String message,
			final int visibleMessagesSentSinceLogout,
			final String dialogOptionData) {
		if (sendToParticipant && pushNotificationsActive) {
			// Message by team manager (send push notification to participant)

			if (visibleMessagesSentSinceLogout > 0) {
				val dialogOption = interventionExecutionManagerService
						.getDialogOptionByTypeAndDataOfActiveInterventions(
								DialogOptionTypes.EXTERNAL_ID,
								dialogOptionData);

				if (dialogOption != null) {
					try {
						pushNotificationService.asyncSendPushNotification(
								dialogOption,
								ImplementationConstants.TEAM_MANAGER_PUSH_NOTIFICATION_PREFIX
										+ message,
								visibleMessagesSentSinceLogout, false);
					} catch (final Exception e) {
						log.warn(
								"Could not send team manager caused push notification: {}",
								e.getMessage());
					}
				}
			}
		} else if (!sendToParticipant && emailActive) {
			// Message by participant (send email notification to team-manager)

			boolean lastNotificationWithinSilenceDuration = false;
			val participantIdString = participantId.toHexString();
			if (lastTeamManagerNotificationsCache
					.containsKey(participantIdString)) {
				final long lastNotificationTimeStamp = lastTeamManagerNotificationsCache
						.get(participantIdString);

				if (lastNotificationTimeStamp
						+ ImplementationConstants.TEAM_MANAGER_EMAIL_NOTIFICATION_SILENCE_DURATION_IN_MINUTES
								* ImplementationConstants.MINUTES_TO_TIME_IN_MILLIS_MULTIPLICATOR > InternalDateTime
										.currentTimeMillis()) {
					lastNotificationWithinSilenceDuration = true;
				} else {
					lastTeamManagerNotificationsCache.put(participantIdString,
							InternalDateTime.currentTimeMillis());
				}
			} else {
				lastTeamManagerNotificationsCache.put(participantIdString,
						InternalDateTime.currentTimeMillis());
			}

			// Send notification if no former notification within the last x
			// minutes
			if (!lastNotificationWithinSilenceDuration) {
				val participant = interventionExecutionManagerService
						.getParticipantById(participantId);

				if (participant.getResponsibleTeamManagerEmail() != null) {
					val runnable = new Runnable() {

						@Override
						public void run() {
							log.debug(
									"Sending notification for new message to team manager");
							val variablesWithValues = variablesManagerService
									.getAllVariablesWithValuesOfParticipantAndSystemAndExternalService(
											participant);

							val message = VariableStringReplacer
									.findVariablesAndReplaceWithTextValues(
											participant.getLanguage(),
											emailTemplateForTeamManager,
											variablesWithValues.values(), "");

							try {
								val emailMessage = new MimeMessage(
										outgoingMailSession);

								emailMessage.setFrom(
										new InternetAddress(emailFrom));
								emailMessage.addRecipient(
										Message.RecipientType.TO,
										new InternetAddress(participant
												.getResponsibleTeamManagerEmail()));
								emailMessage
										.setSubject(emailSubjectForTeamManager);
								emailMessage.setText(message, "UTF-8");

								Transport.send(emailMessage);
							} catch (final Exception e) {
								log.warn(
										"Error when trying to send notifiation email to team manager: {}",
										e);
							}
						}
					};

					new Thread(runnable).start();
				}
			}
		}

	}
}
