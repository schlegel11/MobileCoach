package ch.ethz.mc.ui.components.main_view.interventions.rules;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.memory.types.RuleTypes;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the monitoring reply rule edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringReplyRuleEditComponentWithController
		extends MonitoringReplyRuleEditComponent {
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
		ruleEditComponent.init(intervention.getId(),
				RuleTypes.MONITORING_RULES);
		ruleEditComponent.adjust(monitoringRule);

		/*
		 * Adjust own components
		 */
		// Handle combo boxes
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
						.monitoringReplyRuleChangeRelatedMicroDialog(
								monitoringRule, newMicroDialogId);

				adjust();

			}
		});

		// Add button listeners
		val buttonClickListener = new ButtonClickListener();
		getStoreVariableTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);

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
								.monitoringReplyRuleChangeSendMessageIfTrue(
										monitoringRule, (boolean) event
												.getProperty().getValue());

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
								.monitoringReplyRuleChangeActivateMicroDialogIfTrue(
										monitoringRule, (boolean) event
												.getProperty().getValue());

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
								.monitoringReplyRuleChangeSendMessageToSupervisor(
										monitoringRule, newValue);

						if (newValue
								&& !getSendMessageIfTrueCheckBox().getValue()) {
							getSendMessageIfTrueCheckBox().setValue(true);
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
		} else {
			getMessageGroupLabel().setEnabled(false);
			getMessageGroupComboBox().setEnabled(false);
		}
		if (monitoringRule.isActivateMicroDialogIfTrue()) {
			getMicroDialogLabel().setEnabled(true);
			getMicroDialogComboBox().setEnabled(true);
		} else {
			getMicroDialogLabel().setEnabled(false);
			getMicroDialogComboBox().setEnabled(false);
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
