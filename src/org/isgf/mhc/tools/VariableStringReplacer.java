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

		// Adjust variables in rule to be unique for later replacement
		val variablePreFindPattern = Pattern
				.compile(ImplementationContants.REGULAR_EXPRESSION_TO_MATCH_VARIABLES_IN_STRING);
		Matcher variablePreFindMatcher = variablePreFindPattern
				.matcher(stringWithVariables);

		while (variablePreFindMatcher.find()) {
			stringWithVariables = stringWithVariables.substring(0,
					variablePreFindMatcher.start())
					+ ImplementationContants.VARIABLE_MATCH_MODIFIER
					+ variablePreFindMatcher.group().substring(1,
							variablePreFindMatcher.group().length())
					+ ImplementationContants.VARIABLE_MATCH_MODIFIER
					+ stringWithVariables.substring(variablePreFindMatcher
							.end());

			variablePreFindMatcher = variablePreFindPattern
					.matcher(stringWithVariables);
		}

		// Find variables in rule
		val variableFindPattern = Pattern
				.compile(ImplementationContants.REGULAR_EXPRESSION_TO_MATCH_MODIFIED_VARIABLES_IN_STRING);
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
				if (variable
						.equals(ImplementationContants.VARIABLE_MATCH_MODIFIER
								+ variableWithValue.getName().substring(1,
										variableWithValue.getName().length())
								+ ImplementationContants.VARIABLE_MATCH_MODIFIER)) {
					String value = variableWithValue.getValue();

					// Correct value
					if (value == null || value.equals("")) {
						value = "0";
					}

					// Replace variable with value in rule
					if (value.contains(",")) {
						stringWithVariables = stringWithVariables.replace(
								variable, value);
					} else {
						stringWithVariables = stringWithVariables.replace(
								variable, "(" + value + ")");
					}
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

		// Adjust variables in rule to be unique for later replacement
		val variablePreFindPattern = Pattern
				.compile(ImplementationContants.REGULAR_EXPRESSION_TO_MATCH_VARIABLES_IN_STRING);
		Matcher variablePreFindMatcher = variablePreFindPattern
				.matcher(stringWithVariables);

		while (variablePreFindMatcher.find()) {
			stringWithVariables = stringWithVariables.substring(0,
					variablePreFindMatcher.start())
					+ ImplementationContants.VARIABLE_MATCH_MODIFIER
					+ variablePreFindMatcher.group().substring(1,
							variablePreFindMatcher.group().length())
					+ ImplementationContants.VARIABLE_MATCH_MODIFIER
					+ stringWithVariables.substring(variablePreFindMatcher
							.end());

			variablePreFindMatcher = variablePreFindPattern
					.matcher(stringWithVariables);
		}

		// Find variables in rule
		val variableFindPattern = Pattern
				.compile(ImplementationContants.REGULAR_EXPRESSION_TO_MATCH_MODIFIED_VARIABLES_IN_STRING);
		final Matcher variableFindMatcher = variableFindPattern
				.matcher(stringWithVariables);

		val variablesFoundInRule = new ArrayList<String>();
		val variablesFoundInRuleModifiers = new ArrayList<String>();
		while (variableFindMatcher.find()) {
			variablesFoundInRule.add(variableFindMatcher.group());

			// Check for modifiers
			val variableModifierFindPattern = Pattern
					.compile(ImplementationContants.REGULAR_EXPRESSION_TO_MATCH_VALUE_MODIFIER);
			final Matcher variableModifierFindMatcher = variableModifierFindPattern
					.matcher(stringWithVariables.substring(variableFindMatcher
							.end()));

			if (variableModifierFindMatcher.find()
					&& variableModifierFindMatcher.start() == 0) {
				variablesFoundInRuleModifiers
						.add(variableModifierFindMatcher.group()
								.substring(
										1,
										variableModifierFindMatcher.group()
												.length() - 1));
			} else {
				variablesFoundInRuleModifiers.add(null);
			}

			log.debug("Found variable {} in string {}",
					variableFindMatcher.group(), stringWithVariables);
		}

		// Find variable values and put value into rule
		variableSearchLoop: for (int i = 0; i < variablesFoundInRule.size(); i++) {
			val variable = variablesFoundInRule.get(i);
			val modifier = variablesFoundInRuleModifiers.get(i);
			for (val variableWithValue : variablesWithValues) {
				if (variable
						.equals(ImplementationContants.VARIABLE_MATCH_MODIFIER
								+ variableWithValue.getName().substring(1,
										variableWithValue.getName().length())
								+ ImplementationContants.VARIABLE_MATCH_MODIFIER)) {
					String value = variableWithValue.getValue();

					// Correct value
					if (value == null) {
						value = "";
					}

					// Check if variable has modifiers
					if (modifier != null) {
						// Replace variable with modified value in rule
						val formattedVariable = variable
								+ ImplementationContants.VARIABLE_VALUE_MODIFIER_START
								+ modifier
								+ ImplementationContants.VARIABLE_VALUE_MODIFIER_END;
						try {
							val formattedValue = String.format(modifier,
									Double.parseDouble(value));
							stringWithVariables = stringWithVariables.replace(
									formattedVariable, formattedValue);
						} catch (final Exception e) {
							log.warn(
									"Could not modify string {} with modifier {}",
									formattedVariable, modifier);
							stringWithVariables = stringWithVariables.replace(
									formattedVariable, value);
						}
					} else {
						// Replace variable with value in rule
						stringWithVariables = stringWithVariables.replace(
								variable, value);
					}

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
