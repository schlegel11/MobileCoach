package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.concepts.AbstractMonitoringRule;
import ch.ethz.mc.model.persistent.concepts.MicroDialogElementInterface;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.tools.StringHelpers;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link MicroDialogRule}
 *
 * A {@link MicroDialogRule} is the core aspect in decision making in micro
 * dialogs in this system. The {@link MicroDialogRule}s are executed step by
 * step regarding their order and level. Each {@link MicroDialogRule} can be
 * defined in a way that it stores the result of the rule in a variable and/or
 * if it shall jump to another {@link MicroDialogElementInterface} within this
 * {@link MicroDialog}
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
public class MicroDialogRule extends AbstractMonitoringRule {
	private static final long serialVersionUID = -954409194772554660L;

	/**
	 * Default constructor
	 */
	public MicroDialogRule(final String ruleWithPlaceholders,
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final String comment, final ObjectId isSubRuleOfMonitoringRule,
			final int order, final String storeValueToVariableWithName,
			final ObjectId microDialogDecisionPoint,
			final ObjectId nextMicroDialogWhenTrue,
			final ObjectId nextMicroDialogMessageWhenTrue,
			final ObjectId nextMicroDialogMessageWhenFalse,
			final boolean stopMicroDialogWhenTrue,
			final boolean leaveDecisionPointWhenTrue) {
		// No related monitoring message group and no sending of messages from
		// message groups in any case (neither user nor supervisor)
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, comment,
				isSubRuleOfMonitoringRule, order, storeValueToVariableWithName,
				false, false, null, false, null);

		this.microDialogDecisionPoint = microDialogDecisionPoint;
		this.nextMicroDialogWhenTrue = nextMicroDialogWhenTrue;
		this.nextMicroDialogMessageWhenTrue = nextMicroDialogMessageWhenTrue;
		this.nextMicroDialogMessageWhenFalse = nextMicroDialogMessageWhenFalse;
		this.stopMicroDialogWhenTrue = stopMicroDialogWhenTrue;
		this.leaveDecisionPointWhenTrue = leaveDecisionPointWhenTrue;
	}

	/**
	 * The {@link MicroDialogDecisionPoint} this {@link MicroDialogRule} belongs
	 * to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId		microDialogDecisionPoint;

	/**
	 * <strong>OPTIONAL:</strong> If the rule result is <strong>true</strong>
	 * the {@link Participant} will be redirected to the given
	 * {@link MicroDialogMessage}
	 */
	@Getter
	@Setter
	private ObjectId		nextMicroDialogWhenTrue;

	/**
	 * <strong>OPTIONAL:</strong> If the rule result is <strong>true</strong>
	 * the {@link Participant} will be redirected to the given
	 * {@link MicroDialogMessage}
	 */
	@Getter
	@Setter
	private ObjectId		nextMicroDialogMessageWhenTrue;

	/**
	 * <strong>OPTIONAL:</strong> If the rule result is <strong>false</strong>
	 * the {@link Participant} will be redirected to the given
	 * {@link MicroDialogMessage}
	 */
	@Getter
	@Setter
	private ObjectId		nextMicroDialogMessageWhenFalse;

	/**
	 * <strong>OPTIONAL:</strong> The whole micro dialog will be set to finished
	 * for participant and rule execution will stop when the rule evaluates to
	 * true
	 */
	@Getter
	@Setter
	private boolean			stopMicroDialogWhenTrue;

	/**
	 * <strong>OPTIONAL:</strong> The execution of the rules of this decision
	 * point will stop when the rule evalutes to true
	 */
	@Getter
	@Setter
	private boolean			leaveDecisionPointWhenTrue;

	/**
	 * Not relevant for this instance of the class --> ignore in JSON mapping
	 */
	@Getter
	@JsonIgnore
	private final boolean	sendMessageIfTrue				= false;

	/**
	 * Not relevant for this instance of the class --> ignore in JSON mapping
	 */
	@Getter
	@JsonIgnore
	private final boolean	sendMessageToSupervisor			= false;

	/**
	 * Not relevant for this instance of the class --> ignore in JSON mapping
	 */
	@Getter
	@JsonIgnore
	private final ObjectId	relatedMonitoringMessageGroup	= null;

	/**
	 * Not relevant for this instance of the class --> ignore in JSON mapping
	 */
	@Getter
	@JsonIgnore
	private final boolean	activateMicroDialogIfTrue		= false;

	/**
	 * Not relevant for this instance of the class --> ignore in JSON mapping
	 */
	@Getter
	@JsonIgnore
	private final ObjectId	relatedMicroDialog				= null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	public void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		// Delete sub rules
		val microDialogRulesToDelete = ModelObject.find(MicroDialogRule.class,
				Queries.MICRO_DIALOG_RULE__BY_PARENT, getId());
		ModelObject.delete(microDialogRulesToDelete);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		return toTable(0, microDialogDecisionPoint);
	}

	@JsonIgnore
	public String toTable(final int level,
			final ObjectId microDialogDecisionPointId) {
		val style = level > 0 ? "border-left-width: " + 20 * level + "px;" : "";

		String table = wrapRow(wrapHeader("Rule:", style)
				+ wrapField(escape(StringHelpers.createRuleName(this, false))));
		table += wrapRow(wrapHeader("Comment:", style)
				+ wrapField(escape(getComment())));

		table += wrapRow(wrapHeader("Variable to store value to:", style)
				+ wrapField(escape(getStoreValueToVariableWithName())));
		table += wrapRow(wrapHeader("Send message when TRUE:", style)
				+ wrapField(formatYesNo(isSendMessageIfTrue())));

		if (getRelatedMonitoringMessageGroup() != null) {
			val messageGroup = ModelObject.get(MonitoringMessageGroup.class,
					getRelatedMonitoringMessageGroup());
			if (messageGroup != null) {
				table += wrapRow(wrapHeader(
						"Monitoring Message Group to send from:", style)
						+ wrapField(escape(messageGroup.getName())));
			} else {
				table += wrapRow(wrapHeader(
						"Monitoring Message Group to send from:", style)
						+ wrapField(formatWarning(
								"Message Group set, but not found")));
			}
		}

		// Sub Rules
		val subRules = ModelObject.findSorted(MicroDialogRule.class,
				Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT_AND_PARENT,
				Queries.MONITORING_RULE__SORT_BY_ORDER_ASC,
				microDialogDecisionPointId, getId());

		final StringBuffer buffer = new StringBuffer();
		for (val subRule : subRules) {
			buffer.append(
					subRule.toTable(level + 1, microDialogDecisionPointId));
		}

		if (buffer.length() > 0) {
			return wrapTable(table) + buffer.toString();
		} else {
			return wrapTable(table);
		}
	}
}
