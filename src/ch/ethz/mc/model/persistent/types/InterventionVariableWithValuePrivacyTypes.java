package ch.ethz.mc.model.persistent.types;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import lombok.Getter;

/**
 * Supported privacy types for intervention variable with value
 *
 * @author Andreas Filler
 */
public enum InterventionVariableWithValuePrivacyTypes {
	PRIVATE(0), SHARED_WITH_GROUP(1), SHARED_WITH_INTERVENTION(
			2), SHARED_WITH_INTERVENTION_AND_DASHBOARD(3);

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
