package ch.ethz.mc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.ScreeningSurveyExecutionManagerService;

/**
 * Manages the sheduling of monitoring messages, i.e. intervention, monitoring
 * messages, rules, participants and all other relevant parts in this system
 * 
 * @author Andreas Filler
 */
@Log4j2
public class MonitoringShedulingWorker extends Thread {
	private final ScreeningSurveyExecutionManagerService	screeningSurveyExecutionManagerService;
	private final InterventionExecutionManagerService		interventionExecutionManagerService;

	public MonitoringShedulingWorker(
			final InterventionExecutionManagerService interventionExecutionManagerService,
			final ScreeningSurveyExecutionManagerService screeningSurveyExecutionManagerService) {
		setName("Monitoring Sheduling Worker");
		setPriority(NORM_PRIORITY - 2);

		this.screeningSurveyExecutionManagerService = screeningSurveyExecutionManagerService;
		this.interventionExecutionManagerService = interventionExecutionManagerService;
	}

	@Override
	public void run() {
		try {
			TimeUnit.SECONDS
					.sleep(ImplementationConstants.MASTER_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES);
		} catch (final InterruptedException e) {
			interrupt();
			log.debug("Monitoring sheduling worker received signal to stop (before first run)");
		}

		while (!isInterrupted()) {
			log.info("Executing new run of monitoring sheduling worker...started");

			try {
				try {
					log.debug("Finishing unfinished screening surveys");
					screeningSurveyExecutionManagerService
							.finishUnfinishedScreeningSurveys();
				} catch (final Exception e) {
					log.error(
							"Could not finish unfinished screening surveys: {}",
							e.getMessage());
				}
				try {
					log.debug("React on unanswered messages");
					interventionExecutionManagerService
							.reactOnAnsweredAndUnansweredMessages(false);
				} catch (final Exception e) {
					log.error("Could not react on unanswered messages: {}",
							e.getMessage());
				}
				try {
					log.debug("React on answered messages");
					interventionExecutionManagerService
							.reactOnAnsweredAndUnansweredMessages(true);
				} catch (final Exception e) {
					log.error("Could not react on answered messages: {}",
							e.getMessage());
				}
				try {
					log.debug("Sheduling new messages");
					interventionExecutionManagerService
							.scheduleMessagesForSending();
				} catch (final Exception e) {
					log.error("Could not shedule new monitoring messages: {}",
							e.getMessage());
				}
			} catch (final Exception e) {
				log.error("Could not run whole sheduling process: {}",
						e.getMessage());
			}

			log.info("Executing new run of monitoring sheduling worker...done");

			try {
				TimeUnit.SECONDS
						.sleep(ImplementationConstants.MASTER_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug("Monitoring sheduling worker received signal to stop");
			}
		}
	}
}