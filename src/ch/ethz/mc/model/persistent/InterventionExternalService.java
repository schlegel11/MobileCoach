package ch.ethz.mc.model.persistent;

import java.util.List;

import org.apache.bcel.generic.GETSTATIC;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.ui.UIInterventionExternalService;
import ch.ethz.mc.model.ui.UIInterventionVariable;
import ch.ethz.mc.model.ui.UIModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

@NoArgsConstructor
@AllArgsConstructor
public class InterventionExternalService extends ModelObject {
	private static final long serialVersionUID = 1310200817438821553L;
	
	/**
	 * {@link Intervention} to which this service and its token belong to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			intervention;
	
	/**
	 * Id of the service
	 */
	@Getter
	@Setter
	@NonNull
	private String				serviceId;

	/**
	 * Name of the service
	 */
	@Getter
	@Setter
	@NonNull
	private String				name;

	/**
	 * Token of the service
	 */
	@Getter
	@Setter
	@NonNull
	private String				token;
	
	/**
	 * Status of the service
	 */
	@Getter
	@Setter
	private boolean				active;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		final val variable = new UIInterventionExternalService(getServiceId(), getName(), getToken(), active,
				active ? Messages.getAdminString(AdminMessageStrings.UI_MODEL__ACTIVE)
						: Messages.getAdminString(AdminMessageStrings.UI_MODEL__INACTIVE));
		variable.setRelatedModelObject(this);

		return variable;
	}

	/**
	 * Will recursively collect all related {@link ModelObject} for export
	 *
	 * @param exportList
	 *            The {@link ModelObject} itself and all related
	 *            {@link ModelObject}s
	 */
	@Override
	@JsonIgnore
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}

}
