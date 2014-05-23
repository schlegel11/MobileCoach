package org.isgf.mhc.ui.views.components.interventions;

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
import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.Participant;
import org.isgf.mhc.model.persistent.concepts.AbstractVariableWithValue;
import org.isgf.mhc.model.ui.UIDialogMessageWithParticipant;
import org.isgf.mhc.model.ui.UIParticipant;
import org.isgf.mhc.model.ui.UIVariableWithParticipant;
import org.isgf.mhc.tools.CSVExporter;
import org.isgf.mhc.tools.OnDemandFileDownloader;
import org.isgf.mhc.tools.OnDemandFileDownloader.OnDemandStreamResource;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
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

	private final Intervention												intervention;

	private Collection<ObjectId>											selectedUIParticipantsIds;
	private UIVariableWithParticipant										selectedUIVariableWithParticipant			= null;
	private BeanItem<UIVariableWithParticipant>								selectedUIVariableWithParticipantBeanItem	= null;

	private final BeanContainer<ObjectId, UIParticipant>					beanContainer;

	private final BeanContainer<Integer, UIVariableWithParticipant>			variablesBeanContainer;
	private final BeanContainer<Integer, UIDialogMessageWithParticipant>	messageDialogBeanContainer;

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

		variablesBeanContainer = new BeanContainer<Integer, UIVariableWithParticipant>(
				UIVariableWithParticipant.class);

		variablesTable.setContainerDataSource(variablesBeanContainer);
		variablesTable.setSortContainerPropertyId(UIVariableWithParticipant
				.getSortColumn());
		variablesTable.setVisibleColumns(UIVariableWithParticipant
				.getVisibleColumns());
		variablesTable.setColumnHeaders(UIVariableWithParticipant
				.getColumnHeaders());

		messageDialogBeanContainer = new BeanContainer<Integer, UIDialogMessageWithParticipant>(
				UIDialogMessageWithParticipant.class);

		messageDialogTable.setContainerDataSource(messageDialogBeanContainer);
		messageDialogTable
				.setSortContainerPropertyId(UIDialogMessageWithParticipant
						.getSortColumn());
		messageDialogTable.setVisibleColumns(UIDialogMessageWithParticipant
				.getVisibleColumns());
		messageDialogTable.setColumnHeaders(UIDialogMessageWithParticipant
				.getColumnHeaders());

		// handle selection change
		participantsTable.addValueChangeListener(new ValueChangeListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(final ValueChangeEvent event) {
				selectedUIParticipantsIds = (Collection<ObjectId>) participantsTable
						.getValue();
				updateButtonStatus(selectedUIParticipantsIds);
				updateTables();
			}
		});

		variablesTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = variablesTable.getValue();
				if (objectId == null) {
					selectedUIVariableWithParticipant = null;
					selectedUIVariableWithParticipantBeanItem = null;
					setVariableWithParticipantSelected(false);
				} else {
					selectedUIVariableWithParticipant = getUIModelObjectFromTableByObjectId(
							variablesTable, UIVariableWithParticipant.class,
							objectId);
					selectedUIVariableWithParticipantBeanItem = getBeanItemFromTableByObjectId(
							variablesTable, UIVariableWithParticipant.class,
							objectId);
					setVariableWithParticipantSelected(true);
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getRefreshButton().addClickListener(buttonClickListener);
		getEditButton().addClickListener(buttonClickListener);

		// Special handle for export buttons
		val variablesExportOnDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					public InputStream getStream() {
						final List<UIVariableWithParticipant> items = new ArrayList<UIVariableWithParticipant>();

						for (val itemId : variablesBeanContainer.getItemIds()) {
							items.add(variablesBeanContainer.getItem(itemId)
									.getBean());
						}

						try {
							return CSVExporter
									.convertUIParticipantVariableToCSV(items);
						} catch (final IOException e) {
							log.error("Error at creating CSV: {}",
									e.getMessage());
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

		val messageDialogExportOnDemandFileDownloader = new OnDemandFileDownloader(
				new OnDemandStreamResource() {

					@Override
					public InputStream getStream() {
						final List<UIDialogMessageWithParticipant> items = new ArrayList<UIDialogMessageWithParticipant>();

						for (val itemId : messageDialogBeanContainer
								.getItemIds()) {
							items.add(messageDialogBeanContainer
									.getItem(itemId).getBean());
						}

						try {
							return CSVExporter
									.convertUIDialogMessageToCSV(items);
						} catch (final IOException e) {
							log.error("Error at creating CSV: {}",
									e.getMessage());
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

		adjust();
	}

	private void adjust() {
		updateButtonStatus(selectedUIParticipantsIds);

		val participantsTable = getParticipantsTable();

		log.debug("Update participants");
		refreshBeanContainer(beanContainer, UIParticipant.class,
				getInterventionAdministrationManagerService()
						.getAllParticipantsOfIntervention(intervention.getId()));

		participantsTable.sort();
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {

			if (event.getButton() == getRefreshButton()) {
				adjust();
			} else if (event.getButton() == getEditButton()) {
				editVariableValue();
			}
		}
	}

	private void updateTables() {
		log.debug("Update variables of participant");

		getVariablesTable().select(null);

		variablesBeanContainer.removeAllItems();

		int i = 0;
		for (val participantId : selectedUIParticipantsIds) {
			val participant = getInterventionAdministrationManagerService()
					.getParticipant(participantId);

			val variablesOfParticipant = getInterventionAdministrationManagerService()
					.getAllVariablesWithValuesOfParticipantAndSystem(
							participantId);

			for (val variableOfParticipant : variablesOfParticipant.values()) {
				variablesBeanContainer
						.addItem(
								i++,
								variableOfParticipant
										.toUIVariableWithParticipant(
												participant.getId().toString(),
												participant.getNickname()
														.equals("") ? Messages
														.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
														: participant
																.getNickname()));
			}
		}

		getVariablesTable().sort();

		log.debug("Update message dialog of participant");
		messageDialogBeanContainer.removeAllItems();

		i = 0;
		for (val participantId : selectedUIParticipantsIds) {
			val participant = getInterventionAdministrationManagerService()
					.getParticipant(participantId);

			val dialogMessagesOfParticipant = getInterventionAdministrationManagerService()
					.getAllDialogMessagesOfParticipant(participantId);

			for (val dialogMessageOfParticipant : dialogMessagesOfParticipant) {
				messageDialogBeanContainer
						.addItem(
								i++,
								dialogMessageOfParticipant
										.toUIDialogMessageWithParticipant(
												participant.getId().toString(),
												participant.getNickname()
														.equals("") ? Messages
														.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
														: participant
																.getNickname(),
												participant.getOrganization()
														.equals("") ? Messages
														.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
														: participant
																.getOrganization(),
												participant
														.getOrganizationUnit()
														.equals("") ? Messages
														.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
														: participant
																.getOrganizationUnit()));
			}
		}

		getVariablesTable().sort();
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
						val changeSuceeded = MHC
								.getInstance()
								.getInterventionExecutionManagerService()
								.participantAdjustVariableValue(
										new ObjectId(
												selectedUIVariableWithParticipant
														.getParticipantId()),
										abstractVariableWithValue.getName(),
										getStringValue());

						if (changeSuceeded) {
							getStringItemProperty(
									selectedUIVariableWithParticipantBeanItem,
									UIVariableWithParticipant.VALUE).setValue(
									getStringValue());

							selectedUIVariableWithParticipant
									.setValue(getStringValue());

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
