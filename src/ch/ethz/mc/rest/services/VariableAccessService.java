package ch.ethz.mc.rest.services;

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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.rest.ExtendedVariablesWithValues;
import ch.ethz.mc.model.rest.VariableAverage;
import ch.ethz.mc.model.rest.VariableWithValue;
import ch.ethz.mc.model.rest.VariablesWithValues;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.tools.StringValidator;

/**
 * Service to read/write variables using REST
 *
 * @author Andreas Filler
 */
@Path("/v01/variable")
@Log4j2
public class VariableAccessService extends AbstractService {

	public VariableAccessService(final RESTManagerService restManagerService) {
		super(restManagerService);
	}

	/*
	 * Read functions
	 */
	@GET
	@Path("/read/{variable}")
	@Produces("application/json")
	public VariableWithValue variableRead(
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request) {
		log.debug("Token {}: Read variable {}", token, variable);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipant(token,
					request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		try {
			if (!StringValidator
					.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
							+ variable.trim())) {
				throw new Exception("The variable name is not valid");
			}

			return restManagerService.readVariable(participantId,
					variable.trim());
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	@GET
	@Path("/readArray/{variables}")
	@Produces("application/json")
	public VariablesWithValues variableReadArray(
			@HeaderParam("token") final String token,
			@PathParam("variables") final String variables,
			@Context final HttpServletRequest request) {
		log.debug("Token {}: Read variables {}", token, variables);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipant(token,
					request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		try {
			final val variableArray = variables.split(",");

			val variablesWithValues = new VariablesWithValues();
			val variablesWithValuesList = variablesWithValues
					.getVariablesWithValues();

			for (val variable : variableArray) {
				if (!StringValidator
						.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
								+ variable.trim())) {
					throw new Exception("The variable name is not valid");
				}

				variablesWithValuesList.add(restManagerService.readVariable(
						participantId, variable.trim()));
			}

			variablesWithValues.setSize(variablesWithValuesList.size());

			return variablesWithValues;
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	@GET
	@Path("/readGroupArray/{variable}")
	@Produces("application/json")
	public ExtendedVariablesWithValues variableReadGroupArray(
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request) {
		log.debug(
				"Token {}: Read variable array {} of participants from same group as participant",
				token, variable);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipant(token,
					request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		try {
			if (!StringValidator
					.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
							+ variable.trim())) {
				throw new Exception("The variable name is not valid");
			}

			val variablesWithValues = restManagerService
					.readVariableArrayOfGroupOrIntervention(participantId,
							variable.trim(), true);

			return variablesWithValues;
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	@GET
	@Path("/readInterventionArray/{variable}")
	@Produces("application/json")
	public ExtendedVariablesWithValues variableReadInterventionArray(
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request) {
		log.debug(
				"Token {}: Read variable array {} of participants from same intervention as participant",
				token, variable);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipant(token,
					request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		try {
			if (!StringValidator
					.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
							+ variable.trim())) {
				throw new Exception("The variable name is not valid");
			}

			val variablesWithValues = restManagerService
					.readVariableArrayOfGroupOrIntervention(participantId,
							variable.trim(), false);

			return variablesWithValues;
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	@GET
	@Path("/calculateGroupAverage/{variable}")
	@Produces("application/json")
	public VariableAverage variableCalculateGroupAverage(
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request) {
		log.debug(
				"Token {}: Calculate variable average of variable {} of participants from same group as participant",
				token, variable);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipant(token,
					request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		try {
			if (!StringValidator
					.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
							+ variable.trim())) {
				throw new Exception("The variable name is not valid");
			}

			val variableAverage = restManagerService
					.calculateAverageOfVariableArrayOfGroupOrIntervention(
							participantId, variable.trim(), true);

			return variableAverage;
		} catch (final Exception e) {
			throw new WebApplicationException(Response
					.status(Status.FORBIDDEN)
					.entity("Could not calculate average of variable: "
							+ e.getMessage()).build());
		}
	}

	@GET
	@Path("/calculateInterventionAverage/{variable}")
	@Produces("application/json")
	public VariableAverage variableCalculateInterventionAverage(
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request) {
		log.debug(
				"Token {}: Calculate variable average of variable {} of participants from same intervention as participant",
				token, variable);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipant(token,
					request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		try {
			if (!StringValidator
					.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
							+ variable.trim())) {
				throw new Exception("The variable name is not valid");
			}

			val variableAverage = restManagerService
					.calculateAverageOfVariableArrayOfGroupOrIntervention(
							participantId, variable.trim(), false);

			return variableAverage;
		} catch (final Exception e) {
			throw new WebApplicationException(Response
					.status(Status.FORBIDDEN)
					.entity("Could not calculate average of variable: "
							+ e.getMessage()).build());
		}
	}

	/*
	 * Write functions
	 */
	@POST
	@Path("/write/{variable}")
	@Consumes("text/plain")
	public void variableWrite(@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request, String content) {
		log.debug("Token {}: Write variable {}", token, variable);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipant(token,
					request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		try {
			if (!StringValidator
					.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
							+ variable.trim())) {
				throw new Exception("The variable name is not valid");
			}

			restManagerService.writeVariable(participantId, variable.trim(),
					content);
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not write variable: " + e.getMessage())
					.build());
		}
	}
}
