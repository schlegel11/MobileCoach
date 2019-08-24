package ch.ethz.mc.model.persistent.types;

/* ##LICENSE## */
import lombok.Getter;

/**
 * Supported privacy types for intervention variable with value
 *
 * @author Andreas Filler
 */
public enum InterventionVariableWithValuePrivacyTypes {
	PRIVATE(0),
	SHARED_WITH_GROUP(1),
	SHARED_WITH_INTERVENTION(2),
	SHARED_WITH_INTERVENTION_AND_DASHBOARD(3);

	@Getter
	private int intValue;

	private InterventionVariableWithValuePrivacyTypes(final int intValue) {
		this.intValue = intValue;
	}

	public boolean isAllowedAtGivenOrLessRestrictivePrivacyType(
			final InterventionVariableWithValuePrivacyTypes privacyTypeToCompareTo) {
		if (intValue >= privacyTypeToCompareTo.getIntValue()) {
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
