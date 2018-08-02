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
import java.io.File;
import java.util.concurrent.TimeUnit;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.memory.SystemLoad;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.SurveyExecutionManagerService;
import ch.ethz.mc.tools.StringHelpers;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Manages the scheduling of monitoring messages, i.e. intervention, monitoring
 * messages, rules, participants and all other relevant parts in this system
 *
 * @author Andreas Filler
 */
@Log4j2
public class MonitoringSchedulingWorker extends Thread {
	private final SystemLoad							systemLoad;

	private final SurveyExecutionManagerService			screeningSurveyExecutionManagerService;
	private final InterventionExecutionManagerService	interventionExecutionManagerService;

	private final boolean								statisticsEnabled;
	private String										lastStatisticsCreation				= "";
	private String										lastCacheClearing					= "";
	private long										lastScreeningSurveyFinishingCheck	= 0;

	private static File									statisticsFile						= null;

	@Setter
	private boolean										shouldStop							= false;

	public MonitoringSchedulingWorker(
			final InterventionExecutionManagerService interventionExecutionManagerService,
			final SurveyExecutionManagerService screeningSurveyExecutionManagerService) {
		setName("Monitoring Scheduling Worker");
		setPriority(NORM_PRIORITY - 1);

		systemLoad = SystemLoad.getInstance();

		this.screeningSurveyExecutionManagerService = screeningSurveyExecutionManagerService;
		this.interventionExecutionManagerService = interventionExecutionManagerService;
		statisticsEnabled = Constants.isStatisticsFileEnabled();
		if (statisticsEnabled) {
			statisticsFile = new File(Constants.getStatisticsFile());
		}
	}

	@Override
	public void run() {
		long nextLoadInfo = System.currentTimeMillis() + 30000;

		try {
			TimeUnit.MILLISECONDS.sleep(
					ImplementationConstants.MASTER_RULE_EVALUTION_WORKER_MILLISECONDS_SLEEP_BETWEEN_CHECK_CYCLES);
		} catch (final InterruptedException e) {
			interrupt();
			log.debug(
					"Monitoring scheduling worker received signal to stop (before first run)");
		}

		while (!isInterrupted() && !shouldStop) {
			// Update load info every 30 seconds
			if (nextLoadInfo < System.currentTimeMillis()) {
				nextLoadInfo = System.currentTimeMillis() + 30000;
				systemLoad.log();
			}

			final long startingTime = System.currentTimeMillis();
			log.debug(
					"Executing new run of monitoring scheduling worker...started");

			try {
				// Check cache clearing
				val dailyUniqueIndexForCache = StringHelpers
						.createDailyUniqueIndex();
				if (!lastCacheClearing.equals(dailyUniqueIndexForCache)) {
					log.debug("Clear cache");
					lastCacheClearing = dailyUniqueIndexForCache;

					interventionExecutionManagerService.clearCache();
				}

				// Check statistics creation
				if (statisticsEnabled) {
					try {
						// Statistics creation: Perform only once per day
						val dailyUniqueIndexForStatistics = StringHelpers
								.createDailyUniqueIndex();
						if (!lastStatisticsCreation
								.equals(dailyUniqueIndexForStatistics)) {
							log.debug("It's a new day so create statistics");
							lastStatisticsCreation = dailyUniqueIndexForStatistics;

							final long taskStartingTime = System
									.currentTimeMillis();

							interventionExecutionManagerService
									.createStatistics(statisticsFile);

							systemLoad.setStatisticsCreationRequiredMillis(
									System.currentTimeMillis()
											- taskStartingTime);
						}
					} catch (final Exception e) {
						log.error("Could not create statistics file: {}",
								e.getMessage());
					}
				}

				try {
					// Finish unfinished screening surveys: Check only every x
					// minutes
					if (System
							.currentTimeMillis() > lastScreeningSurveyFinishingCheck
									+ ImplementationConstants.FINISH_UNFINISHED_SCREENING_SURVEYS_INTERVAL_IN_SECONDS
											* 1000) {
						lastScreeningSurveyFinishingCheck = System
								.currentTimeMillis();
						log.debug("Finishing unfinished screening surveys");

						final long taskStartingTime = System
								.currentTimeMillis();

						screeningSurveyExecutionManagerService
								.finishUnfinishedScreeningSurveys();

						systemLoad
								.setFinishingUnfinishedScreeningSurveysRequiredMillis(
										System.currentTimeMillis()
												- taskStartingTime);
					}
				} catch (final Exception e) {
					log.error(
							"Could not finish unfinished screening surveys: {}",
							e.getMessage());
				}
				try {
					// Perform message: "Continuous" process
					final long taskStartingTime = System.currentTimeMillis();

					val count = interventionExecutionManagerService
							.performMessaging();

					systemLoad.setMessagingPerformedForParticipants(count);

					val duration = System.currentTimeMillis()
							- taskStartingTime;

					systemLoad.setPerformContinuousMessagingRequiredMillis(
							duration);

					systemLoad
							.setPerformContinuousMessagingRequiredMillisPerParticipant(
									(double) duration / count);
				} catch (final Exception e) {
					log.error("Could not perform messaging: {}",
							e.getMessage());
				}
			} catch (final Exception e) {
				log.error("Could not run whole scheduling process: {}",
						e.getMessage());
			}

			log.debug(
					"Executing new run of monitoring scheduling worker...done ({} milliseconds)",
					System.currentTimeMillis() - startingTime);

			try {
				TimeUnit.MILLISECONDS.sleep(
						ImplementationConstants.MASTER_RULE_EVALUTION_WORKER_MILLISECONDS_SLEEP_BETWEEN_CHECK_CYCLES);
			} catch (final InterruptedException e) {
				interrupt();
				log.debug(
						"Monitoring scheduling worker received signal to stop (interrupted)");
				return;
			}
		}
		log.debug("Monitoring scheduling worker received signal to stop");
	}
}