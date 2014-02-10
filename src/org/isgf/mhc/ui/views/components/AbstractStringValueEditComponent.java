package org.isgf.mhc.ui.views.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Delegate;
import lombok.Setter;

import com.vaadin.ui.Button;

/**
 * Extends an {@link AbstractCustomComponent} with the ability to set and return
 * a {@link String} value
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public abstract class AbstractStringValueEditComponent extends
		AbstractCustomComponent {
	/**
	 * The {@link String} to create/edit in this component
	 */
	@Setter
	private String	stringValue;

	private interface ReadOnlyStringList {
		boolean addAll(final Collection<String> items);

		String[] toArray(final String[] stringArray);
	}

	/**
	 * The {@link String} to create/edit in this component
	 */

	@Delegate(types = ReadOnlyStringList.class)
	private final List<String>	availableVariables	= new ArrayList<String>();

	/**
	 * Return the current {@link String} value
	 * 
	 * @return
	 */
	public abstract String getStringValue();

	/**
	 * Register the listener which is called when the OK button has been clicked
	 * 
	 * @param clickListener
	 */
	public abstract void registerOkButtonListener(
			final Button.ClickListener clickListener);

	/**
	 * Register the listener which is called when the Cancel button has been
	 * clicked
	 * 
	 * @param clickListener
	 */
	public abstract void registerCancelButtonListener(
			final Button.ClickListener clickListener);
}
