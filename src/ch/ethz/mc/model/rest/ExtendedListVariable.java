package ch.ethz.mc.model.rest;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for variables with values for REST (extended to describe if the
 * variable belongs to the participant)
 *
 * @author Andreas Filler
 */
@AllArgsConstructor
public class ExtendedListVariable {
	@Getter
	private final List<Variable>	variables	= new ArrayList<Variable>();
	@Getter
	@Setter
	private String					identifier;
	@Getter
	@Setter
	private boolean					ownValue;
}
