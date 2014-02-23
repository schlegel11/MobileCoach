package org.isgf.mhc.model;

/**
 * Contains all queries required to retrieve {@link ModelObject}s from the
 * database
 * 
 * @author Andreas Filler
 */
public class Queries {
	public static final String	ALL																	= "{}";

	public static final String	SCREENING_SURVEY__ACTIVE_TRUE										= "{'active':true}";

	public static final String	AUTHOR__BY_USERNAME													= "{'username':#}";
	public static final String	AUTHOR__ADMIN_TRUE													= "{'admin':true}";

	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR								= "{'author':#}";
	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_INTERVENTION							= "{'intervention':#}";
	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR_AND_INTERVENTION				= "{'author':#,'intervention':#}";

	public static final String	INTERVENTION_VARIABLES_WITH_VALUES__BY_INTERVENTION					= "{'intervention':#}";
	public static final String	INTERVENTION_VARIABLES_WITH_VALUES__BY_INTERVENTION_AND_NAME		= "{'intervention':#,'name':#}";

	public static final String	MONITORING_MESSAGE_GROUPS__BY_INTERVENTION							= "{'intervention':#}";
	public static final String	MONITORING_MESSAGE_GROUPS__SORT_BY_NAME_ASC							= "{'name':1}";

	public static final String	MONITORING_MESSAGES__BY_MONITORING_MESSAGE_GROUP					= "{'monitoringMessageGroup':#}";
	public static final String	MONITORING_MESSAGES__BY_MONITORING_MESSAGE_GROUP_AND_ORDER_LOWER	= "{'monitoringMessageGroup':#,'order':{$lt:#}}";
	public static final String	MONITORING_MESSAGES__BY_MONITORING_MESSAGE_GROUP_AND_ORDER_HIGHER	= "{'monitoringMessageGroup':#,'order':{$gt:#}}";
	public static final String	MONITORING_MESSAGES__SORT_BY_ORDER_ASC								= "{'order':1}";
	public static final String	MONITORING_MESSAGES__SORT_BY_ORDER_DESC								= "{'order':-1}";
}
