package org.isgf.mhc.tools;

import lombok.val;

import org.isgf.mhc.model.ui.UIParticipantVariable;

import com.googlecode.jcsv.writer.CSVEntryConverter;

/**
 * Converter to convert {@link UIParticipantVariable} to CSV
 * 
 * @author Andreas Filler
 */
public class CSVUIParticipantVariableEntryConverter implements
		CSVEntryConverter<UIParticipantVariable> {

	@Override
	public String[] convertEntry(
			final UIParticipantVariable uiParticipantVariable) {
		return new String[] { uiParticipantVariable.getParticipantId(),
				uiParticipantVariable.getParticipantName(),
				uiParticipantVariable.getName(),
				uiParticipantVariable.getValue() };
	}

	public static UIParticipantVariable getHeaders() {
		val columnHeaders = UIParticipantVariable.getColumnHeaders();
		return new UIParticipantVariable(columnHeaders[0], columnHeaders[1],
				columnHeaders[2], columnHeaders[3]);
	}
}