package ch.ethz.mc.model.ui;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIScreeningSurveySlide extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ORDER					= "order";
	public static final String	TITLE_WITH_PLACEHOLDERS	= "titleWithPlaceholders";
	public static final String	COMMENT					= "comment";
	public static final String	QUESTION_TYPE			= "questionType";
	public static final String	RESULT_VARIABLE			= "resultVariable";
	public static final String	CONTAINS_RULES			= "containsRules";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(TITLE_WITH_PLACEHOLDERS)
	private String				titleWithPlaceholders;

	@PropertyId(COMMENT)
	private String				comment;

	@PropertyId(QUESTION_TYPE)
	private String				questionType;

	@PropertyId(RESULT_VARIABLE)
	private String				resultVariable;

	@PropertyId(CONTAINS_RULES)
	private int					containsRules;

	public static Object[] getVisibleColumns() {
		return new Object[] { TITLE_WITH_PLACEHOLDERS, COMMENT, QUESTION_TYPE,
				RESULT_VARIABLE, CONTAINS_RULES };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(
						AdminMessageStrings.UI_COLUMNS__SLIDE_TITLE_WITH_PLACEHOLDERS),
				localize(AdminMessageStrings.UI_COLUMNS__COMMENT),
				localize(AdminMessageStrings.UI_COLUMNS__QUESTION_TYPE),
				localize(AdminMessageStrings.UI_COLUMNS__RESULT_VARIABLE),
				localize(AdminMessageStrings.UI_COLUMNS__CONTAINS_RULES) };
	}

	public static String getSortColumn() {
		return ORDER;
	}

	@Override
	public String toString() {
		if (!comment.equals(Messages
				.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET))) {
			return titleWithPlaceholders + " (" + comment + ")";
		} else {
			return titleWithPlaceholders;
		}
	}
}
