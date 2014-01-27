package org.isgf.mhc.ui.views;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.ui.AdminNavigatorUI;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;

/**
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class LoginView extends AbstractView implements View {

	@Override
	public void enter(final ViewChangeEvent event) {
		this.setSizeFull();
		this.setColumns(1);
		this.setRows(1);

		this.addStyleName("login-view");

		final Component loginPanel = this.createLoginPanel();
		this.addComponent(loginPanel, 0, 0);
		this.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);
	}

	private Panel createLoginPanel() {
		final Panel loginPanel = new Panel();

		final GridLayout contentLayout = new GridLayout();
		contentLayout.setWidth("");
		contentLayout.setColumns(3);
		contentLayout.setRows(2);
		contentLayout.setMargin(true);

		final Label titleLabel = new Label(
				Messages.getAdminString(AdminMessageStrings.ADMIN_UI_NAME));
		titleLabel.addStyleName("bold");
		contentLayout.addComponent(titleLabel, 0, 0, 2, 0);
		contentLayout.setComponentAlignment(titleLabel, Alignment.TOP_RIGHT);

		final TextField textField = new TextField("Username", "");
		contentLayout.addComponent(textField, 0, 1);
		textField.focus();
		final PasswordField passwordField = new PasswordField("Password", "");
		contentLayout.addComponent(passwordField, 1, 1);
		final Button button = new Button("Login", new LoginButtonListener());
		contentLayout.addComponent(button, 2, 1);

		loginPanel.setContent(contentLayout);

		return loginPanel;
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
