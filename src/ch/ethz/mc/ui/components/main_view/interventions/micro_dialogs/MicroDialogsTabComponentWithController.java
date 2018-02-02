package ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs;

import java.io.File;
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
import java.util.Hashtable;

import org.bson.types.ObjectId;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.ui.components.basics.ShortStringEditComponent;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the micro dialogs tab with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MicroDialogsTabComponentWithController
		extends MicroDialogsTabComponent {

	private final Intervention				intervention;

	private MicroDialog						selectedMicroDialog	= null;

	private final Hashtable<Tab, ObjectId>	tabsWithObjectIdsOfMicroDialog;

	public MicroDialogsTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		tabsWithObjectIdsOfMicroDialog = new Hashtable<TabSheet.Tab, ObjectId>();

		// Retrieve monitoring message groups to set current and fill tabs
		final Iterable<MicroDialog> microDialogsIterable = getInterventionAdministrationManagerService()
				.getAllMicroDialogsOfIntervention(intervention.getId());

		for (val microDialog : microDialogsIterable) {
			val newTab = addTabComponent(microDialog, intervention);

			tabsWithObjectIdsOfMicroDialog.put(newTab, microDialog.getId());

			if (getMicroDialogsTabSheet().getComponentCount() == 1) {
				// First tab added
				selectedMicroDialog = microDialog;
				getMicroDialogsTabSheet().setSelectedTab(newTab);
			}
		}

		if (getMicroDialogsTabSheet().getComponentCount() > 0) {
			setSomethingSelected();
		}

		// handle tab selection change
		getMicroDialogsTabSheet()
				.addSelectedTabChangeListener(new SelectedTabChangeListener() {

					@Override
					public void selectedTabChange(
							final SelectedTabChangeEvent event) {
						log.debug("New dialog selected");

						val selectedTab = event.getTabSheet().getSelectedTab();
						if (selectedTab == null) {
							setNothingSelected();
							selectedMicroDialog = null;
						} else {
							val selectedTabObject = event.getTabSheet()
									.getTab(selectedTab);
							val microDialogObjectId = tabsWithObjectIdsOfMicroDialog
									.get(selectedTabObject);

							// New tabs cannot be found in list, so the selected
							// tab will be set programmatically after creation
							if (microDialogObjectId != null) {
								selectedMicroDialog = getInterventionAdministrationManagerService()
										.getMicroDialog(microDialogObjectId);
							}

							setSomethingSelected();
						}
					}
				});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewDialogButton().addClickListener(buttonClickListener);
		getRenameDialogButton().addClickListener(buttonClickListener);
		getDuplicateDialogButton().addClickListener(buttonClickListener);
		getMoveDialogLeftButton().addClickListener(buttonClickListener);
		getMoveDialogRightButton().addClickListener(buttonClickListener);
		getDeleteDialogButton().addClickListener(buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getNewDialogButton()) {
				createDialog();
			} else if (event.getButton() == getMoveDialogLeftButton()) {
				moveDialog(true);
			} else if (event.getButton() == getMoveDialogRightButton()) {
				moveDialog(false);
			} else if (event.getButton() == getRenameDialogButton()) {
				renameDialog();
			} else if (event.getButton() == getDuplicateDialogButton()) {
				duplicateDialog();
			} else if (event.getButton() == getDeleteDialogButton()) {
				deleteDialog();
			}
		}
	}

	public void createDialog() {
		log.debug("Create dialog");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_MICRO_DIALOG,
				null, null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						MicroDialog newMicroDialog;
						try {
							// Create new variable
							newMicroDialog = getInterventionAdministrationManagerService()
									.microDialogCreate(getStringValue(),
											intervention.getId());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						val newTab = addTabComponent(newMicroDialog,
								intervention);

						tabsWithObjectIdsOfMicroDialog.put(newTab,
								newMicroDialog.getId());

						selectedMicroDialog = newMicroDialog;

						getMicroDialogsTabSheet().setSelectedTab(newTab);
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void moveDialog(final boolean moveLeft) {
		log.debug("Move dialog {}", moveLeft ? "left" : "right");

		val swappedMicroDialog = getInterventionAdministrationManagerService()
				.microDialogMove(selectedMicroDialog, moveLeft);

		if (swappedMicroDialog == null) {
			log.debug("Micro dialog is already at beginning/end of list");
			return;
		}

		val tabSheet = getMicroDialogsTabSheet();
		val currentPosition = tabSheet
				.getTabPosition(tabSheet.getTab(tabSheet.getSelectedTab()));
		if (moveLeft) {
			tabSheet.setTabPosition(tabSheet.getTab(tabSheet.getSelectedTab()),
					currentPosition - 1);
		} else {
			tabSheet.setTabPosition(tabSheet.getTab(tabSheet.getSelectedTab()),
					currentPosition + 1);
		}

		setSomethingSelected();
	}

	public void renameDialog() {
		log.debug("Rename dialog");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_MICRO_DIALOG,
				selectedMicroDialog.getName(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change name
							getInterventionAdministrationManagerService()
									.microDialogSetName(selectedMicroDialog,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						val tab = getMicroDialogsTabSheet();
						tab.getTab(tab.getSelectedTab())
								.setCaption(selectedMicroDialog.getName());

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_RENAMED);
						closeWindow();
					}
				}, null);
	}

	public void duplicateDialog() {
		log.debug("Duplicate dialog");

		File temporaryBackupFile = null;
		try {
			temporaryBackupFile = getInterventionAdministrationManagerService()
					.microDialogExport(selectedMicroDialog);

			final MicroDialog importedMicroDialog = getInterventionAdministrationManagerService()
					.microDialogImport(temporaryBackupFile);

			if (importedMicroDialog == null) {
				throw new Exception("Imported element not found in import");
			}

			// Adapt UI
			val newTab = addTabComponent(importedMicroDialog, intervention);

			tabsWithObjectIdsOfMicroDialog.put(newTab,
					importedMicroDialog.getId());

			selectedMicroDialog = importedMicroDialog;

			getMicroDialogsTabSheet().setSelectedTab(newTab);
			getAdminUI().showInformationNotification(
					AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_DUPLICATED);
		} catch (final Exception e) {
			getAdminUI().showWarningNotification(
					AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_DUPLICATION_FAILED);
		}

		try {
			temporaryBackupFile.delete();
		} catch (final Exception f) {
			// Do nothing
		}
	}

	public void deleteDialog() {
		log.debug("Delete dialog");

		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					// Delete dialog
					getInterventionAdministrationManagerService()
							.microDialogDelete(selectedMicroDialog);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				val tabSheet = getMicroDialogsTabSheet();

				tabsWithObjectIdsOfMicroDialog
						.remove(tabSheet.getTab(tabSheet.getSelectedTab()));

				tabSheet.removeTab(tabSheet.getTab(tabSheet.getSelectedTab()));

				val selectedTab = tabSheet.getSelectedTab();
				if (selectedTab == null) {
					setNothingSelected();
					selectedMicroDialog = null;
				} else {
					val selectedTabObject = tabSheet.getTab(selectedTab);
					val microDialogObjectId = tabsWithObjectIdsOfMicroDialog
							.get(selectedTabObject);
					selectedMicroDialog = getInterventionAdministrationManagerService()
							.getMicroDialog(microDialogObjectId);

					setSomethingSelected();
				}

				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_DELETED);

				closeWindow();
			}
		}, null);
	}
}
