package ch.ethz.mc.model.ui;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
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
public class UIScreeningSurvey extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	SCREENING_SURVEY_NAME		= "screeningSurveyName";
	public static final String	SCREENING_SURVEY_TYPE		= "screeningSurveyType";
	public static final String	SCREENING_SURVEY_PASSWORD	= "screeningSurveyPassword";
	public static final String	SCREENING_SURVEY_STATUS		= "screeningSurveyStatus";

	@PropertyId(SCREENING_SURVEY_NAME)
	private String				screeningSurveyName;

	@PropertyId(SCREENING_SURVEY_TYPE)
	private String				screeningSurveyType;

	@PropertyId(SCREENING_SURVEY_PASSWORD)
	private String				screeningSurveyPassword;

	private boolean				booleanScreeningSurveyStatus;

	@PropertyId(SCREENING_SURVEY_STATUS)
	private String				screeningSurveyStatus;

	public static Object[] getVisibleColumns() {
		return new Object[] { SCREENING_SURVEY_NAME, SCREENING_SURVEY_TYPE,
				SCREENING_SURVEY_PASSWORD, SCREENING_SURVEY_STATUS };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__SCREENING_SURVEY),
				localize(AdminMessageStrings.UI_COLUMNS__SCREENING_SURVEY_TYPE),
				localize(AdminMessageStrings.UI_COLUMNS__SCREENING_SURVEY_PASSWORD),
				localize(AdminMessageStrings.UI_COLUMNS__SCREENING_SURVEY_STATUS) };
	}

	public static String getSortColumn() {
		return SCREENING_SURVEY_NAME;
	}

	@Override
	public String toString() {
		return screeningSurveyName;
	}
}
