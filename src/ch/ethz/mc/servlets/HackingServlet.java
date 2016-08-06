package ch.ethz.mc.servlets;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.AbstractModelObjectAccessService;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.MonitoringMessage;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringMessageRule;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.services.InterventionAdministrationManagerService;

/**
 * This servlet should NEVER be part of the public release
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Hacking Interface", urlPatterns = "/hacking", asyncSupported = true, loadOnStartup = 4)
@Log4j2
public class HackingServlet extends HttpServlet {
	private final boolean								ACTIVE				= false;
	private final String								PATH				= "/Users/andreas/Dropbox/Dokumente/Cloud/MobileCoach/Development/Cheating";

	private MC											mc;
	private static HackingDatabaseManager				db					= null;

	private InterventionAdministrationManagerService	iam;

	final List<ModelObject>								createdModelObjects	= new ArrayList<ModelObject>();

	@Override
	public void init(final ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		if (!ACTIVE) {
			log.debug("Hacking servlet is disabled");
			return;
		}

		// Only start servlet if context is ready
		if (!MC.getInstance().isReady()) {
			log.error("Servlet {} can't be started. Context is not ready!",
					this.getClass());
			throw new ServletException("Context is not ready!");
		}

		mc = MC.getInstance();
		db = new HackingDatabaseManager();

		iam = mc.getInterventionAdministrationManagerService();

		log.info("Initializing servlet...");

		// Perform hacks
		log.debug("STARTING HACKS...");
		try {
			runHacks();
		} catch (final Exception e) {
			log.error("ERROR at running testcase: " + e.getMessage());
			log.error(e.getStackTrace().toString());
		}
		log.debug("HACK DONE.");

		log.info("Servlet initialized.");

	}

	/*
	 * HACKS START HERE
	 */
	private void runHacks() {
		// TODO for HACKING (OPTIONAL): Hacks performed at startup can be
		// defined here

		try {
			// createQuiz("Emotion");
			// createdModelObjects.clear();
			// createQuiz("Stress");
			// createdModelObjects.clear();
			// createQuiz("Stress&Smoking");
			// createdModelObjects.clear();
			// createQuiz("Norm 1");
			// createdModelObjects.clear();
			// createQuiz("Norm 2");
			// createdModelObjects.clear();
			// createQuiz("Health Smoke");
			// createdModelObjects.clear();
			// createQuiz("Health NoSmoke");
			// createdModelObjects.clear();
			// createQuiz("Add");
			// createdModelObjects.clear();
			// createExtraMessages();
			// createdModelObjects.clear();
			createStageMessages();
			createdModelObjects.clear();
		} catch (final IOException e) {
			log.error("Error at bulk operation: {}", e.getMessage());

			log.debug("Rolling back changes...");
			for (val modelObject : createdModelObjects) {
				db.deleteModelObject(modelObject);
			}
			log.debug("Rollback done.");
		}
	}

	@SuppressWarnings("unused")
	private void createQuiz(final String quizName) throws IOException {
		val operationName = "create quiz " + quizName;
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val interventionId = new ObjectId("579f775d9afa068926c91af5");
		val quiz = quizName;
		val quizReplyVariable = "$replyQuiz"
				+ quiz.replace("&", "").replace(" ", "");

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr" };
		val files = new String[] {
				"import_quiz_"
						+ quiz.toLowerCase().replace("&", "_")
								.replace(" ", "_") + "_DE.txt",
				"import_quiz_"
						+ quiz.toLowerCase().replace("&", "_")
								.replace(" ", "_") + "_FR.txt" };

		for (int i = 0; i < fields.length; i++) {
			fillValuesTable(values, PATH, files[i], fields[i]);
		}

		val countGiver = fields[0];

		MonitoringMessageGroup monitoringMessageGroupQuiz = null;
		MonitoringMessageGroup monitoringMessageGroupQuizNoReply = null;
		MonitoringMessageGroup monitoringMessageGroupQuizReply = null;

		// Check content
		for (int i = 0; i < values.get(countGiver).size(); i++) {
			String element = "";
			for (final String field : fields) {
				element += "\n" + field + ": "
						+ values.get(field).get(i).replaceAll("\n", " // ");
			}
			log.debug(">> Element {}: {}", i, element);

			// Perform operation
			if (PERFORM_CHANGE) {
				switch (i) {
					case 0:
						monitoringMessageGroupQuiz = iam
								.monitoringMessageGroupCreate("Quiz " + quiz,
										interventionId);

						iam.monitoringMessageGroupSetMessagesExceptAnswer(
								monitoringMessageGroupQuiz, true);
						db.saveModelObject(monitoringMessageGroupQuiz);

						monitoringMessageGroupQuizNoReply = iam
								.monitoringMessageGroupCreate("Quiz " + quiz
										+ " (NR)", interventionId);
						db.saveModelObject(monitoringMessageGroupQuizNoReply);

						monitoringMessageGroupQuizReply = iam
								.monitoringMessageGroupCreate("Quiz " + quiz
										+ " (R)", interventionId);
						db.saveModelObject(monitoringMessageGroupQuizReply);

						MonitoringMessage monitoringMessage = iam
								.monitoringMessageCreate(monitoringMessageGroupQuiz
										.getId());

						LString lString = fillLString(i, values, fields);
						monitoringMessage.setTextWithPlaceholders(lString);
						monitoringMessage
								.setStoreValueToVariableWithName(quizReplyVariable);

						db.saveModelObject(monitoringMessage);

						break;
					default:
						monitoringMessage = null;
						if (i == 1 || i == 5) {
							monitoringMessage = iam
									.monitoringMessageCreate(monitoringMessageGroupQuizNoReply
											.getId());
						} else if (i > 1 && i < 5 || i > 5) {
							monitoringMessage = iam
									.monitoringMessageCreate(monitoringMessageGroupQuizReply
											.getId());
						}

						lString = fillLString(i, values, fields);
						monitoringMessage.setTextWithPlaceholders(lString);

						db.saveModelObject(monitoringMessage);

						if (i > 1 && i < 5 || i > 5) {
							val monitoringMessageRule = iam
									.monitoringMessageRuleCreate(monitoringMessage
											.getId());

							monitoringMessageRule
									.setComment("Check quiz result");
							monitoringMessageRule
									.setRuleWithPlaceholders(quizReplyVariable);
							monitoringMessageRule
									.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
							monitoringMessageRule
									.setRuleComparisonTermWithPlaceholders("");

							db.saveModelObject(monitoringMessageRule);
						}

						break;
				}
			}
		}

		log.debug(">> Finished {} operation", operationName);
	}

	@SuppressWarnings("unused")
	private void createExtraMessages() throws IOException {
		val operationName = "create extra messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a0aec69afa06a6854b10a7");

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr", "counter" };
		val files = new String[] { "import_extra_messages_DE.txt",
				"import_extra_messages_FR.txt",
				"import_extra_messages_Counter.txt" };

		for (int i = 0; i < fields.length; i++) {
			fillValuesTable(values, PATH, files[i], fields[i]);
		}

		val countGiver = fields[0];

		// Check content
		for (int i = 0; i < values.get(countGiver).size(); i++) {
			String element = "";
			for (final String field : fields) {
				element += "\n" + field + ": "
						+ values.get(field).get(i).replaceAll("\n", " // ");
			}
			log.debug(">> Element {}: {}", i, element);

			// Perform operation
			if (PERFORM_CHANGE) {
				val monitoringMessage = iam
						.monitoringMessageCreate(monitoringMessageGroupId);

				val lString = fillLString(i, values, fields);
				monitoringMessage.setTextWithPlaceholders(lString);

				db.saveModelObject(monitoringMessage);

				val monitoringMessageRule = iam
						.monitoringMessageRuleCreate(monitoringMessage.getId());

				monitoringMessageRule
						.setComment("Date difference to $participation_extra_goal is "
								+ values.get(fields[2]).get(i));
				monitoringMessageRule
						.setRuleWithPlaceholders("$participation_extra_goal");
				monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.DATE_DIFFERENCE_VALUE_EQUALS);
				monitoringMessageRule
						.setRuleComparisonTermWithPlaceholders(values.get(
								fields[2]).get(i));

				db.saveModelObject(monitoringMessageRule);
			}
		}

		log.debug(">> Finished {} operation", operationName);
	}

	private void createStageMessages() throws IOException {
		val operationName = "create stage messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a633ec9afa06572deab1a5");

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr", "before", "after" };
		val files = new String[] { "import_stage_DE.txt",
				"import_stage_FR.txt", "import_stage_before.txt",
				"import_stage_after.txt" };

		for (int i = 0; i < fields.length; i++) {
			fillValuesTable(values, PATH, files[i], fields[i]);
		}

		val countGiver = fields[0];

		// Check content
		for (int i = 0; i < values.get(countGiver).size(); i++) {
			String element = "";
			for (final String field : fields) {
				element += "\n" + field + ": "
						+ values.get(field).get(i).replaceAll("\n", " // ");
			}
			log.debug(">> Element {}: {}", i, element);

			// Perform operation
			if (PERFORM_CHANGE) {
				val monitoringMessage = iam
						.monitoringMessageCreate(monitoringMessageGroupId);

				val lString = fillLString(i, values, fields);
				monitoringMessage.setTextWithPlaceholders(lString);

				db.saveModelObject(monitoringMessage);

				MonitoringMessageRule monitoringMessageRule = iam
						.monitoringMessageRuleCreate(monitoringMessage.getId());

				monitoringMessageRule.setComment("Check $stageCurrent matches "
						+ values.get(fields[2]).get(i));
				monitoringMessageRule.setRuleWithPlaceholders("$stageCurrent");
				monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.TEXT_VALUE_MATCHES_REGULAR_EXPRESSION);
				monitoringMessageRule
						.setRuleComparisonTermWithPlaceholders(values.get(
								fields[2]).get(i));

				db.saveModelObject(monitoringMessageRule);

				monitoringMessageRule = iam
						.monitoringMessageRuleCreate(monitoringMessage.getId());

				monitoringMessageRule.setComment("Check $stageNew matches "
						+ values.get(fields[3]).get(i));
				monitoringMessageRule.setRuleWithPlaceholders("$stageNew");
				monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.TEXT_VALUE_MATCHES_REGULAR_EXPRESSION);
				monitoringMessageRule
						.setRuleComparisonTermWithPlaceholders(values.get(
								fields[3]).get(i));

				db.saveModelObject(monitoringMessageRule);
			}
		}

		log.debug(">> Finished {} operation", operationName);
	}

	/**
	 * Fills tables values
	 *
	 * @param values
	 * @param path
	 * @param filename
	 * @param field
	 * @throws IOException
	 */
	private void fillValuesTable(final Hashtable<String, List<String>> values,
			final String path, final String filename, final String field)
			throws IOException {
		// Read file
		final FileReader fileReader = new FileReader(new File(path, filename));
		final BufferedReader bufferedReader = new BufferedReader(fileReader);
		final List<String> lines = new ArrayList<String>();
		String lineBuffer = null;
		String readLine = null;
		while ((readLine = bufferedReader.readLine()) != null) {
			if (readLine.equals("")) {
				lines.add(lineBuffer);
				lineBuffer = null;
			} else {
				if (lineBuffer == null) {
					lineBuffer = readLine;
				} else {
					lineBuffer += "\n" + readLine;
				}
			}
		}
		if (lineBuffer != null) {
			lines.add(lineBuffer);
		}
		bufferedReader.close();

		// Add values to hashtable
		values.put(field, lines);
	}

	/*
	 * Helper methods
	 */
	/**
	 * Fills {@link LString} with appropriate values from values and fields
	 *
	 * @param i
	 * @param values
	 * @param fields
	 * @return
	 */
	private LString fillLString(final int i,
			final Hashtable<String, List<String>> values, final String[] fields) {
		val lString = new LString();
		lString.set(Constants.getInterventionLocales()[0], values
				.get(fields[0]).get(i));
		lString.set(Constants.getInterventionLocales()[1], values
				.get(fields[1]).get(i));
		return lString;
	}

	/*
	 * Private class for hacking database manager service
	 */
	private class HackingDatabaseManager extends
			AbstractModelObjectAccessService {

		@Override
		public void saveModelObject(final ModelObject modelObject) {
			super.saveModelObject(modelObject);
			createdModelObjects.add(modelObject);
		}
	}
}