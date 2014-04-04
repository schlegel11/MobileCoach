package org.isgf.mhc.services.threads;

import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.services.InterventionExecutionManagerService;

/**
 * Manages the handling of interventions, rules and participants (== the
 * heart of this
 * application)
 * 
 * @author Andreas Filler
 */
@Log4j2
public class MasterRuleEvaluationWorker extends Thread {
	private final InterventionExecutionManagerService	interventionExecutionManagerService;

	public MasterRuleEvaluationWorker(
			final InterventionExecutionManagerService interventionExecutionManagerService) {
		setName("Master Rule Evaluation Worker");

		this.interventionExecutionManagerService = interventionExecutionManagerService;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			log.debug("Executing new run of master rule evaluation worker...");

			// TODO
			// determineParticipantsRelevantForRuleExecution(); //
			// intervention active, messaging active, messaging of
			// participant acitive , participant finished screening survey
			//
			// prepareDialogMessagesBasedOnRuleEvaluation(participants);

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