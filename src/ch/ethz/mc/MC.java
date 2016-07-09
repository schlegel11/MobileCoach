package ch.ethz.mc;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.services.InterventionAdministrationManagerService;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.ScreeningSurveyAdministrationManagerService;
import ch.ethz.mc.services.ScreeningSurveyExecutionManagerService;
import ch.ethz.mc.services.internal.CommunicationManagerService;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.services.internal.LockingService;
import ch.ethz.mc.services.internal.ModelObjectExchangeService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.tools.InternalDateTime;

/**
 * @author Andreas Filler
 */
@Log4j2
public class MC implements ServletContextListener {
	@Getter
	private static MC							instance;

	@Getter
	private boolean								ready	= false;

	// Internal services
	DatabaseManagerService						databaseManagerService;
	FileStorageManagerService					fileStorageManagerService;

	VariablesManagerService						variablesManagerService;

	CommunicationManagerService					communicationManagerService;
	ModelObjectExchangeService					modelObjectExchangeService;

	@Getter
	LockingService								lockingService;

	// Controller services
	@Getter
	InterventionAdministrationManagerService	interventionAdministrationManagerService;
	@Getter
	ScreeningSurveyAdministrationManagerService	screeningSurveyAdministrationManagerService;
	@Getter
	InterventionExecutionManagerService			interventionExecutionManagerService;
	@Getter
	ScreeningSurveyExecutionManagerService		screeningSurveyExecutionManagerService;

	@Override
	public void contextInitialized(final ServletContextEvent event) {
		boolean noErrorsOccurred = true;

		instance = this;

		log.info("Setting basic configuration...");
		Messages.setLocale(Constants.getAdminLocale());
		Messages.checkForMissingLocales();

		log.info("Logging folder:   {}", Constants.getLoggingFolder());
		log.info("Storage folder:   {}", Constants.getStorageFolder());
		log.info("Templates folder: {}", Constants.getTemplatesFolder());

		log.info("Starting up services...");
		try {
			// Internal services
			databaseManagerService = DatabaseManagerService
					.start(Constants.DATA_MODEL_VERSION);
			fileStorageManagerService = FileStorageManagerService
					.start(databaseManagerService);
			variablesManagerService = VariablesManagerService
					.start(databaseManagerService);
			communicationManagerService = CommunicationManagerService.start();
			modelObjectExchangeService = ModelObjectExchangeService.start(
					databaseManagerService, fileStorageManagerService);
			lockingService = LockingService.start();

			// Controller services
			screeningSurveyAdministrationManagerService = ScreeningSurveyAdministrationManagerService
					.start(databaseManagerService, fileStorageManagerService,
							variablesManagerService, modelObjectExchangeService);
			interventionAdministrationManagerService = InterventionAdministrationManagerService
					.start(databaseManagerService, fileStorageManagerService,
							variablesManagerService,
							modelObjectExchangeService,
							screeningSurveyAdministrationManagerService);
			screeningSurveyExecutionManagerService = ScreeningSurveyExecutionManagerService
					.start(databaseManagerService, fileStorageManagerService,
							variablesManagerService,
							interventionAdministrationManagerService);
			interventionExecutionManagerService = InterventionExecutionManagerService
					.start(databaseManagerService, variablesManagerService,
							communicationManagerService,
							interventionAdministrationManagerService,
							screeningSurveyExecutionManagerService);
		} catch (final Exception e) {
			noErrorsOccurred = false;
			log.error("Error at starting services: {}", e);
		}

		// Only set to true if all relevant services started probably
		if (noErrorsOccurred) {
			ready = true;

			log.info("Services started.");
		} else {
			log.error("Not all services could be started.");
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		log.info("Stopping services...");

		InternalDateTime.setFastForwardMode(false);

		try {
			lockingService.stop();
			screeningSurveyExecutionManagerService.stop();
			interventionExecutionManagerService.stop();
			screeningSurveyAdministrationManagerService.stop();
			interventionAdministrationManagerService.stop();
			modelObjectExchangeService.stop();
			communicationManagerService.stop();
			variablesManagerService.stop();
			fileStorageManagerService.stop();
			databaseManagerService.stop();
		} catch (final Exception e) {
			log.warn("Error at stopping services: {}", e);
		}

		log.info("Services stopped.");
	}
}
