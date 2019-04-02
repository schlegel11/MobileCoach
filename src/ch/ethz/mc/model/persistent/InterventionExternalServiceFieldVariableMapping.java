package ch.ethz.mc.model.persistent;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValueAccessTypes;
import ch.ethz.mc.model.ui.UIInterventionExternalServiceFieldVariableMapping;
import ch.ethz.mc.model.ui.UIModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

@NoArgsConstructor
@AllArgsConstructor
public class InterventionExternalServiceFieldVariableMapping
		extends ModelObject {
	private static final long	serialVersionUID	= 2961832517704835697L;

	/**
	 * {@link InterventionExternalService} to which this mapping belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			interventionExternalService;

	/**
	 * The JSON field name of a received service message which is mapped to a
	 * specific {@link AbstractVariableWithValue}
	 */
	@Getter
	@Setter
	@NonNull
	private String				jsonFieldName;

	/**
	 * The {@link InterventionVariableWithValue} which receives the value of the
	 * {@link #jsonFieldName}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			interventionVariableWithValue;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val interventionVariableWithValue = ModelObject.get(
				InterventionVariableWithValue.class,
				getInterventionVariableWithValue());
		String variableWithValueName;
		if (interventionVariableWithValue == null) {

			variableWithValueName = Messages
					.getAdminString(AdminMessageStrings.UI_MODEL__UNKNOWN);
		} else if (!interventionVariableWithValue.getAccessType()
				.isAllowedAtGivenOrLessRestrictiveAccessType(
						InterventionVariableWithValueAccessTypes.MANAGEABLE_BY_SERVICE)) {

			variableWithValueName = Messages.getAdminString(
					AdminMessageStrings.UI_MODEL__VARIABLE_NOT_ACCESSIBLE,
					interventionVariableWithValue.getName());
		} else {
			variableWithValueName = interventionVariableWithValue.getName();
		}

		final val externalServiceMapping = new UIInterventionExternalServiceFieldVariableMapping(
				getJsonFieldName(), variableWithValueName);
		externalServiceMapping.setRelatedModelObject(this);

		return externalServiceMapping;
	}
}
