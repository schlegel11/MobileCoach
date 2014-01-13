package org.isgf.mhc.services;

import java.io.File;
import java.io.IOException;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang.RandomStringUtils;
import org.isgf.mhc.Constants;

import com.google.gwt.thirdparty.guava.common.io.Files;

@Log4j2
public class FileStorageManagerService {
	private static FileStorageManagerService	instance	= null;

	private final File							storageFolder;

	private FileStorageManagerService() throws Exception {
		log.info("Starting service...");

		this.storageFolder = new File(Constants.STORAGE_FOLDER);
		this.storageFolder.mkdirs();

		// TODO Z: IDEA: Could test for existing files here and remove files
		// with no model object reference

		log.info("Started.");
	}

	public static FileStorageManagerService start() throws Exception {
		if (instance == null) {
			instance = new FileStorageManagerService();
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	/**
	 * Returns the {@link File} fitting to the given file reference
	 * 
	 * @param fileReference
	 *            The reference of the file to retrieve
	 * @return The required file or <code>null</code> if an error occurred
	 */
	public File getFile(final String fileReference) {
		log.debug("Returning file with reference {}", fileReference);
		final String[] fileReferenceParts = fileReference.split("/");
		final File folder = new File(this.storageFolder, fileReferenceParts[0]);
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

		String folderName = RandomStringUtils.randomAlphabetic(40);
		File folder = new File(this.storageFolder, folderName);
		while (folder.exists()) {
			folderName = RandomStringUtils.randomAlphabetic(40);
			folder = new File(this.storageFolder, folderName);
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
}
