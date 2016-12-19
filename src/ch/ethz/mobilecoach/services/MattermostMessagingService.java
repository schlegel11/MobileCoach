package ch.ethz.mobilecoach.services;

import lombok.extern.log4j.Log4j2;

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
import org.bson.types.ObjectId;
import org.json.JSONObject;

import ch.ethz.mobilecoach.app.Post;
import ch.ethz.mobilecoach.app.Results;
import ch.ethz.mobilecoach.model.persistent.MattermostUserConfiguration;
import ch.ethz.mobilecoach.model.persistent.OneSignalUserConfiguration;

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

@Log4j2
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
	public void sendMessage(String sender, ObjectId recipient, String message){
		Post post = new Post();
		post.setMessage(message);
		sendMessage(sender, recipient, post);
	}
	
	public void ensureParticipantExists(ObjectId recipient){
		if (!managementService.existsUserForParticipant(recipient)){
			managementService.createParticipantUser(recipient);
		}
	}
	

	public void sendMessage(String sender, ObjectId recipient, Post post){
		ensureLoggedIn();
		ensureParticipantExists(recipient);	
		
		MattermostUserConfiguration config = managementService.getUserConfiguration(recipient);
        String channelId = config.getChannels().get(0).getId();
        String teamId = config.getTeamId();
        String userId = config.getUserId();
        
        senderIdToRecipient.put(userId, recipient);
		
        JSONObject json = new JSONObject();
        json.put("props", new JSONObject(post));
        json.put("message", post.getMessage());
        json.put("user_id", userId);
        json.put("channel_id", channelId);
        
		new MattermostTask<Void>(managementService.host_url + "teams/" + teamId + "/channels/" + channelId + "/posts/create", json)
			.setToken(mcUserToken).run();
		
		if(null == managementService.findOneSignalObject(recipient)){
			return;
		}
		
		try {
			sendPushNotification(recipient, post);
		} catch (Exception exception){
			log.error("Error sending push notification: ", exception);
		}
	}
	
	private int seq = 0;	
	
	@Override
	public void indicateTyping(String sender, ObjectId recipient) {
		ensureLoggedIn();
		ensureParticipantExists(recipient);
		
		MattermostUserConfiguration config = managementService.getUserConfiguration(recipient);
        String channelId = config.getChannels().get(0).getId();
        
        // example message:
        // {"action":"user_typing","seq":1,"data":{"channel_id":"uk475zcxnibdxfh1r88r1x7d1w","parent_id":""}}
        
        JSONObject data = new JSONObject();
        data.put("channel_id", channelId);
        
        JSONObject message = new JSONObject();
        message.put("action", "user_typing");
        message.put("seq", seq);
        message.put("data", data);
        
        seq++;
		
		webSocketEndpoint.sendMessage(message.toString());
	}

	
	
	public void sendPushNotification(ObjectId recipient, Post post) {

		OneSignalUserConfiguration oneSignalUserConfiguration = managementService.findOneSignalObject(recipient);
		
		if (oneSignalUserConfiguration == null){
			// TODO Dominik/Filipe: handle this case appropriately
			return;
		}
		
		String[] playerIds = new String[oneSignalUserConfiguration.getPlayerIds().size()];
		
		for(int ind = 0; ind < oneSignalUserConfiguration.getPlayerIds().size(); ind++){
			playerIds[ind] = oneSignalUserConfiguration.getPlayerIds().get(ind);
		}

		LinkedHashMap<String, String> headers = new LinkedHashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", "Basic ZjA3ZTkzNDEtYmRjMi00Y2M2LWEwOWItZTk2MzE2YTQ0NWQw");	
		String url = "https://onesignal.com/api/v1/notifications";
		
		String message = post.getMessage();
		if (message == null || "".equals(message)){
			message = "New message"; // TODO: translate
		}

		JSONObject json2 = new JSONObject()
				.put("app_id", MattermostManagementService.appID)     
				.put("contents", new JSONObject().put("en", message))
				.put("include_player_ids", playerIds);

		new OneSignalTask<String>(url, json2, headers){
			@Override
			String handleResponse(PostMethod method) throws Exception {
				return new JSONObject(method.getResponseBodyAsString()).getString("id");
			}
		}.run();	
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
	LinkedHashMap<String, ObjectId> senderIdToRecipient = new LinkedHashMap<>();
	
	
	LinkedHashMap<ObjectId, MessageListener> listenerForRecipient = new LinkedHashMap<>();
	
	
	public void setListener(ObjectId recipient, MessageListener listener){
		listenerForRecipient.put(recipient, listener);
	}
	
	
	private void receiveMessage(String senderId, Post post){
		if (senderIdToRecipient.containsKey(senderId)){
			ObjectId recipient = senderIdToRecipient.get(senderId);
			if (listenerForRecipient.containsKey(recipient)){
				listenerForRecipient.get(recipient).receivePost(post);
			}
		}
	}
	
	
	
	/*
	 *  Endpoint for the WebSocket
	 */
	public class WebSocketEndpoint extends Endpoint {
		
		Session session;
		
		
		private void receiveMessageAtEndpoint(String senderId, Post post){
			receiveMessage(senderId, post);
		}
		
		
		@Override
		public void onClose(Session session, CloseReason closeReason){
			log.error("WebSocket connection closed. Reason: " + closeReason.toString());
			if (onCloseListener != null){
				onCloseListener.run();
			}
		}
		
		@Override
		public void onError(Session session, Throwable thr){
			log.error(thr.getMessage());
		}
		
			
		public void sendMessage(String message) {
			session.getAsyncRemote().sendText(message);
		}
		
		
		@Override
		public void onOpen(Session session, EndpointConfig config) {
			
			log.info("WebSocket connection opened.");
			
			this.session = session;

			session.addMessageHandler(new MessageHandler.Whole<String>() {

				@Override
				public void onMessage(String msg) {
					
					// parse message
					
					log.debug("WebSocket message received: " + msg); // TODO (DR): don't log all messages that are received
					
					JSONObject message = new JSONObject(msg);
					String event = message.optString("event");
					
					/*
					if ("ping".equals(event)){
						sendMessage("{\"event\":\"pong\"}");
					}
					*/
					
					
					if ("posted".equals(event)){
						try { 							
							JSONObject data = message.getJSONObject("data");
							String postString = data.getString("post");
							
							JSONObject post = new JSONObject(postString);
			
							String userId = post.getString("user_id");
							String messageText = post.getString("message");
							
							JSONObject props = post.getJSONObject("props");
							Results results = null;
							
							if (props.has("results")){
								results = new Results(props.getJSONObject("results").getString("selected"));
							}
							
							Post postObject = new Post();
							postObject.setMessage(messageText);
							postObject.setResults(results);
							
							receiveMessageAtEndpoint(userId, postObject);	
						
						} catch (Exception e){
							e.printStackTrace();
						}											
					}
				}

			});
		}
	}
}
