package ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs;
/* ##LICENSE## */

import java.io.File;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

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

	private final Intervention intervention;

	public MicroDialogsTabComponentWithController(
			final Intervention intervention) {
		super();

		this.intervention = intervention;

		// Retrieve monitoring message groups to set current and fill tabs
		final Iterable<MicroDialog> microDialogsIterable = getInterventionAdministrationManagerService()
				.getAllMicroDialogsOfIntervention(intervention.getId());

		for (val microDialog : microDialogsIterable) {
			val newTab = addTabComponent(microDialog, intervention);

			if (getMicroDialogsTabSheet().getComponentCount() == 1) {
				// First tab added
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
						} else {
							refreshRelatedMicroDialog();
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

	private MicroDialog getRelatedMicroDialog() {
		val tabSheet = getMicroDialogsTabSheet();
		val component = (MicroDialogEditComponentWithController) tabSheet
				.getSelectedTab();

		return component.getMicroDialog();
	}

	private void refreshRelatedMicroDialog() {
		val tabSheet = getMicroDialogsTabSheet();
		val component = (MicroDialogEditComponentWithController) tabSheet
				.getSelectedTab();

		component.setMicroDialog(getInterventionAdministrationManagerService()
				.getMicroDialog(component.getMicroDialog().getId()));
		component.adjustCopyPasteButtons();
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

						getMicroDialogsTabSheet().setSelectedTab(newTab);
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void moveDialog(final boolean moveLeft) {
		log.debug("Move dialog {}", moveLeft ? "left" : "right");

		val microDialog = getRelatedMicroDialog();

		val swappedMicroDialog = getInterventionAdministrationManagerService()
				.microDialogMove(microDialog, moveLeft);

		if (!swappedMicroDialog) {
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

		refreshRelatedMicroDialog();
		setSomethingSelected();
	}

	public void renameDialog() {
		log.debug("Rename dialog");

		val microDialog = getRelatedMicroDialog();

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_MICRO_DIALOG,
				microDialog.getName(), null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change name
							getInterventionAdministrationManagerService()
									.microDialogSetName(microDialog,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						val tab = getMicroDialogsTabSheet();
						tab.getTab(tab.getSelectedTab())
								.setCaption(microDialog.getName());
						refreshRelatedMicroDialog();

						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_RENAMED);
						closeWindow();
					}
				}, null);
	}

	public void duplicateDialog() {
		log.debug("Duplicate dialog");

		val microDialog = getRelatedMicroDialog();

		File temporaryBackupFile = null;
		try {
			temporaryBackupFile = getInterventionAdministrationManagerService()
					.microDialogExport(microDialog);

			final MicroDialog importedMicroDialog = getInterventionAdministrationManagerService()
					.microDialogImport(temporaryBackupFile, true);

			if (importedMicroDialog == null) {
				throw new Exception("Imported element not found in import");
			}

			// Adapt UI
			val newTab = addTabComponent(importedMicroDialog, intervention);

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

		val microDialog = getRelatedMicroDialog();

		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					// Delete dialog
					getInterventionAdministrationManagerService()
							.microDialogDelete(microDialog);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				val tabSheet = getMicroDialogsTabSheet();

				tabSheet.removeTab(tabSheet.getTab(tabSheet.getSelectedTab()));

				val selectedTab = tabSheet.getSelectedTab();
				if (selectedTab == null) {
					setNothingSelected();
				} else {
					refreshRelatedMicroDialog();
					setSomethingSelected();
				}

				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_DELETED);

				closeWindow();
			}
		}, null);
	}
}
