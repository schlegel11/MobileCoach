package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.concepts.MicroDialogElementInterface;
import ch.ethz.mc.model.ui.UIMicroDialogElementInterface;
import ch.ethz.mc.model.ui.UIModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link MicroDialogDecisionPoint}
 *
 * {@link MicroDialogDecisionPoint}s are part of {@link MicroDialog}s and
 * contain a collection of {@link MicroDialogRule}s
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class MicroDialogDecisionPoint extends ModelObject
		implements MicroDialogElementInterface {
	private static final long	serialVersionUID	= 4059444409285460396L;

	/**
	 * A comment for the author, not visible to any participant
	 */
	@Getter
	@Setter
	@NonNull
	private String				comment;

	/**
	 * The {@link MicroDialog} this {@link MicroDialogDecisionPoint} belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			microDialog;

	/**
	 * The position of the {@link MicroDialogDecisionPoint} compared to all
	 * other {@link MicroDialogElementInterface}s in the same
	 * {@link MicroDialog}
	 */
	@Getter
	@Setter
	private int					order;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		int decisionPointRules = 0;
		val microDialogRules = MC.getInstance()
				.getInterventionAdministrationManagerService()
				.getAllMicroDialogRulesOfMicroDialogDecisionPoint(getId());

		if (microDialogRules != null) {
			val microDialogRulesIterator = microDialogRules.iterator();

			while (microDialogRulesIterator.hasNext()) {
				microDialogRulesIterator.next();
				decisionPointRules++;
			}
		}

		final val microDialogMessage = new UIMicroDialogElementInterface(
				getOrder(),
				Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__DECISION_POINT),
				false, false,
				StringUtils.isBlank(comment)
						? ImplementationConstants.DEFAULT_OBJECT_NAME : comment,
				"", "", "", "", "", "", decisionPointRules);

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
	public void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);

		// Add micro dialog rule
		for (val microDialogRule : ModelObject.find(MicroDialogRule.class,
				Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT,
				getId())) {
			microDialogRule
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		// Delete rules
		val rulesToDelete = ModelObject.find(MicroDialogRule.class,
				Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT,
				getId());
		ModelObject.delete(rulesToDelete);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		String table = wrapRow(wrapHeader("Micro Dialog Decision Point"));

		// Micro dialog rules
		final StringBuffer buffer = new StringBuffer();
		val rules = ModelObject.findSorted(MicroDialogRule.class,
				Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT_AND_PARENT,
				Queries.MICRO_DIALOG_RULE__SORT_BY_ORDER_ASC, getId(), null);

		for (val rule : rules) {
			buffer.append(rule.toTable());
		}

		if (buffer.length() > 0) {
			table += wrapRow(
					wrapHeader("Rules:") + wrapField(buffer.toString()));
		}

		return wrapTable(table);
	}
}
