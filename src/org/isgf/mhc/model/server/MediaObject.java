package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.types.MediaObjectTypes;

/**
 * {@link ModelObject} to represent an {@link MediaObject}
 * 
 * {@link MediaObject}s represent media files (e.g. images or videos) that are
 * integrated in messages. They consist of a type description and a file
 * reference.
 * 
 * @author Andreas Filler
 */
@AllArgsConstructor
public class MediaObject extends ModelObject {
	/**
	 * The type of the {@link MediaObject}
	 */
	@Getter
	@Setter
	private MediaObjectTypes	type;

	/**
	 * The reference to the file on the server
	 */
	@Getter
	@Setter
	private String				file;
}
