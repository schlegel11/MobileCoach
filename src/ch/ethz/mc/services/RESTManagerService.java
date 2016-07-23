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
import ch.ethz.mc.model.rest.VariableWithValue;
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

	private final VariablesManagerService	variablesManagerService;

	private RESTManagerService(
			final VariablesManagerService variablesManagerService)
					throws Exception {
		$lock = MC.getInstance();

		log.info("Starting service...");

		this.variablesManagerService = variablesManagerService;

		log.info("Started.");
	}

	public static RESTManagerService start(
			final VariablesManagerService variablesManagerService)
					throws Exception {
		if (instance == null) {
			instance = new RESTManagerService(variablesManagerService);
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
			variablesManagerService.externallyWriteVariableForParticipant(
					participantId, ImplementationConstants.VARIABLE_PREFIX
							+ variable, value);

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
								+ variable);
	}
}
