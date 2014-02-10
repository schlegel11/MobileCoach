package org.isgf.mhc.conf;

public enum AdminMessageStrings {
	/*
	 * GLOBAL
	 */
	// Application
	APPLICATION__NAME, APPLICATION__NAME_SHORT, APPLICATION__NAME_LONG,
	// General
	GENERAL__OK, GENERAL__CANCEL, GENERAL__NEW, GENERAL__DELETE, GENERAL__RESIZE_ERROR_MESSAGE,
	/*
	 * TABS
	 */
	// Welcome tab
	WELCOME_TAB__WELCOME_MESSAGE,
	// Access control tab
	ACCESS_CONTROL_TAB__MAKE_AUTHOR, ACCESS_CONTROL_TAB__MAKE_ADMIN, ACCESS_CONTROL_TAB__SET_PASSWORD,
	/*
	 * VIEWS
	 */
	// Login view
	LOGIN_VIEW__USERNAME_FIELD, LOGIN_VIEW__PASSWORD_FIELD, LOGIN_VIEW__LOGIN_BUTTON, LOGIN_VIEW__ABOUT_LINK,
	// Error view
	ERROR_VIEW__ERROR_MESSAGE,
	// Main view
	MAIN_VIEW__WELCOME_BUTTON, MAIN_VIEW__INTERVENTIONS_BUTTON, MAIN_VIEW__ACCESS_CONTROL_BUTTON, MAIN_VIEW__ACCOUNT_BUTTON, MAIN_VIEW__LOGOUT_BUTTON,
	// Main view tabs
	MAIN_VIEW__WELCOME_TAB, MAIN_VIEW__INTERVENTIONS_TAB, MAIN_VIEW__ACCESS_CONTROL_TAB, MAIN_VIEW__ACCOUNT_TAB,
	/*
	 * WINDOWS
	 */
	// About window
	ABOUT_WINDOW__TITLE, ABOUT_WINDOW__HTML_TEXT,
	// Placeholder string editor
	PLACEHOLDER_STRING_EDITOR__SELECT_VARIABLE,
	// Abstract string editor window titles
	ABSTRACT_STRING_EDITOR_WINDOW__SET_PASSWORD, ABSTRACT_STRING_EDITOR_WINDOW__ENTER_USERNAME_FOR_NEW_USER,
	/*
	 * NOTIFICATIONS
	 */
	// Notifications
	NOTIFICATION__UNKNOWN_ERROR, NOTIFICATION__WRONG_LOGIN, NOTIFICATION__NO_VALID_USERNAME, NOTIFICATION__NO_VALID_PASSWORD, NOTIFICATION__DEFAULT_ADMIN_CANT_BE_SET_AS_AUTHOR, NOTIFICATION__CANT_DELETE_YOURSELF, NOTIFICATION__DEFAULT_ADMIN_CANT_BE_DELETED, NOTIFICATION__PASSWORD_CHANGED, NOTIFICATION__THE_GIVEN_PASSWORD_IS_NOT_SAFE,
	// System notifications
	SYSTEM_NOTIFICATION__SESSION_EXPIRED_CAPTION, SYSTEM_NOTIFICATION__SESSION_EXPIRED_MESSAGE, SYSTEM_NOTIFICATION__INTERNAL_ERROR_CAPTION, SYSTEM_NOTIFICATION__INTERNAL_ERROR_MESSAGE, SYSTEM_NOTIFICATION__COMMUNICATION_ERROR_CAPTION, SYSTEM_NOTIFICATION__COMMUNICATION_ERROR_MESSAGE, SYSTEM_NOTIFICATION__OUT_OF_SYNC_CAPTION, SYSTEM_NOTIFICATION__OUT_OF_SYNC_MESSAGE, SYSTEM_NOTIFICATION__COOKIES_DISABLED_CAPTION, SYSTEM_NOTIFICATION__COOKIES_DISABLED_MESSAGE,
	/*
	 * UI MODEL & UI COLUMNS
	 */
	// UI Model
	UI_MODEL__ADMINISTRATOR, UI_MODEL__AUTHOR,
	// UI Columns
	UI_COLUMNS__ACCOUNT, UI_COLUMNS__ACCOUNT_TYPE;

}
