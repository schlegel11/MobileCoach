package ch.ethz.mc.model.persistent.types;

/* ##LICENSE## */
/**
 * Supported {@link SMSServiceType}
 *
 * @author Andreas Filler
 */
public enum SMSServiceType {
	ASPSMS, TWILIO;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}
}
