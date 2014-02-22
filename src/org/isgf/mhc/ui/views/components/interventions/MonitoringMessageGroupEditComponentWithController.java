package org.isgf.mhc.ui.views.components.interventions;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.server.MonitoringMessageGroup;
import org.isgf.mhc.model.ui.UIMonitoringMessage;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Provides the monitoring message group edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringMessageGroupEditComponentWithController extends
		MonitoringMessageGroupEditComponent {

	private final MonitoringMessageGroup						monitoringMessageGroup;

	private UIMonitoringMessage									selectedUIMonitoringMessage			= null;
	private BeanItem<UIMonitoringMessage>						selectedUIMonitoringMessageBeanItem	= null;

	private final BeanContainer<ObjectId, UIMonitoringMessage>	beanContainer;

	protected MonitoringMessageGroupEditComponentWithController(
			final MonitoringMessageGroup monitoringMessageGroup) {
		super();

		this.monitoringMessageGroup = monitoringMessageGroup;

		// table options
		val monitoringMessageTable = getMonitoringMessageTable();

		// table content
		val messagesOfMessageGroup = getInterventionAdministrationManagerService()
				.getAllMonitoringMessagesOfMonitoringMessageGroup(
						monitoringMessageGroup.getId());

		beanContainer = createBeanContainerForModelObjects(
				UIMonitoringMessage.class, messagesOfMessageGroup);

		monitoringMessageTable.setContainerDataSource(beanContainer);
		monitoringMessageTable.setSortContainerPropertyId(UIMonitoringMessage
				.getSortColumn());
		monitoringMessageTable.setVisibleColumns(UIMonitoringMessage
				.getVisibleColumns());
		monitoringMessageTable.setColumnHeaders(UIMonitoringMessage
				.getColumnHeaders());

		// check box
		getRandomOrderCheckBox().setValue(
				monitoringMessageGroup.isSendInRandomOrder());

		// handle table selection change
		monitoringMessageTable
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						val objectId = monitoringMessageTable.getValue();
						if (objectId == null) {
							setNothingSelected();
							selectedUIMonitoringMessage = null;
							selectedUIMonitoringMessageBeanItem = null;
						} else {
							selectedUIMonitoringMessage = getUIModelObjectFromTableByObjectId(
									monitoringMessageTable,
									UIMonitoringMessage.class, objectId);
							selectedUIMonitoringMessageBeanItem = getBeanItemFromTableByObjectId(
									monitoringMessageTable,
									UIMonitoringMessage.class, objectId);
							setSomethingSelected();
						}
					}
				});

		// handle check box change
		getRandomOrderCheckBox().addValueChangeListener(
				new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						getInterventionAdministrationManagerService()
								.monitoringMessageGroupSetSentOrder(
										monitoringMessageGroup,
										getRandomOrderCheckBox().getValue());
					}
				});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewButton().addClickListener(buttonClickListener);
		getEditButton().addClickListener(buttonClickListener);
		getMoveUpButton().addClickListener(buttonClickListener);
		getMoveDownButton().addClickListener(buttonClickListener);
		getDeleteButton().addClickListener(buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getNewButton()) {
				// createMessage();
			} else if (event.getButton() == getEditButton()) {
				// editMessage();
			} else if (event.getButton() == getMoveUpButton()) {
				// moveUpMessage();
			} else if (event.getButton() == getMoveDownButton()) {
				// moveDownMessage();
			} else if (event.getButton() == getDeleteButton()) {
				// deleteMessage();
			}
		}
	}

	// TODO all controller methods
	// public void createMessage() {
	// log.debug("Create variable");
	// showModalStringValueEditWindow(
	// AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_VARIABLE,
	// null, null, new ShortStringEditComponent(),
	// new ExtendableButtonClickListener() {
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// InterventionVariableWithValue newVariable;
	// try {
	// // Create new variable
	// newVariable = getInterventionAdministrationManagerService()
	// .interventionVariableWithValueCreate(
	// getStringValue(),
	// intervention.getId());
	// } catch (final Exception e) {
	// handleException(e);
	// return;
	// }
	//
	// // Adapt UI
	// beanContainer.addItem(newVariable.getId(),
	// UIVariable.class.cast(newVariable
	// .toUIModelObject()));
	// getInterventionVariablesEditComponent()
	// .getVariablesTable()
	// .select(newVariable.getId());
	// getAdminUI()
	// .showInformationNotification(
	// AdminMessageStrings.NOTIFICATION__VARIABLE_CREATED);
	//
	// closeWindow();
	// }
	// }, null);
	// }
	//
	// public void editMessage() {
	// log.debug("Rename variable");
	//
	// showModalStringValueEditWindow(
	// AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_VARIABLE,
	// selectedUIMonitoringMessage.getRelatedModelObject(
	// InterventionVariableWithValue.class).getName(), null,
	// new ShortStringEditComponent(),
	// new ExtendableButtonClickListener() {
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// try {
	// val selectedVariable = selectedUIMonitoringMessage
	// .getRelatedModelObject(InterventionVariableWithValue.class);
	//
	// // Change name
	// getInterventionAdministrationManagerService()
	// .interventionVariableWithValueChangeName(
	// selectedVariable, getStringValue());
	// } catch (final Exception e) {
	// handleException(e);
	// return;
	// }
	//
	// // Adapt UI
	// getStringItemProperty(
	// selectedUIMonitoringMessageBeanItem,
	// UIVariable.NAME)
	// .setValue(
	// selectedUIMonitoringMessage
	// .getRelatedModelObject(
	// InterventionVariableWithValue.class)
	// .getName());
	//
	// getAdminUI()
	// .showInformationNotification(
	// AdminMessageStrings.NOTIFICATION__VARIABLE_RENAMED);
	// closeWindow();
	// }
	// }, null);
	// }
	//
	// public void moveUpMessage() {
	// log.debug("Edit variable value");
	//
	// showModalStringValueEditWindow(
	// AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_VALUE_FOR_VARIABLE,
	// selectedUIMonitoringMessage.getRelatedModelObject(
	// InterventionVariableWithValue.class).getValue(), null,
	// new ShortStringEditComponent(),
	// new ExtendableButtonClickListener() {
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// try {
	// val selectedVariable = selectedUIMonitoringMessage
	// .getRelatedModelObject(InterventionVariableWithValue.class);
	//
	// // Change name
	// getInterventionAdministrationManagerService()
	// .interventionVariableWithValueChangeValue(
	// selectedVariable, getStringValue());
	// } catch (final Exception e) {
	// handleException(e);
	// return;
	// }
	//
	// // Adapt UI
	// getStringItemProperty(
	// selectedUIMonitoringMessageBeanItem,
	// UIVariable.VALUE)
	// .setValue(
	// selectedUIMonitoringMessage
	// .getRelatedModelObject(
	// InterventionVariableWithValue.class)
	// .getValue());
	//
	// getAdminUI()
	// .showInformationNotification(
	// AdminMessageStrings.NOTIFICATION__VARIABLE_VALUE_CHANGED);
	// closeWindow();
	// }
	// }, null);
	// }
	//
	// public void deleteMessage() {
	// log.debug("Delete variable");
	// showConfirmationWindow(new ExtendableButtonClickListener() {
	//
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// try {
	// val selectedVariable =
	// selectedUIMonitoringMessage.getRelatedModelObject(InterventionVariableWithValue.class);
	//
	// // Delete variable
	// getInterventionAdministrationManagerService()
	// .interventionVariableWithValueDelete(
	// selectedVariable);
	// } catch (final Exception e) {
	// closeWindow();
	// handleException(e);
	// return;
	// }
	//
	// // Adapt UI
	// getInterventionVariablesEditComponent()
	// .getVariablesTable()
	// .removeItem(
	// selectedUIMonitoringMessage
	// .getRelatedModelObject(
	// InterventionVariableWithValue.class)
	// .getId());
	// getAdminUI().showInformationNotification(
	// AdminMessageStrings.NOTIFICATION__VARIABLE_DELETED);
	//
	// closeWindow();
	// }
	// }, null);
	// }
}
