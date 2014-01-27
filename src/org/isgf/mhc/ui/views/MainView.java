package org.isgf.mhc.ui.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class MainView extends AbstractView implements View {
	@Override
	public void enter(final ViewChangeEvent event) {
		this.setSizeFull();
		this.setColumns(1);
		this.setRows(1);

		final Button button = new Button("Logout", new Button.ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				MainView.this.getAdminUI().logout();
			}
		});

		this.addComponent(button, 0, 0);
	}
}
