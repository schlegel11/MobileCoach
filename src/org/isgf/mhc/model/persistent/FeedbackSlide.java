package org.isgf.mhc.model.persistent;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.ui.UIFeedbackSlide;
import org.isgf.mhc.model.ui.UIModelObject;

/**
 * {@link ModelObject} to represent an {@link FeedbackSlide}
 * 
 * A {@link Feedback} consists of several {@link FeedbackSlide}s,
 * which are presented to a {@link Participant} in a fixed order, but only when
 * the defined {@link FeedbackSlideRule}s are all true.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackSlide extends ModelObject {
	/**
	 * The {@link Feedback} the {@link FeedbackSlide} belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	feedback;

	/**
	 * The position of the {@link FeedbackSlide} compared to all other
	 * {@link FeedbackSlide}s; the first slide will be presented to the
	 * {@link Participant}; afterwards the next in this order will be
	 * presented, if all rules return <code>true</code>, otherwise the next
	 * slide it tested
	 */
	@Getter
	@Setter
	private int			order;

	/**
	 * The title of the {@link FeedbackSlide} presented to the
	 * {@link Participant} containing placeholders
	 * for variables
	 */
	@Getter
	@Setter
	@NonNull
	private String		titleWithPlaceholders;

	/**
	 * Enables to add an optional layout attribute for the template generation
	 * in addition to the template set by the question type containing
	 * placeholders
	 * for variables
	 */
	@Getter
	@Setter
	private String		optionalLayoutAttributeWithPlaceholders;

	/**
	 * <strong>OPTIONAL:</strong> A {@link MediaObject} can be linked to be
	 * presented on the {@link FeedbackSlide}
	 */
	@Getter
	@Setter
	private ObjectId	linkedMediaObject;

	/**
	 * The text presented to the {@link Participant} containing placeholders
	 * for variables
	 */
	@Getter
	@Setter
	@NonNull
	private String		textWithPlaceholders;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val feedbackSlide = new UIFeedbackSlide(order,
				titleWithPlaceholders.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: titleWithPlaceholders);

		feedbackSlide.setRelatedModelObject(this);

		return feedbackSlide;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.isgf.mhc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	public void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);

		// Linked media object
		if (linkedMediaObject != null) {
			exportList.add(ModelObject
					.get(MediaObject.class, linkedMediaObject));
		}

		// Add feedback slide rule
		for (val feedbackSlideRule : ModelObject.find(FeedbackSlideRule.class,
				Queries.FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE, getId())) {
			feedbackSlideRule
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		if (linkedMediaObject != null) {
			val mediaObjectToDelete = ModelObject.get(MediaObject.class,
					linkedMediaObject);

			if (mediaObjectToDelete != null) {
				ModelObject.delete(mediaObjectToDelete);
			}
		}

		// Delete sub rules
		val rulesToDelete = ModelObject.find(FeedbackSlideRule.class,
				Queries.FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE, getId());
		ModelObject.delete(rulesToDelete);
	}
}
