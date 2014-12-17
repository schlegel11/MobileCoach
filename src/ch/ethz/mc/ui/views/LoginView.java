package ch.ethz.mc.ui.views;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.ui.AdminNavigatorUI;
import ch.ethz.mc.ui.views.components.basics.AboutTextComponent;
import ch.ethz.mc.ui.views.components.views.LoginViewComponent;

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
	private LoginViewComponent	loginViewComponent;

	@Override
	public void enter(final ViewChangeEvent event) {
		log.debug("Entered view {}", AdminNavigatorUI.VIEWS.LOGIN);

		setSizeFull();

		// Create view and listeners
		loginViewComponent = new LoginViewComponent();
		loginViewComponent.getLoginButton().addClickListener(
				new LoginButtonClickListener());
		loginViewComponent.getAboutButton().addClickListener(
				new AboutButtonClickListener());

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
			} else {
				if (!loginViewComponent.getUsernameField().isValid()) {
					getAdminUI()
							.showWarningNotification(
									AdminMessageStrings.NOTIFICATION__NO_VALID_USERNAME);
					return;
				}
				if (!loginViewComponent.getPasswordField().isValid()) {
					getAdminUI()
							.showWarningNotification(
									AdminMessageStrings.NOTIFICATION__NO_VALID_PASSWORD);
					return;
				}

				getAdminUI().login(
						loginViewComponent.getUsernameField().getValue(),
						loginViewComponent.getPasswordField().getValue());
			}
		}
	}

	private class AboutButtonClickListener implements ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			val aboutWindow = new Window(
					Messages.getAdminString(AdminMessageStrings.ABOUT_WINDOW__TITLE));
			aboutWindow.setModal(true);
			aboutWindow.setWidth("600px");
			aboutWindow.setHeight("400px");
			aboutWindow.setContent(new AboutTextComponent());

			getAdminUI().addWindow(aboutWindow);

		}
	}
}
