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
	public static final String	MONITORING_STATUS	= "monitoringStatus";

	@PropertyId(INTERVENTION_NAME)
	private String				interventionName;

	private boolean				booleanInterventionStatus;

	@PropertyId(INTERVENTION_STATUS)
	private String				interventionStatus;

	private boolean				booleanMessagingStatus;

	@PropertyId(MONITORING_STATUS)
	private String				monitoringStatus;

	public static Object[] getVisibleColumns() {
		return new Object[] { INTERVENTION_NAME, INTERVENTION_STATUS,
				MONITORING_STATUS };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__INTERVENTION),
				localize(AdminMessageStrings.UI_COLUMNS__INTERVENTION_STATUS),
				localize(AdminMessageStrings.UI_COLUMNS__MONITORING_STATUS) };
	}

	public static String getSortColumn() {
		return INTERVENTION_NAME;
	}
}
