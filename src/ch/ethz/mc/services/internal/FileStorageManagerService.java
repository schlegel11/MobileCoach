package ch.ethz.mc.services.internal;

/* ##LICENSE## */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.MediaObject;
import ch.ethz.mc.model.persistent.ParticipantVariableWithValue;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileStorageManagerService {
	public enum FILE_STORES {
		STORAGE, MEDIA_UPLOAD;
	}

	private static FileStorageManagerService	instance	= null;

	private final File							storageFolder;

	@Getter
	private final File							mediaUploadFolder;

	@Getter
	private final File							mediaCacheFolder;

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

		log.info("Using media upload folder {}",
				Constants.getMediaUploadFolder());
		mediaUploadFolder = new File(Constants.getMediaUploadFolder());
		mediaUploadFolder.mkdirs();
		if (!mediaUploadFolder.exists()) {
			throw new FileNotFoundException();
		}

		log.info("Using media cache folder {}",
				Constants.getMediaCacheFolder());
		mediaCacheFolder = new File(Constants.getMediaCacheFolder());
		mediaCacheFolder.mkdirs();
		if (!mediaCacheFolder.exists()) {
			throw new FileNotFoundException();
		}

		log.info("Using templates folder {}", Constants.getTemplatesFolder());
		templatesFolder = new File(Constants.getTemplatesFolder());
		templatesFolder.mkdirs();
		if (!templatesFolder.exists()) {
			throw new FileNotFoundException();
		}

		// Checking for file file storage consistency in both ways:
		// a) Check if all required files exist
		// a) Delete unused files
		HashSet<String> requiredFileReferences = new HashSet<String>();

		log.info("Checking media objects and storage folder for consistency:");
		val mediaObjects = databaseManagerService
				.findModelObjects(MediaObject.class, Queries.ALL);

		for (val mediaObject : mediaObjects) {
			if (mediaObject.getFileReference() != null) {
				final String fileReference = mediaObject.getFileReference();
				requiredFileReferences.add(fileReference.split("/")[0]);
				if (getFileByReference(fileReference,
						FILE_STORES.STORAGE) == null) {
					log.warn(
							"Media object {} contains missing file reference {}",
							mediaObject.getId(),
							mediaObject.getFileReference());
				}
			}
		}

		for (val file : storageFolder.listFiles()) {
			if (file.isDirectory()
					&& file.getName().startsWith(
							ImplementationConstants.FILE_STORAGE_PREFIX)
					&& !requiredFileReferences.contains(file.getName())) {
				log.debug("Deleting unused resource {}",
						file.getAbsolutePath());
				for (val nestedFile : file.listFiles()) {
					nestedFile.delete();
				}
				file.delete();
			}
		}

		// Checking for media upload file consistency in both ways:
		// a) Check if all required files exist
		// a) Delete unused files
		requiredFileReferences = new HashSet<String>();

		log.info("Checking variables and media upload folder for consistency:");
		val mediaUploadVariables = databaseManagerService.findModelObjects(
				ParticipantVariableWithValue.class,
				Queries.PARTICIPANT_VARIABLE_WITH_VALUE__BY_DESCRIBES_MEDIA_UPLOAD_OR_FORMER_VALUE_DESCRIBES_MEDIA_UPLOAD,
				true, true);

		for (val mediaUploadVariable : mediaUploadVariables) {
			if (mediaUploadVariable.isDescribesMediaUpload()
					&& mediaUploadVariable.getValue() != null) {
				final String fileReference = mediaUploadVariable.getValue();
				requiredFileReferences.add(fileReference.split("-")[0]);
				if (getFileByReference(fileReference,
						FILE_STORES.MEDIA_UPLOAD) == null) {
					log.warn(
							"Media upload variable {} contains missing file reference {}",
							mediaUploadVariable.getId(),
							mediaUploadVariable.getValue());
				}
			}

			for (val formerValue : mediaUploadVariable
					.getFormerVariableValues()) {
				if (formerValue.isDescribesMediaUpload()
						&& formerValue.getValue() != null) {
					final String fileReference = formerValue.getValue();
					requiredFileReferences.add(fileReference.split("-")[0]);
					if (getFileByReference(fileReference,
							FILE_STORES.MEDIA_UPLOAD) == null) {
						log.warn(
								"Media upload variable {} contains missing file reference {}",
								mediaUploadVariable.getId(),
								formerValue.getValue());
					}
				}
			}
		}

		for (val file : mediaUploadFolder.listFiles()) {
			if (file.isDirectory()
					&& file.getName().startsWith(
							ImplementationConstants.FILE_STORAGE_PREFIX)
					&& !requiredFileReferences.contains(file.getName())) {
				log.debug("Deleting unused resource {}",
						file.getAbsolutePath());
				for (val nestedFile : file.listFiles()) {
					nestedFile.delete();
				}
				file.delete();
			}
		}

		log.info("Check done.");

		log.info("Clearing media cache...");
		for (val file : mediaCacheFolder.listFiles()) {
			if (file.isFile() && file.getName()
					.startsWith(ImplementationConstants.FILE_STORAGE_PREFIX)) {
				log.debug("Deleting cache file {}", file.getAbsolutePath());
				file.delete();
			}
		}

		log.info("Clearing done.");

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
	 * @param fileStore
	 *            The file store for the file
	 * @return The required file or <code>null</code> if an error occurred
	 */
	public File getFileByReference(final String fileReference,
			final FILE_STORES fileStore) {

		File responsibleFolder = null;
		String[] fileReferenceParts = null;
		switch (fileStore) {
			case MEDIA_UPLOAD:
				responsibleFolder = mediaUploadFolder;
				fileReferenceParts = fileReference.split("-");
				break;
			case STORAGE:
				responsibleFolder = storageFolder;
				fileReferenceParts = fileReference.split("/");
				break;
		}

		log.debug("Checking for file with reference {}", fileReference);

		if (fileReferenceParts.length != 2) {
			log.warn(
					"Preventing security lack by not accepting different file names as regularly expected");
			return null;
		}

		final File folder = new File(responsibleFolder, fileReferenceParts[0]);
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
	 * @param fileStore
	 *            The file store for the file
	 * @return The file reference or <code>null</code> if an error occurred
	 */
	public String storeFile(final File file, final FILE_STORES fileStore) {
		log.debug("Storing file {}", file.getAbsoluteFile());

		File responsibleFolder = null;
		switch (fileStore) {
			case MEDIA_UPLOAD:
				responsibleFolder = mediaUploadFolder;
				break;
			case STORAGE:
				responsibleFolder = storageFolder;
				break;
		}

		String fileName;
		if (file.getName().lastIndexOf(".") > -1) {
			fileName = "file"
					+ file.getName().substring(file.getName().lastIndexOf("."));
		} else {
			fileName = "file.unknown";
		}

		String folderName;
		File folder;
		synchronized (this) {
			folderName = ImplementationConstants.FILE_STORAGE_PREFIX
					+ RandomStringUtils.randomAlphabetic(40);
			folder = new File(responsibleFolder, folderName);
			while (folder.exists()) {
				folderName = ImplementationConstants.FILE_STORAGE_PREFIX
						+ RandomStringUtils.randomAlphabetic(40);
				folder = new File(responsibleFolder, folderName);
			}
			folder.mkdirs();
		}

		final File destinationFile = new File(folder, fileName);
		try {
			FileUtils.copyFile(file, destinationFile);
		} catch (final Exception e) {
			log.warn("File {} could not be copied to folder {}: {}",
					file.getAbsoluteFile(), destinationFile.getAbsoluteFile(),
					e.getMessage());
			folder.delete();
			return null;
		}

		final String fileReference = folderName + "/" + fileName;

		log.debug("File copied to {} and created reference {}", destinationFile,
				fileReference);

		return fileReference;
	}

	/**
	 * Deletes a file with the given file reference
	 *
	 * @param fileReference
	 *            The file reference to delete
	 * @param fileStore
	 *            The file store for the file
	 */
	public void deleteFile(final String fileReference,
			final FILE_STORES fileStore) {
		final File fileToDelete = getFileByReference(fileReference, fileStore);

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
