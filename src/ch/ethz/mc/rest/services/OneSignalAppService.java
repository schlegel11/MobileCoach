package ch.ethz.mc.rest.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import org.json.JSONObject;

import ch.ethz.mc.model.persistent.AppToken;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mobilecoach.model.persistent.OneSignalUserConfiguration;
import ch.ethz.mobilecoach.services.MattermostManagementService;

@Path("/app/v01")
public class OneSignalAppService {
	
	private final static String  USER_ID = "userid:";
	private final static String PLAYER_ID = "playerid";

	private RESTManagerService restManagerService;
	private MattermostManagementService mattMgmtService;

	public OneSignalAppService(RESTManagerService restManagerService, MattermostManagementService mattMgmtService) {
		this.restManagerService = restManagerService;
		this.mattMgmtService = mattMgmtService;
	}



	@POST
	@Path("/setplayerid")
	@Consumes("application/json")
	public String setDeviceIdentifier(@Context final HttpServletRequest request, @HeaderParam("Authentication") final String authentication, String content) throws BadRequestException{

		if (authentication == null){
			throw new WebApplicationException(Response.status(400).entity("Missing header 'Authentication'!").build());
		}
		
		if(!AppToken.isAppToken(authentication)){
			throw new WebApplicationException(Response.status(400).entity("Invalid Credential Format!").build());
		}
		
		if(content == null){
			throw new WebApplicationException(Response.status(400).entity("No Content was sent!").build());
		}
		
		JSONObject jsonObject = new JSONObject(content);
		String playerId = jsonObject.getString(PLAYER_ID);
		
		
		ObjectId userId = restManagerService.findParticipantIdForAppToken(authentication);
		if (userId == null){
			throw new WebApplicationException(Response.status(403).entity("Invalid Token!").build());
		}
		
		OneSignalUserConfiguration tmp = mattMgmtService.findOneSignalObject(userId);
		
		if(tmp != null){

			mattMgmtService.addDeviceToDatabase(tmp, userId, playerId);
		}else{
			mattMgmtService.creatOneSignalObject(userId, playerId);
		}
		return "";
	}
}
