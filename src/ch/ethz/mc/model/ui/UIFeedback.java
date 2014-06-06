package ch.ethz.mc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;

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

	@Override
	public String toString() {
		return feedbackName;
	}
}
