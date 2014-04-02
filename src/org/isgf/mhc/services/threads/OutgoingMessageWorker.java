package org.isgf.mhc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.services.internal.CommunicationManagerService;

/**
 * Manages the handling of outgoing messages
 * 
 * @author Andreas Filler
 */
@Log4j2
public class OutgoingMessageWorker extends Thread {
	private final CommunicationManagerService	communicationManagerService;

	public OutgoingMessageWorker(
			final CommunicationManagerService communicationManagerService) {
		setName("Outgoing Message Worker");

		this.communicationManagerService = communicationManagerService;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			log.debug("Executing new run of outgoing message worker...");

			// TODO implementieren, aber hier lassen
			// val dialogMessagesToSend = determineDialogMessagesToSend();
			//
			// for (val dialogMessageToSend : dialogMessagesToSend) {
			// val dialogOption =
			// dialogDialogOptionForParticipant(dialogMessagesToSend
			// .getParticipant());
			// communicationManagerService.sendMessage(dialogOption,
			// dialogMessageToSend.getId(),
			// dialogMessageToSend.getMessage());
			// }

			try {
				TimeUnit.SECONDS
						.sleep(ImplementationContants.MAILING_SEND_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug("Outgoing message worker received signal to stop");
			}
		}
	}
}