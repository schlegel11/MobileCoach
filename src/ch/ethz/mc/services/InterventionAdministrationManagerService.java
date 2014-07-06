package ch.ethz.mc.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.Author;
import ch.ethz.mc.model.persistent.AuthorInterventionAccess;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.Feedback;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;
import ch.ethz.mc.model.persistent.MediaObject;
import ch.ethz.mc.model.persistent.MonitoringMessage;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringMessageRule;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ParticipantVariableWithValue;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.persistent.types.DialogMessageStatusTypes;
import ch.ethz.mc.model.persistent.types.MediaObjectTypes;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.modules.AbstractModule;
import ch.ethz.mc.modules.message_contest.MessageContestMotivationalMessage;
import ch.ethz.mc.modules.message_contest.MessageContestQuitMessage;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.services.internal.ModelObjectExchangeService;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.services.types.ModelObjectExchangeFormatTypes;
import ch.ethz.mc.tools.BCrypt;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.StringValidator;
import ch.ethz.mc.ui.NotificationMessageException;

/**
 * Cares for the creation of the {@link Author}s, {@link Participant}s and
 * {@link Intervention}s as well as all related {@link ModelObject}s
 * 
 * @author Andreas Filler
 */
@Log4j2
public class InterventionAdministrationManagerService {
	private static InterventionAdministrationManagerService	instance	= null;

	private final DatabaseManagerService					databaseManagerService;
	private final FileStorageManagerService					fileStorageManagerService;
	private final VariablesManagerService					variablesManagerService;
	private final ModelObjectExchangeService				modelObjectExchangeService;

	private final List<Class<? extends AbstractModule>>		modules;

	private InterventionAdministrationManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final ModelObjectExchangeService modelObjectExchangeService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.variablesManagerService = variablesManagerService;
		this.modelObjectExchangeService = modelObjectExchangeService;

		log.info("Registering modules...");
		// FIXME Should be done cleaner
		modules = new ArrayList<Class<? extends AbstractModule>>();
		modules.add(MessageContestMotivationalMessage.class);
		modules.add(MessageContestQuitMessage.class);

		log.info("Started.");
	}

	public static InterventionAdministrationManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final ModelObjectExchangeService modelObjectExchangeService)
			throws Exception {
		if (instance == null) {
			instance = new InterventionAdministrationManagerService(
					databaseManagerService, fileStorageManagerService,
					variablesManagerService, modelObjectExchangeService);
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
	// Author
	public Author authorCreate(final String username) {
		val author = new Author(false, username, BCrypt.hashpw(
				RandomStringUtils.randomAlphanumeric(128), BCrypt.gensalt()));

		databaseManagerService.saveModelObject(author);

		return author;
	}

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

	public void authorSetAdmin(final Author author) {
		author.setAdmin(true);

		databaseManagerService.saveModelObject(author);
	}

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

	public void authorChangePassword(final Author author,
			final String newPassword) throws NotificationMessageException {
		if (newPassword.length() < 5) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_PASSWORD_IS_NOT_SAFE);
		}

		author.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
		databaseManagerService.saveModelObject(author);
	}

	public void authorDelete(final ObjectId currentAuthorId,
			final Author authorToDelete) throws NotificationMessageException {
		if (authorToDelete.getId().equals(currentAuthorId)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__CANT_DELETE_YOURSELF);
		}
		if (authorToDelete.getUsername().equals(
				Constants.getDefaultAdminUsername())) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__DEFAULT_ADMIN_CANT_BE_DELETED);
		}

		databaseManagerService.deleteModelObject(authorToDelete);
	}

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
	public Intervention interventionCreate(final String name) {
		val intervention = new Intervention(name,
				InternalDateTime.currentTimeMillis(), false, false);

		if (name.equals("")) {
			intervention.setName(ImplementationConstants.DEFAULT_OBJECT_NAME);
		}

		databaseManagerService.saveModelObject(intervention);

		return intervention;
	}

	public void interventionChangeName(final Intervention intervention,
			final String newName) {
		if (newName.equals("")) {
			intervention.setName(ImplementationConstants.DEFAULT_OBJECT_NAME);
		} else {
			intervention.setName(newName);
		}

		databaseManagerService.saveModelObject(intervention);
	}

	public Intervention interventionImport(final File file)
			throws FileNotFoundException, IOException {
		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.INTERVENTION);

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof Intervention) {
				val intervention = (Intervention) modelObject;

				val dateFormat = DateFormat.getDateTimeInstance(
						DateFormat.MEDIUM, DateFormat.MEDIUM,
						Constants.getAdminLocale());
				val date = dateFormat.format(new Date(InternalDateTime
						.currentTimeMillis()));
				intervention
						.setName(intervention.getName() + " (" + date + ")");

				databaseManagerService.saveModelObject(intervention);

				return intervention;
			}
		}

		return null;
	}

	public File interventionExport(final Intervention intervention) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		log.debug("Recursively collect all model objects related to the intervention");
		intervention
				.collectThisAndRelatedModelObjectsForExport(modelObjectsToExport);

		log.debug("Export intervention");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.INTERVENTION);
	}

	public void interventionDelete(final Intervention interventionToDelete)
			throws NotificationMessageException {

		databaseManagerService.deleteModelObject(interventionToDelete);
	}

	// Author Intervention Access
	public AuthorInterventionAccess authorInterventionAccessCreate(
			final ObjectId authorId, final ObjectId interventionId) {

		val authorInterventionAccess = new AuthorInterventionAccess(authorId,
				interventionId);

		databaseManagerService.saveModelObject(authorInterventionAccess);

		return authorInterventionAccess;
	}

	public void authorInterventionAccessDelete(final ObjectId authorId,
			final ObjectId interventionId) {
		val authorInterventionAccess = databaseManagerService
				.findOneModelObject(
						AuthorInterventionAccess.class,
						Queries.AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR_AND_INTERVENTION,
						authorId, interventionId);

		databaseManagerService.deleteModelObject(authorInterventionAccess);
	}

	// Intervention Variable With Value
	public InterventionVariableWithValue interventionVariableWithValueCreate(
			final String variableName, final ObjectId interventionId)
			throws NotificationMessageException {

		if (!StringValidator.isValidVariableName(variableName)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
		}

		if (variablesManagerService.isWriteProtectedVariableName(variableName)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
		}

		val interventionVariables = databaseManagerService
				.findModelObjects(
						InterventionVariableWithValue.class,
						Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
						interventionId, variableName);
		if (interventionVariables.iterator().hasNext()) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_ALREADY_IN_USE);
		}

		val interventionVariableWithValue = new InterventionVariableWithValue(
				interventionId, variableName, "0");

		databaseManagerService.saveModelObject(interventionVariableWithValue);

		return interventionVariableWithValue;
	}

	public void interventionVariableWithValueCreateOrUpdate(
			final ObjectId interventionId, final String variableName,
			final String variableValue) {

		val interventionVariable = databaseManagerService
				.findOneModelObject(
						InterventionVariableWithValue.class,
						Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
						interventionId, variableName);

		if (interventionVariable == null) {
			final InterventionVariableWithValue interventionVariableWithValue = new InterventionVariableWithValue(
					interventionId, variableName, variableValue);
			databaseManagerService
					.saveModelObject(interventionVariableWithValue);
		} else {
			interventionVariable.setValue(variableValue);
			databaseManagerService.saveModelObject(interventionVariable);
		}
	}

	public void interventionVariableWithValueChangeName(
			final InterventionVariableWithValue interventionVariableWithValue,
			final String newName) throws NotificationMessageException {

		if (!StringValidator.isValidVariableName(newName)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
		}

		if (variablesManagerService.isWriteProtectedVariableName(newName)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
		}

		val interventionVariables = databaseManagerService
				.findModelObjects(
						InterventionVariableWithValue.class,
						Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION_AND_NAME,
						interventionVariableWithValue.getIntervention(),
						newName);
		if (interventionVariables.iterator().hasNext()) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_ALREADY_IN_USE);
		}

		interventionVariableWithValue.setName(newName);

		databaseManagerService.saveModelObject(interventionVariableWithValue);
	}

	public void interventionVariableWithValueChangeValue(
			final InterventionVariableWithValue interventionVariableWithValue,
			final String newValue) throws NotificationMessageException {

		interventionVariableWithValue.setValue(newValue);

		databaseManagerService.saveModelObject(interventionVariableWithValue);
	}

	public void interventionVariableWithValueDelete(
			final InterventionVariableWithValue variableToDelete) {

		databaseManagerService.deleteModelObject(variableToDelete);
	}

	// Monitoring Message Group
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

	public void monitoringMessageGroupSetMessagesExceptAnswer(
			final MonitoringMessageGroup monitoringMessageGroup,
			final boolean newValue) {
		monitoringMessageGroup.setMessagesExpectAnswer(newValue);

		databaseManagerService.saveModelObject(monitoringMessageGroup);
	}

	public void monitoringMessageGroupSetRandomSendOrder(
			final MonitoringMessageGroup monitoringMessageGroup,
			final boolean newValue) {
		monitoringMessageGroup.setSendInRandomOrder(newValue);

		databaseManagerService.saveModelObject(monitoringMessageGroup);
	}

	public void monitoringMessageGroupSetSendSamePositionIfSendingAsReply(
			final MonitoringMessageGroup monitoringMessageGroup,
			final boolean newValue) {
		monitoringMessageGroup.setSendSamePositionIfSendingAsReply(newValue);

		databaseManagerService.saveModelObject(monitoringMessageGroup);
	}

	public MonitoringMessageGroup monitoringMessageGroupMove(
			final MonitoringMessageGroup monitoringMessageGroup,
			final boolean moveLeft) {
		// Find monitoring message to swap with
		val monitoringMessageGroupToSwapWith = databaseManagerService
				.findOneSortedModelObject(
						MonitoringMessageGroup.class,
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
		monitoringMessageGroup.setOrder(monitoringMessageGroupToSwapWith
				.getOrder());
		monitoringMessageGroupToSwapWith.setOrder(order);

		databaseManagerService.saveModelObject(monitoringMessageGroup);
		databaseManagerService
				.saveModelObject(monitoringMessageGroupToSwapWith);

		return monitoringMessageGroupToSwapWith;
	}

	public void monitoringMessageGroupChangeName(
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

	public void monitoringMessageGroupChangeValidationExpression(
			final MonitoringMessageGroup monitoringMessageGroup,
			final String newExpression) {
		if (newExpression.equals("")) {
			monitoringMessageGroup.setValidationExpression(null);
		} else {
			monitoringMessageGroup.setValidationExpression(newExpression);
		}

		databaseManagerService.saveModelObject(monitoringMessageGroup);
	}

	public void monitoringMessageGroupDelete(
			final MonitoringMessageGroup monitoringMessageGroupToDelete) {

		databaseManagerService
				.deleteModelObject(monitoringMessageGroupToDelete);
	}

	// Monitoring Message
	public MonitoringMessage monitoringMessageCreate(
			final ObjectId monitoringMessageGroupId) {
		val monitoringMessage = new MonitoringMessage(monitoringMessageGroupId,
				"", 0, null, null);

		val highestOrderMessage = databaseManagerService
				.findOneSortedModelObject(
						MonitoringMessage.class,
						Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
						Queries.MONITORING_MESSAGE__SORT_BY_ORDER_DESC,
						monitoringMessageGroupId);

		if (highestOrderMessage != null) {
			monitoringMessage.setOrder(highestOrderMessage.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(monitoringMessage);

		return monitoringMessage;
	}

	public MonitoringMessage monitoringMessageMove(
			final MonitoringMessage monitoringMessage, final boolean moveUp) {
		// Find monitoring message to swap with
		val monitoringMessageToSwapWith = databaseManagerService
				.findOneSortedModelObject(
						MonitoringMessage.class,
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

	public void monitoringMessageSetLinkedMediaObject(
			final MonitoringMessage monitoringMessage,
			final ObjectId linkedMediaObjectId) {
		monitoringMessage.setLinkedMediaObject(linkedMediaObjectId);

		databaseManagerService.saveModelObject(monitoringMessage);
	}

	public void monitoringMessageSetTextWithPlaceholders(
			final MonitoringMessage monitoringMessage,
			final String textWithPlaceholders,
			final List<String> allPossibleMessageVariables)
			throws NotificationMessageException {
		if (textWithPlaceholders == null) {
			monitoringMessage.setTextWithPlaceholders("");
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

	public void monitoringMessageSetStoreResultToVariable(
			final MonitoringMessage monitoringMessage, final String variableName)
			throws NotificationMessageException {
		if (variableName == null || variableName.equals("")) {
			monitoringMessage.setStoreValueToVariableWithName(null);

			databaseManagerService.saveModelObject(monitoringMessage);

		} else {
			if (!StringValidator.isValidVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
			}

			if (variablesManagerService
					.isWriteProtectedVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
			}

			monitoringMessage.setStoreValueToVariableWithName(variableName);

			databaseManagerService.saveModelObject(monitoringMessage);
		}
	}

	public void monitoringMessageDelete(
			final MonitoringMessage monitoringMessage) {
		databaseManagerService.deleteModelObject(monitoringMessage);
	}

	// Monitoring Message Rule
	public MonitoringMessageRule monitoringMessageRuleCreate(
			final ObjectId monitoringMessageId) {
		val monitoringMessageRule = new MonitoringMessageRule(
				monitoringMessageId, 0, "",
				RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "");

		val highestOrderSlideRule = databaseManagerService
				.findOneSortedModelObject(MonitoringMessageRule.class,
						Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
						Queries.MONITORING_MESSAGE_RULE__SORT_BY_ORDER_DESC,
						monitoringMessageId);

		if (highestOrderSlideRule != null) {
			monitoringMessageRule
					.setOrder(highestOrderSlideRule.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(monitoringMessageRule);

		return monitoringMessageRule;
	}

	public MonitoringMessageRule monitoringMessageRuleMove(
			final MonitoringMessageRule monitoringMessageRule,
			final boolean moveUp) {
		// Find feedback slide rule to swap with
		val monitoringMessageRuleToSwapWith = databaseManagerService
				.findOneSortedModelObject(
						MonitoringMessageRule.class,
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
		monitoringMessageRule.setOrder(monitoringMessageRuleToSwapWith
				.getOrder());
		monitoringMessageRuleToSwapWith.setOrder(order);

		databaseManagerService.saveModelObject(monitoringMessageRule);
		databaseManagerService.saveModelObject(monitoringMessageRuleToSwapWith);

		return monitoringMessageRuleToSwapWith;
	}

	public void monitoringMessageRuleDelete(
			final MonitoringMessageRule monitoringMessageRule) {
		databaseManagerService.deleteModelObject(monitoringMessageRule);
	}

	// Media Object
	public MediaObject mediaObjectCreate(final File temporaryFile,
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

	public void mediaObjectDelete(final MediaObject mediaObject) {
		databaseManagerService.deleteModelObject(mediaObject);
	}

	// Monitoring Rule
	public MonitoringRule monitoringRuleCreate(final ObjectId interventionId,
			final ObjectId parentMonitoringRuleId) {
		val monitoringRule = new MonitoringRule(
				"",
				RuleEquationSignTypes.CALCULATED_VALUE_EQUALS,
				"",
				parentMonitoringRuleId,
				0,
				null,
				false,
				null,
				interventionId,
				ImplementationConstants.DEFAULT_HOUR_TO_SEND_MESSAGE,
				ImplementationConstants.DEFAULT_HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED,
				false);

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

	public void monitoringRuleMove(final int movement,
			final ObjectId monitoringRuleIdToMove,
			final ObjectId referenceTargetId, final ObjectId interventionId) {
		if (movement == 0) {
			// At the beginning of sublist of target

			// Move all items in sublist one step down
			val otherMonitoringRulesToMove = databaseManagerService
					.findModelObjects(
							MonitoringRule.class,
							Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
							interventionId, referenceTargetId);
			for (val otherMonitoringRuleToMove : otherMonitoringRulesToMove) {
				otherMonitoringRuleToMove.setOrder(otherMonitoringRuleToMove
						.getOrder() + 1);
				databaseManagerService
						.saveModelObject(otherMonitoringRuleToMove);
			}

			// Set target as new parent
			val monitoringRuleToMove = databaseManagerService
					.getModelObjectById(MonitoringRule.class,
							monitoringRuleIdToMove);
			monitoringRuleToMove
					.setIsSubRuleOfMonitoringRule(referenceTargetId);

			// Set at beginning of list
			monitoringRuleToMove.setOrder(0);

			databaseManagerService.saveModelObject(monitoringRuleToMove);
		} else if (movement == 1 || movement == 2) {
			// Move above or below

			// Move all items in sublist after target one step
			// down
			val referenceTarget = databaseManagerService.getModelObjectById(
					MonitoringRule.class, referenceTargetId);

			val otherMonitoringRulesToMove = databaseManagerService
					.findModelObjects(
							MonitoringRule.class,
							Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT_AND_ORDER_HIGHER,
							interventionId,
							referenceTarget.getIsSubRuleOfMonitoringRule(),
							referenceTarget.getOrder());
			for (val otherMonitoringRuleToMove : otherMonitoringRulesToMove) {
				otherMonitoringRuleToMove.setOrder(otherMonitoringRuleToMove
						.getOrder() + 1);
				databaseManagerService
						.saveModelObject(otherMonitoringRuleToMove);
			}

			// Set parent of target to rule
			val monitoringRuleToMove = databaseManagerService
					.getModelObjectById(MonitoringRule.class,
							monitoringRuleIdToMove);
			monitoringRuleToMove.setIsSubRuleOfMonitoringRule(referenceTarget
					.getIsSubRuleOfMonitoringRule());

			if (movement == 1) {
				// Set order to former order of target
				monitoringRuleToMove.setOrder(referenceTarget.getOrder());
				databaseManagerService.saveModelObject(monitoringRuleToMove);

				// Set order of target one down
				referenceTarget.setOrder(referenceTarget.getOrder() + 1);
				databaseManagerService.saveModelObject(referenceTarget);
			} else {
				// Set order to former order of target plus 1
				monitoringRuleToMove.setOrder(referenceTarget.getOrder() + 1);
				databaseManagerService.saveModelObject(monitoringRuleToMove);
			}
		}

	}

	public void monitoringRuleChangeSendMessageIfTrue(
			final MonitoringRule monitoringRule, final boolean newValue) {
		monitoringRule.setSendMessageIfTrue(newValue);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	public void monitoringRuleChangeStopInterventionIfTrue(
			final MonitoringRule monitoringRule, final boolean newValue) {
		monitoringRule.setStopInterventionWhenTrue(newValue);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	public void monitoringRuleChangeRelatedMonitoringMessageGroup(
			final MonitoringRule monitoringRule,
			final ObjectId newMonitoringMessageGroupId) {
		monitoringRule
				.setRelatedMonitoringMessageGroup(newMonitoringMessageGroupId);

		databaseManagerService.saveModelObject(monitoringRule);
	}

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
					.isWriteProtectedVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
			}

			monitoringRule.setStoreValueToVariableWithName(variableName);

			databaseManagerService.saveModelObject(monitoringRule);
		}
	}

	public void monitoringRuleChangeHourToSendMessage(
			final MonitoringRule monitoringRule, final int newValue) {
		monitoringRule.setHourToSendMessage(newValue);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	public void monitoringRuleChangeHoursUntilMessageIsHandledAsUnanswered(
			final MonitoringRule monitoringRule, final int newValue) {
		monitoringRule.setHoursUntilMessageIsHandledAsUnanswered(newValue);

		databaseManagerService.saveModelObject(monitoringRule);
	}

	public void monitoringRuleDelete(final ObjectId monitoringRuleId) {
		databaseManagerService.deleteModelObject(MonitoringRule.class,
				monitoringRuleId);
	}

	public MonitoringReplyRule monitoringReplyRuleCreate(
			final ObjectId monitoringRuleId,
			final ObjectId parentMonitoringReplyRuleId,
			final boolean isGotAnswerRule) {
		val monitoringReplyRule = new MonitoringReplyRule("",
				RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "",
				parentMonitoringReplyRuleId, 0, null, false, null,
				isGotAnswerRule ? monitoringRuleId : null,
				isGotAnswerRule ? null : monitoringRuleId);

		val highestOrderRule = databaseManagerService
				.findOneSortedModelObject(
						MonitoringReplyRule.class,
						isGotAnswerRule ? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER
								: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
						Queries.MONITORING_REPLY_RULE__SORT_BY_ORDER_DESC,
						monitoringRuleId, parentMonitoringReplyRuleId);

		if (highestOrderRule != null) {
			monitoringReplyRule.setOrder(highestOrderRule.getOrder() + 1);
		}

		databaseManagerService.saveModelObject(monitoringReplyRule);

		return monitoringReplyRule;
	}

	public void monitoringReplyRuleMove(final int movement,
			final ObjectId monitoringReplyRuleIdToMove,
			final ObjectId referenceTargetId, final ObjectId monitoringRuleId,
			final boolean isGotAnswerRule) {
		if (movement == 0) {
			// At the beginning of sublist of target

			// Move all items in sublist one step down
			val otherMonitoringRulesToMove = databaseManagerService
					.findModelObjects(
							MonitoringReplyRule.class,
							isGotAnswerRule ? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER
									: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
							monitoringRuleId, referenceTargetId);
			for (val otherMonitoringRuleToMove : otherMonitoringRulesToMove) {
				otherMonitoringRuleToMove.setOrder(otherMonitoringRuleToMove
						.getOrder() + 1);
				databaseManagerService
						.saveModelObject(otherMonitoringRuleToMove);
			}

			// Set target as new parent
			val monitoringRuleToMove = databaseManagerService
					.getModelObjectById(MonitoringReplyRule.class,
							monitoringReplyRuleIdToMove);
			monitoringRuleToMove
					.setIsSubRuleOfMonitoringRule(referenceTargetId);

			// Set at beginning of list
			monitoringRuleToMove.setOrder(0);

			databaseManagerService.saveModelObject(monitoringRuleToMove);
		} else if (movement == 1 || movement == 2) {
			// Move above or below

			// Move all items in sublist after target one step
			// down
			val referenceTarget = databaseManagerService.getModelObjectById(
					MonitoringReplyRule.class, referenceTargetId);

			val otherMonitoringRulesToMove = databaseManagerService
					.findModelObjects(
							MonitoringReplyRule.class,
							isGotAnswerRule ? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_AND_ORDER_HIGHER_ONLY_GOT_ANSWER
									: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_AND_ORDER_HIGHER_ONLY_GOT_NO_ANSWER,
							monitoringRuleId, referenceTarget
									.getIsSubRuleOfMonitoringRule(),
							referenceTarget.getOrder());
			for (val otherMonitoringRuleToMove : otherMonitoringRulesToMove) {
				otherMonitoringRuleToMove.setOrder(otherMonitoringRuleToMove
						.getOrder() + 1);
				databaseManagerService
						.saveModelObject(otherMonitoringRuleToMove);
			}

			// Set parent of target to rule
			val monitoringRuleToMove = databaseManagerService
					.getModelObjectById(MonitoringReplyRule.class,
							monitoringReplyRuleIdToMove);
			monitoringRuleToMove.setIsSubRuleOfMonitoringRule(referenceTarget
					.getIsSubRuleOfMonitoringRule());
			log.debug("> " + referenceTarget.getIsSubRuleOfMonitoringRule());

			if (movement == 1) {
				// Set order to former order of target
				monitoringRuleToMove.setOrder(referenceTarget.getOrder());
				databaseManagerService.saveModelObject(monitoringRuleToMove);

				// Set order of target one down
				referenceTarget.setOrder(referenceTarget.getOrder() + 1);
				databaseManagerService.saveModelObject(referenceTarget);
			} else {
				// Set order to former order of target plus 1
				monitoringRuleToMove.setOrder(referenceTarget.getOrder() + 1);
				databaseManagerService.saveModelObject(monitoringRuleToMove);
			}
		}

	}

	public void monitoringReplyRuleChangeSendMessageIfTrue(
			final MonitoringReplyRule monitoringReplyRule,
			final boolean newValue) {
		monitoringReplyRule.setSendMessageIfTrue(newValue);

		databaseManagerService.saveModelObject(monitoringReplyRule);
	}

	public void monitoringReplyRuleChangeRelatedMonitoringMessageGroup(
			final MonitoringReplyRule monitoringReplyRule,
			final ObjectId newMonitoringMessageGroupId) {
		monitoringReplyRule
				.setRelatedMonitoringMessageGroup(newMonitoringMessageGroupId);

		databaseManagerService.saveModelObject(monitoringReplyRule);
	}

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
					.isWriteProtectedVariableName(variableName)) {
				throw new NotificationMessageException(
						AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
			}

			monitoringReplyRule.setStoreValueToVariableWithName(variableName);

			databaseManagerService.saveModelObject(monitoringReplyRule);
		}
	}

	public void monitoringReplyRuleDelete(final ObjectId monitoringReplyRuleId) {
		databaseManagerService.deleteModelObject(MonitoringReplyRule.class,
				monitoringReplyRuleId);
	}

	// Abstract Rule
	public void abstractRuleChangeRuleWithPlaceholders(
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

	public void abstractRuleChangeRuleComparisonTermWithPlaceholders(
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

			abstractRule
					.setRuleComparisonTermWithPlaceholders(textWithPlaceholders);
		}

		databaseManagerService.saveModelObject(abstractRule);
	}

	public void abstractRuleChangeEquationSign(final AbstractRule abstractRule,
			final RuleEquationSignTypes newType) {
		abstractRule.setRuleEquationSign(newType);

		databaseManagerService.saveModelObject(abstractRule);
	}

	// Participant
	public List<Participant> participantsImport(final File file,
			final ObjectId interventionId) throws FileNotFoundException,
			IOException {
		val importedParticipants = new ArrayList<Participant>();

		val importedModelObjects = modelObjectExchangeService
				.importModelObjects(file,
						ModelObjectExchangeFormatTypes.PARTICIPANTS);

		for (val modelObject : importedModelObjects) {
			if (modelObject instanceof Participant) {
				val participant = (Participant) modelObject;

				val screeningSurvey = databaseManagerService
						.findOneModelObject(
								ScreeningSurvey.class,
								Queries.SCREENING_SURVEY__BY_INTERVENTION_AND_GLOBAL_UNIQUE_ID,
								interventionId,
								participant
										.getAssignedScreeningSurveyGlobalUniqueId());

				if (screeningSurvey == null) {
					participant.setAssignedScreeningSurvey(null);
					participant.setAssignedScreeningSurveyGlobalUniqueId(null);

					participant.setAssignedFeedback(null);
					participant.setAssignedFeedbackGlobalUniqueId(null);
				} else {
					participant.setAssignedScreeningSurvey(screeningSurvey
							.getId());
					participant
							.setAssignedScreeningSurveyGlobalUniqueId(screeningSurvey
									.getGlobalUniqueId());

					val feedback = databaseManagerService
							.findOneModelObject(
									Feedback.class,
									Queries.FEEDBACK__BY_SCREENING_SURVEY_AND_GLOBAL_UNIQUE_ID,
									screeningSurvey.getId(),
									participant
											.getAssignedFeedbackGlobalUniqueId());
					if (feedback == null) {
						participant.setAssignedFeedback(null);
						participant.setAssignedFeedbackGlobalUniqueId(null);
					} else {
						participant.setAssignedFeedback(feedback.getId());
						participant.setAssignedFeedbackGlobalUniqueId(feedback
								.getGlobalUniqueId());
					}
				}

				participant.setIntervention(interventionId);
				databaseManagerService.saveModelObject(participant);

				importedParticipants.add(participant);
			}
		}

		return importedParticipants;
	}

	public File participantsExport(final List<Participant> participants) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		log.debug("Recursively collect all model objects related to the participants");
		for (val participant : participants) {
			participant
					.collectThisAndRelatedModelObjectsForExport(modelObjectsToExport);
		}

		log.debug("Export participants");
		return modelObjectExchangeService.exportModelObjects(
				modelObjectsToExport,
				ModelObjectExchangeFormatTypes.PARTICIPANTS);
	}

	public void participantsSetOrganization(
			final List<Participant> participants, final String newValue) {
		for (val participant : participants) {
			participant.setOrganization(newValue);

			databaseManagerService.saveModelObject(participant);
		}
	}

	public void participantsSetOrganizationUnit(
			final List<Participant> participants, final String newValue) {
		for (val participant : participants) {
			participant.setOrganizationUnit(newValue);

			databaseManagerService.saveModelObject(participant);
		}
	}

	public void participantsDelete(final List<Participant> participantsToDelete) {
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
	public Author getAuthor(final ObjectId authorId) {
		return databaseManagerService
				.getModelObjectById(Author.class, authorId);
	}

	public Iterable<Author> getAllAuthors() {
		return databaseManagerService.findModelObjects(Author.class,
				Queries.ALL);
	}

	public Iterable<Intervention> getAllInterventions() {
		return databaseManagerService.findModelObjects(Intervention.class,
				Queries.ALL);
	}

	public Iterable<Intervention> getAllInterventionsForAuthor(
			final ObjectId authorId) {
		val authorInterventionAccessForAuthor = databaseManagerService
				.findModelObjects(AuthorInterventionAccess.class,
						Queries.AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR, authorId);

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

	public Iterable<Author> getAllAuthorsOfIntervention(
			final ObjectId interventionId) {
		val authorInterventionAccessForIntervention = databaseManagerService
				.findModelObjects(AuthorInterventionAccess.class,
						Queries.AUTHOR_INTERVENTION_ACCESS__BY_INTERVENTION,
						interventionId);

		final List<Author> authors = new ArrayList<Author>();

		for (val authorInterventionAccess : authorInterventionAccessForIntervention) {
			val author = databaseManagerService.getModelObjectById(
					Author.class, authorInterventionAccess.getAuthor());

			if (author != null) {
				authors.add(author);
			} else {
				databaseManagerService.collectGarbage(authorInterventionAccess);
			}
		}

		return authors;
	}

	public Iterable<InterventionVariableWithValue> getAllInterventionVariablesOfIntervention(
			final ObjectId interventionId) {

		return databaseManagerService.findModelObjects(
				InterventionVariableWithValue.class,
				Queries.INTERVENTION_VARIABLE_WITH_VALUE__BY_INTERVENTION,
				interventionId);
	}

	public Iterable<MonitoringMessageGroup> getAllMonitoringMessageGroupsOfIntervention(
			final ObjectId interventionId) {
		return databaseManagerService.findSortedModelObjects(
				MonitoringMessageGroup.class,
				Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION,
				Queries.MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_ASC,
				interventionId);
	}

	public Iterable<MonitoringMessageGroup> getAllMonitoringMessageGroupsExpectingNoAnswerOfIntervention(
			final ObjectId interventionId) {
		return databaseManagerService
				.findSortedModelObjects(
						MonitoringMessageGroup.class,
						Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION_AND_EXPECTING_ANSWER,
						Queries.MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_ASC,
						interventionId, false);
	}

	public Iterable<MonitoringMessage> getAllMonitoringMessagesOfMonitoringMessageGroup(
			final ObjectId monitoringMessageGroupId) {
		return databaseManagerService.findSortedModelObjects(
				MonitoringMessage.class,
				Queries.MONITORING_MESSAGE__BY_MONITORING_MESSAGE_GROUP,
				Queries.MONITORING_MESSAGE__SORT_BY_ORDER_ASC,
				monitoringMessageGroupId);
	}

	public MonitoringMessage getMonitoringMessage(
			final ObjectId monitoringMessageId) {
		return databaseManagerService.getModelObjectById(
				MonitoringMessage.class, monitoringMessageId);
	}

	public MonitoringMessageGroup getMonitoringMessageGroup(
			final ObjectId monitoringMessageGroupId) {
		return databaseManagerService.getModelObjectById(
				MonitoringMessageGroup.class, monitoringMessageGroupId);
	}

	public Iterable<MonitoringMessageRule> getAllMonitoringMessageRulesOfMonitoringMessage(
			final ObjectId monitoringMessageId) {
		return databaseManagerService.findSortedModelObjects(
				MonitoringMessageRule.class,
				Queries.MONITORING_MESSAGE_RULE__BY_MONITORING_MESSAGE,
				Queries.MONITORING_MESSAGE_RULE__SORT_BY_ORDER_ASC,
				monitoringMessageId);
	}

	public Iterable<MonitoringRule> getAllMonitoringRulesOfIntervention(
			final ObjectId interventionId) {
		return databaseManagerService.findModelObjects(MonitoringRule.class,
				Queries.MONITORING_RULE__BY_INTERVENTION, interventionId);
	}

	public Iterable<MonitoringRule> getAllMonitoringRulesOfInterventionAndParent(
			final ObjectId interventionId, final ObjectId parentMonitoringRuleId) {
		return databaseManagerService.findSortedModelObjects(
				MonitoringRule.class,
				Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
				Queries.MONITORING_RULE__SORT_BY_ORDER_ASC, interventionId,
				parentMonitoringRuleId);
	}

	public MonitoringRule getMonitoringRule(final ObjectId monitoringRuleId) {
		return databaseManagerService.getModelObjectById(MonitoringRule.class,
				monitoringRuleId);
	}

	public Iterable<MonitoringReplyRule> getAllMonitoringReplyRulesOfMonitoringRule(
			final ObjectId monitoringRuleId, final boolean isGotAnswerRule) {
		return databaseManagerService
				.findModelObjects(
						MonitoringReplyRule.class,
						isGotAnswerRule ? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_ONLY_GOT_ANSWER
								: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_ONLY_GOT_NO_ANSWER,
						monitoringRuleId);
	}

	public Iterable<MonitoringReplyRule> getAllMonitoringReplyRulesOfMonitoringRuleAndParent(
			final ObjectId monitoringRuleId,
			final ObjectId parentMonitoringReplyRuleId,
			final boolean isGotAnswerRule) {
		return databaseManagerService
				.findSortedModelObjects(
						MonitoringReplyRule.class,
						isGotAnswerRule ? Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_ANSWER
								: Queries.MONITORING_REPLY_RULE__BY_MONITORING_RULE_AND_PARENT_ONLY_GOT_NO_ANSWER,
						Queries.MONITORING_RULE__SORT_BY_ORDER_ASC,
						monitoringRuleId, parentMonitoringReplyRuleId);
	}

	public Iterable<Participant> getAllParticipantsOfIntervention(
			final ObjectId interventionId) {
		return databaseManagerService.findModelObjects(Participant.class,
				Queries.PARTICIPANT__BY_INTERVENTION, interventionId);
	}

	public MonitoringReplyRule getMonitoringReplyRule(
			final ObjectId monitoringReplyRuleId) {
		return databaseManagerService.getModelObjectById(
				MonitoringReplyRule.class, monitoringReplyRuleId);
	}

	public MediaObject getMediaObject(final ObjectId mediaObjectId) {
		return databaseManagerService.getModelObjectById(MediaObject.class,
				mediaObjectId);
	}

	public File getFileByReference(final String fileReference) {
		return fileStorageManagerService.getFileByReference(fileReference);
	}

	public List<String> getAllPossibleMessageVariablesOfIntervention(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

		variables.addAll(variablesManagerService.getAllSystemVariableNames());

		variables.addAll(variablesManagerService
				.getAllInterventionVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllScreeningSurveyVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllMonitoringMessageVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllMonitoringRuleAndReplyRuleVariableNamesOfIntervention(interventionId));

		Collections.sort(variables);

		return variables;
	}

	public List<String> getAllPossibleMonitoringRuleVariablesOfIntervention(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

		variables.addAll(variablesManagerService.getAllSystemVariableNames());

		variables.addAll(variablesManagerService
				.getAllInterventionVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllScreeningSurveyVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllMonitoringMessageVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllMonitoringRuleAndReplyRuleVariableNamesOfIntervention(interventionId));

		Collections.sort(variables);

		return variables;
	}

	public List<String> getAllWritableMessageVariablesOfIntervention(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

		variables.addAll(variablesManagerService
				.getAllWritableSystemVariableNames());

		variables.addAll(variablesManagerService
				.getAllInterventionVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllScreeningSurveyVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllMonitoringMessageVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllMonitoringRuleAndReplyRuleVariableNamesOfIntervention(interventionId));

		Collections.sort(variables);

		return variables;
	}

	public List<String> getAllWritableMonitoringRuleVariablesOfIntervention(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

		variables.addAll(variablesManagerService
				.getAllWritableSystemVariableNames());

		variables.addAll(variablesManagerService
				.getAllInterventionVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllScreeningSurveyVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllMonitoringMessageVariableNamesOfIntervention(interventionId));
		variables
				.addAll(variablesManagerService
						.getAllMonitoringRuleAndReplyRuleVariableNamesOfIntervention(interventionId));

		Collections.sort(variables);

		return variables;
	}

	public DialogStatus getDialogStatusOfParticipant(
			final ObjectId participantId) {
		return databaseManagerService.findOneModelObject(DialogStatus.class,
				Queries.DIALOG_STATUS__BY_PARTICIPANT, participantId);
	}

	public List<Class<? extends AbstractModule>> getRegisteredModules() {
		return modules;
	}

	public Hashtable<String, AbstractVariableWithValue> getAllVariablesWithValuesOfParticipantAndSystem(
			final ObjectId participantId) {
		val participant = databaseManagerService.getModelObjectById(
				Participant.class, participantId);

		return variablesManagerService
				.getAllVariablesWithValuesOfParticipantAndSystem(participant);
	}

	public Participant getParticipant(final ObjectId participantId) {
		return databaseManagerService.getModelObjectById(Participant.class,
				participantId);
	}

	public Iterable<DialogMessage> getAllDialogMessagesOfParticipant(
			final ObjectId participantId) {
		return databaseManagerService.findSortedModelObjects(
				DialogMessage.class, Queries.DIALOG_MESSAGE__BY_PARTICIPANT,
				Queries.DIALOG_MESSAGE__SORT_BY_ORDER_ASC, participantId);
	}

	public Iterable<ParticipantVariableWithValue> getAllParticipantVariablesUpdatedWithinLast28Days(
			final ObjectId participantId, final String variableName) {
		return databaseManagerService
				.findModelObjects(
						ParticipantVariableWithValue.class,
						Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_PARTICIPANT_AND_VARIABLE_NAME_AND_LAST_UPDATED_HIGHER,
						participantId,
						variableName,
						InternalDateTime.currentTimeMillis()
								- ImplementationConstants.DAYS_TO_TIME_IN_MILLIS_MULTIPLICATOR
								* 28);
	}

	public List<DialogMessage> getAllDialogMessagesWhichAreNotAutomaticallyProcessableButAreNotProcessedOfIntervention(
			final ObjectId interventionid) {
		val dialogMessages = new ArrayList<DialogMessage>();

		val participantsOfIntervention = databaseManagerService
				.findModelObjects(Participant.class,
						Queries.PARTICIPANT__BY_INTERVENTION, interventionid);

		for (val participant : participantsOfIntervention) {
			val dialogMessagesOfParticipant = databaseManagerService
					.findModelObjects(
							DialogMessage.class,
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
}