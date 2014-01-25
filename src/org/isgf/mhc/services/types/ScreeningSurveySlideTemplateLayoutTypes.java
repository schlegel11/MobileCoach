package org.isgf.mhc.services.types;

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
	 * <code>{{#closed}}This text will only be displayed on the error slide{{/closed}}</code>
	 */
	CLOSED, PASSWORD_INPUT, TEXT_ONLY, SELECT_ONE, SELECT_MANY, NUMBER_INPUT, TEXT_INPUT, MULTILINE_TEXT_INPUT, DONE;

	/**
	 * Creates the appropriate variable name of the
	 * {@link ScreeningSurveySlideTemplateLayoutTypes}
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
