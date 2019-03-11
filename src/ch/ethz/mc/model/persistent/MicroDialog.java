package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.ui.UIMicroDialog;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.tools.ListMerger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link MicroDialog}
 *
 * An {@link MicroDialog} contains one ore more {@link MicroDialogMessage}s that
 * will be sent to {@link Participant}s during an {@link Intervention}.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class MicroDialog extends ModelObject {
	private static final long	serialVersionUID	= -8890934986782742689L;

	/**
	 * The {@link Intervention} the {@link MicroDialog} belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			intervention;

	/**
	 * The name of the {@link MicroDialog} as shown in the backend
	 */
	@Getter
	@Setter
	@NonNull
	private String				name;

	/**
	 * A comment for the author, not visible to any participant
	 */
	@Getter
	@Setter
	@NonNull
	private String				comment;

	/**
	 * The position of the {@link MicroDialog} compared to all other
	 * {@link MicroDialog}s
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
		final val microDialog = new UIMicroDialog(name);

		microDialog.setRelatedModelObject(this);

		return microDialog;
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

		// Add micro dialog message
		for (val microDialogMessage : ModelObject.find(MicroDialogMessage.class,
				Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG, getId())) {
			microDialogMessage
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}

		// Add micro dialog decision points
		for (val microDialogDecisionPoint : ModelObject.find(
				MicroDialogDecisionPoint.class,
				Queries.MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG,
				getId())) {
			microDialogDecisionPoint
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
		val microDialogMessagesToDelete = ModelObject.find(
				MicroDialogMessage.class,
				Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG, getId());

		ModelObject.delete(microDialogMessagesToDelete);

		val microDialogDecisionPointsToDelete = ModelObject.find(
				MicroDialogDecisionPoint.class,
				Queries.MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG, getId());

		ModelObject.delete(microDialogDecisionPointsToDelete);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		String table = wrapRow(wrapHeader("Name:") + wrapField(escape(name)));

		final StringBuffer buffer = new StringBuffer();

		// Messages
		val messages = ModelObject.findSorted(MicroDialogMessage.class,
				Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG,
				Queries.MICRO_DIALOG_MESSAGE__SORT_BY_ORDER_ASC, getId());
		// Decision points
		val decisionPoints = ModelObject.findSorted(
				MicroDialogDecisionPoint.class,
				Queries.MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG,
				Queries.MICRO_DIALOG_DECISION_POINT__SORT_BY_ORDER_ASC,
				getId());

		// Merge lists while retaining order
		val microDialogElements = ListMerger.mergeMicroDialogElements(messages,
				decisionPoints);

		for (val microDialogElement : microDialogElements) {
			buffer.append(microDialogElement.toTable());
		}

		if (buffer.length() > 0) {
			table += wrapRow(wrapHeader("Messages & Decision Points:")
					+ wrapField(buffer.toString()));
		}

		return wrapTable(table);
	}
}
