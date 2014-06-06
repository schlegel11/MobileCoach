package ch.ethz.mc.ui.views.components;

import com.vaadin.ui.Button;

/**
 * Extends an {@link AbstractCustomComponent} with the ability to confirm or
 * cancel
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public abstract class AbstractConfirmationComponent extends
		AbstractCustomComponent {
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
