package org.isgf.mhc.ui.views;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.ui.AdminNavigatorUI;
import org.isgf.mhc.ui.views.components.LoginViewComponent;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

/**
 * Provides login view and navigation in login view
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class LoginView extends AbstractView implements View {
	private LoginViewComponent	loginViewComponent;

	@Override
	public void enter(final ViewChangeEvent event) {
		log.debug("Entered view {}", AdminNavigatorUI.VIEWS.LOGIN);

		setSizeFull();

		// Create view and listeners
		loginViewComponent = new LoginViewComponent();
		loginViewComponent.getLoginButton().addClickListener(
				new LoginButtonListener());

		// Add view
		this.addComponent(loginViewComponent);
	}

	private class LoginButtonListener implements ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (!loginViewComponent.getUsernameField().isValid()) {
				getAdminUI().showWarningNotification(
						AdminMessageStrings.NOTIFICATION__NO_VALID_USERNAME);
				return;
			}
			if (!loginViewComponent.getPasswordField().isValid()) {
				getAdminUI().showWarningNotification(
						AdminMessageStrings.NOTIFICATION__NO_VALID_PASSWORD);
				return;
			}

			getAdminUI().login(
					loginViewComponent.getUsernameField().getValue(),
					loginViewComponent.getPasswordField().getValue());
		}
	}
}
