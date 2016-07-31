package ch.ethz.mc.ui.views.components.screening_survey;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
 * Provides the screening survey slide rule edit component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class ScreeningSurveySlideEditComponent extends
AbstractClosableEditComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout									mainLayout;
	@AutoGenerated
	private GridLayout										closeButtonLayout;
	@AutoGenerated
	private Button											closeButton;
	@AutoGenerated
	private HorizontalLayout								rulesButtonsLayout;
	@AutoGenerated
	private Button											deleteRuleButton;
	@AutoGenerated
	private Button											levelUpButton;
	@AutoGenerated
	private Button											levelDownButton;
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
	private GridLayout										additionalOptionsLayout;
	@AutoGenerated
	private ComboBox										feedbackComboBox;
	@AutoGenerated
	private CheckBox										isLastSlideCheckbox;
	@AutoGenerated
	private Label											feedbackLabel;
	@AutoGenerated
	private MediaObjectIntegrationComponentWithController	integratedMediaObjectComponent;
	@AutoGenerated
	private Label											integratedMediaObjectLabel;
	@AutoGenerated
	private VariableTextFieldComponent						validationErrorMessageTextFieldComponent;
	@AutoGenerated
	private Label											validationErrorMessageLabel;
	@AutoGenerated
	private HorizontalLayout								questionsLayout;
	@AutoGenerated
	private VerticalLayout									questionsFlowLayout;
	@AutoGenerated
	private GridLayout										questionValueLayout;
	@AutoGenerated
	private VariableTextFieldComponent						defaultVariableValueTextFieldComponent;
	@AutoGenerated
	private Label											defaultVariableValueLabel;
	@AutoGenerated
	private VariableTextFieldComponent						storeVariableTextFieldComponent;
	@AutoGenerated
	private Label											storeVariableLabel;
	@AutoGenerated
	private ComboBox										preselectedAnswerComboBox;
	@AutoGenerated
	private Label											preselectedAnswerLabel;
	@AutoGenerated
	private HorizontalLayout								answersButtonsLayout;
	@AutoGenerated
	private Button											deleteAnswerButton;
	@AutoGenerated
	private Button											moveDownAnswerButton;
	@AutoGenerated
	private Button											moveUpAnswerButton;
	@AutoGenerated
	private Button											editAnswerValueButton;
	@AutoGenerated
	private Button											editAnswerAnswerButton;
	@AutoGenerated
	private Button											newAnswerButton;
	@AutoGenerated
	private Table											answersTable;
	@AutoGenerated
	private GridLayout										questionTextLayout;
	@AutoGenerated
	private VariableTextFieldComponent						questionTextWithPlaceholdersTextField;
	@AutoGenerated
	private Label											questionTextWithPlaceholdersLabel;
	@AutoGenerated
	private VerticalLayout									questionsTableLayout;
	@AutoGenerated
	private Button											deleteQuestionButton;
	@AutoGenerated
	private Button											duplicateQuestionButton;
	@AutoGenerated
	private Button											newQuestionButton;
	@AutoGenerated
	private Table											questionsTable;
	@AutoGenerated
	private GridLayout										switchesLayoutGroup;
	@AutoGenerated
	private VariableTextFieldComponent						optionalLayoutAttributeTextFieldComponent;
	@AutoGenerated
	private Label											optionalLayoutAttributeLabel;
	@AutoGenerated
	private ComboBox										questionTypeComboBox;
	@AutoGenerated
	private Label											questionTypeLabel;
	@AutoGenerated
	private VariableTextFieldComponent						commentTextFieldComponent;
	@AutoGenerated
	private Label											commentLabel;
	@AutoGenerated
	private VariableTextFieldComponent						titleWithPlaceholdersTextFieldComponent;
	@AutoGenerated
	private Label											titleWithPlaceholdersLabel;

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected ScreeningSurveySlideEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		integratedMediaObjectLabel.setContentMode(ContentMode.HTML);

		localize(
				titleWithPlaceholdersLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__TITLE_WITH_PLACEHOLDERS);
		localize(commentLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__COMMENT);
		localize(
				questionTypeLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__QUESTION_TYPE);
		localize(
				optionalLayoutAttributeLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__OPTIONAL_LAYOUT_ATTRIBUTE_WITH_PLACEHOLDERS);
		localize(
				questionTextWithPlaceholdersLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__QUESTION_TEXT);
		localize(
				preselectedAnswerLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__PRESELECTED_ANSWER);
		localize(
				validationErrorMessageLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__VALIDATION_ERROR_MESSAGE);
		localize(
				defaultVariableValueLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__DEFAULT_VARIABLE_VALUE);
		localize(
				storeVariableLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__STORE_RESULT_TO_VARIABLE);
		localize(
				integratedMediaObjectLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__INTEGRATED_MEDIA_OBJECT);
		localize(
				feedbackLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__FINISH_OR_HAND_OVER_TO_FEEDBACK);
		localize(
				isLastSlideCheckbox,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__STOP_SCREENING_SURVEY_AFTER_THIS_SLIDE);

		localize(newQuestionButton, AdminMessageStrings.GENERAL__NEW);
		localize(duplicateQuestionButton,
				AdminMessageStrings.GENERAL__DUPLICATE);
		localize(deleteQuestionButton, AdminMessageStrings.GENERAL__DELETE);

		localize(newAnswerButton, AdminMessageStrings.GENERAL__NEW);
		localize(editAnswerAnswerButton,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__EDIT_ANSWER);
		localize(editAnswerValueButton,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__EDIT_VALUE);
		localize(moveUpAnswerButton, AdminMessageStrings.GENERAL__MOVE_UP);
		localize(moveDownAnswerButton, AdminMessageStrings.GENERAL__MOVE_DOWN);
		localize(levelUpButton, AdminMessageStrings.GENERAL__MAKE_SUPER);
		localize(levelDownButton, AdminMessageStrings.GENERAL__MAKE_SUB);
		localize(deleteAnswerButton, AdminMessageStrings.GENERAL__DELETE);

		localize(newRuleButton, AdminMessageStrings.GENERAL__NEW);
		localize(editRuleButton, AdminMessageStrings.GENERAL__EDIT);
		localize(moveUpRuleButton, AdminMessageStrings.GENERAL__MOVE_UP);
		localize(moveDownRuleButton, AdminMessageStrings.GENERAL__MOVE_DOWN);
		localize(deleteRuleButton, AdminMessageStrings.GENERAL__DELETE);

		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);

		isLastSlideCheckbox.setImmediate(true);

		// set button start state
		setAnswerSelected(false);
		setRuleSelected(false);

		// adjust tables
		questionsTable.setSelectable(true);
		questionsTable.setNullSelectionAllowed(false);
		questionsTable.setImmediate(true);

		answersTable.setSelectable(true);
		answersTable.setImmediate(true);

		rulesTable.setSelectable(true);
		rulesTable.setImmediate(true);

		// adjust combo boxes
		questionTypeComboBox.setImmediate(true);
		questionTypeComboBox.setNullSelectionAllowed(false);
		questionTypeComboBox.setTextInputAllowed(false);

		preselectedAnswerComboBox.setImmediate(true);
		preselectedAnswerComboBox.setNullSelectionAllowed(true);
		preselectedAnswerComboBox.setTextInputAllowed(false);

		feedbackComboBox.setImmediate(true);
		feedbackComboBox.setNullSelectionAllowed(true);
		feedbackComboBox.setTextInputAllowed(false);
	}

	protected void setAnswerSelected(final boolean selection) {
		editAnswerAnswerButton.setEnabled(selection);
		editAnswerValueButton.setEnabled(selection);
		moveUpAnswerButton.setEnabled(selection);
		moveDownAnswerButton.setEnabled(selection);
		deleteAnswerButton.setEnabled(selection);
	}

	protected void setRuleSelected(final boolean selection) {
		editRuleButton.setEnabled(selection);
		moveUpRuleButton.setEnabled(selection);
		moveDownRuleButton.setEnabled(selection);
		levelUpButton.setEnabled(selection);
		levelDownButton.setEnabled(selection);
		deleteRuleButton.setEnabled(selection);
	}

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		closeButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(final ClickListener clickListener) {
		// not required
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("950px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("950px");
		setHeight("-1px");

		// switchesLayoutGroup
		switchesLayoutGroup = buildSwitchesLayoutGroup();
		mainLayout.addComponent(switchesLayoutGroup);

		// questionsLayout
		questionsLayout = buildQuestionsLayout();
		mainLayout.addComponent(questionsLayout);

		// additionalOptionsLayout
		additionalOptionsLayout = buildAdditionalOptionsLayout();
		mainLayout.addComponent(additionalOptionsLayout);

		// rulesTable
		rulesTable = new Table();
		rulesTable.setImmediate(false);
		rulesTable.setWidth("100.0%");
		rulesTable.setHeight("125px");
		mainLayout.addComponent(rulesTable);

		// rulesButtonsLayout
		rulesButtonsLayout = buildRulesButtonsLayout();
		mainLayout.addComponent(rulesButtonsLayout);

		// closeButtonLayout
		closeButtonLayout = buildCloseButtonLayout();
		mainLayout.addComponent(closeButtonLayout);

		return mainLayout;
	}

	@AutoGenerated
	private GridLayout buildSwitchesLayoutGroup() {
		// common part: create layout
		switchesLayoutGroup = new GridLayout();
		switchesLayoutGroup.setImmediate(false);
		switchesLayoutGroup.setWidth("100.0%");
		switchesLayoutGroup.setHeight("-1px");
		switchesLayoutGroup.setMargin(false);
		switchesLayoutGroup.setSpacing(true);
		switchesLayoutGroup.setColumns(2);
		switchesLayoutGroup.setRows(4);

		// titleWithPlaceholdersLabel
		titleWithPlaceholdersLabel = new Label();
		titleWithPlaceholdersLabel.setImmediate(false);
		titleWithPlaceholdersLabel.setWidth("-1px");
		titleWithPlaceholdersLabel.setHeight("-1px");
		titleWithPlaceholdersLabel.setValue("!!! Title (with placeholders):");
		switchesLayoutGroup.addComponent(titleWithPlaceholdersLabel, 0, 0);

		// titleWithPlaceholdersTextFieldComponent
		titleWithPlaceholdersTextFieldComponent = new VariableTextFieldComponent();
		titleWithPlaceholdersTextFieldComponent.setImmediate(false);
		titleWithPlaceholdersTextFieldComponent.setWidth("350px");
		titleWithPlaceholdersTextFieldComponent.setHeight("-1px");
		switchesLayoutGroup.addComponent(
				titleWithPlaceholdersTextFieldComponent, 1, 0);
		switchesLayoutGroup.setComponentAlignment(
				titleWithPlaceholdersTextFieldComponent, new Alignment(6));

		// commentLabel
		commentLabel = new Label();
		commentLabel.setImmediate(false);
		commentLabel.setWidth("-1px");
		commentLabel.setHeight("-1px");
		commentLabel.setValue("!!! Comment:");
		switchesLayoutGroup.addComponent(commentLabel, 0, 1);

		// commentTextFieldComponent
		commentTextFieldComponent = new VariableTextFieldComponent();
		commentTextFieldComponent.setImmediate(false);
		commentTextFieldComponent.setWidth("350px");
		commentTextFieldComponent.setHeight("-1px");
		switchesLayoutGroup.addComponent(commentTextFieldComponent, 1, 1);
		switchesLayoutGroup.setComponentAlignment(commentTextFieldComponent,
				new Alignment(6));

		// questionTypeLabel
		questionTypeLabel = new Label();
		questionTypeLabel.setImmediate(false);
		questionTypeLabel.setWidth("-1px");
		questionTypeLabel.setHeight("-1px");
		questionTypeLabel.setValue("!!! Question/slide type:");
		switchesLayoutGroup.addComponent(questionTypeLabel, 0, 2);

		// questionTypeComboBox
		questionTypeComboBox = new ComboBox();
		questionTypeComboBox.setImmediate(false);
		questionTypeComboBox.setWidth("350px");
		questionTypeComboBox.setHeight("-1px");
		switchesLayoutGroup.addComponent(questionTypeComboBox, 1, 2);
		switchesLayoutGroup.setComponentAlignment(questionTypeComboBox,
				new Alignment(6));

		// optionalLayoutAttributeLabel
		optionalLayoutAttributeLabel = new Label();
		optionalLayoutAttributeLabel.setImmediate(false);
		optionalLayoutAttributeLabel.setWidth("-1px");
		optionalLayoutAttributeLabel.setHeight("-1px");
		optionalLayoutAttributeLabel
		.setValue("!!! Optional layout attribute (e.g. CSS classes):");
		switchesLayoutGroup.addComponent(optionalLayoutAttributeLabel, 0, 3);

		// optionalLayoutAttributeTextFieldComponent
		optionalLayoutAttributeTextFieldComponent = new VariableTextFieldComponent();
		optionalLayoutAttributeTextFieldComponent.setImmediate(false);
		optionalLayoutAttributeTextFieldComponent.setWidth("350px");
		optionalLayoutAttributeTextFieldComponent.setHeight("-1px");
		switchesLayoutGroup.addComponent(
				optionalLayoutAttributeTextFieldComponent, 1, 3);
		switchesLayoutGroup.setComponentAlignment(
				optionalLayoutAttributeTextFieldComponent, new Alignment(6));

		return switchesLayoutGroup;
	}

	@AutoGenerated
	private HorizontalLayout buildQuestionsLayout() {
		// common part: create layout
		questionsLayout = new HorizontalLayout();
		questionsLayout.setImmediate(false);
		questionsLayout.setWidth("100.0%");
		questionsLayout.setHeight("-1px");
		questionsLayout.setMargin(false);
		questionsLayout.setSpacing(true);

		// questionsTableLayout
		questionsTableLayout = buildQuestionsTableLayout();
		questionsLayout.addComponent(questionsTableLayout);
		questionsLayout.setExpandRatio(questionsTableLayout, 0.14f);

		// questionsFlowLayout
		questionsFlowLayout = buildQuestionsFlowLayout();
		questionsLayout.addComponent(questionsFlowLayout);
		questionsLayout.setExpandRatio(questionsFlowLayout, 0.65f);

		return questionsLayout;
	}

	@AutoGenerated
	private VerticalLayout buildQuestionsTableLayout() {
		// common part: create layout
		questionsTableLayout = new VerticalLayout();
		questionsTableLayout.setImmediate(false);
		questionsTableLayout.setWidth("100.0%");
		questionsTableLayout.setHeight("-1px");
		questionsTableLayout.setMargin(false);
		questionsTableLayout.setSpacing(true);

		// questionsTable
		questionsTable = new Table();
		questionsTable.setImmediate(false);
		questionsTable.setWidth("150px");
		questionsTable.setHeight("155px");
		questionsTableLayout.addComponent(questionsTable);

		// newQuestionButton
		newQuestionButton = new Button();
		newQuestionButton.setCaption("!!! New");
		newQuestionButton.setIcon(new ThemeResource("img/add-icon-small.png"));
		newQuestionButton.setImmediate(true);
		newQuestionButton.setWidth("150px");
		newQuestionButton.setHeight("-1px");
		questionsTableLayout.addComponent(newQuestionButton);

		// duplicateQuestionButton
		duplicateQuestionButton = new Button();
		duplicateQuestionButton.setCaption("!!! Duplicate");
		duplicateQuestionButton.setImmediate(true);
		duplicateQuestionButton.setWidth("150px");
		duplicateQuestionButton.setHeight("-1px");
		questionsTableLayout.addComponent(duplicateQuestionButton);

		// deleteQuestionButton
		deleteQuestionButton = new Button();
		deleteQuestionButton.setCaption("!!! Delete");
		deleteQuestionButton.setIcon(new ThemeResource(
				"img/delete-icon-small.png"));
		deleteQuestionButton.setImmediate(true);
		deleteQuestionButton.setWidth("150px");
		deleteQuestionButton.setHeight("-1px");
		questionsTableLayout.addComponent(deleteQuestionButton);

		return questionsTableLayout;
	}

	@AutoGenerated
	private VerticalLayout buildQuestionsFlowLayout() {
		// common part: create layout
		questionsFlowLayout = new VerticalLayout();
		questionsFlowLayout.setImmediate(false);
		questionsFlowLayout.setWidth("100.0%");
		questionsFlowLayout.setHeight("-1px");
		questionsFlowLayout.setMargin(false);
		questionsFlowLayout.setSpacing(true);

		// questionTextLayout
		questionTextLayout = buildQuestionTextLayout();
		questionsFlowLayout.addComponent(questionTextLayout);

		// answersTable
		answersTable = new Table();
		answersTable.setImmediate(false);
		answersTable.setWidth("100.0%");
		answersTable.setHeight("100px");
		questionsFlowLayout.addComponent(answersTable);

		// answersButtonsLayout
		answersButtonsLayout = buildAnswersButtonsLayout();
		questionsFlowLayout.addComponent(answersButtonsLayout);

		// questionValueLayout
		questionValueLayout = buildQuestionValueLayout();
		questionsFlowLayout.addComponent(questionValueLayout);

		return questionsFlowLayout;
	}

	@AutoGenerated
	private GridLayout buildQuestionTextLayout() {
		// common part: create layout
		questionTextLayout = new GridLayout();
		questionTextLayout.setImmediate(false);
		questionTextLayout.setWidth("100.0%");
		questionTextLayout.setHeight("-1px");
		questionTextLayout.setMargin(false);
		questionTextLayout.setSpacing(true);
		questionTextLayout.setColumns(2);

		// questionTextWithPlaceholdersLabel
		questionTextWithPlaceholdersLabel = new Label();
		questionTextWithPlaceholdersLabel.setImmediate(false);
		questionTextWithPlaceholdersLabel.setWidth("-1px");
		questionTextWithPlaceholdersLabel.setHeight("-1px");
		questionTextWithPlaceholdersLabel
		.setValue("!!! Question text (with placeholders):");
		questionTextLayout
		.addComponent(questionTextWithPlaceholdersLabel, 0, 0);

		// questionTextWithPlaceholdersTextField
		questionTextWithPlaceholdersTextField = new VariableTextFieldComponent();
		questionTextWithPlaceholdersTextField.setImmediate(false);
		questionTextWithPlaceholdersTextField.setWidth("350px");
		questionTextWithPlaceholdersTextField.setHeight("-1px");
		questionTextLayout.addComponent(questionTextWithPlaceholdersTextField,
				1, 0);
		questionTextLayout.setComponentAlignment(
				questionTextWithPlaceholdersTextField, new Alignment(6));

		return questionTextLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildAnswersButtonsLayout() {
		// common part: create layout
		answersButtonsLayout = new HorizontalLayout();
		answersButtonsLayout.setImmediate(false);
		answersButtonsLayout.setWidth("-1px");
		answersButtonsLayout.setHeight("-1px");
		answersButtonsLayout.setMargin(false);
		answersButtonsLayout.setSpacing(true);

		// newAnswerButton
		newAnswerButton = new Button();
		newAnswerButton.setCaption("!!! New");
		newAnswerButton.setIcon(new ThemeResource("img/add-icon-small.png"));
		newAnswerButton.setImmediate(true);
		newAnswerButton.setWidth("100px");
		newAnswerButton.setHeight("-1px");
		answersButtonsLayout.addComponent(newAnswerButton);

		// editAnswerAnswerButton
		editAnswerAnswerButton = new Button();
		editAnswerAnswerButton.setCaption("!!! Edit Answer");
		editAnswerAnswerButton.setImmediate(true);
		editAnswerAnswerButton.setWidth("100px");
		editAnswerAnswerButton.setHeight("-1px");
		answersButtonsLayout.addComponent(editAnswerAnswerButton);

		// editAnswerValueButton
		editAnswerValueButton = new Button();
		editAnswerValueButton.setCaption("!!! Edit Value");
		editAnswerValueButton.setImmediate(true);
		editAnswerValueButton.setWidth("100px");
		editAnswerValueButton.setHeight("-1px");
		answersButtonsLayout.addComponent(editAnswerValueButton);

		// moveUpAnswerButton
		moveUpAnswerButton = new Button();
		moveUpAnswerButton.setCaption("!!! Move Up");
		moveUpAnswerButton.setIcon(new ThemeResource(
				"img/arrow-up-icon-small.png"));
		moveUpAnswerButton.setImmediate(true);
		moveUpAnswerButton.setWidth("120px");
		moveUpAnswerButton.setHeight("-1px");
		answersButtonsLayout.addComponent(moveUpAnswerButton);

		// moveDownAnswerButton
		moveDownAnswerButton = new Button();
		moveDownAnswerButton.setCaption("!!! Move Down");
		moveDownAnswerButton.setIcon(new ThemeResource(
				"img/arrow-down-icon-small.png"));
		moveDownAnswerButton.setImmediate(true);
		moveDownAnswerButton.setWidth("120px");
		moveDownAnswerButton.setHeight("-1px");
		answersButtonsLayout.addComponent(moveDownAnswerButton);

		// deleteAnswerButton
		deleteAnswerButton = new Button();
		deleteAnswerButton.setCaption("!!! Delete");
		deleteAnswerButton.setIcon(new ThemeResource(
				"img/delete-icon-small.png"));
		deleteAnswerButton.setImmediate(true);
		deleteAnswerButton.setWidth("100px");
		deleteAnswerButton.setHeight("-1px");
		answersButtonsLayout.addComponent(deleteAnswerButton);

		return answersButtonsLayout;
	}

	@AutoGenerated
	private GridLayout buildQuestionValueLayout() {
		// common part: create layout
		questionValueLayout = new GridLayout();
		questionValueLayout.setImmediate(false);
		questionValueLayout.setWidth("100.0%");
		questionValueLayout.setHeight("-1px");
		questionValueLayout.setMargin(false);
		questionValueLayout.setSpacing(true);
		questionValueLayout.setColumns(2);
		questionValueLayout.setRows(3);

		// preselectedAnswerLabel
		preselectedAnswerLabel = new Label();
		preselectedAnswerLabel.setImmediate(false);
		preselectedAnswerLabel.setWidth("-1px");
		preselectedAnswerLabel.setHeight("-1px");
		preselectedAnswerLabel
		.setValue("!!! Preselected answer (if required):");
		questionValueLayout.addComponent(preselectedAnswerLabel, 0, 0);

		// preselectedAnswerComboBox
		preselectedAnswerComboBox = new ComboBox();
		preselectedAnswerComboBox.setImmediate(false);
		preselectedAnswerComboBox.setWidth("500px");
		preselectedAnswerComboBox.setHeight("-1px");
		questionValueLayout.addComponent(preselectedAnswerComboBox, 1, 0);
		questionValueLayout.setComponentAlignment(preselectedAnswerComboBox,
				new Alignment(6));

		// storeVariableLabel
		storeVariableLabel = new Label();
		storeVariableLabel.setImmediate(false);
		storeVariableLabel.setWidth("-1px");
		storeVariableLabel.setHeight("-1px");
		storeVariableLabel
		.setValue("!!! Store result to variable (if required):");
		questionValueLayout.addComponent(storeVariableLabel, 0, 1);

		// storeVariableTextFieldComponent
		storeVariableTextFieldComponent = new VariableTextFieldComponent();
		storeVariableTextFieldComponent.setImmediate(false);
		storeVariableTextFieldComponent.setWidth("500px");
		storeVariableTextFieldComponent.setHeight("-1px");
		questionValueLayout.addComponent(storeVariableTextFieldComponent, 1, 1);
		questionValueLayout.setComponentAlignment(
				storeVariableTextFieldComponent, new Alignment(6));

		// defaultVariableValueLabel
		defaultVariableValueLabel = new Label();
		defaultVariableValueLabel.setImmediate(false);
		defaultVariableValueLabel.setWidth("-1px");
		defaultVariableValueLabel.setHeight("-1px");
		defaultVariableValueLabel
		.setValue("!!! Default variable value (if required):");
		questionValueLayout.addComponent(defaultVariableValueLabel, 0, 2);

		// defaultVariableValueTextFieldComponent
		defaultVariableValueTextFieldComponent = new VariableTextFieldComponent();
		defaultVariableValueTextFieldComponent.setImmediate(false);
		defaultVariableValueTextFieldComponent.setWidth("500px");
		defaultVariableValueTextFieldComponent.setHeight("-1px");
		questionValueLayout.addComponent(
				defaultVariableValueTextFieldComponent, 1, 2);
		questionValueLayout.setComponentAlignment(
				defaultVariableValueTextFieldComponent, new Alignment(6));

		return questionValueLayout;
	}

	@AutoGenerated
	private GridLayout buildAdditionalOptionsLayout() {
		// common part: create layout
		additionalOptionsLayout = new GridLayout();
		additionalOptionsLayout.setImmediate(false);
		additionalOptionsLayout.setWidth("100.0%");
		additionalOptionsLayout.setHeight("-1px");
		additionalOptionsLayout.setMargin(false);
		additionalOptionsLayout.setSpacing(true);
		additionalOptionsLayout.setColumns(2);
		additionalOptionsLayout.setRows(4);

		// validationErrorMessageLabel
		validationErrorMessageLabel = new Label();
		validationErrorMessageLabel.setImmediate(false);
		validationErrorMessageLabel.setWidth("-1px");
		validationErrorMessageLabel.setHeight("-1px");
		validationErrorMessageLabel
		.setValue("!!! Validation error message (if required):");
		additionalOptionsLayout.addComponent(validationErrorMessageLabel, 0, 0);

		// validationErrorMessageTextFieldComponent
		validationErrorMessageTextFieldComponent = new VariableTextFieldComponent();
		validationErrorMessageTextFieldComponent.setImmediate(false);
		validationErrorMessageTextFieldComponent.setWidth("500px");
		validationErrorMessageTextFieldComponent.setHeight("-1px");
		additionalOptionsLayout.addComponent(
				validationErrorMessageTextFieldComponent, 1, 0);
		additionalOptionsLayout.setComponentAlignment(
				validationErrorMessageTextFieldComponent, new Alignment(34));

		// integratedMediaObjectLabel
		integratedMediaObjectLabel = new Label();
		integratedMediaObjectLabel.setImmediate(false);
		integratedMediaObjectLabel.setWidth("-1px");
		integratedMediaObjectLabel.setHeight("-1px");
		integratedMediaObjectLabel.setValue("!!! Integrated media object:");
		additionalOptionsLayout.addComponent(integratedMediaObjectLabel, 0, 1);

		// integratedMediaObjectComponent
		integratedMediaObjectComponent = new MediaObjectIntegrationComponentWithController();
		integratedMediaObjectComponent.setImmediate(false);
		integratedMediaObjectComponent.setWidth("500px");
		integratedMediaObjectComponent.setHeight("150px");
		additionalOptionsLayout.addComponent(integratedMediaObjectComponent, 1,
				1);
		additionalOptionsLayout.setComponentAlignment(
				integratedMediaObjectComponent, new Alignment(34));

		// feedbackLabel
		feedbackLabel = new Label();
		feedbackLabel.setImmediate(false);
		feedbackLabel.setWidth("-1px");
		feedbackLabel.setHeight("-1px");
		feedbackLabel.setValue("!!! Finish or hand over to feedback:");
		additionalOptionsLayout.addComponent(feedbackLabel, 0, 2);

		// isLastSlideCheckbox
		isLastSlideCheckbox = new CheckBox();
		isLastSlideCheckbox
		.setCaption("!!! Stop screening survey after this slide (no rule execution as well)");
		isLastSlideCheckbox.setImmediate(false);
		isLastSlideCheckbox.setWidth("500px");
		isLastSlideCheckbox.setHeight("-1px");
		additionalOptionsLayout.addComponent(isLastSlideCheckbox, 1, 2);
		additionalOptionsLayout.setComponentAlignment(isLastSlideCheckbox,
				new Alignment(34));

		// feedbackComboBox
		feedbackComboBox = new ComboBox();
		feedbackComboBox.setImmediate(false);
		feedbackComboBox.setWidth("500px");
		feedbackComboBox.setHeight("-1px");
		additionalOptionsLayout.addComponent(feedbackComboBox, 1, 3);
		additionalOptionsLayout.setComponentAlignment(feedbackComboBox,
				new Alignment(34));

		return additionalOptionsLayout;
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
		moveUpRuleButton.setIcon(new ThemeResource(
				"img/arrow-up-icon-small.png"));
		moveUpRuleButton.setImmediate(true);
		moveUpRuleButton.setWidth("120px");
		moveUpRuleButton.setHeight("-1px");
		rulesButtonsLayout.addComponent(moveUpRuleButton);

		// moveDownRuleButton
		moveDownRuleButton = new Button();
		moveDownRuleButton.setCaption("!!! Move Down");
		moveDownRuleButton.setIcon(new ThemeResource(
				"img/arrow-down-icon-small.png"));
		moveDownRuleButton.setImmediate(true);
		moveDownRuleButton.setWidth("120px");
		moveDownRuleButton.setHeight("-1px");
		rulesButtonsLayout.addComponent(moveDownRuleButton);

		// levelDownButton
		levelDownButton = new Button();
		levelDownButton.setCaption("!!! Make Super");
		levelDownButton.setIcon(new ThemeResource(
				"img/arrow-left-icon-small.png"));
		levelDownButton.setImmediate(true);
		levelDownButton.setWidth("120px");
		levelDownButton.setHeight("-1px");
		rulesButtonsLayout.addComponent(levelDownButton);

		// levelUpButton
		levelUpButton = new Button();
		levelUpButton.setCaption("!!! Make Sub");
		levelUpButton.setIcon(new ThemeResource(
				"img/arrow-right-icon-small.png"));
		levelUpButton.setImmediate(true);
		levelUpButton.setWidth("120px");
		levelUpButton.setHeight("-1px");
		rulesButtonsLayout.addComponent(levelUpButton);

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
	private GridLayout buildCloseButtonLayout() {
		// common part: create layout
		closeButtonLayout = new GridLayout();
		closeButtonLayout.setImmediate(false);
		closeButtonLayout.setWidth("100.0%");
		closeButtonLayout.setHeight("-1px");
		closeButtonLayout.setMargin(true);
		closeButtonLayout.setSpacing(true);

		// closeButton
		closeButton = new Button();
		closeButton.setCaption("!!! Close");
		closeButton.setIcon(new ThemeResource("img/ok-icon-small.png"));
		closeButton.setImmediate(true);
		closeButton.setWidth("140px");
		closeButton.setHeight("-1px");
		closeButtonLayout.addComponent(closeButton, 0, 0);
		closeButtonLayout.setComponentAlignment(closeButton, new Alignment(48));

		return closeButtonLayout;
	}

}
