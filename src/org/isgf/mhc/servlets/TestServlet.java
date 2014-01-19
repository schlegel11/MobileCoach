package org.isgf.mhc.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Cleanup;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;
import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.Constants;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.Author;
import org.isgf.mhc.model.server.AuthorInterventionAccess;
import org.isgf.mhc.model.server.Intervention;
import org.isgf.mhc.model.server.InterventionRule;
import org.isgf.mhc.model.server.MediaObject;
import org.isgf.mhc.model.server.ParticipantVariableWithValue;
import org.isgf.mhc.model.server.concepts.AbstractVariableWithValue;
import org.isgf.mhc.model.server.types.EquationSignTypes;
import org.isgf.mhc.model.server.types.MediaObjectTypes;
import org.isgf.mhc.tools.BCrypt;
import org.isgf.mhc.tools.ModelObjectExchange;
import org.isgf.mhc.tools.RuleEvaluator;
import org.isgf.mhc.tools.StringValidator;

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
		if (!MHC.getInstance().isReady()) {
			log.error("Servlet {} can't be started. Context is not ready!",
					this.getClass());
			throw new ServletException("Context is not ready!");
		}

		log.info("Initializing servlet...");

		if (Constants.RUN_TESTS_AT_STARTUP) {
			// Perform tests
			log.debug("STARTING TEST...");
			try {
				this.completeDataTestcase();
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
		this.servletOutputStream = response.getOutputStream();

		// Perform tests
		this.logToWeb("STARTING TEST...");
		try {
			this.completeDataTestcase();
		} catch (final Exception e) {
			this.logToWeb("ERROR at running testcase: " + e.getMessage());
			this.logToWeb(e.getStackTrace().toString());
		}
		this.logToWeb("TEST DONE.");
	}

	private void completeDataTestcase() throws Exception {
		// Object creation
		this.logToWeb("TESTING OBJECT CREATION");
		val author = new Author(false, "Author", BCrypt.hashpw("abc",
				BCrypt.gensalt()));
		author.save();
		val intervention = new Intervention("Testintervention",
				System.currentTimeMillis(), false, false, 16, 10);
		intervention.save();
		val authorInterventionAccess = new AuthorInterventionAccess(
				author.getId(), intervention.getId());
		authorInterventionAccess.save();

		// MediaObject creation
		this.logToWeb("TESTING MEDIA OBJECT CREATION");
		final MediaObject mediaObject;
		try {
			@Cleanup("delete")
			val tempFile = File.createTempFile("MHC_TEST_", ".txt");
			try {
				@Cleanup
				val fileOutputStream = new FileOutputStream(tempFile);
				fileOutputStream.write("This is a testcase!".getBytes("UTF-8"));

				mediaObject = new MediaObject(MediaObjectTypes.HTML_TEXT,
						"test.txt", tempFile);
				mediaObject.save();
			} catch (final Exception e) {
				throw e;
			}

			// Rule and variable validation
			this.logToWeb("TESTING RULE AND VARIABLE VALIDATION");
			val ruleString = "$test*($anonymous+2)-$a_b_c*sin(pi)^2";
			if (!StringValidator.isValidRule(ruleString)) {
				throw new Exception("Rule validation incorrect");
			}
			val variableString = "$A_b_CC";
			if (!StringValidator.isValidVariableName(variableString)) {
				throw new Exception("Variable validation incorrect");
			}

			// Rule evaluation
			this.logToWeb("TESTING RULE EVALUATION");
			val interventionRule = new InterventionRule(null, 0, null, false,
					ruleString, EquationSignTypes.IS_BIGGER_OR_EQUAL_THAN, "");
			val variableList = new ArrayList<AbstractVariableWithValue>();
			val variableWithValue1 = new ParticipantVariableWithValue(
					new ObjectId(), "$test", "5");
			variableList.add(variableWithValue1);
			val variableWithValue2 = new ParticipantVariableWithValue(
					new ObjectId(), "$anonymous", "2");
			variableList.add(variableWithValue2);
			val ruleEvaluationResult = RuleEvaluator.evaluateRule(
					interventionRule, variableList);
			if (!ruleEvaluationResult.isEvaluatedSuccessful()) {
				throw new Exception("Rule evaluation incorrect");
			}
			if (!ruleEvaluationResult.isRuleMatchesEquationSign()) {
				throw new Exception("Rule equation incorrect");
			}

			// Object export
			this.logToWeb("TESTING MODEL EXPORT");
			val modelObjectsToExport = new ArrayList<ModelObject>();
			modelObjectsToExport.add(author);
			modelObjectsToExport.add(intervention);
			modelObjectsToExport.add(authorInterventionAccess);
			modelObjectsToExport.add(mediaObject);
			@Cleanup("delete")
			File exportTempFile = null;
			try {
				exportTempFile = ModelObjectExchange
						.exportModelObjects(modelObjectsToExport);
			} catch (final Exception e) {
				throw e;
			}

			// Object import
			this.logToWeb("TESTING MODEL IMPORT");
			try {
				val importedModelObjects = ModelObjectExchange
						.importModelObjects(exportTempFile);
				for (val importedModelObject : importedModelObjects) {
					ModelObject.remove(importedModelObject.getClass(),
							importedModelObject.getId());
				}
			} catch (final Exception e) {
				throw e;
			}

			// Object deletion
			this.logToWeb("TESTING MEDIA OBJECT DELETION");
		} catch (final Exception e) {
			throw e;
		} finally {
			ModelObject.remove(AuthorInterventionAccess.class,
					authorInterventionAccess.getId());
			ModelObject.remove(Author.class, author.getId());
			ModelObject.remove(Intervention.class, intervention.getId());
		}
		ModelObject.remove(MediaObject.class, mediaObject.getId());
	}

	private void logToWeb(final String logMessage) {
		if (this.servletOutputStream != null) {
			try {
				this.servletOutputStream.print(logMessage + "\n");
				this.servletOutputStream.flush();
			} catch (final IOException e) {
				// Do nothing
			}
		}
		log.debug(logMessage);
	}
}