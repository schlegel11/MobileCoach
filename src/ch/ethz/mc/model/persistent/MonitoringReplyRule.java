package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.concepts.AbstractMonitoringRule;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;

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
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}
}
