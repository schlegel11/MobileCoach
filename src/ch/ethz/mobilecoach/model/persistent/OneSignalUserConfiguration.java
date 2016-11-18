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
	private String participantId;
	@Getter
	@Setter
	private List<String> deviceIds;
	@Getter
	@Setter
	private String playerId;
	@Getter
	@Setter
	private String device_type;
	@Getter
	private long createdTimestamp;
}
