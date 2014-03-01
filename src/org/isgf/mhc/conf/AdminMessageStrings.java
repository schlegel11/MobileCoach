package org.isgf.mhc.conf;

public enum AdminMessageStrings {
	/*
	 * GLOBAL
	 */
	// Application
	APPLICATION__NAME, APPLICATION__NAME_SHORT, APPLICATION__NAME_LONG,
	// General
	GENERAL__OK, GENERAL__CANCEL, GENERAL__NEW, GENERAL__ADD, GENERAL__DELETE, GENERAL__REMOVE, GENERAL__IMPORT, GENERAL__EXPORT, GENERAL__RENAME, GENERAL__EDIT, GENERAL__DUPLICATE, GENERAL__MOVE_UP, GENERAL__MOVE_DOWN, GENERAL__MAKE_SUB, GENERAL__MAKE_SUPER, GENERAL__RESIZE_ERROR_MESSAGE, GENERAL__UPLOAD, GENERAL__CLOSE, GENERAL__EMPTY,
	// Media object integration component
	MEDIA_OBJECT_INTEGRATION_COMPONENT__NO_FILE_UPLOADED,
	/*
	 * TABS
	 */
	// Welcome tab
	WELCOME_TAB__WELCOME_MESSAGE,
	// Access control tab
	ACCESS_CONTROL_TAB__MAKE_AUTHOR, ACCESS_CONTROL_TAB__MAKE_ADMIN, ACCESS_CONTROL_TAB__SET_PASSWORD,
	// Intervention editing container
	INTERVENTION_EDITING_CONTAINER__LIST_ALL_INTERVENTIONS_BUTTON, INTERVENTION_EDITING_CONTAINER__INTERVENTIONS_TITLE, INTERVENTION_EDITING_CONTAINER__BASIC_SETTINGS_TAB, INTERVENTION_EDITING_CONTAINER__ACCESS_TAB, INTERVENTION_EDITING_CONTAINER__VARIABLES_TAB, INTERVENTION_EDITING_CONTAINER__MONITORING_MESSAGE_GROUPS_TAB, INTERVENTION_EDITING_CONTAINER__SCREENING_SURVEYS_TAB,
	// Monitoring message group editing
	MONITORING_MESSAGE_GROUP_EDITING__SEND_MESSAGE_IN_RANDOM_ORDER, MONITORING_MESSAGE_GROUP_EDITING__NEW_GROUP, MONITORING_MESSAGE_GROUP_EDITING__RENAME_GROUP, MONITORING_MESSAGE_GROUP_EDITING__DELETE_GROUP,
	// Monitoring message editing
	MONITORING_MESSAGE_EDITING__TEXT_WITH_PLACEHOLDERS, MONITORING_MESSAGE_EDITING__INTEGRATED_MEDIA_OBJECT, MONITORING_MESSAGE_EDITING__STORE_RESULT_TO_VARIABLE,
	// Intervention screening surveys editing
	INTERVENTION_SCREENING_SURVEY_EDITING__SHOW,
	// Monitoring rules editing
	MONITORING_RULES_EDITING__RESULT_VARIABLE_OF_SELECTED_RULE, MONITORING_RULES_EDITING__SEND_MESSAGE_AFTER_EXECUTION_OF_SELECTED_RULE, MONITORING_RULES_EDITING__SEND_NO_MESSAGE, MONITORING_RULES_EDITING__SEND_MESSAGE_FROM_GROUP, MONITORING_RULES_EDITING__SEND_MESSAGE_FROM_ALREADY_DELETED_GROUP,
	// Intervention basic settings tab
	INTERVENTION_BASIC_SETTINGS_TAB__SECONDS_DELAY_BETWEEN_EACH_PARTICIPANT_LABEL, INTERVENTION_BASIC_SETTINGS_TAB__HOUR_OF_RULE_EXECUTION_LABEL, INTERVENTION_BASIC_SETTINGS_TAB__SECONDS_DELAY_BETWEEN_EACH_PARTICIPANT_SLIDER, INTERVENTION_BASIC_SETTINGS_TAB__HOUR_OF_RULE_EXECUTION_SLIDER, INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_INTERVENTION_BUTTON_ACTIVE, INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_INTERVENTION_BUTTON_INACTIVE, INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_MESSAGING_BUTTON_ACTIVE, INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_MESSAGING_BUTTON_INACTIVE,
	// Account tab
	ACCOUNT_TAB__SET_PASSWORD, ACCOUNT_TAB__ACCOUNT_INFORMATION,
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
	ABSTRACT_STRING_EDITOR_WINDOW__SET_PASSWORD, ABSTRACT_STRING_EDITOR_WINDOW__ENTER_USERNAME_FOR_NEW_USER, ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_INTERVENTION, ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_INTERVENTION, ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_VARIABLE, ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_VARIABLE, ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_VALUE_FOR_VARIABLE, ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_MONITORING_MESSAGE_GROUP, ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_MONITORING_MESSAGE_GROUP, ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VARIABLE, ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TEXT_WITH_PLACEHOLDERS, ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_SCREENING_SURVEY, ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_SCREENING_SURVEY,
	// Abstract model object editor window titles
	ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__CREATE_MONITORING_MESSAGE, ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__EDIT_MONITORING_MESSAGE,
	// Confirmation window title
	CONFIRMATION_WINDOW__TITLE,
	/*
	 * NOTIFICATIONS
	 */
	// Notifications
	NOTIFICATION__UNKNOWN_ERROR, NOTIFICATION__WRONG_LOGIN, NOTIFICATION__NO_VALID_USERNAME, NOTIFICATION__NO_VALID_PASSWORD, NOTIFICATION__DEFAULT_ADMIN_CANT_BE_SET_AS_AUTHOR, NOTIFICATION__CANT_DELETE_YOURSELF, NOTIFICATION__DEFAULT_ADMIN_CANT_BE_DELETED, NOTIFICATION__CANT_DOWNGRADE_YOURSELF, NOTIFICATION__PASSWORD_CHANGED, NOTIFICATION__THE_GIVEN_PASSWORD_IS_NOT_SAFE, NOTIFICATION__ACCOUNT_CHANGED_TO_ADMIN, NOTIFICATION__ACCOUNT_CHANGED_TO_AUTHOR, NOTIFICATION__ACCOUNT_DELETED, NOTIFICATION__ACCOUNT_CREATED, NOTIFICATION__THE_GIVEN_USERNAME_IS_TOO_SHORT, NOTIFICATION__THE_GIVEN_USERNAME_IS_ALREADY_IN_USE, NOTIFICATION__INTERVENTION_CREATED, NOTIFICATION__INTERVENTION_DELETED, NOTIFICATION__INTERVENTION_RENAMED, NOTIFICATION__ACCOUNT_ADDED_TO_INTERVENTION, NOTIFICATION__ACCOUNT_REMOVED_FROM_INTERVENTION, NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_ALREADY_IN_USE, NOTIFICATION__VARIABLE_RENAMED, NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID, NOTIFICATION__VARIABLE_CREATED, NOTIFICATION__VARIABLE_DELETED, NOTIFICATION__VARIABLE_VALUE_CHANGED, NOTIFICATION__MONITORING_MESSAGE_GROUP_CREATED, NOTIFICATION__MONITORING_MESSAGE_GROUP_RENAMED, NOTIFICATION__MONITORING_MESSAGE_GROUP_DELETED, NOTIFICATION__MONITORING_MESSAGE_CREATED, NOTIFICATION__MONITORING_MESSAGE_UPDATED, NOTIFICATION__MONITORING_MESSAGE_DELETED, NOTIFICATION__UPLOAD_FAILED_OR_UNSUPPORTED_FILE_TYPE, NOTIFICATION__UPLOAD_SUCCESSFUL, NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM, NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES, NOTIFICATION__SCREENING_SURVEY_CREATED, NOTIFICATION__SCREENING_SURVEY_RENAMED, NOTIFICATION__SCREENING_SURVEY_DELETED, NOTIFICATION__MONITORING_RULE_DELETED,
	// System notifications
	SYSTEM_NOTIFICATION__SESSION_EXPIRED_CAPTION, SYSTEM_NOTIFICATION__SESSION_EXPIRED_MESSAGE, SYSTEM_NOTIFICATION__INTERNAL_ERROR_CAPTION, SYSTEM_NOTIFICATION__INTERNAL_ERROR_MESSAGE, SYSTEM_NOTIFICATION__COMMUNICATION_ERROR_CAPTION, SYSTEM_NOTIFICATION__COMMUNICATION_ERROR_MESSAGE, SYSTEM_NOTIFICATION__OUT_OF_SYNC_CAPTION, SYSTEM_NOTIFICATION__OUT_OF_SYNC_MESSAGE, SYSTEM_NOTIFICATION__COOKIES_DISABLED_CAPTION, SYSTEM_NOTIFICATION__COOKIES_DISABLED_MESSAGE,
	/*
	 * UI MODEL & UI COLUMNS
	 */
	// UI Model
	UI_MODEL__ADMINISTRATOR, UI_MODEL__AUTHOR, UI_MODEL__ACTIVE, UI_MODEL__INACTIVE, UI_MODEL__YES, UI_MODEL__NO,
	// UI Columns
	UI_COLUMNS__ACCOUNT, UI_COLUMNS__ACCOUNT_TYPE, UI_COLUMNS__INTERVENTION, UI_COLUMNS__INTERVENTION_STATUS, UI_COLUMNS__MESSAGING_STATUS, UI_COLUMNS__VARIABLE_NAME, UI_COLUMNS__VARIABLE_VALUE, UI_COLUMNS__MESSAGE_TEXT, UI_COLUMNS__HAS_LINKED_MEDIA_OBJECT, UI_COLUMNS__RESULT_VARIABLE, UI_COLUMNS__SCREENING_SURVEY, UI_COLUMNS__SCREENING_SURVEY_PASSWORD, UI_COLUMNS__SCREENING_SURVEY_STATUS;

}
