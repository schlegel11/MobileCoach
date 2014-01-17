package org.isgf.mhc.servlets;

import java.io.IOException;
import java.io.InputStreamReader;
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

import org.bson.types.ObjectId;
import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.conf.ScreeningSurveyMessageStrings;

import com.github.mustachejava.DefaultMustacheFactory;
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

		this.mustacheFactory = new DefaultMustacheFactory();

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

		// TODO ADD LOG MESSAGES

		// Set header information (e.g. for no caching)
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
		response.setDateHeader("Expires", 1);
		response.setContentType("text/html");

		// Get information from session
		val session = request.getSession(true);
		ObjectId participantId;
		ObjectId screeningSurveyId;
		try {
			participantId = (ObjectId) session
					.getAttribute(Constants.SESSION_PARTICIPANT_ID);
		} catch (final Exception e) {
			participantId = null;
		}
		try {
			screeningSurveyId = (ObjectId) session
					.getAttribute(Constants.SESSION_SCREENING_SURVEY_ID);
		} catch (final Exception e) {
			screeningSurveyId = null;
		}

		// Get question result if available
		String resultValue;
		try {
			resultValue = (String) request
					.getAttribute(Constants.SSS_TEMPLATE_RESULT_VALUE);
		} catch (final Exception e) {
			resultValue = null;
		}

		// Decide which slide should be send to the participant
		HashMap<String, Object> templateVariables;
		try {
			templateVariables = MHC
					.getInstance()
					.getScreeningSurveyManagerService()
					.getAppropriateSlide(participantId, screeningSurveyId,
							resultValue, session);

			if (templateVariables == null) {
				throw new NullPointerException();
			}
		} catch (final Exception e) {
			templateVariables = new HashMap<String, Object>();
			templateVariables.put(Constants.SSS_TEMPLATE_STEP,
					Constants.SSS_TEMPLATE_STEPS_ERROR);
			templateVariables
					.put(Constants.SSS_TEMPLATE_GLOBAL_MESSAGE,
							Messages.getScreeningSurveyString(ScreeningSurveyMessageStrings.UNKNOWN_ERROR));
		}

		// Fill template
		@Cleanup
		val templateInputStreamReader = new InputStreamReader(this
				.getServletContext()
				.getResource(Constants.SCREENING_SURVEY_TEMPLATE).openStream(),
				"UTF-8");
		val mustache = this.mustacheFactory.compile(templateInputStreamReader,
				"mustache.template");

		@Cleanup
		val responseOutputStreamWriter = new OutputStreamWriter(
				response.getOutputStream());

		// Send template
		mustache.execute(responseOutputStreamWriter, templateVariables);
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
}
