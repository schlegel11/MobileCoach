package ch.ethz.mc.model.persistent.outdated;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;

import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValueAccessTypes;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValuePrivacyTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CAUTION: Will only be used for conversion from data model 2 to 3
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class InterventionVariableWithValueV3 {
	@MongoId
	@JsonProperty("_id")
	public ObjectId										id;

	/**
	 * Name of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private String										name;

	/**
	 * Value of the variable
	 */
	@Getter
	@Setter
	@NonNull
	private String										value;

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
}
