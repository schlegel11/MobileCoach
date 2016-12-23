package ch.ethz.mobilecoach.model.persistent;

import java.util.Queue;
import java.util.Stack;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
public class ChatEngineState extends ModelObject{
	
	@Getter
	@Setter
	private ObjectId participantId;
	@Getter
	@Setter
	private Stack<Integer> stack;
	@Getter
	@Setter
	private long timerValue;
	@Getter
	@Setter
	private Queue<Integer> operations;
	@Getter
	@Setter
	private String userInput;
	@Getter
	@Setter
	private int currentAction;
	@Getter
	@Setter
	private long timeStamp;
	@Getter
	@Setter
	private int dayOfTheMonth;
	@Getter
	@Setter
	private int monthValue;
}
