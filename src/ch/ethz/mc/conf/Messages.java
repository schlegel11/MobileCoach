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
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.tools.UTF8Control;

/**
 * Enables localization of the whole application
 *
 * @author Andreas Filler
 */
@Log4j2
public class Messages {
	private static final String		ADMIN_BUNDLE_NAME		= "ch.ethz.mc.conf.admin-messages"; //$NON-NLS-1$

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
			Locale.setDefault(adminLocale);
		} catch (final Exception e) {
			ADMIN_RESOURCE_BUNDLE = ResourceBundle.getBundle(ADMIN_BUNDLE_NAME,
					new UTF8Control());
			val fallbackLocale = new Locale("en", "GB");
			log.warn(
					"Given admin locale {} cannot be set. Set admin locale to {}",
					adminLocale, fallbackLocale);
			Locale.setDefault(fallbackLocale);
		}
	}

	/**
	 * Checks if all {@link AdminMessageStrings} are available
	 *
	 * @throws Exception
	 */
	public static void checkForMissingLocalizedStrings() {
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
