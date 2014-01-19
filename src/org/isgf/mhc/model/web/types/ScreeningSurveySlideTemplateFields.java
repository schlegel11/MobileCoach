package org.isgf.mhc.model.web.types;

import org.isgf.mhc.model.server.ScreeningSurveySlide;

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
public enum ScreeningSurveySlideTemplateFields {
	/**
	 * Contains the base URL of the website:
	 * 
	 * <code>&lt;head&gt;&lt;base href="{{base_url}}"&gt;&lt;/head&gt;</code>
	 */
	BASE_URL,
	/**
	 * Contains the name of the survey
	 */
	SURVEY_NAME,
	/**
	 * <strong>OPTIONAL:</strong> Can contain an optional value for e.g. a
	 * specific css layout
	 * class
	 */
	OPTIONAL_LAYOUT_ATTRIBUTE,
	/**
	 * Contains the title of the slide
	 */
	SLIDE_TITLE,
	/**
	 * <strong>OPTIONAL:</strong> Can contain the URL of an media object that
	 * should be shown in the slide
	 */
	MEDIA_OBJECT_URL,
	/**
	 * <strong>OPTIONAL:</strong> Can contain the type of an media object that
	 * should be shown in the slide; only one is true at a time and only if
	 * MEDIA_OBJECT_URL is also set
	 */
	MEDIA_OBJECT_TYPE_HTML_TEXT, MEDIA_OBJECT_TYPE_IMAGE, MEDIA_OBJECT_TYPE_AUDIO, MEDIA_OBJECT_TYPE_VIDEO,
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
	 * Contains the name of the result variable, which should contain the slides
	 * selection when pressing the button for the next slide
	 */
	RESULT_VARIABLE,
	/**
	 * <strong>ONLY</strong> internal, can't be used in templates
	 */
	TEMPLATE_FOLDER;

	/**
	 * Creates the appropriate variable name of the
	 * {@link ScreeningSurveySlideTemplateFields}
	 * 
	 * @return The appropriate variable name
	 */
	public String toVariable() {
		return this.toString();
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
