package ch.ethz.mobilecoach.services;

import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;

import lombok.Getter;

public class MattermostMessagingService {
	
	private MattermostManagementService managementService;
	
	private String mcUserToken;
	
	private void login(){        
        mcUserToken = null;        
        JSONObject json = new JSONObject();
        json.put("login_id", managementService.getMcUserLogin());
        json.put("password", managementService.getMcUserPassword());
        
        final MattermostMessagingService self = this;
		new MattermostTask<Void>(managementService.host + "api/v3/users/login", json){
			@Override
			Void handleResponse(PostMethod method){
				self.mcUserToken = method.getResponseHeader("Token").getValue();
				return null;
			}
		}.run();
	}
	
	public void addListener(){
		
		
	}
	
	private MattermostMessagingService(MattermostManagementService managementService){
		this.managementService = managementService;
	}
	
	
	public static MattermostMessagingService start(MattermostManagementService managementService){
		MattermostMessagingService service = new MattermostMessagingService(managementService);
		service.login();
		return service;
	}
	
	public void sendMessage(String recipientId){
		
		
	}

}
