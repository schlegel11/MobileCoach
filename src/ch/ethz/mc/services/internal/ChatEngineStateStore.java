package ch.ethz.mc.services.internal;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import org.bson.types.ObjectId;

import ch.ethz.mobilecoach.chatlib.engine.ChatEngine;
import ch.ethz.mobilecoach.chatlib.engine.ChatEngineState;
import ch.ethz.mobilecoach.chatlib.engine.actions.operations.Operation;
import ch.ethz.mobilecoach.chatlib.engine.stack.Context;
import ch.ethz.mobilecoach.chatlib.engine.variables.ChatEngineStateStoreIfc;
import ch.ethz.mobilecoach.model.persistent.ChatEnginePersistentState;

public class ChatEngineStateStore implements ChatEngineStateStoreIfc {


	private DatabaseManagerService dbMgmtService;
	private ObjectId participantId;
	private ChatEnginePersistentState chatEngineState;

	public ChatEngineStateStore(DatabaseManagerService dbMgmtService, ObjectId participantId){
		this.dbMgmtService = dbMgmtService;
		this.participantId = participantId;
		this.chatEngineState = this.dbMgmtService.findOneModelObject(ChatEnginePersistentState.class,"{'participantId':#}", participantId);
	}
	
	public ChatEngineStateStore(DatabaseManagerService dbMgmtService, ChatEnginePersistentState chatEngineState){
		this.dbMgmtService = dbMgmtService;
		this.participantId = chatEngineState.getParticipantId();
		this.chatEngineState = chatEngineState;
	}

	
	public static boolean containsAValidChatEngineState(ChatEnginePersistentState chatEngineState){
		boolean result = true;
		LocalDateTime ldt = LocalDateTime.now();
		
		if(chatEngineState == null){
			result = false;
		}else if(chatEngineState != null && chatEngineState.getDayOfTheMonth() != ldt.getDayOfMonth() && chatEngineState.getMonthValue() != ldt.getMonthValue()){
			result = false;
		}
		return result;
	}

	@Override
	public void saveChatEngineState(ChatEngine chatEngine) {

		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ldt.atZone(ZoneId.of("Europe/Paris"));
		long timeStamp = zdt.toInstant().toEpochMilli();

		deleteState(chatEngineState);
				
		String serializedState = chatEngine.getSerializer().serialize(chatEngine.getState());
		
		chatEngineState = new ChatEnginePersistentState(participantId, serializedState, timeStamp, ldt.getDayOfMonth(), ldt.getMonthValue());		
		this.dbMgmtService.saveModelObject(chatEngineState);
	}

	@Override
	public void restoreState(ChatEngine chatEngine) {

		ChatEnginePersistentState persistentState = this.dbMgmtService.findOneModelObject(ChatEnginePersistentState.class,"{'participantId':#}", participantId);
		
		ChatEngineState restoredState;
		try {
			
			restoredState = chatEngine.getSerializer().deserialize(persistentState.getSerializedState());
			
			long newTimerValue = computeNewTimerValue(restoredState.getTimerValue(), persistentState.getTimeStamp());
			
			chatEngine.setState(new ChatEngineState(restoredState.getStack(), restoredState.getOperations(), restoredState.getCurrentAction(), 
					restoredState.getUserInput(), newTimerValue));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private long computeNewTimerValue(long stateTimerValue, long stateTimestamp) {
		long newTimerValue;

		LocalDateTime ldtNew = LocalDateTime.now();
		ZonedDateTime zdtNew = ldtNew.atZone(ZoneId.of("Europe/Paris"));
		long millisNew = zdtNew.toInstant().toEpochMilli();

		if (stateTimestamp + stateTimerValue - millisNew > 0L) {
			newTimerValue = stateTimestamp + stateTimerValue - millisNew;
		} else {
			newTimerValue = 0L;
		}
		return newTimerValue;
	}


	@Override
	public void deleteState(Object chatEngineState){

		this.dbMgmtService.deleteModelObject((ChatEnginePersistentState) chatEngineState);
	}
}
