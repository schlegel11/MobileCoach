package org.isgf.mhc.services;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.server.ScreeningSurvey;

@Log4j2
public class ScreeningSurveyAdministrationManagerService {
	private static ScreeningSurveyAdministrationManagerService	instance	= null;

	private ScreeningSurveyAdministrationManagerService() throws Exception {
		log.info("Starting service...");

		log.info("Started.");
	}

	public static ScreeningSurveyAdministrationManagerService start()
			throws Exception {
		if (instance == null) {
			instance = new ScreeningSurveyAdministrationManagerService();
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	public Iterable<ScreeningSurvey> getActiveScreeningSurveys() {
		return ModelObject.find(ScreeningSurvey.class,
				Queries.SCREENING_SURVEYS_OPEN);
	}
}
