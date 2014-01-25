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
		Messages.setLocale(Constants.ADMIN_LOCALE);
		Messages.checkForMissingLocales();

		log.info("Starting up services...");
		try {
			// Internal services
			this.databaseManagerService = DatabaseManagerService.start();
			this.fileStorageManagerService = FileStorageManagerService
					.start(this.databaseManagerService);
			this.communicationManagerService = CommunicationManagerService
					.start();
			this.modelObjectExchangeService = ModelObjectExchangeService
					.start(this.databaseManagerService,
							this.fileStorageManagerService);

			// Controller services
			this.interventionAdministrationManagerService = InterventionAdministrationManagerService
					.start(this.databaseManagerService,
							this.fileStorageManagerService,
							this.modelObjectExchangeService);
			this.screeningSurveyAdministrationManagerService = ScreeningSurveyAdministrationManagerService
					.start(this.databaseManagerService,
							this.fileStorageManagerService,
							this.modelObjectExchangeService);
			this.interventionExecutionManagerService = InterventionExecutionManagerService
					.start(this.databaseManagerService,
							this.fileStorageManagerService);
			this.screeningSurveyExecutionManagerService = ScreeningSurveyExecutionManagerService
					.start(this.databaseManagerService,
							this.fileStorageManagerService);
		} catch (final Exception e) {
			noErrorsOccurred = false;
			log.error("Error at starting services: {}", e);
		}

		// Only set to true if all relevant services started probably
		if (noErrorsOccurred) {
			this.ready = true;

			log.info("Services started.");
		} else {
			log.error("Not all services could be started.");
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		log.info("Stopping services...");

		try {
			this.databaseManagerService.stop();
			this.fileStorageManagerService.stop();
			this.communicationManagerService.stop();
			this.modelObjectExchangeService.stop();
			this.interventionAdministrationManagerService.stop();
			this.screeningSurveyAdministrationManagerService.stop();
			this.interventionExecutionManagerService.stop();
			this.screeningSurveyExecutionManagerService.stop();
		} catch (final Exception e) {
			log.warn("Error at stopping services: {}", e);
		}

		log.info("Services stopped.");
	}
}
