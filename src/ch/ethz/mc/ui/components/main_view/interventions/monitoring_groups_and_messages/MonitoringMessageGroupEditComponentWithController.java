package ch.ethz.mc.ui.components.main_view.interventions.monitoring_groups_and_messages;

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
import java.io.File;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.MonitoringMessage;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.ui.UIMonitoringMessage;
import ch.ethz.mc.ui.components.basics.ShortStringEditComponent;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

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
public class MonitoringMessageGroupEditComponentWithController
		extends MonitoringMessageGroupEditComponent {

	@Getter
	@Setter
	private MonitoringMessageGroup								monitoringMessageGroup;

	private final ObjectId										interventionId;

	private UIMonitoringMessage									selectedUIMonitoringMessage	= null;

	private final BeanContainer<ObjectId, UIMonitoringMessage>	beanContainer;

	protected MonitoringMessageGroupEditComponentWithController(
			final MonitoringMessageGroup monitoringMessageGroupToSet,
			final ObjectId interventionId) {
		super();

		monitoringMessageGroup = monitoringMessageGroupToSet;
		this.interventionId = interventionId;

		// table options
		val monitoringMessageTable = getMonitoringMessageTable();

		// table content
		val messagesOfMessageGroup = getInterventionAdministrationManagerService()
				.getAllMonitoringMessagesOfMonitoringMessageGroup(
						monitoringMessageGroup.getId());

		beanContainer = createBeanContainerForModelObjects(
				UIMonitoringMessage.class, messagesOfMessageGroup);

		monitoringMessageTable.setContainerDataSource(beanContainer);
		monitoringMessageTable.setSortContainerPropertyId(
				UIMonitoringMessage.getSortColumn());
		monitoringMessageTable
				.setVisibleColumns(UIMonitoringMessage.getVisibleColumns());
		monitoringMessageTable
				.setColumnHeaders(UIMonitoringMessage.getColumnHeaders());
		monitoringMessageTable.setSortAscending(true);
		monitoringMessageTable.setSortEnabled(false);

		// check boxes
		getMessagesExpectAnswerCheckBox()
				.setValue(monitoringMessageGroup.isMessagesExpectAnswer());
		getRandomOrderCheckBox()
				.setValue(monitoringMessageGroup.isSendInRandomOrder());
		getSendSamePositionIfSendingAsReplyCheckBox().setValue(
				monitoringMessageGroup.isSendSamePositionIfSendingAsReply());

		// validation expression
		getValidationExpressionTextFieldComponent()
				.setValue(monitoringMessageGroup.getValidationExpression());

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
		getMessagesExpectAnswerCheckBox()
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						getInterventionAdministrationManagerService()
								.monitoringMessageGroupSetMessagesExceptAnswer(
										monitoringMessageGroup,
										getMessagesExpectAnswerCheckBox()
												.getValue());
					}
				});
		getRandomOrderCheckBox()
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						getInterventionAdministrationManagerService()
								.monitoringMessageGroupSetRandomSendOrder(
										monitoringMessageGroup,
										getRandomOrderCheckBox().getValue());

						if (monitoringMessageGroup.isSendInRandomOrder()
								&& monitoringMessageGroup
										.isSendSamePositionIfSendingAsReply()) {
							getSendSamePositionIfSendingAsReplyCheckBox()
									.setValue(false);
						}
					}
				});
		getSendSamePositionIfSendingAsReplyCheckBox()
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						getInterventionAdministrationManagerService()
								.monitoringMessageGroupSetSendSamePositionIfSendingAsReply(
										monitoringMessageGroup,
										getSendSamePositionIfSendingAsReplyCheckBox()
												.getValue());

						if (monitoringMessageGroup.isSendInRandomOrder()
								&& monitoringMessageGroup
										.isSendSamePositionIfSendingAsReply()) {
							getRandomOrderCheckBox().setValue(false);
						}
					}
				});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewButton().addClickListener(buttonClickListener);
		getEditButton().addClickListener(buttonClickListener);
		getDuplicateButton().addClickListener(buttonClickListener);
		getMoveUpButton().addClickListener(buttonClickListener);
		getMoveDownButton().addClickListener(buttonClickListener);
		getDeleteButton().addClickListener(buttonClickListener);
		getValidationExpressionTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getNewButton()) {
				createMessage();
			} else if (event.getButton() == getEditButton()) {
				editMessage();
			} else if (event.getButton() == getDuplicateButton()) {
				duplicateMessage();
			} else if (event.getButton() == getMoveUpButton()) {
				moveMessage(true);
			} else if (event.getButton() == getMoveDownButton()) {
				moveMessage(false);
			} else if (event.getButton() == getDeleteButton()) {
				deleteMessage();
			} else if (event
					.getButton() == getValidationExpressionTextFieldComponent()
							.getButton()) {
				editValidationExpression();
			}
			event.getButton().setEnabled(true);
		}
	}

	public void createMessage() {
		log.debug("Create message");
		val newMonitoringMessage = getInterventionAdministrationManagerService()
				.monitoringMessageCreate(monitoringMessageGroup.getId());

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_MONITORING_MESSAGE,
				new MonitoringMessageEditComponentWithController(
						newMonitoringMessage, interventionId),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						beanContainer.addItem(newMonitoringMessage.getId(),
								UIMonitoringMessage.class
										.cast(newMonitoringMessage
												.toUIModelObject()));
						getMonitoringMessageTable()
								.select(newMonitoringMessage.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_CREATED);

						closeWindow();
					}
				});
	}

	public void editMessage() {
		log.debug("Edit message");
		val selectedMonitoringMessage = selectedUIMonitoringMessage
				.getRelatedModelObject(MonitoringMessage.class);

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_MONITORING_MESSAGE,
				new MonitoringMessageEditComponentWithController(
						selectedMonitoringMessage, interventionId),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						removeAndAddModelObjectToBeanContainer(beanContainer,
								selectedMonitoringMessage);
						getMonitoringMessageTable().sort();
						getMonitoringMessageTable()
								.select(selectedMonitoringMessage.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_UPDATED);

						closeWindow();
					}
				});
	}

	public void duplicateMessage() {
		log.debug("Duplicate message");

		final File temporaryBackupFile = getInterventionAdministrationManagerService()
				.monitoringMessageExport(selectedUIMonitoringMessage
						.getRelatedModelObject(MonitoringMessage.class));

		try {
			final MonitoringMessage importedMonitoringMessage = getInterventionAdministrationManagerService()
					.monitoringMessageImport(temporaryBackupFile, true);

			if (importedMonitoringMessage == null) {
				throw new Exception("Imported message not found in import");
			}

			// Adapt UI
			beanContainer.addItem(importedMonitoringMessage.getId(),
					UIMonitoringMessage.class
							.cast(importedMonitoringMessage.toUIModelObject()));
			getMonitoringMessageTable()
					.select(importedMonitoringMessage.getId());

			getAdminUI().showInformationNotification(
					AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_DUPLICATED);
		} catch (final Exception e) {
			getAdminUI().showWarningNotification(
					AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_DUPLICATION_FAILED);
		}

		try {
			temporaryBackupFile.delete();
		} catch (final Exception f) {
			// Do nothing
		}
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

		removeAndAddModelObjectToBeanContainer(beanContainer,
				swappedMonitoringMessage);
		removeAndAddModelObjectToBeanContainer(beanContainer,
				selectedMonitoringMessage);
		getMonitoringMessageTable().sort();
		getMonitoringMessageTable().select(selectedMonitoringMessage.getId());
	}

	public void deleteMessage() {
		log.debug("Delete message");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedMonitoringMessage = selectedUIMonitoringMessage
							.getRelatedModelObject(MonitoringMessage.class);

					// Delete variable
					getInterventionAdministrationManagerService()
							.monitoringMessageDelete(selectedMonitoringMessage);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getMonitoringMessageTable()
						.removeItem(selectedUIMonitoringMessage
								.getRelatedModelObject(MonitoringMessage.class)
								.getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_DELETED);

				closeWindow();
			}
		}, null);
	}

	public void editValidationExpression() {
		log.debug("Edit validation expression");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VALIDATION_EXPRESSION,
				monitoringMessageGroup.getValidationExpression(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						getInterventionAdministrationManagerService()
								.monitoringMessageGroupSetValidationExpression(
										monitoringMessageGroup,
										getStringValue());

						getValidationExpressionTextFieldComponent()
								.setValue(monitoringMessageGroup
										.getValidationExpression());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__VALIDATION_EXPRESSION_UPDATED);

						closeWindow();
					}
				}, null);
	}
}
