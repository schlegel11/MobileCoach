package ch.ethz.mc.model.persistent;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.concepts.AbstractMonitoringRule;
import ch.ethz.mc.model.persistent.types.MonitoringRuleTypes;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.tools.StringHelpers;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

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
			final String comment, final ObjectId isSubRuleOfMonitoringRule,
			final int order, final String storeValueToVariableWithName,
			final boolean sendMessageIfTrue,
			final boolean sendMessageToSupervisor,
			final ObjectId relatedMonitoringMessageGroup,
			final boolean activateMicroDialogIfTrue,
			final ObjectId relatedMicroDialog, final MonitoringRuleTypes type,
			final ObjectId intervention, final int hourToSendMessage,
			final int minutesUntilMessageIsHandledAsUnanswered,
			final boolean stopInterventionWhenTrue,
			final boolean markCaseAsSolvedWhenTrue) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, comment,
				isSubRuleOfMonitoringRule, order, storeValueToVariableWithName,
				sendMessageIfTrue, sendMessageToSupervisor,
				relatedMonitoringMessageGroup, activateMicroDialogIfTrue,
				relatedMicroDialog);

		this.type = type;
		this.intervention = intervention;
		this.hourToSendMessage = hourToSendMessage;
		this.minutesUntilMessageIsHandledAsUnanswered = minutesUntilMessageIsHandledAsUnanswered;
		this.stopInterventionWhenTrue = stopInterventionWhenTrue;
		this.markCaseAsSolvedWhenTrue = markCaseAsSolvedWhenTrue;
	}

	/**
	 * The type of the {@link MonitoringRule}
	 */
	@Getter
	@Setter
	@NonNull
	private MonitoringRuleTypes	type;

	/**
	 * {@link Intervention} to which this {@link MonitoringRule} belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			intervention;

	/**
	 * <strong>OPTIONAL if sendMessageIfTrue is false:</strong> The hour the
	 * message should be sent
	 */
	@Getter
	@Setter
	private int					hourToSendMessage;

	/**
	 * <strong>OPTIONAL if sendMessageIfTrue is false:</strong> The minutes a
	 * {@link Participant} has to answer the message before it's handled as
	 * unanswered
	 */
	@Getter
	@Setter
	private int					minutesUntilMessageIsHandledAsUnanswered;

	/**
	 * <strong>OPTIONAL:</strong> The intervention will be set to finished for
	 * participant and rule execution will stop when the rule evaluates to true
	 */
	@Getter
	@Setter
	private boolean				stopInterventionWhenTrue;

	/**
	 * <strong>OPTIONAL:</strong> The unexpected incoming message or intention
	 * will be handled as solved when the rule evaluates to true and the rule
	 * execution run will be stopped as well
	 */
	@Getter
	@Setter
	private boolean				markCaseAsSolvedWhenTrue;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	public void collectThisAndRelatedModelObjectsForExport(
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
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		return toTable(0);
	}

	@JsonIgnore
	public String toTable(final int level) {
		val style = level > 0 ? "border-left-width: " + 20 * level + "px;" : "";

		String table = wrapRow(wrapHeader("Rule:", style)
				+ wrapField(escape(StringHelpers.createRuleName(this, false))));
		table += wrapRow(wrapHeader("Comment:", style)
				+ wrapField(escape(getComment())));

		table += wrapRow(wrapHeader("Variable to store value to:", style)
				+ wrapField(escape(getStoreValueToVariableWithName())));
		table += wrapRow(wrapHeader("Send message when TRUE:", style)
				+ wrapField(formatYesNo(isSendMessageIfTrue())));
		table += wrapRow(wrapHeader("Stop Intervention when TRUE:", style)
				+ wrapField(formatYesNo(stopInterventionWhenTrue)));

		if (isSendMessageIfTrue()) {
			table += wrapRow(wrapHeader("Hour to send message:", style)
					+ wrapField(escape(hourToSendMessage + ":00")));
		}

		if (getRelatedMonitoringMessageGroup() != null) {
			val messageGroup = ModelObject.get(MonitoringMessageGroup.class,
					getRelatedMonitoringMessageGroup());
			if (messageGroup != null) {
				table += wrapRow(wrapHeader(
						"Monitoring Message Group to send from:", style)
						+ wrapField(escape(messageGroup.getName())));

				if (messageGroup.isMessagesExpectAnswer()) {
					final int daysUntilMessageIsHandledAsUnanswered = (int) Math
							.floor(getMinutesUntilMessageIsHandledAsUnanswered()
									/ 60 / 24);
					final int hoursWithoutDaysUntilMessageIsHandledAsUnanswered = (int) Math
							.floor(getMinutesUntilMessageIsHandledAsUnanswered()
									/ 60)
							- daysUntilMessageIsHandledAsUnanswered * 24;
					final int minutesWithoutHoursAndDaysUntilMessageIsHandledAsUnanswered = getMinutesUntilMessageIsHandledAsUnanswered()
							- daysUntilMessageIsHandledAsUnanswered * 24 * 60
							- hoursWithoutDaysUntilMessageIsHandledAsUnanswered
									* 60;

					table += wrapRow(wrapHeader(
							"Time until message is handled as unanswered:",
							style)
							+ wrapField(
									escape(daysUntilMessageIsHandledAsUnanswered
											+ " day(s), "
											+ hoursWithoutDaysUntilMessageIsHandledAsUnanswered
											+ " hour(s), "
											+ minutesWithoutHoursAndDaysUntilMessageIsHandledAsUnanswered
											+ " minute(s)")));

					/*
					 * Reply Rules
					 */

					// Reply-Case
					Iterable<MonitoringReplyRule> replyRulesOnRootLevel = ModelObject
							.findSorted(MonitoringReplyRule.class,
									Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER,
									Queries.MONITORING_RULE__SORT_BY_ORDER_ASC,
									getId(), null);

					StringBuffer buffer = new StringBuffer();
					for (val replyRule : replyRulesOnRootLevel) {
						buffer.append(replyRule.toTable(0, getId(), true));
					}

					if (buffer.length() > 0) {
						table += wrapRow(wrapHeader("Reply Rules:", style)
								+ wrapField(buffer.toString()));
					}

					// No-Reply-Case
					replyRulesOnRootLevel = ModelObject.findSorted(
							MonitoringReplyRule.class,
							Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
							Queries.MONITORING_RULE__SORT_BY_ORDER_ASC, getId(),
							null);

					buffer = new StringBuffer();
					for (val replyRule : replyRulesOnRootLevel) {
						buffer.append(replyRule.toTable(0, getId(), false));
					}

					if (buffer.length() > 0) {
						table += wrapRow(wrapHeader("No-Reply Rules:", style)
								+ wrapField(buffer.toString()));
					}
				}
			} else {
				table += wrapRow(wrapHeader(
						"Monitoring Message Group to send from:", style)
						+ wrapField(formatWarning(
								"Message Group set, but not found")));
			}
		}

		// Sub Rules
		val subRules = ModelObject.findSorted(MonitoringRule.class,
				Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
				Queries.MONITORING_RULE__SORT_BY_ORDER_ASC, intervention,
				getId());

		final StringBuffer buffer = new StringBuffer();
		for (val subRule : subRules) {
			buffer.append(subRule.toTable(level + 1));
		}

		if (buffer.length() > 0) {
			return wrapTable(table) + buffer.toString();
		} else {
			return wrapTable(table);
		}
	}
}
