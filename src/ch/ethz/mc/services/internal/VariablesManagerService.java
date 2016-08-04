package ch.ethz.mc.services.internal;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.memory.MemoryVariable;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.IntermediateSurveyAndFeedbackParticipantShortURL;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;
import ch.ethz.mc.model.persistent.MonitoringMessage;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ParticipantVariableWithValue;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.ScreeningSurveySlideRule;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValueAccessTypes;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValuePrivacyTypes;
import ch.ethz.mc.services.types.SystemVariables;
import ch.ethz.mc.services.types.SystemVariables.READ_ONLY_PARTICIPANT_VARIABLES;
import ch.ethz.mc.services.types.SystemVariables.READ_ONLY_SYSTEM_VARIABLES;
import ch.ethz.mc.services.types.SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.tools.StringValidator;

/**
 * Manages all variables for the system and a specific participant
 *
 * @author Andreas Filler
 */
@Log4j2
public class VariablesManagerService {
	private static VariablesManagerService	instance			= null;

	private final DatabaseManagerService	databaseManagerService;

	private final HashSet<String>			allSystemReservedVariableNames;
	private final HashSet<String>			allSystemReservedVariableNamesRelevantForSlides;
	private final HashSet<String>			writableReservedVariableNames;
	private final HashSet<String>			writeProtectedReservedVariableNames;

	private final HashSet<String>			externallyReadableSystemVariableNames;
	private final HashSet<String>			externallyReadableParticipantVariableNames;

	private static SimpleDateFormat			dayInWeekFormatter	= new SimpleDateFormat(
																		"u");
	private static SimpleDateFormat			dayOfMonthFormatter	= new SimpleDateFormat(
																		"d");
	private static SimpleDateFormat			monthFormatter		= new SimpleDateFormat(
																		"M");
	private static SimpleDateFormat			yearFormatter		= new SimpleDateFormat(
																		"yyyy");

	private VariablesManagerService(
			final DatabaseManagerService databaseManagerService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;

		writeProtectedReservedVariableNames = new HashSet<String>();
		for (val variable : SystemVariables.READ_ONLY_SYSTEM_VARIABLES.values()) {
			writeProtectedReservedVariableNames.add(variable.toVariableName());
		}
		for (val variable : SystemVariables.READ_ONLY_PARTICIPANT_VARIABLES
				.values()) {
			writeProtectedReservedVariableNames.add(variable.toVariableName());
		}
		for (val variable : SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES
				.values()) {
			writeProtectedReservedVariableNames.add(variable.toVariableName());
		}

		writableReservedVariableNames = new HashSet<String>();
		for (val variable : SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES
				.values()) {
			writableReservedVariableNames.add(variable.toVariableName());
		}

		allSystemReservedVariableNames = new HashSet<String>();
		allSystemReservedVariableNames
				.addAll(writeProtectedReservedVariableNames);
		for (val variable : SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES
				.values()) {
			allSystemReservedVariableNames.add(variable.toVariableName());
		}

		allSystemReservedVariableNamesRelevantForSlides = new HashSet<String>();
		allSystemReservedVariableNamesRelevantForSlides
				.addAll(allSystemReservedVariableNames);
		for (val variable : SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES
				.values()) {
			allSystemReservedVariableNamesRelevantForSlides.remove(variable
					.toVariableName());
		}

		externallyReadableSystemVariableNames = new HashSet<String>();
		for (val variable : SystemVariables.EXTERNALLY_READABLE_SYSTEM_VARIABLE_NAMES) {
			externallyReadableSystemVariableNames.add(variable);
		}
		externallyReadableParticipantVariableNames = new HashSet<String>();
		for (val variable : SystemVariables.EXTERNALLY_READABLE_PARTICIPANT_VARIABLE_NAMES) {
			externallyReadableParticipantVariableNames.add(variable);
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
	public Hashtable<String, AbstractVariableWithValue> getAllVariablesWithValuesOfParticipantAndSystem(
			final Participant participant) {
		val variablesWithValues = new Hashtable<String, AbstractVariableWithValue>();

		// Add all read/write participant variables
		for (val variable : SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES
				.values()) {
			val readWriteParticipantVariableValue = getReadWriteParticipantVariableValueForParticipant(
					participant, variable);

			if (readWriteParticipantVariableValue != null) {
				addToHashtable(variablesWithValues, variable.toVariableName(),
						readWriteParticipantVariableValue);
			}
		}

		// Retrieve all stored variables
		val participantVariablesWithValues = databaseManagerService
				.findSortedModelObjects(
						ParticipantVariableWithValue.class,
						Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT,
						Queries.PARTICIPANT_VARIABLE_WITH_VALUE__SORT_BY_TIMESTAMP_DESC,
						participant.getId());
		val interventionVariablesWithValues = databaseManagerService
				.findModelObjects(
						InterventionVariableWithValue.class,
						Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION,
						participant.getIntervention());

		// Add all variables of participant
		for (val participantVariableWithValue : participantVariablesWithValues) {
			if (!variablesWithValues.containsKey(participantVariableWithValue
					.getName())) {
				variablesWithValues.put(participantVariableWithValue.getName(),
						participantVariableWithValue);
			}
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

		// Add all read only system variables
		val date = new Date(InternalDateTime.currentTimeMillis());
		for (val variable : SystemVariables.READ_ONLY_SYSTEM_VARIABLES.values()) {
			val readOnlySystemVariableValue = getReadOnlySystemVariableValue(
					date, variable);

			if (readOnlySystemVariableValue != null) {
				addToHashtable(variablesWithValues, variable.toVariableName(),
						readOnlySystemVariableValue);
			}
		}

		// Add all read only participant variables
		val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participant.getId());
		for (val variable : SystemVariables.READ_ONLY_PARTICIPANT_VARIABLES
				.values()) {
			val readOnlyParticipantVariableValue = getReadOnlyParticipantVariableValue(
					participant, dialogStatus, variable);

			if (readOnlyParticipantVariableValue != null) {
				addToHashtable(variablesWithValues, variable.toVariableName(),
						readOnlyParticipantVariableValue);
			}
		}

		// Add all read only participant reply variables
		for (val variable : SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES
				.values()) {
			switch (variable) {
				case participantMessageReply:
					if (!variablesWithValues.containsKey(variable
							.toVariableName())) {
						addToHashtable(variablesWithValues,
								variable.toVariableName(), "");
					}
					break;
			}
		}

		return variablesWithValues;
	}

	/*
	 * Helper methods to retrieve specific variable groups
	 */
	private String getReadWriteParticipantVariableValueForParticipant(
			final Participant participant,
			final READ_WRITE_PARTICIPANT_VARIABLES variable) {
		switch (variable) {
			case participantDialogOptionEmailData:
				val dialogOptionEmail = databaseManagerService
						.findOneModelObject(DialogOption.class,
								Queries.DIALOG_OPTION__BY_PARTICIPANT_AND_TYPE,
								participant.getId(), DialogOptionTypes.EMAIL);
				if (dialogOptionEmail != null) {
					return dialogOptionEmail.getData();
				}
				break;
			case participantDialogOptionSMSData:
				val dialogOptionSMS = databaseManagerService
						.findOneModelObject(DialogOption.class,
								Queries.DIALOG_OPTION__BY_PARTICIPANT_AND_TYPE,
								participant.getId(), DialogOptionTypes.SMS);
				if (dialogOptionSMS != null) {
					return dialogOptionSMS.getData();
				}
				break;
			case participantName:
				return participant.getNickname();
			case participantLanguage:
				return participant.getLanguage().toLanguageTag();
			case participantGroup:
				if (participant.getGroup() != null) {
					return participant.getGroup();
				}
				break;
		}
		return null;
	}

	private String getReadOnlySystemVariableValue(final Date date,
			final READ_ONLY_SYSTEM_VARIABLES variable) {
		switch (variable) {
			case systemDayInWeek:
				return dayInWeekFormatter.format(date);
			case systemDayOfMonth:
				return dayOfMonthFormatter.format(date);
			case systemMonth:
				return monthFormatter.format(date);
			case systemYear:
				return yearFormatter.format(date);
		}
		return null;
	}

	private String getReadOnlyParticipantVariableValue(
			final Participant participant, final DialogStatus dialogStatus,
			final READ_ONLY_PARTICIPANT_VARIABLES variable) {
		switch (variable) {
			case participantParticipationInDays:
				int participationInDays = 0;
				if (dialogStatus != null) {
					participationInDays = dialogStatus
							.getMonitoringDaysParticipated();
				}
				return String.valueOf(participationInDays);
			case participantParticipationInWeeks:
				participationInDays = 0;
				int participationInWeeks = 0;
				if (dialogStatus != null) {
					participationInDays = dialogStatus
							.getMonitoringDaysParticipated();
				}
				if (dialogStatus != null) {
					participationInWeeks = (int) Math
							.floor(participationInDays / 7);
				}
				return String.valueOf(participationInWeeks);
			case participantFeedbackURL:
				String participantFeedbackURL = "";

				if (participant.getAssignedFeedback() != null) {
					val surveyShortURL = databaseManagerService
							.findOneModelObject(
									IntermediateSurveyAndFeedbackParticipantShortURL.class,
									Queries.INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_PARTICIPANT_AND_FEEDBACK,
									participant.getId(),
									participant.getAssignedFeedback());

					if (surveyShortURL != null) {
						participantFeedbackURL = surveyShortURL.calculateURL();
					}
				}
				return participantFeedbackURL;
		}
		return null;
	}

	/**
	 * Adds the variable with value as {@link AbstractVariableWithValue} to the
	 * hashtable
	 *
	 * @param hashtable
	 * @param variable
	 * @param value
	 */
	private void addToHashtable(
			final Hashtable<String, AbstractVariableWithValue> hashtable,
			final String variable, final String value) {
		val newVariableWithValue = new MemoryVariable();
		newVariableWithValue.setName(variable);
		newVariableWithValue.setValue(value);

		hashtable.put(variable, newVariableWithValue);
	}

	@Synchronized
	public void writeVariableValueOfParticipant(final ObjectId participantId,
			final String variableName, final String variableValue)
			throws WriteProtectedVariableException,
			InvalidVariableNameException {
		writeVariableValueOfParticipant(participantId, variableName,
				variableValue, false, false);
	}

	@Synchronized
	public void writeVariableValueOfParticipant(final ObjectId participantId,
			final String variableName, final String variableValue,
			final boolean overwriteAllowed, final boolean describesMediaUpload)
			throws WriteProtectedVariableException,
			InvalidVariableNameException {
		log.debug("Storing variable {} with value {} for participant {}",
				variableName, variableValue, participantId);

		if (!StringValidator.isValidVariableName(variableName)) {
			throw new InvalidVariableNameException();
		}

		if (!overwriteAllowed
				&& isWriteProtectedReservedVariableName(variableName)) {
			log.warn("{} is a write protected variable name", variableName);
			throw new WriteProtectedVariableException();
		}

		// Care for read write participants variable
		if (isWritableReservedVariableName(variableName)) {
			val readWriteVariableName = SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES
					.valueOf(variableName.substring(1));

			switch (readWriteVariableName) {
				case participantDialogOptionEmailData:
					log.debug("Setting variable 'participantDialogOptionEmailData'");
					participantSetDialogOption(participantId,
							DialogOptionTypes.EMAIL,
							StringHelpers.cleanEmailAddress(variableValue));
					break;
				case participantDialogOptionSMSData:
					log.debug("Setting variable 'participantDialogOptionSMSData'");
					participantSetDialogOption(participantId,
							DialogOptionTypes.SMS,
							StringHelpers.cleanPhoneNumber(variableValue));
					break;
				case participantName:
					log.debug("Setting variable 'participantName'");
					participantSetName(participantId, variableValue);
					break;
				case participantLanguage:
					log.debug("Setting variable 'participantLanguage'");
					participantSetLanguage(participantId, variableValue);
					break;
				case participantGroup:
					log.debug("Setting variable 'participantGroup'");
					participantSetGroup(participantId, variableValue);
					break;
			}
		} else {
			log.debug("Creating new variable");
			val newParticipantVariableWithValue = new ParticipantVariableWithValue(
					participantId, InternalDateTime.currentTimeMillis(),
					variableName, variableValue == null ? "" : variableValue);
			if (describesMediaUpload) {
				newParticipantVariableWithValue.setDescribesMediaUpload(true);
			}

			databaseManagerService
					.saveModelObject(newParticipantVariableWithValue);
		}
	}

	@Synchronized
	private void participantSetName(final ObjectId participantId,
			final String participantName) {
		val participant = databaseManagerService.getModelObjectById(
				Participant.class, participantId);

		participant.setNickname(participantName);

		databaseManagerService.saveModelObject(participant);
	}

	@Synchronized
	private void participantSetLanguage(final ObjectId participantId,
			final String language) {
		val participant = databaseManagerService.getModelObjectById(
				Participant.class, participantId);

		tryLocales: try {
			val localeParts = language.replace("-", "_").split("_");
			val localeToSet = new Locale(localeParts[0], localeParts[1]);

			for (val locale : Constants.getInterventionLocales()) {
				if (locale.equals(localeToSet)) {
					participant.setLanguage(localeToSet);
					break tryLocales;
				}
			}

			throw new Exception();
		} catch (final Exception e) {
			log.warn(
					"The value {} could not be interpreted as language (configured for this system) - so the participant language could not be set",
					language);
		}

		databaseManagerService.saveModelObject(participant);
	}

	@Synchronized
	private void participantSetGroup(final ObjectId participantId,
			final String group) {
		val participant = databaseManagerService.getModelObjectById(
				Participant.class, participantId);

		if (group == null || group.equals("")) {
			participant.setGroup(null);
		} else {
			participant.setGroup(group);
		}

		databaseManagerService.saveModelObject(participant);
	}

	@Synchronized
	private void participantSetDialogOption(final ObjectId participantId,
			final DialogOptionTypes dialogOptionType,
			final String dialogOptionData) {
		DialogOption dialogOption = databaseManagerService.findOneModelObject(
				DialogOption.class,
				Queries.DIALOG_OPTION__BY_PARTICIPANT_AND_TYPE, participantId,
				dialogOptionType);

		if (dialogOption == null) {
			dialogOption = new DialogOption(participantId, dialogOptionType,
					dialogOptionData);
		}

		dialogOption.setData(dialogOptionData);

		databaseManagerService.saveModelObject(dialogOption);
	}

	/*
	 * Methods for administration
	 */
	public boolean isWriteProtectedReservedVariableName(final String variable) {
		return writeProtectedReservedVariableNames.contains(variable);
	}

	public boolean isWritableReservedVariableName(final String variable) {
		return writableReservedVariableNames.contains(variable);
	}

	public Set<String> getAllSystemReservedVariableNames() {
		return allSystemReservedVariableNames;
	}

	public Set<String> getAllSystemReservedVariableNamesRelevantForSlides() {
		return allSystemReservedVariableNamesRelevantForSlides;
	}

	public Set<String> getAllWritableSystemReservedVariableNames() {
		return writableReservedVariableNames;
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

	public Set<String> getAllSurveyVariableNamesOfIntervention(
			final ObjectId interventionId) {
		val variables = new HashSet<String>();

		val surveyModelObjects = databaseManagerService.findModelObjects(
				ScreeningSurvey.class,
				Queries.SCREENING_SURVEY__BY_INTERVENTION, interventionId);
		for (val surveyModelObject : surveyModelObjects) {
			val surveySlideModelObjects = databaseManagerService
					.findModelObjects(
							ScreeningSurveySlide.class,
							Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY,
							surveyModelObject.getId());

			for (val surveySlideModelObject : surveySlideModelObjects) {
				for (val question : surveySlideModelObject.getQuestions()) {
					if (question.getStoreValueToVariableWithName() != null) {
						variables.add(question
								.getStoreValueToVariableWithName());
					}
				}

				val surveySlideRuleModelObjects = databaseManagerService
						.findModelObjects(
								ScreeningSurveySlideRule.class,
								Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
								surveySlideModelObject.getId());

				for (val surveySlideRuleModelObject : surveySlideRuleModelObjects) {
					if (surveySlideRuleModelObject
							.getStoreValueToVariableWithName() != null) {
						variables.add(surveySlideRuleModelObject
								.getStoreValueToVariableWithName());
					}
				}
			}
		}

		return variables;
	}

	public Set<String> getAllSurveyVariableNamesOfSurvey(final ObjectId surveyId) {
		val variables = new HashSet<String>();

		val surveySlideModelObjects = databaseManagerService.findModelObjects(
				ScreeningSurveySlide.class,
				Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY, surveyId);

		for (val surveySlideModelObject : surveySlideModelObjects) {
			for (val question : surveySlideModelObject.getQuestions()) {
				if (question.getStoreValueToVariableWithName() != null) {
					variables.add(question.getStoreValueToVariableWithName());
				}
			}

			val surveySlideRuleModelObjects = databaseManagerService
					.findModelObjects(
							ScreeningSurveySlideRule.class,
							Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
							surveySlideModelObject.getId());

			for (val surveySlideRuleModelObject : surveySlideRuleModelObjects) {
				if (surveySlideRuleModelObject
						.getStoreValueToVariableWithName() != null) {
					variables.add(surveySlideRuleModelObject
							.getStoreValueToVariableWithName());
				}

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
	 * External or service access methods
	 */
	/**
	 * Tries to read an variable of a specific participant from an external
	 * interface or service
	 *
	 * @param participantId
	 * @param variable
	 * @param requestPrivacyType
	 * @param isService
	 * @return
	 * @throws ExternallyReadProtectedVariableException
	 */
	public String externallyReadVariableValueForParticipant(
			final ObjectId participantId, final String variable,
			final InterventionVariableWithValuePrivacyTypes requestPrivacyType,
			boolean isService) throws ExternallyReadProtectedVariableException {

		if (allSystemReservedVariableNames.contains(variable)) {
			// It's a reserved variable
			if (externallyReadableSystemVariableNames.contains(variable)) {
				// Check privacy
				if (!InterventionVariableWithValuePrivacyTypes.PRIVATE
						.isAllowedAtGivenOrLessRestrictivePrivacyType(requestPrivacyType)) {
					throw new ExternallyReadProtectedVariableException(
							"The variable "
									+ variable
									+ " can only be requested for the participant itself");
				}

				try {
					return getReadOnlySystemVariableValue(
							new Date(InternalDateTime.currentTimeMillis()),
							READ_ONLY_SYSTEM_VARIABLES.valueOf(variable
									.replace(
											ImplementationConstants.VARIABLE_PREFIX,
											"")));
				} catch (final Exception e) {
					return null;
				}
			} else if (externallyReadableParticipantVariableNames
					.contains(variable)) {
				// Check privacy
				if (!InterventionVariableWithValuePrivacyTypes.PRIVATE
						.isAllowedAtGivenOrLessRestrictivePrivacyType(requestPrivacyType)) {
					throw new ExternallyReadProtectedVariableException(
							"The variable "
									+ variable
									+ " can only be requested for the participant itself");
				}

				val participant = databaseManagerService.getModelObjectById(
						Participant.class, participantId);

				if (participant == null) {
					throw new ExternallyReadProtectedVariableException(
							"The given participant does not exist anymore, so the variable cannot be read");
				}

				String value = null;
				try {
					value = getReadWriteParticipantVariableValueForParticipant(
							participant,
							READ_WRITE_PARTICIPANT_VARIABLES.valueOf(variable
									.replace(
											ImplementationConstants.VARIABLE_PREFIX,
											"")));
				} catch (final Exception e) {
					// Not relevant to handle
				}

				if (value != null) {
					return value;
				}

				try {
					val dialogStatus = databaseManagerService
							.findOneModelObject(DialogStatus.class,
									Queries.DIALOG_STATUS__BY_PARTICIPANT,
									participant.getId());

					value = getReadOnlyParticipantVariableValue(
							participant,
							dialogStatus,
							READ_ONLY_PARTICIPANT_VARIABLES.valueOf(variable
									.replace(
											ImplementationConstants.VARIABLE_PREFIX,
											"")));
				} catch (final Exception e) {
					// Not relevant to handle
				}

				return value;
			}

			throw new ExternallyReadProtectedVariableException();
		} else {
			// It's a self-created variable
			val participant = databaseManagerService.getModelObjectById(
					Participant.class, participantId);

			if (participant == null) {
				throw new ExternallyReadProtectedVariableException(
						"The given participant does not exist anymore, so the variable cannot be read");
			}

			// Check rights of intervention variable
			val interventionVariable = databaseManagerService
					.findOneModelObject(
							InterventionVariableWithValue.class,
							Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
							participant.getIntervention(), variable);

			if (interventionVariable == null) {
				throw new ExternallyReadProtectedVariableException(
						"The variable " + variable + " does not exist");
			}

			// Check access
			if (isService) {
				if (!interventionVariable
						.getAccessType()
						.isAllowedAtGivenOrLessRestrictiveAccessType(
								InterventionVariableWithValueAccessTypes.MANAGEABLE_BY_SERVICE)) {
					throw new ExternallyReadProtectedVariableException();
				}
			} else {
				if (!interventionVariable
						.getAccessType()
						.isAllowedAtGivenOrLessRestrictiveAccessType(
								InterventionVariableWithValueAccessTypes.EXTERNALLY_READABLE)) {
					throw new ExternallyReadProtectedVariableException();
				}
			}
			// Check privacy
			if (!interventionVariable.getPrivacyType()
					.isAllowedAtGivenOrLessRestrictivePrivacyType(
							requestPrivacyType)) {
				throw new ExternallyReadProtectedVariableException(
						"The variable " + variable
								+ " cannot be requested for "
								+ requestPrivacyType.toString());
			}

			// Find variable value for participant
			val participantVariableWithValue = databaseManagerService
					.findOneSortedModelObject(
							ParticipantVariableWithValue.class,
							Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_NAME,
							Queries.PARTICIPANT_VARIABLE_WITH_VALUE__SORT_BY_TIMESTAMP_DESC,
							participant.getId(), variable);

			if (participantVariableWithValue != null) {
				return participantVariableWithValue.getValue();
			}

			// Return default value if not set for participant
			return interventionVariable.getValue();
		}
	}

	/**
	 * Tries to write a new variable value for a specific variable of a
	 * participant from an external interface or service
	 *
	 * @param participantId
	 * @param variable
	 * @param value
	 * @param describesMediaUpload
	 * @param isService
	 * @throws ExternallyWriteProtectedVariableException
	 */
	public void externallyWriteVariableForParticipant(
			final ObjectId participantId, final String variable,
			final String value, final boolean describesMediaUpload,
			boolean isService) throws ExternallyWriteProtectedVariableException {
		if (allSystemReservedVariableNames.contains(variable)) {
			// It's a reserved variable; these can't be written in general from
			// external interfaces
			throw new ExternallyWriteProtectedVariableException();
		} else {
			// It's a self-created variable
			val participant = databaseManagerService.getModelObjectById(
					Participant.class, participantId);

			if (participant == null) {
				throw new ExternallyWriteProtectedVariableException(
						"The given participant does not exist anymore, so the variable cannot be written");
			}

			final val dialogStatus = databaseManagerService.findOneModelObject(
					DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
					participantId);
			if (!participant.isMonitoringActive()) {
				throw new ExternallyWriteProtectedVariableException(
						"The given participant is currently disabled - try again later");
			} else if (dialogStatus == null
					|| dialogStatus.isMonitoringPerformed()) {
				throw new ExternallyWriteProtectedVariableException(
						"The given participant already performed this intervention");
			}

			// Check rights of intervention variable
			val interventionVariable = databaseManagerService
					.findOneModelObject(
							InterventionVariableWithValue.class,
							Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
							participant.getIntervention(), variable);

			if (interventionVariable == null) {
				throw new ExternallyWriteProtectedVariableException(
						"This variable is not defined, so it cannot be written");
			}
			if (isService) {
				if (!interventionVariable
						.getAccessType()
						.isAllowedAtGivenOrLessRestrictiveAccessType(
								InterventionVariableWithValueAccessTypes.MANAGEABLE_BY_SERVICE)) {
					throw new ExternallyWriteProtectedVariableException();
				}
			} else {
				if (!interventionVariable
						.getAccessType()
						.isAllowedAtGivenOrLessRestrictiveAccessType(
								InterventionVariableWithValueAccessTypes.EXTERNALLY_READ_AND_WRITABLE)) {
					throw new ExternallyWriteProtectedVariableException();
				}
			}

			// Write variable for participant
			try {
				writeVariableValueOfParticipant(participantId, variable, value,
						false, describesMediaUpload);
			} catch (final WriteProtectedVariableException
					| InvalidVariableNameException e) {
				throw new ExternallyWriteProtectedVariableException();
			}
		}
	}

	/**
	 * Tries to write a voting from a specific participant for a specific
	 * participant from an service interface
	 *
	 * @param participantId
	 * @param receivingParticipantId
	 * @param variable
	 * @param addVote
	 * @throws ExternallyWriteProtectedVariableException
	 */
	public void serviceWriteVotingFromParticipantForParticipant(
			final ObjectId participantId,
			final ObjectId receivingParticipantId, final String variable,
			boolean addVote) throws ExternallyWriteProtectedVariableException {
		if (allSystemReservedVariableNames.contains(variable)) {
			// It's a reserved variable; these can't be written in general from
			// external interfaces
			throw new ExternallyWriteProtectedVariableException();
		} else {
			// Disallow self voting
			if (participantId.equals(receivingParticipantId)) {
				throw new ExternallyWriteProtectedVariableException(
						"The participant cannot vote for herself");
			}

			// It's a self-created variable
			val participant = databaseManagerService.getModelObjectById(
					Participant.class, participantId);

			if (participant == null) {
				throw new ExternallyWriteProtectedVariableException(
						"The given participant does not exist anymore, so the voting cannot be written");
			}

			val receivingParticipant = databaseManagerService
					.getModelObjectById(Participant.class,
							receivingParticipantId);

			if (receivingParticipant == null) {
				throw new ExternallyWriteProtectedVariableException(
						"The given receiving participant does not exist anymore, so the voting cannot be written");
			}

			final val dialogStatus = databaseManagerService.findOneModelObject(
					DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
					participantId);
			if (!participant.isMonitoringActive()) {
				throw new ExternallyWriteProtectedVariableException(
						"The given participant is currently disabled - try again later");
			} else if (dialogStatus == null
					|| dialogStatus.isMonitoringPerformed()) {
				throw new ExternallyWriteProtectedVariableException(
						"The given participant already performed this intervention");
			}

			final val dialogStatusReceiving = databaseManagerService
					.findOneModelObject(DialogStatus.class,
							Queries.DIALOG_STATUS__BY_PARTICIPANT,
							receivingParticipantId);
			if (!receivingParticipant.isMonitoringActive()) {
				throw new ExternallyWriteProtectedVariableException(
						"The given receiving participant is currently disabled - try again later");
			} else if (dialogStatusReceiving == null
					|| dialogStatusReceiving.isMonitoringPerformed()) {
				throw new ExternallyWriteProtectedVariableException(
						"The given receiving participant already performed this intervention");
			}

			// Check rights of intervention variable
			val interventionVariable = databaseManagerService
					.findOneModelObject(
							InterventionVariableWithValue.class,
							Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
							participant.getIntervention(), variable);

			if (interventionVariable == null) {
				throw new ExternallyWriteProtectedVariableException(
						"This voting variable is not defined, so it cannot be written");
			}
			if (!interventionVariable
					.getAccessType()
					.isAllowedAtGivenOrLessRestrictiveAccessType(
							InterventionVariableWithValueAccessTypes.MANAGEABLE_BY_SERVICE)) {
				throw new ExternallyWriteProtectedVariableException();
			}
			if (interventionVariable
					.getAccessType()
					.isAllowedAtGivenOrLessRestrictiveAccessType(
							InterventionVariableWithValueAccessTypes.EXTERNALLY_READABLE)) {
				throw new ExternallyWriteProtectedVariableException(
						"Security problem: The variable is directly readable/writable from outside - allows hacking");
			}

			// Check privacy (of intervention variable)
			if (!participant.getIntervention().equals(
					receivingParticipant.getIntervention())) {
				throw new ExternallyWriteProtectedVariableException(
						"This voting variable cannot be written because both participants involved are in different interventions");
			} else if (!interventionVariable
					.getPrivacyType()
					.isAllowedAtGivenOrLessRestrictivePrivacyType(
							InterventionVariableWithValuePrivacyTypes.SHARED_WITH_GROUP)) {
				throw new ExternallyWriteProtectedVariableException(
						"This voting variable cannot be written by another participant because it's not shared with at least the group");
			} else if (!interventionVariable
					.getPrivacyType()
					.isAllowedAtGivenOrLessRestrictivePrivacyType(
							InterventionVariableWithValuePrivacyTypes.SHARED_WITH_INTERVENTION)
					&& !participant.getGroup().equals(
							receivingParticipant.getGroup())) {
				throw new ExternallyWriteProtectedVariableException(
						"This voting variable cannot be written by another participant from a different group");
			}

			// Get existing variable of receiving participant
			val participantVariableWithValue = databaseManagerService
					.findOneSortedModelObject(
							ParticipantVariableWithValue.class,
							Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_NAME,
							Queries.PARTICIPANT_VARIABLE_WITH_VALUE__SORT_BY_TIMESTAMP_DESC,
							receivingParticipant.getId(), variable);

			// Write variable for receiving participant
			try {
				if (addVote) {
					// ADD voting
					if (participantVariableWithValue != null
							&& participantVariableWithValue.getValue().length() >= ImplementationConstants.OBJECT_ID_LENGTH) {

						// Check for double voting
						if (!participantVariableWithValue.getValue().contains(
								participantId.toHexString())) {
							writeVariableValueOfParticipant(
									receivingParticipantId,
									variable,
									participantVariableWithValue.getValue()
											+ "," + participantId.toHexString(),
									false, false);
						}
					} else {
						writeVariableValueOfParticipant(receivingParticipantId,
								variable, participantId.toHexString(), false,
								false);
					}
				} else {
					// REMOVE voting
					if (participantVariableWithValue != null
							&& participantVariableWithValue.getValue().length() >= ImplementationConstants.OBJECT_ID_LENGTH) {

						// Check for existence of voting
						if (participantVariableWithValue.getValue().contains(
								participantId.toHexString() + ",")) {
							// Voting with ","
							writeVariableValueOfParticipant(
									receivingParticipantId,
									variable,
									participantVariableWithValue.getValue()
											.replace(
													participantId.toHexString()
															+ ",", ""), false,
									false);
						} else if (participantVariableWithValue.getValue()
								.contains(participantId.toHexString())) {
							// Voting at end of list
							writeVariableValueOfParticipant(
									receivingParticipantId,
									variable,
									participantVariableWithValue
											.getValue()
											.replace(
													participantId.toHexString(),
													""), false, false);
						}
					}
				}
			} catch (final WriteProtectedVariableException
					| InvalidVariableNameException e) {
				throw new ExternallyWriteProtectedVariableException();
			}
		}
	}

	/**
	 * Tries to write credit for given participant and credit name to given
	 * variable from a service
	 *
	 * @param participantId
	 * @param creditName
	 * @param variable
	 * @throws ExternallyWriteProtectedVariableException
	 */
	public void serviceWriteCreditWithNameForParticipantToVariable(
			final ObjectId participantId, String creditName,
			final String variable)
			throws ExternallyWriteProtectedVariableException {
		if (allSystemReservedVariableNames.contains(variable)) {
			// It's a reserved variable; these can't be written in general from
			// external interfaces
			throw new ExternallyWriteProtectedVariableException();
		} else {
			// It's a self-created variable
			val participant = databaseManagerService.getModelObjectById(
					Participant.class, participantId);

			if (participant == null) {
				throw new ExternallyWriteProtectedVariableException(
						"The given participant does not exist anymore, so the voting cannot be written");
			}

			val dialogStatus = databaseManagerService.findOneModelObject(
					DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
					participantId);
			if (!participant.isMonitoringActive()) {
				throw new ExternallyWriteProtectedVariableException(
						"The given participant is currently disabled - try again later");
			} else if (dialogStatus == null
					|| dialogStatus.isMonitoringPerformed()) {
				throw new ExternallyWriteProtectedVariableException(
						"The given participant already performed this intervention");
			}

			// Get involved intervention variables
			val checkVariable = variable
					+ ImplementationConstants.REST_API_CREDITS_CHECK_VARIABLE_POSTFIX;
			val reminderVariable = variable
					+ ImplementationConstants.REST_API_CREDITS_REMINDER_VARIABLE_POSTFIX;

			val interventionVariable = databaseManagerService
					.findOneModelObject(
							InterventionVariableWithValue.class,
							Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
							participant.getIntervention(), variable);
			val interventionCheckVariable = databaseManagerService
					.findOneModelObject(
							InterventionVariableWithValue.class,
							Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
							participant.getIntervention(), checkVariable);
			val interventionReminderVariable = databaseManagerService
					.findOneModelObject(
							InterventionVariableWithValue.class,
							Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
							participant.getIntervention(), reminderVariable);

			// Check credit name
			if (creditName.length() == 0 || creditName.contains(",")) {
				throw new ExternallyWriteProtectedVariableException(
						"This credit name is not valid, so credit cannot be written");
			}

			// Check rights of involved intervention variable
			if (interventionVariable == null) {
				throw new ExternallyWriteProtectedVariableException(
						"This credit variable is not defined, so it cannot be written");
			}
			if (interventionCheckVariable == null) {
				throw new ExternallyWriteProtectedVariableException(
						"This credits check variable "
								+ checkVariable
								+ " is not defined, so credit can not be checked/written");
			}
			if (interventionReminderVariable == null) {
				throw new ExternallyWriteProtectedVariableException(
						"This credits reminder variable "
								+ reminderVariable
								+ " is not defined, so credit can not be remembered/written");
			}
			if (!interventionVariable
					.getAccessType()
					.isAllowedAtGivenOrLessRestrictiveAccessType(
							InterventionVariableWithValueAccessTypes.MANAGEABLE_BY_SERVICE)) {
				throw new ExternallyWriteProtectedVariableException(
						"The credit variable has to be manageably by service");
			}
			if (interventionVariable
					.getAccessType()
					.isAllowedAtGivenOrLessRestrictiveAccessType(
							InterventionVariableWithValueAccessTypes.EXTERNALLY_READ_AND_WRITABLE)) {
				throw new ExternallyWriteProtectedVariableException(
						"Security problem: The variable is directly writable from outside - allows hacking");
			}
			if (!interventionCheckVariable
					.getAccessType()
					.isAllowedAtGivenOrLessRestrictiveAccessType(
							InterventionVariableWithValueAccessTypes.MANAGEABLE_BY_SERVICE)) {
				throw new ExternallyWriteProtectedVariableException(
						"The check variable has to be manageably by service");
			}
			if (interventionCheckVariable
					.getAccessType()
					.isAllowedAtGivenOrLessRestrictiveAccessType(
							InterventionVariableWithValueAccessTypes.EXTERNALLY_READABLE)) {
				throw new ExternallyWriteProtectedVariableException(
						"Security problem: The check variable is directly readable/writable from outside - allows hacking");
			}
			if (!interventionReminderVariable
					.getAccessType()
					.isAllowedAtGivenOrLessRestrictiveAccessType(
							InterventionVariableWithValueAccessTypes.MANAGEABLE_BY_SERVICE)) {
				throw new ExternallyWriteProtectedVariableException(
						"The reminder variable has to be manageably by service");
			}
			if (interventionReminderVariable
					.getAccessType()
					.isAllowedAtGivenOrLessRestrictiveAccessType(
							InterventionVariableWithValueAccessTypes.EXTERNALLY_READABLE)) {
				throw new ExternallyWriteProtectedVariableException(
						"Security problem: The reminder variable is directly readable/writable from outside - allows hacking");
			}

			// Check if credit name is accepted
			if (!("," + interventionCheckVariable.getValue() + ",")
					.contains("," + creditName + ",")) {
				throw new ExternallyWriteProtectedVariableException(
						"The credit name is unkown so credit can not be written");
			}

			// Get existing reminder variable of participant
			val participantReminderVariableWithValue = databaseManagerService
					.findOneSortedModelObject(
							ParticipantVariableWithValue.class,
							Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_NAME,
							Queries.PARTICIPANT_VARIABLE_WITH_VALUE__SORT_BY_TIMESTAMP_DESC,
							participantId, reminderVariable);

			// Remember credit for participant
			try {
				if (participantReminderVariableWithValue != null) {
					// Check for double credits
					if (("," + participantReminderVariableWithValue.getValue() + ",")
							.contains("," + creditName + ",")) {
						// Credit has already been written, so simply return
						return;
					} else {
						writeVariableValueOfParticipant(participantId,
								reminderVariable,
								participantReminderVariableWithValue.getValue()
										+ "," + creditName, false, false);
					}
				} else {
					writeVariableValueOfParticipant(participantId,
							reminderVariable, creditName, false, false);
				}
			} catch (final WriteProtectedVariableException
					| InvalidVariableNameException e) {
				throw new ExternallyWriteProtectedVariableException();
			}

			// Get existing credit variable of participant
			val participantCreditVariableWithValue = databaseManagerService
					.findOneSortedModelObject(
							ParticipantVariableWithValue.class,
							Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_NAME,
							Queries.PARTICIPANT_VARIABLE_WITH_VALUE__SORT_BY_TIMESTAMP_DESC,
							participantId, variable);

			// Write credit for participant
			try {
				if (participantCreditVariableWithValue != null) {
					if (participantCreditVariableWithValue.getValue().length() > 0) {
						writeVariableValueOfParticipant(
								participantId,
								variable,
								String.valueOf(Integer
										.parseInt(participantCreditVariableWithValue
												.getValue()) + 1), false, false);
					} else {
						writeVariableValueOfParticipant(participantId,
								variable, "1", false, false);
					}
				} else {
					writeVariableValueOfParticipant(participantId, variable,
							"1", false, false);
				}
			} catch (final WriteProtectedVariableException
					| InvalidVariableNameException e) {
				throw new ExternallyWriteProtectedVariableException();
			}
		}
	}

	/**
	 * Checks if variable can be written for the given participant
	 *
	 * @param participantId
	 * @param variable
	 * @return
	 */
	public boolean checkVariableForServiceWriting(final ObjectId participantId,
			final String variable) {
		if (allSystemReservedVariableNames.contains(variable)) {
			return false;
		} else {
			// It's a self-created variable
			val participant = databaseManagerService.getModelObjectById(
					Participant.class, participantId);

			if (participant == null) {
				return false;
			}

			// Check rights of intervention variable
			val interventionVariable = databaseManagerService
					.findOneModelObject(
							InterventionVariableWithValue.class,
							Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
							participant.getIntervention(), variable);

			if (interventionVariable == null) {
				return false;
			}
			if (!interventionVariable
					.getAccessType()
					.isAllowedAtGivenOrLessRestrictiveAccessType(
							InterventionVariableWithValueAccessTypes.MANAGEABLE_BY_SERVICE)) {
				return false;
			}

			return true;
		}
	}

	/*
	 * Exceptions
	 */
	@SuppressWarnings("serial")
	public class ExternallyReadProtectedVariableException extends Exception {
		public ExternallyReadProtectedVariableException() {
			super("This variable is read protected for external interfaces");
		}

		public ExternallyReadProtectedVariableException(final String message) {
			super(message);
		}
	}

	@SuppressWarnings("serial")
	private class WriteProtectedVariableException extends Exception {
		public WriteProtectedVariableException() {
			super("This variable is write protected");
		}
	}

	@SuppressWarnings("serial")
	public class ExternallyWriteProtectedVariableException extends Exception {
		public ExternallyWriteProtectedVariableException() {
			super("This variable is write protected for external interfaces");
		}

		public ExternallyWriteProtectedVariableException(final String message) {
			super(message);
		}
	}

	@SuppressWarnings("serial")
	private class InvalidVariableNameException extends Exception {
		public InvalidVariableNameException() {
			super("This variable name is not allowed");
		}
	}
}
