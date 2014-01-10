package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;

/**
 * {@link ModelObject} to represent an {@link AuthorInterventionAccess}
 * 
 * The {@link AuthorInterventionAccess} describes, which {@link Author} is
 * allowed to administrate a specific {@link Intervention}.
 * 
 * @author Andreas Filler
 */
@AllArgsConstructor
public class AuthorInterventionAccess extends ModelObject {
	/**
	 * {@link Author} who is allowed to administrate {@link Intervention}
	 */
	@Getter
	@Setter
	private ObjectId	author;

	/**
	 * {@link Intervention} that can be administrated by the {@link Author}
	 */
	@Getter
	@Setter
	private ObjectId	intervention;
}
