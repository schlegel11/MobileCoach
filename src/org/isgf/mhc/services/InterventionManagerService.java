package org.isgf.mhc.services;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class InterventionManagerService {
	private static InterventionManagerService	instance	= null;

	private InterventionManagerService() throws Exception {
		log.info("Starting service...");

		log.info("Started.");
	}

	public static InterventionManagerService start() throws Exception {
		if (instance == null) {
			instance = new InterventionManagerService();
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}
}
