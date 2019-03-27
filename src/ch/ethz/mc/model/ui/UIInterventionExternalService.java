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
import ch.ethz.mc.model.persistent.InterventionExternalService;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIInterventionExternalService extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ID				= "id";
	public static final String	NAME			= "name";
	public static final String	TOKEN			= "token";
	public static final String	STATUS			= "status";

	@PropertyId(ID)
	private String				id;
	
	@PropertyId(NAME)
	private String				name;

	@PropertyId(TOKEN)
	private String				token;
	
	private boolean				booleanStatus;
	
	@PropertyId(STATUS)
	private String				status;

	public static Object[] getVisibleColumns() {
		return new Object[] { NAME, ID, TOKEN, STATUS };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__EXTERNAL_SERVICE_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__EXTERNAL_SERVICE_ID),
				localize(AdminMessageStrings.UI_COLUMNS__EXTERNAL_SERVICE_TOKEN),
				localize(AdminMessageStrings.UI_COLUMNS__EXTERNAL_SERVICE_STATUS)};
	}

	public static String getSortColumn() {
		return NAME;
	}

	@Override
	public String toString() {
		return getRelatedModelObject(InterventionExternalService.class)
				.getName();
	}
}
