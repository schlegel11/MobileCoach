package ch.ethz.mobilecoach.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.json.JSONArray;

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
		service.resync(); // TODO: remove this. instead, getUnreceivedMessages() should be called with the channelIds of ongoing conversations
		
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
				resync(); // make sure we get messages that we missed while the WebSocket was closed
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
	
	private int seq = 0; // TODO: we might need to store this in the database
	
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
	
	// Reconnecting
	
	public void resync(){
		getUnreceivedMessages(getChannelsToUpdate());
	}
	
	public void getUnreceivedMessages(List<String> channelIds){
		
		// get missed messages
		
		System.out.println("Getting messages from Mattermost (" + channelIds.size() + " channels) " + System.currentTimeMillis());
		
		try {
	        RequestConfig requestConfig = RequestConfig.custom()
	                .setSocketTimeout(3000)
	                .setConnectTimeout(3000).build();
        
            CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
            try {
                httpclient.start();
                final CountDownLatch latch = new CountDownLatch(channelIds.size());
                for (final String channelId: channelIds) {
                	String sinceTime = "0"; // TODO: use last received time
                	//sinceTime = new Long(System.currentTimeMillis()).toString();
                	
                	//https://your-mattermost-url.com/api/v3/teams/{team_id}/channels/{channel_id}/posts/since/{time}
                	HttpGet request = new HttpGet(
                			managementService.host_url + "teams/" + managementService.teamId + "/channels/" + channelId + "/posts/since/" + sinceTime );
          	
                	request.addHeader("Authorization", "Bearer " + this.mcUserToken);
                	
                    httpclient.execute(request, new FutureCallback<HttpResponse>() {

                        @Override
                        public void completed(final HttpResponse response) {
                        	String responseContent = "";
                        	try {
								responseContent = new BasicResponseHandler().handleResponse(response);
							} catch (Exception e) {
								log.error("Error getting messages from channel", e);
							}
                        	JSONObject result = new JSONObject(responseContent);
                        	JSONArray order = result.getJSONArray("order");
                        	JSONObject posts = result.getJSONObject("posts");
                        	
                        	if (order.length() > 0){
                        		String lastPostId = order.getString(0);
                        		JSONObject post = posts.getJSONObject(lastPostId);
                        		
    							String userId = post.getString("user_id");
    							Post postObject = MattermostMessagingService.JSONtoPost(post);
    							receiveMessage(userId, postObject);
                        	}

                            latch.countDown();
                        }

                        @Override
                        public void failed(final Exception ex) {
                            latch.countDown();
                            log.error("Request failed.", ex);
                        }

                        @Override
                        public void cancelled() {
                            latch.countDown();
                            log.debug(request.getRequestLine() + " cancelled");
                        }

                    });
                }
                latch.await();
                System.out.println("Getting messages from Mattermost: done "  + System.currentTimeMillis());
            } finally {
                httpclient.close();
            }

            
		} catch (Exception e){
			log.error("Error getting messages from Mattermost channels.", e);
		}
	}
	
	/**
	 * This method might not be needed as we can simply get the list of channels to update by taking the channels for the ongoing conversations.
	 * @return
	 */
	
	public List<String> getChannelsToUpdate(){
		HttpClient client = new HttpClient();
        
        GetMethod request = new GetMethod(managementService.host_url + "teams/" + managementService.teamId + "/channels/");
		request.setRequestHeader("Authorization", "Bearer " + mcUserToken);
		
		String channelsResponse = "[]";

		try {
			
            int responseCode = client.executeMethod(request);
            if (responseCode != HttpStatus.SC_OK) {
            	throw new Exception("Status " + new Integer(responseCode) + ": " + request.getResponseBodyAsString());
            }
            channelsResponse = request.getResponseBodyAsString();
		} catch (Exception e) {
			log.error("Error getting channels", e);
		}
        
		
		JSONArray channels = new JSONArray(channelsResponse);
		
		
		// TODO: find all the channels with updates that are not received yet
		
		List<String> channelsToUpdate = new LinkedList<>();
		
		for (Object o: channels){
			if (o instanceof JSONObject){
				JSONObject jo = ((JSONObject) o);
				String id = jo.getString("id");
				
 				channelsToUpdate.add(id);
			}
		}
		
		return channelsToUpdate;
	}
	
		
	@AllArgsConstructor
	private class IncomingMessage {
		@Getter
		private String senderId;
		
		@Getter
		private Post post;
	}
	
	public static Post JSONtoPost(JSONObject obj){
		String messageText = obj.getString("message");
		JSONObject props = obj.getJSONObject("props");
		Results results = null;
		
		if (props.has("results")){
			results = new Results(props.getJSONObject("results").getString("selected"));
		}
		
		Post postObject = new Post();
		postObject.setMessage(messageText);
		postObject.setResults(results);
		
		return postObject;
	}
	
	
	/*
	 *  Endpoint for the WebSocket
	 */
	public class WebSocketEndpoint extends Endpoint {
		
		Session session;
		
		
		/**
		 * This function may be called by the websocket's thread
		 * 
		 * @param senderId
		 * @param post
		 */
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
			
							Post postObject = MattermostMessagingService.JSONtoPost(post);
							
							String userId = post.getString("user_id");
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
