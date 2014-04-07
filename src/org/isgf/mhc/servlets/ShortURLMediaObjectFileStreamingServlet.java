package org.isgf.mhc.servlets;

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

import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.persistent.MediaObject;
import org.isgf.mhc.model.persistent.MediaObjectParticipantShortURL;
import org.isgf.mhc.services.InterventionAdministrationManagerService;
import org.isgf.mhc.services.InterventionExecutionManagerService;

/**
 * The {@link ShortURLMediaObjectFileStreamingServlet} serves files contained in
 * {@link MediaObject}s, which are referenced by
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
@WebServlet(displayName = "Short URL based Media Object File Streaming", value = "/"
		+ ImplementationContants.SHORT_ID_FILE_STREAMING_SERVLET_PATH + "/*", asyncSupported = true, loadOnStartup = 1)
@Log4j2
public class ShortURLMediaObjectFileStreamingServlet extends HttpServlet {
	private InterventionAdministrationManagerService	interventionAdministrationManagerService;
	private InterventionExecutionManagerService			interventionExecutionManagerService;

	private FileServletWrapper							fileServletWrapper;

	@Override
	public void init() throws ServletException {
		super.init();
		log.info("Initializing servlet...");

		interventionAdministrationManagerService = MHC.getInstance()
				.getInterventionAdministrationManagerService();
		interventionExecutionManagerService = MHC.getInstance()
				.getInterventionExecutionManagerService();

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
		// Determine requested system unique id
		MediaObjectParticipantShortURL mediaObjectParticipantShortURL = null;
		try {
			final val pathParts = request.getPathInfo().split("/");

			final long shortId = MediaObjectParticipantShortURL
					.validateURLIdPartAndReturnShortId(pathParts[1]);

			mediaObjectParticipantShortURL = interventionExecutionManagerService
					.getMediaObjectParticipantShortURLByShortId(shortId);
		} catch (final Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
		log.debug("Requested system unique id {}",
				mediaObjectParticipantShortURL);

		// Check if system unique id exists
		if (mediaObjectParticipantShortURL == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		final val mediaObject = interventionAdministrationManagerService
				.getMediaObject(mediaObjectParticipantShortURL.getMediaObject());

		// Check if media object exists
		if (mediaObject == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		// Mark media object as seen
		interventionExecutionManagerService
				.dialogMessageSetMediaContentViewed(mediaObjectParticipantShortURL
						.getDialogMessage());

		// Retrieve file from media object
		final val file = interventionAdministrationManagerService
				.getFileByReference(mediaObject.getFileReference());
		log.debug("Serving file {}", file.getAbsoluteFile());

		// Check if file actually exists in filesystem.
		if (!file.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		// Wrapping request
		final HttpServletRequest wrapped = new HttpServletRequestWrapper(
				request) {
			@Override
			public String getPathInfo() {
				return file.getAbsolutePath();
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
		log.debug("Serving short id dynamic {}", request.getPathInfo());

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
		log.debug("Serving short id dynamic {}", request.getPathInfo());

		val wrapped = createWrappedReqest(request, response);

		if (wrapped == null) {
			return;
		}

		fileServletWrapper.doGet(wrapped, response);
	}

}