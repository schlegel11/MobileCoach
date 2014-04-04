package org.isgf.mhc.services.internal;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.persistent.InterventionVariableWithValue;
import org.isgf.mhc.model.persistent.MonitoringMessage;
import org.isgf.mhc.model.persistent.MonitoringMessageGroup;
import org.isgf.mhc.model.persistent.MonitoringReplyRule;
import org.isgf.mhc.model.persistent.MonitoringRule;
import org.isgf.mhc.model.persistent.Participant;
import org.isgf.mhc.model.persistent.ParticipantVariableWithValue;
import org.isgf.mhc.model.persistent.ScreeningSurvey;
import org.isgf.mhc.model.persistent.ScreeningSurveySlide;
import org.isgf.mhc.model.persistent.concepts.AbstractVariableWithValue;
import org.isgf.mhc.services.types.SystemVariables;
import org.isgf.mhc.tools.StringValidator;

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
		for (val variable : SystemVariables.READ_ONLY_SYSTEM_VARIABLES.values()) {
			writeProtectedVariableNames.add("$" + variable.name());
		}
		for (val variable : SystemVariables.READ_ONLY_PARTICIPANT_VARIABLES
				.values()) {
			writeProtectedVariableNames.add("$" + variable.name());
		}
		for (val variable : SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES
				.values()) {
			writeProtectedVariableNames.add("$" + variable.name());
		}

		allSystemVariableNames = new HashSet<String>();
		allSystemVariableNames.addAll(writeProtectedVariableNames);
		for (val variable : SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES
				.values()) {
			allSystemVariableNames.add("$" + variable.name());
		}
		for (val variable : SystemVariables.READ_WRITE_SYSTEM_VARIABLES
				.values()) {
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

	/*
	 * Methods for execution
	 */
	public Hashtable<String, AbstractVariableWithValue> getVariablesWithValuesOfParticipant(
			final Participant participant) {
		val variablesWithValues = new Hashtable<String, AbstractVariableWithValue>();

		val participantVariablesWithValues = databaseManagerService
				.findModelObjects(
						InterventionVariableWithValue.class,
						Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT,
						participant.getIntervention());
		val interventionVariablesWithValues = databaseManagerService
				.findModelObjects(
						InterventionVariableWithValue.class,
						Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION,
						participant.getIntervention());

		// Add all variables of participant
		for (val participantVariableWithValue : participantVariablesWithValues) {
			variablesWithValues.put(participantVariableWithValue.getName(),
					participantVariableWithValue);
		}

		// Add also variables of intervention, but only if not overwritten for
		// participant
		for (val interventionVariableWithValue : interventionVariablesWithValues) {
			if (!variablesWithValues.containsKey(interventionVariableWithValue
					.getName())) {
				variablesWithValues.put(
						interventionVariableWithValue.getName(),
						interventionVariableWithValue);
			}
		}

		return variablesWithValues;
	}

	public void storeVariableValueOfParticipant(final Participant participant,
			final String variableName, final String variableValue)
			throws WriteProtectedVariableException,
			InvalidVariableNameException {
		val participantVariableWithValue = databaseManagerService
				.findOneModelObject(
						ParticipantVariableWithValue.class,
						Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_VARIABLE_NAME,
						participant.getId(), variableName);

		if (StringValidator.isValidVariableName(variableName)) {
			throw new InvalidVariableNameException();
		}

		if (isWriteProtectedVariableName(variableName)) {
			throw new WriteProtectedVariableException();
		}

		if (participantVariableWithValue == null) {

			val newParticipantVariableWithValue = new ParticipantVariableWithValue(
					participant.getId(), variableName,
					variableValue == null ? "" : variableValue);
			databaseManagerService
					.saveModelObject(newParticipantVariableWithValue);
		} else {
			participantVariableWithValue.setValue(variableValue == null ? ""
					: variableValue);
		}
	}

	/*
	 * Methods for administration
	 */
	public boolean isWriteProtectedVariableName(final String variable) {
		return writeProtectedVariableNames.contains(variable);
	}

	public Set<String> getAllSystemVariableNames() {
		return allSystemVariableNames;
	}

	public Set<String> getAllInterventionVariableNamesOfIntervention(
			final ObjectId interventionId) {
		val variables = new HashSet<String>();

		val variableModelObjects = databaseManagerService.findModelObjects(
				InterventionVariableWithValue.class,
				Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION,
				interventionId);

		for (val variableModelObject : variableModelObjects) {
			variables.add(variableModelObject.getName());
		}

		return variables;
	}

	public Set<String> getAllScreeningSurveyVariableNamesOfIntervention(
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

	public Set<String> getAllScreeningSurveyVariableNamesOfScreeningSurvey(
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

	public Set<String> getAllMonitoringMessageVariableNamesOfIntervention(
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

	public Set<String> getAllMonitoringRuleAndReplyRuleVariableNamesOfIntervention(
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

	/*
	 * Exceptions
	 */
	@SuppressWarnings("serial")
	private class WriteProtectedVariableException extends Exception {
		public WriteProtectedVariableException() {
			super("This variable name is write protected");
		}
	}

	@SuppressWarnings("serial")
	private class InvalidVariableNameException extends Exception {
		public InvalidVariableNameException() {
			super("This variable name is not allowed");
		}
	}
}
