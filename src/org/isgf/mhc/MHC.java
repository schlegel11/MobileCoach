package org.isgf.mhc;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.services.InterventionAdministrationManagerService;
import org.isgf.mhc.services.InterventionExecutionManagerService;
import org.isgf.mhc.services.ScreeningSurveyAdministrationManagerService;
import org.isgf.mhc.services.ScreeningSurveyExecutionManagerService;
import org.isgf.mhc.services.internal.CommunicationManagerService;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.ModelObjectExchangeService;
import org.isgf.mhc.services.internal.VariablesManagerService;
import org.isgf.mhc.tools.InternalDateTime;

/**
 * @author Andreas Filler
 */
@Log4j2
public class MHC implements ServletContextListener {
	@Getter
	private static MHC							instance;

	@Getter
	private boolean								ready	= false;

	// Internal services
	DatabaseManagerService						databaseManagerService;
	FileStorageManagerService					fileStorageManagerService;

	VariablesManagerService						variablesManagerService;

	CommunicationManagerService					communicationManagerService;
	ModelObjectExchangeService					modelObjectExchangeService;

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
			databaseManagerService = DatabaseManagerService.start();
			fileStorageManagerService = FileStorageManagerService
					.start(databaseManagerService);
			variablesManagerService = VariablesManagerService
					.start(databaseManagerService);
			communicationManagerService = CommunicationManagerService.start();
			modelObjectExchangeService = ModelObjectExchangeService.start(
					databaseManagerService, fileStorageManagerService);

			// Controller services
			interventionAdministrationManagerService = InterventionAdministrationManagerService
					.start(databaseManagerService, fileStorageManagerService,
							variablesManagerService, modelObjectExchangeService);
			screeningSurveyAdministrationManagerService = ScreeningSurveyAdministrationManagerService
					.start(databaseManagerService, fileStorageManagerService,
							variablesManagerService, modelObjectExchangeService);
			interventionExecutionManagerService = InterventionExecutionManagerService
					.start(databaseManagerService, variablesManagerService,
							communicationManagerService);
			screeningSurveyExecutionManagerService = ScreeningSurveyExecutionManagerService
					.start(databaseManagerService, fileStorageManagerService,
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
