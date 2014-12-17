package ch.ethz.mc.services.threads;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
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