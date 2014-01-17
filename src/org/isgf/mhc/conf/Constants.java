package org.isgf.mhc.conf;

import java.util.Locale;

public class Constants {
	/**
	 * Debugging is only activated when PRODUCTION_MODE is false, but it should
	 * never be false on a live system!
	 */
	public static final boolean	PRODUCTION_MODE				= false;

	/**
	 * Basic debugging configuration
	 */
	public static final boolean	RUN_TESTS_AT_STARTUP		= true;

	/**
	 * Admin configuration
	 */
	public static final Locale	ADMIN_LOCALE				= Locale.ENGLISH;
	public static final String	STORAGE_FOLDER				= "/mhc_data/FileStorage";

	/**
	 * Screening survey configuration
	 */
	public static final Locale	SCREENING_SURVEY_LOCALE		= Locale.GERMAN;
	public static final String	SCREENING_SURVEY_TEMPLATE	= "ScreeningSurvey.template.html";

	/**
	 * Database configuration
	 */
	public static final String	DATABASE_HOST				= "127.0.0.1";
	public static final int		DATABASE_PORT				= 27017;
	public static final String	DATABASE_USER				= "mhc";
	public static final String	DATABASE_PASSWORD			= "mhc";
	public static final String	DATABASE_NAME				= "mhc";

	/**
	 * Session variables
	 */
	public static final String	SESSION_PARTICIPANT_ID		= "ParticipantId";
	public static final String	SESSION_SCREENING_SURVEY_ID	= "ScreeningSurveyId";

	/**
	 * Screening survey slide template variables
	 */
	public static final String	SSS_TEMPLATE_RESULT_VALUE	= "result";
	public static final String	SSS_TEMPLATE_STEP			= "step";
	public static final Object	SSS_TEMPLATE_STEPS_ERROR	= "error";
	public static final String	SSS_TEMPLATE_GLOBAL_MESSAGE	= "global_message";
}
