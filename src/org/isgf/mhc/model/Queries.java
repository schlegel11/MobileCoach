package org.isgf.mhc.model;

/**
 * Contains all queries required to retrieve {@link ModelObject}s from the
 * database
 * 
 * @author Andreas Filler
 */
public class Queries {
	public static final String	ALL																							= "{}";

	public static final String	INTERVENTION__ACTIVE_TRUE																	= "{'active':true}";

	public static final String	SCREENING_SURVEY__ACTIVE_TRUE																= "{'active':true}";
	public static final String	SCREENING_SURVEY__BY_INTERVENTION_AND_ACTIVE_TRUE											= "{'intervention':#,'active':true}";

	public static final String	AUTHOR__BY_USERNAME																			= "{'username':#}";
	public static final String	AUTHOR__ADMIN_TRUE																			= "{'admin':true}";

	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR														= "{'author':#}";
	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_INTERVENTION													= "{'intervention':#}";
	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR_AND_INTERVENTION										= "{'author':#,'intervention':#}";

	public static final String	INTERVENTION_VARIABLES_WITH_VALUES__BY_INTERVENTION											= "{'intervention':#}";
	public static final String	INTERVENTION_VARIABLES_WITH_VALUES__BY_INTERVENTION_AND_NAME								= "{'intervention':#,'name':#}";

	public static final String	MONITORING_MESSAGE_GROUP__BY_INTERVENTION													= "{'intervention':#}";
	public static final String	MONITORING_MESSAGE_GROUP__SORT_BY_NAME_ASC													= "{'name':1}";

	public static final String	MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP												= "{'monitoringMessageGroup':#}";
	public static final String	MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP_AND_ORDER_LOWER								= "{'monitoringMessageGroup':#,'order':{$lt:#}}";
	public static final String	MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP_AND_ORDER_HIGHER							= "{'monitoringMessageGroup':#,'order':{$gt:#}}";
	public static final String	MONITORING_MESSAGE__SORT_BY_ORDER_ASC														= "{'order':1}";
	public static final String	MONITORING_MESSAGE__SORT_BY_ORDER_DESC														= "{'order':-1}";

	public static final String	SCREENING_SURVEY__BY_INTERVENTION															= "{'intervention':#}";

	public static final String	SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY													= "{'screeningSurvey':#}";
	public static final String	SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY_AND_ORDER_LOWER									= "{'screeningSurvey':#,'order':{$lt:#}}";
	public static final String	SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY_AND_ORDER_HIGHER								= "{'screeningSurvey':#,'order':{$gt:#}}";
	public static final String	SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_ASC													= "{'order':1}";
	public static final String	SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_DESC													= "{'order':-1}";

	public static final String	SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE										= "{'belongingScreeningSurveySlide':#}";
	public static final String	SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_ASC												= "{'order':1}";

	public static final String	FEEDBACK__BY_SCREENING_SURVEY																= "{'screeningSurvey':#}";
	public static final String	FEEDBACK__BY_SCREENING_SURVEY_AND_ORDER_LOWER												= "{'screeningSurvey':#,'order':{$lt:#}}";
	public static final String	FEEDBACK__BY_SCREENING_SURVEY_AND_ORDER_HIGHER												= "{'screeningSurvey':#,'order':{$gt:#}}";
	public static final String	FEEDBACK__SORT_BY_ORDER_ASC																	= "{'order':1}";
	public static final String	FEEDBACK__SORT_BY_ORDER_DESC																= "{'order':-1}";

	public static final String	FEEDBACK_SLIDE__BY_FEEDBACK																	= "{'feedback':#}";
	public static final String	FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_LOWER													= "{'feedback':#,'order':{$lt:#}}";
	public static final String	FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_HIGHER												= "{'feedback':#,'order':{$gt:#}}";
	public static final String	FEEDBACK_SLIDE__SORT_BY_ORDER_ASC															= "{'order':1}";
	public static final String	FEEDBACK_SLIDE__SORT_BY_ORDER_DESC															= "{'order':-1}";

	public static final String	FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE														= "{'belongingFeedbackSlide':#}";

	public static final String	MONITORING_RULE__BY_INTERVENTION															= "{'intervention':#}";
	public static final String	MONITORING_RULE__BY_INTERVENTION_AND_PARENT													= "{'intervention':#,'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_RULE__BY_PARENT																	= "{'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_RULE__SORT_BY_ORDER_ASC															= "{'order':1}";
	public static final String	MONITORING_RULE__SORT_BY_ORDER_DESC															= "{'order':-1}";
	public static final String	MONITORING_RULE__BY_INTERVENTION_AND_PARENT_AND_ORDER_HIGHER								= "{'intervention':#,'isSubRuleOfMonitoringRule':#,'order':{$gt:#}}";

	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_ONLY_GOT_ANSWER									= "{'isGotAnswerRuleForMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_ONLY_GOT_NO_ANSWER								= "{'isGotNoAnswerRuleForMonitoringRule':#}}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER						= "{'isGotAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER						= "{'isGotNoAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__BY_PARENT															= "{'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC													= "{'order':1}";
	public static final String	MONITORING_REPLY_RULE__SORT_BY_ORDER_DESC													= "{'order':-1}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_AND_ORDER_HIGHER_ONLY_GOT_ANSWER		= "{'isGotAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#,'order':{$gt:#}}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_AND_ORDER_HIGHER_ONLY_GOT_NO_ANSWER	= "{'isGotNoAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#,'order':{$gt:#}}";

	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE													= "{$or:[{'isGotAnswerRuleForMonitoringRule':#},{'isGotNoAnswerRuleForMonitoringRule':#}]}";

	public static final String	SYSTEM_UNIQUE_ID__BY_SHORT_ID																= "{'shortId':#}";
	public static final String	SYSTEM_UNIQUE_ID__SORT_BY_SHORT_ID_DESC														= "{'shortId':-1}";

	public static final String	PARTICIPANT__BY_INTERVENTION																= "{'intervention':#}";

	public static final String	DIALOG_OPTION__BY_PARTICIPANT																= "{'participant':#}";

	public static final String	PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT												= "{'participant':#}";

	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT																= "{'participant':#}";
}
