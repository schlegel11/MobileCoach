package org.isgf.mhc.model.persistent;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.ui.UIFeedback;
import org.isgf.mhc.model.ui.UIModelObject;

/**
 * {@link ModelObject} to represent an {@link Feedback}
 * 
 * After a {@link Participant} participated in an {@link ScreeningSurvey},
 * she/he can be redirected to a {@link Feedback} A {@link Feedback} consist of
 * several {@link FeedbackSlide}s
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class Feedback extends ModelObject {
	/**
	 * A absolutely unique Id to enable to reference a {@link Feedback} also
	 * after independent export/import to/from
	 * another system
	 */
	@Getter
	@Setter
	@NonNull
	private String		globalUniqueId;

	/**
	 * The {@link ScreeningSurvey} the {@link Feedback} belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	screeningSurvey;

	/**
	 * The name of the {@link Feedback} as displayed in the backend
	 */
	@Getter
	@Setter
	@NonNull
	private String		name;

	/**
	 * The path of the template for the {@link FeedbackSlide}s
	 */
	@Getter
	@Setter
	@NonNull
	private String		templatePath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val feedback = new UIFeedback(name);

		feedback.setRelatedModelObject(this);

		return feedback;
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

		// Add feedback slide
		for (val feedbackSlide : ModelObject.find(FeedbackSlide.class,
				Queries.FEEDBACK_SLIDE__BY_FEEDBACK, getId())) {
			feedbackSlide
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
		// Delete slides
		val slidesToDelete = ModelObject.find(FeedbackSlide.class,
				Queries.FEEDBACK_SLIDE__BY_FEEDBACK, getId());
		ModelObject.delete(slidesToDelete);
	}
}
