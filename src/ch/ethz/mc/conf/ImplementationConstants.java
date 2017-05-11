package ch.ethz.mc.conf;

import java.awt.RenderingHints;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
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
	public static final String	DEFAULT_ANSWER_NAME																	= "";

	public static final int		HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MIN									= 1;
	public static final int		HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX									= 96;
	public static final int		DEFAULT_HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED								= 4;

	public static final int		HOUR_TO_SEND_MESSAGE_MIN															= 1;
	public static final int		HOUR_TO_SEND_MESSAGE_MAX															= 23;
	public static final int		DEFAULT_HOUR_TO_SEND_MESSAGE														= 18;

	public static final long	HOURS_TO_TIME_IN_MILLIS_MULTIPLICATOR												= 60 * 60 * 1000;
	public static final long	DAYS_TO_TIME_IN_MILLIS_MULTIPLICATOR												= 24 * 60 * 60 * 1000;

	public static final long	MASTER_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITHOUT_SIMULATOR	= 300;
	public static final long	MASTER_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITH_SIMULATOR		= 15;

	public static final long	MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS_WITHOUT_SIMULATOR					= 60;
	public static final long	MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS_WITH_SIMULATOR						= 7;

	public static final long	MAILING_SENDING_CHECK_SLEEP_CYCLE_IN_SECONDS_WITHOUT_SIMULATOR						= 60;
	public static final long	MAILING_SENDING_CHECK_SLEEP_CYCLE_IN_SECONDS_WITH_SIMULATOR							= 7;

	// FIXME Special solution for MCAT
	public static String		VARIABLE_DEFINING_PARTICIPATION_IN_MOBILE_COACH_EXTRA								= "$participation_extra";
	// FIXME Special solution for MCAT
	public static String		VARIABLE_DEFINING_MRCT_STATUS_IN_MCAT												= "$mrctStatus";
	// FIXME Special solution for MCAT
	public static String		MESSAGE_GROUP_NAME_SUBSTRING_DEFINING_MRCT_YES_TRIGGER_IN_MCAT						= "mrct yes";

	public static final long	MAILING_SEND_RETRIES																= 2;
	public static final long	MAILING_SEND_RETRIES_SLEEP_BETWEEN_RETRIES_IN_SECONDS								= 5 * 60;
	public static final int		MAILING_MAXIMUM_THREAD_COUNT														= 25;

	public static final long	SIMULATOR_TIME_UPDATE_INTERVAL_IN_SECONDS											= 5;

	public static final int		SURVEY_FILE_CACHE_IN_MINUTES														= 3600;

	public static final int		UI_SESSION_TIMEOUT_IN_SECONDS														= 900;
	public static final String	UI_SESSION_ATTRIBUTE_DETECTOR														= "mc.vaadin.is_vaadin_session";

	public static final int		MAX_UPLOAD_SIZE_IN_BYTE																= 5000000;

	public static enum ACCEPTED_MEDIA_UPLOAD_TYPES {
		IMAGE
	};

	public static final String	ACCEPTED_IMAGE_FORMATS												= ".png|.jpg|.jpeg|.gif";

	public static final String	IMAGE_WATERMARK_TEXT												= "MobileCoach";
	public static final Object	IMAGE_JPEG_RENDERING												= RenderingHints.VALUE_RENDER_DEFAULT;
	// public static final Object IMAGE_JPEG_RENDERING =
	// RenderingHints.VALUE_RENDER_QUALITY;
	public static final float	IMAGE_JPEG_COMPRESSION												= 0.7f;
	public static final int		IMAGE_MAX_WIDTH														= 1000;
	public static final int		IMAGE_MAX_HEIGHT													= 1000;

	// CAUTION: If this is changed it also needs to be adjusted in the web.xml
	// configuration
	public static final String	REST_API_PATH														= "api";
	public static final String	FILE_STREAMING_SERVLET_PATH											= "files";
	public static final String	SHORT_ID_FILE_STREAMING_SERVLET_PATH								= "files-short";
	public static final String	SHORT_ID_SCREEN_SURVEY_AND_FEEDBACK_SERVLET_PATH					= "surveys-short";

	public static final String	REST_API_VERSION													= "v01";
	public static final String	REST_API_CREDITS_CHECK_VARIABLE_POSTFIX								= "Check";
	public static final String	REST_API_CREDITS_REMINDER_VARIABLE_POSTFIX							= "Reminder";

	public static final String	PARTICIPANT_SESSION_ATTRIBUTE_EXPECTED								= "mc.vaadin.assigned_participant.expected";
	public static final String	PARTICIPANT_SESSION_ATTRIBUTE_DESCRIPTION							= "mc.vaadin.assigned_participant.description";
	public static final String	PARTICIPANT_SESSION_ATTRIBUTE										= "mc.vaadin.assigned_participant";

	public static final String	SURVEY_OR_FEEDBACK_SESSION_PREFIX									= "mc.survey_or_feedback.";

	public static final String	SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLES					= "MC_ResultValue_";
	public static final String	SCREENING_SURVEY_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE			= "MC_ConsistencyCheckValue";
	public static final int		SCREENING_SURVEY_SLIDE_AUTOMATIC_EXECUTION_LOOP_DETECTION_THRESHOLD	= 1000;

	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE							= "MC_Navigation";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE_VALUE_PREVIOUS			= "previous";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE_VALUE_NEXT				= "next";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE					= "MC_ConsistencyCheckValue";

	public static final String	PLACEHOLDER_LINKED_SURVEY											= "####LINKED_SURVEY####";
	public static final String	PLACEHOLDER_LINKED_MEDIA_OBJECT										= "####LINKED_MEDIA_OBJECT####";

	public static final String	VARIABLE_PREFIX														= "$";
	public static final String	FILE_STORAGE_PREFIX													= "MC_";

	public static final int		OBJECT_ID_LENGTH													= 24;

	public static final String	REGULAR_EXPRESSION_TO_MATCH_ONE_OBJECT_ID							= "[a-f0-9]{"
																											+ OBJECT_ID_LENGTH
																											+ "}";
	public static final String	REGULAR_EXPRESSION_TO_VALIDATE_CALCULATED_RULE						= "^[\\$a-zA-Z0-9_\\+\\-%*/^().,]*$";
	public static final String	REGULAR_EXPRESSION_TO_VALIDATE_VARIABLE_NAME						= "^\\$[a-zA-Z0-9_]*$";
	public static final String	REGULAR_EXPRESSION_TO_MATCH_VARIABLES_IN_STRING						= "\\$[a-zA-Z0-9_]+";
	public static final String	SELECT_MANY_SEPARATOR												= ",";
	public static final String	VARIABLE_MATCH_MODIFIER												= "#";
	public static final String	REGULAR_EXPRESSION_TO_MATCH_MODIFIED_VARIABLES_IN_STRING			= VARIABLE_MATCH_MODIFIER
																											+ "[a-zA-Z0-9_]+"
																											+ VARIABLE_MATCH_MODIFIER;
	public static final String	VARIABLE_VALUE_MODIFIER_START										= "{";
	public static final String	VARIABLE_VALUE_MODIFIER_END											= "}";
	public static final String	REGULAR_EXPRESSION_TO_MATCH_VALUE_MODIFIER							= "\\{[^\\}]+\\}";

	public static final String	REGULAR_EXPRESSION_TO_CLEAN_PHONE_NUMBERS							= "[^\\d]";
	public static final String	REGULAR_EXPRESSION_TO_CLEAN_RECEIVED_MESSAGE						= "[^a-z0-9\\-\\s\\.]";
	public static final String	REGULAR_EXPRESSION_TO_CLEAN_FILE_NAMES								= "[^A-Za-z0-9\\_\\-\\d.]";
	public static final String	REGULAR_EXPRESSION_TO_CLEAN_DOUBLE_VALUES							= "\\.0+$";

	public static final String	REGULAR_EXPRESSION_TO_FIND_BOLD_STRING_PARTS						= "\\*([\\w\\s]+)\\*";

	public static final String	REPORT_TABLE														= "<table class=\"automatic\">|</table>";
	public static final String	REPORT_TABLE_ROW													= "<tr #>|</tr>";
	public static final String	REPORT_TABLE_HEADER_FIELD											= "<th #>|</th>";
	public static final String	REPORT_TABLE_NORMAL_FIELD											= "<td #>|</td>";
}
