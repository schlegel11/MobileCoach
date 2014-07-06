package ch.ethz.mc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.internal.CommunicationManagerService;

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
		try {
			TimeUnit.SECONDS
					.sleep(ImplementationConstants.MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS);
		} catch (final InterruptedException e) {
			interrupt();
			log.debug("Incoming message worker received signal to stop (before first run)");
		}

		while (!isInterrupted()) {
			log.info("Executing new run of incoming message worker...started");

			try {
				val receivedMessages = communicationManagerService
						.receiveMessages();
				log.debug("Received {} messages", receivedMessages.size());

				for (val receivedMessage : receivedMessages) {
					try {
						interventionExecutionManagerService
								.handleReceivedMessage(receivedMessage);
					} catch (final Exception e) {
						log.error("Could not handle  received message: {}",
								e.getMessage());
					}
				}
			} catch (final Exception e) {
				log.error("Could not handle all received messages: {}",
						e.getMessage());
			}

			log.info("Executing new run of incoming message worker...done");
			try {
				TimeUnit.SECONDS
						.sleep(ImplementationConstants.MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug("Incoming message worker received signal to stop");
			}
		}
	}
}