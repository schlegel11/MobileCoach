package ch.ethz.mc.model.persistent;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.ui.UIBackendUserInterventionAccess;
import ch.ethz.mc.model.ui.UIModelObject;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link BackendUserInterventionAccess}
 * 
 * The {@link BackendUserInterventionAccess} describes, which
 * {@link BackendUser} is allowed to access a specific {@link Intervention}.
 * 
 * author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class BackendUserInterventionAccess extends ModelObject {
	private static final long	serialVersionUID	= -2686891854353434099L;

	/**
	 * {@link BackendUser} who is allowed to access {@link Intervention}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			backendUser;

	/**
	 * {@link Intervention} that can be accessed by the {@link BackendUser}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			intervention;

	/**
	 * The group pattern the {@link BackendUser} has access to
	 */
	@Getter
	@Setter
	@NonNull
	private String				groupPattern;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {

		val backendUser = MC.getInstance()
				.getInterventionAdministrationManagerService()
				.getBackendUser(backendUser);

		String uiType = null;
		UIBackendUserInterventionAccess backendUserInterventionAccess = null;

		if (backendUser != null) {
			switch (backendUser.getType()) {
				case ADMIN:
					uiType = Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__ADMIN);
					break;
				case AUTHOR:
					uiType = Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__AUTHOR);
					break;
				case TEAM_MANAGER:
					uiType = Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__TEAM_MANAGER);
					break;
				case NO_RIGHTS:
					uiType = Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__NO_RIGHTS);
					break;

			}

			backendUserInterventionAccess = new UIBackendUserInterventionAccess(
					backendUser.getUsername(), uiType, getGroupPattern());
		} else {
			backendUserInterventionAccess = new UIBackendUserInterventionAccess(
					Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__NOT_SET),
					Messages.getAdminString(
							AdminMessageStrings.UI_MODEL__NOT_SET),
					getGroupPattern());
		}

		backendUserInterventionAccess.setRelatedModelObject(this);

		return backendUserInterventionAccess;
	}

}
