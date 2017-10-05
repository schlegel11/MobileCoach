package ch.ethz.mc.tools;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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
import java.util.List;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.jongo.Jongo;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.consistency.DataModelConfiguration;
import ch.ethz.mc.model.persistent.outdated.InterventionV12;
import ch.ethz.mc.model.persistent.outdated.MonitoringRuleV12;
import ch.ethz.mc.model.persistent.types.MonitoringRuleTypes;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;

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
				case 5:
					updateToVersion5();
					break;
				case 6:
					updateToVersion6();
					break;
				case 7:
					updateToVersion7();
					break;
				case 9:
					updateToVersion9();
					break;
				case 10:
					updateToVersion10();
					break;
				case 11:
					updateToVersion11();
					break;
				case 12:
					updateToVersion12();
					break;
				case 13:
					updateToVersion13();
					break;
				case 14:
					updateToVersion14();
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
	 * Changes for version 5:
	 */
	private static void updateToVersion5() {
		val collectionsToChange = new String[] { "FeedbackSlide",
				"FeedbackSlideRule", "MonitoringRule", "MonitoringReplyRule",
				"MonitoringMessageRule", "ScreeningSurveySlide",
				"ScreeningSurveySlideRule" };
		for (val collectionName : collectionsToChange) {
			log.debug("Adjusting comment of collection {}", collectionName);
			val collection = jongo.getCollection(collectionName);
			collection.update(Queries.EVERYTHING).multi()
					.with(Queries.UPDATE_VERSION_5__GENERAL_UPDATE_FOR_COMMENT);
		}
	}

	/**
	 * Changes for version 6:
	 */
	private static void updateToVersion6() {
		val interventionCollection = jongo.getCollection("Intervention");
		interventionCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_6__INTERVENTION__CHANGE_1);

		val screeningSurveySlideCollection = jongo
				.getCollection("ScreeningSurveySlide");
		screeningSurveySlideCollection
				.update(Queries.EVERYTHING)
				.multi()
				.with(Queries.UPDATE_VERSION_6__SCREENING_SURVEY_SLIDE__CHANGE_1);
	}

	/**
	 * Changes for version 7:
	 */
	private static void updateToVersion7() {
		val monitoringRuleCollection = jongo.getCollection("MonitoringRule");
		monitoringRuleCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_7__MONITORING_RULE__CHANGE_1);

		val monitoringReplyRuleCollection = jongo
				.getCollection("MonitoringReplyRule");
		monitoringReplyRuleCollection
				.update(Queries.EVERYTHING)
				.multi()
				.with(Queries.UPDATE_VERSION_7__MONITORING_REPLY_RULE__CHANGE_1);

		val dialogMessageCollection = jongo.getCollection("DialogMessage");
		dialogMessageCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_7__DIALOG_MESSAGE__CHANGE_1);
	}

	/**
	 * Changes for version 9:
	 */
	private static void updateToVersion9() {
		val interventionCollection = jongo.getCollection("Intervention");
		interventionCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_9__INTERVENTION__CHANGE_1);
		interventionCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_9__INTERVENTION__CHANGE_2);
	}

	/**
	 * Changes for version 10:
	 */
	private static void updateToVersion10() {
		val interventionCollection = jongo.getCollection("Intervention");
		interventionCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_10__INTERVENTION__CHANGE_1);
		interventionCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_10__INTERVENTION__CHANGE_2);
	}

	/**
	 * Changes for version 11:
	 */
	private static void updateToVersion11() {
		val interventionCollection = jongo.getCollection("Intervention");
		interventionCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_11__INTERVENTION__CHANGE_1);
	}

	/**
	 * Changes for version 12:
	 */
	private static void updateToVersion12() {
		val monitoringRuleCollection = jongo.getCollection("MonitoringRule");
		monitoringRuleCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_12__MONITORING_RULE__CHANGE_1);

		val interventionCollection = jongo.getCollection("Intervention");
		val interventions = interventionCollection.find(Queries.EVERYTHING).as(
				InterventionV12.class);

		for (val intervention : interventions) {
			val monitoringRules = monitoringRuleCollection.find(
					Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
					intervention.getId(), null).as(MonitoringRuleV12.class);

			final List<MonitoringRuleV12> monitoringRulesToAdjust = new ArrayList<MonitoringRuleV12>();
			for (val monitoringRule : monitoringRules) {
				monitoringRulesToAdjust.add(monitoringRule);
			}

			val dailyMonitoringRule = new MonitoringRuleV12(
					"",
					RuleEquationSignTypes.CALCULATED_VALUE_EQUALS,
					"",
					"",
					null,
					0,
					null,
					false,
					null,
					MonitoringRuleTypes.DAILY,
					intervention.getId(),
					ImplementationConstants.DEFAULT_HOUR_TO_SEND_MESSAGE,
					ImplementationConstants.DEFAULT_HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED,
					false);
			monitoringRuleCollection.save(dailyMonitoringRule);
			val periodicMonitoringRule = new MonitoringRuleV12(
					"",
					RuleEquationSignTypes.CALCULATED_VALUE_EQUALS,
					"",
					"",
					null,
					1,
					null,
					false,
					null,
					MonitoringRuleTypes.PERIODIC,
					intervention.getId(),
					ImplementationConstants.DEFAULT_HOUR_TO_SEND_MESSAGE,
					ImplementationConstants.DEFAULT_HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED,
					false);
			monitoringRuleCollection.save(periodicMonitoringRule);
			val unexpectedMessageMonitoringRule = new MonitoringRuleV12(
					"",
					RuleEquationSignTypes.CALCULATED_VALUE_EQUALS,
					"",
					"",
					null,
					2,
					null,
					false,
					null,
					MonitoringRuleTypes.UNEXPECTED_MESSAGE,
					intervention.getId(),
					ImplementationConstants.DEFAULT_HOUR_TO_SEND_MESSAGE,
					ImplementationConstants.DEFAULT_HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED,
					false);
			monitoringRuleCollection.save(unexpectedMessageMonitoringRule);
			val intentionMonitoringRule = new MonitoringRuleV12(
					"",
					RuleEquationSignTypes.CALCULATED_VALUE_EQUALS,
					"",
					"",
					null,
					3,
					null,
					false,
					null,
					MonitoringRuleTypes.USER_INTENTION,
					intervention.getId(),
					ImplementationConstants.DEFAULT_HOUR_TO_SEND_MESSAGE,
					ImplementationConstants.DEFAULT_HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED,
					false);
			monitoringRuleCollection.save(intentionMonitoringRule);

			for (val monitoringRule : monitoringRulesToAdjust) {
				monitoringRule.setIsSubRuleOfMonitoringRule(dailyMonitoringRule
						.getId());
				monitoringRuleCollection.save(monitoringRule);
			}
		}
	}

	/**
	 * Changes for version 13:
	 */
	private static void updateToVersion13() {
		val monitoringRulesCollection = jongo.getCollection("MonitoringRule");
		monitoringRulesCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_13__MONITORING_RULE__CHANGE_1);
	}

	/**
	 * Changes for version 14:
	 */
	private static void updateToVersion14() {
		val dialogMessageCollection = jongo.getCollection("DialogMessage");
		dialogMessageCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_14__DIALOG_MESSAGE__CHANGE_1);
		val monitoringMessageCollection = jongo
				.getCollection("MonitoringMessage");
		monitoringMessageCollection.update(Queries.EVERYTHING).multi()
				.with(Queries.UPDATE_VERSION_14__MONITORING_MESSAGE__CHANGE_1);
	}
}
