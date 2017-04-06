package ch.ethz.mc.rest.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.bson.types.ObjectId;

import ch.ethz.mc.services.RESTManagerService;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Path("/v01")
public class AppAuthenticationService extends AbstractService {

	public AppAuthenticationService(RESTManagerService restManagerService) {
		super(restManagerService);
	}

	@GET
	@Path("/participantid")
	@Produces(MediaType.APPLICATION_JSON)
	public ParticipantInformation checkparticipantid(@HeaderParam("token") final String token,
			@Context final HttpServletRequest request) {
		ObjectId participantId = checkAccessAndReturnParticipantId(token, request.getSession());
		if (participantId != null) {
			return new ParticipantInformation(participantId.toHexString());
		} else {
			throw new WebApplicationException(HttpStatus.SC_NOT_ACCEPTABLE);
		}
	}

	@AllArgsConstructor
	public static class ParticipantInformation {
		@Getter
		private final String participantId;

	}

}
