package org.isgf.mhc.ui.views;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.ui.AdminNavigatorUI;
import org.isgf.mhc.ui.views.components.MainViewComponent;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

@SuppressWarnings("serial")
@Log4j2
public class MainView extends AbstractView implements View {
	private MainViewComponent	mainViewComponent;

	@Override
	public void enter(final ViewChangeEvent event) {
		log.debug("Entered view {}", AdminNavigatorUI.VIEWS.MAIN);

		this.setSizeFull();

		this.mainViewComponent = new MainViewComponent();
		this.mainViewComponent.getLogoutButton().addClickListener(
				new LogoutButtonListener());

		this.addComponent(this.mainViewComponent);
	}

	private class LogoutButtonListener implements ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			MainView.this.getAdminUI().logout();
		}

	}
}
