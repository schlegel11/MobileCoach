package ch.ethz.mc.ui.views;

/* ##LICENSE## */
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.ui.AdminNavigatorUI;
import ch.ethz.mc.ui.components.basics.AboutTextComponent;
import ch.ethz.mc.ui.components.views.LoginViewComponent;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;

/**
 * Provides login view and navigation in login view
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class LoginView extends AbstractView implements View {
	private LoginViewComponent loginViewComponent;

	@Override
	public void enter(final ViewChangeEvent event) {
		log.debug("Entered view {}", AdminNavigatorUI.VIEWS.LOGIN);

		setSizeFull();

		// Create view and listeners
		loginViewComponent = new LoginViewComponent();
		loginViewComponent.getLoginButton()
				.addClickListener(new LoginButtonClickListener());
		loginViewComponent.getAboutButton()
				.addClickListener(new AboutButtonClickListener());

		// Add enter as click shortcut for default button
		loginViewComponent.getLoginButton().setClickShortcut(KeyCode.ENTER);

		// Add view
		this.addComponent(loginViewComponent);
	}

	private class LoginButtonClickListener implements ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (Constants.isAutomaticallyLoginAsDefaultAdmin()) {
				log.warn("AUTOMATIC DEBUG LOGIN - BE CAREFUL");
				getAdminUI().login(Constants.getDefaultAdminUsername(),
						Constants.getDefaultAdminPassword());

				event.getButton().setEnabled(true);
			} else {
				if (!loginViewComponent.getUsernameField().isValid()) {
					getAdminUI().showWarningNotification(
							AdminMessageStrings.NOTIFICATION__NO_VALID_USERNAME);
					event.getButton().setEnabled(true);
					return;
				}
				if (!loginViewComponent.getPasswordField().isValid()) {
					getAdminUI().showWarningNotification(
							AdminMessageStrings.NOTIFICATION__NO_VALID_PASSWORD);
					event.getButton().setEnabled(true);
					return;
				}

				getAdminUI().login(
						loginViewComponent.getUsernameField().getValue(),
						loginViewComponent.getPasswordField().getValue());
				event.getButton().setEnabled(true);
			}
		}
	}

	private class AboutButtonClickListener implements ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			val aboutWindow = new Window(Messages
					.getAdminString(AdminMessageStrings.ABOUT_WINDOW__TITLE));
			aboutWindow.setModal(true);
			aboutWindow.setWidth("600px");
			aboutWindow.setHeight("400px");
			aboutWindow.setContent(new AboutTextComponent());

			getAdminUI().addWindow(aboutWindow);
		}
	}
}
