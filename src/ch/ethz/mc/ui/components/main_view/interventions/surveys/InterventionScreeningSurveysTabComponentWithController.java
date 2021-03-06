package ch.ethz.mc.ui.components.main_view.interventions.surveys;

/* ##LICENSE## */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.ui.UIScreeningSurvey;
import ch.ethz.mc.tools.OnDemandFileDownloader;
import ch.ethz.mc.tools.OnDemandFileDownloader.OnDemandStreamResource;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.ui.components.basics.FileUploadComponentWithController;
import ch.ethz.mc.ui.components.basics.FileUploadComponentWithController.UploadListener;
import ch.ethz.mc.ui.components.basics.LocalizedShortStringEditComponent;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the intervention screening surveys tab component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class InterventionScreeningSurveysTabComponentWithController
		extends InterventionScreeningSurveysTabComponent {

	private final Intervention									intervention;

	private UIScreeningSurvey									selectedUIScreeningSurvey			= null;
	private BeanItem<UIScreeningSurvey>							selectedUIScreeningSurveyBeanItem	= null;

	private Collection<ObjectId>								selectedUIScreeningSurveyIds		= null;

	private final BeanContainer<ObjectId, UIScreeningSurvey>	beanContainer;

	public InterventionScreeningSurveysTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// table options
		val screeningSurveysEditComponent = getInterventionScreeningSurveyEditComponent();
		val screeningSurveysTable = getInterventionScreeningSurveyEditComponent()
				.getScreeningSurveysTable();

		// table content
		beanContainer = createBeanContainerForModelObjects(
				UIScreeningSurvey.class,
				getSurveyAdministrationManagerService()
						.getAllScreeningSurveysOfIntervention(
								intervention.getId()));

		screeningSurveysTable.setContainerDataSource(beanContainer);
		screeningSurveysTable
				.setSortContainerPropertyId(UIScreeningSurvey.getSortColumn());
		screeningSurveysTable
				.setVisibleColumns(UIScreeningSurvey.getVisibleColumns());
		screeningSurveysTable
				.setColumnHeaders(UIScreeningSurvey.getColumnHeaders());

		// handle selection change
		screeningSurveysTable.addValueChangeListener(new ValueChangeListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(final ValueChangeEvent event) {
				selectedUIScreeningSurveyIds = (Collection<ObjectId>) screeningSurveysTable
						.getValue();
				if (selectedUIScreeningSurveyIds == null
						|| selectedUIScreeningSurveyIds.size() == 0) {
					screeningSurveysEditComponent.setNothingSelected();

					selectedUIScreeningSurvey = null;
					selectedUIScreeningSurveyBeanItem = null;
					selectedUIScreeningSurveyIds = null;

				} else if (selectedUIScreeningSurveyIds.size() == 1) {
					val objectId = selectedUIScreeningSurveyIds.iterator()
							.next();

					selectedUIScreeningSurvey = getUIModelObjectFromTableByObjectId(
							screeningSurveysTable, UIScreeningSurvey.class,
							objectId);
					selectedUIScreeningSurveyBeanItem = getBeanItemFromTableByObjectId(
							screeningSurveysTable, UIScreeningSurvey.class,
							objectId);
					selectedUIScreeningSurveyIds = null;

					screeningSurveysEditComponent.setSomethingSelected(false);
				} else {
					selectedUIScreeningSurvey = null;
					selectedUIScreeningSurveyBeanItem = null;

					screeningSurveysEditComponent.setSomethingSelected(true);
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		screeningSurveysEditComponent.getNewButton()
				.addClickListener(buttonClickListener);
		screeningSurveysEditComponent.getImportButton()
				.addClickListener(buttonClickListener);
		screeningSurveysEditComponent.getSwitchTypeButton()
				.addClickListener(buttonClickListener);
		screeningSurveysEditComponent.getSwitchStatusButton()
				.addClickListener(buttonClickListener);
		screeningSurveysEditComponent.getRenameButton()
				.addClickListener(buttonClickListener);
		screeningSurveysEditComponent.getEditButton()
				.addClickListener(buttonClickListener);
		screeningSurveysEditComponent.getDuplicateButton()
				.addClickListener(buttonClickListener);
		screeningSurveysEditComponent.getDeleteButton()
				.addClickListener(buttonClickListener);
		screeningSurveysEditComponent.getShowButton()
				.addClickListener(buttonClickListener);

		// Special handle for export button
		val onDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					@SneakyThrows(FileNotFoundException.class)
					public InputStream getStream() {
						try {
							return new FileInputStream(
									getSurveyAdministrationManagerService()
											.screeningSurveyExport(
													selectedUIScreeningSurvey
															.getRelatedModelObject(
																	ScreeningSurvey.class)));
						} catch (final FileNotFoundException e) {
							log.warn("Error during export: {}", e.getMessage());
							throw e;
						} finally {
							screeningSurveysEditComponent.getExportButton()
									.setEnabled(true);
						}
					}

					@Override
					public String getFilename() {
						return "Intervention_"
								+ StringHelpers.cleanFilenameString(
										intervention.getName())
								+ "_Survey_"
								+ StringHelpers.cleanFilenameString(
										selectedUIScreeningSurvey
												.getScreeningSurveyName())
								+ Constants.getFileExtension();
					}
				});
		onDemandFileDownloader
				.extend(screeningSurveysEditComponent.getExportButton());
		screeningSurveysEditComponent.getExportButton().setDisableOnClick(true);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val interventionScreeningSurveyEditComponent = getInterventionScreeningSurveyEditComponent();

			if (event.getButton() == interventionScreeningSurveyEditComponent
					.getNewButton()) {
				createScreeningSurvey();
			} else if (event
					.getButton() == interventionScreeningSurveyEditComponent
							.getImportButton()) {
				importScreeningSurvey();
			} else if (event
					.getButton() == interventionScreeningSurveyEditComponent
							.getSwitchTypeButton()) {
				switchTypeOfScreeningSurvey();
			} else if (event
					.getButton() == interventionScreeningSurveyEditComponent
							.getSwitchStatusButton()) {
				switchStatusOfScreeningSurvey();
			} else if (event
					.getButton() == interventionScreeningSurveyEditComponent
							.getRenameButton()) {
				renameScreeningSurvey();
			} else if (event
					.getButton() == interventionScreeningSurveyEditComponent
							.getEditButton()) {
				editScreeningSurvey();
			} else if (event
					.getButton() == interventionScreeningSurveyEditComponent
							.getDuplicateButton()) {
				duplicateScreeningSurvey();
			} else if (event
					.getButton() == interventionScreeningSurveyEditComponent
							.getDeleteButton()) {
				deleteScreeningSurvey();
			} else if (event
					.getButton() == interventionScreeningSurveyEditComponent
							.getShowButton()) {
				showScreeningSurvey();
			}
		}
	}

	public void createScreeningSurvey() {
		log.debug("Create screening survey");
		showModalLStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_SCREENING_SURVEY,
				null, null, new LocalizedShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						final ScreeningSurvey newScreeningSurvey;
						try {
							val newScreeningSurveyName = getLStringValue();

							// Create intervention
							newScreeningSurvey = getSurveyAdministrationManagerService()
									.screeningSurveyCreate(
											newScreeningSurveyName,
											intervention.getId());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						beanContainer.addItem(newScreeningSurvey.getId(),
								UIScreeningSurvey.class.cast(
										newScreeningSurvey.toUIModelObject()));
						getInterventionScreeningSurveyEditComponent()
								.getScreeningSurveysTable()
								.select(newScreeningSurvey.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void duplicateScreeningSurvey() {
		log.debug("Duplicate screening survey");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				final File temporaryBackupFile = getSurveyAdministrationManagerService()
						.screeningSurveyExport(selectedUIScreeningSurvey
								.getRelatedModelObject(ScreeningSurvey.class));

				try {
					final ScreeningSurvey importedScreeningSurvey = getSurveyAdministrationManagerService()
							.screeningSurveyImport(temporaryBackupFile,
									intervention.getId(), true);

					if (importedScreeningSurvey == null) {
						throw new Exception(
								"Imported screening survey not found in import");
					}

					// Adapt UI
					beanContainer.addItem(importedScreeningSurvey.getId(),
							UIScreeningSurvey.class.cast(
									importedScreeningSurvey.toUIModelObject()));
					getInterventionScreeningSurveyEditComponent()
							.getScreeningSurveysTable()
							.select(importedScreeningSurvey.getId());
					getInterventionScreeningSurveyEditComponent()
							.getScreeningSurveysTable().sort();

					getAdminUI().showInformationNotification(
							AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_DUPLICATED);
				} catch (final Exception e) {
					getAdminUI().showWarningNotification(
							AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_DUPLICATION_FAILED);
				}

				try {
					temporaryBackupFile.delete();
				} catch (final Exception f) {
					// Do nothing
				}

				closeWindow();
			}
		}, null);
	}

	public void importScreeningSurvey() {
		log.debug("Import screening survey");

		val fileUploadComponentWithController = new FileUploadComponentWithController();
		fileUploadComponentWithController.setListener(new UploadListener() {
			@Override
			public void fileUploadReceived(final File file) {
				log.debug(
						"File upload sucessful, starting import of screening survey");

				try {
					final ScreeningSurvey importedScreeningSurvey = getSurveyAdministrationManagerService()
							.screeningSurveyImport(file, intervention.getId(),
									false);

					if (importedScreeningSurvey == null) {
						throw new Exception(
								"Imported screening survey not found in import");
					}

					// Adapt UI
					beanContainer.addItem(importedScreeningSurvey.getId(),
							UIScreeningSurvey.class.cast(
									importedScreeningSurvey.toUIModelObject()));
					getInterventionScreeningSurveyEditComponent()
							.getScreeningSurveysTable()
							.select(importedScreeningSurvey.getId());
					getInterventionScreeningSurveyEditComponent()
							.getScreeningSurveysTable().sort();

					getAdminUI().showInformationNotification(
							AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_IMPORTED);
				} catch (final Exception e) {
					getAdminUI().showWarningNotification(
							AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_IMPORT_FAILED);
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
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__IMPORT_SCREENING_SURVEY,
				fileUploadComponentWithController, null);
	}

	public void switchTypeOfScreeningSurvey() {
		log.debug("Switch type of screening survey");

		try {
			val selectedScreeningSurvey = selectedUIScreeningSurvey
					.getRelatedModelObject(ScreeningSurvey.class);

			// Change type
			getSurveyAdministrationManagerService()
					.screeningSurveySwitchType(selectedScreeningSurvey);
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		getStringItemProperty(selectedUIScreeningSurveyBeanItem,
				UIScreeningSurvey.SCREENING_SURVEY_TYPE)
						.setValue(selectedUIScreeningSurvey
								.getRelatedModelObject(ScreeningSurvey.class)
								.isIntermediateSurvey()
										? Messages.getAdminString(
												AdminMessageStrings.UI_MODEL__SURVEY__INTERMEDIATE)
										: Messages.getAdminString(
												AdminMessageStrings.UI_MODEL__SURVEY__SCREENING));

		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_TYPE_CHANGED);
	}

	public void switchStatusOfScreeningSurvey() {
		log.debug("Switch status of screening survey");

		if (selectedUIScreeningSurvey == null) {
			val screeningSurveysTable = getInterventionScreeningSurveyEditComponent()
					.getScreeningSurveysTable();

			for (val objectId : selectedUIScreeningSurveyIds) {
				val screeningUIScreeningSurveyOfObjectId = getUIModelObjectFromTableByObjectId(
						screeningSurveysTable, UIScreeningSurvey.class,
						objectId);

				val selectedScreeningSurvey = screeningUIScreeningSurveyOfObjectId
						.getRelatedModelObject(ScreeningSurvey.class);

				try {
					// Change type
					getSurveyAdministrationManagerService()
							.screeningSurveySetActive(selectedScreeningSurvey,
									!selectedScreeningSurvey.isActive());
				} catch (final Exception e) {
					handleException(e);
					return;
				}

				// Adapt UI
				removeAndAddModelObjectToBeanContainer(beanContainer,
						selectedScreeningSurvey);
			}

			screeningSurveysTable.sort();
			screeningSurveysTable.select(null);
		} else {
			try {
				val selectedScreeningSurvey = selectedUIScreeningSurvey
						.getRelatedModelObject(ScreeningSurvey.class);

				// Change type
				getSurveyAdministrationManagerService()
						.screeningSurveySetActive(selectedScreeningSurvey,
								!selectedScreeningSurvey.isActive());
			} catch (final Exception e) {
				handleException(e);
				return;
			}

			// Adapt UI
			val selectedScreeningSurvey = selectedUIScreeningSurvey
					.getRelatedModelObject(ScreeningSurvey.class);

			val screeningSurveysTable = getInterventionScreeningSurveyEditComponent()
					.getScreeningSurveysTable();
			removeAndAddModelObjectToBeanContainer(beanContainer,
					selectedScreeningSurvey);
			screeningSurveysTable.sort();
			screeningSurveysTable.select(null);
			screeningSurveysTable.select(selectedScreeningSurvey.getId());
		}

		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_STATUS_CHANGED);
	}

	public void renameScreeningSurvey() {
		log.debug("Rename screening survey");

		showModalLStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_SCREENING_SURVEY,
				selectedUIScreeningSurvey
						.getRelatedModelObject(ScreeningSurvey.class).getName(),
				null, new LocalizedShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							val selectedScreeningSurvey = selectedUIScreeningSurvey
									.getRelatedModelObject(
											ScreeningSurvey.class);

							// Change name
							getSurveyAdministrationManagerService()
									.screeningSurveyChangeName(
											selectedScreeningSurvey,
											getLStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						getStringItemProperty(selectedUIScreeningSurveyBeanItem,
								UIScreeningSurvey.SCREENING_SURVEY_NAME)
										.setValue(selectedUIScreeningSurvey
												.getRelatedModelObject(
														ScreeningSurvey.class)
												.getName().toString());

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_RENAMED);
						closeWindow();
					}
				}, null);
	}

	public void editScreeningSurvey() {
		log.debug("Edit screening survey");
		val screeningSurvey = selectedUIScreeningSurvey
				.getRelatedModelObject(ScreeningSurvey.class);

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_SCREENING_SURVEY,
				new ScreeningSurveyEditComponentWithController(screeningSurvey),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						val screeningSurveysTable = getInterventionScreeningSurveyEditComponent()
								.getScreeningSurveysTable();

						removeAndAddModelObjectToBeanContainer(beanContainer,
								screeningSurvey);
						screeningSurveysTable.sort();
						screeningSurveysTable.select(screeningSurvey.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_UPDATED);

						closeWindow();
					}
				}, screeningSurvey.getName());
	}

	public void deleteScreeningSurvey() {
		log.debug("Delete screening survey");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedScreeningSurvey = selectedUIScreeningSurvey
							.getRelatedModelObject(ScreeningSurvey.class);

					// Delete intervention
					getSurveyAdministrationManagerService()
							.screeningSurveyDelete(selectedScreeningSurvey);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getInterventionScreeningSurveyEditComponent()
						.getScreeningSurveysTable()
						.removeItem(selectedUIScreeningSurvey
								.getRelatedModelObject(ScreeningSurvey.class)
								.getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_DELETED);

				closeWindow();
			}
		}, null);
	}

	public void showScreeningSurvey() {
		val survey = selectedUIScreeningSurvey
				.getRelatedModelObject(ScreeningSurvey.class);

		if (!intervention.isActive()) {
			getAdminUI().showWarningNotification(
					AdminMessageStrings.NOTIFICATION__INTERVENTION_NOT_ACTIVE);
			return;
		}

		if (!survey.isActive()) {
			getAdminUI().showWarningNotification(
					AdminMessageStrings.NOTIFICATION__SURVEY_NOT_ACTIVE);
			return;
		}

		if (survey.isIntermediateSurvey()) {
			log.debug("Show intermediate survey");

			val participantId = getAdminUI().getUISession()
					.getCurrentBackendUserParticipantId();

			if (participantId == null) {
				getAdminUI().showWarningNotification(
						AdminMessageStrings.NOTIFICATION__SURVEY_PARTICIPATION_REQUIRED);
				return;
			}

			val participant = getInterventionAdministrationManagerService()
					.getParticipant(participantId);

			if (participant == null) {
				getAdminUI().showWarningNotification(
						AdminMessageStrings.NOTIFICATION__SURVEY_PARTICIPATION_REQUIRED);
				return;
			}

			val surveyShortURL = getSurveyExecutionManagerService()
					.intermediateSurveyParticipantShortURLEnsure(participantId,
							survey.getId());

			final String url = getAdminUI().getPage().getLocation().toString()
					.substring(0,
							getAdminUI().getPage().getLocation().toString()
									.lastIndexOf("/") + 1)
					+ ImplementationConstants.SHORT_ID_SCREEN_SURVEY_AND_FEEDBACK_SERVLET_PATH
					+ "/" + surveyShortURL.calculateIdPartOfURL() + "/";

			getAdminUI().getPage().open(url, "_blank");
		} else {
			log.debug("Show screening survey");

			getAdminUI().getUISession().resetParticipantExpection();

			final String url = getAdminUI().getPage().getLocation().toString()
					.substring(0,
							getAdminUI().getPage().getLocation().toString()
									.lastIndexOf("/") + 1)
					+ survey.getId() + "/";

			getAdminUI().getPage().open(url, "_blank");
		}
	}

}
