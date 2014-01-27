package org.isgf.mhc.ui.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;

/**
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class MainView extends AbstractView implements View {
	@Override
	public void enter(final ViewChangeEvent event) {
		this.setSizeFull();

		// Layout with menu on left and view area on right
		final HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSizeFull();

		final Button button = new Button("Logout", new Button.ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				MainView.this.getAdminUI().logout();
			}
		});
		this.addComponent(button);
	}

}
