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

	public static final long	MASTER_RULE_EVALUTION_WORKER_MINUTES_SLEEP_BETWEEN_CHECK_CYCLES	= 10;

	public static final long	MAILING_RETRIEVAL_CHECK_SLEEP_CYCLE_IN_SECONDS					= 2 * 60;
	public static final long	MAILING_SENDING_CHECK_SLEEP_CYCLE_IN_SECONDS					= 2 * 60;

	public static final long	MAILING_SEND_RETRIES											= 2;
	public static final long	MAILING_SEND_RETRIES_SLEEP_BETWEEN_RETRIES_IN_SECONDS			= 5 * 60;

	public static final String	FILE_STREAMING_SERVLET_PATH										= "files";
	public static final String	SHORT_ID_FILE_STREAMING_SERVLET_PATH							= "files-short";

	public static final String	SCREENING_SURVEY_SERVLET_FEEDBACK_SUBPATH						= "feedback";
	public static final String	SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLE					= "MHC_ResultValue";
	public static final String	SCREENING_SURVEY_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE		= "MHC_ConsistencyCheckValue";
}
