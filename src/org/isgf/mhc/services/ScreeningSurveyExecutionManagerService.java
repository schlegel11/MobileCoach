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
import org.isgf.mhc.model.persistent.Feedback;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.Participant;
import org.isgf.mhc.model.persistent.ScreeningSurvey;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.VariablesManagerService;
import org.isgf.mhc.services.types.ScreeningSurveySlideTemplateFieldTypes;
import org.isgf.mhc.services.types.ScreeningSurveySlideTemplateLayoutTypes;
import org.isgf.mhc.services.types.SessionAttributeTypes;

/**
 * Cares for the orchestration of {@link ScreeningSurveySlides} as
 * part of a {@link ScreeningSurvey}
 * 
 * The templates are based on the Mustache standard. Details can be found in the
 * {@link ScreeningSurveySlideTemplateFieldTypes} class
 * 
 * @author Andreas Filler
 */
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
	 * Modification methods
	 */
	private Participant participantCreate(final ScreeningSurvey screeningSurvey) {
		val participant = new Participant(screeningSurvey.getIntervention(),
				System.currentTimeMillis(), "",
				screeningSurvey.getGlobalUniqueId(), null, false, "", "");

		databaseManagerService.saveModelObject(participant);

		return participant;
	}

	/*
	 * Special methods
	 */
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
	 * Returns if the requested {@link ScreeningSurvey} is currently accessible
	 * (means the the {@link Intervention} and {@link ScreeningSurvey} are both
	 * active)
	 * 
	 * @param screeningSurveyId
	 * @return
	 */
	public boolean screeningSurveyCheckIfActive(final ObjectId screeningSurveyId) {
		val screeningSurvey = databaseManagerService.getModelObjectById(
				ScreeningSurvey.class, screeningSurveyId);

		if (screeningSurvey == null || !screeningSurvey.isActive()) {
			return false;
		}

		val intervention = databaseManagerService.getModelObjectById(
				Intervention.class, screeningSurvey.getIntervention());

		if (intervention == null || !intervention.isActive()) {
			return false;
		}

		return true;
	}

	/**
	 * Returns if the requested {@link Feedback} is currently accessible
	 * (means the the {@link Intervention} is active)
	 * 
	 * @param screeningSurveyId
	 * @return
	 */
	public boolean feedbackCheckIfActive(final ObjectId feedbackId) {
		val feedback = databaseManagerService.getModelObjectById(
				Feedback.class, feedbackId);

		if (feedback == null) {
			return false;
		}

		val screeningSurvey = databaseManagerService.getModelObjectById(
				ScreeningSurvey.class, feedback.getScreeningSurvey());

		if (screeningSurvey == null) {
			return false;
		}

		val intervention = databaseManagerService.getModelObjectById(
				Intervention.class, screeningSurvey.getIntervention());

		if (intervention == null || !intervention.isActive()) {
			return false;
		}

		return true;
	}

	/*
	 * Getter methods
	 */
	/**
	 * Returns the appropriate {@link HashMap} to fill the template or
	 * <code>null</code> if an unexpected error occurs
	 * 
	 * @param participantId
	 *            The {@link ObjectId} of the current participant or
	 *            <code>null</code> if not created
	 * @param accessGranted
	 * @param screeningSurveyId
	 *            The {@link ObjectId} of the requested {@link ScreeningSurvey}
	 * @param resultValue
	 * @param session
	 * @return
	 */
	public HashMap<String, Object> getAppropriateScreeningSurveySlide(
			ObjectId participantId, final boolean accessGranted,
			final ObjectId screeningSurveyId, final String resultValue,
			final HttpSession session) {

		val templateVariables = new HashMap<String, Object>();

		// Check if screening survey is active
		if (!screeningSurveyCheckIfActive(screeningSurveyId)) {
			setLayoutTo(templateVariables,
					ScreeningSurveySlideTemplateLayoutTypes.CLOSED);
			return templateVariables;
		}

		val screeningSurvey = getScreeningSurveyById(screeningSurveyId);

		// Set name
		templateVariables
				.put(ScreeningSurveySlideTemplateFieldTypes.SURVEY_NAME
						.toVariable(), screeningSurvey.getName());

		// Check if screening survey template is set
		if (screeningSurvey.getTemplatePath() == null
				|| screeningSurvey.getTemplatePath().equals("")) {
			return templateVariables;
		} else {
			templateVariables.put(
					ScreeningSurveySlideTemplateFieldTypes.TEMPLATE_FOLDER
							.toVariable(), screeningSurvey.getTemplatePath());
		}

		// Check if participant already has access (if required)
		if (screeningSurvey.getPassword() != null
				&& !screeningSurvey.getPassword().equals("") && !accessGranted) {
			// Login is required, check login information, if provided
			if (resultValue != null
					&& resultValue.equals(screeningSurvey.getPassword())) {
				// Remember that user authenticated
				session.setAttribute(
						SessionAttributeTypes.ACCESS_GRANTED.toString(), true);
			} else {
				// Redirect to password page
				setLayoutTo(templateVariables,
						ScreeningSurveySlideTemplateLayoutTypes.PASSWORD_INPUT);
				return templateVariables;
			}
		}

		// Create participant if she does not exist
		if (participantId == null) {
			val participant = participantCreate(screeningSurvey);
			participantId = participant.getId();

			session.setAttribute(
					SessionAttributeTypes.PARTICIPANT_ID.toString(),
					participantId);
		}

		// Get last slide
		val lastScreeningSurveySlideId = session
				.getAttribute(SessionAttributeTypes.LAST_SCREENING_SURVEY_SLIDE_ID
						.toString());

		// If there was a last slide, store result
		setLayoutTo(templateVariables,
				ScreeningSurveySlideTemplateLayoutTypes.SELECT_ONE);

		return templateVariables;
	}

	/**
	 * Returns the appropriate {@link HashMap} to fill the template or
	 * <code>null</code> if an unexpected error occurs
	 * 
	 * @param feedbackParticipantId
	 *            The {@link ObjectId} of the participant of which the feedback
	 *            should be shown
	 * @param session
	 * @return
	 */
	public HashMap<String, Object> getAppropriateFeedbackSlide(
			final ObjectId feedbackParticipantId, final HttpSession session) {
		// TODO AFTER screening survey slide handling is finished
		return null;
	}

	/*
	 * Getter
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
}
