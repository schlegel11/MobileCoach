package ch.ethz.mobilecoach.model.persistent.subelements;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class MattermostChannel {
	@Getter
	private String name;
	@Getter
	private String type;
	@Getter
	private String id;
}
