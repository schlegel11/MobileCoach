package ch.ethz.mc.servlets;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;

/**
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Testing Interface", value = "/self-test", asyncSupported = true, loadOnStartup = 2)
@Log4j2
public class TestServlet extends HttpServlet {
	private ServletOutputStream	servletOutputStream	= null;

	@Override
	public void init(final ServletConfig servletConfig) throws ServletException {
		// Only start servlet if context is ready
		if (!MC.getInstance().isReady()) {
			log.error("Servlet {} can't be started. Context is not ready!",
					this.getClass());
			throw new ServletException("Context is not ready!");
		}

		log.info("Initializing servlet...");

		if (Constants.RUN_TESTS_AT_STARTUP) {
			// Perform tests
			log.debug("STARTING TEST...");
			try {
				runTestcases();
			} catch (final Exception e) {
				log.error("ERROR at running testcase: " + e.getMessage());
				log.error(e.getStackTrace().toString());
			}
			log.debug("TEST DONE.");
		}

		log.info("Servlet initialized.");
		super.init(servletConfig);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	@Synchronized
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		// Set header information (e.g. for no caching)
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
		response.setDateHeader("Expires", 1);
		response.setContentType("text/plain");

		// Remember http servlet response output stream for logging
		servletOutputStream = response.getOutputStream();

		// Perform tests
		logToWeb("STARTING TEST...");
		try {
			runTestcases();
		} catch (final Exception e) {
			logToWeb("ERROR at running testcase: " + e.getMessage());
			logToWeb(e.getStackTrace().toString());
		}
		logToWeb("TEST DONE.");
	}

	private void runTestcases() {
		// TODO OPTIONAL: Testcases could be defined here
		// final MonitoringMessageRule m = new MonitoringMessageRule(
		// null,
		// 0,
		// "03.04.2014",
		// RuleEquationSignTypes.CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_TRUE_IF_ZERO,
		// "01.04.2014");
		// val x = RuleEvaluator.evaluateRule(m,
		// new ArrayList<AbstractVariableWithValue>());
		// log.debug(">>" + x.getCalculatedRuleValue());

		// final MonitoringMessageRule m = new MonitoringMessageRule(null, 0,
		// "3.06.2014",
		// RuleEquationSignTypes.DATE_DIFFERENCE_VALUE_EQUALS, "-1");
		// final val x = RuleEvaluator.evaluateRule(m,
		// new ArrayList<AbstractVariableWithValue>());
		// log.debug(">>" + x.isRuleMatchesEquationSign());

		// val variables = new ArrayList<AbstractVariableWithValue>();
		// variables.add(new InterventionVariableWithValue(null,
		// "$fieldWithValues", "5,42,3"));
		// final MonitoringMessageRule m = new MonitoringMessageRule(
		// null,
		// 0,
		// "position(1,$fieldWithValues)",
		// RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE,
		// "");
		// final val x = RuleEvaluator.evaluateRule(m, variables);
		// log.debug(">>" + x.getCalculatedRuleValue());

	}

	private void logToWeb(final String logMessage) {
		if (servletOutputStream != null) {
			try {
				servletOutputStream.print(logMessage + "\n");
				servletOutputStream.flush();
			} catch (final IOException e) {
				// Do nothing
			}
		}
		log.debug(logMessage);
	}
}