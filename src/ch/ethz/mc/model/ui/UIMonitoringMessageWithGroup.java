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
public class UIMonitoringMessageWithGroup extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	TEXT_WITH_PLACEHOLDERS	= "textWithPlaceholders";

	@PropertyId(TEXT_WITH_PLACEHOLDERS)
	private String				textWithPlaceholders;

	public static Object[] getVisibleColumns() {
		return new Object[] { TEXT_WITH_PLACEHOLDERS };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGE_TEXT) };
	}

	public static String getSortColumn() {
		return TEXT_WITH_PLACEHOLDERS;
	}

	@Override
	public String toString() {
		return textWithPlaceholders;
	}
}
