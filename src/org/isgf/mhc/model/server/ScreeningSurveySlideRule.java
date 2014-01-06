package org.isgf.mhc.model.server;

import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.server.concepts.AbstractRule;
import org.isgf.mhc.model.types.EquationSignTypes;

/**
 * {@link ModelObject} to represent an {@link ScreeningSurveySlideRule}
 * 
 * @author Andreas Filler <andreas@filler.name>
 */
public class ScreeningSurveySlideRule extends AbstractRule {
	/**
	 * Default constructor
	 */
	public ScreeningSurveySlideRule(final String ruleWithPlaceholders,
			final EquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders);
		// TODO Auto-generated constructor stub
	}

}
