package org.isgf.mhc.model.persistent;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.persistent.concepts.AbstractMonitoringRule;
import org.isgf.mhc.model.persistent.types.RuleEquationSignTypes;

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
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final ObjectId isSubRuleOfMonitoringRule, final int order,
			final String storeValueToVariableWithName,
			final boolean sendMessageIfTrue,
			final ObjectId relatedMonitoringMessageGroup,
			final ObjectId intervention, final int hourToSendMessage,
			final int hoursUntilMessageIsHandledAsUnanswered,
			final boolean stopInterventionWhenTrue) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, isSubRuleOfMonitoringRule,
				order, storeValueToVariableWithName, sendMessageIfTrue,
				relatedMonitoringMessageGroup);

		this.intervention = intervention;
		this.hourToSendMessage = hourToSendMessage;
		this.hoursUntilMessageIsHandledAsUnanswered = hoursUntilMessageIsHandledAsUnanswered;
		this.stopInterventionWhenTrue = stopInterventionWhenTrue;
	}

	/**
	 * {@link Intervention} to which this {@link MonitoringRule} belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	intervention;

	/**
	 * <strong>OPTIONAL if sendMassgeIfTrue is false:</strong> The hour the
	 * message should be sent
	 */
	@Getter
	@Setter
	private int			hourToSendMessage;

	/**
	 * <strong>OPTIONAL if sendMassgeIfTrue is false:</strong> The hours a
	 * {@link Participant} has to answer the message before it's handled as
	 * unanswered
	 */
	@Getter
	@Setter
	private int			hoursUntilMessageIsHandledAsUnanswered;

	/**
	 * <strong>OPTIONAL:</strong> The intervention will be set to finished for
	 * participant and rule execution will stop when the rule evaluates to true
	 */
	@Getter
	@Setter
	private boolean		stopInterventionWhenTrue;

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

		// Add monitoring reply rule
		for (val monitoringReplyRule : ModelObject.find(
				MonitoringReplyRule.class,
				Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE, getId(),
				getId())) {
			monitoringReplyRule
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		// Delete related reply rules
		val monitoringReplyRulesToDelete = ModelObject.find(
				MonitoringReplyRule.class,
				Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE, getId(),
				getId());
		ModelObject.delete(monitoringReplyRulesToDelete);

		// Delete sub rules
		val monitoringRulesToDelete = ModelObject.find(MonitoringRule.class,
				Queries.MONITORING_RULE__BY_PARENT, getId());
		ModelObject.delete(monitoringRulesToDelete);
	}
}
