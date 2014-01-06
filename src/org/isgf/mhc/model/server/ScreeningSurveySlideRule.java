package org.isgf.mhc.model.server;

import lombok.Getter;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.concepts.AbstractRule;
import org.isgf.mhc.model.types.EquationSignTypes;
import org.jongo.Oid;

/**
 * {@link ModelObject} to represent an {@link ScreeningSurveySlideRule}
 * 
 * A {@link ScreeningSurveySlideRule} can evaluate which slide should be shown
 * next. Several rules can be defined to make complex decision since always the
 * next rule in the order is perfomed, if no redirect to another
 * {@link ScreeningSurveySlide} happens.
 * 
 * @author Andreas Filler <andreas@filler.name>
 */
public class ScreeningSurveySlideRule extends AbstractRule {
	/**
	 * Default constructor
	 */
	public ScreeningSurveySlideRule(final Oid belongingScreeningSurveySlide,
			final int order, final Oid nextScreeningSurveySlideWhenTrue,
			final Oid nextScreeningSurveySlideWhenFalse,
			final String ruleWithPlaceholders,
			final EquationSignTypes ruleEquationSign,
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
	private Oid	belongingScreeningSurveySlide;

	/**
	 * The position of the {@link ScreeningSurveySlideRule} compared to all
	 * other {@link ScreeningSurveySlideRule}s; the first rule will be called
	 * first; if a rule does not redirect to another
	 * {@link ScreeningSurveySlide} the next rule will be performed.
	 */
	@Getter
	@Setter
	private int	order;

	/**
	 * <strong>OPTIONAL:</string> If the rule result is <strong>true</strong>
	 * the {@link Participant} will be redirected to the given
	 * {@link ScreeningSurveySlide}
	 */
	@Getter
	@Setter
	private Oid	nextScreeningSurveySlideWhenTrue;

	/**
	 * <strong>OPTIONAL:</string> If the rule result is <strong>false</strong>
	 * the {@link Participant} will be redirected to the given
	 * {@link ScreeningSurveySlide}
	 */
	@Getter
	@Setter
	private Oid	nextScreeningSurveySlideWhenFalse;
}
