package ch.ethz.mc.rest.services.v02;

/* ##LICENSE## */
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.rest.OK;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.tools.StringValidator;
import lombok.extern.log4j.Log4j2;

/**
 * Service to collect credits using REST
 *
 * @author Andreas Filler
 */
@Path("/v02/credits")
@Log4j2
public class CreditsServiceV02 extends AbstractServiceV02 {

	public CreditsServiceV02(final RESTManagerService restManagerService) {
		super(restManagerService);
	}

	/*
	 * Write functions
	 */
	@GET
	@Path("/storeCredit/{variable}/{creditName}")
	@Produces("application/json")
	public Response storeCredit(@HeaderParam("user") final String user,
			@HeaderParam("token") final String token,
			@PathParam("variable") final String variable,
			@PathParam("creditName") final String creditName) {
		log.debug("Token {}: Storing credit for {} on {}", token, creditName,
				variable);
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
