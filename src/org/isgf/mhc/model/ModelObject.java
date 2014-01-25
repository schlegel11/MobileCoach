package org.isgf.mhc.model;

import java.io.IOException;
import java.io.StringWriter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.tools.CustomObjectMapper;
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
	private ObjectId							id;

	/**
	 * {@link ObjectMapper} required for JSON generation
	 */
	@JsonIgnore
	private static ObjectMapper					objectMapper	= new CustomObjectMapper();

	/**
	 * {@link Jongo} object required for database access
	 */
	@JsonIgnore
	private static Jongo						db;

	/**
	 * {@link FileStorageManagerService} object required for file management
	 */
	@JsonIgnore
	@Getter(value = AccessLevel.PROTECTED)
	private static FileStorageManagerService	fileStorageManagerService;

	@JsonIgnore
	protected static void configure(final Jongo db) {
		ModelObject.db = db;
	}

	@JsonIgnore
	public static void configure(
			final FileStorageManagerService fileStorageManagerService) {
		ModelObject.fileStorageManagerService = fileStorageManagerService;
	}

	/**
	 * Creates a JSON string of the current {@link ModelObject}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#toJSONString()
	 */
	@JsonIgnore
	@Override
	public String toString() {
		return "[{" + this.getClass().getSimpleName() + "} id: " + this.id
				+ ", content: " + this.toJSONString() + "]";
	}

	/**
	 * Will automatically be called before deletion to enable recursive deletion
	 * of
	 * other objects
	 */
	@JsonIgnore
	protected void performOnRemove() {
		// Nothing, but can be overwritten
	}

	/**
	 * Saves {@link ModelObject} to database
	 */
	@JsonIgnore
	protected void save() {
		final MongoCollection collection = db.getCollection(this.getClass()
				.getSimpleName());

		collection.save(this);

		log.debug("Saved {} with id {}: {}", this.getClass().getSimpleName(),
				this.id, this);
	}

	/**
	 * Load {@link ModelObject} from database
	 * 
	 * @param clazz
	 *            The {@link ModelObject} subclass to retrieve
	 * @param id
	 *            The {@link ObjectId} of the {@link ModelObject}
	 * @return The retrieved {@link ModelObject} subclass object or
	 *         <code>null</code> if not found
	 */
	@JsonIgnore
	protected static final <ModelObjectSubclass extends ModelObject> ModelObjectSubclass get(
			final Class<ModelObjectSubclass> clazz, final ObjectId id) {
		final MongoCollection collection = db.getCollection(clazz
				.getSimpleName());

		ModelObjectSubclass modelObject = null;
		try {
			modelObject = collection.findOne(id).as(clazz);
			log.debug("Retrieved {} with id {}: {}", clazz.getSimpleName(), id,
					modelObject);
		} catch (final Exception e) {
			log.warn("Could not retrieve {} with id {}: {}",
					clazz.getSimpleName(), id, e.getMessage());
		}

		return modelObject;
	}

	/**
	 * Remove {@link ModelObject} from database
	 * 
	 * @param clazz
	 *            The {@link ModelObject} subclass to remove
	 * @param id
	 *            The {@link ObjectId} of the {@link ModelObject}
	 */
	@JsonIgnore
	protected static final void remove(
			final Class<? extends ModelObject> clazz, final ObjectId id) {
		final MongoCollection collection = db.getCollection(clazz
				.getSimpleName());

		try {
			final ModelObject objectToRemove = get(clazz, id);
			if (objectToRemove != null) {
				objectToRemove.performOnRemove();
			}
		} catch (final Exception e) {
			log.warn("Model object {} does not exist (before removal)");
		}

		try {
			collection.remove(id);
			log.debug("Removed {} with id {}", clazz.getSimpleName(), id);
		} catch (final Exception e) {
			log.warn("Could not remove {} with id {}: {}",
					clazz.getSimpleName(), id, e.getMessage());
		}
	}

	/**
	 * Find and load {@link ModelObject}s from database
	 * 
	 * @param clazz
	 *            The {@link ModelObject} subclass to retrieve
	 * @param query
	 *            The query to find the appropriate {@link ModelObject}
	 * @param parameters
	 *            The parameters to fill the query
	 * @return The retrieved {@link ModelObject} subclass objects as
	 *         {@link Iterable} or <code>null</code> if not
	 *         found
	 */
	@JsonIgnore
	protected static final <ModelObjectSubclass extends ModelObject> ModelObjectSubclass findOne(
			final Class<ModelObjectSubclass> clazz, final String query,
			final Object... parameters) {
		final MongoCollection collection = db.getCollection(clazz
				.getSimpleName());

		ModelObjectSubclass modelObject = null;
		try {
			if (parameters != null && parameters.length > 0) {
				modelObject = collection.findOne(query, parameters).as(clazz);
			} else {
				modelObject = collection.findOne(query).as(clazz);
			}
			log.debug(
					"Retrieved {} with find one query {} and parameters {}: {}",
					clazz.getSimpleName(), query, parameters, modelObject);
		} catch (final Exception e) {
			log.warn(
					"Could not retrieve {} with find one query {} and parameters {}: {}",
					clazz.getSimpleName(), query, parameters, e.getMessage());
		}

		return modelObject;
	}

	/**
	 * Find and load {@link ModelObject}s from database
	 * 
	 * @param clazz
	 *            The {@link ModelObject} subclass to retrieve
	 * @param query
	 *            The query to find the appropriate {@link ModelObject}
	 * @param parameters
	 *            The parameters to fill the query
	 * @return The retrieved {@link ModelObject} subclass objects as
	 *         {@link Iterable} or <code>null</code> if not
	 *         found
	 */
	@JsonIgnore
	protected static final <ModelObjectSubclass extends ModelObject> Iterable<ModelObjectSubclass> find(
			final Class<ModelObjectSubclass> clazz, final String query,
			final Object... parameters) {
		final MongoCollection collection = db.getCollection(clazz
				.getSimpleName());

		Iterable<ModelObjectSubclass> iteratable = null;
		try {
			if (parameters != null && parameters.length > 0) {
				iteratable = collection.find(query, parameters).as(clazz);
			} else {
				iteratable = collection.find(query).as(clazz);
			}
			log.debug("Retrieved {} with find query {} and parameters {}",
					clazz.getSimpleName(), query, parameters);
		} catch (final Exception e) {
			log.warn(
					"Could not retrieve {} with find query {} and parameters {}: {}",
					clazz.getSimpleName(), query, parameters, e.getMessage());
		}

		return iteratable;
	}
}
