package org.isgf.mhc.ui.views.components.interventions;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.server.MonitoringMessage;
import org.isgf.mhc.model.server.MonitoringMessageGroup;
import org.isgf.mhc.model.ui.UIModelObject;
import org.isgf.mhc.model.ui.UIMonitoringMessage;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Provides the monitoring message group edit component with a controller
 * 
 * @author Andreas Filler
 */
/**
 * @author Andreas Filler
 * 
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringMessageGroupEditComponentWithController extends
		MonitoringMessageGroupEditComponent {

	private final MonitoringMessageGroup						monitoringMessageGroup;

	private UIMonitoringMessage									selectedUIMonitoringMessage	= null;

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
		monitoringMessageTable.setSortAscending(true);
		monitoringMessageTable.setSortEnabled(false);

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
						} else {
							selectedUIMonitoringMessage = getUIModelObjectFromTableByObjectId(
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
				createMessage();
			} else if (event.getButton() == getEditButton()) {
				editMessage();
			} else if (event.getButton() == getMoveUpButton()) {
				moveMessage(true);
			} else if (event.getButton() == getMoveDownButton()) {
				moveMessage(false);
			} else if (event.getButton() == getDeleteButton()) {
				deleteMessage();
			}
		}
	}

	public void createMessage() {
		log.debug("Create message");
		val newMonitoringMessage = getInterventionAdministrationManagerService()
				.monitoringMessageCreate(monitoringMessageGroup.getId());

		showModalModelObjectEditWindow(
				AdminMessageStrings.ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__CREATE_MONITORING_MESSAGE,
				newMonitoringMessage, new MonitoringMessageEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Update message
							getInterventionAdministrationManagerService()
									.monitoringMessageUpdate(
											newMonitoringMessage);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						beanContainer.addItem(newMonitoringMessage.getId(),
								UIMonitoringMessage.class
										.cast(newMonitoringMessage
												.toUIModelObject()));
						getMonitoringMessageTable().select(
								newMonitoringMessage.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_CREATED);

						closeWindow();
					}
				}, new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Delete message
							getInterventionAdministrationManagerService()
									.monitoringMessageDelete(
											newMonitoringMessage);
						} catch (final Exception e) {
							handleException(e);
						}

						closeWindow();
					}
				});
	}

	public void editMessage() {
		log.debug("Edit message");
		val selectedMonitoringMessage = selectedUIMonitoringMessage
				.getRelatedModelObject(MonitoringMessage.class);

		showModalModelObjectEditWindow(
				AdminMessageStrings.ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__EDIT_MONITORING_MESSAGE,
				selectedMonitoringMessage,
				new MonitoringMessageEditComponentWithController(
						selectedMonitoringMessage),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Update message
							getInterventionAdministrationManagerService()
									.monitoringMessageUpdate(
											selectedMonitoringMessage);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						removeAndAdd(beanContainer, selectedMonitoringMessage);
						getMonitoringMessageTable().sort();
						getMonitoringMessageTable().select(
								selectedMonitoringMessage.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_UPDATED);

						closeWindow();
					}
				}, new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Restore prior message
						selectedUIMonitoringMessage
								.setRelatedModelObject(getInterventionAdministrationManagerService()
										.getMonitoringMessage(
												selectedMonitoringMessage
														.getId()));

						closeWindow();
					}
				});
	}

	/**
	 * Removes and adds a {@link MonitoringMessage} from a {@link BeanContainer}
	 * to update the content
	 * 
	 * @param beanContainer
	 * @param monitoringMessage
	 */
	@SuppressWarnings("unchecked")
	protected <SubClassOfUIModelObject extends UIModelObject> void removeAndAdd(

	final BeanContainer<ObjectId, SubClassOfUIModelObject> beanContainer,
			final MonitoringMessage monitoringMessage) {
		beanContainer.removeItem(monitoringMessage.getId());
		beanContainer.addItem(monitoringMessage.getId(),
				(SubClassOfUIModelObject) monitoringMessage.toUIModelObject());
	}

	public void moveMessage(final boolean moveUp) {
		log.debug("Move message {}", moveUp ? "up" : "down");

		val selectedMonitoringMessage = selectedUIMonitoringMessage
				.getRelatedModelObject(MonitoringMessage.class);
		val swappedMonitoringMessage = getInterventionAdministrationManagerService()
				.monitoringMessageMove(selectedMonitoringMessage, moveUp);

		if (swappedMonitoringMessage == null) {
			log.debug("Message is already at top/end of list");
			return;
		}

		removeAndAdd(beanContainer, swappedMonitoringMessage);
		removeAndAdd(beanContainer, selectedMonitoringMessage);
		getMonitoringMessageTable().sort();
		getMonitoringMessageTable().select(selectedMonitoringMessage.getId());
	}

	public void deleteMessage() {
		log.debug("Delete message");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedMonitoringMessage = selectedUIMonitoringMessage.getRelatedModelObject(MonitoringMessage.class);

					// Delete variable
					getInterventionAdministrationManagerService()
							.monitoringMessageDelete(selectedMonitoringMessage);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getMonitoringMessageTable().removeItem(
						selectedUIMonitoringMessage.getRelatedModelObject(
								MonitoringMessage.class).getId());
				getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_DELETED);

				closeWindow();
			}
		}, null);
	}
}
