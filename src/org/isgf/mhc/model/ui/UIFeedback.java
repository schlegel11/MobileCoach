package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIFeedback extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	FEEDBACK_NAME	= "feedbackName";

	@PropertyId(FEEDBACK_NAME)
	private String				feedbackName;

	public static Object[] getVisibleColumns() {
		return new Object[] { FEEDBACK_NAME };
	}

	public static String[] getColumnHeaders() {
		return new String[] { localize(AdminMessageStrings.UI_COLUMNS__FEEDBACK) };
	}

	public static String getSortColumn() {
		return FEEDBACK_NAME;
	}
}
