package ch.ethz.mc.ui.views.components.feedback;

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
import ch.ethz.mc.model.persistent.FeedbackSlide;
import ch.ethz.mc.model.persistent.FeedbackSlideRule;
import ch.ethz.mc.model.ui.UIFeedbackSlideRule;
import ch.ethz.mc.ui.views.components.basics.LocalizedPlaceholderStringEditComponent;
import ch.ethz.mc.ui.views.components.basics.MediaObjectIntegrationComponentWithController.MediaObjectCreationOrDeleteionListener;
import ch.ethz.mc.ui.views.components.basics.PlaceholderStringEditComponent;
import ch.ethz.mc.ui.views.components.basics.ShortStringEditComponent;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;

/**
 * Provides the feedback slide rule edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class FeedbackSlideEditComponentWithController extends
		FeedbackSlideEditComponent implements
		MediaObjectCreationOrDeleteionListener {

	private final FeedbackSlide									feedbackSlide;
	private final ObjectId										relatedScreeningSurveyId;

	private final Table											rulesTable;

	private UIFeedbackSlideRule									selectedUIFeedbackSlideRule	= null;

	private final BeanContainer<ObjectId, UIFeedbackSlideRule>	rulesBeanContainer;

	public FeedbackSlideEditComponentWithController(
			final FeedbackSlide feedbackSlide,
			final ObjectId relatedScreeningSurveyId) {
		super();

		this.feedbackSlide = feedbackSlide;
		this.relatedScreeningSurveyId = relatedScreeningSurveyId;

		// table options
		rulesTable = getRulesTable();

		// table content
		val rulesOfFeedbackSlide = getScreeningSurveyAdministrationManagerService()
				.getAllFeedbackSlideRulesOfFeedbackSlide(feedbackSlide.getId());

		rulesBeanContainer = createBeanContainerForModelObjects(
				UIFeedbackSlideRule.class, rulesOfFeedbackSlide);

		rulesTable.setContainerDataSource(rulesBeanContainer);
		rulesTable.setSortContainerPropertyId(UIFeedbackSlideRule
				.getSortColumn());
		rulesTable.setVisibleColumns(UIFeedbackSlideRule.getVisibleColumns());
		rulesTable.setColumnHeaders(UIFeedbackSlideRule.getColumnHeaders());
		rulesTable.setSortAscending(true);
		rulesTable.setSortEnabled(false);

		// handle table selection change
		rulesTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = rulesTable.getValue();
				if (objectId == null) {
					setRuleSelected(false);
					selectedUIFeedbackSlideRule = null;
				} else {
					selectedUIFeedbackSlideRule = getUIModelObjectFromTableByObjectId(
							rulesTable, UIFeedbackSlideRule.class, objectId);
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

		getTitleWithPlaceholdersTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		getCommentTextFieldComponent().getButton().addClickListener(
				buttonClickListener);
		getOptionalLayoutAttributeTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);
		getFeedbackTextWithPlaceholdersTextField().getButton()
				.addClickListener(buttonClickListener);

		// Handle media object to component
		if (feedbackSlide.getLinkedMediaObject() == null) {
			getIntegratedMediaObjectComponent().setMediaObject(null, this);
		} else {
			val mediaObject = getInterventionAdministrationManagerService()
					.getMediaObject(feedbackSlide.getLinkedMediaObject());
			getIntegratedMediaObjectComponent().setMediaObject(mediaObject,
					this);
		}

		adjust();
	}

	private void adjust() {
		// Adjust variable text fields
		getTitleWithPlaceholdersTextFieldComponent().setValue(
				feedbackSlide.getTitleWithPlaceholders().toString());
		getCommentTextFieldComponent().setValue(feedbackSlide.getComment());
		getOptionalLayoutAttributeTextFieldComponent().setValue(
				feedbackSlide.getOptionalLayoutAttributeWithPlaceholders());
		getFeedbackTextWithPlaceholdersTextField().setValue(
				feedbackSlide.getTextWithPlaceholders().toString());
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
			} else if (event.getButton() == getTitleWithPlaceholdersTextFieldComponent()
					.getButton()) {
				changeTitleWithPlaceholders();
			} else if (event.getButton() == getCommentTextFieldComponent()
					.getButton()) {
				changeComment();
			} else if (event.getButton() == getOptionalLayoutAttributeTextFieldComponent()
					.getButton()) {
				changeOptionalLayoutAttributeWithPlaceholders();
			} else if (event.getButton() == getFeedbackTextWithPlaceholdersTextField()
					.getButton()) {
				changeFeedbackTextWithPlaceholders();
			}
			event.getButton().setEnabled(true);
		}
	}

	public void changeTitleWithPlaceholders() {
		log.debug("Edit title with placeholder");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
						relatedScreeningSurveyId);
		showModalLStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TITLE_WITH_PLACEHOLDERS,
				feedbackSlide.getTitleWithPlaceholders(), allPossibleVariables,
				new LocalizedPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change title with placeholders
							getScreeningSurveyAdministrationManagerService()
									.feedbackSlideChangeTitle(feedbackSlide,
											getLStringValue(),
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

	public void changeComment() {
		log.debug("Edit comment");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_COMMENT,
				feedbackSlide.getComment(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change comment
							getScreeningSurveyAdministrationManagerService()
							.feedbackSlideChangeComment(feedbackSlide,
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

	public void changeOptionalLayoutAttributeWithPlaceholders() {
		log.debug("Edit optional layout attribute with placeholders");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
						relatedScreeningSurveyId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_OPTIONAL_LAYOUT_ATTRIBUTE_WITH_PLACEHOLDERS,
				feedbackSlide.getOptionalLayoutAttributeWithPlaceholders(),
				allPossibleVariables, new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change optional layout attribute
							getScreeningSurveyAdministrationManagerService()
									.feedbackSlideChangeOptionalLayoutAttributeWithPlaceholders(
											feedbackSlide, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}

	public void changeFeedbackTextWithPlaceholders() {
		log.debug("Edit feedback text with placeholders");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
						relatedScreeningSurveyId);
		showModalLStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_FEEDBACK_TEXT_WITH_PLACEHOLDERS,
				feedbackSlide.getTextWithPlaceholders(), allPossibleVariables,
				new LocalizedPlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change feedback text with placeholders
							getScreeningSurveyAdministrationManagerService()
									.feedbackSlideChangeTextWithPlaceholders(
											feedbackSlide, getLStringValue(),
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

	public void createRule() {
		log.debug("Create rule");
		val newFeedbackSlideRule = getScreeningSurveyAdministrationManagerService()
				.feedbackSlideRuleCreate(feedbackSlide.getId());

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_FEEDBACK_SLIDE_RULE,
				new FeedbackSlideRuleEditComponentWithController(
						newFeedbackSlideRule, relatedScreeningSurveyId),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						rulesBeanContainer.addItem(
								newFeedbackSlideRule.getId(),
								UIFeedbackSlideRule.class
										.cast(newFeedbackSlideRule
												.toUIModelObject()));
						rulesTable.select(newFeedbackSlideRule.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__FEEDBACK_SLIDE_RULE_CREATED);

						closeWindow();
					}
				});
	}

	public void editRule() {
		log.debug("Edit rule");
		val selectedFeedbackSlideRule = selectedUIFeedbackSlideRule
				.getRelatedModelObject(FeedbackSlideRule.class);

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_FEEDBACK_SLIDE_RULE,
				new FeedbackSlideRuleEditComponentWithController(
						selectedFeedbackSlideRule, relatedScreeningSurveyId),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						removeAndAddModelObjectToBeanContainer(
								rulesBeanContainer, selectedFeedbackSlideRule);
						rulesTable.sort();
						rulesTable.select(selectedFeedbackSlideRule.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__FEEDBACK_SLIDE_RULE_UPDATED);

						closeWindow();
					}
				});
	}

	public void moveRule(final boolean moveUp) {
		log.debug("Move rule {}", moveUp ? "up" : "down");

		val selectedFeedbackSlideRule = selectedUIFeedbackSlideRule
				.getRelatedModelObject(FeedbackSlideRule.class);
		val swappedFeedbackSlideRule = getScreeningSurveyAdministrationManagerService()
				.feedbackSlideRuleMove(selectedFeedbackSlideRule, moveUp);

		if (swappedFeedbackSlideRule == null) {
			log.debug("Rule is already at top/end of list");
			return;
		}

		removeAndAddModelObjectToBeanContainer(rulesBeanContainer,
				swappedFeedbackSlideRule);
		removeAndAddModelObjectToBeanContainer(rulesBeanContainer,
				selectedFeedbackSlideRule);
		rulesTable.sort();
		rulesTable.select(selectedFeedbackSlideRule.getId());
	}

	public void deleteRule() {
		log.debug("Delete rule");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedFeedbackSlideRule = selectedUIFeedbackSlideRule.getRelatedModelObject(FeedbackSlideRule.class);

					// Delete rule
					getScreeningSurveyAdministrationManagerService()
							.feedbackSlideRuleDelete(selectedFeedbackSlideRule);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				rulesTable
						.removeItem(selectedUIFeedbackSlideRule
								.getRelatedModelObject(FeedbackSlideRule.class)
								.getId());
				getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__FEEDBACK_SLIDE_RULE_DELETED);

				closeWindow();
			}
		}, null);
	}

	@Override
	public void updateLinkedMediaObjectId(final ObjectId linkedMediaObjectId) {
		getScreeningSurveyAdministrationManagerService()
				.feedbackSlideSetLinkedMediaObject(feedbackSlide,
						linkedMediaObjectId);
	}
}
