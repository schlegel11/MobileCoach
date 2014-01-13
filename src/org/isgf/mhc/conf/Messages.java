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
	private static final String		BUNDLE_NAME		= "org.isgf.mhc.conf.messages"; //$NON-NLS-1$

	private static ResourceBundle	RESOURCE_BUNDLE	= null;

	/**
	 * Set {@link Messages} class to a specific locale or fallback to default
	 * (English)
	 */
	public static void setLocale(final Locale locale) {
		try {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, locale,
					new UTF8Control());
			Locale.setDefault(locale);
			log.debug("Set locale to {}", locale);
		} catch (final Exception e) {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME,
					new UTF8Control());
			Locale.setDefault(Locale.ENGLISH);
			log.debug("Set locale to {}", Locale.ENGLISH);
		}
	}

	/**
	 * Return {@link String} in currently set locale
	 * 
	 * @param key
	 * @return
	 */
	public static String getString(final String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (final MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
