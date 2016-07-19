package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2013-2015 MobileCoach Team at the Health-IS Lab
 *
 * For details see README.md file in the root folder of this project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.UIScreeningSurveySlideRule;
import ch.ethz.mc.tools.StringHelpers;

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
			final boolean showSameSlideBecauseValueNotValidWhenTrue,
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
		this.showSameSlideBecauseValueNotValidWhenTrue = showSameSlideBecauseValueNotValidWhenTrue;
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

	/**
	 * If the rule result is <strong>true</strong> and this value is
	 * <strong>true</strong> as well the rule execution will be stopped and the
	 * same slide will be shown again
	 */
	@Getter
	@Setter
	private boolean		showSameSlideBecauseValueNotValidWhenTrue;

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		String slideNameWhenTrue = Messages
				.getAdminString(AdminMessageStrings.UI_MODEL__UNKNOWN);
		String slideNameWhenFalse = Messages
				.getAdminString(AdminMessageStrings.UI_MODEL__UNKNOWN);

		if (nextScreeningSurveySlideWhenTrue != null) {
			val slideWhenTrue = MC.getInstance()
					.getScreeningSurveyAdministrationManagerService()
					.getScreeningSurveySlide(nextScreeningSurveySlideWhenTrue);
			if (slideWhenTrue != null) {
				slideNameWhenTrue = slideWhenTrue.getTitleWithPlaceholders()
						.toString().equals("") ? ImplementationConstants.DEFAULT_OBJECT_NAME
								: slideWhenTrue.getTitleWithPlaceholders().toString();
			}
		}
		if (nextScreeningSurveySlideWhenFalse != null) {
			val slideWhenFalse = MC.getInstance()
					.getScreeningSurveyAdministrationManagerService()
					.getScreeningSurveySlide(nextScreeningSurveySlideWhenFalse);
			if (slideWhenFalse != null) {
				slideNameWhenFalse = slideWhenFalse.getTitleWithPlaceholders()
						.toString().equals("") ? ImplementationConstants.DEFAULT_OBJECT_NAME
								: slideWhenFalse.getTitleWithPlaceholders().toString();
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
								isShowSameSlideBecauseValueNotValidWhenTrue() ? Messages
										.getAdminString(AdminMessageStrings.UI_MODEL__YES)
										: Messages
										.getAdminString(AdminMessageStrings.UI_MODEL__NO),
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
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}
}
