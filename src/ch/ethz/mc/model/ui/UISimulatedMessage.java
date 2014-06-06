package ch.ethz.mc.model.ui;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UISimulatedMessage extends UIObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	TIMESTAMP	= "timestamp";
	public static final String	TYPE		= "type";
	public static final String	MESSAGE		= "message";

	@PropertyId(TIMESTAMP)
	private Date				timestamp;

	@PropertyId(TYPE)
	private String				type;

	@PropertyId(MESSAGE)
	private String				message;

	public static Object[] getVisibleColumns() {
		return new Object[] { TIMESTAMP, TYPE, MESSAGE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__TIMESTAMP),
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGE_TYPE),
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGE_TEXT) };
	}

	public static String getSortColumn() {
		return TIMESTAMP;
	}

	@Override
	public String toString() {
		return message;
	}
}
