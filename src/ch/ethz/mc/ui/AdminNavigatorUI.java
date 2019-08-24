package ch.ethz.mc.ui;

/* ##LICENSE## */
import java.io.File;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.services.internal.LockingService;
import ch.ethz.mc.services.types.ModelObjectExchangeFormatTypes;
import ch.ethz.mc.ui.views.ErrorView;
import ch.ethz.mc.ui.views.LoginView;
import ch.ethz.mc.ui.views.MainView;
import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Navigates between views and cares for session management (login/logout)
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Theme("mc")
@Log4j2
public class AdminNavigatorUI extends UI
		implements ViewChangeListener, DetachListener {

	private UISession						uiSession;

	@Getter
	private File							clipboard;

	@Getter
	private ModelObjectExchangeFormatTypes	clipboardExchangeFormatType;

	public AdminNavigatorUI() {
		clipboard = null;
		clipboardExchangeFormatType = null;

		val session = VaadinSession.getCurrent().getSession();
		log.debug("Creating new UI Session object based on session {}",
				session.getId());

		uiSession = new UISession(session);
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
		getPage().setTitle(Messages
				.getAdminString(AdminMessageStrings.APPLICATION__NAME_LONG));

		// Store normalized base URL
		val servletRequest = (VaadinServletRequest) request;

		final String normalizedBaseURL = servletRequest.getRequestURL()
				.toString().substring(0,
						servletRequest.getRequestURL().toString()
								.indexOf(servletRequest.getRequestURI()))
				+ request.getContextPath() + "/";
		uiSession.setBaseURL(normalizedBaseURL);

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
					long lastNotification = 0;

					@Override
					public void browserWindowResized(
							final BrowserWindowResizeEvent event) {
						if ((event.getWidth() < 1100 || event.getHeight() < 650)
								&& System.currentTimeMillis()
										- lastNotification > 5000) {
							lastNotification = System.currentTimeMillis();
							showWarningNotification(
									AdminMessageStrings.GENERAL__RESIZE_ERROR_MESSAGE);
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
		log.debug("Resetting UI session object");

		String baseURL = null;
		if (uiSession != null) {
			baseURL = uiSession.getBaseURL();

			getLockingService().releaseLockOfUISession(uiSession);
			uiSession.clearWrappedSession();
		}

		val session = VaadinSession.getCurrent().getSession();
		log.debug("Creating new UI session object based on session {}",
				session.getId());
		uiSession = new UISession(session);
		uiSession.setBaseURL(baseURL);

		cleanupClipboard();
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
	 * Login backend user if provided login information is correct
	 *
	 * @param username
	 * @param password
	 */
	public void login(final String username, final String password) {
		val backendUser = MC.getInstance()
				.getInterventionAdministrationManagerService()
				.backendUserAuthenticateForEditingBackendAndReturn(username,
						password);

		if (backendUser == null) {
			showErrorNotification(
					AdminMessageStrings.NOTIFICATION__WRONG_LOGIN);
		} else {
			final UISession uiSession = getUISession();
			uiSession.setAdmin(backendUser.isAdmin());
			uiSession.setCurrentBackendUserId(backendUser.getId());
			uiSession.setCurrentBackendUserUsername(backendUser.getUsername());
			uiSession.setLoggedIn(true);

			getUI().getNavigator()
					.navigateTo(AdminNavigatorUI.VIEWS.MAIN.getLowerCase());
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
		final boolean newViewIsLoginView = event
				.getNewView() instanceof LoginView;

		if (!session.isLoggedIn() && !newViewIsLoginView) {
			// Redirect to login view always if a user has not yet
			// logged in
			AdminNavigatorUI.this.getNavigator()
					.navigateTo(VIEWS.LOGIN.getLowerCase());
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

		cleanupClipboard();

		getLockingService().releaseLockOfUISession(getUISession());
	}

	public void setClipboard(final File newClipboard,
			final ModelObjectExchangeFormatTypes newClipboardExchangeFormatType) {
		cleanupClipboard();

		clipboard = newClipboard;
		clipboardExchangeFormatType = newClipboardExchangeFormatType;
	}

	private void cleanupClipboard() {
		if (clipboard != null) {
			try {
				clipboard.delete();
			} catch (final Exception e) {
				clipboard.deleteOnExit();
			}
			clipboard = null;
		}
		clipboardExchangeFormatType = null;
	}
}