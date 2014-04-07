package org.isgf.mhc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.services.InterventionExecutionManagerService;

/**
 * Manages the handling of outgoing messages
 * 
 * @author Andreas Filler
 */
@Log4j2
public class OutgoingMessageWorker extends Thread {
	private final InterventionExecutionManagerService	interventionExecutionManagerService;

	public OutgoingMessageWorker(
			final InterventionExecutionManagerService interventionExecutionManagerService) {
		setName("Outgoing Message Worker");

		this.interventionExecutionManagerService = interventionExecutionManagerService;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			log.debug("Executing new run of outgoing message worker...");

			try {
				interventionExecutionManagerService.handleOutgoingMessages();
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
}