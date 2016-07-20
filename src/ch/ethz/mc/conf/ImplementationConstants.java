package ch.ethz.mc.conf;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Contains some implementation specific constants
 *
 * @author Andreas Filler
 */
public class ImplementationConstants {
	public static final String	SYSTEM_CONFIGURATION_PROPERTY_POSTFIX												= ".configuration";
	public static final String	LOGGING_APPLICATION_NAME															= "MC";

	public static final String	DEFAULT_OBJECT_NAME																	= "---";
	public static final String	DEFAULT_ANSWER_NAME																	= "---";

	public static final int		HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MIN									= 1;
	public static final int		HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX									= 96;
	public static final int		DEFAULT_HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED								= 4;

	public static final int		HOUR_TO_SEND_MESSAGE_MIN															= 1;
	public static final int		HOUR_TO_SEND_MESSAGE_MAX															= 23;
	public static final int		DEFAULT_HOUR_TO_SEND_MESSAGE														= 18;

	public static final long	HOURS_TO_TIME_IN_MILLIS_MULTIPLICATOR												= 60 * 60 * 1000;
	public static final long	DAYS_TO_TIME_IN_MILLIS_MULTIPLICATOR												= 24 * 60 * 60 * 1000;

	public static final long	MASTER_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITHOUT_SIMULATOR	= 300;
	public static final long	MASTER_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITH_SIMULATOR		= 60;

	public static final long	MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS_WITHOUT_SIMULATOR					= 60;
	public static final long	MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS_WITH_SIMULATOR						= 30;

	public static final long	MAILING_SENDING_CHECK_SLEEP_CYCLE_IN_SECONDS_WITHOUT_SIMULATOR						= 60;
	public static final long	MAILING_SENDING_CHECK_SLEEP_CYCLE_IN_SECONDS_WITH_SIMULATOR							= 30;

	// FIXME Special solution for MC tobacco
	public static String		VARIABLE_DEFINING_PARTICIPATION_IN_MOBILE_COACH_EXTRA								= "$participation_extra";

	public static final long	MAILING_SEND_RETRIES																= 2;
	public static final long	MAILING_SEND_RETRIES_SLEEP_BETWEEN_RETRIES_IN_SECONDS								= 5 * 60;
	public static final int		MAILING_MAXIMUM_THREAD_COUNT														= 25;

	public static final long	SIMULATOR_TIME_UPDATE_INTERVAL_IN_SECONDS											= 10;

	public static final int		SURVEY_FILE_CACHE_IN_MINUTES														= 3600;

	public static final int		UI_SESSION_TIMEOUT_IN_SECONDS														= 900;

	public static final String	FILE_STREAMING_SERVLET_PATH															= "files";
	public static final String	SHORT_ID_FILE_STREAMING_SERVLET_PATH												= "files-short";
	public static final String	SHORT_ID_SCREEN_SURVEY_AND_FEEDBACK_SERVLET_PATH									= "surveys-short";

	public static final String	PARTICIPANT_SESSION_ATTRIBUTE_EXPECTED												= "mc.vaadin.assigned-participant.expected";
	public static final String	PARTICIPANT_SESSION_ATTRIBUTE_DESCRIPTION											= "mc.vaadin.assigned-participant.description";
	public static final String	PARTICIPANT_SESSION_ATTRIBUTE														= "mc.vaadin.assigned-participant";
	public static final String	SURVEY_SESSION_PREFIX																= "mc.surveys.";
	public static final String	SURVEYS_CURRENT_SURVEY_CHECK_SESSION_ATTRIBUTE										= SURVEY_SESSION_PREFIX
																															+ "current_session";
	public static final String	SURVEYS_CURRENT_PARTICIPANT_CHECK_SESSION_ATTRIBUTE									= SURVEY_SESSION_PREFIX
																															+ "current_participant";

	public static final String	SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLES									= "MC_ResultValue_";
	public static final String	SCREENING_SURVEY_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE							= "MC_ConsistencyCheckValue";
	public static final int		SCREENING_SURVEY_SLIDE_AUTOMATIC_EXECUTION_LOOP_DETECTION_THRESHOLD					= 1000;

	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE											= "MC_Navigation";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE_VALUE_PREVIOUS							= "previous";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE_VALUE_NEXT								= "next";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE									= "MC_ConsistencyCheckValue";

	public static final String	REGULAR_EXPRESSION_TO_MATCH_ONE_OBJECT_ID											= "[A-Za-z0-9]+";
	public static final String	REGULAR_EXPRESSION_TO_VALIDATE_CALCULATED_RULE										= "^[\\$a-zA-Z0-9_\\+\\-%*/^().,]*$";
	public static final String	REGULAR_EXPRESSION_TO_VALIDATE_VARIABLE_NAME										= "^\\$[a-zA-Z0-9_]*$";
	public static final String	REGULAR_EXPRESSION_TO_MATCH_VARIABLES_IN_STRING										= "\\$[a-zA-Z0-9_]+";
	public static final String	VARIABLE_MATCH_MODIFIER																= "#";
	public static final String	REGULAR_EXPRESSION_TO_MATCH_MODIFIED_VARIABLES_IN_STRING							= VARIABLE_MATCH_MODIFIER
																															+ "[a-zA-Z0-9_]+"
																															+ VARIABLE_MATCH_MODIFIER;
	public static final String	VARIABLE_VALUE_MODIFIER_START														= "{";
	public static final String	VARIABLE_VALUE_MODIFIER_END															= "}";
	public static final String	REGULAR_EXPRESSION_TO_MATCH_VALUE_MODIFIER											= "\\{[^\\}]+\\}";

	public static final String	REGULAR_EXPRESSION_TO_CLEAN_PHONE_NUMBERS											= "[^\\d]";
	public static final String	REGULAR_EXPRESSION_TO_CLEAN_RECEIVED_MESSAGE										= "[^a-z0-9-\\s\\.]";
	public static final String	REGULAR_EXPRESSION_TO_CLEAN_DOUBLE_VALUES											= "\\.0+$";

	public static final String	REGULAR_EXPRESSION_TO_FIND_BOLD_STRING_PARTS										= "\\*([\\w\\s]+)\\*";
}
