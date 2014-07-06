package ch.ethz.mc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.services.InterventionExecutionManagerService;

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
		try {
			TimeUnit.SECONDS
					.sleep(ImplementationConstants.MAILING_SENDING_CHECK_SLEEP_CYCLE_IN_SECONDS);
		} catch (final InterruptedException e) {
			interrupt();
			log.debug("Outgoing message worker received signal to stop (before first run)");
		}

		while (!isInterrupted()) {
			log.info("Executing new run of outgoing message worker...started");

			try {
				interventionExecutionManagerService.handleOutgoingMessages();
			} catch (final Exception e) {
				log.error("Could not send all prepared messages: {}",
						e.getMessage());
			}

			log.info("Executing new run of outgoing message worker...done");

			try {
				TimeUnit.SECONDS
						.sleep(ImplementationConstants.MAILING_SENDING_CHECK_SLEEP_CYCLE_IN_SECONDS);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug("Outgoing message worker received signal to stop");
			}
		}
	}
}