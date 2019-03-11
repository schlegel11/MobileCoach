package ch.ethz.mc.model.rest;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for a variable with a value for REST
 *
 * @author Andreas Filler
 */
@AllArgsConstructor
public class Variable {
	@Getter
	@Setter
	private String	variable;
	@Getter
	@Setter
	private String	value;
}
