package ch.ethz.mc.model;

/**
 * Contains all queries required to retrieve {@link ModelObject}s from the
 * database
 * 
 * @author Andreas Filler
 */
public class Queries {
	public static final String	ALL																													= "{}";

	public static final String	INTERVENTION__ACTIVE_TRUE																							= "{'active':true}";
	public static final String	INTERVENTION__ACTIVE_TRUE_MONITORING_ACTIVE_TRUE																	= "{'active':true,'monitoringActive':true}";

	public static final String	SCREENING_SURVEY__ACTIVE_TRUE																						= "{'active':true}";
	public static final String	SCREENING_SURVEY__BY_INTERVENTION_AND_ACTIVE_TRUE																	= "{'intervention':#,'active':true}";
	public static final String	SCREENING_SURVEY__BY_INTERVENTION_AND_GLOBAL_UNIQUE_ID																= "{'intervention':#,'globalUniqueId':#}";

	public static final String	AUTHOR__BY_USERNAME																									= "{'username':#}";
	public static final String	AUTHOR__ADMIN_TRUE																									= "{'admin':true}";

	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR																				= "{'author':#}";
	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_INTERVENTION																			= "{'intervention':#}";
	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR_AND_INTERVENTION																= "{'author':#,'intervention':#}";

	public static final String	INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION																	= "{'intervention':#}";
	public static final String	INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME															= "{'intervention':#,'name':#}";

	public static final String	MONITORING_MESSAGE_GROUP__BY_INTERVENTION																			= "{'intervention':#}";
	public static final String	MONITORING_MESSAGE_GROUP__BY_INTERVENTION_AND_ORDER_LOWER															= "{'intervention':#,'order':{$lt:#}}";
	public static final String	MONITORING_MESSAGE_GROUP__BY_INTERVENTION_AND_ORDER_HIGHER															= "{'intervention':#,'order':{$gt:#}}";
	public static final String	MONITORING_MESSAGE_GROUP__BY_INTERVENTION_AND_EXPECTING_ANSWER														= "{'intervention':#,'messagesExpectAnswer':#}";
	public static final String	MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_ASC																			= "{'order':1}";
	public static final String	MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_DESC																		= "{'order':-1}";

	public static final String	MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP																		= "{'monitoringMessageGroup':#}";
	public static final String	MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP_AND_ORDER_LOWER														= "{'monitoringMessageGroup':#,'order':{$lt:#}}";
	public static final String	MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP_AND_ORDER_HIGHER													= "{'monitoringMessageGroup':#,'order':{$gt:#}}";
	public static final String	MONITORING_MESSAGE__SORT_BY_ORDER_ASC																				= "{'order':1}";
	public static final String	MONITORING_MESSAGE__SORT_BY_ORDER_DESC																				= "{'order':-1}";

	public static final String	MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE																		= "{'belongingMonitoringMessage':#}";
	public static final String	MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE_AND_ORDER_LOWER														= "{'belongingMonitoringMessage':#,'order':{$lt:#}}";
	public static final String	MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE_AND_ORDER_HIGHER														= "{'belongingMonitoringMessage':#,'order':{$gt:#}}";
	public static final String	MONITORING_MESSAGE_RULE__SORT_BY_ORDER_ASC																			= "{'order':1}";
	public static final String	MONITORING_MESSAGE_RULE__SORT_BY_ORDER_DESC																			= "{'order':-1}";

	public static final String	SCREENING_SURVEY__BY_INTERVENTION																					= "{'intervention':#}";

	public static final String	SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY																			= "{'screeningSurvey':#}";
	public static final String	SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY_AND_ORDER_LOWER															= "{'screeningSurvey':#,'order':{$lt:#}}";
	public static final String	SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY_AND_ORDER_HIGHER														= "{'screeningSurvey':#,'order':{$gt:#}}";
	public static final String	SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_ASC																			= "{'order':1}";
	public static final String	SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_DESC																			= "{'order':-1}";

	public static final String	SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE																= "{'belongingScreeningSurveySlide':#}";
	public static final String	SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE_AND_ORDER_LOWER												= "{'belongingScreeningSurveySlide':#,'order':{$lt:#}}";
	public static final String	SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE_AND_ORDER_HIGHER												= "{'belongingScreeningSurveySlide':#,'order':{$gt:#}}";
	public static final String	SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_ASC																		= "{'order':1}";
	public static final String	SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_DESC																		= "{'order':-1}";

	public static final String	FEEDBACK__BY_SCREENING_SURVEY																						= "{'screeningSurvey':#}";
	public static final String	FEEDBACK__BY_SCREENING_SURVEY_AND_ORDER_LOWER																		= "{'screeningSurvey':#,'order':{$lt:#}}";
	public static final String	FEEDBACK__BY_SCREENING_SURVEY_AND_ORDER_HIGHER																		= "{'screeningSurvey':#,'order':{$gt:#}}";
	public static final String	FEEDBACK__BY_SCREENING_SURVEY_AND_GLOBAL_UNIQUE_ID																	= "{'screeningSurvey':#,'globalUniqueId':#}";
	public static final String	FEEDBACK__SORT_BY_ORDER_ASC																							= "{'order':1}";
	public static final String	FEEDBACK__SORT_BY_ORDER_DESC																						= "{'order':-1}";

	public static final String	FEEDBACK_SLIDE__BY_FEEDBACK																							= "{'feedback':#}";
	public static final String	FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_LOWER																			= "{'feedback':#,'order':{$lt:#}}";
	public static final String	FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_HIGHER																		= "{'feedback':#,'order':{$gt:#}}";
	public static final String	FEEDBACK_SLIDE__SORT_BY_ORDER_ASC																					= "{'order':1}";
	public static final String	FEEDBACK_SLIDE__SORT_BY_ORDER_DESC																					= "{'order':-1}";

	public static final String	FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE																				= "{'belongingFeedbackSlide':#}";
	public static final String	FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE_AND_ORDER_LOWER																= "{'belongingFeedbackSlide':#,'order':{$lt:#}}";
	public static final String	FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE_AND_ORDER_HIGHER																= "{'belongingFeedbackSlide':#,'order':{$gt:#}}";
	public static final String	FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_ASC																				= "{'order':1}";
	public static final String	FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_DESC																				= "{'order':-1}";

	public static final String	MONITORING_RULE__BY_INTERVENTION																					= "{'intervention':#}";
	public static final String	MONITORING_RULE__BY_INTERVENTION_AND_PARENT																			= "{'intervention':#,'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_RULE__BY_PARENT																							= "{'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_RULE__SORT_BY_ORDER_ASC																					= "{'order':1}";
	public static final String	MONITORING_RULE__SORT_BY_ORDER_DESC																					= "{'order':-1}";
	public static final String	MONITORING_RULE__BY_INTERVENTION_AND_PARENT_AND_ORDER_HIGHER														= "{'intervention':#,'isSubRuleOfMonitoringRule':#,'order':{$gt:#}}";

	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_ONLY_GOT_ANSWER															= "{'isGotAnswerRuleForMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_ONLY_GOT_NO_ANSWER														= "{'isGotNoAnswerRuleForMonitoringRule':#}}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER												= "{'isGotAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER												= "{'isGotNoAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__BY_PARENT																					= "{'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC																			= "{'order':1}";
	public static final String	MONITORING_REPLY_RULE__SORT_BY_ORDER_DESC																			= "{'order':-1}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_AND_ORDER_HIGHER_ONLY_GOT_ANSWER								= "{'isGotAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#,'order':{$gt:#}}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_AND_ORDER_HIGHER_ONLY_GOT_NO_ANSWER							= "{'isGotNoAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#,'order':{$gt:#}}";

	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE																			= "{$or:[{'isGotAnswerRuleForMonitoringRule':#},{'isGotNoAnswerRuleForMonitoringRule':#}]}";

	public static final String	MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_SHORT_ID																		= "{'shortId':#}";
	public static final String	MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_RELATED_DIALOG_MESSAGE														= "{'dialogMessage':#}";
	public static final String	MEDIA_OBJECT_PARTICIPANT_SHORT_URL__SORT_BY_SHORT_ID_DESC															= "{'shortId':-1}";

	public static final String	PARTICIPANT__BY_INTERVENTION																						= "{'intervention':#}";
	public static final String	PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE																= "{'intervention':#,'monitoringActive':true}";

	public static final String	DIALOG_OPTION__BY_PARTICIPANT																						= "{'participant':#}";
	public static final String	DIALOG_OPTION__BY_PARTICIPANT_AND_TYPE																				= "{'participant':#,'type':#}";
	public static final String	DIALOG_OPTION__BY_TYPE_AND_DATA																						= "{'type':#,'data':#}";

	public static final String	DIALOG_STATUS__BY_PARTICIPANT																						= "{'participant':#}";

	public static final String	PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT																		= "{'participant':#}";
	public static final String	PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_VARIABLE_NAME													= "{'participant':#,'name':#}";
	public static final String	PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_VARIABLE_NAME_AND_LAST_UPDATED_HIGHER							= "{'participant':#,'name':#,'lastUpdated':{$gt:#}}";

	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT																						= "{'participant':#}";
	public static final String	DIALOG_MESSAGE__BY_RELATED_MONITORING_MESSAGE_AND_SENT_AFTER_TIMESTAMP												= "{'relatedMonitoringMessage':#,'sentTimestamp':{$gt:#}}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS																			= "{'participant':#,'status':#}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_NOT_AUTOMATICALLY_PROCESSABLE_AND_UNANSWERED_AFTER_TIMESTAMP_HIGHER	= "{'participant':#,'status':#,'answerNotAutomaticallyProcessable':#,'isUnansweredAfterTimestamp':{$gt:#}}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_OR_STATUS_AND_NOT_AUTOMATICALLY_PROCESSABLE								= "{'participant':#,$or:[{'status':#},{'status':#}],'answerNotAutomaticallyProcessable':#}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_UNANSWERED_AFTER_TIMESTAMP_LOWER										= "{'participant':#,'status':#,'isUnansweredAfterTimestamp':{$lt:#}}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_SHOULD_BE_SENT_TIMESTAMP_LOWER										= "{'participant':#,'status':#,'shouldBeSentTimestamp':{$lt:#}}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_RELATED_MONITORING_MESSAGE														= "{'participant':#,'relatedMonitoringMessage':#}";
	public static final String	DIALOG_MESSAGE__BY_STATUS																							= "{'status':#}";
	public static final String	DIALOG_MESSAGE__SORT_BY_ORDER_ASC																					= "{'order':1}";
	public static final String	DIALOG_MESSAGE__SORT_BY_ORDER_DESC																					= "{'order':-1}";
	public static final String	DIALOG_MESSAGE__SORT_BY_SENT_TIMESTAMP_DESC																			= "{'sentTimestamp':-1}";
}