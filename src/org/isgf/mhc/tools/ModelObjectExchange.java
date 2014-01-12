package org.isgf.mhc.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.tools.model.ExchangeModelObject;

@Log4j2
public class ModelObjectExchange {
	/**
	 * Exports the given {@link ModelObject}s to a zip-file
	 * 
	 * @param modelObjects
	 *            The {@link ModelObject}s to export
	 * @return The zip-file containing the {@link ModelObject}s with meta data
	 *         for import into another system
	 */
	public static File exportModelObjects(final List<ModelObject> modelObjects) {
		val exchangeModelObjects = new ArrayList<ExchangeModelObject>();

		log.debug("Exporting model objects...");

		for (val modelObject : modelObjects) {
			log.debug("Exporting model object {}", modelObject);

			final ExchangeModelObject exchangeModelObject = new ExchangeModelObject(
					modelObject.getClass().getSimpleName(), modelObject
							.getClass().getName(), modelObject.getId()
							.toString(), modelObject.toJSONString());

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

		return createZipFile(exchangeModelObjects);
	}

	/**
	 * Imports the {@link ModelObject}s in the given zip-file into the system
	 * with new and unique {@link ObjectId}s
	 * 
	 * @param zipFile
	 *            The zip-file to import
	 * @return Imported {@link ModelObject}s
	 * @throws FileNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static List<ModelObject> importModelObjects(final File zipFile)
			throws FileNotFoundException, IOException {
		val modelObjects = new ArrayList<ModelObject>();

		val exchangeModelObjects = readZipFile(new ZipFile(zipFile));

		log.debug("Importing model objects...");

		// Collect all model objects
		for (val exchangeModelObject : exchangeModelObjects) {
			final ModelObject modelObject = exchangeModelObject
					.getContainedModelObjectWithoutOriginalId();

			modelObjects.add(modelObject);
		}

		// Create all model objects in the database
		for (val modelObject : modelObjects) {
			modelObject.save();
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
				// for (val methodAndObjectIdEntrySet :
				// toCheckExchangeModelObject
				// .getObjectIdSetMethodsWithAppropriateValues().keys()) {
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
						modelObjectToAdjust.save();
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
	 * @return The zip-file containing all {@link ExchangeModelObject}s
	 */
	@SneakyThrows
	private static File createZipFile(
			final List<ExchangeModelObject> exchangeModelObjects) {

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
		final ZipOutputStream zipOutputStream = new ZipOutputStream(
				new FileOutputStream(zipFile));

		for (final val exchangeModelObject : exchangeModelObjects) {
			final String uniqueZipName = exchangeModelObject.getClazz() + " "
					+ exchangeModelObject.getObjectId();
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
	 * @return {@link List} of {@link ExchangeModelObject}s in the
	 *         {@link ZipFile}
	 * @throws IOException
	 */
	private static List<ExchangeModelObject> readZipFile(final ZipFile zipFile)
			throws IOException {
		val exchangeModelObjects = new ArrayList<ExchangeModelObject>();

		log.debug("Loading exchange model objects from zip file {}",
				zipFile.getName());

		for (val zipEntry : Collections.list(zipFile.entries())) {
			log.debug("Reading entry {}", zipEntry.getName());

			final byte[] exchangeModelObjectBytes = new byte[(int) zipEntry
					.getSize()];

			@Cleanup
			final InputStream zipEntryInputStream = zipFile
					.getInputStream(zipEntry);
			zipEntryInputStream.read(exchangeModelObjectBytes);

			final ExchangeModelObject exchangeModelObject = ExchangeModelObject
					.fromJSONString(new String(exchangeModelObjectBytes,
							"UTF-8"));
			exchangeModelObjects.add(exchangeModelObject);
		}

		return exchangeModelObjects;
	}
}
