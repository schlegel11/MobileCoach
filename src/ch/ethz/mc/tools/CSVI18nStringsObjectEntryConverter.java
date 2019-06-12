package ch.ethz.mc.tools;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.memory.MessagesDialogsI18nStringsObject;
import ch.ethz.mc.model.memory.SurveysFeedbacksI18nStringsObject;
import ch.ethz.mc.model.persistent.subelements.LString;
import lombok.val;

/**
 * Converter to convert {@link MessagesDialogsI18nStringsObject} to CSV and back
 * 
 * @author Andreas Filler
 */
public class CSVI18nStringsObjectEntryConverter {

	/**
	 * Convert entry for export of messages and dialogs
	 * 
	 * @param i18nStringsObject
	 * @return
	 */
	public static List<String> convertMessagesDialogsEntry(
			final MessagesDialogsI18nStringsObject i18nStringsObject) {
		val locales = Constants.getInterventionLocales();
		val entry = new ArrayList<String>();

		val text = i18nStringsObject.getText();
		val answerOptions = i18nStringsObject.getAnswerOptions();

		entry.add(i18nStringsObject.getId());
		entry.add(i18nStringsObject.getDescription());

		for (val locale : locales) {
			if (text != null) {
				entry.add(cleanLinebreaks(text.get(locale), true));
			} else {
				entry.add("");
			}
		}
		for (val locale : locales) {
			if (text != null) {
				entry.add(cleanLinebreaks(answerOptions.get(locale), true));
			} else {
				entry.add("");
			}
		}

		return entry;
	}

	/**
	 * Convert entry for export of surveys and feedback
	 * 
	 * @param i18nStringsObject
	 * @return
	 */
	public static List<String> convertSurveysFeedbacksEntry(
			final SurveysFeedbacksI18nStringsObject i18nStringsObject) {
		val locales = Constants.getInterventionLocales();
		val entry = new ArrayList<String>();

		val title = i18nStringsObject.getTitle();
		val text = i18nStringsObject.getText();
		val errorMessage = i18nStringsObject.getErrorMessage();

		entry.add(i18nStringsObject.getId());
		entry.add(i18nStringsObject.getDescription());

		for (val locale : locales) {
			if (title != null) {
				entry.add(cleanLinebreaks(title.get(locale), true));
			} else {
				entry.add(" --- ");
			}
		}
		for (val locale : locales) {
			if (text != null) {
				entry.add(cleanLinebreaks(text.get(locale), true));
			} else {
				entry.add(" --- ");
			}
		}
		for (val locale : locales) {
			if (errorMessage != null) {
				entry.add(cleanLinebreaks(errorMessage.get(locale), true));
			} else {
				entry.add(" --- ");
			}
		}

		return entry;
	}

	/**
	 * Convert entry for import of messages and dialogs
	 * 
	 * @param csvRecord
	 * @return
	 */
	public static MessagesDialogsI18nStringsObject convertMessagesDialogsEntry(
			final CSVRecord csvRecord) {
		if ((csvRecord.get(0).startsWith("mm_")
				|| csvRecord.get(0).startsWith("dm_"))
				&& csvRecord.get(0).endsWith("_#")) {
			val locales = Constants.getInterventionLocales();
			val i18nStringsObject = new MessagesDialogsI18nStringsObject();

			i18nStringsObject.setId(csvRecord.get(0));

			val text = new LString(null);
			val answerOptions = new LString(null);
			i18nStringsObject.setText(text);
			i18nStringsObject.setAnswerOptions(answerOptions);

			int i = 2;
			for (val locale : locales) {
				if (!StringUtils.isBlank(csvRecord.get(i))) {
					text.set(locale, cleanLinebreaks(csvRecord.get(i), false));
				}
				i++;
			}
			for (val locale : locales) {
				if (!StringUtils.isBlank(csvRecord.get(i))) {
					answerOptions.set(locale,
							cleanLinebreaks(csvRecord.get(i), false));
				}
				i++;
			}

			return i18nStringsObject;
		} else {
			return null;
		}
	}

	/**
	 * Convert entry for import of surveys and feedbacks
	 * 
	 * @param csvRecord
	 * @return
	 */
	public static SurveysFeedbacksI18nStringsObject convertSurveysFeedbacksEntry(
			final CSVRecord csvRecord) {
		if ((csvRecord.get(0).startsWith("su_")
				|| csvRecord.get(0).startsWith("ss_")
				|| csvRecord.get(0).startsWith("sq_")
				|| csvRecord.get(0).startsWith("qa_")
				|| csvRecord.get(0).startsWith("fb_")
				|| csvRecord.get(0).startsWith("fs_"))
				&& csvRecord.get(0).endsWith("_#")) {
			val locales = Constants.getInterventionLocales();
			val i18nStringsObject = new SurveysFeedbacksI18nStringsObject();

			i18nStringsObject.setId(csvRecord.get(0));

			val title = new LString(null);
			val text = new LString(null);
			val errorMessage = new LString(null);
			i18nStringsObject.setTitle(title);
			i18nStringsObject.setText(text);
			i18nStringsObject.setErrorMessage(errorMessage);

			int i = 2;
			for (val locale : locales) {
				if (!StringUtils.isBlank(csvRecord.get(i))) {
					title.set(locale, cleanLinebreaks(csvRecord.get(i), false));
				}
				i++;
			}
			for (val locale : locales) {
				if (!StringUtils.isBlank(csvRecord.get(i))) {
					text.set(locale, cleanLinebreaks(csvRecord.get(i), false));
				}
				i++;
			}
			for (val locale : locales) {
				if (!StringUtils.isBlank(csvRecord.get(i))) {
					errorMessage.set(locale,
							cleanLinebreaks(csvRecord.get(i), false));
				}
				i++;
			}

			return i18nStringsObject;
		} else {
			return null;
		}
	}

	/**
	 * Get headers of messages and dialogs
	 * 
	 * @return
	 */
	public static String[] getMessagesDialogsHeaders() {
		val locales = Constants.getInterventionLocales();
		val entry = new ArrayList<String>();

		entry.add("Identifier (don't change!)");
		entry.add("Description (don't change!)");

		for (val locale : locales) {
			entry.add("Text in " + locale.getDisplayLanguage());
		}
		for (val locale : locales) {
			entry.add("Answer Options in " + locale.getDisplayLanguage());
		}

		return entry.toArray(new String[0]);
	}

	/**
	 * Get headers of surveys and feedbacks
	 * 
	 * @return
	 */
	public static String[] getSurvevsFeedbacksHeaders() {
		val locales = Constants.getInterventionLocales();
		val entry = new ArrayList<String>();

		entry.add("Identifier (don't change!)");
		entry.add("Description (don't change!)");

		for (val locale : locales) {
			entry.add("Title in " + locale.getDisplayLanguage());
		}
		for (val locale : locales) {
			entry.add("Text in " + locale.getDisplayLanguage());
		}
		for (val locale : locales) {
			entry.add("Error Messages in " + locale.getDisplayLanguage());
		}

		return entry.toArray(new String[0]);
	}

	/**
	 * Clean line breaks
	 * 
	 * @param string
	 * @param forWindows
	 * @return
	 */
	private static String cleanLinebreaks(String string, boolean forWindows) {
		if (forWindows) {
			return string.replaceAll("(\r\n|\r|\n)", "\r\n");
		} else {
			return string.replaceAll("(\r\n|\r|\n)", "\n").replaceAll("\u2028",
					"");
		}
	}
}