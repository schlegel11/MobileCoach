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
import ch.ethz.mc.model.persistent.Feedback;
import ch.ethz.mc.model.persistent.FeedbackSlide;
import ch.ethz.mc.model.persistent.IntermediateSurveyAndFeedbackParticipantShortURL;
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
 * Servlet to stream the survey slides of intermediate surveys and feedbacks
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Short URL Screening Surveys and Feedback", value = "/"
		+ ImplementationConstants.SHORT_ID_SCREEN_SURVEY_AND_FEEDBACK_SERVLET_PATH
		+ "/*", asyncSupported = true, loadOnStartup = 1)
@Log4j2
public class ShortURLIntermediateSurveyAndFeedbackServlet extends HttpServlet {
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
		log.debug("Intermediate survey or feedback servlet call");

		request.setCharacterEncoding("UTF-8");
		try {
			// Determine request path
			String path = request
					.getRequestURI()
					.substring(
							request.getContextPath().length()
									+ ImplementationConstants.SHORT_ID_SCREEN_SURVEY_AND_FEEDBACK_SERVLET_PATH
											.length() + 1).replaceAll("^/", "")
					.replaceAll("/$", "");

			// Determine request type
			val pathParts = path.split("/");

			if (pathParts[0].equals("")) {
				// Empty request
				throw new Exception("Cannot be called this way");
			}

			// Get appropriate short id object
			val shortIdLong = IntermediateSurveyAndFeedbackParticipantShortURL
					.validateURLIdPartAndReturnShortId(pathParts[0]);
			val shortId = screeningSurveyExecutionManagerService
					.getIntermediateSurveyAndFeedbackParticipantShortURL(shortIdLong);
			if (shortId == null
					|| !shortId.validateSecretInGivenIdPart(pathParts[0])) {
				throw new Exception("Invalid id");
			}

			log.debug("Request for short id {}", shortId);

			switch (pathParts.length) {
				case 1:
					// Only object ids of active intermediate surveys or
					// feedbacks surveys are accepted
					if (shortId.getSurvey() != null
							&& screeningSurveyExecutionManagerService
									.screeningSurveyCheckIfActiveAndOfGivenType(
											shortId.getSurvey(), true)) {
						handleTemplateRequest(request, response,
								shortId.getParticipant(), shortId.getSurvey(),
								null);
						return;
					} else if (shortId.getFeedback() != null
							&& screeningSurveyExecutionManagerService
									.feedbackCheckIfActiveByBelongingParticipant(
											shortId.getParticipant(),
											shortId.getFeedback())) {
						handleTemplateRequest(request, response,
								shortId.getParticipant(), null,
								shortId.getFeedback());
						return;
					}
					throw new Exception("Invalid id");
				default:
					// Object id and file request
					handleFileRequest(request, response, shortId.getSurvey(),
							shortId.getFeedback(),
							path.substring(path.indexOf("/") + 1));
					return;
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
	 * Return appropriate file fitting to {@link ScreeningSurvey} or
	 * {@link Feedback}
	 * 
	 * @param request
	 * @param response
	 * @param surveyId
	 * @param feedbackId
	 * @param fileRequest
	 */
	private void handleFileRequest(final HttpServletRequest request,
			final HttpServletResponse response, final ObjectId surveyId,
			final ObjectId feedbackId, final String fileRequest)
			throws ServletException, IOException {
		ScreeningSurvey survey = null;
		Feedback feedback = null;

		boolean isSurveyRequest;
		if (surveyId != null) {
			survey = screeningSurveyExecutionManagerService
					.getScreeningSurveyById(surveyId);
			isSurveyRequest = true;
		} else {
			feedback = screeningSurveyExecutionManagerService
					.getFeedbackById(feedbackId);
			isSurveyRequest = false;
		}

		if (survey == null && feedback == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		log.debug("Handling file request '{}' for {} {}", fileRequest,
				isSurveyRequest ? "survey" : "feedback",
				isSurveyRequest ? surveyId : feedbackId);

		final File basicTemplateFolder = new File(
				screeningSurveyExecutionManagerService.getTemplatePath(),
				isSurveyRequest ? survey.getTemplatePath()
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
	 * Return appropriate {@link ScreeningSurveySlide} or {@link FeedbackSlide}
	 * as filled template
	 * 
	 * @param request
	 * @param response
	 * @param participantId
	 * @param surveyId
	 * @param feedbackId
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleTemplateRequest(final HttpServletRequest request,
			final HttpServletResponse response, final ObjectId participantId,
			final ObjectId surveyId, final ObjectId feedbackId)
			throws ServletException, IOException {
		if (surveyId != null) {
			log.debug(
					"Handling template request for intermediate survey {} of participant {}",
					surveyId, participantId);
		} else {
			log.debug(
					"Handling template request for feedback {} of participant {}",
					feedbackId, participantId);
		}

		HashMap<String, Object> templateVariables;
		val session = request.getSession(true);

		// Reset session if there already is a running session but for a
		// different survey
		if (session
				.getAttribute(ImplementationConstants.SURVEYS_CURRENT_SURVEY_CHECK_SESSION_ATTRIBUTE) != null) {
			val currentSurveyRegardingSession = (ObjectId) session
					.getAttribute(ImplementationConstants.SURVEYS_CURRENT_SURVEY_CHECK_SESSION_ATTRIBUTE);
			if ((surveyId != null && !surveyId
					.equals(currentSurveyRegardingSession))
					|| (feedbackId != null && !feedbackId
							.equals(currentSurveyRegardingSession))) {

				// Session needs to be reset
				log.debug("Session needs to be reset due to different survey");
				val sessionAttributeNames = session.getAttributeNames();
				while (sessionAttributeNames.hasMoreElements()) {
					val attribute = (String) sessionAttributeNames
							.nextElement();
					if (attribute
							.startsWith(ImplementationConstants.SURVEY_SESSION_PREFIX)
							&& !attribute
									.equals(ScreeningSurveySessionAttributeTypes.SCREENING_SURVEY_FROM_URL
											.toString()))
						session.removeAttribute(attribute);
				}
			}
		}

		if (surveyId != null) {
			session.setAttribute(
					ImplementationConstants.SURVEYS_CURRENT_SURVEY_CHECK_SESSION_ATTRIBUTE,
					surveyId);

		} else if (feedbackId != null) {
			session.setAttribute(
					ImplementationConstants.SURVEYS_CURRENT_SURVEY_CHECK_SESSION_ATTRIBUTE,
					feedbackId);
		}

		// Reset session if there already is a running session but for a
		// different participant
		if (session
				.getAttribute(ImplementationConstants.SURVEYS_CURRENT_PARTICIPANT_CHECK_SESSION_ATTRIBUTE) != null
				&& !((ObjectId) session
						.getAttribute(ImplementationConstants.SURVEYS_CURRENT_PARTICIPANT_CHECK_SESSION_ATTRIBUTE))
						.equals(participantId)) {

			// Session needs to be reset
			log.debug("Session needs to be reset due to different participant");
			val sessionAttributeNames = session.getAttributeNames();
			while (sessionAttributeNames.hasMoreElements()) {
				val attribute = (String) sessionAttributeNames.nextElement();
				if (attribute
						.startsWith(ImplementationConstants.SURVEY_SESSION_PREFIX)) {
					session.removeAttribute(attribute);
				}
			}
		}

		session.setAttribute(
				ImplementationConstants.SURVEYS_CURRENT_PARTICIPANT_CHECK_SESSION_ATTRIBUTE,
				participantId);

		// Handle survey or feedback request
		if (surveyId != null) {
			/*
			 * Handle screening survey specific things
			 */

			// Get information from session
			boolean accessGranted;
			try {
				accessGranted = (boolean) session
						.getAttribute(ScreeningSurveySessionAttributeTypes.SCREENING_SURVEY_PARTICIPANT_ACCESS_GRANTED
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

			log.debug(
					"Retrieved information from intermediate survey slide request: participant: {}, access granted: {}, screening survey: {}, result value: {}, check value: {}",
					participantId, accessGranted, surveyId, resultValues,
					checkValue);

			// Decide which slide should be send to the participant
			try {
				templateVariables = screeningSurveyExecutionManagerService
						.getAppropriateScreeningSurveySlide(participantId,
								accessGranted, false, surveyId, resultValues,
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
					feedbackId, navigationValue, checkValue);

			// Decide which slide should be send to the participant
			try {
				templateVariables = screeningSurveyExecutionManagerService
						.getAppropriateFeedbackSlide(participantId, feedbackId,
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

		// Slide type
		if (surveyId != null) {
			templateVariables.put(
					ScreeningSurveySlideTemplateFieldTypes.IS_SCREENING_SURVEY
							.toVariable(), true);
		} else {
			templateVariables.put(
					FeedbackSlideTemplateFieldTypes.IS_FEEDBACK.toVariable(),
					true);

			if (session
					.getAttribute(ScreeningSurveySessionAttributeTypes.SCREENING_SURVEY_FROM_URL
							.toString()) != null) {
				templateVariables
						.put(FeedbackSlideTemplateFieldTypes.FROM_SCREENING_SURVEY
								.toVariable(),
								session.getAttribute(ScreeningSurveySessionAttributeTypes.SCREENING_SURVEY_FROM_URL
										.toString()));
			} else {
				templateVariables.put(
						FeedbackSlideTemplateFieldTypes.FROM_SCREENING_SURVEY
								.toVariable(), false);
			}
		}

		// Adjust feedback URL (only for intermediate survey slides)
		if (surveyId != null
				&& session
						.getAttribute(ScreeningSurveySessionAttributeTypes.SCREENING_SURVEY_PARTICIPANT_FEEDBACK_URL
								.toString()) != null) {
			templateVariables
					.put(GeneralSlideTemplateFieldTypes.FEEDBACK_URL
							.toVariable(),
							normalizedBaseURL
									+ session
											.getAttribute(ScreeningSurveySessionAttributeTypes.SCREENING_SURVEY_PARTICIPANT_FEEDBACK_URL
													.toString()));
		}

		// Set layout (only for intermediate survey slides)
		if (surveyId != null) {
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