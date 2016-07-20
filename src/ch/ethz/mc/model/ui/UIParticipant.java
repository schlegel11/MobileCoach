package ch.ethz.mc.model.ui;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
	public static final String	PARTICIPANT_ID					= "participantId";
	public static final String	PARTICIPANT_NAME				= "participantName";
	public static final String	LANGUAGE						= "language";
	public static final String	GROUP							= "group";
	public static final String	ORGANIZATION					= "organization";
	public static final String	UNIT							= "unit";
	public static final String	CREATED							= "created";
	public static final String	SCREENING_SURVEY_NAME			= "screeningSurveyName";
	public static final String	SCREENING_SURVEY_STATUS			= "screeningSurveyStatus";
	public static final String	DATA_FOR_MONITORING_AVAILABLE	= "dataForMonitoringAvailable";
	public static final String	INTERVENTION_STATUS				= "interventionStatus";
	public static final String	MONITORING_STATUS				= "monitoringStatus";

	@PropertyId(PARTICIPANT_ID)
	private String				participantId;

	@PropertyId(PARTICIPANT_NAME)
	private String				participantName;

	@PropertyId(LANGUAGE)
	private String				language;

	@PropertyId(GROUP)
	private String				group;

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
		return new Object[] { PARTICIPANT_ID, PARTICIPANT_NAME, LANGUAGE,
				GROUP, ORGANIZATION, UNIT, CREATED, SCREENING_SURVEY_NAME,
				SCREENING_SURVEY_STATUS, DATA_FOR_MONITORING_AVAILABLE,
				INTERVENTION_STATUS, MONITORING_STATUS };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_ID),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__LANGUAGE),
				localize(AdminMessageStrings.UI_COLUMNS__GROUP),
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
