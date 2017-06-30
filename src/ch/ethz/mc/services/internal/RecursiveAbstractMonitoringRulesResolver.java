package ch.ethz.mc.services.internal;

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
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MonitoringMessage;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.concepts.AbstractMonitoringRule;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.tools.RuleEvaluator;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.tools.VariableStringReplacer;

/**
 * Helps to recursively resolve tree-based rule structures of
 * {@link AbstractMonitoringRule}s and to determine the messages that should be
 * send to a {@link Participant}
 *
 * @author Andreas Filler
 */
@Log4j2
public class RecursiveAbstractMonitoringRulesResolver {
	// General objects
	private final InterventionExecutionManagerService	interventionExecutionManagerService;
	private final DatabaseManagerService				databaseManagerService;
	private final VariablesManagerService				variablesManagerService;

	// Internal objects
	private boolean										completelyStop											= false;

	// Relevant for both cases
	private final Participant							participant;

	// Gives information if the whole class should handle MonitoringRules or
	// MonitoringReplyRules
	private final boolean								isMonitoringRule;

	// Only relevant for MonitoringRules
	private final Intervention							intervention;

	// Only relevant for MonitoringReplyRules
	private MonitoringMessage							relatedMonitoringMessageForReplyRuleCase				= null;
	private MonitoringRule								relatedMonitoringRuleForReplyRuleCase					= null;
	private final boolean								monitoringReplyRuleCaseIsTrue;

	/*
	 * Values to resolve related to intervention process
	 */

	// Only relevant for MonitoringRules
	private boolean										interventionIsFinishedForParticipantAfterThisResolving	= false;

	/*
	 * Helper classes containing resolved values about messages to send
	 */

	private abstract class AbstractMessageSendingResultForAbstractMontoringRule {
		@Getter
		@Setter
		private String					messageTextToSend								= "";

		@Getter
		@Setter
		private MonitoringMessage		monitoringMessageToSend							= null;

		@Getter
		@Setter
		private AbstractMonitoringRule	abstractMonitoringRuleRequiredToPrepareMessage	= null;

		@Getter
		@Setter
		private boolean					monitoringRuleExpectsAnswer						= false;
	}

	public class MessageSendingResultForMonitoringRule extends
			AbstractMessageSendingResultForAbstractMontoringRule {
	}

	public class MessageSendingResultForMonitoringReplyRule extends
			AbstractMessageSendingResultForAbstractMontoringRule {
	}

	/*
	 * Values to resolve
	 */
	@Getter
	private final List<MessageSendingResultForMonitoringRule>		messageSendingResultForMonitoringRules		= new ArrayList<RecursiveAbstractMonitoringRulesResolver.MessageSendingResultForMonitoringRule>();
	@Getter
	private final List<MessageSendingResultForMonitoringReplyRule>	messageSendingResultForMonitoringReplyRules	= new ArrayList<RecursiveAbstractMonitoringRulesResolver.MessageSendingResultForMonitoringReplyRule>();

	public RecursiveAbstractMonitoringRulesResolver(
			final InterventionExecutionManagerService interventionExecutionManagerService,
			final DatabaseManagerService databaseManagerService,
			final VariablesManagerService variablesManagerService,
			final Participant participant, final boolean isMonitoringRule,
			final ObjectId relatedMonitoringMessageForReplyRuleCase,
			final ObjectId relatedMonitoringRuleForReplyRuleCase,
			final boolean monitoringReplyRuleCase) {
		this.interventionExecutionManagerService = interventionExecutionManagerService;
		this.databaseManagerService = databaseManagerService;
		this.variablesManagerService = variablesManagerService;

		this.participant = participant;

		intervention = databaseManagerService.getModelObjectById(
				Intervention.class, participant.getIntervention());

		this.isMonitoringRule = isMonitoringRule;
		if (!isMonitoringRule) {
			this.relatedMonitoringMessageForReplyRuleCase = databaseManagerService
					.getModelObjectById(MonitoringMessage.class,
							relatedMonitoringMessageForReplyRuleCase);
			this.relatedMonitoringRuleForReplyRuleCase = databaseManagerService
					.getModelObjectById(MonitoringRule.class,
							relatedMonitoringRuleForReplyRuleCase);
		}
		monitoringReplyRuleCaseIsTrue = monitoringReplyRuleCase;
	}

	public void resolve() {
		// Recursively check all rules
		executeRules(null);

		final List<? extends AbstractMessageSendingResultForAbstractMontoringRule> resultsToCreateMessagesFor;

		if (isMonitoringRule) {
			resultsToCreateMessagesFor = messageSendingResultForMonitoringRules;
		} else {
			resultsToCreateMessagesFor = messageSendingResultForMonitoringReplyRules;
		}

		for (val resultToCreateMessageFor : resultsToCreateMessagesFor) {
			if (resultToCreateMessageFor
					.getAbstractMonitoringRuleRequiredToPrepareMessage()
					.getRelatedMonitoringMessageGroup() == null) {
				log.warn(
						"There is no message group defined in rule {} to send a message to participant {}",
						resultToCreateMessageFor
								.getAbstractMonitoringRuleRequiredToPrepareMessage(),
						participant.getId());

				resultToCreateMessageFor.setMessageTextToSend(null);

				continue;
			}

			// Determine message to send by checking message groups for already
			// sent messages
			val monitoringMessageGroup = databaseManagerService
					.getModelObjectById(
							MonitoringMessageGroup.class,
							resultToCreateMessageFor
									.getAbstractMonitoringRuleRequiredToPrepareMessage()
									.getRelatedMonitoringMessageGroup());

			if (monitoringMessageGroup == null) {
				log.warn(
						"The monitoring message group {} for participant {} could not be found",
						resultToCreateMessageFor
								.getAbstractMonitoringRuleRequiredToPrepareMessage()
								.getRelatedMonitoringMessageGroup(),
						participant.getId());

				continue;
			}

			resultToCreateMessageFor
					.setMonitoringRuleExpectsAnswer(monitoringMessageGroup
							.isMessagesExpectAnswer());

			val determinedMonitoringMessageToSend = interventionExecutionManagerService
					.determineMessageOfMessageGroupToSend(participant,
							monitoringMessageGroup,
							relatedMonitoringMessageForReplyRuleCase,
							isMonitoringRule);
			if (determinedMonitoringMessageToSend == null) {
				log.warn(
						"There are no more messages left in message group {} to send a message to participant {}",
						resultToCreateMessageFor
								.getAbstractMonitoringRuleRequiredToPrepareMessage()
								.getRelatedMonitoringMessageGroup(),
						participant.getId());

				resultToCreateMessageFor.setMessageTextToSend(null);

				continue;
			}

			// Remember message that will be sent
			resultToCreateMessageFor
					.setMonitoringMessageToSend(determinedMonitoringMessageToSend);

			// Determine message text to send
			val variablesWithValues = variablesManagerService
					.getAllVariablesWithValuesOfParticipantAndSystem(
							participant, determinedMonitoringMessageToSend);
			val messageTextToSend = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(participant
							.getLanguage(), determinedMonitoringMessageToSend
							.getTextWithPlaceholders().get(participant),
							variablesWithValues.values(), "");

			resultToCreateMessageFor.setMessageTextToSend(messageTextToSend);
		}
	}

	/**
	 * Recursively walks through rules until one rule matches
	 *
	 * @param parent
	 * @throws Exception
	 */
	private void executeRules(final AbstractMonitoringRule parent) {
		// Start with the whole process
		Iterable<? extends AbstractMonitoringRule> rulesOnCurrentLevel;
		if (parent == null) {
			if (isMonitoringRule) {
				rulesOnCurrentLevel = databaseManagerService
						.findSortedModelObjects(
								MonitoringRule.class,
								Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
								Queries.MONITORING_RULE__SORT_BY_ORDER_ASC,
								intervention.getId(), null);
			} else {
				if (monitoringReplyRuleCaseIsTrue) {
					rulesOnCurrentLevel = databaseManagerService
							.findSortedModelObjects(
									MonitoringReplyRule.class,
									Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER,
									Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC,
									relatedMonitoringRuleForReplyRuleCase
											.getId(), null);
				} else {
					rulesOnCurrentLevel = databaseManagerService
							.findSortedModelObjects(
									MonitoringReplyRule.class,
									Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
									Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC,
									relatedMonitoringRuleForReplyRuleCase
											.getId(), null);
				}
			}
		} else {
			if (isMonitoringRule) {
				rulesOnCurrentLevel = databaseManagerService
						.findSortedModelObjects(
								MonitoringRule.class,
								Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
								Queries.MONITORING_RULE__SORT_BY_ORDER_ASC,
								intervention.getId(), parent.getId());
			} else {
				if (monitoringReplyRuleCaseIsTrue) {
					rulesOnCurrentLevel = databaseManagerService
							.findSortedModelObjects(
									MonitoringReplyRule.class,
									Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER,
									Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC,
									relatedMonitoringRuleForReplyRuleCase
											.getId(), parent.getId());
				} else {
					rulesOnCurrentLevel = databaseManagerService
							.findSortedModelObjects(
									MonitoringReplyRule.class,
									Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
									Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC,
									relatedMonitoringRuleForReplyRuleCase
											.getId(), parent.getId());
				}
			}
		}

		// Execute all rules on this level
		for (val nextRule : rulesOnCurrentLevel) {
			// There are some more rules on this level to execute so do it

			val lookIntoChildren = executeRule(nextRule);

			// If it was the final rule, then stop the whole iteration
			if (completelyStop) {
				return;
			}

			if (lookIntoChildren) {
				log.debug("Check children");
				// Check children (recursion)
				executeRules(nextRule);

				// If one of the children decided to cancel execution, stop
				// execution, otherwise go on
				if (completelyStop) {
					return;
				}
			}
		}

		return;
	}

	/**
	 * Executes an {@link AbstractMonitoringRule} and returns if the result
	 * should be the last to call for this run
	 *
	 * @param rule
	 * @return
	 * @throws Exception
	 */
	private boolean executeRule(final AbstractMonitoringRule rule) {
		val variablesWithValues = variablesManagerService
				.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		val ruleResult = RuleEvaluator.evaluateRule(participant.getId(),
				participant.getLanguage(), rule, variablesWithValues.values());

		if (!ruleResult.isEvaluatedSuccessful()) {
			log.error("Error when validating rule {} of participant {}: {}",
					rule.getId(), participant.getId(),
					ruleResult.getErrorMessage());
			log.error("Stopping rule execution for participant {}",
					participant.getId());

			return false;
		}

		log.debug("Checked rule and result is {}",
				ruleResult.isRuleMatchesEquationSign());

		// Store result if it should be stored
		if (rule.getStoreValueToVariableWithName() != null
				&& !rule.getStoreValueToVariableWithName().equals("")) {
			try {
				variablesManagerService.writeVariableValueOfParticipant(
						participant.getId(),
						rule.getStoreValueToVariableWithName(),
						ruleResult.isCalculatedRule() ? StringHelpers
								.cleanDoubleValue(ruleResult
										.getCalculatedRuleValue()) : ruleResult
								.getTextRuleValue());
			} catch (final Exception e) {
				log.warn("Could not write variable value: {}", e.getMessage());
			}
		}

		if (ruleResult.isRuleMatchesEquationSign()
				&& rule.isSendMessageIfTrue()) {
			// Sending message
			log.debug("Rule will send message!");

			if (isMonitoringRule) {
				final MessageSendingResultForMonitoringRule result = new MessageSendingResultForMonitoringRule();

				result.setAbstractMonitoringRuleRequiredToPrepareMessage(rule);

				messageSendingResultForMonitoringRules.add(result);
			} else {
				final MessageSendingResultForMonitoringReplyRule result = new MessageSendingResultForMonitoringReplyRule();

				result.setAbstractMonitoringRuleRequiredToPrepareMessage(rule);

				messageSendingResultForMonitoringReplyRules.add(result);
			}
		} else if (isMonitoringRule && ruleResult.isRuleMatchesEquationSign()
				&& ((MonitoringRule) rule).isStopInterventionWhenTrue()) {
			// Stop execution when rule is finalizing rule
			log.debug("Rule will stop intervention for participant!");

			interventionIsFinishedForParticipantAfterThisResolving = true;

			completelyStop = true;
		}

		return ruleResult.isRuleMatchesEquationSign();
	}

	/*
	 * Methods to return results
	 */
	public boolean isInterventionFinishedForParticipantAfterThisResolving() {
		return interventionIsFinishedForParticipantAfterThisResolving;
	}
}
