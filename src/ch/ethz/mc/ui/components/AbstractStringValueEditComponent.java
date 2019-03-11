package ch.ethz.mc.ui.components;

/* ##LICENSE## */
import java.util.List;

/**
 * Extends an {@link AbstractConfirmationComponent} with the ability to set and
 * return a {@link String} value
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public abstract class AbstractStringValueEditComponent
		extends AbstractConfirmationComponent {

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

	/**
	 * Adds variables to select to the component
	 * 
	 * @return
	 */
	public abstract void addVariables(final List<String> variables);
}
