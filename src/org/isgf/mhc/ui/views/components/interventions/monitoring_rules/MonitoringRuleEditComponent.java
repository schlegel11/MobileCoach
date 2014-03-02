package org.isgf.mhc.ui.views.components.interventions.monitoring_rules;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.ui.views.components.AbstractModelObjectEditComponent;
import org.isgf.mhc.ui.views.components.basics.AbstractRuleEditComponentWithController;
import org.isgf.mhc.ui.views.components.basics.VariableTextFieldComponent;

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
import com.vaadin.ui.VerticalLayout;

/**
 * Provides a monitoring rule edit component
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
public class MonitoringRuleEditComponent extends
		AbstractModelObjectEditComponent {
	@AutoGenerated
	private VerticalLayout									mainLayout;

	@AutoGenerated
	private GridLayout										buttonLayout;

	@AutoGenerated
	private Button											closeButton;

	@AutoGenerated
	private MonitoringReplyRulesEditComponentWithController	monitoringReplyRulesEditComponentWithControllerIfNoAnswer;

	@AutoGenerated
	private VerticalLayout									replyRulesIfNoAnswerLabelLayout;

	@AutoGenerated
	private Label											replyRulesIfNoAnswerLabel;

	@AutoGenerated
	private MonitoringReplyRulesEditComponentWithController	monitoringReplyRulesEditComponentWithControllerIfAnswer;

	@AutoGenerated
	private VerticalLayout									replyRulesIfAnswerLabelLayout;

	@AutoGenerated
	private Label											replyRulesIfAnswerLabel;

	@AutoGenerated
	private VerticalLayout									switchesGroupLayout;

	@AutoGenerated
	private GridLayout										gridLayout2;

	@AutoGenerated
	private Slider											hoursUntillHandledAsNotAnsweredSlider;

	@AutoGenerated
	private Label											hoursUntillHandledAsNotAnsweredLabel;

	@AutoGenerated
	private Slider											hourToSendMessageSlider;

	@AutoGenerated
	private Label											hourToSendMessageLabel;

	@AutoGenerated
	private ComboBox										messageGroupComboBox;

	@AutoGenerated
	private Label											messageGroupLabel;

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
				AdminMessageStrings.MONITORING_RULE_EDITING__STOP_RULE_EXECUTION_AND_SEND_MESSAGE_IF_TRUE);
		localize(
				messageGroupLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__MESSAGE_GROUP_TO_SEND_MESSAGES_FROM);
		localize(
				hourToSendMessageLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__HOUR_TO_SEND_MESSAGE);
		localize(
				hoursUntillHandledAsNotAnsweredLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__HOURS_AFTER_SENDING_UNTIL_HANDLED_AS_NOT_ANSWERED);
		localize(
				replyRulesIfAnswerLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__EXECUTE_RULES_IF_ANSWER);
		localize(
				replyRulesIfNoAnswerLabel,
				AdminMessageStrings.MONITORING_RULE_EDITING__EXECUTE_RULES_IF_NO_ANSWER);
		localize(closeButton, AdminMessageStrings.GENERAL__CLOSE);

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
		mainLayout.setMargin(true);

		// top-level component properties
		setWidth("800px");
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

		// replyRulesIfAnswerLabelLayout
		replyRulesIfAnswerLabelLayout = buildReplyRulesIfAnswerLabelLayout();
		mainLayout.addComponent(replyRulesIfAnswerLabelLayout);

		// monitoringReplyRulesEditComponentWithControllerIfAnswer
		monitoringReplyRulesEditComponentWithControllerIfAnswer = new MonitoringReplyRulesEditComponentWithController();
		monitoringReplyRulesEditComponentWithControllerIfAnswer
				.setImmediate(false);
		monitoringReplyRulesEditComponentWithControllerIfAnswer
				.setWidth("100.0%");
		monitoringReplyRulesEditComponentWithControllerIfAnswer
				.setHeight("-1px");
		mainLayout
				.addComponent(monitoringReplyRulesEditComponentWithControllerIfAnswer);

		// replyRulesIfNoAnswerLabelLayout
		replyRulesIfNoAnswerLabelLayout = buildReplyRulesIfNoAnswerLabelLayout();
		mainLayout.addComponent(replyRulesIfNoAnswerLabelLayout);

		// monitoringReplyRulesEditComponentWithControllerIfNoAnswer
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer = new MonitoringReplyRulesEditComponentWithController();
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer
				.setImmediate(false);
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer
				.setWidth("100.0%");
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer
				.setHeight("-1px");
		mainLayout
				.addComponent(monitoringReplyRulesEditComponentWithControllerIfNoAnswer);

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

		// stopRuleExecutionIfTrueComboBox
		stopRuleExecutionIfTrueComboBox = new CheckBox();
		stopRuleExecutionIfTrueComboBox.setStyleName("bold");
		stopRuleExecutionIfTrueComboBox
				.setCaption("!!! Stop rule execution and send message if rule result is TRUE");
		stopRuleExecutionIfTrueComboBox.setImmediate(false);
		stopRuleExecutionIfTrueComboBox.setWidth("100.0%");
		stopRuleExecutionIfTrueComboBox.setHeight("-1px");
		switchesGroupLayout.addComponent(stopRuleExecutionIfTrueComboBox);

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

		// hoursUntillHandledAsNotAnsweredLabel
		hoursUntillHandledAsNotAnsweredLabel = new Label();
		hoursUntillHandledAsNotAnsweredLabel.setImmediate(false);
		hoursUntillHandledAsNotAnsweredLabel.setWidth("-1px");
		hoursUntillHandledAsNotAnsweredLabel.setHeight("-1px");
		hoursUntillHandledAsNotAnsweredLabel
				.setValue("!!! Hours until handled as not answered:");
		gridLayout2.addComponent(hoursUntillHandledAsNotAnsweredLabel, 0, 2);

		// hoursUntillHandledAsNotAnsweredSlider
		hoursUntillHandledAsNotAnsweredSlider = new Slider();
		hoursUntillHandledAsNotAnsweredSlider.setImmediate(false);
		hoursUntillHandledAsNotAnsweredSlider.setWidth("350px");
		hoursUntillHandledAsNotAnsweredSlider.setHeight("-1px");
		gridLayout2.addComponent(hoursUntillHandledAsNotAnsweredSlider, 1, 2);
		gridLayout2.setComponentAlignment(
				hoursUntillHandledAsNotAnsweredSlider, new Alignment(34));

		return gridLayout2;
	}

	@AutoGenerated
	private VerticalLayout buildReplyRulesIfAnswerLabelLayout() {
		// common part: create layout
		replyRulesIfAnswerLabelLayout = new VerticalLayout();
		replyRulesIfAnswerLabelLayout.setImmediate(false);
		replyRulesIfAnswerLabelLayout.setWidth("100.0%");
		replyRulesIfAnswerLabelLayout.setHeight("-1px");
		replyRulesIfAnswerLabelLayout.setMargin(true);

		// replyRulesIfAnswerLabel
		replyRulesIfAnswerLabel = new Label();
		replyRulesIfAnswerLabel.setStyleName("bold");
		replyRulesIfAnswerLabel.setImmediate(false);
		replyRulesIfAnswerLabel.setWidth("-1px");
		replyRulesIfAnswerLabel.setHeight("-1px");
		replyRulesIfAnswerLabel
				.setValue("!!! Execute these rules if participant does answer:");
		replyRulesIfAnswerLabelLayout.addComponent(replyRulesIfAnswerLabel);

		return replyRulesIfAnswerLabelLayout;
	}

	@AutoGenerated
	private VerticalLayout buildReplyRulesIfNoAnswerLabelLayout() {
		// common part: create layout
		replyRulesIfNoAnswerLabelLayout = new VerticalLayout();
		replyRulesIfNoAnswerLabelLayout.setImmediate(false);
		replyRulesIfNoAnswerLabelLayout.setWidth("100.0%");
		replyRulesIfNoAnswerLabelLayout.setHeight("-1px");
		replyRulesIfNoAnswerLabelLayout.setMargin(true);

		// replyRulesIfNoAnswerLabel
		replyRulesIfNoAnswerLabel = new Label();
		replyRulesIfNoAnswerLabel.setStyleName("bold");
		replyRulesIfNoAnswerLabel.setImmediate(false);
		replyRulesIfNoAnswerLabel.setWidth("-1px");
		replyRulesIfNoAnswerLabel.setHeight("-1px");
		replyRulesIfNoAnswerLabel
				.setValue("!!! Execute these rules if participant does NOT answer:");
		replyRulesIfNoAnswerLabelLayout.addComponent(replyRulesIfNoAnswerLabel);

		return replyRulesIfNoAnswerLabelLayout;
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
		buttonLayout.setColumns(2);

		// closeButton
		closeButton = new Button();
		closeButton.setCaption("!!! Close");
		closeButton.setIcon(new ThemeResource("img/ok-icon-small.png"));
		closeButton.setImmediate(true);
		closeButton.setWidth("140px");
		closeButton.setHeight("-1px");
		buttonLayout.addComponent(closeButton, 1, 0);
		buttonLayout.setComponentAlignment(closeButton, new Alignment(9));

		return buttonLayout;
	}

}
