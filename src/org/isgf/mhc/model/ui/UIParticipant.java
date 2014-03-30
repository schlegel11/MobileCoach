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
	public static final String	NAME						= "name";
	public static final String	ORGANIZATION				= "organization";
	public static final String	UNIT						= "unit";
	public static final String	CREATED						= "created";
	public static final String	PERFORMED_SCREENING_SURVEY	= "preformedScreeningSurvey";
	public static final String	MESSAGING_STATUS			= "messagingStatus";

	@PropertyId(NAME)
	private String				name;

	@PropertyId(ORGANIZATION)
	private String				organization;

	@PropertyId(UNIT)
	private String				unit;

	@PropertyId(CREATED)
	private Date				created;

	@PropertyId(PERFORMED_SCREENING_SURVEY)
	private String				preformedScreeningSurvey;

	@PropertyId(MESSAGING_STATUS)
	private String				messagingStatus;

	private boolean				booleanMessagingStatus;

	public static Object[] getVisibleColumns() {
		return new Object[] { NAME, ORGANIZATION, UNIT, CREATED,
				PERFORMED_SCREENING_SURVEY, MESSAGING_STATUS };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION_UNIT),
				localize(AdminMessageStrings.UI_COLUMNS__CREATED),
				localize(AdminMessageStrings.UI_COLUMNS__PERFORMED_SCREENING_SURVEY),
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGING_STATUS) };
	}

	public static String getSortColumn() {
		return CREATED;
	}
}
