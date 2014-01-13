package org.isgf.mhc.conf;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.tools.UTF8Control;

/**
 * Enables localization of the whole application
 * 
 * @author Andreas Filler
 */
@Log4j2
public class Messages {
	private static final String		BUNDLE_NAME							= "org.isgf.mhc.conf.messages"; //$NON-NLS-1$

	private static ResourceBundle	ADMIN_RESOURCE_BUNDLE				= null;
	private static ResourceBundle	SCREENING_SURVEY_RESOURCE_BUNDLE	= null;

	/**
	 * Set {@link Messages} class to a specific locales or fallback to default
	 * (English)
	 */
	public static void setLocales(final Locale adminLocale,
			final Locale screeningSurveyLocale) {
		// Loading admin messages
		try {
			ADMIN_RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME,
					adminLocale, new UTF8Control());
			log.debug("Set admin locale to {}", adminLocale);
		} catch (final Exception e) {
			ADMIN_RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME,
					new UTF8Control());
			log.debug("Set admin locale to {}", Locale.ENGLISH);
		}

		// Loading screening survey messages
		try {
			SCREENING_SURVEY_RESOURCE_BUNDLE = ResourceBundle.getBundle(
					BUNDLE_NAME, screeningSurveyLocale, new UTF8Control());
			log.debug("Set screening survey locale to {}", adminLocale);
		} catch (final Exception e) {
			SCREENING_SURVEY_RESOURCE_BUNDLE = ResourceBundle.getBundle(
					BUNDLE_NAME, new UTF8Control());
			log.debug("Set screening survey locale to {}", Locale.ENGLISH);
		}
	}

	/**
	 * Return {@link String} in currently set admin locale
	 * 
	 * @param key
	 * @return
	 */
	public static String getAdminString(final String key) {
		try {
			return ADMIN_RESOURCE_BUNDLE.getString(key);
		} catch (final MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Return {@link String} in currently set screening survey locale
	 * 
	 * @param key
	 * @return
	 */
	public static String getScreeningSurveyString(final String key) {
		try {
			return SCREENING_SURVEY_RESOURCE_BUNDLE.getString(key);
		} catch (final MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
