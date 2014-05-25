package org.isgf.mhc.modules;

import lombok.AccessLevel;
import lombok.Getter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ui.UIModule;
import org.isgf.mhc.ui.views.components.AbstractClosableEditComponent;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;

/**
 * Abstract module to be extended as module
 * 
 * @author Andreas Filler
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractModule extends AbstractClosableEditComponent {
	@Getter(value = AccessLevel.PRIVATE)
	private static ObjectId	interventionId;

	public AbstractModule(final ObjectId interventionId) {
		AbstractModule.interventionId = interventionId;
	}

	/**
	 * Called before the window itself is shown
	 */
	public abstract void prepareToShow();

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
