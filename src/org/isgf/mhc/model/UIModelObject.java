package org.isgf.mhc.model;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;

/**
 * Basic class for model objects that should be displayed in the UI
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public abstract class UIModelObject {
	/**
	 * Contains the reference to the related model object
	 */
	@NonNull
	private ModelObject	relatedModelObject;

	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass getRelatedModelObject(
			final Class<ModelObjectSubclass> modelObjectClass) {

		return modelObjectClass.cast(relatedModelObject);
	}

	public void setRelatedModelObject(final ModelObject relatedModelObject) {
		this.relatedModelObject = relatedModelObject;
	}

	/**
	 * Returns the appropriate localization {@link String}, filled with given
	 * placeholders (if provided)
	 * 
	 * @param adminMessageString
	 * @param values
	 * @return
	 */
	protected static String localize(
			final AdminMessageStrings adminMessageString,
			final Object... values) {
		return Messages.getAdminString(adminMessageString, values);
	}
}
