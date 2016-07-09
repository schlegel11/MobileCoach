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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
 * Servlet to stream the survey slides of screening surveys with id-based URLs
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Screening Surveys", value = "/*", asyncSupported = true, loadOnStartup = 1)
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
		log.debug("Survey servlet call");

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

					// Only object ids of active screening surveys (non
					// intermediate) surveys are accepted
					if (ObjectId.isValid(pathParts[0])) {
						if (screeningSurveyExecutionManagerService
								.screeningSurveyCheckIfActiveAndOGivenType(new ObjectId(
										pathParts[0]), false)) {
							handleTemplateRequest(request, response,
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
						throw new Exception("Invalid request");
					}
			}
		} catch (final Exception e) {
			log.warn("Request {} could not be fulfilled: {}",
					request.getRequestURI(), e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
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
		val screeningSurvey = screeningSurveyExecutionManagerService
				.getScreeningSurveyById(screeningSurveyId);

		if (screeningSurvey == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		log.debug("Handling file request '{}' for screening survey {}",
				fileRequest, screeningSurveyId);

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
		if (Constants.isCachingActive()) {
			response.setHeader("Pragma", "cache");
			response.setHeader("Cache-Control", "max-age="
					+ ImplementationConstants.SURVEY_FILE_CACHE_IN_MINUTES);
			response.setDateHeader("Expires", System.currentTimeMillis()
					+ ImplementationConstants.SURVEY_FILE_CACHE_IN_MINUTES
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
			// Get all active non intermediate screening surveys
			val activeScreeningSurveys = screeningSurveyExecutionManagerService
					.getActiveNonItermediateScreeningSurveys();

			if (activeScreeningSurveys != null) {
				templateVariables.put("title",
						Constants.getSurveyListingTitle());

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
				templateVariables.put("title",
						Constants.getSurveyListingNoneActive());
			}
		} else {
			templateVariables.put("title",
					Constants.getSurveyListingNotActive());
		}

		@Cleanup
		val templateInputStream = getServletContext().getResourceAsStream(
				"/ActiveScreeningSurveysList.template.html");
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
			final HttpServletResponse response, final ObjectId screeningSurveyId)
			throws ServletException, IOException {
		log.debug("Handling template request for screening survey {}",
				screeningSurveyId);

		HashMap<String, Object> templateVariables;
		val session = request.getSession(true);

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

		// Get question result(s) if available
		List<String> resultValues = null;
		int i = 0;
		String resultValue = null;
		do {
			resultValue = request
					.getParameter(ImplementationConstants.SCREENING_SURVEY_SLIDE_WEB_FORM_RESULT_VARIABLES
							+ i);

			if (resultValue != null) {
				if (resultValues == null) {
					resultValues = new ArrayList<String>();
				}
				resultValues.add(resultValue);
			}
			i++;
		} while (resultValue != null);

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
				participantId, accessGranted, screeningSurveyId, resultValues,
				checkValue);

		// Decide which slide should be send to the participant
		try {
			templateVariables = screeningSurveyExecutionManagerService
					.getAppropriateScreeningSurveySlide(participantId,
							accessGranted, screeningSurveyId, resultValues,
							checkValue, session);

			if (templateVariables == null
					|| !templateVariables
							.containsKey(GeneralSlideTemplateFieldTypes.TEMPLATE_FOLDER
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

		// Adjust feedback URL
		if (session
				.getAttribute(ScreeningSurveySessionAttributeTypes.PARTICIPANT_FEEDBACK_URL
						.toString()) != null) {
			templateVariables
					.put(GeneralSlideTemplateFieldTypes.FEEDBACK_URL
							.toVariable(),
							session.getAttribute(ScreeningSurveySessionAttributeTypes.PARTICIPANT_FEEDBACK_URL
									.toString()));
		}

		// Set layout
		for (val layout : ScreeningSurveySlideTemplateLayoutTypes.values()) {
			if (templateVariables.get(layout.toVariable()) != null) {
				templateVariables.put(
						ScreeningSurveySlideTemplateFieldTypes.LAYOUT
								.toVariable(), layout.toVariable());
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
		if (!Constants.isCachingActive()) {
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
