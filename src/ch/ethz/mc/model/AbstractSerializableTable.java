package ch.ethz.mc.model;

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
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.val;

import org.apache.commons.lang3.StringEscapeUtils;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.subelements.LString;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Allows classes to be serialized as HTML tables
 *
 * @author Andreas Filler
 */
public class AbstractSerializableTable {
	@JsonIgnore
	private final SimpleDateFormat	longDateFormat	= new SimpleDateFormat(
															"yyyy-MM-dd HH:mm:ss");

	/**
	 * Creates a HTML table string of the current {@link ModelObject}
	 *
	 * @return
	 */
	@JsonIgnore
	public String toTable() {
		return "NOT IMPLEMENTED FOR CLASS " + getClass().getName();
	}

	/**
	 * Wrap table for HTML table serializing
	 *
	 * @param tableString
	 * @return
	 */
	@JsonIgnore
	protected String wrapTable(final String tableString) {
		return ImplementationConstants.REPORT_TABLE.replace("|", tableString);
	}

	/**
	 * Wrap table row for HTML table serializing
	 *
	 * @param tableString
	 * @return
	 */
	@JsonIgnore
	protected String wrapRow(String tableString) {
		if (tableString == null) {
			tableString = "";
		}

		return ImplementationConstants.REPORT_TABLE_ROW.replace("#", "")
				.replace("|", tableString);
	}

	/**
	 * Wrap table row for HTML table serializing
	 *
	 * @param tableString
	 * @param additionalStyle
	 * @return
	 */
	@JsonIgnore
	protected String wrapRow(String tableString, final String additionalStyle) {
		if (tableString == null) {
			tableString = "";
		}

		return ImplementationConstants.REPORT_TABLE_ROW.replace("#",
				"style=\"" + additionalStyle + "\"").replace("|", tableString);
	}

	/**
	 * Wrap table header for HTML table serializing
	 *
	 * @param tableString
	 * @return
	 */
	@JsonIgnore
	protected String wrapHeader(String tableString) {
		if (tableString == null) {
			tableString = "";
		}

		return ImplementationConstants.REPORT_TABLE_HEADER_FIELD.replace("#",
				"").replace("|", tableString);
	}

	/**
	 * Wrap table header for HTML table serializing
	 *
	 * @param tableString
	 * @param additionalStyle
	 * @return
	 */
	@JsonIgnore
	protected String wrapHeader(String tableString, final String additionalStyle) {
		if (tableString == null) {
			tableString = "";
		}

		return ImplementationConstants.REPORT_TABLE_HEADER_FIELD.replace("#",
				"style=\"" + additionalStyle + "\"").replace("|", tableString);
	}

	/**
	 * Wrap table field for HTML table serializing
	 *
	 * @param tableString
	 * @return
	 */
	@JsonIgnore
	protected String wrapField(String tableString) {
		if (tableString == null) {
			tableString = "";
		}

		return ImplementationConstants.REPORT_TABLE_NORMAL_FIELD.replace("#",
				"").replace("|", tableString);
	}

	/**
	 * Wrap table field for HTML table serializing
	 *
	 * @param tableString
	 * @param additionalStyle
	 * @return
	 */
	@JsonIgnore
	protected String wrapField(String tableString, final String additionalStyle) {
		if (tableString == null) {
			tableString = "";
		}

		return ImplementationConstants.REPORT_TABLE_NORMAL_FIELD.replace("#",
				"style=\"" + additionalStyle + "\"").replace("|", tableString);
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
			table += wrapRow(wrapHeader(locale.getDisplayLanguage() + ":")
					+ wrapField(escape(lString.get(locale))));
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
	 * Formats the given boolean as status
	 *
	 * @param status
	 * @return
	 */
	@JsonIgnore
	protected String formatStatus(final boolean status) {
		return status ? "✅" : "🔴";
	}

	/**
	 * Formats the given boolean as yes/no
	 *
	 * @param status
	 * @return
	 */
	@JsonIgnore
	protected String formatYesNo(final boolean yesNo) {
		return yesNo ? "<span class=\"yes\">YES 👍</span>"
				: "<span class=\"no\">NO ✋</span>";
	}

	/**
	 * Formats the given warning
	 *
	 * @param warning
	 * @return
	 */
	@JsonIgnore
	protected String formatWarning(final String warning) {
		return "⚠️ " + warning;
	}

	/**
	 * Creates a link
	 *
	 * @param reference
	 * @param name
	 * @return
	 */
	@JsonIgnore
	protected String createLink(final String reference, final String name) {
		return "<a href=\"" + reference + "\" target=\"_blank\">"
				+ escape(name) + "</a>";
	}
}
