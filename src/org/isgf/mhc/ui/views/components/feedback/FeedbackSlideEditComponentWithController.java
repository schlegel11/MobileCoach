package org.isgf.mhc.ui.views.components.feedback;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.persistent.FeedbackSlide;
import org.isgf.mhc.model.persistent.FeedbackSlideRule;
import org.isgf.mhc.model.ui.UIFeedbackSlideRule;
import org.isgf.mhc.ui.views.components.basics.MediaObjectIntegrationComponentWithController.MediaObjectCreationOrDeleteionListener;
import org.isgf.mhc.ui.views.components.basics.PlaceholderStringEditComponent;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;

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
				feedbackSlide.getTitleWithPlaceholders());
		getOptionalLayoutAttributeTextFieldComponent().setValue(
				feedbackSlide.getOptionalLayoutAttribute());
		getFeedbackTextWithPlaceholdersTextField().setValue(
				feedbackSlide.getTextWithPlaceholders());
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
			} else if (event.getButton() == getOptionalLayoutAttributeTextFieldComponent()
					.getButton()) {
				changeOptionalLayoutAttribute();
			} else if (event.getButton() == getFeedbackTextWithPlaceholdersTextField()
					.getButton()) {
				changeFeedbackTextWithPlaceholders();
			}
		}
	}

	public void changeTitleWithPlaceholders() {
		log.debug("Edit title with placeholder");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariables(relatedScreeningSurveyId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_TITLE_WITH_PLACEHOLDERS,
				feedbackSlide.getTitleWithPlaceholders(), allPossibleVariables,
				new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change title with placeholders
							getScreeningSurveyAdministrationManagerService()
									.feedbackSlideChangeTitle(feedbackSlide,
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
				feedbackSlide.getOptionalLayoutAttribute(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change optional layout attribute
							getScreeningSurveyAdministrationManagerService()
									.feedbackSlideChangeOptionalLayoutAttribute(
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
		log.debug("Edit feedback text with placeholder");
		val allPossibleVariables = getScreeningSurveyAdministrationManagerService()
				.getAllPossibleScreenigSurveyVariables(relatedScreeningSurveyId);
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_FEEDBACK_TEXT_WITH_PLACEHOLDERS,
				feedbackSlide.getTextWithPlaceholders(), allPossibleVariables,
				new PlaceholderStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change feedback text with placeholders
							getScreeningSurveyAdministrationManagerService()
									.feedbackSlideChangeTextWithPlaceholders(
											feedbackSlide, getStringValue(),
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
