package org.isgf.mhc.model;

/**
 * Contains all queries required to retrieve {@link ModelObject}s from the
 * database
 * 
 * @author Andreas Filler
 */
public class Queries {
	public static final String	ALL														= "{}";

	public static final String	SCREENING_SURVEY__ACTIVE_TRUE							= "{'active':true}";

	public static final String	AUTHOR__BY_USERNAME										= "{'username':#}";
	public static final String	AUTHOR__ADMIN_TRUE										= "{'admin':true}";

	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR					= "{'author':#}";
	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_INTERVENTION				= "{'intervention':#}";
	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR_AND_INTERVENTION	= "{'author':#,'intervention':#}";
}