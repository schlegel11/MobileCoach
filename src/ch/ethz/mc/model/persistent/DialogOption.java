package ch.ethz.mc.model.persistent;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;

/**
 * {@link ModelObject} to represent an {@link DialogOption}
 * 
 * A {@link DialogOption} describes by which options a {@link Participant} can
 * be contacted. Several {@link DialogOption}s can exist for one
 * {@link Participant}.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class DialogOption extends ModelObject {
	/**
	 * The {@link Participant} which provides this {@link DialogOption}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			participant;

	/**
	 * The {@link DialogOptionTypes} which describes this {@link DialogOption}
	 */
	@Getter
	@Setter
	@NonNull
	private DialogOptionTypes	type;

	/**
	 * The data required to reach the {@link Participant} using this
	 * {@link DialogOption}, e.g. a phone number or an email address
	 */
	@Getter
	@Setter
	@NonNull
	private String				data;

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
}
