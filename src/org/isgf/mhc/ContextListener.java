package org.isgf.mhc;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author Andreas Filler
 */
@Log4j2
public class ContextListener implements ServletContextListener {
	@Getter
	private static boolean	ready	= false;

	@Override
	public void contextDestroyed(final ServletContextEvent arg0) {
		log.info("Context destroyed.");
	}

	@Override
	public void contextInitialized(final ServletContextEvent arg0) {
		// TODO Context initialization

		ContextListener.ready = true;

		log.info("Context initialized.");
	}
}
