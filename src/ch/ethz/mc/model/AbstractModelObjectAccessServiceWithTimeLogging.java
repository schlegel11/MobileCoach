package ch.ethz.mc.model;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.SerializationUtils;
/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
public abstract class AbstractModelObjectAccessServiceWithTimeLogging {
	private final ConcurrentHashMap<String, byte[]>	modelObjectsCache;

	private final long								longQueryThreshold	= 10;

	protected AbstractModelObjectAccessServiceWithTimeLogging() {
		modelObjectsCache = new ConcurrentHashMap<String, byte[]>();
	}

	protected void configure(final Jongo db) {
		ModelObject.configure(db);
	}

	protected void clearCache() {
		synchronized (modelObjectsCache) {
			modelObjectsCache.clear();
		}
	}

	/**
	 * @see ModelObject#save()
	 */
	public void saveModelObject(final ModelObject modelObject) {
		// Force new fetch from database after change
		if (modelObject != null && modelObject.getId() != null) {
			modelObjectsCache.remove(modelObject.getId().toHexString());
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

		final byte[] modelObjectByteArray = modelObjectsCache.get(hexId);

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
			modelObjectsCache.put(modelObject.getId().toHexString(),
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
			modelObjectsCache.remove(id.toHexString());
		}
	}

	/**
	 * @see ModelObject#delete(ModelObject)
	 */
	public void deleteModelObject(final ModelObject modelObject) {
		ModelObject.delete(modelObject);

		if (modelObject != null && modelObject.getId() != null) {
			modelObjectsCache.remove(modelObject.getId().toHexString());
		}
	}

	/**
	 * @see ModelObject#findOne(Class, String, Object...)
	 */
	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass findOneModelObject(
			final Class<ModelObjectSubclass> clazz, final String query,
			final Object... parameters) {

		final long timestamp = System.currentTimeMillis();

		val result = ModelObject.findOne(clazz, query, parameters);

		final long duration = System.currentTimeMillis() - timestamp;
		if (duration > longQueryThreshold) {
			log.warn("Slow query findOneModelObject: {} ms, {}, {}", duration,
					clazz, query);
		}

		return result;
	}

	/**
	 * @see ModelObject#findOneSorted(Class, String, Object...)
	 */
	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass findOneSortedModelObject(
			final Class<ModelObjectSubclass> clazz, final String query,
			final String sort, final Object... parameters) {

		final long timestamp = System.currentTimeMillis();

		val result = ModelObject.findOneSorted(clazz, query, sort, parameters);

		final long duration = System.currentTimeMillis() - timestamp;
		if (duration > longQueryThreshold) {
			log.warn("Slow query findOneSortedModelObject: {} ms, {}, {}, {}",
					duration, clazz, query, sort);
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

		final long duration = System.currentTimeMillis() - timestamp;
		if (duration > longQueryThreshold) {
			log.warn("Slow query findModelObjects: {} ms, {}, {}", duration,
					clazz, query);
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

		final long duration = System.currentTimeMillis() - timestamp;
		if (duration > longQueryThreshold) {
			log.warn("Slow query findModelObjectIds: {} ms, {}, {}", duration,
					clazz, query);
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

		final long duration = System.currentTimeMillis() - timestamp;
		if (duration > longQueryThreshold) {
			log.warn("Slow query findSortedModelObjects: {} ms, {}, {}, {}",
					duration, clazz, query, sort);
		}

		return result;
	}
}
