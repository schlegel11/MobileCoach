package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIDialogMessageWithParticipant extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	PARTICIPANT_ID				= "participantId";
	public static final String	PARTICIPANT_NAME			= "participantName";
	public static final String	ORDER						= "order";
	public static final String	STATUS						= "status";

	public static final String	MESSAGE						= "message";
	public static final String	SHOULD_BE_SENT_TIMESTAMP	= "shouldBeSentTimestamp";
	public static final String	SENT_TIMESTAMP				= "sentTimestamp";

	public static final String	ANSWER						= "answer";
	public static final String	ANSWER_RECEIVED_TIMESTAMP	= "answerReceivedTimestamp";

	public static final String	MANUALLY_SENT				= "manuallySent";
	public static final String	MEDIA_CONTENT_VIEWED		= "mediaContentViewed";

	@PropertyId(PARTICIPANT_ID)
	private String				participantId;

	@PropertyId(PARTICIPANT_NAME)
	private String				participantName;

	@PropertyId(ORDER)
	private String				order;

	@PropertyId(STATUS)
	private String				status;

	@PropertyId(MESSAGE)
	private String				message;

	@PropertyId(SHOULD_BE_SENT_TIMESTAMP)
	private String				shouldBeSentTimestamp;

	@PropertyId(SENT_TIMESTAMP)
	private String				sentTimestamp;

	@PropertyId(ANSWER)
	private String				answer;

	@PropertyId(ANSWER_RECEIVED_TIMESTAMP)
	private String				answerReceivedTimestamp;

	@PropertyId(MANUALLY_SENT)
	private String				manuallySent;
	@PropertyId(MEDIA_CONTENT_VIEWED)
	private String				mediaContentViewed;

	public static Object[] getVisibleColumns() {
		return new Object[] { PARTICIPANT_ID, PARTICIPANT_NAME, ORDER, STATUS,
				MESSAGE, SHOULD_BE_SENT_TIMESTAMP, SENT_TIMESTAMP, ANSWER,
				ANSWER_RECEIVED_TIMESTAMP, MANUALLY_SENT, MEDIA_CONTENT_VIEWED };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_ID),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__ORDER),
				localize(AdminMessageStrings.UI_COLUMNS__STATUS),
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGE),
				localize(AdminMessageStrings.UI_COLUMNS__SHOULD_BE_SENT_TIMESTAMP),
				localize(AdminMessageStrings.UI_COLUMNS__SENT_TIMESTAMP),
				localize(AdminMessageStrings.UI_COLUMNS__ANSWER),
				localize(AdminMessageStrings.UI_COLUMNS__ANSWER_RECEIVED_TIMESTAMP),
				localize(AdminMessageStrings.UI_COLUMNS__MANUALLY_SENT),
				localize(AdminMessageStrings.UI_COLUMNS__MEDIA_CONTENT_VIEWED) };
	}

	public static String getSortColumn() {
		return PARTICIPANT_ID;
	}
}