package org.isgf.mhc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.ImplementationContants;
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
						.sleep(ImplementationContants.MAILING_SENDING_CHECK_SLEEP_CYCLE_IN_SECONDS);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug("Outgoing message worker received signal to stop");
			}
		}
	}
}