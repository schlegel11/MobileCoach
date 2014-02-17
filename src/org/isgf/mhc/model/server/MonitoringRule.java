package org.isgf.mhc.model.server;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.concepts.AbstractMonitoringRule;
import org.isgf.mhc.model.server.types.EquationSignTypes;

/**
 * {@link ModelObject} to represent an {@link MonitoringRule}
 * 
 * A {@link MonitoringRule} is the core aspect in decision making in this
 * system. The {@link MonitoringRule}s are executed step by step regarding
 * their order and level. Each {@link MonitoringRule} can be defined in a way
 * that it stores the result of the rule in a variable and/or if it shall send a
 * message.
 * 
 * A {@link MonitoringRule} also contains information about how to react
 * regarding messages to send to a {@link Participant}
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public class MonitoringRule extends AbstractMonitoringRule {
	/**
	 * Default constructor
	 */
	public MonitoringRule(final String ruleWithPlaceholders,
			final EquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final ObjectId isSubInterventionRuleOfInterventionRule,
			final int order, final String storeValueToVariableWithName,
			final boolean sendMessageIfTrue,
			final ObjectId relatedMonitoringMessageGroup,
			final int hourToSendMessageVariable,
			final int hoursUntilMessageIsHandledAsUnanswered) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders,
				isSubInterventionRuleOfInterventionRule, order,
				storeValueToVariableWithName, sendMessageIfTrue,
				relatedMonitoringMessageGroup);

		this.hourToSendMessageVariable = hourToSendMessageVariable;
		this.hoursUntilMessageIsHandledAsUnanswered = hoursUntilMessageIsHandledAsUnanswered;
	}

	/**
	 * <strong>OPTIONAL if sendMassgeIfTrue is false:</strong> The hour the
	 * message should be sent
	 */
	@Getter
	@Setter
	private int	hourToSendMessageVariable;

	/**
	 * <strong>OPTIONAL if sendMassgeIfTrue is false:</strong> The hours a
	 * {@link Participant} has to answer the message before it's handled as
	 * unanswered
	 */
	@Getter
	@Setter
	private int	hoursUntilMessageIsHandledAsUnanswered;
}
