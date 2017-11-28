package ch.ethz.mc.services.internal;

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
import ch.ethz.mc.model.persistent.Author;
import ch.ethz.mc.model.persistent.consistency.DataModelConfiguration;
import ch.ethz.mc.tools.BCrypt;
import ch.ethz.mc.tools.DataModelUpdateManager;
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

			// Ensure indices
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
			log.error("Error at creating MongoDB connection: {}",
					e.getMessage());
			throw new Exception(
					"Error at creating MongoDB connection: " + e.getMessage());
		}

		// Give Jongo object to model object
		configure(jongo);

		// Doing database updates
		try {
			updateDataToVersionIfNecessary(expectedVersion);
		} catch (final Exception e) {
			log.error("Error at updating database: {}", e.getMessage());
			throw new Exception(
					"Error at updating database: " + e.getMessage());
		}

		// Checking for admin account
		val authors = findModelObjects(Author.class,
				Queries.AUTHOR__ADMIN_TRUE);
		if (!authors.iterator().hasNext()) {
			// Create new admin account if none exists
			log.warn(
					"No admin account has been found! One will be created as '{}' with password '{}'",
					Constants.getDefaultAdminUsername(),
					Constants.getDefaultAdminPassword());
			val author = new Author(true, Constants.getDefaultAdminUsername(),
					BCrypt.hashpw(Constants.getDefaultAdminPassword(),
							BCrypt.gensalt()));
			saveModelObject(author);
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
	public void collectGarbage(final ModelObject modelObject) {
		log.error(
				"The model object with id {} and class {} is inconsistent with the datastate and will be deleted",
				modelObject, modelObject.getClass());
		deleteModelObject(modelObject);
	}
}
