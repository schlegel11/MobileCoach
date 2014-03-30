package org.isgf.mhc.model.server;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.concepts.AbstractRule;
import org.isgf.mhc.model.server.types.EquationSignTypes;
import org.isgf.mhc.model.ui.UIFeedbackSlideRule;
import org.isgf.mhc.model.ui.UIModelObject;
import org.isgf.mhc.tools.StringHelpers;

/**
 * {@link ModelObject} to represent an {@link FeedbackSlideRule}
 * 
 * A {@link FeedbackSlideRule} can evaluate if the belonging
 * {@link FeedbackSlide} should be shown. If all {@link FeedbackSlideRule}s
 * return true a specific {@link FeedbackSlide} is shown.
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val screeningSurveySlide = new UIFeedbackSlideRule(order,
				StringHelpers.createRuleName(this));

		screeningSurveySlide.setRelatedModelObject(this);

		return screeningSurveySlide;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.isgf.mhc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}
}
