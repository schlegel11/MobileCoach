package org.isgf.mhc.ui.views;

import java.util.ArrayList;
import java.util.List;

import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.ui.AdminNavigatorUI;
import org.isgf.mhc.ui.views.components.MainViewComponent;
import org.isgf.mhc.ui.views.components.MenuButtonComponent;
import org.isgf.mhc.ui.views.components.WelcomeTabComponent;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;

@SuppressWarnings("serial")
@Log4j2
public class MainView extends AbstractView implements View, LayoutClickListener {
	private MainViewComponent				mainViewComponent;

	private final List<MenuButtonComponent>	menuButtons	= new ArrayList<MenuButtonComponent>();
	private final List<Tab>					activeTabs	= new ArrayList<TabSheet.Tab>();

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

		// Collect menu buttons
		menuButtons.add(mainViewComponent.getWelcomeButton());
		menuButtons.add(mainViewComponent.getInterventionsButton());
		menuButtons.add(mainViewComponent.getUsersButton());
		currentMenuButton = mainViewComponent.getWelcomeButton();
		currentMenuButton.addStyleName("active");

		// switchToWelcomeView();

		// Add view
		this.addComponent(mainViewComponent);
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
					log.debug("USERS button clicked");
					if (clickedComponent != currentMenuButton) {
						currentMenuButton.removeStyleName("active");
						currentMenuButton = (MenuButtonComponent) clickedComponent;
						currentMenuButton.addStyleName("active");
						switchToUsersView();
					}
					break componentLoop;
			}
			clickedComponent = clickedComponent.getParent();
		}
	}

	@Synchronized
	private void removeAllTabs() {
		for (final Tab activeTab : activeTabs) {
			mainViewComponent.getContentAccordion().removeTab(activeTab);
		}
		activeTabs.clear();
	}

	/*
	 * Switch views
	 */
	@Synchronized
	private void switchToWelcomeView() {
		removeAllTabs();
		activeTabs.add(mainViewComponent.getContentAccordion().addTab(
				new WelcomeTabComponent(), "Welcome!"));
	}

	@Synchronized
	private void switchToInterventionsView() {
		removeAllTabs();
		activeTabs.add(mainViewComponent.getContentAccordion().addTab(
				new WelcomeTabComponent(), "Interventions"));
	}

	@Synchronized
	private void switchToUsersView() {
		removeAllTabs();
		activeTabs.add(mainViewComponent.getContentAccordion().addTab(
				new WelcomeTabComponent(), "Users"));
	}
}
