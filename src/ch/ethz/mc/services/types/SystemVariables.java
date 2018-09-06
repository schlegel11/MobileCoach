package ch.ethz.mc.services.types;

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
		participantParticipationInWeeks,
		participantParticipationInDays,
		participantFeedbackURL,
		participantLastLoginDate,
		participantLastLoginTime,
		participantLastLogoutDate,
		participantLastLogoutTime;

		public String toVariableName() {
			return ImplementationConstants.VARIABLE_PREFIX + toString();
		}
	};

	public enum READ_WRITE_PARTICIPANT_VARIABLES {
		participantName,
		participantLanguage,
		participantGroup,
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
