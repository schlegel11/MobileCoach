package ch.ethz.mc.services.types;

/* ##LICENSE## */
import ch.ethz.mc.model.ModelObject;

/**
 * Contains all available exchange formats for {@link ModelObject}s
 *
 * @author Andreas Filler
 */
public enum ModelObjectExchangeFormatTypes {
	INTERVENTION,
	SURVEY,
	PARTICIPANTS,
	SCREENING_SURVEY_SLIDE,
	FEEDBACK_SLIDE,
	MONITORING_MESSAGE,
	MICRO_DIALOG,
	MICRO_DIALOG_MESSAGE,
	MICRO_DIALOG_DECISION_POINT,
	MICRO_DIALOG_RULE,
	MONITORING_RULE,
	MONITORING_REPLY_RULE;
}