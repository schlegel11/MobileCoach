package ch.ethz.mc.services;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValuePrivacyTypes;
import ch.ethz.mc.model.rest.CollectionOfExtendedListVariables;
import ch.ethz.mc.model.rest.CollectionOfExtendedVariables;
import ch.ethz.mc.model.rest.ExtendedListVariable;
import ch.ethz.mc.model.rest.ExtendedVariable;
import ch.ethz.mc.model.rest.Variable;
import ch.ethz.mc.model.rest.VariableAverage;
import ch.ethz.mc.services.internal.DatabaseManagerService;
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
	private final Object					$lock;

	private static RESTManagerService		instance	= null;

	private final DatabaseManagerService	databaseManagerService;
	@Getter
	private final FileStorageManagerService	fileStorageManagerService;
	private final VariablesManagerService	variablesManagerService;

	private RESTManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		$lock = MC.getInstance();

		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.variablesManagerService = variablesManagerService;

		log.info("Started.");
	}

	public static RESTManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		if (instance == null) {
			instance = new RESTManagerService(databaseManagerService,
					fileStorageManagerService, variablesManagerService);
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
	public boolean checkVariableForWritingRights(final ObjectId participantId,
			final String variable) {
		synchronized ($lock) {
			return variablesManagerService.checkVariableForExternalWriting(
					participantId, ImplementationConstants.VARIABLE_PREFIX
					+ variable.trim());
		}
	}

	/**
	 * Reads variable for given participant
	 *
	 * @param participantId
	 * @param variable
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public Variable readVariable(final ObjectId participantId,
			final String variable)
			throws ExternallyReadProtectedVariableException {
		log.debug("Try to read variable {} for participant {}", variable,
				participantId);

		try {
			val resultVariable = new Variable(variable,
					getVariableValueOfParticipant(participantId, variable));

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
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public CollectionOfExtendedVariables readVariableArrayOfGroupOrIntervention(
			final ObjectId participantId, final String variable,
			final boolean sameGroup)
			throws ExternallyReadProtectedVariableException {
		log.debug(
				"Try to read variable array {} of participants from the same {} as participant {}",
				variable, sameGroup ? "group" : "intervention", participantId);

		try {
			val collecionOfExtendedVariables = getVariableValueOfParticipantsOfGroupOrIntervention(
					participantId,
					variable,
					sameGroup ? InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP
							: InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION);

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
	 * Reads variable for all participants of the same group/intervention as the
	 * given participant
	 *
	 * @param participantId
	 * @param variables
	 * @param sameGroup
	 * @param many
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public CollectionOfExtendedListVariables readVariableListArrayOfGroupOrIntervention(
			final ObjectId participantId, final List<String> variables,
			final boolean sameGroup)
			throws ExternallyReadProtectedVariableException {
		log.debug(
				"Try to read variable array {} of participants from the same {} as participant {}",
				variables, sameGroup ? "group" : "intervention", participantId);

		try {
			val collectionOfExtendendListVariables = getVariableValueOfParticipantsOfGroupOrIntervention(
					participantId,
					variables,
					sameGroup ? InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP
							: InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION);

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
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public VariableAverage calculateAverageOfVariableArrayOfGroupOrIntervention(
			final ObjectId participantId, final String variable,
			final boolean sameGroup)
			throws ExternallyReadProtectedVariableException {
		log.debug(
				"Try to calculate average of variable array {} of participants from the same {} as participant {}",
				variable, sameGroup ? "group" : "intervention", participantId);

		try {
			val variableAverage = new VariableAverage();
			variableAverage.setVariable(variable);

			val resultVariables = readVariableArrayOfGroupOrIntervention(
					participantId, variable, sameGroup);

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
					"Could not calculate averagte of variable {} of participants from the same {} as participant {}: {}",
					variable, sameGroup ? "group" : "intervention",
					participantId, variable, participantId, e.getMessage());
			throw e;
		}
	}

	/**
	 * Writes variable for given participant
	 *
	 * @param participantId
	 * @param variable
	 * @param value
	 * @throws ExternallyWriteProtectedVariableException
	 */
	public void writeVariable(final ObjectId participantId,
			final String variable, final String value,
			final boolean describesMediaUpload)
			throws ExternallyWriteProtectedVariableException {
		log.debug("Try to write variable {} for participant {} with value {}",
				variable, participantId, value);

		try {
			writeVariableValue(participantId, variable, value,
					describesMediaUpload);

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
	 * @throws ExternallyWriteProtectedVariableException
	 */
	public void writeVoting(final ObjectId participantId,
			final ObjectId receivingParticipantId, final String variable)
			throws ExternallyWriteProtectedVariableException {
		log.debug(
				"Try to write voting {} for participant {} from participant {}",
				variable, receivingParticipantId, participantId);

		try {
			writeVotingFromParticipantForParticipant(participantId,
					receivingParticipantId, variable);

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

	/*
	 * Internal helpers
	 */
	/**
	 * Reads variable for given participant
	 *
	 * @param participantId
	 * @param variable
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	@Synchronized
	private String getVariableValueOfParticipant(final ObjectId participantId,
			final String variable)
			throws ExternallyReadProtectedVariableException {
		return variablesManagerService
				.getExternallyReadableVariableValueForParticipant(
						participantId, ImplementationConstants.VARIABLE_PREFIX
								+ variable,
						InterventionVariableWithValuePrivacyTypes.PRIVATE);
	}

	/**
	 * Reads variable for all participants of the same group/intervetion as the
	 * given
	 * participant
	 *
	 * @param participantId
	 * @param variables
	 * @param requestPrivacyType
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	@Synchronized
	private CollectionOfExtendedVariables getVariableValueOfParticipantsOfGroupOrIntervention(
			final ObjectId participantId, final String variable,
			final InterventionVariableWithValuePrivacyTypes requestPrivacyType)
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
						getVariableValueOfParticipant(participantId, variable),
						participant.getId().toHexString(), true, null));
				break;
			case SHARED_WITH_GROUP:
				Iterable<Participant> relevantParticipants = databaseManagerService
						.findModelObjects(
								Participant.class,
								Queries.PARTICIPANT__BY_INTERVENTION_AND_GROUP_AND_MONITORING_ACTIVE_TRUE,
								interventionId, group);

				for (val relevantParticipant : relevantParticipants) {
					final ExtendedVariable variableWithValue = new ExtendedVariable(
							variable,
							variablesManagerService
									.getExternallyReadableVariableValueForParticipant(
											relevantParticipant.getId(),
											ImplementationConstants.VARIABLE_PREFIX
													+ variable,
											InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP),
							relevantParticipant.getId().toHexString(),
							participantId.equals(relevantParticipant.getId()),
									null);

					resultVariables.add(variableWithValue);
				}
				break;
			case SHARED_WITH_INTERVENTION:
				relevantParticipants = databaseManagerService
				.findModelObjects(
						Participant.class,
						Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
						interventionId);

				for (val relevantParticipant : relevantParticipants) {
					final ExtendedVariable variableWithValue = new ExtendedVariable(
							variable,
							variablesManagerService
									.getExternallyReadableVariableValueForParticipant(
											relevantParticipant.getId(),
											ImplementationConstants.VARIABLE_PREFIX
													+ variable,
											InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION),
							relevantParticipant.getId().toHexString(),
							participantId.equals(relevantParticipant.getId()),
									null);

					resultVariables.add(variableWithValue);
				}
				break;
			default:
				break;

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
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	@Synchronized
	private CollectionOfExtendedListVariables getVariableValueOfParticipantsOfGroupOrIntervention(
			final ObjectId participantId, final List<String> variables,
			final InterventionVariableWithValuePrivacyTypes requestPrivacyType)
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
					extendedListVariable.getVariables().add(
							new Variable(variable,
									getVariableValueOfParticipant(
											participantId, variable)));
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
					extendedListVariable = new ExtendedListVariable(
							relevantParticipant.getId().toHexString(),
							participantId.equals(relevantParticipant.getId()));

					for (val variable : variables) {
						extendedListVariable
								.getVariables()
								.add(new Variable(
										variable,
										variablesManagerService
												.getExternallyReadableVariableValueForParticipant(
														relevantParticipant
																.getId(),
														ImplementationConstants.VARIABLE_PREFIX
																+ variable,
														InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP)));
					}
					resultVariables.add(extendedListVariable);
				}
				break;
			case SHARED_WITH_INTERVENTION:
				relevantParticipants = databaseManagerService
						.findModelObjects(
								Participant.class,
								Queries.PARTICIPANT__BY_INTERVENTION_AND_MONITORING_ACTIVE_TRUE,
								interventionId);

				for (val relevantParticipant : relevantParticipants) {
					extendedListVariable = new ExtendedListVariable(
							relevantParticipant.getId().toHexString(),
							participantId.equals(relevantParticipant.getId()));

					for (val variable : variables) {
						extendedListVariable
								.getVariables()
								.add(new Variable(
										variable,
										variablesManagerService
												.getExternallyReadableVariableValueForParticipant(
														relevantParticipant
																.getId(),
														ImplementationConstants.VARIABLE_PREFIX
																+ variable,
														InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION)));
					}
					resultVariables.add(extendedListVariable);
				}
				break;
			default:
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
	 * @throws ExternallyWriteProtectedVariableException
	 */
	@Synchronized
	private void writeVariableValue(final ObjectId participantId,
			final String variable, final String value,
			final boolean describesMediaUpload)
			throws ExternallyWriteProtectedVariableException {
		variablesManagerService.externallyWriteVariableForParticipant(
				participantId, ImplementationConstants.VARIABLE_PREFIX
						+ variable, value, describesMediaUpload);
	}

	/**
	 * Writes voting from given participant for given receiving participant
	 *
	 * @param participantId
	 * @param receivingParticipantId
	 * @param variable
	 * @throws ExternallyWriteProtectedVariableException
	 */
	@Synchronized
	private void writeVotingFromParticipantForParticipant(
			final ObjectId participantId,
			final ObjectId receivingParticipantId, final String variable)
			throws ExternallyWriteProtectedVariableException {
		variablesManagerService
				.externallyWriteVotingFromParticipantForParticipant(
						participantId, receivingParticipantId,
						ImplementationConstants.VARIABLE_PREFIX + variable);
	}
}
