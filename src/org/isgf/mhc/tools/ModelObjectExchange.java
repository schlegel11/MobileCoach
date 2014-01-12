package org.isgf.mhc.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.tools.model.ExchangeModelObject;

@Log4j2
public class ModelObjectExchange {
	public static File exportModelObjects(final List<ModelObject> modelObjects) {
		val exchangeModelObjects = new ArrayList<ExchangeModelObject>();

		for (val modelObject : modelObjects) {
			final ExchangeModelObject exchangeModelObject = new ExchangeModelObject(
					modelObject.getClass().getSimpleName(), modelObject
							.getClass().getName(), modelObject.getId()
							.toString(), modelObject.toJSONString());

			exchangeModelObjects.add(exchangeModelObject);
		}

		return createZipFile(exchangeModelObjects);
	}

	public static void importModelObjects(final File zipFile) {
		try {
			val exchangeModelObjects = readZipFile(zipFile);
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
			val zipEntryName = exchangeModelObject.getClazz() + " "
					+ exchangeModelObject.getObjectId();
			log.debug("Adding model object {} to zip file", zipEntryName);
			final ZipEntry zipEntry = new ZipEntry(zipEntryName);

			final byte[] exchangeModelObjectBytes = exchangeModelObject
					.toJSONString().getBytes("UTF-8");

			zipEntry.setSize(exchangeModelObjectBytes.length);
			zipOutputStream.putNextEntry(zipEntry);
			zipOutputStream.write(exchangeModelObjectBytes);
		}

		return zipFile;
	}

	private static List<ExchangeModelObject> readZipFile(final File zipFile)
			throws FileNotFoundException, IOException {
		val exchangeModelObjects = new ArrayList<ExchangeModelObject>();

		log.debug("Loading exchange model objects from zip file {}",
				zipFile.getAbsoluteFile());

		@Cleanup
		final ZipInputStream zipInputStream = new ZipInputStream(
				new FileInputStream(zipFile));

		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			log.debug("Reading entry {}", zipEntry.getName());
			zipInputStream.closeEntry();
		}

		return null;
	}
}
