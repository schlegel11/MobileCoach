package ch.ethz.mc.model;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

import org.apache.commons.lang3.StringEscapeUtils;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.subelements.LString;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Allows classes to be serialized as special tables
 *
 * @author Andreas Filler
 */
public class AbstractSerializableTable {
	@JsonIgnore
	private final SimpleDateFormat	longDateFormat	= new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	@JsonIgnore
	@Getter(value = AccessLevel.PROTECTED)
	private static String			H				= ImplementationConstants.REPORT_TABLE_HEADER;
	@JsonIgnore
	@Getter(value = AccessLevel.PROTECTED)
	private static String			S				= ImplementationConstants.REPORT_TABLE_SEPARATOR;

	/**
	 * Creates a special table string of the current {@link ModelObject}
	 *
	 * @return
	 */
	@JsonIgnore
	public String toSpecialTable() {
		return "NOT IMPLEMENTED FOR CLASS " + getClass().getName();
	}

	/**
	 * Wrap table for special table serializing
	 *
	 * @param tableString
	 * @return
	 */
	@JsonIgnore
	protected String wrapTable(final String tableString) {
		return "<div class=\"table\">\n" + tableString + "</div>";
	}

	/**
	 * Escape string to be HTML safe
	 *
	 * @param string
	 * @return
	 */
	@JsonIgnore
	protected String escape(final String string) {
		if (string == null || string.equals("")) {
			return "<strong>[not set]</strong>";
		}

		return StringEscapeUtils.escapeHtml4(string);
	}

	/**
	 * Escape {@link LString} to be HTML safe
	 *
	 * @param string
	 * @return
	 */
	@JsonIgnore
	protected String escape(final LString lString) {
		String table = "";

		for (val locale : Constants.getInterventionLocales()) {
			table += getH() + locale.getDisplayLanguage() + ":" + getS()
					+ escape(lString.get(locale)) + "\n";
		}

		return wrapTable(table);
	}

	/**
	 * Formats the given String as a date
	 *
	 * @param new Date(created)
	 * @return
	 */
	@JsonIgnore
	protected String formatDate(final long created) {
		return "🕑 " + longDateFormat.format(new Date(created));
	}

	/**
	 * Formats the given boolean
	 *
	 * @param new Date(created)
	 * @return
	 */
	@JsonIgnore
	protected String formatStatus(final boolean status) {
		return status ? "✅" : "🔴";
	}
}
