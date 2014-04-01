package org.isgf.mhc.services.types;

/**
 * Contains all variables used in the system
 * 
 * @author Andreas Filler
 */
public class SystemVariables {
	public enum READ_ONLY_SYSTEM_VARIABLES {
		systemDayOfMonth, systemMonth, systemYear, systemDayInWeek
	};

	public enum READ_WRITE_SYSTEM_VARIABLES {

	};

	public enum READ_ONLY_PARTICIPANT_VARIABLES {
		participantParticipationInWeeks, participantParticipationInDays
	};

	public enum READ_WRITE_PARTICIPANT_VARIABLES {
		participantName, participantDialogOptionSMSData, participantDialogOptionEmailData
	};

	public enum READ_ONLY_PARTICIPANT_REPLY_VARIABLES {
		participantMessageReply
	};
}
