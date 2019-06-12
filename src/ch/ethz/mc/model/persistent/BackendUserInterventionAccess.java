package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.ui.UIBackendUserInterventionAccess;
import ch.ethz.mc.model.ui.UIModelObject;
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
