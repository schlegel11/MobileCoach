package org.isgf.mhc.ui.views.components.interventions.monitoring_rules;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.model.server.MonitoringMessageGroup;
import org.isgf.mhc.model.server.MonitoringRule;
import org.isgf.mhc.model.ui.UIMonitoringMessageGroup;
import org.isgf.mhc.ui.views.components.basics.AbstractRuleEditComponentWithController;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

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

	private final AbstractRuleEditComponentWithController			ruleEditComponent;

	private final MonitoringReplyRulesEditComponentWithController	monitoringReplyRulesEditComponentWithControllerIfAnswer;
	private final MonitoringReplyRulesEditComponentWithController	monitoringReplyRulesEditComponentWithControllerIfNoAnswer;

	private final MonitoringRule									monitoringRule;

	public MonitoringRuleEditComponentWithController(
			final Intervention intervention, final ObjectId monitoringRuleId) {
		super();

		// Configure integrated components
		monitoringRule = getInterventionAdministrationManagerService()
				.getMonitoringRule(monitoringRuleId);

		ruleEditComponent = getAbstractRuleEditComponentWithController();
		ruleEditComponent.init(intervention.getId());
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
						} else {
							newMonitoringMessageGroupId = uiMonitoringMessageGroup
									.getRelatedModelObject(
											MonitoringMessageGroup.class)
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
				.setMin(ImplementationContants.HOUR_TO_SEND_MESSAGE_MIN);
		hourToSendSlider
				.setMax(ImplementationContants.HOUR_TO_SEND_MESSAGE_MAX);
		hourToSendSlider.addValueChangeListener(valueChangeListener);

		val hoursUntilHandledAsNotAnsweredSlider = getHoursUntillHandledAsNotAnsweredSlider();
		hoursUntilHandledAsNotAnsweredSlider.setImmediate(true);
		hoursUntilHandledAsNotAnsweredSlider
				.setMin(ImplementationContants.HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MIN);
		hoursUntilHandledAsNotAnsweredSlider
				.setMax(ImplementationContants.HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX);
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
						getInterventionAdministrationManagerService()
								.monitoringRuleChangeSendMessageIfTrue(
										monitoringRule,
										(boolean) event.getProperty()
												.getValue());

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
			getHoursUntillHandledAsNotAnsweredLabel().setEnabled(true);
			getHoursUntillHandledAsNotAnsweredSlider().setEnabled(true);

			getReplyRulesTabSheet().setEnabled(true);

			getReplyRulesIfAnswerLabel().setEnabled(true);
			getMonitoringReplyRulesEditComponentWithControllerIfAnswer()
					.setEnabled(true);
			getReplyRulesIfNoAnswerLabel().setEnabled(true);
			getMonitoringReplyRulesEditComponentWithControllerIfNoAnswer()
					.setEnabled(true);
		} else {
			getMessageGroupLabel().setEnabled(false);
			getMessageGroupComboBox().setEnabled(false);
			getHourToSendMessageLabel().setEnabled(false);
			getHourToSendMessageSlider().setEnabled(false);
			getHoursUntillHandledAsNotAnsweredLabel().setEnabled(false);
			getHoursUntillHandledAsNotAnsweredSlider().setEnabled(false);

			getReplyRulesTabSheet().setEnabled(false);

			getReplyRulesIfAnswerLabel().setEnabled(false);
			getMonitoringReplyRulesEditComponentWithControllerIfAnswer()
					.setEnabled(false);
			getReplyRulesIfNoAnswerLabel().setEnabled(false);
			getMonitoringReplyRulesEditComponentWithControllerIfNoAnswer()
					.setEnabled(false);
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
		localize(
				getHoursUntillHandledAsNotAnsweredSlider(),
				AdminMessageStrings.MONITORING_RULE_EDITING__HOURS_AFTER_SENDING_UNTIL_HANDLED_AS_NOT_ANSWERED_VALUE,
				monitoringRule.getHoursUntilMessageIsHandledAsUnanswered());
		try {
			getHoursUntillHandledAsNotAnsweredSlider().setValue(
					(double) monitoringRule
							.getHoursUntilMessageIsHandledAsUnanswered());
		} catch (final Exception e) {
			// Do nothing
		}
	}

	public void editStoreResultVariable() {
		log.debug("Edit store result to variable");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VARIABLE,
				monitoringRule.getStoreValueToVariableWithName(), null,
				new ShortStringEditComponent(),
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
			} else if (event.getProperty() == getHoursUntillHandledAsNotAnsweredSlider()) {
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
