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

import ch.ethz.mc.model.persistent.AppToken;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mobilecoach.model.persistent.MattermostUserConfiguration;
import ch.ethz.mobilecoach.services.MattermostManagementService;
import ch.ethz.mobilecoach.services.MattermostManagementService.UserConfigurationForAuthentication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Service to read/write variables using REST
 *
 * @author Filipe Barata
 */
@Path("/app/v01")
public class AppService {

	private final static String  CONST = "userid";

	private RESTManagerService restManagerService;
	private MattermostManagementService mattMgmtService;


	public AppService(RESTManagerService restManagerService, MattermostManagementService mattMgmtService) {
		super();
		this.restManagerService = restManagerService;
		this.mattMgmtService = mattMgmtService;
	}

	@GET
	@Path("/getconfig")
	@Produces("application/json")
	public Result authenticateApp(@Context final HttpServletRequest request, @HeaderParam("Authentication") final String authentication) throws BadRequestException{
		
		if (authentication == null){
			throw new WebApplicationException(Response.status(400).entity("Missing header 'Authentication'.").build());
		}

		if(!authentication.startsWith(CONST)){
			throw new WebApplicationException(Response.serverError().entity("Invalid Credential Format").build());
		}
		
		String userId = authentication.substring(CONST.length()+1).trim();
		
		// TODO replace the authentication token which contains the participantId with a temporary token (lookup in specific collection with temporary tokens)
		
		MattermostUserConfiguration userConfiguration = fetchUserConfiguration(userId);
		String mctoken = restManagerService.createAppTokenForParticipant(new ObjectId(userId));
		
		return new Result(new MobileCoachAuthentication(userId, mctoken), new UserConfigurationForAuthentication(userConfiguration));
	}



	private MattermostUserConfiguration fetchUserConfiguration(
			final String authentication) {
		MattermostUserConfiguration userConfig;

		if(mattMgmtService.existsUserForParticipant(authentication)){
			userConfig = mattMgmtService.getUserConfiguration(authentication);
		}else{
			userConfig = mattMgmtService.createParticipantUser(authentication);
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
