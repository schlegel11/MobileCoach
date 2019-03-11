package ch.ethz.mc.model.ui;

/* ##LICENSE## */
import com.vaadin.data.fieldgroup.PropertyId;

import ch.ethz.mc.conf.AdminMessageStrings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIBackendUserInterventionAccess extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	USERNAME		= "username";
	public static final String	TYPE			= "type";
	public static final String	GROUP_PATTERN	= "groupPattern";

	@PropertyId(USERNAME)
	private String				username;

	@PropertyId(TYPE)
	private String				type;

	@PropertyId(GROUP_PATTERN)
	private String				groupPattern;

	public static Object[] getVisibleColumns() {
		return new Object[] { USERNAME, TYPE, GROUP_PATTERN };
	}

	public static String[] getColumnHeaders() {
		return new String[] { localize(AdminMessageStrings.UI_COLUMNS__ACCOUNT),
				localize(AdminMessageStrings.UI_COLUMNS__ACCOUNT_TYPE),
				localize(AdminMessageStrings.UI_COLUMNS__GROUP_PATTERN) };
	}

	public static String getSortColumn() {
		return USERNAME;
	}
}
