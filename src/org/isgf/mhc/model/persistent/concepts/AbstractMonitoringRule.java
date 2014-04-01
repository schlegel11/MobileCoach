package org.isgf.mhc.model.persistent.concepts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.persistent.MonitoringMessageGroup;
import org.isgf.mhc.model.persistent.types.EquationSignTypes;

/**
 * {@link ModelObject} to represent an {@link AbstractMonitoringRule}
 * 
 * A {@link AbstractMonitoringRule} is the core aspect in decision making in
 * this
 * system. The {@link AbstractMonitoringRule}s are executed step by step
 * regarding
 * their order and level. Each {@link AbstractMonitoringRule} can be defined in
 * a
 * way that it stores the result of the rule in a variable and/or if it shall
 * send a
 * message.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public abstract class AbstractMonitoringRule extends AbstractRule {
	/**
	 * Default constructor
	 */
	public AbstractMonitoringRule(final String ruleWithPlaceholders,
			final EquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final ObjectId isSubRuleOfMonitoringRule, final int order,
			final String storeValueToVariableWithName,
			final boolean sendMessageIfTrue,
			final ObjectId relatedMonitoringMessageGroup) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders);

		this.isSubRuleOfMonitoringRule = isSubRuleOfMonitoringRule;
		this.order = order;
		this.storeValueToVariableWithName = storeValueToVariableWithName;
		this.sendMessageIfTrue = sendMessageIfTrue;
		this.relatedMonitoringMessageGroup = relatedMonitoringMessageGroup;
	}

	/**
	 * <strong>OPTIONAL:</strong> If the {@link AbstractMonitoringRule} is
	 * nested
	 * below another {@link AbstractMonitoringRule} the father has to be
	 * referenced
	 * here
	 */
	@Getter
	@Setter
	private ObjectId	isSubRuleOfMonitoringRule;

	/**
	 * The position of the {@link AbstractMonitoringRule} compared to all other
	 * {@link AbstractMonitoringRule}s on the same level
	 */
	@Getter
	@Setter
	private int			order;

	/**
	 * <strong>OPTIONAL:</strong> If the result of the
	 * {@link AbstractMonitoringRule} should be stored, the name of the
	 * appropriate
	 * variable can be set here.
	 */
	@Getter
	@Setter
	private String		storeValueToVariableWithName;

	/**
	 * <strong>OPTIONAL:</strong> If the result of the
	 * {@link AbstractMonitoringRule} is
	 * true, a message will be send if this is true
	 */
	@Getter
	@Setter
	private boolean		sendMessageIfTrue;

	/**
	 * <strong>OPTIONAL if sendMassgeIfTrue is false:</strong> The
	 * {@link MonitoringMessageGroup} a message should be send from
	 */
	@Getter
	@Setter
	private ObjectId	relatedMonitoringMessageGroup;
}
