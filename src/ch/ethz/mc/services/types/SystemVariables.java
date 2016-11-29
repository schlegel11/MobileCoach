package ch.ethz.mc.services.types;

import ch.ethz.mc.conf.ImplementationConstants;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
 * Contains all variables used in the system
 *
 * @author Andreas Filler
 */
public class SystemVariables {
	public enum READ_ONLY_SYSTEM_VARIABLES {
		systemDayOfMonth, systemMonth, systemYear, systemDayInWeek, systemLinkedSurvey, systemLinkedMediaObject;

		public String toVariableName() {
			return ImplementationConstants.VARIABLE_PREFIX + toString();
		}
	};

	public enum READ_ONLY_PARTICIPANT_VARIABLES {
		participantParticipationInWeeks, participantParticipationInDays, participantFeedbackURL;

		public String toVariableName() {
			return ImplementationConstants.VARIABLE_PREFIX + toString();
		}
	};

	public enum READ_WRITE_PARTICIPANT_VARIABLES {
		participantName, participantLanguage, participantGroup, participantDialogOptionSMSData, participantDialogOptionEmailData, participantSupervisorDialogOptionSMSData, participantSupervisorDialogOptionEmailData;

		public String toVariableName() {
			return ImplementationConstants.VARIABLE_PREFIX + toString();
		}
	};

	public enum READ_ONLY_PARTICIPANT_REPLY_VARIABLES {
		participantMessageReply;

		public String toVariableName() {
			return ImplementationConstants.VARIABLE_PREFIX + toString();
		}
	}

	public static final String[]	EXTERNALLY_READABLE_SYSTEM_VARIABLE_NAMES		= new String[] {
			READ_ONLY_SYSTEM_VARIABLES.systemDayOfMonth.toVariableName(),
			READ_ONLY_SYSTEM_VARIABLES.systemMonth.toVariableName(),
			READ_ONLY_SYSTEM_VARIABLES.systemYear.toVariableName(),
			READ_ONLY_SYSTEM_VARIABLES.systemDayInWeek.toVariableName()			};

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
			READ_WRITE_PARTICIPANT_VARIABLES.participantGroup.toVariableName()		};
}
