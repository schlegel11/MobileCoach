package org.isgf.mhc.conf;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.Properties;

import lombok.Cleanup;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Contains all relevant constants (which can be overwritten using a
 * configuration file provided as system parameter "mhc.configuration")
 * 
 * @author Andreas Filler
 */
@Log4j2
public class Constants {
	private static boolean		injectionPerformed				= false;

	/**
	 * Debugging is only activated when IS_LIVE_SYSTEM is false, but it should
	 * never be false on a live system!
	 * 
	 * CAUTION: Can NOT be defined in configuration file
	 */
	public static final boolean	IS_LIVE_SYSTEM					= true;

	/**
	 * Basic debugging configuration
	 * 
	 * CAUTION: Can NOT be defined in configuration file
	 */
	public static final boolean	RUN_TESTS_AT_STARTUP			= false;

	/**
	 * Basic configuration
	 */
	@Getter
	private static boolean		listOpenScreenSurveysOnBaseURL	= false;

	/**
	 * Admin configuration
	 */
	@Getter
	private static String		defaultAdminUsername			= "admin";
	@Getter
	private static String		defaultAdminPassword			= "admin";

	@Getter
	private static Locale		adminLocale						= Locale.ENGLISH;

	@Getter
	private static String		loggingFolder					= "/mhc_data/logs";
	@Getter
	private static String		storageFolder					= "/mhc_data/FileStorage";
	@Getter
	private static String		templatesFolder					= "/mhc_data/templates";

	/**
	 * Database configuration
	 */
	@Getter
	private static String		databaseHost					= "127.0.0.1";
	@Getter
	private static int			databasePort					= 27017;
	@Getter
	private static String		databaseUser					= "mhc";
	@Getter
	private static String		databasePassword				= "mhc";
	@Getter
	private static String		databaseName					= "mhc";

	/**
	 * Injects a specific configuration file (if provided as system parameter
	 * "mhc.configuration")
	 * 
	 * @param configurationsFile
	 */
	public static void injectConfiguration(final String configurationsFileString) {
		// Ensure that this method can only be called once
		if (!injectionPerformed) {
			injectionPerformed = true;

			if (configurationsFileString == null) {
				return;
			}

			val configurationFile = new File(configurationsFileString);

			if (!configurationFile.exists()) {
				log.error("Provided configuration file {} does not exist",
						configurationFile.getAbsoluteFile());
				return;
			}

			// Configuration file provided and exists
			log.info("Reading from configuration file {}",
					configurationFile.getAbsoluteFile());

			val properties = new Properties();
			try {
				@Cleanup
				val fileInputStream = new FileInputStream(configurationFile);
				properties.load(fileInputStream);
			} catch (final Exception e) {
				log.error("Error at parsing provided configuration file: {}",
						e.getMessage());
				return;
			}

			// Check all parameters in Constants if it's provided in the
			// configuration file to overwrite the standard value
			for (val field : Constants.class.getDeclaredFields()) {
				if (field != Constants.class.getDeclaredFields()[0]) {
					val fieldName = field.getName();
					val fieldType = field.getType();

					if (properties.containsKey(fieldName)) {
						log.debug("Overwriting '{}' with value '{}'",
								fieldName, properties.getProperty(fieldName));

						try {
							if (fieldType == String.class) {
								field.set(null,
										properties.getProperty(fieldName));
							} else if (fieldType == Boolean.TYPE) {
								field.set(null, Boolean.parseBoolean(properties
										.getProperty(fieldName)));
							} else if (fieldType == Integer.TYPE) {
								field.set(null, Integer.parseInt(properties
										.getProperty(fieldName)));
							} else if (fieldType == Locale.class) {
								field.set(
										null,
										new Locale(properties
												.getProperty(fieldName)));
							} else {
								log.error(
										"Field '{}' seems to be of an unsupported type {}!",
										fieldName, fieldType);
							}
						} catch (final Exception e) {
							log.error(
									"Error at overwriting constants value: {}",
									e.getMessage());
						}

					}
				}
			}
		}
	}
}
