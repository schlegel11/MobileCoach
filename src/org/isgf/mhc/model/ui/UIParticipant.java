package org.isgf.mhc.model.ui;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIParticipant extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	NAME					= "name";
	public static final String	ORGANIZATION			= "organization";
	public static final String	UNIT					= "unit";
	public static final String	CREATED					= "created";
	public static final String	SCREENING_SURVEY_NAME	= "screeningSurveyName";
	public static final String	SCREENING_SURVEY_STATUS	= "screeningSurveyStatus";
	public static final String	INTERVENTION_STATUS		= "interventionStatus";
	public static final String	MESSAGING_STATUS		= "messagingStatus";

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

	@PropertyId(INTERVENTION_STATUS)
	private String				interventionStatus;

	private boolean				booleanInterventionStatus;

	@PropertyId(MESSAGING_STATUS)
	private String				messagingStatus;

	private boolean				booleanMessagingStatus;

	public static Object[] getVisibleColumns() {
		return new Object[] { NAME, ORGANIZATION, UNIT, CREATED,
				SCREENING_SURVEY_NAME, SCREENING_SURVEY_STATUS,
				INTERVENTION_STATUS, MESSAGING_STATUS };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION_UNIT),
				localize(AdminMessageStrings.UI_COLUMNS__CREATED),
				localize(AdminMessageStrings.UI_COLUMNS__SCREENING_SURVEY_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_SCREENING_SURVEY_STATUS),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_INTERVENTION_STATUS),
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGING_STATUS) };
	}

	public static String getSortColumn() {
		return CREATED;
	}
}
