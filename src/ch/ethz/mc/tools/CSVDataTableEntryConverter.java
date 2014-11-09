package ch.ethz.mc.tools;

import ch.ethz.mc.model.memory.DataTable;
import ch.ethz.mc.model.memory.DataTable.DataEntry;
import ch.ethz.mc.model.ui.results.UIVariableWithParticipantForResults;

import com.googlecode.jcsv.writer.CSVEntryConverter;

/**
 * Converter to convert {@link UIVariableWithParticipantForResults} to CSV
 * 
 * @author Andreas Filler
 */
public class CSVDataTableEntryConverter implements CSVEntryConverter<DataEntry> {

	@Override
	public String[] convertEntry(final DataEntry dataEntry) {
		return dataEntry.toStringArray();
	}

	public static DataEntry getHeaders(final DataTable dataTable) {
		return dataTable.getHeaders();
	}

}