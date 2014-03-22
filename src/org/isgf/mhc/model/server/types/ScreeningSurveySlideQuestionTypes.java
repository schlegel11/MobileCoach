package org.isgf.mhc.model.server.types;

/**
 * Supported {@link ScreeningSurveySlideQuestionTypes}
 * 
 * @author Andreas Filler
 */
public enum ScreeningSurveySlideQuestionTypes {
	TEXT_ONLY, SELECT_ONE, SELECT_MANY, NUMBER_INPUT, TEXT_INPUT, MULTILINE_TEXT_INPUT;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}
}
