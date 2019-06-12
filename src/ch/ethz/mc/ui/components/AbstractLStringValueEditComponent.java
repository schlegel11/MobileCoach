package ch.ethz.mc.ui.components;

/* ##LICENSE## */
import java.util.List;

import ch.ethz.mc.model.persistent.subelements.LString;

/**
 * Extends an {@link AbstractConfirmationComponent} with the ability to set and
 * return a {@link LString} value
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public abstract class AbstractLStringValueEditComponent
		extends AbstractConfirmationComponent {

	/**
	 * Set the current {@link LString} value
	 *
	 * @return
	 */
	public abstract void setLStringValue(final LString value);

	/**
	 * Return the current {@link LString} value
	 *
	 * @return
	 */
	public abstract LString getLStringValue();

	/**
	 * Adds variables to select to the component
	 *
	 * @return
	 */
	public abstract void addVariables(final List<String> variables);
}
