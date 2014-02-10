package org.isgf.mhc.ui.views.components;

import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.UIModelObject;
import org.isgf.mhc.ui.AdminNavigatorUI;
import org.isgf.mhc.ui.NotificationMessageException;
import org.isgf.mhc.ui.UISession;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

/**
 * Provides methods for all {@link CustomComponent}s
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public abstract class AbstractCustomComponent extends CustomComponent {

	protected UISession getUISession() {
		return UI.getCurrent().getSession().getAttribute(UISession.class);
	}

	protected AdminNavigatorUI getAdminUI() {
		return (AdminNavigatorUI) UI.getCurrent();
	}

	/**
	 * Creates an appropriate {@link BeanItemContainer} for a specific
	 * {@link Iterable} containing {@link ModelObject}s
	 * 
	 * @param modelClass
	 * @param iterableModelObjects
	 * @return
	 */
	protected <UIModelObjectSubclass extends UIModelObject> BeanContainer<ObjectId, UIModelObjectSubclass> createBeanContainer(
			final Class<UIModelObjectSubclass> modelObjectSubclass,
			final Iterable<? extends ModelObject> iterableModelObjects) {

		final BeanContainer<ObjectId, UIModelObjectSubclass> beanContainer = new BeanContainer<ObjectId, UIModelObjectSubclass>(
				modelObjectSubclass);

		for (final ModelObject modelObject : iterableModelObjects) {
			beanContainer.addItem(modelObject.getId(),
					modelObjectSubclass.cast(modelObject.toUIModelObject()));
		}

		return beanContainer;
	}

	/**
	 * Returns the {@link UIModelObject} fitting to the item selected in a
	 * {@link Table}
	 * 
	 * @param table
	 * @param beanItemClass
	 * @param objectId
	 * @return
	 */
	protected <BeanItemClass extends UIModelObject> BeanItemClass getUIModelObjectFromTableByObjectId(
			final Table table, final Class<BeanItemClass> beanItemClass,
			final Object objectId) {

		@SuppressWarnings("unchecked")
		final BeanItemClass beanItem = ((BeanItem<BeanItemClass>) table
				.getItem(objectId)).getBean();

		return beanItem;
	}

	protected Property<String> getStringItemProperty(
			final BeanItem<? extends UIModelObject> beanItem, final String type) {

		@SuppressWarnings("unchecked")
		final Property<String> itemProperty = beanItem.getItemProperty(type);

		return itemProperty;
	}

	/**
	 * Returns the {@link BeanItem} fitting to the item selected in a
	 * {@link Table}
	 * 
	 * @param table
	 * @param beanItemClass
	 * @param objectId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <BeanItemClass extends UIModelObject> BeanItem<BeanItemClass> getBeanItemFromTableByObjectId(
			final Table table, final Class<BeanItemClass> beanItemClass,
			final Object objectId) {
		return (BeanItem<BeanItemClass>) table.getItem(objectId);
	}

	/**
	 * Localizes a specific {@link CustomComponent} with the given localization
	 * {@link AdminMessageStrings}
	 * 
	 * @param component
	 * @param adminMessageString
	 * @param values
	 */
	protected void localize(final AbstractComponent component,
			final AdminMessageStrings adminMessageString,
			final Object... values) {
		final String valueToSet = Messages.getAdminString(adminMessageString,
				values);

		if (component instanceof Label) {
			((Label) component).setValue(valueToSet);
		} else {
			component.setCaption(valueToSet);
		}
	}

	/**
	 * Handles {@link Exception}s in a UI compatible way
	 * 
	 * @param exception
	 */
	protected void handleException(final Exception exception) {
		if (exception instanceof NotificationMessageException) {
			getAdminUI().showWarningNotification(
					((NotificationMessageException) exception)
							.getNotificationMessage());
			log.debug("Expected error occured: {}", exception.getMessage());
		} else {
			getAdminUI().showErrorNotification(
					AdminMessageStrings.NOTIFICATION__UNKNOWN_ERROR);
			log.error("An unexpected error occured: {}", exception.getMessage());
		}
	}
}
