package org.isgf.mhc.ui.views.components.screening_survey;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.ui.views.components.AbstractModelObjectEditComponent;
import org.isgf.mhc.ui.views.components.basics.MediaObjectIntegrationComponentWithController;
import org.isgf.mhc.ui.views.components.basics.VariableTextFieldComponent;

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
		AbstractModelObjectEditComponent {

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
	private MediaObjectIntegrationComponentWithController	mediaObjectIntegrationComponentWithController;
	@AutoGenerated
	private Label											integratedMediaObjectLabel;
	@AutoGenerated
	private VariableTextFieldComponent						storeVariableTextFieldComponent;
	@AutoGenerated
	private Label											storeVariableLabel;
	@AutoGenerated
	private ComboBox										preselectedAnswerComboBox;
	@AutoGenerated
	private Label											preselectedAnswerLabel;
	@AutoGenerated
	private HorizontalLayout								answersButtionsLayout;
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
	private GridLayout										switchesLayoutGroup;
	@AutoGenerated
	private VariableTextFieldComponent						questionTextWithPlaceholdersTextField;
	@AutoGenerated
	private Label											questionTextWithPlaceholdersLabel;
	@AutoGenerated
	private VariableTextFieldComponent						optionalLayoutAttributeTextFieldComponent;
	@AutoGenerated
	private Label											optionalLayoutAttributeLabel;
	@AutoGenerated
	private ComboBox										questionTypeComboBox;
	@AutoGenerated
	private Label											questionTypeLabel;
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
		localize(
				questionTypeLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__QUESTION_TYPE);
		localize(
				optionalLayoutAttributeLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__OPTIONAL_LAYOUT_ATTRIBUTE);
		localize(
				questionTextWithPlaceholdersLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__QUESTION_TEXT);
		localize(
				preselectedAnswerLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__PRESELECTED_ANSWER);
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

		localize(newAnswerButton, AdminMessageStrings.GENERAL__NEW);
		localize(editAnswerAnswerButton,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__EDIT_ANSWER);
		localize(editAnswerValueButton,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__EDIT_VALUE);
		localize(moveUpAnswerButton, AdminMessageStrings.GENERAL__MOVE_UP);
		localize(moveDownAnswerButton, AdminMessageStrings.GENERAL__MOVE_DOWN);
		localize(deleteAnswerButton, AdminMessageStrings.GENERAL__DELETE);

		localize(newRuleButton, AdminMessageStrings.GENERAL__NEW);
		localize(editRuleButton, AdminMessageStrings.GENERAL__EDIT);
		localize(moveUpRuleButton, AdminMessageStrings.GENERAL__MOVE_UP);
		localize(moveDownRuleButton, AdminMessageStrings.GENERAL__MOVE_DOWN);
		localize(deleteRuleButton, AdminMessageStrings.GENERAL__DELETE);

		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);

		// set button start state
		setAnswerSelected(false);
		setRuleSelected(false);

		// adjust tables
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
		mainLayout.setWidth("800px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("800px");
		setHeight("-1px");

		// switchesLayoutGroup
		switchesLayoutGroup = buildSwitchesLayoutGroup();
		mainLayout.addComponent(switchesLayoutGroup);

		// answersTable
		answersTable = new Table();
		answersTable.setImmediate(false);
		answersTable.setWidth("100.0%");
		answersTable.setHeight("100px");
		mainLayout.addComponent(answersTable);

		// answersButtionsLayout
		answersButtionsLayout = buildAnswersButtionsLayout();
		mainLayout.addComponent(answersButtionsLayout);

		// additionalOptionsLayout
		additionalOptionsLayout = buildAdditionalOptionsLayout();
		mainLayout.addComponent(additionalOptionsLayout);

		// rulesTable
		rulesTable = new Table();
		rulesTable.setImmediate(false);
		rulesTable.setWidth("100.0%");
		rulesTable.setHeight("100px");
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

		// questionTypeLabel
		questionTypeLabel = new Label();
		questionTypeLabel.setImmediate(false);
		questionTypeLabel.setWidth("-1px");
		questionTypeLabel.setHeight("-1px");
		questionTypeLabel.setValue("!!! Question/slide type:");
		switchesLayoutGroup.addComponent(questionTypeLabel, 0, 1);

		// questionTypeComboBox
		questionTypeComboBox = new ComboBox();
		questionTypeComboBox.setImmediate(false);
		questionTypeComboBox.setWidth("350px");
		questionTypeComboBox.setHeight("-1px");
		switchesLayoutGroup.addComponent(questionTypeComboBox, 1, 1);
		switchesLayoutGroup.setComponentAlignment(questionTypeComboBox,
				new Alignment(6));

		// optionalLayoutAttributeLabel
		optionalLayoutAttributeLabel = new Label();
		optionalLayoutAttributeLabel.setImmediate(false);
		optionalLayoutAttributeLabel.setWidth("-1px");
		optionalLayoutAttributeLabel.setHeight("-1px");
		optionalLayoutAttributeLabel
				.setValue("!!! Optional layout attribute (e.g. CSS classes):");
		switchesLayoutGroup.addComponent(optionalLayoutAttributeLabel, 0, 2);

		// optionalLayoutAttributeTextFieldComponent
		optionalLayoutAttributeTextFieldComponent = new VariableTextFieldComponent();
		optionalLayoutAttributeTextFieldComponent.setImmediate(false);
		optionalLayoutAttributeTextFieldComponent.setWidth("350px");
		optionalLayoutAttributeTextFieldComponent.setHeight("-1px");
		switchesLayoutGroup.addComponent(
				optionalLayoutAttributeTextFieldComponent, 1, 2);
		switchesLayoutGroup.setComponentAlignment(
				optionalLayoutAttributeTextFieldComponent, new Alignment(6));

		// questionTextWithPlaceholdersLabel
		questionTextWithPlaceholdersLabel = new Label();
		questionTextWithPlaceholdersLabel.setImmediate(false);
		questionTextWithPlaceholdersLabel.setWidth("-1px");
		questionTextWithPlaceholdersLabel.setHeight("-1px");
		questionTextWithPlaceholdersLabel
				.setValue("!!! Question text (with placeholders):");
		switchesLayoutGroup.addComponent(questionTextWithPlaceholdersLabel, 0,
				3);

		// questionTextWithPlaceholdersTextField
		questionTextWithPlaceholdersTextField = new VariableTextFieldComponent();
		questionTextWithPlaceholdersTextField.setImmediate(false);
		questionTextWithPlaceholdersTextField.setWidth("350px");
		questionTextWithPlaceholdersTextField.setHeight("-1px");
		switchesLayoutGroup.addComponent(questionTextWithPlaceholdersTextField,
				1, 3);
		switchesLayoutGroup.setComponentAlignment(
				questionTextWithPlaceholdersTextField, new Alignment(6));

		return switchesLayoutGroup;
	}

	@AutoGenerated
	private HorizontalLayout buildAnswersButtionsLayout() {
		// common part: create layout
		answersButtionsLayout = new HorizontalLayout();
		answersButtionsLayout.setImmediate(false);
		answersButtionsLayout.setWidth("-1px");
		answersButtionsLayout.setHeight("-1px");
		answersButtionsLayout.setMargin(false);
		answersButtionsLayout.setSpacing(true);

		// newAnswerButton
		newAnswerButton = new Button();
		newAnswerButton.setCaption("!!! New");
		newAnswerButton.setIcon(new ThemeResource("img/add-icon-small.png"));
		newAnswerButton.setImmediate(true);
		newAnswerButton.setWidth("100px");
		newAnswerButton.setHeight("-1px");
		answersButtionsLayout.addComponent(newAnswerButton);

		// editAnswerAnswerButton
		editAnswerAnswerButton = new Button();
		editAnswerAnswerButton.setCaption("!!! Edit Answer");
		editAnswerAnswerButton.setImmediate(true);
		editAnswerAnswerButton.setWidth("100px");
		editAnswerAnswerButton.setHeight("-1px");
		answersButtionsLayout.addComponent(editAnswerAnswerButton);

		// editAnswerValueButton
		editAnswerValueButton = new Button();
		editAnswerValueButton.setCaption("!!! Edit Value");
		editAnswerValueButton.setImmediate(true);
		editAnswerValueButton.setWidth("100px");
		editAnswerValueButton.setHeight("-1px");
		answersButtionsLayout.addComponent(editAnswerValueButton);

		// moveUpAnswerButton
		moveUpAnswerButton = new Button();
		moveUpAnswerButton.setCaption("!!! Move Up");
		moveUpAnswerButton.setIcon(new ThemeResource(
				"img/arrow-up-icon-small.png"));
		moveUpAnswerButton.setImmediate(true);
		moveUpAnswerButton.setWidth("120px");
		moveUpAnswerButton.setHeight("-1px");
		answersButtionsLayout.addComponent(moveUpAnswerButton);

		// moveDownAnswerButton
		moveDownAnswerButton = new Button();
		moveDownAnswerButton.setCaption("!!! Move Down");
		moveDownAnswerButton.setIcon(new ThemeResource(
				"img/arrow-down-icon-small.png"));
		moveDownAnswerButton.setImmediate(true);
		moveDownAnswerButton.setWidth("120px");
		moveDownAnswerButton.setHeight("-1px");
		answersButtionsLayout.addComponent(moveDownAnswerButton);

		// deleteAnswerButton
		deleteAnswerButton = new Button();
		deleteAnswerButton.setCaption("!!! Delete");
		deleteAnswerButton.setIcon(new ThemeResource(
				"img/delete-icon-small.png"));
		deleteAnswerButton.setImmediate(true);
		deleteAnswerButton.setWidth("100px");
		deleteAnswerButton.setHeight("-1px");
		answersButtionsLayout.addComponent(deleteAnswerButton);

		return answersButtionsLayout;
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
		additionalOptionsLayout.setRows(5);

		// preselectedAnswerLabel
		preselectedAnswerLabel = new Label();
		preselectedAnswerLabel.setImmediate(false);
		preselectedAnswerLabel.setWidth("-1px");
		preselectedAnswerLabel.setHeight("-1px");
		preselectedAnswerLabel
				.setValue("!!! Preselected answer (if required):");
		additionalOptionsLayout.addComponent(preselectedAnswerLabel, 0, 0);

		// preselectedAnswerComboBox
		preselectedAnswerComboBox = new ComboBox();
		preselectedAnswerComboBox.setImmediate(false);
		preselectedAnswerComboBox.setWidth("500px");
		preselectedAnswerComboBox.setHeight("-1px");
		additionalOptionsLayout.addComponent(preselectedAnswerComboBox, 1, 0);
		additionalOptionsLayout.setComponentAlignment(
				preselectedAnswerComboBox, new Alignment(34));

		// storeVariableLabel
		storeVariableLabel = new Label();
		storeVariableLabel.setImmediate(false);
		storeVariableLabel.setWidth("-1px");
		storeVariableLabel.setHeight("-1px");
		storeVariableLabel
				.setValue("!!! Store result to variable (if required):");
		additionalOptionsLayout.addComponent(storeVariableLabel, 0, 1);

		// storeVariableTextFieldComponent
		storeVariableTextFieldComponent = new VariableTextFieldComponent();
		storeVariableTextFieldComponent.setImmediate(false);
		storeVariableTextFieldComponent.setWidth("500px");
		storeVariableTextFieldComponent.setHeight("-1px");
		additionalOptionsLayout.addComponent(storeVariableTextFieldComponent,
				1, 1);
		additionalOptionsLayout.setComponentAlignment(
				storeVariableTextFieldComponent, new Alignment(34));

		// integratedMediaObjectLabel
		integratedMediaObjectLabel = new Label();
		integratedMediaObjectLabel.setImmediate(false);
		integratedMediaObjectLabel.setWidth("-1px");
		integratedMediaObjectLabel.setHeight("-1px");
		integratedMediaObjectLabel.setValue("!!! Integrated media object:");
		additionalOptionsLayout.addComponent(integratedMediaObjectLabel, 0, 2);

		// mediaObjectIntegrationComponentWithController
		mediaObjectIntegrationComponentWithController = new MediaObjectIntegrationComponentWithController();
		mediaObjectIntegrationComponentWithController.setImmediate(false);
		mediaObjectIntegrationComponentWithController.setWidth("500px");
		mediaObjectIntegrationComponentWithController.setHeight("150px");
		additionalOptionsLayout.addComponent(
				mediaObjectIntegrationComponentWithController, 1, 2);
		additionalOptionsLayout.setComponentAlignment(
				mediaObjectIntegrationComponentWithController,
				new Alignment(34));

		// feedbackLabel
		feedbackLabel = new Label();
		feedbackLabel.setImmediate(false);
		feedbackLabel.setWidth("-1px");
		feedbackLabel.setHeight("-1px");
		feedbackLabel.setValue("!!! Finish or hand over to feedback:");
		additionalOptionsLayout.addComponent(feedbackLabel, 0, 3);

		// isLastSlideCheckbox
		isLastSlideCheckbox = new CheckBox();
		isLastSlideCheckbox
				.setCaption("!!! Stop screening survey after this slide (no rule execution as well)");
		isLastSlideCheckbox.setImmediate(false);
		isLastSlideCheckbox.setWidth("500px");
		isLastSlideCheckbox.setHeight("-1px");
		additionalOptionsLayout.addComponent(isLastSlideCheckbox, 1, 3);
		additionalOptionsLayout.setComponentAlignment(isLastSlideCheckbox,
				new Alignment(34));

		// feedbackComboBox
		feedbackComboBox = new ComboBox();
		feedbackComboBox.setImmediate(false);
		feedbackComboBox.setWidth("500px");
		feedbackComboBox.setHeight("-1px");
		additionalOptionsLayout.addComponent(feedbackComboBox, 1, 4);
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
