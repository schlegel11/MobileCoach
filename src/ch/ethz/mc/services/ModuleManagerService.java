package ch.ethz.mc.services;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.reflections.Reflections;

import ch.ethz.mc.MC;
import ch.ethz.mc.modules.AbstractModule;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.services.internal.VariablesManagerService;

/**
 * Cares for the orchestration of all modules
 *
 * @author Andreas Filler
 */
@Log4j2
public class ModuleManagerService {
	private final Object					$lock;

	private static ModuleManagerService		instance	= null;

	private final DatabaseManagerService	databaseManagerService;
	@Getter
	private final FileStorageManagerService	fileStorageManagerService;
	private final VariablesManagerService	variablesManagerService;

	private final List<AbstractModule>		modules;

	private ModuleManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		$lock = MC.getInstance();

		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;
		this.variablesManagerService = variablesManagerService;

		modules = new ArrayList<AbstractModule>();

		log.info("Finding and starting modules...");
		final Reflections reflections = new Reflections(AbstractModule.class
				.getPackage().getName());
		final Set<Class<? extends AbstractModule>> moduleClasses = reflections
				.getSubTypesOf(AbstractModule.class);
		val moduleClassesIterator = moduleClasses.iterator();

		while (moduleClassesIterator.hasNext()) {
			val moduleClass = moduleClassesIterator.next();
			log.info("Initializing module {}", moduleClass.getName());
			val module = moduleClass.getConstructor(ModuleManagerService.class)
					.newInstance(this);

			modules.add(module);
		}

		log.info("Started.");
	}

	public static ModuleManagerService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		if (instance == null) {
			instance = new ModuleManagerService(databaseManagerService,
					fileStorageManagerService, variablesManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		for (val module : modules) {
			module.stop();
		}

		log.info("Stopped.");
	}
}
