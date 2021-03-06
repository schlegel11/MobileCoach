package ch.ethz.mc.services.internal;

/* ##LICENSE## */
import java.util.ArrayList;

import org.jongo.Jongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.AbstractModelObjectAccessService;
import ch.ethz.mc.model.Indices;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.BackendUser;
import ch.ethz.mc.model.persistent.BackendUserInterventionAccess;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MicroDialogRule;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.consistency.DataModelConfiguration;
import ch.ethz.mc.model.persistent.types.BackendUserTypes;
import ch.ethz.mc.tools.BCrypt;
import ch.ethz.mc.tools.DataModelUpdateManager;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DatabaseManagerService extends AbstractModelObjectAccessService {
	private static DatabaseManagerService	instance	= null;

	private MongoClient						mongoClient;
	private Jongo							jongo;

	// TODO LONGTERM This warning will be removed when Jongo will support the
	// new MongoDB
	// API
	@SuppressWarnings("deprecation")
	private DatabaseManagerService(final int expectedVersion) throws Exception {
		super();
		log.info("Starting service...");
		try {
			// Creating MongoDB driver object
			val mongoCredentials = new ArrayList<MongoCredential>();
			mongoCredentials.add(MongoCredential.createCredential(
					Constants.getDatabaseUser(), Constants.getDatabaseName(),
					Constants.getDatabasePassword().toCharArray()));
			val mongoDBOptions = MongoClientOptions.builder()
					.socketKeepAlive(true).build();
			mongoClient = new MongoClient(
					new ServerAddress(Constants.getDatabaseHost(),
							Constants.getDatabasePort()),
					mongoCredentials, mongoDBOptions);

			// Checking connection
			log.debug("Existing collections in database {}: ",
					Constants.getDatabaseName());
			for (val collection : mongoClient
					.getDatabase(Constants.getDatabaseName())
					.listCollectionNames()) {
				log.debug(" {}", collection);
			}

			// Creating Jongo object
			jongo = new Jongo(mongoClient.getDB(Constants.getDatabaseName()));
		} catch (final Exception e) {
			log.error("Error at creating MongoDB connection: {}",
					e.getMessage());
			throw new Exception(
					"Error at creating MongoDB connection: " + e.getMessage());
		}

		// Give Jongo object to model object
		configure(jongo);

		// Ensure indices (1 of 2)
		try {
			log.debug("Creating/ensuring indices: ");
			val indicesHashtable = Indices.getIndices();
			val indicesHashtableKeys = indicesHashtable.keys();
			while (indicesHashtableKeys.hasMoreElements()) {
				val clazz = indicesHashtableKeys.nextElement();
				final String[] indices = indicesHashtable.get(clazz);
				val collection = jongo.getCollection(clazz.getSimpleName());

				for (final String index : indices) {
					log.debug("Creating/ensuring index {} on collection {}",
							index, clazz.getSimpleName());
					collection.ensureIndex(index);
				}
			}
		} catch (final Exception e) {
			log.error("Error at creating/ensuring database indices: {}",
					e.getMessage());
			throw new Exception("Error at creating/ensuring database indices: "
					+ e.getMessage());
		}

		// Doing database updates
		try {
			updateDataToVersionIfNecessary(expectedVersion);
		} catch (final Exception e) {
			log.error("Error at updating database: {}", e.getMessage());
			throw new Exception(
					"Error at updating database: " + e.getMessage());
		}

		// Ensure indices (2 of 2)
		try {
			log.debug("Creating/ensuring indices: ");
			val indicesHashtable = Indices.getIndices();
			val indicesHashtableKeys = indicesHashtable.keys();
			while (indicesHashtableKeys.hasMoreElements()) {
				val clazz = indicesHashtableKeys.nextElement();
				final String[] indices = indicesHashtable.get(clazz);
				val collection = jongo.getCollection(clazz.getSimpleName());

				for (final String index : indices) {
					log.debug("Creating/ensuring index {} on collection {}",
							index, clazz.getSimpleName());
					collection.ensureIndex(index);
				}
			}
		} catch (final Exception e) {
			log.error("Error at creating/ensuring database indices: {}",
					e.getMessage());
			throw new Exception("Error at creating/ensuring database indices: "
					+ e.getMessage());
		}

		// Checking for admin account
		val backendUsers = findModelObjects(BackendUser.class,
				Queries.BACKEND_USER__BY_TYPE, BackendUserTypes.ADMIN);
		if (!backendUsers.iterator().hasNext()) {
			// Create new admin account if none exists
			log.warn(
					"No admin account has been found! One will be created as '{}' with password '{}'",
					Constants.getDefaultAdminUsername(),
					Constants.getDefaultAdminPassword());
			val backendUser = new BackendUser(BackendUserTypes.ADMIN,
					Constants.getDefaultAdminUsername(),
					BCrypt.hashpw(Constants.getDefaultAdminPassword(),
							BCrypt.gensalt()));
			saveModelObject(backendUser);
		}

		log.info("Started.");
	}

	/**
	 * Update data to appropriate version
	 *
	 * @param versionToBeReached
	 * @throws Exception
	 */
	private void updateDataToVersionIfNecessary(final int versionToBeReached)
			throws Exception {
		log.debug("Retrieving current data model version from database...");

		// -1 if no version available = first DB setup
		int currentVersion = -1;

		// get current version if available in DB
		DataModelConfiguration configuration = null;
		try {
			val configurationCollection = jongo
					.getCollection(Constants.DATA_MODEL_CONFIGURATION);
			configuration = configurationCollection.findOne()
					.as(DataModelConfiguration.class);
		} catch (final Exception e) {
			log.error("Error at retrieving data model configuration: {}",
					e.getMessage());
			throw e;
		}

		if (configuration != null) {
			currentVersion = configuration.getVersion();
			log.info("Database is on data model version {}", currentVersion);
			if (currentVersion > 0 && currentVersion < 4) {
				log.error(
						"The database is on a very old version ({})! Use MC 1.6.0 to update to a newer state of the database before proceeding with this version.",
						currentVersion);
				throw new Exception("The database is on a very old version ("
						+ currentVersion
						+ ")! Use MC 1.6.0 to update to a newer state of the database before proceeding with this version.");
			}
		} else {
			log.info("Database is new - no data model version");
		}

		// perform update
		DataModelUpdateManager.updateDataFromVersionToVersion(currentVersion,
				versionToBeReached, jongo);

		// clear indices when current and expected database versions are
		// different
		if (currentVersion != versionToBeReached) {
			for (val collectionName : jongo.getDatabase()
					.getCollectionNames()) {
				if (!collectionName.startsWith("system.")) {
					log.info(
							"Index of collection '{}' will be dropped because of database version change",
							collectionName);
					val collection = jongo.getCollection(collectionName);
					collection.dropIndexes();
				}
			}
		}

		log.info("Database is now on data model version {}",
				versionToBeReached);
	}

	public static DatabaseManagerService start(final int expectedVersion)
			throws Exception {
		if (instance == null) {
			instance = new DatabaseManagerService(expectedVersion);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		mongoClient.close();

		log.info("Stopped.");
	}

	/*
	 * Class methods
	 */
	/**
	 * Ensure database consistency
	 */
	public void ensureDatabaseConsistency() {
		log.info("Ensuring database consistency...");

		log.info("Checking rules...");

		// Check recursive data model structures for inconsistencies or obsolete
		// objects

		int rulesChecked = 0;

		val allMonitoringRules = findModelObjects(MonitoringRule.class,
				Queries.ALL);
		for (val monitoringRule : allMonitoringRules) {
			rulesChecked++;

			if (monitoringRule.getIsSubRuleOfMonitoringRule() != null) {
				val relatedRule = getModelObjectById(MonitoringRule.class,
						monitoringRule.getIsSubRuleOfMonitoringRule());
				if (relatedRule == null) {
					log.warn("Deleting unlinked rule with id {}",
							monitoringRule.getId());
					deleteModelObject(monitoringRule);
				}
			}
		}

		val allMonitoringReplyRules = findModelObjects(
				MonitoringReplyRule.class, Queries.ALL);
		for (val monitoringReplyRule : allMonitoringReplyRules) {
			rulesChecked++;

			if (monitoringReplyRule.getIsSubRuleOfMonitoringRule() != null) {
				val relatedRule = getModelObjectById(MonitoringReplyRule.class,
						monitoringReplyRule.getIsSubRuleOfMonitoringRule());
				if (relatedRule == null) {
					log.warn("Deleting unlinked rule with id {}",
							monitoringReplyRule.getId());
					deleteModelObject(monitoringReplyRule);
				}
			}
		}

		val allMicroDialogRules = findModelObjects(MicroDialogRule.class,
				Queries.ALL);
		for (val microDialogRule : allMicroDialogRules) {
			rulesChecked++;

			if (microDialogRule.getIsSubRuleOfMonitoringRule() != null) {
				val relatedRule = getModelObjectById(MicroDialogRule.class,
						microDialogRule.getIsSubRuleOfMonitoringRule());
				if (relatedRule == null) {
					log.warn("Deleting unlinked rule with id {}",
							microDialogRule.getId());
					deleteModelObject(microDialogRule);
				}
			}
		}

		log.info("{} rules checked.", rulesChecked);

		log.info("Checking backend user intervention access...");

		int linksChecked = 0;

		val allBackendUserInterventionAccesses = findModelObjects(
				BackendUserInterventionAccess.class, Queries.ALL);

		for (val backendUserInterventionAccess : allBackendUserInterventionAccesses) {
			linksChecked++;

			val backendUser = getModelObjectById(BackendUser.class,
					backendUserInterventionAccess.getBackendUser());
			val intervention = getModelObjectById(Intervention.class,
					backendUserInterventionAccess.getIntervention());

			if (backendUser == null || intervention == null) {
				log.warn("Deleting backend user intervention access id {}",
						backendUserInterventionAccess.getId());
				deleteModelObject(backendUserInterventionAccess);
			}
		}

		log.info("{} links checked.", linksChecked);

		log.info("Database consistency ensured.");
	}

	/**
	 * Corrects potentially wrong participant timings
	 * 
	 * @param databaseManagerService
	 */
	@Synchronized
	public void ensureSensefulParticipantTimings() {
		log.info("Ensuring senseful participant login/logout timinigs...");

		val participants = findModelObjects(Participant.class,
				Queries.PARTICIPANT__WHERE_LAST_LOGIN_TIME_IS_BIGGER_THAN_LAST_LOGOUT_TIME);

		for (val participant : participants) {
			participant.setLastLogoutTimestamp(
					participant.getLastLoginTimestamp());

			saveModelObject(participant);
		}

		log.info("Done.");
	}

	/**
	 * Remove inconsistent {@link ModelObject}s
	 * 
	 * @param modelObject
	 */
	public void collectGarbage(final ModelObject modelObject) {
		log.error(
				"The model object with id {} and class {} is inconsistent with the datastate and will be deleted",
				modelObject, modelObject.getClass());
		deleteModelObject(modelObject);
	}
}
