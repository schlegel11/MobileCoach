package org.isgf.mhc.model.persistent;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.persistent.concepts.AbstractMonitoringRule;
import org.isgf.mhc.model.persistent.types.RuleEquationSignTypes;

/**
 * {@link ModelObject} to represent an {@link MonitoringReplyRule}
 * 
 * A {@link MonitoringReplyRule} is the core aspect in decision making in this
 * system. The {@link MonitoringReplyRule}s are executed step by step regarding
 * their order and level. Each {@link MonitoringReplyRule} can be defined in a
 * way
 * that it stores the result of the rule in a variable and/or if it shall send a
 * message.
 * 
 * A {@link MonitoringReplyRule} always belongs to a {@link MonitoringRule} to
 * be executed on answer or no answer
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public class MonitoringReplyRule extends AbstractMonitoringRule {
	/**
	 * Default constructor
	 */
	public MonitoringReplyRule(final String ruleWithPlaceholders,
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final ObjectId isSubRuleOfMonitoringRule, final int order,
			final String storeValueToVariableWithName,
			final boolean sendMessageIfTrue,
			final ObjectId relatedMonitoringMessageGroup,
			final ObjectId isGotAnswerRuleForMonitoringRule,
			final ObjectId isGotNoAnswerRuleForMonitoringRule) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, isSubRuleOfMonitoringRule,
				order, storeValueToVariableWithName, sendMessageIfTrue,
				relatedMonitoringMessageGroup);

		this.isGotAnswerRuleForMonitoringRule = isGotAnswerRuleForMonitoringRule;
		this.isGotNoAnswerRuleForMonitoringRule = isGotNoAnswerRuleForMonitoringRule;
	}

	/**
	 * <strong>OPTIONAL:</strong> Belongs to the mentioned
	 * {@link MonitoringRule} and will be executed in
	 * case of an answer by the {@link Participant}
	 */
	@Getter
	@Setter
	private ObjectId	isGotAnswerRuleForMonitoringRule;

	/**
	 * <strong>OPTIONAL:</strong> Belongs to the mentioned
	 * {@link MonitoringRule} and will be executed in
	 * case of no answer by the {@link Participant}
	 */
	@Getter
	@Setter
	private ObjectId	isGotNoAnswerRuleForMonitoringRule;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.isgf.mhc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}
}
