package ch.ethz.mc.conf;

/* ##LICENSE## */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.RandomStringUtils;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.model.persistent.types.SMSServiceType;
import ch.ethz.mc.tools.StringHelpers;

/**
 * Contains all relevant constants (which can be overwritten using a
 * configuration file provided as system parameter "mc.configuration")
 * 
 * @author Andreas Filler
 */
@Log4j2
public class Constants {
	/**
	 * Remembers if the configuration settings have already been injected
	 */
	private static boolean		injectionPerformed					= false;

	/**
	 * Vaadin production mode configuration for debugging purposes. Should NEVER
	 * be false on live systems.
	 * 
	 * CAUTION: Can NOT be defined in configuration file
	 */
	// TODO RELEASE Switch always to "true" before release
	public static final boolean	VAADIN_PRODUCTION_MODE				= true;

	/**
	 * Basic debugging configuration
	 * 
	 * CAUTION: Can NOT be defined in configuration file
	 */
	// TODO RELEASE Switch always to "false" before release
	public static final boolean	RUN_TESTS_AT_STARTUP				= false;

	/**
	 * Data model version
	 * 
	 * CAUTION: Can NOT be defined in configuration file
	 */
	public static final int		DATA_MODEL_VERSION					= 48;
	/**
	 * Data model configuration collection
	 * 
	 * CAUTION: Can NOT be defined in configuration file
	 */
	public static final String	DATA_MODEL_CONFIGURATION			= "DataModelConfiguration";

	/**
	 * Basic configuration
	 */
	/**
	 * CAUTION: Caching can help during development but should NEVER be false on
	 * public servers! It's only for development.
	 */
	@Getter
	public static boolean		cachingActive						= true;

	@Getter
	private static boolean		listOpenScreenSurveysOnBaseURL		= false;

	@Getter
	private static String		mediaObjectLinkingBaseURL			= "https://f.mobile-coach.eu/";
	@Getter
	private static String		surveyLinkingBaseURL				= "https://w.mobile-coach.eu/";

	private static String		acceptedStopWordsSeparatedByComma	= "stop,stopp";

	public static String[] getAcceptedStopWords() {
		return acceptedStopWordsSeparatedByComma.trim().toLowerCase()
				.split(",");
	}

	/**
	 * Admin configuration
	 */
	@Getter
	private static String			defaultAdminUsername					= "admin";
	@Getter
	private static String			defaultAdminPassword					= "admin";

	/**
	 * CAUTION: Do NEVER activate this on public servers! It's only for
	 * development.
	 */
	@Getter
	private static boolean			automaticallyLoginAsDefaultAdmin		= false;

	@Getter
	private static Locale			adminLocale								= new Locale(
			"en", "GB");

	@Getter
	private static Locale[]			interventionLocales						= new Locale[] {
			new Locale("de", "CH"), new Locale("fr", "CH") };

	@Getter
	private static String			loggingFolder							= "/mc_data/logs";
	@Getter
	private static String			storageFolder							= "/mc_data/FileStorage";
	@Getter
	private static String			mediaUploadFolder						= "/mc_data/MediaUpload";
	@Getter
	private static String			mediaCacheFolder						= "/mc_data/MediaCache";
	@Getter
	private static String			templatesFolder							= "/mc_data/templates";

	@Getter
	private static String			loggingConsoleLevel						= "DEBUG";
	@Getter
	private static String			loggingRollingFileLevel					= "DEBUG";

	@Getter
	private static String			fileExtension							= ".mc";

	// -1 can be used to set it to infinite, 0 disables the history completely
	@Getter
	private static int				maxVariableHistory						= 1000;

	@Getter
	private static boolean			mediaUploadSecurityCheck				= true;

	/**
	 * Survey listing configuration
	 */
	@Getter
	private static String			surveyListingTitle						= "Active surveys:";
	@Getter
	private static String			surveyListingNoneActive					= "No survey active.";
	@Getter
	private static String			surveyListingNotActive					= "Survey listing inactive.";

	/**
	 * Statistics configuration
	 */
	@Getter
	private static boolean			statisticsFileEnabled					= false;

	@Getter
	private static String			statisticsFile							= "/mc_data/statistics.properties";

	/**
	 * Simulation configuration
	 */
	/**
	 * CAUTION: Simulated date and time should only be used on SEPARATE
	 * installations, with an OWN database to test ALREADY FINALIZED
	 * interventions! They also create intensively higher system load. Simulated
	 * interventions should never be transfered back to another system.
	 */
	@Getter
	private static boolean			simulatedDateAndTime					= false;

	/**
	 * Database configuration
	 */
	@Getter
	private static String			databaseHost							= "127.0.0.1";
	@Getter
	private static int				databasePort							= 27017;
	@Getter
	private static String			databaseUser							= "mc";
	@Getter
	private static String			databasePassword						= "mc";
	@Getter
	private static String			databaseName							= "mc";

	/**
	 * General Mailing configuration
	 */
	@Getter
	private static String			mailhostIncoming						= "localhost";
	@Getter
	private static String			mailboxProtocol							= "pop3";
	@Getter
	private static String			mailboxFolder							= "INBOX";
	@Getter
	private static String			mailhostOutgoing						= "localhost";
	@Getter
	private static String			mailUser								= "---";
	@Getter
	private static String			mailPassword							= "---";

	/**
	 * Email configuration
	 */
	@Getter
	private static boolean			emailActive								= true;
	@Getter
	private static String			emailFrom								= "a@b.eu";
	@Getter
	private static String			emailSubjectForParticipant				= "MobileCoach Message";
	@Getter
	private static String			emailSubjectForSupervisor				= "MobileCoach Supervisor Notification";
	@Getter
	private static String			emailSubjectForTeamManager				= "MobileCoach Team Manager Notification";
	@Getter
	private static String			emailTemplateForTeamManager				= "Hi Team Manager of $participantName!\n\nYou have one or more new messages from your participant, which can be read on the MobileCoach dashboard.";

	/**
	 * SMS configuration
	 */
	@Getter
	private static boolean			smsActive								= true;
	@Getter
	private static SMSServiceType	smsServiceType							= SMSServiceType.ASPSMS;
	@Getter
	private static String			smsMailSubjectStartsWith				= "SMS received on";
	@Getter
	private static String			smsUserKey								= "abc";
	@Getter
	private static String			smsUserPassword							= "xyz";
	private static String			smsPhoneNumberAcceptedCountryCodes		= "41,43,49";

	/**
	 * Deepstream configuration
	 */
	@Getter
	private static boolean			deepstreamActive						= false;
	@Getter
	private static String			deepstreamHost							= "wss://localhost:6020";
	@Getter
	private static int				deepstreamMinClientVersion				= 1;
	@Getter
	private static int				deepstreamMaxClientVersion				= 1;

	@Getter
	private static final String		deepstreamServerPassword				= RandomStringUtils
			.randomAlphanumeric(64);

	/**
	 * Push notifications configuration
	 */
	@Getter
	private static boolean			pushNotificationsActive					= false;
	@Getter
	private static boolean			pushNotificationsIOSActive				= false;
	@Getter
	private static boolean			pushNotificationsIOSEncrypted			= false;
	@Getter
	private static boolean			pushNotificationsAndroidActive			= false;
	@Getter
	private static boolean			pushNotificationsAndroidEncrypted		= false;
	@Getter
	private static boolean			pushNotificationsProductionMode			= false;
	@Getter
	private static String			pushNotificationsIOSAppIdentifier		= "com.mycompany.myapp";
	@Getter
	private static String			pushNotificationsIOSCertificateFile		= "/mc_data/certs/push-http2.p12";
	@Getter
	private static String			pushNotificationsIOSCertificatePassword	= "abc123topsecret";
	@Getter
	private static String			pushNotificationsAndroidAuthKey			= "1234380438403840384068";

	/**
	 * Get all accepted country codes for SMS phone numbers
	 * 
	 * @return List of accepted country codes
	 */
	public static String[] getSmsPhoneNumberAcceptedCountryCodes() {
		return smsPhoneNumberAcceptedCountryCodes.split(",");
	}

	@Getter
	private static String	smsPhoneNumberCountryCorrection	= "41";
	private static String	smsPhoneNumberFrom				= "+4567890";

	/**
	 * Get all configured recipient SMS phone numbers
	 * 
	 * @return List of SMS phone numbers
	 */
	public static List<String> getSmsPhoneNumberFrom() {
		final List<String> smsPhoneNumbers = new ArrayList<String>();

		val smsPhoneNumbersArray = smsPhoneNumberFrom.split(",");

		for (final String smsPhoneNumber : smsPhoneNumbersArray) {
			smsPhoneNumbers.add(StringHelpers.cleanPhoneNumber(smsPhoneNumber));
		}

		return smsPhoneNumbers;
	}

	// Current version information
	private static String			version			= null;
	private static ServletContext	servletContext	= null;

	/**
	 * Enables the application to retrieve the current version information
	 * 
	 * @return Current version
	 */
	@Synchronized
	public static String getVersion() {
		try {
			if (version == null) {
				@Cleanup
				val inputStream = servletContext
						.getResourceAsStream("/WEB-INF/version.txt");
				@Cleanup
				val inputStreamReader = new InputStreamReader(inputStream);
				@Cleanup
				val bufferedInputStreamReader = new BufferedReader(
						inputStreamReader);

				version = bufferedInputStreamReader.readLine() + " - DM: "
						+ DATA_MODEL_VERSION;
			}
		} catch (final Exception e) {
			log.error("Error at parsing version file: {}", e.getMessage());
			return "---";
		}
		return version;
	}

	/**
	 * Injects a specific configuration file (if provided as system parameter
	 * "[CONTEXT PATH to lower case].configuration")
	 * 
	 * @param servletContext
	 * 
	 * @param configurationsFile
	 */
	public static void injectConfiguration(
			final String configurationsFileString,
			final ServletContext servletContext) {
		// Ensure that this method can only be called once
		if (!injectionPerformed) {
			injectionPerformed = true;

			Constants.servletContext = servletContext;

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
						log.debug("Overwriting '{}' with value '{}'", fieldName,
								properties.getProperty(fieldName));

						try {
							if (fieldType == String.class) {
								field.set(null,
										properties.getProperty(fieldName));
							} else if (fieldType == Boolean.TYPE) {
								field.set(null, Boolean.parseBoolean(
										properties.getProperty(fieldName)));
							} else if (fieldType == Integer.TYPE) {
								field.set(null, Integer.parseInt(
										properties.getProperty(fieldName)));
							} else if (fieldType == Locale.class) {
								val localeProperty = properties
										.getProperty(fieldName)
										.replace(" ", "");
								if (localeProperty.contains("-")) {
									val localePropertyParts = localeProperty
											.split("-");
									field.set(null,
											new Locale(localePropertyParts[0],
													localePropertyParts[1]));
								} else if (localeProperty.contains("_")) {
									val localePropertyParts = localeProperty
											.split("_");
									field.set(null,
											new Locale(localePropertyParts[0],
													localePropertyParts[1]));
								} else {
									field.set(null, new Locale(localeProperty));
								}
							} else if (fieldType.isArray() && fieldType
									.getComponentType() == Locale.class) {
								val localeList = new ArrayList<Locale>();
								val localeProperties = properties
										.getProperty(fieldName);
								val localePropertiesParts = localeProperties
										.split(",");
								for (String localeProperty : localePropertiesParts) {
									if (localeProperty.contains("-")) {
										val localePropertyParts = localeProperty
												.split("-");
										localeList.add(new Locale(
												localePropertyParts[0],
												localePropertyParts[1]));
									} else if (localeProperty.contains("_")) {
										val localePropertyParts = localeProperty
												.split("_");

										localeList.add(new Locale(
												localePropertyParts[0],
												localePropertyParts[1]));
									} else {
										localeList.add(
												new Locale(localeProperty));
									}
								}
								field.set(null,
										localeList.toArray(new Locale[] {}));
							} else if (fieldType == SMSServiceType.class) {
								field.set(null, SMSServiceType.valueOf(
										properties.getProperty(fieldName)));
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
