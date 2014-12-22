package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health IS-Lab
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;

/**
 * {@link ModelObject} to represent an {@link DialogOption}
 * 
 * A {@link DialogOption} describes by which options a {@link Participant} can
 * be contacted. Several {@link DialogOption}s can exist for one
 * {@link Participant}.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class DialogOption extends ModelObject {
	/**
	 * The {@link Participant} which provides this {@link DialogOption}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			participant;

	/**
	 * The {@link DialogOptionTypes} which describes this {@link DialogOption}
	 */
	@Getter
	@Setter
	@NonNull
	private DialogOptionTypes	type;

	/**
	 * The data required to reach the {@link Participant} using this
	 * {@link DialogOption}, e.g. a phone number or an email address
	 */
	@Getter
	@Setter
	@NonNull
	private String				data;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}
}
