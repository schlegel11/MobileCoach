package org.isgf.mhc.services;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.server.ScreeningSurvey;
import org.isgf.mhc.model.web.types.ScreeningSurveySlideTemplateLayoutTypes;

@Log4j2
public class ScreeningSurveyManagerService {
	private static ScreeningSurveyManagerService	instance	= null;

	private ScreeningSurveyManagerService() throws Exception {
		log.info("Starting service...");

		log.info("Started.");
	}

	public static ScreeningSurveyManagerService start() throws Exception {
		if (instance == null) {
			instance = new ScreeningSurveyManagerService();
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	/**
	 * Returns the appropriate {@link HashMap} to fill the template or
	 * <code>null</code> if an unexpected error occurs
	 * 
	 * @param participantId
	 * @param screeningSurveyId
	 * @param resultValue
	 * @param session
	 * @return
	 */
	public HashMap<String, Object> getAppropriateSlide(
			final ObjectId participantId, final ObjectId screeningSurveyId,
			final String resultValue, final HttpSession session) {
		// Check if

		return this.setLayoutTo(null,
				ScreeningSurveySlideTemplateLayoutTypes.ERROR);

		// TODO a lot (not forget to set session values)

		// return null;
	}

	/**
	 * Sets the given layout to <code>true</code> and all other available
	 * layouts to <code>false</code>
	 * 
	 * @param templateVariables
	 *            The {@link HashMap} to extend with the layout information or
	 *            <code>null</code> if a new one shall be created
	 * @param slideTemplateLayout
	 *            The {@link ScreeningSurveySlideTemplateLayoutTypes} to set as
	 *            <code>true</code>
	 * @return
	 */
	public HashMap<String, Object> setLayoutTo(
			HashMap<String, Object> templateVariables,
			final ScreeningSurveySlideTemplateLayoutTypes slideTemplateLayout) {
		if (templateVariables == null) {
			templateVariables = new HashMap<String, Object>();
		}

		for (final val availableSlideLayoutType : ScreeningSurveySlideTemplateLayoutTypes
				.values()) {
			if (slideTemplateLayout == availableSlideLayoutType) {
				templateVariables.put(slideTemplateLayout.toVariable(), true);
			} else {
				templateVariables.put(slideTemplateLayout.toVariable(), true);
			}
		}

		return templateVariables;
	}

	/**
	 * Returns all active screening surveys or <code>null</code> if non has been
	 * found
	 * 
	 * @return
	 */
	public Iterable<ScreeningSurvey> getActiveScreeningSurveys() {
		return ModelObject.find(ScreeningSurvey.class,
				Queries.SCREENING_SURVEYS_OPEN);
	}
}
