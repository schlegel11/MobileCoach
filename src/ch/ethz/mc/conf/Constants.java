package ch.ethz.mc.conf;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import lombok.Cleanup;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.tools.StringHelpers;

/**
 * Contains all relevant constants (which can be overwritten using a
 * configuration file provided as system parameter "mc.configuration")
 * 
 * @author Andreas Filler
 */
@Log4j2
public class Constants {
	private static boolean		injectionPerformed					= false;

	/**
	 * Debugging is only activated when IS_LIVE_SYSTEM is false, but it should
	 * never be false on a live system!
	 * 
	 * CAUTION: Can NOT be defined in configuration file
	 */
	public static final boolean	IS_LIVE_SYSTEM						= true;

	/**
	 * Basic debugging configuration
	 * 
	 * CAUTION: Can NOT be defined in configuration file
	 */
	public static final boolean	RUN_TESTS_AT_STARTUP				= false;

	/**
	 * Data model version
	 * 
	 * CAUTION: Can NOT be defined in configuration file
	 */
	public static final int		DATA_MODEL_VERSION					= 2;
	/**
	 * Data model configuration collection
	 * 
	 * CAUTION: Can NOT be defined in configuration file
	 */
	public static final String	DATA_MODEL_CONFIGURATION			= "DataModelConfiguration";

	/**
	 * Basic configuration
	 */
	@Getter
	private static boolean		listOpenScreenSurveysOnBaseURL		= false;

	@Getter
	private static String		mediaObjectLinkingBaseURL			= "https://f.mobile-coach.eu/";
	@Getter
	private static String		feedbackLinkingBaseURL				= "https://fb.mobile-coach.eu/";

	private static String		acceptedStopWordsSeparatedByComma	= "stop,stopp";

	public static String[] getAcceptedStopWords() {
		return acceptedStopWordsSeparatedByComma.trim().toLowerCase()
				.split(",");
	}

	/**
	 * Admin configuration
	 */
	@Getter
	private static String	defaultAdminUsername				= "admin";
	@Getter
	private static String	defaultAdminPassword				= "admin";

	/**
	 * CAUTION: Do NEVER activate this on public servers! It's only for
	 * development.
	 */
	@Getter
	private static boolean	automaticallyLoginAsDefaultAdmin	= false;

	@Getter
	private static Locale	adminLocale							= Locale.ENGLISH;

	@Getter
	private static String	loggingFolder						= "/mc_data/logs";
	@Getter
	private static String	storageFolder						= "/mc_data/FileStorage";
	@Getter
	private static String	templatesFolder						= "/mc_data/templates";

	@Getter
	private static String	loggingConsoleLevel					= "DEBUG";

	@Getter
	private static String	fileExtension						= ".mc";

	/**
	 * Statistics configuration
	 */
	@Getter
	private static boolean	statisticsFileEnabled				= false;

	@Getter
	private static String	statisticsFile						= "/mc_data/statistics.properties";

	/**
	 * Simulation configuration
	 */
	/**
	 * CAUTION: Simulated date and time should only be used on SEPARATE
	 * installations, with an OWN database to test ALREADY FINALIZED
	 * interventions! Simulated interventions should never be transfered back to
	 * another system.
	 */
	@Getter
	private static boolean	simulatedDateAndTime				= false;

	private static String	smsSimulationNumber					= "+99999";

	public static String getSmsSimulationNumber() {
		return StringHelpers.cleanPhoneNumber(smsSimulationNumber);
	}

	/**
	 * Database configuration
	 */
	@Getter
	private static String	databaseHost			= "127.0.0.1";
	@Getter
	private static int		databasePort			= 27017;
	@Getter
	private static String	databaseUser			= "mc";
	@Getter
	private static String	databasePassword		= "mc";
	@Getter
	private static String	databaseName			= "mc";

	/**
	 * Mailing configuration
	 */
	@Getter
	private static String	mailhostIncoming		= "localhost";
	@Getter
	private static String	mailboxProtocol			= "pop3";
	@Getter
	private static String	mailboxFolder			= "INBOX";
	@Getter
	private static String	mailhostOutgoing		= "localhost";
	@Getter
	private static String	mailUser				= "---";
	@Getter
	private static String	mailPassword			= "---";
	@Getter
	private static String	mailSubjectStartsWith	= "SMS received on";

	/**
	 * SMS configuration
	 */
	@Getter
	private static String	smsEmailFrom			= "a@b.eu";
	@Getter
	private static String	smsEmailTo				= "c@d.eu";
	@Getter
	private static String	smsUserKey				= "abc";
	@Getter
	private static String	smsUserPassword			= "xyz";
	private static String	smsPhoneNumberFrom		= "+4567890";

	public static List<String> getSmsPhoneNumberFrom() {
		final List<String> smsPhoneNumbers = new ArrayList<String>();

		val smsPhoneNumbersArray = smsPhoneNumberFrom.split(",");

		for (final String smsPhoneNumber : smsPhoneNumbersArray) {
			smsPhoneNumbers.add(StringHelpers.cleanPhoneNumber(smsPhoneNumber));
		}

		return smsPhoneNumbers;
	}

	/**
	 * Injects a specific configuration file (if provided as system parameter
	 * "[CONTEXT PATH to lower case].configuration")
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
