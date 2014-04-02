package org.isgf.mhc.model.persistent.concepts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.persistent.types.RuleEquationSignTypes;

/**
 * {@link ModelObject} to represent a rule
 * 
 * An {@link AbstractRule} consists of the rule itself, an equation sign and a
 * comparison term. The rule can be evaluated and checked against the comparison
 * term based on the equation sign.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractRule extends ModelObject {
	/**
	 * Rule containing placeholders for variables
	 */
	@Getter
	@Setter
	@NonNull
	private String				ruleWithPlaceholders;

	/**
	 * Equation sign to compare the rule with the rule comparison term
	 */
	@Getter
	@Setter
	@NonNull
	private RuleEquationSignTypes	ruleEquationSign;

	/**
	 * The term containing placeholders to compare the rule with
	 */
	@Getter
	@Setter
	@NonNull
	private String				ruleComparisonTermWithPlaceholders;
}
