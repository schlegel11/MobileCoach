package org.isgf.mhc.services.internal;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.MonitoringMessage;
import org.isgf.mhc.model.persistent.MonitoringRule;
import org.isgf.mhc.model.persistent.Participant;
import org.isgf.mhc.model.persistent.concepts.AbstractMonitoringRule;
import org.isgf.mhc.tools.RuleEvaluator;
import org.isgf.mhc.tools.VariableStringReplacer;

/**
 * Helps to recursively resolve tree-based rule structures of
 * {@link AbstractMonitoringRule}s
 * 
 * @author Andreas Filler
 */
@Log4j2
public class RecursiveAbstractMonitoringRulesResolver {
	// General objects
	private final DatabaseManagerService	databaseManagerService;
	private final VariablesManagerService	variablesManagerService;

	// Internal objects
	private AbstractMonitoringRule			ruleMatched;
	private boolean							completelyStop											= false;

	// Relevant for both cases
	private final Participant				participant;

	// Gives information if the whole class should handle MonitoringRules or
	// MonitoringReplyRules
	private final boolean					isMonitoringRule;

	// Only relevant for MonitoringRules
	private final Intervention				intervention;

	// Only relevant for MonitoringReplyRules
	private final MonitoringRule			relatedMonitoringRuleForReplyRuleCase;
	private final boolean					monitoringReplyRuleCaseIsTrue;

	/*
	 * Values to resolve related to intervention process
	 */

	// Only relevant for MonitoringRules
	private boolean							interventionIsFinishedForParticipantAfterThisResolving	= false;

	/*
	 * Values to resolve related to message sending
	 */

	// Relevant for both cases
	private boolean							messageShouldBeSentAfterThisResolving					= false;

	// Only relevant for MonitoringRules and if
	// messageShouldBeSentAfterThisResolving is true
	private MonitoringRule					abstractMonitoringRuleThatCausedMessageSending			= null;
	private MonitoringMessage				monitoringMessageToSend									= null;

	// Relevant for both cases and if messageShouldBeSentAfterThisResolving is
	// true
	private String							messageTextToSend										= "";

	public RecursiveAbstractMonitoringRulesResolver(
			final DatabaseManagerService databaseManagerService,
			final VariablesManagerService variablesManagerService,
			final Participant participant, final boolean isMonitoringRule,
			final ObjectId relatedMonitoringRuleForReplyRuleCase,
			final boolean monitoringReplyRuleCase) {
		this.databaseManagerService = databaseManagerService;
		this.variablesManagerService = variablesManagerService;

		this.participant = participant;

		intervention = databaseManagerService.getModelObjectById(
				Intervention.class, participant.getIntervention());

		this.isMonitoringRule = isMonitoringRule;
		this.relatedMonitoringRuleForReplyRuleCase = databaseManagerService
				.getModelObjectById(MonitoringRule.class,
						relatedMonitoringRuleForReplyRuleCase);
		monitoringReplyRuleCaseIsTrue = monitoringReplyRuleCase;
	}

	public void resolve() throws Exception {
		// Recursively check all rules
		executeRules(null);

		if (messageShouldBeSentAfterThisResolving) {
			if (ruleMatched.getRelatedMonitoringMessageGroup() == null) {
				throw new NullPointerException(
						"There are no more messages available in group "
								+ ruleMatched
										.getRelatedMonitoringMessageGroup()
								+ " to send to participant "
								+ participant.getId());
			}

			// Determine message to send by checking message groups for already
			// sent messages
			val determinedMontioringMessageToSend = determineMessageToSend();
			if (determinedMontioringMessageToSend == null) {
				throw new NullPointerException(
						"There are no more messages available in group "
								+ ruleMatched
										.getRelatedMonitoringMessageGroup()
								+ " to send to participant "
								+ participant.getId());
			}

			monitoringMessageToSend = determinedMontioringMessageToSend;

			// Determine message text to send

			val variablesWithValues = variablesManagerService
					.getAllVariablesWithValuesOfParticipantAndSystem(participant);
			messageTextToSend = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(
							monitoringMessageToSend.getTextWithPlaceholders(),
							variablesWithValues.values(), "");
		}
	}

	/**
	 * Recursively walks through rules until one rule matches
	 * 
	 * @param parent
	 * @throws Exception
	 */
	private void executeRules(final AbstractMonitoringRule parent)
			throws Exception {
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
									MonitoringRule.class,
									Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER,
									Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC,
									relatedMonitoringRuleForReplyRuleCase
											.getId(), null);
				} else {
					rulesOnCurrentLevel = databaseManagerService
							.findSortedModelObjects(
									MonitoringRule.class,
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
									MonitoringRule.class,
									Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER,
									Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC,
									relatedMonitoringRuleForReplyRuleCase
											.getId(), parent.getId());
				} else {
					rulesOnCurrentLevel = databaseManagerService
							.findSortedModelObjects(
									MonitoringRule.class,
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

				// If one of the childrens decided to cancel execution, stop
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
	private boolean executeRule(final AbstractMonitoringRule rule)
			throws Exception {
		val variablesWithValues = variablesManagerService
				.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		val ruleResult = RuleEvaluator.evaluateRule(rule,
				variablesWithValues.values());

		if (!ruleResult.isEvaluatedSuccessful()) {
			log.error("Error when validating rule {} of participant {}: {}",
					rule.getId(), participant.getId(),
					ruleResult.getErrorMessage());
			log.error("Stopping rule execution for participant {}",
					participant.getId());
			throw new Exception(
					"Could not execute rule: Error when validating rule "
							+ rule.getId() + " of participant "
							+ participant.getId() + ": "
							+ ruleResult.getErrorMessage());
		}

		log.debug("Checked rule and result is {}",
				ruleResult.isRuleMatchesEquationSign());

		// Store result if it should be stored
		if (rule.getStoreValueToVariableWithName() != null
				&& !rule.getStoreValueToVariableWithName().equals("")) {
			variablesManagerService.writeVariableValueOfParticipant(
					participant,
					rule.getStoreValueToVariableWithName(),
					ruleResult.isCalculatedRule() ? String.valueOf(ruleResult
							.getCalculatedRuleValue()) : ruleResult
							.getTextRuleValue());
		}

		if (ruleResult.isRuleMatchesEquationSign()
				&& rule.isSendMessageIfTrue()) {
			// Stop execution when rule is sending message
			log.debug("Rule will send message!");

			messageShouldBeSentAfterThisResolving = true;

			if (isMonitoringRule) {
				abstractMonitoringRuleThatCausedMessageSending = (MonitoringRule) rule;
			}

			ruleMatched = rule;
			completelyStop = true;
		} else if (isMonitoringRule && ruleResult.isRuleMatchesEquationSign()
				&& ((MonitoringRule) rule).isStopInterventionWhenTrue()) {
			// Stop execution when rule is finalizing rule
			log.debug("Rule will stop intervention for participant!");

			interventionIsFinishedForParticipantAfterThisResolving = true;

			ruleMatched = rule;
			completelyStop = true;
		}

		return ruleResult.isRuleMatchesEquationSign();
	}

	/**
	 * Checks message groups which groups and messages are already used and
	 * returns the {@link MonitoringMessage} to send or null if there are no
	 * messages in the group
	 * left
	 * 
	 * @return
	 */
	private MonitoringMessage determineMessageToSend() {
		// TODO check messages groups of participant

		return null;
	}

	/*
	 * Methods to return results
	 */
	public boolean isInterventionFinishedForParticipantAfterThisResolving() {
		return interventionIsFinishedForParticipantAfterThisResolving;
	}

	public boolean shouldAMessageBeSentAfterThisResolving() {
		return messageShouldBeSentAfterThisResolving;
	}

	public MonitoringRule getRuleThatCausedMessageSending() {
		return abstractMonitoringRuleThatCausedMessageSending;
	}

	public MonitoringMessage getMonitoringMessageToSend() {
		return monitoringMessageToSend;
	}

	public String getMessageTextToSend() {
		return messageTextToSend;
	}
}
