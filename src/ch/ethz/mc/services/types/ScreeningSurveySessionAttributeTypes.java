package ch.ethz.mc.services.types;

import ch.ethz.mc.conf.ImplementationConstants;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
 * @author Andreas Filler
 */
public enum ScreeningSurveySessionAttributeTypes {
	SCREENING_SURVEY_PARTICIPANT_ACCESS_GRANTED, SCREENING_SURVEY_FORMER_SLIDE_ID, SCREENING_SURVEY_PARTICIPANT_FEEDBACK_URL, SCREENING_SURVEY_CONSISTENCY_CHECK_VALUE, SCREENING_SURVEY_FROM_URL;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return ImplementationConstants.SURVEY_SESSION_PREFIX
				+ super.toString().toLowerCase();
	}
}
