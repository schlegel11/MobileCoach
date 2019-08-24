package ch.ethz.mc.model.persistent;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.ui.UIInterventionExternalSystem;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.services.internal.ExternalSystemsManagerService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

@NoArgsConstructor
@AllArgsConstructor
public class InterventionExternalSystem extends ModelObject {
	private static final long serialVersionUID = 1310200817438821553L;
	
	/**
	 * {@link Intervention} to which this service belongs to
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
	private String				systemId;

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
		final val externalSystem = new UIInterventionExternalSystem(getSystemId(), getName(), getToken(), active,
				active ? Messages.getAdminString(AdminMessageStrings.UI_MODEL__ACTIVE)
						: Messages.getAdminString(AdminMessageStrings.UI_MODEL__INACTIVE));
		externalSystem.setRelatedModelObject(this);

		return externalSystem;
	}
	
	@Override
	protected void performOnDelete() {
		val externalSystemsManagerService = ExternalSystemsManagerService
				.getInstance();

		if (externalSystemsManagerService != null) {
			externalSystemsManagerService.deleteExternalSystemOnDeepstream(this);
		}
		
		// Delete intervention external system mappings
		val interventionExternalSystemMappingsToDelete = ModelObject.find(
				InterventionExternalSystemFieldVariableMapping.class,
				Queries.INTERVENTION_EXTERNAL_SYSTEM_FIELD_VARIABLE_MAPPING__BY_INTERVENTION_EXTERNAL_SYSTEM,
				getId());
		ModelObject.delete(interventionExternalSystemMappingsToDelete);
		
	}
}
