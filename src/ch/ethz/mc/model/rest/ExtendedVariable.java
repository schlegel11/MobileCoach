package ch.ethz.mc.model.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for a variable with a value for REST (extended to describe if the
 * variable belongs to the participant)
 *
 * @author Andreas Filler
 */
@AllArgsConstructor
public class ExtendedVariable {
	@Getter
	@Setter
	private String	variable;
	@Getter
	@Setter
	private String	value;
	@Getter
	@Setter
	private String	identifier;
	@Setter
	@JsonInclude(Include.NON_NULL)
	private Boolean	ownValue;
	@Setter
	@JsonInclude(Include.NON_NULL)
	private Boolean	voted;

	@JsonIgnore
	public boolean isOwnValue() {
		if (ownValue == null || ownValue == false) {
			return false;
		} else {
			return true;
		}
	}

	@JsonIgnore
	public boolean isVoted() {
		if (voted == null || voted == false) {
			return false;
		} else {
			return true;
		}
	}
}
