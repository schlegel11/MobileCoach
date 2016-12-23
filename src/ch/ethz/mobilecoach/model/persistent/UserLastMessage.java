package ch.ethz.mobilecoach.model.persistent;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * This object says:
 * 
 * "For participant [participantId] and channel [channelId], the last processed message has the id [lastMessageId]
 * and the timestamp [lastMessageTimestamp]."
 * 
 * @author Dominik
 */
@AllArgsConstructor
public class UserLastMessage extends ModelObject {
	@Getter
	@Setter
	private ObjectId participantId;
	@Getter
	@Setter
	private String channelId;
	@Getter
	@Setter
	private String lastMessageId;
	@Getter
	@Setter
	private long lastMessageTimestamp;
}