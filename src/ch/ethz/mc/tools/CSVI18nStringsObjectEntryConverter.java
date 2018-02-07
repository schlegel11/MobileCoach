package ch.ethz.mc.tools;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.memory.I18nStringsObject;
import ch.ethz.mc.model.persistent.subelements.LString;
import lombok.val;

/**
 * Converter to convert {@link I18nStringsObject} to CSV and back
 * 
 * @author Andreas Filler
 */
public class CSVI18nStringsObjectEntryConverter {

	/**
	 * Convert entry for import
	 * 
	 * @param i18nStringsObject
	 * @return
	 */
	public static List<String> convertEntry(
			final I18nStringsObject i18nStringsObject) {
		val locales = Constants.getInterventionLocales();
		val entry = new ArrayList<String>();

		val text = i18nStringsObject.getText();
		val answerOptions = i18nStringsObject.getAnswerOptions();

		entry.add(i18nStringsObject.getId());
		entry.add(i18nStringsObject.getDescription());

		for (val locale : locales) {
			entry.add(cleanLinebreaks(text.get(locale), true));
		}
		for (val locale : locales) {
			entry.add(cleanLinebreaks(answerOptions.get(locale), true));
		}

		return entry;
	}

	/**
	 * Convert entry for export
	 * 
	 * @param csvRecord
	 * @return
	 */
	public static I18nStringsObject convertEntry(final CSVRecord csvRecord) {
		if ((csvRecord.get(0).startsWith("mm_")
				|| csvRecord.get(0).startsWith("dm_"))
				&& csvRecord.get(0).endsWith("_#")) {
			val locales = Constants.getInterventionLocales();
			val i18nStringsObject = new I18nStringsObject();

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

	public static String[] getHeaders() {
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

	private static String cleanLinebreaks(String string, boolean forWindows) {
		if (forWindows) {
			return string.replaceAll("(\r\n|\r|\n)", "\r\n");
		} else {
			return string.replaceAll("(\r\n|\r|\n)", "\n");
		}
	}
}