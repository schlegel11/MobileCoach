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
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.services.internal.ChatEngineStateStore;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mobilecoach.chatlib.engine.ChatEngine;
import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.ExecutionException;
import ch.ethz.mobilecoach.chatlib.engine.Logger;
import ch.ethz.mobilecoach.chatlib.engine.actions.LastAction;
import ch.ethz.mobilecoach.chatlib.engine.actions.MessageAction;
import ch.ethz.mobilecoach.chatlib.engine.actions.NonbranchingAction;
import ch.ethz.mobilecoach.chatlib.engine.actions.Option;
import ch.ethz.mobilecoach.chatlib.engine.actions.QuestionAction;
import ch.ethz.mobilecoach.chatlib.engine.serialization.RestoreException;
import ch.ethz.mobilecoach.chatlib.engine.test.TestFramework;
import ch.ethz.mobilecoach.chatlib.engine.test.mock.MockConversationUI;
import ch.ethz.mobilecoach.chatlib.engine.test.mock.MockLogger;
import ch.ethz.mobilecoach.chatlib.engine.variables.InMemoryVariableStore;
import ch.ethz.mobilecoach.model.persistent.ChatEnginePersistentState;

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
	public void testRestoreState() throws ExecutionException, RestoreException {
		// set up the conversation repository
		
		ConversationRepository repository = new ConversationRepository();
		NonbranchingAction conversation = new MessageAction("Hi there!", repository);
		QuestionAction question = new QuestionAction("How are you?", repository);
		conversation.nextAction = question;	
		MessageAction m1 = new MessageAction("That's nice.", repository);
		MessageAction m2 = new MessageAction("You'll get better.", repository);
		question.addUserOption(new Option("Well", "1", m1));
		question.addUserOption(new Option("Not well", "2", m2));
		LastAction lastAction = new LastAction(repository);
		m1.nextAction = lastAction;
		m2.nextAction = lastAction;
		repository.addConversation("test-conversation", conversation);
	

		// prepare the engine

		ObjectId participantId = new ObjectId("585ad8bc8949e81293231d3d");
		ChatEngineStateStore chatEngineStateStore = new ChatEngineStateStore(databaseManagerService, participantId);
		MockConversationUI conversationUI = new MockConversationUI();

		ChatEngine engine = new ChatEngine(repository, conversationUI, new InMemoryVariableStore(), null, null, chatEngineStateStore);
		Logger logger = new MockLogger();
		engine.setLogger(logger);
		engine.sendExceptionAsMessage =  false;


		// run the conversation
		engine.startConversation("test-conversation");
		assertEquals("Hi there!", conversationUI.messages.get(0).text);
		assertEquals("How are you?", conversationUI.messages.get(1).text);
		
		engine = null;
		
		java.util.Iterator<ChatEnginePersistentState> iterator = 
				databaseManagerService.findModelObjects(ChatEnginePersistentState.class, "{participantId: #}", participantId).iterator();
		
		while(iterator.hasNext()){
			ChatEnginePersistentState ces = iterator.next();
			if(ChatEngineStateStore.containsARecentChatEngineState(ces)){
				ChatEngineStateStore chatEngineStateStore2 = new ChatEngineStateStore(databaseManagerService, ces.getParticipantId());
				ChatEngine engine2 = new ChatEngine(repository, conversationUI, new InMemoryVariableStore(), null, null, chatEngineStateStore2);
				engine2.sendExceptionAsMessage =  false;
				engine2.setLogger(logger);
				chatEngineStateStore.restoreState(engine2);
				engine2.handleInput("1");
			}
		}

		// check that the conversation was run successfully
		assertEquals("Well", conversationUI.messages.get(2).text);
		assertEquals("That's nice.", conversationUI.messages.get(3).text);

	}


}
