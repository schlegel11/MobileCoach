package ch.ethz.mc.model;

/* ##LICENSE## */
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.SerializationUtils;
import org.bson.types.ObjectId;
import org.jongo.Jongo;

import ch.ethz.mc.model.persistent.ParticipantVariableWithValue;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Provides all methods to modify model objects
 * 
 * @author Andreas Filler
 */
@Log4j2
public abstract class AbstractModelObjectAccessService {
	private final boolean										longQueryLogging			= false;
	private final long											longQueryThresholdInMillis	= 50;

	private final ConcurrentHashMap<String, byte[]>				idBasedIdToModelObjectCache;

	private final ConcurrentHashMap<String, HashSet<String>>	queryBasedIdToQueriesCache;
	private final ConcurrentHashMap<String, String>				queryBasedQueryToIdCache;
	private final ConcurrentHashMap<String, byte[]>				queryBasedIdToModelObjectCache;

	protected AbstractModelObjectAccessService() {
		idBasedIdToModelObjectCache = new ConcurrentHashMap<String, byte[]>();

		queryBasedIdToQueriesCache = new ConcurrentHashMap<String, HashSet<String>>();
		queryBasedQueryToIdCache = new ConcurrentHashMap<String, String>();
		queryBasedIdToModelObjectCache = new ConcurrentHashMap<String, byte[]>();
	}

	protected void configure(final Jongo db) {
		ModelObject.configure(db);
	}

	public void clearCache() {
		log.debug("Clearing all caches...");
		synchronized (idBasedIdToModelObjectCache) {
			idBasedIdToModelObjectCache.clear();
		}

		synchronized (queryBasedIdToQueriesCache) {
			synchronized (queryBasedQueryToIdCache) {
				synchronized (queryBasedIdToModelObjectCache) {
					queryBasedIdToQueriesCache.clear();
					queryBasedQueryToIdCache.clear();
					queryBasedIdToModelObjectCache.clear();
				}
			}
		}
	}

	/**
	 * @see ModelObject#save()
	 */
	public void saveModelObject(final ModelObject modelObject) {
		// Force new fetch from database after change
		if (modelObject != null && modelObject.getId() != null) {
			val hexIdKey = modelObject.getId().toHexString();
			log.debug("Removing {} from cache (while saving)...", hexIdKey);

			idBasedIdToModelObjectCache.remove(hexIdKey);

			synchronized (queryBasedIdToQueriesCache) {
				synchronized (queryBasedQueryToIdCache) {
					synchronized (queryBasedIdToModelObjectCache) {

						HashSet<String> queryKeys = null;
						if ((queryKeys = queryBasedIdToQueriesCache
								.get(hexIdKey)) != null) {
							queryKeys.forEach(
									queryKey -> queryBasedQueryToIdCache
											.remove(queryKey));
							queryBasedIdToQueriesCache.remove(hexIdKey);
							queryBasedIdToModelObjectCache.remove(hexIdKey);
						}
					}
				}
			}
		}

		modelObject.save();
	}

	/**
	 * @see ModelObject#get(Class, ObjectId)
	 */
	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass getModelObjectById(
			final Class<ModelObjectSubclass> clazz, final ObjectId id) {

		ModelObjectSubclass modelObject = null;

		val hexId = id.toHexString();

		final byte[] modelObjectByteArray = idBasedIdToModelObjectCache
				.get(hexId);

		if (modelObjectByteArray != null) {
			modelObject = clazz
					.cast(SerializationUtils.deserialize(modelObjectByteArray));

			return modelObject;
		}

		modelObject = ModelObject.get(clazz, id);

		// Don't cache ParticipantVariableWithValue (because they have their own
		// caching)
		if (modelObject != null
				&& !(modelObject instanceof ParticipantVariableWithValue)) {
			idBasedIdToModelObjectCache.put(modelObject.getId().toHexString(),
					SerializationUtils.serialize(modelObject));
		}

		return modelObject;
	}

	/**
	 * @see ModelObject#delete(Class, ObjectId)
	 */
	public void deleteModelObject(final Class<? extends ModelObject> clazz,
			final ObjectId id) {

		ModelObject.delete(clazz, id);

		if (id != null) {
			val hexIdKey = id.toHexString();
			log.debug("Removing {} from cache...", hexIdKey);

			idBasedIdToModelObjectCache.remove(hexIdKey);

			synchronized (queryBasedIdToQueriesCache) {
				synchronized (queryBasedQueryToIdCache) {
					synchronized (queryBasedIdToModelObjectCache) {

						HashSet<String> queryKeys = null;
						if ((queryKeys = queryBasedIdToQueriesCache
								.get(hexIdKey)) != null) {
							queryKeys.forEach(
									queryKey -> queryBasedQueryToIdCache
											.remove(queryKey));
							queryBasedIdToQueriesCache.remove(hexIdKey);
							queryBasedIdToModelObjectCache.remove(hexIdKey);
						}
					}
				}
			}
		}
	}

	/**
	 * @see ModelObject#delete(ModelObject)
	 */
	public void deleteModelObject(final ModelObject modelObject) {

		ModelObject.delete(modelObject);

		if (modelObject != null && modelObject.getId() != null) {
			val hexIdKey = modelObject.getId().toHexString();
			log.debug("Removing {} from cache...", hexIdKey);

			idBasedIdToModelObjectCache.remove(hexIdKey);

			synchronized (queryBasedIdToQueriesCache) {
				synchronized (queryBasedQueryToIdCache) {
					synchronized (queryBasedIdToModelObjectCache) {

						HashSet<String> queryKeys = null;
						if ((queryKeys = queryBasedIdToQueriesCache
								.get(hexIdKey)) != null) {
							queryKeys.forEach(
									queryKey -> queryBasedQueryToIdCache
											.remove(queryKey));
							queryBasedIdToQueriesCache.remove(hexIdKey);
							queryBasedIdToModelObjectCache.remove(hexIdKey);
						}
					}
				}
			}
		}
	}

	/**
	 * @see ModelObject#findOne(Class, String, Object...)
	 */
	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass findOneModelObject(
			final Class<ModelObjectSubclass> clazz, final String query,
			final Object... parameters) {

		ModelObjectSubclass modelObject = null;

		// Don't cache complex queries or ParticipantVariableWithValues (because
		// they have their own caching)
		if (parameters.length == 1 && parameters[0] instanceof ObjectId
				&& clazz != ParticipantVariableWithValue.class) {
			// Caching case
			final long timestamp = System.currentTimeMillis();

			synchronized (queryBasedIdToQueriesCache) {
				synchronized (queryBasedQueryToIdCache) {
					synchronized (queryBasedIdToModelObjectCache) {
						val queryKey = clazz.getName() + query
								+ ((ObjectId) parameters[0]).toHexString();
						String hexIdKey = queryBasedQueryToIdCache
								.get(queryKey);

						if (hexIdKey != null) {
							final byte[] modelObjectByteArray = queryBasedIdToModelObjectCache
									.get(hexIdKey);

							if (modelObjectByteArray != null) {
								modelObject = clazz.cast(SerializationUtils
										.deserialize(modelObjectByteArray));

								if (longQueryLogging) {
									final long duration = System
											.currentTimeMillis() - timestamp;
									if (duration > longQueryThresholdInMillis) {
										log.warn(
												"Slow CACHED query findOneModelObject: {} ms, {}, {}",
												duration, clazz, query);
									}
								}

								log.debug("Returning {} {} from cache",
										clazz.getName(), hexIdKey);

								return modelObject;
							}
						}

						modelObject = ModelObject.findOne(clazz, query,
								parameters);

						// Don't cache ParticipantVariableWithValue (because
						// they
						// have their
						// own
						// caching)
						if (modelObject != null
								&& !(modelObject instanceof ParticipantVariableWithValue)) {

							if (hexIdKey == null) {
								hexIdKey = modelObject.getId().toHexString();
							}

							log.debug("Caching {} {}...", clazz.getName(),
									hexIdKey);

							HashSet<String> queryKeys = null;
							if ((queryKeys = queryBasedIdToQueriesCache
									.get(hexIdKey)) == null) {
								queryKeys = new HashSet<String>();
								queryBasedIdToQueriesCache.put(hexIdKey,
										queryKeys);
							}
							queryKeys.add(queryKey);

							if (queryKeys.size() > 1) {
								System.err.println(" --> " + queryKeys.size());
							}

							queryBasedQueryToIdCache.put(queryKey, hexIdKey);
							queryBasedIdToModelObjectCache.put(hexIdKey,
									SerializationUtils.serialize(modelObject));
						}
					}
				}
			}

			if (longQueryLogging) {
				final long duration = System.currentTimeMillis() - timestamp;
				if (duration > longQueryThresholdInMillis) {
					log.warn(
							"Slow CACHING query findOneModelObject: {} ms, {}, {}",
							duration, clazz, query);
				}
			}
		} else {
			// Regular case
			final long timestamp = System.currentTimeMillis();

			modelObject = ModelObject.findOne(clazz, query, parameters);

			if (longQueryLogging) {
				final long duration = System.currentTimeMillis() - timestamp;
				if (duration > longQueryThresholdInMillis) {
					log.warn("Slow query findOneModelObject: {} ms, {}, {}",
							duration, clazz, query);
				}
			}
		}

		return modelObject;
	}

	/**
	 * @see ModelObject#findOneSorted(Class, String, Object...)
	 */
	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass findOneSortedModelObject(
			final Class<ModelObjectSubclass> clazz, final String query,
			final String sort, final Object... parameters) {
		final long timestamp = System.currentTimeMillis();

		val result = ModelObject.findOneSorted(clazz, query, sort, parameters);

		if (longQueryLogging) {
			final long duration = System.currentTimeMillis() - timestamp;
			if (duration > longQueryThresholdInMillis) {
				log.warn(
						"Slow query findOneSortedModelObject: {} ms, {}, {}, {}",
						duration, clazz, query, sort);
			}
		}

		return result;
	}

	/**
	 * @see ModelObject#find(Class, String, Object...)
	 */
	public <ModelObjectSubclass extends ModelObject> Iterable<ModelObjectSubclass> findModelObjects(
			final Class<ModelObjectSubclass> clazz, final String query,
			final Object... parameters) {

		final long timestamp = System.currentTimeMillis();

		val result = ModelObject.find(clazz, query, parameters);

		if (longQueryLogging) {
			final long duration = System.currentTimeMillis() - timestamp;
			if (duration > longQueryThresholdInMillis) {
				log.warn("Slow query findModelObjects: {} ms, {}, {}", duration,
						clazz, query);
			}
		}

		return result;
	}

	/**
	 * @see ModelObject#findIds(Class, String, Object...)
	 */
	public Iterable<ObjectId> findModelObjectIds(
			final Class<? extends ModelObject> clazz, final String query,
			final Object... parameters) {

		final long timestamp = System.currentTimeMillis();

		val result = ModelObject.findIds(clazz, query, parameters);

		if (longQueryLogging) {
			final long duration = System.currentTimeMillis() - timestamp;
			if (duration > longQueryThresholdInMillis) {
				log.warn("Slow query findModelObjectIds: {} ms, {}, {}",
						duration, clazz, query);
			}
		}

		return result;
	}

	/**
	 * @see ModelObject#findSorted(Class, String, String, Object...)
	 */
	public <ModelObjectSubclass extends ModelObject> Iterable<ModelObjectSubclass> findSortedModelObjects(
			final Class<ModelObjectSubclass> clazz, final String query,
			final String sort, final Object... parameters) {

		final long timestamp = System.currentTimeMillis();

		val result = ModelObject.findSorted(clazz, query, sort, parameters);

		if (longQueryLogging) {
			final long duration = System.currentTimeMillis() - timestamp;
			if (duration > longQueryThresholdInMillis) {
				log.warn("Slow query findSortedModelObjects: {} ms, {}, {}, {}",
						duration, clazz, query, sort);
			}
		}

		return result;
	}
}
