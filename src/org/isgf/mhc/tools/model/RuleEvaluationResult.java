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
	@Getter
	@Setter
	private boolean	evaluatedSuccessful			= false;

	@Getter
	@Setter
	private double	ruleValue					= 0;

	@Getter
	@Setter
	private double	ruleComparisionTermValue	= 0;

	@Getter
	@Setter
	private boolean	ruleMatchesEquationSign		= false;

	@Getter
	@Setter
	private String	errorMessage				= null;
}
