package ch.ethz.mc;

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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.services.InterventionAdministrationManagerService;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.services.SurveyAdministrationManagerService;
import ch.ethz.mc.services.SurveyExecutionManagerService;
import ch.ethz.mc.services.internal.CommunicationManagerService;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.ExternalServicesManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.services.internal.ImageCachingService;
import ch.ethz.mc.services.internal.LockingService;
import ch.ethz.mc.services.internal.ModelObjectExchangeService;
import ch.ethz.mc.services.internal.ReportGeneratorService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.tools.InternalDateTime;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

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

	@Getter
	ImageCachingService							imageCachingService;

	VariablesManagerService						variablesManagerService;

	@Getter
	CommunicationManagerService					communicationManagerService;
	ModelObjectExchangeService					modelObjectExchangeService;

	@Getter
	ReportGeneratorService						reportGeneratorService;

	@Getter
	LockingService								lockingService;
	
	ExternalServicesManagerService				externalServicesManagerService;

	// Controller services
	@Getter
	InterventionAdministrationManagerService	interventionAdministrationManagerService;
	@Getter
	SurveyAdministrationManagerService			surveyAdministrationManagerService;
	@Getter
	InterventionExecutionManagerService			interventionExecutionManagerService;
	@Getter
	SurveyExecutionManagerService				surveyExecutionManagerService;
	@Getter
	RESTManagerService							restManagerService;

	@Override
	public void contextInitialized(final ServletContextEvent event) {
		boolean noErrorsOccurred = true;

		instance = this;

		log.info("Setting basic configuration...");
		Messages.setLocale(Constants.getAdminLocale());
		Messages.checkForMissingLocalizedStrings();

		log.info("Logging folder: {}", Constants.getLoggingFolder());
		log.info("Storage folder: {}", Constants.getStorageFolder());
		log.info("Media upload folder: {}", Constants.getMediaUploadFolder());
		log.info("Media cache folder: {}", Constants.getMediaCacheFolder());
		log.info("Templates folder: {}", Constants.getTemplatesFolder());

		log.info("Starting up services...");
		try {
			// Internal services
			databaseManagerService = DatabaseManagerService
					.start(Constants.DATA_MODEL_VERSION);
			fileStorageManagerService = FileStorageManagerService
					.start(databaseManagerService);

			databaseManagerService.ensureDatabaseConsistency();
			databaseManagerService.ensureSensefulParticipantTimings();

			imageCachingService = ImageCachingService
					.start(fileStorageManagerService.getMediaCacheFolder());
			variablesManagerService = VariablesManagerService
					.start(databaseManagerService);
			modelObjectExchangeService = ModelObjectExchangeService
					.start(databaseManagerService, fileStorageManagerService);
			reportGeneratorService = ReportGeneratorService
					.start(databaseManagerService);
			lockingService = LockingService.start();

			// Internal services which internally require started controller
			// services
			communicationManagerService = CommunicationManagerService
					.prepare(variablesManagerService);
			externalServicesManagerService = ExternalServicesManagerService
					.start(communicationManagerService
							.getDeepstreamCommunicationService());

			// Controller services
			surveyAdministrationManagerService = SurveyAdministrationManagerService
					.start(databaseManagerService, fileStorageManagerService,
							variablesManagerService,
							modelObjectExchangeService);
			interventionAdministrationManagerService = InterventionAdministrationManagerService
					.start(databaseManagerService, fileStorageManagerService,
							variablesManagerService, modelObjectExchangeService,
							surveyAdministrationManagerService, externalServicesManagerService);
			surveyExecutionManagerService = SurveyExecutionManagerService.start(
					databaseManagerService, fileStorageManagerService,
					variablesManagerService,
					interventionAdministrationManagerService);
			interventionExecutionManagerService = InterventionExecutionManagerService
					.start(databaseManagerService, variablesManagerService,
							communicationManagerService,
							interventionAdministrationManagerService,
							surveyExecutionManagerService);
			restManagerService = RESTManagerService.startThreadedService(
					databaseManagerService, fileStorageManagerService,
					variablesManagerService, communicationManagerService);

			// Start communication and working
			interventionExecutionManagerService
					.startCommunicationAndWorking(this);
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
		stopServices();
	}

	private void stopServices() {
		log.info("Stopping services...");

		ready = false;

		InternalDateTime.setFastForwardMode(false);

		try {
			lockingService.stop();
			reportGeneratorService.stop();
			restManagerService.stopThreadedService();
			surveyExecutionManagerService.stop();
			interventionExecutionManagerService.stop();
			surveyAdministrationManagerService.stop();
			interventionAdministrationManagerService.stop();
			modelObjectExchangeService.stop();
			communicationManagerService.stop();
			externalServicesManagerService.stop();
			variablesManagerService.stop();
			imageCachingService.stop();
			fileStorageManagerService.stop();
			databaseManagerService.stop();
		} catch (final Exception e) {
			log.warn("Error at stopping services: {}", e);
		}

		log.info("Services stopped.");
	}

	public void forceShutdown() {
		log.error("Shutdown forced...");

		stopServices();
	}
}
