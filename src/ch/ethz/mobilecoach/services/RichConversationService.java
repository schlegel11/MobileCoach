package ch.ethz.mobilecoach.services;

import java.io.InputStream;

import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;

import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.variables.InMemoryVariableStore;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableStore;
import ch.ethz.mobilecoach.chatlib.engine.xml.DomParser;
import lombok.Getter;

public class RichConversationService {
	
	private MattermostMessagingService mattermostMessagingService;
	
	private RichConversationService(MattermostMessagingService mattermostMessagingService){
		this.mattermostMessagingService = mattermostMessagingService;
	}
	
	
	public static RichConversationService start(MattermostMessagingService mattermostMessagingService){
		RichConversationService service = new RichConversationService(mattermostMessagingService);
		return service;
	}
	
	
	public void startTestConversation(){
        ConversationRepository repository = new ConversationRepository();

        InputStream stream = this.getClass().getResourceAsStream("/test-conversation1.xml");
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
