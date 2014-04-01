package org.isgf.mhc.services;

import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.persistent.DialogMessage;
import org.isgf.mhc.model.persistent.MediaObject;
import org.isgf.mhc.model.persistent.SystemUniqueId;
import org.isgf.mhc.services.internal.CommunicationManagerService;
import org.isgf.mhc.services.internal.DatabaseManagerService;
import org.isgf.mhc.services.internal.FileStorageManagerService;
import org.isgf.mhc.services.internal.VariablesManagerService;

@Log4j2
public class InterventionExecutionManagerService {
	private static InterventionExecutionManagerService	instance	= null;

	private final DatabaseManagerService				databaseManagerService;
	private final FileStorageManagerService				fileStorageManagerService;
	private final VariablesManagerService				variablesManagerService;
	private final CommunicationManagerService			communicationManagerService;

	private final InternalIncomingMessageWorker			internalIncomingMessageWorker;
	private final InternalOutgoingMessageWorker			internalOutgoingMessageWorker;

	private InterventionExecutionManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final CommunicationManagerService communicationManagerService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.variablesManagerService = variablesManagerService;
		this.communicationManagerService = communicationManagerService;

		internalOutgoingMessageWorker = new InternalOutgoingMessageWorker();
		internalOutgoingMessageWorker.start();
		internalIncomingMessageWorker = new InternalIncomingMessageWorker();
		internalIncomingMessageWorker.start();

		log.info("Started.");
	}

	public static InterventionExecutionManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService,
			final CommunicationManagerService communicationManagerService)
			throws Exception {
		if (instance == null) {
			instance = new InterventionExecutionManagerService(
					databaseManagerService, fileStorageManagerService,
					variablesManagerService, communicationManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.debug("Stopping incoming message worker");
		synchronized (internalIncomingMessageWorker) {
			internalIncomingMessageWorker.interrupt();
			internalIncomingMessageWorker.join();
		}
		log.debug("Stopping outgoing message worker");
		synchronized (internalOutgoingMessageWorker) {
			internalOutgoingMessageWorker.interrupt();
			internalOutgoingMessageWorker.join();
		}

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
	@Synchronized
	public void dialogMessageSetMediaContentViewed(
			final ObjectId dialogMessageId) {
		val dialogMessage = databaseManagerService.getModelObjectById(
				DialogMessage.class, dialogMessageId);

		dialogMessage.setMediaContentViewed(true);

		databaseManagerService.saveModelObject(dialogMessage);
	}

	@Synchronized
	public void dialogMessageSetSent(final ObjectId dialogMessageId,
			final long sentTimestamp) {
		val dialogMessage = databaseManagerService.getModelObjectById(
				DialogMessage.class, dialogMessageId);

		dialogMessage.setSentTimestamp(sentTimestamp);

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

	/*
	 * Internal classes
	 */
	/**
	 * Manages the handling of incoming messages
	 * 
	 * @author Andreas Filler
	 */
	private class InternalIncomingMessageWorker extends Thread {
		public InternalIncomingMessageWorker() {
			setName("Internal Incoming Message Worker");
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				// TODO A LOT
				System.out.println("Und er läuft und er läuft und er läuft");
				// HIER LOKALER KLASSENAUFRUF

				try {
					sleep(ImplementationContants.MAILING_RECEIVE_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES);
				} catch (final InterruptedException e) {
					interrupt();
					System.out.println("Unterbrechung in sleep()");
				}
				// TODO A LOT
			}
		}
	}

	/**
	 * Manages the handling of outgoing messages
	 * 
	 * @author Andreas Filler
	 */
	private class InternalOutgoingMessageWorker extends Thread {
		public InternalOutgoingMessageWorker() {
			setName("Internal Outgoing Message Worker");
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				// TODO A LOT

				// DETERMINE MESSAGES WHICH SHOULD HAVE BEEN SENT BUT AREN'T
				// SENT YET...and are currently not in SEND QUEUE...
				// sending-state? --> SENT auf bestimmte zahl und RESET beim
				// NEUSTART des SYSTEMS (in diesem FAlle kann sogar der Thread
				// dies beim Start aufräumen)

				// HIER WERDEN AUCH DIE MESSAGES GESENDET

				System.out.println("Und er läuft und er läuft und er läuft");
				// HIER LOKALER KLASSENAUFRUF

				try {
					sleep(ImplementationContants.MAILING_PREPARATION_SECONDS_SLEEP_BETWEEN_CHECK_CYCLES);
				} catch (final InterruptedException e) {
					interrupt();
					System.out.println("Unterbrechung in sleep()");
				}
				// TODO A LOT
			}
		}
	}
}
