package org.isgf.mhc.tools.model;

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
import org.isgf.mhc.model.ModelObject;

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

	@Getter
	@NonNull
	private final Hashtable<String, String>	objectIdsWithContainingVariable	= new Hashtable<String, String>();

	/**
	 * {@link ObjectMapper} required for JSON generation
	 */
	@JsonIgnore
	private static ObjectMapper				objectMapper;

	@JsonIgnore
	public static void configure(final ObjectMapper objectMapper) {
		ExchangeModelObject.objectMapper = objectMapper;
	}

	/**
	 * Creates a JSON String of the current {@link ExchangeModelObject}
	 * 
	 * @return JSON string
	 */
	@JsonIgnore
	public String toJSONString() {
		final val stringWriter = new StringWriter();

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
	public ModelObject getContainedModelObject() {
		ModelObject modelObject;

		try {
			modelObject = (ModelObject) objectMapper.readValue(this.content,
					Class.forName(this.packageAndClazz));
		} catch (final Exception e) {
			log.warn("Could not create model object from JSON: {} (JSON: {})",
					e.getMessage(), this.content);
			return null;
		}

		return modelObject;
	}
}
