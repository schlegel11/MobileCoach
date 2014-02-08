package org.isgf.mhc.ui.views.components;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.ui.AdminNavigatorUI;
import org.isgf.mhc.ui.UISession;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * Provides methods for all {@link CustomComponent}s
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public abstract class AbstractCustomComponent extends CustomComponent {

	protected UISession getUISession() {
		return UI.getCurrent().getSession().getAttribute(UISession.class);
	}

	protected AdminNavigatorUI getAdminUI() {
		return (AdminNavigatorUI) UI.getCurrent();
	}

	protected void localize(final AbstractComponent component,
			final AdminMessageStrings adminMessageString,
			final Object... values) {
		final String valueToSet = Messages.getAdminString(adminMessageString,
				values);

		if (component instanceof Label) {
			((Label) component).setValue(valueToSet);
		} else {
			component.setCaption(valueToSet);
		}
	}
}
