package ch.ethz.mc.model.persistent.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Supported {@link RuleEquationSignTypes}
 * 
 * @author Andreas Filler
 */
public enum RuleEquationSignTypes {
	CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE, CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_FALSE, CALCULATED_VALUE_IS_SMALLER_THAN, CALCULATED_VALUE_IS_SMALLER_OR_EQUAL_THAN, CALCULATED_VALUE_EQUALS, CALCULATED_VALUE_IS_BIGGER_OR_EQUAL_THAN, CALCULATED_VALUE_IS_BIGGER_THAN, CREATE_TEXT_BUT_RESULT_IS_ALWAYS_TRUE, CREATE_TEXT_BUT_RESULT_IS_ALWAYS_FALSE, TEXT_VALUE_EQUALS, TEXT_VALUE_NOT_EQUALS, TEXT_VALUE_MATCHES_REGULAR_EXPRESSION, TEXT_VALUE_NOT_MATCHES_REGULAR_EXPRESSION, DATE_DIFFERENCE_VALUE_EQUALS, CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_TRUE_IF_ZERO;

	private static List<RuleEquationSignTypes>	calculatedEquationSigns	= null;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}

	public synchronized boolean isCalculatedEquationSignType() {
		// Create list at first check
		if (calculatedEquationSigns == null) {
			calculatedEquationSigns = new ArrayList<RuleEquationSignTypes>();
			calculatedEquationSigns
					.add(CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE);
			calculatedEquationSigns
					.add(CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_FALSE);
			calculatedEquationSigns.add(CALCULATED_VALUE_IS_SMALLER_THAN);
			calculatedEquationSigns
					.add(CALCULATED_VALUE_IS_SMALLER_OR_EQUAL_THAN);
			calculatedEquationSigns.add(CALCULATED_VALUE_EQUALS);
			calculatedEquationSigns
					.add(CALCULATED_VALUE_IS_BIGGER_OR_EQUAL_THAN);
			calculatedEquationSigns.add(CALCULATED_VALUE_IS_BIGGER_THAN);
		}

		if (calculatedEquationSigns.contains(this)) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean isRegularExpressionBasedEquationSignType() {
		if (this == TEXT_VALUE_MATCHES_REGULAR_EXPRESSION
				|| this == TEXT_VALUE_NOT_MATCHES_REGULAR_EXPRESSION) {
			return true;
		} else {
			return false;
		}
	}
}