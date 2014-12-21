package ch.ethz.mc.servlets;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
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

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.Feedback;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.services.ScreeningSurveyExecutionManagerService;
import ch.ethz.mc.services.types.FeedbackSlideTemplateFieldTypes;
import ch.ethz.mc.services.types.GeneralSlideTemplateFieldTypes;
import ch.ethz.mc.services.types.ScreeningSurveySessionAttributeTypes;
import ch.ethz.mc.services.types.ScreeningSurveySlideTemplateFieldTypes;
import ch.ethz.mc.services.types.ScreeningSurveySlideTemplateLayoutTypes;

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
		if (!MC.getInstance().isReady()) {
			log.error("Servlet {} can't be started. Context is not ready!",
					this.getClass());
			throw new ServletException("Context is not ready!");
		}
		screeningSurveyExecutionManagerService = MC.getInstance()
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

		request.setCharacterEncoding("UTF-8");
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
					if (ObjectId.isValid(pathParts[0])) {
						if (screeningSurveyExecutionManagerService
								.screeningSurveyCheckIfActive(new ObjectId(
										pathParts[0]))) {
							handleTemplateRequest(request, response,
									new ObjectId(pathParts[0]), null);
							return;
						}
						if (screeningSurveyExecutionManagerService
								.feedbackCheckIfActiveByBelongingParticipant(new ObjectId(
										pathParts[0]))) {
							handleTemplateRequest(request, response, null,
									new ObjectId(pathParts[0]));
							return;
						}
						throw new Exception("Invalid id");
					} else {
						throw new Exception("Invalid id");
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
	 * Return appropriate file fitting to {@link ScreeningSurvey} or
	 * {@link Feedback}
	 * 
	 * @param request
	 * @param response
	 * @param screeningSurveyOrFeedbackParticipantId
	 * @param fileRequest
	 */
	private void handleFileRequest(final HttpServletRequest request,
			final HttpServletResponse response,
			final ObjectId screeningSurveyOrFeedbackParticipantId,
			final String fileRequest) throws ServletException, IOException {
		val screeningSurvey = screeningSurveyExecutionManagerService
				.getScreeningSurveyById(screeningSurveyOrFeedbackParticipantId);
		val feedback = screeningSurveyExecutionManagerService
				.getFeedbackByBelongingParticipant(screeningSurveyOrFeedbackParticipantId);

		if (screeningSurvey == null && feedback == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		val isScreeningSurveyRequest = screeningSurvey != null;

		log.debug("Handling file request '{}' for {} {}", fileRequest,
				isScreeningSurveyRequest ? "scrrening survey"
						: "feedback of participant",
				screeningSurveyOrFeedbackParticipantId);

		final File basicTemplateFolder = new File(
				screeningSurveyExecutionManagerService.getTemplatePath(),
				isScreeningSurveyRequest ? screeningSurvey.getTemplatePath()
						: feedback.getTemplatePath());
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
		if (Constants.IS_LIVE_SYSTEM) {
			response.setHeader("Pragma", "cache");
			response.setHeader(
					"Cache-Control",
					"max-age="
							+ ImplementationConstants.SCREENING_SURVEY_FILE_CACHE_IN_MINUTES);
			response.setDateHeader(
					"Expires",
					System.currentTimeMillis()
							+ ImplementationConstants.SCREENING_SURVEY_FILE_CACHE_IN_MINUTES
							* 1000);
		} else {
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
			response.setDateHeader("Expires", 1);
		}

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

		log.debug("Clearing session");
		val session = request.getSession(true);
		for (val attribute : ScreeningSurveySessionAttributeTypes.values()) {
			val sessionObject = session.getAttribute(attribute.toString());
			if (sessionObject != null) {
				session.removeAttribute(attribute.toString());
			}
		}

		val sessionAttributeNames = session.getAttributeNames();
		while (sessionAttributeNames.hasMoreElements()) {
			log.debug("> " + sessionAttributeNames.nextElement());
		}

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

			// FIXME Should be done more generic
			if (activeScreeningSurveys != null) {
				templateVariables.put("title",
						"Hier geht es zur Eingangsbefragung...");

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
				templateVariables
						.put("title", "Keine Eingansgbefragung aktiv.");
			}
		} else {
			templateVariables.put("title", "Auflistung nicht aktiv.");
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
						.getParameter(ImplementationConstants.SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLE);
			} catch (final Exception e) {
				resultValue = null;
			}

			// Get consistency check value if available
			String checkValue;
			try {
				checkValue = request
						.getParameter(ImplementationConstants.SCREENING_SURVEY_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE);
			} catch (final Exception e) {
				checkValue = null;
			}

			// Remember that user participated in screening survey
			session.setAttribute(
					ScreeningSurveySessionAttributeTypes.FROM_SCREENING_SURVEY
							.toString(), true);

			log.debug(
					"Retrieved information from screening survey slide request: participant: {}, access granted: {}, screening survey: {}, result value: {}, check value: {}",
					participantId, accessGranted, screeningSurveyId,
					resultValue, checkValue);

			// Decide which slide should be send to the participant
			try {
				templateVariables = screeningSurveyExecutionManagerService
						.getAppropriateScreeningSurveySlide(participantId,
								accessGranted, screeningSurveyId, resultValue,
								checkValue, session);

				if (templateVariables == null
						|| !templateVariables
								.containsKey(GeneralSlideTemplateFieldTypes.TEMPLATE_FOLDER
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

			// Get navigation value if available
			String navigationValue;
			try {
				navigationValue = request
						.getParameter(ImplementationConstants.FEEDBACK_SLIDE_WEB_FORM_NAVIGATION_VARIABLE);
			} catch (final Exception e) {
				navigationValue = null;
			}

			// Get consistency check value if available
			String checkValue;
			try {
				checkValue = request
						.getParameter(ImplementationConstants.FEEDBACK_SLIDE_WEB_FORM_CONSISTENCY_CHECK_VARIABLE);
			} catch (final Exception e) {
				checkValue = null;
			}

			log.debug(
					"Retrieved information from feedback slide request: feedback participant: {}, navigation value: {}, check value: {}",
					feedbackParticipantId, navigationValue, checkValue);

			// Decide which slide should be send to the participant
			try {
				templateVariables = screeningSurveyExecutionManagerService
						.getAppropriateFeedbackSlide(feedbackParticipantId,
								navigationValue, checkValue, session);

				if (templateVariables == null
						|| !templateVariables
								.containsKey(GeneralSlideTemplateFieldTypes.TEMPLATE_FOLDER
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
		final String normalizedBaseURL = baseURL
				.replaceAll(
						"/"
								+ ImplementationConstants.REGULAR_EXPRESSION_TO_MATCH_ONE_OBJECT_ID
								+ "/$", "/");

		templateVariables.put(
				GeneralSlideTemplateFieldTypes.BASE_URL.toVariable(), baseURL);
		templateVariables
				.put(ScreeningSurveySlideTemplateFieldTypes.RESULT_VARIABLE
						.toVariable(),
						ImplementationConstants.SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLE);

		// Slide type
		if (screeningSurveyId != null) {
			templateVariables.put(
					ScreeningSurveySlideTemplateFieldTypes.IS_SCREENING_SURVEY
							.toVariable(), true);
		} else {
			templateVariables.put(
					FeedbackSlideTemplateFieldTypes.IS_FEEDBACK.toVariable(),
					true);

			if (session
					.getAttribute(ScreeningSurveySessionAttributeTypes.FROM_SCREENING_SURVEY
							.toString()) != null) {
				templateVariables
						.put(FeedbackSlideTemplateFieldTypes.FROM_SCREENING_SURVEY
								.toVariable(),
								session.getAttribute(ScreeningSurveySessionAttributeTypes.FROM_SCREENING_SURVEY
										.toString()));
			} else {
				templateVariables.put(
						FeedbackSlideTemplateFieldTypes.FROM_SCREENING_SURVEY
								.toVariable(), false);
			}
		}

		// Adjust feedback URL (only for screening survey slides)
		if (screeningSurveyId != null
				&& session
						.getAttribute(ScreeningSurveySessionAttributeTypes.PARTICIPANT_FEEDBACK_URL
								.toString()) != null) {
			templateVariables
					.put(GeneralSlideTemplateFieldTypes.FEEDBACK_URL
							.toVariable(),
							normalizedBaseURL
									+ session
											.getAttribute(ScreeningSurveySessionAttributeTypes.PARTICIPANT_FEEDBACK_URL
													.toString()));
		}

		// Set layout (only for screening survey slides)
		if (screeningSurveyId != null) {
			for (val layout : ScreeningSurveySlideTemplateLayoutTypes.values()) {
				if (templateVariables.get(layout.toVariable()) != null) {
					templateVariables.put(
							ScreeningSurveySlideTemplateFieldTypes.LAYOUT
									.toVariable(), layout.toVariable());
				}
			}
		}

		// Adjust media object URL
		if (templateVariables
				.get(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_URL
						.toVariable()) != null) {
			templateVariables
					.put(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_URL
							.toVariable(),
							normalizedBaseURL
									+ ImplementationConstants.FILE_STREAMING_SERVLET_PATH
									+ "/"
									+ templateVariables
											.get(GeneralSlideTemplateFieldTypes.MEDIA_OBJECT_URL
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
				.get(GeneralSlideTemplateFieldTypes.TEMPLATE_FOLDER
						.toVariable());
		templateVariables.remove(GeneralSlideTemplateFieldTypes.TEMPLATE_FOLDER
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
		val mustacheFactory = new DefaultMustacheFactory(
				screeningSurveyExecutionManagerService.getTemplatePath());
		return mustacheFactory;
	}
}
