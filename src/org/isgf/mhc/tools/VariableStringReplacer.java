package org.isgf.mhc.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.persistent.concepts.AbstractVariableWithValue;

/**
 * Replaces variables in Strings with the according values
 * 
 * @author Andreas Filler
 */
@Log4j2
public class VariableStringReplacer {
	/**
	 * Finds variables within the given {@link String} and replaces them with
	 * the appropriate calculatable variable values
	 * 
	 * @param stringWithVariables
	 *            The {@link String} to search for variables
	 * @param variablesWithValues
	 *            The variables that can be used for the replacement process
	 * @param notFoundReplacer
	 *            The replacement {@link String} if a variable value could not
	 *            be found, or null if the variable should not be replaced if no
	 *            variable with the appropriate name could be found
	 * @return The String filled with variable values
	 */
	public static String findVariablesAndReplaceWithCalculatableValues(
			String stringWithVariables,
			final Collection<AbstractVariableWithValue> variablesWithValues,
			final String notFoundReplacer) {
		// Prevent null pointer exceptions
		if (stringWithVariables == null || stringWithVariables.equals("")) {
			log.debug("It's an empty string");
			return "";
		}

		// Find variables in rule
		final String variableFindPatternString = "\\$[a-zA-Z_]+";
		val variableFindPattern = Pattern.compile(variableFindPatternString);
		final Matcher variableFindMatcher = variableFindPattern
				.matcher(stringWithVariables);

		val variablesFoundInRule = new ArrayList<String>();
		while (variableFindMatcher.find()) {
			variablesFoundInRule.add(variableFindMatcher.group());
			log.debug("Found variable {} in string {}",
					variableFindMatcher.group(), stringWithVariables);
		}

		// Find variable values and put value into rule
		variableSearchLoop: for (final String variable : variablesFoundInRule) {
			for (val variableWithValue : variablesWithValues) {
				if (variable.equals(variableWithValue.getName())) {
					String value = variableWithValue.getValue();

					// Correct value
					if (value == null || value.equals("")) {
						value = "0";
					}

					// Replace variable with value in rule
					stringWithVariables = stringWithVariables.replace(variable,
							"(" + value + ")");
					log.debug("Replaced {} with {}", variable, value);
					continue variableSearchLoop;
				}
			}
			if (notFoundReplacer != null) {
				// Variable not found so replace with a specific value
				stringWithVariables = stringWithVariables.replace(variable,
						notFoundReplacer);
				log.debug("Replaced not found variable {} with {}", variable,
						notFoundReplacer);
			} else {
				log.debug("Do not replace not found variable {}", variable);
			}
		}

		return stringWithVariables;
	}

	/**
	 * Finds variables within the given {@link String} and replaces them with
	 * the appropriate text variable values
	 * 
	 * @param stringWithVariables
	 *            The {@link String} to search for variables
	 * @param variablesWithValues
	 *            The variables that can be used for the replacement process
	 * @param notFoundReplacer
	 *            The replacement {@link String} if a variable value could not
	 *            be found, or null if the variable should not be replaced if no
	 *            variable with the appropriate name could be found
	 * @return The String filled with variable values
	 */
	public static String findVariablesAndReplaceWithTextValues(
			String stringWithVariables,
			final Collection<AbstractVariableWithValue> variablesWithValues,
			final String notFoundReplacer) {
		// Prevent null pointer exceptions
		if (stringWithVariables == null || stringWithVariables.equals("")) {
			log.debug("It's an empty string");
			return "";
		}

		// Find variables in rule
		final String variableFindPatternString = ImplementationContants.REGULAR_EXPRESSION_TO_MATCH_VARIABLES_IN_STRING;
		val variableFindPattern = Pattern.compile(variableFindPatternString);
		final Matcher variableFindMatcher = variableFindPattern
				.matcher(stringWithVariables);

		val variablesFoundInRule = new ArrayList<String>();
		while (variableFindMatcher.find()) {
			variablesFoundInRule.add(variableFindMatcher.group());
			log.debug("Found variable {} in string {}",
					variableFindMatcher.group(), stringWithVariables);
		}

		// Find variable values and put value into rule
		variableSearchLoop: for (final String variable : variablesFoundInRule) {
			for (val variableWithValue : variablesWithValues) {
				if (variable.equals(variableWithValue.getName())) {
					String value = variableWithValue.getValue();

					// Correct value
					if (value == null) {
						value = "";
					}

					// Replace variable with value in rule
					stringWithVariables = stringWithVariables.replace(variable,
							value);
					log.debug("Replaced {} with {}", variable, value);
					continue variableSearchLoop;
				}
			}
			if (notFoundReplacer != null) {
				// Variable not found so replace with a specific value
				stringWithVariables = stringWithVariables.replace(variable,
						notFoundReplacer);
				log.debug("Replaced not found variable {} with {}", variable,
						notFoundReplacer);
			} else {
				log.debug("Do not replace not found variable {}", variable);
			}
		}

		return stringWithVariables;
	}
}
