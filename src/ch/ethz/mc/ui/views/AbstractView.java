package ch.ethz.mc.ui.views;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.ui.AdminNavigatorUI;
import ch.ethz.mc.ui.UISession;
import ch.ethz.mc.ui.views.components.AbstractCustomComponent;

import com.vaadin.navigator.View;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides methods for all {@link View}s
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public abstract class AbstractView extends VerticalLayout implements View {

	/**
	 * Returns the current {@link UISession}
	 * 
	 * @return
	 */
	protected UISession getUISession() {
		return getUI().getSession().getAttribute(UISession.class);
	}

	/**
	 * Returns the {@link AdminNavigatorUI}
	 * 
	 * @return
	 */
	protected AdminNavigatorUI getAdminUI() {
		return (AdminNavigatorUI) getUI();
	}

	/**
	 * Adds a tab to the given {@link Accordion} and allows to add an optional
	 * {@link ThemeResource} icon
	 * 
	 * @param accordion
	 * @param tabComponent
	 * @param accordionCaption
	 * @param accordionIcon
	 * @return
	 */
	protected Tab addTab(final Accordion accordion,
			final AbstractCustomComponent tabComponent,
			final AdminMessageStrings accordionCaption,
			final String accordionIcon) {
		if (accordionIcon == null) {
			return accordion.addTab(tabComponent,
					Messages.getAdminString(accordionCaption));
		} else {
			return accordion.addTab(tabComponent,
					Messages.getAdminString(accordionCaption),
					new ThemeResource(accordionIcon));
		}
	}
}
