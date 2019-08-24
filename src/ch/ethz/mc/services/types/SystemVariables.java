package ch.ethz.mc.services.types;

/* ##LICENSE## */
import ch.ethz.mc.conf.ImplementationConstants;

/**
 * Contains all variables used in the system
 *
 * @author Andreas Filler
 */
public class SystemVariables {
	public enum READ_ONLY_SYSTEM_VARIABLES {
		systemDecimalMinuteOfHour,
		systemMinuteOfHour,
		systemHourOfDay,
		systemDayOfMonth,
		systemMonth,
		systemYear,
		systemDayInWeek,
		systemLinkedSurvey,
		systemLinkedMediaObject;

		public String toVariableName() {
			return ImplementationConstants.VARIABLE_PREFIX + toString();
		}
	};

	public enum READ_ONLY_PARTICIPANT_VARIABLES {
		participantIdentifier,
		participantParticipationInWeeks,
		participantParticipationInDays,
		participantFeedbackURL,
		participantLastLoginDate,
		participantLastLoginTime,
		participantLastLogoutDate,
		participantLastLogoutTime,
		participantInfiniteBlockingMessagesCount,
		participantInfiniteBlockingMessagesIdentifiers,
		participantInfiniteBlockingMessagesWaitingMinutesMin,
		participantInfiniteBlockingMessagesWaitingMinutesMax;

		public String toVariableName() {
			return ImplementationConstants.VARIABLE_PREFIX + toString();
		}
	};

	public enum READ_WRITE_PARTICIPANT_VARIABLES {
		participantName,
		participantLanguage,
		participantGroup,
		participantOrganization,
		participantOrganizationUnit,
		participantResponsibleTeamManagerEmailData,
		participantDialogOptionSMSData,
		participantDialogOptionEmailData,
		participantDialogOptionExternalID,
		participantSupervisorDialogOptionSMSData,
		participantSupervisorDialogOptionEmailData,
		participantSupervisorDialogOptionExternalID;

		public String toVariableName() {
			return ImplementationConstants.VARIABLE_PREFIX + toString();
		}
	};

	public enum READ_ONLY_PARTICIPANT_REPLY_VARIABLES {
		participantMessageReply,
		participantRawMessageReply,
		participantUnexpectedMessage,
		participantUnexpectedRawMessage,
		participantIntention,
		participantRawIntention,
		participantIntentionContent;

		public String toVariableName() {
			return ImplementationConstants.VARIABLE_PREFIX + toString();
		}
	}
	
	public enum READ_ONLY_EXTERNAL_SYSTEM_VARIABLES {
		externalSystemName,
		externalSystemId;

		public String toVariableName() {
			return ImplementationConstants.VARIABLE_PREFIX + toString();
		}
	}

	public static final String[]	EXTERNALLY_READABLE_SYSTEM_VARIABLE_NAMES		= new String[] {
			READ_ONLY_SYSTEM_VARIABLES.systemHourOfDay.toVariableName(),
			READ_ONLY_SYSTEM_VARIABLES.systemDayOfMonth.toVariableName(),
			READ_ONLY_SYSTEM_VARIABLES.systemMonth.toVariableName(),
			READ_ONLY_SYSTEM_VARIABLES.systemYear.toVariableName(),
			READ_ONLY_SYSTEM_VARIABLES.systemDayInWeek.toVariableName() };

	public static final String[]	EXTERNALLY_READABLE_PARTICIPANT_VARIABLE_NAMES	= new String[] {
			READ_ONLY_PARTICIPANT_VARIABLES.participantParticipationInWeeks
					.toVariableName(),
			READ_ONLY_PARTICIPANT_VARIABLES.participantParticipationInDays
					.toVariableName(),
			READ_ONLY_PARTICIPANT_VARIABLES.participantFeedbackURL
					.toVariableName(),
			READ_WRITE_PARTICIPANT_VARIABLES.participantName.toVariableName(),
			READ_WRITE_PARTICIPANT_VARIABLES.participantLanguage
					.toVariableName(),
			READ_WRITE_PARTICIPANT_VARIABLES.participantGroup
					.toVariableName() };
}
