package ch.ethz.mc.services;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.Feedback;
import ch.ethz.mc.model.persistent.FeedbackSlide;
import ch.ethz.mc.model.persistent.FeedbackSlideRule;
import ch.ethz.mc.model.persistent.IntermediateSurveyAndFeedbackParticipantShortURL;
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
import ch.ethz.mc.services.types.GeneralSessionAttributeTypes;
import ch.ethz.mc.services.types.GeneralSlideTemplateFieldTypes;
import ch.ethz.mc.services.types.SurveySessionAttributeTypes;
import ch.ethz.mc.services.types.SurveySlideTemplateFieldTypes;
import ch.ethz.mc.services.types.SurveySlideTemplateLayoutTypes;
import ch.ethz.mc.tools.GlobalUniqueIdGenerator;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.RuleEvaluator;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.tools.VariableStringReplacer;

/**
 * Cares for the orchestration of {@link ScreeningSurveySlides} as
 * part of a {@link ScreeningSurvey} or a {@link Feedback}
 *
 * The templates are based on the Mustache standard. Details can be found in the
 * {@link SurveySlideTemplateFieldTypes} class
 *
 * @author Andreas Filler
 */
@Log4j2
public class SurveyExecutionManagerService {
	private final Object									$lock;

	private static SurveyExecutionManagerService			instance	= null;

	private final DatabaseManagerService					databaseManagerService;
	private final FileStorageManagerService					fileStorageManagerService;
	private final VariablesManagerService					variablesManagerService;

	private final InterventionAdministrationManagerService	interventionAdministrationManagerService;

	private SurveyExecutionManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final InterventionAdministrationManagerService interventionAdministrationManagerService)
					throws Exception {
		$lock = MC.getInstance();

		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.variablesManagerService = variablesManagerService;

		this.interventionAdministrationManagerService = interventionAdministrationManagerService;

		log.info("Started.");
	}

	public static SurveyExecutionManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final InterventionAdministrationManagerService interventionAdministrationManagerService)
					throws Exception {
		if (instance == null) {
			instance = new SurveyExecutionManagerService(
					databaseManagerService, fileStorageManagerService,
					variablesManagerService,
					interventionAdministrationManagerService);
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
		final val participant = new Participant(
				screeningSurvey.getIntervention(),
				InternalDateTime.currentTimeMillis(), "",
				Constants.getInterventionLocales()[0], null,
				screeningSurvey.getId(), screeningSurvey.getGlobalUniqueId(),
				null, null, true, "", "");

		databaseManagerService.saveModelObject(participant);

		dialogStatusCreate(participant.getId());

		return participant;
	}

	@Synchronized
	private void participantSetOrganizationAndUnit(
			final Participant participant, final String organization,
			final String organizationUnit) {
		participant.setOrganization(organization);
		participant.setOrganizationUnit(organizationUnit);

		databaseManagerService.saveModelObject(participant);
	}

	@Synchronized
	public IntermediateSurveyAndFeedbackParticipantShortURL participantSetFeedback(
			final Participant participant, final ObjectId feedbackId) {
		val feedback = databaseManagerService.getModelObjectById(
				Feedback.class, feedbackId);

		participant.setAssignedFeedback(feedbackId);
		participant.setAssignedFeedbackGlobalUniqueId(feedback
				.getGlobalUniqueId());

		val feedbackParticipantShortURL = interventionAdministrationManagerService
				.feedbackParticipantShortURLEnsure(participant.getId(),
						feedbackId);

		databaseManagerService.saveModelObject(participant);

		return feedbackParticipantShortURL;
	}

	@Synchronized
	public IntermediateSurveyAndFeedbackParticipantShortURL participantGetFeedbackShortURL(
			final Participant participant) {
		if (participant.getAssignedFeedback() == null) {
			val feedbackShortURL = databaseManagerService
					.findOneModelObject(
							IntermediateSurveyAndFeedbackParticipantShortURL.class,
							Queries.INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_PARTICIPANT_AND_FEEDBACK,
							participant.getId(),
							participant.getAssignedFeedback());

			return feedbackShortURL;
		} else {
			return null;
		}
	}

	// Dialog status
	@Synchronized
	private void dialogStatusCreate(final ObjectId participantId) {
		final long currentTimestamp = InternalDateTime.currentTimeMillis();
		final val dialogStatus = new DialogStatus(participantId, "", null,
				null, currentTimestamp, false, currentTimestamp, 0, false, 0,
				0, false, 0);

		databaseManagerService.saveModelObject(dialogStatus);
	}

	@Synchronized
	private void dialogStatusSetScreeningSurveyFinished(
			final ObjectId participantId) {
		final val dialogStatus = databaseManagerService.findOneModelObject(
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
		final val dialogStatus = databaseManagerService.findOneModelObject(
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
		final val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participantId);

		// Check if all data for monitoring is available
		if (!dialogStatus.isDataForMonitoringParticipationAvailable()) {
			final val dataForMonitoringParticipationAvailable = checkForDataForMonitoringParticipation(participantId);

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

		final val dialogOptions = databaseManagerService.findModelObjects(
				DialogOption.class,
				Queries.DIALOG_OPTION__FOR_PARTICIPANT_BY_PARTICIPANT,
				participantId);

		if (dialogOptions != null && dialogOptions.iterator().hasNext()) {
			return false;
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
		final val screeningSurvey = databaseManagerService.getModelObjectById(
				ScreeningSurvey.class, screeningSurveyId);

		if (screeningSurvey == null || !screeningSurvey.isActive()) {
			return false;
		}

		final val intervention = databaseManagerService.getModelObjectById(
				Intervention.class, screeningSurvey.getIntervention());

		if (intervention == null || !intervention.isActive()) {
			return false;
		}

		return true;
	}

	/**
	 * Returns if the requested {@link ScreeningSurvey} is currently accessible
	 * (means the the {@link Intervention} and {@link ScreeningSurvey} are both
	 * active) and if its the right type
	 *
	 * @param screeningSurveyId
	 * @return
	 */
	@Synchronized
	public boolean screeningSurveyCheckIfActiveAndOfGivenType(
			final ObjectId screeningSurveyId, final boolean isIntermediateSurvey) {
		final val screeningSurvey = databaseManagerService.getModelObjectById(
				ScreeningSurvey.class, screeningSurveyId);

		if (screeningSurvey == null
				|| !screeningSurvey.isActive()
				|| screeningSurvey.isIntermediateSurvey() != isIntermediateSurvey) {
			return false;
		}

		final val intervention = databaseManagerService.getModelObjectById(
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
			final ObjectId participantId, final ObjectId feedbackId) {
		final val participant = databaseManagerService.getModelObjectById(
				Participant.class, participantId);

		if (participant == null) {
			return false;
		}

		final val feedback = databaseManagerService.getModelObjectById(
				Feedback.class, participant.getAssignedFeedback());

		if (feedback == null) {
			return false;
		}

		if (!feedback.getId().equals(feedbackId)) {
			return false;
		}

		final val screeningSurvey = databaseManagerService.getModelObjectById(
				ScreeningSurvey.class, feedback.getScreeningSurvey());

		if (screeningSurvey == null) {
			return false;
		}

		final val intervention = databaseManagerService.getModelObjectById(
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
	 * @param isScreening
	 *            Only for screening surveys, not for intermediate surveys
	 * @param screeningSurveyId
	 *            The {@link ObjectId} of the requested {@link ScreeningSurvey}
	 * @param resultValues
	 * @param session
	 * @return
	 */
	@Synchronized
	public HashMap<String, Object> getAppropriateScreeningSurveySlide(
			ObjectId participantId, final boolean accessGranted,
			final boolean isScreening, final ObjectId screeningSurveyId,
			final List<String> resultValues, final String checkValue,
			final HttpSession session) {

		final val templateVariables = new HashMap<String, Object>();

		// Set default language (can be changed later depending on participant)
		templateVariables.put(
				GeneralSlideTemplateFieldTypes.LANGUAGE.toVariable(),
				Constants.getInterventionLocales()[0].toLanguageTag());

		// Check if screening survey is active
		log.debug("Check if screening survey is active");
		if (!screeningSurveyCheckIfActive(screeningSurveyId)) {
			templateVariables.put(
					SurveySlideTemplateLayoutTypes.CLOSED.toVariable(), true);
			return templateVariables;
		}

		final val screeningSurvey = getScreeningSurveyById(screeningSurveyId);

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

		// Set name
		Participant participant = null;
		if (participantId != null) {
			participant = databaseManagerService.getModelObjectById(
					Participant.class, participantId);

			if (participant == null) {
				// Participant does not exist anymore
				log.debug("Participant does not exist anymore");

				templateVariables.put(
						SurveySlideTemplateLayoutTypes.DISABLED.toVariable(),
						true);
				return templateVariables;
			} else {
				templateVariables.put(
						GeneralSlideTemplateFieldTypes.NAME.toVariable(),
						screeningSurvey.getName().get(participant));
			}
		} else {
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.NAME.toVariable(),
					screeningSurvey.getName().get(
							Constants.getInterventionLocales()[0]));
		}

		// Check if participant already has access (if required)
		log.debug("Check if participant has access to screening survey");
		if (screeningSurvey.getPassword() != null
				&& !screeningSurvey.getPassword().equals("") && !accessGranted) {

			// Login is required, check login information, if provided
			if (resultValues != null
					&& resultValues.size() > 0
					&& resultValues.get(0)
					.equals(screeningSurvey.getPassword())) {
				log.debug("Access granted");
				// Remember that user authenticated
				session.setAttribute(
						SurveySessionAttributeTypes.SURVEY_PARTICIPANT_ACCESS_GRANTED
						.toString(), true);
			} else {
				// Redirect to password page
				log.debug("Access not granted - show password page (again)");
				templateVariables.put(
						SurveySlideTemplateLayoutTypes.PASSWORD_INPUT
						.toVariable(), true);

				templateVariables
				.put(SurveySlideTemplateFieldTypes.RESULT_VARIABLE
						.toVariable(),
						ImplementationConstants.SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLES + 0);

				return templateVariables;
			}
		}

		// Create participant if she does not exist (will only happen for
		// screening survey calls)
		if (participantId == null) {
			log.debug("Create participant");
			participant = participantCreate(screeningSurvey);
			participantId = participant.getId();

			session.setAttribute(
					GeneralSessionAttributeTypes.CURRENT_PARTICIPANT.toString(),
					participantId);

			// Create participant for currently logged in debug user
			if (session
					.getAttribute(ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_EXPECTED) != null
					&& (boolean) session
					.getAttribute(ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_EXPECTED)) {
				session.setAttribute(
						ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_EXPECTED,
						false);
				session.setAttribute(
						ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE,
						participantId);

				if (session
						.getAttribute(ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_DESCRIPTION) != null) {
					val dateFormat = DateFormat.getDateTimeInstance(
							DateFormat.MEDIUM, DateFormat.MEDIUM,
							Constants.getAdminLocale());
					val date = dateFormat.format(new Date(InternalDateTime
							.currentTimeMillis()));

					participantSetOrganizationAndUnit(
							participant,
							Messages.getAdminString(
									AdminMessageStrings.DEBUG__PARTICIPANT_ORGANIZATION,
									(String) session
									.getAttribute(ImplementationConstants.PARTICIPANT_SESSION_ATTRIBUTE_DESCRIPTION)),
									Messages.getAdminString(
											AdminMessageStrings.DEBUG__PARTICIPANT_ORGANIZATION_UNIT,
											date));
				}
			}
		} else {
			log.debug("Participant exists");
			participant = databaseManagerService.getModelObjectById(
					Participant.class, participantId);

			session.setAttribute(
					GeneralSessionAttributeTypes.CURRENT_PARTICIPANT.toString(),
					participantId);

			final val dialogStatus = databaseManagerService.findOneModelObject(
					DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
					participantId);
			if (!participant.isMonitoringActive()) {
				// Redirect to disabled slide if monitoring is switched off for
				// participant
				log.debug("Monitoring is switched off for participant");

				templateVariables.put(
						SurveySlideTemplateLayoutTypes.DISABLED.toVariable(),
						true);
				return templateVariables;
			} else if (isScreening && dialogStatus.isScreeningSurveyPerformed()) {
				// Redirect to done slide if user already completely performed
				// the
				// screening survey
				log.debug("User already participated");

				templateVariables.put(
						SurveySlideTemplateLayoutTypes.DONE.toVariable(), true);
				return templateVariables;
			} else if (!isScreening && dialogStatus.isMonitoringPerformed()) {
				// Redirect to done slide if user already finished monitoring
				log.debug("User already participated in intervention");

				templateVariables.put(
						SurveySlideTemplateLayoutTypes.DONE.toVariable(), true);
				return templateVariables;
			}
		}

		// Get last visited slide
		final val formerSlideId = session
				.getAttribute(SurveySessionAttributeTypes.SURVEY_FORMER_SLIDE_ID
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
						SurveySessionAttributeTypes.SURVEY_CONSISTENCY_CHECK_VALUE
						.toString()).equals(checkValue)) {
			log.debug("Consistency check failed; show same page again");

			// Next slide is former slide
			nextSlide = formerSlide;
		} else {
			// If there was a former slide, store result if provided
			log.debug("Checking for result values to store...");
			if (formerSlideId != null && resultValues != null) {
				for (int i = 0; i < resultValues.size(); i++) {
					// If question results should to be saved
					final val questions = formerSlide.getQuestions();
					ScreeningSurveySlide.Question question = null;

					if (questions.size() > i
							&& (question = questions.get(i))
							.getStoreValueToVariableWithName() != null
							&& !question.getStoreValueToVariableWithName()
							.equals("")) {

						final val resultValue = resultValues.get(i);
						final val variableName = question
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
				participant = databaseManagerService.getModelObjectById(
						Participant.class, participant.getId());
			}
		}

		// Adjust dialog status
		if (isScreening) {
			dialogStatusUpdateAfterDeterminingNextSlide(participantId,
					formerSlide, true);
		}

		if (nextSlide == null) {
			// Screening survey done
			log.debug("No next slide found");

			if (isScreening) {
				dialogStatusSetScreeningSurveyFinished(participantId);
			}

			return null;
		} else {
			// Check if it's the last slide
			if (nextSlide.isLastSlide() && isScreening) {
				// Set feedback URL to participant and session if required
				if (nextSlide.getHandsOverToFeedback() != null) {
					log.debug("Setting feedback {} for participant {}",
							nextSlide.getHandsOverToFeedback(),
							participant.getId());
					val feedbackShortURL = participantSetFeedback(participant,
							nextSlide.getHandsOverToFeedback());
					session.setAttribute(
							SurveySessionAttributeTypes.SURVEY_PARTICIPANT_FEEDBACK_URL
							.toString(), feedbackShortURL
							.calculateURL());
				}

				dialogStatusSetScreeningSurveyFinished(participantId);
			} else if (!isScreening
					&& participant.getAssignedFeedback() != null) {
				// Set feedback URL if a feedback is already set for participant
				val feedbackURL = participantGetFeedbackShortURL(participant);
				if (feedbackURL != null) {
					templateVariables.put(
							GeneralSlideTemplateFieldTypes.FEEDBACK_URL
							.toString(), feedbackURL.calculateURL());
				}
			}

			// Remember next slide as former slide
			session.setAttribute(
					SurveySessionAttributeTypes.SURVEY_FORMER_SLIDE_ID
					.toString(), nextSlide.getId());

			// Remember check variable
			final val newCheckValue = GlobalUniqueIdGenerator
					.createGlobalUniqueId();
			session.setAttribute(
					SurveySessionAttributeTypes.SURVEY_CONSISTENCY_CHECK_VALUE
					.toString(), newCheckValue);

			// Fill next screening survey slide
			log.debug("Filling next slide '{}' with contents",
					nextSlide.getTitleWithPlaceholders());

			// Layout
			switch (nextSlide.getQuestionType()) {
				case MULTILINE_TEXT_INPUT:
					templateVariables.put(
							SurveySlideTemplateLayoutTypes.MULTILINE_TEXT_INPUT
							.toVariable(), true);
					break;
				case NUMBER_INPUT:
					templateVariables.put(
							SurveySlideTemplateLayoutTypes.NUMBER_INPUT
							.toVariable(), true);
					break;
				case SELECT_MANY:
					templateVariables.put(
							SurveySlideTemplateLayoutTypes.SELECT_MANY
							.toVariable(), true);
					break;
				case SELECT_ONE:
					templateVariables.put(
							SurveySlideTemplateLayoutTypes.SELECT_ONE
							.toVariable(), true);
					break;
				case TEXT_INPUT:
					templateVariables.put(
							SurveySlideTemplateLayoutTypes.TEXT_INPUT
							.toVariable(), true);
					break;
				case TEXT_ONLY:
					templateVariables.put(
							SurveySlideTemplateLayoutTypes.TEXT_ONLY
							.toVariable(), true);
					break;
				case MEDIA_ONLY:
					templateVariables.put(
							SurveySlideTemplateLayoutTypes.MEDIA_ONLY
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
			final val optionalLayoutAttribute = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(participant
							.getLanguage(), nextSlide
							.getOptionalLayoutAttributeWithPlaceholders(),
							variablesWithValues.values(), "");
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.OPTIONAL_LAYOUT_ATTRIBUTE
					.toVariable(), optionalLayoutAttribute);

			// Language
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.LANGUAGE.toVariable(),
					participant.getLanguage().toLanguageTag());

			// Group
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.GROUP.toVariable(),
					participant.getGroup());

			// Title
			final val title = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(participant
							.getLanguage(), nextSlide
							.getTitleWithPlaceholders().get(participant),
							variablesWithValues.values(), "");
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.TITLE.toVariable(), title);

			// Validation error message
			if (formerSlide != null && nextSlide != null
					&& formerSlide.getId().equals(nextSlide.getId())) {
				final val validationErrorMessage = VariableStringReplacer
						.findVariablesAndReplaceWithTextValues(participant
								.getLanguage(), nextSlide
								.getValidationErrorMessage().get(participant),
								variablesWithValues.values(), "");
				templateVariables.put(
						SurveySlideTemplateFieldTypes.VALIDATION_ERROR_MESSAGE
						.toVariable(), validationErrorMessage);
			}

			// Media object URL and type
			if (nextSlide.getLinkedMediaObject() != null) {
				final val mediaObject = databaseManagerService
						.getModelObjectById(MediaObject.class,
								nextSlide.getLinkedMediaObject());

				if (mediaObject.getFileReference() != null) {
					templateVariables.put(
							GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_URL
							.toVariable(),
							mediaObject.getId()
							+ "/"
							+ StringHelpers
							.cleanFilenameString(mediaObject
									.getName()));
				} else if (mediaObject.getUrlReference() != null) {
					templateVariables.put(
							GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_URL
							.toVariable(), mediaObject
							.getUrlReference());
				}

				switch (mediaObject.getType()) {
					case HTML_TEXT:
						templateVariables
						.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_HTML_TEXT
								.toVariable(), true);
						break;
					case URL:
						templateVariables
						.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_URL
								.toVariable(), true);
						break;
					case AUDIO:
						templateVariables
						.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_AUDIO
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

			final val questions = nextSlide.getQuestions();
			for (int i = 0; i < questions.size(); i++) {
				final val question = questions.get(i);
				final val questionObject = new HashMap<String, Object>();

				// Question
				final val questionText = VariableStringReplacer
						.findVariablesAndReplaceWithTextValues(
								participant.getLanguage(),
								question.getQuestionWithPlaceholders().get(
										participant), variablesWithValues
										.values(), "");
				questionObject.put(SurveySlideTemplateFieldTypes.QUESTION_TEXT
						.toVariable(), questionText);
				questionObject.put(
						SurveySlideTemplateFieldTypes.QUESTION_POSITION
						.toVariable(), i + 1);

				// Result variable
				questionObject
				.put(SurveySlideTemplateFieldTypes.RESULT_VARIABLE
						.toVariable(),
						ImplementationConstants.SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLES
						+ i);

				// Answers (text, value, preselected)
				final val answersWithPlaceholders = question
						.getAnswersWithPlaceholders();
				final val answerValues = question.getAnswerValues();

				final List<HashMap<String, Object>> answersObjects = new ArrayList<HashMap<String, Object>>();
				for (int j = 0; j < answersWithPlaceholders.length; j++) {
					final val answerObjects = new HashMap<String, Object>();

					final val answerWithPlaceholder = answersWithPlaceholders[j];
					final val finalAnswerText = VariableStringReplacer
							.findVariablesAndReplaceWithTextValues(
									participant.getLanguage(),
									answerWithPlaceholder.get(participant),
									variablesWithValues.values(), "");

					final val answerValue = answerValues[j];

					answerObjects.put(
							SurveySlideTemplateFieldTypes.ANSWER_POSITION
							.toVariable(), j + 1);
					answerObjects.put(SurveySlideTemplateFieldTypes.ANSWER_TEXT
							.toVariable(), finalAnswerText);
					answerObjects.put(
							SurveySlideTemplateFieldTypes.ANSWER_VALUE
							.toVariable(), answerValue);
					if (j == 0) {
						answerObjects.put(
								SurveySlideTemplateFieldTypes.IS_FIRST_ANSWER
								.toVariable(), true);
					}
					if (j == answersWithPlaceholders.length - 1) {
						answerObjects.put(
								SurveySlideTemplateFieldTypes.IS_LAST_ANSWER
								.toVariable(), true);
					}
					if (question.getPreSelectedAnswer() == j) {
						answerObjects
						.put(SurveySlideTemplateFieldTypes.PRESELECTED_ANSWER
								.toVariable(), true);
					}

					answersObjects.add(answerObjects);
				}

				questionObject.put(SurveySlideTemplateFieldTypes.ANSWERS_COUNT
						.toVariable(), answerValues.length);

				if (answersObjects.size() > 0) {
					questionObject.put(
							SurveySlideTemplateFieldTypes.ANSWERS.toVariable(),
							answersObjects);
				}

				questionObjects.add(questionObject);
			}

			templateVariables.put(
					SurveySlideTemplateFieldTypes.QUESTIONS.toVariable(),
					questionObjects);
			templateVariables.put(
					SurveySlideTemplateFieldTypes.QUESTIONS_COUNT.toVariable(),
					questions.size());

			// Set intermediate survey link (if set)
			if (nextSlide.getLinkedIntermediateSurvey() != null
					&& participant != null) {
				val linkedIntermediateSurveyShortURL = intermediateSurveyParticipantShortURLEnsure(
						participant.getId(),
						nextSlide.getLinkedIntermediateSurvey());
				templateVariables.put(
						SurveySlideTemplateFieldTypes.INTERMEDIATE_SURVEY_URL
						.toVariable(), linkedIntermediateSurveyShortURL
						.calculateURL());
			}

			// Is last slide
			if (nextSlide.isLastSlide()) {
				templateVariables.put(
						SurveySlideTemplateFieldTypes.IS_LAST_SLIDE
						.toVariable(), true);
			}
		}

		return templateVariables;
	}

	/**
	 * Tries to finish all unfinished {@link ScreeningSurvey}s of
	 * {@link Participant} with the following state:
	 *
	 * - the belonging intervention is active and automatic finishing of surveys
	 * is on
	 * - the participant has all data for monitoring available
	 * - the participant has not finished the screening survey
	 * - the participant has not finished the monitoring
	 *
	 */
	@Synchronized
	public void finishUnfinishedScreeningSurveys() {
		for (final val interventionId : databaseManagerService
				.findModelObjectIds(
						Intervention.class,
						Queries.INTERVENTION__ACTIVE_TRUE_AND_AUTOMATICALLY_FINISH_SCREENING_SURVEYS_TRUE)) {
			for (final val participant : databaseManagerService
					.findModelObjects(Participant.class,
							Queries.PARTICIPANT__BY_INTERVENTION,
							interventionId)) {
				if (participant != null) {
					for (final val dialogStatus : databaseManagerService
							.findModelObjects(
									DialogStatus.class,
									Queries.DIALOG_STATUS__BY_PARTICIPANT_AND_LAST_VISITED_SCREENING_SURVEY_SLIDE_TIMESTAMP_LOWER_AND_DATA_FOR_MONITORING_PARTICIPATION_AVAILABLE_TRUE_AND_SCREENING_SURVEY_PERFORMED_FALSE_AND_MONITORING_PERFORMED_FALSE,
									participant.getId(),
									InternalDateTime.currentTimeMillis()
									- ImplementationConstants.HOURS_TO_TIME_IN_MILLIS_MULTIPLICATOR
									* 2)) {
						if (dialogStatus != null) {

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

		final val screeningSurvey = getScreeningSurveyById(participant
				.getAssignedScreeningSurvey());

		if (screeningSurvey == null) {
			log.warn("Could not finish screening survey for participant, because it does not exist");
			throw new NullPointerException();
		}

		int i = 0;
		do {
			final val dialogStatus = databaseManagerService.findOneModelObject(
					DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
					participant.getId());

			if (dialogStatus == null
					|| dialogStatus.isScreeningSurveyPerformed()) {
				log.warn("Could not finish screening survey for participant, because dialog status does not exist or is already performed");
				throw new NullPointerException();
			}

			// Get last visited slide
			final val formerSlideId = dialogStatus
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
					final val questions = formerSlide.getQuestions();
					final val question = questions.get(j);

					if (question.getDefaultValue() != null
							&& !question.getDefaultValue().equals("")
							&& question.getStoreValueToVariableWithName() != null
							&& !question.getStoreValueToVariableWithName()
							.equals("")) {
						final val variableName = question
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
	 * Ensure intermediate survey short URL for participant
	 *
	 * @param participantId
	 * @param screeningSurveyId
	 * @return
	 */
	@Synchronized
	public IntermediateSurveyAndFeedbackParticipantShortURL intermediateSurveyParticipantShortURLEnsure(
			final ObjectId participantId, final ObjectId screeningSurveyId) {

		val existingShortIdObject = databaseManagerService
				.findOneModelObject(
						IntermediateSurveyAndFeedbackParticipantShortURL.class,
						Queries.INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_PARTICIPANT_AND_SURVEY,
						participantId, screeningSurveyId);

		if (existingShortIdObject != null) {
			return existingShortIdObject;
		} else {
			val newestShortIdObject = databaseManagerService
					.findOneSortedModelObject(
							IntermediateSurveyAndFeedbackParticipantShortURL.class,
							Queries.ALL,
							Queries.INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__SORT_BY_SHORT_ID_DESC);

			final long nextShortId = newestShortIdObject == null ? 1
					: newestShortIdObject.getShortId() + 1;

			val newShortIdObject = new IntermediateSurveyAndFeedbackParticipantShortURL(
					nextShortId, StringHelpers.createRandomString(4),
					participantId, screeningSurveyId, null);

			databaseManagerService.saveModelObject(newShortIdObject);

			return newShortIdObject;
		}
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
			final val nextSlide = databaseManagerService
					.findOneSortedModelObject(
							ScreeningSurveySlide.class,
							Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY,
							Queries.SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_ASC,
							screeningSurvey.getId());

			return nextSlide;
		} else {
			ScreeningSurveySlide nextSlide = null;

			final val formerSlideRules = databaseManagerService
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
			for (final val formerSlideRule : formerSlideRules) {
				if (formerSlideRule.getLevel() > formerSlideRuleLevel
						&& formerSlideRuleResult != true) {
					log.debug("Skipping rule because of level");
					continue;
				}

				// Remember new level
				formerSlideRuleLevel = formerSlideRule.getLevel();

				// Evaluate rule
				final val ruleResult = RuleEvaluator.evaluateRule(
						participant.getLanguage(), formerSlideRule,
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
						// Store fix value (localized if piped)
						String valueToStore = formerSlideRule
								.getValueToStoreToVariable();

						// Set localized if it is localized and a fitting
						// language exists
						if (formerSlideRule.getValueToStoreToVariable()
								.contains("|")) {
							val valueToStoreParts = formerSlideRule
									.getValueToStoreToVariable().split("\\|");

							int i = 0;
							for (val locale : Constants
									.getInterventionLocales()) {
								if (locale.equals(participant.getLanguage())) {
									valueToStore = valueToStoreParts[i];
								}
								i++;
							}
						}
						try {
							variablesManagerService
							.writeVariableValueOfParticipant(
									participant.getId(),
									formerSlideRule
									.getStoreValueToVariableWithName(),
									valueToStore);
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
					final val fetchedNextSlide = databaseManagerService
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
					final val fetchedNextSlide = databaseManagerService
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
	 * @param participantId
	 *            The {@link ObjectId} of the participant of which the feedback
	 *            should be shown
	 * @param feedbackId
	 *            The {@link ObjectId} of the feedback that should be shown
	 * @param navigationValue
	 * @param checkValue
	 * @param session
	 * @return
	 */
	@Synchronized
	public HashMap<String, Object> getAppropriateFeedbackSlide(
			final ObjectId participantId, final ObjectId feedbackId,
			final String navigationValue, final String checkValue,
			final HttpSession session) {

		final val templateVariables = new HashMap<String, Object>();

		// Check if participant exists and has a feedback
		final val participant = databaseManagerService.getModelObjectById(
				Participant.class, participantId);
		if (participant == null || participant.getAssignedFeedback() == null) {
			return null;
		} else {
			session.setAttribute(
					GeneralSessionAttributeTypes.CURRENT_PARTICIPANT.toString(),
					participantId);
		}

		// Check if feedback exists
		final val feedback = databaseManagerService.getModelObjectById(
				Feedback.class, feedbackId);
		if (feedback == null
				|| !participant.getAssignedFeedback().equals(feedbackId)) {
			return null;
		}

		// If former slide is null or consistency check fails then start over
		// again
		FeedbackSlide formerSlide;
		final val formerSlideValue = session
				.getAttribute(FeedbackSessionAttributeTypes.FEEDBACK_FORMER_SLIDE_ID
						.toString());
		final val consistencyCheck = session
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
				feedback.getName().get(participant));

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
		final val variablesWithValues = variablesManagerService
				.getAllVariablesWithValuesOfParticipantAndSystem(participant);

		final val nextSlide = getNextFeedbackSlide(participant.getLanguage(),
				formerSlide, participant.getAssignedFeedback(),
				variablesWithValues, showNextSlide);

		if (nextSlide == null) {
			// Feedback done
			log.debug("No next slide found");

			return null;
		} else {
			// Remember next slide as former slide
			session.setAttribute(
					FeedbackSessionAttributeTypes.FEEDBACK_FORMER_SLIDE_ID
					.toString(), nextSlide.getId());

			// Remember check variable
			final val newCheckValue = GlobalUniqueIdGenerator
					.createGlobalUniqueId();
			session.setAttribute(
					FeedbackSessionAttributeTypes.FEEDBACK_CONSISTENCY_CHECK_VALUE
					.toString(), newCheckValue);

			// Check if slide is first or last slide
			final val priorAppropriateSlide = getNextFeedbackSlide(
					participant.getLanguage(), nextSlide,
					participant.getAssignedFeedback(), variablesWithValues,
					false);
			if (priorAppropriateSlide == null) {
				templateVariables.put(
						FeedbackSlideTemplateFieldTypes.IS_FIRST_SLIDE
						.toVariable(), true);
			}
			final val nextAppropriateSlide = getNextFeedbackSlide(
					participant.getLanguage(), nextSlide,
					participant.getAssignedFeedback(), variablesWithValues,
					true);
			if (nextAppropriateSlide == null) {
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
			final val optionalLayoutAttribute = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(participant
							.getLanguage(), nextSlide
							.getOptionalLayoutAttributeWithPlaceholders(),
							variablesWithValues.values(), "");
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.OPTIONAL_LAYOUT_ATTRIBUTE
					.toVariable(), optionalLayoutAttribute);
			final val optionalLayoutAttributeObjects = new HashMap<String, Object>();
			for (final val item : optionalLayoutAttribute.split(",")) {
				if (!item.equals("")) {
					optionalLayoutAttributeObjects
					.put(GeneralSlideTemplateFieldTypes.OPTIONAL_LAYOUT_ATTRIBUTE_ITEM
							.toVariable(), item);
				}
			}
			templateVariables
			.put(GeneralSlideTemplateFieldTypes.OPTIONAL_LAYOUT_ATTRIBUTE_LIST
					.toVariable(), optionalLayoutAttributeObjects);

			// Language
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.LANGUAGE.toVariable(),
					participant.getLanguage().toLanguageTag());

			// Group
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.GROUP.toVariable(),
					participant.getGroup());

			// Title
			final val title = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(participant
							.getLanguage(), nextSlide
							.getTitleWithPlaceholders().get(participant),
							variablesWithValues.values(), "");
			templateVariables.put(
					GeneralSlideTemplateFieldTypes.TITLE.toVariable(), title);

			// Media object URL and type
			if (nextSlide.getLinkedMediaObject() != null) {
				final val mediaObject = databaseManagerService
						.getModelObjectById(MediaObject.class,
								nextSlide.getLinkedMediaObject());

				if (mediaObject.getFileReference() != null) {
					templateVariables.put(
							GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_URL
							.toVariable(),
							mediaObject.getId()
							+ "/"
							+ StringHelpers
							.cleanFilenameString(mediaObject
									.getName()));
				} else if (mediaObject.getUrlReference() != null) {
					templateVariables.put(
							GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_URL
							.toVariable(), mediaObject
							.getUrlReference());
				}

				switch (mediaObject.getType()) {
					case HTML_TEXT:
						templateVariables
						.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_HTML_TEXT
								.toVariable(), true);
						break;
					case URL:
						templateVariables
						.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_URL
								.toVariable(), true);
						break;
					case AUDIO:
						templateVariables
						.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_TYPE_AUDIO
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
			final val text = VariableStringReplacer
					.findVariablesAndReplaceWithTextValues(participant
							.getLanguage(), nextSlide.getTextWithPlaceholders()
							.get(participant), variablesWithValues.values(), "");
			templateVariables.put(
					FeedbackSlideTemplateFieldTypes.TEXT.toVariable(), text);
		}

		return templateVariables;
	}

	/**
	 * Determines which {@link FeedbackSlide} is the next slide to present to
	 * the user
	 *
	 * @param locale
	 * @param formerSlide
	 * @param feedbackId
	 * @param variablesWithValues
	 * @param showNextSlide
	 * @return
	 */
	@Synchronized
	private FeedbackSlide getNextFeedbackSlide(
			final Locale locale,
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

		for (final val relevantSlide : relevantSlides) {
			final val slideRules = databaseManagerService
					.findSortedModelObjects(FeedbackSlideRule.class,
							Queries.FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE,
							Queries.FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_ASC,
							relevantSlide.getId());

			// Executing slide rules
			log.debug("Executing slide rules");
			boolean allRulesAreTrue = true;
			for (final val slideRule : slideRules) {
				final val ruleResult = RuleEvaluator.evaluateRule(locale,
						slideRule, variablesWithValues.values());

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
	 * Get a specific {@link Feedback} by {@link ObjectId}
	 *
	 * @param feedbackId
	 * @return
	 */
	@Synchronized
	public Feedback getFeedbackById(final ObjectId feedbackId) {
		return databaseManagerService.getModelObjectById(Feedback.class,
				feedbackId);
	}

	/**
	 * Get a specific {@link IntermediateSurveyAndFeedbackParticipantShortURL}
	 * by the long id
	 *
	 * @param longId
	 * @return
	 */
	@Synchronized
	public IntermediateSurveyAndFeedbackParticipantShortURL getIntermediateSurveyAndFeedbackParticipantShortURL(
			final long shortId) {
		return databaseManagerService
				.findOneModelObject(
						IntermediateSurveyAndFeedbackParticipantShortURL.class,
						Queries.INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_SHORT_ID,
						shortId);
	}

	/**
	 * Returns all active and non intermediate {@link ScreeningSurvey}s or
	 * <code>null</code> if non has been found
	 *
	 * @return
	 */
	@Synchronized
	public Iterable<ScreeningSurvey> getActiveNonItermediateScreeningSurveys() {
		final Iterable<Intervention> activeInterventions = databaseManagerService
				.findModelObjects(Intervention.class,
						Queries.INTERVENTION__ACTIVE_TRUE);

		final val activeNonIntermediateScreeningSurveys = new ArrayList<ScreeningSurvey>();

		for (final val intervention : activeInterventions) {
			CollectionUtils
			.addAll(activeNonIntermediateScreeningSurveys,
					databaseManagerService
					.findModelObjects(
							ScreeningSurvey.class,
							Queries.SCREENING_SURVEY__BY_INTERVENTION_AND_ACTIVE_TRUE_AND_INTERMEDIATE_SURVEY_FALSE,
							intervention.getId()).iterator());
		}

		return activeNonIntermediateScreeningSurveys;
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
