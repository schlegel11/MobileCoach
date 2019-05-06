package ch.ethz.mc.conf;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * @author Andreas Filler
 */
public enum AdminMessageStrings {
	/*
	 * GLOBAL
	 */
	// Application
	APPLICATION__NAME,
	APPLICATION__NAME_SHORT,
	APPLICATION__NAME_LONG,
	// General
	GENERAL__OK,
	GENERAL__CANCEL,
	GENERAL__NEW,
	GENERAL__ADD,
	GENERAL__DELETE,
	GENERAL__REMOVE,
	GENERAL__IMPORT,
	GENERAL__EXPORT,
	GENERAL__REPORT,
	GENERAL__RENAME,
	GENERAL__EDIT,
	GENERAL__DUPLICATE,
	GENERAL__MOVE_UP,
	GENERAL__MOVE_DOWN,
	GENERAL__MAKE_SUB,
	GENERAL__MAKE_SUPER,
	GENERAL__RESIZE_ERROR_MESSAGE,
	GENERAL__UPLOAD,
	GENERAL__CLOSE,
	GENERAL__EMPTY,
	GENERAL__EXPAND,
	GENERAL__COLLAPSE,
	GENERAL__REFRESH,
	GENERAL__RESULTS,
	GENERAL__PROBLEMS,
	GENERAL__I18N,
	GENERAL__SOLVE,
	GENERAL__SWITCH_TYPE,
	GENERAL__SWITCH_STATUS,
	GENERAL__SET_URL,
	GENERAL__OR,
	GENERAL__SAVE,
	GENERAL__INFINITE,
	// Media object integration component
	MEDIA_OBJECT_INTEGRATION_COMPONENT__NO_MEDIA_SET,
	MEDIA_OBJECT_INTEGRATION_COMPONENT__CREATE_HTML,
	/*
	 * TABS & COMPONENTS
	 */
	// Welcome tab
	WELCOME_TAB__WELCOME_MESSAGE,
	// Access control tab
	ACCESS_CONTROL_TAB__MAKE_TEAM_MANAGER,
	ACCESS_CONTROL_TAB__MAKE_AUTHOR,
	ACCESS_CONTROL_TAB__MAKE_ADMIN,
	ACCESS_CONTROL_TAB__SET_PASSWORD,
	// Access edit component
	ACCESS_EDIT_COMPONENT__SET_GROUP_PATTERN,
	// Intervention editing container
	INTERVENTION_EDITING_CONTAINER__LIST_ALL_INTERVENTIONS_BUTTON,
	INTERVENTION_EDITING_CONTAINER__INTERVENTIONS_TITLE,
	INTERVENTION_EDITING_CONTAINER__BASIC_SETTINGS_AND_MODULES_TAB,
	INTERVENTION_EDITING_CONTAINER__ACCESS_TAB,
	INTERVENTION_EDITING_CONTAINER__EXTERNAL_SERVICES_TAB,
	INTERVENTION_EDITING_CONTAINER__VARIABLES_TAB,
	INTERVENTION_EDITING_CONTAINER__MESSAGE_GROUPS_TAB,
	INTERVENTION_EDITING_CONTAINER__MICRO_DIALOGS_TAB,
	INTERVENTION_EDITING_CONTAINER__SCREENING_SURVEYS_TAB,
	INTERVENTION_EDITING_CONTAINER__RULES_TAB,
	INTERVENTION_EDITING_CONTAINER__PARTICIPANTS_TAB,
	// Intervention variables editing
	INTERVENTION_VARIABLES_EDITING__SWITCH_PRIVACY_BUTTON,
	INTERVENTION_VARIABLES_EDITING__SWITCH_ACCESS_BUTTON,
	// Intervention external services editing
	INTERVENTION_EXTERNAL_SERVICES_EDITING__RENEW_TOKEN_BUTTON,
	INTERVENTION_EXTERNAL_SERVICES_EDITING__ACTIVE_INACTIVE_BUTTON,
	INTERVENTION_EXTERNAL_SERVICES_EDITING__FIELD_VARIABLE_MAPPING_BUTTON,
	INTERVENTION_EXTERNAL_SERVICES_EDITING__SERVICE_LOGIN_JSON_TEXT_AREA,
	INTERVENTION_EXTERNAL_SERVICES_EDITING__SERVICE_LOGIN_HTTP_AUTH_TEXT_AREA,
	INTERVENTION_EXTERNAL_SERVICES_EDITING__SERVICE_EXTERNAL_MESSAGE_JSON_TEXT_AREA,
	// Monitoring message group editing
	MONITORING_MESSAGE_GROUP_EDITING__SEND_MESSAGE_IN_RANDOM_ORDER,
	MONITORING_MESSAGE_GROUP_EDITING__NEW_GROUP,
	MONITORING_MESSAGE_GROUP_EDITING__RENAME_GROUP,
	MONITORING_MESSAGE_GROUP_EDITING__DELETE_GROUP,
	MONITORING_MESSAGE_GROUP_EDITING__MESSAGES_EXPECT_TO_BE_ANSWERED_BY_PARTICIPANT,
	MONITORING_MESSAGE_GROUP_EDITING__MOVE_GROUP_LEFT,
	MONITORING_MESSAGE_GROUP_EDITING__MOVE_GROUP_RIGHT,
	MONITORING_MESSAGE_GROUP_EDITING__SEND_SAME_POSITION_IF_SENDING_AS_REPLY,
	MONITORING_MESSAGE_GROUP_EDITING__VALIDATION_EXPRESSION_LABEL,
	// Monitoring message editing
	MONITORING_MESSAGE_EDITING__TEXT_WITH_PLACEHOLDERS,
	MONITORING_MESSAGE_EDITING__INTEGRATED_MEDIA_OBJECT,
	MONITORING_MESSAGE_EDITING__INTERMEDIATE_SURVEY_LABEL,
	MONITORING_MESSAGE_EDITING__STORE_RESULT_TO_VARIABLE,
	MONITORING_MESSAGE_EDITING__STORE_MESSAGE_REPLY_TO_VARIABLE,
	MONITORING_MESSAGE_EDITING__MESSAGE_IS_COMMAND,
	MONITORING_MESSAGE_EDITING__MESSAGE_IS_PUSH_ONLY,
	MONITORING_MESSAGE_EDITING__RULE_INFORMATION_LABEL,
	MONITORING_MESSAGE_EDITING__ANSWER_TYPE_LABEL,
	MONITORING_MESSAGE_EDITING__ANSWER_OPTIONS_LABEL,
	// Micro dialog editing
	MICRO_DIALOG_EDITING__NEW_DIALOG,
	MICRO_DIALOG_EDITING__RENAME_DIALOG,
	MICRO_DIALOG_EDITING__DUPLICATE_DIALOG,
	MICRO_DIALOG_EDITING__MOVE_DIALOG_LEFT,
	MICRO_DIALOG_EDITING__MOVE_DIALOG_RIGHT,
	MICRO_DIALOG_EDITING__DELETE_DIALOG,
	MICRO_DIALOG_EDITING__COMMENT,
	MICRO_DIALOG_EDITING__NEW_MESSAGE,
	MICRO_DIALOG_EDITING__NEW_DECISION_POINT,
	// Micro dialog message editing,
	MICRO_DIALOG_MESSAGE_EDITING__TEXT_WITH_PLACEHOLDERS,
	MICRO_DIALOG_MESSAGE_EDITING__INTEGRATED_MEDIA_OBJECT,
	MICRO_DIALOG_MESSAGE_EDITING__INTERMEDIATE_SURVEY_LABEL,
	MICRO_DIALOG_MESSAGE_EDITING__MESSAGE_KEY_LABEL,
	MICRO_DIALOG_MESSAGE_EDITING__STORE_RESULT_TO_VARIABLE,
	MICRO_DIALOG_MESSAGE_EDITING__STORE_MESSAGE_REPLY_TO_VARIABLE,
	MICRO_DIALOG_MESSAGE_EDITING__MESSAGE_IS_COMMAND,
	MICRO_DIALOG_MESSAGE_EDITING__RULE_INFORMATION_LABEL,
	MICRO_DIALOG_MESSAGE_EDITING__MESSAGE_EXPECTS_TO_BE_ANSWERED,
	MICRO_DIALOG_MESSAGE_EDITING__MESSAGE_BLOCKS_DIALOG,
	MICRO_DIALOG_MESSAGE_EDITING__MESSAGE_IS_STICKY,
	MICRO_DIALOG_MESSAGE_EDITING__DEACTIVATES_ALL_OPEN_QUESTIONS,
	MICRO_DIALOG_MESSAGE_EDITING__NO_REPLY_VALUE_LABEL,
	MICRO_DIALOG_MESSAGE_EDITING__MINUTES_UNTIL_HANDLED_AS_NOT_ANSWERED_LABEL,
	MICRO_DIALOG_MESSAGE_EDITING__MINUTES_BUTTON_INFINITE,
	MICRO_DIALOG_MESSAGE_EDITING__ANSWER_TYPE_LABEL,
	MICRO_DIALOG_MESSAGE_EDITING__ANSWER_OPTIONS_LABEL,
	MICRO_DIALOG_MESSAGE_EDITING__TIMEFRAME_AFTER_SENDING_UNTIL_HANDLED_AS_NOT_ANSWERED_VALUE,
	// Micro dialog rule editing
	MICRO_DIALOG_RULE_EDITING__STORE_RESULT_TO_VARIABLE,
	MICRO_DIALOG_RULE_EDITING__STOP_MICRO_DIALOG_WHEN_TRUE,
	MICRO_DIALOG_RULE_EDITING__LEAVE_DECISION_POINT_WHEN_TRUE,
	MICRO_DIALOG_RULE_EDITING__NEXT_MICRO_DIALOG_WHEN_TRUE,
	MICRO_DIALOG_RULE_EDITING__NEXT_MICRO_DIALOG_MESSAGE_WHEN_TRUE,
	MICRO_DIALOG_RULE_EDITING__NEXT_MICRO_DIALOG_MESSAGE_WHEN_FALSE,
	// Intervention screening surveys editing
	INTERVENTION_SCREENING_SURVEY_EDITING__SHOW,
	// Abstract monitoring rules editing
	ABSTRACT_MONITORING_RULES_EDITING__RESULT_VARIABLE_OF_SELECTED_RULE,
	ABSTRACT_MONITORING_RULES_EDITING__SEND_MESSAGE_AFTER_EXECUTION_OF_SELECTED_RULE,
	ABSTRACT_MONITORING_RULES_EDITING__SEND_NO_MESSAGE,
	ABSTRACT_MONITORING_RULES_EDITING__SEND_MESSAGE_BUT_NO_GROUP_SELECTED,
	ABSTRACT_MONITORING_RULES_EDITING__SEND_MESSAGE_FROM_GROUP,
	ABSTRACT_MONITORING_RULES_EDITING__SEND_MESSAGE_FROM_ALREADY_DELETED_GROUP,
	ABSTRACT_MONITORING_RULES_EDITING__RULE_INFO_LABEL,
	ABSTRACT_MONITORING_RULES_EDITING__SEND_NO_MESSAGE_BUT_FINISH_INTERVENTION,
	ABSTRACT_MONITORING_RULES_EDITING__TO_PARTICIPANT,
	ABSTRACT_MONITORING_RULES_EDITING__TO_SUPERVISOR,
	ABSTRACT_MONITORING_RULES_EDITING__ACTIVATE_MICRO_DIALOG_BUT_NO_MICRO_DIALOG_SELECTED,
	ABSTRACT_MONITORING_RULES_EDITING__ACTIVATE_MICRO_DIALOG_BUT_MICRO_DIALOG_ALREADY_DELETED,
	ABSTRACT_MONITORING_RULES_EDITING__ACTIVATE_MICRO_DIALOG_WITH_NAME,
	// Abstract rule editing
	ABSTRACT_RULE_EDITING__RULE_WITH_PLACEHOLDERS,
	ABSTRACT_RULE_EDITING__RULE_COMPARISON_TERM_WITH_PLACEHOLDERS,
	ABSTRACT_RULE_EDITING__COMMENT,
	// Monitoring rule editing
	MONITORING_RULE_EDITING__SEND_MESSAGE_IF_TRUE,
	MONITORING_RULE_EDITING__SEND_TO_SUPERVISOR,
	MONITORING_RULE_EDITING__MARK_CASE_AS_SOLVED_IF_TRUE,
	MONITORING_RULE_EDITING__MESSAGE_GROUP_TO_SEND_MESSAGES_FROM,
	MONITORING_RULE_EDITING__HOUR_TO_SEND_MESSAGE,
	MONITORING_RULE_EDITING__HOUR_TO_SEND_MESSAGE_VALUE,
	MONITORING_RULE_EDITING__EXECUTE_RULES_IF_ANSWER,
	MONITORING_RULE_EDITING__EXECUTE_RULES_IF_ANSWER_SHORT,
	MONITORING_RULE_EDITING__EXECUTE_RULES_IF_NO_ANSWER,
	MONITORING_RULE_EDITING__EXECUTE_RULES_IF_NO_ANSWER_SHORT,
	MONITORING_RULE_EDITING__MINUTES_AFTER_SENDING_UNTIL_HANDLED_AS_NOT_ANSWERED,
	MONITORING_RULE_EDITING__TIMEFRAME_AFTER_SENDING_UNTIL_HANDLED_AS_NOT_ANSWERED_VALUE,
	MONITORING_RULE_EDITING__STOP_RULE_EXECUTION_AND_FINISH_INTERVENTION_IF_TRUE,
	MONITORING_RULE_EDITING__START_MICRO_DIALOG_IF_TRUE,
	MONITORING_RULE_EDITING__MICRO_DIALOG_TO_START,
	// Screening survey editing (also for Feedbacks)
	SCREENING_SURVEY_EDITING__SWITCH_BUTTON_ACTIVE,
	SCREENING_SURVEY_EDITING__SWITCH_BUTTON_INACTIVE,
	SCREENING_SURVEY_EDITING__PASSWORD_TO_PARTICIPATE,
	SCREENING_SURVEY_EDITING__TEMPLATE_PATH,
	SCREENING_SURVEY_EDITING__SCREENING_SURVEY_SLIDES,
	SCREENING_SURVEY_EDITING__FEEDBACKS,
	SCREENING_SURVEY_EDITING__FEEDBACK_SLIDES,
	// Screening survey slide editing (also for Feedbacks)
	SCREENING_SURVEY_SLIDE_EDITING__EDIT_ANSWER,
	SCREENING_SURVEY_SLIDE_EDITING__EDIT_VALUE,
	SCREENING_SURVEY_SLIDE_EDITING__TITLE_WITH_PLACEHOLDERS,
	SCREENING_SURVEY_SLIDE_EDITING__COMMENT,
	SCREENING_SURVEY_SLIDE_EDITING__QUESTION_TYPE,
	SCREENING_SURVEY_SLIDE_EDITING__OPTIONAL_LAYOUT_ATTRIBUTE_WITH_PLACEHOLDERS,
	SCREENING_SURVEY_SLIDE_EDITING__QUESTION_TEXT,
	SCREENING_SURVEY_SLIDE_EDITING__PRESELECTED_ANSWER,
	SCREENING_SURVEY_SLIDE_EDITING__STORE_RESULT_TO_VARIABLE,
	SCREENING_SURVEY_SLIDE_EDITING__INTEGRATED_MEDIA_OBJECT,
	SCREENING_SURVEY_SLIDE_EDITING__LINK_INTERMEDIATE_SURVEY,
	SCREENING_SURVEY_SLIDE_EDITING__FINISH_OR_HAND_OVER_TO_FEEDBACK,
	SCREENING_SURVEY_SLIDE_EDITING__PROVIDE_EXTERNAL_ID_ACCESS_DATA,
	SCREENING_SURVEY_SLIDE_EDITING__STOP_SCREENING_SURVEY_AFTER_THIS_SLIDE,
	SCREENING_SURVEY_SLIDE_EDITING__FEEDBACK_TEXT,
	SCREENING_SURVEY_SLIDE_EDITING__FEEDBACK_INFORMATION_TEXT,
	SCREENING_SURVEY_SLIDE_EDITING__DEFAULT_VARIABLE_VALUE,
	SCREENING_SURVEY_SLIDE_EDITING__VALIDATION_ERROR_MESSAGE,
	// Screening survey slide rule editing (also for Feedbacks)
	SCREENING_SURVEY_SLIDE_RULE_EDITING__JUMP_TO_SLIDE_IF_TRUE,
	SCREENING_SURVEY_SLIDE_RULE_EDITING__JUMP_TO_SLIDE_IF_FALSE,
	SCREENING_SURVEY_SLIDE_RULE_EDITING__STORE_VALUE_TO_VARIABLE,
	SCREENING_SURVEY_SLIDE_RULE_EDITING__VALUE_TO_VARIABLE,
	SCREENING_SURVEY_SLIDE_RULE_EDITING__INVALID_WHEN_TRUE,
	// Intervention participants editing
	INTERVENTION_PARTICIPANTS_EDITING__ASSIGN_GROUP,
	INTERVENTION_PARTICIPANTS_EDITING__ASSIGN_ORGANIZATION,
	INTERVENTION_PARTICIPANTS_EDITING__ASSIGN_UNIT,
	INTERVENTION_PARTICIPANTS_EDITING__SWITCH_MONITORING,
	INTERVENTION_PARTICIPANTS_EDITING__SEND_MESSAGE,
	// Intervention basic settings tab
	INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_INTERVENTION_BUTTON_ACTIVE,
	INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_INTERVENTION_BUTTON_INACTIVE,
	INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_MONITORING_BUTTON_ACTIVE,
	INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_MONITORING_BUTTON_INACTIVE,
	INTERVENTION_BASIC_SETTINGS_TAB__SENDER_IDENTIFICATION_SELECTION_LABEL,
	INTERVENTION_BASIC_SETTINGS_TAB__AUTOMATICALLY_FINISH_SCREENING_SURVEYS_CHECKBOX,
	INTERVENTION_BASIC_SETTINGS_TAB__DASHBOARD_ENABLED_CHECKBOX,
	INTERVENTION_BASIC_SETTINGS_TAB__DASHBOARD_TEMPLATE_PATH_LABEL,
	INTERVENTION_BASIC_SETTINGS_TAB__DASHBOARD_PASSWORD_EXPRESSION_LABEL,
	INTERVENTION_BASIC_SETTINGS_TAB__DEEPSTREAM_PASSWORD_LABEL,
	INTERVENTION_BASIC_SETTINGS_TAB__MONITORING_STARTING_DAYS_LABEL,
	INTERVENTION_BASIC_SETTINGS_TAB__MONDAY_CHECKBOX,
	INTERVENTION_BASIC_SETTINGS_TAB__TUESDAY_CHECKBOX,
	INTERVENTION_BASIC_SETTINGS_TAB__WEDNESDAY_CHECKBOX,
	INTERVENTION_BASIC_SETTINGS_TAB__THURSDAY_CHECKBOX,
	INTERVENTION_BASIC_SETTINGS_TAB__FRIDAY_CHECKBOX,
	INTERVENTION_BASIC_SETTINGS_TAB__SATURDAY_CHECKBOX,
	INTERVENTION_BASIC_SETTINGS_TAB__SUNDAY_CHECKBOX,
	INTERVENTION_BASIC_SETTINGS_TAB__UNIQUENESS_LABEL,
	// Account tab
	ACCOUNT_TAB__SET_PASSWORD,
	ACCOUNT_TAB__RESET_ALL_LOCKS,
	ACCOUNT_TAB__ACCOUNT_INFORMATION,
	// Simulator component
	SIMULATOR_COMPONENT__JUMP_TEN_MINUTES_TO_THE_FUTURE,
	SIMULATOR_COMPONENT__JUMP_ONE_HOUR_TO_THE_FUTURE,
	SIMULATOR_COMPONENT__JUMP_ONE_DAY_TO_THE_FUTURE,
	SIMULATOR_COMPONENT__THE_CURRENT_SIMULATED_TIME_IS_X,
	SIMULATOR_COMPONENT__SIMULATOR,
	SIMULATOR_COMPONENT__SYSTEM,
	SIMULATOR_COMPONENT__PARTICIPANT,
	SIMULATOR_COMPONENT__ACTIVATE_FAST_FORWARD_MODE,
	SIMULATOR_COMPONENT__DEACTIVATE_FAST_FORWARD_MODE,
	/*
	 * VIEWS
	 */
	// Login view
	LOGIN_VIEW__USERNAME_FIELD,
	LOGIN_VIEW__PASSWORD_FIELD,
	LOGIN_VIEW__LOGIN_BUTTON,
	LOGIN_VIEW__ABOUT_LINK,
	// Error view
	ERROR_VIEW__ERROR_MESSAGE,
	// Main view
	MAIN_VIEW__WELCOME_BUTTON,
	MAIN_VIEW__INTERVENTIONS_BUTTON,
	MAIN_VIEW__ACCESS_CONTROL_BUTTON,
	MAIN_VIEW__ACCOUNT_BUTTON,
	MAIN_VIEW__LOGOUT_BUTTON,
	// Main view tabs
	MAIN_VIEW__WELCOME_TAB,
	MAIN_VIEW__INTERVENTIONS_TAB,
	MAIN_VIEW__ACCESS_CONTROL_TAB,
	MAIN_VIEW__ACCOUNT_TAB,
	/*
	 * WINDOWS
	 */
	// About window
	ABOUT_WINDOW__TITLE,
	ABOUT_WINDOW__HTML_TEXT,
	// Placeholder string editor
	PLACEHOLDER_STRING_EDITOR__SELECT_VARIABLE,
	// Short placeholder string editor
	SHORT_PLACEHOLDER_STRING_EDITOR__OPTIONAL_SELECT_VARIABLE,
	// Abstract string editor window titles
	ABSTRACT_STRING_EDITOR_WINDOW__SET_PASSWORD,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_USERNAME_FOR_NEW_USER,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_INTERVENTION,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_INTERVENTION,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_VARIABLE,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_VARIABLE,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_VALUE_FOR_VARIABLE,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_EXTERNAL_SERVICE,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_EXTERNAL_SERVICE,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_AND_SELECT_VARIABLE_FOR_SERVICE_MAPPING,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_AND_SELECT_VARIABLE_FOR_SERVICE_MAPPING,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_GROUP_PATTERN,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_MONITORING_MESSAGE_GROUP,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_MICRO_DIALOG,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_MONITORING_MESSAGE_GROUP,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_MICRO_DIALOG,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VARIABLE,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_MESSAGE_KEY,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TEXT_WITH_PLACEHOLDERS,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_SCREENING_SURVEY,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_SCREENING_SURVEY,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_FEEDBACK,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_FEEDBACK,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_PASSWORD,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_PASSWORD_PATTERN,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TITLE_WITH_PLACEHOLDERS,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_QUESTION_TEXT_WITH_PLACEHOLDERS,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_ANSWER_WITH_PLACEHOLDERS,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_OPTIONAL_LAYOUT_ATTRIBUTE_WITH_PLACEHOLDERS,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_ANSWER_VALUE,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_FEEDBACK_TEXT_WITH_PLACEHOLDERS,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_GROUP_OF_PARTICIPANTS,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_ORGANIZATION_OF_PARTICIPANTS,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_ORGANIZATION_UNIT_OF_PARTICIPANTS,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_COMMENT,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_RULE_WITH_PLACEHOLDERS,
	ABSTRACT_STRING_EDITOR_WINDOW__SEND_MESSAGE_TO_ALL_SELECTED_PARTICIPANTS,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VALUE,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VALIDATION_EXPRESSION,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_CLEANED_ANSWER,
	ABSTRACT_STRING_EDITOR_WINDOW__SEND_MESSAGE_TO_SELECTED_PARTICIPANT,
	ABSTRACT_STRING_EDITOR_WINDOW__SET_RESULT_VARIABLE_FOR_SELECTED_PARTICIPANTS,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_DEFAULT_VARIABLE_VALUE,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VALIDATION_ERROR_MESSAGE,
	ABSTRACT_STRING_EDITOR_WINDOW__ENTER_EXTERNAL_URL,
	ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VALUE_ON_NO_REPLY,
	// Abstract closable editor window titles
	ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_MONITORING_MESSAGE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_MONITORING_MESSAGE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_SCREENING_SURVEY_SLIDE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_SCREENING_SURVEY_SLIDE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_FEEDBACK,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_FEEDBACK,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_FEEDBACK_SLIDE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_FEEDBACK_SLIDE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_SCREENING_SURVEY,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_SCREENING_SURVEY_SLIDE_RULE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_SCREENING_SURVEY_SLIDE_RULE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_FEEDBACK_SLIDE_RULE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_FEEDBACK_SLIDE_RULE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_MONITORING_MESSAGE_RULE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_MONITORING_MESSAGE_RULE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__IMPORT_INTERVENTION,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__IMPORT_SCREENING_SURVEY,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__IMPORT_PARTICIPANTS,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_MICRO_DIALOG_MESSAGE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_MICRO_DIALOG_DECISION_POINT,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_MICRO_DIALOG_MESSAGE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_MICRO_DIALOG_DECISION_POINT,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_RULE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_RULE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_MICRO_DIALOG_MESSAGE_RULE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_MICRO_DIALOG_MESSAGE_RULE,
	ABSTRACT_CLOSABLE_EDIT_WINDOW__IMPORT_I18N,
	// Confirmation window title
	CONFIRMATION_WINDOW__TITLE,
	// Results
	RESULTS__VARIABLES_LABEL,
	RESULTS__MESSAGE_DIALOG_LABEL,
	RESULTS__TITLE,
	RESULTS__SEND_MESSAGE,
	RESULTS__EXPORT_ALL_DATA,
	// Problems
	PROBLEMS__DIALOG_MESSAGES_LABEL,
	PROBLEMS__SEND_MESSAGE,
	PROBLEMS__TITLE,
	PROBLEMS__MESSAGE_DIALOG_LABEL,
	// I18n
	I18N__TITLE,
	I18N__REMARK,
	I18N__EXPORT_SURVEYS_AND_FEEDBACKS,
	I18N__EXPORT_MESSAGES_AND_DIALOGS,
	I18N__IMPORT_SURVEYS_AND_FEEDBACKS,
	I18N__IMPORT_MESSAGES_AND_DIALOGS,
	// External Service Field Variable Mapping
	MAPPINGS__TITLE,
	/*
	 * NOTIFICATIONS
	 */
	// Notifications
	NOTIFICATION__UNKNOWN_ERROR,
	NOTIFICATION__WRONG_LOGIN,
	NOTIFICATION__NO_VALID_USERNAME,
	NOTIFICATION__NO_VALID_PASSWORD,
	NOTIFICATION__DEFAULT_ADMIN_CANT_BE_DOWNGRADED,
	NOTIFICATION__CANT_DELETE_YOURSELF,
	NOTIFICATION__DEFAULT_ADMIN_CANT_BE_DELETED,
	NOTIFICATION__CANT_DOWNGRADE_YOURSELF,
	NOTIFICATION__PASSWORD_CHANGED,
	NOTIFICATION__THE_GIVEN_PASSWORD_IS_NOT_SAFE,
	NOTIFICATION__ACCOUNT_CHANGED_TO_ADMIN,
	NOTIFICATION__ACCOUNT_CHANGED_TO_AUTHOR,
	NOTIFICATION__ACCOUNT_CHANGED_TO_TEAM_MANAGER,
	NOTIFICATION__ACCOUNT_DELETED,
	NOTIFICATION__ACCOUNT_CREATED,
	NOTIFICATION__THE_GIVEN_USERNAME_IS_TOO_SHORT,
	NOTIFICATION__THE_GIVEN_USERNAME_IS_ALREADY_IN_USE,
	NOTIFICATION__INTERVENTION_CREATED,
	NOTIFICATION__INTERVENTION_DELETED,
	NOTIFICATION__INTERVENTION_RENAMED,
	NOTIFICATION__ACCOUNT_ADDED_TO_INTERVENTION,
	NOTIFICATION__ACCOUNT_REMOVED_FROM_INTERVENTION,
	NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_ALREADY_IN_USE,
	NOTIFICATION__VARIABLE_RENAMED,
	NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID,
	NOTIFICATION__THE_GIVEN_NON_UNIQUE_KEY_IS_NOT_VALID,
	NOTIFICATION__VARIABLE_CREATED,
	NOTIFICATION__VARIABLE_DELETED,
	NOTIFICATION__VARIABLE_VALUE_CHANGED,
	NOTIFICATION__EXTERNAL_SERVICE_CREATED,
	NOTIFICATION__EXTERNAL_SERVICE_DELETED,
	NOTIFICATION__EXTERNAL_SERVICE_RENAMED,
	NOTIFICATION__EXTERNAL_SERVICE_TOKEN_RENEWED,
	NOTIFICATION__EXTERNAL_SERVICE_STATUS_CHANGED,
	NOTIFICATION__THE_GIVEN_EXTERNAL_SERVICE_NAME_IS_ALREADY_IN_USE,
	NOTIFICATION__EXTERNAL_SERVICE_MAPPING_CREATED,
	NOTIFICATION__EXTERNAL_SERVICE_MAPPING_UPDATED,
	NOTIFICATION__EXTERNAL_SERVICE_MAPPING_DELETED,
	NOTIFICATION__THE_GIVEN_EXTERNAL_SERVICE_MAPPING_FIELD_NAME_IS_ALREADY_IN_USE,
	NOTIFICATION__THE_GIVEN_VARIABLE_NAME_WAS_NOT_FOUND,
	NOTIFICATION__CAN_NOT_RENEW_EXTERNAL_SERVICE_TOKEN,
	NOTIFICATION__GROUP_PATTERN_CHANGED,
	NOTIFICATION__MONITORING_MESSAGE_GROUP_CREATED,
	NOTIFICATION__MONITORING_MESSAGE_GROUP_RENAMED,
	NOTIFICATION__MONITORING_MESSAGE_GROUP_DELETED,
	NOTIFICATION__MICRO_DIALOG_CREATED,
	NOTIFICATION__MICRO_DIALOG_RENAMED,
	NOTIFICATION__MICRO_DIALOG_DELETED,
	NOTIFICATION__MICRO_DIALOG_DUPLICATED,
	NOTIFICATION__MICRO_DIALOG_DUPLICATION_FAILED,
	NOTIFICATION__MONITORING_MESSAGE_CREATED,
	NOTIFICATION__MONITORING_MESSAGE_UPDATED,
	NOTIFICATION__MONITORING_MESSAGE_DUPLICATED,
	NOTIFICATION__MONITORING_MESSAGE_DUPLICATION_FAILED,
	NOTIFICATION__MONITORING_MESSAGE_DELETED,
	NOTIFICATION__UPLOAD_FAILED_OR_UNSUPPORTED_FILE_TYPE,
	NOTIFICATION__UPLOAD_SUCCESSFUL,
	NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM,
	NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES,
	NOTIFICATION__SCREENING_SURVEY_CREATED,
	NOTIFICATION__SCREENING_SURVEY_RENAMED,
	NOTIFICATION__SCREENING_SURVEY_UPDATED,
	NOTIFICATION__SCREENING_SURVEY_DELETED,
	NOTIFICATION__RULE_CREATED,
	NOTIFICATION__RULE_DUPLICATED,
	NOTIFICATION__RULE_DUPLICATION_FAILED,
	NOTIFICATION__RULE_UPDATED,
	NOTIFICATION__MONITORING_RULE_DELETED,
	NOTIFICATION__SCREENING_SURVEY_SLIDE_CREATED,
	NOTIFICATION__SCREENING_SURVEY_SLIDE_UPDATED,
	NOTIFICATION__SCREENING_SURVEY_SLIDE_DELETED,
	NOTIFICATION__FEEDBACK_CREATED,
	NOTIFICATION__FEEDBACK_RENAMED,
	NOTIFICATION__FEEDBACK_UPDATED,
	NOTIFICATION__FEEDBACK_DELETED,
	NOTIFICATION__SCREENING_SURVEY_SLIDE_RULE_CREATED,
	NOTIFICATION__SCREENING_SURVEY_SLIDE_RULE_UPDATED,
	NOTIFICATION__SCREENING_SURVEY_SLIDE_RULE_DELETED,
	NOTIFICATION__FEEDBACK_SLIDE_CREATED,
	NOTIFICATION__FEEDBACK_SLIDE_UPDATED,
	NOTIFICATION__FEEDBACK_SLIDE_DELETED,
	NOTIFICATION__FEEDBACK_SLIDE_RULE_CREATED,
	NOTIFICATION__FEEDBACK_SLIDE_RULE_UPDATED,
	NOTIFICATION__FEEDBACK_SLIDE_RULE_DELETED,
	NOTIFICATION__MONITORING_MESSAGE_RULE_CREATED,
	NOTIFICATION__MONITORING_MESSAGE_RULE_UPDATED,
	NOTIFICATION__MONITORING_MESSAGE_RULE_DELETED,
	NOTIFICATION__INTERVENTION_IMPORTED,
	NOTIFICATION__SCREENING_SURVEY_IMPORTED,
	NOTIFICATION__PARTICIPANTS_IMPORTED,
	NOTIFICATION__INTERVENTION_IMPORT_FAILED,
	NOTIFICATION__SCREENING_SURVEY_IMPORT_FAILED,
	NOTIFICATION__PARTICIPANTS_IMPORT_FAILED,
	NOTIFICATION__INTERVENTION_DUPLICATED,
	NOTIFICATION__SCREENING_SURVEY_DUPLICATED,
	NOTIFICATION__INTERVENTION_DUPLICATION_FAILED,
	NOTIFICATION__SCREENING_SURVEY_DUPLICATION_FAILED,
	NOTIFICATION__PARTICIPANTS_GROUP_CHANGED,
	NOTIFICATION__PARTICIPANTS_ORGANIZATION_CHANGED,
	NOTIFICATION__PARTICIPANTS_ORGANIZATION_UNIT_CHANGED,
	NOTIFICATION__PARTICIPANTS_MONITORING_SWITCHED,
	NOTIFICATION__PARTICIPANTS_DELETED,
	NOTIFICATION__THE_MESSAGES_WILL_BE_SENT_IN_THE_NEXT_MINUTES,
	NOTIFICATION__SLIDE_DUPLICATED,
	NOTIFICATION__SLIDE_DUPLICATION_FAILED,
	NOTIFICATION__VALIDATION_EXPRESSION_UPDATED,
	NOTIFICATION__CASE_SOLVED,
	NOTIFICATION__SYSTEM_RESERVED_VARIABLE,
	NOTIFICATION__CASE_CANT_BE_SOLVED_ANYMORE,
	NOTIFICATION__CANT_DELETE_LAST_QUESTION,
	NOTIFICATION__SCREENING_SURVEY_SLIDE_HAS_BACKLINKS,
	NOTIFICATION__INTERVENTION_LOCKED,
	NOTIFICATION__SCREENING_SURVEY_TYPE_CHANGED,
	NOTIFICATION__SCREENING_SURVEY_STATUS_CHANGED,
	NOTIFICATION__SCREENING_SURVEY_CANT_CHANGE_TYPE_1,
	NOTIFICATION__SCREENING_SURVEY_CANT_CHANGE_TYPE_2,
	NOTIFICATION__SCREENING_SURVEY_CANT_CHANGE_TYPE_3,
	NOTIFICATION__SCREENING_SURVEY_CANT_DELETE,
	NOTIFICATION__URL_SET,
	NOTIFICATION__GIVEN_URL_NOT_VALID,
	NOTIFICATION__SURVEY_PARTICIPATION_REQUIRED,
	NOTIFICATION__INTERVENTION_NOT_ACTIVE,
	NOTIFICATION__SURVEY_NOT_ACTIVE,
	NOTIFICATION__VARIABLE_SETTING_CHANGED,
	NOTIFICATION__TOO_MANY_PARTICIPANTS_SELECTED,
	NOTIFICATION__ALL_LOCKS_RESET,
	NOTIFICATION__FILE_CHANGES_SAVED,
	NOTIFICATION__MICRO_DIALOG_MESSAGE_CREATED,
	NOTIFICATION__MICRO_DIALOG_DECISION_POINT_CREATED,
	NOTIFICATION__MICRO_DIALOG_MESSAGE_DELETED,
	NOTIFICATION__MICRO_DIALOG_DECISION_POINT_DELETED,
	NOTIFICATION__MICRO_DIALOG_MESSAGE_RULE_CREATED,
	NOTIFICATION__MICRO_DIALOG_MESSAGE_RULE_UPDATED,
	NOTIFICATION__MICRO_DIALOG_MESSAGE_RULE_DELETED,
	NOTIFICATION__MICRO_DIALOG_MESSAGE_UPDATED,
	NOTIFICATION__MICRO_DIALOG_DECISION_POINT_UPDATED,
	NOTIFICATION__MICRO_DIALOG_MESSAGE_DUPLICATED,
	NOTIFICATION__MICRO_DIALOG_DECISION_POINT_DUPLICATED,
	NOTIFICATION__MICRO_DIALOG_MESSAGE_DUPLICATION_FAILED,
	NOTIFICATION__MICRO_DIALOG_DECISION_POINT_DUPLICATION_FAILD,
	NOTIFICATION__MICRO_DIALOG_MESSAGE_CANT_DELETE,
	NOTIFICATION__MONITORING_MESSAGE_GROUP_CANT_DELETE,
	NOTIFICATION__I18N_IMPORTED,
	NOTIFICATION__I18N_IMPORT_FAILED,
	// System notifications
	SYSTEM_NOTIFICATION__SESSION_EXPIRED_CAPTION,
	SYSTEM_NOTIFICATION__SESSION_EXPIRED_MESSAGE,
	SYSTEM_NOTIFICATION__INTERNAL_ERROR_CAPTION,
	SYSTEM_NOTIFICATION__INTERNAL_ERROR_MESSAGE,
	SYSTEM_NOTIFICATION__COMMUNICATION_ERROR_CAPTION,
	SYSTEM_NOTIFICATION__COMMUNICATION_ERROR_MESSAGE,
	SYSTEM_NOTIFICATION__COOKIES_DISABLED_CAPTION,
	SYSTEM_NOTIFICATION__COOKIES_DISABLED_MESSAGE,
	/*
	 * UI MODEL & UI COLUMNS
	 */
	// UI Model
	UI_MODEL__ADMIN,
	UI_MODEL__AUTHOR,
	UI_MODEL__TEAM_MANAGER,
	UI_MODEL__NO_RIGHTS,
	UI_MODEL__ACTIVE,
	UI_MODEL__INACTIVE,
	UI_MODEL__YES,
	UI_MODEL__NO,
	UI_MODEL__NOT_SET,
	UI_MODEL__UNKNOWN,
	UI_MODEL__FINISHED,
	UI_MODEL__NOT_FINISHED,
	UI_MODEL__EXPECTS_ANSWER,
	UI_MODEL__EXPECTS_NO_ANSWER,
	UI_MODEL__SURVEY__SCREENING,
	UI_MODEL__SURVEY__INTERMEDIATE,
	UI_MODEL__PARTICIPANT_MESSAGE,
	UI_MODEL__SUPERVISOR_MESSAGE,
	UI_MODEL__DAILY_RULE,
	UI_MODEL__PERIODIC_RULE,
	UI_MODEL__UNEXPECTED_MESSAGE_RULE,
	UI_MODEL__EXTERNAL_MESSAGE_RULE,
	UI_MODEL__USER_INTENTION_RULE,
	UI_MODEL__COMMAND_MESSAGE,
	UI_MODEL__USER_INTENTION_MESSAGE,
	UI_MODEL__PLAIN_TEXT_MESSAGE,
	UI_MODEL__MICRO_DIALOG_ACTIVATION_MESSAGE,
	UI_MODEL__MESSAGE,
	UI_MODEL__DECISION_POINT,
	UI_MODEL__VARIABLE_NOT_ACCESSIBLE,
	// UI Columns
	UI_COLUMNS__ACCOUNT,
	UI_COLUMNS__ACCOUNT_TYPE,
	UI_COLUMNS__GROUP_PATTERN,
	UI_COLUMNS__INTERVENTION,
	UI_COLUMNS__INTERVENTION_STATUS,
	UI_COLUMNS__MONITORING_STATUS,
	UI_COLUMNS__VARIABLE_NAME,
	UI_COLUMNS__VARIABLE_VALUE,
	UI_COLUMNS__PRIVACY_TYPE,
	UI_COLUMNS__ACCESS_TYPE,
	UI_COLUMNS__EXTERNAL_SERVICE_ID,
	UI_COLUMNS__EXTERNAL_SERVICE_NAME,
	UI_COLUMNS__EXTERNAL_SERVICE_TOKEN,
	UI_COLUMNS__EXTERNAL_SERVICE_STATUS,
	UI_COLUMNS__EXTERNAL_SERVICE_FIELD_VARIABLE_MAPPING_JSON_FIELD_NAME,
	UI_COLUMNS__EXTERNAL_SERVICE_FIELD_VARIABLE_MAPPING_VARIABLE_WITH_VALUE_NAME,
	UI_COLUMNS__MESSAGE_TEXT,
	UI_COLUMNS__MESSAGE_TEXT_OR_COMMENT,
	UI_COLUMNS__COMMAND_MESSAGE,
	UI_COLUMNS__CONTAINS_MEDIA_CONTENT,
	UI_COLUMNS__CONTAINS_LINK_TO_INTERMEDIATE_SURVEY,
	UI_COLUMNS__ANSWER_TYPE,
	UI_COLUMNS__RESULT_VARIABLE,
	UI_COLUMNS__SCREENING_SURVEY,
	UI_COLUMNS__SCREENING_SURVEY_TYPE,
	UI_COLUMNS__SCREENING_SURVEY_PASSWORD,
	UI_COLUMNS__SCREENING_SURVEY_STATUS,
	UI_COLUMNS__FEEDBACK,
	UI_COLUMNS__RULE,
	UI_COLUMNS__JUMP_TO_SLIDE_WHEN_TRUE,
	UI_COLUMNS__JUMP_TO_SLIDE_WHEN_FALSE,
	UI_COLUMNS__SLIDE_TITLE_WITH_PLACEHOLDERS,
	UI_COLUMNS__QUESTION_TYPE,
	UI_COLUMNS__ANSWER_WITH_PLACEHODLERS,
	UI_COLUMNS__VALUE,
	UI_COLUMNS__PARTICIPANT_NAME,
	UI_COLUMNS__LANGUAGE,
	UI_COLUMNS__GROUP,
	UI_COLUMNS__ORGANIZATION,
	UI_COLUMNS__ORGANIZATION_UNIT,
	UI_COLUMNS__CREATED,
	UI_COLUMNS__SCREENING_SURVEY_NAME,
	UI_COLUMNS__PARTICIPANT_SCREENING_SURVEY_STATUS,
	UI_COLUMNS__PARTICIPANT_INTERVENTION_STATUS,
	UI_COLUMNS__TIMESTAMP,
	UI_COLUMNS__MESSAGE_TYPE,
	UI_COLUMNS__PARTICIPANT_ID,
	UI_COLUMNS__ORDER,
	UI_COLUMNS__STATUS,
	UI_COLUMNS__SENDER_TYPE,
	UI_COLUMNS__TYPE,
	UI_COLUMNS__MESSAGE,
	UI_COLUMNS__SHOULD_BE_SENT_TIMESTAMP,
	UI_COLUMNS__SENT_TIMESTAMP,
	UI_COLUMNS__ANSWER,
	UI_COLUMNS__ANSWER_RECEIVED_TIMESTAMP,
	UI_COLUMNS__MANUALLY_SENT,
	UI_COLUMNS__MEDIA_CONTENT_VIEWED,
	UI_COLUMNS__VARIABLE,
	UI_COLUMNS__PARTICIPANT_DATA_FOR_MONITORING_AVAILABLE,
	UI_COLUMNS__CONTAINS_RULES,
	UI_COLUMNS__RAW_ANSWER,
	UI_COLUMNS__LAST_UPDATED,
	UI_COLUMNS__SHOW_SAME_SLIDE_IF_INVALID,
	UI_COLUMNS__ASSIGNED_SENDER_IDENTIFICATION,
	UI_COLUMNS__QUESTION,
	UI_COLUMNS__COMMENT,
	/*
	 * DEBUG
	 */
	DEBUG__PARTICIPANT_ORGANIZATION,
	DEBUG__PARTICIPANT_ORGANIZATION_UNIT,
	/*
	 * STATISTICS
	 */
	STATISTICS__CREATED,
	STATISTICS__SCREENING_SURVEY_STARTED,
	STATISTICS__SCREENING_SURVEY_PERFORMED,
	STATISTICS__MONITORING_STARTED,
	STATISTICS__MONITORING_PERFORMED,
	STATISTICS__TOTAL_MESSAGES_SENT,
	STATISTICS__TOTAL_MESSAGES_RECEIVED,
	STATISTICS__TOTAL_MESSAGES_DEACTIVATED,
	STATISTICS__ANSWERED_QUESTIONS,
	STATISTICS__UNANSWERED_QUESTIONS,
	STATISTICS__MEDIA_OBJECTS_CONTAINED_IN_MESSAGES,
	STATISTICS__MEDIA_OBJECTS_VIEWED,
	/*
	 * MODULES
	 */
	// General
	MODULES__LABEL,
	MODULES__OPEN_MODULE,
	MODULES__MODULE_NAME,
	// Quiz
	MODULES__MESSAGE_CONTEST__NAME,
	MODULES__MESSAGE_CONTEST__MESSAGES_AND_REPLIES,
	MODULES__MESSAGE_CONTEST__SET_RESULT_FOR_PARTICIPANTS;

}
