package org.isgf.mhc.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.server.ScreeningSurvey;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.ModelObjectExchangeService;
import org.isgf.mhc.services.internal.VariablesManagerService;
import org.isgf.mhc.tools.GlobalUniqueIdGenerator;
import org.isgf.mhc.ui.NotificationMessageException;

@Log4j2
public class ScreeningSurveyAdministrationManagerService {
	private static final String									DEFAULT_OBJECT_NAME	= "---";

	private static ScreeningSurveyAdministrationManagerService	instance			= null;

	private final DatabaseManagerService						databaseManagerService;
	private final FileStorageManagerService						fileStorageManagerService;
	private final VariablesManagerService						variablesManagerService;
	private final ModelObjectExchangeService					modelObjectExchangeService;

	private ScreeningSurveyAdministrationManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final ModelObjectExchangeService modelObjectExchangeService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.variablesManagerService = variablesManagerService;
		this.modelObjectExchangeService = modelObjectExchangeService;

		log.info("Started.");
	}

	public static ScreeningSurveyAdministrationManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final ModelObjectExchangeService modelObjectExchangeService)
			throws Exception {
		if (instance == null) {
			instance = new ScreeningSurveyAdministrationManagerService(
					databaseManagerService, fileStorageManagerService,
					variablesManagerService, modelObjectExchangeService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	/*
	 * Modification methods
	 */
	// ScreeningSurvey
	public ScreeningSurvey screeningSurveyCreate(final String name,
			final ObjectId interventionId,
			final String globalUniqueInterventionId) {
		val screeningSurvey = new ScreeningSurvey(
				GlobalUniqueIdGenerator.createGlobalUniqueId(), interventionId,
				globalUniqueInterventionId, name, "", null, false);

		if (name.equals("")) {
			screeningSurvey.setName(DEFAULT_OBJECT_NAME);
		}

		databaseManagerService.saveModelObject(screeningSurvey);

		return screeningSurvey;
	}

	public void screeningSurveyChangeName(
			final ScreeningSurvey screeningSurvey, final String newName) {
		if (newName.equals("")) {
			screeningSurvey.setName(DEFAULT_OBJECT_NAME);
		} else {
			screeningSurvey.setName(newName);
		}

		databaseManagerService.saveModelObject(screeningSurvey);
	}

	public File screeningSurveyExport(final ScreeningSurvey screeningSurvey) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		modelObjectsToExport.add(screeningSurvey);
		// TODO add also other relevant model objects

		return modelObjectExchangeService
				.exportModelObjects(modelObjectsToExport);
	}

	public void screeningSurveyDelete(
			final ScreeningSurvey screeningSurveyToDelete)
			throws NotificationMessageException {

		databaseManagerService.deleteModelObject(screeningSurveyToDelete);
	}

	/*
	 * Getter methods
	 */
	public Iterable<ScreeningSurvey> getAllScreeningSurveysOfIntervention(
			final ObjectId objectId) {
		return databaseManagerService.findModelObjects(ScreeningSurvey.class,
				Queries.SCREENING_SURVEY__BY_INTERVENTION, objectId);
	}

}
