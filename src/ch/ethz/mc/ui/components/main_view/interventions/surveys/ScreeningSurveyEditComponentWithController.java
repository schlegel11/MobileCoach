package ch.ethz.mc.ui.components.main_view.interventions.surveys;

/* ##LICENSE## */
import java.io.File;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.ThemeImageStrings;
import ch.ethz.mc.model.persistent.Feedback;
import ch.ethz.mc.model.persistent.IntermediateSurveyAndFeedbackParticipantShortURL;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.ui.UIFeedback;
import ch.ethz.mc.model.ui.UIScreeningSurveySlide;
import ch.ethz.mc.ui.components.basics.LocalizedShortStringEditComponent;
import ch.ethz.mc.ui.components.basics.ShortStringEditComponent;
import ch.ethz.mc.ui.components.main_view.interventions.surveys.feedbacks.FeedbackEditComponentWithController;

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
public class ScreeningSurveyEditComponentWithController
		extends ScreeningSurveyEditComponent {

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
		val slidesOfScreeningSurvey = getSurveyAdministrationManagerService()
				.getAllScreeningSurveySlidesOfScreeningSurvey(
						screeningSurvey.getId());
		val feedbacksOfScreeningSurvey = getSurveyAdministrationManagerService()
				.getAllFeedbacksOfScreeningSurvey(screeningSurvey.getId());

		slidesBeanContainer = createBeanContainerForModelObjects(
				UIScreeningSurveySlide.class, slidesOfScreeningSurvey);

		slidesTable.setContainerDataSource(slidesBeanContainer);
		slidesTable.setSortContainerPropertyId(
				UIScreeningSurveySlide.getSortColumn());
		slidesTable
				.setVisibleColumns(UIScreeningSurveySlide.getVisibleColumns());
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

		// handle survey type adjustments (hide feedback area for intermediate
		// surveys)
		if (screeningSurvey.isIntermediateSurvey()) {
			getFeedbacksLabel().setVisible(false);
			getFedbacksButtonLayout().setVisible(false);
			feedbacksTable.setVisible(false);
		}

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
							slidesTable, UIScreeningSurveySlide.class,
							objectId);
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
		val templatePaths = getSurveyAdministrationManagerService()
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
				getSurveyAdministrationManagerService()
						.screeningSurveyChangeTemplatePath(screeningSurvey,
								templatePath);

				adjust();

			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getNewButton().addClickListener(buttonClickListener);
		getEditButton().addClickListener(buttonClickListener);
		getDuplicateButton().addClickListener(buttonClickListener);
		getMoveUpButton().addClickListener(buttonClickListener);
		getMoveDownButton().addClickListener(buttonClickListener);
		getDeleteButton().addClickListener(buttonClickListener);

		getNewFeedbackButton().addClickListener(buttonClickListener);
		getEditFeedbackButton().addClickListener(buttonClickListener);
		getRenameFeedbackButton().addClickListener(buttonClickListener);
		getShowFeedbackButton().addClickListener(buttonClickListener);
		getDeleteFeedbackButton().addClickListener(buttonClickListener);

		getSwitchScreeningSurveyButton().addClickListener(buttonClickListener);

		getPasswordTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);

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
			switchScreeningSurveyButton.setIcon(
					new ThemeResource(ThemeImageStrings.ACTIVE_ICON_SMALL));
			localize(switchScreeningSurveyButton,
					AdminMessageStrings.SCREENING_SURVEY_EDITING__SWITCH_BUTTON_ACTIVE);

			slidesTable.setEnabled(false);
			feedbacksTable.setEnabled(false);
			getActiveOrInactiveLayout().setEnabled(false);
		} else {
			switchScreeningSurveyButton.setIcon(
					new ThemeResource(ThemeImageStrings.INACTIVE_ICON_SMALL));
			localize(switchScreeningSurveyButton,
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
			} else if (event.getButton() == getDuplicateButton()) {
				duplicateSlide();
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
			} else if (event.getButton() == getShowFeedbackButton()) {
				showFeedback();
			} else if (event.getButton() == getDeleteFeedbackButton()) {
				deleteFeedback();
			} else if (event.getButton() == getSwitchScreeningSurveyButton()) {
				switchActiveOrInactive();
			} else if (event.getButton() == getPasswordTextFieldComponent()
					.getButton()) {
				editPassword();
			}
			event.getButton().setEnabled(true);
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
							getSurveyAdministrationManagerService()
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
		val newScreeningSurveySlide = getSurveyAdministrationManagerService()
				.screeningSurveySlideCreate(screeningSurvey.getId());

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_SCREENING_SURVEY_SLIDE,
				new ScreeningSurveySlideEditComponentWithController(
						screeningSurvey.getIntervention(),
						newScreeningSurveySlide),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						slidesBeanContainer.addItem(
								newScreeningSurveySlide.getId(),
								UIScreeningSurveySlide.class
										.cast(newScreeningSurveySlide
												.toUIModelObject()));
						slidesTable.select(newScreeningSurveySlide.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_CREATED);

						closeWindow();
					}
				}, screeningSurvey.getName());
	}

	public void switchActiveOrInactive() {
		log.debug("Switch screening survey");

		try {
			// Change type
			getSurveyAdministrationManagerService().screeningSurveySetActive(
					screeningSurvey, !screeningSurvey.isActive());
		} catch (final Exception e) {
			handleException(e);
			return;
		}

		// Adapt UI
		adjustActiveOrInactive();

		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_STATUS_CHANGED);
	}

	public void editSlide() {
		log.debug("Edit slide");
		val selectedScreeningSurveySlide = selectedUIScreeningSurveySlide
				.getRelatedModelObject(ScreeningSurveySlide.class);

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_SCREENING_SURVEY_SLIDE,
				new ScreeningSurveySlideEditComponentWithController(
						screeningSurvey.getIntervention(),
						selectedScreeningSurveySlide),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						removeAndAddModelObjectToBeanContainer(
								slidesBeanContainer,
								selectedScreeningSurveySlide);
						slidesTable.sort();
						slidesTable
								.select(selectedScreeningSurveySlide.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_UPDATED);

						closeWindow();
					}
				}, screeningSurvey.getName());
	}

	public void duplicateSlide() {
		log.debug("Duplicate slide");

		final File temporaryBackupFile = getSurveyAdministrationManagerService()
				.screeningSurveySlideExport(selectedUIScreeningSurveySlide
						.getRelatedModelObject(ScreeningSurveySlide.class));

		try {
			final ScreeningSurveySlide importedScreeningSurveySlide = getSurveyAdministrationManagerService()
					.screeningSurveySlideImport(temporaryBackupFile, true);

			if (importedScreeningSurveySlide == null) {
				throw new Exception("Imported slide not found in import");
			}

			// Adapt UI
			slidesBeanContainer.addItem(importedScreeningSurveySlide.getId(),
					UIScreeningSurveySlide.class.cast(
							importedScreeningSurveySlide.toUIModelObject()));
			slidesTable.select(importedScreeningSurveySlide.getId());

			getAdminUI().showInformationNotification(
					AdminMessageStrings.NOTIFICATION__SLIDE_DUPLICATED);
		} catch (final Exception e) {
			getAdminUI().showWarningNotification(
					AdminMessageStrings.NOTIFICATION__SLIDE_DUPLICATION_FAILED);
		}

		try {
			temporaryBackupFile.delete();
		} catch (final Exception f) {
			// Do nothing
		}
	}

	public void moveSlide(final boolean moveUp) {
		log.debug("Move slide {}", moveUp ? "up" : "down");

		val selectedScreeningSurveySlide = selectedUIScreeningSurveySlide
				.getRelatedModelObject(ScreeningSurveySlide.class);
		val swappedScreeningSurveySlide = getSurveyAdministrationManagerService()
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
					val selectedScreeningSurveySlide = selectedUIScreeningSurveySlide
							.getRelatedModelObject(ScreeningSurveySlide.class);

					// Delete variable
					getSurveyAdministrationManagerService()
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
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_DELETED);

				closeWindow();
			}
		}, null);
	}

	public void createFeedback() {
		log.debug("Create feedback");
		showModalLStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NAME_FOR_FEEDBACK,
				null, null, new LocalizedShortStringEditComponent(),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						final Feedback newFeedback;
						try {
							val newFeedbackName = getLStringValue();

							// Create feedback
							newFeedback = getSurveyAdministrationManagerService()
									.feedbackCreate(newFeedbackName,
											screeningSurvey.getId());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						feedbacksBeanContainer.addItem(newFeedback.getId(),
								UIFeedback.class
										.cast(newFeedback.toUIModelObject()));
						feedbacksTable.sort();
						feedbacksTable.select(newFeedback.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_CREATED);

						closeWindow();
					}
				}, null);
	}

	public void renameFeedback() {
		log.debug("Rename feedback");

		showModalLStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__ENTER_NEW_NAME_FOR_FEEDBACK,
				selectedUIFeedback.getRelatedModelObject(Feedback.class)
						.getName(),
				null, new LocalizedShortStringEditComponent(),
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
							getSurveyAdministrationManagerService()
									.feedbackChangeName(selectedFeedback,
											getLStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						// Adapt UI
						getStringItemProperty(beanItem,
								UIFeedback.FEEDBACK_NAME)
										.setValue(selectedUIFeedback
												.getRelatedModelObject(
														Feedback.class)
												.getName().toString());
						feedbacksTable.sort();

						getAdminUI().showInformationNotification(
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
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__FEEDBACK_UPDATED);

						closeWindow();
					}
				}, selectedFeedback.getName(), screeningSurvey.getName());
	}

	public void showFeedback() {
		log.debug("Show feedback");

		val selectedFeedback = selectedUIFeedback
				.getRelatedModelObject(Feedback.class);

		val participantId = getAdminUI().getUISession()
				.getCurrentBackendUserParticipantId();

		if (participantId == null) {
			getAdminUI().showWarningNotification(
					AdminMessageStrings.NOTIFICATION__SURVEY_PARTICIPATION_REQUIRED);
			return;
		}

		val participant = getInterventionAdministrationManagerService()
				.getParticipant(participantId);

		if (participant == null) {
			getAdminUI().showWarningNotification(
					AdminMessageStrings.NOTIFICATION__SURVEY_PARTICIPATION_REQUIRED);
			return;
		}

		IntermediateSurveyAndFeedbackParticipantShortURL shortURL = null;
		if (participant.getAssignedFeedback() == null || !participant
				.getAssignedFeedback().equals(selectedFeedback.getId())) {
			shortURL = getSurveyExecutionManagerService()
					.participantSetFeedback(participant,
							selectedFeedback.getId());
		}

		if (shortURL == null) {
			shortURL = getInterventionAdministrationManagerService()
					.feedbackParticipantShortURLEnsure(participantId,
							selectedFeedback.getId());
		}

		final String url = getAdminUI().getPage().getLocation().toString()
				.substring(0,
						getAdminUI().getPage().getLocation().toString()
								.lastIndexOf("/") + 1)
				+ ImplementationConstants.SHORT_ID_SCREEN_SURVEY_AND_FEEDBACK_SERVLET_PATH
				+ "/" + shortURL.calculateIdPartOfURL() + "/";

		getAdminUI().getPage().open(url, "_blank");
	}

	public void deleteFeedback() {
		log.debug("Delete feedback");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedFeedback = selectedUIFeedback
							.getRelatedModelObject(Feedback.class);

					// Delete variable
					getSurveyAdministrationManagerService()
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
