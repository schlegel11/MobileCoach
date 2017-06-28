package ch.ethz.mc.model;

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
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.MongoId;

import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.tools.CustomObjectMapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Basic class for model objects that should be stored in the database or
 * serialized as JSON objects
 *
 * @author Andreas Filler
 */
@Log4j2
public abstract class ModelObject extends AbstractSerializableTable {
	/**
	 * The id of the {@link ModelObject}
	 */
	@MongoId
	@JsonProperty("_id")
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
		synchronized (objectMapper) {
			val stringWriter = new StringWriter();

			try {
				objectMapper.writeValue(stringWriter, this);
			} catch (final IOException e) {
				log.warn("Could not create JSON of {}!", this.getClass());
				return "{ \"Error\" : \"JSON object could not be serialized\" }";
			}

			return stringWriter.toString();
		}
	}

	/**
	 * Creates a multiline string of the current {@link ModelObject}
	 *
	 * @return
	 */
	@JsonIgnore
	public String toMultiLineString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toJSONString()
	 */
	@JsonIgnore
	@Override
	public String toString() {
		return "[" + this.getClass().getSimpleName() + "@" + id + ": "
				+ toJSONString() + "]";
	}

	/**
	 * Creates a {@link UIModelObject} of the current {@link ModelObject}
	 *
	 * @return
	 */
	@JsonIgnore
	public UIModelObject toUIModelObject() {
		log.error("A model object should have been transformed to a UI model object, but the conversion is not implemented!");
		throw new NotImplementedException(this.getClass().getName());
	}

	/**
	 * Will recursively collect all related {@link ModelObject} for export
	 *
	 * @param exportList
	 *            The {@link ModelObject} itself and all related
	 *            {@link ModelObject}s
	 */
	@JsonIgnore
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		// Nothing, but can be overwritten
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
		synchronized (db) {
			final MongoCollection collection = db.getCollection(this.getClass()
					.getSimpleName());

			collection.save(this);

			log.debug("Saved {} with id {}: {}", this.getClass()
					.getSimpleName(), id, this);
		}
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
		synchronized (db) {
			final MongoCollection collection = db.getCollection(clazz
					.getSimpleName());

			ModelObjectSubclass modelObject = null;
			try {
				modelObject = collection.findOne(id).as(clazz);
				log.debug("Retrieved {} with id {}: {}", clazz.getSimpleName(),
						id, modelObject);
			} catch (final Exception e) {
				log.warn("Could not retrieve {} with id {}: {}",
						clazz.getSimpleName(), id, e.getMessage());
			}

			return modelObject;
		}
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

		synchronized (db) {
			final MongoCollection collection = db.getCollection(modelObject
					.getClass().getSimpleName());

			try {
				if (modelObject != null) {
					log.debug(
							"Perform additionnal deletion steps on class {}...",
							modelObject.getClass().getSimpleName());
					modelObject.performOnDelete();
					log.debug("Additionnal deletion steps done on class {}",
							modelObject.getClass().getSimpleName());
				}
			} catch (final Exception e) {
				log.warn("Error at recursive deletion: {}", e.getMessage());
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
		synchronized (db) {
			final MongoCollection collection = db.getCollection(clazz
					.getSimpleName());

			try {
				final ModelObject modelObject = get(clazz, id);
				if (modelObject != null) {
					log.debug(
							"Perform additionnal deletion steps on class {}...",
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
	@Synchronized
	protected static final <ModelObjectSubclass extends ModelObject> ModelObjectSubclass findOne(
			final Class<ModelObjectSubclass> clazz, final String query,
			final Object... parameters) {
		synchronized (db) {
			final MongoCollection collection = db.getCollection(clazz
					.getSimpleName());

			ModelObjectSubclass modelObject = null;
			try {
				if (parameters != null && parameters.length > 0) {
					modelObject = collection.findOne(query, parameters).as(
							clazz);
				} else {
					modelObject = collection.findOne(query).as(clazz);
				}
				log.debug(
						"Retrieved {} with find one query {} and parameters {}: {}",
						clazz.getSimpleName(), query, parameters, modelObject);
			} catch (final Exception e) {
				log.warn(
						"Could not retrieve {} with find one query {} and parameters {}: {}",
						clazz.getSimpleName(), query, parameters,
						e.getMessage());
			}

			return modelObject;
		}
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
	@Synchronized
	protected static final <ModelObjectSubclass extends ModelObject> ModelObjectSubclass findOneSorted(
			final Class<ModelObjectSubclass> clazz, final String query,
			final String sort, final Object... parameters) {
		synchronized (db) {
			final MongoCollection collection = db.getCollection(clazz
					.getSimpleName());

			ModelObjectSubclass modelObject = null;
			try {
				try {
					if (parameters != null && parameters.length > 0) {
						modelObject = collection.find(query, parameters)
								.sort(sort).limit(1).as(clazz).iterator()
								.next();
					} else {
						modelObject = collection.find(query).sort(sort)
								.limit(1).as(clazz).iterator().next();
					}
				} catch (final NullPointerException f) {
					modelObject = null;
				} catch (final NoSuchElementException f) {
					modelObject = null;
				}
				log.debug(
						"Retrieved {} with find one query {}, sort query {} and parameters {}: {}",
						clazz.getSimpleName(), query, sort, parameters,
						modelObject);
			} catch (final Exception e) {
				log.warn(
						"Could not retrieve {} with find one query {}, sort query {} and parameters {}: {}",
						clazz.getSimpleName(), query, sort, parameters,
						e.getMessage());
			}

			return modelObject;
		}
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
	@Synchronized
	protected static final <ModelObjectSubclass extends ModelObject> Iterable<ModelObjectSubclass> find(
			final Class<ModelObjectSubclass> clazz, final String query,
			final Object... parameters) {
		synchronized (db) {
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
						clazz.getSimpleName(), query, parameters,
						e.getMessage());
			}

			return iteratable;
		}
	}

	/**
	 * Find and load {@link ObjectId}s from database
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
	@Synchronized
	protected static final Iterable<ObjectId> findIds(
			final Class<? extends ModelObject> clazz, final String query,
			final Object... parameters) {
		synchronized (db) {
			final MongoCollection collection = db.getCollection(clazz
					.getSimpleName());

			Iterable<ObjectId> iteratable = null;
			try {
				if (parameters != null && parameters.length > 0) {
					iteratable = collection.distinct("_id")
							.query(query, parameters).as(ObjectId.class);
				} else {
					iteratable = collection.distinct("_id").query(query)
							.as(ObjectId.class);
				}
				log.debug(
						"Retrieved id listing of {} with find query {} and parameters {}",
						clazz.getSimpleName(), query, parameters);
			} catch (final Exception e) {
				log.warn(
						"Could not retrieve id listing of {} with find query {} and parameters {}: {}",
						clazz.getSimpleName(), query, parameters,
						e.getMessage());
			}

			return iteratable;
		}
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
	@Synchronized
	protected static final <ModelObjectSubclass extends ModelObject> Iterable<ModelObjectSubclass> findSorted(
			final Class<ModelObjectSubclass> clazz, final String query,
			final String sort, final Object... parameters) {
		synchronized (db) {
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
}
