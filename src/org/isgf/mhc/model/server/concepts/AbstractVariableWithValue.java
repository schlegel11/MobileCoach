package org.isgf.mhc.model.server.concepts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.ui.UIModelObject;
import org.isgf.mhc.model.ui.UIVariable;

/**
 * {@link ModelObject} to represent a variable value combination
 * 
 * A variable has a unique name and a value
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractVariableWithValue extends ModelObject {
	/**
	 * Name of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private String	name;

	/**
	 * Value of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private String	value;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		final val variable = new UIVariable(name, value);

		variable.setRelatedModelObject(this);

		return variable;
	}
}
