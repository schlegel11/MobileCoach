package ch.ethz.mc.services.types;

/* ##LICENSE## */
import ch.ethz.mc.conf.ImplementationConstants;

/**
 * @author Andreas Filler
 */
public enum GeneralSessionAttributeTypes {
	VALIDATOR, TOKEN, CURRENT_SESSION, CURRENT_PARTICIPANT;

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
