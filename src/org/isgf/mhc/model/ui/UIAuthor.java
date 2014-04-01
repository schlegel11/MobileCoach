package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.persistent.Author;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIAuthor extends UIModelObject {
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
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__ACCOUNT),
				localize(AdminMessageStrings.UI_COLUMNS__ACCOUNT_TYPE) };
	}

	public static String getSortColumn() {
		return USERNAME;
	}

	@Override
	public String toString() {
		return getRelatedModelObject(Author.class).getUsername();
	}
}
