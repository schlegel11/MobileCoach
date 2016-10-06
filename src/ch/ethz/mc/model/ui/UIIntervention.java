package ch.ethz.mc.model.ui;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab at the Health-IS Lab
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIIntervention extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	INTERVENTION_NAME				= "interventionName";
	public static final String	INTERVENTION_STATUS				= "interventionStatus";
	public static final String	MONITORING_STATUS				= "monitoringStatus";
	public static final String	ASSIGNED_SENDER_IDENTIFICATION	= "assignedSenderIdentification";

	@PropertyId(INTERVENTION_NAME)
	private String				interventionName;

	private boolean				booleanInterventionStatus;

	@PropertyId(INTERVENTION_STATUS)
	private String				interventionStatus;

	private boolean				booleanMessagingStatus;

	@PropertyId(MONITORING_STATUS)
	private String				monitoringStatus;

	@PropertyId(ASSIGNED_SENDER_IDENTIFICATION)
	private String				assignedSenderIdentification;

	public static Object[] getVisibleColumns() {
		return new Object[] { INTERVENTION_NAME, INTERVENTION_STATUS,
				MONITORING_STATUS, ASSIGNED_SENDER_IDENTIFICATION };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__INTERVENTION),
				localize(AdminMessageStrings.UI_COLUMNS__INTERVENTION_STATUS),
				localize(AdminMessageStrings.UI_COLUMNS__MONITORING_STATUS),
				localize(AdminMessageStrings.UI_COLUMNS__ASSIGNED_SENDER_IDENTIFICATION), };
	}

	public static String getSortColumn() {
		return INTERVENTION_NAME;
	}
}
