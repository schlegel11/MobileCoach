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
public class UploadOK {
	@Getter
	private final String mediaReference;
}
