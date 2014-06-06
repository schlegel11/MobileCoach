package ch.ethz.mc.model.memory;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ch.ethz.mc.services.internal.CommunicationManagerService;

/**
 * Contains a message as received by the {@link CommunicationManagerService}
 * 
 * @author Andreas Filler
 */
@ToString
public class ReceivedMessage {
	@Getter
	@Setter
	private String	recipient;

	@Getter
	@Setter
	private String	sender;

	@Getter
	@Setter
	private String	message;

	@Getter
	@Setter
	private long	receivedTimestamp;
}
