package ch.ethz.mc.services.types;

/**
 * Contains all variables used in the system
 * 
 * @author Andreas Filler
 */
public class SystemVariables {
	public enum READ_ONLY_SYSTEM_VARIABLES {
		systemDayOfMonth, systemMonth, systemYear, systemDayInWeek;

		public String toVariableName() {
			return "$" + toString();
		}
	};

	public enum READ_ONLY_PARTICIPANT_VARIABLES {
		participantParticipationInWeeks, participantParticipationInDays, participantFeedbackURL;

		public String toVariableName() {
			return "$" + toString();
		}
	};

	public enum READ_WRITE_PARTICIPANT_VARIABLES {
		participantName, participantDialogOptionSMSData, participantDialogOptionEmailData;

		public String toVariableName() {
			return "$" + toString();
		}
	};

	public enum READ_ONLY_PARTICIPANT_REPLY_VARIABLES {
		participantMessageReply;

		public String toVariableName() {
			return "$" + toString();
		}
	};
}
