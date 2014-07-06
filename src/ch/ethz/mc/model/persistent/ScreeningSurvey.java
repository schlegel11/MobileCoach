package ch.ethz.mc.model.persistent;

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
import ch.ethz.mc.model.ui.UIModelObject;
import ch.ethz.mc.model.ui.UIScreeningSurvey;

/**
 * {@link ModelObject} to represent an {@link ScreeningSurvey}
 * 
 * Before a {@link Participant} can participate in an {@link Intervention}
 * she/he has to perform one of the {@link ScreeningSurvey}s belonging to an
 * Intervention. In this {@link ScreeningSurvey}, which consist of several
 * {@link ScreeningSurveySlide}s, the basic {@link ParticipantVariableWithValue}
 * s
 * are collected as well as used to calculate the next
 * {@link ScreeningSurveySlide}s.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningSurvey extends ModelObject {
	/**
	 * A absolutely unique Id to enable to reference a {@link ScreeningSurvey}
	 * also after independent export/import to/from
	 * another system
	 */
	@Getter
	@Setter
	@NonNull
	private String		globalUniqueId;

	/**
	 * The {@link Intervention} the {@link ScreeningSurvey} belongs to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	intervention;

	/**
	 * The name of the {@link ScreeningSurvey} as displayed in the backend
	 */
	@Getter
	@Setter
	@NonNull
	private String		name;

	/**
	 * The path of the template for the {@link ScreeningSurveySlide}s
	 */
	@Getter
	@Setter
	@NonNull
	private String		templatePath;

	/**
	 * <strong>OPTIONAL:</strong> The password required to participate in the
	 * {@link ScreeningSurvey}
	 */
	@Getter
	@Setter
	private String		password;

	/**
	 * Defines if the {@link ScreeningSurvey} is open for new
	 * {@link Participant}s
	 */
	@Getter
	@Setter
	private boolean		active;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		val screeningSurvey = new UIScreeningSurvey(
				name,
				password,
				active,
				active ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__ACTIVE)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__INACTIVE));

		screeningSurvey.setRelatedModelObject(this);

		return screeningSurvey;
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

		// Add screening survey slide
		for (val screeningSurveySlide : ModelObject.find(
				ScreeningSurveySlide.class,
				Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY, getId())) {
			screeningSurveySlide
					.collectThisAndRelatedModelObjectsForExport(exportList);
		}

		// Add feedback
		for (val feedback : ModelObject.find(Feedback.class,
				Queries.FEEDBACK__BY_SCREENING_SURVEY, getId())) {
			feedback.collectThisAndRelatedModelObjectsForExport(exportList);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		// Delete feedback
		val feedbacksToDelete = ModelObject.find(Feedback.class,
				Queries.FEEDBACK__BY_SCREENING_SURVEY, getId());
		ModelObject.delete(feedbacksToDelete);

		// Delete slides
		val slidesToDelete = ModelObject.find(ScreeningSurveySlide.class,
				Queries.SCREENING_SURVEY_SLIDE__BY_SCREENING_SURVEY, getId());
		ModelObject.delete(slidesToDelete);
	}
}