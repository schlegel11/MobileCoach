package ch.ethz.mc.model.memory;

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
import java.io.IOException;
import java.io.StringWriter;
import java.util.Hashtable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.tools.CustomObjectMapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Encapsulates a {@link ModelObject} with the meta data required for import and
 * export
 * 
 * @author Andreas Filler
 */
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@Log4j2
public class ExchangeModelObject {
	/**
	 * The class of the {@link ModelObject}
	 */
	@Getter
	@Setter
	@NonNull
	private String							clazz;

	/**
	 * The class and package of the {@link ModelObject}
	 */
	@Getter
	@Setter
	@NonNull
	private String							packageAndClazz;

	/**
	 * The {@link ObjectId} of the {@link ModelObject}
	 */
	@Getter
	@Setter
	@NonNull
	private String							objectId;

	/**
	 * The JSON content of the {@link ModelObject}
	 */
	@Getter
	@Setter
	@NonNull
	private String							content;

	/**
	 * File reference if the {@link ModelObject} has one
	 */
	@Getter
	@Setter
	private String							fileReference;

	/**
	 * Method to set the new file reference after import
	 */
	@Getter
	@Setter
	private String							fileReferenceSetMethod;

	/**
	 * The set methods required to adjust the {@link ObjectId}s accordingly
	 * after import in another system
	 */
	@Getter
	@NonNull
	private final Hashtable<String, String>	objectIdSetMethodsWithAppropriateValues	= new Hashtable<String, String>();

	/**
	 * {@link ObjectMapper} required for JSON generation
	 */
	@JsonIgnore
	private static ObjectMapper				objectMapper							= new CustomObjectMapper();

	/**
	 * Creates a JSON String of the current {@link ExchangeModelObject}
	 * 
	 * @return JSON string
	 */
	@JsonIgnore
	public String toJSONString() {
		val stringWriter = new StringWriter();

		try {
			objectMapper.writeValue(stringWriter, this);
		} catch (final IOException e) {
			log.warn("Could not create JSON of {}!", this.getClass());
			return "{ \"Error\" : \"JSON object could not be serialized\" }";
		}

		return stringWriter.toString();
	}

	/**
	 * Create a {@link ExchangeModelObject} from the given JSON String
	 * 
	 * @param jsonString
	 *            The String to create a {@link ExchangeModelObject} from
	 * @return The created {@link ExchangeModelObject}
	 */
	@JsonIgnore
	public static ExchangeModelObject fromJSONString(final String jsonString) {
		ExchangeModelObject exchangeModelObject;

		try {
			exchangeModelObject = objectMapper.readValue(jsonString,
					ExchangeModelObject.class);
		} catch (final Exception e) {
			log.warn(
					"Could not create exchange model object from JSON: {} (JSON: {})",
					e.getMessage(), jsonString);
			return null;
		}

		return exchangeModelObject;
	}

	/**
	 * Creates a new {@link ModelObject} from the contained content
	 * 
	 * @return Newly created {@link ModelObject}
	 */
	@JsonIgnore
	public ModelObject getContainedModelObjectWithoutOriginalId() {
		ModelObject modelObject;

		// Set object id to null
		final String contentToConvert = content.replaceFirst(
				"\"_id\":\"[^\"]+\"", "\"_id\":null");

		try {
			modelObject = (ModelObject) objectMapper.readValue(
					contentToConvert, Class.forName(packageAndClazz));
		} catch (final Exception e) {
			log.warn("Could not create model object from JSON: {} (JSON: {})",
					e.getMessage(), contentToConvert);
			return null;
		}
		log.debug("Created model object {} from JSON {}", modelObject,
				contentToConvert);

		return modelObject;
	}
}
