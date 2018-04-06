package ch.ethz.mc.tools;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import lombok.val;
import lombok.extern.log4j.Log4j2;

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
		val variablePreFindPattern = Pattern.compile(
				ImplementationConstants.REGULAR_EXPRESSION_TO_MATCH_VARIABLES_IN_STRING);
		Matcher variablePreFindMatcher = variablePreFindPattern
				.matcher(stringWithVariables);

		while (variablePreFindMatcher.find()) {
			stringWithVariables = stringWithVariables.substring(0,
					variablePreFindMatcher.start())
					+ ImplementationConstants.VARIABLE_MATCH_MODIFIER
					+ variablePreFindMatcher.group().substring(1,
							variablePreFindMatcher.group().length())
					+ ImplementationConstants.VARIABLE_MATCH_MODIFIER
					+ stringWithVariables
							.substring(variablePreFindMatcher.end());

			variablePreFindMatcher = variablePreFindPattern
					.matcher(stringWithVariables);
		}

		// Find variables in rule
		val variableFindPattern = Pattern.compile(
				ImplementationConstants.REGULAR_EXPRESSION_TO_MATCH_MODIFIED_VARIABLES_IN_STRING);
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
						.equals(ImplementationConstants.VARIABLE_MATCH_MODIFIER
								+ variableWithValue.getName().substring(1,
										variableWithValue.getName().length())
								+ ImplementationConstants.VARIABLE_MATCH_MODIFIER)) {
					String value = variableWithValue.getValue();

					// Correct value
					if (value == null || value.equals("")) {
						value = "0";
					}

					// Replace variable with value in rule
					if (value.contains(",")) {
						stringWithVariables = stringWithVariables
								.replace(variable, value);
					} else {
						stringWithVariables = stringWithVariables
								.replace(variable, "(" + value + ")");
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
	 * @param locale
	 *            The {@link Locale} of the {@link Participant}
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
			final Locale locale, String stringWithVariables,
			final Collection<AbstractVariableWithValue> variablesWithValues,
			final String notFoundReplacer) {
		// Prevent null pointer exceptions
		if (stringWithVariables == null || stringWithVariables.equals("")) {
			log.debug("It's an empty string");
			return "";
		}

		// Adjust variables in rule to be unique for later replacement
		val variablePreFindPattern = Pattern.compile(
				ImplementationConstants.REGULAR_EXPRESSION_TO_MATCH_VARIABLES_IN_STRING);
		Matcher variablePreFindMatcher = variablePreFindPattern
				.matcher(stringWithVariables);

		while (variablePreFindMatcher.find()) {
			stringWithVariables = stringWithVariables.substring(0,
					variablePreFindMatcher.start())
					+ ImplementationConstants.VARIABLE_MATCH_MODIFIER
					+ variablePreFindMatcher.group().substring(1,
							variablePreFindMatcher.group().length())
					+ ImplementationConstants.VARIABLE_MATCH_MODIFIER
					+ stringWithVariables
							.substring(variablePreFindMatcher.end());

			variablePreFindMatcher = variablePreFindPattern
					.matcher(stringWithVariables);
		}

		// Find variables in rule
		val variableFindPattern = Pattern.compile(
				ImplementationConstants.REGULAR_EXPRESSION_TO_MATCH_MODIFIED_VARIABLES_IN_STRING);
		final Matcher variableFindMatcher = variableFindPattern
				.matcher(stringWithVariables);

		val variablesFoundInRule = new ArrayList<String>();
		val variablesFoundInRuleModifiers = new ArrayList<String>();
		while (variableFindMatcher.find()) {
			variablesFoundInRule.add(variableFindMatcher.group());

			// Check for modifiers
			val variableModifierFindPattern = Pattern.compile(
					ImplementationConstants.REGULAR_EXPRESSION_TO_MATCH_VALUE_MODIFIER);
			final Matcher variableModifierFindMatcher = variableModifierFindPattern
					.matcher(stringWithVariables
							.substring(variableFindMatcher.end()));

			if (variableModifierFindMatcher.find()
					&& variableModifierFindMatcher.start() == 0) {
				variablesFoundInRuleModifiers
						.add(variableModifierFindMatcher.group().substring(1,
								variableModifierFindMatcher.group().length()
										- 1));
			} else {
				variablesFoundInRuleModifiers.add(null);
			}

			log.debug("Found variable {} in string {}",
					variableFindMatcher.group(), stringWithVariables);
		}

		// Find variable values and put value into rule
		variableSearchLoop: for (int i = 0; i < variablesFoundInRule
				.size(); i++) {
			val variable = variablesFoundInRule.get(i);
			val modifier = variablesFoundInRuleModifiers.get(i);
			for (val variableWithValue : variablesWithValues) {
				if (variable
						.equals(ImplementationConstants.VARIABLE_MATCH_MODIFIER
								+ variableWithValue.getName().substring(1,
										variableWithValue.getName().length())
								+ ImplementationConstants.VARIABLE_MATCH_MODIFIER)) {
					String value = variableWithValue.getValue();

					// Correct value
					if (value == null) {
						value = "";
					}

					// Check if variable has modifiers
					if (modifier != null) {
						// Replace variable with modified value in rule
						val formattedVariable = variable
								+ ImplementationConstants.VARIABLE_VALUE_MODIFIER_START
								+ modifier
								+ ImplementationConstants.VARIABLE_VALUE_MODIFIER_END;

						try {
							String formattedValue;
							if (modifier.startsWith("#")) {
								// Own formatter
								switch (modifier) {
									case "#d":
										formattedValue = StringHelpers
												.formatDateString(value);
										break;
									case "#t":
										formattedValue = StringHelpers
												.formatTimeString(value);
										break;
									default:
										formattedValue = modifier;
										break;
								}
							} else {
								if (locale == null) {
									// Regular Java formatter
									formattedValue = String.format(modifier,
											Double.parseDouble(value));
								} else {
									formattedValue = String.format(locale,
											modifier,
											Double.parseDouble(value));
								}
							}
							stringWithVariables = stringWithVariables
									.replace(formattedVariable, formattedValue);
						} catch (final Exception e) {
							log.warn(
									"Could not modify string {} with modifier {}",
									formattedVariable, modifier);
							stringWithVariables = stringWithVariables
									.replace(formattedVariable, value);
						}
					} else {
						// Replace variable with value in rule
						stringWithVariables = stringWithVariables
								.replace(variable, value);
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
