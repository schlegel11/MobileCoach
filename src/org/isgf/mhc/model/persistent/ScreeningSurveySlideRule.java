package org.isgf.mhc.model.persistent;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.persistent.concepts.AbstractRule;
import org.isgf.mhc.model.persistent.types.RuleEquationSignTypes;
import org.isgf.mhc.model.ui.UIModelObject;
import org.isgf.mhc.model.ui.UIScreeningSurveySlideRule;
import org.isgf.mhc.tools.StringHelpers;

/**
 * {@link ModelObject} to represent an {@link ScreeningSurveySlideRule}
 * 
 * A {@link ScreeningSurveySlideRule} can evaluate which slide should be shown
 * next. Several rules can be defined to make complex decision since always the
 * next rule in the order is perfomed, if no redirect to another
 * {@link ScreeningSurveySlide} happens.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
public class ScreeningSurveySlideRule extends AbstractRule {
	/**
	 * Default constructor
	 */
	public ScreeningSurveySlideRule(
			final ObjectId belongingScreeningSurveySlide, final int order,
			final ObjectId nextScreeningSurveySlideWhenTrue,
			final ObjectId nextScreeningSurveySlideWhenFalse,
			final String ruleWithPlaceholders,
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders);

		this.belongingScreeningSurveySlide = belongingScreeningSurveySlide;
		this.order = order;
		this.nextScreeningSurveySlideWhenTrue = nextScreeningSurveySlideWhenTrue;
		this.nextScreeningSurveySlideWhenFalse = nextScreeningSurveySlideWhenFalse;
	}

	/**
	 * The {@link ScreeningSurveySlide} this rule belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	belongingScreeningSurveySlide;

	/**
	 * The position of the {@link ScreeningSurveySlideRule} compared to all
	 * other {@link ScreeningSurveySlideRule}s; the first rule will be called
	 * first; if a rule does not redirect to another
	 * {@link ScreeningSurveySlide} the next rule will be performed.
	 */
	@Getter
	@Setter
	private int			order;

	/**
	 * <strong>OPTIONAL:</strong> If the rule result is <strong>true</strong>
	 * the {@link Participant} will be redirected to the given
	 * {@link ScreeningSurveySlide}
	 */
	@Getter
	@Setter
	private ObjectId	nextScreeningSurveySlideWhenTrue;

	/**
	 * <strong>OPTIONAL:</strong> If the rule result is <strong>false</strong>
	 * the {@link Participant} will be redirected to the given
	 * {@link ScreeningSurveySlide}
	 */
	@Getter
	@Setter
	private ObjectId	nextScreeningSurveySlideWhenFalse;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val screeningSurveySlide = new UIScreeningSurveySlideRule(
				order,
				StringHelpers.createRuleName(this),
				nextScreeningSurveySlideWhenTrue != null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__YES)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NO),
				nextScreeningSurveySlideWhenFalse != null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__YES)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NO));
		;

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
