package org.isgf.mhc.services;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.web.types.ScreeningSurveySlideTemplateFields;
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

		return this.createErrorMessage();

		// TODO a lot (not forget to set session values)

		// return null;
	}

	private HashMap<String, Object> createErrorMessage() {
		val templateVariables = new HashMap<String, Object>();

		templateVariables.put(
				ScreeningSurveySlideTemplateFields.SLIDE_LAYOUT.toVariable(),
				ScreeningSurveySlideTemplateLayoutTypes.ERROR.toVariable());

		return templateVariables;
	}
}
