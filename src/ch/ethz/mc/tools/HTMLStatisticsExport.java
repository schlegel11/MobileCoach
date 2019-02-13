package ch.ethz.mc.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringEscapeUtils;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.Intervention;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Exports HTML for several purposes
 *
 * @author Andreas Filler
 */
@Log4j2
public class HTMLStatisticsExport {
	@Data
	@AllArgsConstructor
	private class MicroDialogRepresentation {
		private String	id;
		private String	name;
		private int		visits;
	}

	@Data
	@AllArgsConstructor
	private class MicroDialogMessageRepresentation {
		private String	microDialogName;
		private int		order;
		private String	question;
		private String	type;
		private String	answerOptions;
		private int		answered;
		private int		unanswered;
		private int		deactivated;
		private int		endPoint;
	}

	private final OutputStreamWriter	writer;

	private final Intervention			intervention;
	private final String				interventionId;

	private final Properties			statistics;

	public HTMLStatisticsExport(final File statisticsFolder,
			final Intervention intervention, final Properties statistics)
			throws IOException {
		log.debug("Creating HTML export instance");

		// Create graph
		val htmlFile = new File(statisticsFolder,
				"statistics_" + intervention.getName().replaceAll(
						ImplementationConstants.REGULAR_EXPRESSION_TO_CLEAN_FILE_NAMES,
						"_") + ".htm");

		if (htmlFile.exists()) {
			htmlFile.delete();
		}

		writer = new OutputStreamWriter(new FileOutputStream(htmlFile),
				StandardCharsets.UTF_8);

		this.intervention = intervention;
		interventionId = intervention.getId().toHexString();

		this.statistics = statistics;
	}

	/**
	 * Creates a statistics HTML file of the given intervention and properties
	 * 
	 * @throws IOException
	 */
	public void createInterventionStatisticsHTMLFile() throws IOException {
		log.debug("Creating HTML intervention export");

		write("<!DOCTYPE>");
		write("<html>");
		write("<head>");
		write("<head>");
		write("<meta charset=\"utf-8\"/>");
		write("<link href='https://fonts.googleapis.com/css?family=Raleway:400,300,600' rel='stylesheet' type='text/css'/>");
		write("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/skeleton/2.0.4/skeleton.min.css\" integrity=\"sha256-2YQRJMXD7pIAPHiXr0s+vlRWA7GYJEK0ARns7k2sbHY=\" crossorigin=\"anonymous\"/>");
		write("<style type=\"text/css\">\ndiv.container { max-width: 2000px; }\ndiv.row { padding-top: 2em; }\ntable { width: 100%; }\ntable.simple th:first-child { width: 70%; }\nh2 { color: darkgray; }\nh3 { color: gray; }\n</style>");

		write("title", intervention.getName() + " - Pathmate Statistics");

		write("</head>");
		write("<body>");
		write("<div class=\"container\">");
		// Start of container

		write("p", " ");
		write("h3", "Pathmate Statistics");
		write("h1", intervention.getName());

		write("<div class=\"row\">");
		// Start of row 1

		write("<div class=\"one-half column\">");
		// Start of column 1

		write("h2", "Basic statistics");

		write("<table class=\"simple\">");
		write("<tr>");
		write("th", "Attribute", "Value");
		write("</tr>");
		write("<tr>");
		write("td", "Valid participants", get("validParticipants"));
		write("</tr>");
		write("<tr>");
		write("td", "Invalid participants", get("invalidParticipants"));
		write("</tr>");
		write("<tr>");
		write("td", "Activated micro dialogs",
				get("totalActivatedMicroDialogs"));
		write("</tr>");
		write("<tr>");
		write("td", "Answered questions", get("answeredQuestions"));
		write("</tr>");
		write("<tr>");
		write("td", "Unanswered questions", get("unansweredQuestions"));
		write("</tr>");
		write("<tr>");
		write("td", "Deactivated questions", get("totalDeactivatedMessages"));
		write("</tr>");
		write("<tr>");
		write("td", "Sent messages", get("totalSentMessages"));
		write("</tr>");
		write("<tr>");
		write("td", "Sent commands", get("totalSentCommands"));
		write("</tr>");
		write("<tr>");
		write("td", "Received messages", get("totalReceivedMessages"));
		write("</tr>");
		write("<tr>");
		write("td", "Received intentions", get("totalReceivedIntentions"));
		write("</tr>");
		write("</table>");

		// End of column 1
		write("</div>");

		write("<div class=\"one-half column\">");
		// Start of column 2

		write("h2", "Participant characteristics");

		write("<table class=\"simple\">");
		write("<tr>");
		write("th", "Attribute", "Value");
		write("</tr>");
		write("<tr>");
		write("td", "Participation in days (total)", TimeUnit.SECONDS
				.toDays(Long.parseLong(get("secondsUsageTotal"))));
		write("</tr>");
		write("<tr>");
		write("td", "Participation in days (per Participant)", TimeUnit.SECONDS
				.toDays(Long.parseLong(get("secondsUsageAverage"))));
		write("</tr>");
		write("</table>");

		write("<table class=\"simple\">");
		write("<tr>");
		write("th", "Language", "Value");
		write("</tr>");

		val languageField = "intervention." + interventionId + ".language.";
		statistics.stringPropertyNames().forEach(property -> {
			if (property.startsWith(languageField)) {
				write("<tr>");
				write("td", property.substring(languageField.length()),
						statistics.getProperty(property));
				write("</tr>");
			}
		});

		write("</table>");

		write("<table class=\"simple\">");
		write("<tr>");
		write("th", "Platform", "Value");
		write("</tr>");

		val platformField = "intervention." + interventionId + ".platform.";
		statistics.stringPropertyNames().forEach(property -> {
			if (property.startsWith(platformField)) {
				write("<tr>");
				write("td", property.substring(platformField.length()),
						statistics.getProperty(property));
				write("</tr>");
			}
		});

		write("</table>");

		// End of column 2
		write("</div>");

		// End of row 1
		write("</div>");

		write("<div class=\"row\">");
		// Start of row 2

		write("h2", "Micro dialogs (by visits)");

		write("<table>");
		write("<tr>");
		write("th", "Name", "Visits");
		write("</tr>");

		val mdList = new ArrayList<MicroDialogRepresentation>();
		val mdField = "intervention." + interventionId + ".md.";
		statistics.stringPropertyNames().forEach(property -> {
			if (property.startsWith(mdField) && property.endsWith(".Name")) {
				val mdId = property.split("\\.")[3];

				mdList.add(new MicroDialogRepresentation(mdId,
						statistics.getProperty(mdField + mdId + ".Name"),
						Integer.parseInt(statistics
								.getProperty(mdField + mdId + ".Value"))));
			}
		});

		Collections.sort(mdList, (final MicroDialogRepresentation md1,
				final MicroDialogRepresentation md2) -> {
			if (md1.getVisits() > md2.getVisits()) {
				return -1;
			} else if (md1.getVisits() < md2.getVisits()) {
				return 1;
			} else {
				return 0;
			}
		});

		mdList.forEach(md -> {
			write("<tr>");
			write("td", md.getName(), md.getVisits());
			write("</tr>");
		});

		write("</table>");

		// End of row 2
		write("</div>");

		write("<div class=\"row\">");
		// Start of row 3

		write("h2", "Micro dialog messages (question status by order)");

		val allMessagesList = new ArrayList<MicroDialogMessageRepresentation>();

		mdList.forEach(md -> {
			write("h3", md.getName());

			write("<table>");
			write("<tr>");
			write("th", "Question", "Type", "Answer options", "Answered",
					"Unanswered", "Deactivated");
			write("</tr>");

			val mdMessageList = new ArrayList<MicroDialogMessageRepresentation>();
			val mdMessageField = "intervention." + interventionId + ".md."
					+ md.getId() + ".m.";
			statistics.stringPropertyNames().forEach(property -> {
				if (property.startsWith(mdMessageField)
						&& property.endsWith(".Question")) {
					val mdMessageId = property.split("\\.")[5];

					val mdmRepresentation = new MicroDialogMessageRepresentation(
							md.getName(),
							Integer.parseInt(statistics.getProperty(
									mdMessageField + mdMessageId + ".Order")),
							statistics.getProperty(
									mdMessageField + mdMessageId + ".Question"),
							statistics.getProperty(
									mdMessageField + mdMessageId + ".Type"),
							statistics.getProperty(mdMessageField + mdMessageId
									+ ".AnswerOptions"),
							Integer.parseInt(
									statistics.getProperty(mdMessageField
											+ mdMessageId + ".Answered")),
							Integer.parseInt(
									statistics.getProperty(mdMessageField
											+ mdMessageId + ".Unanswered")),
							Integer.parseInt(
									statistics.getProperty(mdMessageField
											+ mdMessageId + ".Deactivated")),
							Integer.parseInt(
									statistics.getProperty(mdMessageField
											+ mdMessageId + ".EndPoint")));

					mdMessageList.add(mdmRepresentation);
					allMessagesList.add(mdmRepresentation);
				}
			});

			Collections.sort(mdMessageList,
					(final MicroDialogMessageRepresentation mdm1,
							final MicroDialogMessageRepresentation mdm2) -> {
						if (mdm1.getOrder() > mdm2.getOrder()) {
							return 1;
						} else if (mdm1.getOrder() < mdm2.getOrder()) {
							return -1;
						} else {
							return 0;
						}
					});

			mdMessageList.forEach(mdm -> {
				write("<tr>");
				write("td", mdm.getQuestion(), mdm.getType(),
						mdm.getAnswerOptions(), mdm.getAnswered(),
						mdm.getUnanswered(), mdm.getDeactivated());
				write("</tr>");
			});

			write("</table>");
		});

		// End of row 3
		write("</div>");

		write("<div class=\"row\">");
		// Start of row 4

		write("h2", "Potention end-points (open/unanswered last questions)");

		write("<table>");
		write("<tr>");
		write("th", "Micro dialog", "Question", "Type", "Answer options",
				"Rate");
		write("</tr>");

		Collections.sort(allMessagesList,
				(final MicroDialogMessageRepresentation mdm1,
						final MicroDialogMessageRepresentation mdm2) -> {
					if (mdm1.getEndPoint() > mdm2.getEndPoint()) {
						return -1;
					} else if (mdm1.getEndPoint() < mdm2.getEndPoint()) {
						return 1;
					} else {
						return 0;
					}
				});

		allMessagesList.forEach(mdm -> {
			if (mdm.getEndPoint() > 0) {
				write("<tr>");
				write("td", mdm.getMicroDialogName(), mdm.getQuestion(),
						mdm.getType(), mdm.getAnswerOptions(),
						mdm.getEndPoint());
				write("</tr>");
			}
		});

		write("</table>");

		// End of row 4
		write("</div>");

		write("<div class=\"row\">");
		// Start of last row

		write("<p>Created: " + statistics.getProperty("created") + "<br/>");
		write("&copy; Pathmate Technologies</p>");

		// End of last row
		write("</div>");

		// End of container
		write("</div>");

		write("</body>");
		write("</html>");

		writer.flush();
		writer.close();
	}

	/**
	 * @param properties
	 * @return
	 */
	private String get(final String... properties) {
		return statistics.getProperty("intervention." + interventionId + "."
				+ String.join(".", properties));
	}

	/**
	 * Writes raw tag
	 * 
	 * @param tag
	 */
	private void write(final String tag) {
		try {
			writer.write(tag + "\n");
		} catch (final IOException e) {
			log.error("Error when writing statistics to HTML file: {}",
					e.getMessage());
		}
	}

	/**
	 * Writes tag(s) with value(s)
	 * 
	 * @param tag
	 * @param values
	 */
	private void write(final String tag, final Object... values) {
		try {
			for (val value : values) {
				writer.write("<" + tag + ">"
						+ StringEscapeUtils.escapeHtml4(String.valueOf(value))
								.replaceAll("\n", "<br/>")
						+ "</" + tag + ">\n");
			}
		} catch (final IOException e) {
			log.error("Error when writing statistics to HTML file: {}",
					e.getMessage());
		}
	}
}
