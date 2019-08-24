package ch.ethz.mc.model.ui;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.subelements.LString;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIAnswer extends UIObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ORDER	= "order";
	public static final String	ANSWER	= "answer";
	public static final String	VALUE	= "value";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(ANSWER)
	private LString				answer;

	@PropertyId(VALUE)
	private String				value;

	public static Object[] getVisibleColumns() {
		return new Object[] { ANSWER, VALUE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(
						AdminMessageStrings.UI_COLUMNS__ANSWER_WITH_PLACEHODLERS),
				localize(AdminMessageStrings.UI_COLUMNS__VALUE) };
	}

	public static String getSortColumn() {
		return ORDER;
	}

	@Override
	public String toString() {
		return answer.toString();
	}
}
