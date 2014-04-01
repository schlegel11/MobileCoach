package org.isgf.mhc.services.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.memory.ReceivedMessage;
import org.isgf.mhc.model.persistent.DialogOption;

@Log4j2
public class CommunicationManagerService {
	private static CommunicationManagerService	instance	= null;

	private final Session						mailSession;

	private final String						mailhostIncoming;
	private final boolean						useAuthentication;
	private final String						mailboxFolder;

	private final String						smsEmailFrom;
	private final String						smsEmailTo;
	private final String						smsUserKey;
	private final String						smsUserPassword;
	private final String						smsPhoneNumberFrom;

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

	private CommunicationManagerService() throws Exception {
		log.info("Starting service...");

		// Mailing configuration
		mailhostIncoming = Constants.getMailhostIncoming();
		val mailhostOutgoing = Constants.getMailhostOutgoing();
		val mailUser = Constants.getMailUser();
		val mailPassword = Constants.getMailPassword();
		mailboxFolder = Constants.getMailboxFolder();
		if (mailUser != null && !mailUser.equals("")) {
			useAuthentication = true;
		} else {
			useAuthentication = false;
		}

		// SMS configuration
		smsEmailFrom = Constants.getSmsEmailFrom();
		smsEmailTo = Constants.getSmsEmailTo();
		smsUserKey = Constants.getSmsUserKey();
		smsUserPassword = Constants.getSmsUserPassword();
		smsPhoneNumberFrom = Constants.getSmsPhoneNumberFrom();

		// Setup outgoing mail session
		val properties = new Properties();
		properties.setProperty("mail.smtp.host", mailhostOutgoing);
		if (useAuthentication) {
			properties.setProperty("mail.smtp.auth", "true");
		}

		if (useAuthentication) {
			mailSession = Session.getInstance(properties,
					new PasswordAuthenticator(mailUser, mailPassword));
		} else {
			mailSession = Session.getDefaultInstance(properties);
		}

		// Setup incoming mail session
		// TODO incoming mail

		log.info("Started.");
	}

	public static CommunicationManagerService start() throws Exception {
		if (instance == null) {
			instance = new CommunicationManagerService();
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	/**
	 * Enables threaded sending of messages, with retries
	 * 
	 * @author Andreas Filler
	 */
	private class MailingThread extends Thread {
		private final DialogOption	dialogOption;
		private final String		message;

		public MailingThread(final DialogOption dialogOption,
				final String message) {
			setName("Mailing Thread " + dialogOption.getData());
			this.dialogOption = dialogOption;
			this.message = message;
		}

		@Override
		public void run() {
			try {
				sendMessage(dialogOption, message);
				return;
			} catch (final Exception e) {
				log.warn(
						"Could not send mail to {}, retrying later another {} times: ",
						dialogOption.getData(),
						ImplementationContants.MAILING_RETRIES, e.getMessage());
			}

			for (int i = 0; i < ImplementationContants.MAILING_RETRIES; i++) {
				try {
					sleep(ImplementationContants.SLEEP_MILLIS_BETWEEN_MAILING_RETRIES);
				} catch (final InterruptedException e) {
					log.warn("Interrupted messaging sending approach {}", i);
					return;
				}

				try {
					sendMessage(dialogOption, message);
					return;
				} catch (final Exception e) {
					log.warn("Could not send mail to {} in approach {}: ",
							dialogOption.getData(), i, e.getMessage());
				}
			}

			log.error("Could not send mail to {} several times...giving up",
					dialogOption.getData());
		}

		/**
		 * Sends message using SMTP protocol
		 * 
		 * @param dialogOption
		 * @param message
		 * @throws AddressException
		 * @throws MessagingException
		 */
		private void sendMessage(final DialogOption dialogOption,
				final String message) throws AddressException,
				MessagingException {
			log.debug("Sending mail for outgoing SMS to {} with text {}",
					dialogOption.getData(), message);

			val mailMessage = new MimeMessage(mailSession);

			mailMessage.setFrom(new InternetAddress(smsEmailFrom));
			mailMessage.addRecipient(Message.RecipientType.TO,
					new InternetAddress(smsEmailTo));
			mailMessage.setSubject("UserKey=" + smsUserKey + ",Password="
					+ smsUserPassword + ",Recipient=" + dialogOption.getData()
					+ ",Originator=" + smsPhoneNumberFrom + ",Notify=none");
			mailMessage.setText(message, "ISO-8859-1");

			mailMessage.setText(message);
			Transport.send(mailMessage);

			log.debug("Message sent to {}", dialogOption.getData());
		}
	}

	public void sendMessage(final DialogOption dialogOption,
			final String message) {
		val mailingThread = new MailingThread(dialogOption, message);
		mailingThread.start();
	}

	public List<ReceivedMessage> receiveMessages() {
		val receivedMessages = new ArrayList<ReceivedMessage>();

		// TODO receive email (see homer)

		return receivedMessages;
	}
}
