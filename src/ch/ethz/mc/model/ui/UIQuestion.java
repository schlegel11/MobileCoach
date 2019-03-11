package ch.ethz.mc.model.ui;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIQuestion extends UIObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ORDER		= "order";
	public static final String	QUESTION	= "question";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(QUESTION)
	private String				question;

	public static Object[] getVisibleColumns() {
		return new Object[] { QUESTION };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__QUESTION) };
	}

	public static String getSortColumn() {
		return ORDER;
	}

	@Override
	public String toString() {
		return question;
	}
}
