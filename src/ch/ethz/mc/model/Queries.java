package ch.ethz.mc.model;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * Contains all queries required to retrieve {@link ModelObject}s from the
 * database
 *
 * @author Andreas Filler
 */
public class Queries {
	public static final String	ALL																																																				= "{}";
	public static final String	OBJECT_ID																																																		= "{'_id':#}";

	public static final String	INTERVENTION__ACTIVE_TRUE																																														= "{'active':true}";
	public static final String	INTERVENTION__ACTIVE_TRUE_AND_AUTOMATICALLY_FINISH_SCREENING_SURVEYS_TRUE																																		= "{'active':true,'automaticallyFinishScreeningSurveys':true}";
	public static final String	INTERVENTION__ACTIVE_TRUE_MONITORING_ACTIVE_TRUE																																								= "{'active':true,'monitoringActive':true}";

	public static final String	SCREENING_SURVEY__ACTIVE_TRUE																																													= "{'active':true}";
	public static final String	SCREENING_SURVEY__BY_INTERVENTION_AND_ACTIVE_TRUE																																								= "{'intervention':#,'active':true}";
	public static final String	SCREENING_SURVEY__BY_INTERVENTION_AND_ACTIVE_TRUE_AND_INTERMEDIATE_SURVEY_FALSE																																	= "{'intervention':#,'active':true,'intermediateSurvey':false}";
	public static final String	SCREENING_SURVEY__BY_INTERVENTION_AND_INTERMEDIATE_SURVEY_TRUE																																					= "{'intervention':#,'intermediateSurvey':true}";
	public static final String	SCREENING_SURVEY__BY_INTERVENTION_AND_GLOBAL_UNIQUE_ID																																							= "{'intervention':#,'globalUniqueId':#}";

	public static final String	AUTHOR__BY_USERNAME																																																= "{'username':#}";
	public static final String	AUTHOR__ADMIN_TRUE																																																= "{'admin':true}";

	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR																																											= "{'author':#}";
	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_INTERVENTION																																										= "{'intervention':#}";
	public static final String	AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR_AND_INTERVENTION																																							= "{'author':#,'intervention':#}";

	public static final String	INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION																																								= "{'intervention':#}";
	public static final String	INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME																																						= "{'intervention':#,'name':#}";

	public static final String	MONITORING_MESSAGE_GROUP__BY_INTERVENTION																																										= "{'intervention':#}";
	public static final String	MONITORING_MESSAGE_GROUP__BY_INTERVENTION_AND_ORDER_LOWER																																						= "{'intervention':#,'order':{$lt:#}}";
	public static final String	MONITORING_MESSAGE_GROUP__BY_INTERVENTION_AND_ORDER_HIGHER																																						= "{'intervention':#,'order':{$gt:#}}";
	public static final String	MONITORING_MESSAGE_GROUP__BY_INTERVENTION_AND_EXPECTING_ANSWER																																					= "{'intervention':#,'messagesExpectAnswer':#}";
	public static final String	MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_ASC																																										= "{'order':1}";
	public static final String	MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_DESC																																									= "{'order':-1}";

	public static final String	MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP																																									= "{'monitoringMessageGroup':#}";
	public static final String	MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP_AND_ORDER_LOWER																																					= "{'monitoringMessageGroup':#,'order':{$lt:#}}";
	public static final String	MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP_AND_ORDER_HIGHER																																				= "{'monitoringMessageGroup':#,'order':{$gt:#}}";
	public static final String	MONITORING_MESSAGE__BY_LINKED_INTERMEDIATE_SURVEY																																								= "{'linkedIntermediateSurvey':#}";
	public static final String	MONITORING_MESSAGE__SORT_BY_ORDER_ASC																																											= "{'order':1}";
	public static final String	MONITORING_MESSAGE__SORT_BY_ORDER_DESC																																											= "{'order':-1}";

	public static final String	MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE																																									= "{'belongingMonitoringMessage':#}";
	public static final String	MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE_AND_ORDER_LOWER																																					= "{'belongingMonitoringMessage':#,'order':{$lt:#}}";
	public static final String	MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE_AND_ORDER_HIGHER																																					= "{'belongingMonitoringMessage':#,'order':{$gt:#}}";
	public static final String	MONITORING_MESSAGE_RULE__SORT_BY_ORDER_ASC																																										= "{'order':1}";
	public static final String	MONITORING_MESSAGE_RULE__SORT_BY_ORDER_DESC																																										= "{'order':-1}";

	public static final String	MICRO_DIALOG__BY_INTERVENTION																																													= "{'intervention':#}";
	public static final String	MICRO_DIALOG__BY_INTERVENTION_AND_ORDER_LOWER																																									= "{'intervention':#,'order':{$lt:#}}";
	public static final String	MICRO_DIALOG__BY_INTERVENTION_AND_ORDER_HIGHER																																									= "{'intervention':#,'order':{$gt:#}}";
	public static final String	MICRO_DIALOG__SORT_BY_ORDER_ASC																																													= "{'order':1}";
	public static final String	MICRO_DIALOG__SORT_BY_ORDER_DESC																																												= "{'order':-1}";

	public static final String	MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG																																											= "{'microDialog':#}";
	public static final String	MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG_AND_ORDER_LOWER																																							= "{'microDialog':#,'order':{$lt:#}}";
	public static final String	MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG_AND_ORDER_HIGHER																																							= "{'microDialog':#,'order':{$gt:#}}";
	public static final String	MICRO_DIALOG_MESSAGE__BY_LINKED_INTERMEDIATE_SURVEY																																								= "{'linkedIntermediateSurvey':#}";
	public static final String	MICRO_DIALOG_MESSAGE__SORT_BY_ORDER_ASC																																											= "{'order':1}";
	public static final String	MICRO_DIALOG_MESSAGE__SORT_BY_ORDER_DESC																																										= "{'order':-1}";

	public static final String	MICRO_DIALOG_MESSAGE_RULE__BY_MICRO_DIALOG_MESSAGE																																								= "{'belongingMicroDialogMessage':#}";
	public static final String	MICRO_DIALOG_MESSAGE_RULE__BY_MICRO_DIALOG_MESSAGE_AND_ORDER_LOWER																																				= "{'belongingMicroDialogMessage':#,'order':{$lt:#}}";
	public static final String	MICRO_DIALOG_MESSAGE_RULE__BY_MICRO_DIALOG_MESSAGE_AND_ORDER_HIGHER																																				= "{'belongingMicroDialogMessage':#,'order':{$gt:#}}";
	public static final String	MICRO_DIALOG_MESSAGE_RULE__SORT_BY_ORDER_ASC																																									= "{'order':1}";
	public static final String	MICRO_DIALOG_MESSAGE_RULE__SORT_BY_ORDER_DESC																																									= "{'order':-1}";

	public static final String	MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG																																									= "{'microDialog':#}";
	public static final String	MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG_AND_ORDER_LOWER																																					= "{'microDialog':#,'order':{$lt:#}}";
	public static final String	MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG_AND_ORDER_HIGHER																																					= "{'microDialog':#,'order':{$gt:#}}";
	public static final String	MICRO_DIALOG_DECISION_POINT__SORT_BY_ORDER_ASC																																									= "{'order':1}";
	public static final String	MICRO_DIALOG_DECISION_POINT__SORT_BY_ORDER_DESC																																									= "{'order':-1}";

	public static final String	MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT																																								= "{'microDialogDecisionPoint':#}";
	public static final String	MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT_AND_PARENT																																					= "{'microDialogDecisionPoint':#,'isSubRuleOfMonitoringRule':#}";
	public static final String	MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT_AND_PARENT_AND_ORDER_HIGHER																																	= "{'microDialogDecisionPoint':#,'isSubRuleOfMonitoringRule':#,'order':{$gt:#}}";
	public static final String	MICRO_DIALOG_RULE__BY_PARENT																																													= "{'isSubRuleOfMonitoringRule':#}";
	public static final String	MICRO_DIALOG_RULE__BY_NEXT_MICRO_DIALOG_MESSAGE_WHEN_TRUE																																						= "{'nextMicroDialogMessageWhenTrue':#}";
	public static final String	MICRO_DIALOG_RULE__BY_NEXT_MICRO_DIALOG_MESSAGE_WHEN_FALSE																																						= "{'nextMicroDialogMessageWhenFalse':#}";
	public static final String	MICRO_DIALOG_RULE__SORT_BY_ORDER_ASC																																											= "{'order':1}";
	public static final String	MICRO_DIALOG_RULE__SORT_BY_ORDER_DESC																																											= "{'order':-1}";

	public static final String	SCREENING_SURVEY__BY_INTERVENTION																																												= "{'intervention':#}";

	public static final String	SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY																																										= "{'screeningSurvey':#}";
	public static final String	SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY_AND_ORDER_LOWER																																						= "{'screeningSurvey':#,'order':{$lt:#}}";
	public static final String	SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY_AND_ORDER_HIGHER																																					= "{'screeningSurvey':#,'order':{$gt:#}}";
	public static final String	SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY_AND_GLOBAL_UNIQUE_ID																																				= "{'screeningSurvey':#,'globalUniqueId':#}";
	public static final String	SCREENING_SURVEY_SLIDE__BY_LINKED_INTERMEDIATE_SURVEY																																							= "{'linkedIntermediateSurvey':#}";
	public static final String	SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_ASC																																										= "{'order':1}";
	public static final String	SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_DESC																																										= "{'order':-1}";

	public static final String	SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE																																							= "{'belongingScreeningSurveySlide':#}";
	public static final String	SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE_AND_ORDER_LOWER																																			= "{'belongingScreeningSurveySlide':#,'order':{$lt:#}}";
	public static final String	SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE_AND_ORDER_HIGHER																																			= "{'belongingScreeningSurveySlide':#,'order':{$gt:#}}";
	public static final String	SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_ASC																																									= "{'order':1}";
	public static final String	SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_DESC																																									= "{'order':-1}";
	public static final String	SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE_AND_NEXT_SCREENING_SURVEY_SLIDE_WHEN_TRUE																												= "{'belongingScreeningSurveySlide':#,'nextScreeningSurveySlideWhenTrue':#}";
	public static final String	SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE_AND_NEXT_SCREENING_SURVEY_SLIDE_WHEN_FALSE																												= "{'belongingScreeningSurveySlide':#,'nextScreeningSurveySlideWhenFalse':#}";

	public static final String	INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_PARTICIPANT																																			= "{'participant':#}";
	public static final String	INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_SURVEY																																				= "{'survey':#}";
	public static final String	INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_FEEDBACK																																				= "{'feedback':#}";
	public static final String	INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_PARTICIPANT_AND_SURVEY																																= "{'participant':#,'survey':#}";
	public static final String	INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_PARTICIPANT_AND_FEEDBACK																																= "{'participant':#,'feedback':#}";
	public static final String	INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_SHORT_ID																																				= "{'shortId':#}";
	public static final String	INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__SORT_BY_SHORT_ID_DESC																																	= "{'shortId':-1}";

	public static final String	FEEDBACK__BY_SCREENING_SURVEY																																													= "{'screeningSurvey':#}";
	public static final String	FEEDBACK__BY_SCREENING_SURVEY_AND_ORDER_LOWER																																									= "{'screeningSurvey':#,'order':{$lt:#}}";
	public static final String	FEEDBACK__BY_SCREENING_SURVEY_AND_ORDER_HIGHER																																									= "{'screeningSurvey':#,'order':{$gt:#}}";
	public static final String	FEEDBACK__BY_SCREENING_SURVEY_AND_GLOBAL_UNIQUE_ID																																								= "{'screeningSurvey':#,'globalUniqueId':#}";
	public static final String	FEEDBACK__SORT_BY_ORDER_ASC																																														= "{'order':1}";
	public static final String	FEEDBACK__SORT_BY_ORDER_DESC																																													= "{'order':-1}";

	public static final String	FEEDBACK_SLIDE__BY_FEEDBACK																																														= "{'feedback':#}";
	public static final String	FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_LOWER																																										= "{'feedback':#,'order':{$lt:#}}";
	public static final String	FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_HIGHER																																									= "{'feedback':#,'order':{$gt:#}}";
	public static final String	FEEDBACK_SLIDE__SORT_BY_ORDER_ASC																																												= "{'order':1}";
	public static final String	FEEDBACK_SLIDE__SORT_BY_ORDER_DESC																																												= "{'order':-1}";

	public static final String	FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE																																											= "{'belongingFeedbackSlide':#}";
	public static final String	FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE_AND_ORDER_LOWER																																							= "{'belongingFeedbackSlide':#,'order':{$lt:#}}";
	public static final String	FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE_AND_ORDER_HIGHER																																							= "{'belongingFeedbackSlide':#,'order':{$gt:#}}";
	public static final String	FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_ASC																																											= "{'order':1}";
	public static final String	FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_DESC																																											= "{'order':-1}";

	public static final String	MONITORING_RULE__BY_INTERVENTION																																												= "{'intervention':#}";
	public static final String	MONITORING_RULE__BY_INTERVENTION_AND_PARENT																																										= "{'intervention':#,'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_RULE__BY_INTERVENTION_AND_TYPE																																										= "{'intervention':#,'type':#}";
	public static final String	MONITORING_RULE__BY_PARENT																																														= "{'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_RULE__BY_INTERVENTION_AND_PARENT_AND_ORDER_HIGHER																																					= "{'intervention':#,'isSubRuleOfMonitoringRule':#,'order':{$gt:#}}";
	public static final String	MONITORING_RULE__BY_RELATED_MONITORING_MESSAGE_GROUP																																							= "{'relatedMonitoringMessageGroup':#}";
	public static final String	MONITORING_RULE__SORT_BY_ORDER_ASC																																												= "{'order':1}";
	public static final String	MONITORING_RULE__SORT_BY_ORDER_DESC																																												= "{'order':-1}";

	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_ONLY_GOT_ANSWER																																						= "{'isGotAnswerRuleForMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_ONLY_GOT_NO_ANSWER																																					= "{'isGotNoAnswerRuleForMonitoringRule':#}}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER																																			= "{'isGotAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER																																			= "{'isGotNoAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__BY_PARENT																																												= "{'isSubRuleOfMonitoringRule':#}";
	public static final String	MONITORING_REPLY_RULE__BY_RELATED_MONITORING_MESSAGE_GROUP																																						= "{'relatedMonitoringMessageGroup':#}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_AND_ORDER_HIGHER_ONLY_GOT_ANSWER																															= "{'isGotAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#,'order':{$gt:#}}";
	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_AND_ORDER_HIGHER_ONLY_GOT_NO_ANSWER																														= "{'isGotNoAnswerRuleForMonitoringRule':#,'isSubRuleOfMonitoringRule':#,'order':{$gt:#}}";
	public static final String	MONITORING_REPLY_RULE__SORT_BY_ORDER_ASC																																										= "{'order':1}";
	public static final String	MONITORING_REPLY_RULE__SORT_BY_ORDER_DESC																																										= "{'order':-1}";

	public static final String	MONITORING_REPLY_RULE__BY_MONITORING_RULE																																										= "{$or:[{'isGotAnswerRuleForMonitoringRule':#},{'isGotNoAnswerRuleForMonitoringRule':#}]}";

	public static final String	MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_SHORT_ID																																									= "{'shortId':#}";
	public static final String	MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_MEDIA_OBJECT																																								= "{'mediaObject':#}";
	public static final String	MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_DIALOG_MESSAGE																																							= "{'dialogMessage':#}";
	public static final String	MEDIA_OBJECT_PARTICIPANT_SHORT_URL__BY_DIALOG_MESSAGE_AND_MEDIA_OBJECT																																			= "{'dialogMessage':#,'mediaObject':#}";
	public static final String	MEDIA_OBJECT_PARTICIPANT_SHORT_URL__SORT_BY_SHORT_ID_DESC																																						= "{'shortId':-1}";

	public static final String	PARTICIPANT__BY_INTERVENTION																																													= "{'intervention':#}";
	public static final String	PARTICIPANT__BY_INTERVENTION_AND_GROUP_AND_MONITORING_ACTIVE_TRUE																																				= "{'intervention':#,'group':#,'monitoringActive':true}";
	public static final String	PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE																																							= "{'intervention':#,'monitoringActive':true}";

	public static final String	DIALOG_OPTION__BY_PARTICIPANT																																													= "{'participant':#}";
	public static final String	DIALOG_OPTION__FOR_PARTICIPANT_BY_PARTICIPANT																																									= "{'participant':#,$or:[{'type':'SMS'},{'type':'EMAIL'},{'type':'EXTERNAL_ID'}]}";
	public static final String	DIALOG_OPTION__FOR_SUPERVISOR_BY_PARTICIPANT																																									= "{'participant':#,$or:[{'type':'SUPERVISOR_SMS'},{'type':'SUPERVISOR_EMAIL'},{'type':'SUPERVISOR_EXTERNAL_ID'}]}";
	public static final String	DIALOG_OPTION__BY_PARTICIPANT_AND_TYPE																																											= "{'participant':#,'type':#}";
	public static final String	DIALOG_OPTION__BY_TYPE_AND_DATA																																													= "{'type':#,'data':#}";

	public static final String	DIALOG_STATUS__BY_PARTICIPANT																																													= "{'participant':#}";
	public static final String	DIALOG_STATUS__BY_PARTICIPANT_AND_LAST_VISITED_SCREENING_SURVEY_SLIDE_TIMESTAMP_LOWER_AND_DATA_FOR_MONITORING_PARTICIPATION_AVAILABLE_TRUE_AND_SCREENING_SURVEY_PERFORMED_FALSE_AND_MONITORING_PERFORMED_FALSE	= "{'participant':#,'lastVisitedScreeningSurveySlideTimestamp':{$lt:#},'dataForMonitoringParticipationAvailable':true,'screeningSurveyPerformed':false,'monitoringPerformed':false}";
	public static final String	DIALOG_STATUS__BY_PARTICIPANT_AND_DATA_FOR_MONITORING_PARTICIPATION_AVAILABLE_TRUE_AND_SCREENING_SURVEY_PERFORMED_TRUE_AND_MONITORING_PERFORMED_FALSE															= "{'participant':#,'dataForMonitoringParticipationAvailable':true,'screeningSurveyPerformed':true,'monitoringPerformed':false}";

	public static final String	PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT																																									= "{'participant':#}";
	public static final String	PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_NAME																																						= "{'participant':#,'name':#}";
	public static final String	PARTICIPANT_VARIABLE_WITH_VALUE__BY_DESCRIBES_MEDIA_UPLOAD																																						= "{'describesMediaUpload':#}";

	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT																																													= "{'participant':#}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_MESSAGE_TYPE																																									= "{'participant':#,'supervisorMessage':#}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_ORDER																																										= "{'participant':#,'order':#}";
	public static final String	DIALOG_MESSAGE__BY_RELATED_MONITORING_MESSAGE_AND_SENT_AFTER_TIMESTAMP																																			= "{'relatedMonitoringMessage':#,'sentTimestamp':{$gt:#}}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS																																										= "{'participant':#,'status':#}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_NOT_AUTOMATICALLY_PROCESSABLE_AND_UNANSWERED_AFTER_TIMESTAMP_HIGHER																								= "{'participant':#,'status':#,'answerNotAutomaticallyProcessable':#,'isUnansweredAfterTimestamp':{$gt:#}}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_OR_STATUS_AND_NOT_AUTOMATICALLY_PROCESSABLE																															= "{'participant':#,$or:[{'status':#},{'status':#}],'answerNotAutomaticallyProcessable':#}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_UNANSWERED_AFTER_TIMESTAMP_LOWER																																	= "{'participant':#,'status':#,'isUnansweredAfterTimestamp':{$lt:#}}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_AND_SHOULD_BE_SENT_TIMESTAMP_LOWER																																	= "{'participant':#,'status':#,'shouldBeSentTimestamp':{$lt:#}}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_RELATED_MONITORING_MESSAGE																																					= "{'participant':#,'relatedMonitoringMessage':#}";
	public static final String	DIALOG_MESSAGE__BY_PARTICIPANT_AND_CLIENT_ID																																									= "{'participant':#,'clientId':#}";
	public static final String	DIALOG_MESSAGE__BY_STATUS																																														= "{'status':#}";
	public static final String	DIALOG_MESSAGE__SORT_BY_ORDER_ASC																																												= "{'order':1}";
	public static final String	DIALOG_MESSAGE__SORT_BY_ORDER_DESC																																												= "{'order':-1}";

	// Special
	public static final String	EVERYTHING																																																		= "{}";
	public static final String	UPDATE_VERSION_5__GENERAL_UPDATE_FOR_COMMENT																																									= "{$set:{'comment':''}}";
	public static final String	UPDATE_VERSION_6__INTERVENTION__CHANGE_1																																										= "{$set:{'automaticallyFinishScreeningSurveys':false}}";
	public static final String	UPDATE_VERSION_6__SCREENING_SURVEY_SLIDE__CHANGE_1																																								= "{$set:{'linkedIntermediateSurvey':null}}";
	public static final String	UPDATE_VERSION_7__MONITORING_RULE__CHANGE_1																																										= "{$set:{'sendMessageToSupervisor':false}}";
	public static final String	UPDATE_VERSION_7__MONITORING_REPLY_RULE__CHANGE_1																																								= "{$set:{'sendMessageToSupervisor':false}}";
	public static final String	UPDATE_VERSION_7__DIALOG_MESSAGE__CHANGE_1																																										= "{$set:{'supervisorMessage':false}}";
	public static final String	UPDATE_VERSION_9__INTERVENTION__CHANGE_1																																										= "{$set:{'interventionsToCheckForUniqueness':[]}}";
	public static final String	UPDATE_VERSION_9__INTERVENTION__CHANGE_2																																										= "{$set:{'monitoringStartingDays':[1]}}";
	public static final String	UPDATE_VERSION_10__INTERVENTION__CHANGE_1																																										= "{$set:{'dashboardEnabled':false}}";
	public static final String	UPDATE_VERSION_10__INTERVENTION__CHANGE_2																																										= "{$set:{'dashboardTemplatePath':''}}";
	public static final String	UPDATE_VERSION_10__INTERVENTION__CHANGE_3																																										= "{$set:{'dashboardPasswordPattern':null}}";
	public static final String	UPDATE_VERSION_11__INTERVENTION__CHANGE_1																																										= "{$set:{'deepstreamPassword':null}}";
	public static final String	UPDATE_VERSION_12__MONITORING_RULE__CHANGE_1																																									= "{$set:{'type':'NORMAL'}}";
	public static final String	UPDATE_VERSION_13__MONITORING_RULE__CHANGE_1																																									= "{$set:{'markCaseAsSolvedWhenTrue':false}}";
	public static final String	UPDATE_VERSION_14__DIALOG_MESSAGE__CHANGE_1																																										= "{$set:{'type':'PLAIN'}}";
	public static final String	UPDATE_VERSION_14__MONITORING_MESSAGE__CHANGE_1																																									= "{$set:{'commandMessage':false}}";
	public static final String	UPDATE_VERSION_15__DIALOG_MESSAGE__CHANGE_1_FIELD																																								= "message";
	public static final String	UPDATE_VERSION_15__DIALOG_MESSAGE__CHANGE_1_CHANGE																																								= "{$set:{'messageWithForcedLinks':#}}";
	public static final String	UPDATE_VERSION_15__DIALOG_MESSAGE__CHANGE_2																																										= "{$set:{'surveyLink':null}}";
	public static final String	UPDATE_VERSION_15__DIALOG_MESSAGE__CHANGE_3																																										= "{$set:{'mediaObjectLink':null}}";
	public static final String	UPDATE_VERSION_15__DIALOG_MESSAGE__CHANGE_4																																										= "{$set:{'textBasedMediaObjectContent':null}}";
	public static final String	UPDATE_VERSION_16__MONITORING_MESSAGE__CHANGE_1																																									= "{$set:{'answerType':'FREE_TEXT'}}";
	public static final String	UPDATE_VERSION_16__MONITORING_MESSAGE__CHANGE_2																																									= "{$set:{'answerOptionsWithPlaceholders':{}}}";
	public static final String	UPDATE_VERSION_16__DIALOG_MESSAGE__CHANGE_1																																										= "{$set:{'answerType':null}}";
	public static final String	UPDATE_VERSION_16__DIALOG_MESSAGE__CHANGE_2																																										= "{$set:{'answerOptions':null}}";
	public static final String	UPDATE_VERSION_16__MONITORING_RULE__CHANGE_1_FIELD																																								= "hoursUntilMessageIsHandledAsUnanswered";
	public static final String	UPDATE_VERSION_16__MONITORING_RULE__CHANGE_1_CHANGE																																								= "{$set:{'minutesUntilMessageIsHandledAsUnanswered':#}}";
	public static final String	UPDATE_VERSION_16__MONITORING_RULE__CHANGE_1_REMOVE																																								= "{$unset:{'"
			+ UPDATE_VERSION_16__MONITORING_RULE__CHANGE_1_FIELD + "':1}}";
	public static final String	UPDATE_VERSION_20__MONITORING_MESSAGE__CHANGE_1_FIELD																																							= "isCommandMessage";
	public static final String	UPDATE_VERSION_20__MONITORING_MESSAGE__CHANGE_1_CHANGE																																							= "{$set:{'commandMessage':#}}";
	public static final String	UPDATE_VERSION_20__MONITORING_MESSAGE__CHANGE_1_REMOVE																																							= "{$unset:{'"
			+ UPDATE_VERSION_20__MONITORING_MESSAGE__CHANGE_1_FIELD + "':1}}";
	public static final String	UPDATE_VERSION_20__MONITORING_RULE__CHANGE_1_FIELD																																								= "hourToSendMessage";
	public static final String	UPDATE_VERSION_20__MONITORING_RULE__CHANGE_1_CHANGE																																								= "{$set:{'hourToSendMessageOrActivateMicroDialog':#}}";
	public static final String	UPDATE_VERSION_20__MONITORING_RULE__CHANGE_1_REMOVE																																								= "{$unset:{'"
			+ UPDATE_VERSION_20__MONITORING_RULE__CHANGE_1_FIELD + "':1}}";
	public static final String	UPDATE_VERSION_20__ABSTRACT_MONITORING_RULE__CHANGE_1																																							= "{$set:{'activateMicroDialogIfTrue':false}}";
	public static final String	UPDATE_VERSION_20__ABSTRACT_MONITORING_RULE__CHANGE_2																																							= "{$set:{'relatedMicroDialog':null}}";
	public static final String	UPDATE_VERSION_20__DIALOG_MESSAGE__CHANGE_1																																										= "{$set:{'relatedMicroDialogForActivation':null}}";
	public static final String	UPDATE_VERSION_20__DIALOG_MESSAGE__CHANGE_2																																										= "{$set:{'relatedMicroDialogMessage':null}}";
	public static final String	UPDATE_VERSION_24__DIALOG_OPTION__CHANGE_1																																										= "{$set:{'pushNotificationTokens':[]}}";
	public static final String	UPDATE_VERSION_29__PARTICIPANT_VARIABLE_WITH_VAUE__CHANGE_1_SORT																																				= "{'timestamp':1}";
	public static final String	UPDATE_VERSION_30__DIALOG_MESSAGE__CHANGE_1																																										= "{$set:{'clientId':null}}";
	public static final String	UPDATE_VERSION_31__DIALOG_MESSAGE__CHANGE_1																																										= "{$set:{'messageIsSticky':false}}";
	public static final String	UPDATE_VERSION_31__MICRO_DIALOG_MESSAGE__CHANGE_1																																								= "{$set:{'messageIsSticky':false}}";
	public static final String	UPDATE_VERSION_32__MONITORING_RULE__CHANGE_1																																									= "{$set:{'variableForDecimalHourToSendMessageOrActivateMicroDialog':null}}";
	public static final String	UPDATE_VERSION_33__MONITORING_MESSAGE__CHANGE_1																																									= "{$set:{'i18nIdentifier':#}}";
	public static final String	UPDATE_VERSION_33__MICRO_DIALOG_MESSAGE__CHANGE_1																																								= "{$set:{'i18nIdentifier':#}}";
	public static final String	UPDATE_VERSION_35__SCREENING_SURVEY__CHANGE_1																																									= "{$set:{'i18nIdentifier':#}}";
	public static final String	UPDATE_VERSION_35__SCREENING_SURVEY_SLIDE__CHANGE_1																																								= "{$set:{'i18nIdentifier':#}}";
	public static final String	UPDATE_VERSION_35__FEEDBACK__CHANGE_1																																											= "{$set:{'i18nIdentifier':#}}";
	public static final String	UPDATE_VERSION_35__FEEDBACK_SLIDE__CHANGE_1																																										= "{$set:{'i18nIdentifier':#}}";
	public static final String	UPDATE_VERSION_36__SCREENING_SURVEY_SLIDE__CHANGE_1																																								= "{$set:{'provideExternalIdDialologOptionAccessData':null}}";
	public static final String	UPDATE_VERSION_37__DIALOG_MESSAGE__CHANGE_1																																										= "{$set:{'mediaObjectType':null}}";
	public static final String	UPDATE_VERSION_38__PARTICIPANT__CHANGE_1																																										= "{$set:{'lastLoginTimestamp':#}}";
	public static final String	UPDATE_VERSION_38__PARTICIPANT__CHANGE_2																																										= "{$set:{'lastLogoutTimestamp':#}}";
}
