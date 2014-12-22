package ch.ethz.mc.tools;

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
import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Custom object mapper which maps {@link ObjectId}s a bit "nicer" than the
 * default implementation
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class CustomObjectMapper extends ObjectMapper {
	public CustomObjectMapper() {
		final SimpleModule module = new SimpleModule("ObjectIdModule");
		module.addSerializer(ObjectId.class, new ObjectIdSerializer());
		registerModule(module);
	}

	/**
	 * Custom {@link ObjectId} serializer
	 * 
	 * @author Andreas Filler
	 */
	private class ObjectIdSerializer extends JsonSerializer<ObjectId> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang
		 * .Object, com.fasterxml.jackson.core.JsonGenerator,
		 * com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(final ObjectId value, final JsonGenerator jgen,
				final SerializerProvider provider) throws IOException,
				JsonProcessingException {
			jgen.writeString(value.toString());
		}
	}
}
