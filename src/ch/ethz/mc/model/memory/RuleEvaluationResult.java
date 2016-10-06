package ch.ethz.mc.model.memory;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains the results of a rule evaluation
 * 
 * @author Andreas Filler
 */
@ToString
@NoArgsConstructor
public class RuleEvaluationResult {
	/**
	 * Was the system able to evaluate the rule completely
	 */
	@Getter
	@Setter
	private boolean	evaluatedSuccessful					= false;

	/**
	 * The information if the rule evaluation has been performed calculated or
	 * text based
	 */
	@Getter
	@Setter
	private boolean	isCalculatedRule					= false;

	/**
	 * The result of the rule evaluation of calculated rules
	 */
	@Getter
	@Setter
	private double	calculatedRuleValue					= 0;

	/**
	 * The result of the rule comparison term evaluation of calculated rules
	 */
	@Getter
	@Setter
	private double	calculatedRuleComparisonTermValue	= 0;
	/**
	 * The result of the rule evaluation of text rules
	 */
	@Getter
	@Setter
	private String	textRuleValue						= "";

	/**
	 * The result of the rule comparison term evaluation of text rules
	 */
	@Getter
	@Setter
	private String	textRuleComparisonTermValue			= "";

	/**
	 * The result if the equation sign is correct regarding the evaluated values
	 * for the rule and the rule comparison term
	 */
	@Getter
	@Setter
	private boolean	ruleMatchesEquationSign				= false;

	/**
	 * The error message if an error occurred or <code>null</code> if no error
	 * occurred
	 */
	@Getter
	@Setter
	private String	errorMessage						= null;
}
