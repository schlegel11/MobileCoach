package ch.ethz.mc.model.ui;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIMonitoringMessage extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ORDER					= "order";
	public static final String	TEXT_WITH_PLACEHOLDERS	= "textWithPlaceholders";
	public static final String	HAS_LINKED_MEDIA_OBJECT	= "hasLinkedMediaObject";
	public static final String	RESULT_VARIABLE			= "resultVariable";
	public static final String	CONTAINS_RULES			= "containsRules";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(TEXT_WITH_PLACEHOLDERS)
	private String				textWithPlaceholders;

	private boolean				booleanHasLinkedMediaObject;

	@PropertyId(HAS_LINKED_MEDIA_OBJECT)
	private String				hasLinkedMediaObject;

	@PropertyId(RESULT_VARIABLE)
	private String				resultVariable;

	@PropertyId(CONTAINS_RULES)
	private int					containsRules;

	public static Object[] getVisibleColumns() {
		return new Object[] { TEXT_WITH_PLACEHOLDERS, HAS_LINKED_MEDIA_OBJECT,
				RESULT_VARIABLE, CONTAINS_RULES };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__MESSAGE_TEXT),
				localize(AdminMessageStrings.UI_COLUMNS__HAS_LINKED_MEDIA_OBJECT),
				localize(AdminMessageStrings.UI_COLUMNS__RESULT_VARIABLE),
				localize(AdminMessageStrings.UI_COLUMNS__CONTAINS_RULES) };
	}

	public static String getSortColumn() {
		return ORDER;
	}
}
