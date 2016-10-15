package ch.ethz.mobilecoach.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;

@Log4j
public class MattermostManagementService {
	
	public final String host = "http://localhost:8065/";
	public final String emailHost = "localhost";
	public final String teamId = "13sufqasuigx3jgskph45g7mic"; // TODO: map the intervention to a team
	
	private String adminUserPassword = "12345678";
	private String adminUserLogin = "dm1"; // will use name or email from configuration
	private String adminUserToken = null;
	
	@Getter
	private String mcUserPassword;	
	@Getter
	private String mcUserLogin;	
	
	private HashMap<String, ParticipantCredentials> participants;
	
	public static MattermostManagementService start(){
		MattermostManagementService service = new MattermostManagementService();
		service.loginAdmin();
		service.createMobileCoachUser();
		return service;
	}
	
	/*
	 * 		Creation of Objects
	 */
	
	public void createMobileCoachUser(){
		ensureAuthentication();
		ParticipantCredentials credentials = createMattermostUser();
		mcUserLogin = credentials.email;
		mcUserPassword = credentials.password;
	}
	
	public void createParticipantUser(String participantId){
		ensureAuthentication();
		ParticipantCredentials credentials = createMattermostUser();
		String coachingChannelId = createPrivateChannel(); // TODO: save the channel with the user
		participants.put(participantId, credentials);
	}
	
	private String createPrivateChannel(){
		JSONObject json = new JSONObject()
			.put("name", UUID.randomUUID().toString())
			.put("display_name", "channel")
			.put("type", "P"); // private channel
		
		return new MattermostTask<String>(host + "api/v3/teams/"+teamId+"/channels/create", json){
			@Override
			String handleResponse(PostMethod method) throws Exception {
				return new JSONObject(method.getResponseBodyAsString()).getString("id");
			}
		}.run();
	}
	
	private ParticipantCredentials createMattermostUser(){
		String username = UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString(); // TODO: use a cryptographically secure random generator
		String email = username + "@" + emailHost;
		
		JSONObject json = new JSONObject()
        	.put("email", email)     
        	.put("username", username)
        	.put("password", password);
        
		new MattermostTask<String>(host + "api/v3/users/create", json){
			@Override
			String handleResponse(PostMethod method) throws Exception {
				return new JSONObject(method.getResponseBodyAsString()).getString("id");
			}
		}.setToken(adminUserToken).run();
		
        
        return new ParticipantCredentials(email, password);
	}
	
	/*
	 * 		Authentication
	 */
	
	public void ensureAuthentication(){
		if (adminUserToken == null){
			loginAdmin();
		}	
	}
	
	private void loginAdmin(){        
        this.adminUserToken = null;        
        JSONObject json = new JSONObject()
        	.put("login_id", adminUserLogin)
        	.put("password", adminUserPassword);
        
        final MattermostManagementService self = this;
		new MattermostTask<Void>(host + "api/v3/users/login", json){
			@Override
			Void handleResponse(PostMethod method){
				self.adminUserToken = method.getResponseHeader("Token").getValue();
				return null;
			}
		}.run();
	}
	

	/*
	 * 		Utility Classes
	 */
	
	class ParticipantCredentials {
		public final String email;
		public final String password;
		
		public ParticipantCredentials(String email, String password){
			this.email = email;
			this.password = password;
		}
	}
}
