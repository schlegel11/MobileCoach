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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.concepts.AbstractMonitoringRule;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.tools.StringHelpers;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
			final String comment, final ObjectId isSubRuleOfMonitoringRule,
			final int order, final String storeValueToVariableWithName,
			final boolean sendMessageIfTrue,
			final ObjectId relatedMonitoringMessageGroup,
			final ObjectId isGotAnswerRuleForMonitoringRule,
			final ObjectId isGotNoAnswerRuleForMonitoringRule) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, comment,
				isSubRuleOfMonitoringRule, order, storeValueToVariableWithName,
				sendMessageIfTrue, relatedMonitoringMessageGroup);

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
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	public void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}

	@JsonIgnore
	public String toTable(final int level, final ObjectId monitoringRuleId,
			final boolean replyRuleCase) {
		val style = level > 0 ? "border-left-width: " + 20 * level + "px;" : "";

		String table = wrapRow(wrapHeader("Rule:", style)
				+ wrapField(escape(StringHelpers.createRuleName(this, false))));
		table += wrapRow(wrapHeader("Comment:", style)
				+ wrapField(escape(getComment())));

		table += wrapRow(wrapHeader("Variable to store value to:", style)
				+ wrapField(escape(getStoreValueToVariableWithName())));
		table += wrapRow(wrapHeader("Send message when TRUE:", style)
				+ wrapField(formatYesNo(isSendMessageIfTrue())));

		if (getRelatedMonitoringMessageGroup() != null) {
			val messageGroup = ModelObject.get(MonitoringMessageGroup.class,
					getRelatedMonitoringMessageGroup());
			if (messageGroup != null) {
				table += wrapRow(wrapHeader(
						"Monitoring Message Group to send from:", style)
						+ wrapField(escape(messageGroup.getName())));
			} else {
				table += wrapRow(wrapHeader(
						"Monitoring Message Group to send from:", style)
						+ wrapField(formatWarning(
								"Message Group set, but not found")));
			}
		}

		// Sub Rules
		val subReplyRules = ModelObject.findSorted(MonitoringReplyRule.class,
				replyRuleCase
						? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER
						: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
				Queries.MONITORING_RULE__SORT_BY_ORDER_ASC, monitoringRuleId,
				getId());

		final StringBuffer buffer = new StringBuffer();
		for (val subReplyRule : subReplyRules) {
			buffer.append(subReplyRule.toTable(level + 1, monitoringRuleId,
					replyRuleCase));
		}

		if (buffer.length() > 0) {
			return wrapTable(table) + buffer.toString();
		} else {
			return wrapTable(table);
		}
	}
}
