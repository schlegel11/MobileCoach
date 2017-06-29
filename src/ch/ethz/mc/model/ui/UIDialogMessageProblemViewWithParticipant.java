package ch.ethz.mc.model.ui;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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
public class UIDialogMessageProblemViewWithParticipant extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	PARTICIPANT_ID				= "participantId";
	public static final String	PARTICIPANT_NAME			= "participantName";
	public static final String	LANGUAGE					= "language";
	public static final String	GROUP						= "group";
	public static final String	ORGANIZATION				= "organization";
	public static final String	ORGANIZATION_UNIT			= "organizationUnit";

	public static final String	MESSAGE						= "message";
	public static final String	TYPE						= "type";

	public static final String	SENT_TIMESTAMP				= "sentTimestamp";

	public static final String	ANSWER						= "answer";
	public static final String	RAW_ANSWER					= "rawAnswer";
	public static final String	ANSWER_RECEIVED_TIMESTAMP	= "answerReceivedTimestamp";

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

	@PropertyId(ORGANIZATION_UNIT)
	private String				organizationUnit;

	@PropertyId(TYPE)
	private String				type;

	@PropertyId(MESSAGE)
	private String				message;

	@PropertyId(SENT_TIMESTAMP)
	private Date				sentTimestamp;

	@PropertyId(ANSWER)
	private String				answer;

	@PropertyId(RAW_ANSWER)
	private String				rawAnswer;

	@PropertyId(ANSWER_RECEIVED_TIMESTAMP)
	private Date				answerReceivedTimestamp;

	public static Object[] getVisibleColumns() {
		return new Object[] { PARTICIPANT_ID, PARTICIPANT_NAME, LANGUAGE,
				GROUP, ORGANIZATION, ORGANIZATION_UNIT, TYPE, MESSAGE,
				SENT_TIMESTAMP, ANSWER, RAW_ANSWER, ANSWER_RECEIVED_TIMESTAMP };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_ID),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__LANGUAGE),
				localize(AdminMessageStrings.UI_COLUMNS__GROUP),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION_UNIT),
				localize(AdminMessageStrings.UI_COLUMNS__TYPE),
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGE),
				localize(AdminMessageStrings.UI_COLUMNS__SENT_TIMESTAMP),
				localize(AdminMessageStrings.UI_COLUMNS__ANSWER),
				localize(AdminMessageStrings.UI_COLUMNS__RAW_ANSWER),
				localize(AdminMessageStrings.UI_COLUMNS__ANSWER_RECEIVED_TIMESTAMP) };
	}

	public static String getSortColumn() {
		return ANSWER_RECEIVED_TIMESTAMP;
	}
}
