package ch.ethz.mc.model.memory;

/* ##LICENSE## */
import ch.ethz.mc.model.persistent.subelements.LString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Wrapper for LStrings in combination with keys for i18n export/import of
 * surveys and feedback
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class SurveysFeedbacksI18nStringsObject {
	@Getter
	@Setter
	private String	id;

	@Getter
	@Setter
	private String	description;

	@Getter
	@Setter
	private LString	title;

	@Getter
	@Setter
	private LString	text;

	@Getter
	@Setter
	private LString	errorMessage;
}
