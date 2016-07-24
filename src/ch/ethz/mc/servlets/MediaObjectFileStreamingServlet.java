package ch.ethz.mc.servlets;

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
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import lombok.val;
import lombok.extern.log4j.Log4j2;
import net.balusc.webapp.FileServletWrapper;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.MediaObject;
import ch.ethz.mc.services.InterventionAdministrationManagerService;
import ch.ethz.mc.services.internal.FileStorageManagerService.FILE_STORES;
import ch.ethz.mc.services.internal.ImageCachingService;

/**
 * The {@link MediaObjectFileStreamingServlet} serves files contained in
 * {@link MediaObject}s or uploaded media objects.
 *
 * The library used for serving the files is published under the LGPL license.
 * Therefore all modifications and extensions on these files are published as
 * well in the file "FileServletWrapper.jar" in the WEB-INF/lib folder. The jar
 * file also contains all sources, which are again released under the LGPL
 * license.
 *
 * If you ever plan to strictly use this software ONLY under the Apache license,
 * you need to reimplement this class without the dependencies on your own.
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Media Object File Streaming", urlPatterns = "/"
		+ ImplementationConstants.FILE_STREAMING_SERVLET_PATH + "/*", asyncSupported = true, loadOnStartup = 1)
@Log4j2
public class MediaObjectFileStreamingServlet extends HttpServlet {
	private InterventionAdministrationManagerService	interventionAdministrationManagerService;

	private ImageCachingService							imageCachingService;

	private FileServletWrapper							fileServletWrapper;

	@Override
	public void init() throws ServletException {
		super.init();
		log.info("Initializing servlet...");

		interventionAdministrationManagerService = MC.getInstance()
				.getInterventionAdministrationManagerService();

		imageCachingService = MC.getInstance().getImageCachingService();

		fileServletWrapper = new FileServletWrapper();
		fileServletWrapper.init(getServletContext());

		log.info("Servlet initialized.");
	}

	/**
	 * Create a request that links to a existing file of an existing
	 * {@link MediaObject}
	 *
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private HttpServletRequest createWrappedReqest(
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException {

		// Determine request type
		String requestedElement;
		int width = 0;
		int height = 0;
		boolean withWatermark = false;
		boolean withCropping = false;
		try {
			val pathParts = request.getPathInfo().split("/");
			requestedElement = pathParts[1];

			if (pathParts.length >= 4) {
				width = Integer.parseInt(pathParts[2]);
				height = Integer.parseInt(pathParts[3]);
			}
			if (pathParts.length >= 5) {
				withWatermark = Boolean.parseBoolean(pathParts[4]);
			}
			if (pathParts.length >= 6) {
				withCropping = Boolean.parseBoolean(pathParts[5]);
			}
		} catch (final Exception e) {
			log.debug("Error at parsing path parts: {}", e.getMessage());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}

		File file = null;
		if (requestedElement
				.startsWith(ImplementationConstants.FILE_STORAGE_PREFIX)) {
			log.debug("Uploaded media object request");

			// Retrieve media file
			file = interventionAdministrationManagerService.getFileByReference(
					requestedElement, FILE_STORES.MEDIA_UPLOAD);

			// Retrieve cached/resized version of image if an image is requested
			val fileExtension = file.getName()
					.substring(file.getName().lastIndexOf(".")).toLowerCase();
			if (ImplementationConstants.ACCEPTED_IMAGE_FORMATS
					.contains(fileExtension) && width > 0 && height > 0) {
				if (width > ImplementationConstants.IMAGE_MAX_WIDTH
						|| height > ImplementationConstants.IMAGE_MAX_HEIGHT) {
					log.debug("Image is requested in a bigger size than the allowed maximum size");
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return null;
				}

				try {
					file = imageCachingService.requestCacheImage(file, width,
							height, withWatermark, withCropping);
				} catch (final Exception e) {
					log.warn("Error at requesting cached image: {}",
							e.getMessage());
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return null;
				}
			}
		} else {
			// Determine requested media object
			ObjectId mediaObjectId = null;
			try {
				mediaObjectId = new ObjectId(requestedElement);
			} catch (final Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return null;
			}
			log.debug("Requested media object {}", mediaObjectId);

			final val mediaObject = interventionAdministrationManagerService
					.getMediaObject(mediaObjectId);

			// Check if media object exists and contains a file
			if (mediaObject == null || mediaObject.getFileReference() == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return null;
			}

			// Retrieve file from media object
			file = interventionAdministrationManagerService.getFileByReference(
					mediaObject.getFileReference(), FILE_STORES.STORAGE);
		}

		// Check if file actually exists in file system.
		if (file == null || !file.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		log.debug("Serving file {}", file.getAbsoluteFile());

		val fileToServe = file;
		// Wrapping request
		final HttpServletRequest wrapped = new HttpServletRequestWrapper(
				request) {
			@Override
			public String getPathInfo() {
				return fileToServe.getAbsolutePath();
			}
		};

		return wrapped;
	}

	/**
	 * Process HEAD request. This returns the same headers as GET request, but
	 * without content.
	 *
	 * @see HttpServlet#doHead(HttpServletRequest, HttpServletResponse).
	 */
	@Override
	protected void doHead(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		log.debug("Serving dynamic {}", request.getPathInfo());

		val wrapped = createWrappedReqest(request, response);

		if (wrapped == null) {
			return;
		}

		fileServletWrapper.doHead(wrapped, response);
	}

	/**
	 * Process GET request.
	 *
	 * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse).
	 */
	@Override
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		log.debug("Serving dynamic {}", request.getPathInfo());

		val wrapped = createWrappedReqest(request, response);

		if (wrapped == null) {
			return;
		}

		fileServletWrapper.doGet(wrapped, response);
	}

}