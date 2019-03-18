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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.persistent.types.MonitoringRuleTypes;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import lombok.val;

/**
 * Small helpers for {@link String}s
 *
 * @author Andreas Filler
 */
public class StringHelpers {
	private static SimpleDateFormat	simpleDateFormat				= new SimpleDateFormat(
			"yyyy-MM-dd");
	private static SimpleDateFormat	longDateFormat					= new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private static SimpleDateFormat	internalDateRepresentation		= new SimpleDateFormat(
			"dd.MM.yyyy");

	private static SimpleDateFormat	internalHourRepresentation		= new SimpleDateFormat(
			"HH");
	private static SimpleDateFormat	internalMinuteRepresentation	= new SimpleDateFormat(
			"mm");

	private static SimpleDateFormat	cleanDateRepresentation			= new SimpleDateFormat(
			"dd.MM.yyyy");

	private static SimpleDateFormat	cleanTimeRepresentation			= new SimpleDateFormat(
			"HH:mm");

	private static Gson				gson							= new Gson();

	/**
	 * Creates a readable name representation of a rule's name
	 *
	 * @param abstractRule
	 * @param withComment
	 * @return
	 */
	public static String createRuleName(final AbstractRule abstractRule,
			final boolean withComment) {
		val name = new StringBuffer();

		if (abstractRule instanceof MonitoringRule
				&& ((MonitoringRule) abstractRule)
						.getType() != MonitoringRuleTypes.NORMAL) {
			val monitoringRule = (MonitoringRule) abstractRule;

			switch (monitoringRule.getType()) {
				case DAILY:
					name.append(Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__DAILY_RULE));
					break;
				case PERIODIC:
					val simulatorActive = Constants.isSimulatedDateAndTime();
					name.append(Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__PERIODIC_RULE,
							String.valueOf((int) ((simulatorActive
									? ImplementationConstants.PERIODIC_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITH_SIMULATOR
									: ImplementationConstants.PERIODIC_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITHOUT_SIMULATOR)
									/ 60))));
					break;
				case UNEXPECTED_MESSAGE:
					name.append(Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__UNEXPECTED_MESSAGE_RULE));
					break;
				case EXTERNAL_MESSAGE:
					name.append(Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__EXTERNAL_MESSAGE_RULE));
					break;
				case USER_INTENTION:
					name.append(Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__USER_INTENTION_RULE));
					break;
				default:
					break;
			}

			return name.toString();
		}

		if (withComment && !abstractRule.getComment().equals("")) {
			name.append(abstractRule.getComment() + ": ");
		}

		if (abstractRule.getRuleWithPlaceholders() == null
				|| abstractRule.getRuleWithPlaceholders().equals("")) {
			if (abstractRule
					.getRuleEquationSign() != RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE
					&& abstractRule
							.getRuleEquationSign() != RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_FALSE
					&& abstractRule
							.getRuleEquationSign() != RuleEquationSignTypes.CREATE_TEXT_BUT_RESULT_IS_ALWAYS_TRUE
					&& abstractRule
							.getRuleEquationSign() != RuleEquationSignTypes.CREATE_TEXT_BUT_RESULT_IS_ALWAYS_FALSE) {
				name.append(ImplementationConstants.DEFAULT_OBJECT_NAME + " ");
			}
		} else {
			name.append(abstractRule.getRuleWithPlaceholders() + " ");
		}

		name.append(abstractRule.getRuleEquationSign().toString());

		if (abstractRule.getRuleComparisonTermWithPlaceholders() == null
				|| abstractRule.getRuleComparisonTermWithPlaceholders()
						.equals("")) {
			if (abstractRule
					.getRuleEquationSign() != RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE
					&& abstractRule
							.getRuleEquationSign() != RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_FALSE
					&& abstractRule
							.getRuleEquationSign() != RuleEquationSignTypes.CREATE_TEXT_BUT_RESULT_IS_ALWAYS_TRUE
					&& abstractRule
							.getRuleEquationSign() != RuleEquationSignTypes.CREATE_TEXT_BUT_RESULT_IS_ALWAYS_FALSE) {
				name.append(" " + ImplementationConstants.DEFAULT_OBJECT_NAME);
			}
		} else {
			name.append(
					" " + abstractRule.getRuleComparisonTermWithPlaceholders());
		}

		return name.toString();
	}

	public static String createDailyUniqueIndex() {
		val date = new Date(InternalDateTime.currentTimeMillis());

		return simpleDateFormat.format(date);
	}

	public static String createStringTimestamp(final long timestamp) {
		if (timestamp <= 0) {
			return Messages
					.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET);
		}

		val date = new Date(timestamp);

		return longDateFormat.format(date);
	}

	/**
	 * Formats given timestamp to an internal date representation, e.g.
	 * 26.07.2017
	 * 
	 * @param dateString
	 * @return
	 */
	public static String formatInternalDate(long timestamp) {
		return internalDateRepresentation.format(timestamp);
	}

	/**
	 * Formats given timestamp to an internal time representation, e.g. 04.5
	 * 
	 * @param timeString
	 * @return
	 */
	public static String formatInternalTime(long timestamp) {
		return String.valueOf(Integer
				.parseInt(internalHourRepresentation.format(timestamp))
				+ Double.parseDouble(
						internalMinuteRepresentation.format(timestamp)) / 60);
	}

	/**
	 * Formats given date String to clean date representation, e.g. 26.7. to
	 * 26.07.2017 in 2017
	 * 
	 * @param dateString
	 * @return
	 */
	public static String formatDateString(String dateString) {
		return cleanDateRepresentation.format(
				createInternalDateCalendarRepresentation(dateString).getTime());
	}

	/**
	 * Formats given decimal time String to clean time representation, e.g. 4.3
	 * to 04:18
	 * 
	 * @param timeString
	 * @return
	 */
	public static String formatTimeString(String timeString) {
		// Create calendar instance
		val calendar = Calendar.getInstance();
		calendar.setTimeInMillis(InternalDateTime.currentTimeMillis());

		// Convert value
		val timeValue = Double.parseDouble(timeString);

		val hour = Math.floor(timeValue);
		calendar.set(Calendar.HOUR_OF_DAY, (int) hour);
		calendar.set(Calendar.MINUTE,
				(int) (Math.round((timeValue - hour) * 60)));

		return cleanTimeRepresentation.format(calendar.getTime());
	}

	/**
	 * Creates calendar representation of given date string, e.g. 1.4. as
	 * Calendar representation of 01.04.2017 in 2017
	 * 
	 * @param dateString
	 * @return
	 */
	public static Calendar createInternalDateCalendarRepresentation(
			String dateString) {
		// Create instance of "now"
		val calendarNow = Calendar.getInstance();
		calendarNow.setTimeInMillis(InternalDateTime.currentTimeMillis());

		// Create empty calendar
		val calendar = Calendar.getInstance();

		// Prevent problems with daylight saving time
		calendar.set(Calendar.HOUR_OF_DAY, 12);

		val dateParts = dateString.trim().split("\\.");
		if (dateParts.length > 2 && dateParts[2].length() > 2) {
			calendar.set(Integer.parseInt(dateParts[2]),
					Integer.parseInt(dateParts[1]) - 1,
					Integer.parseInt(dateParts[0]));
		} else if (dateParts.length > 2 && dateParts[2].length() == 2) {
			calendar.set(Integer.parseInt(dateParts[2]) + 2000,
					Integer.parseInt(dateParts[1]) - 1,
					Integer.parseInt(dateParts[0]));
		} else {
			calendar.set(calendarNow.get(Calendar.YEAR),
					Integer.parseInt(dateParts[1]) - 1,
					Integer.parseInt(dateParts[0]));
		}

		return calendar;
	}

	/**
	 * Creates a clean phone number of the given {@link String} to result in the
	 * format "00CCNNNNNNN..." (CC: country code, NN...: number)
	 *
	 * @param phoneNumber
	 * @return
	 */
	public static String cleanPhoneNumber(final String phoneNumber) {
		String numberWithoutZeros = phoneNumber.trim()
				.replaceAll(
						ImplementationConstants.REGULAR_EXPRESSION_TO_CLEAN_PHONE_NUMBERS,
						"")
				.replaceAll("^0+", "");

		boolean needsCorrection = true;

		for (val countryCode : Constants
				.getSmsPhoneNumberAcceptedCountryCodes()) {
			if (numberWithoutZeros.startsWith(countryCode)) {
				needsCorrection = false;
				break;
			}
		}

		if (needsCorrection) {
			numberWithoutZeros = Constants.getSmsPhoneNumberCountryCorrection()
					+ numberWithoutZeros;
		}

		return "00" + numberWithoutZeros;
	}

	/**
	 * Creates a clean phone number of the given {@link String} to result in the
	 * format "+CCNNNNNNN..." (CC: country code, NN...: number)
	 *
	 * @param phoneNumber
	 * @return
	 */
	public static String cleanPhoneNumberPlusFormat(final String phoneNumber) {
		String numberWithoutZeros = phoneNumber.trim()
				.replaceAll(
						ImplementationConstants.REGULAR_EXPRESSION_TO_CLEAN_PHONE_NUMBERS,
						"")
				.replaceAll("^0+", "");

		boolean needsCorrection = true;

		for (val countryCode : Constants
				.getSmsPhoneNumberAcceptedCountryCodes()) {
			if (numberWithoutZeros.startsWith(countryCode)) {
				needsCorrection = false;
				break;
			}
		}

		if (needsCorrection) {
			numberWithoutZeros = Constants.getSmsPhoneNumberCountryCorrection()
					+ numberWithoutZeros;
		}

		return "+" + numberWithoutZeros;
	}

	/**
	 * Creates a clean email address of the given {@link String}
	 *
	 * @param emailAddress
	 * @return
	 */
	public static String cleanEmailAddress(final String emailAddress) {
		val newValue = emailAddress.trim().toLowerCase();
		return newValue;
	}

	/**
	 * Creates a clean message of the given string {@link String}
	 *
	 * @param messageString
	 * @return
	 */
	public static String cleanReceivedMessageString(
			final String messageString) {
		val newValue = messageString.trim().toLowerCase()
				.replaceAll(
						ImplementationConstants.REGULAR_EXPRESSION_TO_CLEAN_RECEIVED_MESSAGE,
						"")
				.replace(",", ".");
		return newValue;
	}

	/**
	 * Creates a clean filename of the given string {@link String}
	 *
	 * @param filenameString
	 * @return
	 */
	public static String cleanFilenameString(final String filenameString) {
		val newValue = filenameString.replaceAll(
				ImplementationConstants.REGULAR_EXPRESSION_TO_CLEAN_FILE_NAMES,
				"_");
		return newValue;
	}

	/**
	 * Creates a clean {@link String} of the given double value
	 *
	 * @param doubleValue
	 * @return
	 */
	public static String cleanDoubleValue(final double doubleValue) {
		val stringValue = String.valueOf(doubleValue).replaceAll(
				ImplementationConstants.REGULAR_EXPRESSION_TO_CLEAN_DOUBLE_VALUES,
				"");
		return stringValue;
	}

	/**
	 * Creates a random {@link String} of a given length
	 *
	 * @param length
	 * @return
	 */
	public static String createRandomString(final int length) {
		return RandomStringUtils.randomAlphanumeric(length);
	}

	/**
	 * Parses an colon separated multi-line list to JSON
	 * 
	 * @param text
	 * @param language
	 * @param variablesWithValues
	 * @return
	 */
	public static String parseColonSeparatedMultiLineStringToJSON(
			final LString text, final Locale language,
			final Collection<AbstractVariableWithValue> variablesWithValues) {

		val jsonOuterArray = new JsonArray();

		for (val line : text.get(language).split("\n", -1)) {
			if (!StringUtils.isBlank(line)) {
				val jsonArray = new JsonArray();

				final int indexOfColon = line.lastIndexOf(":");
				if (indexOfColon > -1) {
					String name = line.substring(0, indexOfColon);
					String value = line.substring(indexOfColon + 1);

					if (name.contains(
							ImplementationConstants.VARIABLE_PREFIX)) {
						name = VariableStringReplacer
								.findVariablesAndReplaceWithTextValues(language,
										name, variablesWithValues, "");
					}
					jsonArray.add(name);

					if (value.contains(
							ImplementationConstants.VARIABLE_PREFIX)) {
						value = VariableStringReplacer
								.findVariablesAndReplaceWithTextValues(language,
										value, variablesWithValues, "");
					}
					jsonArray.add(value);
				} else {
					if (line.contains(
							ImplementationConstants.VARIABLE_PREFIX)) {
						jsonArray.add(VariableStringReplacer
								.findVariablesAndReplaceWithTextValues(language,
										line, variablesWithValues, ""));
					} else {
						jsonArray.add(line);
					}
					jsonArray.add("");
				}

				jsonOuterArray.add(jsonArray);
			}
		}

		return gson.toJson(jsonOuterArray);
	}

	/**
	 * Replaces the simple commands within the text with HTML commands
	 *
	 * @param text
	 * @return
	 */
	public static String parseHTMLFormatting(String text) {
		val pattern = Pattern.compile(
				ImplementationConstants.REGULAR_EXPRESSION_TO_FIND_BOLD_STRING_PARTS);
		val matcher = pattern.matcher(text);

		while (matcher.find()) {
			text = text.substring(0, matcher.start()) + "<strong>"
					+ matcher.group(1) + "</strong>"
					+ text.substring(matcher.end());
		}

		return text;
	}
}
