package ch.ethz.mc.model.persistent.types;

/* ##LICENSE## */
/**
 * Supported external ID dialog option types
 *
 * @author Andreas Filler
 */
public enum ExternalIdDialogOptionTypes {
	DEEPSTREAM_PARTICIPANT;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}
}
