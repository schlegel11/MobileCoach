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
		val date = new Date(System.currentTimeMillis());

		return simpleDateFormat.format(date);
	}

	/**
	 * Creates a clean phone number of the given {@link String}
	 * 
	 * @param variableValue
	 * @return
	 */
	public static String cleanPhoneNumber(final String value) {
		val newValue = value.trim().replaceAll("[^\\d]", "")
				.replaceAll("^0+", "");
		return newValue;
	}

	/**
	 * Creates a clean email address of the given {@link String}
	 * 
	 * @param variableValue
	 * @return
	 */
	public static String cleanEmailAddress(final String value) {
		val newValue = value.trim().toLowerCase();
		return newValue;
	}
}
