package ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.ui.components.AbstractClosableEditComponent;
import ch.ethz.mc.ui.components.basics.AbstractRuleEditComponentWithController;
import ch.ethz.mc.ui.components.basics.VariableTextFieldComponent;
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

/**
 * Provides a monitoring reply rule edit component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class MicroDialogRuleEditComponent
		extends AbstractClosableEditComponent {
	@AutoGenerated
	private VerticalLayout							mainLayout;

	@AutoGenerated
	private GridLayout								buttonLayout;

	@AutoGenerated
	private Button									closeButton;

	@AutoGenerated
	private VerticalLayout							switchesGroupLayout;

	@AutoGenerated
	private GridLayout								jumpGridLayout;

	@AutoGenerated
	private ComboBox								nextMicroDialogMessageWhenFalseComboBox;

	@AutoGenerated
	private Label									nextMicroDialogMessageWhenFalseLabel;

	@AutoGenerated
	private ComboBox								nextMicroDialogMessageWhenTrueComboBox;

	@AutoGenerated
	private Label									nextMicroDialogMessageWhenTrueLabel;

	@AutoGenerated
	private CheckBox								stopMicroDialogWhenTrueCheckBox;

	@AutoGenerated
	private CheckBox								leaveDecisionPointWhenTrueCheckBox;

	@AutoGenerated
	private GridLayout								gridLayout1;

	@AutoGenerated
	private VariableTextFieldComponent				storeVariableTextFieldComponent;

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
	protected MicroDialogRuleEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(storeVariableLabel,
				AdminMessageStrings.MICRO_DIALOG_RULE_EDITING__STORE_RESULT_TO_VARIABLE);
		localize(stopMicroDialogWhenTrueCheckBox,
				AdminMessageStrings.MICRO_DIALOG_RULE_EDITING__STOP_MICRO_DIALOG_WHEN_TRUE);
		localize(leaveDecisionPointWhenTrueCheckBox,
				AdminMessageStrings.MICRO_DIALOG_RULE_EDITING__LEAVE_DECISION_POINT_WHEN_TRUE);
		localize(nextMicroDialogMessageWhenTrueLabel,
				AdminMessageStrings.MICRO_DIALOG_RULE_EDITING__NEXT_MICRO_DIALOG_MESSAGE_WHEN_TRUE);
		localize(nextMicroDialogMessageWhenFalseLabel,
				AdminMessageStrings.MICRO_DIALOG_RULE_EDITING__NEXT_MICRO_DIALOG_MESSAGE_WHEN_FALSE);

		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);

		stopMicroDialogWhenTrueCheckBox.setImmediate(true);
		leaveDecisionPointWhenTrueCheckBox.setImmediate(true);

		nextMicroDialogMessageWhenTrueComboBox.setImmediate(true);
		nextMicroDialogMessageWhenTrueComboBox.setNullSelectionAllowed(true);
		nextMicroDialogMessageWhenTrueComboBox.setTextInputAllowed(false);
		nextMicroDialogMessageWhenFalseComboBox.setImmediate(true);
		nextMicroDialogMessageWhenFalseComboBox.setNullSelectionAllowed(true);
		nextMicroDialogMessageWhenFalseComboBox.setTextInputAllowed(false);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("1050px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(true);

		// top-level component properties
		setWidth("1050px");
		setHeight("-1px");

		// abstractRuleEditComponentWithController
		abstractRuleEditComponentWithController = new AbstractRuleEditComponentWithController();
		abstractRuleEditComponentWithController.setImmediate(false);
		abstractRuleEditComponentWithController.setWidth("100.0%");
		abstractRuleEditComponentWithController.setHeight("-1px");
		mainLayout.addComponent(abstractRuleEditComponentWithController);

		// switchesGroupLayout
		switchesGroupLayout = buildSwitchesGroupLayout();
		mainLayout.addComponent(switchesGroupLayout);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);

		return mainLayout;
	}

	@AutoGenerated
	private VerticalLayout buildSwitchesGroupLayout() {
		// common part: create layout
		switchesGroupLayout = new VerticalLayout();
		switchesGroupLayout.setImmediate(false);
		switchesGroupLayout.setWidth("100.0%");
		switchesGroupLayout.setHeight("-1px");
		switchesGroupLayout.setMargin(true);
		switchesGroupLayout.setSpacing(true);

		// gridLayout1
		gridLayout1 = buildGridLayout1();
		switchesGroupLayout.addComponent(gridLayout1);

		// leaveDecisionPointWhenTrueCheckBox
		leaveDecisionPointWhenTrueCheckBox = new CheckBox();
		leaveDecisionPointWhenTrueCheckBox.setStyleName("bold");
		leaveDecisionPointWhenTrueCheckBox.setCaption(
				"!!! Leave this decision point after this rule if rule result is TRUE");
		leaveDecisionPointWhenTrueCheckBox.setImmediate(false);
		leaveDecisionPointWhenTrueCheckBox.setWidth("100.0%");
		leaveDecisionPointWhenTrueCheckBox.setHeight("-1px");
		switchesGroupLayout.addComponent(leaveDecisionPointWhenTrueCheckBox);

		// stopMicroDialogWhenTrueCheckBox
		stopMicroDialogWhenTrueCheckBox = new CheckBox();
		stopMicroDialogWhenTrueCheckBox.setStyleName("bold");
		stopMicroDialogWhenTrueCheckBox.setCaption(
				"!!! Stop this complete micro dialog after this rule if rule result is TRUE");
		stopMicroDialogWhenTrueCheckBox.setImmediate(false);
		stopMicroDialogWhenTrueCheckBox.setWidth("100.0%");
		stopMicroDialogWhenTrueCheckBox.setHeight("-1px");
		switchesGroupLayout.addComponent(stopMicroDialogWhenTrueCheckBox);

		// jumpGridLayout
		jumpGridLayout = buildJumpGridLayout();
		switchesGroupLayout.addComponent(jumpGridLayout);

		return switchesGroupLayout;
	}

	@AutoGenerated
	private GridLayout buildGridLayout1() {
		// common part: create layout
		gridLayout1 = new GridLayout();
		gridLayout1.setImmediate(false);
		gridLayout1.setWidth("100.0%");
		gridLayout1.setHeight("-1px");
		gridLayout1.setMargin(false);
		gridLayout1.setSpacing(true);
		gridLayout1.setColumns(2);

		// storeVariableLabel
		storeVariableLabel = new Label();
		storeVariableLabel.setImmediate(false);
		storeVariableLabel.setWidth("-1px");
		storeVariableLabel.setHeight("-1px");
		storeVariableLabel
				.setValue("!!! Store result to variable (if required):");
		gridLayout1.addComponent(storeVariableLabel, 0, 0);

		// storeVariableTextFieldComponent
		storeVariableTextFieldComponent = new VariableTextFieldComponent();
		storeVariableTextFieldComponent.setImmediate(false);
		storeVariableTextFieldComponent.setWidth("400px");
		storeVariableTextFieldComponent.setHeight("-1px");
		gridLayout1.addComponent(storeVariableTextFieldComponent, 1, 0);
		gridLayout1.setComponentAlignment(storeVariableTextFieldComponent,
				new Alignment(34));

		return gridLayout1;
	}

	@AutoGenerated
	private GridLayout buildJumpGridLayout() {
		// common part: create layout
		jumpGridLayout = new GridLayout();
		jumpGridLayout.setImmediate(false);
		jumpGridLayout.setWidth("100.0%");
		jumpGridLayout.setHeight("-1px");
		jumpGridLayout.setMargin(false);
		jumpGridLayout.setSpacing(true);
		jumpGridLayout.setColumns(2);
		jumpGridLayout.setRows(2);

		// nextMicroDialogMessageWhenTrueLabel
		nextMicroDialogMessageWhenTrueLabel = new Label();
		nextMicroDialogMessageWhenTrueLabel.setImmediate(false);
		nextMicroDialogMessageWhenTrueLabel.setWidth("-1px");
		nextMicroDialogMessageWhenTrueLabel.setHeight("-1px");
		nextMicroDialogMessageWhenTrueLabel
				.setValue("!!! Jump to message if TRUE:");
		jumpGridLayout.addComponent(nextMicroDialogMessageWhenTrueLabel, 0, 0);

		// nextMicroDialogMessageWhenTrueComboBox
		nextMicroDialogMessageWhenTrueComboBox = new ComboBox();
		nextMicroDialogMessageWhenTrueComboBox.setImmediate(false);
		nextMicroDialogMessageWhenTrueComboBox.setWidth("400px");
		nextMicroDialogMessageWhenTrueComboBox.setHeight("-1px");
		jumpGridLayout.addComponent(nextMicroDialogMessageWhenTrueComboBox, 1,
				0);
		jumpGridLayout.setComponentAlignment(
				nextMicroDialogMessageWhenTrueComboBox, new Alignment(34));

		// nextMicroDialogMessageWhenFalseLabel
		nextMicroDialogMessageWhenFalseLabel = new Label();
		nextMicroDialogMessageWhenFalseLabel.setImmediate(false);
		nextMicroDialogMessageWhenFalseLabel.setWidth("-1px");
		nextMicroDialogMessageWhenFalseLabel.setHeight("-1px");
		nextMicroDialogMessageWhenFalseLabel
				.setValue("!!! Jump to message if TRUE:");
		jumpGridLayout.addComponent(nextMicroDialogMessageWhenFalseLabel, 0, 1);

		// nextMicroDialogMessageWhenFalseComboBox
		nextMicroDialogMessageWhenFalseComboBox = new ComboBox();
		nextMicroDialogMessageWhenFalseComboBox.setImmediate(false);
		nextMicroDialogMessageWhenFalseComboBox.setWidth("400px");
		nextMicroDialogMessageWhenFalseComboBox.setHeight("-1px");
		jumpGridLayout.addComponent(nextMicroDialogMessageWhenFalseComboBox, 1,
				1);
		jumpGridLayout.setComponentAlignment(
				nextMicroDialogMessageWhenFalseComboBox, new Alignment(34));

		return jumpGridLayout;
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
