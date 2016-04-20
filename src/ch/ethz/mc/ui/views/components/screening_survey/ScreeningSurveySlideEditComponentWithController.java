package ch.ethz.mc.ui.views.components.screening_survey;

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
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.persistent.Feedback;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.ScreeningSurveySlideRule;
import ch.ethz.mc.model.persistent.types.ScreeningSurveySlideQuestionTypes;
import ch.ethz.mc.model.ui.UIAnswer;
import ch.ethz.mc.model.ui.UIFeedback;
import ch.ethz.mc.model.ui.UIQuestion;
import ch.ethz.mc.model.ui.UIScreeningSurveySlideRule;
import ch.ethz.mc.tools.StringValidator;
import ch.ethz.mc.ui.NotificationMessageException;
import ch.ethz.mc.ui.views.components.basics.MediaObjectIntegrationComponentWithController.MediaObjectCreationOrDeleteionListener;
import ch.ethz.mc.ui.views.components.basics.PlaceholderStringEditComponent;
import ch.ethz.mc.ui.views.components.basics.ShortPlaceholderStringEditComponent;
import ch.ethz.mc.ui.views.components.basics.ShortStringEditComponent;
import ch.ethz.mc.ui.views.helper.CaseInsensitiveItemSorter;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;

/**
 * Provides the screening survey slide rule edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class ScreeningSurveySlideEditComponentWithController extends
ScreeningSurveySlideEditComponent implements
MediaObjectCreationOrDeleteionListener {

	private final ScreeningSurveySlide									screeningSurveySlide;

	private final Table													questionsTable;
	private final Table													answersTable;
	private final Table													rulesTable;

	private int															selectedQuestion					= 0;
	private UIQuestion													selectedUIQuestion					= null;
	private UIAnswer													selectedUIAnswer					= null;
	private UIScreeningSurveySlideRule									selectedUIScreeningSurveySlideRule	= null;

	private final BeanContainer<Integer, UIQuestion>					questionsBeanContainer;
	private final BeanContainer<Integer, UIAnswer>						answersBeanContainer;
	private final BeanContainer<ObjectId, UIScreeningSurveySlideRule>	rulesBeanContainer;

	private final ValueChangeListener									preselectedComboBoxListener;

	public ScreeningSurveySlideEditComponentWithController(
			final ScreeningSurveySlide screeningSurveySlide) {
		super();

		this.screeningSurveySlide = screeningSurveySlide;

		// table options
		questionsTable = getQuestionsTable();
		answersTable = getAnswersTable();
		rulesTable = getRulesTable();

		// table content
		// Questions table
		questionsBeanContainer = new BeanContainer<Integer, UIQuestion>(
				UIQuestion.class);
		questionsBeanContainer.setItemSorter(new CaseInsensitiveItemSorter());

		val questions = screeningSurveySlide.getQuestions();
		for (int i = 0; i < questions.size(); i++) {
			val question = questions.get(i);
			val uiQuestion = new UIQuestion(
					i,
					question.getQuestionWithPlaceholders().equals("") ? Messages
							.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
							: question.getQuestionWithPlaceholders());
			questionsBeanContainer.addItem(i, uiQuestion);

			if (i == 0) {
				selectedUIQuestion = uiQuestion;
			}
		}

		questionsTable.setContainerDataSource(questionsBeanContainer);
		questionsTable.setSortContainerPropertyId(UIQuestion.getSortColumn());
		questionsTable.setVisibleColumns(UIQuestion.getVisibleColumns());
		questionsTable.setColumnHeaders(UIQuestion.getColumnHeaders());
		questionsTable.setSortAscending(true);
		questionsTable.setSortEnabled(false);
		questionsTable.select(questionsBeanContainer.getIdByIndex(0));

		// Answers table
		answersBeanContainer = new BeanContainer<Integer, UIAnswer>(
				UIAnswer.class);

		adjustAnswersTable();

		answersTable.setContainerDataSource(answersBeanContainer);
		answersTable.setSortContainerPropertyId(UIAnswer.getSortColumn());
		answersTable.setVisibleColumns(UIAnswer.getVisibleColumns());
		answersTable.setColumnHeaders(UIAnswer.getColumnHeaders());
		answersTable.setSortAscending(true);
		answersTable.setSortEnabled(false);

		// Rules table
		val rulesOfScreeningSurveySlide = getScreeningSurveyAdministrationManagerService()
				.getAllScreeningSurveySlideRulesOfScreeningSurveySlide(
						screeningSurveySlide.getId());

		rulesBeanContainer = createBeanContainerForModelObjects(
				UIScreeningSurveySlideRule.class, rulesOfScreeningSurveySlide);

		rulesTable.setContainerDataSource(rulesBeanContainer);
		rulesTable.setSortContainerPropertyId(UIScreeningSurveySlideRule
				.getSortColumn());
		rulesTable.setVisibleColumns(UIScreeningSurveySlideRule
				.getVisibleColumns());
		rulesTable.setColumnHeaders(UIScreeningSurveySlideRule
				.getColumnHeaders());
		rulesTable.setSortAscending(true);
		rulesTable.setSortEnabled(false);

		// handle table selection change
		questionsTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = questionsTable.getValue();

				val oldSelectedQuestion = selectedQuestion;

				selectedUIQuestion = getUIModelObjectFromTableByObjectId(
						questionsTable, UIQuestion.class, objectId);
				selectedQuestion = questionsBeanContainer.indexOfId(objectId);

				if (oldSelectedQuestion != selectedQuestion) {
					adjust(true);
				}
			}
		});

		answersTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = answersTable.getValue();
				if (objectId == null) {
					setAnswerSelected(false);
					selectedUIAnswer = null;
				} else {
					selectedUIAnswer = getUIModelObjectFromTableByObjectId(
							answersTable, UIAnswer.class, objectId);
					setAnswerSelected(true);
				}
			}
		});

		rulesTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = rulesTable.getValue();
				if (objectId == null) {
					setRuleSelected(false);
					selectedUIScreeningSurveySlideRule = null;
				} else {
					selectedUIScreeningSurveySlideRule = getUIModelObjectFromTableByObjectId(
							rulesTable, UIScreeningSurveySlideRule.class,
							objectId);
					setRuleSelected(true);
				}
			}
		});

		// Handle combo boxes
		val questionTypeComboBox = getQuestionTypeComboBox();
		for (val screeningSurveySlideQuestionType : ScreeningSurveySlideQuestionTypes
				.values()) {
			questionTypeComboBox.addItem(screeningSurveySlideQuestionType);
			if (screeningSurveySlide.getQuestionType().equals(
					screeningSurveySlideQuestionType)) {
				questionTypeComboBox.select(screeningSurveySlideQuestionType);
			}
		}
		questionTypeComboBox.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val screeningSurveySlideQuestionType = (ScreeningSurveySlideQuestionTypes) event
						.getProperty().getValue();

				log.debug("Adjust question type to {}",
						screeningSurveySlideQuestionType);
				getScreeningSurveyAdministrationManagerService()
				.screeningSurveySlideChangeQuestionType(
						screeningSurveySlide,
						screeningSurveySlideQuestionType);

				adjust(false);

			}
		});

		preselectedComboBoxListener = new ValueChangeListener() {
			@Override
			public void valueChange(final ValueChangeEvent event) {
				int preselectedAnswer = -1;
				if (event.getProperty().getValue() != null) {
					preselectedAnswer = ((UIAnswer) event.getProperty()
							.getValue()).getOrder();
				}

				log.debug("Adjust preselected answer to {}", preselectedAnswer);
				getScreeningSurveyAdministrationManagerService()
				.screeningSurveySlideChangePreselectedAnswer(
						screeningSurveySlide, selectedQuestion,
						preselectedAnswer);
			}
		};

		adjustPreselectedAnswer();

		val feedbacks = getScreeningSurveyAdministrationManagerService()
				.getAllFeedbacksOfScreeningSurvey(
						screeningSurveySlide.getScreeningSurvey());

		val feedbackComboBox = getFeedbackComboBox();
		for (val feedback : feedbacks) {
			val uiFeedback = feedback.toUIModelObject();
			feedbackComboBox.addItem(uiFeedback);
			if (screeningSurveySlide.getHandsOverToFeedback() != null
					&& screeningSurveySlide.getHandsOverToFeedback().equals(
							feedback.getId())) {
				feedbackComboBox.select(uiFeedback);
			}
		}
		feedbackComboBox.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val selectedUIFeedback = (UIFeedback) event.getProperty()
						.getValue();

				ObjectId feedbackToHandOver = null;
				if (selectedUIFeedback != null) {
					feedbackToHandOver = selectedUIFeedback
							.getRelatedModelObject(Feedback.class).getId();
				}
				log.debug("Adjust hand over feedback to {}", feedbackToHandOver);
				getScreeningSurveyAdministrationManagerService()
				.screeningSurveySlideChangeHandsOverToFeedback(
						screeningSurveySlide, feedbackToHandOver);

				adjust(false);
			}
		});

		// Handle checkbox
		getIsLastSlideCheckbox().addValueChangeListener(
				new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						val newValue = (Boolean) event.getProperty().getValue();

						getScreeningSurveyAdministrationManagerService()
						.screeningSurveySlideChangeStopScreeningSurvey(
								screeningSurveySlide, newValue);

						adjust(false);
					}
				});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();

		getNewQuestionButton().addClickListener(buttonClickListener);
		getDeleteQuestionButton().addClickListener(buttonClickListener);

		getNewAnswerButton().addClickListener(buttonClickListener);
		getEditAnswerAnswerButton().addClickListener(buttonClickListener);
		getEditAnswerValueButton().addClickListener(buttonClickListener);
		getMoveUpAnswerButton().addClickListener(buttonClickListener);
		getMoveDownAnswerButton().addClickListener(buttonClickListener);
		getDeleteAnswerButton().addClickListener(buttonClickListener);

		getNewRuleButton().addClickListener(buttonClickListener);
		getEditRuleButton().addClickListener(buttonClickListener);
		getMoveUpRuleButton().addClickListener(buttonClickListener);
		getMoveDownRuleButton().addClickListener(buttonClickListener);
		getLevelUpButton().addClickListener(buttonClickListener);
		getLevelDownButton().addClickListener(buttonClickListener);
		getDeleteRuleButton().addClickListener(buttonClickListener);

		getTitleWithPlaceholdersTextFieldComponent().getButton()
		.addClickListener(buttonClickListener);
		getOptionalLayoutAttributeTextFieldComponent().getButton()
		.addClickListener(buttonClickListener);
		getQuestionTextWithPlaceholdersTextField().getButton()
		.addClickListener(buttonClickListener);
		getValidationErrorMessageTextFieldComponent().getButton()
		.addClickListener(buttonClickListener);
		getDefaultVariableValueTextFieldComponent().getButton()
		.addClickListener(buttonClickListener);
		getStoreVariableTextFieldComponent().getButton().addClickListener(
				buttonClickListener);

		// Handle media object to component
		if (screeningSurveySlide.getLinkedMediaObject() == null) {
			getIntegratedMediaObjectComponent().setMediaObject(null, this);
		} else {
			val mediaObject = getInterventionAdministrationManagerService()
					.getMediaObject(screeningSurveySlide.getLinkedMediaObject());
			getIntegratedMediaObjectComponent().setMediaObject(mediaObject,
					this);
		}

		adjust(false);
	}

	private void adjustAnswersTable() {
		answersTable.select(null);
		answersBeanContainer.removeAllItems();

		val question = screeningSurveySlide.getQuestions()
				.get(selectedQuestion);

		val answersWithPlaceholders = question.getAnswersWithPlaceholders();
		val answerValues = question.getAnswerValues();
		for (int i = 0; i < answersWithPlaceholders.length; i++) {
			answersBeanContainer.addItem(i, new UIAnswer(i,
					answersWithPlaceholders[i], answerValues[i]));
		}
	}

	private void saveAnswersWithValues() {
		val itemIds = answersBeanContainer.getItemIds();
		val answers = new String[itemIds.size()];
		val values = new String[itemIds.size()];

		for (int i = 0; i < itemIds.size(); i++) {
			val itemId = itemIds.get(i);
			val uiAnswer = answersBeanContainer.getItem(itemId).getBean();
			answers[i] = uiAnswer.getAnswer();
			values[i] = uiAnswer.getValue();
		}
		getScreeningSurveyAdministrationManagerService()
		.screeningSurveySlideSetAnswersWithPlaceholdersAndValues(
				screeningSurveySlide, selectedQuestion, answers, values);
	}

	private void adjustPreselectedAnswer() {
		val preselectedAnswerComboBox = getPreselectedAnswerComboBox();

		if (preselectedComboBoxListener != null) {
			preselectedAnswerComboBox
			.removeValueChangeListener(preselectedComboBoxListener);
		}

		preselectedAnswerComboBox.removeAllItems();

		val itemIds = answersBeanContainer.getItemIds();

		for (int i = 0; i < itemIds.size(); i++) {
			val uiAnswer = answersBeanContainer.getItem(itemIds.get(i))
					.getBean();
			preselectedAnswerComboBox.addItem(uiAnswer);

			if (screeningSurveySlide.getQuestions().get(selectedQuestion)
					.getPreSelectedAnswer() == i) {
				preselectedAnswerComboBox.select(uiAnswer);
			}
		}

		preselectedAnswerComboBox
		.addValueChangeListener(preselectedComboBoxListener);
	}

	private void adjust(final boolean adjustAlsoAnswers) {
		// Adjust variable text fields
		val currentQuestion = screeningSurveySlide.getQuestions().get(
				selectedQuestion);

		getTitleWithPlaceholdersTextFieldComponent().setValue(
				screeningSurveySlide.getTitleWithPlaceholders());
		getOptionalLayoutAttributeTextFieldComponent().setValue(
				screeningSurveySlide
				.getOptionalLayoutAttributeWithPlaceholders());
		getQuestionTextWithPlaceholdersTextField().setValue(
				currentQuestion.getQuestionWithPlaceholders());
		getValidationErrorMessageTextFieldComponent().setValue(
				screeningSurveySlide.getValidationErrorMessage());
		getDefaultVariableValueTextFieldComponent().setValue(
				currentQuestion.getDefaultValue());
		getStoreVariableTextFieldComponent().setValue(
				currentQuestion.getStoreValueToVariableWithName());

		if (adjustAlsoAnswers) {
			adjustAnswersTable();
			adjustPreselectedAnswer();
		}

		// Adjust checkbox
		getIsLastSlideCheckbox().setValue(screeningSurveySlide.isLastSlide());

		if (getIsLastSlideCheckbox().getValue() == true) {
			getFeedbackComboBox().setEnabled(true);
		} else {
			getFeedbackComboBox().setEnabled(false);
			getFeedbackComboBox().select(null);
		}
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getNewQuestionButton()) {
				createQuestion();
			} else if (event.getButton() == getDeleteQuestionButton()) {
				deleteQuestion();
			} else if (event.getButton() == getNewAnswerButton()) {
				createAnswer();
			} else if (event.getButton() == getEditAnswerAnswerButton()) {
				editAnswerAnswer();
			} else if (event.getButton() == getEditAnswerValueButton()) {
				editAnswerValue();
			} else if (event.getButton() == getMoveUpAnswerButton()) {
				moveAnswer(true);
			} else if (event.getButton() == getMoveDownAnswerButton()) {
				moveAnswer(false);
			} else if (event.getButton() == getDeleteAnswerButton()) {
				deleteAnswer();
			} else if (event.getButton() == getNewRuleButton()) {
				createRule();
			} else if (event.getButton() == getEditRuleButton()) {
				editRule();
			} else if (event.getButton() == getMoveUpRuleButton()) {
				moveRule(true);
			} else if (event.getButton() == getMoveDownRuleButton()) {
				moveRule(false);
			} else if (event.getButton() == getLevelUpButton()) {
				changeRuleLevel(true);
			} else if (event.getButton() == getLevelDownButton()) {
				changeRuleLevel(false);
			} else if (event.getButton() == getDeleteRuleButton()) {
				deleteRule();
			} else if (event.getButton() == getTitleWithPlaceholdersTextFieldComponent()
					.getButton()) {
				changeTitleWithPlaceholders();
			} else if (event.getButton() == getOptionalLayoutAttributeTextFieldComponent()
					.getButton()) {
				changeOptionalLayoutAttribute();
			} else if (event.getButton() == getQuestionTextWithPlaceholdersTextField()
					.getButton()) {
				changeQuestionWithPlaceholders();
			} else if (event.getButton() == getValidationErrorMessageTextFieldComponent()
					.getButton()) {
				changeValidationErrorMessage();
			} else if (event.getButton() == getDefaultVariableValueTextFieldComponent()
					.getButton()) {
				changeDefaultVariableValue();
			} else if (event.getButton() == getStoreVariableTextFieldComponent()
					.getButton()) {
				changeStoreResultVariable();
			}
		}
	}

	public void createQuestion() {
		log.debug("Create question");

		int newId = 0;
		if (questionsBeanContainer.size() > 0) {
			newId = questionsBeanContainer.getItemIds().get(
					questionsBeanContainer.size() - 1) + 1;
		}

		log.debug("New question has id {}", newId);
		val question = getScreeningSurveyAdministrationManagerService()
				.screeningSurveySlideAddQuestion(screeningSurveySlide);
		val uiQuestion = new UIQuestion(newId,
				question.getQuestionWithPlaceholders().equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: question.getQuestionWithPlaceholders());

		questionsBeanContainer.addItem(newId, uiQuestion);
		questionsTable.select(newId);
	}

	public void deleteQuestion() {
		log.debug("Delete question");

		try {
			getScreeningSurveyAdministrationManagerService()
			.screeningSurveySlideRemoveQuestion(screeningSurveySlide,
					selectedQuestion);

			questionsBeanContainer.removeItem(selectedUIQuestion.getOrder());
			questionsTable.select(questionsBeanContainer.getIdByIndex(0));
		} catch (final NotificationMessageException e) {
			handleException(e);
		}

		adjust(true);
	}

	public void createAnswer() {
		log.debug("Create answer");

		int newId = 0;
		if (answersBeanContainer.size() > 0) {
			newId = answersBeanContainer.getItemIds().get(
					answersBeanContainer.size() - 1) + 1;
		}

		log.debug("New answer has id {}", newId);
		val uiAnswer = new UIAnswer(newId,
				ImplementationConstants.DEFAULT_ANSWER_NAME,
				String.valueOf(answersBeanContainer.size() + 1));

		answersBeanContainer.addItem(newId, uiAnswer);
		answersTable.select(newId);

		saveAnswersWithValues();
		adjustPreselectedAnswer();
	}

	public void deleteAnswer() {
		log.debug("Delete answer");

		answersBeanContainer.removeItem(selectedUIAnswer.getOrder());

		if (getPreselectedAnswerComboBox().isSelected(selectedUIAnswer)) {
			getScreeningSurveyAdministrationManagerService()
			.screeningSurveySlideChangePreselectedAnswer(
					screeningSurveySlide, selectedQuestion, -1);
		}

		answersTable.select(null);

		saveAnswersWithValues();
		adjustPreselectedAnswer();
	}

	public void editAnswerAnswer() {
		log.debug("Edit answer answer with placeholders");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
						screeningSurveySlide.getScreeningSurvey());
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_ANSWER_WITH_PLACEHOLDERS,
				selectedUIAnswer.getAnswer(), allPossibleVariables,
				new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						BeanItem<UIAnswer> beanItem;
						try {
							beanItem = getBeanItemFromTableByObjectId(
									answersTable, UIAnswer.class,
									selectedUIAnswer.getOrder());

							// Change answer
							if (!StringValidator.isValidVariableText(
									getStringValue(), allPossibleVariables)) {
								throw new NotificationMessageException(
										AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
							}

							selectedUIAnswer.setAnswer(getStringValue());

							saveAnswersWithValues();
							adjustPreselectedAnswer();
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						getStringItemProperty(beanItem, UIAnswer.ANSWER)
						.setValue(selectedUIAnswer.getAnswer());

						closeWindow();
					}
				}, null);
	}

	public void editAnswerValue() {
		log.debug("Edit answer value");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_ANSWER_VALUE,
				selectedUIAnswer.getValue(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						BeanItem<UIAnswer> beanItem;
						try {
							beanItem = getBeanItemFromTableByObjectId(
									answersTable, UIAnswer.class,
									selectedUIAnswer.getOrder());

							// Change value
							selectedUIAnswer.setValue(getStringValue());

							saveAnswersWithValues();
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						getStringItemProperty(beanItem, UIAnswer.VALUE)
						.setValue(selectedUIAnswer.getValue());

						closeWindow();
					}
				}, null);
	}

	public void moveAnswer(final boolean moveUp) {
		log.debug("Move answer {}", moveUp ? "up" : "down");

		if (moveUp
				&& selectedUIAnswer.getOrder() == answersBeanContainer
				.getIdByIndex(0)) {
			log.debug("Answer is already at top");
			return;
		} else if (!moveUp
				&& selectedUIAnswer.getOrder() == answersBeanContainer
				.getIdByIndex(answersBeanContainer.size() - 1)) {
			log.debug("Answer is already at bottom");
			return;
		}

		int swapPosition;
		if (moveUp) {
			swapPosition = answersBeanContainer.indexOfId(selectedUIAnswer
					.getOrder()) - 1;
		} else {
			swapPosition = answersBeanContainer.indexOfId(selectedUIAnswer
					.getOrder()) + 1;
		}

		val uiAnswerToSwapWith = answersBeanContainer.getItem(
				answersBeanContainer.getIdByIndex(swapPosition)).getBean();

		final int positionOnHold = selectedUIAnswer.getOrder();
		selectedUIAnswer.setOrder(uiAnswerToSwapWith.getOrder());
		uiAnswerToSwapWith.setOrder(positionOnHold);

		answersBeanContainer.removeItem(selectedUIAnswer.getOrder());
		answersBeanContainer.removeItem(uiAnswerToSwapWith.getOrder());
		answersBeanContainer.addItem(selectedUIAnswer.getOrder(),
				selectedUIAnswer);
		answersBeanContainer.addItem(uiAnswerToSwapWith.getOrder(),
				uiAnswerToSwapWith);

		answersTable.sort();
		answersTable.select(selectedUIAnswer.getOrder());

		saveAnswersWithValues();
		adjustPreselectedAnswer();
	}

	public void changeTitleWithPlaceholders() {
		log.debug("Edit title with placeholder");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
						screeningSurveySlide.getScreeningSurvey());
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TITLE_WITH_PLACEHOLDERS,
				screeningSurveySlide.getTitleWithPlaceholders(),
				allPossibleVariables, new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change title with placeholders
							getScreeningSurveyAdministrationManagerService()
							.screeningSurveySlideChangeTitle(
									screeningSurveySlide,
									getStringValue(),
									allPossibleVariables);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust(false);

						closeWindow();
					}
				}, null);
	}

	public void changeOptionalLayoutAttribute() {
		log.debug("Edit optional layout attribute");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
						screeningSurveySlide.getScreeningSurvey());
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_OPTIONAL_LAYOUT_ATTRIBUTE_WITH_PLACEHOLDERS,
				screeningSurveySlide
				.getOptionalLayoutAttributeWithPlaceholders(),
				allPossibleVariables, new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change optional layout attribute
							getScreeningSurveyAdministrationManagerService()
							.screeningSurveySlideChangeOptionalLayoutAttributeWithPlaceholders(
									screeningSurveySlide,
									getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust(false);

						closeWindow();
					}
				}, null);
	}

	public void changeQuestionWithPlaceholders() {
		log.debug("Edit question with placeholder");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
						screeningSurveySlide.getScreeningSurvey());
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_QUESTION_TEXT_WITH_PLACEHOLDERS,
				screeningSurveySlide.getQuestions().get(selectedQuestion)
				.getQuestionWithPlaceholders(), allPossibleVariables,
				new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change question with placeholders
							getScreeningSurveyAdministrationManagerService()
							.screeningSurveySlideChangeQuestion(
									screeningSurveySlide,
									selectedQuestion, getStringValue(),
									allPossibleVariables);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						val newQuestion = getStringValue();
						if (newQuestion.equals("")) {
							selectedUIQuestion.setQuestion(Messages
									.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET));
						} else {
							selectedUIQuestion.setQuestion(newQuestion);
						}

						// Adapt UI
						final BeanItem<UIQuestion> beanItem = getBeanItemFromTableByObjectId(
								questionsTable, UIQuestion.class,
								selectedUIQuestion.getOrder());
						getStringItemProperty(beanItem, UIQuestion.QUESTION)
						.setValue(selectedUIQuestion.getQuestion());

						adjust(false);

						closeWindow();
					}
				}, null);
	}

	public void changeValidationErrorMessage() {
		log.debug("Edit validation error message");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
						screeningSurveySlide.getScreeningSurvey());
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VALIDATION_ERROR_MESSAGE,
				screeningSurveySlide.getValidationErrorMessage(),
				allPossibleVariables, new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change validation error message
							getScreeningSurveyAdministrationManagerService()
							.screeningSurveySlideChangeValidationErrorMessage(
									screeningSurveySlide,
									getStringValue(),
									allPossibleVariables);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust(false);

						closeWindow();
					}
				}, null);
	}

	public void changeDefaultVariableValue() {
		log.debug("Edit default variable value");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_DEFAULT_VARIABLE_VALUE,
				screeningSurveySlide.getQuestions().get(selectedQuestion)
				.getDefaultValue(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change default variable value
							getScreeningSurveyAdministrationManagerService()
							.screeningSurveySlideChangeDefaultVariableValue(
									screeningSurveySlide,
									selectedQuestion, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust(false);

						closeWindow();
					}
				}, null);
	}

	public void changeStoreResultVariable() {
		log.debug("Edit store result to variable");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllWritableScreenigSurveyVariablesOfScreeningSurvey(
						screeningSurveySlide.getScreeningSurvey());
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VARIABLE,
				screeningSurveySlide.getQuestions().get(selectedQuestion)
				.getStoreValueToVariableWithName(),
				allPossibleVariables,
				new ShortPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change store result to variable
							getScreeningSurveyAdministrationManagerService()
							.screeningSurveySlideChangeStoreResultToVariable(
									screeningSurveySlide,
									selectedQuestion, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust(false);

						closeWindow();
					}
				}, null);
	}

	public void createRule() {
		log.debug("Create rule");
		val newScreeningSurveySlideRule = getScreeningSurveyAdministrationManagerService()
				.screeningSurveySlideRuleCreate(screeningSurveySlide.getId());

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_SCREENING_SURVEY_SLIDE_RULE,
				new ScreeningSurveySlideRuleEditComponentWithController(
						newScreeningSurveySlideRule, screeningSurveySlide
						.getScreeningSurvey()),
						new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						rulesBeanContainer.addItem(newScreeningSurveySlideRule
								.getId(), UIScreeningSurveySlideRule.class
								.cast(newScreeningSurveySlideRule
										.toUIModelObject()));
						rulesTable.select(newScreeningSurveySlideRule.getId());
						getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_RULE_CREATED);

						closeWindow();
					}
				});
	}

	public void editRule() {
		log.debug("Edit rule");
		val selectedScreeningSurveySlideRule = selectedUIScreeningSurveySlideRule
				.getRelatedModelObject(ScreeningSurveySlideRule.class);

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_SCREENING_SURVEY_SLIDE_RULE,
				new ScreeningSurveySlideRuleEditComponentWithController(
						selectedScreeningSurveySlideRule, screeningSurveySlide
						.getScreeningSurvey()),
						new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						removeAndAddModelObjectToBeanContainer(
								rulesBeanContainer,
								selectedScreeningSurveySlideRule);
						rulesTable.sort();
						rulesTable.select(selectedScreeningSurveySlideRule
								.getId());
						getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_RULE_UPDATED);

						closeWindow();
					}
				});
	}

	public void moveRule(final boolean moveUp) {
		log.debug("Move rule {}", moveUp ? "up" : "down");

		val selectedScreeningSurveySlideRule = selectedUIScreeningSurveySlideRule
				.getRelatedModelObject(ScreeningSurveySlideRule.class);
		val swappedScreeningSurveySlideRule = getScreeningSurveyAdministrationManagerService()
				.screeningSurveySlideRuleMove(selectedScreeningSurveySlideRule,
						moveUp);

		if (swappedScreeningSurveySlideRule == null) {
			log.debug("Rule is already at top/end of list");
			return;
		}

		removeAndAddModelObjectToBeanContainer(rulesBeanContainer,
				swappedScreeningSurveySlideRule);
		removeAndAddModelObjectToBeanContainer(rulesBeanContainer,
				selectedScreeningSurveySlideRule);
		rulesTable.sort();
		rulesTable.select(selectedScreeningSurveySlideRule.getId());
	}

	public void changeRuleLevel(final boolean moveUp) {
		log.debug("Change rule level {}", moveUp ? "up" : "down");

		val selectedScreeningSurveySlideRule = selectedUIScreeningSurveySlideRule
				.getRelatedModelObject(ScreeningSurveySlideRule.class);

		getScreeningSurveyAdministrationManagerService()
		.screeningSurveySlideRuleChangeLevel(
				selectedScreeningSurveySlideRule, moveUp);

		removeAndAddModelObjectToBeanContainer(rulesBeanContainer,
				selectedScreeningSurveySlideRule);
		rulesTable.sort();
		rulesTable.select(selectedScreeningSurveySlideRule.getId());
	}

	public void deleteRule() {
		log.debug("Delete rule");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedScreeningSurveySlideRule = selectedUIScreeningSurveySlideRule.getRelatedModelObject(ScreeningSurveySlideRule.class);

					// Delete rule
					getScreeningSurveyAdministrationManagerService()
					.screeningSurveySlideRuleDelete(
							selectedScreeningSurveySlideRule);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				rulesTable.removeItem(selectedUIScreeningSurveySlideRule
						.getRelatedModelObject(ScreeningSurveySlideRule.class)
						.getId());
				getAdminUI()
				.showInformationNotification(
						AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_RULE_DELETED);

				closeWindow();
			}
		}, null);
	}

	@Override
	public void updateLinkedMediaObjectId(final ObjectId linkedMediaObjectId) {
		getScreeningSurveyAdministrationManagerService()
		.screeningSurveySlideSetLinkedMediaObject(screeningSurveySlide,
				linkedMediaObjectId);
	}
}
