package ch.ethz.mc.services;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpSession;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.Feedback;
import ch.ethz.mc.model.persistent.FeedbackSlide;
import ch.ethz.mc.model.persistent.FeedbackSlideRule;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MediaObject;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.ScreeningSurveySlideRule;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.services.types.FeedbackSessionAttributeTypes;
import ch.ethz.mc.services.types.FeedbackSlideTemplateFieldTypes;
import ch.ethz.mc.services.types.GeneralSlideTemplateFieldTypes;
import ch.ethz.mc.services.types.ScreeningSurveySessionAttributeTypes;
import ch.ethz.mc.services.types.ScreeningSurveySlideTemplateFieldTypes;
import ch.ethz.mc.services.types.ScreeningSurveySlideTemplateLayoutTypes;
import ch.ethz.mc.tools.GlobalUniqueIdGenerator;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.RuleEvaluator;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.tools.VariableStringReplacer;

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
	private final Object									$lock;

	private static ScreeningSurveyExecutionManagerService	instance	= null;

	private final DatabaseManagerService					databaseManagerService;
	private final FileStorageManagerService					fileStorageManagerService;
	private final VariablesManagerService					variablesManagerService;

	private ScreeningSurveyExecutionManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		$lock = MC.getInstance();

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
	@Synchronized
	private Participant participantCreate(final ScreeningSurvey screeningSurvey) {
		val participant = new Participant(screeningSurvey.getIntervention(),
				InternalDateTime.currentTimeMillis(), "",
				screeningSurvey.getId(), screeningSurvey.getGlobalUniqueId(),
				null, null, true, "", "");

		databaseManagerService.saveModelObject(participant);

		dialogStatusCreate(participant.getId());

		return participant;
	}

	@Synchronized
	private void participantSetFeedback(final Participant participant,
			final ObjectId feedbackId) {
		val feedback = databaseManagerService.getModelObjectById(
				Feedback.class, feedbackId);

		participant.setAssignedFeedback(feedbackId);
		participant.setAssignedFeedbackGlobalUniqueId(feedback
				.getGlobalUniqueId());

		databaseManagerService.saveModelObject(participant);
	}

	// Dialog status
	@Synchronized
	private void dialogStatusCreate(final ObjectId participantId) {
		final long currentTimestamp = InternalDateTime.currentTimeMillis();
		val dialogStatus = new DialogStatus(participantId, "", null, null,
				currentTimestamp, false, currentTimestamp, 0, false, 0, 0,
				false, 0);

		databaseManagerService.saveModelObject(dialogStatus);
	}

	@Synchronized
	private void dialogStatusSetScreeningSurveyFinished(
			final ObjectId participantId) {
		val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participantId);

		if (!dialogStatus.isScreeningSurveyPerformed()) {
			dialogStatus.setScreeningSurveyPerformed(true);
			dialogStatus.setScreeningSurveyPerformedTimestamp(InternalDateTime
					.currentTimeMillis());

			databaseManagerService.saveModelObject(dialogStatus);
		}
	}

	@Synchronized
	private void dialogStatusSetDataForMonitoringNotAvailable(
			final ObjectId participantId) {
		val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participantId);

		if (!dialogStatus.isScreeningSurveyPerformed()) {
			dialogStatus.setDataForMonitoringParticipationAvailable(false);

			databaseManagerService.saveModelObject(dialogStatus);
		}
	}

	@Synchronized
	private void dialogStatusUpdateAfterDeterminingNextSlide(
			final ObjectId participantId,
			final ScreeningSurveySlide formerSlide, final boolean adjustTime) {
		val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participantId);

		// Check if all data for monitoring is available
		if (!dialogStatus.isDataForMonitoringParticipationAvailable()) {
			val dataForMonitoringParticipationAvailable = checkForDataForMonitoringParticipation(participantId);

			if (dialogStatus.isDataForMonitoringParticipationAvailable() != dataForMonitoringParticipationAvailable) {
				dialogStatus
						.setDataForMonitoringParticipationAvailable(dataForMonitoringParticipationAvailable);

				databaseManagerService.saveModelObject(dialogStatus);
			}
		}

		// Remember former slide and timestamp
		if (formerSlide != null) {
			dialogStatus
					.setLastVisitedScreeningSurveySlide(formerSlide.getId());
			dialogStatus
					.setLastVisitedScreeningSurveySlideGlobalUniqueId(formerSlide
							.getGlobalUniqueId());
			if (adjustTime) {
				dialogStatus
						.setLastVisitedScreeningSurveySlideTimestamp(InternalDateTime
								.currentTimeMillis());
			}

			databaseManagerService.saveModelObject(dialogStatus);
		}
	}

	/*
	 * Special methods
	 */
	/**
	 * Checks variables of participant if all relevant information for
	 * monitoring is available
	 * 
	 * @param participantId
	 * @return
	 */
	@Synchronized
	private boolean checkForDataForMonitoringParticipation(
			final ObjectId participantId) {

		val dialogOptions = databaseManagerService.findModelObjects(
				DialogOption.class, Queries.DIALOG_OPTION__BY_PARTICIPANT,
				participantId);

		if (dialogOptions != null && dialogOptions.iterator().hasNext()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns if the requested {@link ScreeningSurvey} is currently accessible
	 * (means the the {@link Intervention} and {@link ScreeningSurvey} are both
	 * active)
	 * 
	 * @param screeningSurveyId
	 * @return
	 */
	@Synchronized
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
	 * (means the the {@link Intervention} is active) by checking the
	 * {@link Participant} first
	 * 
	 * @param screeningSurveyId
	 * @return
	 */
	@Synchronized
	public boolean feedbackCheckIfActiveByBelongingParticipant(
			final ObjectId participantId) {
		val participant = databaseManagerService.getModelObjectById(
				Participant.class, participantId);

		if (participant == null) {
			return false;
		}

		val feedback = databaseManagerService.getModelObjectById(
				Feedback.class, participant.getAssignedFeedback());

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
	 * @param resultValues
	 * @param session
	 * @return
	 */
	@Synchronized
	public HashMap<String, Object> getAppropriateScreeningSurveySlide(
			ObjectId participantId, final boolean accessGranted,
			final ObjectId screeningSurveyId, final List<String> resultValues,
			final String checkValue, final HttpSession session) {

		val templateVariables = new HashMap<String, Object>();

		// Check if screening survey is active
		log.debug("Check if screening survey is active");
		if (!screeningSurveyCheckIfActive(screeningSurveyId)) {
			templateVariables
					.put(ScreeningSurveySlideTemplateLayoutTypes.CLOSED
							.toVariable(), true);
			return templateVariables;
		}

		val screeningSurvey = getScreeningSurveyById(screeningSurveyId);

		// Set name
		templateVariables.put(GeneralSlideTemplateFieldTypes.NAME.toVariable(),
				screeningSurvey.getName());

		// Check if screening survey template is set
		log.debug("Check if template is set");
		if (screeningSurvey.getTemplatePath() == null
				|| screeningSurvey.getTemplatePath().equals("")) {
			return templateVariables;
		} else {
			templateVariables
					.put(GeneralSlideTemplateFieldTypes.TEMPLATE_FOLDER
							.toVariable(), screeningSurvey.getTemplatePath());
		}

		// Check if participant already has access (if required)
		log.debug("Check if participant has access to screening survey");
		if (screeningSurvey.getPassword() != null
				&& !screeningSurvey.getPassword().equals("") && !accessGranted) {
			// Login is required, check login information, if provided
			if (resultValues != null
					&& resultValues.equals(screeningSurvey.getPassword())) {
				// Remember that user authenticated
				session.setAttribute(
						ScreeningSurveySessionAttributeTypes.PARTICIPANT_ACCESS_GRANTED
								.toString(), true);
			} else {
				// Redirect to password page
				templateVariables.put(
						ScreeningSurveySlideTemplateLayoutTypes.PASSWORD_INPUT
								.toVariable(), true);
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
					ScreeningSurveySessionAttributeTypes.PARTICIPANT_ID
							.toString(), participantId);
			session.removeAttribute(ScreeningSurveySessionAttributeTypes.PARTICIPANT_FEEDBACK_URL
					.toString());
			session.removeAttribute(ScreeningSurveySessionAttributeTypes.PARTICIPANT_FORMER_SCREENING_SURVEY_SLIDE_ID
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

				templateVariables.put(
						ScreeningSurveySlideTemplateLayoutTypes.DONE
								.toVariable(), true);
				return templateVariables;
			}
		}

		// Get last visited slide
		val formerSlideId = session
				.getAttribute(ScreeningSurveySessionAttributeTypes.PARTICIPANT_FORMER_SCREENING_SURVEY_SLIDE_ID
						.toString());

		ScreeningSurveySlide formerSlide = null;
		if (formerSlideId != null) {
			formerSlide = databaseManagerService.getModelObjectById(
					ScreeningSurveySlide.class, (ObjectId) formerSlideId);
		}

		ScreeningSurveySlide nextSlide;

		if (formerSlideId != null
				&& !session
						.getAttribute(
								ScreeningSurveySessionAttributeTypes.SCREENING_SURVEY_CONSISTENCY_CHECK_VALUE
										.toString()).equals(checkValue)) {
			log.debug("Consistency check failed; show same page again");

			// Next slide is former slide
			nextSlide = formerSlide;
		} else {
			// If there was a former slide, store result if provided
			if (formerSlideId != null && resultValues != null) {
				for (int i = 0; i < resultValues.size(); i++) {
					// If question results should to be saved
					val questions = formerSlide.getQuestions();
					ScreeningSurveySlide.Question question = null;

					if (questions.size() > i
							&& (question = questions.get(i))
									.getStoreValueToVariableWithName() != null
							&& question.getStoreValueToVariableWithName()
									.equals("")) {

						val resultValue = resultValues.get(i);
						val variableName = question
								.getStoreValueToVariableWithName();

						// Store result to variable
						log.debug(
								"Storing result of screening survey slide {} to variable {} as value {}",
								formerSlideId, variableName, resultValue);
						try {
							variablesManagerService
									.writeVariableValueOfParticipant(
											participant.getId(), variableName,
											resultValue);
							participant = databaseManagerService
									.getModelObjectById(Participant.class,
											participant.getId());
						} catch (final Exception e) {
							log.warn(
									"The variable {} could not be written: {}",
									variableName, e.getMessage());
						}
					}
				}
			}

			// Determine next slide
			if (formerSlide != null && formerSlide.isLastSlide()) {
				nextSlide = null;
			} else {
				nextSlide = getNextScreeningSurveySlide(participant,
						screeningSurvey, formerSlide);
			}
		}

		// Adjust dialog status
		dialogStatusUpdateAfterDeterminingNextSlide(participantId, formerSlide,
				true);

		if (nextSlide == null) {
			// Screening survey done
			log.debug("No next slide found");

			dialogStatusSetScreeningSurveyFinished(participantId);

			return null;
		} else {
			// Check if it's the last slide
			if (nextSlide.isLastSlide()) {
				// Set feedback URL to participant and session if required
				if (nextSlide.getHandsOverToFeedback() != null) {
					log.debug("Setting feedback {} for participant {}",
							nextSlide.getHandsOverToFeedback(),
							participant.getId());
					participantSetFeedback(participant,
							nextSlide.getHandsOverToFeedback());
					session.setAttribute(
							ScreeningSurveySessionAttributeTypes.PARTICIPANT_FEEDBACK_URL
									.toString(), participantId);
				}

				dialogStatusSetScreeningSurveyFinished(participantId);
			}

			// Remember next slide as former slide
			session.setAttribute(
					ScreeningSurveySessionAttributeTypes.PARTICIPANT_FORMER_SCREENING_SURVEY_SLIDE_ID
							.toString(), nextSlide.getId());

			// Remember check variable
			val newCheckValue = GlobalUniqueIdGenerator.createGlobalUniqueId();
			session.setAttribute(
					ScreeningSurveySessionAttributeTypes.SCREENING_SURVEY_CONSISTENCY_CHECK_VALUE
							.toString(), newCheckValue);

			// Fill next screening survey slide
			log.debug("Filling next slide {} with contents",
					nextSlide.getTitleWithPlaceholders());

			// Layout
			switch (nextSlide.getQuestionType()) {
				case MULTILINE_TEXT_INPUT:
					templateVariables
							.put(ScreeningSurveySlideTemplateLayoutTypes.MULTILINE_TEXT_INPUT
									.toVariable(), true);
					break;
				case NUMBER_INPUT:
					templateVariables
							.put(ScreeningSurveySlideTemplateLayoutTypes.NUMBER_INPUT
									.toVariable(), true);
					break;
				case SELECT_MANY:
					templateVariables.put(
							ScreeningSurveySlideTemplateLayoutTypes.SELECT_MANY
									.toVariable(), true);
					break;
				case SELECT_ONE:
					templateVariables.put(
							ScreeningSurveySlideTemplateLayoutTypes.SELECT_ONE
									.toVariable(), true);
					break;
				case TEXT_INPUT:
					templateVariables.put(
							ScreeningSurveySlideTemplateLayoutTypes.TEXT_INPUT
									.toVariable(), true);
					break;
				case TEXT_ONLY:
					templateVariables.put(
							ScreeningSurveySlideTemplateLayoutTypes.TEXT_ONLY
									.toVariable(), true);
					break;
				case MEDIA_ONLY:
					templateVariables.put(
							ScreeningSurveySlideTemplateLayoutTypes.MEDIA_ONLY
									.toVariable(), true);
					break;
			}

			// Retrieve all required variables to generate slide
			final Hashtable<String, AbstractVariableWithValue> variablesWithValues = variablesManagerService
					.getAllVariablesWithValuesOfParticipantAndSystem(participant);

			// Check variable
			templateVariables
					.put(GeneralSlideTemplateFieldTypes.HIDDEN_CHECK_VARIABLE
							.toVariable(),
							ImplementationConstants.SCREENING_SURVEY_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE);
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.HIDDEN_CHECK_VARIABLE_VALUE
							.toVariable(), newCheckValue);

			// Optional layout attribute
			val optionalLayoutAttribute = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(nextSlide
							.getOptionalLayoutAttributeWithPlaceholders(),
							variablesWithValues.values(), "");
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.OPTIONAL_LAYOUT_ATTRIBUTE
							.toVariable(), optionalLayoutAttribute);
			// Title
			val title = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(
							nextSlide.getTitleWithPlaceholders(),
							variablesWithValues.values(), "");
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.TITLE.toVariable(), title);

			// Validation error message
			if (formerSlide != null && nextSlide != null
					&& formerSlide.getId().equals(nextSlide.getId())) {
				val validationErrorMessage = VariableStringReplacer
						.findVariablesAndReplaceWithTextValues(
								nextSlide.getValidationErrorMessage(),
								variablesWithValues.values(), "");
				templateVariables
						.put(ScreeningSurveySlideTemplateFieldTypes.VALIDATION_ERROR_MESSAGE
								.toVariable(), validationErrorMessage);
			}

			// Media object URL and type
			if (nextSlide.getLinkedMediaObject() != null) {
				val mediaObject = databaseManagerService.getModelObjectById(
						MediaObject.class, nextSlide.getLinkedMediaObject());

				templateVariables.put(
						GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_URL
								.toVariable(), mediaObject.getId() + "/"
								+ mediaObject.getName());

				switch (mediaObject.getType()) {
					case AUDIO:
						templateVariables
								.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_AUDIO
										.toVariable(), true);
						break;
					case HTML_TEXT:
						templateVariables
								.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_HTML_TEXT
										.toVariable(), true);
						break;
					case IMAGE:
						templateVariables
								.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_IMAGE
										.toVariable(), true);
						break;
					case VIDEO:
						templateVariables
								.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_VIDEO
										.toVariable(), true);
						break;
				}
			}

			// Questions and answers
			final List<HashMap<String, Object>> questionObjects = new ArrayList<HashMap<String, Object>>();

			val questions = nextSlide.getQuestions();
			for (int i = 0; i < questions.size(); i++) {
				val question = questions.get(i);
				val questionObject = new HashMap<String, Object>();

				// Question
				val questionText = VariableStringReplacer
						.findVariablesAndReplaceWithTextValues(
								question.getQuestionWithPlaceholders(),
								variablesWithValues.values(), "");
				questionObject.put(
						ScreeningSurveySlideTemplateFieldTypes.QUESTION_TEXT
								.toVariable(), questionText);
				questionObject
						.put(ScreeningSurveySlideTemplateFieldTypes.QUESTION_POSITION
								.toVariable(), i + 1);

				// Result variable
				questionObject
						.put(ScreeningSurveySlideTemplateFieldTypes.RESULT_VARIABLE
								.toVariable(),
								ImplementationConstants.SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLES
										+ i);

				// Answers (text, value, preselected)
				val answersWithPlaceholders = question
						.getAnswersWithPlaceholders();
				val answerValues = question.getAnswerValues();

				final List<HashMap<String, Object>> answersObjects = new ArrayList<HashMap<String, Object>>();
				for (int j = 0; j < answersWithPlaceholders.length; j++) {
					val answerObjects = new HashMap<String, Object>();

					val answerWithPlaceholder = answersWithPlaceholders[j];
					val finalAnswerText = VariableStringReplacer
							.findVariablesAndReplaceWithTextValues(
									answerWithPlaceholder,
									variablesWithValues.values(), "");

					val answerValue = answerValues[j];

					answerObjects
							.put(ScreeningSurveySlideTemplateFieldTypes.ANSWER_POSITION
									.toVariable(), j + 1);
					answerObjects.put(
							ScreeningSurveySlideTemplateFieldTypes.ANSWER_TEXT
									.toVariable(), finalAnswerText);
					answerObjects.put(
							ScreeningSurveySlideTemplateFieldTypes.ANSWER_VALUE
									.toVariable(), answerValue);
					if (j == 0) {
						answerObjects
								.put(ScreeningSurveySlideTemplateFieldTypes.IS_FIRST_ANSWER
										.toVariable(), true);
					}
					if (j == answersWithPlaceholders.length - 1) {
						answerObjects
								.put(ScreeningSurveySlideTemplateFieldTypes.IS_LAST_ANSWER
										.toVariable(), true);
					}
					if (question.getPreSelectedAnswer() == j) {
						answerObjects
								.put(ScreeningSurveySlideTemplateFieldTypes.PRESELECTED_ANSWER
										.toVariable(), true);
					}

					answersObjects.add(answerObjects);
				}

				questionObject.put(
						ScreeningSurveySlideTemplateFieldTypes.ANSWERS_COUNT
								.toVariable(), answerValues.length);

				if (answersObjects.size() > 0) {
					templateVariables.put(
							ScreeningSurveySlideTemplateFieldTypes.ANSWERS
									.toVariable(), answersObjects);
				}

				questionObjects.add(questionObject);
			}

			templateVariables.put(
					ScreeningSurveySlideTemplateFieldTypes.QUESTIONS
							.toVariable(), questionObjects);
			templateVariables.put(
					ScreeningSurveySlideTemplateFieldTypes.QUESTIONS_COUNT
							.toVariable(), questions.size());

			// Is last slide
			if (nextSlide.isLastSlide()) {
				templateVariables.put(
						ScreeningSurveySlideTemplateFieldTypes.IS_LAST_SLIDE
								.toVariable(), true);
			}
		}

		return templateVariables;
	}

	/**
	 * Tries to finish all unfinished {@link ScreeningSurvey}s of
	 * {@link Participant} with the following state:
	 * 
	 * - the belonging intervention is active
	 * - the participant has all data for monitoring available
	 * - the participant has not finished the screening survey
	 * - the participant has not finished the monitoring
	 * 
	 */
	@Synchronized
	public void finishUnfinishedScreeningSurveys() {
		for (val intervention : databaseManagerService.findModelObjects(
				Intervention.class, Queries.INTERVENTION__ACTIVE_TRUE)) {
			for (val participant : databaseManagerService.findModelObjects(
					Participant.class, Queries.PARTICIPANT__BY_INTERVENTION,
					intervention.getId())) {
				if (participant != null) {
					for (val dialogStatus : databaseManagerService
							.findModelObjects(
									DialogStatus.class,
									Queries.DIALOG_STATUS__BY_PARTICIPANT_AND_LAST_VISITED_SCREENING_SURVEY_SLIDE_TIMESTAMP_LOWER,
									participant.getId(),
									InternalDateTime.currentTimeMillis()
											- ImplementationConstants.HOURS_TO_TIME_IN_MILLIS_MULTIPLICATOR
											* 2)) {
						if (dialogStatus != null
								&& dialogStatus
										.isDataForMonitoringParticipationAvailable()
								&& !dialogStatus.isScreeningSurveyPerformed()
								&& !dialogStatus.isMonitoringPerformed()) {

							log.debug("Trying to finish the screening survey for a participant who did not finish the screening survey");

							try {
								finishScreeningSurveyForParticipant(participant);
								log.debug(
										"Screening survey finished for participant {}",
										participant.getId());
							} catch (final Exception e) {
								log.debug(
										"Could not finish the screening survey for a participant who did not finish it: {}",
										e.getMessage());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Finishes a {@link ScreeningSurvey} for an already existing
	 * {@link Participant}
	 * 
	 * @param participantId
	 *            The {@link ObjectId} of the participant
	 * @return
	 */
	@Synchronized
	private void finishScreeningSurveyForParticipant(Participant participant)
			throws NullPointerException {
		if (participant == null) {
			log.warn("Could not finish screening survey for participant, because participant does not exist");
			throw new NullPointerException();
		}

		log.debug("Finishing screening survey for participant {}",
				participant.getId());

		val screeningSurvey = getScreeningSurveyById(participant
				.getAssignedScreeningSurvey());

		if (screeningSurvey == null) {
			log.warn("Could not finish screening survey for participant, because it does not exist");
			throw new NullPointerException();
		}

		int i = 0;
		do {
			val dialogStatus = databaseManagerService.findOneModelObject(
					DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
					participant.getId());

			if (dialogStatus == null
					|| dialogStatus.isScreeningSurveyPerformed()) {
				log.warn("Could not finish screening survey for participant, because dialog status does not exist or is already performed");
				throw new NullPointerException();
			}

			// Get last visited slide
			val formerSlideId = dialogStatus
					.getLastVisitedScreeningSurveySlide();

			ScreeningSurveySlide formerSlide = null;
			if (formerSlideId != null) {
				formerSlide = databaseManagerService.getModelObjectById(
						ScreeningSurveySlide.class, formerSlideId);
			}

			if (formerSlide == null) {
				log.warn("Could not finish screening survey for participant, because last visited slide could not be found");
				throw new NullPointerException();
			}

			ScreeningSurveySlide nextSlide;

			// If there was a former slide, store default value if provided
			if (formerSlideId != null) {
				for (int j = 0; j < formerSlide.getQuestions().size(); j++) {
					val questions = formerSlide.getQuestions();
					val question = questions.get(j);

					if (question.getDefaultValue() != null
							&& !question.getDefaultValue().equals("")
							&& question.getStoreValueToVariableWithName() != null
							&& !question.getStoreValueToVariableWithName()
									.equals("")) {
						val variableName = question
								.getStoreValueToVariableWithName();

						// Store result to variable
						log.debug(
								"Storing result of screening survey slide {} to variable {} as value {}",
								formerSlideId, variableName,
								question.getDefaultValue());
						try {
							variablesManagerService
									.writeVariableValueOfParticipant(
											participant.getId(), variableName,
											question.getDefaultValue());
							participant = databaseManagerService
									.getModelObjectById(Participant.class,
											participant.getId());
						} catch (final Exception e) {
							log.warn(
									"The variable {} could not be written: {}",
									variableName, e.getMessage());
						}
					}
				}
			}

			// Determine next slide
			if (formerSlide != null && formerSlide.isLastSlide()) {
				nextSlide = null;
			} else {
				nextSlide = getNextScreeningSurveySlide(participant,
						screeningSurvey, formerSlide);
			}

			// Adjust dialog status
			dialogStatusUpdateAfterDeterminingNextSlide(participant.getId(),
					nextSlide, false);

			if (nextSlide == null) {
				// Screening survey done
				log.debug("No next slide found");

				dialogStatusSetScreeningSurveyFinished(participant.getId());

				return;
			} else if (nextSlide.isLastSlide()) {
				// Set feedback URL to participant and session if required
				if (nextSlide.getHandsOverToFeedback() != null) {
					log.debug("Setting feedback {} for participant {}",
							nextSlide.getHandsOverToFeedback(),
							participant.getId());
					participantSetFeedback(participant,
							nextSlide.getHandsOverToFeedback());
				}

				// Check if it's the last slide
				dialogStatusSetScreeningSurveyFinished(participant.getId());

				return;
			}

			i++;
		} while (i < ImplementationConstants.SCREENING_SURVEY_SLIDE_AUTOMATIC_EXECUTION_LOOP_DETECTION_THRESHOLD);

		// Set participant to data not available for monitoring
		dialogStatusSetDataForMonitoringNotAvailable(participant.getId());
		log.error(
				"Detected endless loop while trying to finish unfinished screening survey for participant {}",
				participant.getId());
	}

	/**
	 * Determines which {@link ScreeningSurveySlide} is the next slide to
	 * present to the user
	 * 
	 * @param participant
	 * @param screeningSurvey
	 * @param formerSlide
	 * @return
	 */
	@Synchronized
	private ScreeningSurveySlide getNextScreeningSurveySlide(
			Participant participant, final ScreeningSurvey screeningSurvey,
			final ScreeningSurveySlide formerSlide) {
		if (formerSlide == null) {
			val nextSlide = databaseManagerService.findOneSortedModelObject(
					ScreeningSurveySlide.class,
					Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY,
					Queries.SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_ASC,
					screeningSurvey.getId());

			return nextSlide;
		} else {
			ScreeningSurveySlide nextSlide = null;

			val formerSlideRules = databaseManagerService
					.findSortedModelObjects(
							ScreeningSurveySlideRule.class,
							Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
							Queries.SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_ASC,
							formerSlide.getId());

			// Retrieve all required variables to execute
			// rules
			Hashtable<String, AbstractVariableWithValue> variablesWithValues = variablesManagerService
					.getAllVariablesWithValuesOfParticipantAndSystem(participant);

			// Executing slide rules
			log.debug("Executing slide rules");
			int formerSlideRuleLevel = 0;
			boolean formerSlideRuleResult = true;
			for (val formerSlideRule : formerSlideRules) {
				if (formerSlideRule.getLevel() > formerSlideRuleLevel
						&& formerSlideRuleResult != true) {
					log.debug("Skipping rule because of level");
					continue;
				}

				// Remember new level
				formerSlideRuleLevel = formerSlideRule.getLevel();

				// Evaluate rule
				val ruleResult = RuleEvaluator.evaluateRule(formerSlideRule,
						variablesWithValues.values());

				if (!ruleResult.isEvaluatedSuccessful()) {
					log.error(
							"Error when validating rule {} of intervention: {}",
							formerSlideRule.getId(),
							ruleResult.getErrorMessage());

					formerSlideRuleResult = false;
					continue;
				}

				// Remember result
				formerSlideRuleResult = ruleResult.isRuleMatchesEquationSign();

				// Store value if relevant
				if (ruleResult.isRuleMatchesEquationSign()
						&& formerSlideRule.getStoreValueToVariableWithName() != null) {
					log.debug("Storing rule result to variable {}",
							formerSlideRule.getStoreValueToVariableWithName());

					if (formerSlideRule.getValueToStoreToVariable() == null
							|| formerSlideRule.getValueToStoreToVariable()
									.equals("")) {
						// Store rule result
						try {
							variablesManagerService
									.writeVariableValueOfParticipant(
											participant.getId(),
											formerSlideRule
													.getStoreValueToVariableWithName(),
											ruleResult.isCalculatedRule() ? StringHelpers
													.cleanDoubleValue(ruleResult
															.getCalculatedRuleValue())
													: ruleResult
															.getTextRuleValue());
							participant = databaseManagerService
									.getModelObjectById(Participant.class,
											participant.getId());
						} catch (final Exception e) {
							log.warn(
									"The variable {} could not be written: {}",
									formerSlideRule
											.getStoreValueToVariableWithName(),
									e.getMessage());
						}
					} else {
						// Store fix value
						try {
							variablesManagerService
									.writeVariableValueOfParticipant(
											participant.getId(),
											formerSlideRule
													.getStoreValueToVariableWithName(),
											formerSlideRule
													.getValueToStoreToVariable());
							participant = databaseManagerService
									.getModelObjectById(Participant.class,
											participant.getId());
						} catch (final Exception e) {
							log.warn(
									"The variable {} could not be written: {}",
									formerSlideRule
											.getStoreValueToVariableWithName(),
									e.getMessage());
						}
					}

					log.debug("Refrehsing variables");
					variablesWithValues = variablesManagerService
							.getAllVariablesWithValuesOfParticipantAndSystem(participant);
				}

				// Check if true rule matches
				if (ruleResult.isRuleMatchesEquationSign()
						&& formerSlideRule
								.getNextScreeningSurveySlideWhenTrue() != null) {
					val fetchedNextSlide = databaseManagerService
							.getModelObjectById(
									ScreeningSurveySlide.class,
									formerSlideRule
											.getNextScreeningSurveySlideWhenTrue());
					if (fetchedNextSlide != null) {
						log.debug("Rule matches (TRUE), next slide is '{}'",
								fetchedNextSlide.getTitleWithPlaceholders());
						nextSlide = fetchedNextSlide;
						break;
					} else {
						log.warn(
								"Rule matched (TRUE), but slide {} could not be found",
								formerSlideRule
										.getNextScreeningSurveySlideWhenTrue());
					}
				}

				// Check if false rule matches
				if (!ruleResult.isRuleMatchesEquationSign()
						&& formerSlideRule
								.getNextScreeningSurveySlideWhenFalse() != null) {
					val fetchedNextSlide = databaseManagerService
							.getModelObjectById(
									ScreeningSurveySlide.class,
									formerSlideRule
											.getNextScreeningSurveySlideWhenFalse());
					if (fetchedNextSlide != null) {
						log.debug("Rule matches (FALSE), next slide is '{}'",
								fetchedNextSlide.getTitleWithPlaceholders());
						nextSlide = fetchedNextSlide;
						break;
					} else {
						log.warn(
								"Rule matched (FALSE), but slide {} could not be found",
								formerSlideRule
										.getNextScreeningSurveySlideWhenFalse());
					}
				}

				// Check if validation rule matches
				if (ruleResult.isRuleMatchesEquationSign()
						&& formerSlideRule
								.isShowSameSlideBecauseValueNotValidWhenTrue()) {

					log.debug("Rule matches (VALIDATION), next slide is '{}'",
							formerSlide);
					nextSlide = formerSlide;
					break;
				}
			}

			if (nextSlide == null) {
				nextSlide = databaseManagerService
						.findOneSortedModelObject(
								ScreeningSurveySlide.class,
								Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY_AND_ORDER_HIGHER,
								Queries.SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_ASC,
								screeningSurvey.getId(), formerSlide.getOrder());
			}

			return nextSlide;
		}
	}

	/**
	 * Returns the appropriate {@link HashMap} to fill the template or
	 * <code>null</code> if an unexpected error occurs
	 * 
	 * @param feedbackParticipantId
	 *            The {@link ObjectId} of the participant of which the feedback
	 *            should be shown
	 * @param navigationValue
	 * @param checkValue
	 * @param session
	 * @return
	 */
	@Synchronized
	public HashMap<String, Object> getAppropriateFeedbackSlide(
			final ObjectId feedbackParticipantId, final String navigationValue,
			final String checkValue, final HttpSession session) {

		val templateVariables = new HashMap<String, Object>();

		// Check if participant exists and has a feedback
		val participant = databaseManagerService.getModelObjectById(
				Participant.class, feedbackParticipantId);
		if (participant == null || participant.getAssignedFeedback() == null) {
			return null;
		}

		// Check if feedback exists
		val feedback = databaseManagerService.getModelObjectById(
				Feedback.class, participant.getAssignedFeedback());
		if (feedback == null) {
			return null;
		}

		// If former slide is null or consistency check fails then start over
		// again
		FeedbackSlide formerSlide;
		val formerSlideValue = session
				.getAttribute(FeedbackSessionAttributeTypes.PARTICIPANT_FORMER_FEEDBACK_SLIDE_ID
						.toString());
		val consistencyCheck = session
				.getAttribute(FeedbackSessionAttributeTypes.FEEDBACK_CONSISTENCY_CHECK_VALUE
						.toString());
		if (formerSlideValue == null || checkValue == null
				|| !checkValue.equals(consistencyCheck)) {
			formerSlide = null;
		} else {
			formerSlide = databaseManagerService.getModelObjectById(
					FeedbackSlide.class, (ObjectId) formerSlideValue);
		}

		// Set name
		templateVariables.put(GeneralSlideTemplateFieldTypes.NAME.toVariable(),
				feedback.getName());

		// Check if feedback template is set
		log.debug("Check if template is set");
		if (feedback.getTemplatePath() == null
				|| feedback.getTemplatePath().equals("")) {
			return templateVariables;
		} else {
			templateVariables
					.put(GeneralSlideTemplateFieldTypes.TEMPLATE_FOLDER
							.toVariable(), feedback.getTemplatePath());
		}

		// Determine if former or next slide should be shown
		boolean showNextSlide = true;
		if (navigationValue != null
				&& navigationValue
						.equals(ImplementationConstants.FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE_VALUE_PREVIOUS)) {
			showNextSlide = false;
		}

		log.debug("Former slide was {}, next slide is {}", formerSlide,
				showNextSlide ? "the next slide" : "the slide before");

		// Retrieve all required variables to generate slide and execute
		// rules
		val variablesWithValues = variablesManagerService
				.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		val nextSlide = getNextFeedbackSlide(formerSlide,
				participant.getAssignedFeedback(), variablesWithValues,
				showNextSlide);

		if (nextSlide == null) {
			// Feedback done
			log.debug("No next slide found");

			return null;
		} else {
			// Remember next slide as former slide
			session.setAttribute(
					FeedbackSessionAttributeTypes.PARTICIPANT_FORMER_FEEDBACK_SLIDE_ID
							.toString(), nextSlide.getId());

			// Remember check variable
			val newCheckValue = GlobalUniqueIdGenerator.createGlobalUniqueId();
			session.setAttribute(
					FeedbackSessionAttributeTypes.FEEDBACK_CONSISTENCY_CHECK_VALUE
							.toString(), newCheckValue);

			// Check if slide is first or last slide
			if (databaseManagerService.findOneModelObject(FeedbackSlide.class,
					Queries.FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_LOWER,
					nextSlide.getFeedback(), nextSlide.getOrder()) == null) {
				templateVariables.put(
						FeedbackSlideTemplateFieldTypes.IS_FIRST_SLIDE
								.toVariable(), true);
			}
			if (databaseManagerService.findOneModelObject(FeedbackSlide.class,
					Queries.FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_HIGHER,
					nextSlide.getFeedback(), nextSlide.getOrder()) == null) {
				templateVariables.put(
						FeedbackSlideTemplateFieldTypes.IS_LAST_SLIDE
								.toVariable(), true);
			}

			// Fill next feedback slide
			log.debug("Filling next slide {} with contents",
					nextSlide.getTitleWithPlaceholders());

			// Navigation parameters
			templateVariables
					.put(FeedbackSlideTemplateFieldTypes.HIDDEN_NAVIGATION_VARIABLE
							.toVariable(),
							ImplementationConstants.FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE);
			templateVariables
					.put(FeedbackSlideTemplateFieldTypes.HIDDEN_NAVIGATION_VARIABLE_NAVIGATE_NEXT
							.toVariable(),
							ImplementationConstants.FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE_VALUE_NEXT);
			templateVariables
					.put(FeedbackSlideTemplateFieldTypes.HIDDEN_NAVIGATION_VARIABLE_NAVIGATE_PREVIOUS
							.toVariable(),
							ImplementationConstants.FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE_VALUE_PREVIOUS);

			// Check variable
			templateVariables
					.put(GeneralSlideTemplateFieldTypes.HIDDEN_CHECK_VARIABLE
							.toVariable(),
							ImplementationConstants.FEEDBACK_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE);
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.HIDDEN_CHECK_VARIABLE_VALUE
							.toVariable(), newCheckValue);

			// Optional layout attribute
			val optionalLayoutAttribute = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(nextSlide
							.getOptionalLayoutAttributeWithPlaceholders(),
							variablesWithValues.values(), "");
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.OPTIONAL_LAYOUT_ATTRIBUTE
							.toVariable(), optionalLayoutAttribute);
			val optionalLayoutAttributeObjects = new HashMap<String, Object>();
			for (val item : optionalLayoutAttribute.split(",")) {
				if (!item.equals("")) {
					optionalLayoutAttributeObjects
							.put(GeneralSlideTemplateFieldTypes.OPTIONAL_LAYOUT_ATTRIBUTE_ITEM
									.toVariable(), item);
				}
			}
			templateVariables
					.put(GeneralSlideTemplateFieldTypes.OPTIONAL_LAYOUT_ATTRIBUTE_LIST
							.toVariable(), optionalLayoutAttributeObjects);

			// Title
			val title = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(
							nextSlide.getTitleWithPlaceholders(),
							variablesWithValues.values(), "");
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.TITLE.toVariable(), title);

			// Media object URL and type
			if (nextSlide.getLinkedMediaObject() != null) {
				val mediaObject = databaseManagerService.getModelObjectById(
						MediaObject.class, nextSlide.getLinkedMediaObject());

				templateVariables.put(
						GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_URL
								.toVariable(), mediaObject.getId() + "/"
								+ mediaObject.getName());

				switch (mediaObject.getType()) {
					case AUDIO:
						templateVariables
								.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_AUDIO
										.toVariable(), true);
						break;
					case HTML_TEXT:
						templateVariables
								.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_HTML_TEXT
										.toVariable(), true);
						break;
					case IMAGE:
						templateVariables
								.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_IMAGE
										.toVariable(), true);
						break;
					case VIDEO:
						templateVariables
								.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_VIDEO
										.toVariable(), true);
						break;
				}
			}

			// Text
			val text = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(
							nextSlide.getTextWithPlaceholders(),
							variablesWithValues.values(), "");
			templateVariables.put(
					FeedbackSlideTemplateFieldTypes.TEXT.toVariable(), text);
		}

		return templateVariables;
	}

	/**
	 * Determines which {@link FeedbackSlide} is the next slide to present to
	 * the user
	 * 
	 * @param formerSlide
	 * @param feedbackId
	 * @param variablesWithValues
	 * @param showNextSlide
	 * @return
	 */
	@Synchronized
	private FeedbackSlide getNextFeedbackSlide(
			final FeedbackSlide formerSlide,
			final ObjectId feedbackId,
			final Hashtable<String, AbstractVariableWithValue> variablesWithValues,
			final boolean showNextSlide) {
		Iterable<FeedbackSlide> relevantSlides;
		if (formerSlide == null) {
			relevantSlides = databaseManagerService.findSortedModelObjects(
					FeedbackSlide.class, Queries.FEEDBACK_SLIDE__BY_FEEDBACK,
					Queries.FEEDBACK_SLIDE__SORT_BY_ORDER_ASC, feedbackId);
		} else {
			if (showNextSlide) {
				relevantSlides = databaseManagerService.findSortedModelObjects(
						FeedbackSlide.class,
						Queries.FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_HIGHER,
						Queries.FEEDBACK_SLIDE__SORT_BY_ORDER_ASC, feedbackId,
						formerSlide.getOrder());
			} else {
				relevantSlides = databaseManagerService.findSortedModelObjects(
						FeedbackSlide.class,
						Queries.FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_LOWER,
						Queries.FEEDBACK_SLIDE__SORT_BY_ORDER_DESC, feedbackId,
						formerSlide.getOrder());
			}
		}

		for (val relevantSlide : relevantSlides) {
			val slideRules = databaseManagerService.findSortedModelObjects(
					FeedbackSlideRule.class,
					Queries.FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE,
					Queries.FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_ASC,
					relevantSlide.getId());

			// Executing slide rules
			log.debug("Executing slide rules");
			boolean allRulesAreTrue = true;
			for (val slideRule : slideRules) {
				val ruleResult = RuleEvaluator.evaluateRule(slideRule,
						variablesWithValues.values());

				if (!ruleResult.isEvaluatedSuccessful()) {
					log.error("Error when validating rule: "
							+ ruleResult.getErrorMessage());
					continue;
				}

				// Check if true rule matches
				if (!ruleResult.isRuleMatchesEquationSign()) {
					allRulesAreTrue = false;
				}
			}

			if (allRulesAreTrue) {
				return relevantSlide;
			}
		}

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
	@Synchronized
	public ScreeningSurvey getScreeningSurveyById(
			final ObjectId screeningSurveyId) {
		return databaseManagerService.getModelObjectById(ScreeningSurvey.class,
				screeningSurveyId);
	}

	/**
	 * Get a specific {@link Feedback} by the {@link ObjectId} of a
	 * {@link Participant}
	 * 
	 * @param feedbackParticipantId
	 * @return
	 */
	@Synchronized
	public Feedback getFeedbackByBelongingParticipant(
			final ObjectId feedbackParticipantId) {

		// Check if participant exists and has a feedback
		val participant = databaseManagerService.getModelObjectById(
				Participant.class, feedbackParticipantId);
		if (participant == null || participant.getAssignedFeedback() == null) {
			return null;
		}

		// Check if feedback exists
		val feedback = databaseManagerService.getModelObjectById(
				Feedback.class, participant.getAssignedFeedback());
		if (feedback == null) {
			return null;
		}

		return feedback;
	}

	/**
	 * Returns all active {@link ScreeningSurvey}s or <code>null</code> if non
	 * has been
	 * found
	 * 
	 * @return
	 */
	@Synchronized
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
	@Synchronized
	public File getTemplatePath() {
		return fileStorageManagerService.getTemplatesFolder();
	}
}
