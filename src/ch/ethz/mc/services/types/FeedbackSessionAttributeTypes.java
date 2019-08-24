package ch.ethz.mc.services.types;

/* ##LICENSE## */
import ch.ethz.mc.conf.ImplementationConstants;

/**
 * @author Andreas Filler
 */
public enum FeedbackSessionAttributeTypes {
	FEEDBACK_FORMER_SLIDE_ID, FEEDBACK_CONSISTENCY_CHECK_VALUE;

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
