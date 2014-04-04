package org.isgf.mhc.services;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.persistent.Feedback;
import org.isgf.mhc.model.persistent.FeedbackSlide;
import org.isgf.mhc.model.persistent.FeedbackSlideRule;
import org.isgf.mhc.model.persistent.ScreeningSurvey;
import org.isgf.mhc.model.persistent.ScreeningSurveySlide;
import org.isgf.mhc.model.persistent.ScreeningSurveySlideRule;
import org.isgf.mhc.model.persistent.types.RuleEquationSignTypes;
import org.isgf.mhc.model.persistent.types.ScreeningSurveySlideQuestionTypes;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.ModelObjectExchangeService;
import org.isgf.mhc.services.internal.VariablesManagerService;
import org.isgf.mhc.services.types.ModelObjectExchangeFormats;
import org.isgf.mhc.tools.GlobalUniqueIdGenerator;
import org.isgf.mhc.tools.StringValidator;
import org.isgf.mhc.ui.NotificationMessageException;

@Log4j2
/**
 * Cares for the creation of the {@link ScreeningSurvey}s and {@link Feedback}s
 * as well as all related {@link ModelObject}s
 * 
 * @author Andreas Filler
 */
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
			final ObjectId interventionId) {
		val screeningSurvey = new ScreeningSurvey(
				GlobalUniqueIdGenerator.createGlobalUniqueId(), interventionId,
				name, "", null, false);

		if (name.equals("")) {
			screeningSurvey.setName(DEFAULT_OBJECT_NAME);
		}

		databaseManagerService.saveModelObject(screeningSurvey);

		return screeningSurvey;
	}

	public void screeningSurveyRecreateGlobalUniqueId(
			final ScreeningSurvey importedScreeningSurvey) {
		importedScreeningSurvey.setGlobalUniqueId(GlobalUniqueIdGenerator
				.createGlobalUniqueId());

		val feedbacksOfScreeningSurvey = databaseManagerService
				.findModelObjects(Feedback.class,
						Queries.FEEDBACK__BY_SCREENING_SURVEY,
						importedScreeningSurvey.getId());

		for (val feedback : feedbacksOfScreeningSurvey) {
			feedback.setGlobalUniqueId(GlobalUniqueIdGenerator
					.createGlobalUniqueId());

			databaseManagerService.saveModelObject(importedScreeningSurvey);
		}
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

	public void screeningSurveyChangePassword(
			final ScreeningSurvey screeningSurvey, final String newPassword) {
		screeningSurvey.setPassword(newPassword);

		databaseManagerService.saveModelObject(screeningSurvey);
	}

	public void screeningSurveyChangeTemplatePath(
			final ScreeningSurvey screeningSurvey, final String newTemplatePath) {
		screeningSurvey.setTemplatePath(newTemplatePath);

		databaseManagerService.saveModelObject(screeningSurvey);
	}

	public ScreeningSurvey screeningSurveyImport(final File file,
			final ObjectId interventionId) throws FileNotFoundException,
			IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormats.SCREENING_SURVEY);

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof ScreeningSurvey) {
				val screeningSurvey = (ScreeningSurvey) modelObject;

				screeningSurvey.setIntervention(interventionId);

				val dateFormat = DateFormat.getDateTimeInstance(
						DateFormat.MEDIUM, DateFormat.MEDIUM,
						Constants.getAdminLocale());
				val date = dateFormat.format(Calendar.getInstance().getTime());
				screeningSurvey.setName(screeningSurvey.getName() + " (" + date
						+ ")");

				databaseManagerService.saveModelObject(screeningSurvey);

				return screeningSurvey;
			}
		}

		return null;
	}

	public File screeningSurveyExport(final ScreeningSurvey screeningSurvey) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		log.debug("Recursively collect all model objects related to the screening survey");
		screeningSurvey
				.collectThisAndRelatedModelObjectsForExport(modelObjectsToExport);

		log.debug("Export screening survey");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormats.SCREENING_SURVEY);
	}

	public void screeningSurveySetActive(final ScreeningSurvey screeningSurvey,
			final boolean newValue) {
		screeningSurvey.setActive(newValue);

		databaseManagerService.saveModelObject(screeningSurvey);
	}

	public void screeningSurveyDelete(
			final ScreeningSurvey screeningSurveyToDelete)
			throws NotificationMessageException {

		databaseManagerService.deleteModelObject(screeningSurveyToDelete);
	}

	// Screening Survey Slide
	public ScreeningSurveySlide screeningSurveySlideCreate(
			final ObjectId screeningSurveyId) {
		val screeningSurveySlide = new ScreeningSurveySlide(screeningSurveyId,
				0, "", "", null, "",
				ScreeningSurveySlideQuestionTypes.TEXT_ONLY, false, null,
				new String[0], new String[0], -1, null);

		val highestOrderSlide = databaseManagerService
				.findOneSortedModelObject(ScreeningSurveySlide.class,
						Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY,
						Queries.SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_DESC,
						screeningSurveyId);

		if (highestOrderSlide != null) {
			screeningSurveySlide.setOrder(highestOrderSlide.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(screeningSurveySlide);

		return screeningSurveySlide;
	}

	public void screeningSurveySlideChangeTitle(
			final ScreeningSurveySlide screeningSurveySlide,
			final String textWithPlaceholders,
			final List<String> allPossibleMessageVariables)
			throws NotificationMessageException {
		if (textWithPlaceholders == null) {
			screeningSurveySlide.setTitleWithPlaceholders("");
		} else {
			if (!StringValidator.isValidVariableText(textWithPlaceholders,
					allPossibleMessageVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			screeningSurveySlide.setTitleWithPlaceholders(textWithPlaceholders);
		}

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	public void screeningSurveySlideChangeQuestion(
			final ScreeningSurveySlide screeningSurveySlide,
			final String textWithPlaceholders,
			final List<String> allPossibleMessageVariables)
			throws NotificationMessageException {
		if (textWithPlaceholders == null) {
			screeningSurveySlide.setQuestionWithPlaceholders("");
		} else {
			if (!StringValidator.isValidVariableText(textWithPlaceholders,
					allPossibleMessageVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			screeningSurveySlide
					.setQuestionWithPlaceholders(textWithPlaceholders);
		}

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	public void screeningSurveySlideChangeQuestionType(
			final ScreeningSurveySlide screeningSurveySlide,
			final ScreeningSurveySlideQuestionTypes screeningSurveySlideQuestionType) {
		screeningSurveySlide.setQuestionType(screeningSurveySlideQuestionType);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	public void screeningSurveySlideChangeOptionalLayoutAttributeWithPlaceholders(
			final ScreeningSurveySlide screeningSurveySlide,
			final String optionalLayoutAttributeWithPlaceholders) {
		screeningSurveySlide
				.setOptionalLayoutAttributeWithPlaceholders(optionalLayoutAttributeWithPlaceholders);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	public void screeningSurveySlideChangePreselectedAnswer(
			final ScreeningSurveySlide screeningSurveySlide,
			final int preselectedAnswer) {
		screeningSurveySlide.setPreSelectedAnswer(preselectedAnswer);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	public void screeningSurveySlideChangeStoreResultToVariable(
			final ScreeningSurveySlide screeningSurveySlide,
			final String variableName) throws NotificationMessageException {
		if (variableName == null || variableName.equals("")) {
			screeningSurveySlide.setStoreValueToVariableWithName(null);

			databaseManagerService.saveModelObject(screeningSurveySlide);
		} else {
			if (!StringValidator.isValidVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
			}

			if (variablesManagerService
					.isWriteProtectedVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
			}

			screeningSurveySlide.setStoreValueToVariableWithName(variableName);

			databaseManagerService.saveModelObject(screeningSurveySlide);
		}
	}

	public void screeningSurveySlideChangeHandsOverToFeedback(
			final ScreeningSurveySlide screeningSurveySlide,
			final ObjectId feedbackId) {
		screeningSurveySlide.setHandsOverToFeedback(feedbackId);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	public void screeningSurveySlideChangeStopScreeningSurvey(
			final ScreeningSurveySlide screeningSurveySlide,
			final boolean newValue) {
		screeningSurveySlide.setLastSlide(newValue);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	public void screeningSurveySlideSetAnswersWithPlaceholdersAndValues(
			final ScreeningSurveySlide screeningSurveySlide,
			final String[] answers, final String[] values) {
		screeningSurveySlide.setAnswersWithPlaceholders(answers);
		screeningSurveySlide.setAnswerValues(values);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	public ScreeningSurveySlide screeningSurveySlideMove(
			final ScreeningSurveySlide screeningSurveySlide,
			final boolean moveUp) {
		// Find screening survey slide to swap with
		val screeningSurveySlideToSwapWith = databaseManagerService
				.findOneSortedModelObject(
						ScreeningSurveySlide.class,
						moveUp ? Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY_AND_ORDER_LOWER
								: Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY_AND_ORDER_HIGHER,
						moveUp ? Queries.SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_DESC
								: Queries.SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_ASC,
						screeningSurveySlide.getScreeningSurvey(),
						screeningSurveySlide.getOrder());

		if (screeningSurveySlideToSwapWith == null) {
			return null;
		}

		// Swap order
		final int order = screeningSurveySlide.getOrder();
		screeningSurveySlide
				.setOrder(screeningSurveySlideToSwapWith.getOrder());
		screeningSurveySlideToSwapWith.setOrder(order);

		databaseManagerService.saveModelObject(screeningSurveySlide);
		databaseManagerService.saveModelObject(screeningSurveySlideToSwapWith);

		return screeningSurveySlideToSwapWith;
	}

	public void screeningSurveySlideSetLinkedMediaObject(
			final ScreeningSurveySlide screeningSurveySlide,
			final ObjectId linkedMediaObjectId) {
		screeningSurveySlide.setLinkedMediaObject(linkedMediaObjectId);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	public void screeningSurveySlideDelete(
			final ScreeningSurveySlide screeningSurveySlide) {
		databaseManagerService.deleteModelObject(screeningSurveySlide);
	}

	// Screening Survey Slide Rule
	public ScreeningSurveySlideRule screeningSurveySlideRuleCreate(
			final ObjectId screeningSurveySlideId) {
		val screeningSurveySlideRule = new ScreeningSurveySlideRule(
				screeningSurveySlideId, 0, null, null, "",
				RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "");

		val highestOrderSlideRule = databaseManagerService
				.findOneSortedModelObject(
						ScreeningSurveySlideRule.class,
						Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
						Queries.SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_DESC,
						screeningSurveySlideId);

		if (highestOrderSlideRule != null) {
			screeningSurveySlideRule
					.setOrder(highestOrderSlideRule.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(screeningSurveySlideRule);

		return screeningSurveySlideRule;
	}

	public ScreeningSurveySlideRule screeningSurveySlideRuleMove(
			final ScreeningSurveySlideRule screeningSurveySlideRule,
			final boolean moveUp) {
		// Find screening survey slide rule to swap with
		val screeningSurveySlideRuleToSwapWith = databaseManagerService
				.findOneSortedModelObject(
						ScreeningSurveySlideRule.class,
						moveUp ? Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE_AND_ORDER_LOWER
								: Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE_AND_ORDER_HIGHER,
						moveUp ? Queries.SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_DESC
								: Queries.SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_ASC,
						screeningSurveySlideRule
								.getBelongingScreeningSurveySlide(),
						screeningSurveySlideRule.getOrder());

		if (screeningSurveySlideRuleToSwapWith == null) {
			return null;
		}

		// Swap order
		final int order = screeningSurveySlideRule.getOrder();
		screeningSurveySlideRule.setOrder(screeningSurveySlideRuleToSwapWith
				.getOrder());
		screeningSurveySlideRuleToSwapWith.setOrder(order);

		databaseManagerService.saveModelObject(screeningSurveySlideRule);
		databaseManagerService
				.saveModelObject(screeningSurveySlideRuleToSwapWith);

		return screeningSurveySlideRuleToSwapWith;
	}

	public void screeningSurveySlideRuleSetJumpToSlide(
			final ScreeningSurveySlideRule screeningSurveySlideRule,
			final boolean isTrueCase,
			final ObjectId selectedScreeningSurveySlideId) {
		if (isTrueCase) {
			screeningSurveySlideRule
					.setNextScreeningSurveySlideWhenTrue(selectedScreeningSurveySlideId);
		} else {
			screeningSurveySlideRule
					.setNextScreeningSurveySlideWhenFalse(selectedScreeningSurveySlideId);
		}

		databaseManagerService.saveModelObject(screeningSurveySlideRule);
	}

	public void screeningSurveySlideRuleDelete(
			final ScreeningSurveySlideRule screeningSurveySlideRule) {
		databaseManagerService.deleteModelObject(screeningSurveySlideRule);
	}

	// Feedback Slide
	public FeedbackSlide feedbackSlideCreate(final ObjectId feedbackId) {
		val feedbackSlide = new FeedbackSlide(feedbackId, 0, "", "", null, "");

		val highestOrderSlide = databaseManagerService
				.findOneSortedModelObject(FeedbackSlide.class,
						Queries.FEEDBACK_SLIDE__BY_FEEDBACK,
						Queries.FEEDBACK_SLIDE__SORT_BY_ORDER_DESC, feedbackId);

		if (highestOrderSlide != null) {
			feedbackSlide.setOrder(highestOrderSlide.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(feedbackSlide);

		return feedbackSlide;
	}

	public void feedbackSlideChangeTitle(final FeedbackSlide feedbackSlide,
			final String textWithPlaceholders,
			final List<String> allPossibleFeedbackVariables)
			throws NotificationMessageException {
		if (textWithPlaceholders == null) {
			feedbackSlide.setTitleWithPlaceholders("");
		} else {
			if (!StringValidator.isValidVariableText(textWithPlaceholders,
					allPossibleFeedbackVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			feedbackSlide.setTitleWithPlaceholders(textWithPlaceholders);
		}

		databaseManagerService.saveModelObject(feedbackSlide);
	}

	public void feedbackSlideChangeTextWithPlaceholders(
			final FeedbackSlide feedbackSlide,
			final String textWithPlaceholders,
			final List<String> allPossibleFeedbackVariables)
			throws NotificationMessageException {
		if (textWithPlaceholders == null) {
			feedbackSlide.setTitleWithPlaceholders("");
		} else {
			if (!StringValidator.isValidVariableText(textWithPlaceholders,
					allPossibleFeedbackVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			feedbackSlide.setTextWithPlaceholders(textWithPlaceholders);
		}

		databaseManagerService.saveModelObject(feedbackSlide);
	}

	public void feedbackSlideChangeOptionalLayoutAttribute(
			final FeedbackSlide feedbackSlide,
			final String optionalLayoutAttributes) {
		feedbackSlide.setOptionalLayoutAttribute(optionalLayoutAttributes);

		databaseManagerService.saveModelObject(feedbackSlide);
	}

	public FeedbackSlide feedbackSlideMove(final FeedbackSlide feedbackSlide,
			final boolean moveUp) {
		// Find feedback slide to swap with
		val feedbackSlideToSwapWith = databaseManagerService
				.findOneSortedModelObject(
						FeedbackSlide.class,
						moveUp ? Queries.FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_LOWER
								: Queries.FEEDBACK_SLIDE__BY_FEEDBACK_AND_ORDER_HIGHER,
						moveUp ? Queries.FEEDBACK_SLIDE__SORT_BY_ORDER_DESC
								: Queries.FEEDBACK_SLIDE__SORT_BY_ORDER_ASC,
						feedbackSlide.getFeedback(), feedbackSlide.getOrder());

		if (feedbackSlideToSwapWith == null) {
			return null;
		}

		// Swap order
		final int order = feedbackSlide.getOrder();
		feedbackSlide.setOrder(feedbackSlideToSwapWith.getOrder());
		feedbackSlideToSwapWith.setOrder(order);

		databaseManagerService.saveModelObject(feedbackSlide);
		databaseManagerService.saveModelObject(feedbackSlideToSwapWith);

		return feedbackSlideToSwapWith;
	}

	public void feedbackSlideSetLinkedMediaObject(
			final ScreeningSurveySlide screeningSurveySlide,
			final ObjectId linkedMediaObjectId) {
		screeningSurveySlide.setLinkedMediaObject(linkedMediaObjectId);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	public void feedbackSlideDelete(final FeedbackSlide feedbackSlide) {
		databaseManagerService.deleteModelObject(feedbackSlide);
	}

	// Feedback Slide Rule
	public FeedbackSlideRule feedbackSlideRuleCreate(
			final ObjectId feedbackSlideId) {
		val feedbackSlideRule = new FeedbackSlideRule(feedbackSlideId, 0, "",
				RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "");

		val highestOrderSlideRule = databaseManagerService
				.findOneSortedModelObject(FeedbackSlideRule.class,
						Queries.FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE,
						Queries.FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_DESC,
						feedbackSlideId);

		if (highestOrderSlideRule != null) {
			feedbackSlideRule.setOrder(highestOrderSlideRule.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(feedbackSlideRule);

		return feedbackSlideRule;
	}

	public FeedbackSlideRule feedbackSlideRuleMove(
			final FeedbackSlideRule feedbackSlideRule, final boolean moveUp) {
		// Find feedback slide rule to swap with
		val feedbackSlideRuleToSwapWith = databaseManagerService
				.findOneSortedModelObject(
						FeedbackSlideRule.class,
						moveUp ? Queries.FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE_AND_ORDER_LOWER
								: Queries.FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE_AND_ORDER_HIGHER,
						moveUp ? Queries.FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_DESC
								: Queries.FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_ASC,
						feedbackSlideRule.getBelongingFeedbackSlide(),
						feedbackSlideRule.getOrder());

		if (feedbackSlideRuleToSwapWith == null) {
			return null;
		}

		// Swap order
		final int order = feedbackSlideRule.getOrder();
		feedbackSlideRule.setOrder(feedbackSlideRuleToSwapWith.getOrder());
		feedbackSlideRuleToSwapWith.setOrder(order);

		databaseManagerService.saveModelObject(feedbackSlideRule);
		databaseManagerService.saveModelObject(feedbackSlideRuleToSwapWith);

		return feedbackSlideRuleToSwapWith;
	}

	public void feedbackSlideSetLinkedMediaObject(
			final FeedbackSlide feedbackSlide,
			final ObjectId linkedMediaObjectId) {
		feedbackSlide.setLinkedMediaObject(linkedMediaObjectId);

		databaseManagerService.saveModelObject(feedbackSlide);
	}

	public void feedbackSlideRuleDelete(
			final FeedbackSlideRule feedbackSlideRule) {
		databaseManagerService.deleteModelObject(feedbackSlideRule);
	}

	// Feedback
	public Feedback feedbackCreate(final String name,
			final ObjectId screeningSurveyId) {
		val feedback = new Feedback(
				GlobalUniqueIdGenerator.createGlobalUniqueId(),
				screeningSurveyId, name, "");

		if (name.equals("")) {
			feedback.setName(DEFAULT_OBJECT_NAME);
		}

		databaseManagerService.saveModelObject(feedback);

		return feedback;
	}

	public void feedbackChangeName(final Feedback feedback, final String newName) {
		if (newName.equals("")) {
			feedback.setName(DEFAULT_OBJECT_NAME);
		} else {
			feedback.setName(newName);
		}

		databaseManagerService.saveModelObject(feedback);
	}

	public void feedbackChangeTemplatePath(final Feedback feedback,
			final String newTemplatePath) {
		feedback.setTemplatePath(newTemplatePath);

		databaseManagerService.saveModelObject(feedback);
	}

	public void feedbackDelete(final Feedback feedback) {
		databaseManagerService.deleteModelObject(feedback);
	}

	/*
	 * Special methods
	 */
	public boolean isOneScreeningSurveyOfInterventionActive(
			final ObjectId interventionId) {

		val activeScreeningSurveys = databaseManagerService.findModelObjects(
				ScreeningSurvey.class,
				Queries.SCREENING_SURVEY__BY_INTERVENTION_AND_ACTIVE_TRUE,
				interventionId);

		if (activeScreeningSurveys.iterator().hasNext()) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Getter methods
	 */
	public Iterable<ScreeningSurvey> getAllScreeningSurveysOfIntervention(
			final ObjectId objectId) {
		return databaseManagerService.findModelObjects(ScreeningSurvey.class,
				Queries.SCREENING_SURVEY__BY_INTERVENTION, objectId);
	}

	public Iterable<ScreeningSurveySlide> getAllScreeningSurveySlidesOfScreeningSurvey(
			final ObjectId screeningSurveyId) {
		return databaseManagerService.findSortedModelObjects(
				ScreeningSurveySlide.class,
				Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY,
				Queries.SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_ASC,
				screeningSurveyId);
	}

	public Iterable<ScreeningSurveySlideRule> getAllScreeningSurveySlideRulesOfScreeningSurveySlide(
			final ObjectId screeningSurveySlideId) {
		return databaseManagerService.findSortedModelObjects(
				ScreeningSurveySlideRule.class,
				Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
				Queries.SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_ASC,
				screeningSurveySlideId);
	}

	public Iterable<Feedback> getAllFeedbacksOfScreeningSurvey(
			final ObjectId screeningSurveyId) {
		return databaseManagerService.findSortedModelObjects(Feedback.class,
				Queries.FEEDBACK__BY_SCREENING_SURVEY,
				Queries.FEEDBACK__SORT_BY_ORDER_ASC, screeningSurveyId);
	}

	public Iterable<FeedbackSlide> getAllFeedbackSlidesOfFeedback(
			final ObjectId feedbackId) {
		return databaseManagerService.findSortedModelObjects(
				FeedbackSlide.class, Queries.FEEDBACK_SLIDE__BY_FEEDBACK,
				Queries.FEEDBACK_SLIDE__SORT_BY_ORDER_ASC, feedbackId);
	}

	public Iterable<FeedbackSlideRule> getAllFeedbackSlideRulesOfFeedbackSlide(
			final ObjectId feedbackSlideId) {
		return databaseManagerService
				.findSortedModelObjects(FeedbackSlideRule.class,
						Queries.FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE,
						Queries.FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_ASC,
						feedbackSlideId);
	}

	public String[] getAllTemplatePaths() {
		final File[] templateFolder = fileStorageManagerService
				.getTemplatesFolder().listFiles(new FileFilter() {

					@Override
					public boolean accept(final File pathname) {
						if (pathname.isDirectory()) {
							return true;
						} else {
							return false;
						}
					}
				});

		final String[] templateFolderStrings = new String[templateFolder.length];

		for (int i = 0; i < templateFolder.length; i++) {
			templateFolderStrings[i] = templateFolder[i].getName();
		}

		return templateFolderStrings;
	}

	public List<String> getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
			final ObjectId screeningSurveyId) {
		val variables = new ArrayList<String>();

		variables.addAll(variablesManagerService.getAllSystemVariableNames());

		val screeningSurvey = databaseManagerService.getModelObjectById(
				ScreeningSurvey.class, screeningSurveyId);

		variables.addAll(variablesManagerService
				.getAllInterventionVariableNamesOfIntervention(screeningSurvey
						.getIntervention()));
		variables
				.addAll(variablesManagerService
						.getAllScreeningSurveyVariableNamesOfScreeningSurvey(screeningSurveyId));

		Collections.sort(variables);

		return variables;
	}
}
