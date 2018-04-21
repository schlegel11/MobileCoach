package ch.ethz.mobilecoach.services.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.junit.Test;

import ch.ethz.mobilecoach.app.Post;
import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.ExecutionException;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableStore;
import ch.ethz.mobilecoach.chatlib.engine.xml.DomParser;
import ch.ethz.mobilecoach.services.ConversationManagementService;
import ch.ethz.mobilecoach.services.MessagingService;
import ch.ethz.mobilecoach.services.RichConversationService;


public class RichConversationServiceTest {

    @Test
    public void test() throws Exception {
    	
    	MessagingServiceMock ms = new MessagingServiceMock();
    	
    	ConversationManagementServiceMock conversationManagementService = new ConversationManagementServiceMock();
    	RichConversationService service = RichConversationService.start(ms, conversationManagementService, null, null, null);
    	
    	service.sendMessage("dummy", new ObjectId(), "start-conversation:test-conversation1");
   	
    	assertEquals("Hi there!", ms.messages.get(0));
    	assertEquals("Are you ready for a challenge?", ms.messages.get(2));    	   	
    }
    
    public class MessagingServiceMock implements MessagingService {
    	public final ArrayList<String> messages = new ArrayList<>();

		@Override
		public void sendMessage(String sender, ObjectId recipient, String message) {
			messages.add(message);
		}

		@Override
		public void setListener(ObjectId userId, MessageListener listener) {
			// do nothing
		}

		@Override
		public void sendMessage(String sender, ObjectId recipient, Post post, boolean pushOnly) {
			
		}

		@Override
		public void indicateTyping(String sender, ObjectId recipient) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startReceiving() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setChannelName(ObjectId recipient) {
			// TODO Auto-generated method stub
			
		}
    }
    
    public class ConversationManagementServiceMock implements ConversationManagementService {

		ConversationRepository repository = new ConversationRepository();
		
		public ConversationManagementServiceMock() throws Exception{
			InputStream stream = this.getClass().getResourceAsStream("/test-conversation1.xml");
			DomParser parser = new DomParser(repository, null);
			parser.parse(stream);
		}

		@Override
		public ConversationRepository getRepository(String interventionId) {
			return repository;
		}

		@Override
		public void refresh() {
			// TODO Auto-generated method stub
			
		}
    }
    
    
    public interface RichConversationIfc{
    	
    	public VariableStore createVariableStore(ObjectId participantId);
    	
    	public void sendMessage(String sender, String recipient, String message, ObjectId participantId) throws ExecutionException;
        	
    }
}