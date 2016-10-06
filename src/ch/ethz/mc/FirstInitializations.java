package ch.ethz.mc;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab at the Health-IS Lab
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
		val contextPath = event.getServletContext().getContextPath()
				.toLowerCase().replaceFirst("^/", "");

		String configurationsFileString = null;

		// Tries to get specific configuration based on virtual server
		// configuration
		try {
			val serverNameElements = event.getServletContext()
					.getVirtualServerName().split("/");
			
			configurationsFileString = System
					.getProperty(serverNameElements[serverNameElements.length-1].toLowerCase()
							+ "."
							+ contextPath
							+ ImplementationConstants.SYSTEM_CONFIGURATION_PROPERTY_POSTFIX);
		} catch (final Exception e) {
			System.err
					.println("Error at getting virtual server based configuration file: "
							+ e.getMessage());
		}

		if (configurationsFileString == null
				|| !new File(configurationsFileString).exists()) {
			configurationsFileString = System
					.getProperty(contextPath
							+ ImplementationConstants.SYSTEM_CONFIGURATION_PROPERTY_POSTFIX);
		}

		val loggingFolder = getLoggingFolder(configurationsFileString);
		val loggingConsoleLevel = getLoggingConsoleLevel(configurationsFileString);
		val loggingRollingFileLevel = getLoggingRollingFileLevel(configurationsFileString);

		System.setProperty("mc_logging_folder", loggingFolder);
		System.setProperty("mc_logging_console_level", loggingConsoleLevel);
		System.setProperty("mc_logging_rolling_file_level",
				loggingRollingFileLevel);
		val loggerContext = (LoggerContext) LogManager.getContext(false);
		loggerContext.reconfigure();

		val thread = Thread.currentThread();
		thread.setName(ImplementationConstants.LOGGING_APPLICATION_NAME);

		Constants.injectConfiguration(configurationsFileString,
				event.getServletContext());
	}

	/**
	 * Reads the logging folder from configuration file before the configuration
	 * file is officially parsed for the system
	 *
	 * @param configurationsFileString
	 *            String containing the complete path to the configuration file
	 * @return
	 */
	private String getLoggingFolder(final String configurationsFileString) {
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
	 * configuration file is officially parsed for the system
	 *
	 * @param configurationsFileString
	 *            String containing the complete path to the configuration file
	 * @return
	 */
	private String getLoggingConsoleLevel(final String configurationsFileString) {
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

	/**
	 * Reads the logging level of the rolling file from configuration file
	 * before the
	 * configuration file is officially parsed for the system
	 *
	 * @param configurationsFileString
	 *            String containing the complete path to the configuration file
	 * @return
	 */

	private String getLoggingRollingFileLevel(
			final String configurationsFileString) {
		if (configurationsFileString == null) {
			return Constants.getLoggingRollingFileLevel();
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

		if (properties.getProperty("loggingRollingFileLevel") != null
				&& !properties.getProperty("loggingRollingFileLevel")
						.equals("")) {
			return properties.getProperty("loggingRollingFileLevel");
		}

		return Constants.getLoggingRollingFileLevel();
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		// nothing to do
	}
}
