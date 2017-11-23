package ch.ethz.mc.model.persistent.outdated;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;

/**
 * CAUTION: Will only be used for conversion from data model 11 to 12
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
public abstract class AbstractMonitoringRuleV12 extends AbstractRuleV12 {
	/**
	 * Default constructor
	 */
	public AbstractMonitoringRuleV12(final String ruleWithPlaceholders,
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final String comment, final ObjectId isSubRuleOfMonitoringRule,
			final int order, final String storeValueToVariableWithName,
			final boolean sendMessageIfTrue,
			final ObjectId relatedMonitoringMessageGroup) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, comment);

		this.isSubRuleOfMonitoringRule = isSubRuleOfMonitoringRule;
		this.order = order;
		this.storeValueToVariableWithName = storeValueToVariableWithName;
		this.sendMessageIfTrue = sendMessageIfTrue;
		this.relatedMonitoringMessageGroup = relatedMonitoringMessageGroup;
	}

	/**
	 * <strong>OPTIONAL:</strong> If the {@link AbstractMonitoringRuleV12} is
	 * nested below another {@link AbstractMonitoringRuleV12} the father has to
	 * be referenced here
	 */
	@Getter
	@Setter
	private ObjectId	isSubRuleOfMonitoringRule;

	/**
	 * The position of the {@link AbstractMonitoringRuleV12} compared to all
	 * other {@link AbstractMonitoringRuleV12}s on the same level
	 */
	@Getter
	@Setter
	private int			order;

	/**
	 * <strong>OPTIONAL:</strong> If the result of the
	 * {@link AbstractMonitoringRuleV12} should be stored, the name of the
	 * appropriate variable can be set here.
	 */
	@Getter
	@Setter
	private String		storeValueToVariableWithName;

	/**
	 * <strong>OPTIONAL:</strong> If the result of the
	 * {@link AbstractMonitoringRuleV12} is true, a message will be send if this
	 * is true
	 */
	@Getter
	@Setter
	private boolean		sendMessageIfTrue;

	/**
	 * <strong>OPTIONAL:</strong> If set the message will not be sent to the
	 * participant but to the supervisor
	 */
	@Getter
	@Setter
	private boolean		sendMessageToSupervisor;

	/**
	 * <strong>OPTIONAL if sendMassgeIfTrue is false:</strong> The
	 * {@link MonitoringMessageGroup} a message should be send from
	 */
	@Getter
	@Setter
	private ObjectId	relatedMonitoringMessageGroup;
}
