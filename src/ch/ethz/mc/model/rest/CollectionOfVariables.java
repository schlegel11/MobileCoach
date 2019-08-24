package ch.ethz.mc.model.rest;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for variables with values for REST
 *
 * @author Andreas Filler
 */
public class CollectionOfVariables {
	@Getter
	private final List<Variable>	variables	= new ArrayList<Variable>();
	@Getter
	@Setter
	private int						size;
}
