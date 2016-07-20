package ch.ethz.mc.services;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.MC;
import ch.ethz.mc.model.rest.VariableWithValue;
import ch.ethz.mc.services.internal.VariablesManagerService;

/**
 * Cares for the orchestration of all REST calls
 *
 * @author Andreas Filler
 */
@Log4j2
public class RESTManagerService {
	private final Object					$lock;

	private static RESTManagerService		instance	= null;

	private final VariablesManagerService	variablesManagerService;

	private RESTManagerService(
			final VariablesManagerService variablesManagerService)
			throws Exception {
		$lock = MC.getInstance();

		log.info("Starting service...");

		this.variablesManagerService = variablesManagerService;

		log.info("Started.");
	}

	public static RESTManagerService start(
			final VariablesManagerService variablesManagerService)
			throws Exception {
		if (instance == null) {
			instance = new RESTManagerService(variablesManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	@Synchronized
	public VariableWithValue readVariable(final String variable) {
		// final variablesManagerService.get
		return null;
	}

	public boolean validateToken(final String token) {
		// TODO Auto-generated method stub
		return false;
	}

}
