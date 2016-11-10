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

import ch.ethz.mobilecoach.services.MattermostManagementService;
import ch.ethz.mobilecoach.services.MattermostManagementService.UserConfiguration;
import ch.ethz.mobilecoach.services.MattermostManagementService.UserConfigurationIfc;
/**
 * Service to read/write variables using REST
 *
 * @author Filipe Barata
 */
@Path("/app/v01")
public class AppService {

	private final static String  CONST = "userid";

	private MattermostManagementService mattMgmtService;


	public AppService(MattermostManagementService mattMgmtService){

		this.mattMgmtService = mattMgmtService;	
	}


	@GET
	@Path("/getconfig")
	@Produces("application/json")
	public UserConfigurationIfc variableRead(@Context final HttpServletRequest request, @HeaderParam("Authentication") final String authentication) throws BadRequestException{

		if(!authentication.startsWith(CONST)){ 

			throw new WebApplicationException(Response
					.serverError()
					.entity("Invalid Credential Format").build());
		}
		
		UserConfiguration userConfiguration = fetchUserConfiguration(authentication);
		
		return this.mattMgmtService.new UserConfigurationForAuthentication(userConfiguration);
	}



	private UserConfiguration fetchUserConfiguration(
			final String authentication) {
		UserConfiguration userConfig;

		if(mattMgmtService.existsUserForParticipant(authentication)){
			userConfig = mattMgmtService.getUserConfiguration(authentication);
		}else{
			userConfig = mattMgmtService.createParticipantUser(authentication);
		}
		return userConfig;
	}
}
