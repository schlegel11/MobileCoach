package org.isgf.mhc.model;

import org.bson.types.ObjectId;
import org.jongo.Jongo;

/**
 * Provides all methods to modify model objects
 * 
 * @author Andreas Filler
 */
public abstract class AbstractModelObjectAccessService {
	protected void configure(final Jongo db) {
		ModelObject.configure(db);
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
	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass getModelObjectById(
			final Class<ModelObjectSubclass> clazz, final ObjectId id) {
		return ModelObject.get(clazz, id);
	}

	/**
	 * @see ModelObject#delete(Class, ObjectId)
	 */
	public void deleteModelObject(final Class<? extends ModelObject> clazz,
			final ObjectId id) {
		ModelObject.delete(clazz, id);
	}

	/**
	 * @see ModelObject#delete(Class, ObjectId)
	 */
	public void deleteModelObject(final Class<? extends ModelObject> clazz,
			final ModelObject modelObject) {
		ModelObject.delete(clazz, modelObject.getId());
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
	 * @see ModelObject#find(Class, String, Object...)
	 */
	public <ModelObjectSubclass extends ModelObject> Iterable<ModelObjectSubclass> findModelObjects(
			final Class<ModelObjectSubclass> clazz, final String query,
			final Object... parameters) {
		return ModelObject.find(clazz, query, parameters);
	}
}
