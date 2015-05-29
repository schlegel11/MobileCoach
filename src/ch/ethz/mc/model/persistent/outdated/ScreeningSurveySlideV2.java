package ch.ethz.mc.model.persistent.outdated;

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
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.MediaObject;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.ScreeningSurveySlideRule;
import ch.ethz.mc.model.persistent.types.ScreeningSurveySlideQuestionTypes;

/**
 * CAUTION: Will only be used for conversion from data model 1 to 2
 * 
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
public class ScreeningSurveySlideV2 extends ModelObject {
	/**
	 * The id of the {@link ModelObject}
	 */
	@MongoId
	@Getter
	@Setter
	private ObjectId	id;

	/**
	 * Consists of all attributes related to a {@link Question} within a
	 * {@link ScreeningSurveySlide}
	 * 
	 * @author Andreas Filler
	 */
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Question {
		/**
		 * The question presented to the {@link Participant} containing
		 * placeholders
		 * for variables
		 */
		@Getter
		@Setter
		@NonNull
		private String		questionWithPlaceholders;

		/**
		 * <strong>OPTIONAL:</strong> The answers presented to the
		 * {@link Participant} for selection
		 */
		@Getter
		@Setter
		private String[]	answersWithPlaceholders;

		/**
		 * <strong>OPTIONAL:</strong> The return values of the answers presented
		 * to
		 * the {@link Participant} for selection; the selected answer value can
		 * be
		 * stored
		 */
		@Getter
		@Setter
		private String[]	answerValues;

		/**
		 * <strong>OPTIONAL:</strong> The answer that is preselected when the
		 * {@link Participant} comes to the {@link ScreeningSurveySlide}
		 */
		@Getter
		@Setter
		private int			preSelectedAnswer;

		/**
		 * <strong>OPTIONAL:</strong> The name of the variable in which the
		 * value of
		 * the answer which was selected by the {@link Participant} should be
		 * stored
		 */
		@Getter
		@Setter
		private String		storeValueToVariableWithName;

		/**
		 * <strong>OPTIONAL:</strong> The value the variable should have if the
		 * screening survey is executed automatically, because the participant
		 * stopped filling it manually
		 */
		@Getter
		@Setter
		@NonNull
		private String		defaultValue;
	}

	/**
	 * A absolutely unique Id to enable to reference a {@link ScreeningSurvey}
	 * also after independent export/import to/from
	 * another system
	 */
	@Getter
	@Setter
	@NonNull
	private String								globalUniqueId;

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
	 * The type of the question presented to the {@link Participant} of all
	 * available {@link ScreeningSurveySlideQuestionTypes}
	 */
	@Getter
	@Setter
	@NonNull
	private ScreeningSurveySlideQuestionTypes	questionType;

	/**
	 * Enables to add an optional layout attribute for the template generation
	 * in addition to the template set by the question type containing
	 * placeholders
	 * for variables
	 */
	@Getter
	@Setter
	private String								optionalLayoutAttributeWithPlaceholders;

	/**
	 * Contains all questions in this slide
	 */
	@Getter
	private List<Question>						questions	= new ArrayList<Question>();

	/**
	 * <strong>OPTIONAL:</strong> A {@link MediaObject} can be linked to be
	 * presented on the {@link ScreeningSurveySlide}
	 */
	@Getter
	@Setter
	private ObjectId							linkedMediaObject;

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
	 * The error message to show if the value does not match the validation
	 * steps realized as rules
	 */
	@Getter
	@Setter
	@NonNull
	private String								validationErrorMessage;
}
