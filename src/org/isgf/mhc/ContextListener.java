package org.isgf.mhc;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.services.DatabaseManagerService;

/**
 * @author Andreas Filler
 */
@Log4j2
public class ContextListener implements ServletContextListener {
	@Getter
	private static boolean	ready	= false;

	// Services
	@Getter
	DatabaseManagerService	databaseManagerService;

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		log.info("Stopping base services...");

		try {
			this.databaseManagerService.stop();
		} catch (final Exception e) {
			log.warn("Error at stopping {}: {}", this.databaseManagerService, e);
		}

		log.info("Base services stopped.");
	}

	@Override
	public void contextInitialized(final ServletContextEvent event) {
		boolean noErrorsOccured = true;

		log.info("Setting basic configuration...");
		Messages.setLocale(Constants.SYSTEM_LOCALE);

		log.info("Starting up base services...");
		try {
			this.databaseManagerService = DatabaseManagerService.start();
		} catch (final Exception e) {
			noErrorsOccured = false;
			log.error("Error at starting {}: {}", this.databaseManagerService,
					e);
		}

		// Only set to true if all relevant services started probably
		if (noErrorsOccured) {
			ContextListener.ready = true;

			log.info("Base services started.");
		} else {
			log.error("Not all base services could be started.");
		}
	}
}
