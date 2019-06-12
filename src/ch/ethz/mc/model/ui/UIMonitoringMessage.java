package ch.ethz.mc.model.ui;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIMonitoringMessage extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ORDER									= "order";
	public static final String	TEXT_WITH_PLACEHOLDERS					= "textWithPlaceholders";
	public static final String	IS_COMMAND_MESSAGE						= "isCommandMessage";
	public static final String	CONTAINS_MEDIA_CONTENT					= "containsMediaContent";
	public static final String	CONTAINS_LINK_TO_INTERMEDIATE_SURVEY	= "containsLinkToIntermediateSurvey";
	public static final String	ANSWER_TYPE								= "answerType";
	public static final String	RESULT_VARIABLE							= "resultVariable";
	public static final String	CONTAINS_RULES							= "containsRules";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(TEXT_WITH_PLACEHOLDERS)
	private String				textWithPlaceholders;

	@PropertyId(IS_COMMAND_MESSAGE)
	private String				isCommandMessage;

	@PropertyId(CONTAINS_MEDIA_CONTENT)
	private String				containsMediaContent;

	@PropertyId(CONTAINS_LINK_TO_INTERMEDIATE_SURVEY)
	private String				containsLinkToIntermediateSurvey;

	@PropertyId(ANSWER_TYPE)
	private String				answerType;

	@PropertyId(RESULT_VARIABLE)
	private String				resultVariable;

	@PropertyId(CONTAINS_RULES)
	private int					containsRules;

	public static Object[] getVisibleColumns() {
		return new Object[] { TEXT_WITH_PLACEHOLDERS, IS_COMMAND_MESSAGE,
				CONTAINS_MEDIA_CONTENT, CONTAINS_LINK_TO_INTERMEDIATE_SURVEY,
				ANSWER_TYPE, RESULT_VARIABLE, CONTAINS_RULES };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGE_TEXT),
				localize(AdminMessageStrings.UI_COLUMNS__COMMAND_MESSAGE),
				localize(
						AdminMessageStrings.UI_COLUMNS__CONTAINS_MEDIA_CONTENT),
				localize(
						AdminMessageStrings.UI_COLUMNS__CONTAINS_LINK_TO_INTERMEDIATE_SURVEY),
				localize(AdminMessageStrings.UI_COLUMNS__ANSWER_TYPE),
				localize(AdminMessageStrings.UI_COLUMNS__RESULT_VARIABLE),
				localize(AdminMessageStrings.UI_COLUMNS__CONTAINS_RULES) };
	}

	public static String getSortColumn() {
		return ORDER;
	}
}
