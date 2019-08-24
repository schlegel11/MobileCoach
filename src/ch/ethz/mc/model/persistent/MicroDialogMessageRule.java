package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.model.ui.UIMicroDialogMessageRule;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.tools.StringHelpers;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link MicroDialogMessageRule}
 *
 * A {@link MicroDialogMessageRule} can evaluate if the belonging
 * {@link MicroDialogMessage} should be send. If all
 * {@link MicroDialogMessageRule}s return true a specific
 * {@link MicroDialogMessage} is send.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
public class MicroDialogMessageRule extends AbstractRule {
	private static final long serialVersionUID = 4266506810684596781L;

	/**
	 * Default constructor
	 */
	public MicroDialogMessageRule(final ObjectId belongingMicroDialogMessage,
			final int order, final String ruleWithPlaceholders,
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final String comment) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, comment);

		this.belongingMicroDialogMessage = belongingMicroDialogMessage;
		this.order = order;
	}

	/**
	 * The {@link MicroDialogMessage} this rule belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	belongingMicroDialogMessage;

	/**
	 * The position of the {@link MicroDialogMessageRule} compared to all other
	 * {@link MicroDialogMessageRule}s; the first rule will be called first
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
		val microDialogMessage = new UIMicroDialogMessageRule(order,
				StringHelpers.createRuleName(this, true));

		microDialogMessage.setRelatedModelObject(this);

		return microDialogMessage;
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
