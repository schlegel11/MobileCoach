package ch.ethz.mc.model.ui;

/* ##LICENSE## */
import com.vaadin.data.fieldgroup.PropertyId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.MicroDialogMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIMicroDialogElementInterface extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ORDER										= "order";
	public static final String	TYPE										= "type";
	public static final String	MESSAGE_TEXT_WITH_PLACEHOLDERS_OR_COMMENT	= "messageTextWithPlaceholdersOrComment";
	public static final String	IS_COMMAND_MESSAGE							= "isCommandMessage";
	public static final String	RANDOMIZATION_GROUP							= "randomizationGroup";
	public static final String	CONTAINS_MEDIA_CONTENT						= "containsMediaContent";
	public static final String	CONTAINS_LINK_TO_INTERMEDIATE_SURVEY		= "containsLinkToIntermediateSurvey";
	public static final String	ANSWER_TYPE									= "answerType";
	public static final String	RESULT_VARIABLE								= "resultVariable";
	public static final String	CONTAINS_RULES								= "containsRules";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(TYPE)
	private String				type;

	@Getter
	@Setter
	private boolean				isMessage									= false;

	@Getter
	@Setter
	private boolean				messageDeactivatesAllOpenQuestions			= false;

	@PropertyId(MESSAGE_TEXT_WITH_PLACEHOLDERS_OR_COMMENT)
	private String				messageTextWithPlaceholdersOrComment;

	@PropertyId(IS_COMMAND_MESSAGE)
	private String				isCommandMessage;

	@PropertyId(RANDOMIZATION_GROUP)
	private String				randomizationGroup;

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
		return new Object[] { TYPE, MESSAGE_TEXT_WITH_PLACEHOLDERS_OR_COMMENT,
				ANSWER_TYPE, RESULT_VARIABLE, RANDOMIZATION_GROUP,
				IS_COMMAND_MESSAGE, CONTAINS_MEDIA_CONTENT,
				CONTAINS_LINK_TO_INTERMEDIATE_SURVEY, CONTAINS_RULES };
	}

	public static String[] getColumnHeaders() {
		return new String[] { localize(AdminMessageStrings.UI_COLUMNS__TYPE),
				localize(
						AdminMessageStrings.UI_COLUMNS__MESSAGE_TEXT_OR_COMMENT),
				localize(AdminMessageStrings.UI_COLUMNS__ANSWER_TYPE),
				localize(AdminMessageStrings.UI_COLUMNS__RESULT_VARIABLE),
				localize(AdminMessageStrings.UI_COLUMNS__RANDOMIZATION_GROUP),
				localize(AdminMessageStrings.UI_COLUMNS__COMMAND_MESSAGE),
				localize(
						AdminMessageStrings.UI_COLUMNS__CONTAINS_MEDIA_CONTENT),
				localize(
						AdminMessageStrings.UI_COLUMNS__CONTAINS_LINK_TO_INTERMEDIATE_SURVEY),
				localize(AdminMessageStrings.UI_COLUMNS__CONTAINS_RULES) };
	}

	public static String getSortColumn() {
		return ORDER;
	}

	@Override
	public String toString() {
		if (isMessage) {
			val microDialogMessage = getRelatedModelObject(
					MicroDialogMessage.class);

			if (microDialogMessage.getTextWithPlaceholders().isEmpty()) {
				return Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET);
			} else {
				return microDialogMessage.getTextWithPlaceholders()
						.toShortenedString(160);
			}
		} else {
			return this.getClass().getName();
		}
	}
}
