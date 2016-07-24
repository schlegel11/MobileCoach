package ch.ethz.mc.services;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValuePrivacyTypes;
import ch.ethz.mc.model.rest.ExtendedVariableWithValue;
import ch.ethz.mc.model.rest.ExtendedVariablesWithValues;
import ch.ethz.mc.model.rest.VariableAverage;
import ch.ethz.mc.model.rest.VariableWithValue;
import ch.ethz.mc.services.internal.DatabaseManagerService;
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
	private final VariablesManagerService	variablesManagerService;

	private RESTManagerService(
			final DatabaseManagerService databaseManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		$lock = MC.getInstance();

		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.variablesManagerService = variablesManagerService;

		log.info("Started.");
	}

	public static RESTManagerService start(
			final DatabaseManagerService databaseManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		if (instance == null) {
			instance = new RESTManagerService(databaseManagerService,
					variablesManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	/**
	 * Reads variable for given participant
	 *
	 * @param participantId
	 * @param variable
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public VariableWithValue readVariable(final ObjectId participantId,
			final String variable)
			throws ExternallyReadProtectedVariableException {
		log.debug("Try to read variable {} for participant {}", variable,
				participantId);

		try {
			val variableWithValue = new VariableWithValue(variable,
					getVariableValue(participantId, variable));

			log.debug("Returing variable with value {} for participant {}",
					variableWithValue, participantId);
			return variableWithValue;
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
	public ExtendedVariablesWithValues readVariableArrayOfGroupOrIntervention(
			final ObjectId participantId, final String variable,
			final boolean sameGroup)
			throws ExternallyReadProtectedVariableException {
		log.debug(
				"Try to read variable array {} of participants from the same {} as participant {}",
				variable, sameGroup ? "group" : "intervention", participantId);

		try {
			val variablesWithValues = getVariableValues(
					participantId,
					variable,
					sameGroup ? InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP
							: InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION);

			log.debug(
					"Returing variables with values {} of participants from the same {} as participant {}",
					variablesWithValues, sameGroup ? "group" : "intervention",
					participantId);
			return variablesWithValues;
		} catch (final Exception e) {
			log.debug(
					"Could not read variable {} of participants from the same {} as participant {}: {}",
					variable, sameGroup ? "group" : "intervention",
					participantId, variable, participantId, e.getMessage());
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

			val variablesWithValues = readVariableArrayOfGroupOrIntervention(
					participantId, variable, sameGroup);

			try {
				int i = 0;
				double average = 0d;
				for (val variableWithValue : variablesWithValues
						.getVariablesWithValues()) {
					i++;
					if (variableWithValue.isOwnValue()) {
						variableAverage.setAverage(Double
								.parseDouble(variableWithValue.getValue()));
					}

					log.debug(variableWithValue.getValue());
					log.debug(Double.parseDouble(variableWithValue.getValue()));

					average += Double.parseDouble(variableWithValue.getValue());
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
			final String variable, final String value)
			throws ExternallyWriteProtectedVariableException {
		log.debug("Try to write variable {} for participant {} with value {}",
				variable, participantId, value);

		try {
			writeVariableValue(participantId, variable, value);

			log.debug("Wrote variable {} for participant {}", variable,
					participantId);
		} catch (final Exception e) {
			log.debug(
					"Could not write variable {} for participant {} with value {}: {}",
					variable, participantId, value, e.getMessage());
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
	private String getVariableValue(final ObjectId participantId,
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
	 * @param variable
	 * @param privacyType
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	@Synchronized
	private ExtendedVariablesWithValues getVariableValues(
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

		val variablesWithValues = new ExtendedVariablesWithValues();
		val variablesWithValuesList = variablesWithValues
				.getVariablesWithValues();

		switch (requestPrivacyType) {
			case PRIVATE:
				variablesWithValuesList.add(new ExtendedVariableWithValue(
						variable, getVariableValue(participantId, variable),
						true));
				break;
			case SHARED_WITH_GROUP:
				Iterable<Participant> relevantParticipants = databaseManagerService
						.findModelObjects(Participant.class,
								Queries.PARTICIPANT__BY_INTERVENTION_AND_GROUP,
								interventionId, group);

				for (val relevantParticipant : relevantParticipants) {
					final ExtendedVariableWithValue variableWithValue = new ExtendedVariableWithValue(
							variable,
							variablesManagerService
									.getExternallyReadableVariableValueForParticipant(
											relevantParticipant.getId(),
											ImplementationConstants.VARIABLE_PREFIX
													+ variable,
											InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP),
							participantId.equals(relevantParticipant.getId()));

					variablesWithValuesList.add(variableWithValue);
				}
				break;
			case SHARED_WITH_INTERVENTION:
				relevantParticipants = databaseManagerService.findModelObjects(
						Participant.class,
						Queries.PARTICIPANT__BY_INTERVENTION, interventionId);

				for (val relevantParticipant : relevantParticipants) {
					final ExtendedVariableWithValue variableWithValue = new ExtendedVariableWithValue(
							variable,
							variablesManagerService
							.getExternallyReadableVariableValueForParticipant(
									relevantParticipant.getId(),
									ImplementationConstants.VARIABLE_PREFIX
									+ variable,
									InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION),
									participantId.equals(relevantParticipant.getId()));

					variablesWithValuesList.add(variableWithValue);
				}
				break;
			default:
				break;

		}

		variablesWithValues.setSize(variablesWithValuesList.size());

		return variablesWithValues;
	}

	/**
	 * Writes variable for given participant
	 *
	 * @param participantId
	 * @param variable
	 * @param value
	 * @throws ExternallyWriteProtectedVariableException
	 */
	@Synchronized
	private void writeVariableValue(final ObjectId participantId,
			final String variable, final String value)
			throws ExternallyWriteProtectedVariableException {
		variablesManagerService.externallyWriteVariableForParticipant(
				participantId, ImplementationConstants.VARIABLE_PREFIX
						+ variable, value);
	}
}
