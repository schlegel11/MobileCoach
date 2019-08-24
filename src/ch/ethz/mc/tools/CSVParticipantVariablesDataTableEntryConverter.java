package ch.ethz.mc.tools;

/* ##LICENSE## */
import ch.ethz.mc.model.memory.ParticipantVariablesDataTable;
import ch.ethz.mc.model.memory.ParticipantVariablesDataTable.DataEntry;
import ch.ethz.mc.model.ui.results.UIVariableWithParticipantForResults;

import com.googlecode.jcsv.writer.CSVEntryConverter;

/**
 * Converter to convert {@link UIVariableWithParticipantForResults} to CSV
 * 
 * @author Andreas Filler
 */
public class CSVParticipantVariablesDataTableEntryConverter
		implements CSVEntryConverter<DataEntry> {

	@Override
	public String[] convertEntry(final DataEntry dataEntry) {
		return dataEntry.toStringArray();
	}

	public static DataEntry getHeaders(
			final ParticipantVariablesDataTable dataTable) {
		return dataTable.getHeaders();
	}

}