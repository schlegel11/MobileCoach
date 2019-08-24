package ch.ethz.mc.model.rest;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for variables with values and timestamp for REST
 *
 * @author Andreas Filler
 */
public class CollectionOfVariablesWithTimestamp {
	@Getter
	private final List<VariableWithTimestamp>	variables	= new ArrayList<VariableWithTimestamp>();
	@Getter
	@Setter
	private int									size;
}
