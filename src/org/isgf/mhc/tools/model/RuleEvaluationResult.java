package org.isgf.mhc.tools.model;

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
	private boolean	evaluatedSuccessful		= false;

	/**
	 * The result of the rule evaluation
	 */
	@Getter
	@Setter
	private double	ruleValue				= 0;

	/**
	 * The result of the rule comparison term evaluation
	 */
	@Getter
	@Setter
	private double	ruleComparisonTermValue	= 0;

	/**
	 * The result if the equation sign is correct regarding the evaluated values
	 * for the rule and the rule comparison term
	 */
	@Getter
	@Setter
	private boolean	ruleMatchesEquationSign	= false;

	/**
	 * The error message if an error occurred or <code>null</code> if no error
	 * occurred
	 */
	@Getter
	@Setter
	private String	errorMessage			= null;
}
