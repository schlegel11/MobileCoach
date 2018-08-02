package ch.ethz.mc.model.persistent;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValueAccessTypes;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValuePrivacyTypes;
import ch.ethz.mc.model.ui.UIInterventionVariable;
import ch.ethz.mc.model.ui.UIModelObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

/**
 * {@link ModelObject} to represent an {@link InterventionVariableWithValue}
 *
 * Intervention variables belong to the referenced {@link Intervention} and
 * consist of a type, name and default value.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
public class InterventionVariableWithValue extends AbstractVariableWithValue {
	private static final long serialVersionUID = -8148624003571719902L;

	/**
	 * Default constructor
	 */
	public InterventionVariableWithValue(final ObjectId intervention,
			final String name, final String value,
			final InterventionVariableWithValuePrivacyTypes privacyType,
			final InterventionVariableWithValueAccessTypes accessType) {
		super(name, value);

		this.intervention = intervention;
		this.privacyType = privacyType;
		this.accessType = accessType;
	}

	/**
	 * {@link Intervention} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId									intervention;

	/**
	 * The {@link InterventionVariableWithValuePrivacyTypes} of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private InterventionVariableWithValuePrivacyTypes	privacyType;

	/**
	 * The {@link InterventionVariableWithValueAccessTypes} of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private InterventionVariableWithValueAccessTypes	accessType;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		final val variable = new UIInterventionVariable(getName(), getValue(),
				privacyType.toString(), accessType.toString());

		variable.setRelatedModelObject(this);

		return variable;
	}

	/**
	 * Will recursively collect all related {@link ModelObject} for export
	 *
	 * @param exportList
	 *            The {@link ModelObject} itself and all related
	 *            {@link ModelObject}s
	 */
	@Override
	@JsonIgnore
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}
}
