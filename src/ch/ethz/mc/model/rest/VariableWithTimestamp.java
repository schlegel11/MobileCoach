package ch.ethz.mc.model.rest;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for a variable with a value and timestamp for REST
 *
 * @author Andreas Filler
 */
@AllArgsConstructor
public class VariableWithTimestamp {
	@Getter
	@Setter
	private String	variable;
	@Getter
	@Setter
	private String	value;
	@Getter
	@Setter
	private long	timestamp;
}
