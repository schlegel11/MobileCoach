package ch.ethz.mc.model.ui;

import com.vaadin.data.fieldgroup.PropertyId;

import ch.ethz.mc.conf.AdminMessageStrings;
/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIBackendUserInterventionAccess extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	USERNAME		= "username";
	public static final String	TYPE			= "type";
	public static final String	GROUP_PATTERN	= "groupPattern";

	@PropertyId(USERNAME)
	private String				username;

	@PropertyId(TYPE)
	private String				type;

	@PropertyId(GROUP_PATTERN)
	private String				groupPattern;

	public static Object[] getVisibleColumns() {
		return new Object[] { USERNAME, TYPE, GROUP_PATTERN };
	}

	public static String[] getColumnHeaders() {
		return new String[] { localize(AdminMessageStrings.UI_COLUMNS__ACCOUNT),
				localize(AdminMessageStrings.UI_COLUMNS__ACCOUNT_TYPE),
				localize(AdminMessageStrings.UI_COLUMNS__GROUP_PATTERN) };
	}

	public static String getSortColumn() {
		return USERNAME;
	}
}
