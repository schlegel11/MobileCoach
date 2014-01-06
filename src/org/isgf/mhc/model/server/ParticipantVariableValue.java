package org.isgf.mhc.model.server;

import lombok.Getter;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.concepts.AbstractVariableValue;
import org.jongo.Oid;

/**
 * {@link ModelObject} to represent an {@link ParticipantVariableValue}
 * 
 * Variables belong to the referenced {@link Participant} and consist of a name
 * and a value.
 * 
 * @author Andreas Filler
 */
public class ParticipantVariableValue extends AbstractVariableValue {
	/**
	 * Default constructor
	 */
	public ParticipantVariableValue(final Oid participant, final String name,
			final String value) {
		super(name, value);

		this.participant = participant;
	}

	/**
	 * {@link Participant} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	private Oid	participant;
}
