package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;

/**
 * {@link ModelObject} to represent an {@link Author}
 * 
 * Authors are the backend users of the system. They can be normal authors or
 * administrators.
 * 
 * @author Andreas Filler <andreas@filler.name>
 */
@AllArgsConstructor
public class Author extends ModelObject {
	/**
	 * Admin rights of {@link Author}
	 */
	@Getter
	@Setter
	private boolean	admin;

	/**
	 * Username of {@link Author} required to authenticate
	 */
	@Getter
	@Setter
	private String	username;

	/**
	 * Hash of password of {@link Author} required to authenticate
	 */
	@Getter
	@Setter
	private String	passwordHash;
}
