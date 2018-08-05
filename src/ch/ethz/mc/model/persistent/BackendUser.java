package ch.ethz.mc.model.persistent;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.types.BackendUserTypes;
import ch.ethz.mc.model.ui.UIBackendUser;
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
 * {@link ModelObject} to represent a {@link BackendUser}
 * 
 * Backend users can be admins, authors or team managers.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class BackendUser extends ModelObject {
	private static final long	serialVersionUID	= 9068910637074662586L;

	@Getter
	@Setter
	private BackendUserTypes	type;

	/**
	 * Username of {@link BackendUser} required to authenticate
	 */
	@Getter
	@Setter
	@NonNull
	private String				username;

	/**
	 * Hash of password of {@link BackendUser} required to authenticate
	 */
	@Getter
	@Setter
	@NonNull
	private String				passwordHash;

	/**
	 * Returns if {@link BackendUser} is an admin
	 */
	@JsonIgnore
	public boolean isAdmin() {
		if (type == BackendUserTypes.ADMIN) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns if {@link BackendUser} has the right to access the editing
	 * backend
	 */
	@JsonIgnore
	public boolean hasEditingBackendAccess() {
		if (type == BackendUserTypes.ADMIN || type == BackendUserTypes.AUTHOR) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns if {@link BackendUser} has the right to access the dashboard
	 * backend
	 */
	@JsonIgnore
	public boolean hasDashboardBackendAccess() {
		if (type == BackendUserTypes.ADMIN
				|| type == BackendUserTypes.TEAM_MANAGER) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		String uiType = null;

		switch (type) {
			case ADMIN:
				uiType = Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__ADMIN);
				break;
			case AUTHOR:
				uiType = Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__AUTHOR);
				break;
			case TEAM_MANAGER:
				uiType = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__TEAM_MANAGER);
			case NO_RIGHTS:
				uiType = Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__NO_RIGHTS);
				break;

		}

		final val backendUser = new UIBackendUser(username, uiType);

		backendUser.setRelatedModelObject(this);

		return backendUser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		val backendUserInterventionAccessesToDelete = ModelObject.find(
				BackendUserInterventionAccess.class,
				Queries.BACKEND_USER_INTERVENTION_ACCESS__BY_BACKEND_USER,
				getId());

		ModelObject.delete(backendUserInterventionAccessesToDelete);
	}
}
