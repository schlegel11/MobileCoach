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
public class UIDialogMessageReducedWithParticipant extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	PARTICIPANT_ID				= "participantId";
	public static final String	PARTICIPANT_NAME			= "participantName";
	public static final String	ORGANIZATION				= "organization";
	public static final String	ORGANIZATION_UNIT			= "organizationUnit";
	public static final String	STATUS						= "status";

	public static final String	SENT_TIMESTAMP				= "sentTimestamp";

	public static final String	ANSWER						= "answer";
	public static final String	RAW_ANSWER					= "rawAnswer";
	public static final String	ANSWER_RECEIVED_TIMESTAMP	= "answerReceivedTimestamp";

	@PropertyId(PARTICIPANT_ID)
	private String				participantId;

	@PropertyId(PARTICIPANT_NAME)
	private String				participantName;

	@PropertyId(ORGANIZATION)
	private String				organization;

	@PropertyId(ORGANIZATION_UNIT)
	private String				organizationUnit;

	@PropertyId(STATUS)
	private String				status;

	@PropertyId(SENT_TIMESTAMP)
	private Date				sentTimestamp;

	@PropertyId(ANSWER)
	private String				answer;

	@PropertyId(RAW_ANSWER)
	private String				rawAnswer;

	@PropertyId(ANSWER_RECEIVED_TIMESTAMP)
	private Date				answerReceivedTimestamp;

	public static Object[] getVisibleColumns() {
		return new Object[] { PARTICIPANT_ID, PARTICIPANT_NAME, ORGANIZATION,
				ORGANIZATION_UNIT, STATUS, SENT_TIMESTAMP, ANSWER, RAW_ANSWER,
				ANSWER_RECEIVED_TIMESTAMP };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_ID),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION_UNIT),
				localize(AdminMessageStrings.UI_COLUMNS__STATUS),
				localize(AdminMessageStrings.UI_COLUMNS__SENT_TIMESTAMP),
				localize(AdminMessageStrings.UI_COLUMNS__ANSWER),
				localize(AdminMessageStrings.UI_COLUMNS__RAW_ANSWER),
				localize(AdminMessageStrings.UI_COLUMNS__ANSWER_RECEIVED_TIMESTAMP) };
	}

	public static String getSortColumn() {
		return SENT_TIMESTAMP;
	}
}
