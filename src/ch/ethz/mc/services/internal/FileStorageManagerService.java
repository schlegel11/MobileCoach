package ch.ethz.mc.services.internal;

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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.RandomStringUtils;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.MediaObject;

import com.google.gwt.thirdparty.guava.common.io.Files;

@Log4j2
public class FileStorageManagerService {
	private static FileStorageManagerService	instance	= null;

	private final File							storageFolder;

	@Getter
	private final File							templatesFolder;

	private FileStorageManagerService(
			final DatabaseManagerService databaseManagerService)
					throws Exception {
		log.info("Starting service...");

		log.info("Using storage folder {}", Constants.getStorageFolder());
		storageFolder = new File(Constants.getStorageFolder());
		storageFolder.mkdirs();
		if (!storageFolder.exists()) {
			throw new FileNotFoundException();
		}

		log.info("Using templates folder {}", Constants.getTemplatesFolder());
		templatesFolder = new File(Constants.getTemplatesFolder());
		templatesFolder.mkdirs();
		if (!templatesFolder.exists()) {
			throw new FileNotFoundException();
		}

		// Checking for file consistency in both ways:
		// a) Check if all required files exist
		// a) Delete unused files
		val requiredFileRefernces = new HashSet<String>();

		log.info("Checking media objects and storage folder for consistency:");
		val mediaObjects = databaseManagerService.findModelObjects(
				MediaObject.class, Queries.ALL);

		for (val mediaObject : mediaObjects) {
			if (mediaObject.getFileReference() != null) {
				final String fileReference = mediaObject.getFileReference();
				requiredFileRefernces.add(fileReference.split("/")[0]);
				if (getFileByReference(fileReference) == null) {
					log.warn(
							"Media object {} contains missing file reference {}",
							mediaObject.getId(), mediaObject.getFileReference());
				}
			}
		}

		for (val file : storageFolder.listFiles()) {
			if (file.isDirectory() && file.getName().startsWith("MC_")
					&& !requiredFileRefernces.contains(file.getName())) {
				log.debug("Deleting unused resource {}", file.getAbsolutePath());
				for (val nestedFile : file.listFiles()) {
					nestedFile.delete();
				}
				file.delete();
			}
		}

		log.info("Check done.");

		// Give this instance to model object
		ModelObject.configure(this);

		log.info("Started.");
	}

	public static FileStorageManagerService start(
			final DatabaseManagerService databaseManagerService)
					throws Exception {
		if (instance == null) {
			instance = new FileStorageManagerService(databaseManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	/*
	 * Class methods
	 */

	/**
	 * Returns the {@link File} fitting to the given file reference
	 *
	 * @param fileReference
	 *            The reference of the file to retrieve
	 * @return The required file or <code>null</code> if an error occurred
	 */
	public File getFileByReference(final String fileReference) {
		log.debug("Returning file with reference {}", fileReference);
		final String[] fileReferenceParts = fileReference.split("/");
		final File folder = new File(storageFolder, fileReferenceParts[0]);
		final File file = new File(folder, fileReferenceParts[1]);

		if (!file.exists()) {
			log.warn("File {} not found", file.getAbsoluteFile());
			return null;
		}

		log.debug("Returning file {} for reference {}", file.getAbsoluteFile(),
				fileReference);

		return file;
	}

	/**
	 * Stores a {@link File} and returns the generated file reference
	 *
	 * @param file
	 *            The file to store
	 * @return The file reference or <code>null</code> if an error occurred
	 */
	public String storeFile(final File file) {
		log.debug("Storing file {}", file.getAbsoluteFile());

		String fileName;
		if (file.getName().lastIndexOf(".") > -1) {
			fileName = "file"
					+ file.getName().substring(file.getName().lastIndexOf("."));
		} else {
			fileName = "file.unknown";
		}

		String folderName = "MC_" + RandomStringUtils.randomAlphabetic(40);
		File folder = new File(storageFolder, folderName);
		while (folder.exists()) {
			folderName = "MC_" + RandomStringUtils.randomAlphabetic(40);
			folder = new File(storageFolder, folderName);
		}
		folder.mkdirs();

		final File destinationFile = new File(folder, fileName);
		try {
			Files.copy(file, destinationFile);
		} catch (final IOException e) {
			log.warn("File {} could not be copied to folder {}: {}",
					file.getAbsoluteFile(), destinationFile.getAbsoluteFile(),
					e.getMessage());
			folder.delete();
			return null;
		}

		final String fileReference = folderName + "/" + fileName;

		log.debug("File copied to {} and created reference {}",
				destinationFile, fileReference);

		return fileReference;
	}

	public void deleteFile(final String fileReference) {
		final File fileToDelete = getFileByReference(fileReference);

		if (fileToDelete != null && fileToDelete.exists()) {
			final File folderToDelete = fileToDelete.getParentFile();
			fileToDelete.delete();
			folderToDelete.delete();
			log.debug("File deleted");
		} else {
			log.warn("File not found for deletion");
		}
	}
}
