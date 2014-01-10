package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;

/**
 * {@link ModelObject} to represent an {@link Intervention}
 * 
 * An {@link Intervention} describes the whole project consisting of a
 * {@link ScreeningSurvey}, {@link InterventionRule}s and {@link Participant}s.
 * It's the heart of the whole system.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class Intervention extends ModelObject {
	/**
	 * The name of the {@link Intervention} as shown in the backend
	 */
	@Getter
	@Setter
	private String	name;

	/**
	 * Timestamp when the {@link Intervention} has been created
	 */
	@Getter
	@Setter
	private long	created;

	/**
	 * Defines if the whole intervention is active. If this value is false, also
	 * the messaging and the {@link ScreeningSurvey}s of the intervention are
	 * not accessable.
	 */
	@Getter
	@Setter
	private boolean	active;

	/**
	 * Defines if the messaging in this {@link Intervention} is active. If not
	 * the rule execution will not be executed also if the intervention is
	 * active.
	 */
	@Getter
	@Setter
	private boolean	messagingActive;

	/**
	 * The hour of the day the rule execution for all Participants in the
	 * {@link Intervention} starts. It's defined in the 24h time standard, so
	 * e.g. 1 PM would be 13, 8 AM would be 8.
	 */
	@Getter
	@Setter
	private int		hourOfDailyRuleExecutionStart;

	/**
	 * How many seconds does the system wait between the rule exeuction for each
	 * {@link Participant} in an {@link Intervention}
	 */
	@Getter
	@Setter
	private int		secondsDelayBetweenParticipantsRuleExecution;
}
