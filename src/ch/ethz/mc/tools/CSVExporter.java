package ch.ethz.mc.tools;

/* ##LICENSE## */
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;

import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;

import ch.ethz.mc.model.memory.MessagesDialogsI18nStringsObject;
import ch.ethz.mc.model.memory.ParticipantVariablesDataTable;
import ch.ethz.mc.model.memory.SurveysFeedbacksI18nStringsObject;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.results.UIDialogMessageWithParticipantForResults;
import ch.ethz.mc.model.ui.results.UIVariableWithParticipantForResults;
import lombok.val;

/**
 * Exports specific {@link UIModelObject}s as CSV
 * 
 * @author Andreas Filler
 */
public class CSVExporter {
	/**
	 * Converts participants variables data table to CSV
	 * 
	 * @param dataTable
	 * @return
	 * @throws IOException
	 */
	public static InputStream convertParticipantVariablesDataTableToCSV(
			final ParticipantVariablesDataTable dataTable) throws IOException {
		final StringWriter stringWriter = new StringWriter();

		final CSVWriter<ParticipantVariablesDataTable.DataEntry> csvWriter = new CSVWriterBuilder<ParticipantVariablesDataTable.DataEntry>(
				stringWriter)
						.entryConverter(
								new CSVParticipantVariablesDataTableEntryConverter())
						.build();

		csvWriter.write(dataTable.getHeaders());

		csvWriter.writeAll(dataTable.getEntries());

		csvWriter.flush();
		csvWriter.close();

		return new ByteArrayInputStream(stringWriter.toString().getBytes());
	}

	/**
	 * Converts participants variables to CSV
	 * 
	 * @param items
	 * @return
	 * @throws IOException
	 */
	public static InputStream convertUIParticipantVariableForResultsToCSV(
			final List<UIVariableWithParticipantForResults> items)
			throws IOException {
		final StringWriter stringWriter = new StringWriter();

		final CSVWriter<UIVariableWithParticipantForResults> csvWriter = new CSVWriterBuilder<UIVariableWithParticipantForResults>(
				stringWriter)
						.entryConverter(
								new CSVUIParticipantVariableEntryConverter())
						.build();

		csvWriter.write(CSVUIParticipantVariableEntryConverter.getHeaders());

		csvWriter.writeAll(items);

		csvWriter.flush();
		csvWriter.close();

		return new ByteArrayInputStream(stringWriter.toString().getBytes());
	}

	/**
	 * Converts dialogs messages to CSV
	 * 
	 * @param items
	 * @return
	 * @throws IOException
	 */
	public static InputStream convertUIDialogMessageForResultsToCSV(
			final List<UIDialogMessageWithParticipantForResults> items)
			throws IOException {
		final StringWriter stringWriter = new StringWriter();

		final CSVWriter<UIDialogMessageWithParticipantForResults> csvWriter = new CSVWriterBuilder<UIDialogMessageWithParticipantForResults>(
				stringWriter)
						.entryConverter(new CSVUIPDialogMessageEntryConverter())
						.build();

		csvWriter.write(CSVUIPDialogMessageEntryConverter.getHeaders());

		csvWriter.writeAll(items);

		csvWriter.flush();
		csvWriter.close();

		return new ByteArrayInputStream(stringWriter.toString().getBytes());
	}

	/**
	 * Exports {@link LString}s with keys for i18n of messages and dialogs
	 * 
	 * @param items
	 * @return
	 * @throws IOException
	 */
	public static InputStream convertMessagesDialogsI18nStringsObjectsToCSV(
			final List<MessagesDialogsI18nStringsObject> items)
			throws IOException {
		final StringWriter stringWriter = new StringWriter();

		stringWriter.append('\ufeff'); // Adds UTF-8 BOM
		val csvPrinter = CSVFormat.EXCEL.withDelimiter(';').withHeader(
				CSVI18nStringsObjectEntryConverter.getMessagesDialogsHeaders())
				.print(stringWriter);

		for (val item : items) {
			csvPrinter.printRecord(CSVI18nStringsObjectEntryConverter
					.convertMessagesDialogsEntry(item));
		}

		csvPrinter.flush();
		csvPrinter.close();

		return new ByteArrayInputStream(
				stringWriter.toString().getBytes("UTF-8"));
	}

	/**
	 * Exports {@link LString}s with keys for i18n of surveys and feedbacks
	 * 
	 * @param items
	 * @return
	 * @throws IOException
	 */
	public static InputStream convertSurveysFeedbacksI18nStringsObjectsToCSV(
			final List<SurveysFeedbacksI18nStringsObject> items)
			throws IOException {
		final StringWriter stringWriter = new StringWriter();

		stringWriter.append('\ufeff'); // Adds UTF-8 BOM
		val csvPrinter = CSVFormat.EXCEL.withDelimiter(';').withHeader(
				CSVI18nStringsObjectEntryConverter.getSurvevsFeedbacksHeaders())
				.print(stringWriter);

		for (val item : items) {
			csvPrinter.printRecord(CSVI18nStringsObjectEntryConverter
					.convertSurveysFeedbacksEntry(item));
		}

		csvPrinter.flush();
		csvPrinter.close();

		return new ByteArrayInputStream(
				stringWriter.toString().getBytes("UTF-8"));
	}
}
