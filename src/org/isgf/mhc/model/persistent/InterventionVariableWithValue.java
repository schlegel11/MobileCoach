package org.isgf.mhc.model.persistent;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.persistent.concepts.AbstractVariableWithValue;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link ModelObject} to represent an {@link InterventionVariableWithValue}
 * 
 * SystemVariables belong to the referenced {@link Intervention} and consist of a name
 * and a value.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public class InterventionVariableWithValue extends AbstractVariableWithValue {
	/**
	 * Default constructor
	 */
	public InterventionVariableWithValue(final ObjectId intervention,
			final String name, final String value) {
		super(name, value);

		this.intervention = intervention;
	}

	/**
	 * {@link Intervention} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	intervention;

	/**
	 * Will recursively collect all related {@link ModelObject} for export
	 * 
	 * @param exportList
	 *            The {@link ModelObject} itself and all related
	 *            {@link ModelObject}s
	 */
	@Override
	@JsonIgnore
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}
}