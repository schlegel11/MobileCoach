package ch.ethz.mc.rest.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;

import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mobilecoach.model.persistent.MattermostUserConfiguration;
import ch.ethz.mobilecoach.services.MattermostManagementService;
import ch.ethz.mobilecoach.services.MattermostManagementService.UserConfigurationForAuthentication;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Service to read/write variables using REST
 *
 * @author Filipe Barata
 */
@Path("/app/v01")
public class AppService {

	private RESTManagerService restManagerService;
	private MattermostManagementService mattMgmtService;

	public AppService(RESTManagerService restManagerService, MattermostManagementService mattMgmtService) {
		this.restManagerService = restManagerService;
		this.mattMgmtService = mattMgmtService;
	}

	@GET
	@Path("/getconfig")
	@Produces("application/json")
	public Result authenticateApp(@Context final HttpServletRequest request,
			@HeaderParam("Authentication") final String oneTimeToken) throws BadRequestException {

		if (oneTimeToken == null) {
			throw new WebApplicationException(Response.status(400).entity("Missing header 'Authentication'.").build());
		}

		ObjectId userId = restManagerService.consumeOneTimeToken(oneTimeToken);
		if (userId == null) {
			throw new WebApplicationException(Response.status(403).entity("Invalid Token supplied").build());
		}

		MattermostUserConfiguration userConfiguration = fetchUserConfiguration(userId);
		String mctoken = restManagerService.createAppTokenForParticipant(userId);

		return new Result(new MobileCoachAuthentication(userId.toHexString(), mctoken),
				new UserConfigurationForAuthentication(userConfiguration));
	}

	private MattermostUserConfiguration fetchUserConfiguration(final ObjectId participantId) {
		MattermostUserConfiguration userConfig;
		String participant = participantId.toHexString();
		if (mattMgmtService.existsUserForParticipant(participant)) {
			userConfig = mattMgmtService.getUserConfiguration(participant);
		} else {
			userConfig = mattMgmtService.createParticipantUser(participant);
		}
		return userConfig;
	}

	@AllArgsConstructor
	private static class Result {

		@Getter
		private final MobileCoachAuthentication mobilecoach;

		@Getter
		private final UserConfigurationForAuthentication mattermost;

	}

	@AllArgsConstructor
	private static class MobileCoachAuthentication {
		@Getter
		private final String participant_id;
		@Getter
		private final String token;

	}
}
