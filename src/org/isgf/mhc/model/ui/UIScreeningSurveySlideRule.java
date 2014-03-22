package org.isgf.mhc.model.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.isgf.mhc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIScreeningSurveySlideRule extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ORDER						= "order";
	public static final String	RULE						= "rule";
	public static final String	JUMP_TO_SLIDE_WHEN_TRUE		= "jumpToSlideWhenTrue";
	public static final String	JUMP_TO_SLIDE_WHEN_FALSE	= "jumpToSlideWhenFalse";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(RULE)
	private String				rule;

	@PropertyId(JUMP_TO_SLIDE_WHEN_TRUE)
	private String				jumpToSlideWhenTrue;

	@PropertyId(JUMP_TO_SLIDE_WHEN_FALSE)
	private String				jumpToSlideWhenFalse;

	public static Object[] getVisibleColumns() {
		return new Object[] { RULE, JUMP_TO_SLIDE_WHEN_TRUE,
				JUMP_TO_SLIDE_WHEN_FALSE };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(AdminMessageStrings.UI_COLUMNS__RULE),
				localize(AdminMessageStrings.UI_COLUMNS__JUMP_TO_SLIDE_WHEN_TRUE),
				localize(AdminMessageStrings.UI_COLUMNS__JUMP_TO_SLIDE_WHEN_FALSE) };
	}

	public static String getSortColumn() {
		return ORDER;
	}
}
