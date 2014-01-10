package org.isgf.mhc.tools;

import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j2;

/**
 * Validates strings for several purposes
 * 
 * @author Andreas Filler
 */
@Log4j2
public class StringValidator {
	/**
	 * Check if a string is a valid variable name
	 * 
	 * @param name
	 *            The name to test
	 * @return
	 */
	public static boolean isValidVariableName(final String name) {
		log.debug("Testing if {} is a valid variable name", name);

		// Check null or empty
		if (name == null || name.equals("")) {
			return false;
		}

		// Check letters
		final String pattern = "^\\$[a-zA-Z_]*$";
		if (!Pattern.matches(pattern, name)) {
			return false;
		}

		return true;
	}
}
