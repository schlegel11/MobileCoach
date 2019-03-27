package ch.ethz.mc.model.memory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	private final Set<ExternalServiceVariable> variables = new HashSet<>();
	
	public boolean addParticipant(String participant) {
		return participants.add(participant);
	}
	
	public boolean addAllParticipants(Collection<String> participants) {
		return this.participants.addAll(participants);
	}
	
	public boolean addAllVariables(Collection<ExternalServiceVariable> variables) {
		return this.variables.addAll(variables);
	}
}
