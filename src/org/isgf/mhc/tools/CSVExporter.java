package org.isgf.mhc.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.isgf.mhc.model.ui.UIDialogMessage;
import org.isgf.mhc.model.ui.UIModelObject;
import org.isgf.mhc.model.ui.UIParticipantVariable;

import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;

/**
 * Exports specific {@link UIModelObject}s as CSV
 * 
 * @author Andreas Filler
 */
public class CSVExporter {
	public static InputStream convertUIParticipantVariableToCSV(
			final List<UIParticipantVariable> items) throws IOException {
		final StringWriter stringWriter = new StringWriter();

		final CSVWriter<UIParticipantVariable> csvWriter = new CSVWriterBuilder<UIParticipantVariable>(
				stringWriter).entryConverter(
				new CSVUIParticipantVariableEntryConverter()).build();

		csvWriter.write(CSVUIParticipantVariableEntryConverter.getHeaders());

		csvWriter.writeAll(items);

		csvWriter.flush();
		csvWriter.close();

		return new ByteArrayInputStream(stringWriter.toString().getBytes());
	}

	public static InputStream convertUIDialogMessageToCSV(
			final List<UIDialogMessage> items) throws IOException {
		final StringWriter stringWriter = new StringWriter();

		final CSVWriter<UIDialogMessage> csvWriter = new CSVWriterBuilder<UIDialogMessage>(
				stringWriter).entryConverter(
				new CSVUIPDialogMessageEntryConverter()).build();

		csvWriter.write(CSVUIPDialogMessageEntryConverter.getHeaders());

		csvWriter.writeAll(items);

		csvWriter.flush();
		csvWriter.close();

		return new ByteArrayInputStream(stringWriter.toString().getBytes());
	}
}
