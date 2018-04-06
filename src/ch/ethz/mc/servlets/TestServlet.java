package ch.ethz.mc.servlets;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;
import ch.ethz.mc.model.persistent.MonitoringMessageRule;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValueAccessTypes;
import ch.ethz.mc.model.persistent.types.InterventionVariableWithValuePrivacyTypes;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.tools.RuleEvaluator;
import ch.ethz.mc.tools.StringHelpers;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@WebServlet(displayName = "Testing Interface", urlPatterns = "/internal-test", asyncSupported = true, loadOnStartup = 3)
@Log4j2
public class TestServlet extends HttpServlet {
	private ServletOutputStream	servletOutputStream	= null;

	@SuppressWarnings("unused")
	private MC					mc;

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

		mc = MC.getInstance();

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
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	@Synchronized
	protected void doGet(final HttpServletRequest request,
			final HttpServletResponse response)
			throws ServletException, IOException {
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

	/*
	 * TESTCASES START HERE
	 */
	private void runTestcases() {
		// TODO TESTING Test cases can be defined here

		// stringTests();
		// dateCalculationTests();
		// languageStringSerializationTest();
		// ruleTests();
		// jsonRetrievalAmountTest();
	}

	@SuppressWarnings("unused")
	private void stringTests() {
		val s = "Hallo Welt :-) 12,3 11.2";
		log.debug(">>" + StringHelpers.cleanReceivedMessageString(s));
	}

	@SuppressWarnings("unused")
	private void dateCalculationTests() {
		final MonitoringMessageRule m1 = new MonitoringMessageRule(null, 0,
				"26.10.2015",
				RuleEquationSignTypes.CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_TRUE_IF_ZERO,
				"01.01.16", "");
		final val x1 = RuleEvaluator.evaluateRule(null, null, m1,
				new ArrayList<AbstractVariableWithValue>());
		log.debug(">> " + x1.getCalculatedRuleValue());

		final MonitoringMessageRule m2 = new MonitoringMessageRule(null, 0,
				"26.10.2015",
				RuleEquationSignTypes.CALCULATE_DATE_DIFFERENCE_IN_DAYS_AND_TRUE_IF_ZERO,
				"01.01.2016", "");
		final val x2 = RuleEvaluator.evaluateRule(null, null, m2,
				new ArrayList<AbstractVariableWithValue>());
		log.debug(">> " + x2.getCalculatedRuleValue());

		final MonitoringMessageRule m3 = new MonitoringMessageRule(null, 0,
				"16.11.", RuleEquationSignTypes.DATE_DIFFERENCE_VALUE_EQUALS,
				"0", "");
		final val x3 = RuleEvaluator.evaluateRule(null, null, m3,
				new ArrayList<AbstractVariableWithValue>());
		log.debug(">> " + x3.isRuleMatchesEquationSign());

		final MonitoringMessageRule m4 = new MonitoringMessageRule(null, 0,
				"16.11.2015",
				RuleEquationSignTypes.DATE_DIFFERENCE_VALUE_EQUALS, "0", "");
		final val x4 = RuleEvaluator.evaluateRule(null, null, m4,
				new ArrayList<AbstractVariableWithValue>());
		log.debug(">> " + x4.isRuleMatchesEquationSign());

		val variables = new ArrayList<AbstractVariableWithValue>();
		variables.add(new InterventionVariableWithValue(null,
				"$fieldWithValues", "5,42,3",
				InterventionVariableWithValuePrivacyTypes.PRIVATE,
				InterventionVariableWithValueAccessTypes.INTERNAL));
		final MonitoringMessageRule m = new MonitoringMessageRule(null, 0,
				"position(1,$fieldWithValues)",
				RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE,
				"", "");
		final val x = RuleEvaluator.evaluateRule(null, null, m, variables);
		log.debug(">> " + x.getCalculatedRuleValue());
	}

	@SuppressWarnings("unused")
	private void languageStringSerializationTest() {
		val l = new LString();
		l.set(Constants.getInterventionLocales()[1], "DEF");
		l.set(Constants.getInterventionLocales()[0], "ABC");
		l.set(Locale.JAPAN, "GHI");

		try {
			val objectMapper = new ObjectMapper();
			val stringWriter = new StringWriter();
			objectMapper.writeValue(stringWriter, l);
			log.debug(">> " + stringWriter.toString());

			val v = objectMapper.readValue(stringWriter.toString(),
					LString.class);
			log.debug(">> " + v.toString());
			log.debug(">> " + v.get(Constants.getInterventionLocales()[0]));
			log.debug(">> " + v.get(new Locale("de", "CH")));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void ruleTests() {
		val variables = new ArrayList<AbstractVariableWithValue>();
		variables.add(new InterventionVariableWithValue(null, "$sex", "2",
				InterventionVariableWithValuePrivacyTypes.PRIVATE,
				InterventionVariableWithValueAccessTypes.INTERNAL));
		variables.add(new InterventionVariableWithValue(null, "$auditGT0Digit",
				"2", InterventionVariableWithValuePrivacyTypes.PRIVATE,
				InterventionVariableWithValueAccessTypes.INTERNAL));
		variables.add(new InterventionVariableWithValue(null, "$alterkDigit",
				"3", InterventionVariableWithValuePrivacyTypes.PRIVATE,
				InterventionVariableWithValueAccessTypes.INTERNAL));
		variables.add(new InterventionVariableWithValue(null, "$impins", "7,3",
				InterventionVariableWithValuePrivacyTypes.PRIVATE,
				InterventionVariableWithValueAccessTypes.INTERNAL));
		variables.add(new InterventionVariableWithValue(null, "$time", "4.3",
				InterventionVariableWithValuePrivacyTypes.PRIVATE,
				InterventionVariableWithValueAccessTypes.INTERNAL));
		variables.add(new InterventionVariableWithValue(null, "$date", "1.2.",
				InterventionVariableWithValuePrivacyTypes.PRIVATE,
				InterventionVariableWithValueAccessTypes.INTERNAL));

		val rule1 = new MonitoringMessageRule(null, 0,
				"position($sex,position($auditGT0Digit,position($alterkDigit,26,16,15,24),position($alterkDigit,53,60,63,53),position($alterkDigit,21,25,23,24)),position($auditGT0Digit,position($alterkDigit,21,14,13,14),position($alterkDigit,47,45,52,52),position($alterkDigit,33,42,35,34)))",
				RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE,
				"", "");
		final val result1 = RuleEvaluator.evaluateRule(null, null, rule1,
				variables);
		log.debug(">> " + result1.getCalculatedRuleValue());

		val rule2 = new MonitoringMessageRule(null, 0, "first(5,3,2,7,2,4,3,5)",
				RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE,
				"", "");
		final val result2 = RuleEvaluator.evaluateRule(null, null, rule2,
				variables);
		log.debug(">> " + result2.getCalculatedRuleValue());

		val rule3 = new MonitoringMessageRule(null, 0,
				"second(5,3,2,7,2,4,3,5)",
				RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE,
				"", "");
		final val result3 = RuleEvaluator.evaluateRule(null, null, rule3,
				variables);
		log.debug(">> " + result3.getCalculatedRuleValue());
		final val result4 = RuleEvaluator.evaluateRule(null, null, rule3,
				variables);
		log.debug(">> " + result4.getCalculatedRuleValue());
		final val result5 = RuleEvaluator.evaluateRule(null, null, rule3,
				variables);
		log.debug(">> " + result5.getCalculatedRuleValue());

		val rule5 = new MonitoringMessageRule(null, 0, "third(5,3,2,7,2,4,3,5)",
				RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE,
				"", "");
		final val result6 = RuleEvaluator.evaluateRule(null, null, rule5,
				variables);
		log.debug(">> " + result6.getCalculatedRuleValue());

		val rule4 = new MonitoringMessageRule(null, 0,
				"position(4,5,3,2,7,2,4,3,5)",
				RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE,
				"", "");
		final val result7 = RuleEvaluator.evaluateRule(null, null, rule4,
				variables);
		log.debug(">> " + result7.getCalculatedRuleValue());

		val rule7 = new MonitoringMessageRule(null, 0, "7,3",
				RuleEquationSignTypes.CALCULATE_AMOUNT_OF_SELECT_MANY_VALUES,
				"", "");
		final val result8 = RuleEvaluator.evaluateRule(null, null, rule7,
				variables);
		log.debug(">> " + result8.getTextRuleValue());

		val rule8 = new MonitoringMessageRule(null, 0, "$impins",
				RuleEquationSignTypes.TEXT_VALUE_FROM_SELECT_MANY_AT_POSITION,
				"2", "");
		final val result9 = RuleEvaluator.evaluateRule(null, null, rule8,
				variables);
		log.debug(">> " + result9.getTextRuleValue());

		val rule9 = new MonitoringMessageRule(null, 0, "round(random())",
				RuleEquationSignTypes.CALCULATE_VALUE_BUT_RESULT_IS_ALWAYS_TRUE,
				"", "");
		final val result10 = RuleEvaluator.evaluateRule(null, null, rule9,
				variables);
		log.debug(">> " + result10.getTextRuleValue());

		val rule10 = new MonitoringMessageRule(null, 0, "3.5",
				RuleEquationSignTypes.CALCULATED_VALUE_EQUALS, "3,5", "");
		final val result11 = RuleEvaluator.evaluateRule(null, null, rule10,
				variables);
		log.debug(">> " + result11.getTextRuleValue());
		log.debug(
				"Exception is correct here! Comma required for several other formats.");

		val rule11 = new MonitoringMessageRule(null, 0, "ABC",
				RuleEquationSignTypes.TEXT_VALUE_EQUALS, "$time{#t} Uhr", "");
		final val result12 = RuleEvaluator.evaluateRule(null, null, rule11,
				variables);
		log.debug(">> " + result12.getTextRuleComparisonTermValue());

		val rule12 = new MonitoringMessageRule(null, 0, "ABC",
				RuleEquationSignTypes.TEXT_VALUE_EQUALS, "$date{#d} :-)", "");
		final val result13 = RuleEvaluator.evaluateRule(null, null, rule12,
				variables);
		log.debug(">> " + result13.getTextRuleComparisonTermValue());
	}

	@SuppressWarnings("unused")
	private void jsonRetrievalAmountTest() {
		val variables = new ArrayList<AbstractVariableWithValue>();
		variables.add(new InterventionVariableWithValue(null, "$foodDiary",
				"{\"createdAt\":\"2017-12-08T09:17:01.074Z\",\"mealType\":\"DRINK_FOOD_OTHER\",\"mealPlace\":\"HOME\",\"mealTime\":\"2017-12-08T09:17:01.074Z\",\"food\":[{\"brands\":[\"7 Up\",\"Sprite\",\"Elmer Citro\",\"Citron\",\"Fanta\",\"Orangina\",\"Sinalco\",\"Passaia\",\"Jarimba\",\"Rivella rot\",\"Rivella grün\",\"Farmer\",\"Flauder\",\"Ginger Ale\"],\"foodnameDE\":\"Softdrink (mit Zucker)\",\"shadowDE\":\"Süssgetränk (mit Zucker), Limonade (mit Zucker), Gazosa (mit Zucker)\",\"foodnameFR\":null,\"shadowFR\":null,\"foodnameIT\":null,\"shadowIT\":null,\"pyramidStage\":\"6.29\",\"category\":\"Alkoholfreie Getränke\",\"foodGroup\":\"Getränke, alkoholfrei\",\"recipeName\":\"Softdrink mit Geschmack\",\"class\":13,\"count\":130,\"units\":[{\"unitNameDE\":\"Milliliter\",\"unitId\":1},{\"unitNameDE\":\"Deziliter\",\"conversion\":{\"factor\":100,\"targetUnitId\":1},\"unitId\":2},{\"unitNameDE\":\"Liter\",\"conversion\":{\"factor\":1000,\"targetUnitId\":1},\"unitId\":3},{\"unitNameDE\":\"Glas\",\"unitId\":4,\"conversion\":{\"factor\":200,\"targetUnitId\":1}},{\"unitNameDE\":\"Flasche\",\"unitId\":6,\"conversion\":{\"factor\":500,\"targetUnitId\":1}},{\"unitId\":\"Dose\",\"unitNameDE\":\"Dose\",\"conversion\":{\"factor\":330,\"targetUnitId\":1}}],\"id\":18,\"amount\":{\"value\":500,\"unit\":{\"unitNameDE\":\"Milliliter\",\"unitId\":1}},\"pyramidInfo\":{\"pyramidLevel\":\"6\",\"portionSize\":250}},{\"brands\":[],\"foodnameDE\":\"Rohschinken\",\"shadowDE\":\"Schinken (roh)\",\"foodnameFR\":null,\"shadowFR\":null,\"foodnameIT\":null,\"shadowIT\":null,\"pyramidStage\":\"4.17\",\"category\":\"Fleisch, Fleischprodukte und Fleischersatz\",\"foodGroup\":\"Fleischprodukte & Wurstwaren\",\"recipeName\":\"Schinken, roh (CH)\",\"class\":7,\"count\":198,\"units\":[{\"unitNameDE\":\"Gramm\",\"unitId\":0},{\"unitNameDE\":\"Portion\",\"unitId\":13,\"conversion\":{\"factor\":30,\"targetUnitId\":0}},{\"unitNameDE\":\"Scheibe\",\"unitId\":17,\"conversion\":{\"factor\":15,\"targetUnitId\":0}}],\"id\":121,\"amount\":{\"value\":15,\"unit\":{\"unitNameDE\":\"Gramm\",\"unitId\":0}},\"pyramidInfo\":{\"pyramidLevel\":\"4b\",\"portionSize\":110}}]}",
				InterventionVariableWithValuePrivacyTypes.PRIVATE,
				InterventionVariableWithValueAccessTypes.INTERNAL));
		variables.add(new InterventionVariableWithValue(null, "$foodLength",
				".food.length()",
				InterventionVariableWithValuePrivacyTypes.PRIVATE,
				InterventionVariableWithValueAccessTypes.INTERNAL));
		variables.add(new InterventionVariableWithValue(null, "$food", ".food",
				InterventionVariableWithValuePrivacyTypes.PRIVATE,
				InterventionVariableWithValueAccessTypes.INTERNAL));

		val rule1 = new MonitoringMessageRule(null, 0, "$foodDiary",
				RuleEquationSignTypes.TEXT_VALUE_FROM_JSON_BY_JSON_PATH,
				"$food", "");
		final val result1 = RuleEvaluator.evaluateRule(null, null, rule1,
				variables);
		log.debug(">> " + result1.getTextRuleValue());

		val rule2 = new MonitoringMessageRule(null, 0, "$foodDiary",
				RuleEquationSignTypes.TEXT_VALUE_FROM_JSON_BY_JSON_PATH,
				"$foodLength", "");
		final val result2 = RuleEvaluator.evaluateRule(null, null, rule2,
				variables);
		log.debug(">> " + result2.getTextRuleValue());

		val rule3 = new MonitoringMessageRule(null, 0, "$foodDiary",
				RuleEquationSignTypes.TEXT_VALUE_FROM_JSON_BY_JSON_PATH,
				".food[0].foodnameDE", "");
		final val result3 = RuleEvaluator.evaluateRule(null, null, rule3,
				variables);
		log.debug(">> " + result3.getTextRuleValue());
	}
}