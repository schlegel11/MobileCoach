package ch.ethz.mc.model.persistent.types;

/* ##LICENSE## */
/**
 * Supported dialog message types
 *
 * @author Andreas Filler
 */
public enum DialogMessageTypes {
	PLAIN, INTENTION, COMMAND, MICRO_DIALOG_ACTIVATION;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}
}
