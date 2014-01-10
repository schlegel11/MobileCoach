package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;

/**
 * {@link ModelObject} to represent an {@link InterventionMessageGroup}
 * 
 * An {@link InterventionMessageGroup} contains one ore more
 * {@link InterventionMessage}s that will be sent to {@link Participant}s during
 * an {@link Intervention}.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class InterventionMessageGroup extends ModelObject {
	/**
	 * The {@link Intervention} the {@link InterventionMessageGroup} belongs to
	 */
	@Getter
	@Setter
	private ObjectId	intervention;

	/**
	 * The name of the {@link InterventionMessageGroup} as shown in the backend
	 */
	@Getter
	@Setter
	private String		name;

	/**
	 * Defines if the {@link InterventionMessage}s in the group will be sent in
	 * random order or in
	 * the order as they are stored in the {@link InterventionMessageGroup}
	 */
	@Getter
	@Setter
	private boolean		sendInRandomOrder;
}
