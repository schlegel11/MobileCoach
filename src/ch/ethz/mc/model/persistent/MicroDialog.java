package ch.ethz.mc.model.persistent;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	/**
	 * The {@link Intervention} the {@link MicroDialog} belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	intervention;

	/**
	 * The name of the {@link MicroDialog} as shown in the backend
	 */
	@Getter
	@Setter
	@NonNull
	private String		name;

	/**
	 * The position of the {@link MicroDialog} compared to all other
	 * {@link MicroDialog}s
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
	protected void collectThisAndRelatedModelObjectsForExport(
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
