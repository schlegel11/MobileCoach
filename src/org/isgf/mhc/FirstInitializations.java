package org.isgf.mhc;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lombok.val;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.isgf.mhc.conf.Constants;

/**
 * Sets logging folder as folder for Log4j2 log files and adjusts configuration
 * based on configuration file
 * 
 * @author Andreas Filler
 */
public class FirstInitializations implements ServletContextListener {
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		System.setProperty("mhc_logging_folder", Constants.getLoggingFolder());
		val loggerContext = (LoggerContext) LogManager.getContext(false);
		loggerContext.reconfigure();

		Constants.injectConfiguration(System.getProperty("mhc.configuration"));
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		// nothing to do
	}
}
