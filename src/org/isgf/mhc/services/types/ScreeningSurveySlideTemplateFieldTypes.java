package org.isgf.mhc.services.types;

import org.isgf.mhc.model.persistent.ScreeningSurveySlide;

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
public enum ScreeningSurveySlideTemplateFieldTypes {
	/**
	 * Exists if it's a screening survey slide
	 */
	IS_SCREENING_SURVEY,
	/**
	 * Contains the question text
	 */
	QUESTION,
	/**
	 * Contains a list of answers:
	 * 
	 * <code>{{#answers}}...use the fields {{answer_text}}, {{answer_value}}, {{preselected_answer}} etc. here...{{/answers}}</code>
	 */
	ANSWERS,
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
	 * Is true if the current answer should be preselected and can be used
	 * inside <code>{{#answers}}...{{/answers}}</code>
	 */
	PRESELECTED_ANSWER,
	/**
	 * Contains the name of the result variable which should contain the slides
	 * selection when pressing the button for the next slide
	 */
	RESULT_VARIABLE,
	/**
	 * Is true, when the slide is the last slide of the screening survey
	 */
	IS_LAST_SLIDE;

	/**
	 * Creates the appropriate variable name of the
	 * {@link ScreeningSurveySlideTemplateFieldTypes}
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
