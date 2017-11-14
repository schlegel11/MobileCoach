package ch.ethz.mc.servlets;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import ch.ethz.mc.services.SurveyExecutionManagerService;
import ch.ethz.mc.services.types.DashboardTemplateFields;
import ch.ethz.mc.services.types.GeneralSessionAttributeTypes;
import ch.ethz.mc.services.types.GeneralSessionAttributeValidatorTypes;
import ch.ethz.mc.tools.StringHelpers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * Servlet to stream the dashboards
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Dashboards", urlPatterns = "/"
		+ ImplementationConstants.DASHBOARD_SERVLET_PATH
		+ "/*", asyncSupported = true, loadOnStartup = 1)
@Log4j2
public class DashboardServlet extends HttpServlet {
	private MustacheFactory					mustacheFactory;

	private SurveyExecutionManagerService	surveyExecutionManagerService;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init(final ServletConfig servletConfig)
			throws ServletException {
		super.init(servletConfig);
		// Only start servlet if context is ready
		if (!MC.getInstance().isReady()) {
			log.error("Servlet {} can't be started. Context is not ready!",
					this.getClass());
			throw new ServletException("Context is not ready!");
		}
		surveyExecutionManagerService = MC.getInstance()
				.getSurveyExecutionManagerService();

		log.info("Initializing servlet...");

		log.debug("Initializing mustache template engine");
		mustacheFactory = createMustacheFactory();

		log.info("Servlet initialized.");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response)
			throws ServletException, IOException {
		log.debug("Dashboard servlet call");

		request.setCharacterEncoding("UTF-8");
		try {
			// Determine request path
			final String path = request.getRequestURI()
					.substring(request.getContextPath().length()
							+ ImplementationConstants.DASHBOARD_SERVLET_PATH
									.length()
							+ 1)
					.replaceAll("^/", "").replaceAll("/$", "");

			// Determine request type
			val pathParts = path.split("/");
			switch (pathParts.length) {
				case 1:
				case 2:
					String givenPassword;
					if (pathParts.length == 2) {
						givenPassword = pathParts[1];
					} else {
						givenPassword = null;
					}
					// Only object ids of active interventions are accepted
					if (ObjectId.isValid(pathParts[0])) {
						val interventionId = new ObjectId(pathParts[0]);

						if (surveyExecutionManagerService
								.dashboardCheckIfActive(interventionId)) {
							handleTemplateRequest(request, response,
									interventionId, givenPassword);
							return;
						}
						throw new Exception(
								"Invalid id or intervention/dashboard not active");
					} else {
						throw new Exception("Invalid id");
					}
				default:
					// Object id and file request
					if (ObjectId.isValid(pathParts[0])) {
						handleFileRequest(request, response,
								new ObjectId(pathParts[0]),
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
			final HttpServletResponse response)
			throws ServletException, IOException {
		log.debug("Redirecting POST request to GET request");
		doGet(request, response);
	}

	/**
	 * Return appropriate file fitting to dashboard
	 * 
	 * @param request
	 * @param response
	 * @param interventionId
	 * @param fileRequest
	 */
	private void handleFileRequest(final HttpServletRequest request,
			final HttpServletResponse response, final ObjectId interventionId,
			final String fileRequest) throws ServletException, IOException {
		val intervention = surveyExecutionManagerService
				.getInterventionById(interventionId);

		if (intervention == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		log.debug("Handling file request '{}' for dashboard of intervention {}",
				fileRequest, interventionId);

		final File basicTemplateFolder = new File(
				surveyExecutionManagerService.getTemplatePath(),
				intervention.getDashboardTemplatePath());
		final File requestedFile = new File(basicTemplateFolder, fileRequest);

		if (!requestedFile.getAbsolutePath()
				.startsWith(basicTemplateFolder.getAbsolutePath())
				|| !requestedFile.exists()) {
			log.warn(
					"Requested a file outside the 'sandbox' of the template folder or a file that does not exist");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		log.debug("Requested file is '{}'", requestedFile.getAbsolutePath());

		// Get the MIME type of the requested file
		final String mimeType = getServletContext()
				.getMimeType(requestedFile.getAbsolutePath());
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
		response.setHeader("Content-Disposition",
				"inline; filename=\"" + requestedFile.getName() + "\"");

		// Allow caching
		if (Constants.isCachingActive()) {
			response.setHeader("Pragma", "cache");
			response.setHeader("Cache-Control", "max-age="
					+ ImplementationConstants.DASHBOARD_FILE_CACHE_IN_MINUTES);
			response.setDateHeader("Expires",
					System.currentTimeMillis()
							+ ImplementationConstants.DASHBOARD_FILE_CACHE_IN_MINUTES
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
	 * Return dashboard as filled template
	 * 
	 * @param request
	 * @param response
	 * @param interventionId
	 * @param givenPassword
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleTemplateRequest(final HttpServletRequest request,
			final HttpServletResponse response, final ObjectId interventionId,
			final String givenPassword) throws ServletException, IOException {
		log.debug("Handling template request for dashboard of intervention {}",
				interventionId);

		val intervention = surveyExecutionManagerService
				.getInterventionById(interventionId);

		// Check for password
		val passwordPattern = intervention.getDashboardPasswordPattern();
		if (passwordPattern != null && !passwordPattern.equals("")
				&& (givenPassword == null || givenPassword.equals("")
						|| !givenPassword.matches(passwordPattern))) {
			log.warn("Given password '{}' does not match pattern '{}'",
					givenPassword, passwordPattern);

			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		final HashMap<String, Object> templateVariables = new HashMap<String, Object>();
		val session = request.getSession(true);

		// Reset session if there already is a running session but for a
		// different survey
		if (session.getAttribute(GeneralSessionAttributeTypes.CURRENT_SESSION
				.toString()) != null) {
			val currentSurveyRegardingSession = (ObjectId) session.getAttribute(
					GeneralSessionAttributeTypes.CURRENT_SESSION.toString());
			if (interventionId != null
					&& !interventionId.equals(currentSurveyRegardingSession)) {

				// Session needs to be reset
				log.debug("Session needs to be reset due to different survey");
				val sessionAttributeNames = session.getAttributeNames();
				while (sessionAttributeNames.hasMoreElements()) {
					val attribute = sessionAttributeNames.nextElement();
					if (attribute.startsWith(
							ImplementationConstants.SURVEY_OR_FEEDBACK_SESSION_PREFIX)) {
						session.removeAttribute(attribute);
					}
				}
			}
		}

		// Create token
		session.setAttribute(GeneralSessionAttributeTypes.VALIDATOR.toString(),
				GeneralSessionAttributeValidatorTypes.DASHBOARD_ACCESS
						.toString());
		if (session.getAttribute(
				GeneralSessionAttributeTypes.TOKEN.toString()) == null) {
			session.setAttribute(GeneralSessionAttributeTypes.TOKEN.toString(),
					StringHelpers.createRandomString(40));
		}
		session.setAttribute(
				GeneralSessionAttributeTypes.CURRENT_SESSION.toString(),
				interventionId);

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
		if (givenPassword != null && baseURL.endsWith(givenPassword)) {
			baseURL = baseURL.substring(0,
					baseURL.length() - givenPassword.length());
		}
		if (!baseURL.endsWith("/")) {
			baseURL += "/";
		}
		templateVariables.put(DashboardTemplateFields.BASE_URL.toVariable(),
				baseURL);

		// Token
		templateVariables.put(DashboardTemplateFields.TOKEN.toVariable(),
				session.getAttribute(
						GeneralSessionAttributeTypes.TOKEN.toString()));

		// REST API URL
		templateVariables.put(DashboardTemplateFields.REST_API_URL.toVariable(),
				request.getRequestURL().toString().substring(0,
						request.getRequestURL().toString()
								.indexOf(request.getRequestURI()))
						+ request.getContextPath() + "/"
						+ ImplementationConstants.REST_API_PATH + "/"
						+ ImplementationConstants.REST_SESSION_BASED_API_VERSION
						+ "/");

		// Given password
		templateVariables.put(DashboardTemplateFields.PASSWORD.toVariable(),
				givenPassword);

		// Create new Mustache template factory on non-production system
		if (!Constants.isCachingActive()) {
			log.debug("Initializing NEW mustache template engine");
			synchronized (mustacheFactory) {
				mustacheFactory = createMustacheFactory();
			}
		}

		// Get template folder
		val templateFolder = intervention.getDashboardTemplatePath();

		// Fill template
		log.debug("Filling template in folder {}", templateFolder);
		log.debug("Variables: {}", templateVariables.toString());
		Mustache mustache;
		synchronized (mustacheFactory) {
			try {
				mustache = mustacheFactory
						.compile(templateFolder + "/dashboard.html");
			} catch (final Exception e) {
				log.error("There seems to be a problem with the template: {}",
						e.getMessage());
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
				surveyExecutionManagerService.getTemplatePath());
		return mustacheFactory;
	}
}
