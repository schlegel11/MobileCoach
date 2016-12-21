package ch.ethz.mc.services.internal;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
	public boolean containsAValidChatEngineState(){
		boolean result = true;
		LocalDateTime ldt = LocalDateTime.now();
		if(chatEngineState == null){
			result = false;
		}else if(chatEngineState.getLdt().getDayOfMonth() != ldt.getDayOfMonth()){
			result = false;
			deleteState();
		}
		return result;
	}

	@Override
	public void saveChatEngineState(ChatEngine chatEngine) {
		deleteState();
		LocalDateTime ldt = LocalDateTime.now();
		chatEngineState = new ChatEngineState(chatEngine.getRepository(), chatEngine.getStack(), chatEngine.getTimerValue(), chatEngine.getOperations(), chatEngine.getUserInput(), chatEngine.getCurrentAction(), ldt);		
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
		long newTimerValue = computeNewTimerValue();	
		chatEngine.setTimerValue(newTimerValue);
	}

	private long computeNewTimerValue() {
		long newTimerValue;
		
		ZonedDateTime zdtOld = chatEngineState.getLdt().atZone(ZoneId.of("Europe/Paris"));
		long millisOld = zdtOld.toInstant().toEpochMilli();
		
		LocalDateTime ldtNew = LocalDateTime.now();
		ZonedDateTime zdtNew = ldtNew.atZone(ZoneId.of("Europe/Paris"));
		long millisNew = zdtNew.toInstant().toEpochMilli();
		
		if(millisOld + chatEngineState.getTimerValue() - millisNew > 0L){
			newTimerValue = millisOld + chatEngineState.getTimerValue() - millisNew;
		}else{
			newTimerValue = 0L;
		}
		return newTimerValue;
	}
	
	@Override
	public void deleteState(){
		this.dbMgmtService.deleteModelObject(chatEngineState);
	}
}
