package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link ModelObject} to represent an {@link InterventionVariableWithValue}
 * 
 * SystemVariables belong to the referenced {@link Intervention} and consist of
 * a name
 * and a value.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public class InterventionVariableWithValue extends AbstractVariableWithValue {
	/**
	 * Default constructor
	 */
	public InterventionVariableWithValue(final ObjectId intervention,
			final String name, final String value) {
		super(name, value);

		this.intervention = intervention;
	}

	/**
	 * {@link Intervention} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	intervention;

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
