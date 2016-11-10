package ch.ethz.mobilecoach.services;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

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

	public final String host_url = "http://dev.cdhi.ethz.ch/api/v3/";
	public final String emailHost = "localhost";
	public final String teamId = "zx19spwe9py78noo7qyxyyj8yy"; // TODO: map the intervention to a team

	private String adminUserPassword = "c4dhimatrchtr";
	private String adminUserLogin = "admin"; // will use name or email from configuration
	private String adminUserToken = null;
	private String locale = "de";


	@Getter
	private String mcUserId;
	@Getter
	private String mcUserPassword;	
	@Getter
	private String mcUserLogin;	

	private LinkedHashMap<String, UserConfiguration> participants = new LinkedHashMap<>();

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

	public UserConfiguration createParticipantUser(String participantId){
		ensureAuthentication();
		UserConfiguration credentials = createMattermostUser();
		addUserToTeam(credentials.userId, teamId);
		addUserToChannel(credentials.userId, credentials.getChannels().get(0).getId());
		addUserToChannel(mcUserId, credentials.getChannels().get(0).getId());
		participants.put(participantId, credentials);
		return credentials;
	}

	private Channel createPrivateChannel(String name, String type){
		JSONObject json = new JSONObject()
				.put("name", UUID.randomUUID().toString())
				.put("display_name", name)
				.put("type", type); // private channel

		String channelId= new MattermostTask<String>(host_url + "teams/"+teamId+"/channels/create", json){
			@Override
			String handleResponse(PostMethod method) throws Exception {
				return new JSONObject(method.getResponseBodyAsString()).getString("id");
			}
		}.setToken(adminUserToken).run();
		return new Channel(name, type, channelId);
	}

	private void addUserToChannel(String userId, String channelId){

		JSONObject json = new JSONObject()
				.put("user_id", userId);

		new MattermostTask<Void>(host_url + "teams/"+teamId+"/channels/"+channelId+"/add", json){
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

		String userId = new MattermostTask<String>(host_url + "users/create", json){
			@Override
			String handleResponse(PostMethod method) throws Exception {
				return new JSONObject(method.getResponseBodyAsString()).getString("id");
			}
		}.setToken(adminUserToken).run();

		Channel channel = createPrivateChannel("MC Hammer Channel", "P");
		List<Channel> channels = new ArrayList<Channel>(); 
		channels.add(channel);
		User user = new User(this.mcUserId, "MC Hammer");
		List<User> users = new ArrayList<>();
		users.add(user);
		String token = createATokenForUser(username, password);

		return new UserConfiguration(userId, email, password, token, this.locale, channels, users);
	}

	private void addUserToTeam(String userId, String teamId){
		JSONObject json = new JSONObject()
				.put("user_id", userId);
		new MattermostTask<Void>(host_url + "teams/" + teamId + "/add_user_to_team", json).setToken(adminUserToken).run();
	}

	/*
	 * 		Providing Information
	 */

	public List<Channel> getChannels(String participantId) {
		return participants.get(participantId).getChannels();
	}

	public String getUserId(String participantId) {
		return participants.get(participantId).getUserId();
	}

	public String getTeamId(String participantId) {
		// TODO: return team id based on the intervention
		return this.teamId;
	}

	public Boolean existsUserForParticipant(String participantId){

		return participants.containsKey(participantId);

	}

	public UserConfiguration getUserConfiguration(String participantId){
		return participants.get(participantId);
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
		new MattermostTask<Void>(host_url + "users/login", json){
			@Override
			Void handleResponse(PostMethod method){
				self.adminUserToken = method.getResponseHeader("Token").getValue();
				return null;
			}
		}.run();
	}


	private String createATokenForUser(String userId, String password){

		JSONObject json = new JSONObject()
				.put("login_id", userId)
				.put("password", password);

		String token = new MattermostTask<String>(host_url + "users/login", json){
			@Override
			String handleResponse(PostMethod method){
				return method.getResponseHeader("Token").getValue();
			}
		}.run();
		return token;
	}

	/*
	 * 		Utility Classes
	 */

	@AllArgsConstructor
	public class UserConfiguration implements UserConfigurationIfc {
		@Getter
		private final String userId;
		@Getter
		private final String email;
		@Getter
		private final String password;
		@Getter
		private final String token;
		@Getter
		private final String locale;
		@Getter
		private final List<Channel> channels;
		@Getter
		private final List<User> users;
	}

	
	public interface UserConfigurationIfc{
		public String getUserId();
		public String getEmail();
		public String getToken();
		public String getLocale();
		public List<Channel> getChannels();
		public List<User>getUsers();
	}
	

	public class UserConfigurationForAuthentication implements UserConfigurationIfc{

		private UserConfiguration userConfiguration;

		public UserConfigurationForAuthentication(UserConfiguration userConfiguration){
			this.userConfiguration = userConfiguration;
		}

		@Override
		public String getUserId() {
			return userConfiguration.getUserId();
		}

		@Override
		public String getEmail() {
			return userConfiguration.getEmail();
		}

		@Override
		public String getToken() {
			return userConfiguration.getToken();
		}

		@Override
		public String getLocale() {
			return userConfiguration.getLocale();
		}

		@Override
		public List<Channel> getChannels() {
			return userConfiguration.getChannels();
		}

		@Override
		public List<User> getUsers() {	
			return userConfiguration.getUsers();
		}
	}


	@AllArgsConstructor
	public class Channel{
		@Getter
		private String name;
		@Getter
		private String type;
		@Getter
		private String id;
	}

	@AllArgsConstructor
	public class User{
		@Getter
		private String name;
		@Getter
		private String id;
	}

}