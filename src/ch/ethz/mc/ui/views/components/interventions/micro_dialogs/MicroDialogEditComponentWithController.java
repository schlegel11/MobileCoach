package ch.ethz.mc.ui.views.components.interventions.micro_dialogs;

import java.io.File;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.model.persistent.MicroDialogDecisionPoint;
import ch.ethz.mc.model.persistent.MicroDialogMessage;
import ch.ethz.mc.model.persistent.concepts.MicroDialogElementInterface;
import ch.ethz.mc.model.ui.UIMicroDialogElementInterface;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Provides the micro dialog edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MicroDialogEditComponentWithController
		extends MicroDialogEditComponent {

	private final MicroDialog											microDialog;

	private final ObjectId												interventionId;

	private UIMicroDialogElementInterface								selectedUIMicroDialogElement	= null;

	private final BeanContainer<ObjectId, UIMicroDialogElementInterface>	beanContainer;

	protected MicroDialogEditComponentWithController(
			final MicroDialog microDialog, final ObjectId interventionId) {
		super();

		this.microDialog = microDialog;
		this.interventionId = interventionId;

		// table options
		val microDialogElementsTable = getMicroDialogElementsTable();

		// table content
		val elementsOfMicroDialog = getInterventionAdministrationManagerService()
				.getAllMicroDialogElementsOfMicroDialog(microDialog.getId());

		beanContainer = createBeanContainerForModelObjects(
				UIMicroDialogElementInterface.class, elementsOfMicroDialog);

		microDialogElementsTable.setContainerDataSource(beanContainer);
		microDialogElementsTable.setSortContainerPropertyId(
				UIMicroDialogElementInterface.getSortColumn());
		microDialogElementsTable.setVisibleColumns(
				UIMicroDialogElementInterface.getVisibleColumns());
		microDialogElementsTable.setColumnHeaders(
				UIMicroDialogElementInterface.getColumnHeaders());
		microDialogElementsTable.setSortAscending(true);
		microDialogElementsTable.setSortEnabled(false);

		// handle table selection change
		microDialogElementsTable
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						val objectId = microDialogElementsTable.getValue();
						if (objectId == null) {
							setNothingSelected();
							selectedUIMicroDialogElement = null;
						} else {
							selectedUIMicroDialogElement = getUIModelObjectFromTableByObjectId(
									microDialogElementsTable,
									UIMicroDialogElementInterface.class,
									objectId);
							setSomethingSelected();
						}
					}
				});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewMessageButton().addClickListener(buttonClickListener);
		getNewDecisionPointButton().addClickListener(buttonClickListener);
		getEditButton().addClickListener(buttonClickListener);
		getDuplicateButton().addClickListener(buttonClickListener);
		getMoveUpButton().addClickListener(buttonClickListener);
		getMoveDownButton().addClickListener(buttonClickListener);
		getDeleteButton().addClickListener(buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getNewMessageButton()) {
				createMessage();
			} else if (event.getButton() == getNewDecisionPointButton()) {
				createDecisionPoint();
			} else if (event.getButton() == getEditButton()) {
				editElement();
			} else if (event.getButton() == getDuplicateButton()) {
				duplicateElement();
			} else if (event.getButton() == getMoveUpButton()) {
				moveElement(true);
			} else if (event.getButton() == getMoveDownButton()) {
				moveElement(false);
			} else if (event.getButton() == getDeleteButton()) {
				deleteElement();
			}
			event.getButton().setEnabled(true);
		}
	}

	public void createMessage() {
		log.debug("Create message");
		val newMicroDialogMessage = getInterventionAdministrationManagerService()
				.microDialogMessageCreate(microDialog.getId());

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_MICRO_DIALOG_MESSAGE,
				new MicroDialogMessageEditComponentWithController(
						newMicroDialogMessage, interventionId),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						beanContainer.addItem(newMicroDialogMessage.getId(),
								UIMicroDialogElementInterface.class
										.cast(newMicroDialogMessage
												.toUIModelObject()));
						getMicroDialogElementsTable()
								.select(newMicroDialogMessage.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_MESSAGE_CREATED);

						closeWindow();
					}
				});
	}

	public void createDecisionPoint() {
		log.debug("Create decision point");
		val newMicroDialogDecisionPoint = getInterventionAdministrationManagerService()
				.microDialogDecisionPointCreate(microDialog.getId());

		// showModalClosableEditWindow(
		// AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_MICRO_DIALOG_DECISION_POINT,
		// new MicroDialogDecisionPointEditComponentWithController(
		// newMicroDialogDecisionPoint, interventionId),
		// new ExtendableButtonClickListener() {
		// @Override
		// public void buttonClick(final ClickEvent event) {
		// Adapt UI
		beanContainer.addItem(newMicroDialogDecisionPoint.getId(),
				UIMicroDialogElementInterface.class
						.cast(newMicroDialogDecisionPoint.toUIModelObject()));
		getMicroDialogElementsTable()
				.select(newMicroDialogDecisionPoint.getId());
		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_DECISION_POINT_CREATED);

		// closeWindow();
		// }
		// });
	}

	public void editElement() {
		log.debug("Edit element");

		if (selectedUIMicroDialogElement.isMessage()) {
			val selectedMicroDialogMessage = selectedUIMicroDialogElement
					.getRelatedModelObject(MicroDialogMessage.class);

			showModalClosableEditWindow(
					AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_MICRO_DIALOG_MESSAGE,
					new MicroDialogMessageEditComponentWithController(
							selectedMicroDialogMessage, interventionId),
					new ExtendableButtonClickListener() {
						@Override
						public void buttonClick(final ClickEvent event) {
							// Adapt UI
							removeAndAddModelObjectToBeanContainer(
									beanContainer, selectedMicroDialogMessage);
							getMicroDialogElementsTable().sort();
							getMicroDialogElementsTable()
									.select(selectedMicroDialogMessage.getId());
							getAdminUI().showInformationNotification(
									AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_MESSAGE_UPDATED);

							closeWindow();
						}
					});
		} else {
			val selectedMicroDialogDecisionPoint = selectedUIMicroDialogElement
					.getRelatedModelObject(MicroDialogMessage.class);

			// showModalClosableEditWindow(
			// AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_MICRO_DIALOG_DECISION_POINT,
			// new MicroDialogDecisionPointEditComponentWithController(
			// selectedMicroDialogDecisionPoint, interventionId),
			// new ExtendableButtonClickListener() {
			// @Override
			// public void buttonClick(final ClickEvent event) {
			// Adapt UI
			removeAndAddModelObjectToBeanContainer(beanContainer,
					selectedMicroDialogDecisionPoint);
			getMicroDialogElementsTable().sort();
			getMicroDialogElementsTable()
					.select(selectedMicroDialogDecisionPoint.getId());
			getAdminUI().showInformationNotification(
					AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_DECISION_POINT_UPDATED);

			// closeWindow();
			// }
			// });
		}
	}

	public void duplicateElement() {
		log.debug("Duplicate element");

		File temporaryBackupFile = null;
		try {
			ModelObject importedMicroDialogElement;
			if (selectedUIMicroDialogElement.isMessage()) {
				temporaryBackupFile = getInterventionAdministrationManagerService()
						.microDialogMessageExport(selectedUIMicroDialogElement
								.getRelatedModelObject(
										MicroDialogMessage.class));

				importedMicroDialogElement = getInterventionAdministrationManagerService()
						.microDialogMessageImport(temporaryBackupFile);
			} else {
				temporaryBackupFile = getInterventionAdministrationManagerService()
						.microDialogDecisionPointExport(
								selectedUIMicroDialogElement
										.getRelatedModelObject(
												MicroDialogDecisionPoint.class));

				importedMicroDialogElement = getInterventionAdministrationManagerService()
						.microDialogDecisionPointImport(temporaryBackupFile);
			}

			if (importedMicroDialogElement == null) {
				throw new Exception("Imported element not found in import");
			}

			// Adapt UI
			beanContainer.addItem(importedMicroDialogElement.getId(),
					UIMicroDialogElementInterface.class.cast(
							importedMicroDialogElement.toUIModelObject()));
			getMicroDialogElementsTable()
					.select(importedMicroDialogElement.getId());

			if (selectedUIMicroDialogElement.isMessage()) {
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_MESSAGE_DUPLICATED);
			} else {
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_DECISION_POINT_DUPLICATED);
			}
		} catch (final Exception e) {
			if (selectedUIMicroDialogElement.isMessage()) {
				getAdminUI().showWarningNotification(
						AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_MESSAGE_DUPLICATION_FAILED);
			} else {
				getAdminUI().showWarningNotification(
						AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_DECISION_POINT_DUPLICATION_FAILD);
			}
		}

		try {
			temporaryBackupFile.delete();
		} catch (final Exception f) {
			// Do nothing
		}
	}

	public void moveElement(final boolean moveUp) {
		log.debug("Move element {}", moveUp ? "up" : "down");

		MicroDialogElementInterface selectedMicroDialogElement;
		if (selectedUIMicroDialogElement.isMessage()) {
			selectedMicroDialogElement = selectedUIMicroDialogElement
					.getRelatedModelObject(MicroDialogMessage.class);
		} else {
			selectedMicroDialogElement = selectedUIMicroDialogElement
					.getRelatedModelObject(MicroDialogDecisionPoint.class);
		}

		val swappedMicroDialogElement = getInterventionAdministrationManagerService()
				.microDialogElementMove(selectedMicroDialogElement, moveUp);
		if (swappedMicroDialogElement == null) {
			log.debug("Element is already at top/end of list");
			return;
		}

		removeAndAddModelObjectToBeanContainer(beanContainer,
				(ModelObject) swappedMicroDialogElement);
		removeAndAddModelObjectToBeanContainer(beanContainer,
				(ModelObject) selectedMicroDialogElement);
		getMicroDialogElementsTable().sort();
		getMicroDialogElementsTable()
				.select(((ModelObject) selectedMicroDialogElement).getId());
	}

	public void deleteElement() {
		log.debug("Delete element");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				if (selectedUIMicroDialogElement.isMessage()) {
					try {
						val selectedMicroDialogMessage = selectedUIMicroDialogElement
								.getRelatedModelObject(
										MicroDialogMessage.class);

						// Delete message
						getInterventionAdministrationManagerService()
								.microDialogMessageDelete(
										selectedMicroDialogMessage);
					} catch (final Exception e) {
						closeWindow();
						handleException(e);
						return;
					}

					// Adapt UI
					getMicroDialogElementsTable()
							.removeItem(
									selectedUIMicroDialogElement
											.getRelatedModelObject(
													MicroDialogMessage.class)
											.getId());
					getAdminUI().showInformationNotification(
							AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_MESSAGE_DELETED);
				} else {
					try {
						val selectedMicroDialogDecisionPoint = selectedUIMicroDialogElement
								.getRelatedModelObject(
										MicroDialogDecisionPoint.class);

						// Delete decision point
						getInterventionAdministrationManagerService()
								.microDialogDecisionPointDelete(
										selectedMicroDialogDecisionPoint);
					} catch (final Exception e) {
						closeWindow();
						handleException(e);
						return;
					}

					// Adapt UI
					getMicroDialogElementsTable()
							.removeItem(selectedUIMicroDialogElement
									.getRelatedModelObject(
											MicroDialogDecisionPoint.class)
									.getId());
					getAdminUI().showInformationNotification(
							AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_DECISION_POINT_DELETED);
				}
				closeWindow();
			}
		}, null);
	}

}
