package ch.ethz.mc.ui.views.components.interventions.monitoring_messages;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import ch.ethz.mc.model.persistent.MonitoringMessage;
import ch.ethz.mc.model.persistent.MonitoringMessageRule;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.ui.UIMonitoringMessageRule;
import ch.ethz.mc.model.ui.UIScreeningSurvey;
import ch.ethz.mc.ui.views.components.basics.LocalizedPlaceholderStringEditComponent;
import ch.ethz.mc.ui.views.components.basics.MediaObjectIntegrationComponentWithController.MediaObjectCreationOrDeleteionListener;
import ch.ethz.mc.ui.views.components.basics.ShortPlaceholderStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;

/**
 * Extends the monitoring message edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MonitoringMessageEditComponentWithController extends
		MonitoringMessageEditComponent implements
		MediaObjectCreationOrDeleteionListener {

	private final MonitoringMessage									monitoringMessage;

	private final ObjectId											interventionId;

	private final Table												rulesTable;

	private UIMonitoringMessageRule									selectedUIMonitoringMessageRule	= null;

	private final BeanContainer<ObjectId, UIMonitoringMessageRule>	rulesBeanContainer;

	public MonitoringMessageEditComponentWithController(
			final MonitoringMessage monitoringMessage,
			final ObjectId interventionId) {
		super();

		this.monitoringMessage = monitoringMessage;
		this.interventionId = interventionId;

		// table options
		rulesTable = getRulesTable();

		// table content
		val rules = getInterventionAdministrationManagerService()
				.getAllMonitoringMessageRulesOfMonitoringMessage(
						monitoringMessage.getId());

		rulesBeanContainer = createBeanContainerForModelObjects(
				UIMonitoringMessageRule.class, rules);

		rulesTable.setContainerDataSource(rulesBeanContainer);
		rulesTable.setSortContainerPropertyId(UIMonitoringMessageRule
				.getSortColumn());
		rulesTable.setVisibleColumns(UIMonitoringMessageRule
				.getVisibleColumns());
		rulesTable.setColumnHeaders(UIMonitoringMessageRule.getColumnHeaders());
		rulesTable.setSortAscending(true);
		rulesTable.setSortEnabled(false);

		// handle table selection change
		rulesTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = rulesTable.getValue();
				if (objectId == null) {
					setRuleSelected(false);
					selectedUIMonitoringMessageRule = null;
				} else {
					selectedUIMonitoringMessageRule = getUIModelObjectFromTableByObjectId(
							rulesTable, UIMonitoringMessageRule.class, objectId);
					setRuleSelected(true);
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewRuleButton().addClickListener(buttonClickListener);
		getEditRuleButton().addClickListener(buttonClickListener);
		getMoveUpRuleButton().addClickListener(buttonClickListener);
		getMoveDownRuleButton().addClickListener(buttonClickListener);
		getDeleteRuleButton().addClickListener(buttonClickListener);

		getTextWithPlaceholdersTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		getStoreVariableTextFieldComponent().getButton().addClickListener(
				buttonClickListener);

		// Handle media object to component
		if (monitoringMessage.getLinkedMediaObject() == null) {
			getIntegratedMediaObjectComponent().setMediaObject(null, this);
		} else {
			val mediaObject = getInterventionAdministrationManagerService()
					.getMediaObject(monitoringMessage.getLinkedMediaObject());
			getIntegratedMediaObjectComponent().setMediaObject(mediaObject,
					this);
		}

		// Adjust UI
		adjust();

		// Handle combo boxes
		val intermediateSurveys = getScreeningSurveyAdministrationManagerService()
				.getAllIntermediateSurveysOfIntervention(interventionId);

		val intermediateSurveyComboBox = getIntermediateSurveyComboBox();
		for (val intermediateSurvey : intermediateSurveys) {
			val uiIntermediateSurvey = intermediateSurvey.toUIModelObject();
			intermediateSurveyComboBox.addItem(uiIntermediateSurvey);
			if (monitoringMessage.getLinkedIntermediateSurvey() != null
					&& monitoringMessage.getLinkedIntermediateSurvey().equals(
							intermediateSurvey.getId())) {
				intermediateSurveyComboBox.select(uiIntermediateSurvey);
			}
		}
		intermediateSurveyComboBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						val selectedUIScreeningSurvey = (UIScreeningSurvey) event
								.getProperty().getValue();

						ObjectId intermediateSurveyToSet = null;
						if (selectedUIScreeningSurvey != null) {
							intermediateSurveyToSet = selectedUIScreeningSurvey
									.getRelatedModelObject(
											ScreeningSurvey.class).getId();
						}
						log.debug("Adjust intermediate survey to {}",
								intermediateSurveyToSet);
						getInterventionAdministrationManagerService()
								.monitoringMessageSetLinkedIntermediateSurvey(
										monitoringMessage,
										intermediateSurveyToSet);
					}
				});
	}

	private void adjust() {
		getTextWithPlaceholdersTextFieldComponent().setValue(
				monitoringMessage.getTextWithPlaceholders().toString());
		getStoreVariableTextFieldComponent().setValue(
				monitoringMessage.getStoreValueToVariableWithName());
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getNewRuleButton()) {
				createRule();
			} else if (event.getButton() == getEditRuleButton()) {
				editRule();
			} else if (event.getButton() == getMoveUpRuleButton()) {
				moveRule(true);
			} else if (event.getButton() == getMoveDownRuleButton()) {
				moveRule(false);
			} else if (event.getButton() == getDeleteRuleButton()) {
				deleteRule();
			} else if (event.getButton() == getTextWithPlaceholdersTextFieldComponent()
					.getButton()) {
				editTextWithPlaceholder();
			} else if (event.getButton() == getStoreVariableTextFieldComponent()
					.getButton()) {
				editStoreResultToVariable();
			}
			event.getButton().setEnabled(true);
		}
	}

	public void editTextWithPlaceholder() {
		log.debug("Edit text with placeholder");
		val allPossibleMessageVariables = getInterventionAdministrationManagerService()
				.getAllPossibleMessageVariablesOfIntervention(interventionId);
		showModalLStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TEXT_WITH_PLACEHOLDERS,
				monitoringMessage.getTextWithPlaceholders(),
				allPossibleMessageVariables,
				new LocalizedPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change text with placeholders
							getInterventionAdministrationManagerService()
									.monitoringMessageSetTextWithPlaceholders(
											monitoringMessage,
											getLStringValue(),
											allPossibleMessageVariables);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}

	public void editStoreResultToVariable() {
		log.debug("Edit store result to variable");
		val allPossibleMessageVariables = getInterventionAdministrationManagerService()
				.getAllWritableMessageVariablesOfIntervention(interventionId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VARIABLE,
				monitoringMessage.getStoreValueToVariableWithName(),
				allPossibleMessageVariables,
				new ShortPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change store result to variable
							getInterventionAdministrationManagerService()
									.monitoringMessageSetStoreResultToVariable(
											monitoringMessage, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}

	public void createRule() {
		log.debug("Create rule");
		val newMonitoringMessageRule = getInterventionAdministrationManagerService()
				.monitoringMessageRuleCreate(monitoringMessage.getId());

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_MONITORING_MESSAGE_RULE,
				new MonitoringMessageRuleEditComponentWithController(
						newMonitoringMessageRule, interventionId),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						rulesBeanContainer.addItem(newMonitoringMessageRule
								.getId(), UIMonitoringMessageRule.class
								.cast(newMonitoringMessageRule
										.toUIModelObject()));
						rulesTable.select(newMonitoringMessageRule.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_RULE_CREATED);

						closeWindow();
					}
				});
	}

	public void editRule() {
		log.debug("Edit rule");
		val selectedMonitoringMessageRule = selectedUIMonitoringMessageRule
				.getRelatedModelObject(MonitoringMessageRule.class);

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_MONITORING_MESSAGE_RULE,
				new MonitoringMessageRuleEditComponentWithController(
						selectedMonitoringMessageRule, interventionId),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						removeAndAddModelObjectToBeanContainer(
								rulesBeanContainer,
								selectedMonitoringMessageRule);
						rulesTable.sort();
						rulesTable.select(selectedMonitoringMessageRule.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_RULE_UPDATED);

						closeWindow();
					}
				});
	}

	public void moveRule(final boolean moveUp) {
		log.debug("Move rule {}", moveUp ? "up" : "down");

		val selectedMonitoringMessageRule = selectedUIMonitoringMessageRule
				.getRelatedModelObject(MonitoringMessageRule.class);
		val swappedMonitoringMessageRule = getInterventionAdministrationManagerService()
				.monitoringMessageRuleMove(selectedMonitoringMessageRule,
						moveUp);

		if (swappedMonitoringMessageRule == null) {
			log.debug("Rule is already at top/end of list");
			return;
		}

		removeAndAddModelObjectToBeanContainer(rulesBeanContainer,
				swappedMonitoringMessageRule);
		removeAndAddModelObjectToBeanContainer(rulesBeanContainer,
				selectedMonitoringMessageRule);
		rulesTable.sort();
		rulesTable.select(selectedMonitoringMessageRule.getId());
	}

	public void deleteRule() {
		log.debug("Delete rule");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedMonitoringMessageRule = selectedUIMonitoringMessageRule.getRelatedModelObject(MonitoringMessageRule.class);

					// Delete rule
					getInterventionAdministrationManagerService()
							.monitoringMessageRuleDelete(
									selectedMonitoringMessageRule);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				rulesTable.removeItem(selectedUIMonitoringMessageRule
						.getRelatedModelObject(MonitoringMessageRule.class)
						.getId());
				getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_RULE_DELETED);

				closeWindow();
			}
		}, null);
	}

	@Override
	public void updateLinkedMediaObjectId(final ObjectId mediaObjectId) {
		getInterventionAdministrationManagerService()
				.monitoringMessageSetLinkedMediaObject(monitoringMessage,
						mediaObjectId);
	}
}
