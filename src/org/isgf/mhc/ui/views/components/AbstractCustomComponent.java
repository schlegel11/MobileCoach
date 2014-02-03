package org.isgf.mhc.ui.views.components;

import org.isgf.mhc.ui.AdminNavigatorUI;
import org.isgf.mhc.ui.UISession;

import com.vaadin.ui.CustomComponent;
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
}
