package ch.ethz.mc.model.rest;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for a cluster value of a variable for REST
 *
 * @author Andreas Filler
 */
@AllArgsConstructor
public class ClusterValue {
	@Getter
	@Setter
	private String	value;
	@Getter
	@Setter
	private int		size;
}
