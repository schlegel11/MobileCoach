package org.isgf.mhc;

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

import org.isgf.mhc.conf.Constants;

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
	 * @see HttpServlet#HttpServlet()
	 */
	public ScreeningSurveyServlet() {
		super();
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException {
		log.info("Initializing servlet...");

		this.mustacheFactory = new DefaultMustacheFactory();

		super.init(config);
		log.info("Servlet initialized.");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		// Set header information (e.g. for no caching)
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
		response.setDateHeader("Expires", 1);
		response.setContentType("text/html");

		// TODO Logic has to decide how the map has to be filled. The map also
		// has to include the information for the step of the template etc.
		val scopes = new HashMap<String, Object>();
		scopes.put("name", "Mustache");

		// Fill template
		@Cleanup
		val templateInputStreamReader = new InputStreamReader(this
				.getServletContext()
				.getResource(Constants.SCREENING_SURVEY_TEMPLATE).openStream(),
				"UTF-8");
		val mustache = this.mustacheFactory.compile(templateInputStreamReader,
				"test");

		@Cleanup
		val responseOutputStreamWriter = new OutputStreamWriter(
				response.getOutputStream());

		// Send template
		mustache.execute(responseOutputStreamWriter, scopes);
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
