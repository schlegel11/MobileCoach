package ch.ethz.mc.services.types;

/* ##LICENSE## */
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
