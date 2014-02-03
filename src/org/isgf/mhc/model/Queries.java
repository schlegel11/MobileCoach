package org.isgf.mhc.model;

/**
 * Contains all querys required to retrieve {@link ModelObject}s from the
 * database
 * 
 * @author Andreas Filler
 */
public class Queries {
	public static final String	ALL						= "{}";

	public static final String	SCREENING_SURVEYS_OPEN	= "{'active':true}";

	public static final String	AUTHOR_BY_USERNAME		= "{'username':#}";
	public static final String	AUTHORS_ADMINS			= "{'admin':true}";
}
