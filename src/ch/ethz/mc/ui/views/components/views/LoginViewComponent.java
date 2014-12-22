package ch.ethz.mc.ui.views.components.views;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health IS-Lab
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
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the login view component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class LoginViewComponent extends AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout	mainLayout;
	@AutoGenerated
	private Button			aboutButton;
	@AutoGenerated
	private Panel			loginPanel;
	@AutoGenerated
	private AbsoluteLayout	loginPanelLayout;
	@AutoGenerated
	private Label			titleLabel;
	@AutoGenerated
	private PasswordField	passwordField;
	@AutoGenerated
	private TextField		usernameField;
	@AutoGenerated
	private Button			loginButton;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public LoginViewComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(usernameField, AdminMessageStrings.LOGIN_VIEW__USERNAME_FIELD);
		localize(passwordField, AdminMessageStrings.LOGIN_VIEW__PASSWORD_FIELD);
		localize(titleLabel, AdminMessageStrings.APPLICATION__NAME);
		localize(loginButton, AdminMessageStrings.LOGIN_VIEW__LOGIN_BUTTON);
		localize(aboutButton, AdminMessageStrings.LOGIN_VIEW__ABOUT_LINK);

		usernameField.focus();
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setStyleName("login-view");
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// loginPanel
		loginPanel = buildLoginPanel();
		mainLayout.addComponent(loginPanel);
		mainLayout.setExpandRatio(loginPanel, 1.0f);
		mainLayout.setComponentAlignment(loginPanel, new Alignment(48));

		// aboutButton
		aboutButton = new Button();
		aboutButton.setStyleName("link");
		aboutButton.setCaption("!!! About");
		aboutButton.setImmediate(true);
		aboutButton.setWidth("-1px");
		aboutButton.setHeight("-1px");
		mainLayout.addComponent(aboutButton);
		mainLayout.setComponentAlignment(aboutButton, new Alignment(10));

		return mainLayout;
	}

	@AutoGenerated
	private Panel buildLoginPanel() {
		// common part: create layout
		loginPanel = new Panel();
		loginPanel.setImmediate(false);
		loginPanel.setWidth("595px");
		loginPanel.setHeight("150px");

		// loginPanelLayout
		loginPanelLayout = buildLoginPanelLayout();
		loginPanel.setContent(loginPanelLayout);

		return loginPanel;
	}

	@AutoGenerated
	private AbsoluteLayout buildLoginPanelLayout() {
		// common part: create layout
		loginPanelLayout = new AbsoluteLayout();
		loginPanelLayout.setImmediate(false);
		loginPanelLayout.setWidth("100.0%");
		loginPanelLayout.setHeight("100.0%");

		// loginButton
		loginButton = new Button();
		loginButton.setCaption("!!! Login");
		loginButton.setIcon(new ThemeResource("img/login-icon-small.png"));
		loginButton.setImmediate(true);
		loginButton.setWidth("140px");
		loginButton.setHeight("-1px");
		loginButton.setTabIndex(3);
		loginPanelLayout.addComponent(loginButton,
				"bottom:20.0px;left:415.0px;");

		// usernameField
		usernameField = new TextField();
		usernameField.setCaption("!!! Username:");
		usernameField.setImmediate(false);
		usernameField.setWidth("170px");
		usernameField.setHeight("-1px");
		usernameField.setTabIndex(1);
		usernameField.setRequired(true);
		loginPanelLayout.addComponent(usernameField,
				"bottom:20.0px;left:15.0px;");

		// passwordField
		passwordField = new PasswordField();
		passwordField.setCaption("!!! Password:");
		passwordField.setImmediate(false);
		passwordField.setWidth("170px");
		passwordField.setHeight("-1px");
		passwordField.setTabIndex(2);
		passwordField.setRequired(true);
		loginPanelLayout.addComponent(passwordField,
				"bottom:20.0px;left:215.0px;");

		// titleLabel
		titleLabel = new Label();
		titleLabel.setStyleName("title-label");
		titleLabel.setImmediate(false);
		titleLabel.setWidth("540px");
		titleLabel.setHeight("35px");
		titleLabel.setValue("!!! Mobile Health Coach");
		loginPanelLayout.addComponent(titleLabel, "top:25.0px;left:15.0px;");

		return loginPanelLayout;
	}
}
