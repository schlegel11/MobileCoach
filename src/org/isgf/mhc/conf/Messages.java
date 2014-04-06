package org.isgf.mhc.conf;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.tools.UTF8Control;

/**
 * Enables localization of the whole application
 * 
 * @author Andreas Filler
 */
@Log4j2
public class Messages {
	private static final String		ADMIN_BUNDLE_NAME		= "org.isgf.mhc.conf.admin-messages";	//$NON-NLS-1$

	private static ResourceBundle	ADMIN_RESOURCE_BUNDLE	= null;

	/**
	 * Set {@link Messages} class to a specific locales or fallback to default
	 * (English)
	 */
	public static void setLocale(final Locale adminLocale) {
		// Loading admin messages
		try {
			ADMIN_RESOURCE_BUNDLE = ResourceBundle.getBundle(ADMIN_BUNDLE_NAME,
					adminLocale, new UTF8Control());
			log.debug("Set admin locale to {}", adminLocale);
		} catch (final Exception e) {
			ADMIN_RESOURCE_BUNDLE = ResourceBundle.getBundle(ADMIN_BUNDLE_NAME,
					new UTF8Control());
			log.debug("Set admin locale to {}", Locale.ENGLISH);
		}
	}

	/**
	 * Checks if all {@link AdminMessageStrings} are available
	 * 
	 * @throws Exception
	 */
	public static void checkForMissingLocales() {
		log.info("Checking for missing/obsolete localization strings in the selected language...");
		for (val field : AdminMessageStrings.values()) {
			try {
				ADMIN_RESOURCE_BUNDLE.getString(field.toString().toLowerCase()
						.replace("__", "."));

			} catch (final Exception e) {
				log.error("Admin message string {} is MISSING!", field
						.toString().toLowerCase().replace("__", "."));
			}
		}
		for (val field : ADMIN_RESOURCE_BUNDLE.keySet()) {
			try {
				AdminMessageStrings.valueOf(field.toUpperCase().replace(".",
						"__"));
			} catch (final Exception e) {
				log.error("Admin message string {} is OBSOLETE!", field);
			}
		}
		log.info("Check done.");
	}

	/**
	 * Return {@link String} in currently set admin locale, filled with given
	 * placeholders (if provided)
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public static String getAdminString(final AdminMessageStrings key,
			final Object... values) {
		try {
			if (values == null || values.length == 0) {
				return ADMIN_RESOURCE_BUNDLE.getString(key.toString()
						.toLowerCase().replace("__", "."));
			} else {
				return String.format(
						ADMIN_RESOURCE_BUNDLE.getString(key.toString()
								.toLowerCase().replace("__", ".")), values);
			}
		} catch (final MissingResourceException e) {
			return "! " + key.toString().toLowerCase().replace("__", ".")
					+ " !";
		}
	}
}
