package org.isgf.mhc.services;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.persistent.DialogOption;
import org.isgf.mhc.model.persistent.DialogStatus;
import org.isgf.mhc.model.persistent.Feedback;
import org.isgf.mhc.model.persistent.Intervention;
import org.isgf.mhc.model.persistent.Participant;
import org.isgf.mhc.model.persistent.ScreeningSurvey;
import org.isgf.mhc.model.persistent.ScreeningSurveySlide;
import org.isgf.mhc.model.persistent.types.DialogOptionTypes;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.VariablesManagerService;
import org.isgf.mhc.services.types.ScreeningSurveySlideTemplateFieldTypes;
import org.isgf.mhc.services.types.ScreeningSurveySlideTemplateLayoutTypes;
import org.isgf.mhc.services.types.SessionAttributeTypes;
import org.isgf.mhc.services.types.SystemVariables;
import org.isgf.mhc.tools.StringHelpers;

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
	// Participant
	private Participant participantCreate(final ScreeningSurvey screeningSurvey) {
		val participant = new Participant(screeningSurvey.getIntervention(),
				System.currentTimeMillis(), "",
				screeningSurvey.getGlobalUniqueId(), null, false, "", "");

		databaseManagerService.saveModelObject(participant);

		dialogStatusCreate(participant.getId());

		return participant;
	}

	private void participantSetName(final Participant participant,
			final String participantName) {
		participant.setNickname(participantName);

		databaseManagerService.saveModelObject(participant);
	}

	private void participantSetDialogOption(final Participant participant,
			final DialogOptionTypes dialogOptionType,
			final String dialogOptionData) {
		DialogOption dialogOption = databaseManagerService.findOneModelObject(
				DialogOption.class,
				Queries.DIALOG_OPTION__BY_PARTICIPANT_AND_TYPE,
				participant.getId(), dialogOptionType);

		if (dialogOption == null) {
			dialogOption = new DialogOption(participant.getId(),
					dialogOptionType, dialogOptionData);
		}

		dialogOption.setData(dialogOptionData);

		databaseManagerService.saveModelObject(dialogOption);
	}

	// Dialog status
	private void dialogStatusCreate(final ObjectId participantId) {
		val dialogStatus = new DialogStatus(participantId,
				StringHelpers.createStringTimeStamp(), false, 0, false, 0);

		databaseManagerService.saveModelObject(dialogStatus);
	}

	private void dialogStatusSetScreeningSurveyFinished(
			final ObjectId participantId) {
		val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participantId);

		dialogStatus.setScreeningSurveyPerformed(true);
		dialogStatus.setScreeningSurveyPerformedTimestamp(System
				.currentTimeMillis());

		databaseManagerService.saveModelObject(dialogStatus);
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
		log.debug("Check if screening survey is active");
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
		log.debug("Check if template is set");
		if (screeningSurvey.getTemplatePath() == null
				|| screeningSurvey.getTemplatePath().equals("")) {
			return templateVariables;
		} else {
			templateVariables.put(
					ScreeningSurveySlideTemplateFieldTypes.TEMPLATE_FOLDER
							.toVariable(), screeningSurvey.getTemplatePath());
		}

		// Check if participant already has access (if required)
		log.debug("Check if participant has access to screening survey");
		if (screeningSurvey.getPassword() != null
				&& !screeningSurvey.getPassword().equals("") && !accessGranted) {
			// Login is required, check login information, if provided
			if (resultValue != null
					&& resultValue.equals(screeningSurvey.getPassword())) {
				// Remember that user authenticated
				session.setAttribute(
						SessionAttributeTypes.PARTICIPANT_ACCESS_GRANTED
								.toString(), true);
			} else {
				// Redirect to password page
				setLayoutTo(templateVariables,
						ScreeningSurveySlideTemplateLayoutTypes.PASSWORD_INPUT);
				return templateVariables;
			}
		}

		// Create participant if she does not exist
		Participant participant = null;
		if (participantId == null) {
			log.debug("Create participant");
			participant = participantCreate(screeningSurvey);
			participantId = participant.getId();

			session.setAttribute(
					SessionAttributeTypes.PARTICIPANT_ID.toString(),
					participantId);
			session.removeAttribute(SessionAttributeTypes.PARTICIPANT_FEEDBACK_URL
					.toString());
			session.removeAttribute(SessionAttributeTypes.PARTICIPANT_LAST_SCREENING_SURVEY_SLIDE_ID
					.toString());
		} else {
			log.debug("Participant exists");
			participant = databaseManagerService.getModelObjectById(
					Participant.class, participantId);

			val dialogStatus = databaseManagerService.findOneModelObject(
					DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
					participantId);

			// Redirect to done slide if user already completely performed the
			// screening survey
			if (dialogStatus != null
					&& dialogStatus.isScreeningSurveyPerformed()) {
				log.debug("User already participated");

				setLayoutTo(templateVariables,
						ScreeningSurveySlideTemplateLayoutTypes.DONE);
				return templateVariables;
			}
		}

		// Get last slide
		val lastScreeningSurveySlideId = session
				.getAttribute(SessionAttributeTypes.PARTICIPANT_LAST_SCREENING_SURVEY_SLIDE_ID
						.toString());
		ScreeningSurveySlide lastScreeningSurveySlide = null;

		if (lastScreeningSurveySlideId != null) {
			lastScreeningSurveySlide = databaseManagerService
					.getModelObjectById(ScreeningSurveySlide.class,
							(ObjectId) lastScreeningSurveySlideId);

			// If there was a last slide, store result if provided
			if (resultValue != null
					&& lastScreeningSurveySlide
							.getStoreValueToVariableWithName() != null
					&& !lastScreeningSurveySlide
							.getStoreValueToVariableWithName().equals("")) {
				val variableName = lastScreeningSurveySlide
						.getStoreValueToVariableWithName();
				log.debug(
						"Storing result of screening survey slide {} to variable {} as value {}",
						lastScreeningSurveySlideId, variableName, resultValue);

				if (variableName
						.equals(SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES.participantName
								.toVariableName())) {
					participantSetName(participant, resultValue);
				} else if (variableName
						.equals(SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES.participantDialogOptionSMSData
								.toVariableName())) {
					participantSetDialogOption(participant,
							DialogOptionTypes.SMS, resultValue);
				} else if (variableName
						.equals(SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES.participantDialogOptionEmailData
								.toVariableName())) {
					participantSetDialogOption(participant,
							DialogOptionTypes.EMAIL, resultValue);
				}
				try {
					variablesManagerService.storeVariableValueOfParticipant(
							participant, variableName, resultValue);
				} catch (final Exception e) {
					log.error("Error when storing variable: {}", e.getMessage());
				}
			}

		}

		// Determine next slide
		val nextSlide = getNextScreeningSurveySlide(screeningSurvey,
				participant, lastScreeningSurveySlide);

		if (nextSlide == null) {
			// Screening survey done
			log.debug("No next slide found");

			setLayoutTo(templateVariables,
					ScreeningSurveySlideTemplateLayoutTypes.DONE);
		} else {
			// Check if is last slide
			if (nextSlide.isLastSlide()) {
				dialogStatusSetScreeningSurveyFinished(participantId);

				// Set feedback URL to session if required
				if (nextSlide.getHandsOverToFeedback() != null) {
					session.setAttribute(
							SessionAttributeTypes.PARTICIPANT_FEEDBACK_URL
									.toString(),
							ImplementationContants.SCREENING_SURVEY_SERVLET_FEEDBACK_SUBPATH
									+ "/" + participantId);
				}
			}

			// Fill next screening survey slide
			log.debug("Filling next slide {} with contents",
					nextSlide.getTitleWithPlaceholders());

			// TODO
		}

		return templateVariables;
	}

	private ScreeningSurveySlide getNextScreeningSurveySlide(
			final ScreeningSurvey screeningSurvey, final Object participant,
			final Object lastScreeningSurveySlide) {
		// TODO Auto-generated method stub
		return null;
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
