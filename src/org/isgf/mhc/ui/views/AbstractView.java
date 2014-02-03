package org.isgf.mhc.ui.views;

import org.isgf.mhc.ui.AdminNavigatorUI;
import org.isgf.mhc.ui.UISession;

import com.vaadin.navigator.View;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides methods for all {@link View}s
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public abstract class AbstractView extends VerticalLayout implements View {

	protected UISession getUISession() {
		return getUI().getSession().getAttribute(UISession.class);
	}

	protected AdminNavigatorUI getAdminUI() {
		return (AdminNavigatorUI) getUI();
	}
}
