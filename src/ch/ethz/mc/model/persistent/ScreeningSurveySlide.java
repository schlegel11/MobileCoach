package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab at the Health-IS Lab
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
import lombok.val;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.AbstractSerializableTable;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.persistent.types.ScreeningSurveySlideQuestionTypes;
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.UIScreeningSurveySlide;
import ch.ethz.mc.tools.StringHelpers;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	 * Consists of all attributes related to a {@link Question} within a
	 * {@link ScreeningSurveySlide}
	 *
	 * @author Andreas Filler
	 */
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Question extends AbstractSerializableTable {
		/**
		 * The question presented to the {@link Participant} containing
		 * placeholders
		 * for variables
		 */
		@Getter
		@Setter
		@NonNull
		private LString		questionWithPlaceholders;

		/**
		 * <strong>OPTIONAL:</strong> The answers presented to the
		 * {@link Participant} for selection
		 */
		@Getter
		@Setter
		private LString[]	answersWithPlaceholders;

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

		@Override
		@JsonIgnore
		public String toTable() {
			String table = wrapRow(wrapHeader("Question:")
					+ wrapField(escape(questionWithPlaceholders)));

			for (int i = 0; i < answersWithPlaceholders.length; i++) {
				table += wrapRow(wrapHeader("Answer " + (i + 1) + ":")
						+ wrapField(escape(answersWithPlaceholders[i])));
				table += wrapRow(wrapHeader("Value " + (i + 1) + ":")
						+ wrapField(escape(answerValues[i])));
			}

			if (preSelectedAnswer >= 0) {
				table += wrapRow(wrapHeader("Preselected Answer:")
						+ wrapField(escape(answersWithPlaceholders[preSelectedAnswer])));
			}
			table += wrapRow(wrapHeader("Store value to variable:")
					+ wrapField(escape(storeValueToVariableWithName)));
			table += wrapRow(wrapHeader("Default Value:")
					+ wrapField(escape(defaultValue)));

			return wrapTable(table);
		}
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
	private LString								titleWithPlaceholders;

	/**
	 * A comment for the author, not visible to any participant
	 */
	@Getter
	@Setter
	@NonNull
	private String								comment;

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
	 * <strong>OPTIONAL:</strong> A survey to potentially hand over to if set
	 */
	@Getter
	@Setter
	private ObjectId							linkedIntermediateSurvey;

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
	private LString								validationErrorMessage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		// Number of slide rules
		int slideRules = 0;
		val screeningSurveySlideRules = MC.getInstance()
				.getSurveyAdministrationManagerService()
				.getAllScreeningSurveySlideRulesOfScreeningSurveySlide(getId());

		if (screeningSurveySlideRules != null) {
			val screeningSurveySlideRulesIterator = screeningSurveySlideRules
					.iterator();

			while (screeningSurveySlideRulesIterator.hasNext()) {
				screeningSurveySlideRulesIterator.next();
				slideRules++;
			}
		}

		// Variable names from multi-item questions
		final StringBuffer storeValueToVariableWithNames = new StringBuffer();
		for (val question : questions) {
			if (question.getStoreValueToVariableWithName() != null) {
				if (storeValueToVariableWithNames.length() > 0) {
					storeValueToVariableWithNames.append(", ");
				}

				storeValueToVariableWithNames.append(question
						.getStoreValueToVariableWithName());
			}
		}

		val screeningSurveySlide = new UIScreeningSurveySlide(
				order,
				titleWithPlaceholders.toString().equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: titleWithPlaceholders.toShortenedString(40),
				comment.equals("") ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: comment,
				questionType.toString(),
				storeValueToVariableWithNames.length() > 0 ? storeValueToVariableWithNames
						.toString() : Messages
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
		table += wrapRow(wrapHeader("Question Type:")
				+ wrapField(escape(questionType.toString())));
		table += wrapRow(wrapHeader("Optional Layout Attributes:")
				+ wrapField(escape(optionalLayoutAttributeWithPlaceholders)));

		// Questions
		StringBuffer buffer = new StringBuffer();
		for (val question : questions) {
			buffer.append(question.toTable());
		}

		if (buffer.length() > 0) {
			table += wrapRow(wrapHeader("Questions:")
					+ wrapField(buffer.toString()));
		}

		if (linkedMediaObject != null) {
			val mediaObject = ModelObject.get(MediaObject.class,
					linkedMediaObject);
			if (mediaObject != null) {
				String externalReference;
				if (mediaObject.getFileReference() != null) {
					externalReference = "javascript:showMediaObject('"
							+ mediaObject.getId()
							+ "/"
							+ StringHelpers.cleanFilenameString(mediaObject
									.getName()) + "')";
				} else {
					externalReference = mediaObject.getUrlReference();
				}

				table += wrapRow(wrapHeader("Linked Media Object:")
						+ wrapField(createLink(externalReference,
								mediaObject.getName())));
			} else {
				table += wrapRow(wrapHeader("Linked Media Object:")
						+ wrapField(formatWarning("Media Object set, but not found")));
			}
		}

		table += wrapRow(wrapHeader("Is last slide:")
				+ wrapField(formatYesNo(isLastSlide)));

		if (handsOverToFeedback != null) {
			val feedback = ModelObject.get(Feedback.class, handsOverToFeedback);
			if (feedback != null) {
				table += wrapRow(wrapHeader("Hands over to Feedback:")
						+ wrapField(escape(feedback.getName())));
			} else {
				table += wrapRow(wrapHeader("Hands over to Feedback:")
						+ wrapField(formatWarning("Feedback set, but not found")));
			}
		}

		// Slide Rules
		buffer = new StringBuffer();
		val rules = ModelObject
				.findSorted(
						ScreeningSurveySlideRule.class,
						Queries.SCREENING_SURVEY_SLIDE_RULE__BY_SCREENING_SURVEY_SLIDE,
						Queries.SCREENING_SURVEY_SLIDE_RULE__SORT_BY_ORDER_ASC,
						getId());

		for (val rule : rules) {
			buffer.append(rule.toTable());
		}

		if (buffer.length() > 0) {
			table += wrapRow(wrapHeader("Rules:")
					+ wrapField(buffer.toString()));
		}

		return wrapTable(table);
	}
}
