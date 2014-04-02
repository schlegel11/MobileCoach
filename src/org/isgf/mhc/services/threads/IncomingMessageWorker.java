package org.isgf.mhc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.services.internal.CommunicationManagerService;

/**
 * Manages the handling of incoming messages
 * 
 * @author Andreas Filler
 */
@Log4j2
public class IncomingMessageWorker extends Thread {
	private final CommunicationManagerService	communicationManagerService;

	public IncomingMessageWorker(
			final CommunicationManagerService communicationManagerService) {
		setName("Incoming Message Worker");

		this.communicationManagerService = communicationManagerService;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			log.debug("Executing new run of incoming message worker...");

			val receivedMessages = communicationManagerService
					.receiveMessages();
			log.debug("Received {} messages", receivedMessages.size());

			for (val receivedMessage : receivedMessages) {
				// TODO
				// adjustDialogMessageToIncomingMessage(receivedMessage);
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