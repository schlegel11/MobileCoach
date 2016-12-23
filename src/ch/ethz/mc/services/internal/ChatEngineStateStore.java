package ch.ethz.mc.services.internal;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import org.bson.types.ObjectId;

import ch.ethz.mobilecoach.chatlib.engine.ChatEngine;
import ch.ethz.mobilecoach.chatlib.engine.actions.operations.Operation;
import ch.ethz.mobilecoach.chatlib.engine.stack.Context;
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
		}else if(chatEngineState != null && chatEngineState.getDayOfTheMonth() != ldt.getDayOfMonth() && chatEngineState.getMonthValue() != ldt.getMonthValue()){
			result = false;
			deleteState(chatEngineState);
		}
		return result;
	}

	@Override
	public void saveChatEngineState(ChatEngine chatEngine) {

		LocalDateTime ldt = LocalDateTime.now();
		ZonedDateTime zdt = ldt.atZone(ZoneId.of("Europe/Paris"));
		long timeStamp = zdt.toInstant().toEpochMilli();

		deleteState(chatEngineState);
		
		Queue<Integer> operations = convertOperationQueue(chatEngine.getOperations());	
		Stack<Integer> stack = transformStackContextToStackInteger(chatEngine.getStack());
		System.out.println(chatEngine.getCurrentAction());
		
		chatEngineState = new ChatEngineState(participantId, stack, chatEngine.getTimerValue(), operations, chatEngine.getUserInput(), chatEngine.getCurrentAction().getActionIdInConversation(), timeStamp, ldt.getDayOfMonth(), ldt.getMonthValue());		
		this.dbMgmtService.saveModelObject(chatEngineState);
	}

	private Stack<Integer> transformStackContextToStackInteger(Stack<Context> stack) {
		Stack<Integer> stack2 = new Stack<>();		
		for(int j = 0; j < stack.size(); j++){			
			stack2.add(stack.get(j).nextAction.getActionIdInConversation());
		}
		return stack2;
	}

	private Queue<Integer> convertOperationQueue(Queue<Operation> queueOperations) {
		Queue<Integer> operations = new LinkedBlockingQueue<Integer>();
		for(Operation operation : queueOperations){
			operations.add(operation.getOperationInd());
		}
		return operations;
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

		long millisOld = chatEngineState.getTimeStamp();

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
	public void deleteState(Object chatEngineState){

		this.dbMgmtService.deleteModelObject((ChatEngineState) chatEngineState);
	}
}
