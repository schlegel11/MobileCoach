package ch.ethz.mc.model.ui;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
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
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIParticipant extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	NAME							= "name";
	public static final String	ORGANIZATION					= "organization";
	public static final String	UNIT							= "unit";
	public static final String	CREATED							= "created";
	public static final String	SCREENING_SURVEY_NAME			= "screeningSurveyName";
	public static final String	SCREENING_SURVEY_STATUS			= "screeningSurveyStatus";
	public static final String	DATA_FOR_MONITORING_AVAILABLE	= "dataForMonitoringAvailable";
	public static final String	INTERVENTION_STATUS				= "interventionStatus";
	public static final String	MONITORING_STATUS				= "monitoringStatus";

	@PropertyId(NAME)
	private String				name;

	@PropertyId(ORGANIZATION)
	private String				organization;

	@PropertyId(UNIT)
	private String				unit;

	@PropertyId(CREATED)
	private Date				created;

	@PropertyId(SCREENING_SURVEY_NAME)
	private String				screeningSurveyName;

	@PropertyId(SCREENING_SURVEY_STATUS)
	private String				screeningSurveyStatus;

	private boolean				booleanScreeningSurveyStatus;

	@PropertyId(SCREENING_SURVEY_STATUS)
	private String				dataForMonitoringAvailable;

	private boolean				booleanDataForMonitoringAvailable;

	@PropertyId(INTERVENTION_STATUS)
	private String				interventionStatus;

	private boolean				booleanInterventionStatus;

	@PropertyId(MONITORING_STATUS)
	private String				monitoringStatus;

	private boolean				booleanMonitoringStatus;

	public static Object[] getVisibleColumns() {
		return new Object[] { NAME, ORGANIZATION, UNIT, CREATED,
				SCREENING_SURVEY_NAME, SCREENING_SURVEY_STATUS,
				DATA_FOR_MONITORING_AVAILABLE, INTERVENTION_STATUS,
				MONITORING_STATUS };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION_UNIT),
				localize(AdminMessageStrings.UI_COLUMNS__CREATED),
				localize(AdminMessageStrings.UI_COLUMNS__SCREENING_SURVEY_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_SCREENING_SURVEY_STATUS),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_DATA_FOR_MONITORING_AVAILABLE),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_INTERVENTION_STATUS),
				localize(AdminMessageStrings.UI_COLUMNS__MONITORING_STATUS) };
	}

	public static String getSortColumn() {
		return CREATED;
	}
}
