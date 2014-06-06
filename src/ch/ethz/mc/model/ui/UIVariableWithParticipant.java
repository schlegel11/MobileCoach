package ch.ethz.mc.model.ui;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIVariableWithParticipant extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	PARTICIPANT_ID		= "participantId";
	public static final String	PARTICIPANT_NAME	= "participantName";
	public static final String	ORGANIZATION		= "organization";
	public static final String	ORGANIZATION_UNIT	= "organizationUnit";

	public static final String	NAME				= "name";
	public static final String	VALUE				= "value";
	public static final String	LAST_UPDATED		= "lastUpdated";

	@PropertyId(PARTICIPANT_ID)
	private String				participantId;

	@PropertyId(PARTICIPANT_NAME)
	private String				participantName;

	@PropertyId(ORGANIZATION)
	private String				organization;

	@PropertyId(ORGANIZATION_UNIT)
	private String				organizationUnit;

	@PropertyId(NAME)
	private String				name;

	@PropertyId(VALUE)
	private String				value;

	@PropertyId(VALUE)
	private Date				lastUpdated;

	public static Object[] getVisibleColumns() {
		return new Object[] { PARTICIPANT_ID, PARTICIPANT_NAME, ORGANIZATION,
				ORGANIZATION_UNIT, NAME, VALUE, LAST_UPDATED };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_ID),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION_UNIT),
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_VALUE),
				localize(AdminMessageStrings.UI_COLUMNS__LAST_UPDATED) };
	}

	public static String getSortColumn() {
		return LAST_UPDATED;
	}
}
