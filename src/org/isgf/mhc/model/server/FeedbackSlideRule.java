package org.isgf.mhc.model.server;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.concepts.AbstractRule;
import org.isgf.mhc.model.server.types.EquationSignTypes;

/**
 * {@link ModelObject} to represent an {@link FeedbackSlideRule}
 * 
 * A {@link FeedbackSlideRule} can evaluate if a slide should be shown
 * next. If all {@link FeedbackSlideRule}s return true a specific
 * {@link FeedbackSlide} is shown
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public class FeedbackSlideRule extends AbstractRule {
	/**
	 * Default constructor
	 */
	public FeedbackSlideRule(final ObjectId belongingFeedbackSlide,
			final int order, final String ruleWithPlaceholders,
			final EquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders);

		this.belongingFeedbackSlide = belongingFeedbackSlide;
		this.order = order;
	}

	/**
	 * The {@link FeedbackSlide} this rule belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	belongingFeedbackSlide;

	/**
	 * The position of the {@link FeedbackSlideRule} compared to all
	 * other {@link FeedbackSlideRule}s; the first rule will be called
	 * first; if a rule does not redirect to another
	 * {@link ScreeningSurveySlide} the next rule will be performed.
	 */
	@Getter
	@Setter
	private int			order;
}
