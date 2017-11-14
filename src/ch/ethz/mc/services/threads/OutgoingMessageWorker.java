package ch.ethz.mc.services.threads;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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

import lombok.Setter;
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

	@Setter
	private boolean										shouldStop	= false;

	public OutgoingMessageWorker(
			final InterventionExecutionManagerService interventionExecutionManagerService) {
		setName("Outgoing Message Worker");
		setPriority(NORM_PRIORITY - 2);

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