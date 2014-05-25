package org.isgf.mhc.ui.views.components.feedback;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.persistent.FeedbackSlideRule;
import org.isgf.mhc.ui.views.components.basics.AbstractRuleEditComponentWithController;
import org.isgf.mhc.ui.views.components.basics.AbstractRuleEditComponentWithController.TYPES;

/**
 * Extends the feedback slide rule edit component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class FeedbackSlideRuleEditComponentWithController extends
		FeedbackSlideRuleEditComponent {

	private final AbstractRuleEditComponentWithController	ruleEditComponent;

	public FeedbackSlideRuleEditComponentWithController(
			final FeedbackSlideRule feedbackSlideRule,
			final ObjectId screeningSurveyId) {
		super();

		// Configure integrated components
		ruleEditComponent = getAbstractRuleEditComponentWithController();
		ruleEditComponent.init(screeningSurveyId, TYPES.FEEDBACK_RULES);
		ruleEditComponent.adjust(feedbackSlideRule);

		/*
		 * Adjust own components
		 */
	}
}
