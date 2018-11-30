package ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs;

import org.bson.types.ObjectId;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.MicroDialogMessage;
import ch.ethz.mc.model.persistent.MicroDialogMessageRule;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.types.AnswerTypes;
import ch.ethz.mc.model.persistent.types.TextFormatTypes;
import ch.ethz.mc.model.ui.UIMicroDialogMessageRule;
import ch.ethz.mc.model.ui.UIScreeningSurvey;
import ch.ethz.mc.ui.components.basics.LocalizedPlaceholderStringEditComponent;
import ch.ethz.mc.ui.components.basics.MediaObjectIntegrationComponentWithController.MediaObjectCreationOrDeleteionListener;
import ch.ethz.mc.ui.components.basics.ShortPlaceholderStringEditComponent;
import ch.ethz.mc.ui.components.basics.ShortStringEditComponent;
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
 * Extends the micro dialog message edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MicroDialogMessageEditComponentWithController
		extends MicroDialogMessageEditComponent
		implements MediaObjectCreationOrDeleteionListener {

	private final MicroDialogMessage								microDialogMessage;

	private final ObjectId											interventionId;

	private final Table												rulesTable;

	private UIMicroDialogMessageRule								selectedUIMicroDialogMessageRule	= null;

	private final BeanContainer<ObjectId, UIMicroDialogMessageRule>	rulesBeanContainer;

	public MicroDialogMessageEditComponentWithController(
			final MicroDialogMessage microDialogMessage,
			final ObjectId interventionId) {
		super();

		this.microDialogMessage = microDialogMessage;
		this.interventionId = interventionId;

		// table options
		rulesTable = getRulesTable();

		// table content
		val rules = getInterventionAdministrationManagerService()
				.getAllMicroDialogMessageRulesOfMicroDialogMessage(
						microDialogMessage.getId());

		rulesBeanContainer = createBeanContainerForModelObjects(
				UIMicroDialogMessageRule.class, rules);

		rulesTable.setContainerDataSource(rulesBeanContainer);
		rulesTable.setSortContainerPropertyId(
				UIMicroDialogMessageRule.getSortColumn());
		rulesTable.setVisibleColumns(
				UIMicroDialogMessageRule.getVisibleColumns());
		rulesTable
				.setColumnHeaders(UIMicroDialogMessageRule.getColumnHeaders());
		rulesTable.setSortAscending(true);
		rulesTable.setSortEnabled(false);

		// handle table selection change
		rulesTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = rulesTable.getValue();
				if (objectId == null) {
					setRuleSelected(false);
					selectedUIMicroDialogMessageRule = null;
				} else {
					selectedUIMicroDialogMessageRule = getUIModelObjectFromTableByObjectId(
							rulesTable, UIMicroDialogMessageRule.class,
							objectId);
					setRuleSelected(true);
				}
			}
		});

		// Handle sliders
		final val valueChangeListener = new SliderValueChangeListener();

		val minutesUntilHandledAsNotAnsweredSlider = getMinutesUntilHandledAsNotAnsweredSlider();
		minutesUntilHandledAsNotAnsweredSlider.setImmediate(true);
		minutesUntilHandledAsNotAnsweredSlider.setMin(
				ImplementationConstants.MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MIN);
		minutesUntilHandledAsNotAnsweredSlider
				.setMax(ImplementationConstants.MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX_MICRO_DIALOG_MESSAGE
						+ 1);
		minutesUntilHandledAsNotAnsweredSlider
				.addValueChangeListener(valueChangeListener);

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewRuleButton().addClickListener(buttonClickListener);
		getEditRuleButton().addClickListener(buttonClickListener);
		getMoveUpRuleButton().addClickListener(buttonClickListener);
		getMoveDownRuleButton().addClickListener(buttonClickListener);
		getDeleteRuleButton().addClickListener(buttonClickListener);
		getMinutesButton1().addClickListener(buttonClickListener);
		getMinutesButton5().addClickListener(buttonClickListener);
		getMinutesButton10().addClickListener(buttonClickListener);
		getMinutesButton30().addClickListener(buttonClickListener);
		getMinutesButton60().addClickListener(buttonClickListener);
		getMinutesButtonInf().addClickListener(buttonClickListener);

		getTextWithPlaceholdersTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		getStoreVariableTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		getMessageKeyTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		getAnswerOptionsTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		getNoReplyTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);

		// Handle media object to component
		if (microDialogMessage.getLinkedMediaObject() == null) {
			getIntegratedMediaObjectComponent().setMediaObject(null, this);
		} else {
			val mediaObject = getInterventionAdministrationManagerService()
					.getMediaObject(microDialogMessage.getLinkedMediaObject());
			getIntegratedMediaObjectComponent().setMediaObject(mediaObject,
					this);
		}

		// Adjust UI
		adjust();

		// Handle combo boxes
		val textFormatComboBox = getTextFormatComboBox();

		for (val textFormatType : TextFormatTypes.values()) {
			textFormatComboBox.addItem(textFormatType);
			if (microDialogMessage.getTextFormat() == textFormatType) {
				textFormatComboBox.select(textFormatType);
			}
		}
		textFormatComboBox.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val selectedTextFormatType = (TextFormatTypes) event
						.getProperty().getValue();

				log.debug("Adjust text format type to {}",
						selectedTextFormatType);
				getInterventionAdministrationManagerService()
						.microDialogMessageSetTextFormat(microDialogMessage,
								selectedTextFormatType);
			}
		});

		val intermediateSurveys = getSurveyAdministrationManagerService()
				.getAllIntermediateSurveysOfIntervention(interventionId);

		val intermediateSurveyComboBox = getIntermediateSurveyComboBox();
		for (val intermediateSurvey : intermediateSurveys) {
			val uiIntermediateSurvey = intermediateSurvey.toUIModelObject();
			intermediateSurveyComboBox.addItem(uiIntermediateSurvey);
			if (microDialogMessage.getLinkedIntermediateSurvey() != null
					&& microDialogMessage.getLinkedIntermediateSurvey()
							.equals(intermediateSurvey.getId())) {
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
											ScreeningSurvey.class)
									.getId();
						}
						log.debug("Adjust intermediate survey to {}",
								intermediateSurveyToSet);
						getInterventionAdministrationManagerService()
								.microDialogMessageSetLinkedIntermediateSurvey(
										microDialogMessage,
										intermediateSurveyToSet);
					}
				});

		val answerTypeComboBox = getAnswerTypeComboBox();
		for (val answerType : AnswerTypes.values()) {
			answerTypeComboBox.addItem(answerType);
			if (microDialogMessage.getAnswerType() == answerType) {
				answerTypeComboBox.select(answerType);
			}
		}
		answerTypeComboBox.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val selectedAnswerType = (AnswerTypes) event.getProperty()
						.getValue();

				log.debug("Adjust answer type to {}", selectedAnswerType);
				getInterventionAdministrationManagerService()
						.microDialogMessageSetAnswerType(microDialogMessage,
								selectedAnswerType);
			}
		});

		// Handle check boxes
		val isCommandMessagCheckBox = getIsCommandCheckbox();
		isCommandMessagCheckBox.setValue(microDialogMessage.isCommandMessage());

		isCommandMessagCheckBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						getInterventionAdministrationManagerService()
								.microDialogMessageSetIsCommandMessage(
										microDialogMessage, (boolean) event
												.getProperty().getValue());

						if (microDialogMessage.isCommandMessage()
								&& microDialogMessage
										.isMessageExpectsAnswer()) {
							getMessageExpectsAnswerCheckBox().setValue(false);
						}
						if (microDialogMessage.isCommandMessage()) {
							getDeactivatesAllOpenQuestionsCheckBox()
									.setValue(false);
						}

						adjust();
					}
				});

		val isStickyCheckBox = getIsStickyMessageCheckBox();
		isStickyCheckBox.setValue(microDialogMessage.isMessageIsSticky());

		isStickyCheckBox.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				getInterventionAdministrationManagerService()
						.microDialogMessageSetIsStickyMessage(
								microDialogMessage,
								(boolean) event.getProperty().getValue());
			}
		});

		val deactivatesAllOpenQuestionsCheckBox = getDeactivatesAllOpenQuestionsCheckBox();
		deactivatesAllOpenQuestionsCheckBox.setValue(
				microDialogMessage.isMessageDeactivatesAllOpenQuestions());

		deactivatesAllOpenQuestionsCheckBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						getInterventionAdministrationManagerService()
								.microDialogMessageSetDeactivatesAllOpenQuestions(
										microDialogMessage, (boolean) event
												.getProperty().getValue());
					}
				});

		val messageExpectsAnswerCheckBox = getMessageExpectsAnswerCheckBox();
		messageExpectsAnswerCheckBox
				.setValue(microDialogMessage.isMessageExpectsAnswer());

		messageExpectsAnswerCheckBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						getInterventionAdministrationManagerService()
								.microDialogMessageSetMessageExpectsAnswer(
										microDialogMessage, (boolean) event
												.getProperty().getValue());

						if (!microDialogMessage.isMessageExpectsAnswer()
								&& microDialogMessage
										.isMessageBlocksMicroDialogUntilAnswered()) {
							getDeactivatesAllOpenQuestionsCheckBox()
									.setValue(false);
							getMessageBlocksMicroDialogUntilAnsweredCheckBox()
									.setValue(false);
						}
						if (microDialogMessage.isMessageExpectsAnswer()) {
							getDeactivatesAllOpenQuestionsCheckBox()
									.setValue(false);
						}

						adjust();
					}
				});

		val messageBlocksMicroDialogUntilAnsweredCheckBox = getMessageBlocksMicroDialogUntilAnsweredCheckBox();
		messageBlocksMicroDialogUntilAnsweredCheckBox.setValue(
				microDialogMessage.isMessageBlocksMicroDialogUntilAnswered());

		messageBlocksMicroDialogUntilAnsweredCheckBox
				.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						getInterventionAdministrationManagerService()
								.microDialogMessageSetMessageBlocksMicroDialogUntilAnsweredCheckBox(
										microDialogMessage, (boolean) event
												.getProperty().getValue());
					}
				});
	}

	private void adjust() {
		getTextWithPlaceholdersTextFieldComponent().setValue(
				microDialogMessage.getTextWithPlaceholders().toString());
		getStoreVariableTextFieldComponent()
				.setValue(microDialogMessage.getStoreValueToVariableWithName());
		getMessageKeyTextFieldComponent()
				.setValue(microDialogMessage.getNonUniqueKey());
		getAnswerOptionsTextFieldComponent().setValue(microDialogMessage
				.getAnswerOptionsWithPlaceholders().toString());
		getNoReplyTextFieldComponent()
				.setValue(microDialogMessage.getNoReplyValue());

		// TODO Aktivierungen und Deaktivierungen stimmen ncoh nicht, auch nicht
		// mit Check-Status
		// Außerdem noch Icon

		if (microDialogMessage.isCommandMessage()) {
			getMessageExpectsAnswerCheckBox().setEnabled(false);
		} else {
			getMessageExpectsAnswerCheckBox().setEnabled(true);
		}

		if (microDialogMessage.isMessageExpectsAnswer()) {
			getMessageBlocksMicroDialogUntilAnsweredCheckBox().setEnabled(true);
			getAnswerGridLayout().setEnabled(true);
			getMinutesUntilHandledAsNotAnsweredSlider().setEnabled(true);
		} else {
			getMessageBlocksMicroDialogUntilAnsweredCheckBox()
					.setEnabled(false);
			getAnswerGridLayout().setEnabled(false);
			getMinutesUntilHandledAsNotAnsweredSlider().setEnabled(false);
		}

		if (microDialogMessage.isCommandMessage()
				|| microDialogMessage.isMessageExpectsAnswer()) {
			getDeactivatesAllOpenQuestionsCheckBox().setEnabled(false);
		} else {
			getDeactivatesAllOpenQuestionsCheckBox().setEnabled(true);
		}

		// Adjust sliders
		if (microDialogMessage
				.getMinutesUntilMessageIsHandledAsUnanswered() > ImplementationConstants.MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX_MICRO_DIALOG_MESSAGE) {
			localize(getMinutesUntilHandledAsNotAnsweredSlider(),
					AdminMessageStrings.GENERAL__INFINITE);
		} else {
			final int daysUntilMessageIsHandledAsUnanswered = (int) Math
					.floor(microDialogMessage
							.getMinutesUntilMessageIsHandledAsUnanswered() / 60
							/ 24);
			final int hoursWithoutDaysUntilMessageIsHandledAsUnanswered = (int) Math
					.floor(microDialogMessage
							.getMinutesUntilMessageIsHandledAsUnanswered() / 60)
					- daysUntilMessageIsHandledAsUnanswered * 24;
			final int minutesWithoutHoursAndDaysUntilMessageIsHandledAsUnanswered = microDialogMessage
					.getMinutesUntilMessageIsHandledAsUnanswered()
					- daysUntilMessageIsHandledAsUnanswered * 24 * 60
					- hoursWithoutDaysUntilMessageIsHandledAsUnanswered * 60;

			localize(getMinutesUntilHandledAsNotAnsweredSlider(),
					AdminMessageStrings.MICRO_DIALOG_MESSAGE_EDITING__TIMEFRAME_AFTER_SENDING_UNTIL_HANDLED_AS_NOT_ANSWERED_VALUE,
					daysUntilMessageIsHandledAsUnanswered,
					hoursWithoutDaysUntilMessageIsHandledAsUnanswered,
					minutesWithoutHoursAndDaysUntilMessageIsHandledAsUnanswered);
		}

		try {
			int newValue = microDialogMessage
					.getMinutesUntilMessageIsHandledAsUnanswered();
			if (newValue == Integer.MAX_VALUE) {
				newValue = (int) getMinutesUntilHandledAsNotAnsweredSlider()
						.getMax();
			}
			getMinutesUntilHandledAsNotAnsweredSlider()
					.setValue((double) newValue);
		} catch (final Exception e) {
			// Do nothing
		}
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
			} else if (event
					.getButton() == getTextWithPlaceholdersTextFieldComponent()
							.getButton()) {
				editTextWithPlaceholder();
			} else if (event.getButton() == getStoreVariableTextFieldComponent()
					.getButton()) {
				editStoreResultToVariable();
			} else if (event.getButton() == getMessageKeyTextFieldComponent()
					.getButton()) {
				editMessageKey();
			} else if (event.getButton() == getAnswerOptionsTextFieldComponent()
					.getButton()) {
				editAnswerOptionsWithPlaceholder();
			} else if (event.getButton() == getNoReplyTextFieldComponent()
					.getButton()) {
				editNoReplyValue();
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
			} else if (event.getButton() == getMinutesButtonInf()) {
				getMinutesUntilHandledAsNotAnsweredSlider().setValue(
						(double) (ImplementationConstants.MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX_MICRO_DIALOG_MESSAGE
								+ 1));
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
				microDialogMessage.getTextWithPlaceholders(),
				allPossibleMessageVariables,
				new LocalizedPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change text with placeholders
							getInterventionAdministrationManagerService()
									.microDialogMessageSetTextWithPlaceholders(
											microDialogMessage,
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
				microDialogMessage.getStoreValueToVariableWithName(),
				allPossibleMessageVariables,
				new ShortPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change store result to variable
							getInterventionAdministrationManagerService()
									.microDialogMessageSetStoreResultToVariable(
											microDialogMessage,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}

	public void editMessageKey() {
		log.debug("Edit message key");
		val allPossibleMessageVariables = getInterventionAdministrationManagerService()
				.getAllWritableMessageVariablesOfIntervention(interventionId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_MESSAGE_KEY,
				microDialogMessage.getNonUniqueKey(),
				allPossibleMessageVariables,
				new ShortPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change store result to variable
							getInterventionAdministrationManagerService()
									.microDialogMessageSetNonUniqueKey(
											microDialogMessage,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}

	public void editAnswerOptionsWithPlaceholder() {
		log.debug("Edit answer options with placeholder");
		val allPossibleMessageVariables = getInterventionAdministrationManagerService()
				.getAllPossibleMessageVariablesOfIntervention(interventionId);
		showModalLStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TEXT_WITH_PLACEHOLDERS,
				microDialogMessage.getAnswerOptionsWithPlaceholders(),
				allPossibleMessageVariables,
				new LocalizedPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change text with placeholders
							getInterventionAdministrationManagerService()
									.microDialogMessageSetAnswerOptionsWithPlaceholders(
											microDialogMessage,
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

	public void editNoReplyValue() {
		log.debug("Edit no reply value");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VALUE_ON_NO_REPLY,
				microDialogMessage.getNoReplyValue(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change no reply value
							getInterventionAdministrationManagerService()
									.microDialogMessageSetNoReplyValue(
											microDialogMessage,
											getStringValue());
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
		val newMicroDialogMessageRule = getInterventionAdministrationManagerService()
				.microDialogMessageRuleCreate(microDialogMessage.getId());

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_MICRO_DIALOG_MESSAGE_RULE,
				new MicroDialogMessageRuleEditComponentWithController(
						newMicroDialogMessageRule, interventionId),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						rulesBeanContainer.addItem(
								newMicroDialogMessageRule.getId(),
								UIMicroDialogMessageRule.class
										.cast(newMicroDialogMessageRule
												.toUIModelObject()));
						rulesTable.select(newMicroDialogMessageRule.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_MESSAGE_RULE_CREATED);

						closeWindow();
					}
				});
	}

	public void editRule() {
		log.debug("Edit rule");
		val selectedMicroDialogMessageRule = selectedUIMicroDialogMessageRule
				.getRelatedModelObject(MicroDialogMessageRule.class);

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_MICRO_DIALOG_MESSAGE_RULE,
				new MicroDialogMessageRuleEditComponentWithController(
						selectedMicroDialogMessageRule, interventionId),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						removeAndAddModelObjectToBeanContainer(
								rulesBeanContainer,
								selectedMicroDialogMessageRule);
						rulesTable.sort();
						rulesTable
								.select(selectedMicroDialogMessageRule.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_MESSAGE_RULE_UPDATED);

						closeWindow();
					}
				});
	}

	public void moveRule(final boolean moveUp) {
		log.debug("Move rule {}", moveUp ? "up" : "down");

		val selectedMicroDialogMessageRule = selectedUIMicroDialogMessageRule
				.getRelatedModelObject(MicroDialogMessageRule.class);
		val swappedMicroDialogMessageRule = getInterventionAdministrationManagerService()
				.microDialogMessageRuleMove(selectedMicroDialogMessageRule,
						moveUp);

		if (swappedMicroDialogMessageRule == null) {
			log.debug("Rule is already at top/end of list");
			return;
		}

		removeAndAddModelObjectToBeanContainer(rulesBeanContainer,
				swappedMicroDialogMessageRule);
		removeAndAddModelObjectToBeanContainer(rulesBeanContainer,
				selectedMicroDialogMessageRule);
		rulesTable.sort();
		rulesTable.select(selectedMicroDialogMessageRule.getId());
	}

	public void deleteRule() {
		log.debug("Delete rule");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedMicroDialogMessageRule = selectedUIMicroDialogMessageRule
							.getRelatedModelObject(
									MicroDialogMessageRule.class);

					// Delete rule
					getInterventionAdministrationManagerService()
							.microDialogMessageRuleDelete(
									selectedMicroDialogMessageRule);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				rulesTable.removeItem(selectedUIMicroDialogMessageRule
						.getRelatedModelObject(MicroDialogMessageRule.class)
						.getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_MESSAGE_RULE_DELETED);

				closeWindow();
			}
		}, null);
	}

	@Override
	public void updateLinkedMediaObjectId(final ObjectId mediaObjectId) {
		getInterventionAdministrationManagerService()
				.microDialogMessageSetLinkedMediaObject(microDialogMessage,
						mediaObjectId);
	}

	private class SliderValueChangeListener implements ValueChangeListener {

		@Override
		public void valueChange(final ValueChangeEvent event) {
			if (event
					.getProperty() == getMinutesUntilHandledAsNotAnsweredSlider()) {
				int newValue = ((Double) event.getProperty().getValue())
						.intValue();
				if (newValue > ImplementationConstants.MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED_MAX_MICRO_DIALOG_MESSAGE) {
					newValue = Integer.MAX_VALUE;
				}

				getInterventionAdministrationManagerService()
						.microDialogMessageSetMinutesUntilMessageIsHandledAsUnanswered(
								microDialogMessage, newValue);
			}

			adjust();
		}
	}
}
