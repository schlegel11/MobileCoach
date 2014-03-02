package org.isgf.mhc.model.server.types;

/**
 * Supported {@link EquationSignTypes}
 * 
 * @author Andreas Filler
 */
public enum EquationSignTypes {
	IS_SMALLER_THAN, IS_SMALLER_OR_EQUAL_THAN, EQUALS, IS_BIGGER_OR_EQUAL_THAN, IS_BIGGER_THAN;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}
}
