package org.isgf.mhc.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.model.server.concepts.AbstractRule;
import org.isgf.mhc.model.server.concepts.AbstractVariableWithValue;
import org.isgf.mhc.tools.model.RuleEvaluationResult;

import com.fathzer.soft.javaluator.DoubleEvaluator;

/**
 * Evaluates rules
 * 
 * @author Andreas Filler
 */
@Log4j2
public class RuleEvaluator {
	/**
	 * Evaluates an {@link AbstractRule} including the given
	 * {@link AbstractVariableWithValue}s
	 * 
	 * @param rule
	 *            The {@link AbstractRule} to evaluate
	 * @param variablesWithValues
	 *            The {@link AbstractVariableWithValue}s to use in the
	 *            evaluation
	 * @return {@link RuleEvaluationResult} contains several information about
	 *         the rule evaluation
	 */
	public static RuleEvaluationResult evaluateRule(final AbstractRule rule,
			final List<AbstractVariableWithValue> variablesWithValues) {
		final val ruleEvaluationResult = new RuleEvaluationResult();

		try {
			// Evaluate rule
			final double ruleResult;
			try {
				ruleResult = evaluateRuleTerm(rule.getRuleWithPlaceholders(),
						variablesWithValues);
				ruleEvaluationResult.setRuleValue(ruleResult);
			} catch (final Exception e) {
				throw new Exception("Could not parse rule: " + e.getMessage());
			}

			// Evaluate rule comparison term
			final double ruleComparisonTermResult;
			try {
				ruleComparisonTermResult = evaluateRuleTerm(
						rule.getRuleComparisonTermWithPlaceholders(),
						variablesWithValues);
				ruleEvaluationResult
						.setRuleComparisionTermValue(ruleComparisonTermResult);
			} catch (final Exception e) {
				throw new Exception("Could not parse rule comparision term: "
						+ e.getMessage());
			}

			// Evaluate equation sign
			ruleEvaluationResult.setRuleMatchesEquationSign(false);
			switch (rule.getRuleEquationSign()) {
				case IS_SMALLER_THAN:
					if (ruleResult < ruleComparisonTermResult) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case IS_SMALLER_OR_EQUAL_THAN:
					if (ruleResult <= ruleComparisonTermResult) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case EQUALS:
					if (ruleResult == ruleComparisonTermResult) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case IS_BIGGER_OR_EQUAL_THAN:
					if (ruleResult >= ruleComparisonTermResult) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case IS_BIGGER_THAN:
					if (ruleResult > ruleComparisonTermResult) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
			}

			// Evaluation of rule was successful
			ruleEvaluationResult.setEvaluatedSuccessful(true);
		} catch (final Exception e) {
			ruleEvaluationResult.setEvaluatedSuccessful(false);
			ruleEvaluationResult.setErrorMessage("Could not evaluate rule: "
					+ e.getMessage());
		}

		return ruleEvaluationResult;
	}

	/**
	 * Evaluates a rule String
	 * 
	 * @param ruleWithPlaceholders
	 *            String to evaluate
	 * @param variablesWithValues
	 *            List of {@link AbstractVariableWithValue}s to replace in rule
	 *            before evaluation
	 * @return Value of the rule evaluation
	 */
	private static double evaluateRuleTerm(final String ruleWithPlaceholders,
			final List<AbstractVariableWithValue> variablesWithValues)
			throws Exception {
		String rule = ruleWithPlaceholders;

		// Prevent null pointer exceptions
		if (rule == null || rule.equals("")) {
			log.debug("It's an empty rule");
			return 0.0;
		}

		log.debug("Preparing rule {}", rule);

		// Find variables in rule
		final String variableFindPatternString = "\\$[a-zA-Z_]+";
		final val variableFindPattern = Pattern
				.compile(variableFindPatternString);
		final Matcher variableFindMatcher = variableFindPattern.matcher(rule);

		final val variablesFoundInRule = new ArrayList<String>();
		while (variableFindMatcher.find()) {
			variablesFoundInRule.add(variableFindMatcher.group());
			log.debug("Found variable {} in rule {}",
					variableFindMatcher.group(), rule);
		}

		// Find variable values and put value into rule
		for (final String variable : variablesFoundInRule) {
			for (final val variableWithValue : variablesWithValues) {
				if (variable.equals(variableWithValue.getName())) {
					String value = variableWithValue.getValue();

					// Correct value
					if (value == null || value.equals("")) {
						value = "0";
					}

					// Replace variable with value in rule
					rule = rule.replace(variable, "(" + value + ")");
					log.debug("Replaced {} with {}", variable, value);
					break;
				}
			}
			// Variable not found so replace with "(0)"
			rule = rule.replace(variable, "(0)");
			log.debug("Replaced not found variable {} with (0)", variable);
		}

		// Evaluate rule
		log.debug("Evaluating rule {}", rule);
		final val doubleEvaluator = new DoubleEvaluator();

		final val result = doubleEvaluator.evaluate(rule);
		log.debug("Result of rule {} is {}", rule, result);

		return result;
	}
}
