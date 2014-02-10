package org.isgf.mhc.ui.views.components;

import java.util.List;

import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.UIModelObject;
import org.isgf.mhc.services.InterventionAdministrationManagerService;
import org.isgf.mhc.ui.AdminNavigatorUI;
import org.isgf.mhc.ui.NotificationMessageException;
import org.isgf.mhc.ui.UISession;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Provides methods for all {@link CustomComponent}s
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public abstract class AbstractCustomComponent extends CustomComponent {

	protected InterventionAdministrationManagerService getInterventionAdministrationManagerService() {
		return MHC.getInstance().getInterventionAdministrationManagerService();
	}

	protected UISession getUISession() {
		return UI.getCurrent().getSession().getAttribute(UISession.class);
	}

	protected AdminNavigatorUI getAdminUI() {
		return (AdminNavigatorUI) UI.getCurrent();
	}

	/**
	 * Provides a {@link ClickListener} which can also close the belonging
	 * window
	 * 
	 * @author Andreas Filler
	 */
	public abstract class ExtendableButtonClickListener implements
			Button.ClickListener {
		@Setter
		private Window								belongingWindow;

		@Setter
		private AbstractStringValueEditComponent	belongingStringValueEditComponent;

		/**
		 * Returns string value of belonging {@link AbstractCustomComponent}
		 * 
		 * @return
		 */
		protected String getStringValue() {
			return belongingStringValueEditComponent.getStringValue();
		}

		/**
		 * Closes belonging {@link Window}
		 */
		protected void closeWindow() {
			belongingWindow.close();
		}
	}

	/**
	 * Shows a model window to edit {@link String}s
	 * 
	 * @param title
	 *            The title of the window
	 * @param valueToEdit
	 *            The value to edit or <code>null</code> if not set yet
	 * @param variablesToSelect
	 *            The list of variables that should be available for selection
	 *            or <code>null</code> if not required
	 * @param stringValueComponent
	 *            The appropriate {@link AbstractStringValueEditComponent} to
	 *            create
	 * @param okButtonClickListener
	 *            The listener for the OK button
	 * @param cancelButtonClickListener
	 *            The listener for the Cancel button
	 * @return The shown window
	 */
	protected Window showModalStringValueEditWindow(
			final AdminMessageStrings title, final String valueToEdit,
			final List<String> variablesToSelect,
			final AbstractStringValueEditComponent stringValueComponent,
			final ExtendableButtonClickListener okButtonClickListener,
			final ExtendableButtonClickListener cancelButtonClickListener) {
		val modalWindow = new Window(Messages.getAdminString(title));
		modalWindow.setModal(true);
		modalWindow.setResizable(false);
		modalWindow.setClosable(false);
		modalWindow.setContent(stringValueComponent);

		// Set value to edit
		if (valueToEdit == null) {
			stringValueComponent.setStringValue("");
		} else {
			stringValueComponent.setStringValue(valueToEdit);
		}

		// Set variables if not null
		if (variablesToSelect != null) {
			stringValueComponent.addAll(variablesToSelect);
		}

		// Register ok button listener
		if (okButtonClickListener != null) {
			okButtonClickListener.setBelongingWindow(modalWindow);
			okButtonClickListener
					.setBelongingStringValueEditComponent(stringValueComponent);
			stringValueComponent
					.registerOkButtonListener(okButtonClickListener);
		}

		// Register cancel button listener if provided or a simple window closer
		// if not
		if (cancelButtonClickListener != null) {
			cancelButtonClickListener.setBelongingWindow(modalWindow);
			cancelButtonClickListener
					.setBelongingStringValueEditComponent(stringValueComponent);
			stringValueComponent
					.registerCancelButtonListener(cancelButtonClickListener);
		} else {
			stringValueComponent
					.registerCancelButtonListener(new Button.ClickListener() {
						@Override
						public void buttonClick(final ClickEvent event) {
							modalWindow.close();
						}
					});
		}

		// show window
		getAdminUI().addWindow(modalWindow);

		return modalWindow;
	}

	/**
	 * Creates an appropriate {@link BeanItemContainer} for a specific
	 * {@link Iterable} containing {@link ModelObject}s
	 * 
	 * @param modelClass
	 * @param iterableModelObjects
	 * @return
	 */
	protected <UIModelObjectSubclass extends UIModelObject> BeanContainer<ObjectId, UIModelObjectSubclass> createBeanContainerForModelObjects(
			final Class<UIModelObjectSubclass> uiModelObjectSubclass,
			final Iterable<? extends ModelObject> iterableModelObjects) {

		final BeanContainer<ObjectId, UIModelObjectSubclass> beanContainer = new BeanContainer<ObjectId, UIModelObjectSubclass>(
				uiModelObjectSubclass);

		for (final ModelObject modelObject : iterableModelObjects) {
			beanContainer.addItem(modelObject.getId(),
					uiModelObjectSubclass.cast(modelObject.toUIModelObject()));
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
			log.debug("Expected error occurred: {}", exception.getMessage());
		} else {
			getAdminUI().showErrorNotification(
					AdminMessageStrings.NOTIFICATION__UNKNOWN_ERROR);
			log.error("An unexpected error occurred: {}",
					exception.getMessage());
		}
	}
}
