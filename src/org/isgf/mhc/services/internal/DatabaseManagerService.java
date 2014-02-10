package org.isgf.mhc.services.internal;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.model.AbstractModelObjectAccessService;
import org.isgf.mhc.model.Indices;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.server.Author;
import org.isgf.mhc.tools.BCrypt;
import org.jongo.Jongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@Log4j2
public class DatabaseManagerService extends AbstractModelObjectAccessService {
	private static DatabaseManagerService	instance	= null;

	private MongoClient						mongoClient;
	private Jongo							jongo;

	private DatabaseManagerService() throws Exception {
		log.info("Starting service...");
		try {
			// Creating MongoDB driver object
			final List<MongoCredential> mongoCredentials = new ArrayList<MongoCredential>();
			mongoCredentials.add(MongoCredential.createMongoCRCredential(
					Constants.getDatabaseUser(), Constants.getDatabaseName(),
					Constants.getDatabasePassword().toCharArray()));
			mongoClient = new MongoClient(new ServerAddress(
					Constants.getDatabaseHost(), Constants.getDatabasePort()),
					mongoCredentials);

			// Checking connection
			log.debug("Existing collections in database {}: ",
					Constants.getDatabaseName());
			for (val collection : mongoClient
					.getDB(Constants.getDatabaseName()).getCollectionNames()) {
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
		} catch (final UnknownHostException e) {
			log.error("Error at creating MongoDB connection: {}",
					e.getMessage());
			throw new Exception("Error at creating MongoDB connection: "
					+ e.getMessage());
		}

		// Give Jongo object to model object
		configure(jongo);

		// Checking for admin account
		val authors = findModelObjects(Author.class, Queries.AUTHOR__ADMIN_TRUE);
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

	public static DatabaseManagerService start() throws Exception {
		if (instance == null) {
			instance = new DatabaseManagerService();
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
}
