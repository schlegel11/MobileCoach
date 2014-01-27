package org.isgf.mhc.ui;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.ui.views.LoginView;
import org.isgf.mhc.ui.views.MainView;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

/**
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Theme("mhc")
@Log4j2
public class AdminNavigatorUI extends UI {
	/**
	 * Contains all available admin views
	 * 
	 * @author Andreas Filler
	 */
	public static enum VIEWS {
		LOGIN, MAIN;

		public String getLowerCase() {
			if (this.equals(LOGIN)) {
				return "";
			} else {
				return this.toString().toLowerCase();
			}
		}
	};

	@Override
	protected void init(final VaadinRequest request) {
		// Set basic settings
		this.setLocale(Constants.ADMIN_LOCALE);
		this.getPage().setTitle(
				Messages.getAdminString(AdminMessageStrings.ADMIN_UI_NAME));

		// Configure the error handler for the UI
		this.setErrorHandler(new DefaultErrorHandler() {
			@Override
			public void error(final com.vaadin.server.ErrorEvent event) {
				log.warn("An error occurred in the UI: {}",
						event.getThrowable());

				// Create new session
				AdminNavigatorUI.this.clearSession();

				// Reload page
				AdminNavigatorUI.this.getPage().reload();
			}
		});

		// Create a navigator to control the views
		new Navigator(this, this);

		// Create and register the views
		this.getNavigator()
				.addView(VIEWS.LOGIN.getLowerCase(), LoginView.class);
		this.getNavigator().addView(VIEWS.MAIN.getLowerCase(), MainView.class);

		// Create session if none exists
		UISession uiSession = this.getSession().getAttribute(UISession.class);
		if (uiSession == null) {
			uiSession = new UISession();
			this.getSession().setAttribute(UISession.class, uiSession);
		}

		// Redirect to appropriate view
		this.getNavigator().addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(final ViewChangeEvent event) {
				val session = AdminNavigatorUI.this.getSession().getAttribute(
						UISession.class);

				// Check if a user has logged in
				final boolean isLoginView = event.getNewView() instanceof LoginView;

				if (!session.isLoggedIn() && !isLoginView) {
					// Redirect to login view always if a user has not yet
					// logged in
					AdminNavigatorUI.this.getNavigator().navigateTo(
							VIEWS.LOGIN.getLowerCase());
					return false;

				} else if (session.isLoggedIn() && isLoginView) {
					// If someone tries to access to login view while logged in,
					// then cancel
					return false;
				}

				return true;
			}

			@Override
			public void afterViewChange(final ViewChangeEvent event) {
				// do nothing
			}
		});
	}

	protected void clearSession() {
		this.getSession().setAttribute(UISession.class, new UISession());
	}

	/**
	 * Set new empty session and redirect to login page
	 */
	@Synchronized
	public void logout() {
		this.clearSession();

		this.getNavigator().navigateTo(VIEWS.LOGIN.getLowerCase());
	}
}