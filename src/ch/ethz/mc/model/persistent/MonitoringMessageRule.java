package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.UIMonitoringMessageRule;
import ch.ethz.mc.tools.StringHelpers;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link MonitoringMessageRule}
 *
 * A {@link MonitoringMessageRule} can evaluate if the belonging
 * {@link MonitoringMessage} should be send. If all
 * {@link MonitoringMessageRule}s return true a specific
 * {@link MonitoringMessage} is send.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
public class MonitoringMessageRule extends AbstractRule {
	private static final long serialVersionUID = 1620446052417436521L;

	/**
	 * Default constructor
	 */
	public MonitoringMessageRule(final ObjectId belongingMonitoringMessage,
			final int order, final String ruleWithPlaceholders,
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final String comment) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, comment);

		this.belongingMonitoringMessage = belongingMonitoringMessage;
		this.order = order;
	}

	/**
	 * The {@link MonitoringMessage} this rule belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	belongingMonitoringMessage;

	/**
	 * The position of the {@link MonitoringMessageRule} compared to all other
	 * {@link MonitoringMessageRule}s; the first rule will be called first
	 */
	@Getter
	@Setter
	private int			order;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val monitoringMessage = new UIMonitoringMessageRule(order,
				StringHelpers.createRuleName(this, true));

		monitoringMessage.setRelatedModelObject(this);

		return monitoringMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		String table = wrapRow(wrapHeader("Rule:")
				+ wrapField(escape(StringHelpers.createRuleName(this, false))));
		table += wrapRow(
				wrapHeader("Comment:") + wrapField(escape(getComment())));

		return wrapTable(table);
	}
}
