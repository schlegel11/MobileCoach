package ch.ethz.mobilecoach.services;

import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;

import lombok.Getter;

/*
 * Responsibilities:
 * 
 * - logging in to Mattermost with the coach's account
 * - sending messages to Mattermost
 * - receiving messages from Mattermost
 */

public class MattermostMessagingService implements MessagingService {
	
	private MattermostManagementService managementService;
	private String mcUserToken;
	

	// Construction
	
	private MattermostMessagingService(MattermostManagementService managementService){
		this.managementService = managementService;
	}
	
	
	public static MessagingService start(MattermostManagementService managementService){
		MattermostMessagingService service = new MattermostMessagingService(managementService);
		service.login();
		return service;
	}
	
	// Sending
	
	/* (non-Javadoc)
	 * @see ch.ethz.mobilecoach.services.MessagingService#sendMessage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void sendMessage(String sender, String recipient, String message){
		ensureLoggedIn();
		
		if (!managementService.existsUserForParticipant(recipient)){
			// TODO: create the user before, when the user completed the sign up survey
			managementService.createParticipantUser(recipient);
		}
		
        String channelId = managementService.getCoachingChannelId(recipient);
        String teamId = managementService.getTeamId(recipient);
        String userId = managementService.getUserId(recipient);
		
        JSONObject json = new JSONObject();
        json.put("message", message);
        json.put("user_id", userId);
        json.put("channel_id", channelId);
        
		new MattermostTask<Void>(managementService.host + "api/v3/teams/" + teamId + "/channels/" + channelId + "/posts/create", json)
			.setToken(mcUserToken).run();
	}
	
	// Login
	
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
	
	private void ensureLoggedIn(){
		if (mcUserToken == null){
			login();
		}
	}
}
