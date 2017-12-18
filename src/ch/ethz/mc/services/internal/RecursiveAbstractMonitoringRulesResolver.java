package ch.ethz.mc.services.internal;

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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.memory.RuleEvaluationResult;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.model.persistent.MicroDialogDecisionPoint;
import ch.ethz.mc.model.persistent.MicroDialogMessage;
import ch.ethz.mc.model.persistent.MicroDialogRule;
import ch.ethz.mc.model.persistent.MonitoringMessage;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.concepts.AbstractMonitoringRule;
import ch.ethz.mc.model.persistent.types.AnswerTypes;
import ch.ethz.mc.model.persistent.types.MonitoringRuleTypes;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.tools.RuleEvaluator;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.tools.VariableStringReplacer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Helps to recursively resolve tree-based rule structures of
 * {@link AbstractMonitoringRule}s and to determine the messages that should be
 * send to a {@link Participant}
 *
 * @author Andreas Filler
 */
@Log4j2
public class RecursiveAbstractMonitoringRulesResolver {
	public static enum EXECUTION_CASE {
		MONITORING_RULES_DAILY,
		MONITORING_RULES_PERIODIC,
		MONITORING_RULES_UNEXPECTED_MESSAGE,
		MONITORING_RULES_USER_INTENTION,
		MONITORING_REPLY_RULES,
		MICRO_DIALOG_DECISION_POINT
	}

	// General objects
	private final InterventionExecutionManagerService			interventionExecutionManagerService;
	private final DatabaseManagerService						databaseManagerService;
	private final VariablesManagerService						variablesManagerService;

	// Internal objects
	private boolean												completelyStop											= false;
	private final Hashtable<String, Integer>					iterationCache;
	private final Hashtable<String, Integer>					iterationLimitCache;

	// Relevant for all cases
	private final Participant									participant;
	private final Intervention									intervention;

	// Gives information which kinds of AbstractMonitoringRules this instance
	// should handle
	private final EXECUTION_CASE								executionCase;
	private boolean												ONE_OF_MONITORING_RULES_CASES							= false;

	// Only relevant for MonitoringReplyRules
	private MonitoringMessage									relatedMonitoringMessageForReplyRuleCase				= null;
	private MonitoringRule										relatedMonitoringRuleForReplyRuleCase					= null;
	private boolean												monitoringReplyRuleCaseIsTrue							= false;

	// Only relevant for MonitoringRules and MonitoringReplyRules
	private List<AbstractMonitoringRule>						abstractMonitoringRulesToCheckForMicroDialogActivation	= null;

	// Only relevant for MicroDialogRules
	private MicroDialogDecisionPoint							relatedMicroDialogDecisionPointForMicroDialogRuleCase	= null;

	/*
	 * Values to resolve related to further intervention processing
	 */

	// Only relevant for MonitoringRules
	@Getter
	private boolean												interventionFinishedForParticipantAfterThisResolving	= false;
	@Getter
	private boolean												caseMarkedAsSolved										= false;
	@Getter
	private List<MessageSendingResultForMonitoringRule>			messageSendingResultForMonitoringRules;

	// Only relevant for MonitorinReplyRules
	@Getter
	private List<MessageSendingResultForMonitoringReplyRule>	messageSendingResultForMonitoringReplyRules;

	// Only relevant for MonitoringRules and MonitoringReplyRules
	@Getter
	private List<MicroDialogActivation>							microDialogsToActivate;

	// Only relevant for MicroDialogRules
	@Getter
	private boolean												leaveDecisionPointWhenTrue								= false;
	@Getter
	private boolean												stopMicroDialogWhenTrue									= false;
	@Getter
	private MicroDialog											nextMicroDialog											= null;
	@Getter
	private MicroDialogMessage									nextMicroDialogMessage									= null;

	/*
	 * Helper classes containing resolved values about messages to send
	 */
	private abstract class AbstractMessageSendingResultForAbstractMontoringRule {
		@Getter
		@Setter
		private String		messageTextToSend	= "";

		@Getter
		@Setter
		private AnswerTypes	answerTypeToSend	= null;

		@Getter
		@Setter
		private String		answerOptionsToSend	= null;

		@Getter
		@Setter
		private boolean		answerExpected		= false;
	}

	private abstract class AbstractMessageSendingResultForMonitoringRulesAndMonitoringReplyRules
			extends AbstractMessageSendingResultForAbstractMontoringRule {
		@Getter
		@Setter
		private MonitoringMessage		monitoringMessageToSend							= null;

		@Getter
		@Setter
		private AbstractMonitoringRule	abstractMonitoringRuleRequiredToPrepareMessage	= null;
	}

	/*
	 * Helper class for micro dialog activation
	 */
	@AllArgsConstructor
	public class MicroDialogActivation {
		@Getter
		@Setter
		private MicroDialog	miroDialogToActivate;

		@Getter
		@Setter
		private int			hourToActivateMicroDialog;
	}

	// Solution for MonitoringRules
	public class MessageSendingResultForMonitoringRule extends
			AbstractMessageSendingResultForMonitoringRulesAndMonitoringReplyRules {
	}

	// Solution Solution for MonitoringReplyRules
	public class MessageSendingResultForMonitoringReplyRule extends
			AbstractMessageSendingResultForMonitoringRulesAndMonitoringReplyRules {
	}

	// Solution Solution for MicroDialogRules
	public class MessageSendingResultForMicroDialogRule
			extends AbstractMessageSendingResultForAbstractMontoringRule {
		@Getter
		@Setter
		private MicroDialogMessage		microDialogMessageToSend						= null;

		@Getter
		@Setter
		private AbstractMonitoringRule	abstractMonitoringRuleRequiredToPrepareMessage	= null;
	}

	public RecursiveAbstractMonitoringRulesResolver(
			final InterventionExecutionManagerService interventionExecutionManagerService,
			final DatabaseManagerService databaseManagerService,
			final VariablesManagerService variablesManagerService,
			final Participant participant, final EXECUTION_CASE executionCase,
			final ObjectId relatedMonitoringMessageForReplyRuleCaseId,
			final ObjectId relatedMonitoringRuleForReplyRuleCaseId,
			final boolean monitoringReplyRuleCase,
			final ObjectId relatedMicroDialogDecisionPointForMicroDialogRuleCaseId) {
		this.interventionExecutionManagerService = interventionExecutionManagerService;
		this.databaseManagerService = databaseManagerService;
		this.variablesManagerService = variablesManagerService;

		this.participant = participant;

		intervention = databaseManagerService.getModelObjectById(
				Intervention.class, participant.getIntervention());

		this.executionCase = executionCase;

		iterationCache = new Hashtable<String, Integer>();
		iterationLimitCache = new Hashtable<String, Integer>();

		switch (executionCase) {
			case MONITORING_RULES_DAILY:
			case MONITORING_RULES_PERIODIC:
			case MONITORING_RULES_UNEXPECTED_MESSAGE:
			case MONITORING_RULES_USER_INTENTION:
				ONE_OF_MONITORING_RULES_CASES = true;
				break;
			case MONITORING_REPLY_RULES:
				ONE_OF_MONITORING_RULES_CASES = false;

				relatedMonitoringMessageForReplyRuleCase = databaseManagerService
						.getModelObjectById(MonitoringMessage.class,
								relatedMonitoringMessageForReplyRuleCaseId);
				relatedMonitoringRuleForReplyRuleCase = databaseManagerService
						.getModelObjectById(MonitoringRule.class,
								relatedMonitoringRuleForReplyRuleCaseId);

				monitoringReplyRuleCaseIsTrue = monitoringReplyRuleCase;

				break;
			case MICRO_DIALOG_DECISION_POINT:
				ONE_OF_MONITORING_RULES_CASES = false;

				relatedMicroDialogDecisionPointForMicroDialogRuleCase = databaseManagerService
						.getModelObjectById(MicroDialogDecisionPoint.class,
								relatedMicroDialogDecisionPointForMicroDialogRuleCaseId);
				break;
		}
	}

	public void resolve() {
		// Prepare result lists
		switch (executionCase) {
			case MONITORING_RULES_DAILY:
			case MONITORING_RULES_PERIODIC:
			case MONITORING_RULES_UNEXPECTED_MESSAGE:
			case MONITORING_RULES_USER_INTENTION:
				messageSendingResultForMonitoringRules = new ArrayList<RecursiveAbstractMonitoringRulesResolver.MessageSendingResultForMonitoringRule>();
				abstractMonitoringRulesToCheckForMicroDialogActivation = new ArrayList<AbstractMonitoringRule>();
				break;
			case MONITORING_REPLY_RULES:
				messageSendingResultForMonitoringReplyRules = new ArrayList<RecursiveAbstractMonitoringRulesResolver.MessageSendingResultForMonitoringReplyRule>();
				abstractMonitoringRulesToCheckForMicroDialogActivation = new ArrayList<AbstractMonitoringRule>();
				break;
			case MICRO_DIALOG_DECISION_POINT:
				abstractMonitoringRulesToCheckForMicroDialogActivation = null;
				break;
		}

		// Recursively check all rules
		executeRules(null);

		// Prepare generation of messages
		List<? extends AbstractMessageSendingResultForMonitoringRulesAndMonitoringReplyRules> resultsToCreateMessagesFor = null;

		switch (executionCase) {
			case MONITORING_RULES_DAILY:
			case MONITORING_RULES_PERIODIC:
			case MONITORING_RULES_UNEXPECTED_MESSAGE:
			case MONITORING_RULES_USER_INTENTION:
				resultsToCreateMessagesFor = messageSendingResultForMonitoringRules;
				break;
			case MONITORING_REPLY_RULES:
				resultsToCreateMessagesFor = messageSendingResultForMonitoringReplyRules;
				break;
			case MICRO_DIALOG_DECISION_POINT:
				resultsToCreateMessagesFor = null;
				break;
		}

		// Create messages to be send out
		if (resultsToCreateMessagesFor != null) {
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

				// Determine message to send by checking message groups for
				// already
				// sent messages
				val monitoringMessageGroup = databaseManagerService
						.getModelObjectById(MonitoringMessageGroup.class,
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

				resultToCreateMessageFor.setAnswerExpected(
						monitoringMessageGroup.isMessagesExpectAnswer());

				val determinedMonitoringMessageToSend = interventionExecutionManagerService
						.determineMessageOfMessageGroupToSend(participant,
								monitoringMessageGroup,
								relatedMonitoringMessageForReplyRuleCase,
								ONE_OF_MONITORING_RULES_CASES);
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
				resultToCreateMessageFor.setMonitoringMessageToSend(
						determinedMonitoringMessageToSend);

				// Determine message text and answer type with options to send
				val variablesWithValues = variablesManagerService
						.getAllVariablesWithValuesOfParticipantAndSystem(
								participant, determinedMonitoringMessageToSend,
								null);
				val messageTextToSend = VariableStringReplacer
						.findVariablesAndReplaceWithTextValues(
								participant.getLanguage(),
								determinedMonitoringMessageToSend
										.getTextWithPlaceholders()
										.get(participant),
								variablesWithValues.values(), "");

				AnswerTypes answerTypeToSend = null;
				String answerOptionsToSend = null;
				if (monitoringMessageGroup.isMessagesExpectAnswer()) {
					answerTypeToSend = determinedMonitoringMessageToSend
							.getAnswerType();

					answerOptionsToSend = StringHelpers
							.parseColonSeparatedMultiLineStringToJSON(
									determinedMonitoringMessageToSend
											.getAnswerOptionsWithPlaceholders(),
									participant.getLanguage(),
									variablesWithValues.values());
				}

				resultToCreateMessageFor
						.setMessageTextToSend(messageTextToSend);
				resultToCreateMessageFor.setAnswerTypeToSend(answerTypeToSend);
				resultToCreateMessageFor
						.setAnswerOptionsToSend(answerOptionsToSend);
			}
		}

		// Determine micro dialogs to activate
		if (abstractMonitoringRulesToCheckForMicroDialogActivation != null) {
			microDialogsToActivate = new ArrayList<MicroDialogActivation>();
			for (val ruleToCheckForMicroDialogActivation : abstractMonitoringRulesToCheckForMicroDialogActivation) {
				val microDialogId = ruleToCheckForMicroDialogActivation
						.getRelatedMicroDialog();

				val microDialog = databaseManagerService
						.getModelObjectById(MicroDialog.class, microDialogId);

				int hourToActivateMicroDialog = 0;

				if (ruleToCheckForMicroDialogActivation instanceof MonitoringRule) {
					hourToActivateMicroDialog = ((MonitoringRule) ruleToCheckForMicroDialogActivation)
							.getHourToSendMessageOrActivateMicroDialog();
				}

				if (microDialog != null) {
					val microDialogActivation = new MicroDialogActivation(
							microDialog, hourToActivateMicroDialog);
					microDialogsToActivate.add(microDialogActivation);
				}
			}
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
		Iterable<? extends AbstractMonitoringRule> rulesOnCurrentLevel = null;
		if (parent == null) {
			// Root of rules tree
			if (ONE_OF_MONITORING_RULES_CASES) {
				// Handle monitoring rules separately due to complexity
				MonitoringRule masterParent = null;

				switch (executionCase) {
					case MONITORING_RULES_DAILY:
						masterParent = databaseManagerService
								.findOneModelObject(MonitoringRule.class,
										Queries.MONITORING_RULE__BY_INTERVENTION_AND_TYPE,
										intervention.getId(),
										MonitoringRuleTypes.DAILY);
						break;
					case MONITORING_RULES_PERIODIC:
						masterParent = databaseManagerService
								.findOneModelObject(MonitoringRule.class,
										Queries.MONITORING_RULE__BY_INTERVENTION_AND_TYPE,
										intervention.getId(),
										MonitoringRuleTypes.PERIODIC);
						break;
					case MONITORING_RULES_UNEXPECTED_MESSAGE:
						masterParent = databaseManagerService
								.findOneModelObject(MonitoringRule.class,
										Queries.MONITORING_RULE__BY_INTERVENTION_AND_TYPE,
										intervention.getId(),
										MonitoringRuleTypes.UNEXPECTED_MESSAGE);
						break;
					case MONITORING_RULES_USER_INTENTION:
						masterParent = databaseManagerService
								.findOneModelObject(MonitoringRule.class,
										Queries.MONITORING_RULE__BY_INTERVENTION_AND_TYPE,
										intervention.getId(),
										MonitoringRuleTypes.USER_INTENTION);
						break;
					case MONITORING_REPLY_RULES:
						log.error(
								"Reply rule request in monitoring rule exection: Should never happen!");
						break;
					case MICRO_DIALOG_DECISION_POINT:
						log.error(
								"Micro dialog rule request in monitoring rule exection: Should never happen!");
						break;
				}

				rulesOnCurrentLevel = databaseManagerService
						.findSortedModelObjects(MonitoringRule.class,
								Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
								Queries.MONITORING_RULE__SORT_BY_ORDER_ASC,
								intervention.getId(), masterParent.getId());
			} else {
				switch (executionCase) {
					case MONITORING_RULES_DAILY:
					case MONITORING_RULES_PERIODIC:
					case MONITORING_RULES_UNEXPECTED_MESSAGE:
					case MONITORING_RULES_USER_INTENTION:
						// Already solved above
						break;
					case MONITORING_REPLY_RULES:
						if (monitoringReplyRuleCaseIsTrue) {
							rulesOnCurrentLevel = databaseManagerService
									.findSortedModelObjects(
											MonitoringReplyRule.class,
											Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER,
											Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC,
											relatedMonitoringRuleForReplyRuleCase
													.getId(),
											null);
						} else {
							rulesOnCurrentLevel = databaseManagerService
									.findSortedModelObjects(
											MonitoringReplyRule.class,
											Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
											Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC,
											relatedMonitoringRuleForReplyRuleCase
													.getId(),
											null);
						}
						break;
					case MICRO_DIALOG_DECISION_POINT:
						rulesOnCurrentLevel = databaseManagerService
								.findSortedModelObjects(MicroDialogRule.class,
										Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT_AND_PARENT,
										Queries.MICRO_DIALOG_RULE__SORT_BY_ORDER_ASC,
										relatedMicroDialogDecisionPointForMicroDialogRuleCase
												.getId(),
										null);
						break;
				}
			}
		} else {
			// Leafs of rules tree
			switch (executionCase) {
				case MONITORING_RULES_DAILY:
				case MONITORING_RULES_PERIODIC:
				case MONITORING_RULES_UNEXPECTED_MESSAGE:
				case MONITORING_RULES_USER_INTENTION:
					rulesOnCurrentLevel = databaseManagerService
							.findSortedModelObjects(MonitoringRule.class,
									Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
									Queries.MONITORING_RULE__SORT_BY_ORDER_ASC,
									intervention.getId(), parent.getId());
					break;
				case MONITORING_REPLY_RULES:
					if (monitoringReplyRuleCaseIsTrue) {
						rulesOnCurrentLevel = databaseManagerService
								.findSortedModelObjects(
										MonitoringReplyRule.class,
										Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER,
										Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC,
										relatedMonitoringRuleForReplyRuleCase
												.getId(),
										parent.getId());
					} else {
						rulesOnCurrentLevel = databaseManagerService
								.findSortedModelObjects(
										MonitoringReplyRule.class,
										Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
										Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC,
										relatedMonitoringRuleForReplyRuleCase
												.getId(),
										parent.getId());
					}
					break;
				case MICRO_DIALOG_DECISION_POINT:
					rulesOnCurrentLevel = databaseManagerService
							.findSortedModelObjects(MicroDialogRule.class,
									Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT_AND_PARENT,
									Queries.MICRO_DIALOG_RULE__SORT_BY_ORDER_ASC,
									relatedMicroDialogDecisionPointForMicroDialogRuleCase
											.getId(),
									parent.getId());
					break;
			}
		}

		// Execute all rules on this level
		ruleLoop: for (val rule : rulesOnCurrentLevel) {
			// There are some more rules on this level to execute so do it

			final RuleEquationSignTypes ruleEquationSign = rule
					.getRuleEquationSign();
			RuleEvaluationResult ruleResult = null;

			int currentIterationValue = 0;
			int iterationExecution = 0;
			boolean iterationCompleted = false;
			do {
				val nextRuleId = rule.getId().toHexString();

				// Check current iteration for iterator cycles
				if (ruleEquationSign.isIteratorEquationSignType()) {
					// Get appropriate current value
					if (iterationCache.containsKey(nextRuleId)) {
						currentIterationValue = iterationCache.get(nextRuleId);
						iterationExecution = iterationLimitCache
								.get(nextRuleId);
					} else {
						val variablesWithValues = variablesManagerService
								.getAllVariablesWithValuesOfParticipantAndSystem(
										participant);

						ruleResult = RuleEvaluator.evaluateRule(
								participant.getId(), participant.getLanguage(),
								rule, variablesWithValues.values());

						if (ruleResult.isEvaluatedSuccessful()) {
							currentIterationValue = (int) ruleResult
									.getCalculatedRuleValue();
							iterationCache.put(nextRuleId,
									currentIterationValue);

							// Iteration validation
							if (ruleEquationSign == RuleEquationSignTypes.STARTS_ITERATION_FROM_X_UP_TO_Y_AND_RESULT_IS_CURRENT
									&& ruleResult
											.getCalculatedRuleValue() > ruleResult
													.getCalculatedRuleComparisonTermValue()) {

								log.error(
										"Iteration rule {} contains conditions that can never match (x < y)",
										rule);

								// Reset iteration
								iterationCache.remove(nextRuleId);
								iterationLimitCache.remove(nextRuleId);

								continue ruleLoop;
							} else if (ruleEquationSign == RuleEquationSignTypes.STARTS_REVERSE_ITERATION_FROM_X_DOWN_TO_Y_AND_RESULT_IS_CURRENT
									&& ruleResult
											.getCalculatedRuleValue() < ruleResult
													.getCalculatedRuleComparisonTermValue()) {

								log.error(
										"Iteration rule {} contains conditions that can never match (x > y)",
										rule);

								continue ruleLoop;
							}
						} else {
							log.error(
									"Error when validating rule {} of participant {}: {}",
									rule.getId(), participant.getId(),
									ruleResult.getErrorMessage());
							log.error(
									"Stopping rule execution for participant {}",
									participant.getId());

							return;
						}
					}
				}

				// Execute rule
				val executionResult = executeRule(rule, ruleResult,
						currentIterationValue);

				// Adjust iteration value
				if (ruleEquationSign.isIteratorEquationSignType()) {
					if (ruleEquationSign == RuleEquationSignTypes.STARTS_ITERATION_FROM_X_UP_TO_Y_AND_RESULT_IS_CURRENT) {
						currentIterationValue++;
					} else {
						currentIterationValue--;
					}
					iterationExecution++;

					iterationCache.put(nextRuleId, currentIterationValue);
					iterationLimitCache.put(nextRuleId, iterationExecution);
				}

				// Remember if children should be checked
				final boolean lookIntoChildren;
				if (ruleEquationSign.isIteratorEquationSignType()) {
					// For iterators children should always be checked, but only
					// while the cycle is still running
					iterationCompleted = executionResult;
					lookIntoChildren = true;

					if (iterationCompleted) {
						// Reset iteration if completed
						iterationCache.remove(nextRuleId);
						iterationLimitCache.remove(nextRuleId);
					}
				} else {
					// The result value defines if the iteration is complete for
					// regular rules
					lookIntoChildren = executionResult;
				}

				// For stability reasons stop iteration if something is wrong
				if (iterationExecution == ImplementationConstants.RULE_ITERATORS_AUTOMATIC_EXECUTION_LOOP_DETECTION_THRESHOLD) {
					log.error(
							"One of the iterations seems to be cyclic - stopping the execution of the same");

					continue ruleLoop;
				}

				// If it was the final rule, then stop the whole iteration
				if (completelyStop) {
					return;
				}

				if (lookIntoChildren) {
					log.debug("Check children");
					// Check children (recursion)
					executeRules(rule);

					// If one of the children decided to cancel execution, stop
					// execution, otherwise go on
					if (completelyStop) {
						return;
					}
				}
			} while (!iterationCompleted);
		}

		return;
	}

	/**
	 * Executes an {@link AbstractMonitoringRule} and returns if the result
	 * should be the last to call for this run or if the iteration is completed
	 *
	 * @param rule
	 * @param currentIterationValue
	 * @return
	 */
	private boolean executeRule(final AbstractMonitoringRule rule,
			final RuleEvaluationResult preEvaluatedRuleResult,
			final int currentIterationValue) {
		RuleEvaluationResult ruleResult;
		if (preEvaluatedRuleResult != null) {
			ruleResult = preEvaluatedRuleResult;
		} else {
			val variablesWithValues = variablesManagerService
					.getAllVariablesWithValuesOfParticipantAndSystem(
							participant);

			ruleResult = RuleEvaluator.evaluateRule(participant.getId(),
					participant.getLanguage(), rule,
					variablesWithValues.values());
		}

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
		if (!StringUtils.isBlank(rule.getStoreValueToVariableWithName())) {
			try {
				// Iterators don't store the rule result, but their current
				// iteration value
				if (ruleResult.isIterator()) {
					variablesManagerService.writeVariableValueOfParticipant(
							participant.getId(),
							rule.getStoreValueToVariableWithName(),
							iterationCache.get(rule.getId().toHexString())
									.toString());
				} else {
					variablesManagerService.writeVariableValueOfParticipant(
							participant.getId(),
							rule.getStoreValueToVariableWithName(),
							ruleResult.isCalculatedRule()
									? StringHelpers.cleanDoubleValue(
											ruleResult.getCalculatedRuleValue())
									: ruleResult.getTextRuleValue());
				}
			} catch (final Exception e) {
				log.warn("Could not write variable value: {}", e.getMessage());
			}
		}

		// Rule evaluates to TRUE
		if (ruleResult.isRuleMatchesEquationSign()) {
			switch (executionCase) {
				case MONITORING_RULES_DAILY:
				case MONITORING_RULES_PERIODIC:
				case MONITORING_RULES_UNEXPECTED_MESSAGE:
				case MONITORING_RULES_USER_INTENTION:
					// Rule "solves" case
					if (((MonitoringRule) rule).isMarkCaseAsSolvedWhenTrue()) {
						log.debug(
								"Rule marks case as solved and stops rule execution!");

						caseMarkedAsSolved = true;
						completelyStop = true;
					}

					// Rule stops whole intervention for participant
					if (((MonitoringRule) rule).isStopInterventionWhenTrue()) {
						log.debug("Rule stops intervention for participant!");

						interventionFinishedForParticipantAfterThisResolving = true;
						completelyStop = true;
					}

					// Message sending
					if (rule.isSendMessageIfTrue()) {
						log.debug("Rule will send message!");

						final MessageSendingResultForMonitoringRule result = new MessageSendingResultForMonitoringRule();

						result.setAbstractMonitoringRuleRequiredToPrepareMessage(
								rule);

						messageSendingResultForMonitoringRules.add(result);
					}

					// Micro dialog activation
					if (rule.isActivateMicroDialogIfTrue()) {
						log.debug("Rule will activate micro dialog!");

						abstractMonitoringRulesToCheckForMicroDialogActivation
								.add(rule);
					}

					break;
				case MONITORING_REPLY_RULES:
					// Message sending
					if (rule.isSendMessageIfTrue()) {
						log.debug("Rule will send message!");

						final MessageSendingResultForMonitoringReplyRule result = new MessageSendingResultForMonitoringReplyRule();

						result.setAbstractMonitoringRuleRequiredToPrepareMessage(
								rule);

						messageSendingResultForMonitoringReplyRules.add(result);
					}

					// Micro dialog activation
					if (rule.isActivateMicroDialogIfTrue()) {
						log.debug("Rule will activate micro dialog!");

						abstractMonitoringRulesToCheckForMicroDialogActivation
								.add(rule);
					}
					break;
				case MICRO_DIALOG_DECISION_POINT:
					// Rule leaves decision point
					if (((MicroDialogRule) rule)
							.isLeaveDecisionPointWhenTrue()) {
						log.debug(
								"Rule leaves decision point and stops rule execution!");

						leaveDecisionPointWhenTrue = true;
						completelyStop = true;
					}

					// Rule stops micro dialog
					if (((MicroDialogRule) rule).isStopMicroDialogWhenTrue()) {
						log.debug(
								"Rule stops complete micro dialog and stops rule execution!");

						stopMicroDialogWhenTrue = true;
						completelyStop = true;
					}

					// Redirection to other next micro dialog
					if (((MicroDialogRule) rule)
							.getNextMicroDialogWhenTrue() != null) {
						log.debug("Rule jumps to other next micro dialog");

						val nextMicroDialogId = ((MicroDialogRule) rule)
								.getNextMicroDialogWhenTrue();

						val proposedNextMicroDialog = databaseManagerService
								.getModelObjectById(MicroDialog.class,
										nextMicroDialogId);

						if (proposedNextMicroDialog != null) {
							nextMicroDialog = proposedNextMicroDialog;
						}
					}

					// Redirection to other next micro dialog message
					if (((MicroDialogRule) rule)
							.getNextMicroDialogMessageWhenTrue() != null) {
						log.debug(
								"Rule jumps to other next micro dialog message");

						val nextMicroDialogMessageId = ((MicroDialogRule) rule)
								.getNextMicroDialogMessageWhenTrue();

						val proposedNextDialogMessage = databaseManagerService
								.getModelObjectById(MicroDialogMessage.class,
										nextMicroDialogMessageId);

						if (proposedNextDialogMessage != null) {
							nextMicroDialogMessage = proposedNextDialogMessage;
						}
					}
					break;
			}
		} else {
			// Rule evaluates to FALSE
			switch (executionCase) {
				case MONITORING_RULES_DAILY:
				case MONITORING_RULES_PERIODIC:
				case MONITORING_RULES_UNEXPECTED_MESSAGE:
				case MONITORING_RULES_USER_INTENTION:
				case MONITORING_REPLY_RULES:
					break;
				case MICRO_DIALOG_DECISION_POINT:
					// Redirection to other next message
					if (((MicroDialogRule) rule)
							.getNextMicroDialogMessageWhenFalse() != null) {
						log.debug(
								"Rule jumps to other next micro dialog message");

						val nextMicroDialogMessageId = ((MicroDialogRule) rule)
								.getNextMicroDialogMessageWhenFalse();

						val proposedNextDialogMessage = databaseManagerService
								.getModelObjectById(MicroDialogMessage.class,
										nextMicroDialogMessageId);

						if (proposedNextDialogMessage != null) {
							nextMicroDialogMessage = proposedNextDialogMessage;
						}
					}
					break;
			}
		}

		// Special solutions for iterators
		if (ruleResult.isIterator()) {
			return ruleResult
					.getCalculatedRuleComparisonTermValue() == currentIterationValue;
		} else {
			return ruleResult.isRuleMatchesEquationSign();
		}
	}
}
