package ch.ethz.mc.model.persistent.outdated.helpers;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
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

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used for several data update steps
 *
 * @author Andreas Filler
 */
public class MinimalObject {
	@MongoId
	@JsonProperty("_id")
	@Getter
	private ObjectId	id;
}
