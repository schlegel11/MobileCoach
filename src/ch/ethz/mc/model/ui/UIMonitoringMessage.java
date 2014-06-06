package ch.ethz.mc.model.ui;

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
	public static final String	ORDER					= "order";
	public static final String	TEXT_WITH_PLACEHOLDERS	= "textWithPlaceholders";
	public static final String	HAS_LINKED_MEDIA_OBJECT	= "hasLinkedMediaObject";
	public static final String	RESULT_VARIABLE			= "resultVariable";
	public static final String	CONTAINS_RULES			= "containsRules";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(TEXT_WITH_PLACEHOLDERS)
	private String				textWithPlaceholders;

	private boolean				booleanHasLinkedMediaObject;

	@PropertyId(HAS_LINKED_MEDIA_OBJECT)
	private String				hasLinkedMediaObject;

	@PropertyId(RESULT_VARIABLE)
	private String				resultVariable;

	@PropertyId(CONTAINS_RULES)
	private int					containsRules;

	public static Object[] getVisibleColumns() {
		return new Object[] { TEXT_WITH_PLACEHOLDERS, HAS_LINKED_MEDIA_OBJECT,
				RESULT_VARIABLE, CONTAINS_RULES };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGE_TEXT),
				localize(AdminMessageStrings.UI_COLUMNS__HAS_LINKED_MEDIA_OBJECT),
				localize(AdminMessageStrings.UI_COLUMNS__RESULT_VARIABLE),
				localize(AdminMessageStrings.UI_COLUMNS__CONTAINS_RULES) };
	}

	public static String getSortColumn() {
		return ORDER;
	}
}
