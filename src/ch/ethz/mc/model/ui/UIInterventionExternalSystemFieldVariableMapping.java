package ch.ethz.mc.model.ui;

import com.vaadin.data.fieldgroup.PropertyId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.InterventionExternalSystemFieldVariableMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIInterventionExternalSystemFieldVariableMapping extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	FIELD_NAME							= "fieldName";
	public static final String	VARIABLE_WITH_VALUE_NAME			= "variableWithValueName";

	@PropertyId(FIELD_NAME)
	private String				fieldName;
	
	@PropertyId(VARIABLE_WITH_VALUE_NAME)
	private String				variableWithValueName;

	public static Object[] getVisibleColumns() {
		return new Object[] { FIELD_NAME, VARIABLE_WITH_VALUE_NAME };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__EXTERNAL_SYSTEM_FIELD_VARIABLE_MAPPING_FIELD_NAME),
				localize(AdminMessageStrings.UI_COLUMNS__EXTERNAL_SYSTEM_FIELD_VARIABLE_MAPPING_VARIABLE_WITH_VALUE_NAME)};
	}

	@Override
	public String toString() {
		return getRelatedModelObject(InterventionExternalSystemFieldVariableMapping.class)
				.getFieldName();
	}
}
