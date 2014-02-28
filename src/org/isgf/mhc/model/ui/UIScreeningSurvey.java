package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIScreeningSurvey extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	SCREENING_SURVEY_NAME		= "screeningSurveyName";
	public static final String	SCREENING_SURVEY_PASSWORD	= "screeningSurveyPassword";
	public static final String	SCREENING_SURVEY_STATUS		= "screeningSurveyStatus";

	@PropertyId(SCREENING_SURVEY_NAME)
	private String				screeningSurveyName;

	@PropertyId(SCREENING_SURVEY_PASSWORD)
	private String				screeningSurveyPassword;

	private boolean				booleanScreeningSurveyStatus;

	@PropertyId(SCREENING_SURVEY_STATUS)
	private String				screeningSurveyStatus;

	public static Object[] getVisibleColumns() {
		return new Object[] { SCREENING_SURVEY_NAME, SCREENING_SURVEY_PASSWORD,
				SCREENING_SURVEY_STATUS };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__SCREENING_SURVEY),
				localize(AdminMessageStrings.UI_COLUMNS__SCREENING_SURVEY_PASSWORD),
				localize(AdminMessageStrings.UI_COLUMNS__SCREENING_SURVEY_STATUS) };
	}

	public static String getSortColumn() {
		return SCREENING_SURVEY_NAME;
	}
}
