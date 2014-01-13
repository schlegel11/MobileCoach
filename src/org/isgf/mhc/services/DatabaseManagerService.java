package org.isgf.mhc.services;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.model.Indices;
import org.isgf.mhc.model.ModelObject;
import org.jongo.Jongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@Log4j2
public class DatabaseManagerService {
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
			this.mongoClient = new MongoClient(new ServerAddress(
					Constants.DATABASE_HOST, Constants.DATABASE_PORT),
					mongoCredentials);

			// Checking connection
			log.debug("Existing collections in database {}: ",
					Constants.DATABASE_NAME);
			for (val collection : this.mongoClient.getDB(
					Constants.DATABASE_NAME).getCollectionNames()) {
				log.debug(" {}", collection);
			}

			// Creating Jongo object
			this.jongo = new Jongo(
					this.mongoClient.getDB(Constants.DATABASE_NAME));

			// Ensure indices
			log.debug("Creating/ensuring indices: ");
			val indicesHashtable = Indices.getIndices();
			val indicesHashtableKeys = indicesHashtable.keys();
			while (indicesHashtableKeys.hasMoreElements()) {
				val clazz = indicesHashtableKeys.nextElement();
				final String[] indices = indicesHashtable.get(clazz);
				val collection = this.jongo
						.getCollection(clazz.getSimpleName());
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
		ModelObject.configure(this.jongo);

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

		this.mongoClient.close();

		log.info("Stopped.");
	}

	/**
	 * Returns {@link Jongo} object
	 * 
	 * @return
	 */
	public Jongo getDB() {
		return this.jongo;
	}
}
