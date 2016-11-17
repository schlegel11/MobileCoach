package ch.ethz.mc.ui.views;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
 *
 * For details see README.md file in the root folder of this project.
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
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ThemeImageStrings;
import ch.ethz.mc.ui.AdminNavigatorUI;
import ch.ethz.mc.ui.views.components.access_control.AccessControlTabComponentWithController;
import ch.ethz.mc.ui.views.components.account.AccountTabComponentWithController;
import ch.ethz.mc.ui.views.components.basics.MenuButtonComponent;
import ch.ethz.mc.ui.views.components.interventions.AllInterventionsTabComponentWithController;
import ch.ethz.mc.ui.views.components.views.MainViewComponent;
import ch.ethz.mc.ui.views.components.welcome.WelcomeTabComponentWithController;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

/**
 * Provides main view and navigation in main view
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MainView extends AbstractView implements View, LayoutClickListener {
	private MainViewComponent	mainViewComponent;

	private MenuButtonComponent	currentMenuButton;

	@Override
	public void enter(final ViewChangeEvent event) {
		log.debug("Entered view {}", AdminNavigatorUI.VIEWS.MAIN);

		setSizeFull();

		// Create view and listeners
		mainViewComponent = new MainViewComponent();

		mainViewComponent.getLogoutButton().addClickListener(
				new LogoutButtonListener());
		mainViewComponent.getMenuButtonsLayout().addLayoutClickListener(this);

		// Adjust view of non-admins
		if (!getUISession().isAdmin()) {
			mainViewComponent.getAccessControlButton().setVisible(false);
		}

		// Collect menu buttons
		currentMenuButton = mainViewComponent.getWelcomeButton();
		currentMenuButton.addStyleName("active");

		// Add view
		this.addComponent(mainViewComponent);

		// Switch to welcome view
		switchToWelcomeView();
	}

	private class LogoutButtonListener implements ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			getAdminUI().logout();
			event.getButton().setEnabled(true);
		}

	}

	@Override
	public void layoutClick(final LayoutClickEvent event) {
		Component clickedComponent = event.getClickedComponent();

		// Go through component tree to find out which button has been clicked
		componentLoop: while (clickedComponent != null) {
			if (clickedComponent == mainViewComponent.getWelcomeButton()) {
				log.debug("WELCOME button clicked");
				if (clickedComponent != currentMenuButton) {
					currentMenuButton.removeStyleName("active");
					currentMenuButton = (MenuButtonComponent) clickedComponent;
					currentMenuButton.addStyleName("active");
					switchToWelcomeView();
				}
				break componentLoop;
			} else if (clickedComponent == mainViewComponent
					.getInterventionsButton()) {
				log.debug("INTERVENTIONS button clicked");
				if (clickedComponent != currentMenuButton) {
					currentMenuButton.removeStyleName("active");
					currentMenuButton = (MenuButtonComponent) clickedComponent;
					currentMenuButton.addStyleName("active");
					switchToInterventionsView();
				}
				break componentLoop;
			} else if (clickedComponent == mainViewComponent
					.getAccessControlButton()) {
				log.debug("ACCESS CONTROL button clicked");
				if (clickedComponent != currentMenuButton) {
					currentMenuButton.removeStyleName("active");
					currentMenuButton = (MenuButtonComponent) clickedComponent;
					currentMenuButton.addStyleName("active");
					switchToAccessControlView();
				}
				break componentLoop;
			} else if (clickedComponent == mainViewComponent.getAccountButton()) {
				log.debug("ACCOUNT button clicked");
				if (clickedComponent != currentMenuButton) {
					currentMenuButton.removeStyleName("active");
					currentMenuButton = (MenuButtonComponent) clickedComponent;
					currentMenuButton.addStyleName("active");
					switchToAccountView();
				}
				break componentLoop;
			}
			clickedComponent = clickedComponent.getParent();
		}
	}

	@Synchronized
	private void removeAllTabs() {
		mainViewComponent.getContentAccordion().removeAllComponents();
	}

	/*
	 * Switch views
	 */
	@Synchronized
	private void switchToWelcomeView() {
		getAdminUI().getLockingService().releaseLockOfUISession(getUISession());

		removeAllTabs();

		addTab(mainViewComponent.getContentAccordion(),
				new WelcomeTabComponentWithController(),
				AdminMessageStrings.MAIN_VIEW__WELCOME_TAB,
				ThemeImageStrings.WELCOME_ICON);
	}

	@Synchronized
	public void switchToInterventionsView() {
		removeAllTabs();

		addTab(mainViewComponent.getContentAccordion(),
				new AllInterventionsTabComponentWithController(this),
				AdminMessageStrings.MAIN_VIEW__INTERVENTIONS_TAB,
				ThemeImageStrings.INTERVENTIONS_ICON);
	}

	@Synchronized
	private void switchToAccessControlView() {
		getAdminUI().getLockingService().releaseLockOfUISession(getUISession());

		removeAllTabs();

		addTab(mainViewComponent.getContentAccordion(),
				new AccessControlTabComponentWithController(),
				AdminMessageStrings.MAIN_VIEW__ACCESS_CONTROL_TAB,
				ThemeImageStrings.ACCESS_CONTROL_ICON);
	}

	@Synchronized
	private void switchToAccountView() {
		getAdminUI().getLockingService().releaseLockOfUISession(getUISession());

		removeAllTabs();

		addTab(mainViewComponent.getContentAccordion(),
				new AccountTabComponentWithController(getUISession()
						.getCurrentAuthorId()),
						AdminMessageStrings.MAIN_VIEW__ACCOUNT_TAB,
						ThemeImageStrings.ACCOUNT_ICON);
	}
}
