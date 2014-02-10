package org.isgf.mhc.model.ui;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.UIModelObject;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIAuthor extends UIModelObject {
	public static final String	USERNAME	= "username";
	public static final String	TYPE		= "type";

	@NotNull
	@Size(min = 3, max = 50)
	@PropertyId(USERNAME)
	private String				username;

	@PropertyId(TYPE)
	private String				type;

	public static Object[] getVisibleColumns() {
		return new Object[] { USERNAME, TYPE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__ACCOUNT),
				localize(AdminMessageStrings.UI_COLUMNS__ACCOUNT_TYPE) };
	}
}
