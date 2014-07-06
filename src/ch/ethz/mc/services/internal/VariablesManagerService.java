package ch.ethz.mc.services.internal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.memory.MemoryVariable;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.DialogStatus;
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
import ch.ethz.mc.services.types.SystemVariables;
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

	private final HashSet<String>			allSystemVariableNames;
	private final HashSet<String>			allSystemVariableNamesRelevantForSlides;
	private final HashSet<String>			writableVariableNames;
	private final HashSet<String>			writeProtectedVariableNames;

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

		writeProtectedVariableNames = new HashSet<String>();
		for (val variable : SystemVariables.READ_ONLY_SYSTEM_VARIABLES.values()) {
			writeProtectedVariableNames.add(variable.toVariableName());
		}
		for (val variable : SystemVariables.READ_ONLY_PARTICIPANT_VARIABLES
				.values()) {
			writeProtectedVariableNames.add(variable.toVariableName());
		}
		for (val variable : SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES
				.values()) {
			writeProtectedVariableNames.add(variable.toVariableName());
		}

		writableVariableNames = new HashSet<String>();
		for (val variable : SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES
				.values()) {
			writableVariableNames.add(variable.toVariableName());
		}

		allSystemVariableNames = new HashSet<String>();
		allSystemVariableNames.addAll(writeProtectedVariableNames);
		for (val variable : SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES
				.values()) {
			allSystemVariableNames.add(variable.toVariableName());
		}

		allSystemVariableNamesRelevantForSlides = new HashSet<String>();
		allSystemVariableNamesRelevantForSlides.addAll(allSystemVariableNames);
		for (val variable : SystemVariables.READ_ONLY_PARTICIPANT_REPLY_VARIABLES
				.values()) {
			allSystemVariableNamesRelevantForSlides.remove(variable
					.toVariableName());
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

		// Add all read/write participant variables (to become overwritten, if
		// required)
		for (val variable : SystemVariables.READ_WRITE_PARTICIPANT_VARIABLES
				.values()) {
			switch (variable) {
				case participantDialogOptionEmailData:
					val dialogOptionEmail = databaseManagerService
							.findOneModelObject(
									DialogOption.class,
									Queries.DIALOG_OPTION__BY_PARTICIPANT_AND_TYPE,
									participant.getId(),
									DialogOptionTypes.EMAIL);
					if (dialogOptionEmail != null) {
						addToHashtable(variablesWithValues,
								variable.toVariableName(),
								dialogOptionEmail.getData());
					}
					break;
				case participantDialogOptionSMSData:
					val dialogOptionSMS = databaseManagerService
							.findOneModelObject(
									DialogOption.class,
									Queries.DIALOG_OPTION__BY_PARTICIPANT_AND_TYPE,
									participant.getId(), DialogOptionTypes.SMS);
					if (dialogOptionSMS != null) {
						addToHashtable(variablesWithValues,
								variable.toVariableName(),
								dialogOptionSMS.getData());
					}
					break;
				case participantName:
					addToHashtable(variablesWithValues,
							variable.toVariableName(),
							participant.getNickname());
					break;
			}
		}

		// Retrieve all stored variables
		val participantVariablesWithValues = databaseManagerService
				.findModelObjects(
						ParticipantVariableWithValue.class,
						Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT,
						participant.getId());
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

		// Add all read only system variables
		val date = new Date(InternalDateTime.currentTimeMillis());
		for (val variable : SystemVariables.READ_ONLY_SYSTEM_VARIABLES.values()) {
			switch (variable) {
				case systemDayInWeek:
					addToHashtable(variablesWithValues,
							variable.toVariableName(),
							dayInWeekFormatter.format(date));
					break;
				case systemDayOfMonth:
					addToHashtable(variablesWithValues,
							variable.toVariableName(),
							dayOfMonthFormatter.format(date));
					break;
				case systemMonth:
					addToHashtable(variablesWithValues,
							variable.toVariableName(),
							monthFormatter.format(date));
					break;
				case systemYear:
					addToHashtable(variablesWithValues,
							variable.toVariableName(),
							yearFormatter.format(date));
					break;
			}
		}

		// Add all read only participant variables
		val dialogStatus = databaseManagerService.findOneModelObject(
				DialogStatus.class, Queries.DIALOG_STATUS__BY_PARTICIPANT,
				participant.getId());
		int participationInDays = 0;
		int participationInWeeks = 0;
		String participantFeedbackURL = "";
		if (dialogStatus != null) {
			participationInDays = dialogStatus.getMonitoringDaysParticipated();
			participationInWeeks = (int) Math.floor(participationInDays / 7);
		}
		if (participant.getAssignedFeedback() != null) {
			participantFeedbackURL = Constants.getFeedbackLinkingBaseURL()
					+ participant.getId();
		}
		for (val variable : SystemVariables.READ_ONLY_PARTICIPANT_VARIABLES
				.values()) {
			switch (variable) {
				case participantParticipationInDays:
					addToHashtable(variablesWithValues,
							variable.toVariableName(),
							String.valueOf(participationInDays));
					break;
				case participantParticipationInWeeks:
					addToHashtable(variablesWithValues,
							variable.toVariableName(),
							String.valueOf(participationInWeeks));
					break;
				case participantFeedbackURL:
					addToHashtable(variablesWithValues,
							variable.toVariableName(), participantFeedbackURL);
					break;
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
				variableValue, false);
	}

	@Synchronized
	public void writeVariableValueOfParticipant(final ObjectId participantId,
			final String variableName, final String variableValue,
			final boolean overwriteAllowed)
			throws WriteProtectedVariableException,
			InvalidVariableNameException {
		log.debug("Storing variable {} with value {} for participant {}",
				variableName, variableValue, participantId);

		if (!StringValidator.isValidVariableName(variableName)) {
			throw new InvalidVariableNameException();
		}

		if (!overwriteAllowed && isWriteProtectedVariableName(variableName)) {
			log.warn("{} is a write protected variable name", variableName);
			throw new WriteProtectedVariableException();
		}

		// Care for read write participants variable
		if (isWritableVariableName(variableName)) {
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
			}
		} else {
			val participantVariableWithValue = databaseManagerService
					.findOneModelObject(
							ParticipantVariableWithValue.class,
							Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_VARIABLE_NAME,
							participantId, variableName);

			if (participantVariableWithValue == null) {
				log.debug("Creating new variable");
				val newParticipantVariableWithValue = new ParticipantVariableWithValue(
						participantId, InternalDateTime.currentTimeMillis(),
						variableName, variableValue == null ? ""
								: variableValue);

				databaseManagerService
						.saveModelObject(newParticipantVariableWithValue);
			} else {
				log.debug("Changing existing variable");
				participantVariableWithValue
						.setValue(variableValue == null ? "" : variableValue);
				participantVariableWithValue.setLastUpdated(InternalDateTime
						.currentTimeMillis());

				databaseManagerService
						.saveModelObject(participantVariableWithValue);
			}
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
	public boolean isWriteProtectedVariableName(final String variable) {
		return writeProtectedVariableNames.contains(variable);
	}

	public boolean isWritableVariableName(final String variable) {
		return writableVariableNames.contains(variable);
	}

	public Set<String> getAllSystemVariableNames() {
		return allSystemVariableNames;
	}

	public Set<String> getAllSystemVariableNamesRelevantForSlides() {
		return allSystemVariableNamesRelevantForSlides;
	}

	public Set<String> getAllWritableSystemVariableNames() {
		return writableVariableNames;
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

				val screeningSurveySlideRuleModelObjects = databaseManagerService
						.findModelObjects(
								ScreeningSurveySlideRule.class,
								Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
								screeningSurveySlideModelObject.getId());

				for (val screeningSurveySlideRuleModelObject : screeningSurveySlideRuleModelObjects) {
					if (screeningSurveySlideRuleModelObject
							.getStoreValueToVariableWithName() != null) {
						variables.add(screeningSurveySlideRuleModelObject
								.getStoreValueToVariableWithName());
					}

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

			val screeningSurveySlideRuleModelObjects = databaseManagerService
					.findModelObjects(
							ScreeningSurveySlideRule.class,
							Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
							screeningSurveySlideModelObject.getId());

			for (val screeningSurveySlideRuleModelObject : screeningSurveySlideRuleModelObjects) {
				if (screeningSurveySlideRuleModelObject
						.getStoreValueToVariableWithName() != null) {
					variables.add(screeningSurveySlideRuleModelObject
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