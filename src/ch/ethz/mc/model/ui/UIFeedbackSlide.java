package ch.ethz.mc.model.ui;

/* ##LICENSE## */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ch.ethz.mc.conf.AdminMessageStrings;

import com.vaadin.data.fieldgroup.PropertyId;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UIFeedbackSlide extends UIModelObject {
	// NOTE: The String values have to fit the name of the variables
	public static final String	ORDER					= "order";
	public static final String	TITLE_WITH_PLACEHOLDERS	= "titleWithPlaceholders";
	public static final String	COMMENT					= "comment";

	@PropertyId(ORDER)
	private int					order;

	@PropertyId(TITLE_WITH_PLACEHOLDERS)
	private String				titleWithPlaceholders;

	@PropertyId(COMMENT)
	private String				comment;

	public static Object[] getVisibleColumns() {
		return new Object[] { TITLE_WITH_PLACEHOLDERS, COMMENT };
	}

	public static String[] getColumnHeaders() {
		return new String[] {
				localize(
						AdminMessageStrings.UI_COLUMNS__SLIDE_TITLE_WITH_PLACEHOLDERS),
				localize(AdminMessageStrings.UI_COLUMNS__COMMENT) };
	}

	public static String getSortColumn() {
		return ORDER;
	}

	@Override
	public String toString() {
		return titleWithPlaceholders;
	}
}
