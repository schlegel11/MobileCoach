package ch.ethz.mc.model.ui;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIScreeningSurveySlideRule extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ORDER						= "order";
	public static final String	RULE						= "rule";
	public static final String	VARIABLE					= "variable";
	public static final String	SAME_SLIDE_WHEN_TRUE		= "sameSlideWhenTrue";
	public static final String	JUMP_TO_SLIDE_WHEN_TRUE		= "jumpToSlideWhenTrue";
	public static final String	JUMP_TO_SLIDE_WHEN_FALSE	= "jumpToSlideWhenFalse";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(RULE)
	private String				rule;

	@PropertyId(VARIABLE)
	private String				variable;

	@PropertyId(SAME_SLIDE_WHEN_TRUE)
	private String				sameSlideWhenTrue;

	@PropertyId(JUMP_TO_SLIDE_WHEN_TRUE)
	private String				jumpToSlideWhenTrue;

	@PropertyId(JUMP_TO_SLIDE_WHEN_FALSE)
	private String				jumpToSlideWhenFalse;

	public static Object[] getVisibleColumns() {
		return new Object[] { RULE, VARIABLE, SAME_SLIDE_WHEN_TRUE,
				JUMP_TO_SLIDE_WHEN_TRUE, JUMP_TO_SLIDE_WHEN_FALSE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__RULE),
				localize(AdminMessageStrings.UI_COLUMNS__VARIABLE),
				localize(AdminMessageStrings.UI_COLUMNS__SHOW_SAME_SLIDE_IF_INVALID),
				localize(AdminMessageStrings.UI_COLUMNS__JUMP_TO_SLIDE_WHEN_TRUE),
				localize(AdminMessageStrings.UI_COLUMNS__JUMP_TO_SLIDE_WHEN_FALSE) };
	}

	public static String getSortColumn() {
		return ORDER;
	}
}
