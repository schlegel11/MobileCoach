package ch.ethz.mc.tools;

import java.util.List;
import java.util.regex.Pattern;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.ImplementationConstants;

/**
 * Validates strings for several purposes
 * 
 * @author Andreas Filler
 */
@Log4j2
public class StringValidator {
	/**
	 * Check if a {@link String} is a valid rule
	 * 
	 * @param rule
	 * @return
	 */
	public static boolean isValidRule(final String rule) {
		log.debug("Testing if '{}' is a valid rule", rule);

		// Check null or empty
		if (rule == null || rule.equals("")) {
			log.debug("Yes");
			return true;
		}

		// Check letters
		final String pattern = ImplementationConstants.REGULAR_EXPRESSION_TO_VALIDATE_CALCULATED_RULE;
		if (!Pattern.matches(pattern, rule)) {
			log.debug("No");
			return false;
		}

		log.debug("Yes");
		return true;
	}

	/**
	 * Check if a {@link String} is a valid variable name
	 * 
	 * @param name
	 *            The name to test
	 * @return
	 */
	public static boolean isValidVariableName(final String name) {
		log.debug("Testing if '{}' is a valid variable name", name);

		// Check null or empty
		if (name == null || name.equals("")) {
			log.debug("No");
			return false;
		}

		// Check letters
		final String pattern = ImplementationConstants.REGULAR_EXPRESSION_TO_VALIDATE_VARIABLE_NAME;
		if (!Pattern.matches(pattern, name)) {
			log.debug("No");
			return false;
		}

		log.debug("Yes");
		return true;
	}

	/**
	 * Check if the text contains unknown variables
	 * 
	 * @param textWithPlaceholders
	 * @param allPossibleMessageVariables
	 * @return
	 */
	public static boolean isValidVariableText(String textWithPlaceholders,
			final List<String> allPossibleMessageVariables) {
		log.debug("Testing if '{}' is a valid variable text",
				textWithPlaceholders);

		for (final val variable : allPossibleMessageVariables) {
			textWithPlaceholders = textWithPlaceholders.replace(variable, "");
		}

		if (textWithPlaceholders.contains("$")) {
			return false;
		} else {
			return true;
		}
	}
}