package org.isgf.mhc.model.ui;

import lombok.NoArgsConstructor;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;

/**
 * Basic class for objects that should be displayed in the UI
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public abstract class UIObject {
	/**
	 * Returns the appropriate localization {@link String}, filled with given
	 * placeholders (if provided)
	 * 
	 * @param adminMessageString
	 * @param values
	 * @return
	 */
	protected static String localize(
			final AdminMessageStrings adminMessageString,
			final Object... values) {
		return Messages.getAdminString(adminMessageString, values);
	}

	@Override
	public abstract String toString();
}
