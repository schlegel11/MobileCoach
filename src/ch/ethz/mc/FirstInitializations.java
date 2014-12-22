package ch.ethz.mc;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health IS-Lab
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
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lombok.Cleanup;
import lombok.val;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;

/**
 * Sets logging folder as folder for Log4j2 log files and adjusts configuration
 * based on configuration file
 * 
 * @author Andreas Filler
 */
public class FirstInitializations implements ServletContextListener {
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		val loggingFolder = getLoggingFolder();
		val loggingConsoleLevel = getLoggingConsoleLevel();

		System.setProperty("mc_logging_folder", loggingFolder);
		System.setProperty("mc_logging_console_level", loggingConsoleLevel);
		val loggerContext = (LoggerContext) LogManager.getContext(false);
		loggerContext.reconfigure();

		val thread = Thread.currentThread();
		thread.setName(ImplementationConstants.LOGGING_APPLICATION_NAME);

		Constants
				.injectConfiguration(System
						.getProperty(ImplementationConstants.SYSTEM_CONFIGURATION_PROPERTY));
	}

	/**
	 * Reads the logging folder from configuration file before the configuration
	 * file is officially parsed for the system
	 * 
	 * @return
	 */
	private String getLoggingFolder() {
		val configurationsFileString = System
				.getProperty(ImplementationConstants.SYSTEM_CONFIGURATION_PROPERTY);

		if (configurationsFileString == null) {
			return Constants.getLoggingFolder();
		}

		val configurationFile = new File(configurationsFileString);

		if (!configurationFile.exists()) {
			return Constants.getLoggingFolder();
		}

		// Configuration file provided and exists
		val properties = new Properties();
		try {
			@Cleanup
			val fileInputStream = new FileInputStream(configurationFile);
			properties.load(fileInputStream);
		} catch (final Exception e) {
			return Constants.getLoggingFolder();
		}

		if (properties.getProperty("loggingFolder") != null
				&& !properties.getProperty("loggingFolder").equals("")) {
			return properties.getProperty("loggingFolder");
		}

		return Constants.getLoggingFolder();
	}

	/**
	 * Reads the logging level of the console from configuration file before the
	 * configuration
	 * file is officially parsed for the system
	 * 
	 * @return
	 */
	private String getLoggingConsoleLevel() {
		val configurationsFileString = System
				.getProperty(ImplementationConstants.SYSTEM_CONFIGURATION_PROPERTY);

		if (configurationsFileString == null) {
			return Constants.getLoggingConsoleLevel();
		}

		val configurationFile = new File(configurationsFileString);

		if (!configurationFile.exists()) {
			return Constants.getLoggingConsoleLevel();
		}

		// Configuration file provided and exists
		val properties = new Properties();
		try {
			@Cleanup
			val fileInputStream = new FileInputStream(configurationFile);
			properties.load(fileInputStream);
		} catch (final Exception e) {
			return Constants.getLoggingConsoleLevel();
		}

		if (properties.getProperty("loggingConsoleLevel") != null
				&& !properties.getProperty("loggingConsoleLevel").equals("")) {
			return properties.getProperty("loggingConsoleLevel");
		}

		return Constants.getLoggingConsoleLevel();
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		// nothing to do
	}
}
