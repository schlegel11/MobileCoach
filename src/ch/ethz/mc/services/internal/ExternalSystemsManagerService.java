package ch.ethz.mc.services.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.memory.ExternalRegistration;
import ch.ethz.mc.model.memory.ExternalSystemMessage;
import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.InterventionExternalSystem;
import ch.ethz.mc.model.persistent.InterventionExternalSystemFieldVariableMapping;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.model.rest.Variable;
import ch.ethz.mc.tools.InternalDateTime;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Manages all defined external systems for a specific intervention.
 *
 * @author Marcel Schlegel
 */
@Log4j2
public class ExternalSystemsManagerService {

	@Getter
	private static ExternalSystemsManagerService	instance	= null;

	private final DatabaseManagerService			databaseManagerService;
	private final DeepstreamCommunicationService	deepstreamCommunicationService;

	private ExternalSystemsManagerService(
			final DatabaseManagerService databaseManagerService,
			final DeepstreamCommunicationService deepstreamCommunicationService) {

		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.deepstreamCommunicationService = deepstreamCommunicationService;

		log.info("Started.");
	}

	public static ExternalSystemsManagerService start(
			final DatabaseManagerService databaseManagerService,
			final DeepstreamCommunicationService deepstreamCommunicationService)
			throws Exception {
		if (instance == null) {
			instance = new ExternalSystemsManagerService(databaseManagerService,
					deepstreamCommunicationService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	// Create the external system token and UID record.
	public ExternalRegistration createExternalSystemForDeepstream(
			final String systemName) {

		val systemRegistration = deepstreamCommunicationService
				.registerExternalSystem(systemName);

		return systemRegistration;
	}

	// Delete the external system record.
	public void deleteExternalSystemOnDeepstream(
			final InterventionExternalSystem externalSystem) {
		deepstreamCommunicationService.deleteExternalSystem(externalSystem);
	}

	// Renew external systems token.
	public String renewToken(final InterventionExternalSystem externalSystem) {
		return deepstreamCommunicationService
				.renewExternalSystemToken(externalSystem);
	}

	// Convert ExternalSystemMessages to ReceivedMessages
	public boolean getReceivedMessages(
			final ArrayList<ReceivedMessage> receivedMessages) {

		List<ExternalSystemMessage> externalSystemMessages = new ArrayList<>();
		// Get all received external system messages from deepstream service.
		deepstreamCommunicationService
				.getReceivedExternalSystemMessages(externalSystemMessages);
		// Iterate external system messages.
		for (ExternalSystemMessage externalSystemMessage : externalSystemMessages) {
			// Find the related external system from the received external
			// message.
			val externalSystem = databaseManagerService.findOneModelObject(
					InterventionExternalSystem.class,
					Queries.INTERVENTION_EXTERNAL_SYSTEM__BY_SYSTEM_ID,
					externalSystemMessage.getSystemId());
			// Check if the external system exists.
			if (externalSystem == null) {
				log.error(
						"There exists no external system with system id {}. Message can not be processed.",
						externalSystemMessage.getSystemId());
				continue;
			}
			// Check if the received external message provides participants
			if (externalSystemMessage.getParticipants().isEmpty()) {
				// No participants provided so use all participants from current
				// intervention.
				val participants = databaseManagerService.findModelObjects(
						Participant.class, Queries.PARTICIPANT__BY_INTERVENTION,
						externalSystem.getIntervention());
				participants.forEach(participant -> externalSystemMessage
						.addParticipant(participant.getId().toString()));
			}
			// Iterate the participants and build for everyone a ReceivedMessage
			// object.
			for (String participantId : externalSystemMessage
					.getParticipants()) {
				// Check if the participant id is valid.
				if (!ObjectId.isValid(participantId)) {
					log.warn(
							"Participant id {} is not valid. Message with system id {} can not be processed for participant id {}.",
							participantId, externalSystem.getSystemId(),
							participantId);
					continue;
				}
				val participant = databaseManagerService.getModelObjectById(
						Participant.class, new ObjectId(participantId));
				// Check if a persisted participant with the given id exists.
				if (participant == null) {
					log.warn(
							"Participant with id {} not found. Message with system id {} can not be processed for participant id {}.",
							participantId, externalSystem.getSystemId(),
							participantId);
					continue;
				}
				// Check if a found participant belongs to the current
				// intervention.
				if (!participant.getIntervention()
						.equals(externalSystem.getIntervention())) {
					log.warn(
							"Participant with id {} is not in the same intervention as the external system. Message with system id {} can not be processed for participant id {}.",
							participantId, externalSystem.getSystemId(),
							participantId);
					continue;
				}
				// Build the ReceivedMessage.
				val receivedMessage = mapExternalSystemMessageOnReceivedMessage(
						participant.getId(), externalSystem,
						externalSystemMessage);

				receivedMessages.add(receivedMessage);
			}
		}
		return !externalSystemMessages.isEmpty();
	}

	// Map ExternalSystemMessage on ReceivedMessage.
	private ReceivedMessage mapExternalSystemMessageOnReceivedMessage(
			ObjectId participantId, InterventionExternalSystem externalSystem,
			ExternalSystemMessage externalSystemMessage) {
		// Set all necessary values for the ReceivedMessage.
		val receivedMessage = new ReceivedMessage();
		receivedMessage.addAllExternalSystemVariables(
				mapVariables(externalSystem, externalSystemMessage));

		receivedMessage.setTypeIntention(false);
		receivedMessage.setRelatedMessageIdBasedOnOrder(-1);
		receivedMessage
				.setReceivedTimestamp(InternalDateTime.currentTimeMillis());
		receivedMessage
				.setExternalSystemId(externalSystemMessage.getSystemId());
		receivedMessage.setExternalSystem(true);
		receivedMessage.setMessage("");

		val dialogOptions = databaseManagerService.findModelObjects(
				DialogOption.class, Queries.DIALOG_OPTION__BY_PARTICIPANT,
				participantId);
		for (DialogOption dialogOption : dialogOptions) {
			receivedMessage.setSender(dialogOption.getData());

			if (dialogOption.getType() == DialogOptionTypes.EXTERNAL_ID) {
				receivedMessage.setType(DialogOptionTypes.EXTERNAL_ID);
				break;
			} else if (dialogOption
					.getType() == DialogOptionTypes.SUPERVISOR_EXTERNAL_ID) {
				receivedMessage
						.setType(DialogOptionTypes.SUPERVISOR_EXTERNAL_ID);
				break;
			} else {
				receivedMessage.setType(dialogOption.getType());
			}
		}

		return receivedMessage;
	}

	// Map the received external message key-value pairs to intervention
	// variables.
	private Collection<Variable> mapVariables(
			InterventionExternalSystem externalSystem,
			ExternalSystemMessage externalSystemMessage) {

		// Get all defined variable mappings from the external system.
		val variableMappings = databaseManagerService.findModelObjects(
				InterventionExternalSystemFieldVariableMapping.class,
				Queries.INTERVENTION_EXTERNAL_SYSTEM_FIELD_VARIABLE_MAPPING__BY_INTERVENTION_EXTERNAL_SYSTEM,
				externalSystem.getId());

		Collection<Variable> variables = new ArrayList<>();
		// Iterate the variable mappings.
		for (InterventionExternalSystemFieldVariableMapping variableMapping : variableMappings) {

			// Get the related InterventionVariableWithValue.
			val interventionVariable = databaseManagerService
					.getModelObjectById(InterventionVariableWithValue.class,
							variableMapping.getInterventionVariableWithValue());
			// Check if InterventionVariableWithValue exists.
			if (interventionVariable == null) {
				log.warn(
						"Mapped intervention variable for field {} not found. Mapping is ignored for message with system id {}.",
						variableMapping.getFieldName(),
						externalSystem.getSystemId());
				continue;
			}
			// Check if the received external message contains a key (field)
			// name which equals
			// to a defined variable mapping from the external system.
			val externalSystemVariable = externalSystemMessage.getVariables()
					.get(variableMapping.getFieldName());

			// If the received message contains a key (field) name and a value
			// for a mapping use it.
			// If not use the default value from the mapped
			// InterventionVariableWithValue.
			Variable externalSystemVariableWithInterventionVariableName = new Variable(
					interventionVariable.getName(),
					externalSystemVariable == null
							// Intervention variable value as
							// default
							? interventionVariable.getValue()
							: externalSystemVariable.getValue());

			variables.add(externalSystemVariableWithInterventionVariableName);
		}

		return variables;
	}

}
