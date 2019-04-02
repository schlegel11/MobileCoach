package ch.ethz.mc.model.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.ethz.mc.model.rest.Variable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Message from an external service
 * 
 * @author Marcel Schlegel
 */
@ToString
public class ExternalServiceMessage {

	@Getter
	@Setter
	private String serviceId;
	
	@Getter
	private final Set<String> participants = new HashSet<>();
	
	@Getter
	private final Map<String, Variable> variables = new HashMap<>();
	
	public boolean addParticipant(String participant) {
		return participants.add(participant);
	}
	
	public boolean addAllParticipants(Collection<String> participants) {
		return this.participants.addAll(participants);
	}
	
	public void putAllVariables(Map<String, Variable> variables) {
		this.variables.putAll(variables);
	}
}
