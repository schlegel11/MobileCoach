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

import org.json.JSONObject;

import ch.ethz.mobilecoach.model.persistent.OneSignalUserConfiguration;
import ch.ethz.mobilecoach.services.MattermostManagementService;

@Path("/app/v01")
public class OneSignalAppService {
	
	private final static String  USER_ID = "userid:";
	private final static String DEVICE_ID = "deviceid";
	private final static String DEVICE_TYPE ="device_type";
	private final static int UPPER_RANGE_DEVICES = 9;
	private final static int LOWER_RANGE_DEVICES = 0;

	private MattermostManagementService mattMgmtService;


	public OneSignalAppService(MattermostManagementService mattMgmtService){

		this.mattMgmtService = mattMgmtService;	
	}


	@POST
	@Path("/setdeviceid")
	@Consumes("application/json")
	public String setDeviceIdentifier(@Context final HttpServletRequest request, @HeaderParam("Authentication") final String authentication, String content) throws BadRequestException{

		if (authentication == null){
			throw new WebApplicationException(Response.status(400).entity("Missing header 'Authenication'!").build());
		}
		
		if(!authentication.startsWith(USER_ID)){
			throw new WebApplicationException(Response.status(400).entity("Invalid Credential Format!").build());
		}
		
		if(content == null){
			throw new WebApplicationException(Response.status(400).entity("No Content was sent!").build());
		}
		
		JSONObject jsonObject = new JSONObject(content);
		String deviceID = jsonObject.getString(DEVICE_ID);
		String deviceType = jsonObject.getString(DEVICE_TYPE);
		
		if(Integer.valueOf(deviceType) < LOWER_RANGE_DEVICES &&  Integer.valueOf(deviceType) > UPPER_RANGE_DEVICES ){
			throw new WebApplicationException(Response.status(400).entity("No Matching Device Found!").build());
		}
		
		deviceID = deviceID.replaceAll(USER_ID, "");
		
		OneSignalUserConfiguration tmp = mattMgmtService.findOneSignalObject(authentication);
		
		if(tmp != null){

			mattMgmtService.addDeviceToOneSignal(tmp, authentication, deviceID, deviceType);
		}else{
			mattMgmtService.creatOneSignalObject(authentication, deviceID, deviceType);
		}
		return "";

	}
}
