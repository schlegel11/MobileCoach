package ch.ethz.mc.model.memory;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * External service JSON field as variable
 * 
 * @author Marcel Schlegel
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(exclude = {"value"})
public class ExternalServiceVariable {
	
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private String value;
}
