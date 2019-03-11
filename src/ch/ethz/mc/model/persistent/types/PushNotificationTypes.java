package ch.ethz.mc.model.persistent.types;

/* ##LICENSE## */
/**
 * Supported dialog option types
 *
 * @author Andreas Filler
 */
public enum PushNotificationTypes {
	IOS, ANDROID;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ") + ":";
	}
}
