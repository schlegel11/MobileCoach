package ch.ethz.mc.services;

/* ##LICENSE## */
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.memory.ExternalRegistration;
import ch.ethz.mc.model.persistent.BackendUser;
import ch.ethz.mc.model.persistent.BackendUserInterventionAccess;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.InterventionExternalSystem;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ParticipantVariableWithValue;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValuePrivacyTypes;
import ch.ethz.mc.model.rest.CollectionOfExtendedListVariables;
import ch.ethz.mc.model.rest.CollectionOfExtendedVariables;
import ch.ethz.mc.model.rest.CollectionOfVariablesWithTimestamp;
import ch.ethz.mc.model.rest.ExtendedListVariable;
import ch.ethz.mc.model.rest.ExtendedVariable;
import ch.ethz.mc.model.rest.Variable;
import ch.ethz.mc.model.rest.VariableAverage;
import ch.ethz.mc.model.rest.VariableAverageWithParticipant;
import ch.ethz.mc.model.rest.VariableWithTimestamp;
import ch.ethz.mc.services.internal.CommunicationManagerService;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.DeepstreamCommunicationService;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.services.internal.VariablesManagerService.ExternallyReadProtectedVariableException;
import ch.ethz.mc.services.internal.VariablesManagerService.ExternallyWriteProtectedVariableException;
import ch.ethz.mc.tools.BCrypt;
import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Cares for the orchestration of all REST calls
 *
 * @author Andreas Filler
 */
@Log4j2
public class RESTManagerService extends Thread {
	private final Object							$lock;

	private static RESTManagerService				instance	= null;

	private boolean									running		= true;
	private boolean									shouldStop	= false;

	private final ConcurrentHashMap<String, String>	externalParticipantsTokenHashMap;

	private final DatabaseManagerService			databaseManagerService;
	@Getter
	private final FileStorageManagerService			fileStorageManagerService;
	private final VariablesManagerService			variablesManagerService;

	private final DeepstreamCommunicationService	deepstreamCommunicationService;

	private final String							deepstreamServerRole;
	private final String							deepstreamParticipantRole;
	private final String							deepstreamSuperviserRole;
	private final String							deepstreamTeamManagerRole;
	private final String							deepstreamObserverRole;
	private final String							deepstreamExternalServiceRole;
	private final int								deepstreamMinClientVersion;
	private final int								deepstreamMaxClientVersion;

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

		externalParticipantsTokenHashMap = new ConcurrentHashMap<String, String>();

		deepstreamCommunicationService = communicationManagerService
				.getDeepstreamCommunicationService();

		deepstreamServerRole = ImplementationConstants.DEEPSTREAM_SERVER_ROLE;
		deepstreamParticipantRole = ImplementationConstants.DEEPSTREAM_PARTICIPANT_ROLE;
		deepstreamSuperviserRole = ImplementationConstants.DEEPSTREAM_SUPERVISOR_ROLE;
		deepstreamTeamManagerRole = ImplementationConstants.DEEPSTREAM_TEAM_MANAGER_ROLE;
		deepstreamObserverRole = ImplementationConstants.DEEPSTREAM_OBSERVER_ROLE;
		deepstreamExternalServiceRole = ImplementationConstants.DEEPSTREAM_EXTERNAL_SYSTEM_ROLE;
		deepstreamMinClientVersion = Constants.getDeepstreamMinClientVersion();
		deepstreamMaxClientVersion = Constants.getDeepstreamMaxClientVersion();

		deepstreamServerPassword = Constants.getDeepstreamServerPassword();

		log.info("Started.");
	}

	@Synchronized
	public static RESTManagerService startThreadedService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final CommunicationManagerService communicationManagerService)
			throws Exception {
		if (instance == null) {
			instance = new RESTManagerService(databaseManagerService,
					fileStorageManagerService, variablesManagerService,
					communicationManagerService);

			instance.setName(RESTManagerService.class.getSimpleName());
			instance.start();
		}

		return instance;
	}

	@Override
	public void run() {
		while (!shouldStop) {
			try {
				sleep(500);
			} catch (final InterruptedException e) {
				// Do nothing
			}
		}

		running = false;
	}

	@Synchronized
	public void stopThreadedService() throws Exception {
		log.info("Stopping service...");

		shouldStop = true;
		interrupt();

		while (running) {
			sleep(500);
		}

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
				participantId,
				ImplementationConstants.VARIABLE_PREFIX + variable.trim());
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
			val collectionOfExtendedVariables = getVariableValueOfParticipantsOfGroupOrIntervention(
					participantId, variable,
					sameGroup
							? InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP
							: InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION,
					isService);

			log.debug(
					"Returing variables with values {} of participants from the same {} as participant {}",
					collectionOfExtendedVariables,
					sameGroup ? "group" : "intervention", participantId);

			return collectionOfExtendedVariables;
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
						: "group " + group,
				filterVariable, filterValue);

		try {
			val collectionOfExtendedVariables = getVariableValueForDashboardOrExternalOfParticipantsOfGroupOrIntervention(
					interventionId, variable, group, filterVariable,
					filterValue, isService);

			log.debug(
					"Returing variables with values {} of participants from {}",
					collectionOfExtendedVariables,
					group == null ? "intervention " + interventionId
							: "group " + group);

			return collectionOfExtendedVariables;
		} catch (final Exception e) {
			log.debug("Could not read variable {} of participants from {}: {}",
					variable, group == null ? "intervention " + interventionId
							: "group " + group,
					e.getMessage());
			throw e;
		}
	}

	/**
	 * Reads variable for external for all participants of the given
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
	public CollectionOfExtendedVariables readVariableArrayForExternalOfGroupOrIntervention(
			final ObjectId interventionId, final String variable,
			final String group, final String filterVariable,
			final String filterValue, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		log.debug(
				"Try to read variable array {} of participants from {} filtered by {}={}",
				variable, group == null ? "intervention " + interventionId
						: "group " + group,
				filterVariable, filterValue);

		try {
			val collectionOfExtendedVariables = getVariableValueForDashboardOrExternalOfParticipantsOfGroupOrIntervention(
					interventionId, variable, group, filterVariable,
					filterValue, isService);

			log.debug(
					"Returing variables with values {} of participants from {}",
					collectionOfExtendedVariables,
					group == null ? "intervention " + interventionId
							: "group " + group);

			return collectionOfExtendedVariables;
		} catch (final Exception e) {
			log.debug("Could not read variable {} of participants from {}: {}",
					variable, group == null ? "intervention " + interventionId
							: "group " + group,
					e.getMessage());
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
			val collectionOfExtendedListVariables = getVariableValueOfParticipantsOfGroupOrIntervention(
					participantId, variables,
					sameGroup
							? InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP
							: InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION,
					isService);

			log.debug(
					"Returing variables with values {} of participants from the same {} as participant {}",
					collectionOfExtendedListVariables,
					sameGroup ? "group" : "intervention", participantId);

			return collectionOfExtendedListVariables;
		} catch (final Exception e) {
			log.debug(
					"Could not read variables {} of participants from the same {} as participant {}: {}",
					variables, sameGroup ? "group" : "intervention",
					participantId, variables, participantId, e.getMessage());
			throw e;
		}
	}

	/**
	 * Reads variable for external for all participants of the given
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
	public CollectionOfExtendedListVariables readVariableListArrayForExternalOfGroupOrIntervention(
			final ObjectId interventionId, final List<String> variables,
			final String group, final String filterVariable,
			final String filterValue, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		log.debug(
				"Try to read variables list array {} of participants from {} filtered by {}={}",
				variables, group == null ? "intervention " + interventionId
						: "group " + group,
				filterVariable, filterValue);

		try {
			val collectionOfExtendedListVariables = getVariableValueForDashboardOrExternalOfParticipantsOfGroupOrIntervention(
					interventionId, variables, group, filterVariable,
					filterValue, isService);

			log.debug(
					"Returing variables with values {} of participants from {}",
					collectionOfExtendedListVariables,
					group == null ? "intervention " + interventionId
							: "group " + group);

			return collectionOfExtendedListVariables;
		} catch (final Exception e) {
			log.debug("Could not read variables {} of participants from {}: {}",
					variables, group == null ? "intervention " + interventionId
							: "group " + group,
					e.getMessage());
			throw e;
		}
	}

	/**
	 * Calculate average of variable for all participants of the same
	 * group/intervention as the given participant
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
						variableAverage.setValueOfParticipant(
								Double.parseDouble(resultVariable.getValue()));
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
	 * given group/intervention
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
							: "group " + group,
					e.getMessage());
			throw e;
		}
	}

	/**
	 * Calculate average of variable for external for all participants of the
	 * given group/intervention
	 *
	 * @param interventionId
	 * @param variable
	 * @param group
	 * @param isService
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public VariableAverage calculateAverageOfVariableArrayForExternalOfGroupOrIntervention(
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
							: "group " + group,
					e.getMessage());
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
	public void writeCredit(final ObjectId participantId, final String variable,
			final String creditName)
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
	 * Access management functions
	 */
	/**
	 * Creates a token for a specific external participant
	 * 
	 * @param externalParticipantId
	 * @return
	 */
	public String createParticipantToken(final String externalParticipantId) {
		synchronized (externalParticipantsTokenHashMap) {
			if (externalParticipantsTokenHashMap
					.containsKey(externalParticipantId)) {
				return externalParticipantsTokenHashMap
						.get(externalParticipantId);
			} else {
				val token = RandomStringUtils.randomAlphanumeric(128);

				externalParticipantsTokenHashMap.put(externalParticipantId,
						token);

				return token;
			}
		}
	}

	/**
	 * Destroys a token for a specific external participant
	 * 
	 * @param externalParticipantId
	 */
	public void destroyParticipantToken(final String externalParticipantId) {
		externalParticipantsTokenHashMap.remove(externalParticipantId);
	}

	/**
	 * Check external participant and token and return {@link ObjectId} of
	 * participant if token fits to logged in participant; otherwise return null
	 * 
	 * @param externalParticipantId
	 * @param token
	 * @return
	 */
	public ObjectId checkExternalParticipantAccessAndReturnParticipantId(
			final String externalParticipantId, final String token) {
		val tokenToCheck = externalParticipantsTokenHashMap
				.get(externalParticipantId);

		if (tokenToCheck == null || !tokenToCheck.equals(token)) {
			return null;
		}

		// Check participant access
		val dialogOption = databaseManagerService.findOneModelObject(
				DialogOption.class, Queries.DIALOG_OPTION__BY_TYPE_AND_DATA,
				DialogOptionTypes.EXTERNAL_ID, externalParticipantId);

		if (dialogOption == null) {
			log.debug(
					"Participant with external participant id {} not authorized for REST access: Dialog option not found",
					externalParticipantId);
			return null;
		}

		// Check based on type
		if (externalParticipantId.startsWith(
				ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM)) {
			if (deepstreamCommunicationService == null
					|| !deepstreamCommunicationService
							.checkIfParticipantIsConnected(
									externalParticipantId.substring(
											ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
													.length()))) {
				log.debug(
						"Participant with external participant id {} is currently not connected using deepstream",
						externalParticipantId);
				return null;
			}
		}

		return dialogOption.getParticipant();
	}

	/**
	 * Checks the access rights of the given {@link BackendUser} for the given
	 * group and intervention and returns {@link BackendUserInterventionAccess},
	 * otherwise null
	 * 
	 * @param username
	 * @param password
	 * @param group
	 * @param interventionPattern
	 * @return
	 */
	public BackendUserInterventionAccess checkExternalBackendUserInterventionAccess(
			final String username, final String password, final String group,
			final String interventionPattern) {

		// Prevent unauthorized access with empty values
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)
				|| StringUtils.isBlank(group)
				|| StringUtils.isBlank(interventionPattern)) {
			return null;
		}

		// Find backend user and check password
		val backendUser = databaseManagerService.findOneModelObject(
				BackendUser.class, Queries.BACKEND_USER__BY_USERNAME, username);

		if (backendUser == null) {
			log.debug("Username '{}' not found.", username);
			return null;
		}

		if (!backendUser.hasDashboardBackendAccess()) {
			log.debug("Username '{}' has no access to the dashboard backend.");
			return null;
		}

		if (!BCrypt.checkpw(password, backendUser.getPasswordHash())) {
			log.debug("Wrong password provided");
			return null;
		}

		// Find backend user intervention access
		val backendUserInterventionenAccesses = databaseManagerService
				.findModelObjects(BackendUserInterventionAccess.class,
						Queries.BACKEND_USER_INTERVENTION_ACCESS__BY_BACKEND_USER,
						backendUser.getId());

		BackendUserInterventionAccess approvedBackendUserInterventionAccess = null;

		for (val backendUserInterventionAccess : backendUserInterventionenAccesses) {
			val intervention = databaseManagerService.getModelObjectById(
					Intervention.class,
					backendUserInterventionAccess.getIntervention());

			if (intervention == null || !intervention.isActive()) {
				continue;
			}

			if (intervention.getName().matches(interventionPattern) && group
					.matches(backendUserInterventionAccess.getGroupPattern())) {
				approvedBackendUserInterventionAccess = backendUserInterventionAccess;
				break;
			}
		}

		return approvedBackendUserInterventionAccess;
	}

	/**
	 * Returns all media uploads of the given participant or null if the
	 * {@link Participant} does not fit to the given intervention or group
	 * 
	 * @param intervention
	 * @param group
	 * @param participant
	 * @return
	 */
	public CollectionOfVariablesWithTimestamp readUploadsOfParticipantIfPartOfInterventionAndGroup(
			final ObjectId interventionId, final String group,
			final ObjectId participantId) {

		val participant = databaseManagerService
				.getModelObjectById(Participant.class, participantId);

		if (participant == null
				|| !participant.getIntervention().equals(interventionId)
				|| !participant.getGroup().equals(group)) {
			return null;
		}

		val collectionOfVariablesWithTimestamp = new CollectionOfVariablesWithTimestamp();
		val variables = collectionOfVariablesWithTimestamp.getVariables();

		val variablesWithValues = databaseManagerService.findModelObjects(
				ParticipantVariableWithValue.class,
				Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_DESCRIBES_MEDIA_UPLOAD_OR_FORMER_VALUE_DESCRIBES_MEDIA_UPLOAD,
				participantId, true, true);

		for (val variableWithValue : variablesWithValues) {
			if (variableWithValue.isDescribesMediaUpload()) {
				variables.add(new VariableWithTimestamp(
						variableWithValue.getName().substring(1),
						variableWithValue.getValue(),
						variableWithValue.getTimestamp()));
			}

			for (val formerValue : variableWithValue
					.getFormerVariableValues()) {
				if (formerValue.isDescribesMediaUpload()) {
					variables.add(new VariableWithTimestamp(
							variableWithValue.getName().substring(1),
							formerValue.getValue(),
							formerValue.getTimestamp()));
				}
			}
		}

		collectionOfVariablesWithTimestamp.setSize(variables.size());

		return collectionOfVariablesWithTimestamp;
	}

	/*
	 * Deepstream functions
	 */
	/**
	 * Validates deepstream access for participant/supervisor/server
	 * 
	 * @param username
	 * @param interventionPassword
	 * @param role
	 * @param secret
	 * @return
	 */
	public boolean checkDeepstreamAccess(final int clientVersion,
			final String username, final String secret, final String role,
			final String interventionPassword) {

		// Prevent access for too old or new clients
		if (clientVersion < deepstreamMinClientVersion
				|| clientVersion > deepstreamMaxClientVersion) {
			return false;
		}

		// Prevent unauthorized access with empty values
		if (StringUtils.isBlank(username) || StringUtils.isBlank(secret)
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
		} else if (role.equals(deepstreamParticipantRole)
				|| role.equals(deepstreamTeamManagerRole)
				|| role.equals(deepstreamObserverRole)) {
			// Check participant or observer access
			val dialogOption = databaseManagerService.findOneModelObject(
					DialogOption.class, Queries.DIALOG_OPTION__BY_TYPE_AND_DATA,
					DialogOptionTypes.EXTERNAL_ID,
					ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
							+ username);
			if (dialogOption == null) {
				log.debug(
						"Participant with deepstream id {} not authorized for deepstream access: Dialog option not found",
						username);
				return false;
			}

			val participant = databaseManagerService.getModelObjectById(
					Participant.class, dialogOption.getParticipant());
			if (participant == null) {
				log.debug(
						"Participant with deepstream id {} not authorized for deepstream access: Participant not found",
						username);
				return false;
			}

			val intervention = databaseManagerService.getModelObjectById(
					Intervention.class, participant.getIntervention());
			if (intervention == null || !intervention.isActive()) {
				log.debug(
						"Participant with deepstream id {} not authorized for deepstream access: Intervention not active",
						username);
				return false;
			}
			if (!intervention.getDeepstreamPassword()
					.equals(interventionPassword)) {
				log.debug(
						"Participant with deepstream id {} not authorized for deepstream access: Password does not match deepstream intervention password",
						username);
				return false;
			}

			if (deepstreamCommunicationService != null
					&& deepstreamCommunicationService
							.checkSecret(username, secret,
									role.equals(deepstreamObserverRole) || role
											.equals(deepstreamTeamManagerRole)
													? 64 : -1)) {
				log.debug(
						"Participant with deepstream id {} authorized for deepstream access",
						username);
				return true;
			} else {
				log.debug(
						"Participant with deepstream id {} not authorized for deepstream access: Wrong secret",
						username);
				return false;
			}
		} else if (role.equals(deepstreamSuperviserRole)) {
			// Check supervisor access
			val dialogOption = databaseManagerService.findOneModelObject(
					DialogOption.class, Queries.DIALOG_OPTION__BY_TYPE_AND_DATA,
					DialogOptionTypes.SUPERVISOR_EXTERNAL_ID,
					ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
							+ username);
			if (dialogOption == null) {
				log.debug(
						"Supervisor with deepstream id {} not authorized for deepstream access: Dialog option not found",
						username);
				return false;
			}

			val participant = databaseManagerService.getModelObjectById(
					Participant.class, dialogOption.getParticipant());
			if (participant == null) {
				log.debug(
						"Supervisor with deepstream id {} not authorized for deepstream access: Participant not found",
						username);
				return false;
			}

			val intervention = databaseManagerService.getModelObjectById(
					Intervention.class, participant.getIntervention());
			if (intervention == null || !intervention.isActive()) {
				log.debug(
						"Supervisor with deepstream id {} not authorized for deepstream access: Intervention not active",
						username);
				return false;
			}
			if (!intervention.getDeepstreamPassword()
					.equals(interventionPassword)) {
				log.debug(
						"Supervisor with deepstream id {} not authorized for deepstream access: Password does not match deepstream intervention password",
						username);
				return false;
			}

			if (deepstreamCommunicationService != null
					&& deepstreamCommunicationService.checkSecret(username,
							secret)) {
				log.debug(
						"Supervisor with deepstream id {} authorized for deepstream access",
						username);
				return true;
			} else {
				log.debug(
						"Supervisor with deepstream id {} not authorized for deepstream access: Wrong secret",
						username);
				return false;
			}
		} else {
			log.debug("Unauthorized access with wrong role {}", role);
			return false;
		}
	}
	
	public boolean checkExternalSystemAccess(final int clientVersion, final String role, final String systemId,
			final String token) {

		// Prevent access for too old or new clients.
		if (clientVersion < deepstreamMinClientVersion || clientVersion > deepstreamMaxClientVersion) {
			return false;
		}

		// Prevent unauthorized access with empty values.
		if (StringUtils.isBlank(systemId) || StringUtils.isBlank(role) || StringUtils.isBlank(token)) {
			return false;
		}

		// Check access based on role.
		if (role.equals(deepstreamExternalServiceRole)) {

			// Check if systemId exists.
			val externalSystem = databaseManagerService.findOneModelObject(InterventionExternalSystem.class,
					Queries.INTERVENTION_EXTERNAL_SYSTEM__BY_SYSTEM_ID, systemId);
			if (externalSystem == null) {
				log.debug("System id {} not authorized for deepstream access: System id not found", systemId);
				return false;
			}
			
			// Check if external system is active.
			if(!externalSystem.isActive()) {
				log.debug("System with id {} is inactive", systemId);
				return false;
			}

			// Validate token saved in record with path: "external-systems/[systemId]".
			if (deepstreamCommunicationService != null
					&& deepstreamCommunicationService.checkExternalSystemToken(systemId, token)) {
				log.debug("System {} with id {} authorized for deepstream access", externalSystem.getName(),
						systemId);
				return true;
			} else {
				log.debug("System {} with id {} not authorized for deepstream access: Wrong token",
						externalSystem.getName(), systemId);
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
						null, relatedParticipant, interventionPattern,
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
						null, relatedParticipant, interventionPattern,
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
	 * given participant
	 *
	 * @param participantId
	 * @param variable
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
		val participant = databaseManagerService
				.getModelObjectById(Participant.class, participantId);

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
								isService),
						participant.getId().toHexString(), true, null));
				break;
			case SHARED_WITH_GROUP:
				Iterable<Participant> relevantParticipants = databaseManagerService
						.findModelObjects(Participant.class,
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
												isService),
								relevantParticipant.getId().toHexString(),
								participantId.equals(
										relevantParticipant.getId()),
								null);

						resultVariables.add(variableWithValue);
					}
				}
				break;
			case SHARED_WITH_INTERVENTION:
			case SHARED_WITH_INTERVENTION_AND_DASHBOARD:
				relevantParticipants = databaseManagerService.findModelObjects(
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
												isService),
								relevantParticipant.getId().toHexString(),
								participantId.equals(
										relevantParticipant.getId()),
								null);

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
	private CollectionOfExtendedVariables getVariableValueForDashboardOrExternalOfParticipantsOfGroupOrIntervention(
			final ObjectId interventionId, final String variable,
			final String group, final String filterVariable,
			final String filterValue, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		val intervention = databaseManagerService
				.getModelObjectById(Intervention.class, interventionId);

		if (intervention == null) {
			throw variablesManagerService.new ExternallyReadProtectedVariableException(
					"The given intervention does not exist anymore, so the variables cannot be read");
		}

		val collectionOfExtendedResultVariables = new CollectionOfExtendedVariables();
		val resultVariables = collectionOfExtendedResultVariables
				.getVariables();

		if (group != null) {
			final Iterable<Participant> relevantParticipants = databaseManagerService
					.findModelObjects(Participant.class,
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
							variablesManagerService
									.externallyReadVariableValueForParticipant(
											relevantParticipant.getId(),
											ImplementationConstants.VARIABLE_PREFIX
													+ variable,
											InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION_AND_DASHBOARD,
											isService),
							relevantParticipant.getId().toHexString(), null,
							null);

					resultVariables.add(variableWithValue);
				}
			}
		} else {
			final Iterable<Participant> relevantParticipants = databaseManagerService
					.findModelObjects(Participant.class,
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
							variablesManagerService
									.externallyReadVariableValueForParticipant(
											relevantParticipant.getId(),
											ImplementationConstants.VARIABLE_PREFIX
													+ variable,
											InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION_AND_DASHBOARD,
											isService),
							relevantParticipant.getId().toHexString(), null,
							null);

					resultVariables.add(variableWithValue);
				}
			}
		}

		collectionOfExtendedResultVariables.setSize(resultVariables.size());
		Collections.shuffle(resultVariables);

		return collectionOfExtendedResultVariables;
	}

	/**
	 * Reads variables for all participants of the same group/intervention as
	 * the given participant
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
		val participant = databaseManagerService
				.getModelObjectById(Participant.class, participantId);

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
					extendedListVariable.getVariables()
							.add(new Variable(variable,
									getVariableValueOfParticipant(participantId,
											variable, isService)));
				}

				resultVariables.add(extendedListVariable);
				break;
			case SHARED_WITH_GROUP:
				Iterable<Participant> relevantParticipants = databaseManagerService
						.findModelObjects(Participant.class,
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
								participantId
										.equals(relevantParticipant.getId()));

						for (val variable : variables) {
							extendedListVariable.getVariables()
									.add(new Variable(variable,
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
				relevantParticipants = databaseManagerService.findModelObjects(
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
								participantId
										.equals(relevantParticipant.getId()));

						for (val variable : variables) {
							extendedListVariable.getVariables()
									.add(new Variable(variable,
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
	 * Reads variables for dashboard for all participants of the given
	 * group/intervention (with variable based filter if applied)
	 *
	 * @param interventionId
	 * @param variables
	 * @param group
	 * @param filterVariable
	 * @param filterValue
	 * @return
	 */
	@Synchronized
	private CollectionOfExtendedListVariables getVariableValueForDashboardOrExternalOfParticipantsOfGroupOrIntervention(
			final ObjectId interventionId, final List<String> variables,
			final String group, final String filterVariable,
			final String filterValue, final boolean isService)
			throws ExternallyReadProtectedVariableException {
		val intervention = databaseManagerService
				.getModelObjectById(Intervention.class, interventionId);

		if (intervention == null) {
			throw variablesManagerService.new ExternallyReadProtectedVariableException(
					"The given intervention does not exist anymore, so the variables cannot be read");
		}

		val collectionOfExtendedListResultVariables = new CollectionOfExtendedListVariables();
		val resultVariables = collectionOfExtendedListResultVariables
				.getVariableListing();

		if (group != null) {
			final Iterable<Participant> relevantParticipants = databaseManagerService
					.findModelObjects(Participant.class,
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

					final ExtendedListVariable extendedListVariable = new ExtendedListVariable(
							relevantParticipant.getId().toHexString(), true);

					for (val variable : variables) {
						extendedListVariable.getVariables().add(new Variable(
								variable,
								variablesManagerService
										.externallyReadVariableValueForParticipant(
												relevantParticipant.getId(),
												ImplementationConstants.VARIABLE_PREFIX
														+ variable,
												InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION_AND_DASHBOARD,
												isService)));
					}

					resultVariables.add(extendedListVariable);
				}
			}
		} else {
			final Iterable<Participant> relevantParticipants = databaseManagerService
					.findModelObjects(Participant.class,
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

					final ExtendedListVariable extendedListVariable = new ExtendedListVariable(
							relevantParticipant.getId().toHexString(), true);

					for (val variable : variables) {
						extendedListVariable.getVariables().add(new Variable(
								variable,
								variablesManagerService
										.externallyReadVariableValueForParticipant(
												relevantParticipant.getId(),
												ImplementationConstants.VARIABLE_PREFIX
														+ variable,
												InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION_AND_DASHBOARD,
												isService)));
					}

					resultVariables.add(extendedListVariable);
				}
			}
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
				participantId,
				ImplementationConstants.VARIABLE_PREFIX + variable, value,
				describesMediaUpload, isService);
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
			final ObjectId participantId, final ObjectId receivingParticipantId,
			final String variable, final boolean addVote)
			throws ExternallyWriteProtectedVariableException {
		variablesManagerService.serviceWriteVotingFromParticipantForParticipant(
				participantId, receivingParticipantId,
				ImplementationConstants.VARIABLE_PREFIX + variable, addVote);
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
