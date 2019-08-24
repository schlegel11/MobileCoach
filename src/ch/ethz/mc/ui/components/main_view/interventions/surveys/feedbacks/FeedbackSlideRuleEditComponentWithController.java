package ch.ethz.mc.ui.components.main_view.interventions.surveys.feedbacks;

/* ##LICENSE## */
import org.bson.types.ObjectId;

import ch.ethz.mc.model.memory.types.RuleTypes;
import ch.ethz.mc.model.persistent.FeedbackSlideRule;
import ch.ethz.mc.ui.components.basics.AbstractRuleEditComponentWithController;

/**
 * Extends the feedback slide rule edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class FeedbackSlideRuleEditComponentWithController
		extends FeedbackSlideRuleEditComponent {

	private final AbstractRuleEditComponentWithController ruleEditComponent;

	public FeedbackSlideRuleEditComponentWithController(
			final FeedbackSlideRule feedbackSlideRule,
			final ObjectId screeningSurveyId) {
		super();

		// Configure integrated components
		ruleEditComponent = getAbstractRuleEditComponentWithController();
		ruleEditComponent.init(screeningSurveyId, RuleTypes.FEEDBACK_RULES);
		ruleEditComponent.adjust(feedbackSlideRule);

		/*
		 * Adjust own components
		 */
	}
}
