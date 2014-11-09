package ch.ethz.mc.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import ch.ethz.mc.model.memory.DataTable;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.results.UIDialogMessageWithParticipantForResults;
import ch.ethz.mc.model.ui.results.UIVariableWithParticipantForResults;

import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;

/**
 * Exports specific {@link UIModelObject}s as CSV
 * 
 * @author Andreas Filler
 */
public class CSVExporter {
	/**
	 * @param dataTable
	 * @return
	 * @throws IOException
	 */
	public static InputStream convertDataTableToCSV(final DataTable dataTable)
			throws IOException {
		final StringWriter stringWriter = new StringWriter();

		final CSVWriter<DataTable.DataEntry> csvWriter = new CSVWriterBuilder<DataTable.DataEntry>(
				stringWriter).entryConverter(new CSVDataTableEntryConverter())
				.build();

		csvWriter.write(dataTable.getHeaders());

		csvWriter.writeAll(dataTable.getEntries());

		csvWriter.flush();
		csvWriter.close();

		return new ByteArrayInputStream(stringWriter.toString().getBytes());
	}

	/**
	 * @param items
	 * @return
	 * @throws IOException
	 */
	public static InputStream convertUIParticipantVariableForResultsToCSV(
			final List<UIVariableWithParticipantForResults> items)
			throws IOException {
		final StringWriter stringWriter = new StringWriter();

		final CSVWriter<UIVariableWithParticipantForResults> csvWriter = new CSVWriterBuilder<UIVariableWithParticipantForResults>(
				stringWriter).entryConverter(
				new CSVUIParticipantVariableEntryConverter()).build();

		csvWriter.write(CSVUIParticipantVariableEntryConverter.getHeaders());

		csvWriter.writeAll(items);

		csvWriter.flush();
		csvWriter.close();

		return new ByteArrayInputStream(stringWriter.toString().getBytes());
	}

	/**
	 * @param items
	 * @return
	 * @throws IOException
	 */
	public static InputStream convertUIDialogMessageForResultsToCSV(
			final List<UIDialogMessageWithParticipantForResults> items)
			throws IOException {
		final StringWriter stringWriter = new StringWriter();

		final CSVWriter<UIDialogMessageWithParticipantForResults> csvWriter = new CSVWriterBuilder<UIDialogMessageWithParticipantForResults>(
				stringWriter).entryConverter(
				new CSVUIPDialogMessageEntryConverter()).build();

		csvWriter.write(CSVUIPDialogMessageEntryConverter.getHeaders());

		csvWriter.writeAll(items);

		csvWriter.flush();
		csvWriter.close();

		return new ByteArrayInputStream(stringWriter.toString().getBytes());
	}
}
