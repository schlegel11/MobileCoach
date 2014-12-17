package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
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

/**
 * {@link ModelObject} to represent an {@link AuthorInterventionAccess}
 * 
 * The {@link AuthorInterventionAccess} describes, which {@link Author} is
 * allowed to administrate a specific {@link Intervention}.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class AuthorInterventionAccess extends ModelObject {
	/**
	 * {@link Author} who is allowed to administrate {@link Intervention}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	author;

	/**
	 * {@link Intervention} that can be administrated by the {@link Author}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	intervention;
}
