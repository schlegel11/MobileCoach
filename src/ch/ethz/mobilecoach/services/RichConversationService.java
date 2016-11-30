package ch.ethz.mobilecoach.services;

import java.util.LinkedHashMap;


import ch.ethz.mobilecoach.app.Post;
import ch.ethz.mobilecoach.app.Option;
import ch.ethz.mobilecoach.chatlib.engine.ChatEngine;
import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.ExecutionException;
import ch.ethz.mobilecoach.chatlib.engine.conversation.ConversationUI;
import ch.ethz.mobilecoach.chatlib.engine.conversation.UserReplyListener;
import ch.ethz.mobilecoach.chatlib.engine.model.AnswerOption;
import ch.ethz.mobilecoach.chatlib.engine.model.Message;
import ch.ethz.mobilecoach.chatlib.engine.variables.InMemoryVariableStore;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableStore;
import lombok.Getter;

public class RichConversationService {

	private MessagingService messagingService;
	private ConversationManagementService conversationManagementService;
	private LinkedHashMap<String, VariableStore> variableStores = new LinkedHashMap<>();
	private LinkedHashMap<String, ChatEngine> chatEngines = new LinkedHashMap<>();	

	private RichConversationService(MessagingService mattermostMessagingService, ConversationManagementService conversationManagementService) throws Exception {
		this.messagingService = mattermostMessagingService;
		this.conversationManagementService = conversationManagementService;
		
		/*
		 * TODO:
		 * 
		 * [ ] Create a new VariableStore implementation, which uses
		 *     MobileCoach's VariablesManagerService.
		 * 
		 * [ ] Create a new ChatEngine for a given user whenever a new
		 *     conversation from an XML script is triggered from MobileCoach.
		 * 
		 * [ ] Put the conversation files in an appropriate place (as resources,
		 *     and load them into a ConversationRepository (in future: one per
		 *     intervention).
		 */

	}

	public static RichConversationService start(
			MessagingService messagingService, ConversationManagementService conversationManagementService) throws Exception {
		RichConversationService service = new RichConversationService(messagingService, conversationManagementService);
		return service;
	}

	public void sendMessage(String sender, String recipient, String message) throws ExecutionException {
		final String START_CONVERSATION_PREFIX = "start-conversation:";
		if (message.startsWith(START_CONVERSATION_PREFIX)){
			ConversationRepository repository = conversationManagementService.getRepository(null); // TODO: use Intervention id to get the repository
			
			// start a conversation
			// TODO (DR): make sure these objects get cleaned up when a new conversation starts
			VariableStore variableStore = new InMemoryVariableStore();
			MattermostConnector ui = new MattermostConnector(sender, recipient);
			ChatEngine engine = new ChatEngine(repository, ui, variableStore);
			chatEngines.put(recipient, engine);
			
			messagingService.setListener(recipient, ui);
			
			ui.setUserReplyListener(new UserReplyListener(){
				@Override
				public void userReplied(Message message) {
					String participantId = ui.getRecipient();
					
					if (chatEngines.containsKey(participantId)){
						engine.handleInput(message.answerOptionId);
					} else {
						// TODO (DR): store the message for the MC system to collect it. 
						//            This is not necessary for the PathMate2 intervention.
					}
				}
			});
			
			String conversation = message.substring(START_CONVERSATION_PREFIX.length());
			engine.startConversation(conversation);
			
		} else {
			// stop conversation
			if (chatEngines.containsKey(recipient)){
				chatEngines.remove(recipient);
			}
			
			// send a message
			messagingService.sendMessage(sender, recipient, message);
		}
	}
	
	private class MattermostConnector implements ConversationUI, MessagingService.MessageListener {
		
		private UserReplyListener listener;
		
		private String sender;
		
		@Getter
		private String recipient;
		
		public MattermostConnector(String sender, String recipient){
			this.sender = sender;
			this.recipient = recipient;
		}

		@Override
		public void showMessage(Message message) {
			if (Message.SENDER_COACH.equals(message.sender)){
				
				Post post = new Post();
				post.setMessage(message.text);
				
				if (message.answerOptions.size() > 0){
					post.setPostType(Post.POST_TYPE_REQUEST);
					String requestType = message.answerType != null ? message.answerType : Post.REQUEST_TYPE_SELECT_ONE;
					post.setRequestType(requestType);
					for (AnswerOption answerOption: message.answerOptions){
						Option option = new Option(answerOption.text, answerOption.value);
						post.getOptions().add(option);
					}
				} else if (message.objectId != null){
					post.setPostType(Post.POST_TYPE_REQUEST);
					post.setRequestType(message.objectId);
				} else if (message.answerType != null){
					post.setPostType(Post.POST_TYPE_REQUEST);
					post.setRequestType(message.answerType);
				}
				
				messagingService.sendMessage(sender, recipient, post);
			}
		}

		@Override
		public void setUserReplyListener(UserReplyListener listener) {
			this.listener = listener;
		}

		@Override
		public void delay(Runnable callback, Integer milliseconds) {
			// TODO implement a better delay
			new java.util.Timer().schedule( 
			        new java.util.TimerTask() {
			            @Override
			            public void run() {
			            	callback.run();
			            }
			        }, 
			        milliseconds 
			);
		}

		public void receivePost(Post post) {
			if (this.listener != null){
				Message msg = new Message(post.getMessage(), Message.SENDER_USER);
				if (post.getResults() != null){
					msg.answerOptionId = post.getResults().getSelected();
				}
				this.listener.userReplied(msg);
			}
		}
		
	}

}
