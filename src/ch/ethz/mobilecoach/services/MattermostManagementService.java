package ch.ethz.mobilecoach.services;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.httpclient.HttpMethodBase;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.InDataBaseVariableStore;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mobilecoach.chatlib.engine.translation.SimpleTranslator;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableException;
import ch.ethz.mobilecoach.interventions.PathMate;
import ch.ethz.mobilecoach.model.persistent.MattermostUserConfiguration;
import ch.ethz.mobilecoach.model.persistent.OneSignalUserConfiguration;
import ch.ethz.mobilecoach.model.persistent.subelements.MattermostChannel;
import ch.ethz.mobilecoach.model.persistent.subelements.MattermostUser;
import lombok.AllArgsConstructor;
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
	private final VariablesManagerService variablesManagerService;

	public final String api_url = Constants.getMattermostApiUrl();
	private final String emailHost = "localhost";

	private final String adminUserPassword = Constants.getMattermostAdminUserPassword();
	private final String adminUserName = Constants.getMattermostAdminUserName();
	private String adminUserToken = null;
	private String locale = "de";

	public final static String appID = "325068aa-fc63-411c-a07e-b3e73c455e8e";

	@Getter
	private String coachUserId = Constants.getMattermostCoachUserId();
	@Getter
	private String coachUserName = Constants.getMattermostCoachUserName();
	@Getter
	private String coachUserPassword = Constants.getMattermostCoachUserPassword();
	
	@Getter
	private final List<TeamConfiguration> teamConfigurations = new LinkedList<TeamConfiguration>();
	
	@AllArgsConstructor
	public class TeamConfiguration {
		
		public final Locale language;
		
		public final String teamId;
		
		public final String managerUserId;
		
		public final String managerUserName;
		
		public final String managerUserPassword;
		
		public String toString(){
			String languageString = language != null ? language.toLanguageTag() : "(any)";
			return languageString + ": " + managerUserName;
		}
	}



	public MattermostManagementService (DatabaseManagerService databaseManagerService, VariablesManagerService variablesManagerService){
		this.databaseManagerService = databaseManagerService;
		this.variablesManagerService = variablesManagerService;
		
		// add default configuration
		this.teamConfigurations.add(new TeamConfiguration (null, Constants.getMattermostTeamId(), Constants.getMattermostManagerUserId(),
						Constants.getMattermostManagerUserName(), Constants.getMattermostManagerUserPassword()));
		
		if (!"".equals(Constants.getMattermostTeamId_fr())){
			this.teamConfigurations.add(new TeamConfiguration (Locale.forLanguageTag(SimpleTranslator.FRENCH_CH), 
					Constants.getMattermostTeamId_fr(), Constants.getMattermostManagerUserId_fr(),
					Constants.getMattermostManagerUserName_fr(), Constants.getMattermostManagerUserPassword_fr()));
		}
	}

	public static MattermostManagementService start(DatabaseManagerService databaseManagerService, VariablesManagerService variablesManagerService){
		MattermostManagementService service = new MattermostManagementService(databaseManagerService, variablesManagerService);
		service.loginAdmin();
		return service;
	}

	/*
	 * 		Creation of Objects
	 */

	public OneSignalUserConfiguration findOneSignalObject(ObjectId participantId){

		OneSignalUserConfiguration oneSignalUserConfiguration = databaseManagerService.findOneModelObject(OneSignalUserConfiguration.class, "{'participantId':#}", participantId);

		return oneSignalUserConfiguration;
	}


	public void addDeviceToDatabase(OneSignalUserConfiguration oneSignalUserConfiguration, ObjectId authentication, String playerId){

		if(!oneSignalUserConfiguration.getPlayerIds().contains(playerId)) {
			removePlayerId(playerId);
			oneSignalUserConfiguration.getPlayerIds().add(playerId);
			databaseManagerService.saveModelObject(oneSignalUserConfiguration);
		}
	}
	
	
	/**
	 * Remove a given playerId from all OneSignalUserConfiguration objects.
	 * This is done to ensure that the user only gets push notifications for his most recent sign-up.
	 */
	public void removePlayerId(String playerId){
		// with large numbers of users, this may turn out to be too slow. Then, this should instead be done asynchronously.
		try {
		
			for (OneSignalUserConfiguration config: 
				databaseManagerService.findModelObjects(OneSignalUserConfiguration.class, "{'playerIds':#}", playerId)){
				
				config.getPlayerIds().remove(playerId);
				databaseManagerService.saveModelObject(config);
			}
		
		} catch (Exception e){
			log.error("Error removing playerId.", e);
		}
	}

	

	public void creatOneSignalObject(ObjectId participantId, String playerId){
		
		removePlayerId(playerId);

		long timestamp = System.currentTimeMillis(); 
		List<String> playerIds = new ArrayList<>();
		playerIds.add(playerId);
		OneSignalUserConfiguration config = new OneSignalUserConfiguration(participantId, playerIds, timestamp);

		databaseManagerService.saveModelObject(config);	
	}


	public MattermostUserConfiguration createParticipantUser(ObjectId participantId){
		ensureAuthentication();
		
		Participant participant = databaseManagerService
				.getModelObjectById(Participant.class, participantId);
		
		String userName = variablesManagerService.getVariableValue(participantId, "$username");
		String userId = variablesManagerService.getVariableValue(participantId, "$participantID");
		String userCoachName = variablesManagerService.getVariableValue(participantId, "$participantCoach");
		
		// For testing. TODO: remove
		if (userName == null || userName.equals("")){
			userName = "Tester";
			userId = "000";
			userCoachName = "Lukas";
		}
		
		String language = variablesManagerService.getVariableValue(participantId, "$participantLanguage");
		if (language == null || language.equals("")){
			language = "de"; // use German as default
		}
		
		TeamConfiguration team = this.getTeamConfiguration(participant.getLanguage());
		
		MattermostUserConfiguration config = createMattermostUser(userName, userId, team.teamId, language.substring(0, 2));
		addUserToTeam(config.getUserId(), team.teamId);
		
		String coachingChannelName = userName + " (" + userCoachName + ") " + userId;
		String managerChannelName = userName + " (" + PathMate.SUPPORT_NAME + ") " + userId;
		
		MattermostChannel coachingChannel = createPrivateChannel(userCoachName, coachingChannelName, "BOT", team.teamId);
		MattermostChannel managerChannel = createPrivateChannel(PathMate.SUPPORT_NAME, managerChannelName, "HUMAN", team.teamId);

		List<MattermostChannel> channels = config.getChannels();
		channels.add(coachingChannel);
		channels.add(managerChannel);

		List<MattermostUser> users = config.getUsers();
		users.add(new MattermostUser(this.coachUserId, userCoachName));
		users.add(new MattermostUser(config.getUserId(), userName));
		users.add(new MattermostUser(team.managerUserId, PathMate.SUPPORT_NAME));	

		addUserToChannel(config.getUserId(), coachingChannel.getId(), team.teamId);
		addUserToChannel(coachUserId, coachingChannel.getId(), team.teamId);
		addUserToChannel(team.managerUserId, coachingChannel.getId(), team.teamId);

		addUserToChannel(config.getUserId(), managerChannel.getId(), team.teamId);
		addUserToChannel(team.managerUserId, managerChannel.getId(), team.teamId);

		config.setParticipantId(participantId);
		databaseManagerService.saveModelObject(config);

		return config;
	}

	private TeamConfiguration getTeamConfiguration(Locale language) {
		for (TeamConfiguration tc: teamConfigurations){
			if (tc.language != null && language.toLanguageTag().equals(tc.language.toLanguageTag())){
				return tc;
			}
		}
		
		return teamConfigurations.get(0); // return default
	}
	
	

	private MattermostChannel createPrivateChannel(String nameForUser, String nameForManager, String type, String teamId){
		JSONObject json = new JSONObject()
				.put("name", UUID.randomUUID().toString())
				.put("display_name", nameForManager)
				.put("type", "P"); // private channel

		String channelId = new MattermostTask<String>(api_url + "teams/"+teamId+"/channels/create", json){
			@Override
			String handleResponse(HttpMethodBase method) throws Exception {
				return new JSONObject(method.getResponseBodyAsString()).getString("id");
			}
		}.setToken(adminUserToken).run();
		return new MattermostChannel(nameForUser, type, channelId);
	}

	private void addUserToChannel(String userId, String channelId, String teamId){

		JSONObject json = new JSONObject()
				.put("user_id", userId);
		
		try {

			new MattermostTask<Void>(api_url + "teams/"+teamId+"/channels/"+channelId+"/add", json){
				@Override
				Void handleResponse(HttpMethodBase method) throws Exception {
					log.debug(method.getResponseBodyAsString());
					return null;
				}
			}.setToken(adminUserToken).run();
		
		} catch (Exception e){
			log.error(e.getMessage(), e);
		}
	}

	private MattermostUserConfiguration createMattermostUser(String firstName, String lastName, String teamId, String locale){
		String username = UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString(); // TODO: use a cryptographically secure random generator
		String email = username + "@" + emailHost;

		JSONObject json = new JSONObject()
				.put("email", email)
				.put("password", password);
		
		if (firstName != null){
			json.put("first_name", firstName);
		}
		if (lastName != null){
			json.put("last_name", lastName);
		}
		
		json.put("username", username);

		log.info("Created Mattermost User: "+ username + " : " + password);

		String userId = new MattermostTask<String>(api_url + "users/create", json){
			@Override
			String handleResponse(HttpMethodBase method) throws Exception {
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
				locale, channels, users, teamId, this.api_url,
				timestamp, timestamp);
	}

	private void addUserToTeam(String userId, String teamId){
		JSONObject json = new JSONObject().put("user_id", userId);
		new MattermostTask<Void>(api_url + "teams/" + teamId + "/add_user_to_team", json).setToken(adminUserToken).run();
	}

	/*
	 * 		Providing Information
	 */
	
	public boolean isValidTeamId(String teamId){
		for (TeamConfiguration tc: teamConfigurations){
			if (tc.teamId.equals(teamId)){
				return true;
			}
		}
		return false;
	}

	public boolean existsUserForParticipant(ObjectId participantId){
		return null != databaseManagerService.findOneModelObject(MattermostUserConfiguration.class, "{'participantId':#}", participantId);
	}

	public MattermostUserConfiguration getUserConfiguration(ObjectId participantId){
		MattermostUserConfiguration config =  databaseManagerService.findOneModelObject(MattermostUserConfiguration.class, "{'participantId':#}", participantId);

		if (config != null) {
			long tokenRenewalAfter = config.getTokenTimestamp() + 1000 * 24 * 3600 * TOKEN_RENEWAL_AFTER_DAYS;
			if (System.currentTimeMillis() > tokenRenewalAfter){
				String token = createATokenForUser(config.getEmail(), config.getPassword());
				config.setToken(token);
				config.setTokenTimestamp(System.currentTimeMillis());
				databaseManagerService.saveModelObject(config);
			}
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
				.put("login_id", adminUserName)
				.put("password", adminUserPassword);

		final MattermostManagementService self = this;
		new MattermostTask<Void>(api_url + "users/login", json){
			@Override
			Void handleResponse(HttpMethodBase method){
				self.adminUserToken = method.getResponseHeader("Token").getValue();
				return null;
			}
		}.run();
	}


	private String createATokenForUser(String userId, String password){

		JSONObject json = new JSONObject()
				.put("login_id", userId)
				.put("password", password);

		String token = new MattermostTask<String>(api_url + "users/login", json){
			@Override
			String handleResponse(HttpMethodBase method){
				return method.getResponseHeader("Token").getValue();
			}
		}.run();
		return token;
	}
	
	/*
	 * 		Getting Information from Mattermost
	 */
	
	public String getTeamName(String teamId){
		try {
			
			String teamName = new MattermostTask<String>(api_url + "teams/"+teamId+"/me"){
				@Override
				String handleResponse(HttpMethodBase method) throws Exception {
					return new JSONObject(method.getResponseBodyAsString()).getString("name");
				}
			}.setToken(adminUserToken).run();
			return teamName;
			
		} catch (Exception e){
			e.printStackTrace();
			return "";
		}
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
