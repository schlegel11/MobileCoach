package ch.ethz.mc;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.services.SurveyAdministrationManagerService;
import ch.ethz.mc.services.SurveyExecutionManagerService;
import ch.ethz.mc.services.internal.CommunicationManagerService;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.services.internal.ImageCachingService;
import ch.ethz.mc.services.internal.LockingService;
import ch.ethz.mc.services.internal.ModelObjectExchangeService;
import ch.ethz.mc.services.internal.ReportGeneratorService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mobilecoach.services.FileConversationManagementService;
import ch.ethz.mobilecoach.services.MattermostManagementService;
import ch.ethz.mobilecoach.services.MattermostMessagingService;
import ch.ethz.mobilecoach.services.ResourceConversationManagementService;
import ch.ethz.mobilecoach.services.RichConversationService;

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

	CommunicationManagerService					communicationManagerService;
	ModelObjectExchangeService					modelObjectExchangeService;

	@Getter
	ReportGeneratorService						reportGeneratorService;

	@Getter
	LockingService								lockingService;

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

	@Getter
	MattermostMessagingService					mattermostMessagingService;
	@Getter
	MattermostManagementService					mattermostManagementService;
	@Getter
	RichConversationService						richConversationService;
	@Getter
	ResourceConversationManagementService		resourceConversationManagementService;
	@Getter
	FileConversationManagementService			fileConversationManagementService;

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
		log.info("XML Scripts folder: {}", Constants.getXmlScriptsFolder());

		log.info("Starting up services...");
		try {

			// Internal services
			databaseManagerService = DatabaseManagerService
					.start(Constants.DATA_MODEL_VERSION);
			fileStorageManagerService = FileStorageManagerService
					.start(databaseManagerService);
			imageCachingService = ImageCachingService
					.start(fileStorageManagerService.getMediaCacheFolder());
			variablesManagerService = VariablesManagerService
					.start(databaseManagerService);
			mattermostManagementService = MattermostManagementService
					.start(databaseManagerService);
			mattermostMessagingService = MattermostMessagingService
					.start(mattermostManagementService);
			// resourceConversationManagementService =
			// ResourceConversationManagementService.start(servletContext);
			fileConversationManagementService = FileConversationManagementService
					.start(Constants.getXmlScriptsFolder());
			richConversationService = RichConversationService.start(
					mattermostMessagingService,
					fileConversationManagementService);
			communicationManagerService = CommunicationManagerService
					.start(richConversationService);
			modelObjectExchangeService = ModelObjectExchangeService.start(
					databaseManagerService, fileStorageManagerService);
			reportGeneratorService = ReportGeneratorService
					.start(databaseManagerService);
			lockingService = LockingService.start();

			// Controller services
			surveyAdministrationManagerService = SurveyAdministrationManagerService
					.start(databaseManagerService, fileStorageManagerService,
							variablesManagerService, modelObjectExchangeService);
			interventionAdministrationManagerService = InterventionAdministrationManagerService
					.start(databaseManagerService, fileStorageManagerService,
							variablesManagerService,
							modelObjectExchangeService,
							surveyAdministrationManagerService);
			surveyExecutionManagerService = SurveyExecutionManagerService
					.start(databaseManagerService, fileStorageManagerService,
							variablesManagerService,
							interventionAdministrationManagerService);
			interventionExecutionManagerService = InterventionExecutionManagerService
					.start(databaseManagerService, variablesManagerService,
							communicationManagerService,
							interventionAdministrationManagerService,
							surveyExecutionManagerService);
			restManagerService = RESTManagerService.start(
					databaseManagerService, fileStorageManagerService,
					variablesManagerService);

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
			reportGeneratorService.stop();
			restManagerService.stop();
			surveyExecutionManagerService.stop();
			interventionExecutionManagerService.stop();
			surveyAdministrationManagerService.stop();
			interventionAdministrationManagerService.stop();
			modelObjectExchangeService.stop();
			communicationManagerService.stop();
			variablesManagerService.stop();
			imageCachingService.stop();
			fileStorageManagerService.stop();
			databaseManagerService.stop();
		} catch (final Exception e) {
			log.warn("Error at stopping services: {}", e);
		}

		log.info("Services stopped.");
	}
}
