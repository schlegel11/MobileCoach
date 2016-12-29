package ch.ethz.mobilecoach.model.persistent;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.ui.UIConversation;
import ch.ethz.mc.model.ui.UIModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;


@AllArgsConstructor
public class ChatEnginePersistentState extends ModelObject {
	
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		final val conversation = new UIConversation(participantId.toString(), serializedState);

		conversation.setRelatedModelObject(this);

		return conversation;
	}
}
