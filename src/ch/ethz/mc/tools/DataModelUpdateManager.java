package ch.ethz.mc.tools;

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
import java.util.ArrayList;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.IteratorUtils;
import org.jongo.Jongo;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.consistency.DataModelConfiguration;
import ch.ethz.mc.model.persistent.outdated.ScreeningSurveySlideV1;
import ch.ethz.mc.model.persistent.outdated.ScreeningSurveySlideV2;

/**
 * Manages the modification of the Data Model on the startup of the system
 * 
 * @author Andreas Filler
 */
@Log4j2
public class DataModelUpdateManager {

	private static Jongo	jongo;

	public static void updateDataFromVersionToVersion(final int currentVersion,
			final int versionToBeReached, final Jongo jongo) {
		DataModelUpdateManager.jongo = jongo;

		for (int i = currentVersion; i < versionToBeReached; i++) {
			val updateToVersionInThisStep = i + 1;

			log.info("Updating data model to version {}...",
					updateToVersionInThisStep);

			switch (updateToVersionInThisStep) {
				case 0:
					// First DB setup
					createVersion0();
					break;
				case 1:
					updateToVersion1();
					break;
				case 2:
					updateToVersion2();
					break;
			}

			log.info("Update to version {} done", updateToVersionInThisStep);

			// set new version
			val configurationCollection = jongo
					.getCollection(Constants.DATA_MODEL_CONFIGURATION);
			val configuration = configurationCollection.findOne(
					Queries.EVERYTHING).as(DataModelConfiguration.class);
			configuration.setVersion(updateToVersionInThisStep);
			configurationCollection.save(configuration);
		}
	}

	/**
	 * First DB setup
	 */
	private static void createVersion0() {
		val configuration = new DataModelConfiguration();
		configuration.setVersion(0);

		val configurationCollection = jongo
				.getCollection(Constants.DATA_MODEL_CONFIGURATION);
		configurationCollection.save(configuration);
	}

	/**
	 * Changes for version 1:
	 */
	private static void updateToVersion1() {
		val interventionCollection = jongo.getCollection("Intervention");
		interventionCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_1__INTERVENTION__CHANGE_1);

		val dialogStatusCollection = jongo.getCollection(DialogStatus.class
				.getSimpleName());
		dialogStatusCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_1__DIALOG_STATUS__CHANGE_1);
		dialogStatusCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_1__DIALOG_STATUS__CHANGE_2);
		dialogStatusCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_1__DIALOG_STATUS__CHANGE_3);
		dialogStatusCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_1__DIALOG_STATUS__CHANGE_4);
		dialogStatusCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_1__DIALOG_STATUS__CHANGE_5);

		val screeningSurveySlideCollection = jongo
				.getCollection("ScreeningSurveySlide");
		screeningSurveySlideCollection
				.update(Queries.EVERYTHING)
				.multi()
				.with(Queries.UPDATE_VERSION_1__SCREENING_SURVEY_SLIDE__CHANGE_1);
		screeningSurveySlideCollection
				.update(Queries.EVERYTHING)
				.multi()
				.with(Queries.UPDATE_VERSION_1__SCREENING_SURVEY_SLIDE__CHANGE_2);
		screeningSurveySlideCollection
				.update(Queries.EVERYTHING)
				.multi()
				.with(Queries.UPDATE_VERSION_1__SCREENING_SURVEY_SLIDE__CHANGE_3);

		val screeningSurveySlides = screeningSurveySlideCollection.find(
				Queries.EVERYTHING).as(ScreeningSurveySlide.class);
		for (val screeningSurveySlide : screeningSurveySlides) {
			screeningSurveySlide.setGlobalUniqueId(GlobalUniqueIdGenerator
					.createGlobalUniqueId());
			screeningSurveySlideCollection.save(screeningSurveySlide);
		}

		val screeningSurveySlideRuleCollection = jongo
				.getCollection("ScreeningSurveySlideRule");
		screeningSurveySlideRuleCollection
				.update(Queries.EVERYTHING)
				.multi()
				.with(Queries.UPDATE_VERSION_1__SCREENING_SURVEY_SLIDE_RULE__CHANGE_1);
	}

	/**
	 * Changes for version 2:
	 */
	private static void updateToVersion2() {
		val screeningSurveySlideCollection = jongo
				.getCollection("ScreeningSurveySlide");
		val oldScreeningSurveySlidesIterator = screeningSurveySlideCollection
				.find(Queries.EVERYTHING)
				.as(ch.ethz.mc.model.persistent.outdated.ScreeningSurveySlideV1.class)
				.iterator();

		final ScreeningSurveySlideV1[] oldScreeningSurveySlides = (ScreeningSurveySlideV1[]) IteratorUtils
				.toArray(oldScreeningSurveySlidesIterator,
						ScreeningSurveySlideV1.class);

		for (val oldScreeningSurveySlide : oldScreeningSurveySlides) {
			log.debug("Old ScreeningSurveySlide: {}",
					oldScreeningSurveySlide.toJSONString());
			val questions = new ArrayList<ScreeningSurveySlideV2.Question>();

			val newScreeningSurveySlide = new ScreeningSurveySlideV2(
					oldScreeningSurveySlide.getId(),
					oldScreeningSurveySlide.getGlobalUniqueId(),
					oldScreeningSurveySlide.getScreeningSurvey(),
					oldScreeningSurveySlide.getOrder(),
					oldScreeningSurveySlide.getTitleWithPlaceholders(),
					oldScreeningSurveySlide.getQuestionType(),
					oldScreeningSurveySlide
							.getOptionalLayoutAttributeWithPlaceholders(),
					questions, oldScreeningSurveySlide.getLinkedMediaObject(),
					oldScreeningSurveySlide.isLastSlide(),
					oldScreeningSurveySlide.getHandsOverToFeedback(),
					oldScreeningSurveySlide.getValidationErrorMessage());

			val question = new ScreeningSurveySlideV2.Question(
					oldScreeningSurveySlide.getQuestionWithPlaceholders(),
					oldScreeningSurveySlide.getAnswersWithPlaceholders(),
					oldScreeningSurveySlide.getAnswerValues(),
					oldScreeningSurveySlide.getPreSelectedAnswer(),
					oldScreeningSurveySlide.getStoreValueToVariableWithName(),
					oldScreeningSurveySlide.getDefaultValue());

			questions.add(question);
			log.debug("New ScreeningSurveySlide: {}",
					newScreeningSurveySlide.toJSONString());

			screeningSurveySlideCollection.remove(oldScreeningSurveySlide
					.getId());
			screeningSurveySlideCollection.save(newScreeningSurveySlide);
		}
	}
}
