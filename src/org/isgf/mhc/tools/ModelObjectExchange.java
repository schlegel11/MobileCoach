package org.isgf.mhc.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

		for (val modelObject : modelObjects) {
			final ExchangeModelObject exchangeModelObject = new ExchangeModelObject(
					modelObject.getClass().getSimpleName(), modelObject
							.getClass().getName(), modelObject.getId()
							.toString(), modelObject.toJSONString());

			// TODO

			exchangeModelObjects.add(exchangeModelObject);
		}

		return createZipFile(exchangeModelObjects);
	}

	/**
	 * Imports the {@link ModelObject}s in the given zip-file into the system
	 * with new and unique {@link ObjectId}s
	 * 
	 * @param zipFile
	 *            The zip-file to import
	 */
	public static void importModelObjects(final File zipFile) {
		try {
			val exchangeModelObjects = readZipFile(new ZipFile(zipFile));
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
