package ch.ethz.mc.services.types;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
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
public enum ScreeningSurveySlideTemplateLayoutTypes {
	/**
	 * Can all be used as boolean checks if the current layout is active:
	 * 
	 * <code>{{#closed}}This text will only be displayed on the error slide{{/closed}}</code>
	 */
	CLOSED, PASSWORD_INPUT, TEXT_ONLY, MEDIA_ONLY, SELECT_ONE, SELECT_MANY, NUMBER_INPUT, TEXT_INPUT, MULTILINE_TEXT_INPUT, DONE;

	/**
	 * Creates the appropriate variable name of the
	 * {@link ScreeningSurveySlideTemplateLayoutTypes}
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
