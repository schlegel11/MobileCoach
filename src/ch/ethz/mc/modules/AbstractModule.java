package ch.ethz.mc.modules;

import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ui.UIModule;
import ch.ethz.mc.ui.views.components.AbstractClosableEditComponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;

/**
 * Abstract module to be extended as module
 * 
 * @author Andreas Filler
 * 
 */
@SuppressWarnings("serial")
@Log4j2
public abstract class AbstractModule extends AbstractClosableEditComponent {

	public AbstractModule() {
		log.debug("Abstract module prepared to show in list");
	}

	/**
	 * Called before the window itself is shown
	 */
	public abstract void prepareToShow(final ObjectId interventionId);

	/**
	 * The name of the module
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * The button to close the module
	 * 
	 * @return
	 */
	protected abstract Button getCloseButton();

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		getCloseButton().addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(final ClickListener clickListener) {
		// not required
	}

	public UIModule toUIModule() {
		final UIModule uiModule = new UIModule(getName(), getClass());
		return uiModule;
	}
}
