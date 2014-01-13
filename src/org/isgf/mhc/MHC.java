package org.isgf.mhc;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.services.DatabaseManagerService;
import org.isgf.mhc.services.FileStorageManagerService;

/**
 * @author Andreas Filler
 */
@Log4j2
public class MHC implements ServletContextListener {
	@Getter
	private static MHC			instance;

	@Getter
	private boolean				ready	= false;

	// Services
	@Getter
	DatabaseManagerService		databaseManagerService;

	@Getter
	FileStorageManagerService	fileStorageManagerService;

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		log.info("Stopping base services...");

		try {
			this.databaseManagerService.stop();
			this.fileStorageManagerService.stop();
		} catch (final Exception e) {
			log.warn("Error at stopping {}: {}", this.databaseManagerService, e);
		}

		log.info("Base services stopped.");
	}

	@Override
	public void contextInitialized(final ServletContextEvent event) {
		boolean noErrorsOccurred = true;

		instance = this;

		log.info("Setting basic configuration...");
		Messages.setLocales(Constants.ADMIN_LOCALE,
				Constants.SCREENING_SURVEY_LOCALE);

		log.info("Starting up base services...");
		try {
			this.databaseManagerService = DatabaseManagerService.start();
			this.fileStorageManagerService = FileStorageManagerService.start();
		} catch (final Exception e) {
			noErrorsOccurred = false;
			log.error("Error at starting {}: {}", this.databaseManagerService,
					e);
		}

		// Only set to true if all relevant services started probably
		if (noErrorsOccurred) {
			this.ready = true;

			log.info("Base services started.");
		} else {
			log.error("Not all base services could be started.");
		}
	}
}
