package ch.ethz.mobilecoach.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.httpclient.methods.PostMethod;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mobilecoach.model.persistent.MattermostUserConfiguration;
import ch.ethz.mobilecoach.model.persistent.OneSignalUserConfiguration;
import ch.ethz.mobilecoach.model.persistent.subelements.MattermostChannel;
import ch.ethz.mobilecoach.model.persistent.subelements.MattermostUser;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

/**
 * 
 * Manages accounts and channels in a Mattermost instance.
 * 
 * @author Dominik Rüegger
 * 
 * 
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

	private final int TOKEN_RENEWAL_AFTER_DAYS = 10;

	private final DatabaseManagerService databaseManagerService;

	public final String host_url = "http://dev.cdhi.ethz.ch/api/v3/";
	public final String emailHost = "localhost";
	public final String teamId = "zx19spwe9py78noo7qyxyyj8yy"; // TODO: map the intervention to a team

	private String adminUserPassword = "c4dhimatrchtr";
	private String adminUserLogin = "admin"; // will use name or email from configuration
	private String adminUserToken = null;
	private String locale = "de";

	public final static String appID = "325068aa-fc63-411c-a07e-b3e73c455e8e";

	@Getter
	private String managerUserId = "95x5w43ywty4myq83pr9ri6dsy";
	@Getter
	private String managerUserPassword = "mobile";	
	@Getter
	private String managerUserLogin = "coach";	


	@Getter
	private String mcUserId = "rhwwrccsgjgwfjj8jwroxpcroc";
	@Getter
	private String mcUserPassword = "165dba95-5ed3-4642-aeba-ccbc5de9cf67";	
	@Getter
	private String mcUserLogin = "49b9c6c2-3a1d-4633-b44a-ebb2a3e70e57@localhost";	



	public MattermostManagementService (DatabaseManagerService databaseManagerService){
		this.databaseManagerService = databaseManagerService;
	}

	public static MattermostManagementService start(DatabaseManagerService databaseManagerService){
		MattermostManagementService service = new MattermostManagementService(databaseManagerService);
		service.loginAdmin();
		//service.createMobileCoachUser(); // TODO: create a new Coach user if none exists or is configured
		return service;
	}

	/*
	 * 		Creation of Objects
	 */

	public void createMobileCoachUser(){
		ensureAuthentication();
		
		MattermostUserConfiguration credentials = createMattermostUser();
		addUserToTeam(credentials.getUserId(), teamId);
		mcUserId = credentials.getUserId();
		mcUserLogin = credentials.getEmail();
		mcUserPassword = credentials.getPassword();
	}


	public OneSignalUserConfiguration findOneSignalObject(ObjectId participantId){

		OneSignalUserConfiguration oneSignalUserConfiguration = databaseManagerService.findOneModelObject(OneSignalUserConfiguration.class, "{'participantId':#}", participantId);

		return oneSignalUserConfiguration;
	}


	public void addDeviceToDatabase(OneSignalUserConfiguration oneSignalUserConfiguration, ObjectId authentication, String playerId){

		if(!oneSignalUserConfiguration.getPlayerIds().contains(playerId)) {

			oneSignalUserConfiguration.getPlayerIds().add(playerId);
			databaseManagerService.saveModelObject(oneSignalUserConfiguration);
		}
	}

	

	public void creatOneSignalObject(ObjectId participantId, String playerId){

		long timestamp = System.currentTimeMillis(); 
		List<String> playerIds = new ArrayList<>();
		playerIds.add(playerId);
		OneSignalUserConfiguration config = new OneSignalUserConfiguration(participantId, playerIds, timestamp);

		databaseManagerService.saveModelObject(config);	
	}



	public MattermostUserConfiguration createParticipantUser(ObjectId participantId){
		ensureAuthentication();
		MattermostUserConfiguration config = createMattermostUser();

		addUserToTeam(config.getUserId(), teamId);

		String userShortId = config.getUserId().substring(0, 5);
		MattermostChannel coachingChannel = createPrivateChannel(userShortId + " Coaching", "BOT");
		MattermostChannel managerChannel = createPrivateChannel(userShortId + " Support", "HUMAN");

		List<MattermostChannel> channels = config.getChannels();
		channels.add(coachingChannel);
		channels.add(managerChannel);

		List<MattermostUser> users = config.getUsers();
		users.add(new MattermostUser(this.mcUserId, "Coach"));
		users.add(new MattermostUser(config.getUserId(), "You"));
		users.add(new MattermostUser(managerUserId, "Manager"));		

		addUserToChannel(config.getUserId(), coachingChannel.getId());
		addUserToChannel(mcUserId, coachingChannel.getId());
		addUserToChannel(managerUserId, coachingChannel.getId());

		addUserToChannel(config.getUserId(), managerChannel.getId());
		addUserToChannel(managerUserId, managerChannel.getId());

		config.setParticipantId(participantId);
		databaseManagerService.saveModelObject(config);

		return config;
	}

	private MattermostChannel createPrivateChannel(String name, String type){
		JSONObject json = new JSONObject()
				.put("name", UUID.randomUUID().toString())
				.put("display_name", name)
				.put("type", "P"); // private channel

		String channelId = new MattermostTask<String>(host_url + "teams/"+teamId+"/channels/create", json){
			@Override
			String handleResponse(PostMethod method) throws Exception {
				return new JSONObject(method.getResponseBodyAsString()).getString("id");
			}
		}.setToken(adminUserToken).run();
		return new MattermostChannel(name, type, channelId);
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

	private MattermostUserConfiguration createMattermostUser(){
		String username = UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString(); // TODO: use a cryptographically secure random generator
		String email = username + "@" + emailHost;

		JSONObject json = new JSONObject()
				.put("email", email)     
				.put("username", username)
				.put("password", password);

		log.info("Created Mattermost User: "+ username + " : " + password);

		String userId = new MattermostTask<String>(host_url + "users/create", json){
			@Override
			String handleResponse(PostMethod method) throws Exception {
				return new JSONObject(method.getResponseBodyAsString()).getString("id");
			}
		}.setToken(adminUserToken).run();

		List<MattermostChannel> channels = new ArrayList<MattermostChannel>();		
		List<MattermostUser> users = new ArrayList<>();

		String token = createATokenForUser(username, password);

		// Save a timestamp, mostly to know when the token was created.
		// Use *real* time, as Mattermost also uses real (not simulated) time
		long timestamp = System.currentTimeMillis(); 

		return new MattermostUserConfiguration(
				null, userId, email, password, token, 
				this.locale, channels, users, this.teamId, this.host_url,
				timestamp, timestamp);
	}

	private void addUserToTeam(String userId, String teamId){
		JSONObject json = new JSONObject().put("user_id", userId);
		new MattermostTask<Void>(host_url + "teams/" + teamId + "/add_user_to_team", json).setToken(adminUserToken).run();
	}

	/*
	 * 		Providing Information
	 */

	public String getTeamId(ObjectId participantId) {
		// TODO: return team id based on the intervention
		return this.teamId;
	}

	public Boolean existsUserForParticipant(ObjectId participantId){
		return null != databaseManagerService.findOneModelObject(MattermostUserConfiguration.class, "{'participantId':#}", participantId);
	}

	public MattermostUserConfiguration getUserConfiguration(ObjectId participantId){
		MattermostUserConfiguration config =  databaseManagerService.findOneModelObject(MattermostUserConfiguration.class, "{'participantId':#}", participantId);

		long tokenRenewalAfter = config.getTokenTimestamp() + 1000 * 24 * 3600 * TOKEN_RENEWAL_AFTER_DAYS;
		if (config != null && (System.currentTimeMillis() > tokenRenewalAfter)){
			String token = createATokenForUser(config.getEmail(), config.getPassword());
			config.setToken(token);
			config.setTokenTimestamp(System.currentTimeMillis());
			databaseManagerService.saveModelObject(config);
		}

		return config;
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


	public static class UserConfigurationForAuthentication {

		private MattermostUserConfiguration userConfiguration;

		public UserConfigurationForAuthentication(MattermostUserConfiguration userConfiguration){
			this.userConfiguration = userConfiguration;
		}

		public String getUser_id() {
			return userConfiguration.getUserId();
		}

		public String getToken() {
			return userConfiguration.getToken();
		}

		public String getLocale() {
			return userConfiguration.getLocale();
		}

		public List<MattermostChannel> getChannels() {
			return userConfiguration.getChannels();
		}

		public List<MattermostUser> getUsers() {	
			return userConfiguration.getUsers();
		}

		public String getTeam_id() {
			return userConfiguration.getTeamId();
		}

		public String getUrl() {
			return userConfiguration.getUrl();
		}
	}
}
