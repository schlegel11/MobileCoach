package org.isgf.mhc.ui.views.components.screening_survey;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.ThemeImageStrings;
import org.isgf.mhc.model.server.Feedback;
import org.isgf.mhc.model.server.ScreeningSurvey;
import org.isgf.mhc.model.server.ScreeningSurveySlide;
import org.isgf.mhc.model.ui.UIFeedback;
import org.isgf.mhc.model.ui.UIScreeningSurveySlide;
import org.isgf.mhc.ui.views.components.basics.ShortStringEditComponent;
import org.isgf.mhc.ui.views.components.feedback.FeedbackEditComponentWithController;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;

/**
 * Provides the screening survey edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class ScreeningSurveyEditComponentWithController extends
		ScreeningSurveyEditComponent {

	private final ScreeningSurvey									screeningSurvey;

	private final Table												slidesTable;
	private final Table												feedbacksTable;

	private UIScreeningSurveySlide									selectedUIScreeningSurveySlide	= null;
	private UIFeedback												selectedUIFeedback				= null;

	private final BeanContainer<ObjectId, UIScreeningSurveySlide>	slidesBeanContainer;
	private final BeanContainer<ObjectId, UIFeedback>				feedbacksBeanContainer;

	public ScreeningSurveyEditComponentWithController(
			final ScreeningSurvey screeningSurvey) {
		super();

		this.screeningSurvey = screeningSurvey;

		// table options
		slidesTable = getScreeningSurveySlidesTable();
		feedbacksTable = getFeedbacksTable();

		// table content
		val slidesOfScreeningSurvey = getScreeningSurveyAdministrationManagerService()
				.getAllScreeningSurveySlidesOfScreeningSurvey(
						screeningSurvey.getId());
		val feedbacksOfScreeningSurvey = getScreeningSurveyAdministrationManagerService()
				.getAllFeedbacksOfScreeningSurvey(screeningSurvey.getId());

		slidesBeanContainer = createBeanContainerForModelObjects(
				UIScreeningSurveySlide.class, slidesOfScreeningSurvey);

		slidesTable.setContainerDataSource(slidesBeanContainer);
		slidesTable.setSortContainerPropertyId(UIScreeningSurveySlide
				.getSortColumn());
		slidesTable.setVisibleColumns(UIScreeningSurveySlide
				.getVisibleColumns());
		slidesTable.setColumnHeaders(UIScreeningSurveySlide.getColumnHeaders());
		slidesTable.setSortAscending(true);
		slidesTable.setSortEnabled(false);

		feedbacksBeanContainer = createBeanContainerForModelObjects(
				UIFeedback.class, feedbacksOfScreeningSurvey);

		feedbacksTable.setContainerDataSource(feedbacksBeanContainer);
		feedbacksTable.setSortContainerPropertyId(UIFeedback.getSortColumn());
		feedbacksTable.setVisibleColumns(UIFeedback.getVisibleColumns());
		feedbacksTable.setColumnHeaders(UIFeedback.getColumnHeaders());
		feedbacksTable.setSortAscending(true);
		feedbacksTable.setSortEnabled(false);

		// handle table selection change
		slidesTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = slidesTable.getValue();
				if (objectId == null) {
					setSlideSelected(false);
					selectedUIScreeningSurveySlide = null;
				} else {
					selectedUIScreeningSurveySlide = getUIModelObjectFromTableByObjectId(
							slidesTable, UIScreeningSurveySlide.class, objectId);
					setSlideSelected(true);
				}
			}
		});

		feedbacksTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = feedbacksTable.getValue();
				if (objectId == null) {
					setFeedbackSelected(false);
					selectedUIFeedback = null;
				} else {
					selectedUIFeedback = getUIModelObjectFromTableByObjectId(
							feedbacksTable, UIFeedback.class, objectId);
					setFeedbackSelected(true);
				}
			}
		});

		// Handle combo box
		val templatePaths = getScreeningSurveyAdministrationManagerService()
				.getAllTemplatePaths();

		val templatePathComboBox = getTemplatePathComboBox();
		for (val templatePath : templatePaths) {
			templatePathComboBox.addItem(templatePath);
			if (screeningSurvey.getTemplatePath().equals(templatePath)) {
				templatePathComboBox.select(templatePath);
			}
		}
		templatePathComboBox.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final String templatePath = (String) event.getProperty()
						.getValue();

				log.debug("Adjust template path to {}", templatePath);
				getScreeningSurveyAdministrationManagerService()
						.screeningSurveyChangeTemplatePath(screeningSurvey,
								templatePath);

				adjust();

			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewButton().addClickListener(buttonClickListener);
		getEditButton().addClickListener(buttonClickListener);
		getMoveUpButton().addClickListener(buttonClickListener);
		getMoveDownButton().addClickListener(buttonClickListener);
		getDeleteButton().addClickListener(buttonClickListener);

		getNewFeedbackButton().addClickListener(buttonClickListener);
		getEditFeedbackButton().addClickListener(buttonClickListener);
		getRenameFeedbackButton().addClickListener(buttonClickListener);
		getDeleteFeedbackButton().addClickListener(buttonClickListener);

		getSwitchScreeningSurveyButton().addClickListener(buttonClickListener);

		getPasswordTextFieldComponent().getButton().addClickListener(
				buttonClickListener);

		adjustActiveOrInactive();

		adjust();
	}

	private void adjust() {
		// Adjust password text field
		getPasswordTextFieldComponent().setValue(screeningSurvey.getPassword());
	}

	private void adjustActiveOrInactive() {
		val switchScreeningSurveyButton = getSwitchScreeningSurveyButton();
		if (screeningSurvey.isActive()) {
			switchScreeningSurveyButton.setIcon(new ThemeResource(
					ThemeImageStrings.ACTIVE_ICON_SMALL));
			localize(
					switchScreeningSurveyButton,
					AdminMessageStrings.SCREENING_SURVEY_EDITING__SWITCH_BUTTON_ACTIVE);

			slidesTable.setEnabled(false);
			feedbacksTable.setEnabled(false);
			getActiveOrInactiveLayout().setEnabled(false);
		} else {
			switchScreeningSurveyButton.setIcon(new ThemeResource(
					ThemeImageStrings.INACTIVE_ICON_SMALL));
			localize(
					switchScreeningSurveyButton,
					AdminMessageStrings.SCREENING_SURVEY_EDITING__SWITCH_BUTTON_INACTIVE);

			getActiveOrInactiveLayout().setEnabled(true);
			slidesTable.setEnabled(true);
			feedbacksTable.setEnabled(true);
		}
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getNewButton()) {
				createSlide();
			} else if (event.getButton() == getEditButton()) {
				editSlide();
			} else if (event.getButton() == getMoveUpButton()) {
				moveSlide(true);
			} else if (event.getButton() == getMoveDownButton()) {
				moveSlide(false);
			} else if (event.getButton() == getDeleteButton()) {
				deleteSlide();
			} else if (event.getButton() == getNewFeedbackButton()) {
				createFeedback();
			} else if (event.getButton() == getRenameFeedbackButton()) {
				renameFeedback();
			} else if (event.getButton() == getEditFeedbackButton()) {
				editFeedback();
			} else if (event.getButton() == getDeleteFeedbackButton()) {
				deleteFeedback();
			} else if (event.getButton() == getSwitchScreeningSurveyButton()) {
				switchActiveOrInactive();
			} else if (event.getButton() == getPasswordTextFieldComponent()
					.getButton()) {
				editPassword();
			}
		}
	}

	public void editPassword() {
		log.debug("Edit password");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_PASSWORD,
				screeningSurvey.getPassword(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change password
							getScreeningSurveyAdministrationManagerService()
									.screeningSurveyChangePassword(
											screeningSurvey, getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}
				}, null);
	}

	public void createSlide() {
		log.debug("Create slide");
		val newScreeningSurveySlide = getScreeningSurveyAdministrationManagerService()
				.screeningSurveySlideCreate(screeningSurvey.getId());

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_SCREENING_SURVEY_SLIDE,
				new ScreeningSurveySlideEditComponentWithController(
						newScreeningSurveySlide),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						slidesBeanContainer.addItem(newScreeningSurveySlide
								.getId(),
								UIScreeningSurveySlide.class
										.cast(newScreeningSurveySlide
												.toUIModelObject()));
						slidesTable.select(newScreeningSurveySlide.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_CREATED);

						closeWindow();
					}
				}, screeningSurvey.getName());
	}

	public void switchActiveOrInactive() {
		log.debug("Switch screening survey");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					getScreeningSurveyAdministrationManagerService()
							.screeningSurveySetActive(screeningSurvey,
									!screeningSurvey.isActive());
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				adjustActiveOrInactive();
				closeWindow();
			}
		}, null);
	}

	public void editSlide() {
		log.debug("Edit slide");
		val selectedScreeningSurveySlide = selectedUIScreeningSurveySlide
				.getRelatedModelObject(ScreeningSurveySlide.class);

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_SCREENING_SURVEY_SLIDE,
				new ScreeningSurveySlideEditComponentWithController(
						selectedScreeningSurveySlide),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						removeAndAddModelObjectToBeanContainer(
								slidesBeanContainer,
								selectedScreeningSurveySlide);
						slidesTable.sort();
						slidesTable.select(selectedScreeningSurveySlide.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_UPDATED);

						closeWindow();
					}
				}, screeningSurvey.getName());
	}

	public void moveSlide(final boolean moveUp) {
		log.debug("Move slide {}", moveUp ? "up" : "down");

		val selectedScreeningSurveySlide = selectedUIScreeningSurveySlide
				.getRelatedModelObject(ScreeningSurveySlide.class);
		val swappedScreeningSurveySlide = getScreeningSurveyAdministrationManagerService()
				.screeningSurveySlideMove(selectedScreeningSurveySlide, moveUp);

		if (swappedScreeningSurveySlide == null) {
			log.debug("Slide is already at top/end of list");
			return;
		}

		removeAndAddModelObjectToBeanContainer(slidesBeanContainer,
				swappedScreeningSurveySlide);
		removeAndAddModelObjectToBeanContainer(slidesBeanContainer,
				selectedScreeningSurveySlide);
		slidesTable.sort();
		slidesTable.select(selectedScreeningSurveySlide.getId());
	}

	public void deleteSlide() {
		log.debug("Delete slide");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedScreeningSurveySlide = selectedUIScreeningSurveySlide.getRelatedModelObject(ScreeningSurveySlide.class);

					// Delete variable
					getScreeningSurveyAdministrationManagerService()
							.screeningSurveySlideDelete(
									selectedScreeningSurveySlide);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				slidesTable.removeItem(selectedUIScreeningSurveySlide
						.getRelatedModelObject(ScreeningSurveySlide.class)
						.getId());
				getAdminUI()
						.showInformationNotification(
								AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_DELETED);

				closeWindow();
			}
		}, null);
	}

	public void createFeedback() {
		log.debug("Create feedback");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_FEEDBACK,
				null, null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						final Feedback newFeedback;
						try {
							val newFeedbackName = getStringValue();

							// Create feedback
							newFeedback = getScreeningSurveyAdministrationManagerService()
									.feedbackCreate(newFeedbackName,
											screeningSurvey.getId());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						feedbacksBeanContainer.addItem(newFeedback.getId(),
								UIFeedback.class.cast(newFeedback
										.toUIModelObject()));
						feedbacksTable.sort();
						feedbacksTable.select(newFeedback.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void renameFeedback() {
		log.debug("Rename feedback");

		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_FEEDBACK,
				selectedUIFeedback.getRelatedModelObject(Feedback.class)
						.getName(), null, new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						BeanItem<UIFeedback> beanItem;
						try {
							val selectedFeedback = selectedUIFeedback
									.getRelatedModelObject(Feedback.class);

							beanItem = getBeanItemFromTableByObjectId(
									feedbacksTable, UIFeedback.class,
									selectedFeedback.getId());

							// Change name
							getScreeningSurveyAdministrationManagerService()
									.feedbackChangeName(selectedFeedback,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						getStringItemProperty(beanItem,
								UIFeedback.FEEDBACK_NAME).setValue(
								selectedUIFeedback.getRelatedModelObject(
										Feedback.class).getName());
						feedbacksTable.sort();

						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__FEEDBACK_RENAMED);
						closeWindow();
					}
				}, null);
	}

	public void editFeedback() {
		log.debug("Edit feedback");
		val selectedFeedback = selectedUIFeedback
				.getRelatedModelObject(Feedback.class);

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_FEEDBACK,
				new FeedbackEditComponentWithController(selectedFeedback),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						removeAndAddModelObjectToBeanContainer(
								feedbacksBeanContainer, selectedFeedback);
						feedbacksTable.sort();
						feedbacksTable.select(selectedFeedback.getId());
						getAdminUI()
								.showInformationNotification(
										AdminMessageStrings.NOTIFICATION__FEEDBACK_UPDATED);

						closeWindow();
					}
				}, selectedFeedback.getName(), screeningSurvey.getName());
	}

	public void deleteFeedback() {
		log.debug("Delete feedback");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedFeedback = selectedUIFeedback.getRelatedModelObject(Feedback.class);

					// Delete variable
					getScreeningSurveyAdministrationManagerService()
							.feedbackDelete(selectedFeedback);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				feedbacksTable.removeItem(selectedUIFeedback
						.getRelatedModelObject(Feedback.class).getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__FEEDBACK_DELETED);

				closeWindow();
			}
		}, null);
	}

}
