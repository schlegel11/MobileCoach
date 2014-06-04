package org.isgf.mhc.ui.views.components.interventions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.NullArgumentException;
import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.ui.UIIntervention;
import org.isgf.mhc.tools.OnDemandFileDownloader;
import org.isgf.mhc.tools.OnDemandFileDownloader.OnDemandStreamResource;
import org.isgf.mhc.ui.views.MainView;
import org.isgf.mhc.ui.views.components.basics.FileUploadComponentWithController;
import org.isgf.mhc.ui.views.components.basics.FileUploadComponentWithController.UploadListener;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the all interventions tab component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class AllInterventionsTabComponentWithController extends
		AllInterventionsTabComponent {

	private final MainView									mainView;

	private UIIntervention									selectedUIIntervention			= null;
	private BeanItem<UIIntervention>						selectedUIInterventionBeanItem	= null;

	private final BeanContainer<ObjectId, UIIntervention>	beanContainer;

	public AllInterventionsTabComponentWithController(final MainView mainView) {
		super();

		this.mainView = mainView;

		// table options
		val allInterventionsEditComponent = getAllInterventionsEditComponent();
		val allInterventionsTable = getAllInterventionsEditComponent()
				.getAllInterventionsTable();
		allInterventionsTable.setSelectable(true);
		allInterventionsTable.setImmediate(true);

		// table content
		val allRelevantIntervention = getUISession().isAdmin() ? getInterventionAdministrationManagerService()
				.getAllInterventions()
				: getInterventionAdministrationManagerService()
						.getAllInterventionsForAuthor(
								getUISession().getCurrentAuthorId());
		beanContainer = createBeanContainerForModelObjects(
				UIIntervention.class, allRelevantIntervention);

		allInterventionsTable.setContainerDataSource(beanContainer);
		allInterventionsTable.setSortContainerPropertyId(UIIntervention
				.getSortColumn());
		allInterventionsTable.setVisibleColumns(UIIntervention
				.getVisibleColumns());
		allInterventionsTable.setColumnHeaders(UIIntervention
				.getColumnHeaders());

		// handle selection change
		allInterventionsTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = allInterventionsTable.getValue();
				if (objectId == null) {
					allInterventionsEditComponent.setNothingSelected();
					selectedUIIntervention = null;
					selectedUIInterventionBeanItem = null;
				} else {
					selectedUIIntervention = getUIModelObjectFromTableByObjectId(
							allInterventionsTable, UIIntervention.class,
							objectId);
					selectedUIInterventionBeanItem = getBeanItemFromTableByObjectId(
							allInterventionsTable, UIIntervention.class,
							objectId);
					allInterventionsEditComponent.setSomethingSelected();
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		allInterventionsEditComponent.getNewButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getImportButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getRenameButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getResultsButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getProblemsButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getEditButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getDuplicateButton().addClickListener(
				buttonClickListener);
		allInterventionsEditComponent.getDeleteButton().addClickListener(
				buttonClickListener);

		// Special handle for export button
		val onDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					@SneakyThrows(FileNotFoundException.class)
					public InputStream getStream() {
						return new FileInputStream(
								getInterventionAdministrationManagerService()
										.interventionExport(
												selectedUIIntervention
														.getRelatedModelObject(Intervention.class)));
					}

					@Override
					public String getFilename() {
						return "Intervention_"
								+ selectedUIIntervention.getInterventionName()
										.replaceAll("[^A-Za-z0-9_. ]+", "_")
								+ Constants.getFileExtension();
					}
				});
		onDemandFileDownloader.extend(allInterventionsEditComponent
				.getExportButton());
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val allInterventionsEditComponent = getAllInterventionsEditComponent();

			if (event.getButton() == allInterventionsEditComponent
					.getNewButton()) {
				createIntervention();
			} else if (event.getButton() == allInterventionsEditComponent
					.getImportButton()) {
				importIntervention();
			} else if (event.getButton() == allInterventionsEditComponent
					.getRenameButton()) {
				renameIntervention();
			} else if (event.getButton() == allInterventionsEditComponent
					.getResultsButton()) {
				openResults();
			} else if (event.getButton() == allInterventionsEditComponent
					.getProblemsButton()) {
				openProblems();
			} else if (event.getButton() == allInterventionsEditComponent
					.getEditButton()) {
				editIntervention();
			} else if (event.getButton() == allInterventionsEditComponent
					.getDuplicateButton()) {
				duplicateIntervention();
			} else if (event.getButton() == allInterventionsEditComponent
					.getDeleteButton()) {
				deleteIntervention();
			}
		}
	}

	public void createIntervention() {
		log.debug("Create intervention");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_INTERVENTION,
				null, null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						final Intervention newIntervention;
						try {
							val newInterventionName = getStringValue();

							// Create intervention
							newIntervention = getInterventionAdministrationManagerService()
									.interventionCreate(newInterventionName);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						beanContainer.addItem(newIntervention.getId(),
								UIIntervention.class.cast(newIntervention
										.toUIModelObject()));
						getAllInterventionsEditComponent()
								.getAllInterventionsTable().select(
										newIntervention.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__INTERVENTION_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void duplicateIntervention() {
		log.debug("Duplicate intervention");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				final File temporaryBackupFile = getInterventionAdministrationManagerService()
						.interventionExport(
								selectedUIIntervention
										.getRelatedModelObject(Intervention.class));

				try {
					final Intervention importedIntervention = getInterventionAdministrationManagerService()
							.interventionImport(temporaryBackupFile);

					if (importedIntervention == null) {
						throw new NullArgumentException(
								"Imported intervention not found in import");
					}

					// Adapt UI
					beanContainer.addItem(importedIntervention.getId(),
							UIIntervention.class.cast(importedIntervention
									.toUIModelObject()));
					getAllInterventionsEditComponent()
							.getAllInterventionsTable().select(
									importedIntervention.getId());
					getAllInterventionsEditComponent()
							.getAllInterventionsTable().sort();

					getAdminUI()
							.showInformationNotification(
									AdminMessageStrings.NOTIFICATION__INTERVENTION_DUPLICATED);
				} catch (final Exception e) {
					getAdminUI()
							.showWarningNotification(
									AdminMessageStrings.NOTIFICATION__INTERVENTION_DUPLICATION_FAILED);
				}

				try {
					temporaryBackupFile.delete();
				} catch (final Exception f) {
					// Do nothing
				}
			}
		}, null);
	}

	public void importIntervention() {
		log.debug("Import intervention");

		val fileUploadComponentWithController = new FileUploadComponentWithController();
		fileUploadComponentWithController.setListener(new UploadListener() {
			@Override
			public void fileUploadReceived(final File file) {
				log.debug("File upload sucessful, starting import of intervention");

				try {
					final Intervention importedIntervention = getInterventionAdministrationManagerService()
							.interventionImport(file);

					if (importedIntervention == null) {
						throw new NullArgumentException(
								"Imported intervention not found in import");
					}

					// Adapt UI
					beanContainer.addItem(importedIntervention.getId(),
							UIIntervention.class.cast(importedIntervention
									.toUIModelObject()));
					getAllInterventionsEditComponent()
							.getAllInterventionsTable().select(
									importedIntervention.getId());
					getAllInterventionsEditComponent()
							.getAllInterventionsTable().sort();

					getAdminUI()
							.showInformationNotification(
									AdminMessageStrings.NOTIFICATION__INTERVENTION_IMPORTED);
				} catch (final Exception e) {
					getAdminUI()
							.showWarningNotification(
									AdminMessageStrings.NOTIFICATION__INTERVENTION_IMPORT_FAILED);
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
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__IMPORT_INTERVENTION,
				fileUploadComponentWithController, null);
	}

	public void renameIntervention() {
		log.debug("Rename intervention");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_INTERVENTION,
				selectedUIIntervention
						.getRelatedModelObject(Intervention.class).getName(),
				null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							val selectedIntervention = selectedUIIntervention
									.getRelatedModelObject(Intervention.class);

							// Change name
							getInterventionAdministrationManagerService()
									.interventionChangeName(
											selectedIntervention,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						getStringItemProperty(selectedUIInterventionBeanItem,
								UIIntervention.INTERVENTION_NAME).setValue(
								selectedUIIntervention.getRelatedModelObject(
										Intervention.class).getName());

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__INTERVENTION_RENAMED);
						closeWindow();
					}
				}, null);
	}

	public void openResults() {
		val intervention = selectedUIIntervention
				.getRelatedModelObject(Intervention.class);

		log.debug("Open results of intervention {}", intervention.getId());

		showModalClosableEditWindow(AdminMessageStrings.RESULTS__TITLE,
				new InterventionResultsComponentWithController(intervention),
				null, intervention.getName());
	}

	public void openProblems() {
		val intervention = selectedUIIntervention
				.getRelatedModelObject(Intervention.class);

		log.debug("Open problems of intervention {}", intervention.getId());

		showModalClosableEditWindow(AdminMessageStrings.PROBLEMS__TITLE,
				new InterventionProblemsComponentWithController(intervention),
				null, intervention.getName());
	}

	public void editIntervention() {
		val intervention = selectedUIIntervention
				.getRelatedModelObject(Intervention.class);

		log.debug("Edit intervention {}", intervention.getId());

		// Replace current components with accordion
		getMainLayout().removeAllComponents();
		getMainLayout().addComponent(
				new InterventionEditingContainerComponentWithController(this,
						intervention));
	}

	public void returnToInterventionList() {
		log.debug("Step back to intervention overview");

		mainView.switchToInterventionsView();
	}

	public void deleteIntervention() {
		log.debug("Delete intervention");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedIntervention = selectedUIIntervention.getRelatedModelObject(Intervention.class);

					// Delete intervention
					getInterventionAdministrationManagerService()
							.interventionDelete(selectedIntervention);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getAllInterventionsEditComponent().getAllInterventionsTable()
						.removeItem(
								selectedUIIntervention.getRelatedModelObject(
										Intervention.class).getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__INTERVENTION_DELETED);

				closeWindow();
			}
		}, null);
	}
}
