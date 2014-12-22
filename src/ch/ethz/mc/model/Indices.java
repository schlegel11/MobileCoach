package ch.ethz.mc.model;

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
import java.util.Hashtable;

import lombok.val;
import ch.ethz.mc.model.persistent.Author;

/**
 * Describes all indices that shall be created in the database
 * 
 * @author Andreas Filler
 */
public class Indices {
	private static final String[]	authorIndices	= new String[] { "{'username':1}" };

	/**
	 * Creates a hashtable containing all indices for all {@link ModelObject}
	 * 
	 * @return
	 */
	public static Hashtable<Class<? extends ModelObject>, String[]> getIndices() {
		val indices = new Hashtable<Class<? extends ModelObject>, String[]>();

		indices.put(Author.class, authorIndices);

		return indices;
	}
}
