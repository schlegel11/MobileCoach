package ch.ethz.mc.model.persistent.types;

/* ##LICENSE## */
/**
 * Supported {@link MediaObjectTypes}
 *
 * @author Andreas Filler
 */
public enum MediaObjectTypes {
	HTML_TEXT, URL, IMAGE, AUDIO, VIDEO;

	public String toJSONField() {
		return name().toLowerCase().replace("_", "-");
	}
}
