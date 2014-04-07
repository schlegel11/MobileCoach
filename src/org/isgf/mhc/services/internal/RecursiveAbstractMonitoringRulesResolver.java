package org.isgf.mhc.services.internal;

import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.persistent.MonitoringMessage;
import org.isgf.mhc.model.persistent.MonitoringRule;
import org.isgf.mhc.model.persistent.Participant;
import org.isgf.mhc.model.persistent.concepts.AbstractMonitoringRule;

/**
 * Helps to recursively resolve tree-based rule structures of
 * {@link AbstractMonitoringRule}s
 * 
 * @author Andreas Filler
 */
@Log4j2
public class RecursiveAbstractMonitoringRulesResolver {
	private final DatabaseManagerService	databaseManagerService;
	private final VariablesManagerService	variablesManagerService;

	private final Participant				participant;

	private final boolean					isMonitoringRule;

	// Only relevant for MonitoringReplyRules
	private final ObjectId					relatedMonitoringRuleForReplyRuleCase;
	private final boolean					monitoringReplyRuleCase;

	/*
	 * Values to resolve related to intervention process
	 */

	// Only relevant for MonitoringRules
	private final boolean					interventionIsFinishedForParticipantAfterThisResolving	= false;

	/*
	 * Values to resolve related to message sending
	 */

	// Relevant for both cases
	private final boolean					messageShouldBeSentAfterThisResolving					= false;

	// Only relevant for MonitoringRules
	private final MonitoringRule			abstractMonitoringRuleThatCausedMessageSending			= null;
	private final MonitoringMessage			monitoringMessageToSend									= null;

	// Relevant for both cases
	private final String					messageTextToSend										= "";

	public RecursiveAbstractMonitoringRulesResolver(
			final DatabaseManagerService databaseManagerService,
			final VariablesManagerService variablesManagerService,
			final Participant participant, final boolean isMonitoringRule,
			final ObjectId relatedMonitoringRuleForReplyRuleCase,
			final boolean monitoringReplyRuleCase) {
		this.databaseManagerService = databaseManagerService;
		this.variablesManagerService = variablesManagerService;

		this.participant = participant;

		this.isMonitoringRule = isMonitoringRule;
		this.relatedMonitoringRuleForReplyRuleCase = relatedMonitoringRuleForReplyRuleCase;
		this.monitoringReplyRuleCase = monitoringReplyRuleCase;
	}

	public void resolve() {
		// TODO Auto-generated method stub

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
