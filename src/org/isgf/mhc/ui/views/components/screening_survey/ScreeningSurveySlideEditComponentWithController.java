package org.isgf.mhc.ui.views.components.screening_survey;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.server.Feedback;
import org.isgf.mhc.model.server.ScreeningSurveySlide;
import org.isgf.mhc.model.server.ScreeningSurveySlideRule;
import org.isgf.mhc.model.server.types.ScreeningSurveySlideQuestionTypes;
import org.isgf.mhc.model.ui.UIAnswer;
import org.isgf.mhc.model.ui.UIFeedback;
import org.isgf.mhc.model.ui.UIScreeningSurveySlideRule;
import org.isgf.mhc.tools.StringValidator;
import org.isgf.mhc.ui.NotificationMessageException;
import org.isgf.mhc.ui.views.components.basics.MediaObjectIntegrationComponentWithController.MediaObjectCreationOrDeleteionListener;
import org.isgf.mhc.ui.views.components.basics.PlaceholderStringEditComponent;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

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

	private final Table													answersTable;
	private final Table													rulesTable;

	private UIAnswer													selectedUIAnswer					= null;
	private UIScreeningSurveySlideRule									selectedUIScreeningSurveySlideRule	= null;

	private final BeanContainer<Integer, UIAnswer>						answersBeanContainer;
	private final BeanContainer<ObjectId, UIScreeningSurveySlideRule>	rulesBeanContainer;

	private final ValueChangeListener									preselectedComboBoxListener;

	public ScreeningSurveySlideEditComponentWithController(
			final ScreeningSurveySlide screeningSurveySlide) {
		super();

		this.screeningSurveySlide = screeningSurveySlide;

		// table options
		answersTable = getAnswersTable();
		rulesTable = getRulesTable();

		// table content
		answersBeanContainer = new BeanContainer<Integer, UIAnswer>(
				UIAnswer.class);

		val answersWithPlaceholders = screeningSurveySlide
				.getAnswersWithPlaceholders();
		val answerValues = screeningSurveySlide.getAnswerValues();
		for (int i = 0; i < answersWithPlaceholders.length; i++) {
			answersBeanContainer.addItem(i, new UIAnswer(i,
					answersWithPlaceholders[i], answerValues[i]));
		}

		answersTable.setContainerDataSource(answersBeanContainer);
		answersTable.setSortContainerPropertyId(UIAnswer.getSortColumn());
		answersTable.setVisibleColumns(UIAnswer.getVisibleColumns());
		answersTable.setColumnHeaders(UIAnswer.getColumnHeaders());
		answersTable.setSortAscending(true);
		answersTable.setSortEnabled(false);

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

				adjust();

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
								screeningSurveySlide, preselectedAnswer);
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

				adjust();
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

						adjust();
					}
				});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
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
		getDeleteRuleButton().addClickListener(buttonClickListener);

		getTitleWithPlaceholdersTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		getOptionalLayoutAttributeTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		getQuestionTextWithPlaceholdersTextField().getButton()
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

		adjust();
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
						screeningSurveySlide, answers, values);
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

			if (screeningSurveySlide.getPreSelectedAnswer() == i) {
				preselectedAnswerComboBox.select(uiAnswer);
			}
		}

		preselectedAnswerComboBox
				.addValueChangeListener(preselectedComboBoxListener);
	}

	private void adjust() {
		// Adjust variable text fields
		getTitleWithPlaceholdersTextFieldComponent().setValue(
				screeningSurveySlide.getTitleWithPlaceholders());
		getOptionalLayoutAttributeTextFieldComponent().setValue(
				screeningSurveySlide.getOptionalLayoutAttribute());
		getQuestionTextWithPlaceholdersTextField().setValue(
				screeningSurveySlide.getQuestionWithPlaceholders());
		getStoreVariableTextFieldComponent().setValue(
				screeningSurveySlide.getStoreValueToVariableWithName());

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
			if (event.getButton() == getNewAnswerButton()) {
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
			} else if (event.getButton() == getStoreVariableTextFieldComponent()
					.getButton()) {
				changeStoreResultVariable();
			}
		}
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
				ImplementationContants.DEFAULT_ANSWER_NAME,
				ImplementationContants.DEFAULT_ANSWER_VALUE);

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
							screeningSurveySlide, -1);
		}

		saveAnswersWithValues();
		adjustPreselectedAnswer();
	}

	public void editAnswerAnswer() {
		log.debug("Edit answer answer with placeholders");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariables(
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
				.getAllPossibleScreenigSurveyVariables(
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

						adjust();

						closeWindow();
					}
				}, null);
	}

	public void changeOptionalLayoutAttribute() {
		log.debug("Edit optional layout attribute");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_OPTIONAL_LAYOUT_ATTRIBUTE,
				screeningSurveySlide.getOptionalLayoutAttribute(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change optional layout attribute
							getScreeningSurveyAdministrationManagerService()
									.screeningSurveySlideChangeOptionalLayoutAttribute(
											screeningSurveySlide,
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

	public void changeQuestionWithPlaceholders() {
		log.debug("Edit question with placeholder");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariables(
						screeningSurveySlide.getScreeningSurvey());
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_QUESTION_TEXT_WITH_PLACEHOLDERS,
				screeningSurveySlide.getQuestionWithPlaceholders(),
				allPossibleVariables, new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change question with placeholders
							getScreeningSurveyAdministrationManagerService()
									.screeningSurveySlideChangeQuestion(
											screeningSurveySlide,
											getStringValue(),
											allPossibleVariables);
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}

	public void changeStoreResultVariable() {
		log.debug("Edit store result to variable");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_VARIABLE,
				screeningSurveySlide.getStoreValueToVariableWithName(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change store result to variable
							getScreeningSurveyAdministrationManagerService()
									.screeningSurveySlideChangeStoreResultToVariable(
											screeningSurveySlide,
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
