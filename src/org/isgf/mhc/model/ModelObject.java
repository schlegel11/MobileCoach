package org.isgf.mhc.model;

import java.io.IOException;
import java.io.StringWriter;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Basic class for model objects that should be stored in the database or
 * serialized as JSON objects
 * 
 * @author Andreas Filler
 */
@Log4j2
public abstract class ModelObject {
	/**
	 * The id of the {@link ModelObject}
	 */
	@Id
	@Getter
	@Setter
	private ObjectId			id;

	/**
	 * {@link ObjectMapper} required for JSON generation
	 */
	@JsonIgnore
	private static ObjectMapper	objectMapper	= new ObjectMapper();

	/**
	 * {@link Jongo} object required for database access
	 */
	@JsonIgnore
	private static Jongo		db;

	@JsonIgnore
	public static void configure(final ObjectMapper objectMapper, final Jongo db) {
		ModelObject.objectMapper = objectMapper;
		ModelObject.db = db;
	}

	/**
	 * Creates a JSON string of the current {@link ModelObject}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#toJSONString()
	 */
	@JsonIgnore
	@Override
	public String toString() {
		return this.toJSONString();
	}

	/**
	 * Saves {@link ModelObject} to database
	 */
	public void save() {
		final MongoCollection collection = db.getCollection(this.getClass()
				.getSimpleName());
		collection.save(this);
		log.debug("Saved {} with id {}", this.getClass().getSimpleName(),
				this.id);
	}
}
