package ch.ethz.mc.ui.components.main_view.interventions.rules;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.memory.types.RuleTypes;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.ui.UIMicroDialog;
import ch.ethz.mc.model.ui.UIMonitoringMessageGroup;
import ch.ethz.mc.ui.components.basics.AbstractRuleEditComponentWithController;
import ch.ethz.mc.ui.components.basics.ShortPlaceholderStringEditComponent;
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
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the monitoring rule edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringRuleEditComponentWithController
		extends MonitoringRuleEditComponent {
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
		ruleEditComponent.init(intervention.getId(),
				RuleTypes.MONITORING_RULES);
		ruleEditComponent.adjust(monitoringRule);

		monitoringReplyRulesEditComponentWithControllerIfAnswer = getMonitoringReplyRulesEditComponentWithControllerIfAnswer();
		monitoringReplyRulesEditComponentWithControllerIfAnswer
				.init(intervention, monitoringRuleId, true);
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer = getMonitoringReplyRulesEditComponentWithControllerIfNoAnswer();
		monitoringReplyRulesEditComponentWithControllerIfNoAnswer
				.init(intervention, monitoringRuleId, false);

		/*
		 * Adjust own components
		 */
		// Handle combo boxes
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

				if (monitoringMessageGroup.isMessagesExpectAnswer()) {
					getSendToSupervisorCheckBox().setEnabled(false);
				}
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
									.getRelatedModelObject(
											MonitoringMessageGroup.class);
							newMonitoringMessageGroupId = currentMonitoringMessageGroup
									.getId();
						}

						log.debug(
								"Adjust related monitoring message group to {}",
								newMonitoringMessageGroupId);
						getInterventionAdministrationManagerService()
								.monitoringRuleSetRelatedMonitoringMessageGroup(
										monitoringRule,
										newMonitoringMessageGroupId);

						if (currentMonitoringMessageGroup != null
								&& currentMonitoringMessageGroup
										.isMessagesExpectAnswer()
								&& getSendToSupervisorCheckBox().isEnabled()) {
							if (getSendToSupervisorCheckBox().getValue()) {
								getSendToSupervisorCheckBox().setValue(false);
							}
							getSendToSupervisorCheckBox().setEnabled(false);
						} else if (!getSendToSupervisorCheckBox().isEnabled()) {
							getSendToSupervisorCheckBox().setEnabled(true);
						}

						adjust();

					}
				});

		val allMicroDialogsOfIntervention = getInterventionAdministrationManagerService()
				.getAllMicroDialogsOfIntervention(intervention.getId());
		val microDialogComboBox = getMicroDialogComboBox();
		for (val microDialog : allMicroDialogsOfIntervention) {
			val uiMicroDialog = microDialog.toUIModelObject();
			microDialogComboBox.addItem(uiMicroDialog);
			if (microDialog.getId()
					.equals(monitoringRule.getRelatedMicroDialog())) {
				microDialogComboBox.select(uiMicroDialog);
			}
		}
		microDialogComboBox.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final UIMicroDialog uiMicroDialog = (UIMicroDialog) event
						.getProperty().getValue();

				ObjectId newMicroDialogId;
				if (uiMicroDialog == null) {
					newMicroDialogId = null;
				} else {
					newMicroDialogId = uiMicroDialog
							.getRelatedModelObject(MicroDialog.class).getId();
				}

				log.debug("Adjust related micro dialog to {}",
						newMicroDialogId);
				getInterventionAdministrationManagerService()
						.monitoringRuleChangeRelatedMicroDialog(monitoringRule,
								newMicroDialogId);

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

		val minutesUntilHandledAsNotAnsweredSlider = getMinutesUntilHandledAsNotAnsweredSlider();
		minutesUntilHandledAsNotAnsweredSlider.setImmediate(true);
		minutesUntilHandledAsNotAnsweredSlider.setMin(
				ImplementationConstants.MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MIN);
		minutesUntilHandledAsNotAnsweredSlider.setMax(
				ImplementationConstants.MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX_MONITORING_MESSAGE);
		minutesUntilHandledAsNotAnsweredSlider
				.addValueChangeListener(valueChangeListener);

		// Add button listeners
		val buttonClickListener = new ButtonClickListener();
		getStoreVariableTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		getMinutesButton1().addClickListener(buttonClickListener);
		getMinutesButton5().addClickListener(buttonClickListener);
		getMinutesButton10().addClickListener(buttonClickListener);
		getMinutesButton30().addClickListener(buttonClickListener);
		getMinutesButton60().addClickListener(buttonClickListener);

		// Add other listeners
		getSendMessageIfTrueCheckBox()
				.setValue(monitoringRule.isSendMessageIfTrue());
		getSendMessageIfTrueCheckBox()
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						log.debug("Adjust send message if true");
						val newValue = (boolean) event.getProperty().getValue();

						getInterventionAdministrationManagerService()
								.monitoringRuleSetSendMessageIfTrue(
										monitoringRule, newValue);

						if (newValue) {
							if (getMarkCaseAsSolvedWhenTrueCheckBox()
									.getValue()) {
								getMarkCaseAsSolvedWhenTrueCheckBox()
										.setValue(false);
							}
							if (getStopRuleExecutionAndFinishInterventionIfTrueCheckBox()
									.getValue()) {
								getStopRuleExecutionAndFinishInterventionIfTrueCheckBox()
										.setValue(false);
							}
						}
						if (!newValue
								&& getSendToSupervisorCheckBox().getValue()) {
							getSendToSupervisorCheckBox().setValue(false);
						}
						if (newValue
								&& getStartMicroDialogCheckBox().getValue()) {
							getStartMicroDialogCheckBox().setValue(false);
						}
						if (!newValue && getMessageGroupComboBox()
								.getValue() != null) {
							getMessageGroupComboBox().setValue(null);
						}

						adjust();
					}
				});

		getStartMicroDialogCheckBox()
				.setValue(monitoringRule.isActivateMicroDialogIfTrue());
		getStartMicroDialogCheckBox()
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						log.debug("Adjust start micro dialog if true");
						val newValue = (boolean) event.getProperty().getValue();

						getInterventionAdministrationManagerService()
								.monitoringRuleChangeActivateMicroDialogIfTrue(
										monitoringRule, (boolean) event
												.getProperty().getValue());

						if (newValue) {
							if (getMarkCaseAsSolvedWhenTrueCheckBox()
									.getValue()) {
								getMarkCaseAsSolvedWhenTrueCheckBox()
										.setValue(false);
							}
							if (getStopRuleExecutionAndFinishInterventionIfTrueCheckBox()
									.getValue()) {
								getStopRuleExecutionAndFinishInterventionIfTrueCheckBox()
										.setValue(false);
							}
						}
						if (newValue
								&& getSendMessageIfTrueCheckBox().getValue()) {
							getSendMessageIfTrueCheckBox().setValue(false);
						}
						if (!newValue && getMicroDialogComboBox()
								.getValue() != null) {
							getMicroDialogComboBox().setValue(null);
						}

						adjust();
					}
				});

		getSendToSupervisorCheckBox()
				.setValue(monitoringRule.isSendMessageToSupervisor());
		getSendToSupervisorCheckBox()
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						log.debug("Adjust send message to supervisor");
						val newValue = (boolean) event.getProperty().getValue();

						getInterventionAdministrationManagerService()
								.monitoringRuleSetSendMessageToSupervisor(
										monitoringRule, newValue);

						if (newValue) {
							if (!getSendMessageIfTrueCheckBox().getValue()) {
								getSendMessageIfTrueCheckBox().setValue(true);
							}
							if (getMarkCaseAsSolvedWhenTrueCheckBox()
									.getValue()) {
								getMarkCaseAsSolvedWhenTrueCheckBox()
										.setValue(false);
							}
							if (getStopRuleExecutionAndFinishInterventionIfTrueCheckBox()
									.getValue()) {
								getStopRuleExecutionAndFinishInterventionIfTrueCheckBox()
										.setValue(false);
							}
						}

						adjust();
					}
				});

		getMarkCaseAsSolvedWhenTrueCheckBox()
				.setValue(monitoringRule.isMarkCaseAsSolvedWhenTrue());
		getMarkCaseAsSolvedWhenTrueCheckBox()
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						log.debug("Adjust mark case as solved");
						val newValue = (boolean) event.getProperty().getValue();

						getInterventionAdministrationManagerService()
								.monitoringRuleSetMarkCaseAsSolvedIfTrue(
										monitoringRule, newValue);

						if (newValue) {
							if (getSendMessageIfTrueCheckBox().getValue()) {
								getSendMessageIfTrueCheckBox().setValue(false);
							}
							if (getStartMicroDialogCheckBox().getValue()) {
								getStartMicroDialogCheckBox().setValue(false);
							}
							if (getStopRuleExecutionAndFinishInterventionIfTrueCheckBox()
									.getValue()) {
								getStopRuleExecutionAndFinishInterventionIfTrueCheckBox()
										.setValue(false);
							}
						}

						adjust();
					}
				});

		getStopRuleExecutionAndFinishInterventionIfTrueCheckBox()
				.setValue(monitoringRule.isStopInterventionWhenTrue());
		getStopRuleExecutionAndFinishInterventionIfTrueCheckBox()
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						log.debug("Adjust stop intervention if true");
						val newValue = (boolean) event.getProperty().getValue();

						getInterventionAdministrationManagerService()
								.monitoringRuleSetStopInterventionIfTrue(
										monitoringRule, newValue);

						if (newValue) {
							if (getSendMessageIfTrueCheckBox().getValue()) {
								getSendMessageIfTrueCheckBox().setValue(false);
							}
							if (getStartMicroDialogCheckBox().getValue()) {
								getStartMicroDialogCheckBox().setValue(false);
							}
							if (getMarkCaseAsSolvedWhenTrueCheckBox()
									.getValue()) {
								getMarkCaseAsSolvedWhenTrueCheckBox()
										.setValue(false);
							}
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
			} else if (event.getButton() == getMinutesButton1()) {
				getMinutesUntilHandledAsNotAnsweredSlider().setValue(1d);
			} else if (event.getButton() == getMinutesButton5()) {
				getMinutesUntilHandledAsNotAnsweredSlider().setValue(5d);
			} else if (event.getButton() == getMinutesButton10()) {
				getMinutesUntilHandledAsNotAnsweredSlider().setValue(10d);
			} else if (event.getButton() == getMinutesButton30()) {
				getMinutesUntilHandledAsNotAnsweredSlider().setValue(30d);
			} else if (event.getButton() == getMinutesButton60()) {
				getMinutesUntilHandledAsNotAnsweredSlider().setValue(60d);
			}
			event.getButton().setEnabled(true);
		}
	}

	private void adjust() {
		// Adjust store result variable
		getStoreVariableTextFieldComponent()
				.setValue(monitoringRule.getStoreValueToVariableWithName());

		// Adjust enabled status of UI elements
		if (monitoringRule.isSendMessageIfTrue()) {
			getMessageGroupLabel().setEnabled(true);
			getMessageGroupComboBox().setEnabled(true);

			if (currentMonitoringMessageGroup != null
					&& !currentMonitoringMessageGroup
							.isMessagesExpectAnswer()) {
				getMinutesButton1().setEnabled(false);
				getMinutesButton5().setEnabled(false);
				getMinutesButton10().setEnabled(false);
				getMinutesButton30().setEnabled(false);
				getMinutesButton60().setEnabled(false);
				getMinutesUntilHandledAsNotAnsweredLabel().setEnabled(false);
				getMinutesUntilHandledAsNotAnsweredSlider().setEnabled(false);

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
				getMinutesButton1().setEnabled(true);
				getMinutesButton5().setEnabled(true);
				getMinutesButton10().setEnabled(true);
				getMinutesButton30().setEnabled(true);
				getMinutesButton60().setEnabled(true);
				getMinutesUntilHandledAsNotAnsweredLabel().setEnabled(true);
				getMinutesUntilHandledAsNotAnsweredSlider().setEnabled(true);

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

			getMinutesButton1().setEnabled(false);
			getMinutesButton5().setEnabled(false);
			getMinutesButton10().setEnabled(false);
			getMinutesButton30().setEnabled(false);
			getMinutesButton60().setEnabled(false);
			getMinutesUntilHandledAsNotAnsweredLabel().setEnabled(false);
			getMinutesUntilHandledAsNotAnsweredSlider().setEnabled(false);

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
		if (monitoringRule.isActivateMicroDialogIfTrue()) {
			getMicroDialogLabel().setEnabled(true);
			getMicroDialogComboBox().setEnabled(true);
		} else {
			getMicroDialogLabel().setEnabled(false);
			getMicroDialogComboBox().setEnabled(false);
		}
		if (monitoringRule.isSendMessageIfTrue()
				|| monitoringRule.isActivateMicroDialogIfTrue()) {
			getHourToSendMessageLabel().setEnabled(true);
			getHourToSendMessageSlider().setEnabled(true);
		} else {
			getHourToSendMessageLabel().setEnabled(false);
			getHourToSendMessageSlider().setEnabled(false);
		}

		// Adjust sliders
		localize(getHourToSendMessageSlider(),
				AdminMessageStrings.MONITORING_RULE_EDITING__HOUR_TO_SEND_MESSAGE_VALUE,
				monitoringRule.getHourToSendMessageOrActivateMicroDialog());
		try {
			getHourToSendMessageSlider().setValue((double) monitoringRule
					.getHourToSendMessageOrActivateMicroDialog());
		} catch (final Exception e) {
			// Do nothing
		}
		final int daysUntilMessageIsHandledAsUnanswered = (int) Math.floor(
				monitoringRule.getMinutesUntilMessageIsHandledAsUnanswered()
						/ 60 / 24);
		final int hoursWithoutDaysUntilMessageIsHandledAsUnanswered = (int) Math
				.floor(monitoringRule
						.getMinutesUntilMessageIsHandledAsUnanswered() / 60)
				- daysUntilMessageIsHandledAsUnanswered * 24;
		final int minutesWithoutHoursAndDaysUntilMessageIsHandledAsUnanswered = monitoringRule
				.getMinutesUntilMessageIsHandledAsUnanswered()
				- daysUntilMessageIsHandledAsUnanswered * 24 * 60
				- hoursWithoutDaysUntilMessageIsHandledAsUnanswered * 60;

		localize(getMinutesUntilHandledAsNotAnsweredSlider(),
				AdminMessageStrings.MONITORING_RULE_EDITING__TIMEFRAME_AFTER_SENDING_UNTIL_HANDLED_AS_NOT_ANSWERED_VALUE,
				daysUntilMessageIsHandledAsUnanswered,
				hoursWithoutDaysUntilMessageIsHandledAsUnanswered,
				minutesWithoutHoursAndDaysUntilMessageIsHandledAsUnanswered);
		try {
			getMinutesUntilHandledAsNotAnsweredSlider()
					.setValue((double) monitoringRule
							.getMinutesUntilMessageIsHandledAsUnanswered());
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
				allPossibleVariables, new ShortPlaceholderStringEditComponent(),
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
						.monitoringRuleSetHourToSendMessageOrActivateMicroDialog(
								monitoringRule,
								((Double) event.getProperty().getValue())
										.intValue());
			} else if (event
					.getProperty() == getMinutesUntilHandledAsNotAnsweredSlider()) {
				getInterventionAdministrationManagerService()
						.monitoringRuleSetMinutesUntilMessageIsHandledAsUnanswered(
								monitoringRule,
								((Double) event.getProperty().getValue())
										.intValue());
			}

			adjust();
		}
	}

}
