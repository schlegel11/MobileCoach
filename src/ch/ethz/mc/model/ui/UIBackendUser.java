package ch.ethz.mc.model.ui;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.BackendUser;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIBackendUser extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	USERNAME	= "username";
	public static final String	TYPE		= "type";

	@PropertyId(USERNAME)
	private String				username;

	@PropertyId(TYPE)
	private String				type;

	public static Object[] getVisibleColumns() {
		return new Object[] { USERNAME, TYPE };
	}

	public static String[] getColumnHeaders() {
		return new String[] { localize(AdminMessageStrings.UI_COLUMNS__ACCOUNT),
				localize(AdminMessageStrings.UI_COLUMNS__ACCOUNT_TYPE) };
	}

	public static String getSortColumn() {
		return USERNAME;
	}

	@Override
	public String toString() {
		return getRelatedModelObject(BackendUser.class).getUsername();
	}
}
