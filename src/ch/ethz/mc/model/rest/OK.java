package ch.ethz.mc.model.rest;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Wrapper for OK for REST
 *
 * @author Andreas Filler
 */
@AllArgsConstructor
public class OK {
	@Getter
	private final String result = "OK";
}
