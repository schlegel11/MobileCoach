package org.isgf.mhc.tools;

import lombok.val;

import org.isgf.mhc.model.ui.results.UIDialogMessageWithParticipantForResults;

import com.googlecode.jcsv.writer.CSVEntryConverter;

/**
 * Converter to convert {@link UIDialogMessageWithParticipantForResults} to CSV
 * 
 * @author Andreas Filler
 */
public class CSVUIPDialogMessageEntryConverter implements
		CSVEntryConverter<UIDialogMessageWithParticipantForResults> {

	@Override
	public String[] convertEntry(
			final UIDialogMessageWithParticipantForResults uiDialogMessage) {
		return new String[] { uiDialogMessage.getParticipantId(),
				uiDialogMessage.getParticipantName(),
				uiDialogMessage.getOrder(), uiDialogMessage.getStatus(),
				uiDialogMessage.getMessage(),
				uiDialogMessage.getShouldBeSentTimestamp(),
				uiDialogMessage.getSentTimestamp(),
				uiDialogMessage.getAnswer(), uiDialogMessage.getRawAnswer(),
				uiDialogMessage.getAnswerReceivedTimestamp(),
				uiDialogMessage.getManuallySent(),
				uiDialogMessage.getMediaContentViewed() };
	}

	public static UIDialogMessageWithParticipantForResults getHeaders() {
		val columnHeaders = UIDialogMessageWithParticipantForResults.getColumnHeaders();
		return new UIDialogMessageWithParticipantForResults(columnHeaders[0],
				columnHeaders[1], columnHeaders[2], columnHeaders[3],
				columnHeaders[4], columnHeaders[5], columnHeaders[6],
				columnHeaders[7], columnHeaders[8], columnHeaders[9],
				columnHeaders[10], columnHeaders[11], columnHeaders[12],
				columnHeaders[13]);
	}
}