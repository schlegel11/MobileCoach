package org.isgf.mhc.ui.views.components.interventions;

import java.text.DateFormat;
import java.util.Locale;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.persistent.DialogMessage;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.types.DialogMessageStatusTypes;
import org.isgf.mhc.model.ui.UIDialogMessageProblemViewWithParticipant;
import org.isgf.mhc.tools.StringValidator;
import org.isgf.mhc.ui.views.components.basics.PlaceholderStringEditComponent;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

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

	public InterventionProblemsComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// table options
		val dialogMessagesTable = getDialogMessagesTable();
		dialogMessagesTable.setSelectable(true);
		dialogMessagesTable.setImmediate(true);

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
							participant.getOrganization().equals("") ? Messages
									.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
									: participant.getOrganization(),
							participant.getOrganizationUnit().equals("") ? Messages
									.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
									: participant.getOrganizationUnit());

			beanContainer.addItem(dialogMessage.getId(), uiDialogMessage);
		}

		dialogMessagesTable.sort();
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
						MHC.getInstance()
								.getInterventionExecutionManagerService()
								.dialogMessageSetProblemSolved(
										dialogMessage.getId(), null);

					} catch (final Exception e) {
						handleException(e);

						// Adapt UI
						beanContainer.removeItem(dialogMessage.getId());

						closeWindow();

						return;
					}

					// Adapt UI
					beanContainer.removeItem(dialogMessage.getId());

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
								MHC.getInstance()
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
							val interventionExecutionManagerService = MHC
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
