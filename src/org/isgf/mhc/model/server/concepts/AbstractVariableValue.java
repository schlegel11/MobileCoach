package org.isgf.mhc.model.server.concepts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;

/**
 * {@link ModelObject} to represent a variable value combination
 * 
 * A variable has a unique name and a value
 * 
 * @author Andreas Filler
 */
@AllArgsConstructor
public abstract class AbstractVariableValue extends ModelObject {
	/**
	 * Name of the variable
	 */
	@Getter
	@Setter
	private String	name;

	/**
	 * Value of the variable
	 */
	@Getter
	@Setter
	private String	value;
}
