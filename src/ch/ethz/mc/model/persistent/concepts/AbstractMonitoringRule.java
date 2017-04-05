package ch.ethz.mc.model.persistent.concepts;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
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
import lombok.Setter;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;

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
