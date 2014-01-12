package org.isgf.mhc.tools;

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
		this.registerModule(module);
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
