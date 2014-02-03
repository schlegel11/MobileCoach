package org.isgf.mhc.conf;

import java.util.Locale;

/**
 * @author Andreas Filler
 */
public class Constants {
	/**
	 * Debugging is only activated when IS_LIVE_SYSTEM is false, but it should
	 * never be false on a live system!
	 */
	public static final boolean	IS_LIVE_SYSTEM							= false;

	/**
	 * Basic debugging configuration
	 */
	public static final boolean	RUN_TESTS_AT_STARTUP					= false;

	/**
	 * Basic configuration
	 */
	public static final boolean	LIST_OPEN_SCREENING_SURVEYS_ON_BASE_URL	= true;

	/**
	 * Admin configuration
	 */
	public static final String	DEFAULT_ADMIN_USERNAME					= "admin";
	public static final String	DEFAULT_ADMIN_PASSWORD					= "admin";

	public static final Locale	ADMIN_LOCALE							= Locale.ENGLISH;

	public static final String	STORAGE_FOLDER							= "/mhc_data/FileStorage";
	public static final String	TEMPLATES_FOLDER						= "/mhc_data/templates";

	/**
	 * Database configuration
	 */
	public static final String	DATABASE_HOST							= "127.0.0.1";
	public static final int		DATABASE_PORT							= 27017;
	public static final String	DATABASE_USER							= "mhc";
	public static final String	DATABASE_PASSWORD						= "mhc";
	public static final String	DATABASE_NAME							= "mhc";
}
