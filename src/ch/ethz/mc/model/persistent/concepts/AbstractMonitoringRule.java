package ch.ethz.mc.model.persistent.concepts;

/* ##LICENSE## */
import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link ModelObject} to represent an {@link AbstractMonitoringRule}
 *
 * A {@link AbstractMonitoringRule} is the core aspect in decision making in
 * this system. The {@link AbstractMonitoringRule}s are executed step by step
 * regarding their order and level. Each {@link AbstractMonitoringRule} can be
 * defined in a way that it stores the result of the rule in a variable and/or
 * if it shall send a message.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
public abstract class AbstractMonitoringRule extends AbstractRule {
	private static final long serialVersionUID = -3271775900725942369L;

	/**
	 * Default constructor
	 */
	public AbstractMonitoringRule(final String ruleWithPlaceholders,
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final String comment, final ObjectId isSubRuleOfMonitoringRule,
			final int order, final String storeValueToVariableWithName,
			final boolean sendMessageIfTrue,
			final boolean sendMessageToSupervisor,
			final ObjectId relatedMonitoringMessageGroup,
			final boolean activateMicroDialogIfTrue,
			final ObjectId relatedMicroDialog) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, comment);

		this.isSubRuleOfMonitoringRule = isSubRuleOfMonitoringRule;
		this.order = order;
		this.storeValueToVariableWithName = storeValueToVariableWithName;
		this.sendMessageIfTrue = sendMessageIfTrue;
		this.sendMessageToSupervisor = sendMessageToSupervisor;
		this.relatedMonitoringMessageGroup = relatedMonitoringMessageGroup;
		this.activateMicroDialogIfTrue = activateMicroDialogIfTrue;
		this.relatedMicroDialog = relatedMicroDialog;
	}

	/**
	 * <strong>OPTIONAL:</strong> If the {@link AbstractMonitoringRule} is
	 * nested below another {@link AbstractMonitoringRule} the father has to be
	 * referenced here
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
	 * appropriate variable can be set here.
	 */
	@Getter
	@Setter
	private String		storeValueToVariableWithName;

	/**
	 * <strong>OPTIONAL:</strong> If the result of the
	 * {@link AbstractMonitoringRule} is true, a message will be send
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

	/**
	 * <strong>OPTIONAL:</strong> If the result of the
	 * {@link AbstractMonitoringRule} is true, a micro dialog will be activated
	 * for the {@link Participant}
	 */
	@Getter
	@Setter
	private boolean		activateMicroDialogIfTrue;

	/**
	 * <strong>OPTIONAL if activateMicroDialogIfTrue is false:</strong> The
	 * {@link MicroDialog} that should be activated
	 */
	@Getter
	@Setter
	private ObjectId	relatedMicroDialog;
}
