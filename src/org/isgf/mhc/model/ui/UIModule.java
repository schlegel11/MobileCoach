package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.modules.AbstractModule;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIModule extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String				NAME	= "name";

	@PropertyId(NAME)
	private String							name;

	private Class<? extends AbstractModule>	moduleClass;

	public static Object[] getVisibleColumns() {
		return new Object[] { NAME };
	}

	public static String[] getColumnHeaders() {
		return new String[] { localize(AdminMessageStrings.MODULES__MODULE_NAME) };
	}

	public static String getSortColumn() {
		return NAME;
	}

	@Override
	public String toString() {
		return name;
	}
}
