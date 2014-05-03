package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIMonitoringMessageGroup extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	NAME			= "name";

	public static final String	EXPECTS_ANSWER	= "expectsAnswer";

	@PropertyId(NAME)
	private String				name;

	@PropertyId(EXPECTS_ANSWER)
	private boolean				expectsAnswer;

	@Override
	public String toString() {
		return name
				+ " ("
				+ (expectsAnswer ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__EXPECTS_ANSWER)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__EXPECTS_NO_ANSWER))
				+ ")";
	}
}
