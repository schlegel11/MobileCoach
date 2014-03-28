package org.isgf.mhc.services.internal;

import java.util.HashSet;
import java.util.Set;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.Variables;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.server.InterventionVariableWithValue;
import org.isgf.mhc.model.server.MonitoringMessage;
import org.isgf.mhc.model.server.MonitoringMessageGroup;
import org.isgf.mhc.model.server.MonitoringReplyRule;
import org.isgf.mhc.model.server.MonitoringRule;
import org.isgf.mhc.model.server.ScreeningSurvey;
import org.isgf.mhc.model.server.ScreeningSurveySlide;

/**
 * Manages all variables for the system and a specific participant
 * 
 * @author Andreas Filler
 */
@Log4j2
public class VariablesManagerService {
	private static VariablesManagerService	instance	= null;

	private final DatabaseManagerService	databaseManagerService;

	private final HashSet<String>			allSystemVariableNames;
	private final HashSet<String>			writeProtectedVariableNames;

	private VariablesManagerService(
			final DatabaseManagerService databaseManagerService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;

		writeProtectedVariableNames = new HashSet<String>();
		for (val variable : Variables.READ_ONLY_SYSTEM_VARIABLES.values()) {
			writeProtectedVariableNames.add("$" + variable.name());
		}
		for (val variable : Variables.READ_ONLY_PARTICIPANT_VARIABLES.values()) {
			writeProtectedVariableNames.add("$" + variable.name());
		}
		for (val variable : Variables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES
				.values()) {
			writeProtectedVariableNames.add("$" + variable.name());
		}

		allSystemVariableNames = new HashSet<String>();
		allSystemVariableNames.addAll(writeProtectedVariableNames);
		for (val variable : Variables.READ_WRITE_PARTICIPANT_VARIABLES.values()) {
			allSystemVariableNames.add("$" + variable.name());
		}
		for (val variable : Variables.READ_WRITE_SYSTEM_VARIABLES.values()) {
			allSystemVariableNames.add("$" + variable.name());
		}

		log.info("Started.");
	}

	public static VariablesManagerService start(
			final DatabaseManagerService databaseManagerService)
			throws Exception {
		if (instance == null) {
			instance = new VariablesManagerService(databaseManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	public boolean isWriteProtectedParticipantOrSystemVariableName(
			final String variable) {
		return writeProtectedVariableNames.contains(variable);
	}

	public Set<String> getAllSystemVariableNames() {
		return allSystemVariableNames;
	}

	public Set<String> getAllInterventionVariableNames(
			final ObjectId interventionId) {
		val variables = new HashSet<String>();

		val variableModelObjects = databaseManagerService.findModelObjects(
				InterventionVariableWithValue.class,
				Queries.INTERVENTION_VARIABLES_WITH_VALUES__BY_INTERVENTION,
				interventionId);

		for (val variableModelObject : variableModelObjects) {
			variables.add(variableModelObject.getName());
		}

		return variables;
	}

	public Set<String> getAllInterventionScreeningSurveyVariableNames(
			final ObjectId interventionId) {
		val variables = new HashSet<String>();

		val screeningSurveyModelObjects = databaseManagerService
				.findModelObjects(ScreeningSurvey.class,
						Queries.SCREENING_SURVEY__BY_INTERVENTION,
						interventionId);
		for (val screeningSurveyModelObject : screeningSurveyModelObjects) {
			val screeningSurveySlideModelObjects = databaseManagerService
					.findModelObjects(
							ScreeningSurveySlide.class,
							Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY,
							screeningSurveyModelObject.getId());

			for (val screeningSurveySlideModelObject : screeningSurveySlideModelObjects) {
				if (screeningSurveySlideModelObject
						.getStoreValueToVariableWithName() != null) {
					variables.add(screeningSurveySlideModelObject
							.getStoreValueToVariableWithName());
				}
			}
		}

		return variables;
	}

	public Set<String> getAllScreeningSurveyVariableNames(
			final ObjectId screeningSurveyId) {
		val variables = new HashSet<String>();

		val screeningSurveySlideModelObjects = databaseManagerService
				.findModelObjects(ScreeningSurveySlide.class,
						Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY,
						screeningSurveyId);

		for (val screeningSurveySlideModelObject : screeningSurveySlideModelObjects) {
			if (screeningSurveySlideModelObject
					.getStoreValueToVariableWithName() != null) {
				variables.add(screeningSurveySlideModelObject
						.getStoreValueToVariableWithName());
			}
		}

		return variables;
	}

	public Set<String> getAllMonitoringMessageVariableNames(
			final ObjectId interventionId) {
		val variables = new HashSet<String>();

		val monitoringMessageGroupModelObjects = databaseManagerService
				.findModelObjects(MonitoringMessageGroup.class,
						Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION,
						interventionId);
		for (val monitoringMessageGroupModelObject : monitoringMessageGroupModelObjects) {
			val monitoringMessageModelObjects = databaseManagerService
					.findModelObjects(
							MonitoringMessage.class,
							Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
							monitoringMessageGroupModelObject.getId());

			for (val monitoringMessageModelObject : monitoringMessageModelObjects) {
				if (monitoringMessageModelObject
						.getStoreValueToVariableWithName() != null) {
					variables.add(monitoringMessageModelObject
							.getStoreValueToVariableWithName());
				}
			}
		}

		return variables;
	}

	public Set<String> getAllMonitoringRuleAndReplyRuleVariableNames(
			final ObjectId interventionId) {
		val variables = new HashSet<String>();

		val monitoringRuleModelObjects = databaseManagerService
				.findModelObjects(MonitoringRule.class,
						Queries.MONITORING_RULE__BY_INTERVENTION,
						interventionId);
		for (val monitoringRuleModelObject : monitoringRuleModelObjects) {
			if (monitoringRuleModelObject.getStoreValueToVariableWithName() != null) {
				variables.add(monitoringRuleModelObject
						.getStoreValueToVariableWithName());
			}

			val monitoringReplyRuleModelObjects = databaseManagerService
					.findModelObjects(MonitoringReplyRule.class,
							Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE,
							monitoringRuleModelObject.getId(),
							monitoringRuleModelObject.getId());

			for (val monitoringReplyRuleModelObject : monitoringReplyRuleModelObjects) {
				if (monitoringReplyRuleModelObject
						.getStoreValueToVariableWithName() != null) {
					variables.add(monitoringReplyRuleModelObject
							.getStoreValueToVariableWithName());
				}
			}
		}

		return variables;
	}
}
