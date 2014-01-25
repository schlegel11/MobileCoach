package org.isgf.mhc.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Cleanup;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.model.server.ScreeningSurvey;
import org.isgf.mhc.model.server.ScreeningSurveySlide;
import org.isgf.mhc.model.web.types.ScreeningSurveySlideTemplateFields;
import org.isgf.mhc.model.web.types.SessionAttributes;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * Servlet to stream the screening survey
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Screening Survey", value = "/*", asyncSupported = true, loadOnStartup = 1)
@Log4j2
public class ScreeningSurveyServlet extends HttpServlet {
	private MustacheFactory	mustacheFactory;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init(final ServletConfig servletConfig) throws ServletException {
		// Only start servlet if context is ready
		if (!MHC.getInstance().isReady()) {
			log.error("Servlet {} can't be started. Context is not ready!",
					this.getClass());
			throw new ServletException("Context is not ready!");
		}

		log.info("Initializing servlet...");

		log.debug("Initializing mustache template engine");
		this.mustacheFactory = this.createMustacheFactory();

		log.info("Servlet initialized.");
		super.init(servletConfig);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		try {
			// Determine request path
			final String path = request.getRequestURI()
					.substring(request.getContextPath().length())
					.replaceAll("^/", "").replaceAll("/$", "");

			// Determine request type
			val pathParts = path.split("/");
			switch (pathParts.length) {
				case 1:
					if (pathParts[0].equals("")) {
						// Empty request
						this.listActiveScreeningSurveys(request, response);
						return;
					}

					// Only object id
					if (ObjectId.isValid(pathParts[0])) {
						this.handleTemplateRequest(request, response,
								new ObjectId(pathParts[0]));
						return;
					} else {
						throw new Exception("Invalid id");
					}
				default:
					// Object id and file request
					if (ObjectId.isValid(pathParts[0])) {
						this.handleFileRequest(request, response, new ObjectId(
								pathParts[0]),
								path.substring(path.indexOf("/") + 1));
						return;
					} else {
						throw new Exception("Invalid id");
					}
			}
		} catch (final Exception e) {
			log.warn("Request {} could not be fulfilled: {}",
					request.getRequestURI(), e.getMessage());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		log.debug("Redirecting POST request to GET request");
		this.doGet(request, response);
	}

	/**
	 * Return appropriate file fitting to {@link ScreeningSurvey}
	 * 
	 * @param request
	 * @param response
	 * @param screeningSurveyId
	 * @param fileRequest
	 */
	private void handleFileRequest(final HttpServletRequest request,
			final HttpServletResponse response,
			final ObjectId screeningSurveyId, final String fileRequest)
			throws ServletException, IOException {
		log.debug("Handling file request '{}' for screening survey {}",
				fileRequest, screeningSurveyId);

		final ScreeningSurvey screeningSurvey = MHC.getInstance()
				.getScreeningSurveyAdministrationManagerService()
				.getScreeningSurvey(screeningSurveyId);

		if (screeningSurvey == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		final File basicTemplateFolder = new File(MHC.getInstance()
				.getScreeningSurveyExecutionManagerService().getTemplatePath(),
				screeningSurvey.getTemplatePath());
		final File requestedFile = new File(basicTemplateFolder, fileRequest);

		if (!requestedFile.getAbsolutePath().startsWith(
				basicTemplateFolder.getAbsolutePath())
				|| !requestedFile.exists()) {
			log.warn("Requested a file outside the 'sandbox' of the template folder or a file that does not exist");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		log.debug("Requested file is '{}'", requestedFile.getAbsolutePath());

		// Get the MIME type of the requested file
		final String mimeType = this.getServletContext().getMimeType(
				requestedFile.getAbsolutePath());
		if (mimeType == null) {
			log.warn("Could not get MIME type of file '{}'",
					requestedFile.getAbsolutePath());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		// Set content type
		response.setContentType(mimeType);
		log.debug("Sending file '{}' with mime type {}",
				requestedFile.getAbsolutePath(), mimeType);

		// Set content size
		response.setContentLength((int) requestedFile.length());

		// Set name
		response.setHeader("Content-Disposition", "inline; filename=\""
				+ requestedFile.getName() + "\"");

		// Allow caching
		response.setHeader("Pragma", "cache");
		response.setHeader("Cache-control", "cache");

		// Open the file and output streams
		@Cleanup
		final FileInputStream in = new FileInputStream(requestedFile);
		@Cleanup
		final OutputStream out = response.getOutputStream();

		// Copy the contents of the file to the output stream
		IOUtils.copy(in, out);
	}

	/**
	 * Return list of all currently active {@link ScreeningSurvey}s
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void listActiveScreeningSurveys(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		log.debug("Handling request for all open screening surveys");

		log.debug("Setting no-cache headers");
		// Set header information (e.g. for no caching)
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
		response.setDateHeader("Expires", 1);
		response.setContentType("text/html");

		val templateVariables = new HashMap<String, Object>();

		if (Constants.LIST_OPEN_SCREENING_SURVEYS_ON_BASE_URL) {
			// Get all active screening surveys
			val activeScreeningSurveys = MHC.getInstance()
					.getScreeningSurveyExecutionManagerService()
					.getActiveScreeningSurveys();

			if (activeScreeningSurveys != null) {
				templateVariables.put("title", "Active surveys:");

				val surveysData = new ArrayList<HashMap<String, String>>();

				String baseURL = request.getRequestURL().toString();
				if (!baseURL.endsWith("/")) {
					baseURL += "/";
				}

				for (val screeningSurvey : activeScreeningSurveys) {
					val screeningSurveyData = new HashMap<String, String>();
					screeningSurveyData.put("name", screeningSurvey.getName());
					screeningSurveyData.put("url",
							baseURL + screeningSurvey.getId() + "/");
					surveysData.add(screeningSurveyData);
				}

				templateVariables.put("surveys", surveysData);
			} else {
				templateVariables.put("title", "No survey active.");
			}
		} else {
			templateVariables.put("title", "Listing not active.");
		}

		@Cleanup
		val templateInputStream = this.getServletContext().getResourceAsStream(
				"ActiveScreeningSurveysList.template.html");
		@Cleanup
		val templateInputStreamReader = new InputStreamReader(
				templateInputStream, "UTF-8");
		val mustache = this.mustacheFactory.compile(templateInputStreamReader,
				"mustache.template");

		@Cleanup
		val responseOutputStreamWriter = new OutputStreamWriter(
				response.getOutputStream());

		mustache.execute(responseOutputStreamWriter, templateVariables);
	}

	/**
	 * Return appropriate {@link ScreeningSurveySlide} as filled template
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleTemplateRequest(final HttpServletRequest request,
			final HttpServletResponse response, final ObjectId screeningSurveyId)
			throws ServletException, IOException {
		log.debug("Handling template request for screening survey {}",
				screeningSurveyId);

		// Get information from session
		val session = request.getSession(true);
		ObjectId participantId;
		try {
			participantId = (ObjectId) session
					.getAttribute(SessionAttributes.PARTICIPANT_ID.toString());
		} catch (final Exception e) {
			participantId = null;
		}

		// Get question result if available
		String resultValue;
		try {
			resultValue = (String) request.getAttribute("MHC_result");
		} catch (final Exception e) {
			resultValue = null;
		}
		log.debug(
				"Retrieved information from request: participant: {}, screening survey: {}, result value: {}",
				participantId, screeningSurveyId, resultValue);

		// Decide which slide should be send to the participant
		HashMap<String, Object> templateVariables;
		try {
			templateVariables = MHC
					.getInstance()
					.getScreeningSurveyExecutionManagerService()
					.getAppropriateSlide(participantId, screeningSurveyId,
							resultValue, session);

			if (templateVariables == null
					|| !templateVariables
							.containsKey(ScreeningSurveySlideTemplateFields.TEMPLATE_FOLDER
									.toVariable())) {
				throw new NullPointerException();
			}
		} catch (final Exception e) {
			log.warn("An error occurred while getting appropriate slide: {}",
					e.getMessage());

			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		log.debug("Setting no-cache headers");
		// Set header information (e.g. for no caching)
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
		response.setDateHeader("Expires", 1);
		response.setContentType("text/html");

		// Add essential variables to template variables
		String baseURL = request.getRequestURL().toString();
		if (!baseURL.endsWith("/")) {
			baseURL += "/";
		}
		templateVariables.put(
				ScreeningSurveySlideTemplateFields.BASE_URL.toVariable(),
				baseURL);

		// Create new Mustache template factory on non-production system
		if (!Constants.IS_LIVE_SYSTEM) {
			log.debug("Initializing NEW mustache template engine");
			synchronized (this.mustacheFactory) {
				this.mustacheFactory = this.createMustacheFactory();
			}
		}

		// Get template folder
		final String templateFolder = (String) templateVariables
				.get(ScreeningSurveySlideTemplateFields.TEMPLATE_FOLDER
						.toVariable());
		templateVariables
				.remove(ScreeningSurveySlideTemplateFields.TEMPLATE_FOLDER
						.toVariable());

		// Fill template
		log.debug("Filling template in folder {}", templateFolder);
		Mustache mustache;
		synchronized (this.mustacheFactory) {
			mustache = this.mustacheFactory.compile(templateFolder
					+ "/index.html");
		}

		@Cleanup
		val responseOutputStreamWriter = new OutputStreamWriter(
				response.getOutputStream());

		// Send template
		log.debug("Executing and sending template");
		try {
			mustache.execute(responseOutputStreamWriter, templateVariables);
		} catch (final Exception e) {
			log.error("There seems to be a problem with the template: {}",
					e.getMessage());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}

	/**
	 * Creates a Mustache factory
	 * 
	 * @return The newly created Mustache factory
	 */
	private MustacheFactory createMustacheFactory() {
		return new DefaultMustacheFactory(MHC.getInstance()
				.getScreeningSurveyExecutionManagerService().getTemplatePath());
	}
}
