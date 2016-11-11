package ch.ethz.mobilecoach.services;

import java.net.URI;
import java.util.LinkedHashMap;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;

import ch.ethz.mobilecoach.app.Post;

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
	
	private WebSocketEndpoint webSocketEndpoint;
	
	private Runnable onCloseListener;
	

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
		
		onCloseListener = new Runnable(){
			@Override
			public void run() {
				connectToWebSocket();
			}
		};
				
		webSocketEndpoint = new WebSocketEndpoint();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
			container.connectToServer(webSocketEndpoint, clientConfig, new URI(managementService.host_url.replaceFirst("http:", "ws:") + "users/websocket"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	
	

	
	// Sending
	
	@Override
	public void sendMessage(String sender, String recipient, String message){
		Post post = new Post();
		post.setMessage(message);
		sendMessage(sender, recipient, post);
	}

	public void sendMessage(String sender, String recipient, Post post){
		ensureLoggedIn();
		
		if (!managementService.existsUserForParticipant(recipient)){
			// TODO: create the user before, when the user completed the sign up survey
			managementService.createParticipantUser(recipient);
		}
		
        String channelId = managementService.getUserConfiguration(recipient).getChannels().get(0).getId();
        String teamId = managementService.getTeamId(recipient);
        String userId = managementService.getUserId(recipient);
        
        senderIdToRecipient.put(userId, recipient);
		
        JSONObject json = new JSONObject();
        json.put("props", new JSONObject(post));
        json.put("message", post.getMessage());
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
	
	// Mapping Mattermost userids to MobileCoach user ids
	LinkedHashMap<String, String> senderIdToRecipient = new LinkedHashMap<>();
	
	
	LinkedHashMap<String, MessageListener> listenerForRecipient = new LinkedHashMap<>();
	
	
	public void setListener(String recipient, MessageListener listener){
		listenerForRecipient.put(recipient, listener);
	}
	
	
	private void receiveMessage(String senderId, String message){
		if (senderIdToRecipient.containsKey(senderId)){
			String recipient = senderIdToRecipient.get(senderId);
			if (listenerForRecipient.containsKey(recipient)){
				listenerForRecipient.get(recipient).receiveMessage(message);
			}
		}
	}
	
	
	
	/*
	 *  Endpoint for the WebSocket
	 */
	public class WebSocketEndpoint extends Endpoint {
		
		
		private void receiveMessageAtEndpoint(String senderId, String message){
			receiveMessage(senderId,message);
		}
		
		
		@Override
		public void onClose(Session session, CloseReason closeReason){
			if (onCloseListener != null){
				onCloseListener.run();
			}
		}
		
		public void onError(Session session, Throwable thr){
			System.out.println(thr.getMessage());
		}
		
		
		@Override
		public void onOpen(Session session, EndpointConfig config) {

			session.addMessageHandler(new MessageHandler.Whole<String>() {

				@Override
				public void onMessage(String msg) {
					
					// parse message
					
					JSONObject message = new JSONObject(msg);
					if ("posted".equals(message.getString("event"))){
						try { 
							JSONObject data = message.getJSONObject("data");
							String postString = data.getString("post");
							JSONObject post = new JSONObject(postString);
							
			
							String userId = post.getString("user_id");
							String messageText = post.getString("message");
							
							receiveMessageAtEndpoint(userId, messageText);	
						
						} catch (Exception e){
							e.printStackTrace();
						}											
					}
				}

			});
		}
	}
}
