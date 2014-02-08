package org.isgf.mhc.ui.views;

import java.util.ArrayList;
import java.util.List;

import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.ui.AdminNavigatorUI;
import org.isgf.mhc.ui.views.components.access_control.AccessControlTabComponent;
import org.isgf.mhc.ui.views.components.basics.MenuButtonComponent;
import org.isgf.mhc.ui.views.components.views.MainViewComponent;
import org.isgf.mhc.ui.views.components.welcome.WelcomeTabComponent;

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
	private MainViewComponent				mainViewComponent;

	private final List<MenuButtonComponent>	menuButtons	= new ArrayList<MenuButtonComponent>();

	private MenuButtonComponent				currentMenuButton;

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
			mainViewComponent.getAccountButton().setVisible(false);
		}

		// Collect menu buttons
		menuButtons.add(mainViewComponent.getWelcomeButton());
		menuButtons.add(mainViewComponent.getInterventionsButton());
		menuButtons.add(mainViewComponent.getAccessControlButton());
		menuButtons.add(mainViewComponent.getAccountButton());
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
		}

	}

	@Override
	public void layoutClick(final LayoutClickEvent event) {
		Component clickedComponent = event.getClickedComponent();

		// Go through component tree to find out which button has beend clicked
		componentLoop: while (clickedComponent != null) {
			switch (menuButtons.indexOf(clickedComponent)) {
				case 0:
					log.debug("WELCOME button clicked");
					if (clickedComponent != currentMenuButton) {
						currentMenuButton.removeStyleName("active");
						currentMenuButton = (MenuButtonComponent) clickedComponent;
						currentMenuButton.addStyleName("active");
						switchToWelcomeView();
					}
					break componentLoop;
				case 1:
					log.debug("INTERVENTIONS button clicked");
					if (clickedComponent != currentMenuButton) {
						currentMenuButton.removeStyleName("active");
						currentMenuButton = (MenuButtonComponent) clickedComponent;
						currentMenuButton.addStyleName("active");
						switchToInterventionsView();
					}
					break componentLoop;
				case 2:
					log.debug("ACCESS CONTROL button clicked");
					if (clickedComponent != currentMenuButton) {
						currentMenuButton.removeStyleName("active");
						currentMenuButton = (MenuButtonComponent) clickedComponent;
						currentMenuButton.addStyleName("active");
						switchToAccessControlView();
					}
					break componentLoop;
				case 3:
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
		removeAllTabs();
		mainViewComponent
				.getContentAccordion()
				.addTab(new WelcomeTabComponent(),
						Messages.getAdminString(AdminMessageStrings.MAIN_VIEW__WELCOME_TAB));
	}

	@Synchronized
	private void switchToInterventionsView() {
		removeAllTabs();
		mainViewComponent
				.getContentAccordion()
				.addTab(new WelcomeTabComponent(),
						Messages.getAdminString(AdminMessageStrings.MAIN_VIEW__INTERVENTIONS_TAB));
	}

	@Synchronized
	private void switchToAccessControlView() {
		removeAllTabs();
		mainViewComponent
				.getContentAccordion()
				.addTab(new AccessControlTabComponent(),
						Messages.getAdminString(AdminMessageStrings.MAIN_VIEW__ACCESS_CONTROL_TAB));
	}

	@Synchronized
	private void switchToAccountView() {
		removeAllTabs();
		mainViewComponent
				.getContentAccordion()
				.addTab(new WelcomeTabComponent(),
						Messages.getAdminString(AdminMessageStrings.MAIN_VIEW__ACCOUNT_TAB));
	}
}
