package org.isgf.mhc.ui.views.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Delegate;

/**
 * Extends an {@link AbstractConfirmationComponent} with the ability to set and
 * return
 * a {@link String} value
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public abstract class AbstractStringValueEditComponent extends
		AbstractConfirmationComponent {
	private interface ReadOnlyStringList {
		boolean addAll(final Collection<String> items);

		String[] toArray(final String[] stringArray);
	}

	@Delegate(types = ReadOnlyStringList.class)
	private final List<String>	availableVariables	= new ArrayList<String>();

	/**
	 * Set the current {@link String} value
	 * 
	 * @return
	 */
	public abstract void setStringValue(final String value);

	/**
	 * Return the current {@link String} value
	 * 
	 * @return
	 */
	public abstract String getStringValue();
}
