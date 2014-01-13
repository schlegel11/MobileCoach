package org.isgf.mhc.conf;

import java.util.Locale;

public class Constants {
	/**
	 * Debugging is only activated when PRODUCTION_MODE is false, but it should
	 * never be false on a live system!
	 */
	public static final boolean	PRODUCTION_MODE				= false;

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

}
