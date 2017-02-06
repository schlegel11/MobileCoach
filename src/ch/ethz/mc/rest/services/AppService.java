package ch.ethz.mc.rest.services;

import java.util.Map;

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

import ch.ethz.mc.model.persistent.AppToken;
import ch.ethz.mc.model.persistent.OneTimeToken;
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
	public Result getConfig(@Context final HttpServletRequest request,
			@HeaderParam("Authentication") final String token) throws BadRequestException {

		if (token == null) {
			throw new WebApplicationException(Response.status(400).entity("Missing header 'Authentication'.").build());
		}

		ObjectId userId = null;
		String mctoken = null;
		if (OneTimeToken.isOneTimeToken(token)){
			// handle OneTimeToken: invalidate and create AppToken
			userId = restManagerService.consumeOneTimeToken(token);
			if (userId != null) mctoken = restManagerService.createAppTokenForParticipant(userId);
		} else if (AppToken.isAppToken(token)){
			// handle AppToken: use the supplied token
			userId = restManagerService.findParticipantIdForAppToken(token);
			mctoken = token;
		}
		
		if (userId == null) {
			throw new WebApplicationException(Response.status(403).entity("Invalid Token supplied").build());
		}

		MattermostUserConfiguration userConfiguration = fetchUserConfiguration(userId);

		return new Result(new MobileCoachAuthentication(userId.toHexString(), mctoken),
				new UserConfigurationForAuthentication(userConfiguration),
				getVariables(userId));
	}

	private MattermostUserConfiguration fetchUserConfiguration(final ObjectId participantId) {
		MattermostUserConfiguration userConfig;
		if (mattMgmtService.existsUserForParticipant(participantId)) {
			userConfig = mattMgmtService.getUserConfiguration(participantId);
		} else {
			userConfig = mattMgmtService.createParticipantUser(participantId);
		}
		return userConfig;
	}
	
	private Map<String, String> getVariables(final ObjectId participantId) {
		return restManagerService.getExternallyReadableVariableValues(participantId);
	}

	@AllArgsConstructor
	private static class Result {

		@Getter
		private final MobileCoachAuthentication mobilecoach;

		@Getter
		private final UserConfigurationForAuthentication mattermost;
		
		@Getter
		private final Map<String, String> variables;

	}

	@AllArgsConstructor
	private static class MobileCoachAuthentication {
		@Getter
		private final String participant_id;
		@Getter
		private final String token;

	}
}
