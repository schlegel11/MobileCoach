package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;
import org.jongo.Oid;

/**
 * {@link ModelObject} to represent an {@link InterventionMessage}
 * 
 * {@link InterventionMessage}s will be sent to the {@link Participant} during
 * an {@link Intervention}. {@link InterventionMessage}s are grouped in
 * {@link InterventionMessageGroup}s.
 * 
 * @author Andreas Filler <andreas@filler.name>
 */
@AllArgsConstructor
public class InterventionMessage extends ModelObject {
	/**
	 * The {@link InterventionMessageGroup} this {@link InterventionMessage}
	 * belongs to
	 */
	@Getter
	@Setter
	private Oid		interventionMessageGroup;

	/**
	 * The message text containing placeholders for variables
	 */
	@Getter
	@Setter
	private String	textWithPlaceholders;

	/**
	 * <strong>OPTIONAL:</strong> The {@link MediaObject} used/presented in this
	 * {@link InterventionMessage}
	 */
	@Getter
	@Setter
	private Oid		linkedMediaObject;

	/**
	 * <strong>OPTIONAL:</strong> If the result of the
	 * {@link InterventionMessage} should be
	 * stored, the name of the appropriate variable can be set here.
	 */
	@Getter
	@Setter
	private String	storeValueToVariableWithName;
}
