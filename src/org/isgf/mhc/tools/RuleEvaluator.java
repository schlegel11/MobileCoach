package org.isgf.mhc.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.IteratorUtils;
import org.isgf.mhc.model.memory.RuleEvaluationResult;
import org.isgf.mhc.model.persistent.concepts.AbstractRule;
import org.isgf.mhc.model.persistent.concepts.AbstractVariableWithValue;

import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.Function;

/**
 * Evaluates calculated and text based rules
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
			final Collection<AbstractVariableWithValue> variablesWithValues) {
		val ruleEvaluationResult = new RuleEvaluationResult();

		try {
			if (rule.getRuleEquationSign().isCalculatedEquationSignType()) {
				log.debug("It's a calculated rule");
				ruleEvaluationResult.setCalculatedRule(true);

				// Evaluate rule
				final double ruleResult;
				try {
					ruleResult = evaluateCalculatedRuleTerm(
							rule.getRuleWithPlaceholders(), variablesWithValues);
					ruleEvaluationResult.setCalculatedRuleValue(ruleResult);
				} catch (final Exception e) {
					throw new Exception("Could not parse rule: "
							+ e.getMessage());
				}

				// Evaluate rule comparison term
				final double ruleComparisonTermResult;
				try {
					ruleComparisonTermResult = evaluateCalculatedRuleTerm(
							rule.getRuleComparisonTermWithPlaceholders(),
							variablesWithValues);
					ruleEvaluationResult
							.setCalculatedRuleComparisonTermValue(ruleComparisonTermResult);
				} catch (final Exception e) {
					throw new Exception(
							"Could not parse rule comparision term: "
									+ e.getMessage());
				}
			} else {
				log.debug("It's a text based rule");
				ruleEvaluationResult.setCalculatedRule(false);

				// Evaluate rule
				final String ruleResult;
				try {
					ruleResult = evaluateTextRuleTerm(
							rule.getRuleWithPlaceholders(), variablesWithValues);
					ruleEvaluationResult.setTextRuleValue(ruleResult);
				} catch (final Exception e) {
					throw new Exception("Could not parse rule: "
							+ e.getMessage());
				}

				// Evaluate rule comparison term
				final String ruleComparisonTermResult;
				try {
					ruleComparisonTermResult = evaluateTextRuleTerm(
							rule.getRuleComparisonTermWithPlaceholders(),
							variablesWithValues);
					ruleEvaluationResult
							.setTextRuleComparisonTermValue(ruleComparisonTermResult);
				} catch (final Exception e) {
					throw new Exception(
							"Could not parse rule comparision term: "
									+ e.getMessage());
				}
			}

			// Evaluate equation sign
			ruleEvaluationResult.setRuleMatchesEquationSign(false);
			switch (rule.getRuleEquationSign()) {
				case CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE:
					ruleEvaluationResult.setRuleMatchesEquationSign(true);
					break;
				case CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_FALSE:
					ruleEvaluationResult.setRuleMatchesEquationSign(false);
					break;
				case CALCULATED_VALUE_IS_SMALLER_THAN:
					if (ruleEvaluationResult.getCalculatedRuleValue() < ruleEvaluationResult
							.getCalculatedRuleComparisonTermValue()) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATED_VALUE_IS_SMALLER_OR_EQUAL_THAN:
					if (ruleEvaluationResult.getCalculatedRuleValue() <= ruleEvaluationResult
							.getCalculatedRuleComparisonTermValue()) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATED_VALUE_EQUALS:
					if (ruleEvaluationResult.getCalculatedRuleValue() == ruleEvaluationResult
							.getCalculatedRuleComparisonTermValue()) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATED_VALUE_IS_BIGGER_OR_EQUAL_THAN:
					if (ruleEvaluationResult.getCalculatedRuleValue() >= ruleEvaluationResult
							.getCalculatedRuleComparisonTermValue()) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATED_VALUE_IS_BIGGER_THAN:
					if (ruleEvaluationResult.getCalculatedRuleValue() > ruleEvaluationResult
							.getCalculatedRuleComparisonTermValue()) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CREATE_TEXT_BUT_RESULT_IS_ALWAYS_TRUE:
					ruleEvaluationResult.setRuleMatchesEquationSign(true);
					break;
				case CREATE_TEXT_BUT_RESULT_IS_ALWAYS_FALSE:
					ruleEvaluationResult.setRuleMatchesEquationSign(false);
					break;
				case TEXT_VALUE_EQUALS:
					if (ruleEvaluationResult
							.getTextRuleValue()
							.trim()
							.toLowerCase()
							.equals(ruleEvaluationResult
									.getTextRuleComparisonTermValue().trim()
									.toLowerCase())) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case TEXT_VALUE_NOT_EQUALS:
					if (!ruleEvaluationResult
							.getTextRuleValue()
							.trim()
							.toLowerCase()
							.equals(ruleEvaluationResult
									.getTextRuleComparisonTermValue().trim()
									.toLowerCase())) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case TEXT_VALUE_MATCHES_REGULAR_EXPRESSION:
					if (ruleEvaluationResult
							.getTextRuleValue()
							.trim()
							.toLowerCase()
							.matches(
									"^"
											+ ruleEvaluationResult
													.getTextRuleComparisonTermValue()
													.trim() + "$")) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case TEXT_VALUE_NOT_MATCHES_REGULAR_EXPRESSION:
					if (!ruleEvaluationResult
							.getTextRuleValue()
							.trim()
							.toLowerCase()
							.matches(
									"^"
											+ ruleEvaluationResult
													.getTextRuleComparisonTermValue()
													.trim() + "$")) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case DATE_DIFFERENCE_VALUE_EQUALS:
					val calendarDiff = Calendar.getInstance();
					val dateParts = ruleEvaluationResult.getTextRuleValue()
							.trim().split("\\.");
					val calendar2 = Calendar.getInstance();
					calendar2.setTimeInMillis(InternalDateTime
							.currentTimeMillis());
					if (dateParts.length > 2 && dateParts[2].length() > 2) {
						calendarDiff.set(Integer.parseInt(dateParts[2]),
								Integer.parseInt(dateParts[1]) - 1,
								Integer.parseInt(dateParts[0]));
					} else {
						calendarDiff.set(calendar2.get(Calendar.YEAR),
								Integer.parseInt(dateParts[1]) - 1,
								Integer.parseInt(dateParts[0]));
					}
					calendar2.set(Calendar.HOUR_OF_DAY,
							calendarDiff.get(Calendar.HOUR_OF_DAY));
					calendar2.set(Calendar.MINUTE,
							calendarDiff.get(Calendar.MINUTE));
					calendar2.set(Calendar.SECOND,
							calendarDiff.get(Calendar.SECOND));
					calendar2.set(Calendar.MILLISECOND,
							calendarDiff.get(Calendar.MILLISECOND));

					final long equalDiff = calendar2.getTimeInMillis()
							- calendarDiff.getTimeInMillis();

					if (TimeUnit.MILLISECONDS.toDays(equalDiff) == Long
							.parseLong(ruleEvaluationResult
									.getTextRuleComparisonTermValue())) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
				case CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_TRUE_IF_ZERO:
					val calendarNow = Calendar.getInstance();
					val calendarDiff1 = Calendar.getInstance();
					val calendarDiff2 = Calendar.getInstance();
					val dateParts1 = ruleEvaluationResult.getTextRuleValue()
							.trim().split("\\.");
					val dateParts2 = ruleEvaluationResult
							.getTextRuleComparisonTermValue().trim()
							.split("\\.");
					if (dateParts1.length > 2 && dateParts1[2].length() > 2) {
						calendarDiff1.set(Integer.parseInt(dateParts1[2]),
								Integer.parseInt(dateParts1[1]) - 1,
								Integer.parseInt(dateParts1[0]));
					} else {
						calendarDiff1.set(calendarNow.get(Calendar.YEAR),
								Integer.parseInt(dateParts1[1]) - 1,
								Integer.parseInt(dateParts1[0]));
					}
					if (dateParts2.length > 2 && dateParts2[2].length() > 2) {
						calendarDiff2.set(Integer.parseInt(dateParts2[2]),
								Integer.parseInt(dateParts2[1]) - 1,
								Integer.parseInt(dateParts2[0]));
					} else {
						calendarDiff2.set(calendarNow.get(Calendar.YEAR),
								Integer.parseInt(dateParts2[1]) - 1,
								Integer.parseInt(dateParts2[0]));
					}
					calendarDiff2.set(Calendar.HOUR_OF_DAY,
							calendarDiff1.get(Calendar.HOUR_OF_DAY));
					calendarDiff2.set(Calendar.MINUTE,
							calendarDiff1.get(Calendar.MINUTE));
					calendarDiff2.set(Calendar.SECOND,
							calendarDiff1.get(Calendar.SECOND));
					calendarDiff2.set(Calendar.MILLISECOND,
							calendarDiff1.get(Calendar.MILLISECOND));

					final long calcDiff = calendarDiff2.getTimeInMillis()
							- calendarDiff1.getTimeInMillis();

					final int calcDaysDiff = (int) TimeUnit.MILLISECONDS
							.toDays(calcDiff);
					if (calcDaysDiff == 0) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}

					ruleEvaluationResult.setCalculatedRuleValue(calcDaysDiff);
					ruleEvaluationResult.setTextRuleValue(String
							.valueOf(calcDaysDiff));
					break;
				default:
					break;
			}

			// Evaluation of rule was successful
			ruleEvaluationResult.setEvaluatedSuccessful(true);
		} catch (final Exception e) {
			ruleEvaluationResult.setEvaluatedSuccessful(false);
			ruleEvaluationResult.setErrorMessage("Could not evaluate rule: "
					+ e.getMessage());
			log.warn("Error when evaluation rule: {}", e.getMessage());
		}

		return ruleEvaluationResult;
	}

	/**
	 * Evaluates a calculatable rule {@link String}
	 * 
	 * @param ruleWithPlaceholders
	 *            String to evaluate
	 * @param variablesWithValues
	 *            List of {@link AbstractVariableWithValue}s to replace in rule
	 *            before evaluation
	 * @return Value of the rule evaluation
	 */
	private static double evaluateCalculatedRuleTerm(
			final String ruleWithPlaceholders,
			final Collection<AbstractVariableWithValue> variablesWithValues)
			throws Exception {
		String rule = ruleWithPlaceholders;

		// Prevent null pointer exceptions
		if (rule == null || rule.equals("")) {
			log.debug("It's an empty rule");
			return 0.0;
		}

		log.debug("Preparing rule {}", rule);

		// Replace variables with their according values
		rule = VariableStringReplacer
				.findVariablesAndReplaceWithCalculatableValues(rule,
						variablesWithValues, "(0)");

		// Evaluate rule
		log.debug("Evaluating rule {}", rule);
		val params = DoubleEvaluator.getDefaultParameters();

		// Own functions
		final Function firstPosition = new Function("first", 1,
				Integer.MAX_VALUE);
		final Function secondPosition = new Function("second", 1,
				Integer.MAX_VALUE);
		final Function thirdPosition = new Function("third", 1,
				Integer.MAX_VALUE);
		final Function positionInArray = new Function("position", 2,
				Integer.MAX_VALUE);

		params.add(firstPosition);
		params.add(secondPosition);
		params.add(thirdPosition);
		params.add(positionInArray);

		final AbstractEvaluator<Double> evaluator = new DoubleEvaluator(params) {
			private Double[]	argumentsArrays;

			@Override
			protected Double evaluate(final Function function,
					final Iterator<Double> arguments,
					final Object evaluationContext) {
				if (function == firstPosition) {
					return topPositionEvaluation(0, arguments);
				} else if (function == secondPosition) {
					return topPositionEvaluation(1, arguments);
				} else if (function == thirdPosition) {
					return topPositionEvaluation(2, arguments);
				} else if (function == positionInArray) {
					return positionReturner(arguments);
				} else {
					// If it's another function, pass it to DoubleEvaluator
					return super.evaluate(function, arguments,
							evaluationContext);
				}
			}

			@AllArgsConstructor
			final class PositionItem implements Comparable<PositionItem> {
				@Getter
				@Setter
				private int		position;
				@Getter
				@Setter
				private double	value;

				@Override
				public int compareTo(final PositionItem anotherInstance) {
					return (int) (anotherInstance.getValue() - value);
				}

			}

			/**
			 * Determines the position of the 1st, 2nd, 3rd highest value in the
			 * list; The result is randomized if all/several values would fit
			 * 
			 * @param i
			 * @param arguments
			 * @return
			 */
			private Double topPositionEvaluation(final int position,
					final Iterator<Double> arguments) {
				val positionItems = new ArrayList<PositionItem>();

				int i = 0;
				while (arguments.hasNext()) {
					i++;
					positionItems.add(new PositionItem(i, arguments.next()));
				}

				Collections.sort(positionItems);

				return (double) positionItems.get(position).getPosition();
			}

			/**
			 * Returns the object at the given position in the array
			 * 
			 * @param arguments
			 * @return
			 */
			private Double positionReturner(final Iterator<Double> arguments) {
				argumentsArrays = (Double[]) IteratorUtils.toArray(arguments,
						Double.class);

				return argumentsArrays[argumentsArrays[0].intValue()];
			}
		};

		val result = evaluator.evaluate(rule);
		log.debug("Result of rule {} is {}", rule, result);

		return result;
	}

	/**
	 * Evaluates a text rule {@link String}
	 * 
	 * @param ruleWithPlaceholders
	 *            String to evaluate
	 * @param variablesWithValues
	 *            List of {@link AbstractVariableWithValue}s to replace in rule
	 *            before evaluation
	 * @return Value of the rule evaluation
	 */
	private static String evaluateTextRuleTerm(
			final String ruleWithPlaceholders,
			final Collection<AbstractVariableWithValue> variablesWithValues)
			throws Exception {
		final String rule = ruleWithPlaceholders;

		// Prevent null pointer exceptions
		if (rule == null) {
			log.debug("It's an empty rule");
			return "";
		}

		log.debug("Preparing rule {}", rule);

		// Replace variables with their according values
		val result = VariableStringReplacer
				.findVariablesAndReplaceWithTextValues(rule,
						variablesWithValues, "");

		log.debug("Result of rule {} is {}", rule, result);

		return result;
	}
}
