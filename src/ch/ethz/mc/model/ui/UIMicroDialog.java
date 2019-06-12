package ch.ethz.mc.model.ui;

/* ##LICENSE## */
import com.vaadin.data.fieldgroup.PropertyId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIMicroDialog extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	NAME	= "name";

	@PropertyId(NAME)
	private String				name;

	@Override
	public String toString() {
		return name;
	}
}
