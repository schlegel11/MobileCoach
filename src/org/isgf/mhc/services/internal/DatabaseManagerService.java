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
					Constants.DATABASE_NAME, Constants.DATABASE_NAME,
					Constants.DATABASE_PASSWORD.toCharArray()));
			mongoClient = new MongoClient(new ServerAddress(
					Constants.DATABASE_HOST, Constants.DATABASE_PORT),
					mongoCredentials);

			// Checking connection
			log.debug("Existing collections in database {}: ",
					Constants.DATABASE_NAME);
			for (val collection : mongoClient.getDB(Constants.DATABASE_NAME)
					.getCollectionNames()) {
				log.debug(" {}", collection);
			}

			// Creating Jongo object
			jongo = new Jongo(mongoClient.getDB(Constants.DATABASE_NAME));

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
		val authors = findModelObjects(Author.class, Queries.AUTHORS_ADMINS);
		if (!authors.iterator().hasNext()) {
			// Create new admin account if none exists
			log.warn(
					"No admin account has been found! One will be created as '{}' with password '{}'",
					Constants.DEFAULT_ADMIN_USERNAME,
					Constants.DEFAULT_ADMIN_PASSWORD);
			val author = new Author(true, Constants.DEFAULT_ADMIN_USERNAME,
					BCrypt.hashpw(Constants.DEFAULT_ADMIN_PASSWORD,
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
