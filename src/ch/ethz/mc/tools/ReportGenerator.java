package ch.ethz.mc.tools;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import lombok.Cleanup;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.Intervention;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.reflect.ReflectionObjectHandler;

/**
 * Creates a HTML-based report of an intervention
 *
 * @author Andreas Filler
 */
@Log4j2
public class ReportGenerator {
	private final Object					$lock;

	private static ReportGenerator			instance		= null;

	private final DefaultMustacheFactory	mustacheFactory;

	private static SimpleDateFormat			longDateFormat	= new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private ReportGenerator() {
		$lock = MC.getInstance();

		// Initialize template engine
		mustacheFactory = new DefaultMustacheFactory();
		mustacheFactory.setObjectHandler(new HTMLObjectHandler());
	}

	public static ReportGenerator getInstance() {
		if (instance == null) {
			instance = new ReportGenerator();
		}
		return instance;
	}

	private class HTMLObjectHandler extends ReflectionObjectHandler {
		@Override
		public String stringify(final Object object) {
			if (object instanceof ModelObject) {
				return ((ModelObject) object).toMultiLineString();
			} else {
				return object.toString();
			}
		}
	}

	@Synchronized
	public File generateReport(final Intervention intervention) {
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

		templateVariables.put("intervention", intervention.getName());
		templateVariables.put("version", Constants.getVersion());
		templateVariables.put("timestamp",
				longDateFormat.format(new Date(System.currentTimeMillis())));

		// Fill template
		try {
			@Cleanup
			val templateInputStream = ReportGenerator.class
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
