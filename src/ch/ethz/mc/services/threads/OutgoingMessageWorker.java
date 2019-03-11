package ch.ethz.mc.services.threads;

/* ##LICENSE## */
import java.util.concurrent.TimeUnit;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.memory.SystemLoad;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Manages the handling of outgoing messages
 * 
 * @author Andreas Filler
 */
@Log4j2
public class OutgoingMessageWorker extends Thread {
	private final SystemLoad							systemLoad;

	private final InterventionExecutionManagerService	interventionExecutionManagerService;

	@Setter
	private boolean										shouldStop	= false;

	public OutgoingMessageWorker(
			final InterventionExecutionManagerService interventionExecutionManagerService) {
		setName("Outgoing Message Worker");
		setPriority(NORM_PRIORITY - 1);

		systemLoad = SystemLoad.getInstance();

		this.interventionExecutionManagerService = interventionExecutionManagerService;
	}

	@Override
	public void run() {
		try {
			TimeUnit.MILLISECONDS.sleep(
					ImplementationConstants.OUTGOING_MESSAGE_WORKER_MILLISECONDS_SLEEP_BETWEEN_CHECK_CYCLES);
		} catch (final InterruptedException e) {
			interrupt();
			log.debug(
					"Outgoing message worker received signal to stop (before first run)");
		}

		while (!isInterrupted() && !shouldStop) {
			final long startingTime = System.currentTimeMillis();
			log.debug("Executing new run of outgoing message worker...started");

			try {
				interventionExecutionManagerService.handleOutgoingMessages();
			} catch (final Exception e) {
				log.error("Could not send all prepared messages: {}",
						e.getMessage());
			}

			systemLoad.setOutgoingMessageWorkerRequiredMillis(
					System.currentTimeMillis() - startingTime);
			log.debug(
					"Executing new run of outgoing message worker...done ({} milliseconds)",
					System.currentTimeMillis() - startingTime);

			try {
				TimeUnit.MILLISECONDS.sleep(
						ImplementationConstants.OUTGOING_MESSAGE_WORKER_MILLISECONDS_SLEEP_BETWEEN_CHECK_CYCLES);
			} catch (final InterruptedException e) {
				interrupt();
				return;
			}
		}
		log.debug("Outgoing message worker received signal to stop");
	}
}