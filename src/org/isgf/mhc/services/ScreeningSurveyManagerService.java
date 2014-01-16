package org.isgf.mhc.services;

import lombok.extern.log4j.Log4j2;

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
}
