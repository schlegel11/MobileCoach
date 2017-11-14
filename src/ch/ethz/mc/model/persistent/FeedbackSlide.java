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
import ch.ethz.mc.tools.StringHelpers;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
				titleWithPlaceholders.isEmpty()
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
						: titleWithPlaceholders.toShortenedString(40),
				comment.equals("")
						? Messages.getAdminString(
								AdminMessageStrings.UI_MODEL__NOT_SET)
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
			exportList
					.add(ModelObject.get(MediaObject.class, linkedMediaObject));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		String table = wrapRow(wrapHeader("Title:")
				+ wrapField(escape(titleWithPlaceholders)));
		table += wrapRow(wrapHeader("Comment:") + wrapField(escape(comment)));
		table += wrapRow(wrapHeader("Optional Layout Attributes:")
				+ wrapField(escape(optionalLayoutAttributeWithPlaceholders)));
		table += wrapRow(
				wrapHeader("Text:") + wrapField(escape(textWithPlaceholders)));

		if (linkedMediaObject != null) {
			val mediaObject = ModelObject.get(MediaObject.class,
					linkedMediaObject);
			if (mediaObject != null) {
				String externalReference;
				if (mediaObject.getFileReference() != null) {
					externalReference = "javascript:showMediaObject('"
							+ mediaObject.getId() + "/" + StringHelpers
									.cleanFilenameString(mediaObject.getName())
							+ "')";
				} else {
					externalReference = mediaObject.getUrlReference();
				}

				table += wrapRow(wrapHeader("Linked Media Object:") + wrapField(
						createLink(externalReference, mediaObject.getName())));
			} else {
				table += wrapRow(wrapHeader("Linked Media Object:") + wrapField(
						formatWarning("Media Object set, but not found")));
			}
		}

		// Slide Rules
		final StringBuffer buffer = new StringBuffer();
		val rules = ModelObject.findSorted(FeedbackSlideRule.class,
				Queries.FEEDBACK_SLIDE_RULE__BY_FEEDBACK_SLIDE,
				Queries.FEEDBACK_SLIDE_RULE__SORT_BY_ORDER_ASC, getId());

		for (val rule : rules) {
			buffer.append(rule.toTable());
		}

		if (buffer.length() > 0) {
			table += wrapRow(
					wrapHeader("Rules:") + wrapField(buffer.toString()));
		}

		return wrapTable(table);
	}
}
