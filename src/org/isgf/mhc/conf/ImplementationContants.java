package org.isgf.mhc.conf;

/**
 * Contains some implementation specific constants
 * 
 * @author Andreas Filler
 */
public class ImplementationContants {
	public static final String	DEFAULT_OBJECT_NAME										= "---";
	public static final String	DEFAULT_ANSWER_NAME										= "---";
	public static final String	DEFAULT_ANSWER_VALUE									= "0";

	public static final int		HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MIN		= 1;
	public static final int		HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX		= 23;
	public static final int		DEFAULT_HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED	= 4;

	public static final int		HOUR_TO_SEND_MESSAGE_MIN								= 1;
	public static final int		HOUR_TO_SEND_MESSAGE_MAX								= 23;
	public static final int		DEFAULT_HOUR_TO_SEND_MESSAGE							= 16;

	public static final long	MAILING_RETRIES											= 2;
	public static final long	SLEEP_MILLIS_BETWEEN_MAILING_RETRIES					= 5 * 60 * 1000;
}
