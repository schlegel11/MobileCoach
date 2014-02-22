package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIMonitoringMessage extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	TEXT_WITH_PLACEHOLDERS	= "textWithPlaceholders";
	public static final String	HAS_LINKED_MEDIA_OBJECT	= "hasLinkedMediaObject";
	public static final String	RESULT_VARIABLE			= "resultVariable";

	@PropertyId(TEXT_WITH_PLACEHOLDERS)
	private String				textWithPlaceholders;

	private boolean				booleanHasLinkedMediaObject;

	@PropertyId(HAS_LINKED_MEDIA_OBJECT)
	private String				hasLinkedMediaObject;

	@PropertyId(RESULT_VARIABLE)
	private String				resultVariable;

	public static Object[] getVisibleColumns() {
		return new Object[] { TEXT_WITH_PLACEHOLDERS, HAS_LINKED_MEDIA_OBJECT,
				RESULT_VARIABLE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGE_TEXT),
				localize(AdminMessageStrings.UI_COLUMNS__HAS_LINKED_MEDIA_OBJECT),
				localize(AdminMessageStrings.UI_COLUMNS__RESULT_VARIABLE) };
	}

	public static String getSortColumn() {
		return TEXT_WITH_PLACEHOLDERS;
	}
}
