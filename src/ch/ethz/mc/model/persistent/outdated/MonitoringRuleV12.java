package ch.ethz.mc.model.persistent.outdated;

/* ##LICENSE## */
import org.bson.types.ObjectId;

import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.types.MonitoringRuleTypes;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * CAUTION: Will only be used for conversion from data model 11 to 12
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
public class MonitoringRuleV12 extends AbstractMonitoringRuleV12 {
	private static final long serialVersionUID = -2675541242988438072L;

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
