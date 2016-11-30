package ch.ethz.mobilecoach.model.persistent.subelements;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class MattermostUser {
	@Getter
	private String id;
	@Getter
	private String name;
}

