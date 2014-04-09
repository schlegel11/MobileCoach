package org.isgf.mhc.ui.views.components.interventions;

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

import org.apache.commons.lang.NullArgumentException;
import org.bson.types.ObjectId;
import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.Participant;
import org.isgf.mhc.model.ui.UIParticipant;
import org.isgf.mhc.tools.OnDemandFileDownloader;
import org.isgf.mhc.tools.OnDemandFileDownloader.OnDemandStreamResource;
import org.isgf.mhc.tools.StringValidator;
import org.isgf.mhc.ui.views.components.basics.FileUploadComponentWithController;
import org.isgf.mhc.ui.views.components.basics.FileUploadComponentWithController.UploadListener;
import org.isgf.mhc.ui.views.components.basics.PlaceholderStringEditComponent;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

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

						return new FileInputStream(
								getInterventionAdministrationManagerService()
										.participantsExport(
												selectedParticipants));
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
	}

	public void adjust() {
		log.debug("Check access rights for participants based on scrrening surveys");
		isOneScreeningSurveyActive = getScreeningSurveyAdministrationManagerService()
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
						throw new NullArgumentException(
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
				try {
					// Change organization
					MHC.getInstance().getInterventionExecutionManagerService()
							.participantsSwitchMessaging(selectedParticipants);
				} catch (final Exception e) {
					handleException(e);
					return;
				}

				// Adapt UI
				val participantsTable = getInterventionParticipantsEditComponent()
						.getParticipantsTable();
				for (val selectedParticipant : selectedParticipants) {
					removeAndAddModelObjectToBeanContainer(beanContainer,
							selectedParticipant);
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
