package ch.ethz.mc.ui.views.components;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import java.util.List;

import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.UIObject;
import ch.ethz.mc.services.InterventionAdministrationManagerService;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.ModuleManagerService;
import ch.ethz.mc.services.SurveyAdministrationManagerService;
import ch.ethz.mc.services.SurveyExecutionManagerService;
import ch.ethz.mc.ui.AdminNavigatorUI;
import ch.ethz.mc.ui.NotificationMessageException;
import ch.ethz.mc.ui.UISession;
import ch.ethz.mc.ui.views.components.basics.ConfirmationComponent;
import ch.ethz.mc.ui.views.helper.CaseInsensitiveItemSorter;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
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
		return MC.getInstance().getInterventionAdministrationManagerService();
	}

	protected InterventionExecutionManagerService getInterventionExecutionManagerService() {
		return MC.getInstance().getInterventionExecutionManagerService();
	}

	protected SurveyAdministrationManagerService getSurveyAdministrationManagerService() {
		return MC.getInstance().getSurveyAdministrationManagerService();
	}

	protected SurveyExecutionManagerService getSurveyExecutionManagerService() {
		return MC.getInstance().getSurveyExecutionManagerService();
	}

	protected ModuleManagerService getModuleManagerService() {
		return MC.getInstance().getModuleManagerService();
	}

	protected UISession getUISession() {
		return getAdminUI().getUISession();
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
		private Window							belongingWindow;

		@Setter
		private AbstractConfirmationComponent	belongingComponent;

		/**
		 * Returns string value of belonging {@link AbstractCustomComponent}
		 *
		 * @return
		 */
		protected String getStringValue() {
			if (belongingComponent instanceof AbstractStringValueEditComponent) {
				return ((AbstractStringValueEditComponent) belongingComponent)
						.getStringValue();
			} else {
				return null;
			}
		}

		/**
		 * Returns localized string value of belonging
		 * {@link AbstractCustomComponent}
		 *
		 * @return
		 */
		protected LString getLStringValue() {
			if (belongingComponent instanceof AbstractLStringValueEditComponent) {
				return ((AbstractLStringValueEditComponent) belongingComponent)
						.getLStringValue();
			} else {
				return null;
			}
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
			stringValueComponent.addVariables(variablesToSelect);
		}

		// Register ok button listener
		if (okButtonClickListener != null) {
			okButtonClickListener.setBelongingWindow(modalWindow);
			okButtonClickListener.setBelongingComponent(stringValueComponent);
			stringValueComponent
			.registerOkButtonListener(okButtonClickListener);
		}

		// Register cancel button listener if provided or a simple window closer
		// if not
		if (cancelButtonClickListener != null) {
			cancelButtonClickListener.setBelongingWindow(modalWindow);
			cancelButtonClickListener
			.setBelongingComponent(stringValueComponent);
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
	 * Shows a model window to edit {@link LString}s
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
	protected Window showModalLStringValueEditWindow(
			final AdminMessageStrings title, final LString valueToEdit,
			final List<String> variablesToSelect,
			final AbstractLStringValueEditComponent stringValueComponent,
			final ExtendableButtonClickListener okButtonClickListener,
			final ExtendableButtonClickListener cancelButtonClickListener) {
		val modalWindow = new Window(Messages.getAdminString(title));
		modalWindow.setModal(true);
		modalWindow.setResizable(false);
		modalWindow.setClosable(false);
		modalWindow.setContent(stringValueComponent);

		// Set value to edit
		if (valueToEdit == null) {
			stringValueComponent.setLStringValue(new LString());
		} else {
			stringValueComponent.setLStringValue(valueToEdit);
		}

		// Set variables if not null
		if (variablesToSelect != null) {
			stringValueComponent.addVariables(variablesToSelect);
		}

		// Register ok button listener
		if (okButtonClickListener != null) {
			okButtonClickListener.setBelongingWindow(modalWindow);
			okButtonClickListener.setBelongingComponent(stringValueComponent);
			stringValueComponent
			.registerOkButtonListener(okButtonClickListener);
		}

		// Register cancel button listener if provided or a simple window closer
		// if not
		if (cancelButtonClickListener != null) {
			cancelButtonClickListener.setBelongingWindow(modalWindow);
			cancelButtonClickListener
			.setBelongingComponent(stringValueComponent);
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
	 * Shows a model and closable edit window
	 *
	 * @param title
	 *            The title of the window
	 * @param modelObjectToEdit
	 *            The {@link ModelObject} to edit
	 * @param closableEditComponent
	 *            The appropriate {@link AbstractStringValueEditComponent} to
	 *            create
	 * @param okButtonClickListener
	 *            The listener for the OK button
	 * @param cancelButtonClickListener
	 *            The listener for the Cancel button
	 * @param titleValues
	 *            The placeholders to use in the title
	 * @return The shown window
	 */
	protected Window showModalClosableEditWindow(
			final AdminMessageStrings title,
			final AbstractClosableEditComponent closableEditComponent,
			final ExtendableButtonClickListener closeButtonClickListener,
			final Object... titleValues) {
		return showModalClosableEditWindow(
				Messages.getAdminString(title, titleValues),
				closableEditComponent, closeButtonClickListener, titleValues);
	}

	/**
	 * Shows a model and closable edit window
	 *
	 * @param title
	 *            The title of the window
	 * @param modelObjectToEdit
	 *            The {@link ModelObject} to edit
	 * @param closableEditComponent
	 *            The appropriate {@link AbstractStringValueEditComponent} to
	 *            create
	 * @param okButtonClickListener
	 *            The listener for the OK button
	 * @param cancelButtonClickListener
	 *            The listener for the Cancel button
	 * @param titleValues
	 *            The placeholders to use in the title
	 * @return The shown window
	 */
	protected Window showModalClosableEditWindow(final String title,
			final AbstractClosableEditComponent closableEditComponent,
			final ExtendableButtonClickListener closeButtonClickListener,
			final Object... titleValues) {
		val modalWindow = new Window(title);
		modalWindow.setModal(true);
		modalWindow.setResizable(false);
		modalWindow.setClosable(false);
		modalWindow.setContent(closableEditComponent);

		// Register close button listener if provided or a simple window closer
		// if not
		if (closeButtonClickListener != null) {
			closeButtonClickListener.setBelongingWindow(modalWindow);
			closeButtonClickListener
			.setBelongingComponent(closableEditComponent);
			closableEditComponent
			.registerOkButtonListener(closeButtonClickListener);
		} else {
			closableEditComponent
			.registerOkButtonListener(new Button.ClickListener() {
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

	protected Window showConfirmationWindow(
			final ExtendableButtonClickListener okButtonClickListener,
			final ExtendableButtonClickListener cancelButtonClickListener) {

		val confirmationComponent = new ConfirmationComponent();

		val modalWindow = new Window(
				Messages.getAdminString(AdminMessageStrings.CONFIRMATION_WINDOW__TITLE));
		modalWindow.setModal(true);
		modalWindow.setResizable(false);
		modalWindow.setClosable(false);
		modalWindow.setContent(confirmationComponent);

		// Register ok button listener
		if (okButtonClickListener != null) {
			okButtonClickListener.setBelongingWindow(modalWindow);
			okButtonClickListener.setBelongingComponent(confirmationComponent);
			confirmationComponent
			.registerOkButtonListener(okButtonClickListener);
		}

		// Register cancel button listener if provided or a simple window closer
		// if not
		if (cancelButtonClickListener != null) {
			cancelButtonClickListener.setBelongingWindow(modalWindow);
			cancelButtonClickListener
			.setBelongingComponent(confirmationComponent);
			confirmationComponent
			.registerCancelButtonListener(cancelButtonClickListener);
		} else {
			confirmationComponent
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
	 * Creates an appropriate {@link BeanContainer} for a specific
	 * {@link Iterable} containing {@link ModelObject}s
	 *
	 * @param uiModelObjectSubclass
	 * @param iterableModelObjects
	 * @return
	 */
	protected <UIObjectSubclass extends UIObject> BeanContainer<ObjectId, UIObjectSubclass> createBeanContainerForModelObjects(
			final Class<UIObjectSubclass> uiModelObjectSubclass,
			final Iterable<? extends ModelObject> iterableModelObjects) {

		final BeanContainer<ObjectId, UIObjectSubclass> beanContainer = new BeanContainer<ObjectId, UIObjectSubclass>(
				uiModelObjectSubclass);

		beanContainer.setItemSorter(new CaseInsensitiveItemSorter());

		if (iterableModelObjects != null) {
			for (final ModelObject modelObject : iterableModelObjects) {
				beanContainer.addItem(modelObject.getId(),
						uiModelObjectSubclass.cast(modelObject
								.toUIModelObject()));
			}
		}

		return beanContainer;
	}

	/**
	 * Updates a {@link BeanContainer} with the provided {@link Iterable}
	 * containing {@link ModelObject}s
	 *
	 * @param beanContainer
	 * @param uiModelObjectSubclass
	 * @param iterableModelObjects
	 */
	protected <UIObjectSubclass extends UIObject> void refreshBeanContainer(
			final BeanContainer<ObjectId, UIObjectSubclass> beanContainer,
			final Class<UIObjectSubclass> uiModelObjectSubclass,
			final Iterable<? extends ModelObject> iterableModelObjects) {

		beanContainer.removeAllItems();

		for (final ModelObject modelObject : iterableModelObjects) {
			beanContainer.addItem(modelObject.getId(),
					uiModelObjectSubclass.cast(modelObject.toUIModelObject()));
		}
	}

	/**
	 * Creates an appropriate {@link BeanContainer} for a specific
	 * {@link Iterable} containing {@link ModelObject}s represented by only ONE
	 * property
	 *
	 * @param uiObjectSubclass
	 * @param iterableModelObjects
	 * @return
	 */
	protected <UIObjectSubclass extends UIObject> BeanContainer<UIObjectSubclass, UIObjectSubclass> createSimpleBeanContainerForModelObjects(
			final Class<UIObjectSubclass> uiObjectSubclass,
			final Iterable<? extends ModelObject> iterableModelObjects) {

		final BeanContainer<UIObjectSubclass, UIObjectSubclass> beanContainer = new BeanContainer<UIObjectSubclass, UIObjectSubclass>(
				uiObjectSubclass);

		beanContainer.setItemSorter(new CaseInsensitiveItemSorter());

		for (final ModelObject modelObject : iterableModelObjects) {
			val uiModelObject = modelObject.toUIModelObject();
			beanContainer.addItem(uiObjectSubclass.cast(uiModelObject),
					uiObjectSubclass.cast(uiModelObject));
		}

		return beanContainer;
	}

	/**
	 * Returns the {@link UIObject} fitting to the item selected in a
	 * {@link Table}
	 *
	 * @param table
	 * @param uiObjectSubclass
	 * @param objectId
	 * @return
	 */
	protected <UIObjectSubclass extends UIObject> UIObjectSubclass getUIModelObjectFromTableByObjectId(
			final Table table, final Class<UIObjectSubclass> uiObjectSubclass,
			final Object objectId) {

		@SuppressWarnings("unchecked")
		final UIObjectSubclass beanItem = ((BeanItem<UIObjectSubclass>) table
				.getItem(objectId)).getBean();

		return beanItem;
	}

	/**
	 * Returns the {@link Property} as String fitting to the given
	 * {@link UIObject} subclass
	 *
	 * @param beanItem
	 * @param type
	 * @return
	 */
	protected Property<String> getStringItemProperty(
			final BeanItem<? extends UIObject> beanItem, final String type) {

		@SuppressWarnings("unchecked")
		final Property<String> itemProperty = beanItem.getItemProperty(type);

		return itemProperty;
	}

	/**
	 * Returns the {@link Property} as {@link LString} fitting to the given
	 * {@link UIObject} subclass
	 *
	 * @param beanItem
	 * @param type
	 * @return
	 */
	protected Property<LString> getLStringItemProperty(
			final BeanItem<? extends UIObject> beanItem, final String type) {

		@SuppressWarnings("unchecked")
		final Property<LString> itemProperty = beanItem.getItemProperty(type);

		return itemProperty;
	}

	/**
	 * Returns the {@link BeanItem} fitting to the item selected in a
	 * {@link Table}
	 *
	 * @param table
	 * @param uiObjectSubclass
	 * @param objectId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <UIObjectSubclass extends UIObject> BeanItem<UIObjectSubclass> getBeanItemFromTableByObjectId(
			final Table table, final Class<UIObjectSubclass> uiObjectSubclass,
			final Object objectId) {
		return (BeanItem<UIObjectSubclass>) table.getItem(objectId);
	}

	/**
	 * Adds a tab to the given {@link TabSheet} and allows to add an optional
	 * {@link ThemeResource} icon
	 *
	 * @param tabSheet
	 * @param tabComponent
	 * @param tabSheetCaption
	 * @param tabSheetIcon
	 * @return
	 */
	protected Tab addPointableTab(final TabSheet tabSheet,
			final AbstractCustomComponent tabComponent,
			final AdminMessageStrings tabSheetCaption, final String tabSheetIcon) {
		if (tabSheetIcon == null) {
			val tab = tabSheet.addTab(tabComponent,
					Messages.getAdminString(tabSheetCaption));
			tab.setStyleName("pointable");
			return tab;
		} else {
			val tab = tabSheet.addTab(tabComponent,
					Messages.getAdminString(tabSheetCaption),
					new ThemeResource(tabSheetIcon));
			tab.setStyleName("pointable");
			return tab;
		}
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

	/**
	 * Removes and adds a {@link ModelObject} from a {@link BeanContainer} to
	 * update the content
	 *
	 * @param answersBeanContainer
	 * @param slide
	 */
	@SuppressWarnings("unchecked")
	protected <SubClassOfUIModelObject extends UIModelObject> void removeAndAddModelObjectToBeanContainer(

			final BeanContainer<ObjectId, SubClassOfUIModelObject> beanContainer,
			final ModelObject modelObject) {
		beanContainer.removeItem(modelObject.getId());
		beanContainer.addItem(modelObject.getId(),
				(SubClassOfUIModelObject) modelObject.toUIModelObject());
	}
}
