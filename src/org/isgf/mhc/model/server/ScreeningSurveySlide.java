package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.types.ScreeningSurveySlideQuestionTypes;

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
	 * Defines the layout of the {@link ScreeningSurveySlide}
	 * 
	 * @TODO Not solved how to do this! Are CSS classes enough? Would be good.
	 */
	@Getter
	@Setter
	private String								layout;

	/**
	 * The title of the {@link ScreeningSurveySlide} presented to the
	 * {@link Participant} containing placeholders
	 * for variables
	 */
	@Getter
	@Setter
	private String								titleWithPlaceholders;

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
	private String								questionWithPlaceholders;

	/**
	 * The type of the question presented to the {@link Participant} of all
	 * available {@link ScreeningSurveySlideQuestionTypes}
	 */
	@Getter
	@Setter
	private ScreeningSurveySlideQuestionTypes	questionType;

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
}
