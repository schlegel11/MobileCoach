package ch.ethz.mc.tools;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import lombok.val;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;

/**
 * Small helpers for {@link String}s
 * 
 * @author Andreas Filler
 */
public class StringHelpers {
	private static SimpleDateFormat	simpleDateFormat	= new SimpleDateFormat(
																"yyyy-MM-dd");
	private static SimpleDateFormat	longDateFormat		= new SimpleDateFormat(
																"yyyy-MM-dd HH:mm:ss");

	/**
	 * Creates a readable name representation of a rule's name
	 * 
	 * @param abstractRule
	 * @return
	 */
	public static String createRuleName(final AbstractRule abstractRule) {
		val name = new StringBuffer();

		if (abstractRule.getRuleWithPlaceholders() == null
				|| abstractRule.getRuleWithPlaceholders().equals("")) {
			if (abstractRule.getRuleEquationSign() != RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE
					&& abstractRule.getRuleEquationSign() != RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_FALSE
					&& abstractRule.getRuleEquationSign() != RuleEquationSignTypes.CREATE_TEXT_BUT_RESULT_IS_ALWAYS_TRUE
					&& abstractRule.getRuleEquationSign() != RuleEquationSignTypes.CREATE_TEXT_BUT_RESULT_IS_ALWAYS_FALSE) {
				name.append(ImplementationConstants.DEFAULT_OBJECT_NAME + " ");
			}
		} else {
			name.append(abstractRule.getRuleWithPlaceholders() + " ");
		}

		name.append(abstractRule.getRuleEquationSign().toString());

		if (abstractRule.getRuleComparisonTermWithPlaceholders() == null
				|| abstractRule.getRuleComparisonTermWithPlaceholders().equals(
						"")) {
			if (abstractRule.getRuleEquationSign() != RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE
					&& abstractRule.getRuleEquationSign() != RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_FALSE
					&& abstractRule.getRuleEquationSign() != RuleEquationSignTypes.CREATE_TEXT_BUT_RESULT_IS_ALWAYS_TRUE
					&& abstractRule.getRuleEquationSign() != RuleEquationSignTypes.CREATE_TEXT_BUT_RESULT_IS_ALWAYS_FALSE) {
				name.append(" " + ImplementationConstants.DEFAULT_OBJECT_NAME);
			}
		} else {
			name.append(" "
					+ abstractRule.getRuleComparisonTermWithPlaceholders());
		}

		return name.toString();
	}

	public static String createDailyUniqueIndex() {
		val date = new Date(InternalDateTime.currentTimeMillis());

		return simpleDateFormat.format(date);
	}

	public static String createStringTimeStamp(final long timeStamp) {
		if (timeStamp <= 0) {
			return Messages
					.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET);
		}

		val date = new Date(timeStamp);

		return longDateFormat.format(date);
	}

	/**
	 * Creates a clean phone number of the given {@link String}
	 * 
	 * @param phoneNumber
	 * @return
	 */
	public static String cleanPhoneNumber(final String phoneNumber) {
		String numberWithoutZeros = phoneNumber
				.trim()
				.replaceAll(
						ImplementationConstants.REGULAR_EXPRESSION_TO_CLEAN_PHONE_NUMBERS,
						"").replaceAll("^0+", "");

		// FIXME Need to be more international using the configuration (at best:
		// per intervention project)
		if (!numberWithoutZeros.startsWith("4")) {
			numberWithoutZeros = "41" + numberWithoutZeros;
		}

		return "00" + numberWithoutZeros;
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
	public static String cleanReceivedMessageString(final String messageString) {
		val newValue = messageString
				.trim()
				.toLowerCase()
				.replaceAll(
						ImplementationConstants.REGULAR_EXPRESSION_TO_CLEAN_RECEIVED_MESSAGE,
						"");
		return newValue;
	}

	/**
	 * Creates a clean {@link String} of the given double value
	 * 
	 * @param doubleValue
	 * @return
	 */
	public static String cleanDoubleValue(final double doubleValue) {
		val stringValue = String
				.valueOf(doubleValue)
				.replaceAll(
						ImplementationConstants.REGULAR_EXPRESSION_TO_CLEAN_DOUBLE_VALUES,
						"");
		return stringValue;
	}

	/**
	 * Replaces the simple commands within the text with HTML commands
	 * 
	 * @param text
	 * @return
	 */
	public static String parseHTMLFormatting(String text) {
		val pattern = Pattern
				.compile(ImplementationConstants.REGULAR_EXPRESSION_TO_FIND_BOLD_STRING_PARTS);
		val matcher = pattern.matcher(text);

		while (matcher.find()) {
			text = text.substring(0, matcher.start()) + "<strong>"
					+ matcher.group(1) + "</strong>"
					+ text.substring(matcher.end());
		}

		return text;
	}
}
