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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.ui.UIFeedbackSlide;
import ch.ethz.mc.model.ui.UIModelObject;

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
	private LString		titleWithPlaceholders;

	/**
	 * A comment for the author, not visible to any participant
	 */
	@Getter
	@Setter
	@NonNull
	private String		comment;

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
	private LString		textWithPlaceholders;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val feedbackSlide = new UIFeedbackSlide(order,
				titleWithPlaceholders.isEmpty() ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: titleWithPlaceholders.toShortenedString(40),
						comment.equals("") ? Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
								: comment);

		feedbackSlide.setRelatedModelObject(this);

		return feedbackSlide;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
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
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
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
