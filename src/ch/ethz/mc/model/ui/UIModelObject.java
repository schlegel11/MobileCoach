package ch.ethz.mc.model.ui;

/* ##LICENSE## */
import lombok.NoArgsConstructor;
import lombok.NonNull;
import ch.ethz.mc.model.ModelObject;

/**
 * Basic class for model objects that should be displayed in the UI
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public abstract class UIModelObject extends UIObject {
	/**
	 * Contains the reference to the related model object
	 */
	@NonNull
	private ModelObject relatedModelObject;

	public <ModelObjectSubclass extends ModelObject> ModelObjectSubclass getRelatedModelObject(
			final Class<ModelObjectSubclass> modelObjectClass) {

		return modelObjectClass.cast(relatedModelObject);
	}

	public void setRelatedModelObject(final ModelObject relatedModelObject) {
		this.relatedModelObject = relatedModelObject;
	}
}
