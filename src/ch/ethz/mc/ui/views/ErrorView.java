package ch.ethz.mc.ui.views;

/* ##LICENSE## */
import lombok.extern.log4j.Log4j2;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

import ch.ethz.mc.ui.components.views.ErrorViewComponent;

/**
 * Provides error view
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class ErrorView extends AbstractView implements View {
	private ErrorViewComponent errorViewComponent;

	@Override
	public void enter(final ViewChangeEvent event) {
		log.debug("Entered ERROR view");

		setSizeFull();

		// Create view and listeners
		errorViewComponent = new ErrorViewComponent();

		// Add view
		this.addComponent(errorViewComponent);
	}
}
