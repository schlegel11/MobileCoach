package ch.ethz.mobilecoach.model.persistent;

import java.util.List;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mobilecoach.model.persistent.subelements.MattermostChannel;
import ch.ethz.mobilecoach.model.persistent.subelements.MattermostUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class MattermostUserConfiguration extends ModelObject {
	@Getter
	@Setter
	private String participantId;
	@Getter
	private String userId;
	@Getter
	private String email;
	@Getter
	private String password;
	@Getter
	@Setter
	private String token;
	@Getter
	private String locale;
	@Getter
	private List<MattermostChannel> channels;
	@Getter
	private List<MattermostUser> users;
	@Getter
	private String teamId;
	@Getter
	private String url;
	@Getter
	private long createdTimestamp;
	@Getter
	@Setter
	private long tokenTimestamp;
}

