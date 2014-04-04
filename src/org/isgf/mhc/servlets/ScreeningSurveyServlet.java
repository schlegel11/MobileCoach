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
import org.isgf.mhc.conf.ImplementationContants;
import org.isgf.mhc.model.persistent.ScreeningSurvey;
import org.isgf.mhc.model.persistent.ScreeningSurveySlide;
import org.isgf.mhc.services.ScreeningSurveyExecutionManagerService;
import org.isgf.mhc.services.types.ScreeningSurveySessionAttributeTypes;
import org.isgf.mhc.services.types.ScreeningSurveySlideTemplateFieldTypes;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * Servlet to stream the screening survey
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Screening Survey and Feedback", value = "/*", asyncSupported = true, loadOnStartup = 1)
@Log4j2
public class ScreeningSurveyServlet extends HttpServlet {
	private MustacheFactory							mustacheFactory;

	private ScreeningSurveyExecutionManagerService	screeningSurveyExecutionManagerService;

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
		screeningSurveyExecutionManagerService = MHC.getInstance()
				.getScreeningSurveyExecutionManagerService();

		log.info("Initializing servlet...");

		log.debug("Initializing mustache template engine");
		mustacheFactory = createMustacheFactory();

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
		log.debug("Screening survey servlet call");
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
						listActiveScreeningSurveys(request, response);
						return;
					}

					// Only object id
					if (ObjectId.isValid(pathParts[0])
							&& screeningSurveyExecutionManagerService
									.screeningSurveyCheckIfActive(new ObjectId(
											pathParts[0]))) {
						handleTemplateRequest(request, response, new ObjectId(
								pathParts[0]), null);
						return;
					} else {
						throw new Exception("Invalid id");
					}
				case 2:
					// Check if it's a feedback, otherwise handle as file
					// (default case)
					if (pathParts[0]
							.equals(ImplementationContants.SCREENING_SURVEY_SERVLET_FEEDBACK_SUBPATH)) {
						if (pathParts[1].equals("")) {
							// Empty request
							throw new Exception("Invalid feedback request");
						}

						// Only object id
						if (ObjectId.isValid(pathParts[1])
								&& screeningSurveyExecutionManagerService
										.feedbackCheckIfActive(new ObjectId(
												pathParts[1]))) {
							handleTemplateRequest(request, response, null,
									new ObjectId(pathParts[1]));
							return;
						} else {
							throw new Exception("Invalid id");
						}
					}
				default:
					// Object id and file request
					if (ObjectId.isValid(pathParts[0])) {
						handleFileRequest(request, response, new ObjectId(
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
		doGet(request, response);
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

		final ScreeningSurvey screeningSurvey = screeningSurveyExecutionManagerService
				.getScreeningSurveyById(screeningSurveyId);

		if (screeningSurvey == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		final File basicTemplateFolder = new File(
				screeningSurveyExecutionManagerService.getTemplatePath(),
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
		final String mimeType = getServletContext().getMimeType(
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

		if (Constants.isListOpenScreenSurveysOnBaseURL()) {
			// Get all active screening surveys
			val activeScreeningSurveys = screeningSurveyExecutionManagerService
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
		val templateInputStream = getServletContext().getResourceAsStream(
				"ActiveScreeningSurveysList.template.html");
		@Cleanup
		val templateInputStreamReader = new InputStreamReader(
				templateInputStream, "UTF-8");
		val mustache = mustacheFactory.compile(templateInputStreamReader,
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
			final HttpServletResponse response,
			final ObjectId screeningSurveyId,
			final ObjectId feedbackParticipantId) throws ServletException,
			IOException {
		if (screeningSurveyId == null) {
			log.debug("Handling template request for feedback participant {}",
					feedbackParticipantId);
		} else {
			log.debug("Handling template request for screening survey {}",
					screeningSurveyId);
		}

		HashMap<String, Object> templateVariables;
		val session = request.getSession(true);

		// Handle screening survey or feedback request
		if (screeningSurveyId != null) {
			/*
			 * Handle screening survey specific things
			 */

			// Get information from session
			ObjectId participantId;
			try {
				participantId = (ObjectId) session
						.getAttribute(ScreeningSurveySessionAttributeTypes.PARTICIPANT_ID
								.toString());
			} catch (final Exception e) {
				participantId = null;
			}
			boolean accessGranted;
			try {
				accessGranted = (boolean) session
						.getAttribute(ScreeningSurveySessionAttributeTypes.PARTICIPANT_ACCESS_GRANTED
								.toString());
			} catch (final Exception e) {
				accessGranted = false;
			}

			// Get question result if available
			String resultValue;
			try {
				resultValue = request
						.getParameter(ImplementationContants.SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLE);
			} catch (final Exception e) {
				resultValue = null;
			}
			// Get consistence check value if available
			String checkValue;
			try {
				checkValue = request
						.getParameter(ImplementationContants.SCREENING_SURVEY_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE);
			} catch (final Exception e) {
				checkValue = null;
			}
			log.debug(
					"Retrieved information from request: participant: {}, access granted: {}, screening survey: {}, result value: {}",
					participantId, accessGranted, screeningSurveyId,
					resultValue);

			// Decide which slide should be send to the participant
			try {
				templateVariables = screeningSurveyExecutionManagerService
						.getAppropriateScreeningSurveySlide(participantId,
								accessGranted, screeningSurveyId, resultValue,
								checkValue, session);

				if (templateVariables == null
						|| !templateVariables
								.containsKey(ScreeningSurveySlideTemplateFieldTypes.TEMPLATE_FOLDER
										.toVariable())) {
					throw new NullPointerException();
				}
			} catch (final Exception e) {
				log.warn(
						"An error occurred while getting appropriate slide: {}",
						e.getMessage());

				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		} else {
			/*
			 * Handle feedback specific things
			 */

			// Decide which slide should be send to the participant
			try {
				templateVariables = screeningSurveyExecutionManagerService
						.getAppropriateFeedbackSlide(feedbackParticipantId,
								session);

				if (templateVariables == null
						|| !templateVariables
								.containsKey(ScreeningSurveySlideTemplateFieldTypes.TEMPLATE_FOLDER
										.toVariable())) {
					throw new NullPointerException();
				}
			} catch (final Exception e) {
				log.warn(
						"An error occurred while getting appropriate slide: {}",
						e.getMessage());

				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		}

		log.debug("Setting no-cache headers");
		// Set header information (e.g. for no caching)
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
		response.setDateHeader("Expires", 1);
		response.setContentType("text/html");

		/*
		 * Add essential variables to template variables
		 */

		// Base URL
		String baseURL = request.getRequestURL().toString();
		if (!baseURL.endsWith("/")) {
			baseURL += "/";
		}
		templateVariables.put(
				ScreeningSurveySlideTemplateFieldTypes.BASE_URL.toVariable(),
				baseURL);
		templateVariables
				.put(ScreeningSurveySlideTemplateFieldTypes.RESULT_VARIABLE
						.toVariable(),
						ImplementationContants.SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLE);

		// Adjust feedback URL
		if (session
				.getAttribute(ScreeningSurveySessionAttributeTypes.PARTICIPANT_FEEDBACK_URL
						.toString()) != null) {
			templateVariables
					.put(ScreeningSurveySlideTemplateFieldTypes.FEEDBACK_URL
							.toVariable(),
							baseURL

									+ session
											.getAttribute(ScreeningSurveySessionAttributeTypes.PARTICIPANT_FEEDBACK_URL
													.toString()));
		}

		// Adjust media object URL
		if (templateVariables
				.get(ScreeningSurveySlideTemplateFieldTypes.MEDIA_OBJECT_URL
						.toVariable()) != null) {
			templateVariables
					.put(ScreeningSurveySlideTemplateFieldTypes.MEDIA_OBJECT_URL
							.toVariable(),
							baseURL
									+ "../"
									+ ImplementationContants.FILE_STREAMING_SERVLET_PATH
									+ "/"
									+ templateVariables
											.get(ScreeningSurveySlideTemplateFieldTypes.MEDIA_OBJECT_URL
													.toVariable()));
		}

		// Create new Mustache template factory on non-production system
		if (!Constants.IS_LIVE_SYSTEM) {
			log.debug("Initializing NEW mustache template engine");
			synchronized (mustacheFactory) {
				mustacheFactory = createMustacheFactory();
			}
		}

		// Get template folder
		final String templateFolder = (String) templateVariables
				.get(ScreeningSurveySlideTemplateFieldTypes.TEMPLATE_FOLDER
						.toVariable());
		templateVariables
				.remove(ScreeningSurveySlideTemplateFieldTypes.TEMPLATE_FOLDER
						.toVariable());

		// Fill template
		log.debug("Filling template in folder {}", templateFolder);
		log.debug("Variables: {}", templateVariables.toString());
		Mustache mustache;
		synchronized (mustacheFactory) {
			try {
				mustache = mustacheFactory.compile(templateFolder
						+ "/index.html");
			} catch (final Exception e) {
				log.error("There seems to be a problem with the template: {}",
						e.getMessage());
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
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
		return new DefaultMustacheFactory(
				screeningSurveyExecutionManagerService.getTemplatePath());
	}
}
