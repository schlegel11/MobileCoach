package ch.ethz.mc.model.persistent.outdated;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.Intervention;

/**
 * CAUTION: Will only be used for conversion from data model 2 to 3
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class InterventionVariableWithValueV3 extends ModelObject {
	/**
	 * Name of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private String		name;

	/**
	 * Value of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private String		value;

	/**
	 * {@link Intervention} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	intervention;
}
