package ch.ethz.mc.model.persistent.types;

import ch.ethz.mc.model.persistent.MicroDialogMessage;
import ch.ethz.mc.tools.VariableStringReplacer.ENCODING;

/* ##LICENSE## */
/**
 * Supported {@link TextFormatTypes} for {@link MicroDialogMessage}
 *
 * @author Andreas Filler
 */
public enum TextFormatTypes {
	PLAIN, HTML;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}

	/**
	 * Returns the appropriate encoding
	 * 
	 * @return
	 */
	public ENCODING toEncoding() {
		switch (this) {
			case HTML:
				return ENCODING.HTML;
			case PLAIN:
			default:
				return ENCODING.NONE;
		}
	}
}
