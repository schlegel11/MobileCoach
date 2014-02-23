package org.isgf.mhc.model;

import java.io.IOException;
import java.io.StringWriter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.NotImplementedException;
import org.bson.types.ObjectId;
import org.isgf.mhc.model.ui.UIModelObject;
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
		return "[{" + this.getClass().getSimpleName() + "} id: " + id
				+ ", content: " + toJSONString() + "]";
	}

	/**
	 * Creates a {@link UIModelObject} of the current {@link ModelObject}
	 * 
	 * @return
	 */
	@JsonIgnore
	public UIModelObject toUIModelObject() {
		log.error("A model object should have been transformed to a UI model object, but the conversion is not implemented!");
		throw new NotImplementedException(this.getClass());
	}

	/**
	 * Will automatically be called before deletion to enable recursive deletion
	 * of other objects
	 */
	@JsonIgnore
	protected void performOnDelete() {
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
				id, this);
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
	 * Deletes {@link ModelObject} from database
	 * 
	 * @param modelObject
	 *            The {@link ModelObject} to delete
	 */
	@JsonIgnore
	protected static final void delete(final ModelObject modelObject) {
		if (modelObject == null) {
			log.warn("Model object does not exist (before delete)");
			return;
		}

		final MongoCollection collection = db.getCollection(modelObject
				.getClass().getSimpleName());

		try {
			if (modelObject != null) {
				log.debug("Perform additionnal deletion steps on class {}...",
						modelObject.getClass().getSimpleName());
				modelObject.performOnDelete();
				log.debug("Additionnal deletion steps done on class {}",
						modelObject.getClass().getSimpleName());
			}
		} catch (final Exception e) {
			log.warn("Error at recursive deleteion: {}", e.getMessage());
		}

		try {
			collection.remove(modelObject.getId());
			log.debug("Removed {} with id {}", modelObject.getClass()
					.getSimpleName(), modelObject.getId());
		} catch (final Exception e) {
			log.warn("Could not delete {} with id {}: {}", modelObject
					.getClass().getSimpleName(), modelObject.getId(), e
					.getMessage());
		}
	}

	/**
	 * Deletes {@link ModelObject} from database
	 * 
	 * @param clazz
	 *            The {@link ModelObject} subclass to delete
	 * @param id
	 *            The {@link ObjectId} of the {@link ModelObject}
	 */
	@JsonIgnore
	protected static final void delete(
			final Class<? extends ModelObject> clazz, final ObjectId id) {
		final MongoCollection collection = db.getCollection(clazz
				.getSimpleName());

		try {
			final ModelObject modelObject = get(clazz, id);
			if (modelObject != null) {
				log.debug("Perform additionnal deletion steps on class {}...",
						modelObject.getClass().getSimpleName());
				modelObject.performOnDelete();
				log.debug("Additionnal deletion steps done on class {}",
						modelObject.getClass().getSimpleName());
			}
		} catch (final Exception e) {
			log.warn("Error at recursive deletion: {}", e.getMessage());
		}

		try {
			collection.remove(id);
			log.debug("Removed {} with id {}", clazz.getSimpleName(), id);
		} catch (final Exception e) {
			log.warn("Could not delete {} with id {}: {}",
					clazz.getSimpleName(), id, e.getMessage());
		}
	}

	/**
	 * Deletes several {@link ModelObject}s from database
	 * 
	 * @param modelObjects
	 *            The {@link ModelObject}s to delete
	 */
	@JsonIgnore
	protected static final void delete(
			final Iterable<? extends ModelObject> modelObjects) {
		for (final ModelObject modelObject : modelObjects) {
			delete(modelObject);
		}
	}

	/**
	 * Find and load a {@link ModelObject} from database
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
	 * Find and load a {@link ModelObject} from database after sorting
	 * 
	 * @param clazz
	 *            The {@link ModelObject} subclass to retrieve
	 * @param query
	 *            The query to find the appropriate {@link ModelObject}
	 * @param sort
	 *            The sort rules to sort the appropriate {@link ModelObject}
	 * @param parameters
	 *            The parameters to fill the query
	 * @return The retrieved {@link ModelObject} subclass objects as
	 *         {@link Iterable} or <code>null</code> if not
	 *         found
	 */
	@JsonIgnore
	protected static final <ModelObjectSubclass extends ModelObject> ModelObjectSubclass findOneSorted(
			final Class<ModelObjectSubclass> clazz, final String query,
			final String sort, final Object... parameters) {
		final MongoCollection collection = db.getCollection(clazz
				.getSimpleName());

		ModelObjectSubclass modelObject = null;
		try {
			try {
				if (parameters != null && parameters.length > 0) {
					modelObject = collection.find(query, parameters).sort(sort)
							.limit(1).as(clazz).iterator().next();
				} else {
					modelObject = collection.find(query).sort(sort).limit(1)
							.as(clazz).iterator().next();
				}
			} catch (final NullPointerException f) {
				modelObject = null;
			}
			log.debug(
					"Retrieved {} with find one query {}, sort query {} and parameters {}: {}",
					clazz.getSimpleName(), query, sort, parameters, modelObject);
		} catch (final Exception e) {
			log.warn(
					"Could not retrieve {} with find one query {}, sort query {} and parameters {}: {}",
					clazz.getSimpleName(), query, sort, parameters,
					e.getMessage());
		}

		return modelObject;
	}

	/**
	 * Find and load {@link ModelObject}s from database
	 * 
	 * @param clazz
	 *            The {@link ModelObject} subclass to retrieve
	 * @param query
	 *            The query to find the appropriate {@link ModelObject}s
	 * @param parameters
	 *            The parameters to fill the query
	 * @return The retrieved {@link ModelObject} subclass objects as
	 *         {@link Iterable} (which contains no items if none has been found)
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

	/**
	 * Find, loads and sort {@link ModelObject}s from database
	 * 
	 * @param clazz
	 *            The {@link ModelObject} subclass to retrieve
	 * @param query
	 *            The query to find the appropriate {@link ModelObject}s
	 * @param sort
	 *            The sort rules to sort the appropriate {@link ModelObject}s
	 * @param parameters
	 *            The parameters to fill the query
	 * @return The retrieved {@link ModelObject} subclass objects as
	 *         {@link Iterable} (which contains no items if none has been found)
	 */
	@JsonIgnore
	protected static final <ModelObjectSubclass extends ModelObject> Iterable<ModelObjectSubclass> findSorted(
			final Class<ModelObjectSubclass> clazz, final String query,
			final String sort, final Object... parameters) {
		final MongoCollection collection = db.getCollection(clazz
				.getSimpleName());

		Iterable<ModelObjectSubclass> iteratable = null;
		try {
			if (parameters != null && parameters.length > 0) {
				iteratable = collection.find(query, parameters).sort(sort)
						.as(clazz);
			} else {
				iteratable = collection.find(query).sort(sort).as(clazz);
			}
			log.debug(
					"Retrieved {} with find query {}, sort query {} and parameters {}",
					clazz.getSimpleName(), query, sort, parameters);
		} catch (final Exception e) {
			log.warn(
					"Could not retrieve {} with find query {}, sort query {} and parameters {}: {}",
					clazz.getSimpleName(), query, sort, parameters,
					e.getMessage());
		}

		return iteratable;
	}
}
