package ch.ethz.mobilecoach.model.persistent;

import java.util.List;

import ch.ethz.mc.model.ModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class OneSignalUserConfiguration extends ModelObject {
	@Getter
	@Setter
	private String participantId; // TODO: use ObjectId
	@Getter
	@Setter
	private List<String> playerIds;
	@Getter
	private long createdTimestamp;
}
