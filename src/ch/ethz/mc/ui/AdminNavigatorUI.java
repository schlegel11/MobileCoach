package ch.ethz.mc.ui;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.Messages;
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
				AdminNavigatorUI.this.clearSession();

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

		// Always create new session
		clearSession();

		// Redirect to appropriate view
		getNavigator().addViewChangeListener(this);

		// Listener for close
		addDetachListener(this);
	}

	protected void clearSession() {
		getSession().setAttribute(UISession.class, new UISession());
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
		clearSession();

		getNavigator().navigateTo(VIEWS.LOGIN.getLowerCase());
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

	/**
	 * Returns the {@link UISession} of the current currentAuthorId
	 * 
	 * @return
	 */
	private UISession getUISession() {
		return getSession().getAttribute(UISession.class);
	}

	@Override
	public void detach(final DetachEvent event) {
		log.debug("View detached");
	}
}