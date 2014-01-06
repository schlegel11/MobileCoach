package org.isgf.mhc.model.server;

import lombok.Getter;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.concepts.AbstractVariableValue;
import org.jongo.Oid;

/**
 * {@link ModelObject} to represent an {@link InterventionVariableValue}
 * 
 * Variables belong to the referenced {@link Intervention} and consist of a name
 * and a value.
 * 
 * @author Andreas Filler
 */
public class InterventionVariableValue extends AbstractVariableValue {
	/**
	 * Default constructor
	 */
	public InterventionVariableValue(final Oid intervention, final String name,
			final String value) {
		super(name, value);

		this.intervention = intervention;
	}

	/**
	 * {@link Intervention} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	private Oid	intervention;
}
