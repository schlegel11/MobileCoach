package ch.ethz.mc.model.persistent.types;

/* ##LICENSE## */
/**
 * Supported {@link ScreeningSurveySlideQuestionTypes}
 * 
 * @author Andreas Filler
 */
public enum ScreeningSurveySlideQuestionTypes {
	TEXT_ONLY,
	MEDIA_ONLY,
	SELECT_ONE,
	SELECT_MANY,
	NUMBER_INPUT,
	TEXT_INPUT,
	MULTILINE_TEXT_INPUT;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}
}
