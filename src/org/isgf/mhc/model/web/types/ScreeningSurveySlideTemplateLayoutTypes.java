package org.isgf.mhc.model.web.types;

import org.isgf.mhc.model.server.ScreeningSurveySlide;
import org.isgf.mhc.model.server.types.ScreeningSurveySlideQuestionTypes;

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
	 * <code>{{#error}}This text will only be displayed on the error slide{{/error}}</code>
	 */
	CLOSED, PASSWORD_INPUT, TEXT_ONLY, SELECT_ONE, SELECT_MANY, NUMBER_INPUT, TEXT_INPUT, MULTILINE_TEXT_INPUT;

	/**
	 * Creates the appropriate variable name of the
	 * {@link ScreeningSurveySlideTemplateLayoutTypes}
	 * 
	 * @return The appropriate variable name
	 */
	public String toVariable() {
		return this.toString().toLowerCase();
	}
}
