package ch.ethz.mc.tools;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab at the Health-IS Lab
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.IteratorUtils;

import ch.ethz.mc.model.memory.RuleEvaluationResult;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;

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
	 * @param locale
	 *            Locale used for rule resolution, esp. for number formatting of
	 *            double values
	 * @param rule
	 *            The {@link AbstractRule} to evaluate
	 * @param variablesWithValues
	 *            The {@link AbstractVariableWithValue}s to use in the
	 *            evaluation
	 * @return {@link RuleEvaluationResult} contains several information about
	 *         the rule evaluation
	 */
	public static RuleEvaluationResult evaluateRule(final Locale locale,
			final AbstractRule rule,
			final Collection<AbstractVariableWithValue> variablesWithValues) {
		val ruleEvaluationResult = new RuleEvaluationResult();

		log.debug("Rule of type '{}'", rule.getRuleEquationSign());
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
					ruleResult = evaluateTextRuleTerm(locale,
							rule.getRuleWithPlaceholders(), variablesWithValues);
					ruleEvaluationResult.setTextRuleValue(ruleResult);
				} catch (final Exception e) {
					throw new Exception("Could not parse rule: "
							+ e.getMessage());
				}

				// Evaluate rule comparison term
				final String ruleComparisonTermResult;
				try {
					ruleComparisonTermResult = evaluateTextRuleTerm(locale,
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

					// Prevent problems with daylight saving time
					calendarDiff.set(Calendar.HOUR_OF_DAY, 12);

					val dateParts = ruleEvaluationResult.getTextRuleValue()
							.trim().split("\\.");
					val calendar2 = Calendar.getInstance();
					calendar2.setTimeInMillis(InternalDateTime
							.currentTimeMillis());
					if (dateParts.length > 2 && dateParts[2].length() > 2) {
						calendarDiff.set(Integer.parseInt(dateParts[2]),
								Integer.parseInt(dateParts[1]) - 1,
								Integer.parseInt(dateParts[0]));
					} else if (dateParts.length > 2
							&& dateParts[2].length() == 2) {
						calendarDiff.set(Integer.parseInt(dateParts[2]) + 2000,
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

					final int equalDiff = calculateDaysBetweenDates(
							calendarDiff, calendar2);
					log.debug("Difference is {}", equalDiff);

					if (equalDiff == Integer.parseInt(ruleEvaluationResult
							.getTextRuleComparisonTermValue())) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_TRUE_IF_ZERO:
				case CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_ALWAYS_TRUE:
					val calendarNow = Calendar.getInstance();
					val calendarDiff1 = Calendar.getInstance();
					val calendarDiff2 = Calendar.getInstance();

					// Prevent problems with daylight saving time
					calendarDiff1.set(Calendar.HOUR_OF_DAY, 12);

					val dateParts1 = ruleEvaluationResult.getTextRuleValue()
							.trim().split("\\.");
					val dateParts2 = ruleEvaluationResult
							.getTextRuleComparisonTermValue().trim()
							.split("\\.");

					if (dateParts1.length > 2 && dateParts1[2].length() > 2) {
						calendarDiff1.set(Integer.parseInt(dateParts1[2]),
								Integer.parseInt(dateParts1[1]) - 1,
								Integer.parseInt(dateParts1[0]));
					} else if (dateParts1.length > 2
							&& dateParts1[2].length() == 2) {
						calendarDiff1.set(
								Integer.parseInt(dateParts1[2]) + 2000,
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
					} else if (dateParts2.length > 2
							&& dateParts2[2].length() == 2) {
						calendarDiff2.set(
								Integer.parseInt(dateParts2[2]) + 2000,
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
					calendarDiff2.setTimeZone(calendarDiff1.getTimeZone());

					final int calcDaysDiff = calculateDaysBetweenDates(
							calendarDiff1, calendarDiff2);
					log.debug("Difference is {}", calcDaysDiff);

					if (calcDaysDiff == 0) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					} else if (rule.getRuleEquationSign() == RuleEquationSignTypes.CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_ALWAYS_TRUE) {
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
	 * Calculate date difference in days between two dates
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	private static int calculateDaysBetweenDates(final Calendar date1,
			final Calendar date2) {
		Calendar calendar1 = (Calendar) date1.clone();
		Calendar calendar2 = (Calendar) date2.clone();

		if (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)) {
			return calendar2.get(Calendar.DAY_OF_YEAR)
					- calendar1.get(Calendar.DAY_OF_YEAR);
		} else {
			boolean swapped = false;
			if (calendar2.get(Calendar.YEAR) > calendar1.get(Calendar.YEAR)) {
				final Calendar temp = calendar1;
				calendar1 = calendar2;
				calendar2 = temp;
				swapped = true;
			}
			int additonalDays = 0;

			final int dayOfYear1 = calendar1.get(Calendar.DAY_OF_YEAR);

			while (calendar1.get(Calendar.YEAR) > calendar2.get(Calendar.YEAR)) {
				calendar1.add(Calendar.YEAR, -1);

				additonalDays += calendar1
						.getActualMaximum(Calendar.DAY_OF_YEAR);
			}

			if (!swapped) {
				return -1
						* (additonalDays - calendar2.get(Calendar.DAY_OF_YEAR) + dayOfYear1);
			} else {
				return additonalDays - calendar2.get(Calendar.DAY_OF_YEAR)
						+ dayOfYear1;
			}
		}
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
		final Function digitAtPosition = new Function("digit", 2);
		final Function inRangeCheck = new Function("inrange", 3);

		params.add(firstPosition);
		params.add(secondPosition);
		params.add(thirdPosition);
		params.add(positionInArray);
		params.add(digitAtPosition);
		params.add(inRangeCheck);

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
					return positionInArray(arguments);
				} else if (function == digitAtPosition) {
					return digitAtPosition(arguments.next(), arguments.next());
				} else if (function == inRangeCheck) {
					return inRangeCheck(arguments.next(), arguments.next(),
							arguments.next());
				} else {
					// If it's another function, pass it to DoubleEvaluator
					return super.evaluate(function, arguments,
							evaluationContext);
				}
			}

			final class PositionItem implements Comparable<PositionItem> {
				@Getter
				@Setter
				private int		position;
				@Getter
				@Setter
				private double	value;
				@Getter
				@Setter
				private double	shuffleValue;

				public PositionItem(final int position, final double value) {
					this.position = position;
					this.value = value;

					shuffleValue = 0;
				}

				private void swap() {
					final double remember = shuffleValue;
					shuffleValue = value;
					value = remember;
				}

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

				fakeShuffe(positionItems);
				Collections.sort(positionItems);

				return (double) positionItems.get(position).getPosition();
			}

			/**
			 * Returns the object at the given position in the array
			 *
			 * @param arguments
			 * @return
			 */
			private Double positionInArray(final Iterator<Double> arguments) {
				argumentsArrays = (Double[]) IteratorUtils.toArray(arguments,
						Double.class);

				return argumentsArrays[argumentsArrays[0].intValue()];
			}

			/**
			 * Returns the digit at the position given (counted from right) of
			 * the given number, e.g. position 2 of 12345 would be 4
			 *
			 * @param positionDouble
			 * @param numberDouble
			 * @return
			 */
			private Double digitAtPosition(final Double positionDouble,
					final Double numberDouble) {
				val position = positionDouble.intValue();
				val number = (int) Math.floor(numberDouble);

				return (double) (int) (number / Math.pow(10, position - 1) % 10);
			}

			/**
			 * Returns 1 if the value is between min and max, or 0 if not
			 *
			 * @param positionDouble
			 * @param numberDouble
			 * @return
			 */
			private Double inRangeCheck(final Double valueDouble,
					final Double minDouble, final Double maxDouble) {
				if (valueDouble >= minDouble && valueDouble <= maxDouble) {
					return 1d;
				} else {
					return 0d;
				}
			}

			/**
			 * Fake shuffles the position items; fake in this context means that
			 * it's shuffled, but same dataset return the same result at every
			 * run
			 *
			 * @param positionItems
			 */
			public void fakeShuffe(final ArrayList<PositionItem> positionItems) {
				val items = positionItems.size();

				for (int i = 0; i < items; i++) {
					final PositionItem positionItem = positionItems.get(i);

					int modifier1;
					if (i % 2 == 0) {
						modifier1 = 1;
					} else {
						modifier1 = -1;
					}
					int modifier2;
					if (positionItem.value % 2 == 0) {
						modifier2 = 1;
					} else {
						modifier2 = -1;
					}

					positionItem.shuffleValue = positionItem.value * modifier1
							* modifier2;

					positionItem.swap();
				}

				Collections.sort(positionItems);

				for (int i = 0; i < items; i++) {
					final PositionItem positionItem = positionItems.get(i);

					positionItem.swap();
				}
			}
		};

		val result = evaluator.evaluate(rule);
		log.debug("Result of rule {} is {}", rule, result);

		return result;
	}

	/**
	 * Evaluates a text rule {@link String}
	 *
	 * @param locale
	 *            Locale used for rule resolution, esp. for number formatting of
	 *            double values
	 * @param ruleWithPlaceholders
	 *            String to evaluate
	 * @param variablesWithValues
	 *            List of {@link AbstractVariableWithValue}s to replace in rule
	 *            before evaluation
	 * @return Value of the rule evaluation
	 */
	private static String evaluateTextRuleTerm(final Locale locale,
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
				.findVariablesAndReplaceWithTextValues(locale, rule,
						variablesWithValues, "");

		log.debug("Result of rule {} is {}", rule, result);

		return result;
	}
}
