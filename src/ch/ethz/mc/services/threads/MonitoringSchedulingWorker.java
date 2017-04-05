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
import java.io.File;
import java.util.concurrent.TimeUnit;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.SurveyExecutionManagerService;
import ch.ethz.mc.tools.StringHelpers;

/**
 * Manages the scheduling of monitoring messages, i.e. intervention, monitoring
 * messages, rules, participants and all other relevant parts in this system
 *
 * @author Andreas Filler
 */
@Log4j2
public class MonitoringSchedulingWorker extends Thread {
	private final SurveyExecutionManagerService	screeningSurveyExecutionManagerService;
	private final InterventionExecutionManagerService		interventionExecutionManagerService;

	private final boolean									statisticsEnabled;
	private String											lastStatisticsCreation	= "";

	private static File										statisticsFile			= null;

	public MonitoringSchedulingWorker(
			final InterventionExecutionManagerService interventionExecutionManagerService,
			final SurveyExecutionManagerService screeningSurveyExecutionManagerService) {
		setName("Monitoring Sheduling Worker");
		setPriority(NORM_PRIORITY - 2);

		this.screeningSurveyExecutionManagerService = screeningSurveyExecutionManagerService;
		this.interventionExecutionManagerService = interventionExecutionManagerService;
		statisticsEnabled = Constants.isStatisticsFileEnabled();
		if (statisticsEnabled) {
			statisticsFile = new File(Constants.getStatisticsFile());
		}
	}

	@Override
	public void run() {
		val simulatorActive = Constants.isSimulatedDateAndTime();
		try {
			TimeUnit.SECONDS
					.sleep(simulatorActive ? ImplementationConstants.MASTER_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITH_SIMULATOR
							: ImplementationConstants.MASTER_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITHOUT_SIMULATOR);
		} catch (final InterruptedException e) {
			interrupt();
			log.debug("Monitoring scheduling worker received signal to stop (before first run)");
		}

		while (!isInterrupted()) {
			final long startingTime = System.currentTimeMillis();
			log.info("Executing new run of monitoring scheduling worker...started");

			try {
				if (statisticsEnabled) {
					try {
						val dailyUniqueIndex = StringHelpers
								.createDailyUniqueIndex();
						if (!lastStatisticsCreation.equals(dailyUniqueIndex)) {
							log.debug("It's a new day so create statistics");
							lastStatisticsCreation = dailyUniqueIndex;
							interventionExecutionManagerService
									.createStatistics(statisticsFile);
						}
					} catch (final Exception e) {
						log.error("Could not create statistics file: {}",
								e.getMessage());
					}
				}

				/*
				 * The following four steps should always be performed in this
				 * order to retain data consistency
				 */
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
					log.debug("Scheduling new messages");
					interventionExecutionManagerService
							.scheduleMessagesForSending();
				} catch (final Exception e) {
					log.error("Could not schedule new monitoring messages: {}",
							e.getMessage());
				}

			} catch (final Exception e) {
				log.error("Could not run whole scheduling process: {}",
						e.getMessage());
			}

			log.info(
					"Executing new run of monitoring scheduling worker...done ({} seconds)",
					(System.currentTimeMillis() - startingTime) / 1000.0);

			try {
				TimeUnit.SECONDS
						.sleep(simulatorActive ? ImplementationConstants.MASTER_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITH_SIMULATOR
								: ImplementationConstants.MASTER_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITHOUT_SIMULATOR);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug("Monitoring scheduling worker received signal to stop");
			}
		}
	}
}