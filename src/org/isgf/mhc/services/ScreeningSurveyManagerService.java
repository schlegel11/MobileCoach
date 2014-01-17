package org.isgf.mhc.services;

import java.util.HashMap;

import javax.servlet.http.HttpSession;

import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

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

	public HashMap<String, Object> getAppropriateSlide(
			final ObjectId participantId, final ObjectId screeningSurveyId,
			final String resultValue, final HttpSession session) {

		// TODO a lot (not forget to set session values)

		return null;
	}
}
