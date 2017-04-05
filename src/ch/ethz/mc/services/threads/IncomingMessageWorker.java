package ch.ethz.mc.services.threads;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.concurrent.TimeUnit;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.Constants;
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
		val simulatorActive = Constants.isSimulatedDateAndTime();
		try {
			TimeUnit.SECONDS
					.sleep(simulatorActive ? ImplementationConstants.MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS_WITH_SIMULATOR
							: ImplementationConstants.MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS_WITHOUT_SIMULATOR);
		} catch (final InterruptedException e) {
			interrupt();
			log.debug("Incoming message worker received signal to stop (before first run)");
		}

		while (!isInterrupted()) {
			final long startingTime = System.currentTimeMillis();
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

			log.info(
					"Executing new run of incoming message worker...done ({} seconds)",
					(System.currentTimeMillis() - startingTime) / 1000.0);
			try {
				TimeUnit.SECONDS
						.sleep(simulatorActive ? ImplementationConstants.MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS_WITH_SIMULATOR
								: ImplementationConstants.MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS_WITHOUT_SIMULATOR);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug("Incoming message worker received signal to stop");
			}
		}
	}
}