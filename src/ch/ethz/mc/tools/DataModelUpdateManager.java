package ch.ethz.mc.tools;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.bson.types.ObjectId;
import org.jongo.Jongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.BackendUser;
import ch.ethz.mc.model.persistent.BackendUserInterventionAccess;
import ch.ethz.mc.model.persistent.consistency.DataModelConfiguration;
import ch.ethz.mc.model.persistent.outdated.AuthorInterventionAccessV39;
import ch.ethz.mc.model.persistent.outdated.AuthorV39;
import ch.ethz.mc.model.persistent.outdated.InterventionV12;
import ch.ethz.mc.model.persistent.outdated.MonitoringRuleV12;
import ch.ethz.mc.model.persistent.outdated.ParticipantVariableWithValueV28;
import ch.ethz.mc.model.persistent.outdated.ParticipantVariableWithValueV29;
import ch.ethz.mc.model.persistent.types.BackendUserTypes;
import ch.ethz.mc.model.persistent.types.MonitoringRuleTypes;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Manages the modification of the Data Model on the startup of the system
 *
 * @author Andreas Filler
 */
@Log4j2
public class DataModelUpdateManager {

	private static Jongo jongo;

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
				case 15:
					updateToVersion15();
					break;
				case 16:
					updateToVersion16();
					break;
				case 20:
					updateToVersion20();
					break;
				case 24:
					updateToVersion24();
					break;
				case 29:
					updateToVersion29();
					break;
				case 30:
					updateToVersion30();
					break;
				case 31:
					updateToVersion31();
					break;
				case 32:
					updateToVersion32();
					break;
				case 33:
					updateToVersion33();
					break;
				case 35:
					updateToVersion35();
					break;
				case 36:
					updateToVersion36();
					break;
				case 37:
					updateToVersion37();
					break;
				case 38:
					updateToVersion38();
					break;
				case 40:
					updateToVersion40();
					break;
				case 41:
					updateToVersion41();
					break;
				case 42:
					updateToVersion42();
					break;
				case 44:
					updateToVersion44();
					break;
				case 45:
					updateToVersion45();
					break;
				case 46:
					updateToVersion46();
					break;
				case 47:
					updateToVersion47();
					break;
				case 48:
					updateToVersion48();
					break;
			}

			log.info("Update to version {} done", updateToVersionInThisStep);

			// set new version
			val configurationCollection = jongo
					.getCollection(Constants.DATA_MODEL_CONFIGURATION);
			val configuration = configurationCollection.findOne(Queries.ALL)
					.as(DataModelConfiguration.class);
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
			collection.update(Queries.ALL).multi()
					.with(Queries.UPDATE_VERSION_5__GENERAL_UPDATE_FOR_COMMENT);
		}
	}

	/**
	 * Changes for version 6:
	 */
	private static void updateToVersion6() {
		val interventionCollection = jongo.getCollection("Intervention");
		interventionCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_6__INTERVENTION__CHANGE_1);

		val screeningSurveySlideCollection = jongo
				.getCollection("ScreeningSurveySlide");
		screeningSurveySlideCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_6__SCREENING_SURVEY_SLIDE__CHANGE_1);
	}

	/**
	 * Changes for version 7:
	 */
	private static void updateToVersion7() {
		val monitoringRuleCollection = jongo.getCollection("MonitoringRule");
		monitoringRuleCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_7__MONITORING_RULE__CHANGE_1);

		val monitoringReplyRuleCollection = jongo
				.getCollection("MonitoringReplyRule");
		monitoringReplyRuleCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_7__MONITORING_REPLY_RULE__CHANGE_1);

		val dialogMessageCollection = jongo.getCollection("DialogMessage");
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_7__DIALOG_MESSAGE__CHANGE_1);
	}

	/**
	 * Changes for version 9:
	 */
	private static void updateToVersion9() {
		val interventionCollection = jongo.getCollection("Intervention");
		interventionCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_9__INTERVENTION__CHANGE_1);
		interventionCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_9__INTERVENTION__CHANGE_2);
	}

	/**
	 * Changes for version 10:
	 */
	private static void updateToVersion10() {
		val interventionCollection = jongo.getCollection("Intervention");
		interventionCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_10__INTERVENTION__CHANGE_1);
		interventionCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_10__INTERVENTION__CHANGE_2);
	}

	/**
	 * Changes for version 11:
	 */
	private static void updateToVersion11() {
		val interventionCollection = jongo.getCollection("Intervention");
		interventionCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_11__INTERVENTION__CHANGE_1);
	}

	/**
	 * Changes for version 12:
	 */
	private static void updateToVersion12() {
		val monitoringRuleCollection = jongo.getCollection("MonitoringRule");
		monitoringRuleCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_12__MONITORING_RULE__CHANGE_1);

		val interventionCollection = jongo.getCollection("Intervention");
		val interventions = interventionCollection.find(Queries.ALL)
				.as(InterventionV12.class);

		for (val intervention : interventions) {
			val monitoringRules = monitoringRuleCollection
					.find(Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
							intervention.getId(), null)
					.as(MonitoringRuleV12.class);

			final List<MonitoringRuleV12> monitoringRulesToAdjust = new ArrayList<MonitoringRuleV12>();
			for (val monitoringRule : monitoringRules) {
				monitoringRulesToAdjust.add(monitoringRule);
			}

			val dailyMonitoringRule = new MonitoringRuleV12("",
					RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "", "", null,
					0, null, false, null, MonitoringRuleTypes.DAILY,
					intervention.getId(), 0, 0, false);
			monitoringRuleCollection.save(dailyMonitoringRule);
			val periodicMonitoringRule = new MonitoringRuleV12("",
					RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "", "", null,
					1, null, false, null, MonitoringRuleTypes.PERIODIC,
					intervention.getId(), 0, 0, false);
			monitoringRuleCollection.save(periodicMonitoringRule);
			val unexpectedMessageMonitoringRule = new MonitoringRuleV12("",
					RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "", "", null,
					2, null, false, null,
					MonitoringRuleTypes.UNEXPECTED_MESSAGE,
					intervention.getId(), 0, 0, false);
			monitoringRuleCollection.save(unexpectedMessageMonitoringRule);
			val intentionMonitoringRule = new MonitoringRuleV12("",
					RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "", "", null,
					3, null, false, null, MonitoringRuleTypes.USER_INTENTION,
					intervention.getId(), 0, 0, false);
			monitoringRuleCollection.save(intentionMonitoringRule);

			for (val monitoringRule : monitoringRulesToAdjust) {
				monitoringRule.setIsSubRuleOfMonitoringRule(
						dailyMonitoringRule.getId());
				monitoringRuleCollection.save(monitoringRule);
			}
		}
	}

	/**
	 * Changes for version 13:
	 */
	private static void updateToVersion13() {
		val monitoringRulesCollection = jongo.getCollection("MonitoringRule");
		monitoringRulesCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_13__MONITORING_RULE__CHANGE_1);
	}

	/**
	 * Changes for version 14:
	 */
	private static void updateToVersion14() {
		val dialogMessageCollection = jongo.getCollection("DialogMessage");
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_14__DIALOG_MESSAGE__CHANGE_1);
		val monitoringMessageCollection = jongo
				.getCollection("MonitoringMessage");
		monitoringMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_14__MONITORING_MESSAGE__CHANGE_1);
	}

	/**
	 * Changes for version 15:
	 */
	private static void updateToVersion15() {
		val mongoDriverDialogMessageCollection = jongo.getDatabase()
				.getCollection("DialogMessage");
		val dialogMessageCollection = jongo.getCollection("DialogMessage");

		for (final DBObject document : mongoDriverDialogMessageCollection.find()
				.snapshot()) {

			dialogMessageCollection.update((ObjectId) document.get("_id")).with(
					Queries.UPDATE_VERSION_15__DIALOG_MESSAGE__CHANGE_1_CHANGE,
					document.get(
							Queries.UPDATE_VERSION_15__DIALOG_MESSAGE__CHANGE_1_FIELD));
		}

		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_15__DIALOG_MESSAGE__CHANGE_2);
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_15__DIALOG_MESSAGE__CHANGE_3);
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_15__DIALOG_MESSAGE__CHANGE_4);
	}

	/**
	 * Changes for version 16:
	 */
	private static void updateToVersion16() {
		val monitoringMessageCollection = jongo
				.getCollection("MonitoringMessage");
		monitoringMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_16__MONITORING_MESSAGE__CHANGE_1);
		monitoringMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_16__MONITORING_MESSAGE__CHANGE_2);

		val dialogMessageCollection = jongo.getCollection("DialogMessage");
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_16__DIALOG_MESSAGE__CHANGE_1);
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_16__DIALOG_MESSAGE__CHANGE_2);

		val mongoDriverMonitoringRuleCollection = jongo.getDatabase()
				.getCollection("MonitoringRule");
		val monitoringRuleCollection = jongo.getCollection("MonitoringRule");

		for (final DBObject document : mongoDriverMonitoringRuleCollection
				.find().snapshot()) {

			monitoringRuleCollection.update((ObjectId) document.get("_id"))
					.with(Queries.UPDATE_VERSION_16__MONITORING_RULE__CHANGE_1_CHANGE,
							Integer.parseInt(document
									.get(Queries.UPDATE_VERSION_16__MONITORING_RULE__CHANGE_1_FIELD)
									.toString()) * 60);
			monitoringRuleCollection.update((ObjectId) document.get("_id"))
					.with(Queries.UPDATE_VERSION_16__MONITORING_RULE__CHANGE_1_REMOVE);
		}
	}

	/**
	 * Changes for version 20:
	 */
	private static void updateToVersion20() {
		val mongoDriverMonitoringMessageCollection = jongo.getDatabase()
				.getCollection("MonitoringMessage");
		val monitoringMessageCollection = jongo
				.getCollection("MonitoringMessage");

		for (final DBObject document : mongoDriverMonitoringMessageCollection
				.find().snapshot()) {

			val formerValue = document.get(
					Queries.UPDATE_VERSION_20__MONITORING_MESSAGE__CHANGE_1_FIELD);

			if (formerValue == null) {
				monitoringMessageCollection
						.update((ObjectId) document.get("_id"))
						.with(Queries.UPDATE_VERSION_20__MONITORING_MESSAGE__CHANGE_1_CHANGE,
								false);
			} else {
				monitoringMessageCollection
						.update((ObjectId) document.get("_id"))
						.with(Queries.UPDATE_VERSION_20__MONITORING_MESSAGE__CHANGE_1_CHANGE,
								formerValue);
			}
			monitoringMessageCollection.update((ObjectId) document.get("_id"))
					.with(Queries.UPDATE_VERSION_20__MONITORING_MESSAGE__CHANGE_1_REMOVE);
		}

		val mongoDriverMonitoringRuleCollection = jongo.getDatabase()
				.getCollection("MonitoringRule");
		val monitoringRuleCollection = jongo.getCollection("MonitoringRule");
		monitoringRuleCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_20__ABSTRACT_MONITORING_RULE__CHANGE_1);
		monitoringRuleCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_20__ABSTRACT_MONITORING_RULE__CHANGE_2);

		for (final DBObject document : mongoDriverMonitoringRuleCollection
				.find().snapshot()) {

			val formerValue = document.get(
					Queries.UPDATE_VERSION_20__MONITORING_RULE__CHANGE_1_FIELD);

			if (formerValue == null) {
				monitoringRuleCollection.update((ObjectId) document.get("_id"))
						.with(Queries.UPDATE_VERSION_20__MONITORING_RULE__CHANGE_1_CHANGE,
								false);
			} else {
				monitoringRuleCollection.update((ObjectId) document.get("_id"))
						.with(Queries.UPDATE_VERSION_20__MONITORING_RULE__CHANGE_1_CHANGE,
								formerValue);
			}
			monitoringRuleCollection.update((ObjectId) document.get("_id"))
					.with(Queries.UPDATE_VERSION_20__MONITORING_RULE__CHANGE_1_REMOVE);
		}

		val monitoringReplyRuleCollection = jongo
				.getCollection("MonitoringReplyRule");
		monitoringReplyRuleCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_20__ABSTRACT_MONITORING_RULE__CHANGE_1);
		monitoringReplyRuleCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_20__ABSTRACT_MONITORING_RULE__CHANGE_2);

		val dialogMessageCollection = jongo.getCollection("DialogMessage");
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_20__DIALOG_MESSAGE__CHANGE_1);
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_20__DIALOG_MESSAGE__CHANGE_2);
	}

	/**
	 * Changes for version 24:
	 */
	private static void updateToVersion24() {
		val dialogOptionCollection = jongo.getCollection("DialogOption");
		dialogOptionCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_24__DIALOG_OPTION__CHANGE_1);
	}

	/**
	 * Changes for version 29:
	 */
	private static void updateToVersion29() {
		log.info("Converting old variables scheme to new one...");
		val cache = new Hashtable<String, ParticipantVariableWithValueV29>();
		val participantVariableWithValueCollection = jongo
				.getCollection("ParticipantVariableWithValue");

		log.info("Creating index...");
		participantVariableWithValueCollection.ensureIndex(
				Queries.UPDATE_VERSION_29__PARTICIPANT_VARIABLE_WITH_VAUE__CHANGE_1_SORT);

		for (val participantVariableWithValue : participantVariableWithValueCollection
				.find(Queries.ALL)
				.sort(Queries.UPDATE_VERSION_29__PARTICIPANT_VARIABLE_WITH_VAUE__CHANGE_1_SORT)
				.as(ParticipantVariableWithValueV28.class)) {
			val key = participantVariableWithValue.getParticipant()
					.toHexString() + "-"
					+ participantVariableWithValue.getName();

			final ParticipantVariableWithValueV29 newParticipantVariableWithValue;
			if (cache.containsKey(key)) {
				newParticipantVariableWithValue = cache.get(key);
				newParticipantVariableWithValue.rememberFormerValue();

				newParticipantVariableWithValue.setDescribesMediaUpload(
						participantVariableWithValue.isDescribesMediaUpload());
				newParticipantVariableWithValue.setTimestamp(
						participantVariableWithValue.getTimestamp());
				newParticipantVariableWithValue
						.setValue(participantVariableWithValue.getValue());
			} else {
				newParticipantVariableWithValue = new ParticipantVariableWithValueV29(
						participantVariableWithValue.getParticipant(),
						participantVariableWithValue.getTimestamp(),
						participantVariableWithValue.getName(),
						participantVariableWithValue.getValue());

				newParticipantVariableWithValue.setDescribesMediaUpload(
						participantVariableWithValue.isDescribesMediaUpload());

				cache.put(key, newParticipantVariableWithValue);
			}
		}

		log.info("Removing index...");
		participantVariableWithValueCollection.dropIndex(
				Queries.UPDATE_VERSION_29__PARTICIPANT_VARIABLE_WITH_VAUE__CHANGE_1_SORT);

		log.info("Clearing all variables...");
		participantVariableWithValueCollection.remove(Queries.ALL);

		log.info("Storing new variables...");
		for (val newParticipantVariableWithValueEntry : cache.entrySet()) {
			participantVariableWithValueCollection
					.save(newParticipantVariableWithValueEntry.getValue());
		}

		log.info("Done.");
	}

	/**
	 * Changes for version 30:
	 */
	private static void updateToVersion30() {
		val dialogMessageCollection = jongo.getCollection("DialogMessage");
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_30__DIALOG_MESSAGE__CHANGE_1);
	}

	/**
	 * Changes for version 31:
	 */
	private static void updateToVersion31() {
		val dialogMessageCollection = jongo.getCollection("DialogMessage");
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_31__DIALOG_MESSAGE__CHANGE_1);
		val microDialogMessageCollection = jongo
				.getCollection("MicroDialogMessage");
		microDialogMessageCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_31__MICRO_DIALOG_MESSAGE__CHANGE_1);
	}

	/**
	 * Changes for version 32:
	 */
	private static void updateToVersion32() {
		val monitoringRuleCollection = jongo.getCollection("MonitoringRule");
		monitoringRuleCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_32__MONITORING_RULE__CHANGE_1);
	}

	/**
	 * Changes for version 33:
	 */
	private static void updateToVersion33() {
		val mongoDriverMonitoringMessageCollection = jongo.getDatabase()
				.getCollection("MonitoringMessage");
		val monitoringMessageCollection = jongo
				.getCollection("MonitoringMessage");

		for (final DBObject document : mongoDriverMonitoringMessageCollection
				.find().snapshot()) {
			monitoringMessageCollection.update((ObjectId) document.get("_id"))
					.with(Queries.UPDATE_VERSION_33__MONITORING_MESSAGE__CHANGE_1,
							GlobalUniqueIdGenerator
									.createSimpleGlobalUniqueId());
		}

		val mongoDriverMicroDialogMessageCollection = jongo.getDatabase()
				.getCollection("MicroDialogMessage");
		val microDialogMessageCollection = jongo
				.getCollection("MicroDialogMessage");

		for (final DBObject document : mongoDriverMicroDialogMessageCollection
				.find().snapshot()) {
			microDialogMessageCollection.update((ObjectId) document.get("_id"))
					.with(Queries.UPDATE_VERSION_33__MICRO_DIALOG_MESSAGE__CHANGE_1,
							GlobalUniqueIdGenerator
									.createSimpleGlobalUniqueId());
		}
	}

	/**
	 * Changes for version 35:
	 */
	private static void updateToVersion35() {
		val mongoDriverScreeningSurveyCollection = jongo.getDatabase()
				.getCollection("ScreeningSurvey");
		val screeningSurveyCollection = jongo.getCollection("ScreeningSurvey");

		for (final DBObject document : mongoDriverScreeningSurveyCollection
				.find().snapshot()) {
			screeningSurveyCollection.update((ObjectId) document.get("_id"))
					.with(Queries.UPDATE_VERSION_35__SCREENING_SURVEY__CHANGE_1,
							GlobalUniqueIdGenerator
									.createSimpleGlobalUniqueId());
		}

		val mongoDriverScreeningSurveySlideCollection = jongo.getDatabase()
				.getCollection("ScreeningSurveySlide");
		val screeningSurveySlideCollection = jongo
				.getCollection("ScreeningSurveySlide");

		for (final DBObject document : mongoDriverScreeningSurveySlideCollection
				.find().snapshot()) {
			screeningSurveySlideCollection
					.update((ObjectId) document.get("_id"))
					.with(Queries.UPDATE_VERSION_35__SCREENING_SURVEY_SLIDE__CHANGE_1,
							GlobalUniqueIdGenerator
									.createSimpleGlobalUniqueId());
		}

		val mongoDriverFeedbackCollection = jongo.getDatabase()
				.getCollection("Feedback");
		val feedbackCollection = jongo.getCollection("Feedback");

		for (final DBObject document : mongoDriverFeedbackCollection.find()
				.snapshot()) {
			feedbackCollection.update((ObjectId) document.get("_id")).with(
					Queries.UPDATE_VERSION_35__FEEDBACK__CHANGE_1,
					GlobalUniqueIdGenerator.createSimpleGlobalUniqueId());
		}

		val mongoDriverFeedbackSlideCollection = jongo.getDatabase()
				.getCollection("FeedbackSlide");
		val feedbackSlideCollection = jongo.getCollection("FeedbackSlide");

		for (final DBObject document : mongoDriverFeedbackSlideCollection.find()
				.snapshot()) {
			feedbackSlideCollection.update((ObjectId) document.get("_id")).with(
					Queries.UPDATE_VERSION_35__FEEDBACK_SLIDE__CHANGE_1,
					GlobalUniqueIdGenerator.createSimpleGlobalUniqueId());
		}
	}

	/**
	 * Changes for version 36:
	 */
	private static void updateToVersion36() {
		val screeningSurveySlideCollection = jongo
				.getCollection("ScreeningSurveySlide");
		screeningSurveySlideCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_36__SCREENING_SURVEY_SLIDE__CHANGE_1);
	}

	/**
	 * Changes for version 37:
	 */
	private static void updateToVersion37() {
		val dialogMessageCollection = jongo.getCollection("DialogMessage");
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_37__DIALOG_MESSAGE__CHANGE_1);
	}

	/**
	 * Changes for version 38:
	 */
	private static void updateToVersion38() {
		val participantCollection = jongo.getCollection("Participant");

		val currentTimestamp = InternalDateTime.currentTimeMillis();

		participantCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_38__PARTICIPANT__CHANGE_1,
				currentTimestamp);
		participantCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_38__PARTICIPANT__CHANGE_2,
				currentTimestamp);
	}

	/**
	 * Changes for version 40:
	 */
	private static void updateToVersion40() {
		val dialogMessageCollection = jongo.getCollection("DialogMessage");
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_40__DIALOG_MESSAGE__CHANGE_1);

		val authorCollection = jongo.getCollection("Author");
		val authorInterventionAccessCollection = jongo
				.getCollection("AuthorInterventionAccess");

		val backendUserCollection = jongo.getCollection("BackendUser");
		val backendUserInterventionAccessCollection = jongo
				.getCollection("BackendUserInterventionAccess");

		for (val author : authorCollection.find(Queries.ALL)
				.as(AuthorV39.class)) {

			val backendUser = new BackendUser(
					author.isAdmin() ? BackendUserTypes.ADMIN
							: BackendUserTypes.AUTHOR,
					author.getUsername(), author.getPasswordHash());
			backendUserCollection.save(backendUser);

			for (val authorInterventionAccess : authorInterventionAccessCollection
					.find(Queries.UPDATE_VERSION_40__AUTHOR_INTERVENTION_ACCESS__CHANGE_1,
							author.getId())
					.as(AuthorInterventionAccessV39.class)) {

				val backendUserInterventionAccess = new BackendUserInterventionAccess(
						backendUser.getId(),
						authorInterventionAccess.getIntervention(), "");
				backendUserInterventionAccessCollection
						.save(backendUserInterventionAccess);
			}
		}

		authorCollection.dropIndexes();
		authorInterventionAccessCollection.dropIndexes();

		authorCollection.drop();
		authorInterventionAccessCollection.drop();

		log.info("Done.");
	}

	/**
	 * Changes for version 41:
	 */
	private static void updateToVersion41() {
		val microDialogMessageCollection = jongo
				.getCollection("MicroDialogMessage");

		microDialogMessageCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_41__MICRO_DIALOG_MESSAGE__CHANGE_1);

		val participantCollection = jongo.getCollection("Participant");

		participantCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_41__PARTICIPANT__CHANGE_1);
	}

	/**
	 * Changes for version 42:
	 */
	private static void updateToVersion42() {
		val monitoringMessageCollection = jongo
				.getCollection("MonitoringMessage");

		monitoringMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_42__MONITORING_MESSAGE__CHANGE_1);

		val dialogMessageCollection = jongo.getCollection("DialogMessage");

		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_42__DIALOG_MESSAGE__CHANGE_1);
	}

	/**
	 * Changes for version 44:
	 */
	private static void updateToVersion44() {
		val microDialogCollection = jongo.getCollection("MicroDialog");

		microDialogCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_44__MICRO_DIALOG__CHANGE_1);
	}

	/**
	 * Changes for version 45:
	 */
	private static void updateToVersion45() {

		val mongoDriverInterventionCollection = jongo.getDatabase()
				.getCollection("Intervention");

		val mongoDriverMonitoringMessageGroupCollection = jongo.getDatabase()
				.getCollection("MonitoringMessageGroup");
		val monitoringMessageGroupCollection = jongo
				.getCollection("MonitoringMessageGroup");
		val mongoDriverMicroDialogCollection = jongo.getDatabase()
				.getCollection("MicroDialog");
		val microDialogCollection = jongo.getCollection("MicroDialog");

		final List<ObjectId> objectsToChange = new ArrayList<>();
		for (final DBObject document : mongoDriverInterventionCollection.find()
				.snapshot()) {
			val interventionObjectId = (ObjectId) document.get("_id");

			int i = 0;
			objectsToChange.clear();
			for (final DBObject subDocument : mongoDriverMonitoringMessageGroupCollection
					.find(new BasicDBObject("intervention",
							interventionObjectId))
					.sort(new BasicDBObject("order", 1))) {
				objectsToChange.add((ObjectId) subDocument.get("_id"));
			}
			for (val objectId : objectsToChange) {
				monitoringMessageGroupCollection.update(objectId).with(
						Queries.UPDATE_VERSION_45__MONITORING_MESSAGE_GROUP__CHANGE_1,
						i);
				i++;
			}

			i = 0;
			objectsToChange.clear();
			for (final DBObject subDocument : mongoDriverMicroDialogCollection
					.find(new BasicDBObject("intervention",
							interventionObjectId))
					.sort(new BasicDBObject("order", 1))) {
				objectsToChange.add((ObjectId) subDocument.get("_id"));
			}
			for (val objectId : objectsToChange) {
				microDialogCollection.update(objectId).with(
						Queries.UPDATE_VERSION_45__MICRO_DIALOG__CHANGE_1, i);
				i++;
			}
		}
	}

	/**
	 * Changes for version 46:
	 */
	private static void updateToVersion46() {
		val microDialogMessageCollection = jongo
				.getCollection("MicroDialogMessage");

		microDialogMessageCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_46__MICRO_DIALOG_MESSAGE__CHANGE_1);
		microDialogMessageCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_46__MICRO_DIALOG_MESSAGE__CHANGE_2);

		val dialogMessageCollection = jongo.getCollection("DialogMessage");

		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_46__DIALOG_MESSAGE__CHANGE_1);
		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_46__DIALOG_MESSAGE__CHANGE_2);

	}

	/**
	 * Changes for version 47:
	 */
	private static void updateToVersion47() {
		val microDialogMessageCollection = jongo
				.getCollection("MicroDialogMessage");

		microDialogMessageCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_47__MICRO_DIALOG_MESSAGE__CHANGE_1);
	}

	/**
	 * Changes for version 46:
	 */
	private static void updateToVersion48() {
		val microDialogMessageCollection = jongo
				.getCollection("MicroDialogMessage");

		microDialogMessageCollection.update(Queries.ALL).multi().with(
				Queries.UPDATE_VERSION_48__MICRO_DIALOG_MESSAGE__CHANGE_1);

		val dialogMessageCollection = jongo.getCollection("DialogMessage");

		dialogMessageCollection.update(Queries.ALL).multi()
				.with(Queries.UPDATE_VERSION_48__DIALOG_MESSAGE__CHANGE_1);

	}
}
