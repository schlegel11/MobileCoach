package ch.ethz.mobilecoach.app;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Option {
	
	@Getter
	@Setter
	private String label;
	
	
	@Getter
	@Setter
	private String value;
	
	
}
