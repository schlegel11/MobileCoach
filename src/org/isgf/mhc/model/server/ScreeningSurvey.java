package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;
import org.jongo.Oid;

/**
 * {@link ModelObject} to represent an {@link ScreeningSurvey}
 * 
 * Before a {@link Participant} can participate in an {@link Intervention}
 * she/he has to perform one of the {@link ScreeningSurvey}s belonging to an
 * Intervention. In this {@link ScreeningSurvey}, which consist of several
 * {@link ScreeningSurveySlide}s, the basic {@link ParticipantVariableValue}s
 * are collected as well as used to calculate the next
 * {@link ScreeningSurveySlide}s.
 * 
 * @author Andreas Filler
 */
@AllArgsConstructor
public class ScreeningSurvey extends ModelObject {
	/**
	 * The {@link Intervention} the {@link ScreeningSurvey} belongs to
	 */
	@Getter
	@Setter
	private Oid		intervention;

	/**
	 * The name of the {@link ScreeningSurvey} as displayed in the backend
	 */
	@Getter
	@Setter
	private String	name;

	/**
	 * <strong>OPTIONAL:</strong> The password required to participate in the
	 * {@link ScreeningSurvey}
	 */
	@Getter
	@Setter
	private String	password;

	/**
	 * Defines if the {@link ScreeningSurvey} is open for new
	 * {@link Participant}s
	 */
	@Getter
	@Setter
	private boolean	active;
}
