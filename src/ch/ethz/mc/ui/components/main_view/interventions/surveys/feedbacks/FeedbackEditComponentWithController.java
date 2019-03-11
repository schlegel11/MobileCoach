package ch.ethz.mc.ui.components.main_view.interventions.surveys.feedbacks;

/* ##LICENSE## */
import java.io.File;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.Feedback;
import ch.ethz.mc.model.persistent.FeedbackSlide;
import ch.ethz.mc.model.ui.UIFeedbackSlide;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;

/**
 * Provides the feedback edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class FeedbackEditComponentWithController extends FeedbackEditComponent {

	private final Feedback									feedback;

	private final Table										slidesTable;

	private UIFeedbackSlide									selectedUIFeedbackSlide	= null;

	private final BeanContainer<ObjectId, UIFeedbackSlide>	slidesBeanContainer;

	public FeedbackEditComponentWithController(final Feedback feedback) {
		super();

		this.feedback = feedback;

		// table options
		slidesTable = getFeedbackSlidesTable();

		// table content
		val slidesOfFeedback = getSurveyAdministrationManagerService()
				.getAllFeedbackSlidesOfFeedback(feedback.getId());

		slidesBeanContainer = createBeanContainerForModelObjects(
				UIFeedbackSlide.class, slidesOfFeedback);

		slidesTable.setContainerDataSource(slidesBeanContainer);
		slidesTable.setSortContainerPropertyId(UIFeedbackSlide.getSortColumn());
		slidesTable.setVisibleColumns(UIFeedbackSlide.getVisibleColumns());
		slidesTable.setColumnHeaders(UIFeedbackSlide.getColumnHeaders());
		slidesTable.setSortAscending(true);
		slidesTable.setSortEnabled(false);

		// handle table selection change
		slidesTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = slidesTable.getValue();
				if (objectId == null) {
					setSlideSelected(false);
					selectedUIFeedbackSlide = null;
				} else {
					selectedUIFeedbackSlide = getUIModelObjectFromTableByObjectId(
							slidesTable, UIFeedbackSlide.class, objectId);
					setSlideSelected(true);
				}
			}
		});

		// Handle combo box
		val templatePaths = getSurveyAdministrationManagerService()
				.getAllTemplatePaths();

		val templatePathComboBox = getTemplatePathComboBox();
		for (val templatePath : templatePaths) {
			templatePathComboBox.addItem(templatePath);
			if (feedback.getTemplatePath().equals(templatePath)) {
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
						.feedbackChangeTemplatePath(feedback, templatePath);
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
			}
		}
	}

	public void createSlide() {
		log.debug("Create slide");
		val newFeedbackSlide = getSurveyAdministrationManagerService()
				.feedbackSlideCreate(feedback.getId());

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__CREATE_FEEDBACK_SLIDE,
				new FeedbackSlideEditComponentWithController(newFeedbackSlide,
						feedback.getScreeningSurvey()),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						slidesBeanContainer.addItem(newFeedbackSlide.getId(),
								UIFeedbackSlide.class.cast(
										newFeedbackSlide.toUIModelObject()));
						slidesTable.select(newFeedbackSlide.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__FEEDBACK_SLIDE_CREATED);

						closeWindow();
					}
				}, feedback.getName());
	}

	public void editSlide() {
		log.debug("Edit slide");
		val selectedFeedbackSlide = selectedUIFeedbackSlide
				.getRelatedModelObject(FeedbackSlide.class);

		showModalClosableEditWindow(
				AdminMessageStrings.ABSTRACT_CLOSABLE_EDIT_WINDOW__EDIT_FEEDBACK_SLIDE,
				new FeedbackSlideEditComponentWithController(
						selectedFeedbackSlide, feedback.getScreeningSurvey()),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// Adapt UI
						removeAndAddModelObjectToBeanContainer(
								slidesBeanContainer, selectedFeedbackSlide);
						slidesTable.sort();
						slidesTable.select(selectedFeedbackSlide.getId());
						getAdminUI().showInformationNotification(
								AdminMessageStrings.NOTIFICATION__FEEDBACK_SLIDE_UPDATED);

						closeWindow();
					}
				}, feedback.getName());
	}

	public void duplicateSlide() {
		log.debug("Duplicate slide");

		final File temporaryBackupFile = getSurveyAdministrationManagerService()
				.feedbackSlideExport(selectedUIFeedbackSlide
						.getRelatedModelObject(FeedbackSlide.class));

		try {
			final FeedbackSlide importedFeedbackSlide = getSurveyAdministrationManagerService()
					.feedbackSlideImport(temporaryBackupFile, true);

			if (importedFeedbackSlide == null) {
				throw new Exception("Imported slide not found in import");
			}

			// Adapt UI
			slidesBeanContainer.addItem(importedFeedbackSlide.getId(),
					UIFeedbackSlide.class
							.cast(importedFeedbackSlide.toUIModelObject()));
			slidesTable.select(importedFeedbackSlide.getId());

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

		val selectedFeedbackSlide = selectedUIFeedbackSlide
				.getRelatedModelObject(FeedbackSlide.class);
		val swappedFeedbackSlide = getSurveyAdministrationManagerService()
				.feedbackSlideMove(selectedFeedbackSlide, moveUp);

		if (swappedFeedbackSlide == null) {
			log.debug("Slide is already at top/end of list");
			return;
		}

		removeAndAddModelObjectToBeanContainer(slidesBeanContainer,
				swappedFeedbackSlide);
		removeAndAddModelObjectToBeanContainer(slidesBeanContainer,
				selectedFeedbackSlide);
		slidesTable.sort();
		slidesTable.select(selectedFeedbackSlide.getId());
	}

	public void deleteSlide() {
		log.debug("Delete slide");
		showConfirmationWindow(new ExtendableButtonClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedFeedbackSlide = selectedUIFeedbackSlide
							.getRelatedModelObject(FeedbackSlide.class);

					// Delete variable
					getSurveyAdministrationManagerService()
							.feedbackSlideDelete(selectedFeedbackSlide);
				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				slidesTable.removeItem(selectedUIFeedbackSlide
						.getRelatedModelObject(FeedbackSlide.class).getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__FEEDBACK_SLIDE_DELETED);

				closeWindow();
			}
		}, null);
	}

}
