package ch.ethz.mc.tools;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.jongo.Jongo;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.ScreeningSurveySlideRule;
import ch.ethz.mc.model.persistent.consistency.DataModelConfiguration;

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
			}

			log.info("Update to version {} done", updateToVersionInThisStep);

			// set new version
			val configurationCollection = jongo
					.getCollection(Constants.DATA_MODEL_CONFIGURATION);
			val configuration = configurationCollection.findOne().as(
					DataModelConfiguration.class);
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
				.getCollection(ScreeningSurveySlide.class.getSimpleName());
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
				.getCollection(ScreeningSurveySlideRule.class.getSimpleName());
		screeningSurveySlideRuleCollection
				.update(Queries.EVERYTHING)
				.multi()
				.with(Queries.UPDATE_VERSION_1__SCREENING_SURVEY_SLIDE_RULE__CHANGE_1);
	}
}
