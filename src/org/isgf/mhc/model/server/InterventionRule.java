package org.isgf.mhc.model.server;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.concepts.AbstractRule;
import org.isgf.mhc.model.types.EquationSignTypes;

/**
 * {@link ModelObject} to represent an {@link InterventionRule}
 * 
 * A {@link InterventionRule} is the core aspect in decision making in this
 * system. The {@link InterventionRule}s are executed step by step regarding
 * their order and level. Each {@link InterventionRule} can be defined in a way
 * that it stores the result of the rule in a variable and/or if it shall send a
 * message.
 * 
 * @author Andreas Filler
 */
public class InterventionRule extends AbstractRule {
	/**
	 * Default constructor
	 */
	public InterventionRule(
			final ObjectId isSubInterventionRuleOfInterventionRule,
			final int order, final String storeValueToVariableWithName,
			final boolean sendMessageIfTrue, final String ruleWithPlaceholders,
			final EquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders);

		this.isSubInterventionRuleOfInterventionRule = isSubInterventionRuleOfInterventionRule;
		this.order = order;
		this.storeValueToVariableWithName = storeValueToVariableWithName;
		this.sendMessageIfTrue = sendMessageIfTrue;
	}

	/**
	 * <strong>OPTIONAL:</strong> If the {@link InterventionRule} is nested
	 * below another {@link InterventionRule} the father has to be referenced
	 * here
	 */
	@Getter
	@Setter
	private ObjectId	isSubInterventionRuleOfInterventionRule;

	/**
	 * The position of the {@link InterventionRule} compared to all other
	 * {@link InterventionRule}s on the same level
	 */
	@Getter
	@Setter
	private int			order;

	/**
	 * <strong>OPTIONAL:</strong> If the result of the {@link InterventionRule}
	 * should be stored, the name of the appropriate variable can be set here.
	 */
	@Getter
	@Setter
	private String		storeValueToVariableWithName;

	/**
	 * <strong>OPTIONAL:</strong> If the result of the {@link InterventionRule}
	 * is true, a message from the defined {@link InterventionMessageGroup} will
	 * be send, if the value is set
	 */
	@Getter
	@Setter
	private boolean		sendMessageIfTrue;
}
