package ch.ethz.mc.model.ui.results;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import ch.ethz.mc.model.ui.UIModelObject;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIVariableWithParticipantForResults extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	PARTICIPANT_ID		= "participantId";
	public static final String	PARTICIPANT_NAME	= "participantName";
	public static final String	NAME				= "name";
	public static final String	VALUE				= "value";

	@PropertyId(PARTICIPANT_ID)
	private String				participantId;

	@PropertyId(PARTICIPANT_NAME)
	private String				participantName;

	@PropertyId(NAME)
	private String				name;

	@PropertyId(VALUE)
	private String				value;

	public static Object[] getVisibleColumns() {
		return new Object[] { PARTICIPANT_ID, PARTICIPANT_NAME, NAME, VALUE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_ID),
				localize(AdminMessageStrings.UI_COLUMNS__PARTICIPANT_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_VALUE) };
	}

	public static String getSortColumn() {
		return PARTICIPANT_ID;
	}
}