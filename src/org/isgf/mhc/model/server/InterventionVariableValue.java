package org.isgf.mhc.model.server;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.concepts.AbstractVariableValue;

/**
 * {@link ModelObject} to represent an {@link InterventionVariableValue}
 * 
 * Variables belong to the referenced {@link Intervention} and consist of a name
 * and a value.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public class InterventionVariableValue extends AbstractVariableValue {
	/**
	 * Default constructor
	 */
	public InterventionVariableValue(final ObjectId intervention,
			final String name, final String value) {
		super(name, value);

		this.intervention = intervention;
	}

	/**
	 * {@link Intervention} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	private ObjectId	intervention;
}
