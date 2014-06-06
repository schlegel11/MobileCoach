package ch.ethz.mc.model;

import java.util.Hashtable;

import lombok.val;
import ch.ethz.mc.model.persistent.Author;

/**
 * Describes all indices that shall be created in the database
 * 
 * @author Andreas Filler
 */
public class Indices {
	private static final String[]	authorIndices	= new String[] { "{'username':1}" };

	/**
	 * Creates a hashtable containing all indices for all {@link ModelObject}
	 * 
	 * @return
	 */
	public static Hashtable<Class<? extends ModelObject>, String[]> getIndices() {
		val indices = new Hashtable<Class<? extends ModelObject>, String[]>();

		indices.put(Author.class, authorIndices);

		return indices;
	}
}
