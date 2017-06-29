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
public class UIParticipantVariableWithParticipant extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	PARTICIPANT_ID		= "participantId";
	public static final String	PARTICIPANT_NAME	= "participantName";
	public static final String	GROUP				= "group";
	public static final String	ORGANIZATION		= "organization";
	public static final String	ORGANIZATION_UNIT	= "organizationUnit";

	public static final String	NAME				= "name";
	public static final String	VALUE				= "value";
	public static final String	TIMESTAMP			= "timestamp";

	@PropertyId(PARTICIPANT_ID)
	private String				participantId;

	@PropertyId(PARTICIPANT_NAME)
	private String				participantName;

	@PropertyId(GROUP)
	private String				group;

	@PropertyId(ORGANIZATION)
	private String				organization;

	@PropertyId(ORGANIZATION_UNIT)
	private String				organizationUnit;

	@PropertyId(NAME)
	private String				name;

	@PropertyId(VALUE)
	private String				value;

	@PropertyId(TIMESTAMP)
	private Date				timestamp;

	public static Object[] getVisibleColumns() {
		return new Object[] { PARTICIPANT_ID, PARTICIPANT_NAME, GROUP,
				ORGANIZATION, ORGANIZATION_UNIT, NAME, VALUE, TIMESTAMP };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_ID),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__GROUP),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION),
				localize(AdminMessageStrings.UI_COLUMNS__ORGANIZATION_UNIT),
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_VALUE),
				localize(AdminMessageStrings.UI_COLUMNS__TIMESTAMP) };
	}

	public static String getSortColumn() {
		return TIMESTAMP;
	}
}
