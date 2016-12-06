package ch.ethz.mc.services.internal;

import java.util.Hashtable;
import java.util.Set;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.services.internal.VariablesManagerService.InvalidVariableNameException;
import ch.ethz.mc.services.internal.VariablesManagerService.WriteProtectedVariableException;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableException;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableStore;

public class InDataBaseVariableStore implements VariableStore {

	private final VariablesManagerService variableManagerService;
	private final ObjectId participantId;
	private final Participant participant;

	public InDataBaseVariableStore(final VariablesManagerService variableManagerService, ObjectId participantId, Participant participant){
		this.variableManagerService = variableManagerService;
		this.participantId = participantId;
		this.participant = participant;
	}

	@Override
	public void initialize(String variableName, String value) throws VariableException {

		if (!variableName.startsWith("$")){
			throw new VariableException("variable name does not start with '$': " + value);
		}

		Hashtable<String, AbstractVariableWithValue> table = variableManagerService.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		if(table.containsKey(variableName)) throw new VariableException("variable already exists: " + variableName); 

		try {
			variableManagerService.writeVariableValueOfParticipant(participantId,
					variableName, value,
					true, true);
		} catch (WriteProtectedVariableException | InvalidVariableNameException e) {

			throw new VariableException("variable writing failed: " + variableName);
		} 
	}

	@Override
	public void set(String variableName, String value) throws VariableException {

		Hashtable<String, AbstractVariableWithValue> table = variableManagerService.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		if(!table.containsKey(variableName)) throw new VariableException("Variable has not been initilaized: " + variableName);

		try{
			variableManagerService.writeVariableValueOfParticipant(participantId,
					variableName, value,
					true, true);

		}catch(WriteProtectedVariableException | InvalidVariableNameException e){
			throw new VariableException("variable writing failed: " + variableName);
		}
	}

	@Override
	public String get(String variableName) throws VariableException {
		String variable = null;

		Hashtable<String, AbstractVariableWithValue> table = variableManagerService.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		if(table.containsKey(variableName)){
			variable = table.get(variableName).getValue();
		}

		if(variable == null) throw new VariableException("variable cannot be read: " + variableName);
		return variable;
	}


	@Override
	public Boolean containsVariable(String variableName) {
		Hashtable<String, AbstractVariableWithValue> table = variableManagerService.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		return table.keySet().contains(variableName);
	}

	@Override
	public Set<String> getVariableNames() {

		Hashtable<String, AbstractVariableWithValue> table = variableManagerService.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		return table.keySet();
	}
}