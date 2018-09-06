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
import java.io.StringReader;
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

import org.bson.types.ObjectId;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.DialogMessageTypes;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.tools.VariableStringReplacer;
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
	private static CommunicationManagerService		instance		= null;

	private final boolean							simulatorActive;

	private long									lastSMSCheck	= 0;
	private long									lastEmailCheck	= 0;

	private final boolean							emailActive;
	private final boolean							smsActive;
	private final boolean							deepstreamActive;
	private final boolean							pushNotificationsActive;

	@Getter
	private DeepstreamCommunicationService			deepstreamCommunicationService;
	private PushNotificationService					pushNotificationService;

	private InterventionExecutionManagerService		interventionExecutionManagerService;
	private final VariablesManagerService			variablesManagerService;

	private final Session							incomingMailSession;
	private final Session							outgoingMailSession;

	private final String							mailboxProtocol;
	private final String							mailboxFolder;

	private final String							emailFrom;
	private final String							emailSubjectForParticipant;
	private final String							emailSubjectForSupervisor;
	private final String							emailSubjectForTeamManager;
	private final String							emailTemplateForTeamManager;

	private final String							smsEmailFrom;
	private final String							smsMailSubjectStartsWith;
	private final String							smsEmailTo;
	private final String							smsUserKey;
	private final String							smsUserPassword;

	private final DocumentBuilderFactory			documentBuilderFactory;
	private final SimpleDateFormat					receiverDateFormat;

	private final List<MailingThread>				runningMailingThreads;

	private final ConcurrentHashMap<String, Long>	lastTeamManagerNotificationsCache;

	private CommunicationManagerService(
			final VariablesManagerService variablesManagerService)
			throws Exception {
		log.info("Preparing service...");

		this.variablesManagerService = variablesManagerService;

		runningMailingThreads = new ArrayList<MailingThread>();

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
		smsMailSubjectStartsWith = Constants.getSmsMailSubjectStartsWith();
		smsEmailFrom = Constants.getSmsEmailFrom();
		smsEmailTo = Constants.getSmsEmailTo();
		smsUserKey = Constants.getSmsUserKey();
		smsUserPassword = Constants.getSmsUserPassword();

		// General properties
		val properties = new Properties();
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
			final VariablesManagerService variablesManagerService)
			throws Exception {
		if (instance == null) {
			instance = new CommunicationManagerService(variablesManagerService);
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
		synchronized (runningMailingThreads) {
			for (val runningMailingThread : runningMailingThreads) {
				synchronized (runningMailingThread) {
					runningMailingThread.interrupt();
					runningMailingThread.join();
				}
			}
		}

		log.info("Stopped.");
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
					val mailingThread = new MailingThread(dialogOption,
							dialogMessage.getId(), messageSender,
							dialogMessage.getMessageWithForcedLinks(),
							dialogMessage.isMessageExpectsAnswer());

					synchronized (runningMailingThreads) {
						runningMailingThreads.add(mailingThread);
					}

					mailingThread.start();
				}
				break;
			case EMAIL:
			case SUPERVISOR_EMAIL:
				if (emailActive) {
					val mailingThread = new MailingThread(dialogOption,
							dialogMessage.getId(), messageSender,
							dialogMessage.getMessageWithForcedLinks(),
							dialogMessage.isMessageExpectsAnswer());

					synchronized (runningMailingThreads) {
						runningMailingThreads.add(mailingThread);
					}

					mailingThread.start();
				}
				break;
			case EXTERNAL_ID:
			case SUPERVISOR_EXTERNAL_ID:
				int visibleMessagesSentSinceLogout = 0;
				if (dialogOption.getData().startsWith(
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM)
						&& deepstreamActive) {
					try {
						visibleMessagesSentSinceLogout = deepstreamCommunicationService
								.asyncSendMessage(dialogOption,
										dialogMessage.getId());
					} catch (final Exception e) {
						log.warn("Could not send message using deepstream: {}",
								e.getMessage());
					}
				} else {
					log.warn(
							"No appropriate handler could be found for external id dialog option with data {}",
							dialogOption.getData());
				}
				// Only send push notifications if
				// (1) it is switched on in general
				// (2) if the message was really sent to out
				// (3) it is a visible message
				if (pushNotificationsActive
						&& visibleMessagesSentSinceLogout > 0 && dialogMessage
								.getType() != DialogMessageTypes.COMMAND) {
					try {
						pushNotificationService.asyncSendPushNotification(
								dialogOption, dialogMessage.getMessage(),
								visibleMessagesSentSinceLogout);
					} catch (final Exception e) {
						log.warn("Could not send push notification: {}",
								e.getMessage());
					}
				}
				break;
		}
	}

	/**
	 * Receives mail messages
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
		Store store = null;
		Folder folder = null;
		if (smsActive
				& System.currentTimeMillis() > lastSMSCheck + (simulatorActive
						? ImplementationConstants.SMS_AND_EMAIL_RETRIEVAL_INTERVAL_IN_SECONDS_WITH_SIMULATOR
						: ImplementationConstants.SMS_AND_EMAIL_RETRIEVAL_INTERVAL_IN_SECONDS_WITHOUT_SIMULATOR)
						* 1000) {
			lastSMSCheck = System.currentTimeMillis();

			try {
				log.debug("Retrieving SMS messages...");
				store = incomingMailSession.getStore(mailboxProtocol);
				store.connect();
				folder = store.getFolder(mailboxFolder);
				folder.open(Folder.READ_WRITE);

				for (val message : folder.getMessages()) {
					// Only handle messages who match the subject pattern
					if (message.getSubject()
							.startsWith(smsMailSubjectStartsWith)) {
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

							val receivedTimestamp = receiverDateFormat
									.parse(receivedTimestampString).getTime();

							// Abjust for simulated date and time
							if (simulatorActive) {
								receivedMessage.setReceivedTimestamp(
										InternalDateTime.currentTimeMillis());
							} else {
								receivedMessage.setReceivedTimestamp(
										receivedTimestamp);
							}

							val messageStringEncoded = ((NodeList) xPath
									.evaluate("/aspsms/MessageData",
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
				log.error("Could not retrieve SMS messages: {}",
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

		// Add deepstream messages
		if (deepstreamActive) {
			try {
				val receivedDeepstreamMessages = deepstreamCommunicationService
						.getReceivedMessages();
				receivedMessages.addAll(receivedDeepstreamMessages);
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
				if (deepstreamActive) {
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
	 * Enables threaded sending of mailing messages, with retries
	 *
	 * @author Andreas Filler
	 */
	private class MailingThread extends Thread {
		private final DialogOption	dialogOption;
		private final ObjectId		dialogMessageId;
		private final String		messageSender;
		private final String		message;
		private final boolean		messageExpectsAnswer;

		public MailingThread(final DialogOption dialogOption,
				final ObjectId dialogMessageId, final String smsPhoneNumberFrom,
				final String message, final boolean messageExpectsAnswer) {
			setName("Mailing Thread " + dialogOption.getData());
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
						"Could not send mail to {}, retrying later another {} times: ",
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
			synchronized (runningMailingThreads) {
				runningMailingThreads.remove(this);
			}
		}

		/**
		 * Sends mailing messages
		 *
		 * @param dialogOption
		 * @param messageSender
		 * @param message
		 * @throws AddressException
		 * @throws MessagingException
		 */
		private void sendMessage(final DialogOption dialogOption,
				final String messageSender, final String message)
				throws AddressException, MessagingException {
			log.debug("Sending message with text {} to {}", message,
					dialogOption.getData());

			switch (dialogOption.getType()) {
				case SMS:
				case SUPERVISOR_SMS:
					val SMSMailMessage = new MimeMessage(outgoingMailSession);

					SMSMailMessage.setFrom(new InternetAddress(smsEmailFrom));
					SMSMailMessage.addRecipient(Message.RecipientType.TO,
							new InternetAddress(smsEmailTo));
					SMSMailMessage.setSubject("UserKey=" + smsUserKey
							+ ",Password=" + smsUserPassword + ",Recipient="
							+ dialogOption.getData() + ",Originator="
							+ messageSender + ",Notify=none");
					SMSMailMessage.setText(message, "ISO-8859-1");

					Transport.send(SMSMailMessage);
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
	}

	public int getMessagingThreadCount() {
		return runningMailingThreads.size();
	}

	public void sendDashboardChatNotification(final boolean sendToParticipant,
			final ObjectId participantId, final String message,
			final int visibleMessagesSentSinceLogout,
			final String dialogOptionData) {
		if (sendToParticipant && pushNotificationsActive) {
			// Massage by team manager (send push notification to participant)

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
								visibleMessagesSentSinceLogout);
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
				val runnable = new Runnable() {

					@Override
					public void run() {
						log.debug(
								"Sending notification for new message to team manager");
						val participant = interventionExecutionManagerService
								.getParticipantById(participantId);
						val variablesWithValues = variablesManagerService
								.getAllVariablesWithValuesOfParticipantAndSystem(
										participant);

						val message = VariableStringReplacer
								.findVariablesAndReplaceWithTextValues(
										participant.getLanguage(),
										emailTemplateForTeamManager,
										variablesWithValues.values(), "");

						try {
							val emailMessage = new MimeMessage(
									outgoingMailSession);

							emailMessage
									.setFrom(new InternetAddress(emailFrom));
							emailMessage.addRecipient(Message.RecipientType.TO,
									new InternetAddress(participant
											.getResponsibleTeamManagerEmail()));
							emailMessage.setSubject(emailSubjectForTeamManager);
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
