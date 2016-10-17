package ch.ethz.mobilecoach.services;

import java.util.HashMap;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;

/*
 * Responsibilities:
 * - logging in to Mattermost with the admin account
 * - creating a Mattermost user for the coach
 * - creating a Mattermost user for a participant given his/her phone number or other id
 * - creating a private channel for a participant, which allows the coach and the participant to communicate
 * 
 * 
 * Future responsibilities (TODO):
 * - creating a Mattermost team for each intervention
 */

@Log4j
public class MattermostManagementService {
	
	public final String host = "http://localhost:8065/";
	public final String emailHost = "localhost";
	public final String teamId = "13sufqasuigx3jgskph45g7mic"; // TODO: map the intervention to a team
	
	private String adminUserPassword = "12345678";
	private String adminUserLogin = "dm1"; // will use name or email from configuration
	private String adminUserToken = null;
	
	@Getter
	private String mcUserId;
	@Getter
	private String mcUserPassword;	
	@Getter
	private String mcUserLogin;	
	
	private HashMap<String, UserConfiguration> participants = new HashMap<>();
	
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
		UserConfiguration credentials = createMattermostUser();
		addUserToTeam(credentials.userId, teamId);
		mcUserId = credentials.userId;
		mcUserLogin = credentials.email;
		mcUserPassword = credentials.password;
	}
	
	public void createParticipantUser(String participantId){
		ensureAuthentication();
		UserConfiguration credentials = createMattermostUser();
		addUserToTeam(credentials.userId, teamId);
		String coachingChannelId = createPrivateChannel();
		addUserToChannel(credentials.userId, coachingChannelId);
		addUserToChannel(mcUserId, coachingChannelId);
		credentials.setCoachingChannelId(coachingChannelId);
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
		}.setToken(adminUserToken).run();
	}
	
	private void addUserToChannel(String userId, String channelId){
		
		JSONObject json = new JSONObject()
			.put("user_id", userId);
	
		new MattermostTask<Void>(host + "api/v3/teams/"+teamId+"/channels/"+channelId+"/add", json){
			@Override
			Void handleResponse(PostMethod method) throws Exception {
				log.error(method.getResponseBodyAsString());
				return null;
			}
		}.setToken(adminUserToken).run();
	}
	
	private UserConfiguration createMattermostUser(){
		String username = UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString(); // TODO: use a cryptographically secure random generator
		String email = username + "@" + emailHost;
		
		JSONObject json = new JSONObject()
        	.put("email", email)     
        	.put("username", username)
        	.put("password", password);
		
		// TODO: remove logging
		log.error("***** CREATED MOBILECOACH USER *****: "+ username + " : " + password);
        
		String userId = new MattermostTask<String>(host + "api/v3/users/create", json){
			@Override
			String handleResponse(PostMethod method) throws Exception {
				return new JSONObject(method.getResponseBodyAsString()).getString("id");
			}
		}.setToken(adminUserToken).run();
        
        return new UserConfiguration(userId, email, password);
	}
	
	private void addUserToTeam(String userId, String teamId){
		JSONObject json = new JSONObject()
    		.put("user_id", userId);
		new MattermostTask<Void>(host + "api/v3/teams/" + teamId + "/add_user_to_team", json).setToken(adminUserToken).run();
	}
	
	/*
	 * 		Providing Information
	 */
	
	public String getCoachingChannelId(String participantId) {
		return participants.get(participantId).coachingChannelId;
	}
	
	public String getUserId(String participantId) {
		return participants.get(participantId).getCoachingChannelId();
	}

	public String getTeamId(String participantId) {
		// TODO: return team id based on the intervention
		return this.teamId;
	}
	
	public Boolean existsUserForParticipant(String participantId){
		return participants.containsKey(participantId);
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
	
	
	class UserConfiguration {
		public final String email;
		public final String password;
		public final String userId;
		
		@Setter
		@Getter
		private String coachingChannelId = null;
		
		public UserConfiguration(String userId, String email, String password){
			this.userId = userId;
			this.email = email;
			this.password = password;
		}
		
		public UserConfiguration(String userId, String email, String password, String coachingChannelId){
			this.userId = userId;
			this.email = email;
			this.password = password;
			this.coachingChannelId = coachingChannelId;
		}
		
	}

}
