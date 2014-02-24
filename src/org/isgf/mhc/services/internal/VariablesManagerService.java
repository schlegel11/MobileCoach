package org.isgf.mhc.services.internal;

import java.util.HashSet;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.conf.Variables;

/**
 * Manages all variables for the system and a specific participant
 * 
 * @author Andreas Filler
 */
@Log4j2
public class VariablesManagerService {
	private static VariablesManagerService	instance	= null;

	private final DatabaseManagerService	databaseManagerService;

	private final HashSet<String>			allSystemVariables;
	private final HashSet<String>			writeProtectedVariables;

	private VariablesManagerService(
			final DatabaseManagerService databaseManagerService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;

		writeProtectedVariables = new HashSet<String>();
		for (val variable : Variables.READ_ONLY_SYSTEM_VARIABLES.values()) {
			writeProtectedVariables.add("$" + variable.name());
		}
		for (val variable : Variables.READ_ONLY_PARTICIPANT_VARIABLES.values()) {
			writeProtectedVariables.add("$" + variable.name());
		}

		allSystemVariables = new HashSet<String>();
		allSystemVariables.addAll(writeProtectedVariables);
		for (val variable : Variables.READ_WRITE_PARTICIPANT_VARIABLES.values()) {
			allSystemVariables.add("$" + variable.name());
		}
		for (val variable : Variables.READ_WRITE_SYSTEM_VARIABLES.values()) {
			allSystemVariables.add("$" + variable.name());
		}

		log.info("Started.");
	}

	public static VariablesManagerService start(
			final DatabaseManagerService databaseManagerService)
			throws Exception {
		if (instance == null) {
			instance = new VariablesManagerService(databaseManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	public boolean isWriteProtectedParticipantOrSystemVariable(
			final String variable) {
		return writeProtectedVariables.contains(variable);
	}

	public String[] getAllSystemVariables() {
		return allSystemVariables.toArray(new String[] {});
	}
}
