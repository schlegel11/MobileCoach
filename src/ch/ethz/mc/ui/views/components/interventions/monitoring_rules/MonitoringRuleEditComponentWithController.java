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
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.ui.UIMonitoringMessageGroup;
import ch.ethz.mc.ui.views.components.basics.AbstractRuleEditComponentWithController;
import ch.ethz.mc.ui.views.components.basics.AbstractRuleEditComponentWithController.TYPES;
import ch.ethz.mc.ui.views.components.basics.ShortPlaceholderStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the monitoring rule edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringRuleEditComponentWithController extends
		MonitoringRuleEditComponent {
	private final ObjectId											interventionId;

	private final AbstractRuleEditComponentWithController			ruleEditComponent;

	private final MonitoringReplyRulesEditComponentWithController	monitoringReplyRulesEditComponentWithControllerIfAnswer;
	private final MonitoringReplyRulesEditComponentWithController	monitoringReplyRulesEditComponentWithControllerIfNoAnswer;

	private final MonitoringRule									monitoringRule;

	private MonitoringMessageGroup									currentMonitoringMessageGroup;

	public MonitoringRuleEditComponentWithController(
			final Intervention intervention, final ObjectId monitoringRuleId) {
		super();

		interventionId = intervention.getId();

		// Configure integrated components
		monitoringRule = getInterventionAdministrationManagerService()
				.getMonitoringRule(monitoringRuleId);

		ruleEditComponent = getAbstractRuleEditComponentWithController();
		ruleEditComponent.init(intervention.getId(), TYPES.MONITORING_RULES);
		ruleEditComponent.adjust(monitoringRule);

		monitoringReplyRulesEditComponentWithControllerIfAnswer = getMonitoringReplyRulesEditComponentWithControllerIfAnswer();
		monitoringReplyRulesEditComponentWithControllerIfAnswer.init(
				intervention, monitoringRuleId, true);
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer = getMonitoringReplyRulesEditComponentWithControllerIfNoAnswer();
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer.init(
				intervention, monitoringRuleId, false);

		/*
		 * Adjust own components
		 */
		// Handle combo box
		currentMonitoringMessageGroup = null;
		val allMonitoringMessageGroupsOfIntervention = getInterventionAdministrationManagerService()
				.getAllMonitoringMessageGroupsOfIntervention(
						intervention.getId());
		val monitoringMessageComboBox = getMessageGroupComboBox();
		for (val monitoringMessageGroup : allMonitoringMessageGroupsOfIntervention) {
			val uiMonitoringMessageGroup = monitoringMessageGroup
					.toUIModelObject();
			monitoringMessageComboBox.addItem(uiMonitoringMessageGroup);
			if (monitoringMessageGroup.getId().equals(
					monitoringRule.getRelatedMonitoringMessageGroup())) {
				monitoringMessageComboBox.select(uiMonitoringMessageGroup);
				currentMonitoringMessageGroup = monitoringMessageGroup;
			}
		}
		monitoringMessageComboBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						final UIMonitoringMessageGroup uiMonitoringMessageGroup = (UIMonitoringMessageGroup) event
								.getProperty().getValue();

						ObjectId newMonitoringMessageGroupId;
						if (uiMonitoringMessageGroup == null) {
							newMonitoringMessageGroupId = null;
							currentMonitoringMessageGroup = null;
						} else {
							currentMonitoringMessageGroup = uiMonitoringMessageGroup
									.getRelatedModelObject(MonitoringMessageGroup.class);
							newMonitoringMessageGroupId = currentMonitoringMessageGroup
									.getId();
						}

						log.debug(
								"Adjust related monitoring message group to {}",
								newMonitoringMessageGroupId);
						getInterventionAdministrationManagerService()
								.monitoringRuleChangeRelatedMonitoringMessageGroup(
										monitoringRule,
										newMonitoringMessageGroupId);

						adjust();

					}
				});

		// Handle sliders
		final val valueChangeListener = new SliderValueChangeListener();
		val hourToSendSlider = getHourToSendMessageSlider();
		hourToSendSlider.setImmediate(true);
		hourToSendSlider
				.setMin(ImplementationConstants.HOUR_TO_SEND_MESSAGE_MIN);
		hourToSendSlider
				.setMax(ImplementationConstants.HOUR_TO_SEND_MESSAGE_MAX);
		hourToSendSlider.addValueChangeListener(valueChangeListener);

		val hoursUntilHandledAsNotAnsweredSlider = getHoursUntilHandledAsNotAnsweredSlider();
		hoursUntilHandledAsNotAnsweredSlider.setImmediate(true);
		hoursUntilHandledAsNotAnsweredSlider
				.setMin(ImplementationConstants.HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MIN);
		hoursUntilHandledAsNotAnsweredSlider
				.setMax(ImplementationConstants.HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX);
		hoursUntilHandledAsNotAnsweredSlider
				.addValueChangeListener(valueChangeListener);

		// Add button listeners
		val buttonClickListener = new ButtonClickListener();
		getStoreVariableTextFieldComponent().getButton().addClickListener(
				buttonClickListener);

		// Add other listeners
		getStopRuleExecutionIfTrueComboBox().setValue(
				monitoringRule.isSendMessageIfTrue());
		getStopRuleExecutionIfTrueComboBox().addValueChangeListener(
				new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						log.debug("Adjust send message if true");
						val newValue = (boolean) event.getProperty().getValue();

						getInterventionAdministrationManagerService()
								.monitoringRuleChangeSendMessageIfTrue(
										monitoringRule, newValue);

						if (newValue
								&& getStopRuleExecutionAndFinishInterventionIfTrueComboBox()
										.getValue()) {
							getStopRuleExecutionAndFinishInterventionIfTrueComboBox()
									.setValue(false);
						}

						adjust();
					}
				});

		getStopRuleExecutionAndFinishInterventionIfTrueComboBox().setValue(
				monitoringRule.isStopInterventionWhenTrue());
		getStopRuleExecutionAndFinishInterventionIfTrueComboBox()
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						log.debug("Adjust stop intervention if true");
						val newValue = (boolean) event.getProperty().getValue();

						getInterventionAdministrationManagerService()
								.monitoringRuleChangeStopInterventionIfTrue(
										monitoringRule, newValue);

						if (newValue
								&& getStopRuleExecutionIfTrueComboBox()
										.getValue()) {
							getStopRuleExecutionIfTrueComboBox()
									.setValue(false);
						}

						adjust();
					}
				});

		// Adjust UI for first time
		adjust();
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getStoreVariableTextFieldComponent()
					.getButton()) {
				editStoreResultVariable();
			}
		}
	}

	private void adjust() {
		// Adjust store result variable
		getStoreVariableTextFieldComponent().setValue(
				monitoringRule.getStoreValueToVariableWithName());

		// Adjust stop rule execution checkbox
		if (monitoringRule.isSendMessageIfTrue()) {
			getMessageGroupLabel().setEnabled(true);
			getMessageGroupComboBox().setEnabled(true);
			getHourToSendMessageLabel().setEnabled(true);
			getHourToSendMessageSlider().setEnabled(true);

			if (currentMonitoringMessageGroup != null
					&& !currentMonitoringMessageGroup.isMessagesExpectAnswer()) {
				getHoursUntilHandledAsNotAnsweredLabel().setEnabled(false);
				getHoursUntilHandledAsNotAnsweredSlider().setEnabled(false);

				getReplyRulesTabSheet().setEnabled(false);

				getReplyRulesIfAnswerLabel().setEnabled(false);
				getMonitoringReplyRulesEditComponentWithControllerIfAnswer()
						.setEnabled(false);
				getMonitoringReplyRulesEditComponentWithControllerIfAnswer()
						.getRulesTree().setEnabled(false);

				getReplyRulesIfNoAnswerLabel().setEnabled(false);
				getMonitoringReplyRulesEditComponentWithControllerIfNoAnswer()
						.setEnabled(false);
				getMonitoringReplyRulesEditComponentWithControllerIfNoAnswer()
						.getRulesTree().setEnabled(false);
			} else {
				getHoursUntilHandledAsNotAnsweredLabel().setEnabled(true);
				getHoursUntilHandledAsNotAnsweredSlider().setEnabled(true);

				getReplyRulesTabSheet().setEnabled(true);

				getReplyRulesIfAnswerLabel().setEnabled(true);
				getMonitoringReplyRulesEditComponentWithControllerIfAnswer()
						.setEnabled(true);
				getMonitoringReplyRulesEditComponentWithControllerIfAnswer()
						.getRulesTree().setEnabled(true);

				getReplyRulesIfNoAnswerLabel().setEnabled(true);
				getMonitoringReplyRulesEditComponentWithControllerIfNoAnswer()
						.setEnabled(true);
				getMonitoringReplyRulesEditComponentWithControllerIfNoAnswer()
						.getRulesTree().setEnabled(true);
			}

		} else {
			getMessageGroupLabel().setEnabled(false);
			getMessageGroupComboBox().setEnabled(false);
			getHourToSendMessageLabel().setEnabled(false);
			getHourToSendMessageSlider().setEnabled(false);

			getHoursUntilHandledAsNotAnsweredLabel().setEnabled(false);
			getHoursUntilHandledAsNotAnsweredSlider().setEnabled(false);

			getReplyRulesTabSheet().setEnabled(false);

			getReplyRulesIfAnswerLabel().setEnabled(false);
			getMonitoringReplyRulesEditComponentWithControllerIfAnswer()
					.setEnabled(false);
			getMonitoringReplyRulesEditComponentWithControllerIfAnswer()
					.getRulesTree().setEnabled(false);

			getReplyRulesIfNoAnswerLabel().setEnabled(false);
			getMonitoringReplyRulesEditComponentWithControllerIfNoAnswer()
					.setEnabled(false);
			getMonitoringReplyRulesEditComponentWithControllerIfNoAnswer()
					.getRulesTree().setEnabled(false);
		}

		// Adjust sliders
		localize(
				getHourToSendMessageSlider(),
				AdminMessageStrings.MONITORING_RULE_EDITING__HOUR_TO_SEND_MESSAGE_VALUE,
				monitoringRule.getHourToSendMessage());
		try {
			getHourToSendMessageSlider().setValue(
					(double) monitoringRule.getHourToSendMessage());
		} catch (final Exception e) {
			// Do nothing
		}
		final int daysUntilMessageIsHandledAsUnanswered = (int) Math
				.floor(monitoringRule
						.getHoursUntilMessageIsHandledAsUnanswered() / 24);
		final int hoursWithoutDaysUntilMessageIsHandledAsUnanswered = monitoringRule
				.getHoursUntilMessageIsHandledAsUnanswered()
				- daysUntilMessageIsHandledAsUnanswered * 24;

		localize(
				getHoursUntilHandledAsNotAnsweredSlider(),
				AdminMessageStrings.MONITORING_RULE_EDITING__DAYS_AND_HOURS_AFTER_SENDING_UNTIL_HANDLED_AS_NOT_ANSWERED_VALUE,
				daysUntilMessageIsHandledAsUnanswered,
				hoursWithoutDaysUntilMessageIsHandledAsUnanswered);
		try {
			getHoursUntilHandledAsNotAnsweredSlider().setValue(
					(double) monitoringRule
							.getHoursUntilMessageIsHandledAsUnanswered());
		} catch (final Exception e) {
			// Do nothing
		}
	}

	public void editStoreResultVariable() {
		log.debug("Edit store result to variable");
		val allPossibleVariables = getInterventionAdministrationManagerService()
				.getAllWritableMonitoringRuleVariablesOfIntervention(
						interventionId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VARIABLE,
				monitoringRule.getStoreValueToVariableWithName(),
				allPossibleVariables,
				new ShortPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change name
							getInterventionAdministrationManagerService()
									.monitoringRuleSetStoreResultToVariable(
											monitoringRule, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}

	private class SliderValueChangeListener implements ValueChangeListener {

		@Override
		public void valueChange(final ValueChangeEvent event) {
			if (event.getProperty() == getHourToSendMessageSlider()) {
				getInterventionAdministrationManagerService()
						.monitoringRuleChangeHourToSendMessage(
								monitoringRule,
								((Double) event.getProperty().getValue())
										.intValue());
			} else if (event.getProperty() == getHoursUntilHandledAsNotAnsweredSlider()) {
				getInterventionAdministrationManagerService()
						.monitoringRuleChangeHoursUntilMessageIsHandledAsUnanswered(
								monitoringRule,
								((Double) event.getProperty().getValue())
										.intValue());
			}

			adjust();
		}
	}

}
