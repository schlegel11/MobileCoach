package ch.ethz.mc.services;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.Author;
import ch.ethz.mc.model.persistent.AuthorInterventionAccess;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.Feedback;
import ch.ethz.mc.model.persistent.IntermediateSurveyAndFeedbackParticipantShortURL;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;
import ch.ethz.mc.model.persistent.MediaObject;
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.model.persistent.MicroDialogDecisionPoint;
import ch.ethz.mc.model.persistent.MicroDialogMessage;
import ch.ethz.mc.model.persistent.MicroDialogMessageRule;
import ch.ethz.mc.model.persistent.MicroDialogRule;
import ch.ethz.mc.model.persistent.MonitoringMessage;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringMessageRule;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.persistent.concepts.MicroDialogElementInterface;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.persistent.types.AnswerTypes;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValueAccessTypes;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValuePrivacyTypes;
import ch.ethz.mc.model.persistent.types.MediaObjectTypes;
import ch.ethz.mc.model.persistent.types.MonitoringRuleTypes;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.modules.AbstractModule;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService.FILE_STORES;
import ch.ethz.mc.services.internal.ModelObjectExchangeService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.services.types.ModelObjectExchangeFormatTypes;
import ch.ethz.mc.tools.BCrypt;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.ListMerger;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mc.tools.StringValidator;
import ch.ethz.mc.ui.NotificationMessageException;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Cares for the creation of the {@link Author}s, {@link Participant}s and
 * {@link Intervention}s as well as all related {@link ModelObject}s
 *
 * @author Andreas Filler
 */
@Log4j2
public class InterventionAdministrationManagerService {
	private final Object									$lock;

	private static InterventionAdministrationManagerService	instance	= null;

	private final DatabaseManagerService					databaseManagerService;
	private final FileStorageManagerService					fileStorageManagerService;
	private final VariablesManagerService					variablesManagerService;
	private final ModelObjectExchangeService				modelObjectExchangeService;

	private final SurveyAdministrationManagerService		screeningSurveyAdministrationManagerService;

	private final List<Class<? extends AbstractModule>>		modules;

	private InterventionAdministrationManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final ModelObjectExchangeService modelObjectExchangeService,
			final SurveyAdministrationManagerService screeningSurveyAdministrationManagerService)
			throws Exception {
		$lock = MC.getInstance();

		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.variablesManagerService = variablesManagerService;
		this.modelObjectExchangeService = modelObjectExchangeService;
		this.screeningSurveyAdministrationManagerService = screeningSurveyAdministrationManagerService;

		log.info("Registering modules...");
		// FIXME LONGTERM Also relevant for reimplementation of module system
		modules = new ArrayList<Class<? extends AbstractModule>>();
		// modules.add(MessageContestMotivationalMessage.class);
		// modules.add(MessageContestQuitMessage.class);
		// modules.add(MessageContestDrinkingMessage.class);

		log.info("Started.");
	}

	public static InterventionAdministrationManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final ModelObjectExchangeService modelObjectExchangeService,
			final SurveyAdministrationManagerService screeningSurveyAdministrationManagerService)
			throws Exception {
		if (instance == null) {
			instance = new InterventionAdministrationManagerService(
					databaseManagerService, fileStorageManagerService,
					variablesManagerService, modelObjectExchangeService,
					screeningSurveyAdministrationManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	/*
	 * Modification methods
	 */
	// Intermediate Survey and Feedback Participant Short URL
	@Synchronized
	public IntermediateSurveyAndFeedbackParticipantShortURL feedbackParticipantShortURLEnsure(
			final ObjectId participantId, final ObjectId feedbackId) {

		val existingShortIdObject = databaseManagerService.findOneModelObject(
				IntermediateSurveyAndFeedbackParticipantShortURL.class,
				Queries.INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_PARTICIPANT_AND_FEEDBACK,
				participantId, feedbackId);

		if (existingShortIdObject != null) {
			return existingShortIdObject;
		} else {
			val newestShortIdObject = databaseManagerService
					.findOneSortedModelObject(
							IntermediateSurveyAndFeedbackParticipantShortURL.class,
							Queries.ALL,
							Queries.INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__SORT_BY_SHORT_ID_DESC);

			final long nextShortId = newestShortIdObject == null ? 1
					: newestShortIdObject.getShortId() + 1;

			val newShortIdObject = new IntermediateSurveyAndFeedbackParticipantShortURL(
					nextShortId, StringHelpers.createRandomString(4),
					participantId, null, feedbackId);

			databaseManagerService.saveModelObject(newShortIdObject);

			return newShortIdObject;
		}
	}

	// Author
	@Synchronized
	public Author authorCreate(final String username) {
		val author = new Author(false, username, BCrypt.hashpw(
				RandomStringUtils.randomAlphanumeric(128), BCrypt.gensalt()));

		databaseManagerService.saveModelObject(author);

		return author;
	}

	@Synchronized
	public Author authorAuthenticateAndReturn(final String username,
			final String password) {
		val author = databaseManagerService.findOneModelObject(Author.class,
				Queries.AUTHOR__BY_USERNAME, username);

		if (author == null) {
			log.debug("Username '{}' not found.", username);
			return null;
		}

		if (BCrypt.checkpw(password, author.getPasswordHash())) {
			log.debug("Author with fitting password found");
			return author;
		} else {
			log.debug("Wrong password provided");
			return null;
		}
	}

	@Synchronized
	public void authorSetAdmin(final Author author) {
		author.setAdmin(true);

		databaseManagerService.saveModelObject(author);
	}

	@Synchronized
	public void authorSetAuthor(final Author author,
			final ObjectId currentAuthor) throws NotificationMessageException {
		if (author.getUsername().equals(Constants.getDefaultAdminUsername())) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__DEFAULT_ADMIN_CANT_BE_SET_AS_AUTHOR);
		}
		if (author.getId().equals(currentAuthor)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__CANT_DOWNGRADE_YOURSELF);
		}

		author.setAdmin(false);

		databaseManagerService.saveModelObject(author);
	}

	@Synchronized
	public void authorSetPassword(final Author author, final String newPassword)
			throws NotificationMessageException {
		if (newPassword.length() < 5) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_PASSWORD_IS_NOT_SAFE);
		}

		author.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
		databaseManagerService.saveModelObject(author);
	}

	@Synchronized
	public void authorDelete(final ObjectId currentAuthorId,
			final Author authorToDelete) throws NotificationMessageException {
		if (authorToDelete.getId().equals(currentAuthorId)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__CANT_DELETE_YOURSELF);
		}
		if (authorToDelete.getUsername()
				.equals(Constants.getDefaultAdminUsername())) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__DEFAULT_ADMIN_CANT_BE_DELETED);
		}

		databaseManagerService.deleteModelObject(authorToDelete);
	}

	@Synchronized
	public void authorCheckValidAndUnique(final String newUsername)
			throws NotificationMessageException {
		if (newUsername.length() < 3) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_USERNAME_IS_TOO_SHORT);
		}

		val authors = databaseManagerService.findModelObjects(Author.class,
				Queries.AUTHOR__BY_USERNAME, newUsername);
		if (authors.iterator().hasNext()) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_USERNAME_IS_ALREADY_IN_USE);
		}
	}

	// Intervention
	@Synchronized
	public Intervention interventionCreate(final String name) {
		val intervention = new Intervention(name,
				InternalDateTime.currentTimeMillis(), false, false, false, "",
				null, null, false, new String[] {}, new int[] { 1 }, null);

		if (name.equals("")) {
			intervention.setName(ImplementationConstants.DEFAULT_OBJECT_NAME);
		}

		databaseManagerService.saveModelObject(intervention);
		monitoringRuleCreate(intervention.getId(), null,
				MonitoringRuleTypes.DAILY);
		monitoringRuleCreate(intervention.getId(), null,
				MonitoringRuleTypes.PERIODIC);
		monitoringRuleCreate(intervention.getId(), null,
				MonitoringRuleTypes.UNEXPECTED_MESSAGE);
		monitoringRuleCreate(intervention.getId(), null,
				MonitoringRuleTypes.USER_INTENTION);

		return intervention;
	}

	@Synchronized
	private void interventionRecreateGlobalUniqueIdsForSubelements(
			final Intervention intervention) {
		val screeningSurveysOfIntervention = databaseManagerService
				.findModelObjects(ScreeningSurvey.class,
						Queries.SCREENING_SURVEY__BY_INTERVENTION,
						intervention.getId());

		databaseManagerService.saveModelObject(intervention);

		for (val screeningSurvey : screeningSurveysOfIntervention) {
			screeningSurveyAdministrationManagerService
					.screeningSurveyRecreateGlobalUniqueId(screeningSurvey);
		}
	}

	@Synchronized
	public void interventionSetName(final Intervention intervention,
			final String newName) {
		if (newName.equals("")) {
			intervention.setName(ImplementationConstants.DEFAULT_OBJECT_NAME);
		} else {
			intervention.setName(newName);
		}

		databaseManagerService.saveModelObject(intervention);
	}

	@Synchronized
	public void interventionSetSenderIdentification(
			final Intervention intervention,
			final String newSenderIdentification) {
		intervention.setAssignedSenderIdentification(newSenderIdentification);

		databaseManagerService.saveModelObject(intervention);
	}

	@Synchronized
	public void interventionSetDashboardEnabled(final Intervention intervention,
			final boolean newValue) {
		intervention.setDashboardEnabled(newValue);

		databaseManagerService.saveModelObject(intervention);
	}

	@Synchronized
	public void interventionSetDashboardTemplatePath(
			final Intervention intervention, final String newTemplatePath) {
		intervention.setDashboardTemplatePath(newTemplatePath);

		databaseManagerService.saveModelObject(intervention);
	}

	@Synchronized
	public void interventionSetDashboardPasswordPattern(
			final Intervention intervention, final String newPasswordPattern) {
		intervention.setDashboardPasswordPattern(newPasswordPattern);

		databaseManagerService.saveModelObject(intervention);
	}

	@Synchronized
	public void interventionSetDeepstreamPassword(
			final Intervention intervention, final String newPassword) {
		intervention.setDeepstreamPassword(newPassword);

		databaseManagerService.saveModelObject(intervention);
	}

	@Synchronized
	public void interventionSetAutomaticallyFinishScreeningSurveys(
			final Intervention intervention, final boolean value) {
		intervention.setAutomaticallyFinishScreeningSurveys(value);

		databaseManagerService.saveModelObject(intervention);
	}

	@Synchronized
	public void interventionSetStartingDay(final Intervention intervention,
			final int day, final boolean value) {
		if (value && !ArrayUtils
				.contains(intervention.getMonitoringStartingDays(), day)) {
			intervention.setMonitoringStartingDays(ArrayUtils
					.add(intervention.getMonitoringStartingDays(), day));
		} else if (!value && ArrayUtils
				.contains(intervention.getMonitoringStartingDays(), day)) {
			intervention.setMonitoringStartingDays(ArrayUtils.removeElement(
					intervention.getMonitoringStartingDays(), day));
		}

		databaseManagerService.saveModelObject(intervention);
	}

	@Synchronized
	public void interventionSetInterventionsToCheckForUniqueness(
			final Intervention intervention,
			final String[] interventionsToCheckForUniqueness) {
		intervention.setInterventionsToCheckForUniqueness(
				interventionsToCheckForUniqueness);

		databaseManagerService.saveModelObject(intervention);
	}

	@Synchronized
	public Intervention interventionImport(final File file,
			final boolean duplicate) throws FileNotFoundException, IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.INTERVENTION);

		Intervention importedIntervention = null;

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof Intervention) {
				val intervention = (Intervention) modelObject;
				importedIntervention = intervention;

				// Adjust name
				val dateFormat = DateFormat.getDateTimeInstance(
						DateFormat.MEDIUM, DateFormat.MEDIUM,
						Constants.getAdminLocale());
				val date = dateFormat
						.format(new Date(InternalDateTime.currentTimeMillis()));
				intervention
						.setName(intervention.getName() + " (" + date + ")");

				databaseManagerService.saveModelObject(intervention);
			}
		}

		// Recreate global unique IDs for subelements
		if (duplicate && importedIntervention != null) {
			interventionRecreateGlobalUniqueIdsForSubelements(
					importedIntervention);
		}

		return importedIntervention;
	}

	@Synchronized
	public File interventionExport(final Intervention intervention) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		log.debug(
				"Recursively collect all model objects related to the intervention");
		intervention.collectThisAndRelatedModelObjectsForExport(
				modelObjectsToExport);

		log.debug("Export intervention");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.INTERVENTION);
	}

	@Synchronized
	public void interventionDelete(final Intervention interventionToDelete)
			throws NotificationMessageException {

		databaseManagerService.deleteModelObject(interventionToDelete);
	}

	// Author Intervention Access
	@Synchronized
	public AuthorInterventionAccess authorInterventionAccessCreate(
			final ObjectId authorId, final ObjectId interventionId) {

		val authorInterventionAccess = new AuthorInterventionAccess(authorId,
				interventionId);

		databaseManagerService.saveModelObject(authorInterventionAccess);

		return authorInterventionAccess;
	}

	@Synchronized
	public void authorInterventionAccessDelete(final ObjectId authorId,
			final ObjectId interventionId) {
		val authorInterventionAccess = databaseManagerService
				.findOneModelObject(AuthorInterventionAccess.class,
						Queries.AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR_AND_INTERVENTION,
						authorId, interventionId);

		databaseManagerService.deleteModelObject(authorInterventionAccess);
	}

	// Intervention Variable With Value
	@Synchronized
	public InterventionVariableWithValue interventionVariableWithValueCreate(
			final String variableName, final ObjectId interventionId)
			throws NotificationMessageException {

		if (!StringValidator.isValidVariableName(variableName)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
		}

		if (variablesManagerService
				.isWriteProtectedReservedVariableName(variableName)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
		}

		val interventionVariables = databaseManagerService.findModelObjects(
				InterventionVariableWithValue.class,
				Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
				interventionId, variableName);
		if (interventionVariables.iterator().hasNext()) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_ALREADY_IN_USE);
		}

		val interventionVariableWithValue = new InterventionVariableWithValue(
				interventionId, variableName, "0",
				InterventionVariableWithValuePrivacyTypes.PRIVATE,
				InterventionVariableWithValueAccessTypes.INTERNAL);

		databaseManagerService.saveModelObject(interventionVariableWithValue);

		return interventionVariableWithValue;
	}

	@Synchronized
	public void interventionVariableWithValueCreateOrUpdate(
			final ObjectId interventionId, final String variableName,
			final String variableValue) {

		val interventionVariable = databaseManagerService.findOneModelObject(
				InterventionVariableWithValue.class,
				Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
				interventionId, variableName);

		if (interventionVariable == null) {
			final InterventionVariableWithValue interventionVariableWithValue = new InterventionVariableWithValue(
					interventionId, variableName, variableValue,
					InterventionVariableWithValuePrivacyTypes.PRIVATE,
					InterventionVariableWithValueAccessTypes.INTERNAL);
			databaseManagerService
					.saveModelObject(interventionVariableWithValue);
		} else {
			interventionVariable.setValue(variableValue);
			databaseManagerService.saveModelObject(interventionVariable);
		}
	}

	@Synchronized
	public void interventionVariableWithValueSetName(
			final InterventionVariableWithValue interventionVariableWithValue,
			final String newName) throws NotificationMessageException {

		if (!StringValidator.isValidVariableName(newName)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
		}

		if (variablesManagerService
				.isWriteProtectedReservedVariableName(newName)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
		}

		val interventionVariables = databaseManagerService.findModelObjects(
				InterventionVariableWithValue.class,
				Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
				interventionVariableWithValue.getIntervention(), newName);
		if (interventionVariables.iterator().hasNext()) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_ALREADY_IN_USE);
		}

		interventionVariableWithValue.setName(newName);

		databaseManagerService.saveModelObject(interventionVariableWithValue);
	}

	@Synchronized
	public void interventionVariableWithValueSetValue(
			final InterventionVariableWithValue interventionVariableWithValue,
			final String newValue) throws NotificationMessageException {

		interventionVariableWithValue.setValue(newValue);

		databaseManagerService.saveModelObject(interventionVariableWithValue);
	}

	@Synchronized
	public void interventionVariableWithValueSetPrivacyType(
			final InterventionVariableWithValue interventionVariableWithValue,
			final InterventionVariableWithValuePrivacyTypes newPrivacyType)
			throws NotificationMessageException {

		interventionVariableWithValue.setPrivacyType(newPrivacyType);

		databaseManagerService.saveModelObject(interventionVariableWithValue);
	}

	@Synchronized
	public void interventionVariableWithValueSetAccessType(
			final InterventionVariableWithValue interventionVariableWithValue,
			final InterventionVariableWithValueAccessTypes newAccessType)
			throws NotificationMessageException {

		interventionVariableWithValue.setAccessType(newAccessType);

		databaseManagerService.saveModelObject(interventionVariableWithValue);
	}

	@Synchronized
	public void interventionVariableWithValueDelete(
			final InterventionVariableWithValue variableToDelete) {

		databaseManagerService.deleteModelObject(variableToDelete);
	}

	// Monitoring Message Group
	@Synchronized
	public MonitoringMessageGroup monitoringMessageGroupCreate(
			final String groupName, final ObjectId interventionId) {
		val monitoringMessageGroup = new MonitoringMessageGroup(interventionId,
				groupName, null, 0, false, false, false);

		if (monitoringMessageGroup.getName().equals("")) {
			monitoringMessageGroup
					.setName(ImplementationConstants.DEFAULT_OBJECT_NAME);
		}

		val highestOrderMessageGroup = databaseManagerService
				.findOneSortedModelObject(MonitoringMessageGroup.class,
						Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION,
						Queries.MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_DESC,
						interventionId);

		if (highestOrderMessageGroup != null) {
			monitoringMessageGroup
					.setOrder(highestOrderMessageGroup.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(monitoringMessageGroup);

		return monitoringMessageGroup;
	}

	@Synchronized
	public void monitoringMessageGroupSetMessagesExceptAnswer(
			final MonitoringMessageGroup monitoringMessageGroup,
			final boolean newValue) {
		monitoringMessageGroup.setMessagesExpectAnswer(newValue);

		databaseManagerService.saveModelObject(monitoringMessageGroup);
	}

	@Synchronized
	public void monitoringMessageGroupSetRandomSendOrder(
			final MonitoringMessageGroup monitoringMessageGroup,
			final boolean newValue) {
		monitoringMessageGroup.setSendInRandomOrder(newValue);

		databaseManagerService.saveModelObject(monitoringMessageGroup);
	}

	@Synchronized
	public void monitoringMessageGroupSetSendSamePositionIfSendingAsReply(
			final MonitoringMessageGroup monitoringMessageGroup,
			final boolean newValue) {
		monitoringMessageGroup.setSendSamePositionIfSendingAsReply(newValue);

		databaseManagerService.saveModelObject(monitoringMessageGroup);
	}

	@Synchronized
	public MonitoringMessageGroup monitoringMessageGroupMove(
			final MonitoringMessageGroup monitoringMessageGroup,
			final boolean moveLeft) {
		// Find monitoring message to swap with
		val monitoringMessageGroupToSwapWith = databaseManagerService
				.findOneSortedModelObject(MonitoringMessageGroup.class,
						moveLeft ? Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION_AND_ORDER_LOWER
								: Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION_AND_ORDER_HIGHER,
						moveLeft ? Queries.MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_DESC
								: Queries.MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_ASC,
						monitoringMessageGroup.getIntervention(),
						monitoringMessageGroup.getOrder());

		if (monitoringMessageGroupToSwapWith == null) {
			return null;
		}

		// Swap order
		final int order = monitoringMessageGroup.getOrder();
		monitoringMessageGroup
				.setOrder(monitoringMessageGroupToSwapWith.getOrder());
		monitoringMessageGroupToSwapWith.setOrder(order);

		databaseManagerService.saveModelObject(monitoringMessageGroup);
		databaseManagerService
				.saveModelObject(monitoringMessageGroupToSwapWith);

		return monitoringMessageGroupToSwapWith;
	}

	@Synchronized
	public void monitoringMessageGroupSetName(
			final MonitoringMessageGroup monitoringMessageGroup,
			final String newName) {
		if (newName.equals("")) {
			monitoringMessageGroup
					.setName(ImplementationConstants.DEFAULT_OBJECT_NAME);
		} else {
			monitoringMessageGroup.setName(newName);
		}

		databaseManagerService.saveModelObject(monitoringMessageGroup);
	}

	@Synchronized
	public void monitoringMessageGroupSetValidationExpression(
			final MonitoringMessageGroup monitoringMessageGroup,
			final String newExpression) {
		if (newExpression.equals("")) {
			monitoringMessageGroup.setValidationExpression(null);
		} else {
			monitoringMessageGroup.setValidationExpression(newExpression);
		}

		databaseManagerService.saveModelObject(monitoringMessageGroup);
	}

	@Synchronized
	public void monitoringMessageGroupDelete(
			final MonitoringMessageGroup monitoringMessageGroupToDelete)
			throws NotificationMessageException {

		val linkedMonitoringRules = databaseManagerService.findModelObjects(
				MonitoringRule.class,
				Queries.MONITORING_RULE__BY_RELATED_MONITORING_MESSAGE_GROUP,
				monitoringMessageGroupToDelete.getId());
		if (linkedMonitoringRules.iterator().hasNext()) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_GROUP_CANT_DELETE);
		}

		val linkedMonitoringReplyRules = databaseManagerService
				.findModelObjects(MonitoringReplyRule.class,
						Queries.MONITORING_REPLY_RULE__BY_RELATED_MONITORING_MESSAGE_GROUP,
						monitoringMessageGroupToDelete.getId());
		if (linkedMonitoringReplyRules.iterator().hasNext()) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__MONITORING_MESSAGE_GROUP_CANT_DELETE);
		}

		databaseManagerService
				.deleteModelObject(monitoringMessageGroupToDelete);
	}

	// Monitoring Message
	@Synchronized
	public MonitoringMessage monitoringMessageCreate(
			final ObjectId monitoringMessageGroupId) {
		val monitoringMessage = new MonitoringMessage(monitoringMessageGroupId,
				new LString(), false, 0, null, null, null,
				AnswerTypes.FREE_TEXT, new LString());

		val highestOrderMessage = databaseManagerService
				.findOneSortedModelObject(MonitoringMessage.class,
						Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
						Queries.MONITORING_MESSAGE__SORT_BY_ORDER_DESC,
						monitoringMessageGroupId);

		if (highestOrderMessage != null) {
			monitoringMessage.setOrder(highestOrderMessage.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(monitoringMessage);

		return monitoringMessage;
	}

	@Synchronized
	public MonitoringMessage monitoringMessageMove(
			final MonitoringMessage monitoringMessage, final boolean moveUp) {
		// Find monitoring message to swap with
		val monitoringMessageToSwapWith = databaseManagerService
				.findOneSortedModelObject(MonitoringMessage.class,
						moveUp ? Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP_AND_ORDER_LOWER
								: Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP_AND_ORDER_HIGHER,
						moveUp ? Queries.MONITORING_MESSAGE__SORT_BY_ORDER_DESC
								: Queries.MONITORING_MESSAGE__SORT_BY_ORDER_ASC,
						monitoringMessage.getMonitoringMessageGroup(),
						monitoringMessage.getOrder());

		if (monitoringMessageToSwapWith == null) {
			return null;
		}

		// Swap order
		final int order = monitoringMessage.getOrder();
		monitoringMessage.setOrder(monitoringMessageToSwapWith.getOrder());
		monitoringMessageToSwapWith.setOrder(order);

		databaseManagerService.saveModelObject(monitoringMessage);
		databaseManagerService.saveModelObject(monitoringMessageToSwapWith);

		return monitoringMessageToSwapWith;
	}

	@Synchronized
	public void monitoringMessageSetLinkedMediaObject(
			final MonitoringMessage monitoringMessage,
			final ObjectId linkedMediaObjectId) {
		monitoringMessage.setLinkedMediaObject(linkedMediaObjectId);

		databaseManagerService.saveModelObject(monitoringMessage);
	}

	@Synchronized
	public void monitoringMessageSetLinkedIntermediateSurvey(
			final MonitoringMessage monitoringMessage,
			final ObjectId screeningSurveyId) {
		monitoringMessage.setLinkedIntermediateSurvey(screeningSurveyId);

		databaseManagerService.saveModelObject(monitoringMessage);
	}

	@Synchronized
	public void monitoringMessageSetIsCommandMessage(
			final MonitoringMessage monitoringMessage,
			final boolean isCommandMessage) {
		monitoringMessage.setCommandMessage(isCommandMessage);

		databaseManagerService.saveModelObject(monitoringMessage);
	}

	@Synchronized
	public void monitoringMessageSetTextWithPlaceholders(
			final MonitoringMessage monitoringMessage,
			final LString textWithPlaceholders,
			final List<String> allPossibleMessageVariables)
			throws NotificationMessageException {
		if (textWithPlaceholders == null) {
			monitoringMessage.setTextWithPlaceholders(new LString());
		} else {
			if (!StringValidator.isValidVariableText(textWithPlaceholders,
					allPossibleMessageVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			monitoringMessage.setTextWithPlaceholders(textWithPlaceholders);
		}

		databaseManagerService.saveModelObject(monitoringMessage);
	}

	@Synchronized
	public void monitoringMessageSetStoreResultToVariable(
			final MonitoringMessage monitoringMessage,
			final String variableName) throws NotificationMessageException {
		if (variableName == null || variableName.equals("")) {
			monitoringMessage.setStoreValueToVariableWithName(null);

			databaseManagerService.saveModelObject(monitoringMessage);

		} else {
			if (!StringValidator.isValidVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
			}

			if (variablesManagerService
					.isWriteProtectedReservedVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
			}

			monitoringMessage.setStoreValueToVariableWithName(variableName);

			databaseManagerService.saveModelObject(monitoringMessage);
		}
	}

	@Synchronized
	public void monitoringMessageSetAnswerType(
			final MonitoringMessage monitoringMessage,
			final AnswerTypes answerType) {
		monitoringMessage.setAnswerType(answerType);

		databaseManagerService.saveModelObject(monitoringMessage);
	}

	@Synchronized
	public void monitoringMessageSetAnswerOptionsWithPlaceholders(
			final MonitoringMessage monitoringMessage,
			final LString answerOptions,
			final List<String> allPossibleMessageVariables)
			throws NotificationMessageException {
		if (answerOptions == null) {
			monitoringMessage.setAnswerOptionsWithPlaceholders(new LString());
		} else {
			if (!StringValidator.isValidVariableText(answerOptions,
					allPossibleMessageVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			monitoringMessage.setAnswerOptionsWithPlaceholders(answerOptions);
		}

		databaseManagerService.saveModelObject(monitoringMessage);
	}

	@Synchronized
	public void monitoringMessageUpdateL18n(final ObjectId monitoringMessageId,
			final LString textWithPlaceholders,
			final LString answerOptionsWithPlaceholders) {
		val monitoringMessage = getMonitoringMessage(monitoringMessageId);
		if (monitoringMessage != null) {
			monitoringMessage.setTextWithPlaceholders(textWithPlaceholders);
			monitoringMessage.setAnswerOptionsWithPlaceholders(
					answerOptionsWithPlaceholders);
			databaseManagerService.saveModelObject(monitoringMessage);
		}
	}

	@Synchronized
	public MonitoringMessage monitoringMessageImport(final File file)
			throws FileNotFoundException, IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.MONITORING_MESSAGE);

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof MonitoringMessage) {
				val monitoringMessage = (MonitoringMessage) modelObject;

				// Adjust order
				monitoringMessage.setOrder(0);

				val highestOrderMessage = databaseManagerService
						.findOneSortedModelObject(MonitoringMessage.class,
								Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
								Queries.MONITORING_MESSAGE__SORT_BY_ORDER_DESC,
								monitoringMessage.getMonitoringMessageGroup());

				if (highestOrderMessage != null) {
					monitoringMessage
							.setOrder(highestOrderMessage.getOrder() + 1);
				}

				databaseManagerService.saveModelObject(monitoringMessage);

				return monitoringMessage;
			}
		}

		return null;
	}

	@Synchronized
	public File monitoringMessageExport(
			final MonitoringMessage monitoringMessage) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		log.debug(
				"Recursively collect all model objects related to the monitoring message");
		monitoringMessage.collectThisAndRelatedModelObjectsForExport(
				modelObjectsToExport);

		log.debug("Export monitoring message");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.MONITORING_MESSAGE);
	}

	@Synchronized
	public void monitoringMessageDelete(
			final MonitoringMessage monitoringMessage) {
		databaseManagerService.deleteModelObject(monitoringMessage);
	}

	// Monitoring Message Rule
	@Synchronized
	public MonitoringMessageRule monitoringMessageRuleCreate(
			final ObjectId monitoringMessageId) {
		val monitoringMessageRule = new MonitoringMessageRule(
				monitoringMessageId, 0, "",
				RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "", "");

		val highestOrderRule = databaseManagerService.findOneSortedModelObject(
				MonitoringMessageRule.class,
				Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
				Queries.MONITORING_MESSAGE_RULE__SORT_BY_ORDER_DESC,
				monitoringMessageId);

		if (highestOrderRule != null) {
			monitoringMessageRule.setOrder(highestOrderRule.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(monitoringMessageRule);

		return monitoringMessageRule;
	}

	@Synchronized
	public MonitoringMessageRule monitoringMessageRuleMove(
			final MonitoringMessageRule monitoringMessageRule,
			final boolean moveUp) {
		// Find feedback slide rule to swap with
		val monitoringMessageRuleToSwapWith = databaseManagerService
				.findOneSortedModelObject(MonitoringMessageRule.class,
						moveUp ? Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE_AND_ORDER_LOWER
								: Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE_AND_ORDER_HIGHER,
						moveUp ? Queries.MONITORING_MESSAGE_RULE__SORT_BY_ORDER_DESC
								: Queries.MONITORING_MESSAGE_RULE__SORT_BY_ORDER_ASC,
						monitoringMessageRule.getBelongingMonitoringMessage(),
						monitoringMessageRule.getOrder());

		if (monitoringMessageRuleToSwapWith == null) {
			return null;
		}

		// Swap order
		final int order = monitoringMessageRule.getOrder();
		monitoringMessageRule
				.setOrder(monitoringMessageRuleToSwapWith.getOrder());
		monitoringMessageRuleToSwapWith.setOrder(order);

		databaseManagerService.saveModelObject(monitoringMessageRule);
		databaseManagerService.saveModelObject(monitoringMessageRuleToSwapWith);

		return monitoringMessageRuleToSwapWith;
	}

	@Synchronized
	public void monitoringMessageRuleDelete(
			final MonitoringMessageRule monitoringMessageRule) {
		databaseManagerService.deleteModelObject(monitoringMessageRule);
	}

	// Micro Dialog
	@Synchronized
	public MicroDialog microDialogCreate(final String microDialogName,
			final ObjectId interventionId) {
		val microDialog = new MicroDialog(interventionId, microDialogName, 0);

		if (microDialog.getName().equals("")) {
			microDialog.setName(ImplementationConstants.DEFAULT_OBJECT_NAME);
		}

		val highestOrderMicroDialog = databaseManagerService
				.findOneSortedModelObject(MicroDialog.class,
						Queries.MICRO_DIALOG__BY_INTERVENTION,
						Queries.MICRO_DIALOG__SORT_BY_ORDER_DESC,
						interventionId);

		if (highestOrderMicroDialog != null) {
			microDialog.setOrder(highestOrderMicroDialog.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(microDialog);

		return microDialog;
	}

	@Synchronized
	public MicroDialog microDialogMove(final MicroDialog microDialog,
			final boolean moveLeft) {
		// Find micro dialog to swap with
		val microDialogToSwapWith = databaseManagerService
				.findOneSortedModelObject(MicroDialog.class,
						moveLeft ? Queries.MICRO_DIALOG__BY_INTERVENTION_AND_ORDER_LOWER
								: Queries.MICRO_DIALOG__BY_INTERVENTION_AND_ORDER_HIGHER,
						moveLeft ? Queries.MICRO_DIALOG__SORT_BY_ORDER_DESC
								: Queries.MICRO_DIALOG__SORT_BY_ORDER_ASC,
						microDialog.getIntervention(), microDialog.getOrder());

		if (microDialogToSwapWith == null) {
			return null;
		}

		// Swap order
		final int order = microDialog.getOrder();
		microDialog.setOrder(microDialogToSwapWith.getOrder());
		microDialogToSwapWith.setOrder(order);

		databaseManagerService.saveModelObject(microDialog);
		databaseManagerService.saveModelObject(microDialogToSwapWith);

		return microDialogToSwapWith;
	}

	@Synchronized
	public void microDialogSetName(final MicroDialog microDialog,
			final String newName) {
		if (newName.equals("")) {
			microDialog.setName(ImplementationConstants.DEFAULT_OBJECT_NAME);
		} else {
			microDialog.setName(newName);
		}

		databaseManagerService.saveModelObject(microDialog);
	}

	@Synchronized
	public void microDialogDelete(final MicroDialog microDialogToDelete) {

		databaseManagerService.deleteModelObject(microDialogToDelete);
	}

	// Micro Dialog Message
	@Synchronized
	public MicroDialogMessage microDialogMessageCreate(
			final ObjectId microDialogId) {
		val microDialogMessage = new MicroDialogMessage(microDialogId, 0,
				new LString(), false, null, null, false, false, null, null,
				AnswerTypes.FREE_TEXT,
				ImplementationConstants.DEFAULT_MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED,
				new LString());

		int newOrder = 0;
		val highestOrderMessage = databaseManagerService
				.findOneSortedModelObject(MicroDialogMessage.class,
						Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG,
						Queries.MICRO_DIALOG__SORT_BY_ORDER_DESC,
						microDialogId);
		val highestOrderDecisionPoint = databaseManagerService
				.findOneSortedModelObject(MicroDialogDecisionPoint.class,
						Queries.MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG,
						Queries.MICRO_DIALOG__SORT_BY_ORDER_DESC,
						microDialogId);

		if (highestOrderMessage != null) {
			newOrder = highestOrderMessage.getOrder();
		}
		if (highestOrderDecisionPoint != null
				&& newOrder < highestOrderDecisionPoint.getOrder()) {
			newOrder = highestOrderDecisionPoint.getOrder();
		}
		microDialogMessage.setOrder(newOrder + 1);

		databaseManagerService.saveModelObject(microDialogMessage);

		return microDialogMessage;
	}

	@Synchronized
	public void microDialogMessageSetLinkedMediaObject(
			final MicroDialogMessage microDialogMessage,
			final ObjectId linkedMediaObjectId) {
		microDialogMessage.setLinkedMediaObject(linkedMediaObjectId);

		databaseManagerService.saveModelObject(microDialogMessage);
	}

	@Synchronized
	public void microDialogMessageSetLinkedIntermediateSurvey(
			final MicroDialogMessage microDialogMessage,
			final ObjectId screeningSurveyId) {
		microDialogMessage.setLinkedIntermediateSurvey(screeningSurveyId);

		databaseManagerService.saveModelObject(microDialogMessage);
	}

	@Synchronized
	public void microDialogMessageSetIsCommandMessage(
			final MicroDialogMessage microDialogMessage,
			final boolean isCommandMessage) {
		microDialogMessage.setCommandMessage(isCommandMessage);

		databaseManagerService.saveModelObject(microDialogMessage);
	}

	@Synchronized
	public void microDialogMessageSetMessageExpectsAnswer(
			final MicroDialogMessage microDialogMessage,
			final boolean messageExpectsAnswer) {
		microDialogMessage.setMessageExpectsAnswer(messageExpectsAnswer);

		databaseManagerService.saveModelObject(microDialogMessage);
	}

	@Synchronized
	public void microDialogMessageSetMessageBlocksMicroDialogUntilAnsweredCheckBox(
			final MicroDialogMessage microDialogMessage,
			final boolean messageBlocksMicroDialog) {
		microDialogMessage.setMessageBlocksMicroDialogUntilAnswered(
				messageBlocksMicroDialog);

		databaseManagerService.saveModelObject(microDialogMessage);
	}

	@Synchronized
	public void microDialogMessageSetTextWithPlaceholders(
			final MicroDialogMessage microDialogMessage,
			final LString textWithPlaceholders,
			final List<String> allPossibleMessageVariables)
			throws NotificationMessageException {
		if (textWithPlaceholders == null) {
			microDialogMessage.setTextWithPlaceholders(new LString());
		} else {
			if (!StringValidator.isValidVariableText(textWithPlaceholders,
					allPossibleMessageVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			microDialogMessage.setTextWithPlaceholders(textWithPlaceholders);
		}

		databaseManagerService.saveModelObject(microDialogMessage);
	}

	@Synchronized
	public void microDialogMessageSetStoreResultToVariable(
			final MicroDialogMessage microDialogMessage,
			final String variableName) throws NotificationMessageException {
		if (variableName == null || variableName.equals("")) {
			microDialogMessage.setStoreValueToVariableWithName(null);

			databaseManagerService.saveModelObject(microDialogMessage);

		} else {
			if (!StringValidator.isValidVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
			}

			if (variablesManagerService
					.isWriteProtectedReservedVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
			}

			microDialogMessage.setStoreValueToVariableWithName(variableName);

			databaseManagerService.saveModelObject(microDialogMessage);
		}
	}

	@Synchronized
	public void microDialogMessageSetNoReplyValue(
			final MicroDialogMessage microDialogMessage,
			final String newValue) {
		microDialogMessage.setNoReplyValue(newValue);

		databaseManagerService.saveModelObject(microDialogMessage);
	}

	@Synchronized
	public void microDialogMessageSetMinutesUntilMessageIsHandledAsUnanswered(
			final MicroDialogMessage microDialogMessage, final int newValue) {
		microDialogMessage
				.setMinutesUntilMessageIsHandledAsUnanswered(newValue);

		databaseManagerService.saveModelObject(microDialogMessage);
	}

	@Synchronized
	public void microDialogMessageSetAnswerType(
			final MicroDialogMessage microDialogMessage,
			final AnswerTypes answerType) {
		microDialogMessage.setAnswerType(answerType);

		databaseManagerService.saveModelObject(microDialogMessage);
	}

	@Synchronized
	public void microDialogMessageSetAnswerOptionsWithPlaceholders(
			final MicroDialogMessage microDialogMessage,
			final LString answerOptions,
			final List<String> allPossibleMessageVariables)
			throws NotificationMessageException {
		if (answerOptions == null) {
			microDialogMessage.setAnswerOptionsWithPlaceholders(new LString());
		} else {
			if (!StringValidator.isValidVariableText(answerOptions,
					allPossibleMessageVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			microDialogMessage.setAnswerOptionsWithPlaceholders(answerOptions);
		}

		databaseManagerService.saveModelObject(microDialogMessage);
	}

	@Synchronized
	public void microDialogMessageUpdateL18n(
			final ObjectId microDialogMessageId,
			final LString textWithPlaceholders,
			final LString answerOptionsWithPlaceholders) {
		val microDialogMessage = getMicroDialogMessage(microDialogMessageId);
		if (microDialogMessage != null) {
			microDialogMessage.setTextWithPlaceholders(textWithPlaceholders);
			microDialogMessage.setAnswerOptionsWithPlaceholders(
					answerOptionsWithPlaceholders);
			databaseManagerService.saveModelObject(microDialogMessage);
		}
	}

	@Synchronized
	public MicroDialogMessage microDialogMessageImport(final File file)
			throws FileNotFoundException, IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.MICRO_DIALOG_MESSAGE);

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof MicroDialogMessage) {
				val microDialogMessage = (MicroDialogMessage) modelObject;

				// Adjust order
				microDialogMessage.setOrder(0);

				int newOrder = 0;
				val highestOrderMessage = databaseManagerService
						.findOneSortedModelObject(MicroDialogMessage.class,
								Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG,
								Queries.MICRO_DIALOG__SORT_BY_ORDER_DESC,
								microDialogMessage.getMicroDialog());
				val highestOrderDecisionPoint = databaseManagerService
						.findOneSortedModelObject(
								MicroDialogDecisionPoint.class,
								Queries.MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG,
								Queries.MICRO_DIALOG__SORT_BY_ORDER_DESC,
								microDialogMessage.getMicroDialog());

				if (highestOrderMessage != null) {
					newOrder = highestOrderMessage.getOrder();
				}
				if (highestOrderDecisionPoint != null
						&& newOrder < highestOrderDecisionPoint.getOrder()) {
					newOrder = highestOrderDecisionPoint.getOrder();
				}
				microDialogMessage.setOrder(newOrder + 1);

				databaseManagerService.saveModelObject(microDialogMessage);

				return microDialogMessage;
			}
		}

		return null;
	}

	@Synchronized
	public File microDialogMessageExport(
			final MicroDialogMessage microDialogMessage) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		log.debug(
				"Recursively collect all model objects related to the micro dialog message");
		microDialogMessage.collectThisAndRelatedModelObjectsForExport(
				modelObjectsToExport);

		log.debug("Export micro dialog message");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.MICRO_DIALOG_MESSAGE);
	}

	@Synchronized
	public void microDialogMessageDelete(
			final MicroDialogMessage microDialogMessage)
			throws NotificationMessageException {

		val linkedMicroDialogRulesWhenTrue = databaseManagerService
				.findModelObjects(MicroDialogRule.class,
						Queries.MICRO_DIALOG_RULE__BY_NEXT_MICRO_DIALOG_MESSAGE_WHEN_TRUE,
						microDialogMessage.getId());
		if (linkedMicroDialogRulesWhenTrue.iterator().hasNext()) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_MESSAGE_CANT_DELETE);
		}

		val linkedMicroDialogRulesWhenFalse = databaseManagerService
				.findModelObjects(MicroDialogRule.class,
						Queries.MICRO_DIALOG_RULE__BY_NEXT_MICRO_DIALOG_MESSAGE_WHEN_FALSE,
						microDialogMessage.getId());
		if (linkedMicroDialogRulesWhenFalse.iterator().hasNext()) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__MICRO_DIALOG_MESSAGE_CANT_DELETE);
		}

		databaseManagerService.deleteModelObject(microDialogMessage);
	}

	// Micro Dialog Message Rule
	@Synchronized
	public MicroDialogMessageRule microDialogMessageRuleCreate(
			final ObjectId microDialogMessageId) {
		val microDialogMessageRule = new MicroDialogMessageRule(
				microDialogMessageId, 0, "",
				RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "", "");

		val highestOrderRule = databaseManagerService.findOneSortedModelObject(
				MicroDialogMessageRule.class,
				Queries.MICRO_DIALOG_MESSAGE_RULE__BY_MICRO_DIALOG_MESSAGE,
				Queries.MICRO_DIALOG_MESSAGE_RULE__SORT_BY_ORDER_DESC,
				microDialogMessageId);

		if (highestOrderRule != null) {
			microDialogMessageRule.setOrder(highestOrderRule.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(microDialogMessageRule);

		return microDialogMessageRule;
	}

	@Synchronized
	public MicroDialogMessageRule microDialogMessageRuleMove(
			final MicroDialogMessageRule microDialogMessageRule,
			final boolean moveUp) {
		// Find feedback slide rule to swap with
		val microDialogMessageRuleToSwapWith = databaseManagerService
				.findOneSortedModelObject(MicroDialogMessageRule.class,
						moveUp ? Queries.MICRO_DIALOG_MESSAGE_RULE__BY_MICRO_DIALOG_MESSAGE_AND_ORDER_LOWER
								: Queries.MICRO_DIALOG_MESSAGE_RULE__BY_MICRO_DIALOG_MESSAGE_AND_ORDER_HIGHER,
						moveUp ? Queries.MICRO_DIALOG_MESSAGE_RULE__SORT_BY_ORDER_DESC
								: Queries.MICRO_DIALOG_MESSAGE_RULE__SORT_BY_ORDER_ASC,
						microDialogMessageRule.getBelongingMicroDialogMessage(),
						microDialogMessageRule.getOrder());

		if (microDialogMessageRuleToSwapWith == null) {
			return null;
		}

		// Swap order
		final int order = microDialogMessageRule.getOrder();
		microDialogMessageRule
				.setOrder(microDialogMessageRuleToSwapWith.getOrder());
		microDialogMessageRuleToSwapWith.setOrder(order);

		databaseManagerService.saveModelObject(microDialogMessageRule);
		databaseManagerService
				.saveModelObject(microDialogMessageRuleToSwapWith);

		return microDialogMessageRuleToSwapWith;
	}

	@Synchronized
	public void microDialogMessageRuleDelete(
			final MicroDialogMessageRule microDialogMessageRule) {
		databaseManagerService.deleteModelObject(microDialogMessageRule);
	}

	// Micro Dialog Decision Point
	@Synchronized
	public MicroDialogDecisionPoint microDialogDecisionPointCreate(
			final ObjectId microDialogId) {
		val microDialogDecisionPoint = new MicroDialogDecisionPoint("",
				microDialogId, 0);

		int newOrder = 0;
		val highestOrderMessage = databaseManagerService
				.findOneSortedModelObject(MicroDialogMessage.class,
						Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG,
						Queries.MICRO_DIALOG__SORT_BY_ORDER_DESC,
						microDialogId);
		val highestOrderDecisionPoint = databaseManagerService
				.findOneSortedModelObject(MicroDialogDecisionPoint.class,
						Queries.MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG,
						Queries.MICRO_DIALOG__SORT_BY_ORDER_DESC,
						microDialogId);

		if (highestOrderMessage != null) {
			newOrder = highestOrderMessage.getOrder();
		}
		if (highestOrderDecisionPoint != null
				&& newOrder < highestOrderDecisionPoint.getOrder()) {
			newOrder = highestOrderDecisionPoint.getOrder();
		}
		microDialogDecisionPoint.setOrder(newOrder + 1);

		databaseManagerService.saveModelObject(microDialogDecisionPoint);

		return microDialogDecisionPoint;
	}

	@Synchronized
	public void microDialogDecisionPointSetComment(
			final MicroDialogDecisionPoint microDialogDecisionPoint,
			final String comment) throws NotificationMessageException {
		if (comment == null) {
			microDialogDecisionPoint.setComment("");
		} else {
			microDialogDecisionPoint.setComment(comment);
		}

		databaseManagerService.saveModelObject(microDialogDecisionPoint);
	}

	@Synchronized
	public MicroDialogDecisionPoint microDialogDecisionPointImport(
			final File file) throws FileNotFoundException, IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.MICRO_DIALOG_DECISION_POINT);

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof MicroDialogDecisionPoint) {
				val microDialogDecisionPoint = (MicroDialogDecisionPoint) modelObject;

				// Adjust order
				int newOrder = 0;
				val highestOrderMessage = databaseManagerService
						.findOneSortedModelObject(MicroDialogMessage.class,
								Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG,
								Queries.MICRO_DIALOG__SORT_BY_ORDER_DESC,
								microDialogDecisionPoint.getMicroDialog());
				val highestOrderDecisionPoint = databaseManagerService
						.findOneSortedModelObject(
								MicroDialogDecisionPoint.class,
								Queries.MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG,
								Queries.MICRO_DIALOG__SORT_BY_ORDER_DESC,
								microDialogDecisionPoint.getMicroDialog());

				if (highestOrderMessage != null) {
					newOrder = highestOrderMessage.getOrder();
				}
				if (highestOrderDecisionPoint != null
						&& newOrder < highestOrderDecisionPoint.getOrder()) {
					newOrder = highestOrderDecisionPoint.getOrder();
				}
				microDialogDecisionPoint.setOrder(newOrder + 1);

				databaseManagerService
						.saveModelObject(microDialogDecisionPoint);

				return microDialogDecisionPoint;
			}
		}

		return null;
	}

	@Synchronized
	public File microDialogDecisionPointExport(
			final MicroDialogDecisionPoint microDialogDecisionPoint) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		log.debug(
				"Recursively collect all model objects related to the micro dialog decision point");
		microDialogDecisionPoint.collectThisAndRelatedModelObjectsForExport(
				modelObjectsToExport);

		log.debug("Export micro dialog decision point");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.MICRO_DIALOG_DECISION_POINT);
	}

	@Synchronized
	public void microDialogDecisionPointDelete(
			final MicroDialogDecisionPoint microDialogDecisionPoint) {
		databaseManagerService.deleteModelObject(microDialogDecisionPoint);
	}

	// Micro Dialog Element (Interface)
	@Synchronized
	public MicroDialogElementInterface microDialogElementMove(
			final MicroDialogElementInterface microDialogElement,
			final boolean moveUp) {
		// Find micro dialog element (message or decision point) to swap with
		MicroDialogElementInterface microDialogElementToSwapWith;

		val microDialogMessageToCompareWith = databaseManagerService
				.findOneSortedModelObject(MicroDialogMessage.class,
						moveUp ? Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG_AND_ORDER_LOWER
								: Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG_AND_ORDER_HIGHER,
						moveUp ? Queries.MICRO_DIALOG_MESSAGE__SORT_BY_ORDER_DESC
								: Queries.MICRO_DIALOG_MESSAGE__SORT_BY_ORDER_ASC,
						microDialogElement.getMicroDialog(),
						microDialogElement.getOrder());
		val microDialogDecisionPointToCompareWith = databaseManagerService
				.findOneSortedModelObject(MicroDialogDecisionPoint.class,
						moveUp ? Queries.MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG_AND_ORDER_LOWER
								: Queries.MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG_AND_ORDER_HIGHER,
						moveUp ? Queries.MICRO_DIALOG_DECISION_POINT__SORT_BY_ORDER_DESC
								: Queries.MICRO_DIALOG_DECISION_POINT__SORT_BY_ORDER_ASC,
						microDialogElement.getMicroDialog(),
						microDialogElement.getOrder());

		if (microDialogMessageToCompareWith == null
				&& microDialogDecisionPointToCompareWith == null) {
			return null;
		}

		if (microDialogMessageToCompareWith == null) {
			microDialogElementToSwapWith = microDialogDecisionPointToCompareWith;
		} else if (microDialogDecisionPointToCompareWith == null) {
			microDialogElementToSwapWith = microDialogMessageToCompareWith;
		} else if (moveUp) {
			if (microDialogMessageToCompareWith
					.getOrder() > microDialogDecisionPointToCompareWith
							.getOrder()) {
				microDialogElementToSwapWith = microDialogMessageToCompareWith;
			} else {
				microDialogElementToSwapWith = microDialogDecisionPointToCompareWith;
			}
		} else {
			if (microDialogMessageToCompareWith
					.getOrder() < microDialogDecisionPointToCompareWith
							.getOrder()) {
				microDialogElementToSwapWith = microDialogMessageToCompareWith;
			} else {
				microDialogElementToSwapWith = microDialogDecisionPointToCompareWith;
			}
		}

		// Swap order
		final int order = microDialogElement.getOrder();
		microDialogElement.setOrder(microDialogElementToSwapWith.getOrder());
		microDialogElementToSwapWith.setOrder(order);

		databaseManagerService
				.saveModelObject((ModelObject) microDialogElement);
		databaseManagerService
				.saveModelObject((ModelObject) microDialogElementToSwapWith);

		return microDialogElementToSwapWith;
	}

	// Micro Dialog Rule
	@Synchronized
	public MicroDialogRule microDialogRuleCreate(
			final ObjectId microDialogDecisionPointId,
			final ObjectId parentMicroDialogRuleId) {
		val microDialogRule = new MicroDialogRule("",
				RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "", "",
				parentMicroDialogRuleId, 0, null, microDialogDecisionPointId,
				null, null, null, false, false);

		val highestOrderRule = databaseManagerService.findOneSortedModelObject(
				MicroDialogRule.class,
				Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT_AND_PARENT,
				Queries.MICRO_DIALOG_RULE__SORT_BY_ORDER_DESC,
				microDialogDecisionPointId, parentMicroDialogRuleId);

		if (highestOrderRule != null) {
			microDialogRule.setOrder(highestOrderRule.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(microDialogRule);

		return microDialogRule;
	}

	/**
	 * @param movement
	 *            Relative movement to target: 0=as child, 1=above, 2=below
	 * @param microDialogRuleIdToMove
	 *            {@link MicroDialogRule} to move
	 * @param parentItemId
	 *            The new parent {@link MicroDialogRule}
	 * @param sameLevelTargetItemId
	 *            The new relative {@link MicroDialogRule} above/below the
	 *            {@link MicroDialogRule} to move
	 * @param microDialogDecisionPointId
	 */
	@Synchronized
	public void microDialogRuleMove(final int movement,
			final ObjectId microDialogRuleIdToMove, final ObjectId parentItemId,
			final ObjectId sameLevelTargetItemId,
			final ObjectId microDialogDecisionPointId) {
		if (movement == 0) {
			// At the beginning of sublist of target

			// Move all items in sublist one step down
			val otherMicroDialogRulesToMove = databaseManagerService
					.findModelObjects(MicroDialogRule.class,
							Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT_AND_PARENT,
							microDialogDecisionPointId, parentItemId);
			for (val otherMicroDialogRuleToMove : otherMicroDialogRulesToMove) {
				otherMicroDialogRuleToMove
						.setOrder(otherMicroDialogRuleToMove.getOrder() + 1);
				databaseManagerService
						.saveModelObject(otherMicroDialogRuleToMove);
			}

			// Set target as new parent
			val microDialogRuleToMove = databaseManagerService
					.getModelObjectById(MicroDialogRule.class,
							microDialogRuleIdToMove);
			microDialogRuleToMove.setIsSubRuleOfMonitoringRule(parentItemId);

			// Set at beginning of list
			microDialogRuleToMove.setOrder(0);

			databaseManagerService.saveModelObject(microDialogRuleToMove);
		} else if (movement == 1 || movement == 2) {
			// Move above or below

			// Move all items in sublist after target one step
			// down
			val referenceTarget = databaseManagerService.getModelObjectById(
					MicroDialogRule.class, sameLevelTargetItemId);

			val otherMicroDialogRulesToMove = databaseManagerService
					.findModelObjects(MicroDialogRule.class,
							Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT_AND_PARENT_AND_ORDER_HIGHER,
							microDialogDecisionPointId,
							referenceTarget.getIsSubRuleOfMonitoringRule(),
							referenceTarget.getOrder());
			for (val otherMicroDialogRuleToMove : otherMicroDialogRulesToMove) {
				otherMicroDialogRuleToMove
						.setOrder(otherMicroDialogRuleToMove.getOrder() + 1);
				databaseManagerService
						.saveModelObject(otherMicroDialogRuleToMove);
			}

			// Set parent of target to rule
			val microDialogRuleToMove = databaseManagerService
					.getModelObjectById(MicroDialogRule.class,
							microDialogRuleIdToMove);
			microDialogRuleToMove.setIsSubRuleOfMonitoringRule(
					referenceTarget.getIsSubRuleOfMonitoringRule());

			if (movement == 1) {
				// Adjust order for moved above

				// Set order to former order of target
				microDialogRuleToMove.setOrder(referenceTarget.getOrder());

				// Set order of target one down
				referenceTarget.setOrder(referenceTarget.getOrder() + 1);
				databaseManagerService.saveModelObject(referenceTarget);
			} else {
				// Adjust order for moved below

				// Set order to former order of target plus 1
				microDialogRuleToMove.setOrder(referenceTarget.getOrder() + 1);
			}

			databaseManagerService.saveModelObject(microDialogRuleToMove);
		}
	}

	@Synchronized
	public void microDialogRuleSetStoreResultToVariable(
			final MicroDialogRule microDialogRule, final String variableName)
			throws NotificationMessageException {
		if (variableName == null || variableName.equals("")) {
			microDialogRule.setStoreValueToVariableWithName(null);

			databaseManagerService.saveModelObject(microDialogRule);

		} else {
			if (!StringValidator.isValidVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
			}

			if (variablesManagerService
					.isWriteProtectedReservedVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
			}

			microDialogRule.setStoreValueToVariableWithName(variableName);

			databaseManagerService.saveModelObject(microDialogRule);
		}
	}

	@Synchronized
	public void microDialogRuleSetNextMicroDialogWhenTrue(
			final MicroDialogRule microDialogRule,
			final ObjectId newMicroDialogId) {
		microDialogRule.setNextMicroDialogWhenTrue(newMicroDialogId);

		databaseManagerService.saveModelObject(microDialogRule);
	}

	@Synchronized
	public void microDialogRuleSetNextMicroDialogMessageWhenTrue(
			final MicroDialogRule microDialogRule,
			final ObjectId newMicroDialogMessageId) {
		microDialogRule
				.setNextMicroDialogMessageWhenTrue(newMicroDialogMessageId);

		databaseManagerService.saveModelObject(microDialogRule);
	}

	@Synchronized
	public void microDialogRuleSetNextMicroDialogMessageWhenFalse(
			final MicroDialogRule microDialogRule,
			final ObjectId newMicroDialogMessageId) {
		microDialogRule
				.setNextMicroDialogMessageWhenFalse(newMicroDialogMessageId);

		databaseManagerService.saveModelObject(microDialogRule);
	}

	@Synchronized
	public void microDialogRuleSetStopMicroDialogWhenTrue(
			final MicroDialogRule microDialogRule, final boolean newValue) {
		microDialogRule.setStopMicroDialogWhenTrue(newValue);

		databaseManagerService.saveModelObject(microDialogRule);
	}

	@Synchronized
	public void microDialogRuleSetLeaveDecisionPointWhenTrue(
			final MicroDialogRule microDialogRule, final boolean newValue) {
		microDialogRule.setLeaveDecisionPointWhenTrue(newValue);

		databaseManagerService.saveModelObject(microDialogRule);
	}

	@Synchronized
	public MicroDialogRule microDialogRuleImport(final File file)
			throws FileNotFoundException, IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.MICRO_DIALOG_RULE);

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof MicroDialogRule) {
				val microDialogRule = (MicroDialogRule) modelObject;

				// Adjust order
				microDialogRule.setOrder(0);

				val highestOrderRule = databaseManagerService
						.findOneSortedModelObject(MicroDialogRule.class,
								Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT_AND_PARENT,
								Queries.MICRO_DIALOG_RULE__SORT_BY_ORDER_DESC,
								microDialogRule.getMicroDialogDecisionPoint(),
								microDialogRule.getIsSubRuleOfMonitoringRule());

				if (highestOrderRule != null) {
					microDialogRule.setOrder(highestOrderRule.getOrder() + 1);
				}

				databaseManagerService.saveModelObject(microDialogRule);

				return microDialogRule;
			}
		}

		return null;
	}

	@Synchronized
	public File microDialogRuleExport(final ObjectId microDialogRuleId) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		val microDialogRule = databaseManagerService
				.getModelObjectById(MicroDialogRule.class, microDialogRuleId);

		log.debug(
				"Recursively collect all model objects related to the micro dialog rule");
		microDialogRule.collectThisAndRelatedModelObjectsForExport(
				modelObjectsToExport);

		log.debug("Export micro dialog rule");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.MICRO_DIALOG_RULE);
	}

	@Synchronized
	public void microDialogRuleDelete(final ObjectId microDialogRuleId) {
		databaseManagerService.deleteModelObject(MicroDialogRule.class,
				microDialogRuleId);
	}

	// Media Object
	@Synchronized
	public MediaObject mediaObjectCreateWithFile(final File temporaryFile,
			final String originalFileName,
			final MediaObjectTypes originalFileType) {

		MediaObject mediaObject;
		try {
			mediaObject = new MediaObject(originalFileType, originalFileName,
					temporaryFile);
		} catch (final Exception e) {
			log.error("Can't create media object: {}", e.getMessage());
			return null;
		}

		databaseManagerService.saveModelObject(mediaObject);

		return mediaObject;
	}

	@Synchronized
	public MediaObject mediaObjectCreateWithURL(final String url) {

		if (url == null) {
			log.error("Can't create media object with empty URL");
			return null;
		}

		try {
			new URL(url);
		} catch (final MalformedURLException e) {
			log.error("Cant' create media object with URL: {}", e.getMessage());
			return null;
		}

		val mediaObject = new MediaObject(MediaObjectTypes.URL, url, null, url);

		databaseManagerService.saveModelObject(mediaObject);

		return mediaObject;
	}

	@Synchronized
	public void mediaObjectDelete(final MediaObject mediaObject) {
		databaseManagerService.deleteModelObject(mediaObject);
	}

	@Synchronized
	public File mediaObjectGetFile(final MediaObject mediaObject,
			final FILE_STORES fileStore) {
		return fileStorageManagerService
				.getFileByReference(mediaObject.getFileReference(), fileStore);
	}

	// Monitoring Rule
	@Synchronized
	private MonitoringRule monitoringRuleCreate(final ObjectId interventionId,
			final ObjectId parentMonitoringRuleId,
			final MonitoringRuleTypes type) {
		val monitoringRule = new MonitoringRule("",
				RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "", "",
				parentMonitoringRuleId, 0, null, false, false, null, false,
				null, type, interventionId,
				ImplementationConstants.DEFAULT_HOUR_TO_SEND_MESSAGE,
				ImplementationConstants.DEFAULT_MINUTES_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED,
				false, false);

		val highestOrderRule = databaseManagerService.findOneSortedModelObject(
				MonitoringRule.class,
				Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
				Queries.MONITORING_RULE__SORT_BY_ORDER_DESC, interventionId,
				parentMonitoringRuleId);

		if (highestOrderRule != null) {
			monitoringRule.setOrder(highestOrderRule.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(monitoringRule);

		return monitoringRule;
	}

	@Synchronized
	public MonitoringRule monitoringRuleCreate(final ObjectId interventionId,
			final ObjectId parentMonitoringRuleId) {
		val monitoringRule = monitoringRuleCreate(interventionId,
				parentMonitoringRuleId, MonitoringRuleTypes.NORMAL);
		return monitoringRule;
	}

	/**
	 * @param movement
	 *            Relative movement to target: 0=as child, 1=above, 2=below
	 * @param monitoringRuleIdToMove
	 *            {@link MonitoringRule} to move
	 * @param parentItemId
	 *            The new parent {@link MonitoringRule}
	 * @param sameLevelTargetItemId
	 *            The new relative {@link MonitoringRule} above/below the
	 *            {@link MonitoringRule} to move
	 * @param interventionId
	 * @param objectId
	 * @param objectId
	 */
	@Synchronized
	public void monitoringRuleMove(final int movement,
			final ObjectId monitoringRuleIdToMove, final ObjectId parentItemId,
			final ObjectId sameLevelTargetItemId,
			final ObjectId interventionId) {
		if (movement == 0) {
			// At the beginning of sublist of target

			// Move all items in sublist one step down
			val otherMonitoringRulesToMove = databaseManagerService
					.findModelObjects(MonitoringRule.class,
							Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
							interventionId, parentItemId);
			for (val otherMonitoringRuleToMove : otherMonitoringRulesToMove) {
				otherMonitoringRuleToMove
						.setOrder(otherMonitoringRuleToMove.getOrder() + 1);
				databaseManagerService
						.saveModelObject(otherMonitoringRuleToMove);
			}

			// Set target as new parent
			val monitoringRuleToMove = databaseManagerService
					.getModelObjectById(MonitoringRule.class,
							monitoringRuleIdToMove);
			monitoringRuleToMove.setIsSubRuleOfMonitoringRule(parentItemId);

			// Set at beginning of list
			monitoringRuleToMove.setOrder(0);

			databaseManagerService.saveModelObject(monitoringRuleToMove);
		} else if (movement == 1 || movement == 2) {
			// Move above or below

			// Move all items in sublist after target one step
			// down
			val referenceTarget = databaseManagerService.getModelObjectById(
					MonitoringRule.class, sameLevelTargetItemId);

			val otherMonitoringRulesToMove = databaseManagerService
					.findModelObjects(MonitoringRule.class,
							Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT_AND_ORDER_HIGHER,
							interventionId,
							referenceTarget.getIsSubRuleOfMonitoringRule(),
							referenceTarget.getOrder());
			for (val otherMonitoringRuleToMove : otherMonitoringRulesToMove) {
				otherMonitoringRuleToMove
						.setOrder(otherMonitoringRuleToMove.getOrder() + 1);
				databaseManagerService
						.saveModelObject(otherMonitoringRuleToMove);
			}

			// Set parent of target to rule
			val monitoringRuleToMove = databaseManagerService
					.getModelObjectById(MonitoringRule.class,
							monitoringRuleIdToMove);
			monitoringRuleToMove.setIsSubRuleOfMonitoringRule(
					referenceTarget.getIsSubRuleOfMonitoringRule());

			if (movement == 1) {
				// Adjust order for moved above

				// Set order to former order of target
				monitoringRuleToMove.setOrder(referenceTarget.getOrder());

				// Set order of target one down
				referenceTarget.setOrder(referenceTarget.getOrder() + 1);
				databaseManagerService.saveModelObject(referenceTarget);
			} else {
				// Adjust order for moved below

				// Set order to former order of target plus 1
				monitoringRuleToMove.setOrder(referenceTarget.getOrder() + 1);
			}

			databaseManagerService.saveModelObject(monitoringRuleToMove);
		}
	}

	@Synchronized
	public void monitoringRuleSetSendMessageIfTrue(
			final MonitoringRule monitoringRule, final boolean newValue) {
		monitoringRule.setSendMessageIfTrue(newValue);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	@Synchronized
	public void monitoringRuleChangeActivateMicroDialogIfTrue(
			final MonitoringRule monitoringRule, final boolean newValue) {
		monitoringRule.setActivateMicroDialogIfTrue(newValue);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	@Synchronized
	public void monitoringRuleSetSendMessageToSupervisor(
			final MonitoringRule monitoringRule, final boolean newValue) {
		monitoringRule.setSendMessageToSupervisor(newValue);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	@Synchronized
	public void monitoringRuleSetMarkCaseAsSolvedIfTrue(
			final MonitoringRule monitoringRule, final boolean newValue) {
		monitoringRule.setMarkCaseAsSolvedWhenTrue(newValue);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	@Synchronized
	public void monitoringRuleSetStopInterventionIfTrue(
			final MonitoringRule monitoringRule, final boolean newValue) {
		monitoringRule.setStopInterventionWhenTrue(newValue);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	@Synchronized
	public void monitoringRuleSetRelatedMonitoringMessageGroup(
			final MonitoringRule monitoringRule,
			final ObjectId newMonitoringMessageGroupId) {
		monitoringRule
				.setRelatedMonitoringMessageGroup(newMonitoringMessageGroupId);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	@Synchronized
	public void monitoringRuleChangeRelatedMicroDialog(
			final MonitoringRule monitoringRule,
			final ObjectId newMicroDialogId) {
		monitoringRule.setRelatedMicroDialog(newMicroDialogId);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	@Synchronized
	public void monitoringRuleSetStoreResultToVariable(
			final MonitoringRule monitoringRule, final String variableName)
			throws NotificationMessageException {
		if (variableName == null || variableName.equals("")) {
			monitoringRule.setStoreValueToVariableWithName(null);

			databaseManagerService.saveModelObject(monitoringRule);

		} else {
			if (!StringValidator.isValidVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
			}

			if (variablesManagerService
					.isWriteProtectedReservedVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
			}

			monitoringRule.setStoreValueToVariableWithName(variableName);

			databaseManagerService.saveModelObject(monitoringRule);
		}
	}

	@Synchronized
	public void monitoringRuleSetHourToSendMessageOrActivateMicroDialog(
			final MonitoringRule monitoringRule, final int newValue) {
		monitoringRule.setHourToSendMessageOrActivateMicroDialog(newValue);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	@Synchronized
	public void monitoringRuleSetMinutesUntilMessageIsHandledAsUnanswered(
			final MonitoringRule monitoringRule, final int newValue) {
		monitoringRule.setMinutesUntilMessageIsHandledAsUnanswered(newValue);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	@Synchronized
	public MonitoringRule monitoringRuleImport(final File file)
			throws FileNotFoundException, IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.MONITORING_RULE);

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof MonitoringRule) {
				val monitoringRule = (MonitoringRule) modelObject;

				// Adjust order
				monitoringRule.setOrder(0);

				val highestOrderRule = databaseManagerService
						.findOneSortedModelObject(MonitoringRule.class,
								Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
								Queries.MONITORING_RULE__SORT_BY_ORDER_DESC,
								monitoringRule.getIntervention(),
								monitoringRule.getIsSubRuleOfMonitoringRule());

				if (highestOrderRule != null) {
					monitoringRule.setOrder(highestOrderRule.getOrder() + 1);
				}

				databaseManagerService.saveModelObject(monitoringRule);

				return monitoringRule;
			}
		}

		return null;
	}

	@Synchronized
	public File monitoringRuleExport(final ObjectId monitoringRuleId) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		val monitoringRule = databaseManagerService
				.getModelObjectById(MonitoringRule.class, monitoringRuleId);

		log.debug(
				"Recursively collect all model objects related to the monitoringRule");
		monitoringRule.collectThisAndRelatedModelObjectsForExport(
				modelObjectsToExport);

		log.debug("Export monitoring rule");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.MONITORING_RULE);
	}

	@Synchronized
	public void monitoringRuleDelete(final ObjectId monitoringRuleId) {
		databaseManagerService.deleteModelObject(MonitoringRule.class,
				monitoringRuleId);
	}

	// Monitoring Reply Rule
	@Synchronized
	public MonitoringReplyRule monitoringReplyRuleCreate(
			final ObjectId monitoringRuleId,
			final ObjectId parentMonitoringReplyRuleId,
			final boolean isGotAnswerRule) {
		val monitoringReplyRule = new MonitoringReplyRule("",
				RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "", "",
				parentMonitoringReplyRuleId, 0, null, false, false, null, false,
				null, isGotAnswerRule ? monitoringRuleId : null,
				isGotAnswerRule ? null : monitoringRuleId);

		val highestOrderRule = databaseManagerService.findOneSortedModelObject(
				MonitoringReplyRule.class,
				isGotAnswerRule
						? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER
						: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
				Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_DESC,
				monitoringRuleId, parentMonitoringReplyRuleId);

		if (highestOrderRule != null) {
			monitoringReplyRule.setOrder(highestOrderRule.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(monitoringReplyRule);

		return monitoringReplyRule;
	}

	@Synchronized
	public void monitoringReplyRuleMove(final int movement,
			final ObjectId monitoringReplyRuleIdToMove,
			final ObjectId parentItemId, final ObjectId sameLevelTargetItemId,
			final ObjectId monitoringRuleId, final boolean isGotAnswerRule) {
		if (movement == 0) {
			// At the beginning of sublist of target

			// Move all items in sublist one step down
			val otherMonitoringRulesToMove = databaseManagerService
					.findModelObjects(MonitoringReplyRule.class,
							isGotAnswerRule
									? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER
									: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
							monitoringRuleId, parentItemId);
			for (val otherMonitoringRuleToMove : otherMonitoringRulesToMove) {
				otherMonitoringRuleToMove
						.setOrder(otherMonitoringRuleToMove.getOrder() + 1);
				databaseManagerService
						.saveModelObject(otherMonitoringRuleToMove);
			}

			// Set target as new parent
			val monitoringRuleToMove = databaseManagerService
					.getModelObjectById(MonitoringReplyRule.class,
							monitoringReplyRuleIdToMove);
			monitoringRuleToMove.setIsSubRuleOfMonitoringRule(parentItemId);

			// Set at beginning of list
			monitoringRuleToMove.setOrder(0);

			databaseManagerService.saveModelObject(monitoringRuleToMove);
		} else if (movement == 1 || movement == 2) {
			// Move above or below

			// Move all items in sublist after target one step
			// down
			val referenceTarget = databaseManagerService.getModelObjectById(
					MonitoringReplyRule.class, sameLevelTargetItemId);

			val otherMonitoringRulesToMove = databaseManagerService
					.findModelObjects(MonitoringReplyRule.class,
							isGotAnswerRule
									? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_AND_ORDER_HIGHER_ONLY_GOT_ANSWER
									: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_AND_ORDER_HIGHER_ONLY_GOT_NO_ANSWER,
							monitoringRuleId,
							referenceTarget.getIsSubRuleOfMonitoringRule(),
							referenceTarget.getOrder());
			for (val otherMonitoringRuleToMove : otherMonitoringRulesToMove) {
				otherMonitoringRuleToMove
						.setOrder(otherMonitoringRuleToMove.getOrder() + 1);
				databaseManagerService
						.saveModelObject(otherMonitoringRuleToMove);
			}

			// Set parent of target to rule
			val monitoringRuleToMove = databaseManagerService
					.getModelObjectById(MonitoringReplyRule.class,
							monitoringReplyRuleIdToMove);
			monitoringRuleToMove.setIsSubRuleOfMonitoringRule(
					referenceTarget.getIsSubRuleOfMonitoringRule());

			if (movement == 1) {
				// Adjust order for moved above

				// Set order to former order of target
				monitoringRuleToMove.setOrder(referenceTarget.getOrder());

				// Set order of target one down
				referenceTarget.setOrder(referenceTarget.getOrder() + 1);
				databaseManagerService.saveModelObject(referenceTarget);
			} else {
				// Adjust order for moved below

				// Set order to former order of target plus 1
				monitoringRuleToMove.setOrder(referenceTarget.getOrder() + 1);
			}

			databaseManagerService.saveModelObject(monitoringRuleToMove);
		}
	}

	@Synchronized
	public void monitoringReplyRuleChangeSendMessageIfTrue(
			final MonitoringReplyRule monitoringReplyRule,
			final boolean newValue) {
		monitoringReplyRule.setSendMessageIfTrue(newValue);

		databaseManagerService.saveModelObject(monitoringReplyRule);
	}

	@Synchronized
	public void monitoringReplyRuleChangeActivateMicroDialogIfTrue(
			final MonitoringReplyRule monitoringReplyRule,
			final boolean newValue) {
		monitoringReplyRule.setActivateMicroDialogIfTrue(newValue);

		databaseManagerService.saveModelObject(monitoringReplyRule);
	}

	@Synchronized
	public void monitoringReplyRuleChangeSendMessageToSupervisor(
			final MonitoringReplyRule monitoringReplyRule,
			final boolean newValue) {
		monitoringReplyRule.setSendMessageToSupervisor(newValue);

		databaseManagerService.saveModelObject(monitoringReplyRule);
	}

	@Synchronized
	public void monitoringReplyRuleChangeRelatedMonitoringMessageGroup(
			final MonitoringReplyRule monitoringReplyRule,
			final ObjectId newMonitoringMessageGroupId) {
		monitoringReplyRule
				.setRelatedMonitoringMessageGroup(newMonitoringMessageGroupId);

		databaseManagerService.saveModelObject(monitoringReplyRule);
	}

	@Synchronized
	public void monitoringReplyRuleChangeRelatedMicroDialog(
			final MonitoringReplyRule monitoringReplyRule,
			final ObjectId newMicroDialogId) {
		monitoringReplyRule.setRelatedMicroDialog(newMicroDialogId);

		databaseManagerService.saveModelObject(monitoringReplyRule);
	}

	@Synchronized
	public void monitoringReplyRuleSetStoreResultToVariable(
			final MonitoringReplyRule monitoringReplyRule,
			final String variableName) throws NotificationMessageException {
		if (variableName == null || variableName.equals("")) {
			monitoringReplyRule.setStoreValueToVariableWithName(null);

			databaseManagerService.saveModelObject(monitoringReplyRule);

		} else {
			if (!StringValidator.isValidVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
			}

			if (variablesManagerService
					.isWriteProtectedReservedVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
			}

			monitoringReplyRule.setStoreValueToVariableWithName(variableName);

			databaseManagerService.saveModelObject(monitoringReplyRule);
		}
	}

	@Synchronized
	public MonitoringReplyRule monitoringReplyRuleImport(final File file)
			throws FileNotFoundException, IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.MONITORING_REPLY_RULE);

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof MonitoringReplyRule) {
				val monitoringReplyRule = (MonitoringReplyRule) modelObject;

				boolean isGotAnswerRule = false;
				if (monitoringReplyRule
						.getIsGotAnswerRuleForMonitoringRule() != null) {
					isGotAnswerRule = true;
				}

				// Adjust order
				monitoringReplyRule.setOrder(0);

				val highestOrderRule = databaseManagerService
						.findOneSortedModelObject(MonitoringReplyRule.class,
								isGotAnswerRule
										? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER
										: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
								Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_DESC,
								isGotAnswerRule
										? monitoringReplyRule
												.getIsGotAnswerRuleForMonitoringRule()
										: monitoringReplyRule
												.getIsGotNoAnswerRuleForMonitoringRule(),
								monitoringReplyRule
										.getIsSubRuleOfMonitoringRule());

				if (highestOrderRule != null) {
					monitoringReplyRule
							.setOrder(highestOrderRule.getOrder() + 1);
				}

				databaseManagerService.saveModelObject(monitoringReplyRule);

				return monitoringReplyRule;
			}
		}

		return null;
	}

	@Synchronized
	public File monitoringReplyRuleExport(
			final ObjectId monitoringReplyRuleId) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		val monitoringReplyRule = databaseManagerService.getModelObjectById(
				MonitoringReplyRule.class, monitoringReplyRuleId);

		log.debug(
				"Recursively collect all model objects related to the monitoring reply rule");
		monitoringReplyRule.collectThisAndRelatedModelObjectsForExport(
				modelObjectsToExport);

		log.debug("Export monitoring rule");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.MONITORING_REPLY_RULE);
	}

	@Synchronized
	public void monitoringReplyRuleDelete(
			final ObjectId monitoringReplyRuleId) {
		databaseManagerService.deleteModelObject(MonitoringReplyRule.class,
				monitoringReplyRuleId);
	}

	// Abstract Rule
	@Synchronized
	public void abstractRuleSetComment(final AbstractRule abstractRule,
			final String comment) throws NotificationMessageException {
		if (comment == null) {
			abstractRule.setComment("");
		} else {
			abstractRule.setComment(comment);
		}

		databaseManagerService.saveModelObject(abstractRule);
	}

	@Synchronized
	public void abstractRuleSetRuleWithPlaceholders(
			final AbstractRule abstractRule, final String textWithPlaceholders,
			final List<String> allPossibleVariables)
			throws NotificationMessageException {
		if (textWithPlaceholders == null) {
			abstractRule.setRuleWithPlaceholders("");
		} else {
			if (!StringValidator.isValidVariableText(textWithPlaceholders,
					allPossibleVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			abstractRule.setRuleWithPlaceholders(textWithPlaceholders);
		}

		databaseManagerService.saveModelObject(abstractRule);
	}

	@Synchronized
	public void abstractRuleSetRuleComparisonTermWithPlaceholders(
			final AbstractRule abstractRule, final String textWithPlaceholders,
			final List<String> allPossibleVariables)
			throws NotificationMessageException {
		if (textWithPlaceholders == null) {
			abstractRule.setRuleComparisonTermWithPlaceholders("");
		} else {
			if (!StringValidator.isValidVariableText(textWithPlaceholders,
					allPossibleVariables)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_TEXT_CONTAINS_UNKNOWN_VARIABLES);
			}

			abstractRule.setRuleComparisonTermWithPlaceholders(
					textWithPlaceholders);
		}

		databaseManagerService.saveModelObject(abstractRule);
	}

	@Synchronized
	public void abstractRuleSetEquationSign(final AbstractRule abstractRule,
			final RuleEquationSignTypes newType) {
		abstractRule.setRuleEquationSign(newType);

		databaseManagerService.saveModelObject(abstractRule);
	}

	// Participant
	@Synchronized
	public List<Participant> participantsImport(final File file,
			final ObjectId interventionId)
			throws FileNotFoundException, IOException {
		val importedParticipants = new ArrayList<Participant>();

		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.PARTICIPANTS);

		for (val modelObject : importedModelObjects) {
			// Adjust dialog status regarding last visited slide
			if (modelObject instanceof DialogStatus) {
				val dialogStatus = (DialogStatus) modelObject;

				if (dialogStatus
						.getLastVisitedScreeningSurveySlideGlobalUniqueId() != null) {
					val screeningSurveys = databaseManagerService
							.findModelObjects(ScreeningSurvey.class,
									Queries.SCREENING_SURVEY__BY_INTERVENTION,
									interventionId);

					ScreeningSurveySlide screeningSurveySlide = null;
					for (val screeningSurvey : screeningSurveys) {
						val foundScreeningSurveySlide = databaseManagerService
								.findOneModelObject(ScreeningSurveySlide.class,
										Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY_AND_GLOBAL_UNIQUE_ID,
										screeningSurvey.getId(), dialogStatus
												.getLastVisitedScreeningSurveySlideGlobalUniqueId());

						if (foundScreeningSurveySlide != null) {
							screeningSurveySlide = foundScreeningSurveySlide;
						}
					}

					if (screeningSurveySlide != null) {
						dialogStatus.setLastVisitedScreeningSurveySlide(
								screeningSurveySlide.getId());
					} else {
						dialogStatus.setLastVisitedScreeningSurveySlide(null);
						dialogStatus
								.setLastVisitedScreeningSurveySlideGlobalUniqueId(
										null);
					}

					databaseManagerService.saveModelObject(dialogStatus);
				}
			}

			if (modelObject instanceof Participant) {
				// Adjust participant regarding screening survey and feedback
				val participant = (Participant) modelObject;

				val screeningSurvey = databaseManagerService.findOneModelObject(
						ScreeningSurvey.class,
						Queries.SCREENING_SURVEY__BY_INTERVENTION_AND_GLOBAL_UNIQUE_ID,
						interventionId,
						participant.getAssignedScreeningSurveyGlobalUniqueId());

				if (screeningSurvey == null) {
					participant.setAssignedScreeningSurvey(null);
					participant.setAssignedScreeningSurveyGlobalUniqueId(null);

					participant.setAssignedFeedback(null);
					participant.setAssignedFeedbackGlobalUniqueId(null);
				} else {
					participant.setAssignedScreeningSurvey(
							screeningSurvey.getId());
					participant.setAssignedScreeningSurveyGlobalUniqueId(
							screeningSurvey.getGlobalUniqueId());

					val feedback = databaseManagerService.findOneModelObject(
							Feedback.class,
							Queries.FEEDBACK__BY_SCREENING_SURVEY_AND_GLOBAL_UNIQUE_ID,
							screeningSurvey.getId(),
							participant.getAssignedFeedbackGlobalUniqueId());
					if (feedback == null) {
						participant.setAssignedFeedback(null);
						participant.setAssignedFeedbackGlobalUniqueId(null);
					} else {
						participant.setAssignedFeedback(feedback.getId());
						participant.setAssignedFeedbackGlobalUniqueId(
								feedback.getGlobalUniqueId());

						feedbackParticipantShortURLEnsure(participant.getId(),
								feedback.getId());
					}
				}

				participant.setIntervention(interventionId);
				databaseManagerService.saveModelObject(participant);

				importedParticipants.add(participant);
			}
		}

		return importedParticipants;
	}

	@Synchronized
	public File participantsExport(final List<Participant> participants) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		log.debug(
				"Recursively collect all model objects related to the participants");
		for (val participant : participants) {
			participant.collectThisAndRelatedModelObjectsForExport(
					modelObjectsToExport);
		}

		log.debug("Export participants");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.PARTICIPANTS);
	}

	@Synchronized
	public void participantsSetGroup(final List<Participant> participants,
			final String newValue) {
		for (val participant : participants) {
			if (newValue == null || newValue.equals("")) {
				participant.setGroup(null);
			} else {
				participant.setGroup(newValue);
			}

			databaseManagerService.saveModelObject(participant);
		}
	}

	@Synchronized
	public void participantsSetOrganization(
			final List<Participant> participants, final String newValue) {
		for (val participant : participants) {
			participant.setOrganization(newValue);

			databaseManagerService.saveModelObject(participant);
		}
	}

	@Synchronized
	public void participantsSetOrganizationUnit(
			final List<Participant> participants, final String newValue) {
		for (val participant : participants) {
			participant.setOrganizationUnit(newValue);

			databaseManagerService.saveModelObject(participant);
		}
	}

	@Synchronized
	public void participantsDelete(
			final List<Participant> participantsToDelete) {
		for (val participantToDelete : participantsToDelete) {
			databaseManagerService.deleteModelObject(participantToDelete);
		}
	}

	/*
	 * Special methods
	 */

	/*
	 * Getter methods
	 */
	@Synchronized
	public Author getAuthor(final ObjectId authorId) {
		return databaseManagerService.getModelObjectById(Author.class,
				authorId);
	}

	@Synchronized
	public Iterable<Author> getAllAuthors() {
		return databaseManagerService.findModelObjects(Author.class,
				Queries.ALL);
	}

	@Synchronized
	public Iterable<Intervention> getAllInterventions() {
		return databaseManagerService.findModelObjects(Intervention.class,
				Queries.ALL);
	}

	@Synchronized
	public Iterable<Intervention> getAllInterventionsForAuthor(
			final ObjectId authorId) {
		val authorInterventionAccessForAuthor = databaseManagerService
				.findModelObjects(AuthorInterventionAccess.class,
						Queries.AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR,
						authorId);

		final List<Intervention> interventions = new ArrayList<Intervention>();

		for (val authorInterventionAccess : authorInterventionAccessForAuthor) {
			val intervention = databaseManagerService.getModelObjectById(
					Intervention.class,
					authorInterventionAccess.getIntervention());

			if (intervention != null) {
				interventions.add(intervention);
			} else {
				databaseManagerService.collectGarbage(authorInterventionAccess);
			}
		}

		return interventions;
	}

	@Synchronized
	public Iterable<Author> getAllAuthorsOfIntervention(
			final ObjectId interventionId) {
		val authorInterventionAccessForIntervention = databaseManagerService
				.findModelObjects(AuthorInterventionAccess.class,
						Queries.AUTHOR_INTERVENTION_ACCESS__BY_INTERVENTION,
						interventionId);

		final List<Author> authors = new ArrayList<Author>();

		for (val authorInterventionAccess : authorInterventionAccessForIntervention) {
			val author = databaseManagerService.getModelObjectById(Author.class,
					authorInterventionAccess.getAuthor());

			if (author != null) {
				authors.add(author);
			} else {
				databaseManagerService.collectGarbage(authorInterventionAccess);
			}
		}

		return authors;
	}

	@Synchronized
	public Iterable<InterventionVariableWithValue> getAllInterventionVariablesOfIntervention(
			final ObjectId interventionId) {

		return databaseManagerService.findModelObjects(
				InterventionVariableWithValue.class,
				Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION,
				interventionId);
	}

	@Synchronized
	public Iterable<MonitoringMessageGroup> getAllMonitoringMessageGroupsOfIntervention(
			final ObjectId interventionId) {
		return databaseManagerService.findSortedModelObjects(
				MonitoringMessageGroup.class,
				Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION,
				Queries.MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_ASC,
				interventionId);
	}

	@Synchronized
	public Iterable<MonitoringMessageGroup> getAllMonitoringMessageGroupsExpectingNoAnswerOfIntervention(
			final ObjectId interventionId) {
		return databaseManagerService.findSortedModelObjects(
				MonitoringMessageGroup.class,
				Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION_AND_EXPECTING_ANSWER,
				Queries.MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_ASC,
				interventionId, false);
	}

	@Synchronized
	public Iterable<MonitoringMessage> getAllMonitoringMessagesOfMonitoringMessageGroup(
			final ObjectId monitoringMessageGroupId) {
		return databaseManagerService.findSortedModelObjects(
				MonitoringMessage.class,
				Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
				Queries.MONITORING_MESSAGE__SORT_BY_ORDER_ASC,
				monitoringMessageGroupId);
	}

	@Synchronized
	public MonitoringMessage getMonitoringMessage(
			final ObjectId monitoringMessageId) {
		return databaseManagerService.getModelObjectById(
				MonitoringMessage.class, monitoringMessageId);
	}

	@Synchronized
	public MonitoringMessageGroup getMonitoringMessageGroup(
			final ObjectId monitoringMessageGroupId) {
		return databaseManagerService.getModelObjectById(
				MonitoringMessageGroup.class, monitoringMessageGroupId);
	}

	@Synchronized
	public Iterable<MonitoringMessageRule> getAllMonitoringMessageRulesOfMonitoringMessage(
			final ObjectId monitoringMessageId) {
		return databaseManagerService.findSortedModelObjects(
				MonitoringMessageRule.class,
				Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
				Queries.MONITORING_MESSAGE_RULE__SORT_BY_ORDER_ASC,
				monitoringMessageId);
	}

	@Synchronized
	public MicroDialogMessage getMicroDialogMessage(
			final ObjectId microDialogMessageId) {
		return databaseManagerService.getModelObjectById(
				MicroDialogMessage.class, microDialogMessageId);
	}

	@Synchronized
	public MicroDialog getMicroDialog(final ObjectId microDialogId) {
		return databaseManagerService.getModelObjectById(MicroDialog.class,
				microDialogId);
	}

	@Synchronized
	public Iterable<MicroDialog> getAllMicroDialogsOfIntervention(
			final ObjectId interventionId) {
		return databaseManagerService.findSortedModelObjects(MicroDialog.class,
				Queries.MICRO_DIALOG__BY_INTERVENTION,
				Queries.MICRO_DIALOG__SORT_BY_ORDER_ASC, interventionId);
	}

	@Synchronized
	public Iterable<MicroDialogMessageRule> getAllMicroDialogMessageRulesOfMicroDialogMessage(
			final ObjectId microDialogMessageId) {
		return databaseManagerService.findSortedModelObjects(
				MicroDialogMessageRule.class,
				Queries.MICRO_DIALOG_MESSAGE_RULE__BY_MICRO_DIALOG_MESSAGE,
				Queries.MICRO_DIALOG_MESSAGE_RULE__SORT_BY_ORDER_ASC,
				microDialogMessageId);
	}

	@Synchronized
	public Iterable<MicroDialogRule> getAllMicroDialogRulesOfMicroDialogDecisionPoint(
			final ObjectId microDialogDecisionPointId) {
		return databaseManagerService.findSortedModelObjects(
				MicroDialogRule.class,
				Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT,
				Queries.MICRO_DIALOG_RULE__SORT_BY_ORDER_ASC,
				microDialogDecisionPointId);
	}

	@Synchronized
	public Iterable<MicroDialogRule> getAllMicroDialogRulesOfMicroDialogDecisionPointAndParent(
			final ObjectId microDialogDecisionPointId,
			final ObjectId parentMicroDialogRuleId) {
		return databaseManagerService.findSortedModelObjects(
				MicroDialogRule.class,
				Queries.MICRO_DIALOG_RULE__BY_MICRO_DIALOG_DECISION_POINT_AND_PARENT,
				Queries.MICRO_DIALOG_RULE__SORT_BY_ORDER_ASC,
				microDialogDecisionPointId, parentMicroDialogRuleId);
	}

	@Synchronized
	public MicroDialogRule getMicroDialogRule(
			final ObjectId microDialogRuleId) {
		return databaseManagerService.getModelObjectById(MicroDialogRule.class,
				microDialogRuleId);
	}

	@Synchronized
	public Iterable<MicroDialogMessage> getAllMicroDialogMessagesOfMicroDialog(
			final ObjectId microDialogId) {

		return databaseManagerService.findSortedModelObjects(
				MicroDialogMessage.class,
				Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG,
				Queries.MICRO_DIALOG_MESSAGE__SORT_BY_ORDER_ASC, microDialogId);
	}

	@Synchronized
	public List<? extends ModelObject> getAllMicroDialogElementsOfMicroDialog(
			final ObjectId microDialogId) {
		// Messages
		val messages = databaseManagerService.findSortedModelObjects(
				MicroDialogMessage.class,
				Queries.MICRO_DIALOG_MESSAGE__BY_MICRO_DIALOG,
				Queries.MICRO_DIALOG_MESSAGE__SORT_BY_ORDER_ASC, microDialogId);
		// Decision points
		val decisionPoints = databaseManagerService.findSortedModelObjects(
				MicroDialogDecisionPoint.class,
				Queries.MICRO_DIALOG_DECISION_POINT__BY_MICRO_DIALOG,
				Queries.MICRO_DIALOG_DECISION_POINT__SORT_BY_ORDER_ASC,
				microDialogId);

		// Merge lists while retaining order
		val microDialogElements = ListMerger.mergeMicroDialogElements(messages,
				decisionPoints);

		final List<ModelObject> modelObjectList = new ArrayList<>();

		for (val microDialogElement : microDialogElements) {
			modelObjectList.add((ModelObject) microDialogElement);
		}

		return modelObjectList;
	}

	@Synchronized
	public Iterable<MonitoringRule> getAllMonitoringRulesOfIntervention(
			final ObjectId interventionId) {
		return databaseManagerService.findModelObjects(MonitoringRule.class,
				Queries.MONITORING_RULE__BY_INTERVENTION, interventionId);
	}

	@Synchronized
	public Iterable<MonitoringRule> getAllMonitoringRulesOfInterventionAndParent(
			final ObjectId interventionId,
			final ObjectId parentMonitoringRuleId) {
		return databaseManagerService.findSortedModelObjects(
				MonitoringRule.class,
				Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
				Queries.MONITORING_RULE__SORT_BY_ORDER_ASC, interventionId,
				parentMonitoringRuleId);
	}

	@Synchronized
	public MonitoringRule getMonitoringRule(final ObjectId monitoringRuleId) {
		return databaseManagerService.getModelObjectById(MonitoringRule.class,
				monitoringRuleId);
	}

	@Synchronized
	public Iterable<MonitoringReplyRule> getAllMonitoringReplyRulesOfMonitoringRule(
			final ObjectId monitoringRuleId, final boolean isGotAnswerRule) {
		return databaseManagerService.findModelObjects(
				MonitoringReplyRule.class,
				isGotAnswerRule
						? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_ONLY_GOT_ANSWER
						: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_ONLY_GOT_NO_ANSWER,
				monitoringRuleId);
	}

	@Synchronized
	public Iterable<MonitoringReplyRule> getAllMonitoringReplyRulesOfMonitoringRuleAndParent(
			final ObjectId monitoringRuleId,
			final ObjectId parentMonitoringReplyRuleId,
			final boolean isGotAnswerRule) {
		return databaseManagerService.findSortedModelObjects(
				MonitoringReplyRule.class,
				isGotAnswerRule
						? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER
						: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
				Queries.MONITORING_RULE__SORT_BY_ORDER_ASC, monitoringRuleId,
				parentMonitoringReplyRuleId);
	}

	@Synchronized
	public Iterable<Participant> getAllParticipantsOfIntervention(
			final ObjectId interventionId) {
		return databaseManagerService.findModelObjects(Participant.class,
				Queries.PARTICIPANT__BY_INTERVENTION, interventionId);
	}

	@Synchronized
	public MonitoringReplyRule getMonitoringReplyRule(
			final ObjectId monitoringReplyRuleId) {
		return databaseManagerService.getModelObjectById(
				MonitoringReplyRule.class, monitoringReplyRuleId);
	}

	@Synchronized
	public MediaObject getMediaObject(final ObjectId mediaObjectId) {
		return databaseManagerService.getModelObjectById(MediaObject.class,
				mediaObjectId);
	}

	@Synchronized
	public File getFileByReference(final String fileReference,
			final FILE_STORES fileStorage) {
		return fileStorageManagerService.getFileByReference(fileReference,
				fileStorage);
	}

	@Synchronized
	public List<String> getAllPossibleMessageVariablesOfIntervention(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

		variables.addAll(
				variablesManagerService.getAllSystemReservedVariableNames());

		variables.addAll(variablesManagerService
				.getAllInterventionVariableNamesOfIntervention(interventionId));
		variables.addAll(variablesManagerService
				.getAllSurveyVariableNamesOfIntervention(interventionId));
		variables.addAll(variablesManagerService
				.getAllMonitoringMessageVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMonitoringRuleAndReplyRuleVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMicroDialogMessageVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMicroDialogRuleVariableNamesOfIntervention(
						interventionId));

		Collections.sort(variables);

		return variables;
	}

	@Synchronized
	public List<String> getAllPossibleMonitoringRuleVariablesOfIntervention(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

		variables.addAll(
				variablesManagerService.getAllSystemReservedVariableNames());

		variables.addAll(variablesManagerService
				.getAllInterventionVariableNamesOfIntervention(interventionId));
		variables.addAll(variablesManagerService
				.getAllSurveyVariableNamesOfIntervention(interventionId));
		variables.addAll(variablesManagerService
				.getAllMonitoringMessageVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMonitoringRuleAndReplyRuleVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMicroDialogMessageVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMicroDialogRuleVariableNamesOfIntervention(
						interventionId));

		Collections.sort(variables);

		return variables;
	}

	@Synchronized
	public List<String> getAllWritableMessageVariablesOfIntervention(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

		variables.addAll(variablesManagerService
				.getAllWritableSystemReservedVariableNames());

		variables.addAll(variablesManagerService
				.getAllInterventionVariableNamesOfIntervention(interventionId));
		variables.addAll(variablesManagerService
				.getAllSurveyVariableNamesOfIntervention(interventionId));
		variables.addAll(variablesManagerService
				.getAllMonitoringMessageVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMonitoringRuleAndReplyRuleVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMicroDialogMessageVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMicroDialogRuleVariableNamesOfIntervention(
						interventionId));

		Collections.sort(variables);

		return variables;
	}

	@Synchronized
	public List<String> getAllWritableMonitoringRuleVariablesOfIntervention(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

		variables.addAll(variablesManagerService
				.getAllWritableSystemReservedVariableNames());

		variables.addAll(variablesManagerService
				.getAllInterventionVariableNamesOfIntervention(interventionId));
		variables.addAll(variablesManagerService
				.getAllSurveyVariableNamesOfIntervention(interventionId));
		variables.addAll(variablesManagerService
				.getAllMonitoringMessageVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMonitoringRuleAndReplyRuleVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMicroDialogMessageVariableNamesOfIntervention(
						interventionId));
		variables.addAll(variablesManagerService
				.getAllMicroDialogRuleVariableNamesOfIntervention(
						interventionId));

		Collections.sort(variables);

		return variables;
	}

	@Synchronized
	public DialogStatus getDialogStatusOfParticipant(
			final ObjectId participantId) {
		return databaseManagerService.findOneModelObject(DialogStatus.class,
				Queries.DIALOG_STATUS__BY_PARTICIPANT, participantId);
	}

	@Synchronized
	public List<Class<? extends AbstractModule>> getRegisteredModules() {
		return modules;
	}

	@Synchronized
	public Hashtable<String, AbstractVariableWithValue> getAllVariablesWithValuesOfParticipantAndSystem(
			final ObjectId participantId) {
		val participant = databaseManagerService
				.getModelObjectById(Participant.class, participantId);

		return variablesManagerService
				.getAllVariablesWithValuesOfParticipantAndSystem(participant);
	}

	@Synchronized
	public Participant getParticipant(final ObjectId participantId) {
		return databaseManagerService.getModelObjectById(Participant.class,
				participantId);
	}

	@Synchronized
	public Iterable<DialogMessage> getAllDialogMessagesOfParticipant(
			final ObjectId participantId) {
		return databaseManagerService.findSortedModelObjects(
				DialogMessage.class, Queries.DIALOG_MESSAGE__BY_PARTICIPANT,
				Queries.DIALOG_MESSAGE__SORT_BY_ORDER_ASC, participantId);
	}

	@Synchronized
	public List<DialogMessage> getAllDialogMessagesWhichAreNotAutomaticallyProcessableButAreNotProcessedOfIntervention(
			final ObjectId interventionid) {
		val dialogMessages = new ArrayList<DialogMessage>();

		val participantsOfIntervention = databaseManagerService
				.findModelObjects(Participant.class,
						Queries.PARTICIPANT__BY_INTERVENTION, interventionid);

		for (val participant : participantsOfIntervention) {
			val dialogMessagesOfParticipant = databaseManagerService
					.findModelObjects(DialogMessage.class,
							Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_STATUS_OR_STATUS_AND_NOT_AUTOMATICALLY_PROCESSABLE,
							participant.getId(),
							DialogMessageStatusTypes.SENT_AND_WAITING_FOR_ANSWER,
							DialogMessageStatusTypes.RECEIVED_UNEXPECTEDLY,
							true);

			CollectionUtils.addAll(dialogMessages,
					dialogMessagesOfParticipant.iterator());
		}

		return dialogMessages;
	}

	@Synchronized
	public Hashtable<String, String> getAllStatisticValuesOfParticipant(
			final ObjectId participantId) {
		val values = new Hashtable<String, String>();

		// Started finished times
		val participant = getParticipant(participantId);
		val dialogStatus = getDialogStatusOfParticipant(participantId);

		values.put(
				Messages.getAdminString(
						AdminMessageStrings.STATISTICS__CREATED),
				StringHelpers.createStringTimeStamp(
						participant.getCreatedTimestamp()));

		if (dialogStatus != null) {
			values.put(
					Messages.getAdminString(
							AdminMessageStrings.STATISTICS__SCREENING_SURVEY_STARTED),
					StringHelpers.createStringTimeStamp(
							dialogStatus.getScreeningSurveyStartedTimestamp()));
			values.put(
					Messages.getAdminString(
							AdminMessageStrings.STATISTICS__SCREENING_SURVEY_PERFORMED),
					StringHelpers.createStringTimeStamp(dialogStatus
							.getScreeningSurveyPerformedTimestamp()));
			values.put(
					Messages.getAdminString(
							AdminMessageStrings.STATISTICS__MONITORING_STARTED),
					StringHelpers.createStringTimeStamp(
							dialogStatus.getMonitoringStartedTimestamp()));
			values.put(
					Messages.getAdminString(
							AdminMessageStrings.STATISTICS__MONITORING_PERFORMED),
					StringHelpers.createStringTimeStamp(
							dialogStatus.getMonitoringPerformedTimestamp()));
		} else {
			values.put(
					Messages.getAdminString(
							AdminMessageStrings.STATISTICS__SCREENING_SURVEY_STARTED),
					StringHelpers.createStringTimeStamp(0));
			values.put(
					Messages.getAdminString(
							AdminMessageStrings.STATISTICS__SCREENING_SURVEY_PERFORMED),
					StringHelpers.createStringTimeStamp(0));
			values.put(
					Messages.getAdminString(
							AdminMessageStrings.STATISTICS__MONITORING_STARTED),
					StringHelpers.createStringTimeStamp(0));
			values.put(
					Messages.getAdminString(
							AdminMessageStrings.STATISTICS__MONITORING_PERFORMED),
					StringHelpers.createStringTimeStamp(0));
		}

		// Message counts
		int totalSentMessages = 0;
		int totalReceivedMessages = 0;
		int answeredQuestions = 0;
		int unansweredQuestions = 0;
		int mediaObjectsContained = 0;
		int mediaObjectsViewed = 0;

		val dialogMessages = databaseManagerService.findModelObjects(
				DialogMessage.class,
				Queries.DIALOG_MESSAGE__BY_PARTICIPANT_AND_MESSAGE_TYPE,
				participantId, false);
		for (val dialogMessage : dialogMessages) {
			switch (dialogMessage.getStatus()) {
				case IN_CREATION:
					break;
				case PREPARED_FOR_SENDING:
					break;
				case RECEIVED_UNEXPECTEDLY:
					totalReceivedMessages++;
					break;
				case RECEIVED_AS_INTENTION:
					totalReceivedMessages++;
					break;
				case SENDING:
					break;
				case SENT_AND_ANSWERED_AND_PROCESSED:
					totalSentMessages++;
					totalReceivedMessages++;
					answeredQuestions++;
					break;
				case SENT_AND_ANSWERED_BY_PARTICIPANT:
					totalSentMessages++;
					totalReceivedMessages++;
					answeredQuestions++;
					break;
				case SENT_AND_NOT_ANSWERED_AND_PROCESSED:
					totalSentMessages++;
					unansweredQuestions++;
					break;
				case SENT_AND_WAITING_FOR_ANSWER:
					totalSentMessages++;
					break;
				case SENT_BUT_NOT_WAITING_FOR_ANSWER:
					totalSentMessages++;
					break;
			}

			val relatedMonitoringMessageId = dialogMessage
					.getRelatedMonitoringMessage();

			if (relatedMonitoringMessageId != null) {
				val relatedMonitoringMessage = getMonitoringMessage(
						relatedMonitoringMessageId);

				if (relatedMonitoringMessage != null && relatedMonitoringMessage
						.getLinkedMediaObject() != null) {
					val linkedMediaObject = getMediaObject(
							relatedMonitoringMessage.getLinkedMediaObject());

					if (linkedMediaObject != null) {
						mediaObjectsContained++;
					}
				}
			}

			if (dialogMessage.isMediaContentViewed()) {
				mediaObjectsViewed++;
			}
		}

		values.put(
				Messages.getAdminString(
						AdminMessageStrings.STATISTICS__TOTAL_MESSAGES_SENT),
				String.valueOf(totalSentMessages));
		values.put(
				Messages.getAdminString(
						AdminMessageStrings.STATISTICS__TOTAL_MESSAGES_RECEIVED),
				String.valueOf(totalReceivedMessages));
		values.put(
				Messages.getAdminString(
						AdminMessageStrings.STATISTICS__ANSWERED_QUESTIONS),
				String.valueOf(answeredQuestions));
		values.put(
				Messages.getAdminString(
						AdminMessageStrings.STATISTICS__UNANSWERED_QUESTIONS),
				String.valueOf(unansweredQuestions));
		values.put(
				Messages.getAdminString(
						AdminMessageStrings.STATISTICS__MEDIA_OBJECTS_CONTAINED_IN_MESSAGES),
				String.valueOf(mediaObjectsContained));
		values.put(
				Messages.getAdminString(
						AdminMessageStrings.STATISTICS__MEDIA_OBJECTS_VIEWED),
				String.valueOf(mediaObjectsViewed));

		return values;
	}
}
