package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;

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
}
