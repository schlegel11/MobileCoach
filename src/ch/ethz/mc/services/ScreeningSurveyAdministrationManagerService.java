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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.Feedback;
import ch.ethz.mc.model.persistent.FeedbackSlide;
import ch.ethz.mc.model.persistent.FeedbackSlideRule;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.ScreeningSurveySlideRule;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.model.persistent.types.ScreeningSurveySlideQuestionTypes;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.services.internal.ModelObjectExchangeService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.services.types.ModelObjectExchangeFormatTypes;
import ch.ethz.mc.tools.GlobalUniqueIdGenerator;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.StringValidator;
import ch.ethz.mc.ui.NotificationMessageException;

@Log4j2
/**
 * Cares for the creation of the {@link ScreeningSurvey}s and {@link Feedback}s
 * as well as all related {@link ModelObject}s
 *
 * @author Andreas Filler
 */
public class ScreeningSurveyAdministrationManagerService {
	private final Object										$lock;

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
		$lock = MC.getInstance();

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
	@Synchronized
	public ScreeningSurvey screeningSurveyCreate(final String name,
			final ObjectId interventionId) {
		val screeningSurvey = new ScreeningSurvey(
				GlobalUniqueIdGenerator.createGlobalUniqueId(), interventionId,
				name, "", null, false, false);

		if (name.equals("")) {
			screeningSurvey.setName(DEFAULT_OBJECT_NAME);
		}

		databaseManagerService.saveModelObject(screeningSurvey);

		return screeningSurvey;
	}

	@Synchronized
	protected ScreeningSurvey screeningSurveyRecreateGlobalUniqueId(
			final ScreeningSurvey screeningSurvey) {
		screeningSurvey.setGlobalUniqueId(GlobalUniqueIdGenerator
				.createGlobalUniqueId());

		val feedbacksOfScreeningSurvey = databaseManagerService
				.findModelObjects(Feedback.class,
						Queries.FEEDBACK__BY_SCREENING_SURVEY,
						screeningSurvey.getId());

		for (val feedback : feedbacksOfScreeningSurvey) {
			feedback.setGlobalUniqueId(GlobalUniqueIdGenerator
					.createGlobalUniqueId());

			databaseManagerService.saveModelObject(feedback);
		}

		val screeningSurveySlidesOfScreeningSurvey = databaseManagerService
				.findModelObjects(ScreeningSurveySlide.class,
						Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY,
						screeningSurvey.getId());

		for (val screeningSurveySlide : screeningSurveySlidesOfScreeningSurvey) {
			screeningSurveySlideRecreateGlobalUniqueId(screeningSurveySlide);
		}

		databaseManagerService.saveModelObject(screeningSurvey);

		return screeningSurvey;
	}

	@Synchronized
	public void screeningSurveyChangeName(
			final ScreeningSurvey screeningSurvey, final String newName) {
		if (newName.equals("")) {
			screeningSurvey.setName(DEFAULT_OBJECT_NAME);
		} else {
			screeningSurvey.setName(newName);
		}

		databaseManagerService.saveModelObject(screeningSurvey);
	}

	@Synchronized
	public void screeningSurveySwitchType(final ScreeningSurvey screeningSurvey)
			throws NotificationMessageException {
		if (!screeningSurvey.isIntermediateSurvey()
				&& getAllFeedbacksOfScreeningSurvey(screeningSurvey.getId())
				.iterator().hasNext()) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_CANT_CHANGE_TYPE);
		} else {
			screeningSurvey.setIntermediateSurvey(!screeningSurvey
					.isIntermediateSurvey());
		}

		databaseManagerService.saveModelObject(screeningSurvey);
	}

	@Synchronized
	public void screeningSurveyChangePassword(
			final ScreeningSurvey screeningSurvey, final String newPassword) {
		screeningSurvey.setPassword(newPassword);

		databaseManagerService.saveModelObject(screeningSurvey);
	}

	@Synchronized
	public void screeningSurveyChangeTemplatePath(
			final ScreeningSurvey screeningSurvey, final String newTemplatePath) {
		screeningSurvey.setTemplatePath(newTemplatePath);

		databaseManagerService.saveModelObject(screeningSurvey);
	}

	@Synchronized
	public ScreeningSurvey screeningSurveyImport(final File file,
			final ObjectId interventionId, final boolean duplicate)
			throws FileNotFoundException, IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file, ModelObjectExchangeFormatTypes.SURVEY);

		ScreeningSurvey importedScreeningSurvey = null;

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof ScreeningSurvey) {
				val screeningSurvey = (ScreeningSurvey) modelObject;
				importedScreeningSurvey = screeningSurvey;

				// Assign intervention
				screeningSurvey.setIntervention(interventionId);

				// Adjust name
				val dateFormat = DateFormat.getDateTimeInstance(
						DateFormat.MEDIUM, DateFormat.MEDIUM,
						Constants.getAdminLocale());
				val date = dateFormat.format(new Date(InternalDateTime
						.currentTimeMillis()));
				screeningSurvey.setName(screeningSurvey.getName() + " (" + date
						+ ")");

				databaseManagerService.saveModelObject(screeningSurvey);
			}
		}

		if (duplicate && importedScreeningSurvey != null) {
			importedScreeningSurvey = screeningSurveyRecreateGlobalUniqueId(importedScreeningSurvey);
		}

		return importedScreeningSurvey;
	}

	@Synchronized
	public File screeningSurveyExport(final ScreeningSurvey screeningSurvey) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		log.debug("Recursively collect all model objects related to the screening survey");
		screeningSurvey
		.collectThisAndRelatedModelObjectsForExport(modelObjectsToExport);

		log.debug("Export screening survey");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport, ModelObjectExchangeFormatTypes.SURVEY);
	}

	@Synchronized
	public void screeningSurveySetActive(final ScreeningSurvey screeningSurvey,
			final boolean newValue) {
		screeningSurvey.setActive(newValue);

		databaseManagerService.saveModelObject(screeningSurvey);
	}

	@Synchronized
	public void screeningSurveyDelete(
			final ScreeningSurvey screeningSurveyToDelete)
					throws NotificationMessageException {

		databaseManagerService.deleteModelObject(screeningSurveyToDelete);
	}

	// Screening Survey Slide
	@Synchronized
	public ScreeningSurveySlide screeningSurveySlideCreate(
			final ObjectId screeningSurveyId) {
		val questions = new ArrayList<ScreeningSurveySlide.Question>();

		val screeningSurveySlide = new ScreeningSurveySlide(
				GlobalUniqueIdGenerator.createGlobalUniqueId(),
				screeningSurveyId, 0, "",
				ScreeningSurveySlideQuestionTypes.TEXT_ONLY, "", questions,
				null, false, null, "");

		val question = new ScreeningSurveySlide.Question("", new String[0],
				new String[0], -1, null, "");

		questions.add(question);

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

	@Synchronized
	public ScreeningSurveySlide.Question screeningSurveySlideAddQuestion(
			final ScreeningSurveySlide screeningSurveySlide) {
		val newQuestion = new ScreeningSurveySlide.Question("", new String[0],
				new String[0], -1, null, "");

		screeningSurveySlide.getQuestions().add(newQuestion);

		databaseManagerService.saveModelObject(screeningSurveySlide);

		return newQuestion;
	}

	@Synchronized
	public void screeningSurveySlideRemoveQuestion(
			final ScreeningSurveySlide screeningSurveySlide,
			final int questionToRemove) throws NotificationMessageException {

		val questions = screeningSurveySlide.getQuestions();

		if (questions.size() > 1) {
			questions.remove(questionToRemove);
		} else {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__CANT_DELETE_LAST_QUESTION);
		}

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	@Synchronized
	private void screeningSurveySlideRecreateGlobalUniqueId(
			final ScreeningSurveySlide screeningSurveySlide) {
		screeningSurveySlide.setGlobalUniqueId(GlobalUniqueIdGenerator
				.createGlobalUniqueId());

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	@Synchronized
	public ScreeningSurveySlide screeningSurveySlideImport(final File file,
			final boolean duplicate) throws FileNotFoundException, IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.SCREENING_SURVEY_SLIDE);

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof ScreeningSurveySlide) {
				val slide = (ScreeningSurveySlide) modelObject;

				if (duplicate) {
					// Recreate global unique ID
					slide.setGlobalUniqueId(GlobalUniqueIdGenerator
							.createGlobalUniqueId());
				}

				// Adjust order
				slide.setOrder(0);

				val highestOrderSlide = databaseManagerService
						.findOneSortedModelObject(
								ScreeningSurveySlide.class,
								Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY,
								Queries.SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_DESC,
								slide.getScreeningSurvey());

				if (highestOrderSlide != null) {
					slide.setOrder(highestOrderSlide.getOrder() + 1);
				}

				databaseManagerService.saveModelObject(slide);

				return slide;
			}
		}

		return null;
	}

	@Synchronized
	public File screeningSurveySlideExport(final ScreeningSurveySlide slide) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		log.debug("Recursively collect all model objects related to the slide");
		slide.collectThisAndRelatedModelObjectsForExport(modelObjectsToExport);

		log.debug("Export slide");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.SCREENING_SURVEY_SLIDE);
	}

	@Synchronized
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

	@Synchronized
	public void screeningSurveySlideChangeQuestionType(
			final ScreeningSurveySlide screeningSurveySlide,
			final ScreeningSurveySlideQuestionTypes screeningSurveySlideQuestionType) {
		screeningSurveySlide.setQuestionType(screeningSurveySlideQuestionType);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	@Synchronized
	public void screeningSurveySlideChangeOptionalLayoutAttributeWithPlaceholders(
			final ScreeningSurveySlide screeningSurveySlide,
			final String optionalLayoutAttributeWithPlaceholders) {
		screeningSurveySlide
		.setOptionalLayoutAttributeWithPlaceholders(optionalLayoutAttributeWithPlaceholders);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	@Synchronized
	public void screeningSurveySlideChangeQuestion(
			final ScreeningSurveySlide screeningSurveySlide,
			final int questionPosition, final String textWithPlaceholders,
			final List<String> allPossibleMessageVariables)
					throws NotificationMessageException {
		val question = screeningSurveySlide.getQuestions()
				.get(questionPosition);

		if (textWithPlaceholders == null) {
			question.setQuestionWithPlaceholders("");
		} else {
			if (!StringValidator.isValidVariableText(textWithPlaceholders,
					allPossibleMessageVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			question.setQuestionWithPlaceholders(textWithPlaceholders);
		}

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	public void screeningSurveySlideChangePreselectedAnswer(
			final ScreeningSurveySlide screeningSurveySlide,
			final int questionPosition, final int preselectedAnswer) {
		val question = screeningSurveySlide.getQuestions()
				.get(questionPosition);

		question.setPreSelectedAnswer(preselectedAnswer);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	@Synchronized
	public void screeningSurveySlideChangeDefaultVariableValue(
			final ScreeningSurveySlide screeningSurveySlide,
			final int questionPosition, final String text)
					throws NotificationMessageException {
		val question = screeningSurveySlide.getQuestions()
				.get(questionPosition);

		if (text == null) {
			question.setDefaultValue("");
		} else {
			question.setDefaultValue(text);
		}

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	@Synchronized
	public void screeningSurveySlideChangeStoreResultToVariable(
			final ScreeningSurveySlide screeningSurveySlide,
			final int questionPosition, final String variableName)
					throws NotificationMessageException {
		val question = screeningSurveySlide.getQuestions()
				.get(questionPosition);

		if (variableName == null || variableName.equals("")) {
			question.setStoreValueToVariableWithName(null);

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

			question.setStoreValueToVariableWithName(variableName);

			databaseManagerService.saveModelObject(screeningSurveySlide);
		}
	}

	@Synchronized
	public void screeningSurveySlideSetAnswersWithPlaceholdersAndValues(
			final ScreeningSurveySlide screeningSurveySlide,
			final int questionPosition, final String[] answers,
			final String[] values) {
		val question = screeningSurveySlide.getQuestions()
				.get(questionPosition);

		question.setAnswersWithPlaceholders(answers);
		question.setAnswerValues(values);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	@Synchronized
	public void screeningSurveySlideChangeValidationErrorMessage(
			final ScreeningSurveySlide screeningSurveySlide,
			final String textWithPlaceholders,
			final List<String> allPossibleMessageVariables)
					throws NotificationMessageException {
		if (textWithPlaceholders == null) {
			screeningSurveySlide.setValidationErrorMessage("");
		} else {
			if (!StringValidator.isValidVariableText(textWithPlaceholders,
					allPossibleMessageVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			screeningSurveySlide
			.setValidationErrorMessage(textWithPlaceholders);
		}

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	@Synchronized
	public void screeningSurveySlideChangeHandsOverToFeedback(
			final ScreeningSurveySlide screeningSurveySlide,
			final ObjectId feedbackId) {
		screeningSurveySlide.setHandsOverToFeedback(feedbackId);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	@Synchronized
	public void screeningSurveySlideChangeStopScreeningSurvey(
			final ScreeningSurveySlide screeningSurveySlide,
			final boolean newValue) {
		screeningSurveySlide.setLastSlide(newValue);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	@Synchronized
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

	@Synchronized
	public void screeningSurveySlideSetLinkedMediaObject(
			final ScreeningSurveySlide screeningSurveySlide,
			final ObjectId linkedMediaObjectId) {
		screeningSurveySlide.setLinkedMediaObject(linkedMediaObjectId);

		databaseManagerService.saveModelObject(screeningSurveySlide);
	}

	@Synchronized
	public void screeningSurveySlideDelete(
			final ScreeningSurveySlide screeningSurveySlide)
					throws NotificationMessageException {
		val otherScreeningSurveySlides = databaseManagerService
				.findModelObjects(ScreeningSurveySlide.class,
						Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY,
						screeningSurveySlide.getScreeningSurvey());

		for (val otherScreeningSurveySlide : otherScreeningSurveySlides) {
			if (otherScreeningSurveySlide.getId().equals(
					screeningSurveySlide.getId())) {
				continue;
			}

			val screeningSurveySlidesWhenTrue = databaseManagerService
					.findModelObjects(
							ScreeningSurveySlideRule.class,
							Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE_AND_NEXT_SCREENING_SURVEY_SLIDE_WHEN_TRUE,
							otherScreeningSurveySlide.getId(),
							screeningSurveySlide.getId());

			if (screeningSurveySlidesWhenTrue.iterator().hasNext()) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_HAS_BACKLINKS);
			}
			val screeningSurveySlidesWhenFalse = databaseManagerService
					.findModelObjects(
							ScreeningSurveySlideRule.class,
							Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE_AND_NEXT_SCREENING_SURVEY_SLIDE_WHEN_FALSE,
							otherScreeningSurveySlide.getId(),
							screeningSurveySlide.getId());

			if (screeningSurveySlidesWhenFalse.iterator().hasNext()) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__SCREENING_SURVEY_SLIDE_HAS_BACKLINKS);
			}
		}

		databaseManagerService.deleteModelObject(screeningSurveySlide);
	}

	// Screening Survey Slide Rule
	@Synchronized
	public ScreeningSurveySlideRule screeningSurveySlideRuleCreate(
			final ObjectId screeningSurveySlideId) {
		val screeningSurveySlideRule = new ScreeningSurveySlideRule(
				screeningSurveySlideId, 0, 0, "", null, null, null, false, "",
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

	@Synchronized
	public void screeningSurveySlideRuleChangeValueToStoreToVariable(
			final ScreeningSurveySlideRule screeningSurveySlideRule,
			final String variableValue) throws NotificationMessageException {
		if (variableValue == null) {
			screeningSurveySlideRule.setValueToStoreToVariable("");

			databaseManagerService.saveModelObject(screeningSurveySlideRule);
		} else {
			screeningSurveySlideRule.setValueToStoreToVariable(variableValue);

			databaseManagerService.saveModelObject(screeningSurveySlideRule);
		}
	}

	@Synchronized
	public void screeningSurveySlideRuleChangeLevel(
			final ScreeningSurveySlideRule screeningSurveySlideRule,
			final boolean up) {
		if (up) {
			screeningSurveySlideRule.setLevel(screeningSurveySlideRule
					.getLevel() + 1);
		} else if (screeningSurveySlideRule.getLevel() > 0) {
			screeningSurveySlideRule.setLevel(screeningSurveySlideRule
					.getLevel() - 1);
		}

		databaseManagerService.saveModelObject(screeningSurveySlideRule);
	}

	@Synchronized
	public void screeningSurveySlideRuleChangeVariableToStoreValueTo(
			final ScreeningSurveySlideRule screeningSurveySlideRule,
			final String variableName) throws NotificationMessageException {
		if (variableName == null || variableName.equals("")) {
			screeningSurveySlideRule.setStoreValueToVariableWithName(null);

			databaseManagerService.saveModelObject(screeningSurveySlideRule);
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

			screeningSurveySlideRule
			.setStoreValueToVariableWithName(variableName);

			databaseManagerService.saveModelObject(screeningSurveySlideRule);
		}
	}

	@Synchronized
	public void screeningSurveySlideRuleChangeShowSameSlideBecauseValueNotValidWhenTrue(
			final ScreeningSurveySlideRule screeningSurveySlideRule,
			final boolean newValue) {
		screeningSurveySlideRule
		.setShowSameSlideBecauseValueNotValidWhenTrue(newValue);

		if (newValue) {
			screeningSurveySlideRule.setNextScreeningSurveySlideWhenTrue(null);
			screeningSurveySlideRule.setNextScreeningSurveySlideWhenFalse(null);
		}

		databaseManagerService.saveModelObject(screeningSurveySlideRule);
	}

	@Synchronized
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

	@Synchronized
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
	@Synchronized
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

	@Synchronized
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

	@Synchronized
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

	@Synchronized
	public void feedbackSlideChangeOptionalLayoutAttributeWithPlaceholders(
			final FeedbackSlide feedbackSlide,
			final String optionalLayoutAttributeWithPlaceholders) {
		feedbackSlide
		.setOptionalLayoutAttributeWithPlaceholders(optionalLayoutAttributeWithPlaceholders);

		databaseManagerService.saveModelObject(feedbackSlide);
	}

	@Synchronized
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

	@Synchronized
	public void feedbackSlideSetLinkedMediaObject(
			final FeedbackSlide feedbackSlide,
			final ObjectId linkedMediaObjectId) {
		feedbackSlide.setLinkedMediaObject(linkedMediaObjectId);

		databaseManagerService.saveModelObject(feedbackSlide);
	}

	@Synchronized
	public void feedbackSlideDelete(final FeedbackSlide feedbackSlide) {
		databaseManagerService.deleteModelObject(feedbackSlide);
	}

	// Feedback Slide Rule
	@Synchronized
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

	@Synchronized
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

	@Synchronized
	public void feedbackSlideRuleDelete(
			final FeedbackSlideRule feedbackSlideRule) {
		databaseManagerService.deleteModelObject(feedbackSlideRule);
	}

	// Feedback
	@Synchronized
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

	@Synchronized
	public FeedbackSlide feedbackSlideImport(final File file,
			final boolean duplicate) throws FileNotFoundException, IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.FEEDBACK_SLIDE);

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof FeedbackSlide) {
				val slide = (FeedbackSlide) modelObject;

				slide.setOrder(0);

				val highestOrderSlide = databaseManagerService
						.findOneSortedModelObject(FeedbackSlide.class,
								Queries.FEEDBACK_SLIDE__BY_FEEDBACK,
								Queries.FEEDBACK_SLIDE__SORT_BY_ORDER_DESC,
								slide.getFeedback());

				if (highestOrderSlide != null) {
					slide.setOrder(highestOrderSlide.getOrder() + 1);
				}

				databaseManagerService.saveModelObject(slide);

				return slide;
			}
		}

		return null;
	}

	@Synchronized
	public File feedbackSlideExport(final FeedbackSlide slide) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		log.debug("Recursively collect all model objects related to the slide");
		slide.collectThisAndRelatedModelObjectsForExport(modelObjectsToExport);

		log.debug("Export slide");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.FEEDBACK_SLIDE);
	}

	@Synchronized
	public void feedbackChangeName(final Feedback feedback, final String newName) {
		if (newName.equals("")) {
			feedback.setName(DEFAULT_OBJECT_NAME);
		} else {
			feedback.setName(newName);
		}

		databaseManagerService.saveModelObject(feedback);
	}

	@Synchronized
	public void feedbackChangeTemplatePath(final Feedback feedback,
			final String newTemplatePath) {
		feedback.setTemplatePath(newTemplatePath);

		databaseManagerService.saveModelObject(feedback);
	}

	@Synchronized
	public void feedbackDelete(final Feedback feedback) {
		databaseManagerService.deleteModelObject(feedback);
	}

	/*
	 * Special methods
	 */
	@Synchronized
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
	@Synchronized
	public Iterable<ScreeningSurvey> getAllScreeningSurveysOfIntervention(
			final ObjectId objectId) {
		return databaseManagerService.findModelObjects(ScreeningSurvey.class,
				Queries.SCREENING_SURVEY__BY_INTERVENTION, objectId);
	}

	@Synchronized
	public Iterable<ScreeningSurveySlide> getAllScreeningSurveySlidesOfScreeningSurvey(
			final ObjectId screeningSurveyId) {
		return databaseManagerService.findSortedModelObjects(
				ScreeningSurveySlide.class,
				Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY,
				Queries.SCREENING_SURVEY_SLIDE__SORT_BY_ORDER_ASC,
				screeningSurveyId);
	}

	@Synchronized
	public Iterable<ScreeningSurveySlideRule> getAllScreeningSurveySlideRulesOfScreeningSurveySlide(
			final ObjectId screeningSurveySlideId) {
		return databaseManagerService.findSortedModelObjects(
				ScreeningSurveySlideRule.class,
				Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
				Queries.SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_ASC,
				screeningSurveySlideId);
	}

	@Synchronized
	public Iterable<Feedback> getAllFeedbacksOfScreeningSurvey(
			final ObjectId screeningSurveyId) {
		return databaseManagerService.findSortedModelObjects(Feedback.class,
				Queries.FEEDBACK__BY_SCREENING_SURVEY,
				Queries.FEEDBACK__SORT_BY_ORDER_ASC, screeningSurveyId);
	}

	@Synchronized
	public Iterable<FeedbackSlide> getAllFeedbackSlidesOfFeedback(
			final ObjectId feedbackId) {
		return databaseManagerService.findSortedModelObjects(
				FeedbackSlide.class, Queries.FEEDBACK_SLIDE__BY_FEEDBACK,
				Queries.FEEDBACK_SLIDE__SORT_BY_ORDER_ASC, feedbackId);
	}

	@Synchronized
	public Iterable<FeedbackSlideRule> getAllFeedbackSlideRulesOfFeedbackSlide(
			final ObjectId feedbackSlideId) {
		return databaseManagerService
				.findSortedModelObjects(FeedbackSlideRule.class,
						Queries.FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE,
						Queries.FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_ASC,
						feedbackSlideId);
	}

	@Synchronized
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

	@Synchronized
	public List<String> getAllPossibleScreenigSurveyVariablesOfScreeningSurvey(
			final ObjectId screeningSurveyId) {
		val variables = new ArrayList<String>();

		variables.addAll(variablesManagerService
				.getAllSystemVariableNamesRelevantForSlides());

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

	@Synchronized
	public List<String> getAllWritableScreenigSurveyVariablesOfScreeningSurvey(
			final ObjectId screeningSurveyId) {
		val variables = new ArrayList<String>();

		variables.addAll(variablesManagerService
				.getAllWritableSystemVariableNames());

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

	@Synchronized
	public ScreeningSurveySlide getScreeningSurveySlide(final ObjectId objectId) {
		return databaseManagerService.getModelObjectById(
				ScreeningSurveySlide.class, objectId);
	}
}
