package org.isgf.mhc.services;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class InterventionExecutionManagerService {
	private static InterventionExecutionManagerService	instance	= null;

	private InterventionExecutionManagerService() throws Exception {
		log.info("Starting service...");

		log.info("Started.");
	}

	public static InterventionExecutionManagerService start() throws Exception {
		if (instance == null) {
			instance = new InterventionExecutionManagerService();
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}
}
