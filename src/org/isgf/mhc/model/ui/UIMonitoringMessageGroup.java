package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIMonitoringMessageGroup extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	NAME	= "name";

	@PropertyId(NAME)
	private String				name;

	@Override
	public String toString() {
		return name;
	}
}
