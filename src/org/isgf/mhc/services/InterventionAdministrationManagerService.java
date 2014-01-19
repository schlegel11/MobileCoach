package org.isgf.mhc.services;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class InterventionAdministrationManagerService {
	private static InterventionAdministrationManagerService	instance	= null;

	private InterventionAdministrationManagerService() throws Exception {
		log.info("Starting service...");

		log.info("Started.");
	}

	public static InterventionAdministrationManagerService start() throws Exception {
		if (instance == null) {
			instance = new InterventionAdministrationManagerService();
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}
}
