package ch.ethz.mc.rest.services;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.services.internal.ChatEngineStateStore;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mobilecoach.chatlib.engine.ChatEngine;
import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.ExecutionException;
import ch.ethz.mobilecoach.chatlib.engine.actions.LastAction;
import ch.ethz.mobilecoach.chatlib.engine.actions.MessageAction;
import ch.ethz.mobilecoach.chatlib.engine.actions.NonbranchingAction;
import ch.ethz.mobilecoach.chatlib.engine.test.TestFramework;
import ch.ethz.mobilecoach.chatlib.engine.test.mock.MockConversationUI;
import ch.ethz.mobilecoach.chatlib.engine.test.mock.MockLogger;
import ch.ethz.mobilecoach.chatlib.engine.variables.InMemoryVariableStore;

public class RestoreChatEngineStateTest extends TestFramework {

	private DatabaseManagerService databaseManagerService;
	private final static String FILE_NAME = "/pathToMobileCoachConfigPropertiesFile.txt";

	@Before
	public void preparation() throws IOException{

		BufferedReader brTest = null;

		String userPath = System.getProperty("user.dir");
		String packagePath = this.getClass().getPackage().getName().replaceAll("\\.", "/"); 

		try {
			brTest = new BufferedReader(new FileReader(userPath + "/test/" + packagePath + FILE_NAME));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String configPropPath = brTest.readLine();

		Constants.injectConfiguration(configPropPath, null);
		try {
			this.databaseManagerService = DatabaseManagerService.start(Constants.DATA_MODEL_VERSION);
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}

	@Test
	public void testRestoreState() throws ExecutionException {
		// set up the conversation repository
		
		ConversationRepository repository = new ConversationRepository();
		LastAction lastAction = new LastAction(repository);
		NonbranchingAction conversation = new MessageAction("Hi there!", repository);
		conversation.nextAction = lastAction;	
		repository.addConversation("test-conversation", conversation);
	

		// prepare the engine

		ObjectId participantId = new ObjectId("585ad8bc8949e81293231d3d");
		ChatEngineStateStore chatEngineStateStore = new ChatEngineStateStore(databaseManagerService, participantId);
		MockConversationUI conversationUI = new MockConversationUI();

		ChatEngine engine = new ChatEngine(repository, conversationUI, new InMemoryVariableStore(), null, null, chatEngineStateStore);
		engine.setLogger(new MockLogger());


		// run the conversation
		engine.startConversation("test-conversation");
		engine = null;
		
		ChatEngineStateStore chatEngineStateStore2 = new ChatEngineStateStore(databaseManagerService, participantId);
		engine = new ChatEngine(repository, conversationUI, new InMemoryVariableStore(), null, null, chatEngineStateStore2);
		engine.startConversation("test-conversation");

		System.out.println(conversationUI.messages.get(0).text);

		// check that the conversation was run successfully
		assertEquals(conversationUI.messages.get(0).text, "Hi there!");

	}


}
