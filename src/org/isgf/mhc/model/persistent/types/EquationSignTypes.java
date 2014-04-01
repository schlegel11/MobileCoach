package org.isgf.mhc.model.persistent.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Supported {@link EquationSignTypes}
 * 
 * @author Andreas Filler
 */
public enum EquationSignTypes {
	CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE, CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_FALSE, CALCULATED_VALUE_IS_SMALLER_THAN, CALCULATED_VALUE_IS_SMALLER_OR_EQUAL_THAN, CALCULATED_VALUE_EQUALS, CALCULATED_VALUE_IS_BIGGER_OR_EQUAL_THAN, CALCULATED_VALUE_IS_BIGGER_THAN, CREATE_TEXT_BUT_RESULT_IS_ALWAYS_TRUE, CREATE_TEXT_BUT_RESULT_IS_ALWAYS_FALSE, TEXT_VALUE_EQUALS, TEXT_VALUE_NOT_EQUALS;

	private static List<EquationSignTypes>	calculatedEquationSigns	= null;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}

	public synchronized boolean isCalculatedEquationSignType() {
		if (calculatedEquationSigns == null) {
			calculatedEquationSigns = new ArrayList<EquationSignTypes>();
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
}
