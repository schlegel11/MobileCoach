package ch.ethz.mc.services.types;

/* ##LICENSE## */
import ch.ethz.mc.conf.ImplementationConstants;

/**
 * @author Andreas Filler
 */
public enum SurveySessionAttributeTypes {
	SURVEY_PARTICIPANT_ACCESS_GRANTED,
	SURVEY_FORMER_SLIDE_ID,
	SURVEY_PARTICIPANT_FEEDBACK_URL,
	SURVEY_CONSISTENCY_CHECK_VALUE,
	SURVEY_FROM_URL;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return ImplementationConstants.SURVEY_OR_FEEDBACK_SESSION_PREFIX
				+ super.toString().toLowerCase();
	}
}
