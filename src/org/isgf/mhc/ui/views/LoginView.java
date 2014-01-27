package org.isgf.mhc.ui.views;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.ui.AdminNavigatorUI;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

@SuppressWarnings("serial")
@Log4j2
public class LoginView extends AbstractView implements View {
	private LoginViewComponent	loginViewComponent;

	@Override
	public void enter(final ViewChangeEvent event) {
		log.debug("Entered view {}", AdminNavigatorUI.VIEWS.LOGIN);

		this.setSizeFull();

		this.loginViewComponent = new LoginViewComponent();
		this.loginViewComponent.getLoginButton().addClickListener(
				new LoginButtonListener());

		this.addComponent(this.loginViewComponent);
	}

	private class LoginButtonListener implements ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			LoginView.this.getUISession().setLoggedIn(true);

			LoginView.this.getUI().getNavigator()
					.navigateTo(AdminNavigatorUI.VIEWS.MAIN.getLowerCase());
		}

	}
}
