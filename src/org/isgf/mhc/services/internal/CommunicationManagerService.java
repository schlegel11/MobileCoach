package org.isgf.mhc.services.internal;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CommunicationManagerService {
	private static CommunicationManagerService	instance	= null;

	private CommunicationManagerService() throws Exception {
		log.info("Starting service...");

		log.info("Started.");
	}

	public static CommunicationManagerService start() throws Exception {
		if (instance == null) {
			instance = new CommunicationManagerService();
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}
}
