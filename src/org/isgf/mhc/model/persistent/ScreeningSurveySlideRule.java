package org.isgf.mhc.model.persistent;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.isgf.mhc.MHC;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.ImplementationContants;
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
			final int level, final String valueToStoreToVariable,
			final String storeValueToVariableWithName,
			final ObjectId nextScreeningSurveySlideWhenTrue,
			final ObjectId nextScreeningSurveySlideWhenFalse,
			final String ruleWithPlaceholders,
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders);

		this.belongingScreeningSurveySlide = belongingScreeningSurveySlide;
		this.order = order;
		this.level = level;
		this.valueToStoreToVariable = valueToStoreToVariable;
		this.storeValueToVariableWithName = storeValueToVariableWithName;
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
	 * The rule will only be evaluated if the level is the same or lower as the
	 * former rule or if the former rule evaluated as true.
	 */
	@Getter
	@Setter
	private int			level;

	/**
	 * <strong>OPTIONAL:</strong> If the former value of the
	 * {@link ScreeningSurveySlideRule} should be stored in the case the rule
	 * matches, the name of the
	 * appropriate variable can be set here.
	 */
	@Getter
	@Setter
	private String		valueToStoreToVariable;

	/**
	 * <strong>OPTIONAL:</strong> If the former value of the
	 * {@link ScreeningSurveySlideRule} should be stored in the case the rule
	 * matches, the name of the
	 * appropriate variable can be set here.
	 */
	@Getter
	@Setter
	private String		storeValueToVariableWithName;

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
		String slideNameWhenTrue = Messages
				.getAdminString(AdminMessageStrings.UI_MODEL__UNKNOWN);
		String slideNameWhenFalse = Messages
				.getAdminString(AdminMessageStrings.UI_MODEL__UNKNOWN);

		if (nextScreeningSurveySlideWhenTrue != null) {
			val slideWhenTrue = MHC.getInstance()
					.getScreeningSurveyAdministrationManagerService()
					.getScreeningSurveySlide(nextScreeningSurveySlideWhenTrue);
			if (slideWhenTrue != null) {
				slideNameWhenTrue = slideWhenTrue.getTitleWithPlaceholders()
						.equals("") ? ImplementationContants.DEFAULT_OBJECT_NAME
						: slideWhenTrue.getTitleWithPlaceholders();
			}
		}
		if (nextScreeningSurveySlideWhenFalse != null) {
			val slideWhenFalse = MHC.getInstance()
					.getScreeningSurveyAdministrationManagerService()
					.getScreeningSurveySlide(nextScreeningSurveySlideWhenTrue);
			if (slideWhenFalse != null) {
				slideNameWhenFalse = slideWhenFalse.getTitleWithPlaceholders()
						.equals("") ? ImplementationContants.DEFAULT_OBJECT_NAME
						: slideWhenFalse.getTitleWithPlaceholders();
			}
		}

		val screeningSurveySlide = new UIScreeningSurveySlideRule(
				order,
				StringUtils.repeat(" → ", level)
						+ StringHelpers.createRuleName(this),
				storeValueToVariableWithName == null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: Messages
								.getAdminString(
										AdminMessageStrings.SCREENING_SURVEY_SLIDE_RULE_EDITING__VALUE_TO_VARIABLE,
										valueToStoreToVariable,
										storeValueToVariableWithName),
				nextScreeningSurveySlideWhenTrue != null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__YES)
						+ ": " + slideNameWhenTrue : Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NO),
				nextScreeningSurveySlideWhenFalse != null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__YES)
						+ ": " + slideNameWhenFalse : Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NO));

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
