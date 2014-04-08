package org.isgf.mhc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.services.InterventionExecutionManagerService;

/**
 * Manages the sheduling of monitoring messages, i.e. intervention, monitoring
 * messages, rules, participants and all other relevant parts in this system
 * 
 * @author Andreas Filler
 */
@Log4j2
public class MonitoringShedulingWorker extends Thread {
	private final InterventionExecutionManagerService	interventionExecutionManagerService;

	public MonitoringShedulingWorker(
			final InterventionExecutionManagerService interventionExecutionManagerService) {
		setName("Monitoring Sheduling Worker");
		setPriority(NORM_PRIORITY - 2);

		this.interventionExecutionManagerService = interventionExecutionManagerService;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			log.debug("Executing new run of monitoring sheduling worker...started");

			try {
				try {
					log.debug("React on unanswered messages");
					interventionExecutionManagerService
							.reactOnAnsweredAndUnansweredMessages(false);
				} catch (final Exception e) {
					log.error("Could react on unanswered messages: {}",
							e.getMessage());
				}
				try {
					log.debug("React on answered messages");
					interventionExecutionManagerService
							.reactOnAnsweredAndUnansweredMessages(true);
				} catch (final Exception e) {
					log.error("Could react on answered messages: {}",
							e.getMessage());
				}
				try {
					log.debug("Sheduling new messages");
					interventionExecutionManagerService
							.sheduleMessagesForSending();
				} catch (final Exception e) {
					log.error("Could not shedule new monitoring messages: {}",
							e.getMessage());
				}
			} catch (final Exception e) {
				log.error("Could not run whole sheduling process: {}",
						e.getMessage());
			}

			log.debug("Executing new run of monitoring sheduling worker...done");

			try {
				TimeUnit.MINUTES
						.sleep(ImplementationContants.MASTER_RULE_EVALUTION_WORKER_MINUTES_SLEEP_BETWEEN_CHECK_CYCLES);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug("Monitoring sheduling worker received signal to stop");
			}
		}
	}
}