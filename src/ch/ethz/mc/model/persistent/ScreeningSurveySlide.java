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

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.types.ScreeningSurveySlideQuestionTypes;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.UIScreeningSurveySlide;

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
	 * Enables to add an optional layout attribute for the template generation
	 * in addition to the template set by the question type containing
	 * placeholders
	 * for variables
	 */
	@Getter
	@Setter
	private String								optionalLayoutAttributeWithPlaceholders;

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

	/**
	 * <strong>OPTIONAL:</strong> The value the variable should have if the
	 * screening survey is executed automatically, because the participant
	 * stopped filling it manually
	 */
	@Getter
	@Setter
	@NonNull
	private String								defaultValue;

	/**
	 * The error message to show if the value does not match the validation
	 * steps realized as rules
	 */
	@Getter
	@Setter
	@NonNull
	private String								validationErrorMessage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		int slideRules = 0;
		val screeningSurveySlideRules = MC.getInstance()
				.getScreeningSurveyAdministrationManagerService()
				.getAllScreeningSurveySlideRulesOfScreeningSurveySlide(getId());

		if (screeningSurveySlideRules != null) {
			val screeningSurveySlideRulesIterator = screeningSurveySlideRules
					.iterator();

			while (screeningSurveySlideRulesIterator.hasNext()) {
				screeningSurveySlideRulesIterator.next();
				slideRules++;
			}
		}

		val screeningSurveySlide = new UIScreeningSurveySlide(
				order,
				titleWithPlaceholders.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: titleWithPlaceholders,
				questionType.toString(),
				storeValueToVariableWithName != null ? storeValueToVariableWithName
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET),
				slideRules);

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
	public void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);

		// Linked media object
		if (linkedMediaObject != null) {
			exportList.add(ModelObject
					.get(MediaObject.class, linkedMediaObject));
		}

		// Add screening survey slide rule
		for (val screeningSurveySlideRule : ModelObject.find(
				ScreeningSurveySlideRule.class,
				Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
				getId())) {
			screeningSurveySlideRule
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
		val rulesToDelete = ModelObject.find(ScreeningSurveySlideRule.class,
				Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
				getId());
		ModelObject.delete(rulesToDelete);
	}
}
