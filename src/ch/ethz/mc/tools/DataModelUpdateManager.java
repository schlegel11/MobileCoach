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
import java.util.Locale;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.IteratorUtils;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jongo.ResultHandler;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.consistency.DataModelConfiguration;
import ch.ethz.mc.model.persistent.outdated.InterventionVariableWithValueV2;
import ch.ethz.mc.model.persistent.outdated.InterventionVariableWithValueV3;
import ch.ethz.mc.model.persistent.outdated.ParticipantVariableWithValueV2;
import ch.ethz.mc.model.persistent.outdated.ParticipantVariableWithValueV3;
import ch.ethz.mc.model.persistent.outdated.ScreeningSurveySlideV1;
import ch.ethz.mc.model.persistent.outdated.ScreeningSurveySlideV2;
import ch.ethz.mc.model.persistent.outdated.helpers.MinimalObject;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValueAccessTypes;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValuePrivacyTypes;

import com.mongodb.DBObject;
import com.mongodb.LazyDBList;
import com.mongodb.LazyDBObject;

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
				case 3:
					updateToVersion3();
					break;
				case 4:
					updateToVersion4();
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

			screeningSurveySlideCollection.remove(oldScreeningSurveySlide
					.getId());
			screeningSurveySlideCollection.save(newScreeningSurveySlide);
		}
	}

	/**
	 * Changes for version 3:
	 */
	private static void updateToVersion3() {
		val screeningSurveyCollection = jongo.getCollection("ScreeningSurvey");
		screeningSurveyCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_3__SCREENING_SURVEY__CHANGE_1);

		val monitoringMessageCollection = jongo
				.getCollection("MonitoringMessage");
		monitoringMessageCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_3__MONITORING_MESSAGE__CHANGE_1);

		val mediaObjectCollection = jongo.getCollection("MediaObject");
		mediaObjectCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_3__MEDIA_OBJECT__CHANGE_1);

		val localeToSet = Constants.getInterventionLocales()[0];

		val participantCollection = jongo.getCollection("Participant");
		participantCollection
				.update(Queries.EVERYTHING)
				.multi()
				.with(Queries.UPDATE_VERSION_3__PARTICIPANT__CHANGE_1,
						localeToSet);
		participantCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_3__PARTICIPANT__CHANGE_2);

		updateLStrings(jongo.getCollection("Feedback"),
				new String[] { "name" }, localeToSet);
		updateLStrings(jongo.getCollection("FeedbackSlide"), new String[] {
				"titleWithPlaceholders", "textWithPlaceholders" }, localeToSet);
		updateLStrings(jongo.getCollection("ScreeningSurvey"),
				new String[] { "name" }, localeToSet);
		updateLStrings(jongo.getCollection("ScreeningSurveySlide"),
				new String[] { "titleWithPlaceholders",
						"validationErrorMessage" }, localeToSet);
		updateLStrings(jongo.getCollection("MonitoringMessage"),
				new String[] { "textWithPlaceholders" }, localeToSet);

		// Special case: questions -> questionWithPlaceholders in
		// ScreeningSurveySlide
		MongoCollection collection = jongo
				.getCollection("ScreeningSurveySlide");
		final String fieldQuestions = "questions";
		String subField = "questionWithPlaceholders";

		for (val minimalObject : collection.find(Queries.EVERYTHING)
				.projection(Queries.OBJECT_ID, 1).as(MinimalObject.class)) {
			final MongoCursor<LazyDBList> questionArray = collection.find(
					Queries.OBJECT_ID, minimalObject.getId()).map(
					new ResultHandler<LazyDBList>() {
						@Override
						public LazyDBList map(final DBObject result) {
							return (LazyDBList) result.get(fieldQuestions);
						}
					});

			int i = 0;
			while (questionArray.hasNext()) {
				val itemList = questionArray.next();

				for (val rawItem : itemList) {
					final LazyDBObject dbItem = (LazyDBObject) rawItem;
					val fieldValue = (String) dbItem.get(subField);
					if (fieldValue == null || fieldValue.equals("")) {
						collection
								.update(Queries.OBJECT_ID,
										minimalObject.getId())
								.with(Queries.UPDATE_VERSION_3__GENERAL_UPDATE_FOR_EMPTY_LSTRING,
										fieldQuestions + "." + i + "."
												+ subField);
					} else {
						collection
								.update(Queries.OBJECT_ID,
										minimalObject.getId())
								.with(Queries.UPDATE_VERSION_3__GENERAL_UPDATE_FOR_FILLED_LSTRING,
										fieldQuestions + "." + i + "."
												+ subField,
										localeToSet.toString(), fieldValue);
					}
					i++;
				}
			}
		}

		// Special case: questions -> answersWithPlaceholders[] in
		// ScreeningSurveySlide
		collection = jongo.getCollection("ScreeningSurveySlide");
		subField = "answersWithPlaceholders";

		for (val minimalObject : collection.find(Queries.EVERYTHING)
				.projection(Queries.OBJECT_ID, 1).as(MinimalObject.class)) {
			final MongoCursor<LazyDBList> questionArray = collection.find(
					Queries.OBJECT_ID, minimalObject.getId()).map(
					new ResultHandler<LazyDBList>() {
						@Override
						public LazyDBList map(final DBObject result) {
							return (LazyDBList) result.get(fieldQuestions);
						}
					});

			int i = 0;
			while (questionArray.hasNext()) {
				val itemList = questionArray.next();

				for (val rawItem : itemList) {
					final LazyDBObject dbItem = (LazyDBObject) rawItem;

					int j = 0;
					for (val rawSubItem : (LazyDBList) dbItem.get(subField)) {
						val fieldValue = (String) rawSubItem;
						if (fieldValue == null || fieldValue.equals("")) {
							collection
									.update(Queries.OBJECT_ID,
											minimalObject.getId())
									.with(Queries.UPDATE_VERSION_3__GENERAL_UPDATE_FOR_EMPTY_LSTRING,
											fieldQuestions + "." + i + "."
													+ subField + "." + j);
						} else {
							collection
									.update(Queries.OBJECT_ID,
											minimalObject.getId())
									.with(Queries.UPDATE_VERSION_3__GENERAL_UPDATE_FOR_FILLED_LSTRING,
											fieldQuestions + "." + i + "."
													+ subField + "." + j,
											localeToSet.toString(), fieldValue);
						}
						j++;
					}
					i++;
				}
			}
		}
	}

	/**
	 * Changes for version 4:
	 */
	private static void updateToVersion4() {
		val interventionVariableWithValueCollection = jongo
				.getCollection("InterventionVariableWithValue");

		val oldInterventionVariableWithValueIterator = interventionVariableWithValueCollection
				.find(Queries.EVERYTHING)
				.as(ch.ethz.mc.model.persistent.outdated.InterventionVariableWithValueV2.class)
				.iterator();

		final InterventionVariableWithValueV2[] oldInterventionVariableWithValues = (InterventionVariableWithValueV2[]) IteratorUtils
				.toArray(oldInterventionVariableWithValueIterator,
						InterventionVariableWithValueV2.class);

		for (val oldInterventionVariableWithValue : oldInterventionVariableWithValues) {
			val newInterventionVariableWithValue = new InterventionVariableWithValueV3(
					oldInterventionVariableWithValue.getId(),
					oldInterventionVariableWithValue.getName(),
					oldInterventionVariableWithValue.getValue(),
					oldInterventionVariableWithValue.getIntervention(),
					InterventionVariableWithValuePrivacyTypes.PRIVATE,
					InterventionVariableWithValueAccessTypes.INTERNAL);

			interventionVariableWithValueCollection
					.remove(oldInterventionVariableWithValue.getId());
			interventionVariableWithValueCollection
					.save(newInterventionVariableWithValue);
		}

		val participantVariableWithValueCollection = jongo
				.getCollection("ParticipantVariableWithValue");

		val oldParticipantVariableWithValueIterator = participantVariableWithValueCollection
				.find(Queries.EVERYTHING)
				.as(ch.ethz.mc.model.persistent.outdated.ParticipantVariableWithValueV2.class)
				.iterator();

		final ParticipantVariableWithValueV2[] oldParticipantVariableWithValues = (ParticipantVariableWithValueV2[]) IteratorUtils
				.toArray(oldParticipantVariableWithValueIterator,
						ParticipantVariableWithValueV2.class);

		for (val oldParticipantVariableWithValue : oldParticipantVariableWithValues) {
			val newParticipantVariableWithValue = new ParticipantVariableWithValueV3(
					oldParticipantVariableWithValue.getId(),
					oldParticipantVariableWithValue.getName(),
					oldParticipantVariableWithValue.getValue(),
					oldParticipantVariableWithValue.getParticipant(),
					oldParticipantVariableWithValue.getLastUpdated());

			participantVariableWithValueCollection
					.remove(oldParticipantVariableWithValue.getId());
			participantVariableWithValueCollection
					.save(newParticipantVariableWithValue);
		}
	}

	private static void updateLStrings(final MongoCollection collection,
			final String[] fields, final Locale localeToSet) {
		for (val minimalObject : collection.find(Queries.EVERYTHING)
				.projection(Queries.OBJECT_ID, 1).as(MinimalObject.class)) {
			for (val field : fields) {
				final MongoCursor<String> fieldValues = collection.find(
						Queries.OBJECT_ID, minimalObject.getId()).map(
						new ResultHandler<String>() {
							@Override
							public String map(final DBObject result) {
								if (result.get(field) instanceof String) {
									return (String) result.get(field);
								} else {
									return null;
								}
							}
						});
				while (fieldValues.hasNext()) {
					val fieldValue = fieldValues.next();

					if (fieldValue == null || fieldValue.equals("")) {
						collection
								.update(Queries.OBJECT_ID,
										minimalObject.getId())
								.with(Queries.UPDATE_VERSION_3__GENERAL_UPDATE_FOR_EMPTY_LSTRING,
										field);
					} else {
						collection
								.update(Queries.OBJECT_ID,
										minimalObject.getId())
								.with(Queries.UPDATE_VERSION_3__GENERAL_UPDATE_FOR_FILLED_LSTRING,
										field, localeToSet.toString(),
										fieldValue);
					}
				}
			}
		}

	}
}
