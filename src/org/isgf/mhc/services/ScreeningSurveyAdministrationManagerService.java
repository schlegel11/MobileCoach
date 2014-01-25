package org.isgf.mhc.services;

import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.server.ScreeningSurvey;

@Log4j2
public class ScreeningSurveyAdministrationManagerService {
	private static ScreeningSurveyAdministrationManagerService	instance	= null;

	private final DatabaseManagerService						databaseManagerService;
	private final FileStorageManagerService						fileStorageManagerService;
	private final ModelObjectExchangeService					modelObjectExchangeService;

	private ScreeningSurveyAdministrationManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final ModelObjectExchangeService modelObjectExchangeService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.modelObjectExchangeService = modelObjectExchangeService;

		log.info("Started.");
	}

	public static ScreeningSurveyAdministrationManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final ModelObjectExchangeService modelObjectExchangeService)
			throws Exception {
		if (instance == null) {
			instance = new ScreeningSurveyAdministrationManagerService(
					databaseManagerService, fileStorageManagerService,
					modelObjectExchangeService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	/*
	 * Class methods
	 */

	public Iterable<ScreeningSurvey> getActiveScreeningSurveys() {
		return this.databaseManagerService.findModelObjects(ScreeningSurvey.class,
				Queries.SCREENING_SURVEYS_OPEN);
	}

	public ScreeningSurvey getScreeningSurvey(final ObjectId screeningSurveyId) {
		return this.databaseManagerService.getModelObjectById(
				ScreeningSurvey.class, screeningSurveyId);
	}
}
