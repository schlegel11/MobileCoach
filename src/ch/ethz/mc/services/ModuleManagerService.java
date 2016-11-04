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
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import lombok.Getter;
import lombok.Synchronized;
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
	private final Object								$lock;

	private static ModuleManagerService					instance	= null;

	@Getter
	private final DatabaseManagerService				databaseManagerService;
	@Getter
	private final FileStorageManagerService				fileStorageManagerService;
	@Getter
	private final VariablesManagerService				variablesManagerService;

	private final Dictionary<String, AbstractModule>	modules;

	// Adapters of modules

	private ModuleManagerService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService,
			final VariablesManagerService variablesManagerService)
			throws Exception {
		$lock = MC.getInstance();

		log.info("Starting service...");

		synchronized ($lock) {
			this.databaseManagerService = databaseManagerService;
			this.fileStorageManagerService = fileStorageManagerService;
			this.variablesManagerService = variablesManagerService;

			modules = new Hashtable<String, AbstractModule>();

			log.info("Finding and starting modules...");
			final Reflections reflections = new Reflections(
					AbstractModule.class.getPackage().getName());
			final Set<Class<? extends AbstractModule>> moduleClasses = reflections
					.getSubTypesOf(AbstractModule.class);

			val moduleClassesIterator = moduleClasses.iterator();
			while (moduleClassesIterator.hasNext()) {
				val moduleClass = moduleClassesIterator.next();
				log.info("Initializing module {}", moduleClass.getName());
				val module = moduleClass.getConstructor(
						ModuleManagerService.class).newInstance(this);

				modules.put(module.getKey(), module);
			}
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

	@Synchronized
	public void stop() throws Exception {
		log.info("Stopping service...");

		val modulesIterator = modules.elements();
		while (modulesIterator.hasMoreElements()) {
			val module = modulesIterator.nextElement();
			module.stop();
		}

		log.info("Stopped.");
	}

	/*
	 * Module access methods
	 */
	/**
	 * Get all modules
	 *
	 * @return
	 */
	public Enumeration<AbstractModule> getAllModules() {
		return modules.elements();
	}

	/**
	 * Get module by key
	 *
	 * @param key
	 * @return
	 */
	public AbstractModule getModuleByKey(final String key) {
		return modules.get(key);
	}

	/**
	 * Check if a specific module exists
	 *
	 * @param key
	 * @return
	 */
	public boolean moduleExists(final String key) {
		return modules.get(key) != null;
	}

	/*
	 * Adapter Management
	 */
}
