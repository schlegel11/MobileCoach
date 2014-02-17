package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIIntervention extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	INTERVENTION_NAME	= "interventionName";
	public static final String	INTERVENTION_STATUS	= "interventionStatus";
	public static final String	MESSAGING_STATUS	= "messagingStatus";

	@PropertyId(INTERVENTION_NAME)
	private String				interventionName;

	private boolean				booleanInterventionStatus;

	@PropertyId(INTERVENTION_STATUS)
	private String				interventionStatus;

	private boolean				booleanMessagingStatus;

	@PropertyId(MESSAGING_STATUS)
	private String				messagingStatus;

	public static Object[] getVisibleColumns() {
		return new Object[] { INTERVENTION_NAME, INTERVENTION_STATUS,
				MESSAGING_STATUS };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__INTERVENTION),
				localize(AdminMessageStrings.UI_COLUMNS__INTERVENTION_STATUS),
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGING_STATUS) };
	}

	public static String getSortColumn() {
		return INTERVENTION_NAME;
	}
}
