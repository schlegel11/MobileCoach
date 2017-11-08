package ch.ethz.mc.model.persistent.outdated;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.types.MonitoringRuleTypes;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;

/**
 * CAUTION: Will only be used for conversion from data model 11 to 12
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
public class MonitoringRuleV12 extends AbstractMonitoringRuleV12 {
	/**
	 * Default constructor
	 */
	public MonitoringRuleV12(final String ruleWithPlaceholders,
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final String comment, final ObjectId isSubRuleOfMonitoringRule,
			final int order, final String storeValueToVariableWithName,
			final boolean sendMessageIfTrue,
			final ObjectId relatedMonitoringMessageGroup,
			final MonitoringRuleTypes type, final ObjectId intervention,
			final int hourToSendMessage,
			final int hoursUntilMessageIsHandledAsUnanswered,
			final boolean stopInterventionWhenTrue) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, comment,
				isSubRuleOfMonitoringRule, order, storeValueToVariableWithName,
				sendMessageIfTrue, relatedMonitoringMessageGroup);

		this.type = type;
		this.intervention = intervention;
		this.hourToSendMessage = hourToSendMessage;
		this.hoursUntilMessageIsHandledAsUnanswered = hoursUntilMessageIsHandledAsUnanswered;
		this.stopInterventionWhenTrue = stopInterventionWhenTrue;
	}

	/**
	 * The type of the {@link MonitoringRuleV12}
	 */
	@Getter
	@Setter
	@NonNull
	private MonitoringRuleTypes	type;

	/**
	 * {@link Intervention} to which this {@link MonitoringRuleV12} belongs to
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
	 * <strong>OPTIONAL if sendMessageIfTrue is false:</strong> The hours a
	 * {@link Participant} has to answer the message before it's handled as
	 * unanswered
	 */
	@Getter
	@Setter
	private int					hoursUntilMessageIsHandledAsUnanswered;

	/**
	 * <strong>OPTIONAL:</strong> The intervention will be set to finished for
	 * participant and rule execution will stop when the rule evaluates to true
	 */
	@Getter
	@Setter
	private boolean				stopInterventionWhenTrue;
}
