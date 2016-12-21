package ch.ethz.mobilecoach.model.persistent;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.Stack;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.actions.Action;
import ch.ethz.mobilecoach.chatlib.engine.actions.operations.Operation;
import ch.ethz.mobilecoach.chatlib.engine.stack.Context;



public class ChatEngineState extends ModelObject{
	

	private ConversationRepository convRep;
	private Stack<Context> stack;
	private long timerValue;
	private Queue<Operation> operations;
	private String userInput;
	private Action currentAction;
	private LocalDateTime ldt;

	
	
	public ChatEngineState(ConversationRepository convRep, Stack<Context> stack, long timerValue,
			Queue<Operation> operations, String userInput, Action currentAction, LocalDateTime ldt) {
		super();
		this.convRep = convRep;
		this.stack = stack;
		this.timerValue = timerValue;
		this.operations = operations;
		this.userInput = userInput;
		this.currentAction = currentAction;
	}


	public Action getCurrentAction() {
		return currentAction;
	}


	public void setCurrentAction(Action currentAction) {
		this.currentAction = currentAction;
	}


	public ConversationRepository getConvRep() {
		return convRep;
	}


	public void setConvRep(ConversationRepository convRep) {
		this.convRep = convRep;
	}


	public Stack<Context> getStack() {
		return stack;
	}


	public void setStack(Stack<Context> stack) {
		this.stack = stack;
	}


	public long getTimerValue() {
		return timerValue;
	}


	public void setTimerValue(long timerValue) {
		this.timerValue = timerValue;
	}


	public Queue<Operation> getOperations() {
		return operations;
	}


	public void setOperations(Queue<Operation> operations) {
		this.operations = operations;
	}


	public String getUserInput() {
		return userInput;
	}


	public void setUserInput(String userInput) {
		this.userInput = userInput;
	}


	public LocalDateTime getLdt() {
		return ldt;
	}


	public void setLdt(LocalDateTime ldt) {
		this.ldt = ldt;
	}
}
