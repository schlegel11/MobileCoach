package ch.ethz.mc.model.persistent;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
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

import org.apache.commons.lang3.StringUtils;
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

import com.fasterxml.jackson.annotation.JsonIgnore;

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
			final String ruleComparisonTermWithPlaceholders,
			final String comment) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, comment);

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
					.getSurveyAdministrationManagerService()
					.getScreeningSurveySlide(nextScreeningSurveySlideWhenTrue);
			if (slideWhenTrue != null) {
				slideNameWhenTrue = (slideWhenTrue.getTitleWithPlaceholders()
						.toString().equals("")
								? ImplementationConstants.DEFAULT_OBJECT_NAME
								: slideWhenTrue.getTitleWithPlaceholders()
										.toShortenedString(20))
						+ (!slideWhenTrue.getComment().equals("")
								? " (" + slideWhenTrue.getComment() + ")" : "");
			}
		}
		if (nextScreeningSurveySlideWhenFalse != null) {
			val slideWhenFalse = MC.getInstance()
					.getSurveyAdministrationManagerService()
					.getScreeningSurveySlide(nextScreeningSurveySlideWhenFalse);
			if (slideWhenFalse != null) {
				slideNameWhenFalse = (slideWhenFalse.getTitleWithPlaceholders()
						.toString().equals("")
								? ImplementationConstants.DEFAULT_OBJECT_NAME
								: slideWhenFalse.getTitleWithPlaceholders()
										.toShortenedString(20))
						+ (!slideWhenFalse.getComment().equals("")
								? " (" + slideWhenFalse.getComment() + ")"
								: "");
			}
		}

		val screeningSurveySlide = new UIScreeningSurveySlideRule(order,
				StringUtils.repeat(" → ", level)
						+ StringHelpers.createRuleName(this, true),
				storeValueToVariableWithName == null
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
						: Messages.getAdminString(
								AdminMessageStrings.SCREENING_SURVEY_SLIDE_RULE_EDITING__VALUE_TO_VARIABLE,
								valueToStoreToVariable,
								storeValueToVariableWithName),
				isShowSameSlideBecauseValueNotValidWhenTrue()
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES)
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO),
				nextScreeningSurveySlideWhenTrue != null
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES) + ": "
								+ slideNameWhenTrue
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO),
				nextScreeningSurveySlideWhenFalse != null
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__YES) + ": "
								+ slideNameWhenFalse
						: Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NO));

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		val style = level > 0 ? "border-left-width: " + 20 * level + "px;" : "";

		String table = wrapRow(wrapHeader("Rule:", style)
				+ wrapField(escape(StringHelpers.createRuleName(this, false))));
		table += wrapRow(wrapHeader("Comment:", style)
				+ wrapField(escape(getComment())));
		table += wrapRow(wrapHeader("Value to store to variable:", style)
				+ wrapField(escape(valueToStoreToVariable)));
		table += wrapRow(wrapHeader("Variable to store value to:", style)
				+ wrapField(escape(storeValueToVariableWithName)));
		if (nextScreeningSurveySlideWhenTrue != null) {
			val slide = ModelObject.get(ScreeningSurveySlide.class,
					nextScreeningSurveySlideWhenTrue);
			if (slide != null) {
				table += wrapRow(wrapHeader("Slide to go when TRUE:", style)
						+ wrapField(escape(slide.getTitleWithPlaceholders())
								+ escape(!slide.getComment().equals("")
										? " (" + slide.getComment() + ")"
										: "")));
			} else {
				table += wrapRow(wrapHeader("Slide to go when TRUE:", style)
						+ wrapField(formatWarning("Slide set, but not found")));
			}
		}
		if (nextScreeningSurveySlideWhenFalse != null) {
			val slide = ModelObject.get(ScreeningSurveySlide.class,
					nextScreeningSurveySlideWhenFalse);
			if (slide != null) {
				table += wrapRow(wrapHeader("Slide to go when FALSE:", style)
						+ wrapField(escape(slide.getTitleWithPlaceholders())
								+ escape(!slide.getComment().equals("")
										? " (" + slide.getComment() + ")"
										: "")));
			} else {
				table += wrapRow(wrapHeader("Slide to go when FALSE:", style)
						+ wrapField(formatWarning("Slide set, but not found")));
			}
		}
		table += wrapRow(
				wrapHeader("Show same slide when TRUE (Validation):", style)
						+ wrapField(formatYesNo(
								showSameSlideBecauseValueNotValidWhenTrue)));

		return wrapTable(table);
	}
}
