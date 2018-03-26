package ch.ethz.mc.tools;

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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;

import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;

import ch.ethz.mc.model.memory.I18nStringsObject;
import ch.ethz.mc.model.memory.ParticipantVariablesDataTable;
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
	 * Exports {@link LString}s with keys for i18n
	 * 
	 * @param items
	 * @return
	 * @throws IOException
	 */
	public static InputStream convertI18nStringsObjectsToCSV(
			final List<I18nStringsObject> items) throws IOException {
		final StringWriter stringWriter = new StringWriter();

		stringWriter.append('\ufeff'); // Adds UTF-8 BOM
		val csvPrinter = CSVFormat.EXCEL.withDelimiter(';')
				.withHeader(CSVI18nStringsObjectEntryConverter.getHeaders())
				.print(stringWriter);

		for (val item : items) {
			csvPrinter.printRecord(
					CSVI18nStringsObjectEntryConverter.convertEntry(item));
		}

		csvPrinter.flush();
		csvPrinter.close();

		return new ByteArrayInputStream(
				stringWriter.toString().getBytes("UTF-8"));
	}
}
