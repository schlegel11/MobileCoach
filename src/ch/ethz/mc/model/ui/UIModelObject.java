package ch.ethz.mc.model.ui;

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
import lombok.NoArgsConstructor;
import lombok.NonNull;
import ch.ethz.mc.model.ModelObject;

/**
 * Basic class for model objects that should be displayed in the UI
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public abstract class UIModelObject extends UIObject {
	/**
	 * Contains the reference to the related model object
	 */
	@NonNull
	private ModelObject	relatedModelObject;

	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass getRelatedModelObject(
			final Class<ModelObjectSubclass> modelObjectClass) {

		return modelObjectClass.cast(relatedModelObject);
	}

	public void setRelatedModelObject(final ModelObject relatedModelObject) {
		this.relatedModelObject = relatedModelObject;
	}
}
