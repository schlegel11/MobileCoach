package org.isgf.mhc.ui.views.components.welcome;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.model.server.ScreeningSurvey;
import org.isgf.mhc.ui.views.components.screening_survey.ScreeningSurveyEditComponentWithController;

import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the welcome tab component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class WelcomeTabComponentWithController extends WelcomeTabComponent {
	public WelcomeTabComponentWithController() {
		super();

		// TODO DEBUGGING
		final ScreeningSurvey screeningSurvey = getScreeningSurveyAdministrationManagerService()
				.getAllScreeningSurveysOfIntervention(
						new ObjectId("5336bc5dc2e6abfde2d01264")).iterator()
				.next();

		// showModalModelObjectEditWindow(
		// AdminMessageStrings.ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__EDIT_SCREENING_SURVEY_SLIDE,
		// new ScreeningSurveySlideEditComponentWithController(
		// screeningSurveySlide),
		// new ExtendableButtonClickListener() {
		// @Override
		// public void buttonClick(final ClickEvent event) {
		//
		// closeWindow();
		// }
		// }, "TEST");
		showModalModelObjectEditWindow(
				AdminMessageStrings.ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__EDIT_SCREENING_SURVEY,
				new ScreeningSurveyEditComponentWithController(screeningSurvey),
				new ExtendableButtonClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {

						closeWindow();
					}
				}, "TEST");
	}
}
