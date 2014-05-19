package org.isgf.mhc.ui.views.components.interventions;

import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.ui.UIParticipant;
import org.isgf.mhc.ui.views.components.AbstractCustomComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the intervention participants edit component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class InterventionParticipantsEditComponent extends
		AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout		mainLayout;
	@AutoGenerated
	private HorizontalLayout	buttonLayout;
	@AutoGenerated
	private Button				refreshButton;
	@AutoGenerated
	private Button				deleteButton;
	@AutoGenerated
	private Button				sendMessageButton;
	@AutoGenerated
	private Button				switchMessagingButton;
	@AutoGenerated
	private Button				assignUnitButton;
	@AutoGenerated
	private Button				assignOrganizationButton;
	@AutoGenerated
	private Button				exportButton;
	@AutoGenerated
	private Button				importButton;
	@AutoGenerated
	private Table				participantsTable;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected InterventionParticipantsEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(importButton, AdminMessageStrings.GENERAL__IMPORT);
		localize(exportButton, AdminMessageStrings.GENERAL__EXPORT);
		localize(
				assignOrganizationButton,
				AdminMessageStrings.INTERVENTION_PARTICIPANTS_EDITING__ASSIGN_ORGANIZATION);
		localize(
				assignUnitButton,
				AdminMessageStrings.INTERVENTION_PARTICIPANTS_EDITING__ASSIGN_UNIT);
		localize(
				switchMessagingButton,
				AdminMessageStrings.INTERVENTION_PARTICIPANTS_EDITING__SWITCH_MONITORING);
		localize(
				sendMessageButton,
				AdminMessageStrings.INTERVENTION_PARTICIPANTS_EDITING__SEND_MESSAGE);
		localize(deleteButton, AdminMessageStrings.GENERAL__DELETE);
		localize(refreshButton, AdminMessageStrings.GENERAL__REFRESH);

		// set button start state
		setNothingSelected();

		// set table formatter
		participantsTable.setCellStyleGenerator(new Table.CellStyleGenerator() {
			@Override
			public String getStyle(final Table source, final Object itemId,
					final Object propertyId) {
				if (propertyId != null) {
					if (propertyId.equals(UIParticipant.MONITORING_STATUS)) {
						val uiParticipant = getUIModelObjectFromTableByObjectId(
								source, UIParticipant.class, itemId);
						if (uiParticipant.isBooleanMonitoringStatus()) {
							return "active";
						} else {
							return "inactive";
						}
					} else if (propertyId
							.equals(UIParticipant.SCREENING_SURVEY_STATUS)) {
						val uiParticipant = getUIModelObjectFromTableByObjectId(
								source, UIParticipant.class, itemId);
						if (uiParticipant.isBooleanScreeningSurveyStatus()) {
							return "active";
						} else {
							return "inactive";
						}
					} else if (propertyId
							.equals(UIParticipant.DATA_FOR_MONITORING_AVAILABLE)) {
						val uiParticipant = getUIModelObjectFromTableByObjectId(
								source, UIParticipant.class, itemId);
						if (uiParticipant.isBooleanDataForMonitoringAvailable()) {
							return "active";
						} else {
							return "inactive";
						}
					} else if (propertyId
							.equals(UIParticipant.INTERVENTION_STATUS)) {
						val uiParticipant = getUIModelObjectFromTableByObjectId(
								source, UIParticipant.class, itemId);
						if (uiParticipant.isBooleanInterventionStatus()) {
							return "active";
						} else {
							return "inactive";
						}
					}
				}

				return null;
			}
		});
	}

	public void updateButtonStatus(
			final Collection<ObjectId> selectedUIParticipantsIds,
			final boolean interventionMonitoringActive,
			final boolean oneScreeningSurveyActive) {
		// Import button
		if (interventionMonitoringActive || oneScreeningSurveyActive) {
			importButton.setEnabled(false);
		} else {
			importButton.setEnabled(true);
		}

		// All other buttons
		if (selectedUIParticipantsIds == null
				|| selectedUIParticipantsIds.size() == 0) {
			setNothingSelected();
		} else {
			if (!oneScreeningSurveyActive && interventionMonitoringActive) {
				sendMessageButton.setEnabled(true);
			} else {
				sendMessageButton.setEnabled(false);
			}
			if (!oneScreeningSurveyActive && !interventionMonitoringActive) {
				exportButton.setEnabled(true);
				assignOrganizationButton.setEnabled(true);
				assignUnitButton.setEnabled(true);
				deleteButton.setEnabled(true);
			}
			if (oneScreeningSurveyActive) {
				switchMessagingButton.setEnabled(false);
			} else {
				switchMessagingButton.setEnabled(true);
			}
		}
	}

	protected void setNothingSelected() {
		exportButton.setEnabled(false);
		assignOrganizationButton.setEnabled(false);
		assignUnitButton.setEnabled(false);
		switchMessagingButton.setEnabled(false);
		sendMessageButton.setEnabled(false);
		deleteButton.setEnabled(false);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// participantsTable
		participantsTable = new Table();
		participantsTable.setImmediate(false);
		participantsTable.setWidth("100.0%");
		participantsTable.setHeight("150px");
		mainLayout.addComponent(participantsTable);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

		return mainLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildButtonLayout() {
		// common part: create layout
		buttonLayout = new HorizontalLayout();
		buttonLayout.setImmediate(false);
		buttonLayout.setWidth("-1px");
		buttonLayout.setHeight("-1px");
		buttonLayout.setMargin(false);
		buttonLayout.setSpacing(true);

		// importButton
		importButton = new Button();
		importButton.setCaption("!!! Import");
		importButton.setImmediate(true);
		importButton.setWidth("100px");
		importButton.setHeight("-1px");
		buttonLayout.addComponent(importButton);

		// exportButton
		exportButton = new Button();
		exportButton.setCaption("!!! Export");
		exportButton.setImmediate(true);
		exportButton.setWidth("100px");
		exportButton.setHeight("-1px");
		buttonLayout.addComponent(exportButton);

		// assignOrganizationButton
		assignOrganizationButton = new Button();
		assignOrganizationButton.setCaption("!!! Assign Organization");
		assignOrganizationButton.setImmediate(true);
		assignOrganizationButton.setWidth("150px");
		assignOrganizationButton.setHeight("-1px");
		buttonLayout.addComponent(assignOrganizationButton);

		// assignUnitButton
		assignUnitButton = new Button();
		assignUnitButton.setCaption("!!! Assign Unit");
		assignUnitButton.setImmediate(true);
		assignUnitButton.setWidth("100px");
		assignUnitButton.setHeight("-1px");
		buttonLayout.addComponent(assignUnitButton);

		// switchMessagingButton
		switchMessagingButton = new Button();
		switchMessagingButton.setCaption("!!! Switch Messaging");
		switchMessagingButton.setImmediate(true);
		switchMessagingButton.setWidth("150px");
		switchMessagingButton.setHeight("-1px");
		buttonLayout.addComponent(switchMessagingButton);

		// sendMessageButton
		sendMessageButton = new Button();
		sendMessageButton.setCaption("!!! Send Message");
		sendMessageButton.setIcon(new ThemeResource(
				"img/message-icon-small.png"));
		sendMessageButton.setImmediate(true);
		sendMessageButton.setWidth("150px");
		sendMessageButton.setHeight("-1px");
		buttonLayout.addComponent(sendMessageButton);

		// deleteButton
		deleteButton = new Button();
		deleteButton.setCaption("!!! Delete");
		deleteButton.setIcon(new ThemeResource("img/delete-icon-small.png"));
		deleteButton.setImmediate(true);
		deleteButton.setWidth("100px");
		deleteButton.setHeight("-1px");
		buttonLayout.addComponent(deleteButton);

		// refreshButton
		refreshButton = new Button();
		refreshButton.setCaption("!!! Refresh");
		refreshButton.setIcon(new ThemeResource("img/loading-icon-small.png"));
		refreshButton.setImmediate(true);
		refreshButton.setWidth("100px");
		refreshButton.setHeight("-1px");
		buttonLayout.addComponent(refreshButton);

		return buttonLayout;
	}
}
