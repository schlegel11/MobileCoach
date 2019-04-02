package ch.ethz.mc.model.persistent;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.ui.UIInterventionExternalService;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.services.internal.ExternalServicesManagerService;
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
		final val externalService = new UIInterventionExternalService(getServiceId(), getName(), getToken(), active,
				active ? Messages.getAdminString(AdminMessageStrings.UI_MODEL__ACTIVE)
						: Messages.getAdminString(AdminMessageStrings.UI_MODEL__INACTIVE));
		externalService.setRelatedModelObject(this);

		return externalService;
	}
	
	@Override
	protected void performOnDelete() {
		val externalServicesManagerService = ExternalServicesManagerService
				.getInstance();

		if (externalServicesManagerService != null) {
			externalServicesManagerService.deleteExternalServiceOnDeepstream(this);
		}
		
		// Delete intervention external service mappings
		val interventionExternalServiceMappingsToDelete = ModelObject.find(
				InterventionExternalServiceFieldVariableMapping.class,
				Queries.INTERVENTION_EXTERNAL_SERVICE_FIELD_VARIABLE_MAPPING__BY_INTERVENTION_EXTERNAL_SERVICE,
				getId());
		ModelObject.delete(interventionExternalServiceMappingsToDelete);
		
	}
}
