package ch.ethz.mobilecoach.services;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.InDataBaseVariableStore;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mobilecoach.app.Post;
import ch.ethz.mobilecoach.app.Results;
import ch.ethz.mobilecoach.chatlib.engine.Evaluator;
import ch.ethz.mobilecoach.chatlib.engine.Input;
import ch.ethz.mobilecoach.chatlib.engine.variables.InMemoryVariableStore;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableStore;
import ch.ethz.mobilecoach.model.persistent.MattermostUserConfiguration;
import ch.ethz.mobilecoach.model.persistent.OneSignalUserConfiguration;
import ch.ethz.mobilecoach.model.persistent.UserLastMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

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
	
	private final DatabaseManagerService databaseManagerService;
	private final MattermostManagementService managementService;
	private final VariablesManagerService variablesManagerService;
	
	private String mcUserToken;
	
	private WebSocketEndpoint webSocketEndpoint;
	
	private Runnable onCloseListener;
	
	@AllArgsConstructor
	public class Channel {
		
		public final String channelId;
		public final String teamId;
	}

	// Construction
	
	private MattermostMessagingService(MattermostManagementService managementService, 
									   DatabaseManagerService databaseManagerService, 
									   VariablesManagerService variablesManagerService){
		this.managementService = managementService;
		this.databaseManagerService = databaseManagerService;
		this.variablesManagerService = variablesManagerService;
	}	
	
	public static MattermostMessagingService start(MattermostManagementService managementService, 
			                                       DatabaseManagerService databaseManagerService,
			                                       VariablesManagerService variablesManagerService){
		MattermostMessagingService service = new MattermostMessagingService(managementService, databaseManagerService, variablesManagerService);
		service.login();		
		return service;
	}
	
	public void startReceiving(){
		connectToWebSocket();
		resync(); // TODO: remove this. instead, getUnreceivedMessages() should be called with the channelIds of ongoing conversations
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
        
        boolean connected = false;
        
        int sleepAmount = 1000;
        
        while(!connected){
            try {
            	String wsUrl = managementService.api_url.replaceFirst("http:", "ws:").replaceFirst("https:", "wss:");
    			container.connectToServer(webSocketEndpoint, clientConfig, new URI(wsUrl + "users/websocket"));
    			return;
    		} catch (Exception e) {
    			log.warn(e);
    			try {
    				log.debug("Waiting for "+sleepAmount+" msec.");
    				Thread.sleep(sleepAmount);
    				sleepAmount *= 2;
    			} catch (InterruptedException e1) {
    				log.error(e1);
    			}
    		}
        }
	}

	
	// Sending
	
	@Override
	public void sendMessage(String sender, ObjectId recipient, String message){
		
		Post post = new Post();
		post.setId(UUID.randomUUID().toString());
		post.setMessage(message);
		sendMessage(sender, recipient, post, false);
	}
	
	public void ensureParticipantExists(ObjectId recipient){
		if (!managementService.existsUserForParticipant(recipient)){
			managementService.createParticipantUser(recipient);
		}
	}
	

	public void sendMessage(String sender, ObjectId recipient, Post post, boolean pushOnly){
		ensureLoggedIn();
		ensureParticipantExists(recipient);	
		
		//TODO Dominik: REMOVE THIS TESTING CODE
		/*
		if (!post.getOptions().isEmpty()){
			test_closeWebsocketFor10Sec();
		}
		*/
		
		MattermostUserConfiguration config = managementService.getUserConfiguration(recipient);
        String channelId = config.getChannels().get(0).getId();
        String teamId = config.getTeamId();
        String userId = config.getUserId();
        
               
        if (!managementService.isValidTeamId(teamId) && Constants.isMattermostAllowOnlyConfiguredTeams()){
        	// This caused sending requests to fail with error 403: Forbidden
        	// We abort and accept that after changing the teamId, conversations on the older team cannot continue.
        	log.error("Could not send message to conversation using unused teamId: " + teamId);
        	return;
        }
        
        senderIdToRecipient.put(userId, recipient);
        
        if (!pushOnly){
	        JSONObject json = new JSONObject();
	        json.put("props", new JSONObject(post));
	        json.put("message", post.getMessage());
	        json.put("user_id", userId);
	        json.put("channel_id", channelId);
	        
	        try {
				new MattermostTask<Void>(managementService.api_url + "teams/" + teamId + "/channels/" + channelId + "/posts/create", json)
					.setToken(mcUserToken).run();
	        } catch (Exception e){
	        	
	        	// add user to channel
	        	try {
	        		managementService.addUserToTeam(managementService.getCoachUserId(), teamId);
	        	} catch (Exception e1){
	        		// do nothing
	        	}
	        	
	        	try {
	        		managementService.addUserToChannel(managementService.getCoachUserId(), channelId, teamId);
	        	} catch (Exception e2){
	        		// do nothing
	        	}
	        	
	        	// try again
				new MattermostTask<Void>(managementService.api_url + "teams/" + teamId + "/channels/" + channelId + "/posts/create", json)
				.setToken(mcUserToken).run();
	        }
        }
		
        // send push notifications only after 1 minute after the last message was received, unless the message is push-only
		if (pushOnly || post.getHidden() != true && wasLastMessageReceivedLongerAgoThan(channelId, 60 * 1000)){
			
			if(null == managementService.findOneSignalObject(recipient)){
				log.info("Could not send push since OnSignal config missing: " + recipient);
			} else {
				try {
					sendPushNotification(recipient, post, channelId, userId);
				} catch (Exception e){
					log.warn("Error sending push notification: " + StringHelpers.getStackTraceAsLine(e), e);
				}
			}
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
		
        try {
			webSocketEndpoint.sendMessage(message.toString());
			
		} catch (Exception e){
			//log.error("Error sending typing indicator: " + StringHelpers.getStackTraceAsLine(e), e);
		}
        
        seq++;
	}

	
	
	public void sendPushNotification(ObjectId recipient, Post post, String channelId, String userId) {

		OneSignalUserConfiguration oneSignalUserConfiguration = managementService.findOneSignalObject(recipient);
		
		if (oneSignalUserConfiguration == null){
			// TODO Dominik/Filipe: handle this case appropriately
			return;
		}
		
		String[] playerIds = new String[oneSignalUserConfiguration.getPlayerIds().size()];
		
		if (oneSignalUserConfiguration.getPlayerIds().isEmpty()){
			return;
		}

		String recipients = "";
		
		for(int ind = 0; ind < oneSignalUserConfiguration.getPlayerIds().size(); ind++){
			playerIds[ind] = oneSignalUserConfiguration.getPlayerIds().get(ind);
			recipients = recipients + playerIds[ind] + " ";
		}

		LinkedHashMap<String, String> headers = new LinkedHashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", "Basic " + Constants.getOneSignalApiKey());	
		String url = "https://onesignal.com/api/v1/notifications";
		
		//log.info("Sending push using key " + Constants.getOneSignalApiKey().substring(0, 5) + "... to " + recipients);
		
		String message = post.getMessage();
		if (message == null || "".equals(message)){
			message = "New message"; // TODO: translate
		}
		
		if (Constants.getPushMessage() != null){
			message = Constants.getPushMessage();
		}

		JSONObject data = new JSONObject();
		data.put("channel_id", channelId);
		data.put("message_id", post.getId());
		
		JSONObject json2 = new JSONObject()
				.put("app_id", Constants.getOneSignalAppId())     
				.put("contents", new JSONObject().put("en", message))
				.put("include_player_ids", playerIds)
				.put("data", data)
				.put("ios_badgeType", "SetTo")
				.put("ios_badgeCount", 1)
				.put("collapse_id", userId);

		String result = new OneSignalTask<String>(url, json2, headers){
			@Override
			protected
			String handleResponse(PostMethod method) throws Exception {
				log.info("Response from OneSignal: " + method.getResponseBodyAsString(100));
				if (method.getStatusCode() == 200){
					return null;
				}
				return "HTTP " + method.getStatusCode() + ": " + method.getResponseBodyAsString(200);
			}
		}.run();
		
		if (result != null){
			log.warn("Error sending push: " + result);
		}
	}
	
	
	
	// Login
	
	private void login(){        
        mcUserToken = null;        
        JSONObject json = new JSONObject();
        json.put("login_id", managementService.getCoachUserName());
        json.put("password", managementService.getCoachUserPassword());
          
		this.mcUserToken = new MattermostTask<String>(managementService.api_url + "users/login", json){

			@Override
			protected String handleResponse(HttpMethodBase method){
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
		
		// for continuing persisted conversations: find the Mattermost user id, if it exists, so that we can forward the incoming messages to the listener
		MattermostUserConfiguration config = managementService.getUserConfiguration(recipient);
		if (config != null){
			senderIdToRecipient.put(config.getUserId(), recipient);
		}
	}
	

	private void receiveMessage(String senderId, Post post){
		if (senderIdToRecipient.containsKey(senderId)){
			ObjectId recipient = senderIdToRecipient.get(senderId);
			if (listenerForRecipient.containsKey(recipient)){
				synchronized (recipient){
					if (this.wasMessageNotProcessedYet(recipient, post.getChannelId(), post.getId(), post.getCreateAt())){
						log.debug("Message accepted for processing: '" + post.getMessage() + "' "+ post.getId() + " to " + recipient + " at " + post.getCreateAt() + " on channel " + post.getChannelId());
					
						listenerForRecipient.get(recipient).receivePost(post);
						
						// mark this message as processed
						this.saveUserLastMessage(recipient, post.getChannelId(), post.getId(), post.getCreateAt());
						log.debug("Marked message as processed: '" + post.getMessage() + "' "+ post.getId());
					}
				}
			}
		}
	}
	
	// Reconnecting
	
	public void resync(){
		getUnreceivedMessages(getChannelsToUpdate());
	}
	
	public void getUnreceivedMessages(List<Channel> channelIds){
		
		// get missed messages
		
		long timingStart = System.currentTimeMillis();
		log.debug("Getting messages from Mattermost (" + channelIds.size() + " channels) ");
		
		Map<String, Long> channelLastMessage = new HashMap<>();
    	for (UserLastMessage ulm: this.databaseManagerService.findModelObjects(UserLastMessage.class, Queries.ALL)){

    		String channelId = ulm.getChannelId();
    		
    		if (!channelLastMessage.containsKey(channelId) || channelLastMessage.get(channelId) > ulm.getLastMessageTimestamp()){
    			// put if no value exists or new value is smaller
    			channelLastMessage.put(channelId, ulm.getLastMessageTimestamp());
    		}
    	};
		
		
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
                for (final Channel channel: channelIds) {
                	long sinceTime = 0L;
                	
                	if (channelLastMessage.containsKey(channel.channelId)){
                		sinceTime = channelLastMessage.get(channel.channelId);
                	}
                	
                	//https://your-mattermost-url.com/api/v3/teams/{team_id}/channels/{channel_id}/posts/since/{time}
                	String url = managementService.api_url + "teams/" + channel.teamId + "/channels/" + channel.channelId + "/posts/since/" + sinceTime;
                	
                	HttpGet request = new HttpGet( url );
          	
                	request.addHeader("Authorization", "Bearer " + this.mcUserToken);
                	
                    httpclient.execute(request, new FutureCallback<HttpResponse>() {

                        @Override
                        public void completed(final HttpResponse response) {
                        	String responseContent = "";
                        	try {
                        		
                                HttpEntity entity = response.getEntity();
								responseContent = EntityUtils.toString(entity, "UTF-8");
							} catch (Exception e) {
								log.error("Error getting messages from channel", e);
							}
                        	
                        	try {
	                        	JSONObject result = new JSONObject(responseContent);
	                        	JSONArray order = result.getJSONArray("order");
	                        	                        	
	                        	if (order.length() > 0){
	                        		JSONObject posts = result.getJSONObject("posts");
	                        		String lastPostId = order.getString(0);
	                        		JSONObject post = posts.getJSONObject(lastPostId);
	                        		
	                        		if (post.optString("type").equals("")){ // for normal posts, type should be empty
		    							String userId = post.getString("user_id");
		    							Post postObject = MattermostMessagingService.JSONtoPost(post);
		    							receiveMessage(userId, postObject);
	                        		}
	                        	}
                        	} catch (Exception e) {
								log.error("Error parsing response: " + e.getMessage(), e);
								log.error("Error parsing: " + responseContent);
							}

                            latch.countDown();
                        }

                        @Override
                        public void failed(final Exception ex) {
                            latch.countDown();
                            log.error("Request failed: " + ex.getMessage(), ex);
                        }

                        @Override
                        public void cancelled() {
                            latch.countDown();
                            log.debug(request.getRequestLine() + " cancelled");
                        }

                    });
                }
                latch.await();
                log.debug("Getting messages from Mattermost: done in " + (System.currentTimeMillis() - timingStart)/1000.0 + " sec");
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
	
	public List<Channel> getChannelsToUpdate(){
		HttpClient client = new HttpClient();
		
		List<Channel> channelsToUpdate = new LinkedList<>();
		
		Set<String> teamIds = new HashSet<>();
		for (MattermostManagementService.TeamConfiguration tc: managementService.getTeamConfigurations()){
			teamIds.add(tc.teamId);
		}
		
		for (String activeId: Constants.getMattermostAdditionalTeamIds().split(",")){
			String trimmed = activeId.trim();
			if (trimmed.length() > 5){
				teamIds.add(trimmed);
			}
		}
		
		for (String teamId: teamIds){
        
	        GetMethod request = new GetMethod(managementService.api_url + "teams/" + teamId + "/channels/");
			request.setRequestHeader("Authorization", "Bearer " + mcUserToken);
			
			String channelsResponse = "[]";
	
			try {
				
	            int responseCode = client.executeMethod(request);
	            if (responseCode != HttpStatus.SC_OK) {
	            	throw new Exception("Status " + new Integer(responseCode) + ": " + request.getResponseBodyAsString());
	            }
	            channelsResponse = request.getResponseBodyAsString();
			} catch (Exception e) {
				log.error("Error getting channels: " + e.getMessage(), e);
				
				continue;
			}
	        
			
			JSONArray channels = new JSONArray(channelsResponse);
			
			
			
			for (Object o: channels){
				if (o instanceof JSONObject){
					JSONObject jo = ((JSONObject) o);
					String id = jo.getString("id");
					long lastUpdateAt = jo.getLong("last_post_at");
					
					if (this.mayChannelHaveNewMessages(id, lastUpdateAt)){
						channelsToUpdate.add(new Channel(id, teamId));
					}
				}
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
			
		Post postObject = new Post();
		
		postObject.setMessage(messageText);
		postObject.setId(obj.getString("id"));
		postObject.setCreateAt(obj.getLong("create_at"));
		postObject.setChannelId(obj.getString("channel_id"));
		
		if ("result_keyboard".equals(props.optString(("post_type")))){
			postObject.setInput(new Input(messageText, null));
		}
		
		JSONObject results = props.optJSONObject("results");
		if (results != null){
			Map<String, Object> data = results.toMap();
			String resultValue = messageText;
			if (data.containsKey("selected")){
				resultValue = data.get("selected").toString();
			}

			postObject.setInput(new Input(resultValue, data));
		}
		
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
		
		public void test_close(){
			try {
				session.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		@Override
		public void onClose(Session session, CloseReason closeReason){
			log.warn("WebSocket connection closed. Reason: " + closeReason.toString());
			if (onCloseListener != null){
				onCloseListener.run();
			}
		}
		
		@Override
		public void onError(Session session, Throwable thr){
			log.warn(thr.getMessage());
		}
		
			
		public void sendMessage(String message) {
			if (session == null) throw new NullPointerException("Session is null.");
			
			Async async = session.getAsyncRemote();
			if (async == null) throw new NullPointerException("Async is null.");
			
			async.sendText(message);
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

					if ("posted".equals(event)){
						try { 							
							JSONObject data = message.getJSONObject("data");
							String postString = data.getString("post");
							
							JSONObject post = new JSONObject(postString);
			
							Post postObject = MattermostMessagingService.JSONtoPost(post);
							
							String userId = post.getString("user_id");
							receiveMessageAtEndpoint(userId, postObject);	
						
						} catch (Exception e){
							log.error(StringHelpers.getStackTraceAsLine(e), e);
						}											
					}
				}

			});
		}
	}
	
	// Database access
	
	private void saveUserLastMessage(ObjectId participantId, String channelId, String lastMessageId, long lastMessageTimestamp){
		UserLastMessage record = this.databaseManagerService.findOneModelObject(UserLastMessage.class, "{participantId:#, channelId:#}", participantId, channelId);
		
		if (record == null){
			record = new UserLastMessage(participantId, channelId, lastMessageId, lastMessageTimestamp);
		} else {
			record.setLastMessageId(lastMessageId);
			record.setLastMessageTimestamp(lastMessageTimestamp);
		}
		
		this.databaseManagerService.saveModelObject(record);
	}
	
	private boolean wasMessageNotProcessedYet(ObjectId participantId, String channelId, String lastMessageId, long timestamp){
		UserLastMessage record = this.databaseManagerService.findOneModelObject(UserLastMessage.class, "{participantId:#, channelId:#}", participantId, channelId);
		
		if (record == null){
			return true;
		}
		
		return record.getLastMessageTimestamp() < timestamp || 
				(record.getLastMessageTimestamp() == timestamp && !record.getLastMessageId().equals(lastMessageId));
	}
	
	private boolean mayChannelHaveNewMessages(String channelId, long lastUpdateAt){
		// we know that each channelId is only used by one user
		UserLastMessage record = this.databaseManagerService.findOneModelObject(UserLastMessage.class, "{channelId:#}", channelId);
		if (record == null){
			return true;
		}
		
		return lastUpdateAt > record.getLastMessageTimestamp();
	}
	
	private boolean wasLastMessageReceivedLongerAgoThan(String channelId, int milliseconds){
		UserLastMessage record = this.databaseManagerService.findOneModelObject(UserLastMessage.class, "{channelId:#}", channelId);
		if (record == null){
			return true;
		}
		
		return System.currentTimeMillis() > (record.getLastMessageTimestamp() + milliseconds);
	}
	
	// Functions for testing
	
	public void test_closeWebsocketFor10Sec(){
		Runnable runAfter = this.onCloseListener;
		this.onCloseListener = null;
		webSocketEndpoint.test_close();
		
		new java.util.Timer().schedule( 
			new java.util.TimerTask() {
				@Override
				public void run() {
					onCloseListener = runAfter;
					runAfter.run();
				}
			}, 
			10000 
		);
		
	}

	
	@Override
	public void setChannelName(ObjectId recipient) {
		
		// update channel name
		managementService.updateChannelName(recipient);
		
	}
}
