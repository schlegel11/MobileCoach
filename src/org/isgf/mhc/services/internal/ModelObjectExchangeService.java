package org.isgf.mhc.services.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.memory.ExchangeModelObject;
import org.isgf.mhc.model.persistent.MediaObject;
import org.isgf.mhc.services.types.ModelObjectExchangeFormats;

/**
 * @author Andreas Filler
 */
@Log4j2
public class ModelObjectExchangeService {
	private static ModelObjectExchangeService	instance	= null;

	private final DatabaseManagerService		databaseManagerService;
	private final FileStorageManagerService		fileStorageManagerService;

	private ModelObjectExchangeService(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService)
			throws Exception {
		log.info("Starting service...");

		this.databaseManagerService = databaseManagerService;
		this.fileStorageManagerService = fileStorageManagerService;

		log.info("Started.");
	}

	public static ModelObjectExchangeService start(
			final DatabaseManagerService databaseManagerService,
			final FileStorageManagerService fileStorageManagerService)
			throws Exception {
		if (instance == null) {
			instance = new ModelObjectExchangeService(databaseManagerService,
					fileStorageManagerService);
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
	 * Exports the given {@link ModelObject}s to a zip-file
	 * 
	 * @param modelObjects
	 *            The {@link ModelObject}s to export
	 * @param exchangeFormat
	 *            The format of {@link ModelObject}s in this export
	 * @return The zip-file containing the {@link ModelObject}s with meta data
	 *         for import into another system
	 */
	public File exportModelObjects(final List<ModelObject> modelObjects,
			final ModelObjectExchangeFormats exchangeFormat) {
		val exchangeModelObjects = new ArrayList<ExchangeModelObject>();

		log.debug("Exporting model objects...");

		for (val modelObject : modelObjects) {
			log.debug("Exporting model object {}", modelObject);

			final ExchangeModelObject exchangeModelObject = new ExchangeModelObject(
					modelObject.getClass().getSimpleName(), modelObject
							.getClass().getName(), modelObject.getId()
							.toString(), modelObject.toJSONString());

			// Determine if the model object contains also files that need to be
			// exported
			if (modelObject instanceof MediaObject) {
				final MediaObject mediaObject = (MediaObject) modelObject;
				if (mediaObject.getFileReference() != null) {
					exchangeModelObject.setFileReference(mediaObject
							.getFileReference());
					exchangeModelObject
							.setFileReferenceSetMethod("setFileReference");
				}
			}

			// Determine which methods set object ids and store there values
			// separately for adaption after import into other system
			for (val method : modelObject.getClass().getMethods()) {
				if (method.getName().startsWith("set")
						&& method.getParameterTypes().length == 1
						&& method.getParameterTypes()[0].getName().equals(
								ObjectId.class.getName())) {
					Method appropriateGetMethod;
					final String objectIdString;
					try {
						appropriateGetMethod = modelObject.getClass()
								.getMethod(
										method.getName().replaceFirst("set",
												"get"));
						final ObjectId objectId = (ObjectId) appropriateGetMethod
								.invoke(modelObject);

						if (objectId == null) {
							continue;
						}

						objectIdString = objectId.toString();
					} catch (final Exception e) {
						log.error(
								"Could not determine references object id: {}",
								e.getMessage());
						return null;
					}
					log.debug(
							"Method {} contains object id {} in model object {} with object id {}",
							method.getName(), objectIdString, modelObject
									.getClass().getSimpleName(), modelObject
									.getId());
					exchangeModelObject
							.getObjectIdSetMethodsWithAppropriateValues().put(
									method.getName(), objectIdString);
				}
			}

			exchangeModelObjects.add(exchangeModelObject);
		}

		log.debug("Export done.");

		return createZipFile(exchangeModelObjects, exchangeFormat);
	}

	/**
	 * Imports the {@link ModelObject}s in the given zip-file into the system
	 * with new and unique {@link ObjectId}s
	 * 
	 * @param zipFile
	 *            The zip-file to import
	 * @param expectedExchangeFormat
	 *            The format of {@link ModelObject}s expected in this import
	 * @return Imported {@link ModelObject}s
	 * @throws FileNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public List<ModelObject> importModelObjects(final File zipFile,
			final ModelObjectExchangeFormats expectedExchangeFormat)
			throws FileNotFoundException, IOException {
		val modelObjects = new ArrayList<ModelObject>();

		val exchangeModelObjects = readZipFile(new ZipFile(zipFile),
				expectedExchangeFormat);

		log.debug("Importing model objects...");

		// Collect all model objects
		for (val exchangeModelObject : exchangeModelObjects) {
			final ModelObject modelObject = exchangeModelObject
					.getContainedModelObjectWithoutOriginalId();

			// Adjust file reference
			if (exchangeModelObject.getFileReference() != null) {
				try {
					log.debug(
							"Setting new file reference {} to model object {}",
							exchangeModelObject.getFileReference(), modelObject);
					final Method method = modelObject.getClass().getMethod(
							exchangeModelObject.getFileReferenceSetMethod(),
							String.class);
					method.invoke(modelObject,
							exchangeModelObject.getFileReference());
				} catch (final Exception e) {
					log.error(
							"Could not set new file reference {} to model object {}",
							exchangeModelObject.getFileReference(), modelObject);
				}
			}

			modelObjects.add(modelObject);
		}

		// Create all model objects in the database
		for (val modelObject : modelObjects) {
			databaseManagerService.saveModelObject(modelObject);
		}

		// Adjust all relevant id references
		for (val modelObjectToCheckIfIsReferenced : modelObjects) {
			// Collect relevant information
			final ObjectId modelObjectToCheckIfIsReferencedNewObjectId = modelObjectToCheckIfIsReferenced
					.getId();
			final ExchangeModelObject exchangeModelObjectOfModelObjectToCheckIfIsReferenced = exchangeModelObjects
					.get(modelObjects.indexOf(modelObjectToCheckIfIsReferenced));
			final String modelObjectToCheckIfIsReferencedOldObjectId = exchangeModelObjectOfModelObjectToCheckIfIsReferenced
					.getObjectId();

			// Check all exchange model objects for references
			for (val toCheckExchangeModelObject : exchangeModelObjects) {
				for (val methodName : toCheckExchangeModelObject
						.getObjectIdSetMethodsWithAppropriateValues().keySet()) {
					final String oldObjectId = toCheckExchangeModelObject
							.getObjectIdSetMethodsWithAppropriateValues().get(
									methodName);
					if (!oldObjectId.equals("")
							&& oldObjectId
									.equals(modelObjectToCheckIfIsReferencedOldObjectId)) {
						toCheckExchangeModelObject
								.getObjectIdSetMethodsWithAppropriateValues()
								.put(methodName, "");

						// Get appropriate model object
						final ModelObject modelObjectToAdjust = modelObjects
								.get(exchangeModelObjects
										.indexOf(toCheckExchangeModelObject));

						// Find appropriate method to call to set object id
						final Method methodToAdjustObjectIdReference;
						try {
							methodToAdjustObjectIdReference = modelObjectToAdjust
									.getClass().getMethod(methodName,
											ObjectId.class);
						} catch (final Exception e) {
							log.error(
									"Could not find method to adapt reference on object {}: {}",
									toCheckExchangeModelObject
											.getPackageAndClazz(), e
											.getMessage());
							continue;
						}

						// Set object id as reference
						try {
							methodToAdjustObjectIdReference
									.invoke(modelObjectToAdjust,
											modelObjectToCheckIfIsReferencedNewObjectId);
						} catch (final Exception e) {
							log.error(
									"Could not adjust referenced object id on object {}: {}",
									toCheckExchangeModelObject
											.getPackageAndClazz(), e
											.getMessage());
							continue;
						}

						// Save changes
						databaseManagerService
								.saveModelObject(modelObjectToAdjust);
					}
				}
			}
		}

		log.debug("Import done.");

		return modelObjects;
	}

	/**
	 * Creates a {@link File} in zip-format containing the given
	 * {@link ExchangeModelObject}s
	 * 
	 * @param exchangeModelObjects
	 *            The {@link ExchangeModelObject}s to save in the zip-file
	 * @param exchangeFormat
	 *            The format of {@link ModelObject}s in this export
	 * @return The zip-file containing all {@link ExchangeModelObject}s
	 */
	@SneakyThrows
	private File createZipFile(
			final List<ExchangeModelObject> exchangeModelObjects,
			final ModelObjectExchangeFormats exchangeFormat) {

		log.debug("Writing exchange model objects to zip file");

		File zipFile = null;
		try {
			zipFile = File.createTempFile("MHC_", ".mhc", null);
			zipFile.deleteOnExit();
		} catch (final IOException e) {
			log.error("Could not create temp file: {}", e.getMessage());
			return null;
		}
		log.debug("Temporary file {} created", zipFile.getAbsoluteFile());

		@Cleanup
		val fileOutputStream = new FileOutputStream(zipFile);
		@Cleanup
		val zipOutputStream = new ZipOutputStream(fileOutputStream);
		zipOutputStream.setComment(exchangeFormat.toString());

		for (val exchangeModelObject : exchangeModelObjects) {
			// Care for linked files
			if (exchangeModelObject.getFileReference() != null) {
				final String uniqueFileZipName = "F "
						+ exchangeModelObject.getFileReference();
				final File referencedFile = fileStorageManagerService
						.getFileByReference(exchangeModelObject
								.getFileReference());
				log.debug("Adding referenced file {} as {} to zip file",
						referencedFile, uniqueFileZipName);

				final ZipEntry fileZipEntry = new ZipEntry(uniqueFileZipName);

				fileZipEntry.setSize(referencedFile.length());
				zipOutputStream.putNextEntry(fileZipEntry);
				@Cleanup
				val fileInputStream = new FileInputStream(referencedFile);
				IOUtils.copy(fileInputStream, zipOutputStream);
				zipOutputStream.closeEntry();
			}

			// Care for exchange model object itself
			final String uniqueZipName = "M " + exchangeModelObject.getClazz()
					+ " " + exchangeModelObject.getObjectId();
			log.debug("Adding model object {} to zip file", uniqueZipName);
			final ZipEntry zipEntry = new ZipEntry(uniqueZipName);

			final byte[] exchangeModelObjectBytes = exchangeModelObject
					.toJSONString().getBytes("UTF-8");

			zipEntry.setSize(exchangeModelObjectBytes.length);
			zipOutputStream.putNextEntry(zipEntry);
			zipOutputStream.write(exchangeModelObjectBytes);
			zipOutputStream.closeEntry();
		}

		return zipFile;
	}

	/**
	 * Reads {@link ExchangeModelObject}s from {@link ZipFile}
	 * 
	 * @param zipFile
	 *            The file to read from
	 * @param expectedExchangeFormat
	 *            The format of {@link ModelObject}s expected in this import
	 * @return {@link List} of {@link ExchangeModelObject}s in the
	 *         {@link ZipFile}
	 * @throws IOException
	 */
	private List<ExchangeModelObject> readZipFile(final ZipFile zipFile,
			final ModelObjectExchangeFormats expectedExchangeFormat) throws IOException {
		val exchangeModelObjects = new ArrayList<ExchangeModelObject>();

		if (!zipFile.getComment().equals(expectedExchangeFormat.toString())) {
			log.warn("Wrong exchange format provided: "
					+ expectedExchangeFormat + " expected, "
					+ zipFile.getComment() + " received");
			throw new IOException("Wrong exchange format provided: "
					+ expectedExchangeFormat + " expected, "
					+ zipFile.getComment() + " received");
		}

		log.debug("Loading exchange model objects from zip file {}",
				zipFile.getName());

		val oldToNewFileReferencesHashtable = new Hashtable<String, String>();
		for (val zipEntry : Collections.list(zipFile.entries())) {
			log.debug("Reading entry {}", zipEntry.getName());

			if (zipEntry.getName().startsWith("F ")) {
				// Extracting file
				String fileExtension;
				if (zipEntry.getName().lastIndexOf(".") > -1) {
					fileExtension = zipEntry.getName().substring(
							zipEntry.getName().lastIndexOf("."));
				} else {
					fileExtension = ".unknown";
				}

				@Cleanup("delete")
				final File temporaryFile = File.createTempFile("MHC_", ".temp"
						+ fileExtension);
				@Cleanup
				final FileOutputStream fileOutputStream = new FileOutputStream(
						temporaryFile);
				@Cleanup
				final InputStream zipEntryInputStream = zipFile
						.getInputStream(zipEntry);
				IOUtils.copy(zipEntryInputStream, fileOutputStream);

				final String newFileReference = fileStorageManagerService
						.storeFile(temporaryFile);

				final String oldFileReference = zipEntry.getName().split(" ")[1];

				oldToNewFileReferencesHashtable.put(oldFileReference,
						newFileReference);
			} else {
				// Extracting exchange model object
				final byte[] exchangeModelObjectBytes = new byte[(int) zipEntry
						.getSize()];

				@Cleanup
				final InputStream zipEntryInputStream = zipFile
						.getInputStream(zipEntry);
				zipEntryInputStream.read(exchangeModelObjectBytes);

				final ExchangeModelObject exchangeModelObject = ExchangeModelObject
						.fromJSONString(new String(exchangeModelObjectBytes,
								"UTF-8"));

				// Adjusting file reference from old to new
				if (exchangeModelObject.getFileReference() != null) {
					exchangeModelObject
							.setFileReference(oldToNewFileReferencesHashtable
									.get(exchangeModelObject.getFileReference()));
				}

				exchangeModelObjects.add(exchangeModelObject);
			}
		}

		return exchangeModelObjects;
	}
}
