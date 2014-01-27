package org.isgf.mhc.ui.views;

import lombok.Synchronized;

import org.isgf.mhc.ui.AdminNavigatorUI;
import org.isgf.mhc.ui.UISession;

import com.vaadin.navigator.View;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public abstract class AbstractView extends VerticalLayout implements View {
	@Synchronized
	protected UISession getUISession() {
		UISession uiSession = this.getUI().getSession()
				.getAttribute(UISession.class);

		if (uiSession == null) {
			uiSession = new UISession();
			this.getUI().getSession().setAttribute(UISession.class, uiSession);
		}

		return uiSession;
	}

	protected AdminNavigatorUI getAdminUI() {
		return (AdminNavigatorUI) this.getUI();
	}
}
