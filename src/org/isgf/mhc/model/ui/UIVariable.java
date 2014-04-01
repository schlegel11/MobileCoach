package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.persistent.InterventionVariableWithValue;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIVariable extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	NAME	= "name";
	public static final String	VALUE	= "value";

	@PropertyId(NAME)
	private String				name;

	@PropertyId(VALUE)
	private String				value;

	public static Object[] getVisibleColumns() {
		return new Object[] { NAME, VALUE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_VALUE) };
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
