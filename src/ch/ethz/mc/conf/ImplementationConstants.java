package ch.ethz.mc.conf;

/* ##LICENSE## */
import java.awt.RenderingHints;

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

	public static final int		MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MIN									= 1;
	public static final int		MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX_MONITORING_MESSAGE				= 5760;
	public static final int		MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX_MICRO_DIALOG_MESSAGE				= 1440;
	public static final int		MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX_MANUAL_MESSAGE					= 2160;
	public static final int		DEFAULT_MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED								= 240;

	public static final int		HOUR_TO_SEND_MESSAGE_MIN															= 0;
	public static final int		HOUR_TO_SEND_MESSAGE_MAX															= 23;
	public static final int		DEFAULT_HOUR_TO_SEND_MESSAGE														= 0;
	public static final int		FALLBACK_HOUR_TO_SEND_MESSAGE														= 12;

	public static final long	MINUTES_TO_TIME_IN_MILLIS_MULTIPLICATOR												= 60
			* 1000;
	public static final long	HOURS_TO_TIME_IN_MILLIS_MULTIPLICATOR												= 60
			* 60 * 1000;
	public static final long	DAYS_TO_TIME_IN_MILLIS_MULTIPLICATOR												= 24
			* 60 * 60 * 1000;

	public static final double	MILLIS_TO_MINUTES_DIVIDER															= 1000
			* 60;

	public static final long	MASTER_RULE_EVALUTION_WORKER_MILLISECONDS_SLEEP_BETWEEN_CHECK_CYCLES				= 500;

	public static final long	PERIODIC_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITHOUT_SIMULATOR	= 300;
	public static final long	PERIODIC_RULE_EVALUTION_WORKER_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES_WITH_SIMULATOR	= 10;

	public static final long	FINISH_UNFINISHED_SCREENING_SURVEYS_INTERVAL_IN_SECONDS								= 600;

	public static final long	INCOMING_MESSAGE_WORKER_MILLISECONDS_SLEEP_BETWEEN_CHECK_CYCLES						= 250;
	public static final long	OUTGOING_MESSAGE_WORKER_MILLISECONDS_SLEEP_BETWEEN_CHECK_CYCLES						= 250;

	public static final long	SMS_AND_EMAIL_RETRIEVAL_INTERVAL_IN_SECONDS_WITHOUT_SIMULATOR						= 30;
	public static final long	SMS_AND_EMAIL_RETRIEVAL_INTERVAL_IN_SECONDS_WITH_SIMULATOR							= 5;

	public static final long	EMAIL_SENDING_RETRIES																= 2;
	public static final long	EMAIL_SENDING_RETRIES_SLEEP_BETWEEN_RETRIES_IN_SECONDS								= 2
			* 60;
	public static final int		ASYNC_SENDING_MAXIMUM_THREAD_COUNT													= 25;

	public static final long	SIMULATOR_TIME_UPDATE_INTERVAL_IN_SECONDS											= 5;

	public static final int		SURVEY_FILE_CACHE_IN_MINUTES														= 3600;
	public static final int		DASHBOARD_FILE_CACHE_IN_MINUTES														= 3600;

	public static final int		UI_SESSION_TIMEOUT_IN_SECONDS														= 900;
	public static final String	UI_SESSION_ATTRIBUTE_DETECTOR														= "mc.vaadin.is_vaadin_session";

	public static final int		PUSH_SERVER_TIMEOUT																	= 10000;

	public static final int		SMS_SERVER_TIMEOUT																	= 10000;

	public static final String	MAIL_SERVER_TIMEOUT																	= "5000";
	public static final String	MAIL_SERVER_CONNECTION_TIMEOUT														= "30000";

	public static final long	MICRO_DIALOG_MESSAGE_UNHANDLED_MESSAGE_MINIMUM_THRESHOLD_IN_MILLIS					= 30
			* MINUTES_TO_TIME_IN_MILLIS_MULTIPLICATOR;
	public static final int		MICRO_DIALOG_LOOP_DETECTION_THRESHOLD												= 500;

	public static enum ACCEPTED_MEDIA_UPLOAD_TYPES {
		IMAGE, VIDEO, AUDIO
	};

	public static final String	ACCEPTED_IMAGE_FORMATS												= ".png|.jpg|.jpeg|.gif";
	public static final String	ACCEPTED_VIDEO_FORMATS												= ".mp4|.mov|.m4v";
	public static final String	ACCEPTED_AUDIO_FORMATS												= ".aac";

	public static int			MAX_IMAGE_UPLOAD_SIZE_IN_BYTE										= 5000000;
	public static int			MAX_VIDEO_UPLOAD_SIZE_IN_BYTE										= 50000000;
	public static int			MAX_AUDIO_UPLOAD_SIZE_IN_BYTE										= 25000000;

	public static final String	IMAGE_WATERMARK_TEXT												= "MobileCoach";
	public static final Object	IMAGE_JPEG_RENDERING												= RenderingHints.VALUE_RENDER_DEFAULT;
	// public static final Object IMAGE_JPEG_RENDERING =
	// RenderingHints.VALUE_RENDER_QUALITY;
	public static final float	IMAGE_JPEG_COMPRESSION												= 0.85f;
	public static final int		IMAGE_MAX_WIDTH														= 1000;
	public static final int		IMAGE_MAX_HEIGHT													= 1000;

	// CAUTION: If this is changed it also needs to be adjusted in the web.xml
	// configuration
	public static final String	REST_API_PATH														= "api";
	public static final String	FILE_STREAMING_SERVLET_PATH											= "files";
	public static final String	SHORT_ID_FILE_STREAMING_SERVLET_PATH								= "files-short";
	public static final String	SHORT_ID_SCREEN_SURVEY_AND_FEEDBACK_SERVLET_PATH					= "surveys-short";
	public static final String	DASHBOARD_SERVLET_PATH												= "dashboard";
	public static final String	DEEPSTREAM_SERVLET_PATH												= "deepstream";

	public static final String	REST_SESSION_BASED_API_VERSION										= "v01";
	public static final String	TOKEN_BASED_API_VERSION												= "v02";

	public static final String	REST_API_ADDITIONAL_ALLOWED_HEADERS									= "token,password,interventionPattern,group,user";

	public static final String	REST_API_CREDITS_CHECK_VARIABLE_POSTFIX								= "Check";
	public static final String	REST_API_CREDITS_REMINDER_VARIABLE_POSTFIX							= "Reminder";

	public static final String	PARTICIPANT_SESSION_ATTRIBUTE_EXPECTED								= "mc.vaadin.assigned_participant.expected";
	public static final String	PARTICIPANT_SESSION_ATTRIBUTE_DESCRIPTION							= "mc.vaadin.assigned_participant.description";
	public static final String	PARTICIPANT_SESSION_ATTRIBUTE										= "mc.vaadin.assigned_participant";

	public static final String	SURVEY_OR_FEEDBACK_SESSION_PREFIX									= "mc.survey_or_feedback.";

	public static final int		RULE_ITERATORS_AUTOMATIC_EXECUTION_LOOP_DETECTION_THRESHOLD			= 500;

	public static final String	SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLES					= "MC_ResultValue_";
	public static final String	SCREENING_SURVEY_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE			= "MC_ConsistencyCheckValue";
	public static final int		SCREENING_SURVEY_SLIDE_AUTOMATIC_EXECUTION_LOOP_DETECTION_THRESHOLD	= 1000;

	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE							= "MC_Navigation";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE_VALUE_PREVIOUS			= "previous";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE_VALUE_NEXT				= "next";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE					= "MC_ConsistencyCheckValue";

	public static final String	PLACEHOLDER_NEW_MESSAGE_APP_IDENTIFIER								= "\n---\n";
	public static final String	PLACEHOLDER_LINKED_SURVEY											= "####LINKED_SURVEY####";
	public static final String	PLACEHOLDER_LINKED_MEDIA_OBJECT										= "####LINKED_MEDIA_OBJECT####";

	public static final String	VARIABLE_PREFIX														= "$";
	public static final String	FILE_STORAGE_PREFIX													= "MC_";

	public static final int		OBJECT_ID_LENGTH													= 24;

	public static final String	REGULAR_EXPRESSION_TO_MATCH_ONE_OBJECT_ID							= "[a-f0-9]{"
			+ OBJECT_ID_LENGTH + "}";
	public static final String	REGULAR_EXPRESSION_TO_VALIDATE_CALCULATED_RULE						= "^[\\$a-zA-Z0-9_\\+\\-%*/^().,]*$";
	public static final String	REGULAR_EXPRESSION_TO_VALIDATE_VARIABLE_NAME						= "^\\$[a-zA-Z0-9_]*$";
	public static final String	REGULAR_EXPRESSION_TO_MATCH_VARIABLES_IN_STRING						= "\\$[a-zA-Z0-9_]+";
	public static final String	SELECT_MANY_SEPARATOR												= ",";
	public static final String	VARIABLE_MATCH_MODIFIER												= "#";
	public static final String	REGULAR_EXPRESSION_TO_MATCH_MODIFIED_VARIABLES_IN_STRING			= VARIABLE_MATCH_MODIFIER
			+ "[a-zA-Z0-9_]+" + VARIABLE_MATCH_MODIFIER;
	public static final String	VARIABLE_VALUE_MODIFIER_START										= "{";
	public static final String	VARIABLE_VALUE_MODIFIER_END											= "}";
	public static final String	REGULAR_EXPRESSION_TO_MATCH_VALUE_MODIFIER							= "\\{[^\\}]+\\}";

	public static final String	REGULAR_EXPRESSION_TO_CLEAN_PHONE_NUMBERS							= "[^\\d]";
	public static final String	REGULAR_EXPRESSION_TO_CLEAN_RECEIVED_MESSAGE						= "[^a-z0-9\\-\\s\\.\\,]";
	public static final String	REGULAR_EXPRESSION_TO_CLEAN_FILE_NAMES								= "[^A-Za-z0-9\\_\\-\\d.]";
	public static final String	REGULAR_EXPRESSION_TO_CLEAN_DOUBLE_VALUES							= "\\.0+$";

	public static final String	REGULAR_EXPRESSION_TO_FIND_BOLD_STRING_PARTS						= "\\*([\\w\\s]+)\\*";

	public static final String	DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM								= "ds:";
	public static final String	DEFAULT_PARTICIPANT_NICKNAME_FOR_SURVEY_GENERATION_FOR_DEEPSTREAM	= "Participant";

	public static final String	DEEPSTREAM_SERVER_ROLE												= "server";
	public static final String	DEEPSTREAM_PARTICIPANT_ROLE											= "participant";
	public static final String	DEEPSTREAM_SUPERVISOR_ROLE											= "supervisor";
	public static final String	DEEPSTREAM_TEAM_MANAGER_ROLE										= "team-manager";
	public static final String	DEEPSTREAM_OBSERVER_ROLE											= "observer";
	public static final String	DEEPSTREAM_EXTERNAL_SYSTEM_ROLE										= "external-system";

	public static final String	TEAM_MANAGER_PUSH_NOTIFICATION_PREFIX								= "👤: ";
	public static final int		TEAM_MANAGER_EMAIL_NOTIFICATION_SILENCE_DURATION_IN_MINUTES			= 5;

	public static final String	REPORT_TABLE														= "<table class=\"automatic\">|</table>";
	public static final String	REPORT_TABLE_ROW													= "<tr #>|</tr>";
	public static final String	REPORT_TABLE_HEADER_FIELD											= "<th #>|</th>";
	public static final String	REPORT_TABLE_NORMAL_FIELD											= "<td #>|</td>";
}
