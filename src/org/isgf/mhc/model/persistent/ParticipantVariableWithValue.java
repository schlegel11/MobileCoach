package org.isgf.mhc.model.persistent;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.persistent.concepts.AbstractVariableWithValue;

/**
 * {@link ModelObject} to represent an {@link ParticipantVariableWithValue}
 * 
 * SystemVariables belong to the referenced {@link Participant} and consist of a name
 * and a value.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public class ParticipantVariableWithValue extends AbstractVariableWithValue {
	/**
	 * Default constructor
	 */
	public ParticipantVariableWithValue(final ObjectId participant,
			final String name, final String value) {
		super(name, value);

		this.participant = participant;
	}

	/**
	 * {@link Participant} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	participant;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.isgf.mhc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}
}
