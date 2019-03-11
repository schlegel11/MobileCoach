package ch.ethz.mc.model.ui;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIInterventionVariable extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	NAME			= "name";
	public static final String	VALUE			= "value";
	public static final String	PRIVACY_TYPE	= "privacyType";
	public static final String	ACCESS_TYPE		= "accessType";

	@PropertyId(NAME)
	private String				name;

	@PropertyId(VALUE)
	private String				value;

	@PropertyId(PRIVACY_TYPE)
	private String				privacyType;

	@PropertyId(ACCESS_TYPE)
	private String				accessType;

	public static Object[] getVisibleColumns() {
		return new Object[] { NAME, VALUE, PRIVACY_TYPE, ACCESS_TYPE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_VALUE),
				localize(AdminMessageStrings.UI_COLUMNS__PRIVACY_TYPE),
				localize(AdminMessageStrings.UI_COLUMNS__ACCESS_TYPE) };
	}

	public static String getSortColumn() {
		return NAME;
	}

	@Override
	public String toString() {
		return getRelatedModelObject(InterventionVariableWithValue.class)
				.getName();
	}
}
