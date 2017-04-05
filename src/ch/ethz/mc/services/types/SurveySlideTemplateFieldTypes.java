package ch.ethz.mc.services.types;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
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

/**
 * Contains all template fields that can be available in the HTML template of a
 * {@link ScreeningSurveySlide}
 *
 * All fields can be used as <code>{{field}}</code> to get the content, as
 * <code>{{#field}}...{{/field}}</code> for loops and existence checks as well
 * as <code>{{^field}}...{{/field}}</code> for non-existence checks
 *
 * Detailed information regarding the template system can be found in the
 * <a href="http://mustache.github.io/mustache.5.html">Mustache
 * documentation</a>
 *
 * @author Andreas Filler
 */
public enum SurveySlideTemplateFieldTypes {
	/**
	 * Exists if it's a survey slide
	 */
	IS_SURVEY,
	/**
	 * Exists if it's an intermediate survey slide
	 */
	IS_INTERMEDIATE_SURVEY,
	/**
	 * Contains the current layout of the slide; can be used instead or together
	 * with the values defined in
	 * {@link ScreeningSurveySlideTemplateLayoutTypes}
	 */
	LAYOUT,
	/**
	 * Contains the number of available questions
	 */
	QUESTIONS_COUNT,
	/**
	 * Contains a list of questions:
	 *
	 * <code>{{#questions}}...use the fields {{question_text}}, {{#answers}}...{{/answers}} etc. here...{{/questions}}</code>
	 */
	QUESTIONS,
	/**
	 * Contains the position (1...n) of one question and can be used inside
	 * <code>{{#questions}}...{{/questions}}</code>
	 */
	QUESTION_POSITION,
	/**
	 * Contains the question text and can be used inside
	 * <code>{{#questions}}...{{/questions}}</code>
	 */
	QUESTION_TEXT,
	/**
	 * Contains the name of the result variable which should contain the answer
	 * value when pressing the button for the next slide and can be used inside
	 * <code>{{#questions}}...{{/questions}}</code>
	 */
	RESULT_VARIABLE,
	/**
	 * Can be used inside <code>{{#questions}}...{{/questions}}</code> and
	 * contains the number of available answers
	 */
	ANSWERS_COUNT,
	/**
	 * Can be used inside <code>{{#questions}}...{{/questions}}</code> and
	 * contains a list of answers:
	 *
	 * <code>{{#answers}}...use the fields {{answer_text}}, {{answer_value}}, {{preselected_answer}} etc. here...{{/answers}}</code>
	 */
	ANSWERS,
	/**
	 * Contains the position (1...n) of one answer option and can be used inside
	 * <code>{{#answers}}...{{/answers}}</code>
	 */
	ANSWER_POSITION,
	/**
	 * Contains the text of one answer option and can be used inside
	 * <code>{{#answers}}...{{/answers}}</code>
	 */
	ANSWER_TEXT,
	/**
	 * Contains the value of one answer option and can be used inside
	 * <code>{{#answers}}...{{/answers}}</code>
	 */
	ANSWER_VALUE,
	/**
	 * <strong>OPTIONAL:</strong> Exists if the answer is the last one in the
	 * list of answers; it can be
	 * used inside <code>{{#answers}}...{{/answers}}</code>
	 */
	IS_FIRST_ANSWER,
	/**
	 * <strong>OPTIONAL:</strong> Exists if the answer is the last one in the
	 * list of answers; it can be
	 * used inside <code>{{#answers}}...{{/answers}}</code>
	 */
	IS_LAST_ANSWER,
	/**
	 * Is true if the current answer should be preselected and can be used
	 * inside <code>{{#answers}}...{{/answers}}</code>
	 */
	PRESELECTED_ANSWER,
	/**
	 * <strong>OPTIONAL:</strong> Can contain the error message of the
	 * validation
	 */
	VALIDATION_ERROR_MESSAGE,
	/**
	 * <strong>OPTIONAL:</strong> Can contain the URL to an intermediate survey
	 */
	INTERMEDIATE_SURVEY_URL,
	/**
	 * Is true, when the slide is the last slide of the screening survey
	 */
	IS_LAST_SLIDE;

	/**
	 * Creates the appropriate variable name of the
	 * {@link SurveySlideTemplateFieldTypes}
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
