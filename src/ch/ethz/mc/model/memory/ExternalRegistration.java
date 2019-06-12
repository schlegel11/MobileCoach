package ch.ethz.mc.model.memory;

/* ##LICENSE## */
import lombok.Data;
import lombok.NonNull;

/**
 * Contains the user id and secret after a registration based on an external id
 *
 * @author Andreas Filler
 */
@Data
public class ExternalRegistration {
	@NonNull
	private String	externalId;

	@NonNull
	private String	secret;
}
