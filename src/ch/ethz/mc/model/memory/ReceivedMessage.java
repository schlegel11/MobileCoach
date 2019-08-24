package ch.ethz.mc.model.memory;

/* ##LICENSE## */
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.model.rest.Variable;
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
	private DialogOptionTypes		type;

	@Getter
	@Setter
	private String					sender;

	@Getter
	@Setter
	private String					message;

	@Getter
	@Setter
	private boolean					typeIntention;

	@Getter
	@Setter
	private String					clientId;

	@Getter
	@Setter
	private int						relatedMessageIdBasedOnOrder;

	@Getter
	@Setter
	private String					intention;

	@Getter
	@Setter
	private String					content;

	@Getter
	@Setter
	private String					text;

	@Getter
	@Setter
	private long					receivedTimestamp;

	@Getter
	@Setter
	private long					clientTimestamp;

	@Getter
	@Setter
	private String					mediaURL;

	@Getter
	@Setter
	private String					mediaType;

	@Getter
	@Setter
	private String					externalSystemId;

	@Getter
	@Setter
	private boolean					externalSystem;

	@Getter
	private final List<Variable>	externalSystemVariables	= new ArrayList<>();

	public boolean addExternalSystemVariable(Variable variable) {
		return externalSystemVariables.add(variable);
	}
	
	public boolean addAllExternalSystemVariables(Collection<Variable> variables) {
		return externalSystemVariables.addAll(variables);
	}

}
