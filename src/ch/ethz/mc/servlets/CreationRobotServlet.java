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
@WebServlet(displayName = "Creation Robot Interface", urlPatterns = "/hacking", asyncSupported = true, loadOnStartup = 4)
@Log4j2
public class CreationRobotServlet extends HttpServlet {
	private final boolean								ACTIVE				= false;
	private final String								PATH				= "/Users/andreas/Dropbox/Dokumente/Cloud/MobileCoach/Development/Cheating";

	private MC											mc;
	private static CreationRobotDatabaseManager			db					= null;

	private InterventionAdministrationManagerService	iam;

	final List<ModelObject>								createdModelObjects	= new ArrayList<ModelObject>();

	@Override
	public void init(final ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		if (!ACTIVE) {
			log.debug("Creation robot servlet is disabled");
			return;
		}

		// Only start servlet if context is ready
		if (!MC.getInstance().isReady()) {
			log.error("Servlet {} can't be started. Context is not ready!",
					this.getClass());
			throw new ServletException("Context is not ready!");
		}

		mc = MC.getInstance();
		db = new CreationRobotDatabaseManager();

		iam = mc.getInterventionAdministrationManagerService();

		log.info("Initializing servlet...");

		// Perform tasks
		log.debug("STARTING CREATION ROBOT...");
		try {
			runRobotTasks();
		} catch (final Exception e) {
			log.error("ERROR at bulk operation: " + e.getMessage());
			log.error(e.getStackTrace().toString());
		}
		log.debug("CREATION ROBOT DONE.");

		log.info("Servlet initialized.");

	}

	/*
	 * ROBOT TASKS START HERE
	 */
	private void runRobotTasks() {
		// TODO for ROBOT TASKS (OPTIONAL): Robot tasks performed at startup can
		// be
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
			// createStageMessages();
			// createdModelObjects.clear();
			// createStageEmotionMessages1();
			// createdModelObjects.clear();
			// createStageEmotionMessages2();
			// createdModelObjects.clear();
			// createSmokestatusAndBingeMessages();
			// createdModelObjects.clear();
			// createCPDCPWMessages();
			// createdModelObjects.clear();
			// createImpinCravMessages();
			// createdModelObjects.clear();
			// createImpinCravReplyMessages();
			// createdModelObjects.clear();
			// createImpinSozMessages();
			// createdModelObjects.clear();
			// createImpinSozReplyMessages();
			// createdModelObjects.clear();
			// createImpinStressMessages();
			// createdModelObjects.clear();
			// createImpinStressReplyMessages();
			// createdModelObjects.clear();
			// createBaselineMessagesBeXYZ();
			// createdModelObjects.clear();
			createGenericBaselineMessages();
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
	private void createImpinCravMessages() throws IOException {
		val operationName = "impin crav messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a8819c9afa06781b80a96c");
		val header = new String[] {
				"r4l Frage\nAntworte bis Do (19h) mit Ja oder Nein und sammle Credits!\n\nHast du in der letzten Zeit folgende Strategie angewendet?\n",
				"Question r4l\nRéponds par oui ou par non avant JE 19h et gagne des crédits!\n\nAs-tu appliqué les stratégies suivantes dernièrement?\n" };

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr" };
		val files = new String[] { "import_impin_crav_DE.txt",
				"import_impin_crav_FR.txt" };

		for (int i = 0; i < fields.length; i++) {
			fillValuesTable(values, PATH, files[i], fields[i]);
		}

		val countGiver = fields[0];

		// Check content
		int impin = 1;
		int impinValueCounter = 1;
		for (int i = 0; i < values.get(countGiver).size(); i++) {
			String element = "";
			for (final String field : fields) {
				element += "\n" + field + ": "
						+ values.get(field).get(i).replaceAll("\n", " // ");
			}
			log.debug(">> Element {}: {}", i, element);

			// Perform operation
			if (PERFORM_CHANGE) {
				int j = 1;
				for (val week : new String[] { "3", "14" }) {
					val monitoringMessage = iam
							.monitoringMessageCreate(monitoringMessageGroupId);

					val lString = fillLString(i, values, fields, header);
					monitoringMessage.setTextWithPlaceholders(lString);

					db.saveModelObject(monitoringMessage);

					MonitoringMessageRule monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Participation week is "
							+ week);
					monitoringMessageRule
							.setRuleWithPlaceholders("$participationWeek");
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders(week);

					db.saveModelObject(monitoringMessageRule);

					monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Impin Top " + j + " is "
							+ impin);
					monitoringMessageRule
							.setRuleWithPlaceholders("$impinCravTop" + j);
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders(String
									.valueOf(impin));

					db.saveModelObject(monitoringMessageRule);

					monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Impin " + impin
							+ " value is " + impinValueCounter);
					monitoringMessageRule.setRuleWithPlaceholders("$impin"
							+ impin + "T0");
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders(String
									.valueOf(impinValueCounter));

					db.saveModelObject(monitoringMessageRule);

					j++;
				}
			}

			impinValueCounter++;

			if (impinValueCounter == 4) {
				impinValueCounter = 1;
				impin++;
			}
		}

		log.debug(">> Finished {} operation", operationName);
	}

	@SuppressWarnings("unused")
	private void createImpinStressMessages() throws IOException {
		val operationName = "impin stress messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a89f2e9afa0679d0b9e0ca");
		val header = new String[] {
				"r4l Frage\nAntworte bis Do (19h) mit Ja oder Nein und sammle Credits!\n\nHast du in der letzten Zeit folgende Strategie angewendet?\n",
				"Question r4l\nRéponds par oui ou par non avant JE 19h et gagne des crédits!\n\nAs-tu appliqué les stratégies suivantes dernièrement?\n" };
		val impinName = new String[] { "1a", "1b", "1c", "1d", "2a", "2b" };

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr" };
		val files = new String[] { "import_impin_stress_DE.txt",
				"import_impin_stress_FR.txt" };

		for (int i = 0; i < fields.length; i++) {
			fillValuesTable(values, PATH, files[i], fields[i]);
		}

		val countGiver = fields[0];

		// Check content
		int impin = 0;
		int impinValueCounter = 1;
		for (int i = 0; i < values.get(countGiver).size(); i++) {
			String element = "";
			for (final String field : fields) {
				element += "\n" + field + ": "
						+ values.get(field).get(i).replaceAll("\n", " // ");
			}
			log.debug(">> Element {}: {}", i, element);

			// Perform operation
			if (PERFORM_CHANGE) {
				int j = 1;
				for (val week : new String[] { "8", "3|21" }) {
					val monitoringMessage = iam
							.monitoringMessageCreate(monitoringMessageGroupId);

					val lString = fillLString(i, values, fields, header);
					monitoringMessage.setTextWithPlaceholders(lString);

					db.saveModelObject(monitoringMessage);

					MonitoringMessageRule monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Participation week is "
							+ week);
					monitoringMessageRule
							.setRuleWithPlaceholders("$participationWeek");
					if (week.equals("8")) {
						monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					} else {
						monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.TEXT_VALUE_MATCHES_REGULAR_EXPRESSION);
					}
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders(week);

					db.saveModelObject(monitoringMessageRule);

					monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Impin Top " + j + " is "
							+ (impin + 1));
					monitoringMessageRule.setRuleWithPlaceholders("$impinTop"
							+ j);
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders(String
									.valueOf(impin + 1));

					db.saveModelObject(monitoringMessageRule);

					monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Impin "
							+ impinName[impin] + " value is "
							+ impinValueCounter);
					monitoringMessageRule
					.setRuleWithPlaceholders("$impin_stress"
							+ impinName[impin] + "T0");
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders(String
									.valueOf(impinValueCounter));

					db.saveModelObject(monitoringMessageRule);

					j++;
				}
			}

			impinValueCounter++;

			if (impinValueCounter == 4) {
				impinValueCounter = 1;
				impin++;
			}
		}

		log.debug(">> Finished {} operation", operationName);
	}

	@SuppressWarnings("unused")
	private void createImpinSozMessages() throws IOException {
		val operationName = "impin soz messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a88cf89afa0678e31ca3b0");
		val header = new String[] {
				"r4l Frage\nAntworte bis Do (19h) mit Ja oder Nein und sammle Credits!\n\nHast du in der letzten Woche folgendes getan?\n",
				"Question r4l\nRéponds par oui ou par non avant JE 19h et gagne des crédits!\n\nAs-tu fait ce qui suit dernièrement?\n" };

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr" };
		val files = new String[] { "import_impin_soz_DE.txt",
				"import_impin_soz_FR.txt" };

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

				val lString = fillLString(i, values, fields, header);
				monitoringMessage.setTextWithPlaceholders(lString);

				db.saveModelObject(monitoringMessage);

				final MonitoringMessageRule monitoringMessageRule = iam
						.monitoringMessageRuleCreate(monitoringMessage.getId());

				monitoringMessageRule.setComment("$sozzTop is " + (i + 1));
				monitoringMessageRule.setRuleWithPlaceholders("$sozzTop");
				monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
				monitoringMessageRule
						.setRuleComparisonTermWithPlaceholders(String
								.valueOf(i + 1));

				db.saveModelObject(monitoringMessageRule);
			}
		}

		log.debug(">> Finished {} operation", operationName);
	}

	@SuppressWarnings("unused")
	private void createImpinCravReplyMessages() throws IOException {
		val operationName = "impin crav reply messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a88b0d9afa0678cbad1dfd");

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr" };
		val files = new String[] { "import_impin_crav_reply_DE.txt",
				"import_impin_crav_reply_FR.txt" };

		for (int i = 0; i < fields.length; i++) {
			fillValuesTable(values, PATH, files[i], fields[i]);
		}

		val countGiver = fields[0];

		// Check content
		int impin = 1;
		int impinValueCounter = 1;
		int situationCounter = 1;
		int ruleReplyCounter = 1;
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

				monitoringMessageRule.setComment("Current top is impin crav "
						+ impin);
				monitoringMessageRule
						.setRuleWithPlaceholders("$impinCravCurrent");
				monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
				monitoringMessageRule
						.setRuleComparisonTermWithPlaceholders(String
								.valueOf(impin));

				db.saveModelObject(monitoringMessageRule);

				monitoringMessageRule = iam
						.monitoringMessageRuleCreate(monitoringMessage.getId());

				monitoringMessageRule.setComment("$impin" + impin + "T0 is "
						+ situationCounter);
				monitoringMessageRule.setRuleWithPlaceholders("$impin" + impin
						+ "T0");
				monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
				monitoringMessageRule
						.setRuleComparisonTermWithPlaceholders(String
								.valueOf(situationCounter));

				db.saveModelObject(monitoringMessageRule);

				monitoringMessageRule = iam
						.monitoringMessageRuleCreate(monitoringMessage.getId());

				monitoringMessageRule.setComment("$replyImpinCrav is "
						+ (ruleReplyCounter == 1 ? "yes" : "no"));
				monitoringMessageRule
						.setRuleWithPlaceholders("$replyImpinCrav");
				monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
				monitoringMessageRule
						.setRuleComparisonTermWithPlaceholders(String
								.valueOf(ruleReplyCounter));

				db.saveModelObject(monitoringMessageRule);
			}

			ruleReplyCounter++;

			if (ruleReplyCounter == 3) {
				ruleReplyCounter = 1;
				situationCounter++;
			}

			if (situationCounter == 4) {
				situationCounter = 1;
			}

			impinValueCounter++;

			if (impinValueCounter == 7) {
				impinValueCounter = 1;
				impin++;
			}
		}

		log.debug(">> Finished {} operation", operationName);
	}

	@SuppressWarnings("unused")
	private void createImpinStressReplyMessages() throws IOException {
		val operationName = "impin stress reply messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a88d1b9afa0678e31ca3b3");
		val impinName = new String[] { "1a", "1b", "1c", "1d", "2a", "2b" };

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr" };
		val files = new String[] { "import_impin_stress_reply_DE.txt",
				"import_impin_stress_reply_FR.txt" };

		for (int i = 0; i < fields.length; i++) {
			fillValuesTable(values, PATH, files[i], fields[i]);
		}

		val countGiver = fields[0];

		// Check content
		int impin = 1;
		int impinValueCounter = 1;
		int situationCounter = 1;
		int ruleReplyCounter = 1;
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

				monitoringMessageRule.setComment("Current top is impin stress "
						+ impin);
				monitoringMessageRule
						.setRuleWithPlaceholders("$impinStressCurrent");
				monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
				monitoringMessageRule
						.setRuleComparisonTermWithPlaceholders(String
								.valueOf(impin));

				db.saveModelObject(monitoringMessageRule);

				monitoringMessageRule = iam
						.monitoringMessageRuleCreate(monitoringMessage.getId());

				monitoringMessageRule.setComment("$impin_stress"
						+ impinName[impin - 1] + "T0 is " + situationCounter);
				monitoringMessageRule.setRuleWithPlaceholders("$impin_stress"
						+ impinName[impin - 1] + "T0");
				monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
				monitoringMessageRule
						.setRuleComparisonTermWithPlaceholders(String
								.valueOf(situationCounter));

				db.saveModelObject(monitoringMessageRule);

				monitoringMessageRule = iam
						.monitoringMessageRuleCreate(monitoringMessage.getId());

				monitoringMessageRule.setComment("$replyImpinStress is "
						+ (ruleReplyCounter == 1 ? "yes" : "no"));
				monitoringMessageRule
						.setRuleWithPlaceholders("$replyImpinStress");
				monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
				monitoringMessageRule
						.setRuleComparisonTermWithPlaceholders(String
								.valueOf(ruleReplyCounter));

				db.saveModelObject(monitoringMessageRule);
			}

			ruleReplyCounter++;

			if (ruleReplyCounter == 3) {
				ruleReplyCounter = 1;
				situationCounter++;
			}

			if (situationCounter == 4) {
				situationCounter = 1;
			}

			impinValueCounter++;

			if (impinValueCounter == 7) {
				impinValueCounter = 1;
				impin++;
			}
		}

		log.debug(">> Finished {} operation", operationName);
	}

	@SuppressWarnings("unused")
	private void createImpinSozReplyMessages() throws IOException {
		val operationName = "impin soz reply messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a896cb9afa067959b8819b");

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr" };
		val files = new String[] { "import_impin_soz_reply_DE.txt",
		"import_impin_soz_reply_FR.txt" };

		for (int i = 0; i < fields.length; i++) {
			fillValuesTable(values, PATH, files[i], fields[i]);
		}

		val countGiver = fields[0];

		// Check content
		int impin = 1;
		int ruleReplyCounter = 1;
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

				monitoringMessageRule.setComment("Top is $sozz" + impin + "T0");
				monitoringMessageRule.setRuleWithPlaceholders("$sozzTop");
				monitoringMessageRule
				.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
				monitoringMessageRule
				.setRuleComparisonTermWithPlaceholders("$sozz" + impin
						+ "T0");

				db.saveModelObject(monitoringMessageRule);

				monitoringMessageRule = iam
						.monitoringMessageRuleCreate(monitoringMessage.getId());

				monitoringMessageRule.setComment("$replyImpinSozz is "
						+ (ruleReplyCounter == 1 ? "yes" : "no"));
				monitoringMessageRule
				.setRuleWithPlaceholders("$replyImpinSozz");
				monitoringMessageRule
				.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
				monitoringMessageRule
				.setRuleComparisonTermWithPlaceholders(String
						.valueOf(ruleReplyCounter));

				db.saveModelObject(monitoringMessageRule);
			}

			ruleReplyCounter++;

			if (ruleReplyCounter == 3) {
				ruleReplyCounter = 1;
				impin++;
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

	@SuppressWarnings("unused")
	private void createBaselineMessagesBeXYZ() throws IOException {
		val operationName = "create baseline messages beXYZ";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a8ba349afa0679e72a0bfe");

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr", "week" };
		val files = new String[] { "import_baseline_behealthy_DE.txt",
				"import_baseline_behealthy_FR.txt",
		"import_baseline_behealthy_week.txt" };

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

				monitoringMessageRule.setComment("Be You only in week "
						+ values.get(fields[2]).get(i));
				monitoringMessageRule
				.setRuleWithPlaceholders("$participationWeek");
				monitoringMessageRule
				.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
				monitoringMessageRule
				.setRuleComparisonTermWithPlaceholders(values.get(
						fields[2]).get(i));

				db.saveModelObject(monitoringMessageRule);
			}
		}

		log.debug(">> Finished {} operation", operationName);
	}

	@SuppressWarnings("unused")
	private void createGenericBaselineMessages() throws IOException {
		val operationName = "create generic baseline messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a8bb4d9afa0679e72a0c07");
		val withRule = false;

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr", "variable", "value" };
		val files = new String[] { "import_baseline_generic_DE.txt",
				"import_baseline_generic_FR.txt",
				"import_baseline_generic_variable.txt",
				"import_baseline_generic_value.txt" };

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

				if (withRule) {
					val monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Check if  "
							+ values.get(fields[2]).get(i) + " is "
							+ values.get(fields[3]).get(i));
					monitoringMessageRule.setRuleWithPlaceholders(values.get(
							fields[2]).get(i));
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders(values.get(
									fields[3]).get(i));

					db.saveModelObject(monitoringMessageRule);
				}
			}
		}

		log.debug(">> Finished {} operation", operationName);
	}

	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
	private void createSmokestatusAndBingeMessages() throws IOException {
		val operationName = "create smoke status and  binge messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a714759afa0666c4f3bdbe");

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr", "compare" };
		val files = new String[] { "import_ssb_DE.txt", "import_ssb_FR.txt",
				"import_ssb_compare.txt" };

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

				final MonitoringMessageRule monitoringMessageRule = iam
						.monitoringMessageRuleCreate(monitoringMessage.getId());

				monitoringMessageRule
						.setComment("Check $replySmokestatusAndBinge matches "
								+ values.get(fields[2]).get(i));
				monitoringMessageRule
						.setRuleWithPlaceholders("$replySmokestatusAndBinge");
				monitoringMessageRule
						.setRuleEquationSign(RuleEquationSignTypes.TEXT_VALUE_MATCHES_REGULAR_EXPRESSION);
				monitoringMessageRule
						.setRuleComparisonTermWithPlaceholders(values.get(
								fields[2]).get(i));

				db.saveModelObject(monitoringMessageRule);
			}
		}

		log.debug(">> Finished {} operation", operationName);
	}

	@SuppressWarnings("unused")
	private void createCPDCPWMessages() throws IOException {
		val operationName = "create smoke status and  binge messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId("57a728fc9afa066744564c33");
		int T = 0;
		val runs = 2;
		val participationWeeks = new String[] { "3", "14" };

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr", "var" };
		val files = new String[] { "import_cpdcpw_DE.txt",
				"import_cpdcpw_FR.txt", "import_cpdcpw_variable.txt" };

		for (int i = 0; i < fields.length; i++) {
			fillValuesTable(values, PATH, files[i], fields[i]);
		}

		val countGiver = fields[0];

		for (int j = 0; j < runs; j++) {
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
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Check for week "
							+ participationWeeks[j]);
					monitoringMessageRule
							.setRuleWithPlaceholders("$participationWeek");
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders(participationWeeks[j]);

					db.saveModelObject(monitoringMessageRule);

					switch (i) {
						case 0:
						case 1:
						case 8:
						case 9:
							monitoringMessageRule = iam
									.monitoringMessageRuleCreate(monitoringMessage
											.getId());

							monitoringMessageRule.setComment("Check "
									+ values.get("var").get(i)
									+ "New  more than 20% smaller than "
									+ values.get("var").get(i) + "T" + T);
							monitoringMessageRule
									.setRuleWithPlaceholders(values.get("var")
											.get(i) + "New");
							monitoringMessageRule
									.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_IS_SMALLER_OR_EQUAL_THAN);
							monitoringMessageRule
									.setRuleComparisonTermWithPlaceholders(values
											.get("var").get(i)
											+ "T"
											+ T
											+ "-"
											+ values.get("var").get(i)
											+ "T"
											+ T + "*0.2");

							db.saveModelObject(monitoringMessageRule);

							break;
						case 4:
						case 5:
						case 12:
						case 13:
							monitoringMessageRule = iam
									.monitoringMessageRuleCreate(monitoringMessage
											.getId());

							monitoringMessageRule.setComment("Check "
									+ values.get("var").get(i)
									+ "New  more than 20% smaller than "
									+ values.get("var").get(i) + "T0");
							monitoringMessageRule
									.setRuleWithPlaceholders(values.get("var")
											.get(i) + "New");
							monitoringMessageRule
									.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_IS_SMALLER_OR_EQUAL_THAN);
							monitoringMessageRule
									.setRuleComparisonTermWithPlaceholders(values
											.get("var").get(i)
											+ "T0-"
											+ values.get("var").get(i)
											+ "T0*0.2");

							db.saveModelObject(monitoringMessageRule);

							break;
						case 2:
						case 3:
						case 10:
						case 11:
							monitoringMessageRule = iam
									.monitoringMessageRuleCreate(monitoringMessage
											.getId());

							monitoringMessageRule.setComment("Check "
									+ values.get("var").get(i)
									+ "New  more than 20% bigger than "
									+ values.get("var").get(i) + "T" + T);
							monitoringMessageRule
									.setRuleWithPlaceholders(values.get("var")
											.get(i) + "New");
							monitoringMessageRule
									.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_IS_BIGGER_OR_EQUAL_THAN);
							monitoringMessageRule
									.setRuleComparisonTermWithPlaceholders(values
											.get("var").get(i)
											+ "T"
											+ T
											+ "+"
											+ values.get("var").get(i)
											+ "T"
											+ T + "*0.2");

							db.saveModelObject(monitoringMessageRule);

							break;
						case 6:
						case 7:
						case 14:
						case 15:
							monitoringMessageRule = iam
									.monitoringMessageRuleCreate(monitoringMessage
											.getId());

							monitoringMessageRule.setComment("Check "
									+ values.get("var").get(i)
									+ "New  more than 20% bigger than "
									+ values.get("var").get(i) + "T0");
							monitoringMessageRule
									.setRuleWithPlaceholders(values.get("var")
											.get(i) + "New");
							monitoringMessageRule
									.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_IS_BIGGER_OR_EQUAL_THAN);
							monitoringMessageRule
									.setRuleComparisonTermWithPlaceholders(values
											.get("var").get(i)
											+ "T0+"
											+ values.get("var").get(i)
											+ "T0*0.2");

							db.saveModelObject(monitoringMessageRule);

							break;
					}
				}
			}
			T++;
		}

		log.debug(">> Finished {} operation", operationName);
	}

	@SuppressWarnings("unused")
	private void createStageEmotionMessages1() throws IOException {
		val operationName = "create stage emotion messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId[] {
				new ObjectId("57a6feaf9afa0664e2089ca4"),
				new ObjectId("57a6feb29afa0664e2089ca5") };
		int T = 1;
		val runs = 2;

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr", "v1", "v1_l", "v1_h", "v2",
				"v2_l", "v2_h" };
		val files = new String[] { "import_stage_emo_DE.txt",
				"import_stage_emo_FR.txt", "import_stage_emo_variable_1.txt",
				"import_stage_emo_lower_1.txt",
				"import_stage_emo_higher_1.txt",
				"import_stage_emo_variable_2.txt",
				"import_stage_emo_lower_2.txt", "import_stage_emo_higher_2.txt" };

		for (int i = 0; i < fields.length; i++) {
			fillValuesTable(values, PATH, files[i], fields[i]);
		}

		val countGiver = fields[0];

		for (int j = 0; j < runs; j++) {
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
							.monitoringMessageCreate(monitoringMessageGroupId[j]);

					val lString = fillLString(i, values, fields);
					monitoringMessage.setTextWithPlaceholders(lString);

					db.saveModelObject(monitoringMessage);

					MonitoringMessageRule monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Check former value "
							+ values.get("v1").get(i) + (T - 1) + " in range "
							+ values.get("v1_h").get(i) + "-"
							+ values.get("v1_l").get(i));
					monitoringMessageRule.setRuleWithPlaceholders("inrange("
							+ values.get("v1").get(i) + (T - 1) + ","
							+ values.get("v1_h").get(i) + ","
							+ values.get("v1_l").get(i) + ")");
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders("1");

					db.saveModelObject(monitoringMessageRule);

					monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Check new value "
							+ values.get("v1").get(i) + T + " in range "
							+ values.get("v1_h").get(i) + "-"
							+ values.get("v1_l").get(i));
					monitoringMessageRule.setRuleWithPlaceholders("inrange("
							+ values.get("v1").get(i) + T + ","
							+ values.get("v1_h").get(i) + ","
							+ values.get("v1_l").get(i) + ")");
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders("1");

					db.saveModelObject(monitoringMessageRule);

					monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Check new value "
							+ values.get("v2").get(i) + T + " in range "
							+ values.get("v2_h").get(i) + "-"
							+ values.get("v2_l").get(i));
					monitoringMessageRule.setRuleWithPlaceholders("inrange("
							+ values.get("v2").get(i) + T + ","
							+ values.get("v2_h").get(i) + ","
							+ values.get("v2_l").get(i) + ")");
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders("1");

					db.saveModelObject(monitoringMessageRule);

				}
			}
			T++;
		}

		log.debug(">> Finished {} operation", operationName);
	}

	@SuppressWarnings("unused")
	private void createStageEmotionMessages2() throws IOException {
		val operationName = "create stage emotion messages";
		log.debug(">> Starting {} operation...", operationName);

		val PERFORM_CHANGE = false;
		val monitoringMessageGroupId = new ObjectId[] {
				new ObjectId("57a6feaf9afa0664e2089ca4"),
				new ObjectId("57a6feb29afa0664e2089ca5") };
		int T = 1;
		val runs = 2;

		// Load values
		val values = new Hashtable<String, List<String>>();

		val fields = new String[] { "de", "fr", "v1", "v1_l", "v1_h", "v2",
				"v2_l", "v2_h" };
		val files = new String[] { "import_stage_emo_DE.txt",
				"import_stage_emo_FR.txt", "import_stage_emo_variable_1.txt",
				"import_stage_emo_lower_1.txt",
				"import_stage_emo_higher_1.txt",
				"import_stage_emo_variable_2.txt",
				"import_stage_emo_lower_2.txt", "import_stage_emo_higher_2.txt" };

		for (int i = 0; i < fields.length; i++) {
			fillValuesTable(values, PATH, files[i], fields[i]);
		}

		val countGiver = fields[0];

		for (int j = 0; j < runs; j++) {
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
							.monitoringMessageCreate(monitoringMessageGroupId[j]);

					val lString = fillLString(i, values, fields);
					monitoringMessage.setTextWithPlaceholders(lString);

					db.saveModelObject(monitoringMessage);

					MonitoringMessageRule monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Check former value "
							+ values.get("v1").get(i) + (T - 1) + " in range "
							+ values.get("v1_h").get(i) + "-"
							+ values.get("v1_l").get(i));
					monitoringMessageRule.setRuleWithPlaceholders("inrange("
							+ values.get("v1").get(i) + (T - 1) + ","
							+ values.get("v1_h").get(i) + ","
							+ values.get("v1_l").get(i) + ")");
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders("1");

					db.saveModelObject(monitoringMessageRule);

					monitoringMessageRule = iam
							.monitoringMessageRuleCreate(monitoringMessage
									.getId());

					monitoringMessageRule.setComment("Check new value "
							+ values.get("v2").get(i) + T + " in range "
							+ values.get("v2_h").get(i) + "-"
							+ values.get("v2_l").get(i));
					monitoringMessageRule.setRuleWithPlaceholders("inrange("
							+ values.get("v2").get(i) + T + ","
							+ values.get("v2_h").get(i) + ","
							+ values.get("v2_l").get(i) + ")");
					monitoringMessageRule
							.setRuleEquationSign(RuleEquationSignTypes.CALCULATED_VALUE_EQUALS);
					monitoringMessageRule
							.setRuleComparisonTermWithPlaceholders("1");

					db.saveModelObject(monitoringMessageRule);
				}
			}
			T++;
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

	/**
	 * Fills {@link LString} with appropriate values from values and fields and
	 * the adds the given header before
	 *
	 * @param i
	 * @param values
	 * @param fields
	 * @return
	 */
	private LString fillLString(final int i,
			final Hashtable<String, List<String>> values,
			final String[] fields, final String[] header) {
		val lString = new LString();
		lString.set(Constants.getInterventionLocales()[0], header[0]
				+ values.get(fields[0]).get(i));
		lString.set(Constants.getInterventionLocales()[1], header[1]
				+ values.get(fields[1]).get(i));
		return lString;
	}

	/*
	 * Private class for hacking database manager service
	 */
	private class CreationRobotDatabaseManager extends
			AbstractModelObjectAccessService {

		@Override
		public void saveModelObject(final ModelObject modelObject) {
			super.saveModelObject(modelObject);
			createdModelObjects.add(modelObject);
		}
	}
}