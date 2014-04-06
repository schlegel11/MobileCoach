package org.isgf.mhc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.persistent.DialogMessage;
import org.isgf.mhc.services.InterventionExecutionManagerService;
import org.isgf.mhc.services.internal.CommunicationManagerService;

/**
 * Manages the handling of outgoing messages
 * 
 * @author Andreas Filler
 */
@Log4j2
public class OutgoingMessageWorker extends Thread {
	private final InterventionExecutionManagerService	interventionExecutionManagerService;
	private final CommunicationManagerService			communicationManagerService;

	public OutgoingMessageWorker(
			final InterventionExecutionManagerService interventionExecutionManagerService,
			final CommunicationManagerService communicationManagerService) {
		setName("Outgoing Message Worker");

		this.interventionExecutionManagerService = interventionExecutionManagerService;
		this.communicationManagerService = communicationManagerService;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			log.debug("Executing new run of outgoing message worker...");

			try {
				final val dialogMessagesToSend = determineDialogMessagesToSend();
				for (val dialogMessageToSend : dialogMessagesToSend) {
					try {
						val dialogOption = interventionExecutionManagerService
								.getDialogOptionByParticipantAndType(
										dialogMessageToSend.getParticipant(),
										communicationManagerService
												.getSupportedDialogOptionType());

						if (dialogOption != null) {
							communicationManagerService.sendMessage(
									dialogOption, dialogMessageToSend.getId(),
									dialogMessageToSend.getMessage());
						} else {
							log.error("Could not send prepared message, because there was no valid dialog option to send message to participant; solution: deactive messaging for participant and removing current dialog message");

							try {
								interventionExecutionManagerService
										.deactivateMessagingForParticipantAndDeleteDialogMessages(dialogMessageToSend
												.getParticipant());
								log.debug("Cleanup sucessful");
							} catch (final Exception e) {
								log.error("Cleanup not sucessful: {}",
										e.getMessage());
							}
						}
					} catch (final Exception e) {
						log.error("Could not send prepared message: {}",
								e.getMessage());
					}
				}
			} catch (final Exception e) {
				log.error("Could not send all prepared messages: {}",
						e.getMessage());
			}

			try {
				TimeUnit.SECONDS
						.sleep(ImplementationContants.MAILING_SENDING_CHECK_SLEEP_CYCLE_IN_SECONDS);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug("Outgoing message worker received signal to stop");
			}
		}
	}

	/**
	 * Returns a list of {@link DialogMessage}s that should be sent
	 * 
	 * @return
	 */
	private Iterable<DialogMessage> determineDialogMessagesToSend() {
		val dialogMessagesWaitingToBeSend = interventionExecutionManagerService
				.getDialogMessagesWaitingToBeSent();
		return dialogMessagesWaitingToBeSend;
	}
}