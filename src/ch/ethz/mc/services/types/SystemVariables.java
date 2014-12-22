package ch.ethz.mc.services.types;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health IS-Lab
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
