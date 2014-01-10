package org.isgf.mhc.model.server;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.concepts.AbstractVariableWithValue;

/**
 * {@link ModelObject} to represent an {@link InterventionVariableWithValue}
 * 
 * Variables belong to the referenced {@link Intervention} and consist of a name
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
}
