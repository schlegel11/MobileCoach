package org.isgf.mhc.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.val;

import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.persistent.concepts.AbstractRule;
import org.isgf.mhc.model.persistent.types.RuleEquationSignTypes;

/**
 * Small helpers for {@link String}s
 * 
 * @author Andreas Filler
 */
public class StringHelpers {
	private static SimpleDateFormat	simpleDateFormat	= new SimpleDateFormat(
																"yyyy-MM-dd");

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
				name.append(ImplementationContants.DEFAULT_OBJECT_NAME + " ");
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
				name.append(" " + ImplementationContants.DEFAULT_OBJECT_NAME);
			}
		} else {
			name.append(" "
					+ abstractRule.getRuleComparisonTermWithPlaceholders());
		}

		return name.toString();
	}

	public static String createStringTimeStamp() {
		val date = new Date(InternalDateTime.currentTimeMillis());

		return simpleDateFormat.format(date);
	}

	/**
	 * Creates a clean phone number of the given {@link String}
	 * 
	 * @param phoneNumber
	 * @return
	 */
	public static String cleanPhoneNumber(final String phoneNumber) {
		val newValue = "00"
				+ phoneNumber.trim().replaceAll("[^\\d]", "")
						.replaceAll("^0+", "");
		return newValue;
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
		val newValue = messageString.trim().replaceAll("[^A-Za-z0-9\\s]", "");
		return newValue;
	}
}
