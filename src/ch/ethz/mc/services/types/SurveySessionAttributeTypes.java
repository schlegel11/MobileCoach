package ch.ethz.mc.services.types;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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
import ch.ethz.mc.conf.ImplementationConstants;

/**
 * @author Andreas Filler
 */
public enum SurveySessionAttributeTypes {
	SURVEY_PARTICIPANT_ACCESS_GRANTED, SURVEY_FORMER_SLIDE_ID, SURVEY_PARTICIPANT_FEEDBACK_URL, SURVEY_CONSISTENCY_CHECK_VALUE, SURVEY_FROM_URL;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return ImplementationConstants.SURVEY_OR_FEEDBACK_SESSION_PREFIX
				+ super.toString().toLowerCase();
	}
}
