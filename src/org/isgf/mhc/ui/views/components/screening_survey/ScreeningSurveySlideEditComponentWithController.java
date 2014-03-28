package org.isgf.mhc.ui.views.components.screening_survey;

import java.util.ArrayList;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.Feedback;
import org.isgf.mhc.model.server.ScreeningSurveySlide;
import org.isgf.mhc.model.server.types.ScreeningSurveySlideQuestionTypes;
import org.isgf.mhc.model.ui.UIAnswer;
import org.isgf.mhc.model.ui.UIFeedback;
import org.isgf.mhc.model.ui.UIModelObject;
import org.isgf.mhc.model.ui.UIScreeningSurveySlideRule;
import org.isgf.mhc.ui.views.components.basics.PlaceholderStringEditComponent;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
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
		ScreeningSurveySlideEditComponent {

	private final ScreeningSurveySlide									screeningSurveySlide;

	private final Table													answersTable;
	private final Table													rulesTable;

	private UIAnswer													selectedUIAnswer					= null;
	private UIScreeningSurveySlideRule									selectedUIScreeningSurveySlideRule	= null;

	private final BeanContainer<Integer, UIAnswer>						answersBeanContainer;
	private final BeanContainer<ObjectId, UIScreeningSurveySlideRule>	rulesBeanContainer;

	public ScreeningSurveySlideEditComponentWithController(
			final ScreeningSurveySlide screeningSurveySlide) {
		super();

		this.screeningSurveySlide = screeningSurveySlide;

		// table options
		answersTable = getAnswersTable();
		rulesTable = getRulesTable();

		// table content
		val answersOfScreeningSurveySlide = new ArrayList<UIAnswer>();

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
				.getAllScreeningSurveySlidesRulesOfScreeningSurveySlide(
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
		val questionTypes = getScreeningSurveyAdministrationManagerService()
				.getAllTemplatePaths();

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

		adjustPreselectedAnswer();

		getPreselectedAnswerComboBox().addValueChangeListener(
				new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						final String preselectedAnswer = (String) event
								.getProperty().getValue();

						// TODO adjust
						// log.debug("Adjust template path to {}",
						// screeningSurveySlideQuestionType);
						// getScreeningSurveyAdministrationManagerService()
						// .screeningSurveyChangeTemplatePath(
						// screeningSurveySlide,
						// screeningSurveySlideQuestionType);

						adjust();

					}
				});

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

		adjust();
	}

	private void adjustPreselectedAnswer() {
		val preselectedAnswerComboBox = getPreselectedAnswerComboBox();
		int i = 0;
		for (val answer : screeningSurveySlide.getAnswersWithPlaceholders()) {
			preselectedAnswerComboBox.addItem(answer);
			if (screeningSurveySlide.getPreSelectedAnswer() == i) {
				preselectedAnswerComboBox.select(answer);
			}
			i++;
		}
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
				// createAnswer();
			} else if (event.getButton() == getEditAnswerAnswerButton()) {
				// editAnswerAnswer();
			} else if (event.getButton() == getEditAnswerValueButton()) {
				// editAnswerValue();
			} else if (event.getButton() == getMoveUpAnswerButton()) {
				// moveAnswer(true);
			} else if (event.getButton() == getMoveDownAnswerButton()) {
				// moveSlide(false);
			} else if (event.getButton() == getDeleteAnswerButton()) {
				// deleteAnswer();
			} else if (event.getButton() == getNewRuleButton()) {
				// createRule();
			} else if (event.getButton() == getEditRuleButton()) {
				// editRule();
			} else if (event.getButton() == getMoveUpRuleButton()) {
				// moveRule(true);
			} else if (event.getButton() == getMoveDownRuleButton()) {
				// moveRule(false);
			} else if (event.getButton() == getDeleteRuleButton()) {
				// deleteRule();
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

	// public void editPassword() {
	// log.debug("Edit password");
	// showModalStringValueEditWindow(
	// AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_PASSWORD,
	// screeningSurveySlide.getPassword(), null,
	// new ShortStringEditComponent(),
	// new ExtendableButtonClickListener() {
	//
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// try {
	// // Change password
	// getScreeningSurveyAdministrationManagerService()
	// .screeningSurveyChangePassword(
	// screeningSurveySlide,
	// getStringValue());
	// } catch (final Exception e) {
	// handleException(e);
	// return;
	// }
	//
	// adjust();
	//
	// closeWindow();
	// }
	// }, null);
	// }
	//
	// public void createSlide() {
	// log.debug("Create slide");
	// val newScreeningSurveySlide =
	// getScreeningSurveyAdministrationManagerService()
	// .screeningSurveySlideCreate(screeningSurveySlide.getId());
	//
	// // TODO folgendes NULL gegen passende komponente austauschen
	// showModalModelObjectEditWindow(
	// AdminMessageStrings.ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__CREATE_SCREENING_SURVEY_SLIDE,
	// null, new ExtendableButtonClickListener() {
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// // Adapt UI
	// answersBeanContainer.addItem(newScreeningSurveySlide
	// .getId(),
	// UIScreeningSurveySlide.class
	// .cast(newScreeningSurveySlide
	// .toUIModelObject()));
	// answersTable.select(newScreeningSurveySlide.getId());
	// getAdminUI()
	// .showInformationNotification(
	// AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_CREATED);
	//
	// closeWindow();
	// }
	// });
	// }
	//
	// public void switchActiveOrInactive() {
	// log.debug("Switch screening survey");
	// showConfirmationWindow(new ExtendableButtonClickListener() {
	//
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// try {
	// getScreeningSurveyAdministrationManagerService()
	// .screeningSurveySetActive(screeningSurveySlide,
	// !screeningSurveySlide.isActive());
	// } catch (final Exception e) {
	// closeWindow();
	// handleException(e);
	// return;
	// }
	//
	// adjustActiveOrInactive();
	// closeWindow();
	// }
	// }, null);
	// }
	//
	// public void editSlide() {
	// log.debug("Edit slide");
	// val selectedMonitoringSlide = selectedUIScreeningSurveySlideRule
	// .getRelatedModelObject(ScreeningSurveySlide.class);
	//
	// // TODO folgendes NULL gegen passende komponente austauschen
	// showModalModelObjectEditWindow(
	// AdminMessageStrings.ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__EDIT_SCREENING_SURVEY_SLIDE,
	// null, new ExtendableButtonClickListener() {
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// // Adapt UI
	// removeAndAdd(answersBeanContainer,
	// selectedMonitoringSlide);
	// answersTable.sort();
	// answersTable.select(selectedMonitoringSlide.getId());
	// getAdminUI()
	// .showInformationNotification(
	// AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_UPDATED);
	//
	// closeWindow();
	// }
	// });
	// }
	//
	// public void moveSlide(final boolean moveUp) {
	// log.debug("Move slide {}", moveUp ? "up" : "down");
	//
	// val selectedMonitoringSlide = selectedUIScreeningSurveySlideRule
	// .getRelatedModelObject(ScreeningSurveySlide.class);
	// val swappedMonitoringSlide =
	// getScreeningSurveyAdministrationManagerService()
	// .screeningSurveySlideMove(selectedMonitoringSlide, moveUp);
	//
	// if (swappedMonitoringSlide == null) {
	// log.debug("Slide is already at top/end of list");
	// return;
	// }
	//
	// removeAndAdd(answersBeanContainer, swappedMonitoringSlide);
	// removeAndAdd(answersBeanContainer, selectedMonitoringSlide);
	// answersTable.sort();
	// answersTable.select(selectedMonitoringSlide.getId());
	// }
	//
	// public void deleteSlide() {
	// log.debug("Delete slide");
	// showConfirmationWindow(new ExtendableButtonClickListener() {
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// try {
	// val selectedMonitoringSlide =
	// selectedUIScreeningSurveySlideRule.getRelatedModelObject(ScreeningSurveySlide.class);
	//
	// // Delete variable
	// getScreeningSurveyAdministrationManagerService()
	// .screeningSurveySlideDelete(selectedMonitoringSlide);
	// } catch (final Exception e) {
	// closeWindow();
	// handleException(e);
	// return;
	// }
	//
	// // Adapt UI
	// answersTable.removeItem(selectedUIScreeningSurveySlideRule
	// .getRelatedModelObject(ScreeningSurveySlide.class)
	// .getId());
	// getAdminUI()
	// .showInformationNotification(
	// AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_DELETED);
	//
	// closeWindow();
	// }
	// }, null);
	// }
	//
	// public void createFeedback() {
	// log.debug("Create feedback");
	// showModalStringValueEditWindow(
	// AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_FEEDBACK,
	// null, null, new ShortStringEditComponent(),
	// new ExtendableButtonClickListener() {
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// final Feedback newFeedback;
	// try {
	// val newFeedbackName = getStringValue();
	//
	// // Create feedback
	// newFeedback = getScreeningSurveyAdministrationManagerService()
	// .feedbackCreate(newFeedbackName,
	// screeningSurveySlide.getId());
	// } catch (final Exception e) {
	// handleException(e);
	// return;
	// }
	//
	// // Adapt UI
	// rulesBeanContainer.addItem(newFeedback.getId(),
	// UIFeedback.class.cast(newFeedback
	// .toUIModelObject()));
	// rulesTable.sort();
	// rulesTable.select(newFeedback.getId());
	// getAdminUI()
	// .showInformationNotification(
	// AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_CREATED);
	//
	// closeWindow();
	// }
	// }, null);
	// }
	//
	// public void renameFeedback() {
	// log.debug("Rename feedback");
	//
	// showModalStringValueEditWindow(
	// AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_FEEDBACK,
	// selectedUIFeedback.getRelatedModelObject(Feedback.class)
	// .getName(), null, new ShortStringEditComponent(),
	// new ExtendableButtonClickListener() {
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// BeanItem<UIFeedback> beanItem;
	// try {
	// val selectedFeedback = selectedUIFeedback
	// .getRelatedModelObject(Feedback.class);
	//
	// beanItem = getBeanItemFromTableByObjectId(
	// rulesTable, UIFeedback.class,
	// selectedFeedback.getId());
	//
	// // Change name
	// getScreeningSurveyAdministrationManagerService()
	// .feedbackChangeName(selectedFeedback,
	// getStringValue());
	// } catch (final Exception e) {
	// handleException(e);
	// return;
	// }
	//
	// // Adapt UI
	// getStringItemProperty(beanItem,
	// UIFeedback.FEEDBACK_NAME).setValue(
	// selectedUIFeedback.getRelatedModelObject(
	// Feedback.class).getName());
	// rulesTable.sort();
	//
	// getAdminUI()
	// .showInformationNotification(
	// AdminMessageStrings.NOTIFICATION__FEEDBACK_RENAMED);
	// closeWindow();
	// }
	// }, null);
	// }
	//
	// public void editFeedback() {
	// log.debug("Edit feedback");
	// val selectedFeedback = selectedUIFeedback
	// .getRelatedModelObject(Feedback.class);
	//
	// // TODO folgendes NULL gegen passende komponente austauschen
	// showModalModelObjectEditWindow(
	// AdminMessageStrings.ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__EDIT_FEEDBACK,
	// null, new ExtendableButtonClickListener() {
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// // Adapt UI
	// removeAndAdd(rulesBeanContainer, selectedFeedback);
	// rulesTable.sort();
	// rulesTable.select(selectedFeedback.getId());
	// getAdminUI()
	// .showInformationNotification(
	// AdminMessageStrings.NOTIFICATION__FEEDBACK_UPDATED);
	//
	// closeWindow();
	// }
	// });
	// }
	//
	// public void deleteFeedback() {
	// log.debug("Delete feedback");
	// showConfirmationWindow(new ExtendableButtonClickListener() {
	// @Override
	// public void buttonClick(final ClickEvent event) {
	// try {
	// val selectedFeedback =
	// selectedUIFeedback.getRelatedModelObject(Feedback.class);
	//
	// // Delete variable
	// getScreeningSurveyAdministrationManagerService()
	// .feedbackDelete(selectedFeedback);
	// } catch (final Exception e) {
	// closeWindow();
	// handleException(e);
	// return;
	// }
	//
	// // Adapt UI
	// rulesTable.removeItem(selectedUIFeedback.getRelatedModelObject(
	// Feedback.class).getId());
	// getAdminUI().showInformationNotification(
	// AdminMessageStrings.NOTIFICATION__FEEDBACK_DELETED);
	//
	// closeWindow();
	// }
	// }, null);
	// }

	/**
	 * Removes and adds a {@link ModelObject} from a {@link BeanContainer} to
	 * update the content
	 * 
	 * @param answersBeanContainer
	 * @param slide
	 */
	@SuppressWarnings("unchecked")
	protected <SubClassOfUIModelObject extends UIModelObject> void removeAndAdd(

	final BeanContainer<ObjectId, SubClassOfUIModelObject> beanContainer,
			final ModelObject modelObject) {
		beanContainer.removeItem(modelObject.getId());
		beanContainer.addItem(modelObject.getId(),
				(SubClassOfUIModelObject) modelObject.toUIModelObject());
	}

}
