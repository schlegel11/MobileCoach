package ch.ethz.mc.services.internal;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.bson.types.ObjectId;

import ch.ethz.mobilecoach.chatlib.engine.ChatEngine;
import ch.ethz.mobilecoach.chatlib.engine.ChatEngineState;
import ch.ethz.mobilecoach.chatlib.engine.serialization.ChatEngineStateStoreIfc;
import ch.ethz.mobilecoach.chatlib.engine.serialization.RestoreException;
import ch.ethz.mobilecoach.model.persistent.ChatEnginePersistentState;
import lombok.extern.log4j.Log4j2;

@Log4j2
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

	
	public static boolean containsARecentChatEngineState(ChatEnginePersistentState chatEngineState){
		
		if(chatEngineState == null){
			return false;
		} 
		
		// we consider it recent if it is newer than 3 * 24 hours
		return System.currentTimeMillis() - chatEngineState.getTimeStamp() < 1000 * 3600 * 24 * 3;
	}

	@Override
	public void saveChatEngineState(ChatEngine chatEngine) {

		deleteState(chatEngineState);
				
		String serializedState = chatEngine.getSerializer().serialize(chatEngine.getState());
		
		chatEngineState = new ChatEnginePersistentState(participantId, serializedState, System.currentTimeMillis(), "Saved", chatEngine.getConversationsHash());		
		this.dbMgmtService.saveModelObject(chatEngineState);
	}

	@Override
	public void restoreState(ChatEngine chatEngine) throws RestoreException {

		ChatEnginePersistentState persistentState = this.dbMgmtService.findOneModelObject(ChatEnginePersistentState.class,"{'participantId':#}", participantId);
		
		ChatEngineState restoredState;
		try {
			
			restoredState = chatEngine.getSerializer().deserialize(persistentState.getSerializedState());
			
			long newTimerValue = computeNewTimerValue(restoredState.getTimerValue(), persistentState.getTimeStamp());
			
			chatEngine.setState(new ChatEngineState(restoredState.getStack(), restoredState.getOperations(), restoredState.getCurrentAction(), 
					restoredState.getUserInput(), newTimerValue));
		
		} catch (IOException e) {
			persistentState.setStatus("Error");
			this.dbMgmtService.saveModelObject(persistentState);
			
			log.error("Error deserializing state: " + persistentState.getSerializedState(), e);
			throw new RestoreException(e);
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

	@Override
	public void deleteState() {
		deleteState(chatEngineState);
	}
}
