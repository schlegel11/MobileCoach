package ch.ethz.mc.model.persistent.types;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health IS-Lab
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
