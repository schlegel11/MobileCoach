package ch.ethz.mc.rest.services.v01;

/* ##LICENSE## */
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
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
@Path("/v01/voting")
@Log4j2
public class VotingServiceV01 extends AbstractServiceV01 {

	public VotingServiceV01(final RESTManagerService restManagerService) {
		super(restManagerService);
	}

	/*
	 * Read functions
	 */
	@GET
	@Path("/votings/{variable}")
	@Produces("application/json")
	public Variable votingVotings(@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request) {
		log.debug("Token {}: Read voting {}", token, variable);
		ObjectId participantId;
		try {
			participantId = checkParticipantRelatedAccessAndReturnParticipantId(
					token, request.getSession());
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

			if (votingVariable.getValue()
					.length() < ImplementationConstants.OBJECT_ID_LENGTH) {
				votingVariable.setValue("0");
			} else {
				votingVariable.setValue(String.valueOf(
						StringUtils.countMatches(votingVariable.getValue(), ",")
								+ 1));
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
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request) {
		log.debug(
				"Token {}: Read votings array {} of participants from same group as participant",
				token, variable);
		ObjectId participantId;
		try {
			participantId = checkParticipantRelatedAccessAndReturnParticipantId(
					token, request.getSession());
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

				if (votingVariable.getValue()
						.length() < ImplementationConstants.OBJECT_ID_LENGTH) {
					votingVariable.setValue("0");
					votingVariable.setVoted(false);
				} else {
					if (votingVariable.getValue()
							.contains(participantId.toHexString())) {
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
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@Context final HttpServletRequest request) {
		log.debug(
				"Token {}: Read votings array {} of participants from same intervention as participant",
				token, variable);
		ObjectId participantId;
		try {
			participantId = checkParticipantRelatedAccessAndReturnParticipantId(
					token, request.getSession());
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

				if (votingVariable.getValue()
						.length() < ImplementationConstants.OBJECT_ID_LENGTH) {
					votingVariable.setValue("0");
					votingVariable.setVoted(false);
				} else {
					if (votingVariable.getValue()
							.contains(participantId.toHexString())) {
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
	public Response votingVote(@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@PathParam("receivingParticipant") final String receivingParticipantIdString,
			@Context final HttpServletRequest request) {
		log.debug("Token {}: Write voting {} for participant {}", token,
				receivingParticipantIdString);
		ObjectId participantId;
		try {
			participantId = checkParticipantRelatedAccessAndReturnParticipantId(
					token, request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		ObjectId receivingParticipantId;
		if (ObjectId.isValid(receivingParticipantIdString)) {
			receivingParticipantId = new ObjectId(receivingParticipantIdString);
		} else {
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST)
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
	public Response votingUnvote(@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@PathParam("receivingParticipant") final String receivingParticipantIdString,
			@Context final HttpServletRequest request) {
		log.debug("Token {}: Remove voting {} for participant {}", token,
				receivingParticipantIdString);
		ObjectId participantId;
		try {
			participantId = checkParticipantRelatedAccessAndReturnParticipantId(
					token, request.getSession());
		} catch (final Exception e) {
			throw e;
		}

		ObjectId receivingParticipantId;
		if (ObjectId.isValid(receivingParticipantIdString)) {
			receivingParticipantId = new ObjectId(receivingParticipantIdString);
		} else {
			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST)
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
