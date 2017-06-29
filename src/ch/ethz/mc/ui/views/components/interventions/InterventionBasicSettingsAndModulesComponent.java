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
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ThemeImageStrings;
import ch.ethz.mc.ui.views.components.AbstractCustomComponent;
import ch.ethz.mc.ui.views.components.simulator.SimulatorComponentWithController;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the XYZ component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class InterventionBasicSettingsAndModulesComponent extends
		AbstractCustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout						mainLayout;
	@AutoGenerated
	private SimulatorComponentWithController	simulatorComponent;
	@AutoGenerated
	private GridLayout							gridLayout_2;
	@AutoGenerated
	private ListSelect							uniquenessList;
	@AutoGenerated
	private Label								uniquenessLabel;
	@AutoGenerated
	private HorizontalLayout					horizontalLayout_1;
	@AutoGenerated
	private CheckBox							sundayCheckbox;
	@AutoGenerated
	private CheckBox							saturdayCheckbox;
	@AutoGenerated
	private CheckBox							fridayCheckbox;
	@AutoGenerated
	private CheckBox							thursdayCheckbox;
	@AutoGenerated
	private CheckBox							wednesdayCheckbox;
	@AutoGenerated
	private CheckBox							tuesdayCheckbox;
	@AutoGenerated
	private CheckBox							mondayCheckbox;
	@AutoGenerated
	private Label								monitoringStartingDaysLabel;
	@AutoGenerated
	private GridLayout							gridLayout_1;
	@AutoGenerated
	private CheckBox							finishScreeningSurveysCheckbox;
	@AutoGenerated
	private ComboBox							senderIdentificationSelectionComboBox;
	@AutoGenerated
	private Label								senderIdentificationSelectionLabel;
	@AutoGenerated
	private Button								switchMessagingButton;
	@AutoGenerated
	private Button								switchInterventionButton;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected InterventionBasicSettingsAndModulesComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(
				switchInterventionButton,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_INTERVENTION_BUTTON_INACTIVE);
		localize(
				switchMessagingButton,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_MONITORING_BUTTON_INACTIVE);
		localize(
				senderIdentificationSelectionLabel,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SENDER_IDENTIFICATION_SELECTION_LABEL);
		localize(
				finishScreeningSurveysCheckbox,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__AUTOMATICALLY_FINISH_SCREENING_SURVEYS_CHECKBOX);
		localize(
				monitoringStartingDaysLabel,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__MONITORING_STARTING_DAYS_LABEL);
		localize(
				mondayCheckbox,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__MONDAY_CHECKBOX);
		localize(
				tuesdayCheckbox,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__TUESDAY_CHECKBOX);
		localize(
				wednesdayCheckbox,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__WEDNESDAY_CHECKBOX);
		localize(
				thursdayCheckbox,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__THURSDAY_CHECKBOX);
		localize(
				fridayCheckbox,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__FRIDAY_CHECKBOX);
		localize(
				saturdayCheckbox,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SATURDAY_CHECKBOX);
		localize(
				sundayCheckbox,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SUNDAY_CHECKBOX);
		localize(
				uniquenessLabel,
				AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__PARTICIPANT_UNIQUENESS_LABEL);

		// Adjust combo boxes
		senderIdentificationSelectionComboBox.setImmediate(true);
		senderIdentificationSelectionComboBox.setNullSelectionAllowed(true);
		senderIdentificationSelectionComboBox.setTextInputAllowed(false);

		// Adjust checkboxes
		finishScreeningSurveysCheckbox.setImmediate(true);

		mondayCheckbox.setImmediate(true);
		tuesdayCheckbox.setImmediate(true);
		wednesdayCheckbox.setImmediate(true);
		thursdayCheckbox.setImmediate(true);
		fridayCheckbox.setImmediate(true);
		saturdayCheckbox.setImmediate(true);
		sundayCheckbox.setImmediate(true);

		// Adjust list
		uniquenessList.setImmediate(true);
		uniquenessList.setMultiSelect(true);

		// Deactivate simulator if not set
		if (!Constants.isSimulatedDateAndTime()) {
			simulatorComponent.setVisible(false);
		}

		// set start settings
		adjust(false, false);
	}

	protected void adjust(final boolean interventionStatus,
			final boolean messagingStatus) {
		// Adjust intervention status
		if (interventionStatus) {
			switchInterventionButton.setIcon(new ThemeResource(
					ThemeImageStrings.ACTIVE_ICON_SMALL));
			localize(
					switchInterventionButton,
					AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_INTERVENTION_BUTTON_ACTIVE);

			switchMessagingButton.setEnabled(true);
		} else {
			switchInterventionButton.setIcon(new ThemeResource(
					ThemeImageStrings.INACTIVE_ICON_SMALL));
			localize(
					switchInterventionButton,
					AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_INTERVENTION_BUTTON_INACTIVE);

			switchMessagingButton.setEnabled(false);
		}

		// Adjust messaging status
		if (messagingStatus) {
			switchMessagingButton.setIcon(new ThemeResource(
					ThemeImageStrings.ACTIVE_ICON_SMALL));
			localize(
					switchMessagingButton,
					AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_MONITORING_BUTTON_ACTIVE);

			switchInterventionButton.setEnabled(false);
		} else {
			switchMessagingButton.setIcon(new ThemeResource(
					ThemeImageStrings.INACTIVE_ICON_SMALL));
			localize(
					switchMessagingButton,
					AdminMessageStrings.INTERVENTION_BASIC_SETTINGS_TAB__SWITCH_MONITORING_BUTTON_INACTIVE);

			switchInterventionButton.setEnabled(true);
		}

		// Adjust other options based on intervention status
		if (interventionStatus) {
			senderIdentificationSelectionComboBox.setEnabled(false);

			finishScreeningSurveysCheckbox.setEnabled(false);

			mondayCheckbox.setEnabled(false);
			tuesdayCheckbox.setEnabled(false);
			wednesdayCheckbox.setEnabled(false);
			thursdayCheckbox.setEnabled(false);
			fridayCheckbox.setEnabled(false);
			saturdayCheckbox.setEnabled(false);
			sundayCheckbox.setEnabled(false);

			uniquenessList.setEnabled(false);
		} else {
			senderIdentificationSelectionComboBox.setEnabled(true);

			finishScreeningSurveysCheckbox.setEnabled(true);

			mondayCheckbox.setEnabled(true);
			tuesdayCheckbox.setEnabled(true);
			wednesdayCheckbox.setEnabled(true);
			thursdayCheckbox.setEnabled(true);
			fridayCheckbox.setEnabled(true);
			saturdayCheckbox.setEnabled(true);
			sundayCheckbox.setEnabled(true);

			uniquenessList.setEnabled(true);
		}
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

		// switchInterventionButton
		switchInterventionButton = new Button();
		switchInterventionButton.setCaption("!!! Activate Intervention");
		switchInterventionButton.setImmediate(true);
		switchInterventionButton.setWidth("100.0%");
		switchInterventionButton.setHeight("-1px");
		mainLayout.addComponent(switchInterventionButton);

		// switchMessagingButton
		switchMessagingButton = new Button();
		switchMessagingButton.setCaption("!!! Activate Messaging");
		switchMessagingButton.setImmediate(true);
		switchMessagingButton.setWidth("100.0%");
		switchMessagingButton.setHeight("-1px");
		mainLayout.addComponent(switchMessagingButton);

		// gridLayout_1
		gridLayout_1 = buildGridLayout_1();
		mainLayout.addComponent(gridLayout_1);

		// monitoringStartingDaysLabel
		monitoringStartingDaysLabel = new Label();
		monitoringStartingDaysLabel.setImmediate(false);
		monitoringStartingDaysLabel.setWidth("-1px");
		monitoringStartingDaysLabel.setHeight("-1px");
		monitoringStartingDaysLabel.setValue("!! Monitoring starting days:");
		mainLayout.addComponent(monitoringStartingDaysLabel);

		// horizontalLayout_1
		horizontalLayout_1 = buildHorizontalLayout_1();
		mainLayout.addComponent(horizontalLayout_1);

		// gridLayout_2
		gridLayout_2 = buildGridLayout_2();
		mainLayout.addComponent(gridLayout_2);

		// simulatorComponent
		simulatorComponent = new SimulatorComponentWithController();
		simulatorComponent.setImmediate(false);
		simulatorComponent.setWidth("100.0%");
		simulatorComponent.setHeight("-1px");
		mainLayout.addComponent(simulatorComponent);

		return mainLayout;
	}

	@AutoGenerated
	private GridLayout buildGridLayout_1() {
		// common part: create layout
		gridLayout_1 = new GridLayout();
		gridLayout_1.setImmediate(false);
		gridLayout_1.setWidth("100.0%");
		gridLayout_1.setHeight("-1px");
		gridLayout_1.setMargin(false);
		gridLayout_1.setSpacing(true);
		gridLayout_1.setColumns(2);
		gridLayout_1.setRows(2);

		// senderIdentificationSelectionLabel
		senderIdentificationSelectionLabel = new Label();
		senderIdentificationSelectionLabel.setImmediate(false);
		senderIdentificationSelectionLabel.setWidth("-1px");
		senderIdentificationSelectionLabel.setHeight("-1px");
		senderIdentificationSelectionLabel
				.setValue("!! Sender identification selection:");
		gridLayout_1.addComponent(senderIdentificationSelectionLabel, 0, 0);

		// senderIdentificationSelectionComboBox
		senderIdentificationSelectionComboBox = new ComboBox();
		senderIdentificationSelectionComboBox.setImmediate(false);
		senderIdentificationSelectionComboBox.setWidth("300px");
		senderIdentificationSelectionComboBox.setHeight("-1px");
		gridLayout_1.addComponent(senderIdentificationSelectionComboBox, 1, 0);
		gridLayout_1.setComponentAlignment(
				senderIdentificationSelectionComboBox, new Alignment(34));

		// finishScreeningSurveysCheckbox
		finishScreeningSurveysCheckbox = new CheckBox();
		finishScreeningSurveysCheckbox
				.setCaption("!! Automatically finish unfinished screening surveys (with default values)");
		finishScreeningSurveysCheckbox.setImmediate(false);
		finishScreeningSurveysCheckbox.setWidth("-1px");
		finishScreeningSurveysCheckbox.setHeight("-1px");
		gridLayout_1.addComponent(finishScreeningSurveysCheckbox, 0, 1);

		return gridLayout_1;
	}

	@AutoGenerated
	private HorizontalLayout buildHorizontalLayout_1() {
		// common part: create layout
		horizontalLayout_1 = new HorizontalLayout();
		horizontalLayout_1.setImmediate(false);
		horizontalLayout_1.setWidth("100.0%");
		horizontalLayout_1.setHeight("-1px");
		horizontalLayout_1.setMargin(false);
		horizontalLayout_1.setSpacing(true);

		// mondayCheckbox
		mondayCheckbox = new CheckBox();
		mondayCheckbox.setCaption("!! Monday");
		mondayCheckbox.setImmediate(false);
		mondayCheckbox.setWidth("-1px");
		mondayCheckbox.setHeight("-1px");
		horizontalLayout_1.addComponent(mondayCheckbox);
		horizontalLayout_1.setComponentAlignment(mondayCheckbox, new Alignment(
				48));

		// tuesdayCheckbox
		tuesdayCheckbox = new CheckBox();
		tuesdayCheckbox.setCaption("!! Tuesday");
		tuesdayCheckbox.setImmediate(false);
		tuesdayCheckbox.setWidth("-1px");
		tuesdayCheckbox.setHeight("-1px");
		horizontalLayout_1.addComponent(tuesdayCheckbox);
		horizontalLayout_1.setComponentAlignment(tuesdayCheckbox,
				new Alignment(48));

		// wednesdayCheckbox
		wednesdayCheckbox = new CheckBox();
		wednesdayCheckbox.setCaption("!! Wednesday");
		wednesdayCheckbox.setImmediate(false);
		wednesdayCheckbox.setWidth("-1px");
		wednesdayCheckbox.setHeight("-1px");
		horizontalLayout_1.addComponent(wednesdayCheckbox);

		// thursdayCheckbox
		thursdayCheckbox = new CheckBox();
		thursdayCheckbox.setCaption("!! Thursday");
		thursdayCheckbox.setImmediate(false);
		thursdayCheckbox.setWidth("-1px");
		thursdayCheckbox.setHeight("-1px");
		horizontalLayout_1.addComponent(thursdayCheckbox);

		// fridayCheckbox
		fridayCheckbox = new CheckBox();
		fridayCheckbox.setCaption("!! Friday");
		fridayCheckbox.setImmediate(false);
		fridayCheckbox.setWidth("-1px");
		fridayCheckbox.setHeight("-1px");
		horizontalLayout_1.addComponent(fridayCheckbox);

		// saturdayCheckbox
		saturdayCheckbox = new CheckBox();
		saturdayCheckbox.setCaption("!! Saturday");
		saturdayCheckbox.setImmediate(false);
		saturdayCheckbox.setWidth("-1px");
		saturdayCheckbox.setHeight("-1px");
		horizontalLayout_1.addComponent(saturdayCheckbox);

		// sundayCheckbox
		sundayCheckbox = new CheckBox();
		sundayCheckbox.setCaption("!! Sunday");
		sundayCheckbox.setImmediate(false);
		sundayCheckbox.setWidth("-1px");
		sundayCheckbox.setHeight("-1px");
		horizontalLayout_1.addComponent(sundayCheckbox);

		return horizontalLayout_1;
	}

	@AutoGenerated
	private GridLayout buildGridLayout_2() {
		// common part: create layout
		gridLayout_2 = new GridLayout();
		gridLayout_2.setImmediate(false);
		gridLayout_2.setWidth("100.0%");
		gridLayout_2.setHeight("-1px");
		gridLayout_2.setMargin(false);
		gridLayout_2.setSpacing(true);
		gridLayout_2.setColumns(2);

		// uniquenessLabel
		uniquenessLabel = new Label();
		uniquenessLabel.setImmediate(false);
		uniquenessLabel.setWidth("-1px");
		uniquenessLabel.setHeight("-1px");
		uniquenessLabel
				.setValue("!! Interventions to check for participant uniqueness:");
		gridLayout_2.addComponent(uniquenessLabel, 0, 0);

		// uniquenessList
		uniquenessList = new ListSelect();
		uniquenessList.setImmediate(false);
		uniquenessList.setWidth("300px");
		uniquenessList.setHeight("100px");
		gridLayout_2.addComponent(uniquenessList, 1, 0);
		gridLayout_2.setComponentAlignment(uniquenessList, new Alignment(6));

		return gridLayout_2;
	}

}
