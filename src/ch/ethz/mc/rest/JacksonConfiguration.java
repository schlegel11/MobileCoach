package ch.ethz.mc.rest;

/* ##LICENSE## */
import java.io.IOException;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import lombok.val;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * JSON serialization/deserialization configuration for REST interface
 *
 * @author Andreas Filler
 */
@Provider
public class JacksonConfiguration implements ContextResolver<ObjectMapper> {
	private final ObjectMapper objectMapper;

	class BooleanSerializer extends JsonSerializer<Boolean> {

		@Override
		public void serialize(final Boolean value,
				final JsonGenerator jsonGenerator,
				final SerializerProvider serializerProvider)
				throws IOException, JsonProcessingException {
			jsonGenerator.writeString(value.toString());
		}
	}

	class BooleanDeserializer extends JsonDeserializer<Boolean> {

		@Override
		public Boolean deserialize(final JsonParser jsonParser,
				final DeserializationContext deserializationContext)
				throws IOException, JsonProcessingException {
			return Boolean.valueOf(jsonParser.getText());
		}
	}

	class IntegerSerializer extends JsonSerializer<Integer> {

		@Override
		public void serialize(final Integer value,
				final JsonGenerator jsonGenerator,
				final SerializerProvider serializerProvider)
				throws IOException, JsonProcessingException {
			jsonGenerator.writeString(value.toString());
		}
	}

	class IntegerDeserializer extends JsonDeserializer<Integer> {

		@Override
		public Integer deserialize(final JsonParser jsonParser,
				final DeserializationContext deserializationContext)
				throws IOException, JsonProcessingException {
			return Integer.valueOf(jsonParser.getText());
		}
	}

	public JacksonConfiguration() throws Exception {
		objectMapper = new ObjectMapper();
		objectMapper.configure(
				SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING, true);
		objectMapper.configure(
				DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true);
		objectMapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE,
				false);

		final val booleanModule = new SimpleModule("BooleanModule",
				new Version(0, 0, 1, "RC1"));
		booleanModule.addSerializer(Boolean.class, new BooleanSerializer());
		booleanModule.addDeserializer(Boolean.class, new BooleanDeserializer());

		objectMapper.registerModule(booleanModule);

		final val integerModule = new SimpleModule("IntegerModule",
				new Version(0, 0, 1, "RC1"));
		integerModule.addSerializer(Integer.class, new IntegerSerializer());
		integerModule.addDeserializer(Integer.class, new IntegerDeserializer());

		objectMapper.registerModule(integerModule);
	}

	@Override
	public ObjectMapper getContext(final Class<?> type) {
		return objectMapper;
	}
}
