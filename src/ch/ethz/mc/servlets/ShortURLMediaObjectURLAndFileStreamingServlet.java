package ch.ethz.mc.servlets;

/* ##LICENSE## */
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
import ch.ethz.mc.MC;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.MediaObject;
import ch.ethz.mc.model.persistent.MediaObjectParticipantShortURL;
import ch.ethz.mc.services.InterventionAdministrationManagerService;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import ch.ethz.mc.services.internal.ImageCachingService;
import ch.ethz.mc.services.internal.FileStorageManagerService.FILE_STORES;

/**
 * The {@link ShortURLMediaObjectURLAndFileStreamingServlet} serves files
 * contained in {@link MediaObject}s, which are referenced by
 * {@link MediaObjectParticipantShortURL}s
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
@WebServlet(displayName = "Short URL based Media Object File Streaming", urlPatterns = "/"
		+ ImplementationConstants.SHORT_ID_FILE_STREAMING_SERVLET_PATH
		+ "/*", asyncSupported = true, loadOnStartup = 1)
@Log4j2
public class ShortURLMediaObjectURLAndFileStreamingServlet extends HttpServlet {
	private InterventionAdministrationManagerService	interventionAdministrationManagerService;
	private InterventionExecutionManagerService			interventionExecutionManagerService;

	private ImageCachingService							imageCachingService;

	private FileServletWrapper							fileServletWrapper;

	@Override
	public void init() throws ServletException {
		super.init();
		log.info("Initializing servlet...");

		interventionAdministrationManagerService = MC.getInstance()
				.getInterventionAdministrationManagerService();
		interventionExecutionManagerService = MC.getInstance()
				.getInterventionExecutionManagerService();

		imageCachingService = MC.getInstance().getImageCachingService();

		fileServletWrapper = new FileServletWrapper();
		fileServletWrapper.init(getServletContext());

		log.info("Servlet initialized.");
	}

	/**
	 * Handles the request and creates a request that links to a existing file
	 * of an existing {@link MediaObject} or redirects to its URL
	 * 
	 * @param request
	 * @param response
	 * @param headerOnly
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleRequest(final HttpServletRequest request,
			final HttpServletResponse response, boolean headerOnly)
			throws ServletException, IOException {
		// Determine requested system unique id
		MediaObjectParticipantShortURL mediaObjectParticipantShortURL = null;
		int width = 0;
		int height = 0;
		boolean withWatermark = false;
		boolean withCropping = false;
		try {
			final val pathParts = request.getPathInfo().split("/");

			final long shortId = MediaObjectParticipantShortURL
					.validateURLIdPartAndReturnShortId(pathParts[1]);

			mediaObjectParticipantShortURL = interventionExecutionManagerService
					.getMediaObjectParticipantShortURLByShortId(shortId);

			// Additional, optional image parameters
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
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		log.debug("Requested media object short id {}",
				mediaObjectParticipantShortURL);

		// Check if system unique id exists
		if (mediaObjectParticipantShortURL == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		final val mediaObject = interventionAdministrationManagerService
				.getMediaObject(
						mediaObjectParticipantShortURL.getMediaObject());

		// Check if media object exists
		if (mediaObject == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		// Mark media object as seen
		interventionExecutionManagerService.dialogMessageSetMediaContentViewed(
				mediaObjectParticipantShortURL.getDialogMessage());

		// Handle file or URL based media objects
		if (mediaObject.getFileReference() != null) {
			// Retrieve file from media object
			final File file = interventionAdministrationManagerService
					.getFileByReference(mediaObject.getFileReference(),
							FILE_STORES.STORAGE);
			File cachedFile = null;

			// Check if file actually exists in file system
			if (file == null || !file.exists()) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			// Retrieve cached/resized version of image if it is an image and
			// additional resizing parameters are given
			val fileExtension = file.getName()
					.substring(file.getName().lastIndexOf(".")).toLowerCase();
			if (ImplementationConstants.ACCEPTED_IMAGE_FORMATS
					.contains(fileExtension) && width > 0 && height > 0) {
				if (width > ImplementationConstants.IMAGE_MAX_WIDTH
						|| height > ImplementationConstants.IMAGE_MAX_HEIGHT) {
					log.debug(
							"Image is requested in a bigger size than the allowed maximum size");
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}

				try {
					cachedFile = imageCachingService.requestCacheImage(file,
							width, height, withWatermark, withCropping);
				} catch (final Exception e) {
					log.warn("Error at requesting cached image: {}",
							e.getMessage());
					response.sendError(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return;
				}

				// Check if file actually exists in file system
				if (cachedFile == null || !cachedFile.exists()) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
			}

			final File fileToWrap;

			if (cachedFile != null) {
				fileToWrap = cachedFile;
			} else {
				fileToWrap = file;
			}

			log.debug("Serving file {}", fileToWrap.getAbsoluteFile());

			// Wrapping request
			val wrapped = new HttpServletRequestWrapper(request) {
				@Override
				public String getPathInfo() {
					return fileToWrap.getAbsolutePath();
				}
			};

			if (headerOnly) {
				fileServletWrapper.doHead(wrapped, response);
			} else {
				fileServletWrapper.doGet(wrapped, response);
			}
		} else if (mediaObject.getUrlReference() != null) {
			// Send redirect for URL
			response.sendRedirect(mediaObject.getUrlReference());
		} else {
			// Send error (should never occur)
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}

	/**
	 * Process HEAD request. This returns the same headers as GET request, but
	 * without content.
	 * 
	 * @see HttpServlet#doHead(HttpServletRequest, HttpServletResponse).
	 */
	@Override
	protected void doHead(final HttpServletRequest request,
			final HttpServletResponse response)
			throws ServletException, IOException {
		log.debug("Serving short id dynamic {}", request.getPathInfo());

		handleRequest(request, response, true);
	}

	/**
	 * Process GET request.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse).
	 */
	@Override
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response)
			throws ServletException, IOException {
		log.debug("Serving short id dynamic {}", request.getPathInfo());

		handleRequest(request, response, false);
	}

}