package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;
import org.jongo.Oid;

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
	private Oid	author;

	/**
	 * {@link Intervention} that can be administrated by the {@link Author}
	 */
	@Getter
	@Setter
	private Oid	intervention;
}
