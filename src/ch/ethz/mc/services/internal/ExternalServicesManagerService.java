package ch.ethz.mc.services.internal;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.memory.ExternalRegistration;
import ch.ethz.mc.model.memory.ExternalServiceMessage;
import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.InterventionExternalService;
import ch.ethz.mc.model.persistent.InterventionExternalServiceFieldVariableMapping;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.model.rest.Variable;
import ch.ethz.mc.tools.InternalDateTime;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Manages all defined external services for a specific intervention.
 *
 * @author Marcel Schlegel
 */
@Log4j2
public class ExternalServicesManagerService {

	@Getter
	private static ExternalServicesManagerService	instance	= null;

	private final DatabaseManagerService			databaseManagerService;
	private final DeepstreamCommunicationService	deepstreamCommunicationService;

	private ExternalServicesManagerService(
			final DatabaseManagerService databaseManagerService,
			final DeepstreamCommunicationService deepstreamCommunicationService) {

		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.deepstreamCommunicationService = deepstreamCommunicationService;

		log.info("Started.");
	}

	public static ExternalServicesManagerService start(
			final DatabaseManagerService databaseManagerService,
			final DeepstreamCommunicationService deepstreamCommunicationService)
			throws Exception {
		if (instance == null) {
			instance = new ExternalServicesManagerService(
					databaseManagerService, deepstreamCommunicationService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	public ExternalRegistration createExternalServiceForDeepstream(
			final String serviceName) {

		val serviceRegistration = deepstreamCommunicationService
				.registerExternalService(serviceName);

		return serviceRegistration;
	}

	public void deleteExternalServiceOnDeepstream(
			final InterventionExternalService externalService) {
		deepstreamCommunicationService.deleteExternalService(externalService);
	}

	public String renewToken(
			final InterventionExternalService externalService) {
		return deepstreamCommunicationService
				.renewExternalServiceToken(externalService);
	}

	public boolean getReceivedMessages(
			final ArrayList<ReceivedMessage> receivedMessages) {

		List<ExternalServiceMessage> externalServiceMessages = new ArrayList<>();
		deepstreamCommunicationService
				.getReceivedExternalServiceMessages(externalServiceMessages);

		for (ExternalServiceMessage externalServiceMessage : externalServiceMessages) {

			val externalService = databaseManagerService.findOneModelObject(
					InterventionExternalService.class,
					Queries.INTERVENTION_EXTERNAL_SERVICE__BY_SERVICE_ID,
					externalServiceMessage.getServiceId());
			if (externalService == null) {
				log.error(
						"There exists no external service with service id {}. Message can not be processed.",
						externalServiceMessage.getServiceId());
				continue;
			}
			if (externalServiceMessage.getParticipants().isEmpty()) {
				val participants = databaseManagerService.findModelObjects(
						Participant.class, Queries.PARTICIPANT__BY_INTERVENTION,
						externalService.getIntervention());
				participants.forEach(participant -> externalServiceMessage
						.addParticipant(participant.getId().toString()));
			}

			for (String participantId : externalServiceMessage
					.getParticipants()) {

				if (!ObjectId.isValid(participantId)) {
					log.warn(
							"Participant id {} is not valid. Message with service id {} can not be processed for participant id {}.",
							participantId, externalService.getServiceId(),
							participantId);
					continue;
				}
				val participant = databaseManagerService.getModelObjectById(
						Participant.class, new ObjectId(participantId));

				if (participant == null) {
					log.warn(
							"Participant with id {} not found. Message with service id {} can not be processed for participant id {}.",
							participantId, externalService.getServiceId(),
							participantId);
					continue;
				}

				if (!participant.getIntervention()
						.equals(externalService.getIntervention())) {
					log.warn(
							"Participant with id {} is not in the same intervention as the external service. Message with service id {} can not be processed for participant id {}.",
							participantId, externalService.getServiceId(),
							participantId);
					continue;
				}

				val receivedMessage = new ReceivedMessage();

				val variableMappings = databaseManagerService.findModelObjects(
						InterventionExternalServiceFieldVariableMapping.class,
						Queries.INTERVENTION_EXTERNAL_SERVICE_FIELD_VARIABLE_MAPPING__BY_INTERVENTION_EXTERNAL_SERVICE,
						externalService.getId());

				for (InterventionExternalServiceFieldVariableMapping variableMapping : variableMappings) {

					val interventionVariable = databaseManagerService
							.getModelObjectById(
									InterventionVariableWithValue.class,
									variableMapping
											.getInterventionVariableWithValue());

					if (interventionVariable == null) {
						log.warn(
								"Mapped intervention variable for field {} not found. Mapping is ignored for message with service id {}.",
								variableMapping.getJsonFieldName(),
								externalService.getServiceId());
						continue;
					}

					val externalServiceVariable = externalServiceMessage
							.getVariables()
							.get(variableMapping.getJsonFieldName());

					Variable externalServiceVariableWithInterventionVariableName = new Variable(
							interventionVariable.getName(),
							externalServiceVariable == null
									// Intervention variable value as
									// default
									? interventionVariable.getValue()
									: externalServiceVariable.getValue());

					receivedMessage.addExternalServiceVariable(
							externalServiceVariableWithInterventionVariableName);
				}

				val dialogOptions = databaseManagerService.findModelObjects(
						DialogOption.class,
						Queries.DIALOG_OPTION__BY_PARTICIPANT,
						participant.getId());
				receivedMessage.setTypeIntention(false);
				receivedMessage.setRelatedMessageIdBasedOnOrder(-1);
				receivedMessage.setReceivedTimestamp(
						InternalDateTime.currentTimeMillis());
				receivedMessage.setExternalServiceId(
						externalServiceMessage.getServiceId());
				receivedMessage.setExternalService(true);
				receivedMessage.setMessage("");

				for (DialogOption dialogOption : dialogOptions) {
					receivedMessage.setSender(dialogOption.getData());

					if (dialogOption
							.getType() == DialogOptionTypes.EXTERNAL_ID) {
						receivedMessage.setType(DialogOptionTypes.EXTERNAL_ID);
						break;
					} else if (dialogOption
							.getType() == DialogOptionTypes.SUPERVISOR_EXTERNAL_ID) {
						receivedMessage.setType(
								DialogOptionTypes.SUPERVISOR_EXTERNAL_ID);
						break;
					} else {
						receivedMessage.setType(dialogOption.getType());
					}
				}
				receivedMessages.add(receivedMessage);
			}
		}
		return !externalServiceMessages.isEmpty();
	}

}
