package ch.ethz.mc.model.persistent.types;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.List;

/**
 * Supported {@link RuleEquationSignTypes}
 *
 * @author Andreas Filler
 */
public enum RuleEquationSignTypes {
	CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE,
	CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_FALSE,
	CREATE_TEXT_BUT_RESULT_IS_ALWAYS_TRUE,
	CREATE_TEXT_BUT_RESULT_IS_ALWAYS_FALSE,
	CALCULATED_VALUE_IS_SMALLER_THAN,
	CALCULATED_VALUE_IS_SMALLER_OR_EQUAL_THAN,
	CALCULATED_VALUE_EQUALS,
	CALCULATED_VALUE_NOT_EQUALS,
	CALCULATED_VALUE_IS_BIGGER_OR_EQUAL_THAN,
	CALCULATED_VALUE_IS_BIGGER_THAN,
	CALCULATE_AMOUNT_OF_SELECT_MANY_VALUES,
	CALCULATE_SUM_OF_SELECT_MANY_VALUES_AND_TRUE_IF_SMALLER_THAN,
	CALCULATE_SUM_OF_SELECT_MANY_VALUES_AND_TRUE_IF_EQUALS,
	CALCULATE_SUM_OF_SELECT_MANY_VALUES_AND_TRUE_IF_BIGGER_THAN,
	TEXT_VALUE_EQUALS,
	TEXT_VALUE_NOT_EQUALS,
	TEXT_VALUE_MATCHES_REGULAR_EXPRESSION,
	TEXT_VALUE_NOT_MATCHES_REGULAR_EXPRESSION,
	TEXT_VALUE_FROM_SELECT_MANY_AT_POSITION,
	TEXT_VALUE_FROM_SELECT_MANY_AT_RANDOM_POSITION,
	TEXT_VALUE_FROM_JSON_BY_JSON_PATH,
	TEXT_VALUE_MATCHES_KEY,
	TEXT_VALUE_NOT_MATCHES_KEY,
	DATE_DIFFERENCE_VALUE_EQUALS,
	CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_TRUE_IF_ZERO,
	CALCULATE_DATE_DIFFERENCE_IN_MONTHS_AND_TRUE_IF_ZERO,
	CALCULATE_DATE_DIFFERENCE_IN_YEARS_AND_TRUE_IF_ZERO,
	CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_ALWAYS_TRUE,
	CALCULATE_DATE_DIFFERENCE_IN_MONTHS_AND_ALWAYS_TRUE,
	CALCULATE_DATE_DIFFERENCE_IN_YEARS_AND_ALWAYS_TRUE,
	STARTS_ITERATION_FROM_X_UP_TO_Y_AND_RESULT_IS_CURRENT,
	STARTS_REVERSE_ITERATION_FROM_X_DOWN_TO_Y_AND_RESULT_IS_CURRENT,
	CHECK_VALUE_IN_VARIABLE_ACROSS_INVTERVENTIONS_AND_TRUE_IF_DUPLICATE_FOUND,
	EXECUTE_JAVASCRIPT_IN_X_AND_STORE_VALUES_BUT_RESULT_IS_ALWAYS_TRUE;

	private static List<RuleEquationSignTypes>	calculatedEquationSigns	= null;
	private static List<RuleEquationSignTypes>	iteratorEquationSigns	= null;

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
			calculatedEquationSigns.add(CALCULATED_VALUE_NOT_EQUALS);
			calculatedEquationSigns
					.add(CALCULATED_VALUE_IS_BIGGER_OR_EQUAL_THAN);
			calculatedEquationSigns.add(CALCULATED_VALUE_IS_BIGGER_THAN);
			calculatedEquationSigns
					.add(STARTS_ITERATION_FROM_X_UP_TO_Y_AND_RESULT_IS_CURRENT);
			calculatedEquationSigns.add(
					RuleEquationSignTypes.STARTS_REVERSE_ITERATION_FROM_X_DOWN_TO_Y_AND_RESULT_IS_CURRENT);
		}

		if (calculatedEquationSigns.contains(this)) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean isIteratorEquationSignType() {
		// Create list at first check
		if (iteratorEquationSigns == null) {
			iteratorEquationSigns = new ArrayList<RuleEquationSignTypes>();
			iteratorEquationSigns
					.add(STARTS_ITERATION_FROM_X_UP_TO_Y_AND_RESULT_IS_CURRENT);
			iteratorEquationSigns.add(
					RuleEquationSignTypes.STARTS_REVERSE_ITERATION_FROM_X_DOWN_TO_Y_AND_RESULT_IS_CURRENT);
		}

		if (iteratorEquationSigns.contains(this)) {
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

	public synchronized boolean isACrossInterventionVariableComparisionBasedEquationSignType() {
		if (this == CHECK_VALUE_IN_VARIABLE_ACROSS_INVTERVENTIONS_AND_TRUE_IF_DUPLICATE_FOUND) {
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean isJavaScriptBasedRule() {
		if (this == RuleEquationSignTypes.EXECUTE_JAVASCRIPT_IN_X_AND_STORE_VALUES_BUT_RESULT_IS_ALWAYS_TRUE) {
			return true;
		} else {
			return false;
		}
	}
}
