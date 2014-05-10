package org.isgf.mhc.ui.views.components.interventions.monitoring_rules;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.MonitoringMessageGroup;
import org.isgf.mhc.model.persistent.MonitoringReplyRule;
import org.isgf.mhc.model.ui.UIMonitoringMessageGroup;
import org.isgf.mhc.ui.views.components.basics.AbstractRuleEditComponentWithController;
import org.isgf.mhc.ui.views.components.basics.AbstractRuleEditComponentWithController.TYPES;
import org.isgf.mhc.ui.views.components.basics.ShortPlaceholderStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the monitoring reply rule edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringReplyRuleEditComponentWithController extends
		MonitoringReplyRuleEditComponent {
	private final ObjectId									interventionId;

	private final AbstractRuleEditComponentWithController	ruleEditComponent;

	private final MonitoringReplyRule						monitoringRule;

	public MonitoringReplyRuleEditComponentWithController(
			final Intervention intervention,
			final ObjectId monitoringReplyRuleId) {
		super();

		interventionId = intervention.getId();

		// Configure integrated components
		monitoringRule = getInterventionAdministrationManagerService()
				.getMonitoringReplyRule(monitoringReplyRuleId);

		ruleEditComponent = getAbstractRuleEditComponentWithController();
		ruleEditComponent.init(intervention.getId(), TYPES.MONITORING_RULES);
		ruleEditComponent.adjust(monitoringRule);

		/*
		 * Adjust own components
		 */
		// Handle combo box
		val allMonitoringMessageGroupsExpectingNoAnswerOfIntervention = getInterventionAdministrationManagerService()
				.getAllMonitoringMessageGroupsExpectingNoAnswerOfIntervention(
						intervention.getId());
		val monitoringMessageComboBox = getMessageGroupComboBox();
		for (val monitoringMessageGroup : allMonitoringMessageGroupsExpectingNoAnswerOfIntervention) {
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
								.monitoringReplyRuleChangeRelatedMonitoringMessageGroup(
										monitoringRule,
										newMonitoringMessageGroupId);

						adjust();

					}
				});

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
								.monitoringReplyRuleChangeSendMessageIfTrue(
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
		} else {
			getMessageGroupLabel().setEnabled(false);
			getMessageGroupComboBox().setEnabled(false);
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
									.monitoringReplyRuleSetStoreResultToVariable(
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
}
