package org.isgf.mhc.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.RandomStringUtils;
import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.server.Author;
import org.isgf.mhc.model.server.AuthorInterventionAccess;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.model.server.InterventionVariableWithValue;
import org.isgf.mhc.model.server.MediaObject;
import org.isgf.mhc.model.server.MonitoringMessage;
import org.isgf.mhc.model.server.MonitoringMessageGroup;
import org.isgf.mhc.model.server.MonitoringRule;
import org.isgf.mhc.model.server.types.EquationSignTypes;
import org.isgf.mhc.model.server.types.MediaObjectTypes;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.ModelObjectExchangeService;
import org.isgf.mhc.services.internal.VariablesManagerService;
import org.isgf.mhc.tools.BCrypt;
import org.isgf.mhc.tools.GlobalUniqueIdGenerator;
import org.isgf.mhc.tools.StringValidator;
import org.isgf.mhc.ui.NotificationMessageException;

/**
 * @author Andreas Filler
 * 
 */
@Log4j2
public class InterventionAdministrationManagerService {
	private static InterventionAdministrationManagerService	instance	= null;

	private final DatabaseManagerService					databaseManagerService;
	private final FileStorageManagerService					fileStorageManagerService;
	private final VariablesManagerService					variablesManagerService;
	private final ModelObjectExchangeService				modelObjectExchangeService;

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
		val intervention = new Intervention(
				GlobalUniqueIdGenerator.createGlobalUniqueId(), name,
				System.currentTimeMillis(), false, false);

		if (name.equals("")) {
			intervention.setName(ImplementationContants.DEFAULT_OBJECT_NAME);
		}

		databaseManagerService.saveModelObject(intervention);

		return intervention;
	}

	public void interventionChangeName(final Intervention intervention,
			final String newName) {
		if (newName.equals("")) {
			intervention.setName(ImplementationContants.DEFAULT_OBJECT_NAME);
		} else {
			intervention.setName(newName);
		}

		databaseManagerService.saveModelObject(intervention);
	}

	public void interventionSetActive(final Intervention intervention,
			final boolean value) {
		intervention.setActive(value);

		databaseManagerService.saveModelObject(intervention);
	}

	public void interventionSetMessagingActive(final Intervention intervention,
			final boolean value) {
		intervention.setMessagingActive(value);

		databaseManagerService.saveModelObject(intervention);
	}

	public File interventionExport(final Intervention intervention) {
		final List<ModelObject> modelObjectsToExport = new ArrayList<ModelObject>();

		modelObjectsToExport.add(intervention);
		// TODO add also other relevant model objects

		return modelObjectExchangeService
				.exportModelObjects(modelObjectsToExport);
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

		if (variablesManagerService
				.isWriteProtectedParticipantOrSystemVariable(variableName)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
		}

		val interventionVariables = databaseManagerService
				.findModelObjects(
						InterventionVariableWithValue.class,
						Queries.INTERVENTION_VARIABLES_WITH_VALUES__BY_INTERVENTION_AND_NAME,
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

	public void interventionVariableWithValueChangeName(
			final InterventionVariableWithValue interventionVariableWithValue,
			final String newName) throws NotificationMessageException {

		if (!StringValidator.isValidVariableName(newName)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_NOT_VALID);
		}

		if (variablesManagerService
				.isWriteProtectedParticipantOrSystemVariable(newName)) {
			throw new NotificationMessageException(
					AdminMessageStrings.NOTIFICATION__THE_GIVEN_VARIABLE_NAME_IS_RESERVED_BY_THE_SYSTEM);
		}

		val interventionVariables = databaseManagerService
				.findModelObjects(
						InterventionVariableWithValue.class,
						Queries.INTERVENTION_VARIABLES_WITH_VALUES__BY_INTERVENTION_AND_NAME,
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
				groupName, false);

		if (monitoringMessageGroup.getName().equals("")) {
			monitoringMessageGroup
					.setName(ImplementationContants.DEFAULT_OBJECT_NAME);
		}

		databaseManagerService.saveModelObject(monitoringMessageGroup);

		return monitoringMessageGroup;
	}

	public void monitoringMessageGroupSetSentOrder(
			final MonitoringMessageGroup monitoringMessageGroup,
			final boolean newValue) {
		monitoringMessageGroup.setSendInRandomOrder(newValue);

		databaseManagerService.saveModelObject(monitoringMessageGroup);
	}

	public void monitoringMessageGroupChangeName(
			final MonitoringMessageGroup monitoringMessageGroup,
			final String newName) {
		if (newName.equals("")) {
			monitoringMessageGroup
					.setName(ImplementationContants.DEFAULT_OBJECT_NAME);
		} else {
			monitoringMessageGroup.setName(newName);
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
					.isWriteProtectedParticipantOrSystemVariable(variableName)) {
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
				EquationSignTypes.EQUALS,
				"",
				parentMonitoringRuleId,
				0,
				null,
				false,
				null,
				interventionId,
				ImplementationContants.DEFAULT_HOUR_TO_SEND_MESSAGE,
				ImplementationContants.DEFAULT_HOURS_UNTIL_MESSAGE_IS_HANDLED_AS_UNANSWERED);

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
			final MonitoringRule monitoringRuleToMove = databaseManagerService
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
			final MonitoringRule referenceTarget = databaseManagerService
					.getModelObjectById(MonitoringRule.class, referenceTargetId);

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
			final MonitoringRule monitoringRuleToMove = databaseManagerService
					.getModelObjectById(MonitoringRule.class,
							monitoringRuleIdToMove);
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

	public void monitoringRuleDelete(final ObjectId monitoringRuleId) {
		databaseManagerService.deleteModelObject(MonitoringRule.class,
				monitoringRuleId);
	}

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
				Queries.INTERVENTION_VARIABLES_WITH_VALUES__BY_INTERVENTION,
				interventionId);
	}

	public Iterable<MonitoringMessageGroup> getAllMonitoringMessageGroupsOfIntervention(
			final ObjectId interventionId) {
		return databaseManagerService.findSortedModelObjects(
				MonitoringMessageGroup.class,
				Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION,
				Queries.MONITORING_MESSAGE_GROUP__SORT_BY_NAME_ASC,
				interventionId);
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

	public MediaObject getMediaObject(final ObjectId mediaObjectId) {
		return databaseManagerService.getModelObjectById(MediaObject.class,
				mediaObjectId);
	}

	public File getFileByReference(final String fileReference) {
		return fileStorageManagerService.getFileByReference(fileReference);
	}

	public List<String> getAllPossibleMessageVariables(
			final ObjectId interventionId) {
		val variables = new ArrayList<String>();

		variables.addAll(Arrays.asList(variablesManagerService
				.getAllSystemVariables()));

		variables.addAll(variablesManagerService
				.getAllInterventionVariables(interventionId));
		variables.addAll(variablesManagerService
				.getAllInterventionScreeningSurveyVariables(interventionId));
		variables.addAll(variablesManagerService
				.getAllMonitoringMessageVariables(interventionId));
		variables.addAll(variablesManagerService
				.getAllMonitoringRuleAndReplyRuleVariables(interventionId));

		Collections.sort(variables);

		return variables;
	}

}
