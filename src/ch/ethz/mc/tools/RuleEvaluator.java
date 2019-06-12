package ch.ethz.mc.tools;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.script.ScriptEngineManager;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Years;

import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.fathzer.soft.javaluator.Function;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.memory.RuleEvaluationResult;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.tools.VariableStringReplacer.ENCODING;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Evaluates calculated and text based rules
 *
 * @author Andreas Filler
 */
@Log4j2
public class RuleEvaluator {
	static {
		// Initialize JsonPath to use Jackson
		Configuration.setDefaults(new Configuration.Defaults() {
			private final JsonProvider		jsonProvider	= new JacksonJsonProvider();
			private final MappingProvider	mappingProvider	= new JacksonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.of(Option.DEFAULT_PATH_LEAF_TO_NULL,
						Option.SUPPRESS_EXCEPTIONS);
			}
		});
	}

	private static ScriptEngineManager		scriptEngineManager	= new ScriptEngineManager();

	@Setter
	@Getter(value = AccessLevel.PRIVATE)
	private static VariablesManagerService	variablesManagerService;

	/**
	 * Evaluates an {@link AbstractRule} including the given
	 * {@link AbstractVariableWithValue}s
	 *
	 * @param participantId
	 *            The {@link ObjectId} of the {@link Participant} the rule
	 *            resolution refers to
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
	public static RuleEvaluationResult evaluateRule(
			final ObjectId participantId, final Locale locale,
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
							rule.getRuleWithPlaceholders(),
							variablesWithValues);
					ruleEvaluationResult.setCalculatedRuleValue(ruleResult);
				} catch (final Exception e) {
					throw new Exception(
							"Could not parse rule: " + e.getMessage());
				}

				// Evaluate rule comparison term
				final double ruleComparisonTermResult;
				try {
					ruleComparisonTermResult = evaluateCalculatedRuleTerm(
							rule.getRuleComparisonTermWithPlaceholders(),
							variablesWithValues);
					ruleEvaluationResult.setCalculatedRuleComparisonTermValue(
							ruleComparisonTermResult);
				} catch (final Exception e) {
					throw new Exception(
							"Could not parse rule comparision term: "
									+ e.getMessage());
				}
			} else if (rule.getRuleEquationSign()
					.isACrossInterventionVariableComparisionBasedEquationSignType()) {
				log.debug(
						"It's a cross intervention variable comparision based rule");
				ruleEvaluationResult.setCalculatedRule(false);

				// Evaluate rule
				final String ruleResult;
				try {
					ruleResult = evaluateTextRuleTerm(locale,
							rule.getRuleWithPlaceholders(), variablesWithValues,
							ENCODING.NONE);
					ruleEvaluationResult.setTextRuleValue(ruleResult);
				} catch (final Exception e) {
					throw new Exception(
							"Could not parse rule: " + e.getMessage());
				}

				// Clean and evaluate rule comparison term
				val ruleComparisonTermResult = rule
						.getRuleComparisonTermWithPlaceholders().trim();
				if (StringValidator
						.isValidVariableName(ruleComparisonTermResult)) {
					ruleEvaluationResult.setTextRuleComparisonTermValue(
							ruleComparisonTermResult);
				} else {
					throw new Exception(
							"Could not parse rule comparision term: It's not a valid variable name");
				}
			} else if (rule.getRuleEquationSign().isJavaScriptBasedRule()) {
				log.debug("It's a javascript based rule");
				ruleEvaluationResult.setCalculatedRule(false);

				// Evaluate rule
				final String ruleResult;
				try {
					ruleResult = evaluateTextRuleTerm(locale,
							rule.getRuleWithPlaceholders(), variablesWithValues,
							ENCODING.JAVASCRIPT);
					ruleEvaluationResult.setTextRuleValue(ruleResult);
				} catch (final Exception e) {
					throw new Exception(
							"Could not parse rule: " + e.getMessage());
				}
			} else {
				log.debug("It's a text based rule");
				ruleEvaluationResult.setCalculatedRule(false);

				// Evaluate rule
				final String ruleResult;
				try {
					ruleResult = evaluateTextRuleTerm(locale,
							rule.getRuleWithPlaceholders(), variablesWithValues,
							ENCODING.NONE);
					ruleEvaluationResult.setTextRuleValue(ruleResult);
				} catch (final Exception e) {
					throw new Exception(
							"Could not parse rule: " + e.getMessage());
				}

				// Evaluate rule comparison term
				final String ruleComparisonTermResult;
				try {
					ruleComparisonTermResult = evaluateTextRuleTerm(locale,
							rule.getRuleComparisonTermWithPlaceholders(),
							variablesWithValues, ENCODING.NONE);
					ruleEvaluationResult.setTextRuleComparisonTermValue(
							ruleComparisonTermResult);
				} catch (final Exception e) {
					throw new Exception(
							"Could not parse rule comparision term: "
									+ e.getMessage());
				}
			}

			// Objects
			final Calendar calendarDiff1;
			final Calendar calendarDiff2;

			// Evaluate equation sign
			ruleEvaluationResult.setRuleMatchesEquationSign(false);
			switch (rule.getRuleEquationSign()) {
				case CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE:
					ruleEvaluationResult.setRuleMatchesEquationSign(true);
					break;
				case CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_FALSE:
					ruleEvaluationResult.setRuleMatchesEquationSign(false);
					break;
				case CREATE_TEXT_BUT_RESULT_IS_ALWAYS_TRUE:
					ruleEvaluationResult.setRuleMatchesEquationSign(true);
					break;
				case CREATE_TEXT_BUT_RESULT_IS_ALWAYS_FALSE:
					ruleEvaluationResult.setRuleMatchesEquationSign(false);
					break;
				case CALCULATED_VALUE_IS_SMALLER_THAN:
					if (ruleEvaluationResult
							.getCalculatedRuleValue() < ruleEvaluationResult
									.getCalculatedRuleComparisonTermValue()) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATED_VALUE_IS_SMALLER_OR_EQUAL_THAN:
					if (ruleEvaluationResult
							.getCalculatedRuleValue() <= ruleEvaluationResult
									.getCalculatedRuleComparisonTermValue()) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATED_VALUE_EQUALS:
					if (ruleEvaluationResult
							.getCalculatedRuleValue() == ruleEvaluationResult
									.getCalculatedRuleComparisonTermValue()) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATED_VALUE_NOT_EQUALS:
					if (ruleEvaluationResult
							.getCalculatedRuleValue() != ruleEvaluationResult
									.getCalculatedRuleComparisonTermValue()) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATED_VALUE_IS_BIGGER_OR_EQUAL_THAN:
					if (ruleEvaluationResult
							.getCalculatedRuleValue() >= ruleEvaluationResult
									.getCalculatedRuleComparisonTermValue()) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATED_VALUE_IS_BIGGER_THAN:
					if (ruleEvaluationResult
							.getCalculatedRuleValue() > ruleEvaluationResult
									.getCalculatedRuleComparisonTermValue()) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATE_AMOUNT_OF_SELECT_MANY_VALUES:
					ruleEvaluationResult.setRuleMatchesEquationSign(true);

					String[] parts = ruleEvaluationResult.getTextRuleValue()
							.split(ImplementationConstants.SELECT_MANY_SEPARATOR,
									-1);

					ruleEvaluationResult.setCalculatedRuleValue(parts.length);
					ruleEvaluationResult
							.setTextRuleValue(String.valueOf(parts.length));
					break;
				case CALCULATE_SUM_OF_SELECT_MANY_VALUES_AND_TRUE_IF_SMALLER_THAN:
				case CALCULATE_SUM_OF_SELECT_MANY_VALUES_AND_TRUE_IF_EQUALS:
				case CALCULATE_SUM_OF_SELECT_MANY_VALUES_AND_TRUE_IF_BIGGER_THAN:
					parts = ruleEvaluationResult.getTextRuleValue().split(
							ImplementationConstants.SELECT_MANY_SEPARATOR, -1);

					double sum = 0d;

					for (val part : parts) {
						if (!StringUtils.isBlank(part)) {
							try {
								sum += Double.parseDouble(part);
							} catch (final Exception e) {
								// Do nothing
							}
						}
					}

					double compareValue = 0;
					if (!StringUtils.isBlank(ruleEvaluationResult
							.getTextRuleComparisonTermValue())) {
						try {
							compareValue = Double
									.parseDouble(ruleEvaluationResult
											.getTextRuleComparisonTermValue());
						} catch (final Exception e) {
							// Do nothing
						}
					}

					if (rule.getRuleEquationSign() == RuleEquationSignTypes.CALCULATE_SUM_OF_SELECT_MANY_VALUES_AND_TRUE_IF_EQUALS
							&& sum == compareValue) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					} else if (rule
							.getRuleEquationSign() == RuleEquationSignTypes.CALCULATE_SUM_OF_SELECT_MANY_VALUES_AND_TRUE_IF_SMALLER_THAN
							&& sum < compareValue) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					} else if (rule
							.getRuleEquationSign() == RuleEquationSignTypes.CALCULATE_SUM_OF_SELECT_MANY_VALUES_AND_TRUE_IF_BIGGER_THAN
							&& sum > compareValue) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					ruleEvaluationResult.setCalculatedRuleValue(sum);
					ruleEvaluationResult.setTextRuleValue(
							StringHelpers.cleanDoubleValue(sum));
					break;
				case TEXT_VALUE_EQUALS:
					if (ruleEvaluationResult.getTextRuleValue().trim()
							.toLowerCase()
							.equals(ruleEvaluationResult
									.getTextRuleComparisonTermValue().trim()
									.toLowerCase())) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case TEXT_VALUE_NOT_EQUALS:
					if (!ruleEvaluationResult.getTextRuleValue().trim()
							.toLowerCase()
							.equals(ruleEvaluationResult
									.getTextRuleComparisonTermValue().trim()
									.toLowerCase())) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case TEXT_VALUE_MATCHES_REGULAR_EXPRESSION:
					if (ruleEvaluationResult.getTextRuleValue().trim()
							.toLowerCase()
							.matches("^" + ruleEvaluationResult
									.getTextRuleComparisonTermValue().trim()
									.toLowerCase() + "$")) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case TEXT_VALUE_NOT_MATCHES_REGULAR_EXPRESSION:
					if (!ruleEvaluationResult.getTextRuleValue().trim()
							.toLowerCase()
							.matches("^" + ruleEvaluationResult
									.getTextRuleComparisonTermValue().trim()
									.toLowerCase() + "$")) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case TEXT_VALUE_FROM_SELECT_MANY_AT_POSITION:
					if (!StringUtils.isNumeric(ruleEvaluationResult
							.getTextRuleComparisonTermValue())) {
						ruleEvaluationResult.setRuleMatchesEquationSign(false);

						ruleEvaluationResult.setTextRuleValue("");
					} else {
						parts = ruleEvaluationResult.getTextRuleValue().split(
								ImplementationConstants.SELECT_MANY_SEPARATOR,
								-1);

						val position = Integer.parseInt(ruleEvaluationResult
								.getTextRuleComparisonTermValue()) - 1;

						if (position >= 0 && position < parts.length) {
							ruleEvaluationResult
									.setRuleMatchesEquationSign(true);

							ruleEvaluationResult
									.setTextRuleValue(parts[position]);
						} else {
							ruleEvaluationResult
									.setRuleMatchesEquationSign(false);

							ruleEvaluationResult.setTextRuleValue("");
						}
					}
					break;
				case TEXT_VALUE_FROM_SELECT_MANY_AT_RANDOM_POSITION:
					parts = ruleEvaluationResult.getTextRuleValue().split(
							ImplementationConstants.SELECT_MANY_SEPARATOR, -1);

					val position = RandomUtils.nextInt(0, parts.length);

					ruleEvaluationResult.setRuleMatchesEquationSign(true);

					ruleEvaluationResult.setTextRuleValue(parts[position]);
					break;
				case TEXT_VALUE_FROM_JSON_BY_JSON_PATH:
					try {

						val jsonResult = JsonPath.read(
								ruleEvaluationResult.getTextRuleValue(),
								"$" + ruleEvaluationResult
										.getTextRuleComparisonTermValue());

						ruleEvaluationResult.setRuleMatchesEquationSign(false);

						if (jsonResult instanceof LinkedList) {
							final StringBuffer resultsString = new StringBuffer();

							val jsonResultsList = (LinkedList<?>) jsonResult;
							for (int i = 0; i < jsonResultsList.size(); i++) {
								val item = jsonResultsList.get(i);

								if (item != null) {
									ruleEvaluationResult
											.setRuleMatchesEquationSign(true);

									if (resultsString.length() > 0) {
										resultsString.append(", ");
									}

									resultsString.append(item.toString());
								}
							}
							ruleEvaluationResult
									.setTextRuleValue(resultsString.toString());
						} else {
							ruleEvaluationResult
									.setTextRuleValue(jsonResult.toString());
						}
					} catch (final Exception e) {
						ruleEvaluationResult.setRuleMatchesEquationSign(false);
						ruleEvaluationResult.setTextRuleValue("");
					}

					break;
				case TEXT_VALUE_MATCHES_KEY:
					for (val splitValue1 : ruleEvaluationResult
							.getTextRuleValue().split(",")) {
						for (val splitValue2 : ruleEvaluationResult
								.getTextRuleComparisonTermValue().split(",")) {
							if (splitValue1.equals(splitValue2)) {
								ruleEvaluationResult
										.setRuleMatchesEquationSign(true);
								break;
							}
						}
					}
					break;
				case TEXT_VALUE_NOT_MATCHES_KEY:
					ruleEvaluationResult.setRuleMatchesEquationSign(true);

					for (val splitValue1 : ruleEvaluationResult
							.getTextRuleValue().split(",")) {
						for (val splitValue2 : ruleEvaluationResult
								.getTextRuleComparisonTermValue().split(",")) {
							if (splitValue1.equals(splitValue2)) {
								ruleEvaluationResult
										.setRuleMatchesEquationSign(false);
								break;
							}
						}
					}
					break;
				case DATE_DIFFERENCE_VALUE_EQUALS:
					calendarDiff1 = StringHelpers
							.createInternalDateCalendarRepresentation(
									ruleEvaluationResult.getTextRuleValue());

					calendarDiff2 = Calendar.getInstance();
					calendarDiff2.setTimeInMillis(
							InternalDateTime.currentTimeMillis());

					calendarDiff2.set(Calendar.HOUR_OF_DAY,
							calendarDiff1.get(Calendar.HOUR_OF_DAY));
					calendarDiff2.set(Calendar.MINUTE,
							calendarDiff1.get(Calendar.MINUTE));
					calendarDiff2.set(Calendar.SECOND,
							calendarDiff1.get(Calendar.SECOND));
					calendarDiff2.set(Calendar.MILLISECOND,
							calendarDiff1.get(Calendar.MILLISECOND));
					calendarDiff2.setTimeZone(calendarDiff1.getTimeZone());

					final int equalDiff = calculateDaysBetweenDates(
							calendarDiff1, calendarDiff2);
					log.debug("Difference is {}", equalDiff);

					if (equalDiff == Integer.parseInt(ruleEvaluationResult
							.getTextRuleComparisonTermValue())) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}
					break;
				case CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_TRUE_IF_ZERO:
				case CALCULATE_DATE_DIFFERENCE_IN_MONTHS_AND_TRUE_IF_ZERO:
				case CALCULATE_DATE_DIFFERENCE_IN_YEARS_AND_TRUE_IF_ZERO:
				case CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_ALWAYS_TRUE:
				case CALCULATE_DATE_DIFFERENCE_IN_MONTHS_AND_ALWAYS_TRUE:
				case CALCULATE_DATE_DIFFERENCE_IN_YEARS_AND_ALWAYS_TRUE:
					calendarDiff1 = StringHelpers
							.createInternalDateCalendarRepresentation(
									ruleEvaluationResult.getTextRuleValue());
					calendarDiff2 = StringHelpers
							.createInternalDateCalendarRepresentation(
									ruleEvaluationResult
											.getTextRuleComparisonTermValue());

					calendarDiff2.set(Calendar.HOUR_OF_DAY,
							calendarDiff1.get(Calendar.HOUR_OF_DAY));
					calendarDiff2.set(Calendar.MINUTE,
							calendarDiff1.get(Calendar.MINUTE));
					calendarDiff2.set(Calendar.SECOND,
							calendarDiff1.get(Calendar.SECOND));
					calendarDiff2.set(Calendar.MILLISECOND,
							calendarDiff1.get(Calendar.MILLISECOND));
					calendarDiff2.setTimeZone(calendarDiff1.getTimeZone());

					final int calcUnitsDiff;
					switch (rule.getRuleEquationSign()) {
						case CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_ALWAYS_TRUE:
						case CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_TRUE_IF_ZERO:
							calcUnitsDiff = calculateDaysBetweenDates(
									calendarDiff1, calendarDiff2);
							break;
						case CALCULATE_DATE_DIFFERENCE_IN_MONTHS_AND_ALWAYS_TRUE:
						case CALCULATE_DATE_DIFFERENCE_IN_MONTHS_AND_TRUE_IF_ZERO:
							calcUnitsDiff = calculateMonthsBetweenDates(
									calendarDiff1, calendarDiff2);
							break;
						case CALCULATE_DATE_DIFFERENCE_IN_YEARS_AND_ALWAYS_TRUE:
						case CALCULATE_DATE_DIFFERENCE_IN_YEARS_AND_TRUE_IF_ZERO:
							calcUnitsDiff = calculateYearsBetweenDates(
									calendarDiff1, calendarDiff2);
							break;
						default:
							// Not possible
							calcUnitsDiff = 0;
							break;
					}
					log.debug("Difference is {}", calcUnitsDiff);

					if (calcUnitsDiff == 0) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					} else if (rule
							.getRuleEquationSign() == RuleEquationSignTypes.CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_ALWAYS_TRUE
							|| rule.getRuleEquationSign() == RuleEquationSignTypes.CALCULATE_DATE_DIFFERENCE_IN_MONTHS_AND_ALWAYS_TRUE
							|| rule.getRuleEquationSign() == RuleEquationSignTypes.CALCULATE_DATE_DIFFERENCE_IN_YEARS_AND_ALWAYS_TRUE) {
						ruleEvaluationResult.setRuleMatchesEquationSign(true);
					}

					ruleEvaluationResult.setCalculatedRuleValue(calcUnitsDiff);
					ruleEvaluationResult
							.setTextRuleValue(String.valueOf(calcUnitsDiff));
					break;
				case STARTS_ITERATION_FROM_X_UP_TO_Y_AND_RESULT_IS_CURRENT:
				case STARTS_REVERSE_ITERATION_FROM_X_DOWN_TO_Y_AND_RESULT_IS_CURRENT:
					ruleEvaluationResult.setRuleMatchesEquationSign(true);
					ruleEvaluationResult.setIterator(true);
					break;
				case CHECK_VALUE_IN_VARIABLE_ACROSS_INVTERVENTIONS_AND_TRUE_IF_DUPLICATE_FOUND:
					val duplicateFound = checkValueInVariableForDuplicates(
							participantId,
							ruleEvaluationResult.getTextRuleValue(),
							ruleEvaluationResult
									.getTextRuleComparisonTermValue());

					ruleEvaluationResult
							.setRuleMatchesEquationSign(duplicateFound);

					break;
				case EXECUTE_JAVASCRIPT_IN_X_AND_STORE_VALUES_BUT_RESULT_IS_ALWAYS_TRUE:
					val scriptExecutionValues = executeJavaScript(
							ruleEvaluationResult.getTextRuleValue());

					for (val entry : scriptExecutionValues.entrySet()) {
						variablesManagerService
								.writeVariableValueOfParticipant(participantId,
										ImplementationConstants.VARIABLE_PREFIX
												+ entry.getKey(),
										entry.getValue());
					}

					ruleEvaluationResult.setRuleMatchesEquationSign(true);
					break;
				default:
					break;
			}

			// Evaluation of rule was successful
			ruleEvaluationResult.setEvaluatedSuccessful(true);
		} catch (final Exception e) {
			ruleEvaluationResult.setEvaluatedSuccessful(false);
			ruleEvaluationResult.setErrorMessage(
					"Could not evaluate rule: " + e.getMessage());
			log.warn("Error when evaluation rule: {}", e.getMessage());
		}

		return ruleEvaluationResult;
	}

	/**
	 * Executes given script and returns result values, if provided in variable
	 * results as Map
	 * 
	 * @param script
	 * @return
	 * @throws Exception
	 */
	private static Map<String, String> executeJavaScript(final String script)
			throws Exception {
		val resultValuesMap = new HashMap<String, String>();

		val jsEngine = scriptEngineManager.getEngineByName("JavaScript");

		val scriptResultValues = jsEngine.eval(script);

		if (scriptResultValues != null) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> scriptResultValuesMap = (Map<String, Object>) scriptResultValues;

			for (val entry : scriptResultValuesMap.entrySet()) {
				resultValuesMap.put(entry.getKey().toString(),
						entry.getValue().toString());
			}
		}

		return resultValuesMap;
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
		final Calendar calendar1 = (Calendar) date1.clone();
		final Calendar calendar2 = (Calendar) date2.clone();

		final TimeZone timeZone1 = calendar1.getTimeZone();
		final TimeZone timeZone2 = calendar2.getTimeZone();

		final DateTimeZone jodaTimeZone1 = DateTimeZone
				.forID(timeZone1.getID());
		final DateTimeZone jodaTimeZone2 = DateTimeZone
				.forID(timeZone2.getID());

		final DateTime dateTime1 = new DateTime(calendar1.getTimeInMillis(),
				jodaTimeZone1);
		final DateTime dateTime2 = new DateTime(calendar2.getTimeInMillis(),
				jodaTimeZone2);

		return Days.daysBetween(dateTime1, dateTime2).getDays();
	}

	/**
	 * Calculate date difference in months between two dates
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	private static int calculateMonthsBetweenDates(final Calendar date1,
			final Calendar date2) {
		final Calendar calendar1 = (Calendar) date1.clone();
		final Calendar calendar2 = (Calendar) date2.clone();

		final TimeZone timeZone1 = calendar1.getTimeZone();
		final TimeZone timeZone2 = calendar2.getTimeZone();

		final DateTimeZone jodaTimeZone1 = DateTimeZone
				.forID(timeZone1.getID());
		final DateTimeZone jodaTimeZone2 = DateTimeZone
				.forID(timeZone2.getID());

		final DateTime dateTime1 = new DateTime(calendar1.getTimeInMillis(),
				jodaTimeZone1);
		final DateTime dateTime2 = new DateTime(calendar2.getTimeInMillis(),
				jodaTimeZone2);

		return Months.monthsBetween(dateTime1, dateTime2).getMonths();
	}

	/**
	 * Calculate date difference in years between two dates
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	private static int calculateYearsBetweenDates(final Calendar date1,
			final Calendar date2) {
		final Calendar calendar1 = (Calendar) date1.clone();
		final Calendar calendar2 = (Calendar) date2.clone();

		final TimeZone timeZone1 = calendar1.getTimeZone();
		final TimeZone timeZone2 = calendar2.getTimeZone();

		final DateTimeZone jodaTimeZone1 = DateTimeZone
				.forID(timeZone1.getID());
		final DateTimeZone jodaTimeZone2 = DateTimeZone
				.forID(timeZone2.getID());

		final DateTime dateTime1 = new DateTime(calendar1.getTimeInMillis(),
				jodaTimeZone1);
		final DateTime dateTime2 = new DateTime(calendar2.getTimeInMillis(),
				jodaTimeZone2);

		return Years.yearsBetween(dateTime1, dateTime2).getYears();
	}

	/**
	 * Checks if the variable value already exists for the given variable in
	 * other interventions mentioned in the list of interventions for uniqueness
	 * checks
	 * 
	 * @param participantId
	 *            The {@link ObjectId} of the {@link Participant} defining the
	 *            search scope
	 * @param value
	 *            The value to use for the check
	 * @param variableName
	 *            The variable to check regarding the value
	 * @return
	 */
	private static boolean checkValueInVariableForDuplicates(
			final ObjectId participantId, final String value,
			final String variableName) {

		final boolean duplicatesAvailable = variablesManagerService
				.checkValueInVariableForDuplicates(participantId, value,
						variableName);

		if (duplicatesAvailable) {
			log.debug(
					"At least one duplicate has been found in the mentioned interventions");
		} else {
			log.debug(
					"No duplicates have been found in the mentioned interventions");
		}

		return duplicatesAvailable;
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

		final AbstractEvaluator<Double> evaluator = new DoubleEvaluator(
				params) {
			private Double[] argumentsArrays;

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

				return (double) (int) (number / Math.pow(10, position - 1)
						% 10);
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
			public void fakeShuffe(
					final ArrayList<PositionItem> positionItems) {
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
	 * @param encoding
	 *            If set all variable values will be specifically encoded
	 * @return Value of the rule evaluation
	 */
	private static String evaluateTextRuleTerm(final Locale locale,
			final String ruleWithPlaceholders,
			final Collection<AbstractVariableWithValue> variablesWithValues,
			final ENCODING encoding) throws Exception {
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
						variablesWithValues, "", encoding);

		log.debug("Result of rule {} is {}", rule, result);

		return result;
	}
}
