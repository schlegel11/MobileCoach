package ch.ethz.mobilecoach.services;

import java.net.URI;
import java.util.LinkedHashMap;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;

/**
 * Sends and receives messages from a Mattermost instance.
 * 
 * @author Dominik Rüegger
 * 
 * 
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
	
	public static MattermostMessagingService start(MattermostManagementService managementService){
		MattermostMessagingService service = new MattermostMessagingService(managementService);
		service.login();
		service.connectToWebSocket();
		
		return service;
	}
	
	
	private void connectToWebSocket(){
		final String authToken = mcUserToken;	
		WebSocketConfigurator configurator = new WebSocketConfigurator(authToken);		
		ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create().configurator(configurator).build();
				
	    WebSocketEndpoint ws = new WebSocketEndpoint();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
			container.connectToServer(ws, clientConfig, new URI(managementService.host_url.replaceFirst("http:", "ws:") + "users/websocket"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
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
		
        String channelId = managementService.getUserConfiguration(recipient).getChannels().get(0).getId();
        String teamId = managementService.getTeamId(recipient);
        String userId = managementService.getUserId(recipient);
		
        JSONObject json = new JSONObject();
        json.put("message", message);
        json.put("user_id", userId);
        json.put("channel_id", channelId);
        
		new MattermostTask<Void>(managementService.host_url + "teams/" + teamId + "/channels/" + channelId + "/posts/create", json)
			.setToken(mcUserToken).run();
	}
	
	// Login
	
	private void login(){        
        mcUserToken = null;        
        JSONObject json = new JSONObject();
        json.put("login_id", managementService.getMcUserLogin());
        json.put("password", managementService.getMcUserPassword());
          
		this.mcUserToken = new MattermostTask<String>(managementService.host_url + "users/login", json){

			@Override
			String handleResponse(PostMethod method){
				return method.getResponseHeader("Token").getValue();
			}
		}.run();
	}
	
	private void ensureLoggedIn(){
		if (mcUserToken == null){
			login();
		}
	}
	
	/*
	 *  Receiving
	 */
	
	LinkedHashMap<String, MessageListener> listeners = new LinkedHashMap<>();
	
	
	public void setListener(String userId, MessageListener listener){
		listeners.put(userId, listener);
	}
	
	
	void receiveMessage(String senderId, String message){
		if (listeners.containsKey(senderId)){
			listeners.get(senderId).receiveMessage(message);
		}
	}
	
	
	/*
	 *  Endpoint for the WebSocket
	 */
	public class WebSocketEndpoint extends Endpoint {
		
		
		private void receiveMessageAtEndpoint(String senderId, String message){
			receiveMessage(senderId,message);
		}

		@OnOpen
		public void onOpen(Session session, EndpointConfig config) {

			session.addMessageHandler(new MessageHandler.Whole<String>() {

				@Override
				public void onMessage(String msg) {
					
					// parse message
					
					JSONObject message = new JSONObject(msg);
					if ("posted".equals(message.getString("event"))){
						JSONObject post = message.getJSONObject("data").getJSONObject("post");
						
						String userId = post.getString("user_id");
						String messageText = post.getString("message");
						
						receiveMessageAtEndpoint(userId, messageText);						
					}
				}
			});
			
			
		}
	}
}
