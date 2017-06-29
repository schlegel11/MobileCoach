package ch.ethz.mc.ui.views.components.interventions;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.ui.UIParticipant;
import ch.ethz.mc.tools.OnDemandFileDownloader;
import ch.ethz.mc.tools.OnDemandFileDownloader.OnDemandStreamResource;
import ch.ethz.mc.tools.StringValidator;
import ch.ethz.mc.ui.views.components.basics.FileUploadComponentWithController;
import ch.ethz.mc.ui.views.components.basics.FileUploadComponentWithController.UploadListener;
import ch.ethz.mc.ui.views.components.basics.PlaceholderStringEditWithMessageGroupSelectionComponent;
import ch.ethz.mc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.converter.StringToDateConverter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the intervention participants tab component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionParticipantsTabComponentWithController extends
		InterventionParticipantsTabComponent {

	private final Intervention								intervention;
	private boolean											isOneScreeningSurveyActive;

	private Collection<ObjectId>							selectedUIParticipantsIds;

	private final BeanContainer<ObjectId, UIParticipant>	beanContainer;

	public InterventionParticipantsTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// table options
		val participantsEditComponent = getInterventionParticipantsEditComponent();
		val participantsTable = getInterventionParticipantsEditComponent()
				.getParticipantsTable();

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

		// handle selection change
		participantsTable.addValueChangeListener(new ValueChangeListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(final ValueChangeEvent event) {
				selectedUIParticipantsIds = (Collection<ObjectId>) participantsTable
						.getValue();
				getInterventionParticipantsEditComponent().updateButtonStatus(
						selectedUIParticipantsIds,
						intervention.isMonitoringActive(),
						isOneScreeningSurveyActive);
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		participantsEditComponent.getImportButton().addClickListener(
				buttonClickListener);
		participantsEditComponent.getExportButton().addClickListener(
				buttonClickListener);
		participantsEditComponent.getAssignGroupButton().addClickListener(
				buttonClickListener);
		participantsEditComponent.getAssignOrganizationButton()
				.addClickListener(buttonClickListener);
		participantsEditComponent.getAssignUnitButton().addClickListener(
				buttonClickListener);
		participantsEditComponent.getSwitchMessagingButton().addClickListener(
				buttonClickListener);
		participantsEditComponent.getSendMessageButton().addClickListener(
				buttonClickListener);
		participantsEditComponent.getDeleteButton().addClickListener(
				buttonClickListener);
		participantsEditComponent.getRefreshButton().addClickListener(
				buttonClickListener);

		// Special handle for export button
		val onDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					@SneakyThrows(FileNotFoundException.class)
					public InputStream getStream() {
						val selectedParticipants = convertSelectedToParticipantsList();

						try {
							return new FileInputStream(
									getInterventionAdministrationManagerService()
											.participantsExport(
													selectedParticipants));
						} catch (final FileNotFoundException e) {
							log.warn("Error during export: {}", e.getMessage());
							throw e;
						} finally {
							participantsEditComponent.getExportButton()
									.setEnabled(true);
						}
					}

					@Override
					public String getFilename() {
						return "Intervention_"
								+ intervention.getName().replaceAll(
										"[^A-Za-z0-9_. ]+", "_")
								+ "_Participants"
								+ Constants.getFileExtension();
					}
				});
		onDemandFileDownloader.extend(participantsEditComponent
				.getExportButton());
		participantsEditComponent.getExportButton().setDisableOnClick(true);
	}

	public void adjust() {
		log.debug("Check access rights for participants based on screening surveys");
		isOneScreeningSurveyActive = getSurveyAdministrationManagerService()
				.isOneScreeningSurveyOfInterventionActive(intervention.getId());

		getInterventionParticipantsEditComponent().updateButtonStatus(
				selectedUIParticipantsIds, intervention.isMonitoringActive(),
				isOneScreeningSurveyActive);

		val participantsTable = getInterventionParticipantsEditComponent()
				.getParticipantsTable();

		log.debug("Update participants");
		refreshBeanContainer(beanContainer, UIParticipant.class,
				getInterventionAdministrationManagerService()
						.getAllParticipantsOfIntervention(intervention.getId()));

		participantsTable.sort();
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val interventionScreeningSurveyEditComponent = getInterventionParticipantsEditComponent();

			if (event.getButton() == interventionScreeningSurveyEditComponent
					.getImportButton()) {
				importParticipants();
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getAssignGroupButton()) {
				assignGroup();
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getAssignOrganizationButton()) {
				assignOrganization();
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getAssignUnitButton()) {
				assignOrganizationUnit();
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getSwitchMessagingButton()) {
				switchMessaging();
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getSendMessageButton()) {
				sendMessage();
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getDeleteButton()) {
				deleteParticipants();
			} else if (event.getButton() == interventionScreeningSurveyEditComponent
					.getRefreshButton()) {
				adjust();
			}
		}
	}

	public void importParticipants() {
		log.debug("Import participants");

		val fileUploadComponentWithController = new FileUploadComponentWithController();
		fileUploadComponentWithController.setListener(new UploadListener() {
			@Override
			public void fileUploadReceived(final File file) {
				log.debug("File upload sucessful, starting import of screening survey");

				try {
					val importedParticipants = getInterventionAdministrationManagerService()
							.participantsImport(file, intervention.getId());

					if (importedParticipants == null) {
						throw new Exception(
								"Imported participants not found in import");
					}

					// Adapt UI
					val participantsTable = getInterventionParticipantsEditComponent()
							.getParticipantsTable();

					if (selectedUIParticipantsIds != null) {
						for (val uiParticipant : selectedUIParticipantsIds) {
							participantsTable.unselect(uiParticipant);
						}
					}
					for (val importedParticipant : importedParticipants) {
						beanContainer.addItem(importedParticipant.getId(),
								UIParticipant.class.cast(importedParticipant
										.toUIModelObject()));
						participantsTable.select(importedParticipant.getId());
					}
					participantsTable.sort();

					getAdminUI()
							.showInformationNotification(
									AdminMessageStrings.NOTIFICATION__PARTICIPANTS_IMPORTED);
				} catch (final Exception e) {
					getAdminUI()
							.showWarningNotification(
									AdminMessageStrings.NOTIFICATION__PARTICIPANTS_IMPORT_FAILED);
				} finally {
					try {
						file.delete();
					} catch (final Exception f) {
						// Do nothing
					}
				}
			}
		});
		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__IMPORT_PARTICIPANTS,
				fileUploadComponentWithController, null);

	}

	public void assignGroup() {
		log.debug("Assign group to participants");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_GROUP_OF_PARTICIPANTS,
				"", null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						val selectedParticipants = convertSelectedToParticipantsList();
						try {
							// Change group
							getInterventionAdministrationManagerService()
									.participantsSetGroup(selectedParticipants,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						for (val selectedParticipant : selectedParticipants) {
							if (getStringValue().equals("")) {
								getStringItemProperty(
										beanContainer
												.getItem(selectedParticipant
														.getId()),
										UIParticipant.GROUP)
										.setValue(
												Messages.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET));
							} else {
								getStringItemProperty(
										beanContainer
												.getItem(selectedParticipant
														.getId()),
										UIParticipant.GROUP).setValue(
										getStringValue());
							}
						}

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__PARTICIPANTS_GROUP_CHANGED);
						closeWindow();
					}
				}, null);
	}

	public void assignOrganization() {
		log.debug("Assign organization to participants");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_ORGANIZATION_OF_PARTICIPANTS,
				"", null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						val selectedParticipants = convertSelectedToParticipantsList();
						try {
							// Change organization
							getInterventionAdministrationManagerService()
									.participantsSetOrganization(
											selectedParticipants,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						for (val selectedParticipant : selectedParticipants) {
							getStringItemProperty(
									beanContainer.getItem(selectedParticipant
											.getId()),
									UIParticipant.ORGANIZATION).setValue(
									getStringValue());
						}

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__PARTICIPANTS_ORGANIZATION_CHANGED);
						closeWindow();
					}
				}, null);
	}

	public void assignOrganizationUnit() {
		log.debug("Assign organization unit to participants");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_ORGANIZATION_UNIT_OF_PARTICIPANTS,
				"", null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						val selectedParticipants = convertSelectedToParticipantsList();
						try {
							// Change organization
							getInterventionAdministrationManagerService()
									.participantsSetOrganizationUnit(
											selectedParticipants,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						for (val selectedParticipant : selectedParticipants) {
							getStringItemProperty(
									beanContainer.getItem(selectedParticipant
											.getId()), UIParticipant.UNIT)
									.setValue(getStringValue());
						}

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__PARTICIPANTS_ORGANIZATION_UNIT_CHANGED);
						closeWindow();
					}
				}, null);
	}

	public void switchMessaging() {
		log.debug("Switch messaging of participants");

		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				val selectedParticipants = convertSelectedToParticipantsList();
				List<Participant> adjustedParticipants;
				try {
					// Switch messaging status
					adjustedParticipants = MC.getInstance()
							.getInterventionExecutionManagerService()
							.participantsSwitchMonitoring(selectedParticipants);
				} catch (final Exception e) {
					handleException(e);
					return;
				}

				// Adapt UI
				val participantsTable = getInterventionParticipantsEditComponent()
						.getParticipantsTable();
				for (val adjustedParticipant : adjustedParticipants) {
					removeAndAddModelObjectToBeanContainer(beanContainer,
							adjustedParticipant);
					participantsTable.unselect(adjustedParticipant);
					participantsTable.select(adjustedParticipant);
				}
				participantsTable.sort();

				getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__PARTICIPANTS_MONITORING_SWITCHED);
				closeWindow();
			}
		}, null);
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

						closeWindow();
					}
				}, null);
	}

	public void deleteParticipants() {
		log.debug("Delete participants");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				val selectedParticipants = convertSelectedToParticipantsList();
				try {
					// Delete participants
					getInterventionAdministrationManagerService()
							.participantsDelete(selectedParticipants);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				for (val selectedParticipant : selectedParticipants) {
					getInterventionParticipantsEditComponent()
							.getParticipantsTable().removeItem(
									selectedParticipant.getId());
				}

				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__PARTICIPANTS_DELETED);

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
