package ch.ethz.mc.ui.views.components.interventions.monitoring_rules;

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
import ch.ethz.mc.conf.Messages;
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
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

/**
 * Provides a monitoring rule edit component
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class MonitoringRuleEditComponent extends AbstractClosableEditComponent {
	@AutoGenerated
	private VerticalLayout									mainLayout;

	@AutoGenerated
	private VerticalLayout									bottomLayout;

	@AutoGenerated
	private GridLayout										buttonLayout;

	@AutoGenerated
	private Button											closeButton;

	@AutoGenerated
	private TabSheet										replyRulesTabSheet;

	@AutoGenerated
	private VerticalLayout									replyRulesIfNoAnswerLayout;

	@AutoGenerated
	private MonitoringReplyRulesEditComponentWithController	monitoringReplyRulesEditComponentWithControllerIfNoAnswer;

	@AutoGenerated
	private Label											replyRulesIfNoAnswerLabel;

	@AutoGenerated
	private VerticalLayout									replyRulesIfAnswerLayout;

	@AutoGenerated
	private MonitoringReplyRulesEditComponentWithController	monitoringReplyRulesEditComponentWithControllerIfAnswer;

	@AutoGenerated
	private Label											replyRulesIfAnswerLabel;

	@AutoGenerated
	private VerticalLayout									topLayout;

	@AutoGenerated
	private VerticalLayout									switchesGroupLayout;

	@AutoGenerated
	private GridLayout										gridLayout2;

	@AutoGenerated
	private Slider											hoursUntilHandledAsNotAnsweredSlider;

	@AutoGenerated
	private Label											hoursUntilHandledAsNotAnsweredLabel;

	@AutoGenerated
	private Slider											hourToSendMessageSlider;

	@AutoGenerated
	private Label											hourToSendMessageLabel;

	@AutoGenerated
	private ComboBox										messageGroupComboBox;

	@AutoGenerated
	private Label											messageGroupLabel;

	@AutoGenerated
	private CheckBox										stopRuleExecutionAndFinishInterventionIfTrueComboBox;

	@AutoGenerated
	private CheckBox										stopRuleExecutionIfTrueComboBox;

	@AutoGenerated
	private GridLayout										gridLayout1;

	@AutoGenerated
	private VariableTextFieldComponent						storeVariableTextFieldComponent;

	@AutoGenerated
	private Label											storeVariableLabel;

	@AutoGenerated
	private AbstractRuleEditComponentWithController			abstractRuleEditComponentWithController;

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
	protected MonitoringRuleEditComponent() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// manually added
		localize(
				storeVariableLabel,
				AdminMessageStrings.MONITORING_MESSAGE_EDITING__STORE_RESULT_TO_VARIABLE);
		localize(
				stopRuleExecutionIfTrueComboBox,
				AdminMessageStrings.MONITORING_RULE_EDITING__SEND_MESSAGE_IF_TRUE);
		localize(
				stopRuleExecutionAndFinishInterventionIfTrueComboBox,
				AdminMessageStrings.MONITORING_RULE_EDITING__STOP_RULE_EXECUTION_AND_FINISH_INTERVENTION_IF_TRUE);
		localize(
				messageGroupLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__MESSAGE_GROUP_TO_SEND_MESSAGES_FROM);
		localize(
				hourToSendMessageLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__HOUR_TO_SEND_MESSAGE);
		localize(
				hoursUntilHandledAsNotAnsweredLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__HOURS_AFTER_SENDING_UNTIL_HANDLED_AS_NOT_ANSWERED);
		localize(
				replyRulesIfAnswerLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__EXECUTE_RULES_IF_ANSWER);
		localize(
				replyRulesIfNoAnswerLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__EXECUTE_RULES_IF_NO_ANSWER);
		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);

		replyRulesTabSheet
		.getTab(0)
		.setCaption(
				Messages.getAdminString(AdminMessageStrings.MONITORING_RULE_EDITING__EXECUTE_RULES_IF_ANSWER_SHORT));
		replyRulesTabSheet
		.getTab(1)
		.setCaption(
				Messages.getAdminString(AdminMessageStrings.MONITORING_RULE_EDITING__EXECUTE_RULES_IF_NO_ANSWER_SHORT));

		stopRuleExecutionIfTrueComboBox.setImmediate(true);
		stopRuleExecutionAndFinishInterventionIfTrueComboBox.setImmediate(true);

		messageGroupComboBox.setImmediate(true);
		messageGroupComboBox.setNullSelectionAllowed(true);
		messageGroupComboBox.setTextInputAllowed(false);
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("800px");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(false);

		// top-level component properties
		setWidth("800px");
		setHeight("-1px");

		// topLayout
		topLayout = buildTopLayout();
		mainLayout.addComponent(topLayout);

		// replyRulesTabSheet
		replyRulesTabSheet = buildReplyRulesTabSheet();
		mainLayout.addComponent(replyRulesTabSheet);

		// bottomLayout
		bottomLayout = buildBottomLayout();
		mainLayout.addComponent(bottomLayout);

		return mainLayout;
	}

	@AutoGenerated
	private VerticalLayout buildTopLayout() {
		// common part: create layout
		topLayout = new VerticalLayout();
		topLayout.setImmediate(false);
		topLayout.setWidth("100.0%");
		topLayout.setHeight("-1px");
		topLayout.setMargin(true);

		// abstractRuleEditComponentWithController
		abstractRuleEditComponentWithController = new AbstractRuleEditComponentWithController();
		abstractRuleEditComponentWithController.setImmediate(false);
		abstractRuleEditComponentWithController.setWidth("100.0%");
		abstractRuleEditComponentWithController.setHeight("-1px");
		topLayout.addComponent(abstractRuleEditComponentWithController);

		// switchesGroupLayout
		switchesGroupLayout = buildSwitchesGroupLayout();
		topLayout.addComponent(switchesGroupLayout);

		return topLayout;
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

		// stopRuleExecutionIfTrueComboBox
		stopRuleExecutionIfTrueComboBox = new CheckBox();
		stopRuleExecutionIfTrueComboBox.setStyleName("bold");
		stopRuleExecutionIfTrueComboBox
		.setCaption("!!! Stop rule execution and send message if rule result is TRUE");
		stopRuleExecutionIfTrueComboBox.setImmediate(false);
		stopRuleExecutionIfTrueComboBox.setWidth("100.0%");
		stopRuleExecutionIfTrueComboBox.setHeight("-1px");
		switchesGroupLayout.addComponent(stopRuleExecutionIfTrueComboBox);

		// stopRuleExecutionAndFinishInterventionIfTrueComboBox
		stopRuleExecutionAndFinishInterventionIfTrueComboBox = new CheckBox();
		stopRuleExecutionAndFinishInterventionIfTrueComboBox
		.setStyleName("bold");
		stopRuleExecutionAndFinishInterventionIfTrueComboBox
		.setCaption("!!! Stop rule execution and finish intervention for this participant if rule result is TRUE");
		stopRuleExecutionAndFinishInterventionIfTrueComboBox
		.setImmediate(false);
		stopRuleExecutionAndFinishInterventionIfTrueComboBox.setWidth("100.0%");
		stopRuleExecutionAndFinishInterventionIfTrueComboBox.setHeight("-1px");
		switchesGroupLayout
		.addComponent(stopRuleExecutionAndFinishInterventionIfTrueComboBox);

		// gridLayout2
		gridLayout2 = buildGridLayout2();
		switchesGroupLayout.addComponent(gridLayout2);

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
		storeVariableTextFieldComponent.setWidth("350px");
		storeVariableTextFieldComponent.setHeight("-1px");
		gridLayout1.addComponent(storeVariableTextFieldComponent, 1, 0);
		gridLayout1.setComponentAlignment(storeVariableTextFieldComponent,
				new Alignment(34));

		return gridLayout1;
	}

	@AutoGenerated
	private GridLayout buildGridLayout2() {
		// common part: create layout
		gridLayout2 = new GridLayout();
		gridLayout2.setImmediate(false);
		gridLayout2.setWidth("100.0%");
		gridLayout2.setHeight("-1px");
		gridLayout2.setMargin(false);
		gridLayout2.setSpacing(true);
		gridLayout2.setColumns(2);
		gridLayout2.setRows(3);

		// messageGroupLabel
		messageGroupLabel = new Label();
		messageGroupLabel.setImmediate(false);
		messageGroupLabel.setWidth("-1px");
		messageGroupLabel.setHeight("-1px");
		messageGroupLabel.setValue("!!! Message group to send messages from:");
		gridLayout2.addComponent(messageGroupLabel, 0, 0);

		// messageGroupComboBox
		messageGroupComboBox = new ComboBox();
		messageGroupComboBox.setImmediate(false);
		messageGroupComboBox.setWidth("350px");
		messageGroupComboBox.setHeight("-1px");
		gridLayout2.addComponent(messageGroupComboBox, 1, 0);
		gridLayout2.setComponentAlignment(messageGroupComboBox, new Alignment(
				34));

		// hourToSendMessageLabel
		hourToSendMessageLabel = new Label();
		hourToSendMessageLabel.setImmediate(false);
		hourToSendMessageLabel.setWidth("-1px");
		hourToSendMessageLabel.setHeight("-1px");
		hourToSendMessageLabel.setValue("!!! Hour to send message:");
		gridLayout2.addComponent(hourToSendMessageLabel, 0, 1);

		// hourToSendMessageSlider
		hourToSendMessageSlider = new Slider();
		hourToSendMessageSlider.setImmediate(false);
		hourToSendMessageSlider.setWidth("350px");
		hourToSendMessageSlider.setHeight("-1px");
		gridLayout2.addComponent(hourToSendMessageSlider, 1, 1);
		gridLayout2.setComponentAlignment(hourToSendMessageSlider,
				new Alignment(34));

		// hoursUntilHandledAsNotAnsweredLabel
		hoursUntilHandledAsNotAnsweredLabel = new Label();
		hoursUntilHandledAsNotAnsweredLabel.setImmediate(false);
		hoursUntilHandledAsNotAnsweredLabel.setWidth("-1px");
		hoursUntilHandledAsNotAnsweredLabel.setHeight("-1px");
		hoursUntilHandledAsNotAnsweredLabel
		.setValue("!!! Hours until handled as not answered:");
		gridLayout2.addComponent(hoursUntilHandledAsNotAnsweredLabel, 0, 2);

		// hoursUntilHandledAsNotAnsweredSlider
		hoursUntilHandledAsNotAnsweredSlider = new Slider();
		hoursUntilHandledAsNotAnsweredSlider.setImmediate(false);
		hoursUntilHandledAsNotAnsweredSlider.setWidth("350px");
		hoursUntilHandledAsNotAnsweredSlider.setHeight("-1px");
		gridLayout2.addComponent(hoursUntilHandledAsNotAnsweredSlider, 1, 2);
		gridLayout2.setComponentAlignment(hoursUntilHandledAsNotAnsweredSlider,
				new Alignment(34));

		return gridLayout2;
	}

	@AutoGenerated
	private TabSheet buildReplyRulesTabSheet() {
		// common part: create layout
		replyRulesTabSheet = new TabSheet();
		replyRulesTabSheet.setImmediate(true);
		replyRulesTabSheet.setWidth("100.0%");
		replyRulesTabSheet.setHeight("-1px");

		// replyRulesIfAnswerLayout
		replyRulesIfAnswerLayout = buildReplyRulesIfAnswerLayout();
		replyRulesTabSheet.addTab(replyRulesIfAnswerLayout,
				"!!! Execute on answer", null);

		// replyRulesIfNoAnswerLayout
		replyRulesIfNoAnswerLayout = buildReplyRulesIfNoAnswerLayout();
		replyRulesTabSheet.addTab(replyRulesIfNoAnswerLayout,
				"!!! Execute on NO answer", null);

		return replyRulesTabSheet;
	}

	@AutoGenerated
	private VerticalLayout buildReplyRulesIfAnswerLayout() {
		// common part: create layout
		replyRulesIfAnswerLayout = new VerticalLayout();
		replyRulesIfAnswerLayout.setImmediate(false);
		replyRulesIfAnswerLayout.setWidth("100.0%");
		replyRulesIfAnswerLayout.setHeight("-1px");
		replyRulesIfAnswerLayout.setMargin(true);

		// replyRulesIfAnswerLabel
		replyRulesIfAnswerLabel = new Label();
		replyRulesIfAnswerLabel.setStyleName("bold indent-left");
		replyRulesIfAnswerLabel.setImmediate(false);
		replyRulesIfAnswerLabel.setWidth("-1px");
		replyRulesIfAnswerLabel.setHeight("-1px");
		replyRulesIfAnswerLabel
		.setValue("!!! Execute these rules if participant does answer:");
		replyRulesIfAnswerLayout.addComponent(replyRulesIfAnswerLabel);

		// monitoringReplyRulesEditComponentWithControllerIfAnswer
		monitoringReplyRulesEditComponentWithControllerIfAnswer = new MonitoringReplyRulesEditComponentWithController();
		monitoringReplyRulesEditComponentWithControllerIfAnswer
		.setImmediate(false);
		monitoringReplyRulesEditComponentWithControllerIfAnswer
		.setWidth("100.0%");
		monitoringReplyRulesEditComponentWithControllerIfAnswer
		.setHeight("-1px");
		replyRulesIfAnswerLayout
		.addComponent(monitoringReplyRulesEditComponentWithControllerIfAnswer);

		return replyRulesIfAnswerLayout;
	}

	@AutoGenerated
	private VerticalLayout buildReplyRulesIfNoAnswerLayout() {
		// common part: create layout
		replyRulesIfNoAnswerLayout = new VerticalLayout();
		replyRulesIfNoAnswerLayout.setImmediate(false);
		replyRulesIfNoAnswerLayout.setWidth("100.0%");
		replyRulesIfNoAnswerLayout.setHeight("-1px");
		replyRulesIfNoAnswerLayout.setMargin(true);

		// replyRulesIfNoAnswerLabel
		replyRulesIfNoAnswerLabel = new Label();
		replyRulesIfNoAnswerLabel.setStyleName("bold indent-left");
		replyRulesIfNoAnswerLabel.setImmediate(false);
		replyRulesIfNoAnswerLabel.setWidth("-1px");
		replyRulesIfNoAnswerLabel.setHeight("-1px");
		replyRulesIfNoAnswerLabel
		.setValue("!!! Execute these rules if participant does NOT answer:");
		replyRulesIfNoAnswerLayout.addComponent(replyRulesIfNoAnswerLabel);

		// monitoringReplyRulesEditComponentWithControllerIfNoAnswer
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer = new MonitoringReplyRulesEditComponentWithController();
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer
		.setImmediate(false);
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer
		.setWidth("100.0%");
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer
		.setHeight("-1px");
		replyRulesIfNoAnswerLayout
		.addComponent(monitoringReplyRulesEditComponentWithControllerIfNoAnswer);

		return replyRulesIfNoAnswerLayout;
	}

	@AutoGenerated
	private VerticalLayout buildBottomLayout() {
		// common part: create layout
		bottomLayout = new VerticalLayout();
		bottomLayout.setImmediate(false);
		bottomLayout.setWidth("100.0%");
		bottomLayout.setHeight("-1px");
		bottomLayout.setMargin(false);

		// buttonLayout
		buttonLayout = buildButtonLayout();
		bottomLayout.addComponent(buttonLayout);

		return bottomLayout;
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
