package ch.ethz.mc.ui.views.components.interventions;

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
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.memory.DataTable;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.ui.UIParticipant;
import ch.ethz.mc.model.ui.results.UIDialogMessageWithParticipantForResults;
import ch.ethz.mc.model.ui.results.UIVariableWithParticipantForResults;
import ch.ethz.mc.tools.CSVExporter;
import ch.ethz.mc.tools.OnDemandFileDownloader;
import ch.ethz.mc.tools.OnDemandFileDownloader.OnDemandStreamResource;
import ch.ethz.mc.tools.StringValidator;
import ch.ethz.mc.ui.views.components.basics.PlaceholderStringEditWithMessageGroupSelectionComponent;
import ch.ethz.mc.ui.views.components.basics.ShortStringEditComponent;
import ch.ethz.mc.ui.views.helper.CaseInsensitiveItemSorter;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.converter.StringToDateConverter;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the intervention results component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionResultsComponentWithController extends
		InterventionResultsComponent {

	private final int																maximumItemsForInstantUIUpdate		= 250;

	private final Intervention														intervention;

	private Collection<ObjectId>													selectedUIParticipantsIds;
	private UIVariableWithParticipantForResults										selectedUIVariableWithParticipant	= null;

	private final BeanContainer<ObjectId, UIParticipant>							beanContainer;

	private final BeanContainer<Integer, UIVariableWithParticipantForResults>		variablesBeanContainer;
	private final BeanContainer<Integer, UIDialogMessageWithParticipantForResults>	messageDialogBeanContainer;

	public InterventionResultsComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// table options
		val participantsTable = getParticipantsTable();
		participantsTable.setSelectable(true);
		participantsTable.setMultiSelect(true);
		participantsTable.setMultiSelectMode(MultiSelectMode.DEFAULT);
		participantsTable.setImmediate(true);

		val variablesTable = getVariablesTable();
		variablesTable.setImmediate(true);
		variablesTable.setSelectable(true);

		val messageDialogTable = getMessageDialogTable();
		messageDialogTable.setImmediate(true);

		// table content
		beanContainer = createBeanContainerForModelObjects(UIParticipant.class,
				null);

		participantsTable.setContainerDataSource(beanContainer);
		participantsTable.setSortContainerPropertyId(UIParticipant
				.getSortColumn());
		participantsTable.setVisibleColumns(UIParticipant.getVisibleColumns());
		participantsTable.setColumnHeaders(UIParticipant.getColumnHeaders());
		participantsTable.setConverter(UIParticipant.CREATED,
				new StringToDateConverter() {
					@Override
					protected DateFormat getFormat(final Locale locale) {
						val dateFormat = DateFormat.getDateTimeInstance(
								DateFormat.MEDIUM, DateFormat.MEDIUM,
								Constants.getAdminLocale());
						return dateFormat;
					}
				});

		variablesBeanContainer = new BeanContainer<Integer, UIVariableWithParticipantForResults>(
				UIVariableWithParticipantForResults.class);
		variablesBeanContainer.setItemSorter(new CaseInsensitiveItemSorter());

		variablesTable.setContainerDataSource(variablesBeanContainer);
		variablesTable
				.setSortContainerPropertyId(UIVariableWithParticipantForResults
						.getSortColumn());
		variablesTable.setVisibleColumns(UIVariableWithParticipantForResults
				.getVisibleColumns());
		variablesTable.setColumnHeaders(UIVariableWithParticipantForResults
				.getColumnHeaders());

		messageDialogBeanContainer = new BeanContainer<Integer, UIDialogMessageWithParticipantForResults>(
				UIDialogMessageWithParticipantForResults.class);
		messageDialogBeanContainer
				.setItemSorter(new CaseInsensitiveItemSorter());

		messageDialogTable.setContainerDataSource(messageDialogBeanContainer);
		messageDialogTable
				.setSortContainerPropertyId(UIDialogMessageWithParticipantForResults
						.getSortColumn());
		messageDialogTable
				.setVisibleColumns(UIDialogMessageWithParticipantForResults
						.getVisibleColumns());
		messageDialogTable
				.setColumnHeaders(UIDialogMessageWithParticipantForResults
						.getColumnHeaders());

		// handle selection change
		participantsTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				adjust(false);
			}
		});

		variablesTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = variablesTable.getValue();
				if (objectId == null) {
					selectedUIVariableWithParticipant = null;
					setVariableWithParticipantSelected(false);
				} else {
					selectedUIVariableWithParticipant = getUIModelObjectFromTableByObjectId(
							variablesTable,
							UIVariableWithParticipantForResults.class, objectId);
					setVariableWithParticipantSelected(true);
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getRefreshButton().addClickListener(buttonClickListener);
		getSendMessageButton().addClickListener(buttonClickListener);
		getEditButton().addClickListener(buttonClickListener);

		// Special handle for export buttons
		val allDataExportOnDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					public InputStream getStream() {
						final DataTable dataTable = new DataTable();

						int i = 0;
						for (val participantId : selectedUIParticipantsIds) {
							val participant = getInterventionAdministrationManagerService()
									.getParticipant(participantId);

							val variablesWithValuesOfParticipant = getInterventionAdministrationManagerService()
									.getAllVariablesWithValuesOfParticipantAndSystem(
											participantId);
							val statisticValuesOfParticipant = getInterventionAdministrationManagerService()
									.getAllStatisticValuesOfParticipant(
											participantId);
							dataTable.addEntry(participantId, participant,
									statisticValuesOfParticipant,
									variablesWithValuesOfParticipant);

							i++;
							if (i % 100 == 0) {
								log.info("Exporting entry {} of {}", i,
										selectedUIParticipantsIds.size());
							}
						}

						try {
							log.info("Converting table to CSV...");
							return CSVExporter.convertDataTableToCSV(dataTable);
						} catch (final IOException e) {
							log.error("Error at creating CSV: {}",
									e.getMessage());
						} finally {
							getExportDataButton().setEnabled(true);
						}

						return null;
					}

					@Override
					public String getFilename() {
						return "Intervention_"
								+ intervention.getName().replaceAll(
										"[^A-Za-z0-9_. ]+", "_")
								+ "_Participant_All_Data.csv";
					}
				});
		allDataExportOnDemandFileDownloader.extend(getExportDataButton());
		getExportDataButton().setDisableOnClick(true);

		val variablesExportOnDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					public InputStream getStream() {
						final List<UIVariableWithParticipantForResults> items = new ArrayList<UIVariableWithParticipantForResults>();

						for (val itemId : variablesBeanContainer.getItemIds()) {
							items.add(variablesBeanContainer.getItem(itemId)
									.getBean());
						}

						try {
							return CSVExporter
									.convertUIParticipantVariableForResultsToCSV(items);
						} catch (final IOException e) {
							log.error("Error at creating CSV: {}",
									e.getMessage());
						} finally {
							getVariablesExportButton().setEnabled(true);
						}

						return null;
					}

					@Override
					public String getFilename() {
						return "Intervention_"
								+ intervention.getName().replaceAll(
										"[^A-Za-z0-9_. ]+", "_")
								+ "_Participant_Variable_Results.csv";
					}
				});
		variablesExportOnDemandFileDownloader
				.extend(getVariablesExportButton());
		getVariablesExportButton().setDisableOnClick(true);

		val messageDialogExportOnDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					public InputStream getStream() {
						final List<UIDialogMessageWithParticipantForResults> items = new ArrayList<UIDialogMessageWithParticipantForResults>();

						for (val itemId : messageDialogBeanContainer
								.getItemIds()) {
							items.add(messageDialogBeanContainer
									.getItem(itemId).getBean());
						}

						try {
							return CSVExporter
									.convertUIDialogMessageForResultsToCSV(items);
						} catch (final IOException e) {
							log.error("Error at creating CSV: {}",
									e.getMessage());
						} finally {
							getMessageDialogExportButton().setEnabled(true);
						}

						return null;
					}

					@Override
					public String getFilename() {
						return "Intervention_"
								+ intervention.getName().replaceAll(
										"[^A-Za-z0-9_. ]+", "_")
								+ "_Participant_Message_Dialog_Results.csv";
					}
				});
		messageDialogExportOnDemandFileDownloader
				.extend(getMessageDialogExportButton());
		getMessageDialogExportButton().setDisableOnClick(true);

		adjust(true);
	}

	@SuppressWarnings("unchecked")
	private void adjust(final boolean refreshAlsoParticipants) {
		val participantsTable = getParticipantsTable();

		if (refreshAlsoParticipants) {
			log.debug("Update participants");
			refreshBeanContainer(
					beanContainer,
					UIParticipant.class,
					getInterventionAdministrationManagerService()
							.getAllParticipantsOfIntervention(
									intervention.getId()));

			participantsTable.sort();
		}

		log.debug("Update tables");
		selectedUIParticipantsIds = (Collection<ObjectId>) participantsTable
				.getValue();

		if (selectedUIParticipantsIds == null) {
			updateButtonStatus(null, false, intervention.isMonitoringActive());

			updateTables(true);
		} else if (selectedUIParticipantsIds.size() > maximumItemsForInstantUIUpdate) {
			getAdminUI()
					.showWarningNotification(
							AdminMessageStrings.NOTIFICATION__TOO_MANY_PARTICIPANTS_SELECTED,
							maximumItemsForInstantUIUpdate);

			updateButtonStatus(selectedUIParticipantsIds, false,
					intervention.isMonitoringActive());

			updateTables(true);
		} else {
			updateButtonStatus(selectedUIParticipantsIds, true,
					intervention.isMonitoringActive());

			updateTables(false);
		}
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {

			if (event.getButton() == getRefreshButton()) {
				adjust(true);
			} else if (event.getButton() == getSendMessageButton()) {
				sendMessage();
			} else if (event.getButton() == getEditButton()) {
				editVariableValue();
			}
		}
	}

	private void updateTables(final boolean clearOnly) {
		log.debug("Update variables of participant");

		getVariablesTable().select(null);

		variablesBeanContainer.removeAllItems();

		int i = 0;
		if (!clearOnly) {
			for (val participantId : selectedUIParticipantsIds) {
				val participant = getInterventionAdministrationManagerService()
						.getParticipant(participantId);

				val variablesOfParticipant = getInterventionAdministrationManagerService()
						.getAllVariablesWithValuesOfParticipantAndSystem(
								participantId);

				for (val variableOfParticipant : variablesOfParticipant
						.values()) {
					variablesBeanContainer
							.addItem(
									i++,
									variableOfParticipant
											.toUIVariableWithParticipantForResults(
													participant.getId()
															.toString(),
													participant.getNickname()
															.equals("") ? Messages
															.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
															: participant
																	.getNickname()));
				}
			}

			getVariablesTable().sort();
		}

		log.debug("Update message dialog of participant");
		messageDialogBeanContainer.removeAllItems();

		i = 0;
		if (!clearOnly) {
			for (val participantId : selectedUIParticipantsIds) {
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
								.getMonitoringMessage(
										relatedMonitoringMessageId);

						if (relatedMonitoringMessage != null
								&& relatedMonitoringMessage
										.getLinkedMediaObject() != null) {
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
											.toUIDialogMessageWithParticipantForResults(
													participant.getId()
															.toString(),
													participant.getNickname()
															.equals("") ? Messages
															.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
															: participant
																	.getNickname(),
													participant
															.getLanguage()
															.getDisplayLanguage(),
													participant.getGroup() == null ? Messages
															.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
															: participant
																	.getGroup(),
													participant
															.getOrganization()
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
	}

	public void sendMessage() {
		log.debug("Send manual message");
		val allPossibleMessageVariables = getInterventionAdministrationManagerService()
				.getAllPossibleMonitoringRuleVariablesOfIntervention(
						intervention.getId());
		val allPossibleMessageGroups = getInterventionAdministrationManagerService()
				.getAllMonitoringMessageGroupsOfIntervention(
						intervention.getId());

		val placeholderStringEditWithMessageGroupSelectionComponent = new PlaceholderStringEditWithMessageGroupSelectionComponent(
				allPossibleMessageGroups);

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__SEND_MESSAGE_TO_ALL_SELECTED_PARTICIPANTS,
				"", allPossibleMessageVariables,
				placeholderStringEditWithMessageGroupSelectionComponent,
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

							val selectedParticipants = convertSelectedToParticipantsList();

							// Distinguish between text or message group based
							// sending
							if (placeholderStringEditWithMessageGroupSelectionComponent
									.getSelectedMonitoringMessageGroup() != null) {
								for (val participant : selectedParticipants) {
									interventionExecutionManagerService
											.sendManualMessage(
													participant,
													placeholderStringEditWithMessageGroupSelectionComponent
															.getSendToSupervisorComboBox()
															.getValue(),
													placeholderStringEditWithMessageGroupSelectionComponent
															.getSelectedMonitoringMessageGroup(),
													placeholderStringEditWithMessageGroupSelectionComponent
															.getHoursUntilHandledAsNotAnsweredSlider()
															.getValue()
															.intValue());
								}
							} else {
								for (val participant : selectedParticipants) {
									interventionExecutionManagerService
											.sendManualMessage(
													participant,
													placeholderStringEditWithMessageGroupSelectionComponent
															.getSendToSupervisorComboBox()
															.getValue(),
													getStringValue());
								}
							}
						}

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__THE_MESSAGES_WILL_BE_SENT_IN_THE_NEXT_MINUTES);

						adjust(false);

						closeWindow();
					}
				}, null);
	}

	public void editVariableValue() {
		log.debug("Edit variable value");
		val abstractVariableWithValue = selectedUIVariableWithParticipant
				.getRelatedModelObject(AbstractVariableWithValue.class);

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_VALUE_FOR_VARIABLE,
				abstractVariableWithValue.getValue(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Change value
						val changeSuceeded = MC
								.getInstance()
								.getInterventionExecutionManagerService()
								.participantAdjustVariableValue(
										new ObjectId(
												selectedUIVariableWithParticipant
														.getParticipantId()),
										abstractVariableWithValue.getName(),
										getStringValue());

						if (changeSuceeded) {
							adjust(false);

							getAdminUI()
									.showInformationNotification(
											AdminMessageStrings.NOTIFICATION__VARIABLE_VALUE_CHANGED);
						} else {
							getAdminUI()
									.showWarningNotification(
											AdminMessageStrings.NOTIFICATION__SYSTEM_RESERVED_VARIABLE);
						}
						closeWindow();
					}
				}, null);
	}

	protected List<Participant> convertSelectedToParticipantsList() {
		val selectedParticipants = new ArrayList<Participant>();

		for (val selectedUIParticipantId : selectedUIParticipantsIds) {
			val selectedUIParticipant = beanContainer.getItem(
					selectedUIParticipantId).getBean();

			selectedParticipants.add(selectedUIParticipant
					.getRelatedModelObject(Participant.class));
		}

		return selectedParticipants;
	}
}
