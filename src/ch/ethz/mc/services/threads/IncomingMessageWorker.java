package ch.ethz.mc.services.threads;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.util.concurrent.TimeUnit;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.memory.SystemLoad;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.internal.CommunicationManagerService;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Manages the handling of incoming messages
 * 
 * @author Andreas Filler
 */
@Log4j2
public class IncomingMessageWorker extends Thread {
	private final SystemLoad							systemLoad;

	private final InterventionExecutionManagerService	interventionExecutionManagerService;
	private final CommunicationManagerService			communicationManagerService;

	@Setter
	private boolean										shouldStop	= false;

	public IncomingMessageWorker(
			final InterventionExecutionManagerService interventionExecutionManagerService,
			final CommunicationManagerService communicationManagerService) {
		setName("Incoming Message Worker");
		setPriority(NORM_PRIORITY - 2);

		systemLoad = SystemLoad.getInstance();

		this.interventionExecutionManagerService = interventionExecutionManagerService;
		this.communicationManagerService = communicationManagerService;
	}

	@Override
	public void run() {
		try {
			TimeUnit.MILLISECONDS.sleep(
					ImplementationConstants.INCOMING_MESSAGE_WORKER_MILLISECONDS_SLEEP_BETWEEN_CHECK_CYCLES);
		} catch (final InterruptedException e) {
			interrupt();
			log.debug(
					"Incoming message worker received signal to stop (before first run)");
		}

		while (!isInterrupted() && !shouldStop) {
			final long startingTime = System.currentTimeMillis();
			log.debug("Executing new run of incoming message worker...started");

			try {
				val receivedMessages = communicationManagerService
						.receiveMessages();
				log.debug("Received {} messages", receivedMessages.size());

				for (val receivedMessage : receivedMessages) {
					try {
						val dialogMessage = interventionExecutionManagerService
								.handleReceivedMessage(receivedMessage);
						if (dialogMessage != null) {
							communicationManagerService.acknowledgeMessage(
									dialogMessage, receivedMessage);
						}
					} catch (final Exception e) {
						log.error("Could not handle received message: {}",
								e.getMessage());
					}
				}
			} catch (final Exception e) {
				log.error("Could not handle all received messages: {}",
						e.getMessage());
			}

			systemLoad.setIncomingMessageWorkerRequiredMillis(
					System.currentTimeMillis() - startingTime);
			log.debug(
					"Executing new run of incoming message worker...done ({} milliseconds)",
					System.currentTimeMillis() - startingTime);

			try {
				TimeUnit.MILLISECONDS.sleep(
						ImplementationConstants.INCOMING_MESSAGE_WORKER_MILLISECONDS_SLEEP_BETWEEN_CHECK_CYCLES);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug(
						"Incoming message worker received signal to stop (interrupted)");
				return;
			}
		}
		log.debug("Incoming message worker received signal to stop");
	}
}