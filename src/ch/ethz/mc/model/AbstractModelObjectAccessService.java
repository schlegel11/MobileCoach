package ch.ethz.mc.model;

import java.util.concurrent.ConcurrentHashMap;

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

import lombok.val;

/**
 * Provides all methods to modify model objects
 * 
 * @author Andreas Filler
 */
public abstract class AbstractModelObjectAccessService {
	private final ConcurrentHashMap<String, ModelObject> modelObjectsCache;

	protected AbstractModelObjectAccessService() {
		modelObjectsCache = new ConcurrentHashMap<String, ModelObject>();
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
		modelObject.save();
	}

	/**
	 * @see ModelObject#get(Class, ObjectId)
	 */
	@SuppressWarnings("unchecked")
	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass getModelObjectById(
			final Class<ModelObjectSubclass> clazz, final ObjectId id) {

		ModelObjectSubclass modelObject = null;

		val hexId = id.toHexString();
		modelObject = (ModelObjectSubclass) modelObjectsCache.get(hexId);

		if (modelObject != null) {
			return modelObject;
		}

		modelObject = ModelObject.get(clazz, id);

		if (modelObject != null) {
			modelObjectsCache.put(modelObject.getId().toHexString(),
					modelObject);
		}

		return modelObject;
	}

	/**
	 * @see ModelObject#delete(Class, ObjectId)
	 */
	public void deleteModelObject(final Class<? extends ModelObject> clazz,
			final ObjectId id) {
		ModelObject.delete(clazz, id);

		modelObjectsCache.remove(id.toHexString());
	}

	/**
	 * @see ModelObject#delete(ModelObject)
	 */
	public void deleteModelObject(final ModelObject modelObject) {
		ModelObject.delete(modelObject);

		modelObjectsCache.remove(modelObject.getId().toHexString());
	}

	/**
	 * @see ModelObject#findOne(Class, String, Object...)
	 */
	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass findOneModelObject(
			final Class<ModelObjectSubclass> clazz, final String query,
			final Object... parameters) {
		return ModelObject.findOne(clazz, query, parameters);
	}

	/**
	 * @see ModelObject#findOneSorted(Class, String, Object...)
	 */
	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass findOneSortedModelObject(
			final Class<ModelObjectSubclass> clazz, final String query,
			final String sort, final Object... parameters) {
		return ModelObject.findOneSorted(clazz, query, sort, parameters);
	}

	/**
	 * @see ModelObject#find(Class, String, Object...)
	 */
	public <ModelObjectSubclass extends ModelObject> Iterable<ModelObjectSubclass> findModelObjects(
			final Class<ModelObjectSubclass> clazz, final String query,
			final Object... parameters) {
		return ModelObject.find(clazz, query, parameters);
	}

	/**
	 * @see ModelObject#findIds(Class, String, Object...)
	 */
	public Iterable<ObjectId> findModelObjectIds(
			final Class<? extends ModelObject> clazz, final String query,
			final Object... parameters) {
		return ModelObject.findIds(clazz, query, parameters);
	}

	/**
	 * @see ModelObject#findSorted(Class, String, String, Object...)
	 */
	public <ModelObjectSubclass extends ModelObject> Iterable<ModelObjectSubclass> findSortedModelObjects(
			final Class<ModelObjectSubclass> clazz, final String query,
			final String sort, final Object... parameters) {
		return ModelObject.findSorted(clazz, query, sort, parameters);
	}
}
