package ch.ethz.mc.ui.views.components.feedback;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab
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
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides the feedback slide rule edit component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class FeedbackSlideEditComponent extends AbstractClosableEditComponent {

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
	private Label											informationLabel;
	@AutoGenerated
	private GridLayout										additionalOptionsLayout;
	@AutoGenerated
	private MediaObjectIntegrationComponentWithController	integratedMediaObjectComponent;
	@AutoGenerated
	private Label											integratedMediaObjectLabel;
	@AutoGenerated
	private GridLayout										switchesLayoutGroup;
	@AutoGenerated
	private VariableTextFieldComponent						feedbackTextWithPlaceholdersTextField;
	@AutoGenerated
	private Label											feedbackTextWithPlaceholdersLabel;
	@AutoGenerated
	private VariableTextFieldComponent						optionalLayoutAttributeTextFieldComponent;
	@AutoGenerated
	private Label											commentLabel;
	@AutoGenerated
	private VariableTextFieldComponent						commentTextFieldComponent;
	@AutoGenerated
	private Label											optionalLayoutAttributeLabel;
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
	protected FeedbackSlideEditComponent() {
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
				optionalLayoutAttributeLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__OPTIONAL_LAYOUT_ATTRIBUTE_WITH_PLACEHOLDERS);
		localize(
				feedbackTextWithPlaceholdersLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__FEEDBACK_TEXT);
		localize(
				integratedMediaObjectLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__INTEGRATED_MEDIA_OBJECT);
		localize(
				informationLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_EDITING__FEEDBACK_INFORMATION_TEXT);

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

		// additionalOptionsLayout
		additionalOptionsLayout = buildAdditionalOptionsLayout();
		mainLayout.addComponent(additionalOptionsLayout);

		// informationLabel
		informationLabel = new Label();
		informationLabel.setStyleName("bold");
		informationLabel.setImmediate(false);
		informationLabel.setWidth("-1px");
		informationLabel.setHeight("-1px");
		informationLabel
				.setValue("!!! Slide will only be shown if the following rules are ALL TRUE:");
		mainLayout.addComponent(informationLabel);

		// rulesTable
		rulesTable = new Table();
		rulesTable.setImmediate(false);
		rulesTable.setWidth("100.0%");
		rulesTable.setHeight("150px");
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

		// optionalLayoutAttributeLabel
		optionalLayoutAttributeLabel = new Label();
		optionalLayoutAttributeLabel.setImmediate(false);
		optionalLayoutAttributeLabel.setWidth("-1px");
		optionalLayoutAttributeLabel.setHeight("-1px");
		optionalLayoutAttributeLabel
				.setValue("!!! Optional layout attribute (with placeholders, e.g. CSS classes):");
		switchesLayoutGroup.addComponent(optionalLayoutAttributeLabel, 0, 2);

		// commentTextFieldComponent
		commentTextFieldComponent = new VariableTextFieldComponent();
		commentTextFieldComponent.setImmediate(false);
		commentTextFieldComponent.setWidth("350px");
		commentTextFieldComponent.setHeight("-1px");
		switchesLayoutGroup.addComponent(commentTextFieldComponent, 1, 1);
		switchesLayoutGroup.setComponentAlignment(commentTextFieldComponent,
				new Alignment(6));

		// commentLabel
		commentLabel = new Label();
		commentLabel.setImmediate(false);
		commentLabel.setWidth("-1px");
		commentLabel.setHeight("-1px");
		commentLabel.setValue("!!! Comment");
		switchesLayoutGroup.addComponent(commentLabel, 0, 1);

		// optionalLayoutAttributeTextFieldComponent
		optionalLayoutAttributeTextFieldComponent = new VariableTextFieldComponent();
		optionalLayoutAttributeTextFieldComponent.setImmediate(false);
		optionalLayoutAttributeTextFieldComponent.setWidth("350px");
		optionalLayoutAttributeTextFieldComponent.setHeight("-1px");
		switchesLayoutGroup.addComponent(
				optionalLayoutAttributeTextFieldComponent, 1, 2);
		switchesLayoutGroup.setComponentAlignment(
				optionalLayoutAttributeTextFieldComponent, new Alignment(6));

		// feedbackTextWithPlaceholdersLabel
		feedbackTextWithPlaceholdersLabel = new Label();
		feedbackTextWithPlaceholdersLabel.setImmediate(false);
		feedbackTextWithPlaceholdersLabel.setWidth("-1px");
		feedbackTextWithPlaceholdersLabel.setHeight("-1px");
		feedbackTextWithPlaceholdersLabel
				.setValue("!!! Feedback text (with placeholders):");
		switchesLayoutGroup.addComponent(feedbackTextWithPlaceholdersLabel, 0,
				3);

		// feedbackTextWithPlaceholdersTextField
		feedbackTextWithPlaceholdersTextField = new VariableTextFieldComponent();
		feedbackTextWithPlaceholdersTextField.setImmediate(false);
		feedbackTextWithPlaceholdersTextField.setWidth("350px");
		feedbackTextWithPlaceholdersTextField.setHeight("-1px");
		switchesLayoutGroup.addComponent(feedbackTextWithPlaceholdersTextField,
				1, 3);
		switchesLayoutGroup.setComponentAlignment(
				feedbackTextWithPlaceholdersTextField, new Alignment(6));

		return switchesLayoutGroup;
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

		// integratedMediaObjectLabel
		integratedMediaObjectLabel = new Label();
		integratedMediaObjectLabel.setImmediate(false);
		integratedMediaObjectLabel.setWidth("-1px");
		integratedMediaObjectLabel.setHeight("-1px");
		integratedMediaObjectLabel.setValue("!!! Integrated media object:");
		additionalOptionsLayout.addComponent(integratedMediaObjectLabel, 0, 0);

		// integratedMediaObjectComponent
		integratedMediaObjectComponent = new MediaObjectIntegrationComponentWithController();
		integratedMediaObjectComponent.setImmediate(false);
		integratedMediaObjectComponent.setWidth("500px");
		integratedMediaObjectComponent.setHeight("150px");
		additionalOptionsLayout.addComponent(integratedMediaObjectComponent, 1,
				0);
		additionalOptionsLayout.setComponentAlignment(
				integratedMediaObjectComponent, new Alignment(34));

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
