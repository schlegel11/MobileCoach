package org.isgf.mhc.tools;

import lombok.val;

import org.isgf.mhc.model.ui.UIDialogMessageWithParticipant;

import com.googlecode.jcsv.writer.CSVEntryConverter;

/**
 * Converter to convert {@link UIDialogMessageWithParticipant} to CSV
 * 
 * @author Andreas Filler
 */
public class CSVUIPDialogMessageEntryConverter implements
		CSVEntryConverter<UIDialogMessageWithParticipant> {

	@Override
	public String[] convertEntry(
			final UIDialogMessageWithParticipant uiDialogMessage) {
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

	public static UIDialogMessageWithParticipant getHeaders() {
		val columnHeaders = UIDialogMessageWithParticipant.getColumnHeaders();
		return new UIDialogMessageWithParticipant(columnHeaders[0],
				columnHeaders[1], columnHeaders[2], columnHeaders[3],
				columnHeaders[4], columnHeaders[5], columnHeaders[6],
				columnHeaders[7], columnHeaders[8], columnHeaders[9],
				columnHeaders[10], columnHeaders[11]);
	}
}