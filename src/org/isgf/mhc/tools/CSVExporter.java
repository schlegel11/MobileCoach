package org.isgf.mhc.tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.isgf.mhc.model.ui.UIDialogMessageWithParticipant;
import org.isgf.mhc.model.ui.UIModelObject;
import org.isgf.mhc.model.ui.UIVariableWithParticipant;

import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;

/**
 * Exports specific {@link UIModelObject}s as CSV
 * 
 * @author Andreas Filler
 */
public class CSVExporter {
	public static InputStream convertUIParticipantVariableToCSV(
			final List<UIVariableWithParticipant> items) throws IOException {
		final StringWriter stringWriter = new StringWriter();

		final CSVWriter<UIVariableWithParticipant> csvWriter = new CSVWriterBuilder<UIVariableWithParticipant>(
				stringWriter).entryConverter(
				new CSVUIParticipantVariableEntryConverter()).build();

		csvWriter.write(CSVUIParticipantVariableEntryConverter.getHeaders());

		csvWriter.writeAll(items);

		csvWriter.flush();
		csvWriter.close();

		return new ByteArrayInputStream(stringWriter.toString().getBytes());
	}

	public static InputStream convertUIDialogMessageToCSV(
			final List<UIDialogMessageWithParticipant> items) throws IOException {
		final StringWriter stringWriter = new StringWriter();

		final CSVWriter<UIDialogMessageWithParticipant> csvWriter = new CSVWriterBuilder<UIDialogMessageWithParticipant>(
				stringWriter).entryConverter(
				new CSVUIPDialogMessageEntryConverter()).build();

		csvWriter.write(CSVUIPDialogMessageEntryConverter.getHeaders());

		csvWriter.writeAll(items);

		csvWriter.flush();
		csvWriter.close();

		return new ByteArrayInputStream(stringWriter.toString().getBytes());
	}
}
