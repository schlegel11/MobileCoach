package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIFeedbackSlide extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ORDER					= "order";
	public static final String	TITLE_WITH_PLACEHOLDERS	= "titleWithPlaceholders";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(TITLE_WITH_PLACEHOLDERS)
	private String				titleWithPlaceholders;

	public static Object[] getVisibleColumns() {
		return new Object[] { TITLE_WITH_PLACEHOLDERS };
	}

	public static String[] getColumnHeaders() {
		return new String[] { localize(AdminMessageStrings.UI_COLUMNS__SLIDE_TITLE_WITH_PLACEHOLDERS) };
	}

	public static String getSortColumn() {
		return ORDER;
	}
}
