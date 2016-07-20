package ch.ethz.mc.ui.views.components.interventions;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import java.text.DateFormat;
import java.util.Locale;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.ui.UIDialogMessageProblemViewWithParticipant;
import ch.ethz.mc.model.ui.UIDialogMessageWithParticipant;
import ch.ethz.mc.tools.StringValidator;
import ch.ethz.mc.ui.views.components.basics.PlaceholderStringEditComponent;
import ch.ethz.mc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.converter.StringToDateConverter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the intervention problems component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionProblemsComponentWithController extends
		InterventionProblemsComponent {

	private final Intervention															intervention;

	private UIDialogMessageProblemViewWithParticipant									selectedUIDialogMessageProblemViewWithParticipant;

	private final BeanContainer<ObjectId, UIDialogMessageProblemViewWithParticipant>	beanContainer;
	private final BeanContainer<Integer, UIDialogMessageWithParticipant>				messageDialogBeanContainer;

	public InterventionProblemsComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// table options
		val dialogMessagesTable = getDialogMessagesTable();
		dialogMessagesTable.setSelectable(true);
		dialogMessagesTable.setImmediate(true);

		val messageDialogTable = getMessageDialogTable();
		messageDialogTable.setImmediate(true);

		// table content
		beanContainer = createBeanContainerForModelObjects(
				UIDialogMessageProblemViewWithParticipant.class, null);

		dialogMessagesTable.setContainerDataSource(beanContainer);
		dialogMessagesTable
				.setSortContainerPropertyId(UIDialogMessageProblemViewWithParticipant
						.getSortColumn());
		dialogMessagesTable
				.setVisibleColumns(UIDialogMessageProblemViewWithParticipant
						.getVisibleColumns());
		dialogMessagesTable
				.setColumnHeaders(UIDialogMessageProblemViewWithParticipant
						.getColumnHeaders());
		dialogMessagesTable.setConverter(
				UIDialogMessageProblemViewWithParticipant.SENT_TIMESTAMP,
				new StringToDateConverter() {
					@Override
					protected DateFormat getFormat(final Locale locale) {
						val dateFormat = DateFormat.getDateTimeInstance(
								DateFormat.MEDIUM, DateFormat.MEDIUM,
								Constants.getAdminLocale());
						return dateFormat;
					}
				});
		dialogMessagesTable
				.setConverter(
						UIDialogMessageProblemViewWithParticipant.ANSWER_RECEIVED_TIMESTAMP,
						new StringToDateConverter() {
							@Override
							protected DateFormat getFormat(final Locale locale) {
								val dateFormat = DateFormat
										.getDateTimeInstance(DateFormat.MEDIUM,
												DateFormat.MEDIUM,
												Constants.getAdminLocale());
								return dateFormat;
							}
						});

		messageDialogBeanContainer = new BeanContainer<Integer, UIDialogMessageWithParticipant>(
				UIDialogMessageWithParticipant.class);

		messageDialogTable.setContainerDataSource(messageDialogBeanContainer);
		messageDialogTable
				.setSortContainerPropertyId(UIDialogMessageWithParticipant
						.getSortColumn());
		messageDialogTable.setVisibleColumns(UIDialogMessageWithParticipant
				.getVisibleColumns());
		messageDialogTable.setColumnHeaders(UIDialogMessageWithParticipant
				.getColumnHeaders());

		// handle selection change
		dialogMessagesTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = dialogMessagesTable.getValue();
				if (objectId == null) {
					setNothingSelected();
					selectedUIDialogMessageProblemViewWithParticipant = null;
				} else {
					setSomethingSelected();
					selectedUIDialogMessageProblemViewWithParticipant = getUIModelObjectFromTableByObjectId(
							dialogMessagesTable,
							UIDialogMessageProblemViewWithParticipant.class,
							objectId);
				}

				updateTables();
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getSolveButton().addClickListener(buttonClickListener);
		getSendMessageButton().addClickListener(buttonClickListener);
		getRefreshButton().addClickListener(buttonClickListener);

		adjust();
	}

	private void adjust() {
		val dialogMessagesTable = getDialogMessagesTable();

		log.debug("Update dialog messages");

		beanContainer.removeAllItems();

		val dialogMessages = getInterventionAdministrationManagerService()
				.getAllDialogMessagesWhichAreNotAutomaticallyProcessableButAreNotProcessedOfIntervention(
						intervention.getId());

		for (val dialogMessage : dialogMessages) {
			val participant = getInterventionAdministrationManagerService()
					.getParticipant(dialogMessage.getParticipant());

			val uiDialogMessage = dialogMessage
					.toUIDialogMessageProblemViewWithParticipant(
							participant.getId().toString(),
							participant.getNickname().equals("") ? Messages
									.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
									: participant.getNickname(),
							participant.getLanguage().getDisplayLanguage(),
							participant.getGroup() == null ? Messages
									.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
											: participant.getGroup(),
							participant.getOrganization().equals("") ? Messages
									.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
									: participant.getOrganization(),
							participant.getOrganizationUnit().equals("") ? Messages
									.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
									: participant.getOrganizationUnit());

			beanContainer.addItem(dialogMessage.getId(), uiDialogMessage);
		}

		dialogMessagesTable.sort();

		updateTables();
	}

	private void updateTables() {
		log.debug("Update message dialog of participant");
		messageDialogBeanContainer.removeAllItems();

		ObjectId participantId = null;

		if (selectedUIDialogMessageProblemViewWithParticipant != null) {
			participantId = selectedUIDialogMessageProblemViewWithParticipant
					.getRelatedModelObject(DialogMessage.class)
					.getParticipant();
		}

		int i = 0;
		if (participantId != null) {
			val participant = getInterventionAdministrationManagerService()
					.getParticipant(participantId);

			val dialogMessagesOfParticipant = getInterventionAdministrationManagerService()
					.getAllDialogMessagesOfParticipant(participantId);

			for (val dialogMessageOfParticipant : dialogMessagesOfParticipant) {
				boolean containsMediaContentInMessage = false;
				val relatedMonitoringMessageId = dialogMessageOfParticipant
						.getRelatedMonitoringMessage();

				if (relatedMonitoringMessageId != null) {
					val relatedMonitoringMessage = getInterventionAdministrationManagerService()
							.getMonitoringMessage(relatedMonitoringMessageId);

					if (relatedMonitoringMessage != null
							&& relatedMonitoringMessage.getLinkedMediaObject() != null) {
						val linkedMediaObject = getInterventionAdministrationManagerService()
								.getMediaObject(
										relatedMonitoringMessage
												.getLinkedMediaObject());

						if (linkedMediaObject != null) {
							containsMediaContentInMessage = true;
						}
					}
				}

				messageDialogBeanContainer
						.addItem(
								i++,
								dialogMessageOfParticipant
										.toUIDialogMessageWithParticipant(
												participant.getId().toString(),
												participant.getNickname()
														.equals("") ? Messages
														.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
														: participant
																.getNickname(),
												participant.getLanguage()
														.getDisplayLanguage(),
												participant.getGroup() == null ? Messages
														.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
														: participant
																.getGroup(),
												participant.getOrganization()
														.equals("") ? Messages
														.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
														: participant
																.getOrganization(),
														participant
														.getOrganizationUnit()
														.equals("") ? Messages
																.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
																: participant
																.getOrganizationUnit(),
																containsMediaContentInMessage));
			}
		}

		getMessageDialogTable().sort();
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {

			if (event.getButton() == getRefreshButton()) {
				adjust();
			} else if (event.getButton() == getSolveButton()) {
				solveSelectedCase();
			} else if (event.getButton() == getSendMessageButton()) {
				sendMessage();
			}
		}
	}

	public void solveSelectedCase() {
		log.debug("Solve selected case");
		val dialogMessage = selectedUIDialogMessageProblemViewWithParticipant
				.getRelatedModelObject(DialogMessage.class);

		if (dialogMessage.getStatus() == DialogMessageStatusTypes.RECEIVED_UNEXPECTEDLY) {
			showConfirmationWindow(new ExtendableButtonClickListener() {
				@Override
				public void buttonClick(final ClickEvent event) {
					try {
						// Set case as solved
						MC.getInstance()
								.getInterventionExecutionManagerService()
								.dialogMessageSetProblemSolved(
										dialogMessage.getId(), null);

					} catch (final Exception e) {
						handleException(e);

						// Adapt UI
						beanContainer.removeItem(dialogMessage.getId());

						getDialogMessagesTable().select(null);

						closeWindow();

						return;
					}

					// Adapt UI
					beanContainer.removeItem(dialogMessage.getId());

					getDialogMessagesTable().select(null);

					getAdminUI().showInformationNotification(
							AdminMessageStrings.NOTIFICATION__CASE_SOLVED);
					closeWindow();
				}
			}, null);

		} else {
			showModalStringValueEditWindow(
					AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_CLEANED_ANSWER,
					dialogMessage.getAnswerReceived(), null,
					new ShortStringEditComponent(),
					new ExtendableButtonClickListener() {
						@Override
						public void buttonClick(final ClickEvent event) {
							try {
								// Set case as solved
								MC.getInstance()
										.getInterventionExecutionManagerService()
										.dialogMessageSetProblemSolved(
												dialogMessage.getId(),
												getStringValue());

							} catch (final Exception e) {
								handleException(e);

								// Adapt UI
								beanContainer.removeItem(dialogMessage.getId());

								closeWindow();

								return;
							}

							// Adapt UI
							beanContainer.removeItem(dialogMessage.getId());

							getAdminUI()
									.showInformationNotification(
											AdminMessageStrings.NOTIFICATION__CASE_SOLVED);
							closeWindow();
						}
					}, null);
		}
	}

	public void sendMessage() {
		log.debug("Send manual message");
		val allPossibleMessageVariables = getInterventionAdministrationManagerService()
				.getAllPossibleMonitoringRuleVariablesOfIntervention(
						intervention.getId());
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__SEND_MESSAGE_TO_SELECTED_PARTICIPANT,
				"", allPossibleMessageVariables,
				new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						// Check if message contains only valid strings
						if (!StringValidator.isValidVariableText(
								getStringValue(), allPossibleMessageVariables)) {

							getAdminUI()
									.showWarningNotification(
											AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);

							return;
						} else {
							val interventionExecutionManagerService = MC
									.getInstance()
									.getInterventionExecutionManagerService();

							val participantId = selectedUIDialogMessageProblemViewWithParticipant
									.getRelatedModelObject(DialogMessage.class)
									.getParticipant();

							val participant = getInterventionAdministrationManagerService()
									.getParticipant(participantId);

							interventionExecutionManagerService
									.sendManualMessage(participant,
											getStringValue());
						}

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__THE_MESSAGES_WILL_BE_SENT_IN_THE_NEXT_MINUTES);

						closeWindow();
					}
				}, null);
	}
}
