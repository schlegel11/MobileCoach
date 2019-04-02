package ch.ethz.mc.model.ui;

import com.vaadin.data.fieldgroup.PropertyId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.InterventionExternalServiceFieldVariableMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIInterventionExternalServiceFieldVariableMapping extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	JSON_FIELD_NAME							= "jsonFieldName";
	public static final String	VARIABLE_WITH_VALUE_NAME				= "variableWithValueName";

	@PropertyId(JSON_FIELD_NAME)
	private String				jsonFieldName;
	
	@PropertyId(VARIABLE_WITH_VALUE_NAME)
	private String				variableWithValueName;

	public static Object[] getVisibleColumns() {
		return new Object[] { JSON_FIELD_NAME, VARIABLE_WITH_VALUE_NAME };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__EXTERNAL_SERVICE_FIELD_VARIABLE_MAPPING_JSON_FIELD_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__EXTERNAL_SERVICE_FIELD_VARIABLE_MAPPING_VARIABLE_WITH_VALUE_NAME)};
	}

	@Override
	public String toString() {
		return getRelatedModelObject(InterventionExternalServiceFieldVariableMapping.class)
				.getJsonFieldName();
	}
}
