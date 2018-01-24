package ch.ethz.mc.ui.components.main_view.interventions;

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.ui.UIIntervention;
import ch.ethz.mc.model.ui.UIModule;
import ch.ethz.mc.modules.AbstractModule;
import ch.ethz.mc.tools.OnDemandFileDownloader;
import ch.ethz.mc.tools.OnDemandFileDownloader.OnDemandStreamResource;
import ch.ethz.mc.ui.components.basics.FileUploadComponentWithController;
import ch.ethz.mc.ui.components.basics.FileUploadComponentWithController.UploadListener;
import ch.ethz.mc.ui.components.basics.ShortStringEditComponent;
import ch.ethz.mc.ui.components.helpers.CaseInsensitiveItemSorter;
import ch.ethz.mc.ui.views.MainView;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the all interventions tab component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class AllInterventionsTabComponentWithController
		extends AllInterventionsTabComponent {

	private final MainView													mainView;

	private UIIntervention													selectedUIIntervention			= null;
	private BeanItem<UIIntervention>										selectedUIInterventionBeanItem	= null;

	private final BeanContainer<ObjectId, UIIntervention>					interventionsBeanContainer;

	private final BeanContainer<Class<? extends AbstractModule>, UIModule>	modulesBeanContainer;

	private Class<? extends AbstractModule>									selectedModule					= null;

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
		val allRelevantIntervention = getUISession().isAdmin()
				? getInterventionAdministrationManagerService()
						.getAllInterventions()
				: getInterventionAdministrationManagerService()
						.getAllInterventionsForAuthor(
								getUISession().getCurrentAuthorId());
		interventionsBeanContainer = createBeanContainerForModelObjects(
				UIIntervention.class, allRelevantIntervention);

		allInterventionsTable
				.setContainerDataSource(interventionsBeanContainer);
		allInterventionsTable
				.setSortContainerPropertyId(UIIntervention.getSortColumn());
		allInterventionsTable
				.setVisibleColumns(UIIntervention.getVisibleColumns());
		allInterventionsTable
				.setColumnHeaders(UIIntervention.getColumnHeaders());

		// handle selection change
		allInterventionsTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final ObjectId objectId = (ObjectId) allInterventionsTable
						.getValue();
				if (objectId == null) {
					selectedUIIntervention = null;
					selectedUIInterventionBeanItem = null;

					getAdminUI().getLockingService()
							.releaseLockOfUISession(getUISession());
				} else {
					selectedUIIntervention = getUIModelObjectFromTableByObjectId(
							allInterventionsTable, UIIntervention.class,
							objectId);
					selectedUIInterventionBeanItem = getBeanItemFromTableByObjectId(
							allInterventionsTable, UIIntervention.class,
							objectId);

					if (!getAdminUI().getLockingService()
							.checkAndSetLockForUISession(getUISession(),
									objectId)) {

						allInterventionsTable.select(null);
						selectedUIIntervention = null;
						selectedUIInterventionBeanItem = null;

						getAdminUI().showWarningNotification(
								AdminMessageStrings.NOTIFICATION__INTERVENTION_LOCKED);
					}
				}

				allInterventionsEditComponent.adjust(
						selectedUIIntervention != null,
						selectedUIIntervention != null && selectedUIIntervention
								.getRelatedModelObject(Intervention.class)
								.isMonitoringActive(),
						selectedModule != null);
			}
		});

		// Handle modules table
		modulesBeanContainer = new BeanContainer<Class<? extends AbstractModule>, UIModule>(
				UIModule.class);
		modulesBeanContainer.setItemSorter(new CaseInsensitiveItemSorter());

		val modules = getInterventionAdministrationManagerService()
				.getRegisteredModules();
		val modulesTable = allInterventionsEditComponent.getModulesTable();
		modulesTable.setImmediate(true);
		modulesTable.setSelectable(true);
		modulesTable.setContainerDataSource(modulesBeanContainer);
		modulesTable.setSortContainerPropertyId(UIModule.getSortColumn());
		modulesTable.setVisibleColumns(UIModule.getVisibleColumns());
		modulesTable.setColumnHeaders(UIModule.getColumnHeaders());

		for (val moduleClass : modules) {
			AbstractModule module;
			try {
				module = moduleClass.newInstance();
				modulesBeanContainer.addItem(moduleClass, module.toUIModule());
			} catch (final Exception e) {
				log.error("Error when creating new module instance: {}",
						e.getMessage());
			}
		}

		// Handle table selection change
		modulesTable.addValueChangeListener(new ValueChangeListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = modulesTable.getValue();
				if (objectId == null) {
					selectedModule = null;
				} else {
					selectedModule = (Class<? extends AbstractModule>) modulesTable
							.getValue();
				}

				allInterventionsEditComponent.adjust(
						selectedUIIntervention != null,
						selectedUIIntervention != null && selectedUIIntervention
								.getRelatedModelObject(Intervention.class)
								.isMonitoringActive(),
						selectedModule != null);
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		allInterventionsEditComponent.getNewButton()
				.addClickListener(buttonClickListener);
		allInterventionsEditComponent.getImportButton()
				.addClickListener(buttonClickListener);
		allInterventionsEditComponent.getRenameButton()
				.addClickListener(buttonClickListener);
		allInterventionsEditComponent.getEditButton()
				.addClickListener(buttonClickListener);
		allInterventionsEditComponent.getDuplicateButton()
				.addClickListener(buttonClickListener);
		allInterventionsEditComponent.getDeleteButton()
				.addClickListener(buttonClickListener);
		allInterventionsEditComponent.getOpenModuleButton()
				.addClickListener(buttonClickListener);

		allInterventionsEditComponent.getResultsButton()
				.addClickListener(buttonClickListener);
		allInterventionsEditComponent.getProblemsButton()
				.addClickListener(buttonClickListener);
		allInterventionsEditComponent.getI18nButton()
				.addClickListener(buttonClickListener);

		// Special handle for export button
		val onDemandFileDownloaderExport = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					@SneakyThrows(FileNotFoundException.class)
					public InputStream getStream() {
						try {
							return new FileInputStream(
									getInterventionAdministrationManagerService()
											.interventionExport(
													selectedUIIntervention
															.getRelatedModelObject(
																	Intervention.class)));
						} catch (final FileNotFoundException e) {
							log.warn("Error during export: {}", e.getMessage());
							throw e;
						} finally {
							allInterventionsEditComponent.getExportButton()
									.setEnabled(true);
						}
					}

					@Override
					public String getFilename() {
						return "Intervention_"
								+ selectedUIIntervention.getInterventionName()
										.replaceAll("[^A-Za-z0-9_. ]+", "_")
								+ Constants.getFileExtension();
					}
				});
		onDemandFileDownloaderExport
				.extend(allInterventionsEditComponent.getExportButton());
		allInterventionsEditComponent.getExportButton().setDisableOnClick(true);

		// Special handle for report button
		val onDemandFileDownloaderReport = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					@SneakyThrows(FileNotFoundException.class)
					public InputStream getStream() {
						try {
							return new FileInputStream(MC.getInstance()
									.getReportGeneratorService().generateReport(
											selectedUIIntervention
													.getRelatedModelObject(
															Intervention.class),
											getUISession().getBaseURL()));
						} catch (final FileNotFoundException e) {
							log.warn("Error during report generation: {}",
									e.getMessage());
							throw e;
						} finally {
							allInterventionsEditComponent.getReportButton()
									.setEnabled(true);
						}
					}

					@Override
					public String getFilename() {
						return "Intervention_"
								+ selectedUIIntervention.getInterventionName()
										.replaceAll("[^A-Za-z0-9_. ]+", "_")
								+ ".html";
					}
				});
		onDemandFileDownloaderReport
				.extend(allInterventionsEditComponent.getReportButton());
		allInterventionsEditComponent.getReportButton().setDisableOnClick(true);
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
					.getI18nButton()) {
				openI18N();
			} else if (event.getButton() == allInterventionsEditComponent
					.getEditButton()) {
				editIntervention();
			} else if (event.getButton() == allInterventionsEditComponent
					.getDuplicateButton()) {
				duplicateIntervention();
			} else if (event.getButton() == allInterventionsEditComponent
					.getDeleteButton()) {
				deleteIntervention();
			} else if (event.getButton() == allInterventionsEditComponent
					.getOpenModuleButton()) {
				openModule();
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
						interventionsBeanContainer.addItem(
								newIntervention.getId(),
								UIIntervention.class.cast(
										newIntervention.toUIModelObject()));
						getAllInterventionsEditComponent()
								.getAllInterventionsTable()
								.select(newIntervention.getId());
						getAdminUI().showInformationNotification(
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
						.interventionExport(selectedUIIntervention
								.getRelatedModelObject(Intervention.class));

				try {
					final Intervention importedIntervention = getInterventionAdministrationManagerService()
							.interventionImport(temporaryBackupFile, true);

					if (importedIntervention == null) {
						throw new Exception(
								"Imported intervention not found in import");
					}

					// Adapt UI
					interventionsBeanContainer.addItem(
							importedIntervention.getId(),
							UIIntervention.class.cast(
									importedIntervention.toUIModelObject()));
					getAllInterventionsEditComponent()
							.getAllInterventionsTable()
							.select(importedIntervention.getId());
					getAllInterventionsEditComponent()
							.getAllInterventionsTable().sort();

					getAdminUI().showInformationNotification(
							AdminMessageStrings.NOTIFICATION__INTERVENTION_DUPLICATED);
				} catch (final Exception e) {
					getAdminUI().showWarningNotification(
							AdminMessageStrings.NOTIFICATION__INTERVENTION_DUPLICATION_FAILED);
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

	public void importIntervention() {
		log.debug("Import intervention");

		val fileUploadComponentWithController = new FileUploadComponentWithController();
		fileUploadComponentWithController.setListener(new UploadListener() {
			@Override
			public void fileUploadReceived(final File file) {
				log.debug(
						"File upload sucessful, starting import of intervention");

				try {
					final Intervention importedIntervention = getInterventionAdministrationManagerService()
							.interventionImport(file, false);

					if (importedIntervention == null) {
						throw new Exception(
								"Imported intervention not found in import");
					}

					// Adapt UI
					interventionsBeanContainer.addItem(
							importedIntervention.getId(),
							UIIntervention.class.cast(
									importedIntervention.toUIModelObject()));
					getAllInterventionsEditComponent()
							.getAllInterventionsTable()
							.select(importedIntervention.getId());
					getAllInterventionsEditComponent()
							.getAllInterventionsTable().sort();

					getAdminUI().showInformationNotification(
							AdminMessageStrings.NOTIFICATION__INTERVENTION_IMPORTED);
				} catch (final Exception e) {
					getAdminUI().showWarningNotification(
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
				selectedUIIntervention.getRelatedModelObject(Intervention.class)
						.getName(),
				null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							val selectedIntervention = selectedUIIntervention
									.getRelatedModelObject(Intervention.class);

							// Change name
							getInterventionAdministrationManagerService()
									.interventionSetName(selectedIntervention,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						getStringItemProperty(selectedUIInterventionBeanItem,
								UIIntervention.INTERVENTION_NAME)
										.setValue(selectedUIIntervention
												.getRelatedModelObject(
														Intervention.class)
												.getName());

						getAdminUI().showInformationNotification(
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

	public void openI18N() {
		val intervention = selectedUIIntervention
				.getRelatedModelObject(Intervention.class);

		log.debug("Open i18n of intervention {}", intervention.getId());

		showModalClosableEditWindow(AdminMessageStrings.I18N__TITLE,
				new InterventionI18nComponentenWithController(intervention),
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

		getAdminUI().getLockingService().releaseLockOfUISession(getUISession());

		mainView.switchToInterventionsView();
	}

	public void deleteIntervention() {
		log.debug("Delete intervention");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedIntervention = selectedUIIntervention
							.getRelatedModelObject(Intervention.class);

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
						.removeItem(selectedUIIntervention
								.getRelatedModelObject(Intervention.class)
								.getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__INTERVENTION_DELETED);

				closeWindow();
			}
		}, null);
	}

	public void openModule() {
		log.debug("Open module");
		val intervention = selectedUIIntervention
				.getRelatedModelObject(Intervention.class);

		@val
		ch.ethz.mc.modules.AbstractModule selectedModuleInstance;
		try {
			selectedModuleInstance = selectedModule.newInstance();

			selectedModuleInstance.prepareToShow(intervention.getId());

			showModalClosableEditWindow(selectedModuleInstance.getName(),
					selectedModuleInstance, null);
		} catch (final Exception e) {
			log.error("Error when creating new module instance: {}",
					e.getMessage());
		}
	}
}
