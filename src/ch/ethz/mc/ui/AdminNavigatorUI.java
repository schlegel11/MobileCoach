package ch.ethz.mc.ui;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.services.internal.LockingService;
import ch.ethz.mc.ui.views.ErrorView;
import ch.ethz.mc.ui.views.LoginView;
import ch.ethz.mc.ui.views.MainView;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * Navigates between views and cares for session management (login/logout)
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Theme("mc")
@Log4j2
public class AdminNavigatorUI extends UI implements ViewChangeListener,
DetachListener {

	private UISession	uiSession;

	public AdminNavigatorUI() {
		val sessionId = VaadinSession.getCurrent().getSession().getId();
		log.debug("Creating new UI session based on session {}", sessionId);
		uiSession = new UISession(sessionId);
	}

	/**
	 * Contains all available admin views
	 *
	 * @author Andreas Filler
	 */
	public static enum VIEWS {
		LOGIN, MAIN;

		public String getLowerCase() {
			if (equals(LOGIN)) {
				return "";
			} else {
				return toString().toLowerCase();
			}
		}
	};

	@Override
	protected void init(final VaadinRequest request) {
		log.debug("Init admin navigator ui");
		// Set basic settings
		setLocale(Constants.getAdminLocale());
		getPage()
		.setTitle(
				Messages.getAdminString(AdminMessageStrings.APPLICATION__NAME_LONG));

		// Configure the error handler for the UI
		setErrorHandler(new DefaultErrorHandler() {
			@Override
			public void error(final com.vaadin.server.ErrorEvent event) {
				log.warn("An error occurred in the UI: {}",
						event.getThrowable());

				// Create new session
				AdminNavigatorUI.this.resetSession();

				// Reload page
				AdminNavigatorUI.this.getPage().reload();
			}
		});

		// Create a navigator to control the views
		new Navigator(this, this);

		// Create and register the views
		getNavigator().addView(VIEWS.LOGIN.getLowerCase(), LoginView.class);
		getNavigator().addView(VIEWS.MAIN.getLowerCase(), MainView.class);
		getNavigator().setErrorView(ErrorView.class);

		// Inform about too small window
		getPage().addBrowserWindowResizeListener(
				new BrowserWindowResizeListener() {
					long	lastNotification	= 0;

					@Override
					public void browserWindowResized(
							final BrowserWindowResizeEvent event) {
						if ((event.getWidth() < 1100 || event.getHeight() < 650)
								&& System.currentTimeMillis()
								- lastNotification > 5000) {
							lastNotification = System.currentTimeMillis();
							showWarningNotification(AdminMessageStrings.GENERAL__RESIZE_ERROR_MESSAGE);
						}
					}
				});

		// Redirect to appropriate view
		getNavigator().addViewChangeListener(this);

		// Listener for close
		addDetachListener(this);
	}

	@Synchronized
	protected void resetSession() {
		log.debug("Resetting UI session");

		if (uiSession != null) {
			getLockingService().releaseLockOfUISession(uiSession);
		}

		val sessionId = VaadinSession.getCurrent().getSession().getId();
		log.debug("Creating new UI session based on session {}", sessionId);
		uiSession = new UISession(sessionId);
	}

	public void showInformationNotification(final AdminMessageStrings message,
			final Object... values) {
		Notification.show(Messages.getAdminString(message, values),
				Notification.Type.HUMANIZED_MESSAGE);
	}

	public void showWarningNotification(final AdminMessageStrings message,
			final Object... values) {
		Notification.show(Messages.getAdminString(message, values),
				Notification.Type.WARNING_MESSAGE);
	}

	public void showErrorNotification(final AdminMessageStrings message,
			final Object... values) {
		Notification.show(Messages.getAdminString(message, values),
				Notification.Type.ERROR_MESSAGE);
	}

	/**
	 * Login currentAuthorId if provided login information is correct
	 *
	 * @param currentAuthorUsername
	 * @param password
	 */
	public void login(final String username, final String password) {
		val author = MC.getInstance()
				.getInterventionAdministrationManagerService()
				.authorAuthenticateAndReturn(username, password);

		if (author == null) {
			showErrorNotification(AdminMessageStrings.NOTIFICATION__WRONG_LOGIN);
		} else {
			final UISession uiSession = getUISession();
			uiSession.setLoggedIn(true);
			uiSession.setAdmin(author.isAdmin());
			uiSession.setCurrentAuthorId(author.getId());
			uiSession.setCurrentAuthorUsername(author.getUsername());

			getUI().getNavigator().navigateTo(
					AdminNavigatorUI.VIEWS.MAIN.getLowerCase());
		}
	}

	/**
	 * Set new empty session and redirect to login page
	 */
	@Synchronized
	public void logout() {
		resetSession();

		getNavigator().navigateTo(VIEWS.LOGIN.getLowerCase());
	}

	/**
	 * Returns the {@link UISession} of the current currentAuthorId
	 *
	 * @return
	 */
	public UISession getUISession() {
		return uiSession;
	}

	/**
	 * Returns the {@link LockingService}
	 *
	 * @return
	 */
	public LockingService getLockingService() {
		return MC.getInstance().getLockingService();
	}

	@Override
	public boolean beforeViewChange(final ViewChangeEvent event) {
		val session = getUISession();

		// Check if a user has logged in
		final boolean newViewIsLoginView = event.getNewView() instanceof LoginView;

		if (!session.isLoggedIn() && !newViewIsLoginView) {
			// Redirect to login view always if a user has not yet
			// logged in
			AdminNavigatorUI.this.getNavigator().navigateTo(
					VIEWS.LOGIN.getLowerCase());
			return false;
		} else if (session.isLoggedIn() && newViewIsLoginView) {
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

	@Override
	public void detach(final DetachEvent event) {
		log.debug("View detached");

		getLockingService().releaseLockOfUISession(getUISession());
	}
}