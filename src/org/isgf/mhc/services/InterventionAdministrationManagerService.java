package org.isgf.mhc.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.RandomStringUtils;
import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.server.Author;
import org.isgf.mhc.model.server.AuthorInterventionAccess;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.ModelObjectExchangeService;
import org.isgf.mhc.tools.BCrypt;
import org.isgf.mhc.tools.GlobalUniqueIdGenerator;
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
	private final ModelObjectExchangeService				modelObjectExchangeService;

	private InterventionAdministrationManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final ModelObjectExchangeService modelObjectExchangeService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.modelObjectExchangeService = modelObjectExchangeService;

		log.info("Started.");
	}

	public static InterventionAdministrationManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final ModelObjectExchangeService modelObjectExchangeService)
			throws Exception {
		if (instance == null) {
			instance = new InterventionAdministrationManagerService(
					databaseManagerService, fileStorageManagerService,
					modelObjectExchangeService);
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
				System.currentTimeMillis(), false, false, 16, 5);

		if (name.equals("")) {
			intervention.setName("---");
		}

		databaseManagerService.saveModelObject(intervention);

		return intervention;
	}

	public void interventionChangeName(final Intervention intervention,
			final String newName) {
		if (newName.equals("")) {
			intervention.setName("---");
		} else {
			intervention.setName(newName);
		}

		databaseManagerService.saveModelObject(intervention);
	}

	public void interventionChangeHourOfDailyRuleExecution(
			final Intervention intervention, final int value) {
		intervention.setHourOfDailyRuleExecutionStart(value);

		databaseManagerService.saveModelObject(intervention);
	}

	public void interventionChangeSecondsDelayBetweenParticipantsRuleExecution(
			final Intervention intervention, final int value) {
		intervention.setSecondsDelayBetweenParticipantsRuleExecution(value);

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
	public void authorInterventionAccessCreate(final ObjectId authorId,
			final ObjectId interventionId) {

		val authorInterventionAccess = new AuthorInterventionAccess(authorId,
				interventionId);

		databaseManagerService.saveModelObject(authorInterventionAccess);
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

	/*
	 * Getter methods
	 */
	public Author getAuthor(final ObjectId accountObjectId) {
		return databaseManagerService.getModelObjectById(Author.class,
				accountObjectId);
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
}
