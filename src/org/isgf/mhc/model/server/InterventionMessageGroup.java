package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;
import org.jongo.Oid;

/**
 * {@link ModelObject} to represent an {@link InterventionMessageGroup}
 * 
 * An {@link InterventionMessageGroup} contains one ore more
 * {@link InterventionMessage}s that will be sent to {@link Participant}s during
 * an {@link Intervention}.
 * 
 * @author Andreas Filler
 */
@AllArgsConstructor
public class InterventionMessageGroup extends ModelObject {
	/**
	 * The {@link Intervention} the {@link InterventionMessageGroup} belongs to
	 */
	@Getter
	@Setter
	private Oid		intervention;

	/**
	 * The name of the {@link InterventionMessageGroup} as shown in the backend
	 */
	@Getter
	@Setter
	private String	name;

	/**
	 * Defines if the {@link InterventionMessage}s in the group will be sent in
	 * random order or in
	 * the order as they are stored in the {@link InterventionMessageGroup}
	 */
	@Getter
	@Setter
	private boolean	sendInRandomOrder;
}
