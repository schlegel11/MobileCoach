package ch.ethz.mc.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import ch.ethz.mc.model.memory.MessagesDialogsI18nStringsObject;
import ch.ethz.mc.model.memory.SurveysFeedbacksI18nStringsObject;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.ui.UIModelObject;
import lombok.val;

/**
 * Imports specific {@link UIModelObject}s from CSV
 * 
 * @author Andreas Filler
 */
public class CSVImporter {
	/**
	 * Import {@link LString}s with keys for i18n of messages and dialogs
	 * 
	 * @param csvFile
	 * @return
	 * @throws IOException
	 */
	public static List<MessagesDialogsI18nStringsObject> convertCSVToI18nMessagesDialogsStringsObjects(
			final File csvFile) throws IOException {
		val items = new ArrayList<MessagesDialogsI18nStringsObject>();
		val fileReader = new FileReader(csvFile);

		final Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';')
				.parse(fileReader);

		for (final CSVRecord record : records) {
			val i18nStringsObject = CSVI18nStringsObjectEntryConverter
					.convertMessagesDialogsEntry(record);

			if (i18nStringsObject != null) {
				items.add(i18nStringsObject);
			}
		}

		return items;
	}

	/**
	 * Import {@link LString}s with keys for i18n of surveys and feedbacks
	 * 
	 * @param csvFile
	 * @return
	 * @throws IOException
	 */
	public static List<SurveysFeedbacksI18nStringsObject> convertCSVToI18nSurveysFeedbacksStringsObjects(
			final File csvFile) throws IOException {
		val items = new ArrayList<SurveysFeedbacksI18nStringsObject>();
		val fileReader = new FileReader(csvFile);

		final Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';')
				.parse(fileReader);

		for (final CSVRecord record : records) {
			val i18nStringsObject = CSVI18nStringsObjectEntryConverter
					.convertSurveysFeedbacksEntry(record);

			if (i18nStringsObject != null) {
				items.add(i18nStringsObject);
			}
		}

		return items;
	}
}
