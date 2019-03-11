package ch.ethz.mc.model.rest;

/* ##LICENSE## */
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for the average of variables with REST
 *
 * @author Andreas Filler
 */
public class VariableAverageWithParticipant {
	@Getter
	@Setter
	private String	variable;
	@Getter
	@Setter
	private double	average;
	@Getter
	@Setter
	private int		size;
	@Getter
	@Setter
	private double	valueOfParticipant;
}
