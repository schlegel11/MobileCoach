package ch.ethz.mobilecoach.services.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import org.apache.commons.httpclient.HttpMethodBase;
import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.mobilecoach.services.MattermostTask;

public class MattermostTool {

	public static void main(String[] args) {
		
		if (args.length < 3){
			System.out.println("No valid command given. Examples: ");
			System.out.println("https://www.pm.c4dhi-ac.ethz.ch:8443/api/v3/ admin create-team clara Clara");
			System.out.println("https://www.pm.c4dhi-ac.ethz.ch:8443/api/v3/ admin create-user [team-id] clara-coach");
		}
		
		String url = args[0];
		String adminName = args[1];		
		String operation = args[2];
		
		String adminPassword;
		try {
			adminPassword = getPasswordFromConsole();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		String token = login(url, adminName, adminPassword);
		
		if ("create-user".equals(operation)){
			createUser(token, args[3], args[4], url, adminName, "");
		} else if ("add-user-to-team".equals(operation)){
			String team = args[3];
			String userId = args[4];
			
			addUserToTeam(url, token, userId, team);
		} else if ("create-team".equals(operation)){
			String teamName = args[3];
			String teamDisplayName = args.length > 4 ? args[4] : args[3];
			
			String teamId = createTeam(teamName, teamDisplayName, url, token);
		} else if ("new-team".equals(operation)){
			try {
			String teamDisplayName = readFromConsole("Team Display Name");
			String teamName = readFromConsole("Team id name");
			
			String teamId = createTeam(teamName, teamDisplayName, url, token);
			
			String userId;
			do {
				userId = readFromConsole("Id of user to add to team");
				addUserToTeam(url, token, userId, teamId);
				
			} while (!"".equals(userId));
			
			} catch (IOException e){
				e.printStackTrace();
			}
			
		}
		
		System.out.println("Done.");
	}
	
	private static String createTeam(String teamName, String teamDisplayName, String url, String token){
        JSONObject json = new JSONObject();
        json.put("name", teamName);
        json.put("display_name", teamDisplayName);
        json.put("type", "O");
          
		String teamId = new MattermostTask<String>(url + "teams/create", json){
			@Override
			protected String handleResponse(HttpMethodBase method) throws JSONException, IOException {
				return new JSONObject(method.getResponseBodyAsString()).getString("id");
			}
		}.setToken(token).run();
		
		System.out.println("mattermostTeamId = " + teamId);
		System.out.println();
		
		return teamId;
	}

	private static void createUser(String token, String team, String username, String url, String adminName, String ouputPrefix) {

		String password = UUID.randomUUID().toString(); // TODO: use a cryptographically secure random generator
		String email = username + "@localhost";

		JSONObject json = new JSONObject()
				.put("email", email)
				.put("password", password)
				.put("username", username);
		

		String userId = new MattermostTask<String>(url + "users/create", json){
			@Override
			protected String handleResponse(HttpMethodBase method) throws Exception {
				return new JSONObject(method.getResponseBodyAsString()).getString("id");
			}
		}.setToken(token).run();
		
		addUserToTeam(url, token, userId, team);
		
		System.out.println(ouputPrefix + "UserId = " + userId);
		System.out.println(ouputPrefix + "UserName = " + username);
		System.out.println(ouputPrefix + "UserPassword = " + password);
	}
	
	private static String readFromConsole(String prompt) throws IOException{
		System.out.print(prompt+": ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
	}
	
	private static String getPasswordFromConsole() throws IOException{
		System.out.print("Password: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String result = br.readLine();
        System.out.println("Executing ...");
        return result;
	}
	
	
	private static String login(String url, String username, String password){   
        JSONObject json = new JSONObject();
        json.put("login_id", username);
        json.put("password", password);
          
		return new MattermostTask<String>(url + "users/login", json){

			@Override
			protected String handleResponse(HttpMethodBase method){
				return method.getResponseHeader("Token").getValue();
			}
		}.run();
	}
	
	private static void addUserToTeam(String url, String token, String userId, String teamId){
		JSONObject json = new JSONObject().put("user_id", userId);
		new MattermostTask<Void>(url + "teams/" + teamId + "/add_user_to_team", json).setToken(token).run();
	}

}
