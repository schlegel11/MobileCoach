package org.isgf.mhc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.persistent.types.DialogMessageStatusTypes;
import org.isgf.mhc.services.InterventionExecutionManagerService;
import org.isgf.mhc.services.internal.CommunicationManagerService;
import org.isgf.mhc.tools.StringHelpers;

/**
 * Manages the handling of incoming messages
 * 
 * @author Andreas Filler
 */
@Log4j2
public class IncomingMessageWorker extends Thread {
	private final InterventionExecutionManagerService	interventionExecutionManagerService;
	private final CommunicationManagerService			communicationManagerService;

	public IncomingMessageWorker(
			final InterventionExecutionManagerService interventionExecutionManagerService,
			final CommunicationManagerService communicationManagerService) {
		setName("Incoming Message Worker");

		this.interventionExecutionManagerService = interventionExecutionManagerService;
		this.communicationManagerService = communicationManagerService;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			log.debug("Executing new run of incoming message worker...");

			try {
				val receivedMessages = communicationManagerService
						.receiveMessages();
				log.debug("Received {} messages", receivedMessages.size());

				for (val receivedMessage : receivedMessages) {
					try {
						val dialogOption = interventionExecutionManagerService
								.getDialogOptionByTypeAndData(
										communicationManagerService
												.getSupportedDialogOptionType(),
										StringHelpers
												.cleanPhoneNumber(receivedMessage
														.getSender()));
						if (dialogOption == null) {
							log.warn(
									"The received message with sender number '{}' does not fit to any participant, skip it",
									receivedMessage.getSender());
							continue;
						}

						val dialogMessage = interventionExecutionManagerService
								.getDialogMessageByParticipantAndStatus(
										dialogOption.getParticipant(),
										DialogMessageStatusTypes.SENT);

						if (dialogMessage == null) {
							log.debug(
									"Received an unexpected SMS from '{}', store it and mark it as unexpected",
									receivedMessage.getSender());
							interventionExecutionManagerService
									.dialogMessageCreateAsUnexpectedReceived(
											dialogOption.getParticipant(),
											receivedMessage);

							continue;
						}

						interventionExecutionManagerService
								.dialogMessageSetStatus(
										dialogMessage.getId(),
										DialogMessageStatusTypes.SENT_AND_ANSWERED_BY_PARTICIPANT,
										receivedMessage.getReceivedTimestamp(),
										receivedMessage.getMessage());
					} catch (final Exception e) {
						log.error("Could not handle  received message: {}",
								e.getMessage());
					}
				}
			} catch (final Exception e) {
				log.error("Could not handle all received messages: {}",
						e.getMessage());
			}

			try {
				TimeUnit.SECONDS
						.sleep(ImplementationContants.MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug("Incoming message worker received signal to stop");
			}
		}
	}
}