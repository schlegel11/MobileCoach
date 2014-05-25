package org.isgf.mhc.tools;

import lombok.val;

import org.isgf.mhc.model.ui.UIVariableWithParticipant;
import org.isgf.mhc.model.ui.results.UIVariableWithParticipantForResults;

import com.googlecode.jcsv.writer.CSVEntryConverter;

/**
 * Converter to convert {@link UIVariableWithParticipantForResults} to CSV
 * 
 * @author Andreas Filler
 */
public class CSVUIParticipantVariableEntryConverter implements
		CSVEntryConverter<UIVariableWithParticipantForResults> {

	@Override
	public String[] convertEntry(
			final UIVariableWithParticipantForResults uiParticipantVariable) {
		return new String[] { uiParticipantVariable.getParticipantId(),
				uiParticipantVariable.getParticipantName(),
				uiParticipantVariable.getName(),
				uiParticipantVariable.getValue() };
	}

	public static UIVariableWithParticipantForResults getHeaders() {
		val columnHeaders = UIVariableWithParticipant.getColumnHeaders();
		return new UIVariableWithParticipantForResults(columnHeaders[0],
				columnHeaders[1], columnHeaders[2], columnHeaders[3]);
	}
}