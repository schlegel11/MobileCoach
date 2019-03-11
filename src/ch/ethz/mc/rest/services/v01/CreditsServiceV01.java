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

import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.rest.OK;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.tools.StringValidator;

/**
 * Service to collect credits using REST
 *
 * @author Andreas Filler
 */
@Path("/v01/credits")
@Log4j2
public class CreditsServiceV01 extends AbstractServiceV01 {

	public CreditsServiceV01(final RESTManagerService restManagerService) {
		super(restManagerService);
	}

	/*
	 * Write functions
	 */
	@GET
	@Path("/storeCredit/{variable}/{creditName}")
	@Produces("application/json")
	public Response storeCredit(@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@PathParam("creditName") final String creditName,
			@Context final HttpServletRequest request) {
		log.debug("Token {}: Storing credit for {} on {}", token, creditName,
				variable);
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

			restManagerService.writeCredit(participantId, variable.trim(),
					creditName);
		} catch (final Exception e) {
			throw new WebApplicationException(Response.status(Status.FORBIDDEN)
					.entity("Could not store credit: " + e.getMessage())
					.build());
		}

		return Response.ok(new OK()).build();
	}
}
