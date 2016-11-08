package ch.ethz.mobilecoach.services;

import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.ServletContext;

import ch.ethz.mobilecoach.chatlib.engine.conversation.ConversationUI;
import ch.ethz.mobilecoach.chatlib.engine.conversation.UserReplyListener;
import ch.ethz.mobilecoach.chatlib.engine.ChatEngine;
import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.ExecutionException;
import ch.ethz.mobilecoach.chatlib.engine.model.Message;
import ch.ethz.mobilecoach.chatlib.engine.variables.InMemoryVariableStore;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableStore;
import ch.ethz.mobilecoach.chatlib.engine.xml.DomParser;
import ch.ethz.mobilecoach.services.MattermostManagementService.UserConfiguration;

public class RichConversationService {

	private MattermostMessagingService	mattermostMessagingService;
	
	private ConversationRepository repository = new ConversationRepository();
	
	private HashMap<String, VariableStore> variableStores = new HashMap<>();
	private HashMap<String, ChatEngine> chatEngines = new HashMap<>();	

	private RichConversationService(MattermostMessagingService mattermostMessagingService, ServletContext context) throws Exception {
		this.mattermostMessagingService = mattermostMessagingService;

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
		 * 
		 * [ ] Create a new conversationUI implementation which uses WebSockets / REST 
		 *     to communicate with the Mattermost server.
		 */

		InputStream stream = this.getClass().getResourceAsStream(context.getRealPath("/test-conversation1.xml"));
		DomParser parser = new DomParser(repository, null);
		parser.parse(stream);
	}

	public static RichConversationService start(
			MattermostMessagingService mattermostMessagingService, ServletContext context) throws Exception {
		RichConversationService service = new RichConversationService(mattermostMessagingService, context);
		return service;
	}

	public void sendMessage(String sender, String recipient, String message) throws ExecutionException {
		final String START_CONVERSATION_PREFIX = "start-conversation:";
		if (message.startsWith(START_CONVERSATION_PREFIX)){
			
			// start a conversation
			VariableStore variableStore = new InMemoryVariableStore();
			MattermostConnector ui = new MattermostConnector();
			ChatEngine engine = new ChatEngine(repository, ui, variableStore);
			chatEngines.put(recipient, engine);
			
			String conversation = message.substring(START_CONVERSATION_PREFIX.length());
			engine.startConversation(conversation);
			
		} else {
			// stop conversation
			if (chatEngines.containsKey(recipient)){
				chatEngines.remove(recipient);
			}
			
			// send a message
			mattermostMessagingService.sendMessage(sender, recipient, message);
		}
	}
	
	private class MattermostConnector implements ConversationUI {
		
		private UserReplyListener listener;

		@Override
		public void showMessage(Message message) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setUserReplyListener(UserReplyListener listener) {
			this.listener = listener;
		}

		@Override
		public void delay(Runnable callback, Integer milliseconds) {
			// TODO Auto-generated method stub
			callback.run();
		}
		
	}

}
