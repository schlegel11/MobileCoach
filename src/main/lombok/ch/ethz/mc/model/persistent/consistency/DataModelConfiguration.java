package ch.ethz.mc.model.persistent.consistency;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import lombok.Setter;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores the current version of the data model
 * 
 * @author Andreas Filler
 */
public class DataModelConfiguration {
	/**
	 * The id of the {@link DataModelConfiguration}
	 */
	@MongoId
	@JsonProperty("_id")
	@Getter
	private ObjectId	id;

	/**
	 * The current version of the database
	 */
	@Getter
	@Setter
	private int			version;

}
