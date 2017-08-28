package ch.ethz.mc.services;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.memory.ExternalRegistration;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValuePrivacyTypes;
import ch.ethz.mc.model.rest.CollectionOfExtendedListVariables;
import ch.ethz.mc.model.rest.CollectionOfExtendedVariables;
import ch.ethz.mc.model.rest.ExtendedListVariable;
import ch.ethz.mc.model.rest.ExtendedVariable;
import ch.ethz.mc.model.rest.Variable;
import ch.ethz.mc.model.rest.VariableAverage;
import ch.ethz.mc.model.rest.VariableAverageWithParticipant;
import ch.ethz.mc.services.internal.CommunicationManagerService;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.DeepstreamCommunicationService;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.services.internal.VariablesManagerService.ExternallyReadProtectedVariableException;
import ch.ethz.mc.services.internal.VariablesManagerService.ExternallyWriteProtectedVariableException;

/**
 * Cares for the orchestration of all REST calls
 *
 * @author Andreas Filler
 */
@Log4j2
public class RESTManagerService {
	private final Object							$lock;

	private static RESTManagerService				instance	= null;

	private final DatabaseManagerService			databaseManagerService;
	@Getter
	private final FileStorageManagerService			fileStorageManagerService;
	private final VariablesManagerService			variablesManagerService;

	private final DeepstreamCommunicationService	deepstreamCommunicationService;

	private final String							deepstreamServerRole;
	private final String							deepstreamParticipantRole;
	private final String							deepstreamSuperviserRole;

	private final String							deepstreamServerPassword;

	private RESTManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final CommunicationManagerService communicationManagerService)
			throws Exception {
		$lock = MC.getInstance();

		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.variablesManagerService = variablesManagerService;

		deepstreamCommunicationService = communicationManagerService
				.getDeepstreamCommunicationService();

		deepstreamServerRole = Constants.getDeepstreamServerRole();
		deepstreamParticipantRole = Constants.getDeepstreamParticipantRole();
		deepstreamSuperviserRole = Constants.getDeepstreamSupervisorRole();

		deepstreamServerPassword = Constants.getDeepstreamServerPassword();

		log.info("Started.");
	}

	public static RESTManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final CommunicationManagerService communicationManagerService)
			throws Exception {
		if (instance == null) {
			instance = new RESTManagerService(databaseManagerService,
					fileStorageManagerService, variablesManagerService,
					communicationManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	/**
	 * Checks if variable can be written for the given participant
	 *
	 * @param participantId
	 * @param variable
	 * @return
	 */
	@Synchronized
	public boolean checkVariableForServiceWritingRights(
			final ObjectId participantId, final String variable) {
		return variablesManagerService.checkVariableForServiceWriting(
				participantId, ImplementationConstants.VARIABLE_PREFIX
						+ variable.trim());
	}

	/**
	 * Reads variable for given participant
	 *
	 * @param participantId
	 * @param variable
	 * @param isService
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public Variable readVariable(final ObjectId participantId,
			final String variable, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		log.debug("Try to read variable {} for participant {}", variable,
				participantId);

		try {
			val resultVariable = new Variable(variable,
					getVariableValueOfParticipant(participantId, variable,
							isService));

			log.debug("Returing variable with value {} for participant {}",
					resultVariable, participantId);
			return resultVariable;
		} catch (final Exception e) {
			log.debug("Could not read variable {} for participant {}: {}",
					variable, participantId, e.getMessage());
			throw e;
		}
	}

	/**
	 * Reads variable for all participants of the same group/intervention as the
	 * given participant
	 *
	 * @param participantId
	 * @param variable
	 * @param sameGroup
	 * @param isService
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public CollectionOfExtendedVariables readVariableArrayOfGroupOrIntervention(
			final ObjectId participantId, final String variable,
			final boolean sameGroup, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		log.debug(
				"Try to read variable array {} of participants from the same {} as participant {}",
				variable, sameGroup ? "group" : "intervention", participantId);

		try {
			val collecionOfExtendedVariables = getVariableValueOfParticipantsOfGroupOrIntervention(
					participantId,
					variable,
					sameGroup ? InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP
							: InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION,
					isService);

			log.debug(
					"Returing variables with values {} of participants from the same {} as participant {}",
					collecionOfExtendedVariables, sameGroup ? "group"
							: "intervention", participantId);

			return collecionOfExtendedVariables;
		} catch (final Exception e) {
			log.debug(
					"Could not read variable {} of participants from the same {} as participant {}: {}",
					variable, sameGroup ? "group" : "intervention",
					participantId, variable, participantId, e.getMessage());
			throw e;
		}
	}

	/**
	 * Reads variable for dashboard for all participants of the given
	 * group/intervention
	 *
	 * @param interventionId
	 * @param variable
	 * @param group
	 * @param filterVariable
	 * @param filterValue
	 * @param isService
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public CollectionOfExtendedVariables readVariableArrayForDashboardOfGroupOrIntervention(
			final ObjectId interventionId, final String variable,
			final String group, final String filterVariable,
			final String filterValue, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		log.debug(
				"Try to read variable array {} of participants from {} filtered by {}={}",
				variable, group == null ? "intervention " + interventionId
						: "group " + group, filterVariable, filterValue);

		try {
			val collecionOfExtendedVariables = getVariableValueForDashboardOfParticipantsOfGroupOrIntervention(
					interventionId, variable, group, filterVariable,
					filterValue, isService);

			log.debug(
					"Returing variables with values {} of participants from {}",
					collecionOfExtendedVariables,
					group == null ? "intervention " + interventionId : "group "
							+ group);

			return collecionOfExtendedVariables;
		} catch (final Exception e) {
			log.debug("Could not read variable {} of participants from {}: {}",
					variable, group == null ? "intervention " + interventionId
							: "group " + group, e.getMessage());
			throw e;
		}
	}

	/**
	 * Reads variable for all participants of the same group/intervention as the
	 * given participant
	 *
	 * @param participantId
	 * @param variables
	 * @param sameGroup
	 * @param isService
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public CollectionOfExtendedListVariables readVariableListArrayOfGroupOrIntervention(
			final ObjectId participantId, final List<String> variables,
			final boolean sameGroup, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		log.debug(
				"Try to read variable array {} of participants from the same {} as participant {}",
				variables, sameGroup ? "group" : "intervention", participantId);

		try {
			val collectionOfExtendendListVariables = getVariableValueOfParticipantsOfGroupOrIntervention(
					participantId,
					variables,
					sameGroup ? InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP
							: InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION,
					isService);

			log.debug(
					"Returing variables with values {} of participants from the same {} as participant {}",
					collectionOfExtendendListVariables, sameGroup ? "group"
							: "intervention", participantId);
			return collectionOfExtendendListVariables;
		} catch (final Exception e) {
			log.debug(
					"Could not read variable {} of participants from the same {} as participant {}: {}",
					variables, sameGroup ? "group" : "intervention",
					participantId, variables, participantId, e.getMessage());
			throw e;
		}
	}

	/**
	 * Calculate average of variable for all participants of the same
	 * group/intervention as the
	 * given participant
	 *
	 * @param participantId
	 * @param variable
	 * @param sameGroup
	 * @param isService
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public VariableAverageWithParticipant calculateAverageOfVariableArrayOfGroupOrIntervention(
			final ObjectId participantId, final String variable,
			final boolean sameGroup, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		log.debug(
				"Try to calculate average of variable array {} of participants from the same {} as participant {}",
				variable, sameGroup ? "group" : "intervention", participantId);

		try {
			val variableAverage = new VariableAverageWithParticipant();
			variableAverage.setVariable(variable);

			val resultVariables = readVariableArrayOfGroupOrIntervention(
					participantId, variable, sameGroup, isService);

			try {
				int i = 0;
				double average = 0d;
				for (val resultVariable : resultVariables.getVariables()) {
					i++;
					if (resultVariable.isOwnValue()) {
						variableAverage.setValueOfParticipant(Double
								.parseDouble(resultVariable.getValue()));
					}

					average += Double.parseDouble(resultVariable.getValue());
				}
				variableAverage.setAverage(average / i);
				variableAverage.setSize(i);
			} catch (final Exception e) {
				throw variablesManagerService.new ExternallyReadProtectedVariableException(
						"Variable value can not be interpreted as calculateable value: "
								+ e.getMessage());
			}

			return variableAverage;
		} catch (final Exception e) {
			log.debug(
					"Could not calculate average of variable {} of participants from the same {} as participant {}: {}",
					variable, sameGroup ? "group" : "intervention",
					participantId, variable, participantId, e.getMessage());
			throw e;
		}
	}

	/**
	 * Calculate average of variable for dashboard for all participants of the
	 * given
	 * group/intervention
	 *
	 * @param interventionId
	 * @param variable
	 * @param group
	 * @param isService
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public VariableAverage calculateAverageOfVariableArrayForDashboardOfGroupOrIntervention(
			final ObjectId interventionId, final String variable,
			final String group, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		log.debug(
				"Try to calculate average of variable array {} of participants from {}",
				variable, group == null ? "intervention " + interventionId
						: "group " + group);

		try {
			val variableAverage = new VariableAverage();
			variableAverage.setVariable(variable);

			val resultVariables = readVariableArrayForDashboardOfGroupOrIntervention(
					interventionId, variable, group, null, null, isService);

			try {
				int i = 0;
				double average = 0d;
				for (val resultVariable : resultVariables.getVariables()) {
					i++;
					average += Double.parseDouble(resultVariable.getValue());
				}
				variableAverage.setAverage(average / i);
				variableAverage.setSize(i);
			} catch (final Exception e) {
				throw variablesManagerService.new ExternallyReadProtectedVariableException(
						"Variable value can not be interpreted as calculateable value: "
								+ e.getMessage());
			}

			return variableAverage;
		} catch (final Exception e) {
			log.debug(
					"Could not calculate average of variable {} of participants from {}: {}",
					variable, group == null ? "intervention " + interventionId
							: "group " + group, e.getMessage());
			throw e;
		}
	}

	/**
	 * Writes variable for given participant
	 *
	 * @param participantId
	 * @param variable
	 * @param value
	 * @param isService
	 * @throws ExternallyWriteProtectedVariableException
	 */
	public void writeVariable(final ObjectId participantId,
			final String variable, final String value,
			final boolean describesMediaUpload, final boolean isService)
			throws ExternallyWriteProtectedVariableException {
		log.debug("Try to write variable {} for participant {} with value {}",
				variable, participantId, value);

		try {
			writeVariableValue(participantId, variable, value,
					describesMediaUpload, isService);

			log.debug("Wrote variable {} for participant {}", variable,
					participantId);
		} catch (final Exception e) {
			log.debug(
					"Could not write variable {} for participant {} with value {}: {}",
					variable, participantId, value, e.getMessage());
			throw e;
		}
	}

	/**
	 * Writes voting from given participant for given receiving participant
	 *
	 * @param participantId
	 * @param receivingParticipantId
	 * @param variable
	 * @param addVote
	 * @throws ExternallyWriteProtectedVariableException
	 */
	public void writeVoting(final ObjectId participantId,
			final ObjectId receivingParticipantId, final String variable,
			final boolean addVote)
			throws ExternallyWriteProtectedVariableException {
		log.debug(
				"Try to write voting {} for participant {} from participant {}",
				variable, receivingParticipantId, participantId);

		try {
			writeVotingFromParticipantForParticipant(participantId,
					receivingParticipantId, variable, addVote);

			log.debug("Wrote voting {} for participant {} from participant {}",
					variable, receivingParticipantId, participantId);
		} catch (final Exception e) {
			log.debug(
					"Could not write voting {} for participant {} from participant {}: {}",
					variable, receivingParticipantId, participantId,
					e.getMessage());
			throw e;
		}
	}

	/**
	 * Writes credit for given participant and credit name to given variable
	 *
	 * @param participantId
	 * @param variable
	 * @param creditName
	 * @throws ExternallyWriteProtectedVariableException
	 */
	public void writeCredit(final ObjectId participantId,
			final String variable, final String creditName)
			throws ExternallyWriteProtectedVariableException {
		log.debug("Try to write credit for {} on {} for participant {}",
				creditName, variable, participantId);

		try {
			writeCreditWithNameForParticipantToVariable(participantId,
					creditName, variable);

			log.debug("Wrote credit for {} on {} for participant {}",
					creditName, variable, participantId);
		} catch (final Exception e) {
			log.debug(
					"Could not write credit for {} on {} for participant {}: {}",
					creditName, variable, participantId, e.getMessage());
			throw e;
		}
	}

	/*
	 * Deepstream functions
	 */
	/**
	 * Validates deepstream access for participant/supervisor/server
	 * 
	 * @param user
	 * @param interventionPassword
	 * @param role
	 * @param secret
	 * @return
	 */
	public boolean checkDeepstreamAccessAndRetrieveUserId(final String user,
			final String secret, final String role,
			final String interventionPassword) {

		// Prevent unauthorized access with empty values
		if (StringUtils.isBlank(user) || StringUtils.isBlank(secret)
				|| StringUtils.isBlank(role)
				|| StringUtils.isBlank(interventionPassword)) {
			return false;
		}

		// Check access based on role
		if (role.equals(deepstreamServerRole)) {
			// Check server access
			if (secret.equals(deepstreamServerPassword)) {
				log.debug("Server authorized for deepstream access");
				return true;
			} else {
				log.debug("Server not authorized for deepstream access");
				return false;
			}
		} else if (role.equals(deepstreamParticipantRole)) {
			// Check participant access
			val dialogOption = databaseManagerService
					.findOneModelObject(
							DialogOption.class,
							Queries.DIALOG_OPTION__BY_TYPE_AND_DATA,
							DialogOptionTypes.EXTERNAL_ID,
							ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
									+ user);
			if (dialogOption == null) {
				log.debug(
						"Participant with deepstream id {} not authorized for deepstream access: Dialog option not found",
						user);
				return false;
			}

			val participant = databaseManagerService.getModelObjectById(
					Participant.class, dialogOption.getParticipant());
			if (participant == null) {
				log.debug(
						"Participant with deepstream id {} not authorized for deepstream access: Participant not found",
						user);
				return false;
			}

			val intervention = databaseManagerService.getModelObjectById(
					Intervention.class, participant.getIntervention());
			if (intervention == null || !intervention.isActive()) {
				log.debug(
						"Participant with deepstream id {} not authorized for deepstream access: Intervention not active",
						user);
				return false;
			}
			if (!intervention.getDeepstreamPassword().equals(
					interventionPassword)) {
				log.debug(
						"Participant with deepstream id {} not authorized for deepstream access: Password does not match deepstream intervention password",
						user);
				return false;
			}

			if (deepstreamCommunicationService != null
					&& deepstreamCommunicationService.checkSecret(user, secret)) {
				log.debug(
						"Participant with deepstream id {} authorized for deepstream access",
						user);
				return true;
			} else {
				log.debug(
						"Participant with deepstream id {} not authorized for deepstream access: Wrong secret",
						user);
				return false;
			}
		} else if (role.equals(deepstreamSuperviserRole)) {
			// Check supervisor access
			val dialogOption = databaseManagerService
					.findOneModelObject(
							DialogOption.class,
							Queries.DIALOG_OPTION__BY_TYPE_AND_DATA,
							DialogOptionTypes.SUPERVISOR_EXTERNAL_ID,
							ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
									+ user);
			if (dialogOption == null) {
				log.debug(
						"Supervisor with deepstream id {} not authorized for deepstream access: Dialog option not found",
						user);
				return false;
			}

			val participant = databaseManagerService.getModelObjectById(
					Participant.class, dialogOption.getParticipant());
			if (participant == null) {
				log.debug(
						"Supervisor with deepstream id {} not authorized for deepstream access: Participant not found",
						user);
				return false;
			}

			val intervention = databaseManagerService.getModelObjectById(
					Intervention.class, participant.getIntervention());
			if (intervention == null || !intervention.isActive()) {
				log.debug(
						"Supervisor with deepstream id {} not authorized for deepstream access: Intervention not active",
						user);
				return false;
			}
			if (!intervention.getDeepstreamPassword().equals(
					interventionPassword)) {
				log.debug(
						"Supervisor with deepstream id {} not authorized for deepstream access: Password does not match deepstream intervention password",
						user);
				return false;
			}

			if (deepstreamCommunicationService != null
					&& deepstreamCommunicationService.checkSecret(user, secret)) {
				log.debug(
						"Supervisor with deepstream id {} authorized for deepstream access",
						user);
				return true;
			} else {
				log.debug(
						"Supervisor with deepstream id {} not authorized for deepstream access: Wrong secret",
						user);
				return false;
			}
		} else {
			log.debug("Unauthorized access with wrong role {}", role);
			return false;
		}
	}

	/**
	 * Creates a deepstream participant/supervisor and the belonging
	 * intervention participant structures or returns null if not allowed
	 * 
	 * @param interventionPattern
	 * @param interventionPassword
	 * @param relatedParticipant
	 * @param requestedRole
	 * @return Deepstream user id and secret
	 */
	public ExternalRegistration createDeepstreamUser(final String nickname,
			final String relatedParticipant, final String interventionPattern,
			final String interventionPassword, final String requestedRole) {

		if (StringUtils.isBlank(requestedRole)) {
			return null;
		}

		if (requestedRole.equals(deepstreamParticipantRole)) {
			if (nickname == null) {
				return null;
			}
			if (deepstreamCommunicationService != null) {
				return deepstreamCommunicationService.registerUser(nickname,
						relatedParticipant, interventionPattern,
						interventionPassword, false);
			} else {
				return null;
			}
		} else if (requestedRole.equals(deepstreamSuperviserRole)) {
			if (relatedParticipant == null) {
				return null;
			}
			if (deepstreamCommunicationService != null) {
				return deepstreamCommunicationService.registerUser(nickname,
						relatedParticipant, interventionPattern,
						interventionPassword, true);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Inform {@link DeepstreamCommunicationService} about startup of REST
	 * interface
	 */
	public void informDeepstreamAboutStartup() {
		if (deepstreamCommunicationService != null) {
			deepstreamCommunicationService.RESTInterfaceStarted(this);
		}
	}

	/*
	 * Internal helpers
	 */
	/**
	 * Reads variable for given participant
	 *
	 * @param participantId
	 * @param variable
	 * @param isService
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	@Synchronized
	private String getVariableValueOfParticipant(final ObjectId participantId,
			final String variable, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		return variablesManagerService
				.externallyReadVariableValueForParticipant(participantId,
						ImplementationConstants.VARIABLE_PREFIX + variable,
						InterventionVariableWithValuePrivacyTypes.PRIVATE,
						isService);
	}

	/**
	 * Reads variable for all participants of the same group/intervention as the
	 * given
	 * participant
	 *
	 * @param participantId
	 * @param variables
	 * @param requestPrivacyType
	 * @param isService
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	@Synchronized
	private CollectionOfExtendedVariables getVariableValueOfParticipantsOfGroupOrIntervention(
			final ObjectId participantId, final String variable,
			final InterventionVariableWithValuePrivacyTypes requestPrivacyType,
			final boolean isService)
			throws ExternallyReadProtectedVariableException {
		val participant = databaseManagerService.getModelObjectById(
				Participant.class, participantId);

		if (participant == null) {
			throw variablesManagerService.new ExternallyReadProtectedVariableException(
					"The given participant does not exist anymore, so the variables cannot be read");
		}

		val interventionId = participant.getIntervention();
		val group = participant.getGroup();

		if (group == null) {
			throw variablesManagerService.new ExternallyReadProtectedVariableException(
					"The given participant does not belong to a group, so the variable cannot be read");
		}

		val collectionOfExtendedResultVariables = new CollectionOfExtendedVariables();
		val resultVariables = collectionOfExtendedResultVariables
				.getVariables();

		switch (requestPrivacyType) {
			case PRIVATE:
				resultVariables.add(new ExtendedVariable(variable,
						getVariableValueOfParticipant(participantId, variable,
								isService), participant.getId().toHexString(),
						true, null));
				break;
			case SHARED_WITH_GROUP:
				Iterable<Participant> relevantParticipants = databaseManagerService
						.findModelObjects(
								Participant.class,
								Queries.PARTICIPANT__BY_INTERVENTION_AND_GROUP_AND_MONITORING_ACTIVE_TRUE,
								interventionId, group);

				for (val relevantParticipant : relevantParticipants) {
					val dialogStatus = databaseManagerService
							.findOneModelObject(DialogStatus.class,
									Queries.DIALOG_STATUS__BY_PARTICIPANT,
									relevantParticipant.getId());

					if (dialogStatus != null
							&& dialogStatus.isScreeningSurveyPerformed()
							&& dialogStatus
									.isDataForMonitoringParticipationAvailable()) {
						final ExtendedVariable variableWithValue = new ExtendedVariable(
								variable,
								variablesManagerService
										.externallyReadVariableValueForParticipant(
												relevantParticipant.getId(),
												ImplementationConstants.VARIABLE_PREFIX
														+ variable,
												InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP,
												isService), relevantParticipant
										.getId().toHexString(),
								participantId.equals(relevantParticipant
										.getId()), null);

						resultVariables.add(variableWithValue);
					}
				}
				break;
			case SHARED_WITH_INTERVENTION:
			case SHARED_WITH_INTERVENTION_AND_DASHBOARD:
				relevantParticipants = databaseManagerService
						.findModelObjects(
								Participant.class,
								Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
								interventionId);

				for (val relevantParticipant : relevantParticipants) {
					val dialogStatus = databaseManagerService
							.findOneModelObject(DialogStatus.class,
									Queries.DIALOG_STATUS__BY_PARTICIPANT,
									relevantParticipant.getId());

					if (dialogStatus != null
							&& dialogStatus.isScreeningSurveyPerformed()
							&& dialogStatus
									.isDataForMonitoringParticipationAvailable()) {
						final ExtendedVariable variableWithValue = new ExtendedVariable(
								variable,
								variablesManagerService
										.externallyReadVariableValueForParticipant(
												relevantParticipant.getId(),
												ImplementationConstants.VARIABLE_PREFIX
														+ variable,
												InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION,
												isService), relevantParticipant
										.getId().toHexString(),
								participantId.equals(relevantParticipant
										.getId()), null);

						resultVariables.add(variableWithValue);
					}
				}
				break;
		}

		collectionOfExtendedResultVariables.setSize(resultVariables.size());
		Collections.shuffle(resultVariables);

		return collectionOfExtendedResultVariables;
	}

	/**
	 * Reads variable for dashboard for all participants of the given
	 * group/intervention (with variable based filter if applied)
	 *
	 * @param interventionId
	 * @param variable
	 * @param group
	 * @param filterVariable
	 * @param filterValue
	 * @return
	 */
	@Synchronized
	private CollectionOfExtendedVariables getVariableValueForDashboardOfParticipantsOfGroupOrIntervention(
			final ObjectId interventionId, final String variable,
			final String group, final String filterVariable,
			final String filterValue, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		val intervention = databaseManagerService.getModelObjectById(
				Intervention.class, interventionId);

		if (intervention == null) {
			throw variablesManagerService.new ExternallyReadProtectedVariableException(
					"The given intervention does not exist anymore, so the variables cannot be read");
		}

		val collectionOfExtendedResultVariables = new CollectionOfExtendedVariables();
		val resultVariables = collectionOfExtendedResultVariables
				.getVariables();

		if (group != null) {
			final Iterable<Participant> relevantParticipants = databaseManagerService
					.findModelObjects(
							Participant.class,
							Queries.PARTICIPANT__BY_INTERVENTION_AND_GROUP_AND_MONITORING_ACTIVE_TRUE,
							interventionId, group);

			for (val relevantParticipant : relevantParticipants) {
				val dialogStatus = databaseManagerService.findOneModelObject(
						DialogStatus.class,
						Queries.DIALOG_STATUS__BY_PARTICIPANT,
						relevantParticipant.getId());

				if (dialogStatus != null
						&& dialogStatus.isScreeningSurveyPerformed()
						&& dialogStatus
								.isDataForMonitoringParticipationAvailable()) {

					// Check filter
					if (filterVariable != null && filterValue != null) {
						val userFilterVariable = variablesManagerService
								.externallyReadVariableValueForParticipant(
										relevantParticipant.getId(),
										ImplementationConstants.VARIABLE_PREFIX
												+ filterVariable,
										InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION_AND_DASHBOARD,
										isService);

						if (userFilterVariable == null
								|| !userFilterVariable.equals(filterValue)) {
							continue;
						}
					}

					final ExtendedVariable variableWithValue = new ExtendedVariable(
							variable,
							variablesManagerService.externallyReadVariableValueForParticipant(
									relevantParticipant.getId(),
									ImplementationConstants.VARIABLE_PREFIX
											+ variable,
									InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION_AND_DASHBOARD,
									isService), relevantParticipant.getId()
									.toHexString(), null, null);

					resultVariables.add(variableWithValue);
				}
			}
		} else {
			final Iterable<Participant> relevantParticipants = databaseManagerService
					.findModelObjects(
							Participant.class,
							Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
							interventionId);

			for (val relevantParticipant : relevantParticipants) {
				val dialogStatus = databaseManagerService.findOneModelObject(
						DialogStatus.class,
						Queries.DIALOG_STATUS__BY_PARTICIPANT,
						relevantParticipant.getId());

				if (dialogStatus != null
						&& dialogStatus.isScreeningSurveyPerformed()
						&& dialogStatus
								.isDataForMonitoringParticipationAvailable()) {

					// Check filter
					if (filterVariable != null && filterValue != null) {
						val userFilterVariable = variablesManagerService
								.externallyReadVariableValueForParticipant(
										relevantParticipant.getId(),
										ImplementationConstants.VARIABLE_PREFIX
												+ filterVariable,
										InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION_AND_DASHBOARD,
										isService);

						if (userFilterVariable == null
								|| !userFilterVariable.equals(filterValue)) {
							continue;
						}
					}

					final ExtendedVariable variableWithValue = new ExtendedVariable(
							variable,
							variablesManagerService.externallyReadVariableValueForParticipant(
									relevantParticipant.getId(),
									ImplementationConstants.VARIABLE_PREFIX
											+ variable,
									InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION_AND_DASHBOARD,
									isService), relevantParticipant.getId()
									.toHexString(), null, null);

					resultVariables.add(variableWithValue);
				}
			}
		}

		collectionOfExtendedResultVariables.setSize(resultVariables.size());
		Collections.shuffle(resultVariables);

		return collectionOfExtendedResultVariables;
	}

	/**
	 * Reads variables for all participants of the same group/intervetion as the
	 * given
	 * participant
	 *
	 * @param participantId
	 * @param variables
	 * @param requestPrivacyType
	 * @param isService
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	@Synchronized
	private CollectionOfExtendedListVariables getVariableValueOfParticipantsOfGroupOrIntervention(
			final ObjectId participantId, final List<String> variables,
			final InterventionVariableWithValuePrivacyTypes requestPrivacyType,
			final boolean isService)
			throws ExternallyReadProtectedVariableException {
		val participant = databaseManagerService.getModelObjectById(
				Participant.class, participantId);

		if (participant == null) {
			throw variablesManagerService.new ExternallyReadProtectedVariableException(
					"The given participant does not exist anymore, so the variables cannot be read");
		}

		val interventionId = participant.getIntervention();
		val group = participant.getGroup();

		if (group == null) {
			throw variablesManagerService.new ExternallyReadProtectedVariableException(
					"The given participant does not belong to a group, so the variable cannot be read");
		}

		val collectionOfExtendedListResultVariables = new CollectionOfExtendedListVariables();
		val resultVariables = collectionOfExtendedListResultVariables
				.getVariableListing();

		switch (requestPrivacyType) {
			case PRIVATE:
				ExtendedListVariable extendedListVariable = new ExtendedListVariable(
						participant.getId().toHexString(), true);

				for (val variable : variables) {
					extendedListVariable
							.getVariables()
							.add(new Variable(variable,
									getVariableValueOfParticipant(
											participantId, variable, isService)));
				}

				resultVariables.add(extendedListVariable);
				break;
			case SHARED_WITH_GROUP:
				Iterable<Participant> relevantParticipants = databaseManagerService
						.findModelObjects(
								Participant.class,
								Queries.PARTICIPANT__BY_INTERVENTION_AND_GROUP_AND_MONITORING_ACTIVE_TRUE,
								interventionId, group);

				for (val relevantParticipant : relevantParticipants) {
					val dialogStatus = databaseManagerService
							.findOneModelObject(DialogStatus.class,
									Queries.DIALOG_STATUS__BY_PARTICIPANT,
									relevantParticipant.getId());

					if (dialogStatus != null
							&& dialogStatus.isScreeningSurveyPerformed()
							&& dialogStatus
									.isDataForMonitoringParticipationAvailable()) {
						extendedListVariable = new ExtendedListVariable(
								relevantParticipant.getId().toHexString(),
								participantId.equals(relevantParticipant
										.getId()));

						for (val variable : variables) {
							extendedListVariable
									.getVariables()
									.add(new Variable(
											variable,
											variablesManagerService
													.externallyReadVariableValueForParticipant(
															relevantParticipant
																	.getId(),
															ImplementationConstants.VARIABLE_PREFIX
																	+ variable,
															InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP,
															isService)));
						}
						resultVariables.add(extendedListVariable);
					}
				}
				break;
			case SHARED_WITH_INTERVENTION:
			case SHARED_WITH_INTERVENTION_AND_DASHBOARD:
				relevantParticipants = databaseManagerService
						.findModelObjects(
								Participant.class,
								Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
								interventionId);

				for (val relevantParticipant : relevantParticipants) {
					val dialogStatus = databaseManagerService
							.findOneModelObject(DialogStatus.class,
									Queries.DIALOG_STATUS__BY_PARTICIPANT,
									relevantParticipant.getId());

					if (dialogStatus != null
							&& dialogStatus.isScreeningSurveyPerformed()
							&& dialogStatus
									.isDataForMonitoringParticipationAvailable()) {
						extendedListVariable = new ExtendedListVariable(
								relevantParticipant.getId().toHexString(),
								participantId.equals(relevantParticipant
										.getId()));

						for (val variable : variables) {
							extendedListVariable
									.getVariables()
									.add(new Variable(
											variable,
											variablesManagerService
													.externallyReadVariableValueForParticipant(
															relevantParticipant
																	.getId(),
															ImplementationConstants.VARIABLE_PREFIX
																	+ variable,
															InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION,
															isService)));
						}
						resultVariables.add(extendedListVariable);
					}
				}
				break;
		}

		collectionOfExtendedListResultVariables.setSize(resultVariables.size());
		Collections.shuffle(resultVariables);

		return collectionOfExtendedListResultVariables;
	}

	/**
	 * Writes variable for given participant
	 *
	 * @param participantId
	 * @param variable
	 * @param value
	 * @param describesMediaUpload
	 * @param isService
	 * @throws ExternallyWriteProtectedVariableException
	 */
	@Synchronized
	private void writeVariableValue(final ObjectId participantId,
			final String variable, final String value,
			final boolean describesMediaUpload, final boolean isService)
			throws ExternallyWriteProtectedVariableException {
		variablesManagerService.externallyWriteVariableForParticipant(
				participantId, ImplementationConstants.VARIABLE_PREFIX
						+ variable, value, describesMediaUpload, isService);
	}

	/**
	 * Writes voting from given participant for given receiving participant
	 *
	 * @param participantId
	 * @param receivingParticipantId
	 * @param variable
	 * @param addVote
	 * @throws ExternallyWriteProtectedVariableException
	 */
	@Synchronized
	private void writeVotingFromParticipantForParticipant(
			final ObjectId participantId,
			final ObjectId receivingParticipantId, final String variable,
			final boolean addVote)
			throws ExternallyWriteProtectedVariableException {
		variablesManagerService
				.serviceWriteVotingFromParticipantForParticipant(participantId,
						receivingParticipantId,
						ImplementationConstants.VARIABLE_PREFIX + variable,
						addVote);
	}

	/**
	 * Writes credit for given participant and credit name to given variable
	 *
	 * @param participantId
	 * @param creditName
	 * @param variable
	 * @throws ExternallyWriteProtectedVariableException
	 */
	@Synchronized
	private void writeCreditWithNameForParticipantToVariable(
			final ObjectId participantId, final String creditName,
			final String variable)
			throws ExternallyWriteProtectedVariableException {
		variablesManagerService
				.serviceWriteCreditWithNameForParticipantToVariable(
						participantId, creditName,
						ImplementationConstants.VARIABLE_PREFIX + variable);
	}
}
