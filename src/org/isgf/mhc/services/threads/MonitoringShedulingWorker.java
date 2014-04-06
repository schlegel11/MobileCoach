package org.isgf.mhc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.services.InterventionExecutionManagerService;
import org.isgf.mhc.tools.StringHelpers;

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

		this.interventionExecutionManagerService = interventionExecutionManagerService;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			log.debug("Executing new run of monitoring sheduling worker...");

			try {
				log.debug("Create a list of all relevant participants for sheduling of monitoring messages");
				val participants = interventionExecutionManagerService
						.getAllParticipantsRelevantForMonitoringSheduling();

				try {
					log.debug("React on unanswered messages");

					// TODO unanswered messages --> rules
				} catch (final Exception e) {
					log.error("Could react on unanswered messages: {}",
							e.getMessage());
				}
				try {
					log.debug("React on answered messages");
					// TODO answered messages --> rules
				} catch (final Exception e) {
					log.error("Could react on answered messages: {}",
							e.getMessage());
				}
				try {
					log.debug("Sheduling new messages");
					val dateIndex = StringHelpers.createStringTimeStamp();
					for (val participant : participants) {

						// Check if participant has already been sheduled today
						val dialogStatus = interventionExecutionManagerService
								.getDialogStatusByParticipant(participant
										.getId());

						if (dialogStatus != null
								&& !dialogStatus
										.getDateIndexOfLastDailyMonitoringProcessing()
										.equals(dateIndex)) {
							interventionExecutionManagerService
									.dialogStatusSetDateIndexOfLastDailyMonitoringProcessing(
											dialogStatus.getId(), dateIndex);

							// TODO
							// prepareDialogMessagesBasedOnRuleEvaluation(participant);
						}
					}
				} catch (final Exception e) {
					log.error("Could not shedule new monitoring messages: {}",
							e.getMessage());
				}
			} catch (final Exception e) {
				log.error("Could not run whole sheduling process: {}",
						e.getMessage());
			}

			try {
				TimeUnit.MINUTES
						.sleep(ImplementationContants.MASTER_RULE_EVALUTION_WORKER_MINUTES_SLEEP_BETWEEN_CHECK_CYCLES);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug("Master rule evaluation worker received signal to stop");
			}
		}
	}
}