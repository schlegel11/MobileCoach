package ch.ethz.mc.ui.views.components.interventions.monitoring_messages;

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
import ch.ethz.mc.ui.views.components.AbstractClosableEditComponent;
import ch.ethz.mc.ui.views.components.basics.MediaObjectIntegrationComponentWithController;
import ch.ethz.mc.ui.views.components.basics.VariableTextFieldComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides a monitoring message edit component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class MonitoringMessageEditComponent
		extends AbstractClosableEditComponent {
	@AutoGenerated
	private VerticalLayout									mainLayout;

	@AutoGenerated
	private GridLayout										buttonLayout;

	@AutoGenerated
	private Button											closeButton;

	@AutoGenerated
	private GridLayout										gridLayout_2;

	@AutoGenerated
	private VariableTextFieldComponent						answerOptionsTextFieldComponent;

	@AutoGenerated
	private Label											answerOptionsLabel;

	@AutoGenerated
	private ComboBox										answerTypeComboBox;

	@AutoGenerated
	private Label											answerTypeLabel;

	@AutoGenerated
	private HorizontalLayout								rulesButtonsLayout;

	@AutoGenerated
	private Button											deleteRuleButton;

	@AutoGenerated
	private Button											moveDownRuleButton;

	@AutoGenerated
	private Button											moveUpRuleButton;

	@AutoGenerated
	private Button											editRuleButton;

	@AutoGenerated
	private Button											newRuleButton;

	@AutoGenerated
	private Table											rulesTable;

	@AutoGenerated
	private Label											informationLabel;

	@AutoGenerated
	private CheckBox										isCommandCheckbox;

	@AutoGenerated
	private GridLayout										gridLayout_1;

	@AutoGenerated
	private VariableTextFieldComponent						storeVariableTextFieldComponent;

	@AutoGenerated
	private Label											storeVariableLabel;

	@AutoGenerated
	private ComboBox										intermediateSurveyComboBox;

	@AutoGenerated
	private Label											intermediateSurveyLabel;

	@AutoGenerated
	private MediaObjectIntegrationComponentWithController	integratedMediaObjectComponent;

	@AutoGenerated
	private Label											integratedMediaObjectLabel;

	@AutoGenerated
	private VariableTextFieldComponent						textWithPlaceholdersTextFieldComponent;

	@AutoGenerated
	private Label											textWithPlaceholdersLabel;

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		closeButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(
			final ClickListener clickListener) {
		// not required
	}

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected MonitoringMessageEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		integratedMediaObjectLabel.setContentMode(ContentMode.HTML);
		localize(textWithPlaceholdersLabel,
				AdminMessageStrings.MONITORING_MESSAGE_EDITING__TEXT_WITH_PLACEHOLDERS);
		localize(integratedMediaObjectLabel,
				AdminMessageStrings.MONITORING_MESSAGE_EDITING__INTEGRATED_MEDIA_OBJECT);
		localize(intermediateSurveyLabel,
				AdminMessageStrings.MONITORING_MESSAGE_EDITING__INTERMEDIATE_SURVEY_LABEL);
		localize(storeVariableLabel,
				AdminMessageStrings.MONITORING_MESSAGE_EDITING__STORE_MESSAGE_REPLY_TO_VARIABLE);
		localize(isCommandCheckbox,
				AdminMessageStrings.MONITORING_MESSAGE_EDITING__MESSAGE_IS_COMMAND);
		localize(informationLabel,
				AdminMessageStrings.MONITORING_MESSAGE_EDITING__RULE_INFORMATION_LABEL);
		localize(answerTypeLabel,
				AdminMessageStrings.MONITORING_MESSAGE_EDITING__ANSWER_TYPE_LABEL);
		localize(answerOptionsLabel,
				AdminMessageStrings.MONITORING_MESSAGE_EDITING__ANSWER_OPTIONS_LABEL);

		localize(newRuleButton, AdminMessageStrings.GENERAL__NEW);
		localize(editRuleButton, AdminMessageStrings.GENERAL__EDIT);
		localize(moveUpRuleButton, AdminMessageStrings.GENERAL__MOVE_UP);
		localize(moveDownRuleButton, AdminMessageStrings.GENERAL__MOVE_DOWN);
		localize(deleteRuleButton, AdminMessageStrings.GENERAL__DELETE);

		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);

		// set button start state
		setRuleSelected(false);

		// adjust tables
		rulesTable.setSelectable(true);
		rulesTable.setImmediate(true);

		// adjust combo boxes
		intermediateSurveyComboBox.setImmediate(true);
		intermediateSurveyComboBox.setNullSelectionAllowed(true);
		intermediateSurveyComboBox.setTextInputAllowed(false);
		answerTypeComboBox.setImmediate(true);
		answerTypeComboBox.setNullSelectionAllowed(false);
		answerTypeComboBox.setTextInputAllowed(false);

		// adjust checkboxes
		isCommandCheckbox.setImmediate(true);
	}

	protected void setRuleSelected(final boolean selection) {
		editRuleButton.setEnabled(selection);
		moveUpRuleButton.setEnabled(selection);
		moveDownRuleButton.setEnabled(selection);
		deleteRuleButton.setEnabled(selection);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("800px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("800px");
		setHeight("-1px");

		// gridLayout_1
		gridLayout_1 = buildGridLayout_1();
		mainLayout.addComponent(gridLayout_1);
		mainLayout.setExpandRatio(gridLayout_1, 1.0f);

		// isCommandCheckbox
		isCommandCheckbox = new CheckBox();
		isCommandCheckbox.setCaption(
				"!!! This message is a command (invisible for participant)");
		isCommandCheckbox.setImmediate(false);
		isCommandCheckbox.setWidth("100.0%");
		isCommandCheckbox.setHeight("-1px");
		mainLayout.addComponent(isCommandCheckbox);

		// informationLabel
		informationLabel = new Label();
		informationLabel.setStyleName("bold");
		informationLabel.setImmediate(false);
		informationLabel.setWidth("-1px");
		informationLabel.setHeight("-1px");
		informationLabel.setValue(
				"!!! Message will only be send if the following rules are ALL TRUE:");
		mainLayout.addComponent(informationLabel);

		// rulesTable
		rulesTable = new Table();
		rulesTable.setImmediate(false);
		rulesTable.setWidth("100.0%");
		rulesTable.setHeight("100px");
		mainLayout.addComponent(rulesTable);

		// rulesButtonsLayout
		rulesButtonsLayout = buildRulesButtonsLayout();
		mainLayout.addComponent(rulesButtonsLayout);

		// gridLayout_2
		gridLayout_2 = buildGridLayout_2();
		mainLayout.addComponent(gridLayout_2);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

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
		gridLayout_1.setRows(4);

		// textWithPlaceholdersLabel
		textWithPlaceholdersLabel = new Label();
		textWithPlaceholdersLabel.setImmediate(false);
		textWithPlaceholdersLabel.setWidth("-1px");
		textWithPlaceholdersLabel.setHeight("-1px");
		textWithPlaceholdersLabel.setValue("!!! Text (with placeholders):");
		gridLayout_1.addComponent(textWithPlaceholdersLabel, 0, 0);

		// textWithPlaceholdersTextFieldComponent
		textWithPlaceholdersTextFieldComponent = new VariableTextFieldComponent();
		textWithPlaceholdersTextFieldComponent.setImmediate(false);
		textWithPlaceholdersTextFieldComponent.setWidth("500px");
		textWithPlaceholdersTextFieldComponent.setHeight("-1px");
		gridLayout_1.addComponent(textWithPlaceholdersTextFieldComponent, 1, 0);
		gridLayout_1.setComponentAlignment(
				textWithPlaceholdersTextFieldComponent, new Alignment(34));

		// integratedMediaObjectLabel
		integratedMediaObjectLabel = new Label();
		integratedMediaObjectLabel.setStyleName("media-object-description");
		integratedMediaObjectLabel.setImmediate(false);
		integratedMediaObjectLabel.setWidth("-1px");
		integratedMediaObjectLabel.setHeight("-1px");
		integratedMediaObjectLabel.setValue("!!! Integrated media object:");
		gridLayout_1.addComponent(integratedMediaObjectLabel, 0, 1);

		// integratedMediaObjectComponent
		integratedMediaObjectComponent = new MediaObjectIntegrationComponentWithController();
		integratedMediaObjectComponent.setImmediate(false);
		integratedMediaObjectComponent.setWidth("500px");
		integratedMediaObjectComponent.setHeight("300px");
		gridLayout_1.addComponent(integratedMediaObjectComponent, 1, 1);
		gridLayout_1.setComponentAlignment(integratedMediaObjectComponent,
				new Alignment(34));

		// intermediateSurveyLabel
		intermediateSurveyLabel = new Label();
		intermediateSurveyLabel.setImmediate(false);
		intermediateSurveyLabel.setWidth("-1px");
		intermediateSurveyLabel.setHeight("-1px");
		intermediateSurveyLabel.setValue("!!! Integrate link to survey:");
		gridLayout_1.addComponent(intermediateSurveyLabel, 0, 2);

		// intermediateSurveyComboBox
		intermediateSurveyComboBox = new ComboBox();
		intermediateSurveyComboBox.setImmediate(false);
		intermediateSurveyComboBox.setWidth("500px");
		intermediateSurveyComboBox.setHeight("-1px");
		gridLayout_1.addComponent(intermediateSurveyComboBox, 1, 2);
		gridLayout_1.setComponentAlignment(intermediateSurveyComboBox,
				new Alignment(34));

		// storeVariableLabel
		storeVariableLabel = new Label();
		storeVariableLabel.setImmediate(false);
		storeVariableLabel.setWidth("-1px");
		storeVariableLabel.setHeight("-1px");
		storeVariableLabel
				.setValue("!!! Store result to variable (if required):");
		gridLayout_1.addComponent(storeVariableLabel, 0, 3);

		// storeVariableTextFieldComponent
		storeVariableTextFieldComponent = new VariableTextFieldComponent();
		storeVariableTextFieldComponent.setImmediate(false);
		storeVariableTextFieldComponent.setWidth("500px");
		storeVariableTextFieldComponent.setHeight("-1px");
		gridLayout_1.addComponent(storeVariableTextFieldComponent, 1, 3);
		gridLayout_1.setComponentAlignment(storeVariableTextFieldComponent,
				new Alignment(34));

		return gridLayout_1;
	}

	@AutoGenerated
	private HorizontalLayout buildRulesButtonsLayout() {
		// common part: create layout
		rulesButtonsLayout = new HorizontalLayout();
		rulesButtonsLayout.setImmediate(false);
		rulesButtonsLayout.setWidth("-1px");
		rulesButtonsLayout.setHeight("-1px");
		rulesButtonsLayout.setMargin(false);
		rulesButtonsLayout.setSpacing(true);

		// newRuleButton
		newRuleButton = new Button();
		newRuleButton.setCaption("!!! New");
		newRuleButton.setIcon(new ThemeResource("img/add-icon-small.png"));
		newRuleButton.setImmediate(true);
		newRuleButton.setWidth("100px");
		newRuleButton.setHeight("-1px");
		rulesButtonsLayout.addComponent(newRuleButton);

		// editRuleButton
		editRuleButton = new Button();
		editRuleButton.setCaption("!!! Edit");
		editRuleButton.setIcon(new ThemeResource("img/edit-icon-small.png"));
		editRuleButton.setImmediate(true);
		editRuleButton.setWidth("100px");
		editRuleButton.setHeight("-1px");
		rulesButtonsLayout.addComponent(editRuleButton);

		// moveUpRuleButton
		moveUpRuleButton = new Button();
		moveUpRuleButton.setCaption("!!! Move Up");
		moveUpRuleButton
				.setIcon(new ThemeResource("img/arrow-up-icon-small.png"));
		moveUpRuleButton.setImmediate(true);
		moveUpRuleButton.setWidth("120px");
		moveUpRuleButton.setHeight("-1px");
		rulesButtonsLayout.addComponent(moveUpRuleButton);

		// moveDownRuleButton
		moveDownRuleButton = new Button();
		moveDownRuleButton.setCaption("!!! Move Down");
		moveDownRuleButton
				.setIcon(new ThemeResource("img/arrow-down-icon-small.png"));
		moveDownRuleButton.setImmediate(true);
		moveDownRuleButton.setWidth("120px");
		moveDownRuleButton.setHeight("-1px");
		rulesButtonsLayout.addComponent(moveDownRuleButton);

		// deleteRuleButton
		deleteRuleButton = new Button();
		deleteRuleButton.setCaption("!!! Delete");
		deleteRuleButton
				.setIcon(new ThemeResource("img/delete-icon-small.png"));
		deleteRuleButton.setImmediate(true);
		deleteRuleButton.setWidth("100px");
		deleteRuleButton.setHeight("-1px");
		rulesButtonsLayout.addComponent(deleteRuleButton);

		return rulesButtonsLayout;
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
		gridLayout_2.setRows(2);

		// answerTypeLabel
		answerTypeLabel = new Label();
		answerTypeLabel.setImmediate(false);
		answerTypeLabel.setWidth("-1px");
		answerTypeLabel.setHeight("-1px");
		answerTypeLabel.setValue("!!! Answer type (if required):");
		gridLayout_2.addComponent(answerTypeLabel, 0, 0);

		// answerTypeComboBox
		answerTypeComboBox = new ComboBox();
		answerTypeComboBox.setImmediate(false);
		answerTypeComboBox.setWidth("500px");
		answerTypeComboBox.setHeight("-1px");
		gridLayout_2.addComponent(answerTypeComboBox, 1, 0);
		gridLayout_2.setComponentAlignment(answerTypeComboBox,
				new Alignment(34));

		// answerOptionsLabel
		answerOptionsLabel = new Label();
		answerOptionsLabel.setImmediate(false);
		answerOptionsLabel.setWidth("-1px");
		answerOptionsLabel.setHeight("-1px");
		answerOptionsLabel.setValue(
				"!!! Answer options (with placeholders, if required):");
		gridLayout_2.addComponent(answerOptionsLabel, 0, 1);

		// answerOptionsTextFieldComponent
		answerOptionsTextFieldComponent = new VariableTextFieldComponent();
		answerOptionsTextFieldComponent.setImmediate(false);
		answerOptionsTextFieldComponent.setWidth("500px");
		answerOptionsTextFieldComponent.setHeight("-1px");
		gridLayout_2.addComponent(answerOptionsTextFieldComponent, 1, 1);
		gridLayout_2.setComponentAlignment(answerOptionsTextFieldComponent,
				new Alignment(34));

		return gridLayout_2;
	}

	@AutoGenerated
	private GridLayout buildButtonLayout() {
		// common part: create layout
		buttonLayout = new GridLayout();
		buttonLayout.setImmediate(false);
		buttonLayout.setWidth("100.0%");
		buttonLayout.setHeight("-1px");
		buttonLayout.setMargin(true);
		buttonLayout.setSpacing(true);

		// closeButton
		closeButton = new Button();
		closeButton.setCaption("!!! Close");
		closeButton.setIcon(new ThemeResource("img/ok-icon-small.png"));
		closeButton.setImmediate(true);
		closeButton.setWidth("140px");
		closeButton.setHeight("-1px");
		buttonLayout.addComponent(closeButton, 0, 0);
		buttonLayout.setComponentAlignment(closeButton, new Alignment(48));

		return buttonLayout;
	}

}
