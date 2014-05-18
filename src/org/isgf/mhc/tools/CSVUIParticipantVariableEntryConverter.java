package org.isgf.mhc.tools;

import lombok.val;

import org.isgf.mhc.model.ui.UIVariableWithParticipant;

import com.googlecode.jcsv.writer.CSVEntryConverter;

/**
 * Converter to convert {@link UIVariableWithParticipant} to CSV
 * 
 * @author Andreas Filler
 */
public class CSVUIParticipantVariableEntryConverter implements
		CSVEntryConverter<UIVariableWithParticipant> {

	@Override
	public String[] convertEntry(
			final UIVariableWithParticipant uiParticipantVariable) {
		return new String[] { uiParticipantVariable.getParticipantId(),
				uiParticipantVariable.getParticipantName(),
				uiParticipantVariable.getName(),
				uiParticipantVariable.getValue() };
	}

	public static UIVariableWithParticipant getHeaders() {
		val columnHeaders = UIVariableWithParticipant.getColumnHeaders();
		return new UIVariableWithParticipant(columnHeaders[0], columnHeaders[1],
				columnHeaders[2], columnHeaders[3]);
	}
}