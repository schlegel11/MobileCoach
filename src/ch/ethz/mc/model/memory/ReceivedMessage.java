package ch.ethz.mc.model.memory;

/* ##LICENSE## */
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.services.internal.CommunicationManagerService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains a message as received by the {@link CommunicationManagerService}
 *
 * @author Andreas Filler
 */
@ToString
public class ReceivedMessage {
	@Getter
	@Setter
	private DialogOptionTypes	type;

	@Getter
	@Setter
	private String				sender;

	@Getter
	@Setter
	private String				message;

	@Getter
	@Setter
	private boolean				typeIntention;

	@Getter
	@Setter
	private String				clientId;

	@Getter
	@Setter
	private int					relatedMessageIdBasedOnOrder;

	@Getter
	@Setter
	private String				intention;

	@Getter
	@Setter
	private String				content;

	@Getter
	@Setter
	private String				text;

	@Getter
	@Setter
	private long				receivedTimestamp;

	@Getter
	@Setter
	private long				clientTimestamp;

	@Getter
	@Setter
	private String				mediaURL;

	@Getter
	@Setter
	private String				mediaType;
}
