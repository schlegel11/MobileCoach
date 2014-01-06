package org.isgf.mhc.model.server;

import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.concepts.AbstractRule;
import org.isgf.mhc.model.types.EquationSignTypes;
import org.jongo.Oid;

/**
 * {@link ModelObject} to represent an {@link InterventionRule}
 * 
 * @author Andreas Filler <andreas@filler.name>
 */
public class InterventionRule extends AbstractRule {
	public InterventionRule(final String ruleWithPlaceholders,
			final EquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final Oid isSubInterventionRuleOfInterventionRule) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders);
		this.isSubInterventionRuleOfInterventionRule = isSubInterventionRuleOfInterventionRule;
	}

	private final Oid	isSubInterventionRuleOfInterventionRule;
}
