package ch.ethz.mobilecoach.model.persistent;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
public class ChatEnginePersistentState extends ModelObject{
	
	@Getter
	@Setter
	private ObjectId participantId;
	@Getter
	@Setter
	private String serializedState;
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
