package ch.ethz.mc.rest.services;

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
import java.util.ArrayList;

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
import ch.ethz.mc.model.rest.CollectionOfExtendedListVariables;
import ch.ethz.mc.model.rest.CollectionOfExtendedVariables;
import ch.ethz.mc.model.rest.OK;
import ch.ethz.mc.model.rest.VariableAverage;
import ch.ethz.mc.model.rest.Variable;
import ch.ethz.mc.model.rest.CollectionOfVariables;
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
	public Variable variableRead(@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request) {
		log.debug("Token {}: Read variable {}", token, variable);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipantId(token,
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
					variable.trim(), false);
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	@GET
	@Path("/readMany/{variables}")
	@Produces("application/json")
	public CollectionOfVariables variableReadMany(
			@HeaderParam("token") final String token,
			@PathParam("variables") final String variables,
			@Context final HttpServletRequest request) {
		log.debug("Token {}: Read variables {}", token, variables);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipantId(token,
					request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		try {
			final val variableArray = variables.split(",");

			val collectionOfVariables = new CollectionOfVariables();
			val resultVariables = collectionOfVariables.getVariables();

			for (val variable : variableArray) {
				if (!StringValidator
						.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
								+ variable.trim())) {
					throw new Exception("The variable name is not valid");
				}

				resultVariables.add(restManagerService.readVariable(
						participantId, variable.trim(), false));
			}

			collectionOfVariables.setSize(resultVariables.size());

			return collectionOfVariables;
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	@GET
	@Path("/readGroupArray/{variable}")
	@Produces("application/json")
	public CollectionOfExtendedVariables variableReadGroupArray(
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request) {
		log.debug(
				"Token {}: Read variable array {} of participants from same group as participant",
				token, variable);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipantId(token,
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

			val collectionOfExtendedVariables = restManagerService
					.readVariableArrayOfGroupOrIntervention(participantId,
							variable.trim(), true, false);

			return collectionOfExtendedVariables;
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	@GET
	@Path("/readGroupArrayMany/{variables}")
	@Produces("application/json")
	public CollectionOfExtendedListVariables variableReadGroupArrayMany(
			@HeaderParam("token") final String token,
			@PathParam("variables") final String variables,
			@Context final HttpServletRequest request) {
		log.debug(
				"Token {}: Read variables array {} of participants from same group as participant",
				token, variables);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipantId(token,
					request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		try {
			val cleanedVariableList = new ArrayList<String>();

			for (val variable : variables.split(",")) {
				if (!StringValidator
						.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
								+ variable.trim())) {
					throw new Exception("The variable name is not valid");
				}
				cleanedVariableList.add(variable.trim());
			}

			val collectionOfExtendedListVariables = restManagerService
					.readVariableListArrayOfGroupOrIntervention(participantId,
							cleanedVariableList, true, false);

			return collectionOfExtendedListVariables;
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	@GET
	@Path("/readInterventionArray/{variable}")
	@Produces("application/json")
	public CollectionOfExtendedVariables variableReadInterventionArray(
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request) {
		log.debug(
				"Token {}: Read variable array {} of participants from same intervention as participant",
				token, variable);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipantId(token,
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

			val collectionOfExtendedVariables = restManagerService
					.readVariableArrayOfGroupOrIntervention(participantId,
							variable.trim(), false, false);

			return collectionOfExtendedVariables;
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	@GET
	@Path("/readInterventionArrayMany/{variables}")
	@Produces("application/json")
	public CollectionOfExtendedListVariables variableReadInterventionArrayMany(
			@HeaderParam("token") final String token,
			@PathParam("variables") final String variables,
			@Context final HttpServletRequest request) {
		log.debug(
				"Token {}: Read variables array {} of participants from same intervention as participant",
				token, variables);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipantId(token,
					request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		try {
			val cleanedVariableList = new ArrayList<String>();

			for (val variable : variables.split(",")) {
				if (!StringValidator
						.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
								+ variable.trim())) {
					throw new Exception("The variable name is not valid");
				}
				cleanedVariableList.add(variable.trim());
			}

			val collectionOfExtendedListVariables = restManagerService
					.readVariableListArrayOfGroupOrIntervention(participantId,
							cleanedVariableList, false, false);

			return collectionOfExtendedListVariables;
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
			participantId = checkAccessAndReturnParticipantId(token,
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
							participantId, variable.trim(), true, false);

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
			participantId = checkAccessAndReturnParticipantId(token,
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
							participantId, variable.trim(), false, false);

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
	@Produces("application/json")
	public Response variableWrite(@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request, String content) {
		log.debug("Token {}: Write variable {}", token, variable);
		ObjectId participantId;
		try {
			participantId = checkAccessAndReturnParticipantId(token,
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
					content, false, false);
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not write variable: " + e.getMessage())
					.build());
		}

		return Response.ok(new OK()).build();
	}
}
