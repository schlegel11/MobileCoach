package ch.ethz.mc.services.internal;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.reflect.ReflectionObjectHandler;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.AbstractSerializableTable;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import lombok.Cleanup;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Creates a HTML-based report of an intervention
 *
 * @author Andreas Filler
 */
@Log4j2
public class ReportGeneratorService {
	private final Object					$lock;

	private static ReportGeneratorService	instance		= null;

	private final DatabaseManagerService	databaseManagerService;

	private final DefaultMustacheFactory	mustacheFactory;

	private final SimpleDateFormat			longDateFormat	= new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private ReportGeneratorService(
			final DatabaseManagerService databaseManagerService) {
		$lock = MC.getInstance();

		this.databaseManagerService = databaseManagerService;

		// Initialize template engine
		mustacheFactory = new DefaultMustacheFactory();
		mustacheFactory.setObjectHandler(new HTMLObjectHandler());
	}

	public static ReportGeneratorService start(
			final DatabaseManagerService databaseManagerService)
			throws Exception {
		if (instance == null) {
			instance = new ReportGeneratorService(databaseManagerService);
		}
		return instance;
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		log.info("Stopped.");
	}

	private class HTMLObjectHandler extends ReflectionObjectHandler {
		@Override
		public String stringify(final Object object) {
			if (object instanceof AbstractSerializableTable) {
				return ((AbstractSerializableTable) object).toTable();
			} else if (object instanceof List<?>) {
				val buffer = new StringBuffer();
				for (val item : (List<?>) object) {
					buffer.append(stringify(item) + "<br/>");
				}
				return buffer.toString();
			} else {
				return object.toString();
			}
		}
	}

	@Synchronized
	public File generateReport(final Intervention intervention,
			final String baseURL) {
		// Create temporary file
		File reportFile = null;
		try {
			reportFile = File.createTempFile("MC_", ".html", null);
			reportFile.deleteOnExit();
		} catch (final IOException e) {
			log.error("Could not create temp file: {}", e.getMessage());
			return null;
		}
		log.debug("Temporary file {} created", reportFile.getAbsoluteFile());

		// Collect variables
		val templateVariables = new HashMap<String, Object>();

		templateVariables.put("mediaObjectBaseURL", baseURL
				+ ImplementationConstants.FILE_STREAMING_SERVLET_PATH + "/");
		templateVariables.put("version", Constants.getVersion());
		templateVariables.put("timestamp",
				longDateFormat.format(new Date(System.currentTimeMillis())));

		// Intervention
		templateVariables.put("intervention", intervention);

		// Surveys
		val surveys = databaseManagerService.findModelObjects(
				ScreeningSurvey.class,
				Queries.SCREENING_SURVEY__BY_INTERVENTION,
				intervention.getId());

		val screeningSurveys = new ArrayList<ScreeningSurvey>();
		val intermediateSurveys = new ArrayList<ScreeningSurvey>();
		for (val survey : surveys) {
			if (survey.isIntermediateSurvey()) {
				intermediateSurveys.add(survey);
			} else {
				screeningSurveys.add(survey);
			}
		}

		templateVariables.put("screeningSurveys", screeningSurveys);
		templateVariables.put("intermediateSurveys", intermediateSurveys);

		// Monitoring Rules
		val rulesOnRootLevelIterable = databaseManagerService
				.findSortedModelObjects(MonitoringRule.class,
						Queries.MONITORING_RULE__BY_INTERVENTION_AND_PARENT,
						Queries.MONITORING_RULE__SORT_BY_ORDER_ASC,
						intervention.getId(), null);

		val monitoringRules = new ArrayList<MonitoringRule>();
		for (val monitoringRule : rulesOnRootLevelIterable) {
			monitoringRules.add(monitoringRule);
		}

		templateVariables.put("monitoringRules", monitoringRules);

		// Monitoring Message Groups and Messages
		val monitoringMessageGroupsIterable = databaseManagerService
				.findSortedModelObjects(MonitoringMessageGroup.class,
						Queries.MONITORING_MESSAGE_GROUP__BY_INTERVENTION,
						Queries.MONITORING_MESSAGE_GROUP__SORT_BY_ORDER_ASC,
						intervention.getId());

		val monitoringMessageGroups = new ArrayList<MonitoringMessageGroup>();
		for (val monitoringMessageGroup : monitoringMessageGroupsIterable) {
			monitoringMessageGroups.add(monitoringMessageGroup);
		}

		templateVariables.put("monitoringMessageGroups",
				monitoringMessageGroups);

		// Micro Dialogs
		val microDialogsIterable = databaseManagerService
				.findSortedModelObjects(MonitoringMessageGroup.class,
						Queries.MICRO_DIALOG__BY_INTERVENTION,
						Queries.MICRO_DIALOG__SORT_BY_ORDER_ASC,
						intervention.getId());

		val microDialogs = new ArrayList<MonitoringMessageGroup>();
		for (val microDialog : microDialogsIterable) {
			microDialogs.add(microDialog);
		}

		templateVariables.put("microDialogs", microDialogs);

		// Fill template
		try {
			@Cleanup
			val templateInputStream = ReportGeneratorService.class
					.getResourceAsStream("Report.template.html");
			@Cleanup
			val templateInputStreamReader = new InputStreamReader(
					templateInputStream, "UTF-8");
			val mustache = mustacheFactory.compile(templateInputStreamReader,
					"mustache.template");

			@Cleanup
			val fileOutputStream = new FileOutputStream(reportFile);
			@Cleanup
			val outputStreamWriter = new OutputStreamWriter(fileOutputStream);

			mustache.execute(outputStreamWriter, templateVariables);
		} catch (final Exception e) {
			log.error("Could not fill template: {}", e.getMessage());
		}

		return reportFile;
	}
}
