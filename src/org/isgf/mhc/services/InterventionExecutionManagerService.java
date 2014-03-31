package org.isgf.mhc.services;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.server.DialogMessage;
import org.isgf.mhc.model.server.MediaObject;
import org.isgf.mhc.model.server.SystemUniqueId;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.VariablesManagerService;

@Log4j2
public class InterventionExecutionManagerService {
	private static InterventionExecutionManagerService	instance	= null;

	private final DatabaseManagerService				databaseManagerService;
	private final FileStorageManagerService				fileStorageManagerService;
	private final VariablesManagerService				variablesManagerService;

	private InterventionExecutionManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.variablesManagerService = variablesManagerService;

		log.info("Started.");
	}

	public static InterventionExecutionManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		if (instance == null) {
			instance = new InterventionExecutionManagerService(
					databaseManagerService, fileStorageManagerService,
					variablesManagerService);
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
	// System Unique Id
	@Synchronized
	public SystemUniqueId systemUniqueIdCreate(
			final DialogMessage relatedDialogMessage,
			final MediaObject relatedMediaObject) {

		val newestSystemUniqueId = databaseManagerService
				.findOneSortedModelObject(SystemUniqueId.class, Queries.ALL,
						Queries.SYSTEM_UNIQUE_ID__SORT_BY_SHORT_ID_DESC);

		final long nextShortId = newestSystemUniqueId == null ? 1
				: newestSystemUniqueId.getShortId() + 1;

		val newSystemUniqueId = new SystemUniqueId(nextShortId,
				relatedDialogMessage.getId(), relatedMediaObject.getId());

		databaseManagerService.saveModelObject(newSystemUniqueId);

		return newSystemUniqueId;
	}

	// Dialog Message
	public void dialogMessageSetMediaContentViewed(
			final ObjectId dialogMessageId) {
		val dialogMessage = databaseManagerService.getModelObjectById(
				DialogMessage.class, dialogMessageId);

		dialogMessage.setMediaContentViewed(true);

		databaseManagerService.saveModelObject(dialogMessage);
	}

	/*
	 * Special methods
	 */

	/*
	 * Getter methods
	 */
	public SystemUniqueId getSystemUniqueId(final long shortId) {
		return databaseManagerService.findOneModelObject(SystemUniqueId.class,
				Queries.SYSTEM_UNIQUE_ID__BY_SHORT_ID, shortId);
	}
}
