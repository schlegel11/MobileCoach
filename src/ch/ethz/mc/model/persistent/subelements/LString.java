package ch.ethz.mc.model.persistent.subelements;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 *
 * For details see README.md file in the root folder of this project.
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
import java.util.HashMap;
import java.util.Locale;

import lombok.NoArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.persistent.Participant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to represent a localized {@link String}
 *
 * @author Andreas Filler
 */
@Log4j2
@NoArgsConstructor
public class LString {
	// Contains all values in different values in different languages
	@JsonProperty
	private final HashMap<Locale, String>	values	= new HashMap<Locale, String>();

	@JsonIgnore
	public LString(final String defaultValue) {
		if (defaultValue != null && !defaultValue.endsWith("")) {
			for (val locale : Constants.getInterventionLocales()) {
				values.put(locale, defaultValue);
			}
		}
	}

	@JsonIgnore
	public void set(final Locale locale, final String value) {
		if (value == null || value.equals("")) {
			values.remove(locale);
		} else {
			values.put(locale, value);
		}
	}

	@JsonIgnore
	public String get(final Participant participant) {
		if (participant == null) {
			log.error("Language string requested with non-existing participant");
			return "";
		}
		val requestedLocale = participant.getLanguage();

		if (values.containsKey(requestedLocale)) {
			return values.get(requestedLocale);
		} else {
			return "";
		}
	}

	@JsonIgnore
	public String get(final Locale locale) {
		if (values.containsKey(locale)) {
			return values.get(locale);
		} else {
			return "";
		}
	}

	public LString appendToAll(final String valueToAdd) {
		for (val key : values.keySet()) {
			values.put(key, values.get(key) + valueToAdd);
		}

		return this;
	}

	@JsonIgnore
	public boolean isEmpty() {
		for (val locale : Constants.getInterventionLocales()) {
			if (values.containsKey(locale)) {
				return false;
			}
		}
		return true;
	}

	@Override
	@JsonIgnore
	public String toString() {
		if (values.size() == 0) {
			return "";
		} else {
			val stringBuffer = new StringBuffer();
			for (val locale : Constants.getInterventionLocales()) {
				if (values.containsKey(locale)) {
					if (stringBuffer.length() > 0) {
						stringBuffer.append(" / ");
					}
					stringBuffer.append(locale.getDisplayLanguage() + ": "
							+ values.get(locale));
				}
			}
			return stringBuffer.toString();
		}
	}

	@JsonIgnore
	public String toShortenedString(int length) {
		length = length / Constants.getInterventionLocales().length;

		if (length < 15) {
			length = 15;
		}

		if (values.size() == 0) {
			return "";
		} else {
			val stringBuffer = new StringBuffer();
			for (val locale : Constants.getInterventionLocales()) {
				if (values.containsKey(locale)) {
					if (stringBuffer.length() > 0) {
						stringBuffer.append(" / ");
					}
					if (values.get(locale).length() > length) {
						stringBuffer.append(locale.getDisplayLanguage() + ": "
								+ values.get(locale).substring(0, length)
								+ "...");
					} else {
						stringBuffer.append(locale.getDisplayLanguage() + ": "
								+ values.get(locale));
					}
				}
			}
			return stringBuffer.toString();
		}
	}

	@Override
	@JsonIgnore
	public LString clone() {
		val newLString = new LString();

		for (val key : values.keySet()) {
			newLString.set(key, values.get(key));
		}

		return newLString;
	}
}
