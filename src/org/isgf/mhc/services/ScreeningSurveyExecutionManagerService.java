package org.isgf.mhc.services;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.model.server.ScreeningSurvey;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.VariablesManagerService;
import org.isgf.mhc.services.types.ScreeningSurveySlideTemplateFieldTypes;
import org.isgf.mhc.services.types.ScreeningSurveySlideTemplateLayoutTypes;

@Log4j2
public class ScreeningSurveyExecutionManagerService {
	private static ScreeningSurveyExecutionManagerService	instance	= null;

	private final DatabaseManagerService					databaseManagerService;
	private final FileStorageManagerService					fileStorageManagerService;
	private final VariablesManagerService					variablesManagerService;

	private ScreeningSurveyExecutionManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.variablesManagerService = variablesManagerService;

		log.info("Started.");
	}

	public static ScreeningSurveyExecutionManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		if (instance == null) {
			instance = new ScreeningSurveyExecutionManagerService(
					databaseManagerService, fileStorageManagerService,
					variablesManagerService);
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

		// TODO a lot (not forget to set session values)
		// handle also "empty" template path (= "")

		val templateVariables = new HashMap<String, Object>();
		setLayoutTo(templateVariables,
				ScreeningSurveySlideTemplateLayoutTypes.SELECT_ONE);

		templateVariables.put(
				ScreeningSurveySlideTemplateFieldTypes.TEMPLATE_FOLDER
						.toVariable(), "basic-template");

		return templateVariables;
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

	/*
	 * Getter methods
	 */

	/**
	 * Get a specific {@link ScreeningSurvey} by {@link ObjectId}
	 * 
	 * @param screeningSurveyId
	 * @return
	 */
	public ScreeningSurvey getScreeningSurveyById(
			final ObjectId screeningSurveyId) {
		return databaseManagerService.getModelObjectById(ScreeningSurvey.class,
				screeningSurveyId);
	}

	/**
	 * Returns all active {@link ScreeningSurvey}s or <code>null</code> if non
	 * has been
	 * found
	 * 
	 * @return
	 */
	public Iterable<ScreeningSurvey> getActiveScreeningSurveys() {
		final Iterable<Intervention> activeInterventions = databaseManagerService
				.findModelObjects(Intervention.class,
						Queries.INTERVENTION__ACTIVE_TRUE);

		val activeScreeningSurveys = new ArrayList<ScreeningSurvey>();

		for (val intervention : activeInterventions) {
			CollectionUtils
					.addAll(activeScreeningSurveys,
							databaseManagerService
									.findModelObjects(
											ScreeningSurvey.class,
											Queries.SCREENING_SURVEY__BY_INTERVENTION_AND_ACTIVE_TRUE,
											intervention.getId()).iterator());
		}

		return activeScreeningSurveys;
	}

	/**
	 * Returns the path containing the templates
	 * 
	 * @return
	 */
	public File getTemplatePath() {
		return fileStorageManagerService.getTemplatesFolder();
	}

	/**
	 * Returns if the requested {@link ScreeningSurvey} is currently accessible
	 * (means the the {@link Intervention} and {@link ScreeningSurvey} are both
	 * active)
	 * 
	 * @param screeningSurveyId
	 * @return
	 */
	public boolean screeningSurveyCheckIfActive(final ObjectId screeningSurveyId) {
		final ScreeningSurvey screeningSurvey = databaseManagerService
				.getModelObjectById(ScreeningSurvey.class, screeningSurveyId);

		if (screeningSurvey == null || !screeningSurvey.isActive()) {
			return false;
		}

		final Intervention intervention = databaseManagerService
				.getModelObjectById(Intervention.class,
						screeningSurvey.getIntervention());

		if (intervention == null || !intervention.isActive()) {
			return false;
		}

		return true;
	}
}
