package ch.ethz.mc.services.types;

/* ##LICENSE## */

/**
 * @author Andreas Filler
 */
public enum GeneralSessionAttributeValidatorTypes {
	PARTICIPANT_RELATED, DASHBOARD_ACCESS;

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
