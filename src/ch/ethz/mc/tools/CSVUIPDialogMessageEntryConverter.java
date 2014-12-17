package ch.ethz.mc.tools;

/*
 * Copyright (C) 2014-2015 MobileCoach Team at Health IS-Lab
 * 
 * See a detailed listing of copyright owners and team members in
 * the README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import lombok.val;
import ch.ethz.mc.model.ui.results.UIDialogMessageWithParticipantForResults;

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
				clean(uiDialogMessage.getMessage()),
				uiDialogMessage.getShouldBeSentTimestamp(),
				uiDialogMessage.getSentTimestamp(),
				clean(uiDialogMessage.getAnswer()),
				clean(uiDialogMessage.getRawAnswer()),
				uiDialogMessage.getAnswerReceivedTimestamp(),
				uiDialogMessage.getManuallySent(),
				uiDialogMessage.getMediaContentViewed() };
	}

	private String clean(final String value) {
		return value.replace("\n", "").replace("\r", "");
	}

	public static UIDialogMessageWithParticipantForResults getHeaders() {
		val columnHeaders = UIDialogMessageWithParticipantForResults
				.getColumnHeaders();
		return new UIDialogMessageWithParticipantForResults(columnHeaders[0],
				columnHeaders[1], columnHeaders[2], columnHeaders[3],
				columnHeaders[4], columnHeaders[5], columnHeaders[6],
				columnHeaders[7], columnHeaders[8], columnHeaders[9],
				columnHeaders[10], columnHeaders[11], columnHeaders[12],
				columnHeaders[13]);
	}
}