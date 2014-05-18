package org.isgf.mhc.modules.quiz;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.persistent.DialogMessage;
import org.isgf.mhc.model.persistent.MonitoringMessage;
import org.isgf.mhc.model.persistent.Participant;
import org.isgf.mhc.model.ui.UIDialogMessageReducedWithParticipant;
import org.isgf.mhc.model.ui.UIMonitoringMessageWithGroup;
import org.isgf.mhc.tools.StringValidator;
import org.isgf.mhc.ui.views.components.basics.PlaceholderStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.converter.StringToDateConverter;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the QuizModule with a controller
 * 
 * @author Andreas Filler
 * 
 */
@SuppressWarnings("serial")
@Log4j2
public class QuizModuleWithController extends QuizModule {

	private final ObjectId													interventionId;

	private BeanContainer<ObjectId, UIDialogMessageReducedWithParticipant>	beanContainer	= null;

	private Collection<ObjectId>											selectedDialogMessageIds;

	public QuizModuleWithController(final ObjectId interventionId) {
		super(interventionId);

		this.interventionId = interventionId;
	}

	@Override
	public void prepareToShow() {
		// Handle combo box
		val comboBox = getSelectQuestionMessageComboBox();
		comboBox.setNullSelectionAllowed(false);
		comboBox.setTextInputAllowed(false);
		comboBox.setImmediate(true);

		val monitoringMessageGroups = getInterventionAdministrationManagerService()
				.getAllMonitoringMessageGroupsOfIntervention(interventionId);
		for (val monitoringMessageGroup : monitoringMessageGroups) {
			val monitoringMessages = getInterventionAdministrationManagerService()
					.getAllMonitoringMessagesOfMonitoringMessageGroup(
							monitoringMessageGroup.getId());

			for (val monitoringMessage : monitoringMessages) {
				val groupName = monitoringMessageGroup.getName().equals("") ? ImplementationContants.DEFAULT_OBJECT_NAME
						: monitoringMessageGroup.getName();
				val messageText = monitoringMessage.getTextWithPlaceholders()
						.equals("") ? ImplementationContants.DEFAULT_OBJECT_NAME
						: monitoringMessage.getTextWithPlaceholders();

				val uiMonitoringMessageWithGroup = new UIMonitoringMessageWithGroup(
						groupName + ": " + messageText);

				uiMonitoringMessageWithGroup
						.setRelatedModelObject(monitoringMessage);

				comboBox.addItem(uiMonitoringMessageWithGroup);
			}
		}

		// Table options
		val dialogMessageTable = getRelevantDialogMessagesTable();
		dialogMessageTable.setSelectable(true);
		dialogMessageTable.setMultiSelect(true);
		dialogMessageTable.setMultiSelectMode(MultiSelectMode.DEFAULT);
		dialogMessageTable.setImmediate(true);

		// Init table
		beanContainer = createBeanContainerForModelObjects(
				UIDialogMessageReducedWithParticipant.class, null);
		dialogMessageTable.setContainerDataSource(beanContainer);
		dialogMessageTable
				.setSortContainerPropertyId(UIDialogMessageReducedWithParticipant
						.getSortColumn());
		dialogMessageTable
				.setVisibleColumns(UIDialogMessageReducedWithParticipant
						.getVisibleColumns());
		dialogMessageTable
				.setColumnHeaders(UIDialogMessageReducedWithParticipant
						.getColumnHeaders());
		dialogMessageTable
				.setConverter(
						UIDialogMessageReducedWithParticipant.ANSWER_RECEIVED_TIMESTAMP,
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
		dialogMessageTable.setConverter(
				UIDialogMessageReducedWithParticipant.SENT_TIMESTAMP,
				new StringToDateConverter() {
					@Override
					protected DateFormat getFormat(final Locale locale) {
						val dateFormat = DateFormat.getDateTimeInstance(
								DateFormat.MEDIUM, DateFormat.MEDIUM,
								Constants.getAdminLocale());
						return dateFormat;
					}
				});

		// handle button
		val buttonClickListener = new ButtonClickListener();
		getSendMessageToSelectedButton().addClickListener(buttonClickListener);

		// Listener for combo box change
		comboBox.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val selectedItem = event.getProperty().getValue();

				if (selectedItem == null) {
					fillTable(null);
				} else {
					fillTable(((UIMonitoringMessageWithGroup) selectedItem)
							.getRelatedModelObject(MonitoringMessage.class));
				}
			}
		});

		// Listener for table
		dialogMessageTable.addValueChangeListener(new ValueChangeListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(final ValueChangeEvent event) {
				selectedDialogMessageIds = (Collection<ObjectId>) dialogMessageTable
						.getValue();

				if (selectedDialogMessageIds == null
						|| selectedDialogMessageIds.size() == 0) {
					getSendMessageToSelectedButton().setEnabled(false);
				} else {
					getSendMessageToSelectedButton().setEnabled(true);
				}
			}
		});

		getSendMessageToSelectedButton().setEnabled(false);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {

			if (event.getButton() == getSendMessageToSelectedButton()) {
				sendMessage();
			}
		}
	}

	protected void fillTable(final MonitoringMessage relatedMonitoringMessage) {
		beanContainer.removeAllItems();

		if (relatedMonitoringMessage != null) {
			log.debug("Fill table with reduced dialog messages");

			val dialogMessages = getInterventionAdministrationManagerService()
					.getAllDialogMessagesOfRelatedMonitoringMessageSentWithinLast14Days(
							relatedMonitoringMessage.getId());

			for (val dialogMessage : dialogMessages) {
				val participant = getInterventionAdministrationManagerService()
						.getParticipant(dialogMessage.getParticipant());

				val uiDialogMessage = dialogMessage
						.toUIDialogMessageReducedWithParticipant(
								participant.getId().toString(),
								participant.getNickname().equals("") ? Messages
										.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
										: participant.getNickname());

				beanContainer.addItem(dialogMessage.getId(), uiDialogMessage);
			}
		}
	}

	public void sendMessage() {
		log.debug("Send manual message");
		val allPossibleMessageVariables = getInterventionAdministrationManagerService()
				.getAllPossibleMonitoringRuleVariablesOfIntervention(
						interventionId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__SEND_MESSAGE_TO_ALL_SELECTED_PARTICIPANTS,
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

							val selectedParticipants = convertSelectedToParticipantsList();

							for (val participant : selectedParticipants) {
								interventionExecutionManagerService
										.sendManualMessage(participant,
												getStringValue());
							}
						}

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__THE_MESSAGES_WILL_BE_SENT_IN_THE_NEXT_MINUTES);

						closeWindow();
					}
				}, null);
	}

	protected List<Participant> convertSelectedToParticipantsList() {
		val selectedParticipants = new ArrayList<Participant>();

		for (val selectedUIDialogMessage : selectedDialogMessageIds) {
			val selectedDialogMessage = beanContainer.getItem(
					selectedUIDialogMessage).getBean();

			val participantId = selectedDialogMessage.getRelatedModelObject(
					DialogMessage.class).getParticipant();

			val participant = getInterventionAdministrationManagerService()
					.getParticipant(participantId);

			selectedParticipants.add(participant);
		}

		return selectedParticipants;
	}
}
