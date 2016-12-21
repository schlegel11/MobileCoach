package ch.ethz.mc.services.internal;

import org.bson.types.ObjectId;

import ch.ethz.mobilecoach.chatlib.engine.ChatEngine;
import ch.ethz.mobilecoach.chatlib.engine.variables.ChatEngineStateStoreIfc;
import ch.ethz.mobilecoach.model.persistent.ChatEngineState;

public class ChatEngineStateStore implements ChatEngineStateStoreIfc{


	private DatabaseManagerService dbMgmtService;
	private ObjectId participantId;
	private ChatEngineState chatEngineState;

	public ChatEngineStateStore(DatabaseManagerService dbMgmtService, ObjectId participantId){
		this.dbMgmtService = dbMgmtService;
		this.participantId = participantId;
		this.chatEngineState = this.dbMgmtService.findOneModelObject(ChatEngineState.class,"{'participantId':#}", participantId);
	}

	@Override
	public boolean containChatEngineState(){
		boolean result = true;
		if(chatEngineState == null) result = false;
		return result;
	}

	@Override
	public void saveChatEngineState(ChatEngine chatEngine) {
		chatEngineState = new ChatEngineState(chatEngine.getRepository(), chatEngine.getStack(), chatEngine.getTimerValue(), chatEngine.getOperations(), chatEngine.getUserInput(), chatEngine.getCurrentAction());		
		this.dbMgmtService.saveModelObject(chatEngineState);		
	}

	@Override
	public void restoreState(ChatEngine chatEngine) {

		chatEngineState = this.dbMgmtService.findOneModelObject(ChatEngineState.class,"{'participantId':#}", participantId);
		chatEngine.setCurrentAction(chatEngineState.getCurrentAction());
		chatEngine.setTimerValue(chatEngineState.getTimerValue());
		chatEngine.setUserInput(chatEngineState.getUserInput());
		chatEngine.setOperations(chatEngineState.getOperations());
		chatEngine.setStack(chatEngineState.getStack());	
	}
	
	@Override
	public void deleteState(){
		this.dbMgmtService.deleteModelObject(chatEngineState);
	}
}
