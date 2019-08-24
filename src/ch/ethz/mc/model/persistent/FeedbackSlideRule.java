package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.concepts.AbstractRule;
import ch.ethz.mc.model.persistent.types.RuleEquationSignTypes;
import ch.ethz.mc.model.ui.UIFeedbackSlideRule;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.tools.StringHelpers;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

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
	private static final long serialVersionUID = -4582903243084282634L;

	/**
	 * Default constructor
	 */
	public FeedbackSlideRule(final ObjectId belongingFeedbackSlide,
			final int order, final String ruleWithPlaceholders,
			final RuleEquationSignTypes ruleEquationSign,
			final String ruleComparisonTermWithPlaceholders,
			final String comment) {
		super(ruleWithPlaceholders, ruleEquationSign,
				ruleComparisonTermWithPlaceholders, comment);

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
	 * The position of the {@link FeedbackSlideRule} compared to all other
	 * {@link FeedbackSlideRule}s; the first rule will be called first
	 */
	@Getter
	@Setter
	private int			order;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val screeningSurveySlide = new UIFeedbackSlideRule(order,
				StringHelpers.createRuleName(this, true));

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
		String table = wrapRow(wrapHeader("Rule:")
				+ wrapField(escape(StringHelpers.createRuleName(this, false))));
		table += wrapRow(
				wrapHeader("Comment:") + wrapField(escape(getComment())));

		return wrapTable(table);
	}
}
