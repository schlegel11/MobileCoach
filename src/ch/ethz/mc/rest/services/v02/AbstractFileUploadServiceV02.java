package ch.ethz.mc.rest.services.v02;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import lombok.Cleanup;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.ImplementationConstants.ACCEPTED_MEDIA_UPLOAD_TYPES;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService.FILE_STORES;

/**
 * Allows the upload of files to the system
 *
 * @author Andreas Filler
 */
@Log4j2
public abstract class AbstractFileUploadServiceV02 extends AbstractServiceV02 {
	private final FileStorageManagerService fileStorageManagerService;

	public AbstractFileUploadServiceV02(
			final RESTManagerService restManagerService) {
		super(restManagerService);
		fileStorageManagerService = restManagerService
				.getFileStorageManagerService();
	}

	/**
	 * Handle upload of file to a temporary file
	 *
	 * @param input
	 * @param mediaType
	 * @return
	 * @throws Exception
	 */
	protected File handleUpload(final MultipartFormDataInput input,
			final ACCEPTED_MEDIA_UPLOAD_TYPES mediaType) throws Exception {

		log.debug("Handling upload of {}...", mediaType);

		// Handle upload
		File file;
		try {
			file = formDataToFile(input, mediaType);
		} catch (final Exception e) {
			log.warn("Upload error: {}", e.getMessage());

			throw new WebApplicationException(Response.serverError()
					.entity("Upload error: " + e.getMessage()).build());
		}

		return file;
	}

	/**
	 * Handles the persistent storing of an uploaded temporary file and returns
	 * the created file reference
	 *
	 * @param temporaryFile
	 * @return
	 */
	protected String handleStoring(final File temporaryFile) {
		return fileStorageManagerService.storeFile(temporaryFile,
				FILE_STORES.MEDIA_UPLOAD);
	}

	private File formDataToFile(final MultipartFormDataInput input,
			final ACCEPTED_MEDIA_UPLOAD_TYPES mediaType) throws Exception {
		File file = null;

		final Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

		if (uploadForm.get("file") == null) {
			log.warn("No field of name 'file' found in upload.");

			throw new IOException("No field of name 'file' found in upload.");
		}

		final List<InputPart> inputParts = uploadForm.get("file");

		for (final InputPart inputPart : inputParts) {
			try {
				final MultivaluedMap<String, String> header = inputPart
						.getHeaders();
				val filename = getFileName(header);

				// Convert the uploaded file to an InputStream
				@Cleanup
				val inputStream = inputPart.getBody(InputStream.class, null);

				// Clean file name and check file type
				String temporaryFileExtension = null;
				val temporaryFileName = filename.replaceAll("[^A-Za-z0-9_. ]+",
						"_");
				if (filename.lastIndexOf(".") > -1) {
					temporaryFileExtension = filename
							.substring(filename.lastIndexOf(".")).toLowerCase();
				} else {
					throw new Exception("File has no file extension");
				}

				switch (mediaType) {
					case IMAGE:
						if (!ImplementationConstants.ACCEPTED_IMAGE_FORMATS
								.contains(temporaryFileExtension)) {
							throw new Exception("File type is not supported");
						}
						break;
				}

				File temporaryFile = null;
				try {
					temporaryFile = File.createTempFile(temporaryFileName,
							temporaryFileExtension);
					log.debug("Using temporary file {} for upload",
							temporaryFile.getAbsolutePath());
				} catch (final IOException e) {
					log.error("Could not create temporary file: {}",
							e.getMessage());
					throw new Exception("File cannot be uploaded");
				}

				// Write contents to File
				file = writeFile(inputStream, temporaryFile);
			} catch (final IOException e) {
				log.warn("Writing file error: {}", e.getMessage());

				throw e;
			}
		}

		if (file == null) {
			log.warn("No file content found in upload.");

			throw new IOException("No file content found in upload.");
		} else {
			log.debug("Upload successful.");
		}

		return file;
	}

	private String getFileName(final MultivaluedMap<String, String> header) {
		final String[] contentDisposition = header
				.getFirst("Content-Disposition").split(";");

		for (final String filename : contentDisposition) {
			if (filename.trim().startsWith("filename")) {

				final String[] name = filename.split("=");

				final String finalFileName = name[1].trim().replaceAll("\"",
						"");
				return finalFileName;
			}
		}
		return "unknown";
	}

	private File writeFile(final InputStream inputStream, final File file)
			throws IOException {

		if (!file.exists()) {
			file.createNewFile();
		}

		log.debug("Writing content to file {}...", file.getAbsolutePath());

		@Cleanup
		val fileOutputStream = new FileOutputStream(file);

		IOUtils.copy(inputStream, fileOutputStream);

		return file;
	}
}
