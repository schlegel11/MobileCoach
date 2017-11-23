package ch.ethz.mc.model.ui;

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
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIInterventionVariable extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	NAME			= "name";
	public static final String	VALUE			= "value";
	public static final String	PRIVACY_TYPE	= "privacyType";
	public static final String	ACCESS_TYPE		= "accessType";

	@PropertyId(NAME)
	private String				name;

	@PropertyId(VALUE)
	private String				value;

	@PropertyId(PRIVACY_TYPE)
	private String				privacyType;

	@PropertyId(ACCESS_TYPE)
	private String				accessType;

	public static Object[] getVisibleColumns() {
		return new Object[] { NAME, VALUE, PRIVACY_TYPE, ACCESS_TYPE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE_VALUE),
				localize(AdminMessageStrings.UI_COLUMNS__PRIVACY_TYPE),
				localize(AdminMessageStrings.UI_COLUMNS__ACCESS_TYPE) };
	}

	public static String getSortColumn() {
		return NAME;
	}

	@Override
	public String toString() {
		return getRelatedModelObject(InterventionVariableWithValue.class)
				.getName();
	}
}
