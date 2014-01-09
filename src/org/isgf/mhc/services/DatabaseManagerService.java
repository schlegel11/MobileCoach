package org.isgf.mhc.services;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.Constants;
import org.isgf.mhc.model.ModelObject;
import org.jongo.Jongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@Log4j2
public class DatabaseManagerService {
	private static DatabaseManagerService	instance	= null;

	private MongoClient						mongoClient;
	private Jongo							jongo;

	private final ObjectMapper				objectMapper;

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

			// Creating Jongo object
			this.jongo = new Jongo(
					this.mongoClient.getDB(Constants.DATABASE_NAME));

			// Ensure indices
			// TODO Ensure indices
			// final MongoCollection users = this.jongo
			// .getCollection(Constants.COLL_USERS);
			// users.ensureIndex(Constants.COLL_USERS_INDEX_1);
			// users.ensureIndex(Constants.COLL_USERS_INDEX_2);
		} catch (final UnknownHostException e) {
			log.error("Error at creating MongoDB connection: " + e.getMessage());
			throw new Exception("Error at creating MongoDB connection: "
					+ e.getMessage());
		}

		// Create Jackson JSON mapper
		this.objectMapper = new ObjectMapper();
		ModelObject.configure(this.objectMapper, this.jongo);

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

	public Jongo getDB() {
		return this.jongo;
	}
}
