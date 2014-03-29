package org.isgf.mhc.model.server;

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
import org.isgf.mhc.model.server.types.ScreeningSurveySlideQuestionTypes;
import org.isgf.mhc.model.ui.UIModelObject;
import org.isgf.mhc.model.ui.UIScreeningSurveySlide;

/**
 * {@link ModelObject} to represent an {@link ScreeningSurveySlide}
 * 
 * A {@link ScreeningSurvey} consists of several {@link ScreeningSurveySlide}s,
 * which are presented to a {@link Participant} in a dynamic order. The order
 * can be defined by using the rules or by defining a default next slide.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningSurveySlide extends ModelObject {
	/**
	 * The {@link ScreeningSurvey} the {@link ScreeningSurveySlide} belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId							screeningSurvey;

	/**
	 * The position of the {@link ScreeningSurveySlide} compared to all other
	 * {@link ScreeningSurveySlide}s; the first slide will be presented to the
	 * {@link Participant}; if no {@link ScreeningSurveySlideRule} leads to
	 * another {@link ScreeningSurveySlide} the next in this order will be
	 * presented
	 */
	@Getter
	@Setter
	private int									order;

	/**
	 * The title of the {@link ScreeningSurveySlide} presented to the
	 * {@link Participant} containing placeholders
	 * for variables
	 */
	@Getter
	@Setter
	@NonNull
	private String								titleWithPlaceholders;

	/**
	 * Enables to add an optional layout attribute for the template generation
	 * in addition to the template set by the question type
	 */
	@Getter
	@Setter
	private String								optionalLayoutAttribute;

	/**
	 * <strong>OPTIONAL:</strong> A {@link MediaObject} can be linked to be
	 * presented on the {@link ScreeningSurveySlide}
	 */
	@Getter
	@Setter
	private ObjectId							linkedMediaObject;

	/**
	 * The question presented to the {@link Participant} containing placeholders
	 * for variables
	 */
	@Getter
	@Setter
	@NonNull
	private String								questionWithPlaceholders;

	/**
	 * The type of the question presented to the {@link Participant} of all
	 * available {@link ScreeningSurveySlideQuestionTypes}
	 */
	@Getter
	@Setter
	@NonNull
	private ScreeningSurveySlideQuestionTypes	questionType;

	/**
	 * Slide presentation stops after this slide and {@link ScreeningSurvey} is
	 * marked as performed by {@link Participant}
	 */
	@Getter
	@Setter
	private boolean								isLastSlide;

	/**
	 * <strong>OPTIONAL:</strong> The {@link Feedback} to hand over to if set
	 */
	@Getter
	@Setter
	private ObjectId							handsOverToFeedback;

	/**
	 * <strong>OPTIONAL:</strong> The answers presented to the
	 * {@link Participant} for selection
	 */
	@Getter
	@Setter
	private String[]							answersWithPlaceholders;

	/**
	 * <strong>OPTIONAL:</strong> The return values of the answers presented to
	 * the {@link Participant} for selection; the selected answer value can be
	 * stored
	 */
	@Getter
	@Setter
	private String[]							answerValues;

	/**
	 * <strong>OPTIONAL:</strong> The answer that is preselected when the
	 * {@link Participant} comes to the {@link ScreeningSurveySlide}
	 */
	@Getter
	@Setter
	private int									preSelectedAnswer;

	/**
	 * <strong>OPTIONAL:</strong> The name of the variable in which the value of
	 * the answer which was selected by the {@link Participant} should be stored
	 */
	@Getter
	@Setter
	private String								storeValueToVariableWithName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.isgf.mhc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val screeningSurveySlide = new UIScreeningSurveySlide(
				order,
				titleWithPlaceholders.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: titleWithPlaceholders,
				questionType.toString(),
				storeValueToVariableWithName != null ? storeValueToVariableWithName
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET));

		screeningSurveySlide.setRelatedModelObject(this);

		return screeningSurveySlide;
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
		val rulesToDelete = ModelObject.find(ScreeningSurveySlideRule.class,
				Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
				getId());
		ModelObject.delete(rulesToDelete);
	}
}
