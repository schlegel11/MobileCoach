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
	private String status;
	@Getter
	@Setter
	private String conversationsHash;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		String hash = this.conversationsHash + "        ";
		
		final val conversation = new UIConversation(participantId.toString(), status, hash.substring(0, 7), serializedState);

		conversation.setRelatedModelObject(this);

		return conversation;
	}
}
