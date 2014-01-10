package org.isgf.mhc.model.server.concepts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;

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
}
