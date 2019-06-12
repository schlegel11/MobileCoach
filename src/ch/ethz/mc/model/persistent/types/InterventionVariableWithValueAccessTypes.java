package ch.ethz.mc.model.persistent.types;

/* ##LICENSE## */
import lombok.Getter;

/**
 * Supported access types for intervention variable with value
 *
 * @author Andreas Filler
 */
public enum InterventionVariableWithValueAccessTypes {
	INTERNAL(0),
	MANAGEABLE_BY_SERVICE(1),
	EXTERNALLY_READABLE(2),
	EXTERNALLY_READ_AND_WRITABLE(3);

	@Getter
	private int intValue;

	private InterventionVariableWithValueAccessTypes(final int intValue) {
		this.intValue = intValue;
	}

	public boolean isAllowedAtGivenOrLessRestrictiveAccessType(
			final InterventionVariableWithValueAccessTypes accessTypeToCompareTo) {
		if (intValue >= accessTypeToCompareTo.getIntValue()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}
}
