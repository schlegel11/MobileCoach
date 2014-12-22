package ch.ethz.mc.ui.views.components.screening_survey;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
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
import ch.ethz.mc.ui.views.components.basics.AbstractRuleEditComponentWithController;
import ch.ethz.mc.ui.views.components.basics.VariableTextFieldComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides a screening survey slide rule edit component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class ScreeningSurveySlideRuleEditComponent extends
		AbstractClosableEditComponent {
	@AutoGenerated
	private VerticalLayout							mainLayout;

	@AutoGenerated
	private GridLayout								buttonLayout;

	@AutoGenerated
	private Button									closeButton;

	@AutoGenerated
	private GridLayout								jumpLayout;

	@AutoGenerated
	private CheckBox								invalidWhenTrueCheckbox;

	@AutoGenerated
	private ComboBox								jumpIfFalseComboBox;

	@AutoGenerated
	private Label									jumpIfFalseLabel;

	@AutoGenerated
	private ComboBox								jumpIfTrueComboBox;

	@AutoGenerated
	private Label									jumpIfTrueLabel;

	@AutoGenerated
	private HorizontalLayout						horizontalLayout_1;

	@AutoGenerated
	private VariableTextFieldComponent				storeVariableTextFieldComponent;

	@AutoGenerated
	private Embedded								arrowImage;

	@AutoGenerated
	private VariableTextFieldComponent				storeValueTextFieldComponent;

	@AutoGenerated
	private VerticalLayout							variableLabelLayout;

	@AutoGenerated
	private Label									storeVariableLabel;

	@AutoGenerated
	private AbstractRuleEditComponentWithController	abstractRuleEditComponentWithController;

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@Override
	public void registerOkButtonListener(final ClickListener clickListener) {
		closeButton.addClickListener(clickListener);
	}

	@Override
	public void registerCancelButtonListener(final ClickListener clickListener) {
		// not required
	}

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 * 
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	protected ScreeningSurveySlideRuleEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(
				storeVariableLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_RULE_EDITING__STORE_VALUE_TO_VARIABLE);
		localize(
				invalidWhenTrueCheckbox,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_RULE_EDITING__INVALID_WHEN_TRUE);
		localize(
				jumpIfTrueLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_RULE_EDITING__JUMP_TO_SLIDE_IF_TRUE);
		localize(
				jumpIfFalseLabel,
				AdminMessageStrings.SCREENING_SURVEY_SLIDE_RULE_EDITING__JUMP_TO_SLIDE_IF_FALSE);

		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);

		jumpIfTrueComboBox.setImmediate(true);
		jumpIfTrueComboBox.setNullSelectionAllowed(true);
		jumpIfTrueComboBox.setTextInputAllowed(false);
		jumpIfFalseComboBox.setImmediate(true);
		jumpIfFalseComboBox.setNullSelectionAllowed(true);
		jumpIfFalseComboBox.setTextInputAllowed(false);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("700px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);

		// top-level component properties
		setWidth("700px");
		setHeight("-1px");

		// abstractRuleEditComponentWithController
		abstractRuleEditComponentWithController = new AbstractRuleEditComponentWithController();
		abstractRuleEditComponentWithController.setImmediate(false);
		abstractRuleEditComponentWithController.setWidth("100.0%");
		abstractRuleEditComponentWithController.setHeight("-1px");
		mainLayout.addComponent(abstractRuleEditComponentWithController);

		// variableLabelLayout
		variableLabelLayout = buildVariableLabelLayout();
		mainLayout.addComponent(variableLabelLayout);

		// horizontalLayout_1
		horizontalLayout_1 = buildHorizontalLayout_1();
		mainLayout.addComponent(horizontalLayout_1);
		mainLayout.setComponentAlignment(horizontalLayout_1, new Alignment(48));

		// jumpLayout
		jumpLayout = buildJumpLayout();
		mainLayout.addComponent(jumpLayout);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

		return mainLayout;
	}

	@AutoGenerated
	private VerticalLayout buildVariableLabelLayout() {
		// common part: create layout
		variableLabelLayout = new VerticalLayout();
		variableLabelLayout.setImmediate(false);
		variableLabelLayout.setWidth("100.0%");
		variableLabelLayout.setHeight("-1px");
		variableLabelLayout.setMargin(true);

		// storeVariableLabel
		storeVariableLabel = new Label();
		storeVariableLabel.setImmediate(false);
		storeVariableLabel.setWidth("-1px");
		storeVariableLabel.setHeight("-1px");
		storeVariableLabel
				.setValue("!!! Store fix value (if assigned) OR rule result to variable (if required and only if rule is TRUE):");
		variableLabelLayout.addComponent(storeVariableLabel);

		return variableLabelLayout;
	}

	@AutoGenerated
	private HorizontalLayout buildHorizontalLayout_1() {
		// common part: create layout
		horizontalLayout_1 = new HorizontalLayout();
		horizontalLayout_1.setImmediate(false);
		horizontalLayout_1.setWidth("100.0%");
		horizontalLayout_1.setHeight("-1px");
		horizontalLayout_1.setMargin(true);

		// storeValueTextFieldComponent
		storeValueTextFieldComponent = new VariableTextFieldComponent();
		storeValueTextFieldComponent.setImmediate(false);
		storeValueTextFieldComponent.setWidth("240px");
		storeValueTextFieldComponent.setHeight("-1px");
		horizontalLayout_1.addComponent(storeValueTextFieldComponent);
		horizontalLayout_1.setExpandRatio(storeValueTextFieldComponent, 1.0f);

		// arrowImage
		arrowImage = new Embedded();
		arrowImage.setImmediate(false);
		arrowImage.setWidth("32px");
		arrowImage.setHeight("32px");
		arrowImage.setSource(new ThemeResource("img/arrow-right-icon.png"));
		arrowImage.setType(1);
		arrowImage.setMimeType("image/png");
		horizontalLayout_1.addComponent(arrowImage);
		horizontalLayout_1.setComponentAlignment(arrowImage, new Alignment(48));

		// storeVariableTextFieldComponent
		storeVariableTextFieldComponent = new VariableTextFieldComponent();
		storeVariableTextFieldComponent.setImmediate(false);
		storeVariableTextFieldComponent.setWidth("240px");
		storeVariableTextFieldComponent.setHeight("-1px");
		horizontalLayout_1.addComponent(storeVariableTextFieldComponent);
		horizontalLayout_1
				.setExpandRatio(storeVariableTextFieldComponent, 1.0f);
		horizontalLayout_1.setComponentAlignment(
				storeVariableTextFieldComponent, new Alignment(6));

		return horizontalLayout_1;
	}

	@AutoGenerated
	private GridLayout buildJumpLayout() {
		// common part: create layout
		jumpLayout = new GridLayout();
		jumpLayout.setImmediate(false);
		jumpLayout.setWidth("100.0%");
		jumpLayout.setHeight("-1px");
		jumpLayout.setMargin(true);
		jumpLayout.setSpacing(true);
		jumpLayout.setColumns(2);
		jumpLayout.setRows(4);

		// jumpIfTrueLabel
		jumpIfTrueLabel = new Label();
		jumpIfTrueLabel.setImmediate(false);
		jumpIfTrueLabel.setWidth("-1px");
		jumpIfTrueLabel.setHeight("-1px");
		jumpIfTrueLabel.setValue("!!! Jump to slide when rule is TRUE:");
		jumpLayout.addComponent(jumpIfTrueLabel, 0, 1);

		// jumpIfTrueComboBox
		jumpIfTrueComboBox = new ComboBox();
		jumpIfTrueComboBox.setImmediate(false);
		jumpIfTrueComboBox.setWidth("300px");
		jumpIfTrueComboBox.setHeight("-1px");
		jumpLayout.addComponent(jumpIfTrueComboBox, 1, 1);
		jumpLayout.setComponentAlignment(jumpIfTrueComboBox, new Alignment(34));

		// jumpIfFalseLabel
		jumpIfFalseLabel = new Label();
		jumpIfFalseLabel.setImmediate(false);
		jumpIfFalseLabel.setWidth("-1px");
		jumpIfFalseLabel.setHeight("-1px");
		jumpIfFalseLabel.setValue("!!! Jump to slide when rule is FALSE:");
		jumpLayout.addComponent(jumpIfFalseLabel, 0, 2);

		// jumpIfFalseComboBox
		jumpIfFalseComboBox = new ComboBox();
		jumpIfFalseComboBox.setImmediate(false);
		jumpIfFalseComboBox.setWidth("300px");
		jumpIfFalseComboBox.setHeight("-1px");
		jumpLayout.addComponent(jumpIfFalseComboBox, 1, 2);
		jumpLayout
				.setComponentAlignment(jumpIfFalseComboBox, new Alignment(34));

		// invalidWhenTrueCheckbox
		invalidWhenTrueCheckbox = new CheckBox();
		invalidWhenTrueCheckbox
				.setCaption("!!! Show same slide again if rule is TRUE (invalid value)");
		invalidWhenTrueCheckbox.setImmediate(false);
		invalidWhenTrueCheckbox.setWidth("-1px");
		invalidWhenTrueCheckbox.setHeight("-1px");
		jumpLayout.addComponent(invalidWhenTrueCheckbox, 0, 0);

		return jumpLayout;
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
