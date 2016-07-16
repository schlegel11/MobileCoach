package ch.ethz.mc.model.persistent.subelements;

import java.util.HashMap;
import java.util.Locale;

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
public class LString {
	// Contains all values in different values in different languages
	@JsonProperty
	private final HashMap<Locale, String>	values	= new HashMap<Locale, String>();

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
}
