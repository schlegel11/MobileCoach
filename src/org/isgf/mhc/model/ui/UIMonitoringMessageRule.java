package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIMonitoringMessageRule extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ORDER	= "order";
	public static final String	RULE	= "rule";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(RULE)
	private String				rule;

	public static Object[] getVisibleColumns() {
		return new Object[] { RULE };
	}

	public static String[] getColumnHeaders() {
		return new String[] { localize(AdminMessageStrings.UI_COLUMNS__RULE) };
	}

	public static String getSortColumn() {
		return ORDER;
	}
}
