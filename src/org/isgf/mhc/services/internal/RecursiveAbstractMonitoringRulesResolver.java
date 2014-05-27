package org.isgf.mhc.services.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.bson.types.ObjectId;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.persistent.DialogMessage;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.MonitoringMessage;
import org.isgf.mhc.model.persistent.MonitoringMessageGroup;
import org.isgf.mhc.model.persistent.MonitoringMessageRule;
import org.isgf.mhc.model.persistent.MonitoringReplyRule;
import org.isgf.mhc.model.persistent.MonitoringRule;
import org.isgf.mhc.model.persistent.Participant;
import org.isgf.mhc.model.persistent.concepts.AbstractMonitoringRule;
import org.isgf.mhc.model.persistent.concepts.AbstractVariableWithValue;
import org.isgf.mhc.tools.RuleEvaluator;
import org.isgf.mhc.tools.StringHelpers;
import org.isgf.mhc.tools.VariableStringReplacer;

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
	private final DatabaseManagerService	databaseManagerService;
	private final VariablesManagerService	variablesManagerService;

	// Internal objects
	private boolean							completelyStop											= false;

	// Relevant for both cases
	private final Participant				participant;

	// Gives information if the whole class should handle MonitoringRules or
	// MonitoringReplyRules
	private final boolean					isMonitoringRule;

	// Only relevant for MonitoringRules
	private final Intervention				intervention;

	// Only relevant for MonitoringReplyRules
	private MonitoringMessage				relatedMonitoringMessageForReplyRuleCase				= null;
	private MonitoringRule					relatedMonitoringRuleForReplyRuleCase					= null;
	private final boolean					monitoringReplyRuleCaseIsTrue;

	/*
	 * Values to resolve related to intervention process
	 */

	// Only relevant for MonitoringRules
	private boolean							interventionIsFinishedForParticipantAfterThisResolving	= false;

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
		@Getter
		@Setter
		private MonitoringRule	monitoringRuleThatCausedMessageSending	= null;
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
			final DatabaseManagerService databaseManagerService,
			final VariablesManagerService variablesManagerService,
			final Participant participant, final boolean isMonitoringRule,
			final ObjectId relatedMonitoringMessageForReplyRuleCase,
			final ObjectId relatedMonitoringRuleForReplyRuleCase,
			final boolean monitoringReplyRuleCase) {
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

			val determinedMonitoringMessageToSend = determineMessageToSend(monitoringMessageGroup);
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
					.getAllVariablesWithValuesOfParticipantAndSystem(participant);
			val messageTextToSend = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(
							determinedMonitoringMessageToSend
									.getTextWithPlaceholders(),
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

		val ruleResult = RuleEvaluator.evaluateRule(rule,
				variablesWithValues.values());

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
			// Stop execution when rule is sending message
			log.debug("Rule will send message!");

			if (isMonitoringRule) {
				final MessageSendingResultForMonitoringRule result = new MessageSendingResultForMonitoringRule();

				result.setMonitoringRuleThatCausedMessageSending((MonitoringRule) rule);
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

	/**
	 * Checks message groups which groups and messages are (1) already used //
	 * (2) used less // (3) simply fit and
	 * returns the {@link MonitoringMessage} to send or null if there are no
	 * messages in the group
	 * left
	 * 
	 * @return
	 */
	private MonitoringMessage determineMessageToSend(
			final MonitoringMessageGroup messageGroup) {
		val iterableMessages = databaseManagerService.findSortedModelObjects(
				MonitoringMessage.class,
				Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
				Queries.MONITORING_MESSAGE__SORT_BY_ORDER_ASC,
				messageGroup.getId());

		@SuppressWarnings("unchecked")
		final List<MonitoringMessage> messages = IteratorUtils
				.toList(iterableMessages.iterator());

		if (!isMonitoringRule
				&& messageGroup.isSendSamePositionIfSendingAsReply()) {
			// Send in same position if sending as reply
			val originalMessageGroupId = relatedMonitoringMessageForReplyRuleCase
					.getMonitoringMessageGroup();
			val originalIterableMessages = databaseManagerService
					.findSortedModelObjects(
							MonitoringMessage.class,
							Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
							Queries.MONITORING_MESSAGE__SORT_BY_ORDER_ASC,
							originalMessageGroupId);

			@SuppressWarnings("unchecked")
			final List<MonitoringMessage> originalMessages = IteratorUtils
					.toList(originalIterableMessages.iterator());

			for (int i = 0; i < originalMessages.size(); i++) {
				if (originalMessages.get(i).getId()
						.equals(relatedMonitoringMessageForReplyRuleCase)
						&& i < messages.size()) {
					val message = messages.get(i);
					log.debug(
							"Monitoring message {} is at the same position as monitoring message {} and will thereofore be used as reply on answer",
							message.getId(),
							relatedMonitoringMessageForReplyRuleCase.getId());
					return message;
				}
			}
		} else {
			// Send in random order?
			if (messageGroup.isSendInRandomOrder()) {
				Collections.shuffle(messages);
			}

			Hashtable<String, AbstractVariableWithValue> variablesWithValues = null;

			// Loop over all messages until an appropriate message has been
			// found
			MonitoringMessage messageToStartWithInFallbackCase = null;
			int timesMessageAlreadyUsed = Integer.MAX_VALUE;

			for (int i = 0; i < 3; i++) {
				messageLoop: for (val message : messages) {
					if (i == 1 && message != messageToStartWithInFallbackCase) {
						continue messageLoop;
					}

					val dialogMessages = new ArrayList<DialogMessage>();
					if (i == 0) {
						val dialogMessagesIterator = databaseManagerService
								.findModelObjects(
										DialogMessage.class,
										Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_RELATED_MONITORING_MESSAGE,
										participant.getId(), message.getId())
								.iterator();

						CollectionUtils.addAll(dialogMessages,
								dialogMessagesIterator);

						if (dialogMessages.size() < timesMessageAlreadyUsed) {
							// Remember this message as least used message
							messageToStartWithInFallbackCase = message;
							timesMessageAlreadyUsed = dialogMessages.size();
						}
					}

					if (i >= 1 || dialogMessages.size() == 0) {
						if (i == 0) {
							log.debug(
									"Monitoring message {} was not used for participant, yet",
									message.getId());
						} else if (i == 1) {
							log.debug(
									"Monitoring message {} was LESS used for participant",
									message.getId());
						} else if (i == 2) {
							log.debug(
									"Monitoring message {} was could be used for participant as last option",
									message.getId());
						}

						// Check rules of message for execution
						val rules = databaseManagerService
								.findSortedModelObjects(
										MonitoringMessageRule.class,
										Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
										Queries.MONITORING_MESSAGE_RULE__SORT_BY_ORDER_ASC,
										message.getId());

						for (val rule : rules) {
							if (variablesWithValues == null) {
								variablesWithValues = variablesManagerService
										.getAllVariablesWithValuesOfParticipantAndSystem(participant);
							}

							val ruleResult = RuleEvaluator.evaluateRule(rule,
									variablesWithValues.values());

							if (!ruleResult.isEvaluatedSuccessful()) {
								log.error("Error when validating rule: "
										+ ruleResult.getErrorMessage());
								continue;
							}

							// Check if true rule matches
							if (!ruleResult.isRuleMatchesEquationSign()) {
								log.debug("Rule does not match, so skip this message");
								continue messageLoop;
							}
						}

						return message;
					}
				}

				log.debug("All message in this group were already used for the participant...so start over and use least used message");
			}
		}

		return null;
	}

	/*
	 * Methods to return results
	 */
	public boolean isInterventionFinishedForParticipantAfterThisResolving() {
		return interventionIsFinishedForParticipantAfterThisResolving;
	}
}
