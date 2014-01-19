package org.isgf.mhc.model.web.types;

import org.isgf.mhc.model.server.ScreeningSurveySlide;
import org.isgf.mhc.model.server.types.ScreeningSurveySlideQuestionTypes;

/**
 * "Extends" {@link ScreeningSurveySlideQuestionTypes} and contains all layouts
 * that can be available in the HTML template of a {@link ScreeningSurveySlide}
 * 
 * @author Andreas Filler
 */
public enum ScreeningSurveySlideTemplateFields {
	BASE_URL;

	/**
	 * The variable name of the {@link ScreeningSurveySlideTemplateLayoutTypes}
	 * 
	 * @return
	 */
	public String toVariable() {
		return this.toString().toLowerCase();
	}
}
