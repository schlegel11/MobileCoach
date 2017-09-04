package ch.ethz.mc.rest.services.v02;

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
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.rest.CollectionOfExtendedVariables;
import ch.ethz.mc.model.rest.OK;
import ch.ethz.mc.model.rest.Variable;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.tools.StringValidator;

/**
 * Service to read voting values and to vote/unvote using REST
 *
 * @author Andreas Filler
 */
@Path("/v02/voting")
@Log4j2
public class VotingServiceV02 extends AbstractServiceV02 {

	public VotingServiceV02(final RESTManagerService restManagerService) {
		super(restManagerService);
	}

	/*
	 * Read functions
	 */
	@GET
	@Path("/votings/{variable}")
	@Produces("application/json")
	public Variable votingVotings(@HeaderParam("user") final String user,
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable) {
		log.debug("Token {}: Read voting {}", token, variable);
		ObjectId participantId;
		try {
			participantId = checkExternalParticipantAccessAndReturnParticipantId(
					user, token);
		} catch (final Exception e) {
			throw e;
		}

		try {
			if (!StringValidator
					.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
							+ variable.trim())) {
				throw new Exception("The variable name is not valid");
			}

			// Transform votings to amounts
			val votingVariable = restManagerService.readVariable(participantId,
					variable.trim(), true);

			if (votingVariable.getValue().length() < ImplementationConstants.OBJECT_ID_LENGTH) {
				votingVariable.setValue("0");
			} else {
				votingVariable.setValue(String.valueOf(StringUtils
						.countMatches(votingVariable.getValue(), ",") + 1));
			}

			return votingVariable;
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	@GET
	@Path("/votingsGroupArray/{variable}")
	@Produces("application/json")
	public CollectionOfExtendedVariables votingVotingsGroupArray(
			@HeaderParam("user") final String user,
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable) {
		log.debug(
				"Token {}: Read votings array {} of participants from same group as participant",
				token, variable);
		ObjectId participantId;
		try {
			participantId = checkExternalParticipantAccessAndReturnParticipantId(
					user, token);
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
							variable.trim(), true, true);

			// Transform votings to amounts
			for (val votingVariable : collectionOfExtendedVariables
					.getVariables()) {

				if (votingVariable.getValue().length() < ImplementationConstants.OBJECT_ID_LENGTH) {
					votingVariable.setValue("0");
					votingVariable.setVoted(false);
				} else {
					if (votingVariable.getValue().contains(
							participantId.toHexString())) {
						votingVariable.setVoted(true);
					} else {
						votingVariable.setVoted(false);
					}

					votingVariable.setValue(String.valueOf(StringUtils
							.countMatches(votingVariable.getValue(), ",") + 1));
				}
			}

			return collectionOfExtendedVariables;
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	@GET
	@Path("/votingsInterventionArray/{variable}")
	@Produces("application/json")
	public CollectionOfExtendedVariables votingVotingsInterventionArray(
			@HeaderParam("user") final String user,
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable) {
		log.debug(
				"Token {}: Read votings array {} of participants from same intervention as participant",
				token, variable);
		ObjectId participantId;
		try {
			participantId = checkExternalParticipantAccessAndReturnParticipantId(
					user, token);
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
							variable.trim(), false, true);

			// Transform votings to amounts
			for (val votingVariable : collectionOfExtendedVariables
					.getVariables()) {

				if (votingVariable.getValue().length() < ImplementationConstants.OBJECT_ID_LENGTH) {
					votingVariable.setValue("0");
					votingVariable.setVoted(false);
				} else {
					if (votingVariable.getValue().contains(
							participantId.toHexString())) {
						votingVariable.setVoted(true);
					} else {
						votingVariable.setVoted(false);
					}

					votingVariable.setValue(String.valueOf(StringUtils
							.countMatches(votingVariable.getValue(), ",") + 1));
				}
			}

			return collectionOfExtendedVariables;
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not retrieve variable: " + e.getMessage())
					.build());
		}
	}

	/*
	 * Write functions
	 */
	@GET
	@Path("/vote/{variable}/{receivingParticipant}")
	@Produces("application/json")
	public Response votingVote(
			@HeaderParam("user") final String user,
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@PathParam("receivingParticipant") final String receivingParticipantIdString) {
		log.debug("Token {}: Write voting {} for participant {}", token,
				receivingParticipantIdString);
		ObjectId participantId;
		try {
			participantId = checkExternalParticipantAccessAndReturnParticipantId(
					user, token);
		} catch (final Exception e) {
			throw e;
		}

		ObjectId receivingParticipantId;
		if (ObjectId.isValid(receivingParticipantIdString)) {
			receivingParticipantId = new ObjectId(receivingParticipantIdString);
		} else {
			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST)
					.entity("The given receiving participant is not valid")
					.build());
		}

		try {
			if (!StringValidator
					.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
							+ variable.trim())) {
				throw new Exception("The variable name is not valid");
			}

			restManagerService.writeVoting(participantId,
					receivingParticipantId, variable.trim(), true);
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not write voting: " + e.getMessage())
					.build());
		}

		return Response.ok(new OK()).build();
	}

	@GET
	@Path("/unvote/{variable}/{receivingParticipant}")
	@Produces("application/json")
	public Response votingUnvote(
			@HeaderParam("user") final String user,
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@PathParam("receivingParticipant") final String receivingParticipantIdString) {
		log.debug("Token {}: Remove voting {} for participant {}", token,
				receivingParticipantIdString);
		ObjectId participantId;
		try {
			participantId = checkExternalParticipantAccessAndReturnParticipantId(
					user, token);
		} catch (final Exception e) {
			throw e;
		}

		ObjectId receivingParticipantId;
		if (ObjectId.isValid(receivingParticipantIdString)) {
			receivingParticipantId = new ObjectId(receivingParticipantIdString);
		} else {
			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST)
					.entity("The given receiving participant is not valid")
					.build());
		}

		try {
			if (!StringValidator
					.isValidVariableName(ImplementationConstants.VARIABLE_PREFIX
							+ variable.trim())) {
				throw new Exception("The variable name is not valid");
			}

			restManagerService.writeVoting(participantId,
					receivingParticipantId, variable.trim(), false);
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not write voting: " + e.getMessage())
					.build());
		}

		return Response.ok(new OK()).build();
	}
}
