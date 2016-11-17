package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.subelements.LString;
import ch.ethz.mc.model.ui.UIFeedback;
import ch.ethz.mc.model.ui.UIModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	private LString		name;

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
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val feedback = new UIFeedback(name.toString());

		feedback.setRelatedModelObject(this);

		return feedback;
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
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		// Delete slides
		val slidesToDelete = ModelObject.find(FeedbackSlide.class,
				Queries.FEEDBACK_SLIDE__BY_FEEDBACK, getId());
		ModelObject.delete(slidesToDelete);

		// Delete intermediate survey and feedback participant short URLs
		val intermediateSurveysAndFeedbackParticipantShortURLsToDelete = ModelObject
				.find(IntermediateSurveyAndFeedbackParticipantShortURL.class,
						Queries.INTERMEDIATE_SURVEY_AND_FEEDBACK_PARTICIPANT_SHORT_URL__BY_FEEDBACK,
						getId());
		ModelObject
		.delete(intermediateSurveysAndFeedbackParticipantShortURLsToDelete);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.AbstractSerializableTable#toTable()
	 */
	@Override
	@JsonIgnore
	public String toTable() {
		String table = wrapRow(wrapHeader("Name:") + wrapField(escape(name)));
		table += wrapRow(wrapHeader("Template:")
				+ wrapField(escape(templatePath)));

		// Slides
		final StringBuffer buffer = new StringBuffer();
		val slides = ModelObject.findSorted(FeedbackSlide.class,
				Queries.FEEDBACK_SLIDE__BY_FEEDBACK,
				Queries.FEEDBACK_SLIDE__SORT_BY_ORDER_ASC, getId());

		for (val slide : slides) {
			buffer.append(slide.toTable());
		}

		if (buffer.length() > 0) {
			table += wrapRow(wrapHeader("Slides:")
					+ wrapField(buffer.toString()));
		}

		return wrapTable(table);
	}
}
