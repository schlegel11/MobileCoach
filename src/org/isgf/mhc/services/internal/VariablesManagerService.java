package org.isgf.mhc.services.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

	private final HashSet<String>			allSystemVariables;
	private final HashSet<String>			writeProtectedVariables;

	private VariablesManagerService(
			final DatabaseManagerService databaseManagerService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;

		writeProtectedVariables = new HashSet<String>();
		for (val variable : Variables.READ_ONLY_SYSTEM_VARIABLES.values()) {
			writeProtectedVariables.add("$" + variable.name());
		}
		for (val variable : Variables.READ_ONLY_PARTICIPANT_VARIABLES.values()) {
			writeProtectedVariables.add("$" + variable.name());
		}

		allSystemVariables = new HashSet<String>();
		allSystemVariables.addAll(writeProtectedVariables);
		for (val variable : Variables.READ_WRITE_PARTICIPANT_VARIABLES.values()) {
			allSystemVariables.add("$" + variable.name());
		}
		for (val variable : Variables.READ_WRITE_SYSTEM_VARIABLES.values()) {
			allSystemVariables.add("$" + variable.name());
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

	public boolean isWriteProtectedParticipantOrSystemVariable(
			final String variable) {
		return writeProtectedVariables.contains(variable);
	}

	public String[] getAllSystemVariables() {
		return allSystemVariables.toArray(new String[] {});
	}

	public List<String> getAllInterventionVariables(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

		val variableModelObjects = databaseManagerService.findModelObjects(
				InterventionVariableWithValue.class,
				Queries.INTERVENTION_VARIABLES_WITH_VALUES__BY_INTERVENTION,
				interventionId);

		for (val variableModelObject : variableModelObjects) {
			variables.add(variableModelObject.getName());
		}

		return variables;
	}

	public List<String> getAllInterventionScreeningSurveyVariables(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

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

	public List<String> getAllMonitoringMessageVariables(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

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

	public List<String> getAllMonitoringRuleAndReplyRuleVariables(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

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
