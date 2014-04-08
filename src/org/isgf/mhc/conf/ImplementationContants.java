package org.isgf.mhc.conf;

/**
 * Contains some implementation specific constants
 * 
 * @author Andreas Filler
 */
public class ImplementationContants {
	public static final String	SYSTEM_CONFIGURATION_PROPERTY									= "mhc.configuration";
	public static final String	LOGGING_APPLICATION_NAME										= "MHC";

	public static final String	DEFAULT_OBJECT_NAME												= "---";
	public static final String	DEFAULT_ANSWER_NAME												= "---";
	public static final String	DEFAULT_ANSWER_VALUE											= "0";

	public static final int		HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MIN				= 1;
	public static final int		HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX				= 23;
	public static final int		DEFAULT_HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED			= 4;

	public static final int		HOUR_TO_SEND_MESSAGE_MIN										= 1;
	public static final int		HOUR_TO_SEND_MESSAGE_MAX										= 23;
	public static final int		DEFAULT_HOUR_TO_SEND_MESSAGE									= 16;

	public static final int		HOURS_TO_TIME_IN_MILLIS_MULTIPLICATOR							= 60 * 60 * 1000;

	public static final long	MASTER_RULE_EVALUTION_WORKER_MINUTES_SLEEP_BETWEEN_CHECK_CYCLES	= 10;

	public static final long	MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS					= 2 * 60;
	public static final long	MAILING_SENDING_CHECK_SLEEP_CYCLE_IN_SECONDS					= 2 * 60;

	public static final long	MAILING_SEND_RETRIES											= 2;
	public static final long	MAILING_SEND_RETRIES_SLEEP_BETWEEN_RETRIES_IN_SECONDS			= 5 * 60;

	public static final long	SIMULATOR_TIME_UPDATE_INTERVAL_IN_SECONDS						= 20;

	public static final int		SCREENING_SURVEY_FILE_CACHE_IN_MINUTES							= 3600;

	public static final String	FILE_STREAMING_SERVLET_PATH										= "files";
	public static final String	SHORT_ID_FILE_STREAMING_SERVLET_PATH							= "files-short";

	public static final String	SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLE					= "MHC_ResultValue";
	public static final String	SCREENING_SURVEY_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE		= "MHC_ConsistencyCheckValue";

	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE						= "MHC_Navigation";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE_VALUE_PREVIOUS		= "previous";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE_VALUE_NEXT			= "next";
	public static final String	FEEDBACK_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE				= "MHC_ConsistencyCheckValue";

	public static final String	REGULAR_EXPRESSION_TO_MATCH_ONE_OBJECT_ID						= "[A-Za-z0-9]+";
	public static final String	REGULAR_EXPRESSION_TO_VALIDATE_RULE								= "^[\\$a-zA-Z0-9_\\+\\-%*/^().,]*$";
	public static final String	REGULAR_EXPRESSION_TO_VALIDATE_VARIABLE_NAME					= "^\\$[a-zA-Z_]*$";
}
