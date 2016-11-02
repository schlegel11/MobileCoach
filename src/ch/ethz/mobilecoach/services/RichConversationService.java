package ch.ethz.mobilecoach.services;

import java.io.InputStream;

import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.variables.InMemoryVariableStore;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableStore;
import ch.ethz.mobilecoach.chatlib.engine.xml.DomParser;

public class RichConversationService {

	private MattermostMessagingService	mattermostMessagingService;

	private RichConversationService(
			MattermostMessagingService mattermostMessagingService) {
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

		ConversationRepository repository = new ConversationRepository();

		InputStream stream = this.getClass().getResourceAsStream(
				"/test-conversation1.xml");
		VariableStore variableStore = new InMemoryVariableStore();
		DomParser parser = new DomParser(repository, null, variableStore);
		try {
			parser.parse(stream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// prepare the engine
		// ChatEngine engine = new ChatEngine(repository, conversationUI,
		// variableStore);

	}

	public static RichConversationService start(
			MattermostMessagingService mattermostMessagingService) {
		RichConversationService service = new RichConversationService(
				mattermostMessagingService);
		return service;
	}

	public void startTestConversation() {
		ConversationRepository repository = new ConversationRepository();

		InputStream stream = this.getClass().getResourceAsStream(
				"/test-conversation1.xml");
		VariableStore variableStore = new InMemoryVariableStore();
		DomParser parser = new DomParser(repository, null, variableStore);
		try {
			parser.parse(stream);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void sendMessage(String sender, String recipient, String message) {
		mattermostMessagingService.sendMessage(sender, recipient, message);
	}

}
