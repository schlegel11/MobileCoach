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
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.types.ScreeningSurveySlideQuestionTypes;

/**
 * "Extends" {@link ScreeningSurveySlideQuestionTypes} and contains all layouts
 * that can be available in the HTML template of a {@link ScreeningSurveySlide}
 *
 * @author Andreas Filler
 */
public enum SurveySlideTemplateLayoutTypes {
	/**
	 * Can all be used as boolean checks if the current layout is active:
	 *
	 * <code>{{#closed}}This text will only be displayed on the error slide{{/closed}}</code>
	 */
	CLOSED, PASSWORD_INPUT, TEXT_ONLY, MEDIA_ONLY, SELECT_ONE, SELECT_MANY, NUMBER_INPUT, TEXT_INPUT, MULTILINE_TEXT_INPUT, DISABLED, DONE;

	/**
	 * Creates the appropriate variable name of the
	 * {@link SurveySlideTemplateLayoutTypes}
	 *
	 * @return The appropriate variable name
	 */
	public String toVariable() {
		return toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}
